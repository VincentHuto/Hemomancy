package com.vincenthuto.hemomancy.client.render.tile.unstained.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.tile.unstained.functional.AltarOfCleansingBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class AltarOfCleansingRenderer implements BlockEntityRenderer<AltarOfCleansingBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public AltarOfCleansingRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public boolean shouldRenderOffScreen(AltarOfCleansingBlockEntity te) {
		return true;
	}

	@Override
	public void render(AltarOfCleansingBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
	}
}
