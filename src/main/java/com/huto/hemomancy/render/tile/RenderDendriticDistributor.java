package com.huto.hemomancy.render.tile;

import com.huto.hemomancy.tile.BlockEntityDendriticDistributor;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class RenderDendriticDistributor implements BlockEntityRenderer<BlockEntityDendriticDistributor> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public RenderDendriticDistributor(BlockEntityRendererProvider.Context p_173636_) {
	}
	@Override
	public void render(BlockEntityDendriticDistributor te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
	}
}
