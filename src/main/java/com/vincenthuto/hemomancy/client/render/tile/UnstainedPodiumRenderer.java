package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.tile.UnstainedPodiumBlockEntity;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class UnstainedPodiumRenderer implements BlockEntityRenderer<UnstainedPodiumBlockEntity> {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public UnstainedPodiumRenderer(BlockEntityRendererProvider.Context p_173636_) {
	}

	@Override
	public void render(UnstainedPodiumBlockEntity te, float partialTicks, PoseStack matrixStackIn,
			MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
	}

}
