package net.minecraft.world.level.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Access widened by fabric-transitive-access-wideners-v1 to accessible
 */
public class WoolCarpetBlock extends CarpetBlock {
	private final DyeColor color;

	/**
	 * Access widened by fabric-transitive-access-wideners-v1 to accessible
	 */
	public WoolCarpetBlock(final DyeColor color, final BlockBehaviour.Properties properties) {
		super(properties);
		this.color = color;
	}

	public DyeColor getColor() {
		return this.color;
	}
}
