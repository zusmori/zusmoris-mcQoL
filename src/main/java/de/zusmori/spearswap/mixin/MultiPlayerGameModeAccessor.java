package de.zusmori.spearswap.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
	/** Schickt den aktuell ausgewählten Hotbar-Slot sofort an den Server. */
	@Invoker("ensureHasSentCarriedItem")
	void spearswap$ensureHasSentCarriedItem();
}
