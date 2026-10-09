package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.summon.MnemonistPuppetModel;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import com.vincenthuto.hutoslib.client.HLRenderTypeInit;
import com.vincenthuto.hutoslib.client.particle.data.TendrilGeometry;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Three to five red memory threads paying out of the puppet's spool. Each thread is a live spine that
 * integrates travelling curvature waves from root to tip, so the strands writhe rather than sway; they
 * trail while it walks, lash over its head when it strikes, spasm when hurt and go slack as it dies.
 */
final class MnemonistThreadLayer extends RenderLayer<MnemonistPuppetEntity, MnemonistPuppetModel> {
	private static final ResourceLocation WHITE = ResourceLocation.withDefaultNamespace("textures/misc/white.png");
	private static final int SEGMENTS = 18;
	private static final float PIXEL = 1.0F / 16.0F;
	// Body-space centre of the spool's wound core, where every thread pays out.
	private static final float SPOOL_Y = 3.5F;
	private static final float SPOOL_Z = 3.5F;
	private static final float SPOOL_RADIUS = 1.5F;
	private static final float ROOT_RADIUS = 0.36F * PIXEL;
	private static final float TIP_RADIUS = 0.07F * PIXEL;
	private static final float GLOW_SCALE = 2.6F;

	MnemonistThreadLayer(RenderLayerParent<MnemonistPuppetEntity, MnemonistPuppetModel> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
			MnemonistPuppetEntity entity, float limbSwing, float limbSwingAmount, float partialTick,
			float ageInTicks, float netHeadYaw, float headPitch) {
		if (entity.isInvisible()) return;
		long seed = entity.getUUID().getMostSignificantBits() ^ entity.getUUID().getLeastSignificantBits();
		int count = 3 + Math.floorMod(seed, 3);
		Motion motion = Motion.of(entity, partialTick, limbSwingAmount);

		poseStack.pushPose();
		getParentModel().body.translateAndRotate(poseStack);
		PoseStack.Pose pose = poseStack.last();
		VertexConsumer core = buffer.getBuffer(RenderType.entityCutoutNoCull(WHITE));
		int overlay = LivingEntityRenderer.getOverlayCoords(entity, 0.0F);
		List<TendrilGeometry.Strand> strands = new ArrayList<>(count);
		for (int index = 0; index < count; index++) {
			TendrilGeometry.Strand strand = strand(seed, index, count, ageInTicks, motion);
			strands.add(strand);
			renderCore(pose, core, strand, packedLight, overlay);
		}
		VertexConsumer glow = buffer.getBuffer(HLRenderTypeInit.TENDRIL_GLOW);
		for (int index = 0; index < count; index++) {
			renderGlow(pose, glow, strands.get(index), ageInTicks + index * 9.0F, motion.alive());
		}
		poseStack.popPose();
	}

	private record Motion(float trail, float lash, float spasm, float slack) {
		static Motion of(MnemonistPuppetEntity entity, float partialTick, float limbSwingAmount) {
			float lash = Mth.sin(entity.getAttackAnim(partialTick) * Mth.PI);
			float spasm = entity.hurtTime > 0 ? (entity.hurtTime - partialTick) / Math.max(1, entity.hurtDuration) : 0.0F;
			float slack = entity.deathTime > 0 ? Math.min(1.0F, (entity.deathTime + partialTick) / 20.0F) : 0.0F;
			return new Motion(Math.min(1.0F, limbSwingAmount), lash, Math.max(0.0F, spasm), slack);
		}

		float alive() {
			return 1.0F - slack;
		}
	}

	private static TendrilGeometry.Strand strand(long seed, int index, int count, float time, Motion motion) {
		RandomSource random = RandomSource.create(seed + index * 0x9E3779B97F4A7C15L);
		float fan = count == 1 ? 0.0F : index / (count - 1.0F) * 2.0F - 1.0F;
		fan += (random.nextFloat() - 0.5F) * 0.3F;
		float length = (20.0F + random.nextFloat() * 10.0F) * PIXEL;
		float pitchWave = 2.6F + random.nextFloat() * 1.2F;
		float yawWave = 2.0F + random.nextFloat() * 1.2F;
		float phaseA = random.nextFloat() * Mth.TWO_PI;
		float phaseB = random.nextFloat() * Mth.TWO_PI;
		float speed = 0.11F + random.nextFloat() * 0.06F;

		// Roots fan across the top of the wound core; the threads leave up and back over the shoulders.
		float rootAngle = fan * 1.2F;
		Vec3 point = new Vec3(Mth.sin(rootAngle) * SPOOL_RADIUS, SPOOL_Y - Mth.cos(rootAngle) * SPOOL_RADIUS, SPOOL_Z)
				.scale(PIXEL);
		float pitch = 1.15F + random.nextFloat() * 0.3F - motion.trail() * 0.55F + motion.lash() * 0.45F;
		float yaw = fan + (random.nextFloat() - 0.5F) * 0.25F;
		float alive = motion.alive();
		float wave = alive * (1.0F + motion.lash() * 0.8F);
		float droop = 0.4F + motion.trail() * 0.45F + motion.slack() * 2.7F;
		float flow = time * speed + motion.lash() * 2.0F;

		Vec3[] centers = new Vec3[SEGMENTS + 1];
		centers[0] = point;
		for (int segment = 0; segment < SEGMENTS; segment++) {
			float s = (segment + 0.5F) / SEGMENTS;
			// Curvature grows toward the tip, so the root stays anchored while the end whips.
			float envelope = 0.35F + 1.3F * s;
			float bendPitch = Mth.sin(Mth.TWO_PI * 1.3F * s - flow + phaseA)
					+ 0.45F * Mth.sin(Mth.TWO_PI * 2.1F * s + time * speed * 0.7F + phaseB);
			float bendYaw = Mth.sin(Mth.TWO_PI * 1.1F * s - flow * 0.8F + phaseB)
					+ 0.4F * Mth.cos(Mth.TWO_PI * 2.4F * s + time * speed * 0.55F + phaseA);
			float jitter = motion.spasm() * 2.5F * Mth.sin(Mth.TWO_PI * 3.5F * s - time * 0.9F + phaseA);
			// A strike lifts the roots first, then curls the tips forward over the head.
			float whip = motion.lash() * 3.2F * s;
			pitch += (envelope * wave * (pitchWave * bendPitch + jitter) - droop + whip) / SEGMENTS;
			yaw += envelope * wave * (yawWave * bendYaw + jitter) / SEGMENTS;
			// +Y is down and +Z is the puppet's back in body space.
			float horizontal = Mth.cos(pitch);
			Vec3 direction = new Vec3(Mth.sin(yaw) * horizontal, -Mth.sin(pitch), Mth.cos(yaw) * horizontal);
			point = point.add(direction.scale(length / SEGMENTS));
			centers[segment + 1] = point;
		}
		return new TendrilGeometry.Strand(rings(centers), false, 0);
	}

