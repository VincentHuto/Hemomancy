package com.huto.hemomancy.render.tile;

import com.huto.hemomancy.tile.BlockEntityUnstainedPodium;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class RenderUnstainedPodium implements BlockEntityRenderer<BlockEntityUnstainedPodium> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public RenderUnstainedPodium(BlockEntityRenderDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(BlockEntityUnstainedPodium te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
		/*
		 * IRenderTypeBuffer.Impl impl =
		 * Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		 * matrixStackIn.push(); matrixStackIn.translate(0.5, 1.0, 0.5); ItemStack stack
		 * = new ItemStack(ItemInit.living_blade.get()); IVertexBuilder main =
		 * bufferIn.getBuffer(RenderTypeLookup.getRenderType(stack, true));
		 * IVertexBuilder glint = bufferIn.getBuffer(RenderTypeInit.getCrimsonGlint());
		 * IVertexBuilder buffer = VertexBuilderUtils.newDelegate(glint, main);
		 * Minecraft.getInstance().getItemRenderer().renderModel(
		 * Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(stack,
		 * te.getLevel(), null), stack, 0xF000F0, combinedOverlayIn, matrixStackIn,
		 * buffer); matrixStackIn.pop();
		 * impl.finish(RenderTypeLookup.getRenderType(stack, true));
		 */
	}
}
