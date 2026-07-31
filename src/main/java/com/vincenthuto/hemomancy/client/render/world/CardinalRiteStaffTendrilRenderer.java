package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Draws camera-facing translucent ribbons along the planted-staff tendril
 * curves. Alternating crimson and near-black cores make the knot read as
 * several intertwined strands rather than a single glowing helix.
 */
final class CardinalRiteStaffTendrilRenderer {
	private static final float GLOW_WIDTH_MULTIPLIER = 2.65F;

	private CardinalRiteStaffTendrilRenderer() {
	}

	static void render(PoseStack poseStack, VertexConsumer consumer, BlockPos focus,
			float time, Vec3 camera, float visibilityProgress, boolean glowPass) {
		if (visibilityProgress <= 0.0F) return;
		Matrix4f matrix = poseStack.last().pose();

		for (CardinalRiteStaffTendrilGeometry.Strand strand
				: CardinalRiteStaffTendrilGeometry.strands(focus, time)) {
			List<CardinalRiteStaffTendrilGeometry.Joint> joints =
					CardinalRiteStaffTendrilGeometry.visibleJoints(
							strand.joints(), visibilityProgress);
			for (int index = 0; index < joints.size() - 1; index++) {
				var first = joints.get(index);
				var second = joints.get(index + 1);
				Vec3 firstCenter = first.center();
				Vec3 secondCenter = second.center();
				Vec3 firstSide = ribbonSide(joints, index, camera);
				Vec3 secondSide = ribbonSide(joints, index + 1, camera);
				float widthMultiplier = glowPass ? GLOW_WIDTH_MULTIPLIER : 1.0F;
				double firstWidth = first.halfWidth() * widthMultiplier;
				double secondWidth = second.halfWidth() * widthMultiplier;

				Vec3 firstLeft = firstCenter.add(firstSide.scale(firstWidth)).subtract(camera);
				Vec3 firstRight = firstCenter.subtract(firstSide.scale(firstWidth)).subtract(camera);
				Vec3 secondLeft = secondCenter.add(secondSide.scale(secondWidth)).subtract(camera);
				Vec3 secondRight = secondCenter.subtract(secondSide.scale(secondWidth)).subtract(camera);
				Color color = color(strand.index(), glowPass);
				float firstAlpha = first.opacity() * color.alpha();
				float secondAlpha = second.opacity() * color.alpha();

				emitQuad(consumer, matrix,
						firstLeft, firstRight, secondRight, secondLeft,
						color.red(), color.green(), color.blue(), firstAlpha, secondAlpha);
			}
		}
	}

	private static Vec3 ribbonSide(List<CardinalRiteStaffTendrilGeometry.Joint> joints, int index,
			Vec3 camera) {
		int previousIndex = Math.max(0, index - 1);
		int nextIndex = Math.min(joints.size() - 1, index + 1);
		Vec3 previous = joints.get(previousIndex).center();
		Vec3 next = joints.get(nextIndex).center();
		Vec3 center = joints.get(index).center();
		Vec3 tangent = next.subtract(previous);
		Vec3 side = tangent.cross(camera.subtract(center));
		if (side.lengthSqr() < 1.0E-7D) {
			side = new Vec3(-tangent.z, 0.0D, tangent.x);
		}
		if (side.lengthSqr() < 1.0E-7D) {
			return new Vec3(1.0D, 0.0D, 0.0D);
		}
		return side.normalize();
	}

	private static Color color(int strandIndex, boolean glowPass) {
		boolean crimsonCore = (strandIndex & 1) == 0;
		if (glowPass) {
			return crimsonCore
					? new Color(0.008F, 0.0F, 0.006F, 0.26F)
					: new Color(0.42F, 0.006F, 0.018F, 0.24F);
		}
		return crimsonCore
				? new Color(0.50F, 0.008F, 0.022F, 0.72F)
				: new Color(0.012F, 0.0F, 0.008F, 0.80F);
	}

	private static void emitQuad(VertexConsumer consumer, Matrix4f matrix,
			Vec3 firstLeft, Vec3 firstRight, Vec3 secondRight, Vec3 secondLeft,
			float red, float green, float blue, float firstAlpha, float secondAlpha) {
		consumer.addVertex(matrix, (float) firstLeft.x, (float) firstLeft.y, (float) firstLeft.z)
				.setColor(red, green, blue, firstAlpha);
		consumer.addVertex(matrix, (float) firstRight.x, (float) firstRight.y, (float) firstRight.z)
				.setColor(red, green, blue, firstAlpha);
		consumer.addVertex(matrix, (float) secondRight.x, (float) secondRight.y, (float) secondRight.z)
				.setColor(red, green, blue, secondAlpha);
		consumer.addVertex(matrix, (float) secondLeft.x, (float) secondLeft.y, (float) secondLeft.z)
				.setColor(red, green, blue, secondAlpha);
	}

	private record Color(float red, float green, float blue, float alpha) {
	}
}
