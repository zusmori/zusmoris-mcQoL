package de.zusmori.mcqol.mixin;

import de.zusmori.mcqol.McQoLConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {
	/** Keine Abbau-Risse: die gesammelten Risse direkt wieder leeren, bevor sie gezeichnet werden. */
	@Inject(method = "extractBlockDestroyAnimation", at = @At("TAIL"))
	private void mcqol$noBreakAnimation(Camera camera, LevelRenderState levelRenderState, CallbackInfo ci) {
		if (McQoLConfig.noBreakAnimation) {
			levelRenderState.blockBreakingRenderStates.clear();
		}
	}
}