	/** Parallel-transported frames keep the tube from twisting as the spine curls. */
	private static List<TendrilGeometry.Ring> rings(Vec3[] centers) {
		List<TendrilGeometry.Ring> rings = new ArrayList<>(centers.length);
		Vec3 right = null;
		for (int index = 0; index < centers.length; index++) {
			Vec3 tangent = centers[Math.min(centers.length - 1, index + 1)]
					.subtract(centers[Math.max(0, index - 1)]).normalize();
			if (right == null) {
				right = tangent.cross(new Vec3(0, 1, 0));
				if (right.lengthSqr() < 1.0E-6D) right = tangent.cross(new Vec3(1, 0, 0));
			} else {
				right = right.subtract(tangent.scale(right.dot(tangent)));
			}
			right = right.normalize();
			Vec3 up = tangent.cross(right);
			float progress = index / (float) SEGMENTS;
			rings.add(new TendrilGeometry.Ring(centers[index], Mth.lerp(progress, ROOT_RADIUS, TIP_RADIUS),
					right, up, progress));
		}
		return rings;
	}

	private static void renderCore(PoseStack.Pose pose, VertexConsumer consumer, TendrilGeometry.Strand strand,
			int light, int overlay) {
		List<TendrilGeometry.Ring> rings = strand.rings();
		List<Vec3> vertices = TendrilGeometry.createTubeQuads(strand, 1.0F).vertices();
		for (int quad = 0; quad * 4 < vertices.size(); quad++) {
			// createTubeQuads emits four faces per segment, ordered from0, from1, to1, to0.
			int segment = quad / 4;
			TendrilGeometry.Ring from = rings.get(segment);
			TendrilGeometry.Ring to = rings.get(segment + 1);
			Vec3 axis = from.center().add(to.center()).scale(0.5D);
			Vec3 middle = Vec3.ZERO;
			for (int corner = 0; corner < 4; corner++) middle = middle.add(vertices.get(quad * 4 + corner));
			Vec3 normal = middle.scale(0.25D).subtract(axis).normalize();
			for (int corner = 0; corner < 4; corner++) {
				Vec3 vertex = vertices.get(quad * 4 + corner);
				float progress = corner < 2 ? from.progress() : to.progress();
				// Old dried thread at the spool, fresh crimson toward the waking tip.
				consumer.addVertex(pose, (float) vertex.x, (float) vertex.y, (float) vertex.z)
						.setColor(Mth.lerp(progress, 0.36F, 0.80F), Mth.lerp(progress, 0.05F, 0.07F),
								Mth.lerp(progress, 0.07F, 0.10F), 1.0F)
						.setUv(0.5F, 0.5F)
						.setOverlay(overlay)
						.setLight(light)
						.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
			}
		}
	}

	private static void renderGlow(PoseStack.Pose pose, VertexConsumer consumer, TendrilGeometry.Strand strand,
			float time, float alive) {
		if (alive <= 0.0F) return;
		List<TendrilGeometry.Ring> rings = strand.rings();
		List<Vec3> vertices = TendrilGeometry.createTubeQuads(strand, GLOW_SCALE).vertices();
		for (int index = 0; index < vertices.size(); index++) {
			int segment = index / 16;
			float progress = (index & 3) < 2 ? rings.get(segment).progress() : rings.get(segment + 1).progress();
			// A slow pulse of remembered blood travels outward along each thread.
			float pulse = 0.5F + 0.5F * Mth.sin(time * 0.12F - progress * 7.0F);
			float alpha = alive * (0.08F + 0.16F * pulse * pulse) * (1.0F - progress * 0.4F);
			Vec3 vertex = vertices.get(index);
			consumer.addVertex(pose, (float) vertex.x, (float) vertex.y, (float) vertex.z)
					.setColor(1.0F, 0.08F, 0.10F, alpha);
		}
	}
}
