package com.huto.hemomancy.render.tile;

import com.huto.hemomancy.tile.TileEntityVisceralRecaller;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.DirectionProperty;

public class RenderVisceralRecaller extends TileEntityRenderer<TileEntityVisceralRecaller> {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

	public RenderVisceralRecaller(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TileEntityVisceralRecaller te, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
		/*
		 * IRenderTypeBuffer.Impl impl =
		 * Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		 * matrixStackIn.push(); matrixStackIn.translate(0.5, 1.0, 0.5); ItemStack stack
		 * = new ItemStack(ItemInit.living_blade.get()); IVertexBuilder main =
		 * bufferIn.getBuffer(RenderTypeLookup.func_239219_a_(stack, true));
		 * IVertexBuilder glint = bufferIn.getBuffer(RenderTypeInit.getCrimsonGlint());
		 * IVertexBuilder buffer = VertexBuilderUtils.newDelegate(glint, main);
		 * Minecraft.getInstance().getItemRenderer().renderModel(
		 * Minecraft.getInstance().getItemRenderer().getItemModelWithOverrides(stack,
		 * te.getWorld(), null), stack, 0xF000F0, combinedOverlayIn, matrixStackIn,
		 * buffer); matrixStackIn.pop();
		 * impl.finish(RenderTypeLookup.func_239219_a_(stack, true));
		 */
	}
}
