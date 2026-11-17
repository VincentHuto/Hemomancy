package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.world.ManipulationMaterials;
import com.vincenthuto.hemomancy.client.render.world.VisceralGeometry;
import com.vincenthuto.hemomancy.client.render.world.VisceralMesh;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.BroodRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.KeptSpecimen;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenBrood;
import com.vincenthuto.hemomancy.common.tile.harbinger.menagerie.SpecimenVessel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * The brood's living thread (core-loop round two, P6): while two kept creatures are paired, the bearer's vessel draws
 * two Animus strands twisting round one another in an arc from its mate's lid to its own, the same living material as
 * the Marionette's tether and a leash. It thickens and brightens as the brood is paid, dims while it waits for Vigor or
 * blood, and a bead of blood runs along it toward the bearer. When a young quickens, its vessel throws out a bloom of
 * blood tendrils that grow and fade over a second and a half. Drawn in the vessel's own frame.
 */
public final class BroodThreadRenderer {
	private static final int SEGMENTS = 24;
	private static final int THREAD = 0xD8364F;
	private static final int BEAD = 0xFF8090;
	private static final int BLOOM = 0xB8243A;
	private static final int BLOOM_ARMS = 9;

	private BroodThreadRenderer() {
	}

	/** The render box a vessel needs so its thread and bloom are not culled with only part of them in view. */
	public static AABB bounds(BlockEntity vessel, AABB own) {
		AABB box = own.inflate(1.5D, 0.0D, 1.5D).expandTowards(0.0D, 1.5D, 0.0D);
		if (!(vessel instanceof SpecimenVessel specimen) || !specimen.kept().broodBearer()) return box;
		BlockPos mate = specimen.kept().broodMate();
		return mate == null ? box : box.minmax(new AABB(mate).expandTowards(0.0D, 3.0D, 0.0D));
	}

	public static void draw(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource buffer) {
		if (!(blockEntity instanceof SpecimenVessel vessel)) return;
		Level level = blockEntity.getLevel();
		if (level == null) return;
		double time = AnimationTime.gameTime(level, (double) partialTick);
		Vec3 base = Vec3.atLowerCornerOf(blockEntity.getBlockPos());
		KeptSpecimen kept = vessel.kept();
		float quickening = kept.quickening(level.getGameTime(), partialTick);
		if (quickening > 0.0F) {
			bloom(poseStack, buffer.getBuffer(ManipulationMaterials.ANIMUS.renderType()),
					SpecimenBrood.glassCentre(vessel).subtract(base), quickening, time);
		}
		if (!kept.broodBearer() || !kept.living()) return;
		SpecimenVessel mate = SpecimenBrood.mateOf(level, vessel);
		if (mate == null) return;
		float progress = (float) BroodRules.progress(kept.broodPaid(), BroodRules.broodCost(kept.maxHealth()));
		boolean fed = BroodRules.gestates(kept.vigor(), mate.kept().vigor());
		Vec3 from = SpecimenBrood.threadAnchor(mate).subtract(base);
		Vec3 to = SpecimenBrood.threadAnchor(vessel).subtract(base);
		float alpha = (fed ? 0.75F : 0.4F) + 0.25F * progress;
		double width = 0.045D + 0.045D * progress;
		VertexConsumer consumer = buffer.getBuffer(ManipulationMaterials.ANIMUS.renderType());
		strand(poseStack, consumer, from, to, width, time * 0.05D, alpha);
		strand(poseStack, consumer, from, to, width * 0.65D, time * 0.05D + Math.PI, alpha * 0.85F);
		if (fed) {
			for (int bead = 0; bead < 2; bead++) {
				double travel = (time * 0.025D + bead * 0.5D) % 1.0D;
				Vec3 at = SpecimenBrood.threadPoint(from, to, travel);
				VisceralMesh.drop(poseStack, consumer, at, 0.045D + 0.03D * progress, 0.07D, BEAD, alpha, time * 0.02D);
			}
		}
	}

	/** One strand of the thread, wound round the arc so the two read as a twisted cord. */
	private static void strand(PoseStack poseStack, VertexConsumer consumer, Vec3 from, Vec3 to, double width, double phase,
			float alpha) {
		Vec3 axis = to.subtract(from);
		Vec3 side = VisceralGeometry.side(axis);
		Vec3 previous = null;
		double previousWidth = 0.0D;
		for (int segment = 0; segment <= SEGMENTS; segment++) {
			double t = segment / (double) SEGMENTS;
			double wind = 0.07D * Math.sin(Math.PI * t);
			Vec3 point = SpecimenBrood.threadPoint(from, to, t)
					.add(side.scale(wind * Math.cos(t * 9.0D + phase)))
					.add(0.0D, wind * Math.sin(t * 9.0D + phase), 0.0D);
			// Thickest in the middle, where the two lives meet.
			double here = width * (0.7D + 0.6D * Math.sin(Math.PI * t));
			if (previous != null) {
				VisceralMesh.segment(poseStack, consumer, previous, point, previousWidth, here, THREAD, alpha,
						(segment - 1) * 0.2D, segment * 0.2D);
			}
			previous = point;
			previousWidth = here;
		}
	}

	/** Blood tendrils thrown out from a quickening vessel's glass, growing as they fade. */
	private static void bloom(PoseStack poseStack, VertexConsumer consumer, Vec3 centre, float quickening, double time) {
		double growth = 1.0D - quickening;
		float alpha = Math.min(1.0F, quickening * 1.6F);
		for (int arm = 0; arm < BLOOM_ARMS; arm++) {
			double angle = arm * Math.PI * 2.0D / BLOOM_ARMS + 0.3D * Math.sin(arm * 1.7D);
			Vec3 out = new Vec3(Math.cos(angle), 0.0D, Math.sin(angle));
			double rise = (arm % 3 - 1) * 0.35D;
			Vec3 start = centre.add(out.scale(0.42D)).add(0.0D, rise, 0.0D);
			Vec3 end = start.add(out.scale(0.25D + 0.95D * growth)).add(0.0D, 0.25D * growth - rise * 0.3D, 0.0D);
			double width = 0.035D + 0.04D * quickening;
			Vec3 previous = start;
			for (int segment = 1; segment <= 8; segment++) {
				double t = segment / 8.0D;
				Vec3 next = VisceralGeometry.strandPoint(start, end, t, 0.12D, arm + time * 0.04D);
				VisceralMesh.segment(poseStack, consumer, previous, next, width * (1.0D - (t - 0.125D) * 0.8D),
						width * (1.0D - t * 0.8D), BLOOM, alpha, (segment - 1) * 0.2D, segment * 0.2D);
				previous = next;
			}
			VisceralMesh.drop(poseStack, consumer, end, 0.05D * quickening + 0.02D, 0.07D, BEAD, alpha, time * 0.02D);
		}
	}
}
