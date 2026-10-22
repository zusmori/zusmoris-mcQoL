package de.zusmori.mcqol;

import com.mojang.blaze3d.platform.InputConstants;
import de.zusmori.mcqol.elytra.AutoElytra;
import de.zusmori.mcqol.spear.SpearSwap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.sdl.SDLScancode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McQoLClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("mcqol");

	private static KeyMapping toggleSpear;
	private static KeyMapping toggleElytra;

	@Override
	public void onInitializeClient() {
		McQoLConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("mcqol", "main"));
		toggleSpear = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.mcqol.toggle_spear", InputConstants.Type.KEYBOARD, SDLScancode.SDL_SCANCODE_K, category));
		toggleElytra = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.mcqol.toggle_elytra", InputConstants.Type.KEYBOARD, SDLScancode.SDL_SCANCODE_J, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleSpear.consumeClick()) {
				McQoLConfig.spearEnabled = !McQoLConfig.spearEnabled;
				McQoLConfig.save();
				SpearSwap.reset();
				notify(client, "Spear Swap", McQoLConfig.spearEnabled);
			}
			while (toggleElytra.consumeClick()) {
				McQoLConfig.elytraEnabled = !McQoLConfig.elytraEnabled;
				McQoLConfig.save();
				AutoElytra.reset();
				notify(client, "Auto-Elytra", McQoLConfig.elytraEnabled);
			}
			AutoElytra.onTick(client);
		});

		LOGGER.info("zusmori's mcQoL geladen");
	}

	private static void notify(Minecraft client, String feature, boolean on) {
		if (client.player != null) {
			client.player.sendOverlayMessage(on
					? Component.literal(feature + " AN").withStyle(ChatFormatting.GREEN)
					: Component.literal(feature + " AUS").withStyle(ChatFormatting.RED));
		}
	}
}
