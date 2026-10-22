package de.zusmori.mcqol.elytra;

import de.zusmori.mcqol.McQoLConfig;
import de.zusmori.mcqol.mixin.MinecraftAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;

/**
 * Auto-Elytra:
 * - Rakete benutzen, ohne zu gleiten: Elytra wird aus Hotbar/Inventar angezogen (Brustplatte kommt
 *   an den Platz der Elytra), du springst ab, fängst an zu gleiten und die Rakete zündet.
 * - Nach der Landung wird die Brustplatte wieder angezogen und die Elytra zurückgelegt.
 *
 * Slot-Nummern im Spielerinventar (InventoryMenu): 6 = Brust, 9-35 = Inventar,
 * 36-44 = Hotbar, 45 = Zweithand.
 */
public final class AutoElytra {
	private static final int CHEST_SLOT = 6;
	private static final int LAUNCH_TIMEOUT_TICKS = 20;
	private static final int LANDED_TICKS_NEEDED = 3;

	/** Wartet darauf, nach dem Absprung zu gleiten und die Rakete zu zünden. */
	private static boolean pendingLaunch = false;
	private static InteractionHand launchHand = InteractionHand.MAIN_HAND;
	private static int launchTicks = 0;

	/** Elytra wurde von der Mod angezogen und soll nach der Landung zurück. */
	private static boolean autoEquipped = false;
	/** Menü-Slot, in dem jetzt die alte Brustplatte liegt (vorher die Elytra). */
	private static int swappedSlot = -1;
	/** War die Brust vorher leer? */
	private static boolean chestWasEmpty = false;
	private static int landedTicks = 0;

	private AutoElytra() {}

	public static void reset() {
		pendingLaunch = false;
		launchTicks = 0;
		landedTicks = 0;
	}

	/** Wird beim Rechtsklick aufgerufen. true = Vanilla-Verhalten abbrechen. */
	public static boolean onUseItem(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (!McQoLConfig.elytraEnabled || player == null || mc.gameMode == null) {
			return false;
		}
		if (pendingLaunch) {
			return true; // Start läuft schon, Rakete nicht doppelt verbrauchen
		}
		if (player.isFallFlying() || player.isPassenger() || player.isInLiquid() || player.getAbilities().flying) {
			return false; // Vanilla: im Flug normal boosten
		}

		InteractionHand hand;
		if (player.getMainHandItem().is(Items.FIREWORK_ROCKET)) {
			hand = InteractionHand.MAIN_HAND;
		} else if (player.getOffhandItem().is(Items.FIREWORK_ROCKET)) {
			hand = InteractionHand.OFF_HAND;
		} else {
			return false;
		}

		boolean onGround = player.onGround();
		if (onGround && !McQoLConfig.elytraLaunchFromGround) {
			return false;
		}

		// Elytra anziehen, falls noch nicht an
		if (!LivingEntity.canGlideUsing(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST)) {
			if (!equipElytra(mc, player)) {
				return false; // keine Elytra gefunden -> Vanilla
			}
		}

		pendingLaunch = true;
		launchHand = hand;
		launchTicks = 0;
		((MinecraftAccessor) mc).mcqol$setRightClickDelay(4);

		if (onGround) {
			player.jumpFromGround();
		} else {
			tryLaunch(mc, player); // in der Luft: sofort losfliegen
		}
		return true;
	}

	/** Läuft am Ende jedes Client-Ticks. */
	public static void onTick(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (player == null || mc.gameMode == null) {
			pendingLaunch = false;
			autoEquipped = false;
			return;
		}

		if (pendingLaunch) {
			launchTicks++;
			if (!player.onGround()) {
				tryLaunch(mc, player);
			}
			if (launchTicks > LAUNCH_TIMEOUT_TICKS) {
				pendingLaunch = false;
			}
		}

		if (autoEquipped && !pendingLaunch && McQoLConfig.elytraRestoreChestplate) {
			boolean landed = !player.isFallFlying() && (player.onGround() || player.isInLiquid());
			landedTicks = landed ? landedTicks + 1 : 0;
			if (landedTicks >= LANDED_TICKS_NEEDED) {
				restoreChestplate(mc, player);
				landedTicks = 0;
			}
		}
	}

