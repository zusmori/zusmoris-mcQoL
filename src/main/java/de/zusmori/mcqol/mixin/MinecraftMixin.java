package de.zusmori.mcqol.mixin;

import de.zusmori.mcqol.elytra.AutoElytra;
import de.zusmori.mcqol.spear.SpearSwap;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	/** Spear Swap: runs every tick before vanilla handles left clicks. */
	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void mcqol$beforeKeybinds(CallbackInfo ci) {
		SpearSwap.onKeybinds((Minecraft) (Object) this);
	}

	/** Spear Swap: stops mining with the empty hand while in lunge mode. */
	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void mcqol$blockMining(boolean down, CallbackInfo ci) {
		Minecraft mc = (Minecraft) (Object) this;
		if (SpearSwap.isLungeModeActive()) {
			if (mc.gameMode != null) {
				mc.gameMode.stopDestroyBlock();
			}
			ci.cancel();
		}
	}

	/** Auto Elytra: catch rocket right clicks when not flying yet. */
	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void mcqol$beforeUseItem(CallbackInfo ci) {
		if (AutoElytra.onUseItem((Minecraft) (Object) this)) {
			ci.cancel();
		}
	}
}
