package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilGeometry;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Draws the strands binding a planted staff to its Focus as HutosLib tendril tubes, in the
 * {@code TENDRIL_GLOW} and {@code TENDRIL_CORE} render types: the coiled spines come from
 * {@link CardinalRiteStaffTendrilGeometry}, and once a strand has fully climbed, its tip frees
 * itself from the staff as a short writhing HutosLib tendril. Crimson and clotted near-black
 * cores alternate so the knot reads as several strands rather than one helix.
 */
final class CardinalRiteStaffTendrilRenderer {
	private static final float GLOW_SCALE = 1.35F;
	private static final float[] CRIMSON_CORE = {0.46F, 0.02F, 0.04F, 0.96F};
	private static final float[] CLOT_CORE = {0.07F, 0.004F, 0.01F, 0.97F};
	private static final float[] CRIMSON_GLOW = {0.72F, 0.04F, 0.08F, 0.30F};
	private static final float[] CLOT_GLOW = {0.30F, 0.01F, 0.03F, 0.24F};
	/** How far a free tip reaches off the staff, and how much it rises as it does. */
	private static final double WHIP_REACH = 0.24D;
	private static final double WHIP_RISE = 0.10D;

	private CardinalRiteStaffTendrilRenderer() {
	}

	static void render(PoseStack poseStack, VertexConsumer consumer, BlockPos focus,
			float time, Vec3 camera, float visibilityProgress, boolean glowPass) {
		if (visibilityProgress <= 0.0F) return;
		Matrix4f matrix = poseStack.last().pose();
		float widthScale = glowPass ? GLOW_SCALE : 1.0F;
		for (CardinalRiteStaffTendrilGeometry.Strand strand : CardinalRiteStaffTendrilGeometry.strands(focus, time)) {
			List<CardinalRiteStaffTendrilGeometry.Joint> joints =
					CardinalRiteStaffTendrilGeometry.visibleJoints(strand.joints(), visibilityProgress);
			if (joints.size() < 2) continue;
			boolean crimson = (strand.index() & 1) == 0;
			float[] color = glowPass ? (crimson ? CRIMSON_GLOW : CLOT_GLOW) : (crimson ? CRIMSON_CORE : CLOT_CORE);
			TendrilGeometry.Strand spine = tube(joints, camera);
			emit(consumer, matrix, TendrilGeometry.createTubeQuads(spine, widthScale).vertices(),
					spine.rings(), joints, color);
			if (visibilityProgress >= 0.999F) {
				emitWhip(consumer, matrix, focus, strand.index(), joints, camera, time, widthScale, color);
			}
		}
	}

	/** The coiled spine as a HutosLib tube, with frames carried along it so the tube never twists. */
	private static TendrilGeometry.Strand tube(List<CardinalRiteStaffTendrilGeometry.Joint> joints, Vec3 camera) {
		List<TendrilGeometry.Ring> rings = new ArrayList<>(joints.size());
		Vec3 right = null;
		int last = joints.size() - 1;
		for (int index = 0; index <= last; index++) {
			Vec3 tangent = joints.get(Math.min(last, index + 1)).center()
					.subtract(joints.get(Math.max(0, index - 1)).center()).normalize();
			if (right == null) {
				right = tangent.cross(new Vec3(0.0D, 1.0D, 0.0D));
				if (right.lengthSqr() < 1.0E-6D) right = tangent.cross(new Vec3(1.0D, 0.0D, 0.0D));
			} else {
				right = right.subtract(tangent.scale(right.dot(tangent)));
			}
			right = right.normalize();
			CardinalRiteStaffTendrilGeometry.Joint joint = joints.get(index);
			rings.add(new TendrilGeometry.Ring(joint.center().subtract(camera), joint.halfWidth(), right,
					tangent.cross(right), index / (float) last));
		}
		return new TendrilGeometry.Strand(rings, false, 0);
	}

	/** The strand's free end: a HutosLib tendril leaving the staff from the last coiled joint. */
	private static void emitWhip(VertexConsumer consumer, Matrix4f matrix, BlockPos focus, int index,
			List<CardinalRiteStaffTendrilGeometry.Joint> joints, Vec3 camera, float time, float widthScale,
			float[] color) {
		CardinalRiteStaffTendrilGeometry.Joint tip = joints.get(joints.size() - 1);
		Vec3 axis = new Vec3(focus.getX() + 0.5D, tip.center().y, focus.getZ() + 0.5D);
		Vec3 outward = tip.center().subtract(axis);
		outward = outward.lengthSqr() < 1.0E-6D ? new Vec3(1.0D, 0.0D, 0.0D) : outward.normalize();
		Vec3 start = tip.center().subtract(camera);
		Vec3 end = start.add(outward.scale(WHIP_REACH)).add(0.0D, WHIP_RISE, 0.0D);
		long seed = focus.asLong() * 31L + index;
		TendrilEffectConfig config = TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withShape(8, 1, tip.halfWidth(), 0.08F)
				.withBranching(1, 1, 0.08F, 0.5F)
				.withWrithe(0.05F, 0.14F, 0.12F, 0.0F)
				.withFixedSeed(true, seed);
		TendrilGeometry whip = TendrilGeometry.generate(start, end, config, seed, time);
		for (TendrilGeometry.Strand strand : whip.strands()) {
			List<Vec3> vertices = TendrilGeometry.createTubeQuads(strand, widthScale).vertices();
			for (Vec3 vertex : vertices) {
				consumer.addVertex(matrix, (float) vertex.x, (float) vertex.y, (float) vertex.z)
						.setColor(color[0], color[1], color[2], color[3] * tip.opacity());
			}
		}
	}

	private static void emit(VertexConsumer consumer, Matrix4f matrix, List<Vec3> vertices,
			List<TendrilGeometry.Ring> rings, List<CardinalRiteStaffTendrilGeometry.Joint> joints, float[] color) {
		// createTubeQuads emits four faces per segment, each ordered from0, from1, to1, to0.
		for (int vertex = 0; vertex < vertices.size(); vertex++) {
			int segment = Math.min(rings.size() - 2, vertex / 16);
			int joint = (vertex & 3) < 2 ? segment : segment + 1;
			float opacity = Mth.clamp(joints.get(joint).opacity() * 1.25F, 0.0F, 1.0F);
			Vec3 point = vertices.get(vertex);
			consumer.addVertex(matrix, (float) point.x, (float) point.y, (float) point.z)
					.setColor(color[0], color[1], color[2], color[3] * opacity);
		}
	}
}
