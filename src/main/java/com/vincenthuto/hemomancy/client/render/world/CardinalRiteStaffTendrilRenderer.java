package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Draws camera-facing translucent ribbons along the planted-staff tendril
 * curves. Alternating crimson and near-black cores make the knot read as
 * several intertwined strands rather than a single glowing helix.
 */
final class CardinalRiteStaffTendrilRenderer {
	private CardinalRiteStaffTendrilRenderer() {
	}

	static void render(PoseStack poseStack, VertexConsumer consumer, BlockPos focus,
			float time, Vec3 camera, float visibilityProgress, boolean glowPass) {
		if (visibilityProgress <= 0.0F) return;
		var strands = CardinalRiteStaffTendrilGeometry.strands(focus, time).stream()
				.map(strand -> new CardinalRiteStaffTendrilGeometry.Strand(strand.index(),
						CardinalRiteStaffTendrilGeometry.visibleJoints(strand.joints(), visibilityProgress)))
				.toList();
		SanguineTendrilRibbonRenderer.render(poseStack, consumer, strands, camera, glowPass);
	}
}
