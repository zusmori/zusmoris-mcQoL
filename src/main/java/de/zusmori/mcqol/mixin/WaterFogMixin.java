package de.zusmori.mcqol.mixin;

import de.zusmori.mcqol.McQoLConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.WaterFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WaterFogEnvironment.class)
public abstract class WaterFogMixin {
	/**
	 * Clear Water: the fog ends far away (80% of render distance, at least 64 blocks).
	 * It still starts right in front of you, so everything keeps a light blue tint.
	 */
	@Inject(method = "setupFog", at = @At("TAIL"))
	private void mcqol$clearWater(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (McQoLConfig.clearWater) {
			float end = Math.max(fog.environmentalEnd, Math.max(64.0F, renderDistance * 0.8F));
			fog.environmentalEnd = end;
			fog.skyEnd = end;
			fog.cloudEnd = end;
		}
	}
}
