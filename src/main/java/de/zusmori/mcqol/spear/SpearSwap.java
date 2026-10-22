package de.zusmori.mcqol.spear;

import de.zusmori.mcqol.McQoLConfig;
import de.zusmori.mcqol.mixin.MinecraftAccessor;
import de.zusmori.mcqol.mixin.MultiPlayerGameModeAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * How it works:
 * 1. Spear in hand + left click: jab with the spear, then switch to an empty hotbar slot
 *    (the "carrier") in the same tick. Lunge mode is now on.
 * 2. In lunge mode every left click (or held left click) does:
 *    select spear -> jab -> back to the carrier, all in the same tick.
 *    The cooldown now follows the carrier (empty hand), not the spear.
 * 3. Picking another slot yourself ends lunge mode.
 */
public final class SpearSwap {
	private static final int HOTBAR_SIZE = 9;

	private static boolean lungeMode = false;
	private static int spearSlot = -1;
	private static int carrierSlot = -1;

	private SpearSwap() {}

	public static boolean isLungeModeActive() {
		return lungeMode && McQoLConfig.spearEnabled;
	}

	public static void reset() {
		lungeMode = false;
		spearSlot = -1;
		carrierSlot = -1;
	}

	public static boolean isSpear(ItemStack stack) {
		return !stack.isEmpty() && (stack.is(ItemTags.SPEARS) || stack.has(DataComponents.PIERCING_WEAPON));
	}

	public static void onKeybinds(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (!McQoLConfig.spearEnabled || player == null || mc.gameMode == null) {
			reset();
			return;
		}
		if (mc.gui.screen() != null || player.isUsingItem()) {
			return;
		}

		Inventory inv = player.getInventory();
		int selected = inv.getSelectedSlot();
		boolean holdingSpear = isSpear(inv.getItem(selected));

		int useSpear;
		int useCarrier;

		if (holdingSpear) {
			// Spear in hand: find a carrier slot, otherwise let vanilla handle it
			useSpear = selected;
			useCarrier = findCarrier(inv, selected);
			if (useCarrier < 0) {
				return;
			}
		} else if (lungeMode) {
			// Lunge mode: only go on if you are still on the carrier and the spear is still there
			if (selected != carrierSlot || !isSpear(inv.getItem(spearSlot))) {
				reset();
				return;
			}
			useSpear = spearSlot;
			useCarrier = carrierSlot;
		} else {
			return;
		}

		KeyMapping attack = mc.options.keyAttack;
		boolean clicked = false;
		while (attack.consumeClick()) {
			clicked = true;
		}

		boolean repeat = lungeMode
				&& !holdingSpear
				&& McQoLConfig.spearAutoRepeat
				&& attack.isDown()
				&& player.getAttackStrengthScale(0.0F) >= 1.0F;

		if (!clicked && !repeat) {
			return;
		}

		doLunge(mc, inv, useSpear, useCarrier);

		spearSlot = useSpear;
		carrierSlot = useCarrier;
		lungeMode = true;
	}

	/** Select spear, jab, back to the carrier. All in the same tick. */
	private static void doLunge(Minecraft mc, Inventory inv, int spear, int carrier) {
		MultiPlayerGameModeAccessor gameMode = (MultiPlayerGameModeAccessor) mc.gameMode;
		MinecraftAccessor client = (MinecraftAccessor) mc;

		inv.setSelectedSlot(spear);
		gameMode.mcqol$ensureHasSentCarriedItem();

		client.mcqol$setMissTime(0);
		client.mcqol$startAttack();

		inv.setSelectedSlot(carrier);
		gameMode.mcqol$ensureHasSentCarriedItem();
	}

	/** First empty hotbar slot, otherwise the fallback slot from the config. */
	private static int findCarrier(Inventory inv, int spear) {
		for (int i = 0; i < HOTBAR_SIZE; i++) {
			if (i != spear && inv.getItem(i).isEmpty()) {
				return i;
			}
		}
		int fallback = McQoLConfig.spearFallbackSlot - 1;
		if (fallback >= 0 && fallback < HOTBAR_SIZE && fallback != spear && !isSpear(inv.getItem(fallback))) {
			return fallback;
		}
		return -1;
	}
}
