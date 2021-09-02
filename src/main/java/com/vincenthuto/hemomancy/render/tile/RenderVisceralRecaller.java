package com.vincenthuto.hemomancy.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.tile.BlockEntityVisceralRecaller;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class RenderVisceralRecaller implements BlockEntityRenderer<BlockEntityVisceralRecaller> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public RenderVisceralRecaller(BlockEntityRendererProvider.Context p_173636_) {
	}

	@Override
	public void render(BlockEntityVisceralRecaller te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		/*
		 * IRenderTypeBuffer.Impl impl =
		 * Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		 * matrixStackIn.push(); matrixStackIn.translate(0.5, 1.0, 0.5); ItemStack stack
		 * = new ItemStack(ItemInit.living_blade.get()); VertexConsumer main =
		 * bufferIn.getBuffer(RenderTypeLookup.getRenderType(stack, true));
		 * VertexConsumer glint = bufferIn.getBuffer(RenderTypeInit.getCrimsonGlint());
		 * VertexConsumer buffer = VertexBuilderUtils.newDelegate(glint, main);
		 * Minecraft.getInstance().getItemRenderer().renderModel(
		 * Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(stack,
		 * te.getLevel(), null), stack, 0xF000F0, combinedOverlayIn, matrixStackIn,
		 * buffer); matrixStackIn.pop();
		 * impl.finish(RenderTypeLookup.getRenderType(stack, true));
		 */
	}
}
