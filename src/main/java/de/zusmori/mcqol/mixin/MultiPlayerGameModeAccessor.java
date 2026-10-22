package de.zusmori.mcqol.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
	/** Sends the selected hotbar slot to the server right away. */
	@Invoker("ensureHasSentCarriedItem")
	void mcqol$ensureHasSentCarriedItem();
}
