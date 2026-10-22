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
 * Ablauf:
 * 1. Speer in der Hand + Linksklick: Stoß mit dem Speer, im selben Tick Wechsel auf einen
 *    leeren Hotbar-Slot ("Träger"). Ab jetzt ist der Lunge-Modus aktiv.
 * 2. Im Lunge-Modus löst jeder Linksklick (oder gehaltener Linksklick) aus:
 *    Speer auswählen -> zustechen -> zurück auf den Träger, alles im selben Tick.
 *    Der Cooldown richtet sich so nach dem Träger (leere Hand), nicht nach dem Speer.
 * 3. Wählst du selbst einen anderen Slot, endet der Lunge-Modus.
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
			// Speer in der Hand: Träger-Slot suchen, sonst Vanilla machen lassen
			useSpear = selected;
			useCarrier = findCarrier(inv, selected);
			if (useCarrier < 0) {
				return;
			}
		} else if (lungeMode) {
			// Lunge-Modus: nur weitermachen, wenn du noch auf dem Träger bist und der Speer noch da ist
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

	/** Speer auswählen, zustechen, zurück auf den Träger. Alles im selben Tick. */
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

	/** Erster leerer Hotbar-Slot, sonst der Ersatz-Slot aus der Config. */
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
