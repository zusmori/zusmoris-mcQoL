package de.zusmori.mcqol.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.zusmori.mcqol.McQoLConfig;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
	/** Kein Kamerawackeln bei Schaden. Die Todes-Animation bleibt erhalten. */
	@Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
	private void mcqol$noHurtCam(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
		if (McQoLConfig.noHurtCam && !cameraState.entityRenderState.isDeadOrDying) {
			ci.cancel();
		}
	}
}