	private static void tryLaunch(Minecraft mc, LocalPlayer player) {
		if (!player.isFallFlying() && player.tryToStartFallFlying()) {
			player.connection.send(new ServerboundPlayerCommandPacket(player, ServerboundPlayerCommandPacket.Action.START_FALL_FLYING));
		}
		if (player.isFallFlying()) {
			if (player.getItemInHand(launchHand).is(Items.FIREWORK_ROCKET)) {
				mc.gameMode.useItem(player, launchHand);
			}
			pendingLaunch = false;
		}
	}

	/** Sucht eine Elytra im Inventar und tauscht sie mit dem Brust-Slot. */
	private static boolean equipElytra(Minecraft mc, LocalPlayer player) {
		if (!canClickInventory(player)) {
			return false;
		}
		InventoryMenu menu = player.inventoryMenu;
		int elytraSlot = -1;
		for (int i = InventoryMenu.INV_SLOT_START; i <= InventoryMenu.SHIELD_SLOT; i++) {
			if (LivingEntity.canGlideUsing(menu.getSlot(i).getItem(), EquipmentSlot.CHEST)) {
				elytraSlot = i;
				break;
			}
		}
		if (elytraSlot < 0) {
			return false;
		}

		chestWasEmpty = menu.getSlot(CHEST_SLOT).getItem().isEmpty();
		swapWithChest(mc, player, elytraSlot);

		if (!LivingEntity.canGlideUsing(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST)) {
			return false; // z. B. Fluch der Bindung
		}
		autoEquipped = true;
		swappedSlot = elytraSlot;
		landedTicks = 0;
		return true;
	}

	/** Nach der Landung: Brustplatte wieder an, Elytra an ihren alten Platz. */
	private static void restoreChestplate(Minecraft mc, LocalPlayer player) {
		if (!canClickInventory(player)) {
			return; // später nochmal versuchen
		}
		InventoryMenu menu = player.inventoryMenu;
		autoEquipped = false;

		// Trägt man inzwischen gar keine Elytra mehr, nichts anfassen
		if (!menu.getSlot(CHEST_SLOT).getItem().has(DataComponents.GLIDER)) {
			return;
		}

		int target = -1;
		ItemStack inSwapped = swappedSlot >= 0 ? menu.getSlot(swappedSlot).getItem() : ItemStack.EMPTY;
		if (chestWasEmpty ? inSwapped.isEmpty() : isChestArmor(inSwapped)) {
			target = swappedSlot;
		} else {
			// Brustplatte wurde verschoben: irgendeine Brustplatte im Inventar suchen
			for (int i = InventoryMenu.INV_SLOT_START; i < InventoryMenu.SHIELD_SLOT; i++) {
				if (isChestArmor(menu.getSlot(i).getItem())) {
					target = i;
					break;
				}
			}
		}
		if (target >= 0) {
			swapWithChest(mc, player, target);
		}
	}

	private static void swapWithChest(Minecraft mc, LocalPlayer player, int slot) {
		int id = player.inventoryMenu.containerId;
		boolean slotEmpty = player.inventoryMenu.getSlot(slot).getItem().isEmpty();
		if (slotEmpty) {
			// Brust-Item in den leeren Slot legen
			mc.gameMode.handleContainerInput(id, CHEST_SLOT, 0, ContainerInput.PICKUP, player);
			mc.gameMode.handleContainerInput(id, slot, 0, ContainerInput.PICKUP, player);
		} else {
			mc.gameMode.handleContainerInput(id, slot, 0, ContainerInput.PICKUP, player);
			mc.gameMode.handleContainerInput(id, CHEST_SLOT, 0, ContainerInput.PICKUP, player);
			mc.gameMode.handleContainerInput(id, slot, 0, ContainerInput.PICKUP, player);
		}
	}

	private static boolean canClickInventory(LocalPlayer player) {
		return player.containerMenu == player.inventoryMenu && player.inventoryMenu.getCarried().isEmpty();
	}

	private static boolean isChestArmor(ItemStack stack) {
		if (stack.isEmpty() || stack.has(DataComponents.GLIDER)) {
			return false;
		}
		Equippable eq = stack.get(DataComponents.EQUIPPABLE);
		return eq != null && eq.slot() == EquipmentSlot.CHEST;
	}
}
