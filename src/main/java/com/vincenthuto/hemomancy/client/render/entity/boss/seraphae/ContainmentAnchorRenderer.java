package com.vincenthuto.hemomancy.client.render.entity.boss.seraphae;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.ContainmentAnchorBlock;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.ContainmentAnchorEntity;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * A woken containment anchor draws as the block it woke from, through the block's own generated model, both storeys;
 * its crown light, chain and ripple are the radiance geometry's. A slipped anchor stands snapped: its binding broken.
 */
public class ContainmentAnchorRenderer extends EntityRenderer<ContainmentAnchorEntity> {
	private final BlockRenderDispatcher blocks;

	public ContainmentAnchorRenderer(Context context) {
		super(context);
		this.blocks = context.getBlockRenderDispatcher();
		this.shadowRadius = 0.3F;
	}

	@Override
	public ResourceLocation getTextureLocation(ContainmentAnchorEntity entity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}

	@Override
	public void render(ContainmentAnchorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
			MultiBufferSource buffers, int packedLight) {
		super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
		BlockState state = BlockInit.containment_anchor_block.get().defaultBlockState()
				.setValue(ContainmentAnchorBlock.SNAPPED, !entity.isHeld());
		poseStack.pushPose();
		poseStack.translate(-0.5D, 0.0D, -0.5D);
		blocks.renderSingleBlock(state.setValue(ContainmentAnchorBlock.HALF, DoubleBlockHalf.LOWER), poseStack, buffers,
				packedLight, OverlayTexture.NO_OVERLAY);
		poseStack.translate(0.0D, 1.0D, 0.0D);
		blocks.renderSingleBlock(state.setValue(ContainmentAnchorBlock.HALF, DoubleBlockHalf.UPPER), poseStack, buffers,
				packedLight, OverlayTexture.NO_OVERLAY);
		poseStack.popPose();
	}
}
