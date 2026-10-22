package de.zusmori.mcqol.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.zusmori.mcqol.McQoLConfig;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
	/** Low Fire: move the fire overlay down. */
	@Inject(method = "submitFire", at = @At("HEAD"))
	private static void mcqol$lowFireStart(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
		poseStack.pushPose();
		if (McQoLConfig.lowFire) {
			poseStack.translate(0.0F, -McQoLConfig.fireOffset, 0.0F);
		}
	}

	@Inject(method = "submitFire", at = @At("RETURN"))
	private static void mcqol$lowFireEnd(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
		poseStack.popPose();
	}

	/** Small Totem: shrink the totem right before it is drawn. */
	@Inject(
			method = "renderItemActivationAnimation",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
			)
	)
	private void mcqol$smallTotem(PlayerRenderState playerRenderState, PoseStack poseStack, float partialTicks,
			SubmitNodeCollector collector, CallbackInfo ci) {
		if (McQoLConfig.smallTotem) {
			float scale = McQoLConfig.totemScale;
			poseStack.scale(scale, scale, scale);
		}
	}
}
