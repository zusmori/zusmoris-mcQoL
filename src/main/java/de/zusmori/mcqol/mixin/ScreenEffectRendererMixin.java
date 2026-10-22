package de.zusmori.mcqol.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.zusmori.mcqol.McQoLConfig;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
	/** Low Fire: Feuer-Overlay nach unten verschieben. */
	@Inject(method = "submitFire", at = @At("HEAD"))
	private static void mcqol$lowerFire(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
		poseStack.pushPose();
		if (McQoLConfig.lowFire) {
			poseStack.translate(0.0F, -McQoLConfig.fireOffset, 0.0F);
		}
	}

	@Inject(method = "submitFire", at = @At("RETURN"))
	private static void mcqol$lowerFireEnd(PoseStack poseStack, SubmitNodeCollector collector, TextureAtlasSprite sprite, CallbackInfo ci) {
		poseStack.popPose();
	}

	/** Kleines Totem: die Größe der Totem-Animation (Vanilla 0.8) verkleinern. */
	@ModifyConstant(method = "renderItemActivationAnimation", constant = @Constant(floatValue = 0.8F))
	private float mcqol$smallTotem(float original) {
		return McQoLConfig.smallTotem ? original * McQoLConfig.totemScale : original;
	}
}
