package de.zusmori.spearswap.mixin;

import de.zusmori.spearswap.SpearSwapLogic;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	/** Läuft jeden Tick, bevor Vanilla die Linksklicks verarbeitet. */
	@Inject(method = "handleKeybinds", at = @At("HEAD"))
	private void spearswap$beforeKeybinds(CallbackInfo ci) {
		SpearSwapLogic.onKeybinds((Minecraft) (Object) this);
	}

	/** Verhindert, dass man im Lunge-Modus mit der leeren Hand Blöcke abbaut. */
	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void spearswap$blockMining(boolean breaking, CallbackInfo ci) {
		Minecraft mc = (Minecraft) (Object) this;
		if (SpearSwapLogic.isLungeModeActive()) {
			if (mc.gameMode != null) {
				mc.gameMode.stopDestroyBlock();
			}
			ci.cancel();
		}
	}
}
