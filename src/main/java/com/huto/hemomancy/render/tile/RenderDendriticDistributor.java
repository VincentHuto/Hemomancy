package com.huto.hemomancy.render.tile;

import com.huto.hemomancy.tile.TileEntityDendriticDistributor;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.state.DirectionProperty;

public class RenderDendriticDistributor extends TileEntityRenderer<TileEntityDendriticDistributor> {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;

	public RenderDendriticDistributor(TileEntityRendererDispatcher rendererDispatcherIn) {
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TileEntityDendriticDistributor te, float partialTicks, MatrixStack matrixStackIn,
			IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
	}
}
