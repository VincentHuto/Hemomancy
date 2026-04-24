package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.data.QliphothBloomClientData;
import com.vincenthuto.hemomancy.client.render.world.QliphothBloomRenderer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.functional.QliphothBloomBlockEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/**
 * Block entity renderer for the Qliphoth Bloom. Delegates to the static
 * drawing helpers in {@link QliphothBloomRenderer} to render the tree
 * geometry (trunk, branches, canopy, roots) relative to the block entity
 * position. Pulse rings are handled in the world-stage renderer.
 */
public class QliphothBloomBlockRenderer implements BlockEntityRenderer<QliphothBloomBlockEntity> {

	public QliphothBloomBlockRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(QliphothBloomBlockEntity be, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {

		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		// Active blooms are rendered in the world-stage renderer (after translucent blocks)
		// to avoid ordering issues against transparent surfaces.
		boolean handledByWorldRenderer = QliphothBloomClientData.containsCenter(be.getBlockPos());
		if (handledByWorldRenderer) {
			return;
		}

		float currentTime = mc.level.getGameTime() + partialTick;

		// Render the tree geometry centered on the block entity position.
		// The PoseStack is already translated to the block entity origin by the
		// vanilla BER pipeline, so we only need a small +0.5/+0.1/+0.5 offset
		// to center the tree on the block.
		QliphothBloomRenderer.renderTree(poseStack, buffer, currentTime, 0.5, 0.1, 0.5, 0);

		// Flush the batches so the render types are committed
		if (buffer instanceof MultiBufferSource.BufferSource bs) {
			bs.endBatch(RenderTypeInit.RITE_BOUNDARY_CORE);
			bs.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
		}
	}

	@Override
	public int getViewDistance() {
		// The tree should be visible from far away
		return 256;
	}
}
