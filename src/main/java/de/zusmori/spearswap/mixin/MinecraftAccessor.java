package de.zusmori.spearswap.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Invoker("startAttack")
	boolean spearswap$startAttack();

	@Accessor("missTime")
	void spearswap$setMissTime(int missTime);
}
