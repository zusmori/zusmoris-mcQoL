package de.zusmori.mcqol.mixin;

import de.zusmori.mcqol.McQoLConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
	/** Kein Nebel: auch den Nebel am Rand der Sichtweite entfernen. */
	@Inject(method = "setupFog", at = @At("RETURN"))
	private void mcqol$noDistanceFog(Camera camera, int renderDistanceInChunks, DeltaTracker deltaTracker, float darkenWorldAmount,
			ClientLevel level, CallbackInfoReturnable<FogData> cir) {
		if (McQoLConfig.noFog) {
			FogData fog = cir.getReturnValue();
			fog.renderDistanceStart = 1.0E6F;
			fog.renderDistanceEnd = 2.0E6F;
		}
	}
}
