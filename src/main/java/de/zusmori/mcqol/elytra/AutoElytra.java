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
 * Auto Elytra:
 * - Use a rocket while not flying: the elytra is put on from your hotbar/inventory (the chestplate
 *   goes where the elytra was), you jump, start flying and the rocket fires.
 * - After landing the chestplate goes back on and the elytra goes back to its slot.
 *
 * Slot numbers in the player inventory (InventoryMenu): 6 = chest, 9-35 = inventory,
 * 36-44 = hotbar, 45 = offhand.
 */
public final class AutoElytra {
	private static final int CHEST_SLOT = 6;
	private static final int LAUNCH_TIMEOUT_TICKS = 20;
	private static final int LANDED_TICKS_NEEDED = 3;

	/** Waiting to start flying after the jump and fire the rocket. */
	private static boolean pendingLaunch = false;
	private static InteractionHand launchHand = InteractionHand.MAIN_HAND;
	private static int launchTicks = 0;

	/** The mod put the elytra on and should swap back after landing. */
	private static boolean autoEquipped = false;
	/** Menu slot that now holds the old chestplate (where the elytra was). */
	private static int swappedSlot = -1;
	/** Was the chest slot empty before? */
	private static boolean chestWasEmpty = false;
	private static int landedTicks = 0;

	private AutoElytra() {}

	public static void reset() {
		pendingLaunch = false;
		launchTicks = 0;
		landedTicks = 0;
	}

	/** Called on right click. true = cancel vanilla. */
	public static boolean onUseItem(Minecraft mc) {
		LocalPlayer player = mc.player;
		if (!McQoLConfig.elytraEnabled || player == null || mc.gameMode == null) {
			return false;
		}
		if (pendingLaunch) {
			return true; // launch already running, don't use a second rocket
		}
		if (player.isFallFlying() || player.isPassenger() || player.isInLiquid() || player.getAbilities().flying) {
			return false; // vanilla: normal boost while flying
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

		// put on the elytra if it's not on yet
		if (!LivingEntity.canGlideUsing(player.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST)) {
			if (!equipElytra(mc, player)) {
				return false; // no elytra found -> vanilla
			}
		}

		pendingLaunch = true;
		launchHand = hand;
		launchTicks = 0;
		((MinecraftAccessor) mc).mcqol$setRightClickDelay(4);

		if (onGround) {
			player.jumpFromGround();
		} else {
			tryLaunch(mc, player); // in the air: start flying right away
		}
		return true;
	}

	/** Runs at the end of every client tick. */
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

	/** Finds an elytra in the inventory and swaps it with the chest slot. */
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
			return false; // e.g. Curse of Binding
		}
		autoEquipped = true;
		swappedSlot = elytraSlot;
		landedTicks = 0;
		return true;
	}

	/** After landing: chestplate back on, elytra back to its old slot. */
	private static void restoreChestplate(Minecraft mc, LocalPlayer player) {
		if (!canClickInventory(player)) {
			return; // try again later
		}
		InventoryMenu menu = player.inventoryMenu;
		autoEquipped = false;

		// not wearing an elytra anymore: don't touch anything
		if (!menu.getSlot(CHEST_SLOT).getItem().has(DataComponents.GLIDER)) {
			return;
		}

		int target = -1;
		ItemStack inSwapped = swappedSlot >= 0 ? menu.getSlot(swappedSlot).getItem() : ItemStack.EMPTY;
		if (chestWasEmpty ? inSwapped.isEmpty() : isChestArmor(inSwapped)) {
			target = swappedSlot;
		} else {
			// chestplate was moved: look for any chestplate in the inventory
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
			// move the chest item into the empty slot
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
