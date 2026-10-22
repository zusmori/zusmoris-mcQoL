package de.zusmori.spearswap;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.sdl.SDLScancode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpearSwapClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("spearswap");

	private static KeyMapping toggleKey;

	@Override
	public void onInitializeClient() {
		SpearSwapConfig.load();

		KeyMapping.Category category = KeyMapping.Category.register(
				Identifier.fromNamespaceAndPath("spearswap", "main"));
		toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.spearswap.toggle", InputConstants.Type.KEYBOARD, SDLScancode.SDL_SCANCODE_K, category));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (toggleKey.consumeClick()) {
				SpearSwapConfig.enabled = !SpearSwapConfig.enabled;
				SpearSwapConfig.save();
				SpearSwapLogic.reset();
				if (client.player != null) {
					client.player.sendOverlayMessage(SpearSwapConfig.enabled
							? Component.literal("Spear Swap AN").withStyle(ChatFormatting.GREEN)
							: Component.literal("Spear Swap AUS").withStyle(ChatFormatting.RED));
				}
			}
		});

		LOGGER.info("zusmori's Spear Swap geladen");
	}
}
