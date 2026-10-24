package com.vincenthuto.hemomancy.client.render.world.zone;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.render.RenderTypeInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.zone.RiteZoneKind;
import com.vincenthuto.hemomancy.common.network.rite.RiteZoneSyncPacket;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Draws the edge of each nearby Harbinger rite zone on the ground, loosely
 * following the terrain. Sanguine Dominion is a slow dark vein with a
 * travelling heartbeat and the odd rising blood cell; Sanguine Fervor is a
 * low, breathing ember haze. Only edge stretches within
 * {@link RiteZoneBoundaryRules#VIEW_DISTANCE} of the camera are drawn, fading out
 * towards that distance.
 */
public final class RiteZoneBoundaryRenderer {
	private static final float GROUND_LIFT = 0.04f;

	private static final float VEIN_CORE_HALF = 0.07f;
	private static final float VEIN_GLOW_HALF = 0.32f;
	private static final ParticleColor BLOOD_CELL = new ParticleColor(112, 8, 14);

	private static final float HAZE_HEIGHT = 1.4f;
	private static final float EMBER_LINE_HALF = 0.05f;
	private static final ParticleColor EMBER = new ParticleColor(255, 112, 36);

	private static long lastParticleTick = Long.MIN_VALUE;

	private RiteZoneBoundaryRenderer() {}

	public static void render(PoseStack poseStack, float partialTick) {
		List<RiteZoneSyncPacket.Zone> zones = RiteZoneClientData.zones();
		if (zones.isEmpty()) return;
		Minecraft mc = Minecraft.getInstance();
		ClientLevel level = mc.level;
		if (level == null || mc.player == null) return;

		Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
		float time = AnimationTime.gameTime(level, partialTick);
		boolean particleTick = level.getGameTime() != lastParticleTick
				&& mc.options.particles().get() != ParticleStatus.MINIMAL;
		if (particleTick) lastParticleTick = level.getGameTime();

		MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();
		boolean drew = false;
		Matrix4f mat = poseStack.last().pose();
		for (RiteZoneSyncPacket.Zone zone : zones) {
			if (zone.kind() != RiteZoneKind.SANGUINE_DOMINION && zone.kind() != RiteZoneKind.SANGUINE_FERVOR) continue;
			BlockPos center = zone.center();
			int r = zone.blockRadius();
			double minX = center.getX() - r;
			double minZ = center.getZ() - r;
			double maxX = center.getX() + r + 1;
			double maxZ = center.getZ() + r + 1;
			if (RiteZoneBoundaryRules.distanceToPerimeter(cam.x, cam.z, minX, minZ, maxX, maxZ)
					>= RiteZoneBoundaryRules.VIEW_DISTANCE) continue;

			VertexConsumer vc = buffer.getBuffer(RenderTypeInit.RITE_BOUNDARY_GLOW);
			EdgeSampler sampler = new EdgeSampler(level, zone.kind(), cam, time, center.asLong(), particleTick);
			// West and east faces run along Z; north and south faces run along X.
			sampler.edge(vc, mat, false, minX, minZ, maxZ, -1);
			sampler.edge(vc, mat, false, maxX, minZ, maxZ, 1);
			sampler.edge(vc, mat, true, minZ, minX, maxX, -1);
			sampler.edge(vc, mat, true, maxZ, minX, maxX, 1);
			if (particleTick) sampler.emitParticle();
			drew = true;
		}
		if (drew) buffer.endBatch(RenderTypeInit.RITE_BOUNDARY_GLOW);
	}

	/** Walks one zone's edges, drawing each in-range stretch and remembering one spot for a particle. */
	private static final class EdgeSampler {
		private final ClientLevel level;
		private final RiteZoneKind kind;
		private final Vec3 cam;
		private final float time;
		private final double seed;
		private final boolean wantsParticle;
		private int candidates;
		private double particleX;
		private double particleY;
		private double particleZ;

		EdgeSampler(ClientLevel level, RiteZoneKind kind, Vec3 cam, float time, long seed, boolean wantsParticle) {
			this.level = level;
			this.kind = kind;
			this.cam = cam;
			this.time = time;
			this.seed = (seed % 997L) * 0.37;
			this.wantsParticle = wantsParticle;
		}

		/**
		 * @param alongX  true when the edge runs along X (a north or south face)
		 * @param fixed   the edge's coordinate on the other axis
		 * @param outward -1 or 1: which side of the edge lies outside the zone
		 */
		void edge(VertexConsumer vc, Matrix4f mat, boolean alongX, double fixed, double min, double max,
				int outward) {
			double camAlong = alongX ? cam.x : cam.z;
			double camAcross = alongX ? cam.z : cam.x;
			double[] span = RiteZoneBoundaryRules.visibleSpan(fixed, min, max, camAlong, camAcross,
					RiteZoneBoundaryRules.VIEW_DISTANCE);
			if (span == null) return;

			double prevAlong = span[0];
			double prevY = groundY(alongX, fixed, prevAlong, outward);
			for (double along = span[0] + RiteZoneBoundaryRules.STEP; along <= span[1] + 1.0e-6;
					along += RiteZoneBoundaryRules.STEP) {
				double y = groundY(alongX, fixed, along, outward);
				if (!Double.isNaN(prevY) && !Double.isNaN(y)) {
					segment(vc, mat, alongX, fixed, prevAlong, prevY, along, y, outward);
				}
				prevAlong = along;
				prevY = y;
			}
		}

		private void segment(VertexConsumer vc, Matrix4f mat, boolean alongX, double fixed,
				double a0, double y0, double a1, double y1, int outward) {
			double mid = (a0 + a1) * 0.5;
			double across = alongX ? cam.z - fixed : cam.x - fixed;
			double camAlong = alongX ? cam.x : cam.z;
			double distance = Math.sqrt(across * across + (mid - camAlong) * (mid - camAlong));
			float fade = RiteZoneBoundaryRules.fade(distance, RiteZoneBoundaryRules.VIEW_DISTANCE);
			if (fade < 0.02f) return;

			if (kind == RiteZoneKind.SANGUINE_DOMINION) {
				drawVein(vc, mat, alongX, fixed, a0, y0, a1, y1, fade);
			} else {
				drawHaze(vc, mat, alongX, fixed, a0, y0, a1, y1, fade);
			}
			if (wantsParticle && distance < RiteZoneBoundaryRules.VIEW_DISTANCE * 0.7
					&& level.random.nextInt(++candidates) == 0) {
				particleX = alongX ? mid : fixed + outward * 0.1;
				particleZ = alongX ? fixed + outward * 0.1 : mid;
				particleY = (y0 + y1) * 0.5 + 0.1;
			}
		}

		private void drawVein(VertexConsumer vc, Matrix4f mat, boolean alongX, double fixed,
				double a0, double y0, double a1, double y1, float fade) {
			double m0 = fixed + RiteZoneBoundaryRules.veinMeander(a0, seed);
			double m1 = fixed + RiteZoneBoundaryRules.veinMeander(a1, seed);
			float p0 = RiteZoneBoundaryRules.dominionPulse(time, a0);
			float p1 = RiteZoneBoundaryRules.dominionPulse(time, a1);

			// Diffuse bruise around the vessel: clot red at the centre, gone at the rim.
			ribbon(vc, mat, alongX, a0, m0, y0, a1, m1, y1, VEIN_GLOW_HALF,
					0.42f, 0.02f, 0.03f, 0.20f * fade * p0, 0.20f * fade * p1, true);
			// The vessel itself: dark, brightening as the heartbeat passes.
			ribbon(vc, mat, alongX, a0, m0, y0 + 0.005, a1, m1, y1 + 0.005, VEIN_CORE_HALF,
					0.30f + 0.38f * (p0 + p1) * 0.5f, 0.012f, 0.02f, 0.72f * fade, 0.72f * fade, false);
		}

		private void drawHaze(VertexConsumer vc, Matrix4f mat, boolean alongX, double fixed,
				double a0, double y0, double a1, double y1, float fade) {
			float p0 = RiteZoneBoundaryRules.fervorPulse(time, a0);
			float p1 = RiteZoneBoundaryRules.fervorPulse(time, a1);
			float base0 = 0.16f * fade * p0;
			float base1 = 0.16f * fade * p1;
			double bottom0 = y0 + GROUND_LIFT;
			double bottom1 = y1 + GROUND_LIFT;
			// A low curtain of heat, brightest at the ground.
			vertex(vc, mat, alongX, a0, fixed, bottom0, 0.95f, 0.36f, 0.09f, base0);
			vertex(vc, mat, alongX, a1, fixed, bottom1, 0.95f, 0.36f, 0.09f, base1);
			vertex(vc, mat, alongX, a1, fixed, bottom1 + HAZE_HEIGHT * p1, 0.80f, 0.16f, 0.04f, 0.0f);
			vertex(vc, mat, alongX, a0, fixed, bottom0 + HAZE_HEIGHT * p0, 0.80f, 0.16f, 0.04f, 0.0f);
			// A thin smouldering line where the haze meets the ground.
			ribbon(vc, mat, alongX, a0, fixed, y0, a1, fixed, y1, EMBER_LINE_HALF,
					1.0f, 0.42f, 0.12f, 0.30f * fade * p0, 0.30f * fade * p1, true);
		}

		/**
		 * A flat ground ribbon of half-width {@code half} around the line from
		 * (a0, c0) to (a1, c1). With {@code softRim} its edges fade to nothing.
		 */
		private void ribbon(VertexConsumer vc, Matrix4f mat, boolean alongX,
				double a0, double c0, double y0, double a1, double c1, double y1, float half,
				float red, float green, float blue, float alpha0, float alpha1, boolean softRim) {
			double fy0 = y0 + GROUND_LIFT;
			double fy1 = y1 + GROUND_LIFT;
			float rim0 = softRim ? 0.0f : alpha0;
			float rim1 = softRim ? 0.0f : alpha1;
			// Inner half.
			vertex(vc, mat, alongX, a0, c0 - half, fy0, red, green, blue, rim0);
			vertex(vc, mat, alongX, a1, c1 - half, fy1, red, green, blue, rim1);
			vertex(vc, mat, alongX, a1, c1, fy1, red, green, blue, alpha1);
			vertex(vc, mat, alongX, a0, c0, fy0, red, green, blue, alpha0);
			// Outer half.
			vertex(vc, mat, alongX, a0, c0, fy0, red, green, blue, alpha0);
			vertex(vc, mat, alongX, a1, c1, fy1, red, green, blue, alpha1);
			vertex(vc, mat, alongX, a1, c1 + half, fy1, red, green, blue, rim1);
			vertex(vc, mat, alongX, a0, c0 + half, fy0, red, green, blue, rim0);
		}

		/** Emits a world-space vertex relative to the camera, keeping float precision far from the origin. */
		private void vertex(VertexConsumer vc, Matrix4f mat, boolean alongX, double along, double across,
				double y, float red, float green, float blue, float alpha) {
			double x = alongX ? along : across;
			double z = alongX ? across : along;
			vc.addVertex(mat, (float) (x - cam.x), (float) (y - cam.y), (float) (z - cam.z))
					.setColor(red, green, blue, alpha);
		}

		/** Top of the ground at the column just inside this edge sample, or NaN when it is too far from the camera. */
		private double groundY(boolean alongX, double fixed, double along, int outward) {
			double inside = fixed - outward * 0.5;
			int bx = Mth.floor(alongX ? along : inside);
			int bz = Mth.floor(alongX ? inside : along);
			int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz);
			if (top <= level.getMinBuildHeight() || Math.abs(top - cam.y) > RiteZoneBoundaryRules.MAX_GROUND_GAP) {
				return Double.NaN;
			}
			return top;
		}

		void emitParticle() {
			if (candidates == 0) return;
			if (kind == RiteZoneKind.SANGUINE_DOMINION) {
				if (level.random.nextFloat() < 0.3f) {
					spawn(HemoParticleData.bloodCell(BLOOD_CELL), 0.012);
				}
			} else if (level.random.nextFloat() < 0.4f) {
				spawn(HemoParticleData.emberSpark(EMBER, 0.12f, 0.55f, 34), 0.03);
			}
		}

		private void spawn(ParticleOptions particle, double rise) {
			level.addParticle(particle, particleX, particleY, particleZ,
					(level.random.nextDouble() - 0.5) * 0.01, rise, (level.random.nextDouble() - 0.5) * 0.01);
		}
	}
}
