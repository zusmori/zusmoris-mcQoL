package de.zusmori.mcqol.mixin;

import de.zusmori.mcqol.McQoLConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.AtmosphericFogEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AtmosphericFogEnvironment.class)
public abstract class AtmosphericFogMixin {
	@Inject(method = "setupFog", at = @At("TAIL"))
	private void mcqol$noFog(FogData fog, Camera camera, ClientLevel level, float renderDistance, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (McQoLConfig.noFog) {
			fog.environmentalStart = 1.0E6F;
			fog.environmentalEnd = 2.0E6F;
		}
	}
}
