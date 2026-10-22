package de.zusmori.mcqol.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
	@Invoker("startAttack")
	boolean mcqol$startAttack();

	@Accessor("missTime")
	void mcqol$setMissTime(int missTime);

	@Accessor("rightClickDelay")
	void mcqol$setRightClickDelay(int delay);
}
