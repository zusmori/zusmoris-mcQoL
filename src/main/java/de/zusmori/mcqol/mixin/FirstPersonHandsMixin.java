package de.zusmori.mcqol.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import de.zusmori.mcqol.McQoLConfig;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsMixin {
	/** Totem Size: shrink a totem held in your hand. */
	@Inject(
			method = "submitArmWithItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/item/ItemStackRenderState;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;III)V"
			)
	)
	private void mcqol$smallHandTotem(PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks,
			float xRot, InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
			SubmitNodeCollector collector, int lightCoords, CallbackInfo ci) {
		float scale = McQoLConfig.totemScale;
		if (scale < 1.0F && itemStack.is(Items.TOTEM_OF_UNDYING)) {
			poseStack.scale(scale, scale, scale);
		}
	}
}
