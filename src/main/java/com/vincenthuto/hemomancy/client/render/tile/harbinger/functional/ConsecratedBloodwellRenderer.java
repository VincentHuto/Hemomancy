package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.particle.factory.AbsorbedBloodCellParticleFactory;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ConsecratedBloodwellBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.Map;

public class ConsecratedBloodwellRenderer implements BlockEntityRenderer<ConsecratedBloodwellBlockEntity> {
	private static final float TAU = (float) (Math.PI * 2.0);
	private static final int ARC_COUNT = 6;
	private static final int ARC_SEGMENTS = 16;
	private static final int PARTICLE_INTERVAL_TICKS = 4;
	private static final int PARTICLE_CACHE_LIMIT = 64;
	private static final float LOW_JET_HEIGHT = 0.42f;
	private static final float FULL_JET_HEIGHT = 1.34f;
	// Basin geometry from tools/model_export/consecrated_bloodwell.py, in blocks.
	/** Pool surface of a nearly empty well, just over the dried floor (11.6 px) so the ostium shows. */
	private static final float POOL_FLOOR_Y = 0.74f;
	/** Pool surface of a full well, just under the spout channels cut into the rim (13.6 px). */
	private static final float POOL_BRIM_Y = 0.846f;
	/** Inside the basin wall's 5.4 px flats. */
	private static final float POOL_RADIUS = 0.325f;
	private static final float RIM_TOP_Y = 0.90f;
	/** Past the rim's outer corners: arcs reaching this far fall outside the font. */
	private static final float BASIN_OUTER_RADIUS = 0.46f;
	/** Just outside the 7.6 px spout lips, over the catch cups' blood (6.7 to 7.75 px). */
	private static final float SPOUT_LIP_RADIUS = 0.479f;
	private static final float SPOUT_CHANNEL_Y = 0.857f;
	private static final float CATCH_CUP_Y = 0.088f;
	/** Pressure above which the pool brims into the four cardinal spouts. */
	private static final float SPILL_PRESSURE = 0.6f;
	private final Map<Long, Long> lastParticleTickByPos = new HashMap<>();

	public ConsecratedBloodwellRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(ConsecratedBloodwellBlockEntity be, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		if (be.getLevel() == null) {
			return;
		}

		double stored = be.getStoredBlood();
		if (stored <= 0.0) {
			return;
		}

		float fullness = Mth.clamp((float) (stored / be.getMaxBlood()), 0.0f, 1.0f);
		float pressure = bloodPressure(fullness);
		float time = (be.getLevel().getGameTime() + partialTick) * 0.08f;
		VertexConsumer vc = buffer.getBuffer(HemoRenderTypes.BLOODWELL_FOUNTAIN);

		poseStack.pushPose();
		Matrix4f mat = poseStack.last().pose();
		float poolY = poolSurface(pressure);
		renderSurfacePool(vc, mat, pressure, time, poolY);
		renderCentralBloodJet(vc, mat, pressure, time, poolY);
		int activeArcCount = Mth.clamp(4 + Math.round(pressure * (ARC_COUNT - 4)), 4, ARC_COUNT);
		for (int i = 0; i < activeArcCount; i++) {
			renderFountainArc(vc, mat, i, activeArcCount, pressure, time, poolY);
		}
		renderSpoutSpill(vc, mat, pressure, time);
		poseStack.popPose();
		spawnBloodwellParticles(be, pressure, time, poolY);
	}

	private static float bloodPressure(float fullness) {
		return Mth.clamp(fullness * fullness * (3.0f - 2.0f * fullness), 0.0f, 1.0f);
	}

	private static float poolSurface(float pressure) {
		return POOL_FLOOR_Y + pressure * (POOL_BRIM_Y - POOL_FLOOR_Y);
	}

	private static float spillStrength(float pressure) {
		return Mth.clamp((pressure - SPILL_PRESSURE) / (1.0f - SPILL_PRESSURE), 0.0f, 1.0f);
	}

	private static void renderSurfacePool(VertexConsumer vc, Matrix4f mat, float pressure, float time, float y) {
		float pulse = 0.5f + 0.5f * Mth.sin(time * 1.8f);
		float radius = POOL_RADIUS - 0.006f + pulse * 0.006f;
		int alphaInner = (int) (150 + pressure * 70.0f);
		int alphaOuter = (int) (110 + pressure * 80.0f);
		for (int i = 0; i < 16; i++) {
			float a0 = i * TAU / 16.0f;
			float a1 = (i + 1) * TAU / 16.0f;
			quad(vc, mat,
					0.5f, y + 0.003f, 0.5f,
					0.5f + Mth.cos(a0) * radius, y, 0.5f + Mth.sin(a0) * radius,
					0.5f + Mth.cos(a1) * radius, y, 0.5f + Mth.sin(a1) * radius,
					0.5f, y + 0.003f, 0.5f,
					190, 6, 5, alphaInner,
					85, 1, 1, alphaOuter);
		}
	}

	private static void renderCentralBloodJet(VertexConsumer vc, Matrix4f mat, float pressure, float time,
			float baseY) {
		float width = jetWidth(pressure, time);
		float height = jetHeight(pressure, time);
		float topY = baseY + height;
		int baseAlpha = (int) (145 + pressure * 82.0f);
		int topAlpha = (int) (26 + pressure * 54.0f);

		verticalSheet(vc, mat, 0.5f, baseY, 0.5f, 1.0f, 0.0f, width, topY,
				225, 14, 10, baseAlpha, 120, 2, 2, topAlpha);
		verticalSheet(vc, mat, 0.5f, baseY, 0.5f, 0.0f, 1.0f, width * 0.82f, topY,
				205, 8, 7, Math.max(96, baseAlpha - 28), 100, 2, 2, Math.max(16, topAlpha - 12));
	}

	private static float jetWidth(float pressure, float time) {
		float pulse = 0.5f + 0.5f * Mth.sin(time * 2.4f);
		return 0.024f + pressure * 0.074f + pulse * (0.004f + pressure * 0.014f);
	}

	private static float jetHeight(float pressure, float time) {
		float pulse = 0.5f + 0.5f * Mth.sin(time * 2.4f);
		return LOW_JET_HEIGHT + pressure * (FULL_JET_HEIGHT - LOW_JET_HEIGHT)
				+ pulse * (0.030f + pressure * 0.075f);
	}

	private static void renderFountainArc(VertexConsumer vc, Matrix4f mat, int index, int activeArcCount,
			float pressure, float time, float poolY) {
		float angle = index * TAU / activeArcCount + time * (0.09f + pressure * 0.16f);
		float width = 0.007f + pressure * 0.024f;
		renderSmoothFountainArc(vc, mat, index, pressure, time, angle, width, poolY);
	}

	private static void renderSmoothFountainArc(VertexConsumer vc, Matrix4f mat, int index, float pressure,
			float time, float angle, float width, float poolY) {
		for (int segment = 0; segment < ARC_SEGMENTS; segment++) {
			float t0 = segment / (float) ARC_SEGMENTS;
			float t1 = (segment + 1.0f) / ARC_SEGMENTS;
			float[] p0 = arcPoint(angle, t0, pressure, time, index, poolY);
			float[] p1 = arcPoint(angle, t1, pressure, time, index, poolY);
			taperedRibbon(vc, mat, p0[0], p0[1], p0[2], p1[0], p1[1], p1[2],
					arcWidthAt(t0, width), arcWidthAt(t1, width),
					214, 9, 7, arcAlphaAt(t0, pressure), arcAlphaAt(t1, pressure));
		}
	}

	private static float arcWidthAt(float t, float width) {
		float taper = Mth.sin(t * (float) Math.PI);
		return width * (0.28f + taper * 0.72f);
	}

	private static int arcAlphaAt(float t, float pressure) {
		float taper = Mth.sin(t * (float) Math.PI);
		return Mth.clamp((int) (26 + pressure * 54.0f + taper * (62.0f + pressure * 84.0f)), 20, 210);
	}

	private void spawnBloodwellParticles(ConsecratedBloodwellBlockEntity be, float pressure, float time,
			float poolY) {
		var level = be.getLevel();
		if (level == null || !level.isClientSide) {
			return;
		}
		long tick = level.getGameTime();
		long posKey = be.getBlockPos().asLong();
		int stagger = Math.floorMod(posKey, PARTICLE_INTERVAL_TICKS);
		if ((tick + stagger) % PARTICLE_INTERVAL_TICKS != 0) {
			return;
		}
		Long lastTick = lastParticleTickByPos.get(posKey);
		if (lastTick != null && lastTick == tick) {
			return;
		}
		if (lastParticleTickByPos.size() > PARTICLE_CACHE_LIMIT) {
			lastParticleTickByPos.clear();
		}
		lastParticleTickByPos.put(posKey, tick);

		BlockPos pos = be.getBlockPos();
		double cx = pos.getX() + 0.5D;
		double cy = pos.getY() + poolY;
		double cz = pos.getZ() + 0.5D;
		float jetTop = poolY + jetHeight(pressure, time);
		int basinParticles = Mth.clamp(1 + Math.round(pressure * 3.0f), 1, 4);

		for (int i = 0; i < basinParticles; i++) {
			double angle = level.random.nextDouble() * TAU;
			double radius = 0.06D + level.random.nextDouble() * (POOL_RADIUS - 0.08D);
			double x = cx + Math.cos(angle) * radius;
			double z = cz + Math.sin(angle) * radius;
			double y = cy + 0.03D + level.random.nextDouble() * (0.28D + pressure * 0.24D);
			double speed = 0.006D + pressure * 0.010D;
			level.addParticle(
					AbsorbedBloodCellParticleFactory.createData(new ParticleColor(245, 30, 14)),
					x, y, z,
					Math.cos(angle) * speed, 0.015D + pressure * 0.015D, Math.sin(angle) * speed);
		}

		int topParticles = Mth.clamp(1 + Math.round(pressure * 2.0f), 1, 3);
		for (int i = 0; i < topParticles; i++) {
			double angle = level.random.nextDouble() * TAU;
			double radius = level.random.nextDouble() * (0.025D + pressure * 0.055D);
			double x = cx + Math.cos(angle) * radius;
			double z = cz + Math.sin(angle) * radius;
			double y = pos.getY() + jetTop - 0.02D + level.random.nextDouble() * 0.08D;
			level.addParticle(
					GlowParticleFactory.createData(ParticleColor.BLOOD),
					x, y, z,
					Math.cos(angle) * (0.006D + pressure * 0.012D),
					0.012D + pressure * 0.018D,
					Math.sin(angle) * (0.006D + pressure * 0.012D));
		}

		float spill = spillStrength(pressure);
		if (spill > 0.0f && level.random.nextFloat() < spill) {
			double angle = level.random.nextInt(4) * TAU / 4.0D;
			double x = cx + Math.cos(angle) * SPOUT_LIP_RADIUS;
			double z = cz + Math.sin(angle) * SPOUT_LIP_RADIUS;
			level.addParticle(
					AbsorbedBloodCellParticleFactory.createData(new ParticleColor(200, 12, 8)),
					x, pos.getY() + CATCH_CUP_Y + 0.03D, z,
					(level.random.nextDouble() - 0.5D) * 0.02D, 0.02D,
					(level.random.nextDouble() - 0.5D) * 0.02D);
		}
	}

	/** A brimming pool runs out along the four cardinal spout channels and pours into the catch cups. */
	private static void renderSpoutSpill(VertexConsumer vc, Matrix4f mat, float pressure, float time) {
		float spill = spillStrength(pressure);
		if (spill <= 0.0f) {
			return;
		}
		for (int i = 0; i < 4; i++) {
			float angle = i * TAU / 4.0f;
			float cos = Mth.cos(angle);
			float sin = Mth.sin(angle);
			float pulse = 0.5f + 0.5f * Mth.sin(time * 1.7f + i * 1.3f);
			float width = 0.010f + spill * 0.014f + pulse * 0.004f;
			int alpha = (int) (70 + spill * 130.0f);
			float tx = -sin * width;
			float tz = cos * width;
			float x0 = 0.5f + cos * (POOL_RADIUS - 0.01f);
			float z0 = 0.5f + sin * (POOL_RADIUS - 0.01f);
			float x1 = 0.5f + cos * SPOUT_LIP_RADIUS;
			float z1 = 0.5f + sin * SPOUT_LIP_RADIUS;
			quad(vc, mat,
					x0 - tx, SPOUT_CHANNEL_Y, z0 - tz,
					x1 - tx, SPOUT_CHANNEL_Y, z1 - tz,
					x1 + tx, SPOUT_CHANNEL_Y, z1 + tz,
					x0 + tx, SPOUT_CHANNEL_Y, z0 + tz,
					200, 8, 6, alpha,
					170, 5, 4, alpha);
			ribbon(vc, mat, x1, SPOUT_CHANNEL_Y, z1, x1, CATCH_CUP_Y, z1, angle, width * 0.8f,
					170, 5, 4, alpha);
		}
	}

	/** Low wells fall back into their own pool; full wells throw clear of the rim. */
	private static float[] arcPoint(float angle, float t, float pressure, float time, int index, float poolY) {
		float reach = 0.10f + pressure * 0.62f;
		float height = 0.12f + pressure * 0.50f;
		float launch = poolY + 0.06f;
		float land = arcLanding(0.06f + reach, poolY);
		float wobble = Mth.sin(time * 2.0f + index * 1.9f + t * 4.0f) * (0.010f + pressure * 0.025f);
		float radial = 0.06f + t * reach + wobble;
		float y = launch + Mth.sin(t * (float) Math.PI) * height - t * (launch - land);
		return new float[] {
				0.5f + Mth.cos(angle) * radial,
				y,
				0.5f + Mth.sin(angle) * radial
		};
	}

	private static float arcLanding(float endRadius, float poolY) {
		if (endRadius <= POOL_RADIUS) {
			return poolY;
		}
		if (endRadius < BASIN_OUTER_RADIUS) {
			return RIM_TOP_Y;
		}
		return RIM_TOP_Y - (endRadius - BASIN_OUTER_RADIUS) * 1.2f;
	}

	private static void verticalSheet(VertexConsumer vc, Matrix4f mat, float centerX, float baseY, float centerZ,
			float axisX, float axisZ, float halfWidth, float topY, int r0, int g0, int b0, int a0,
			int r1, int g1, int b1, int a1) {
		float dx = axisX * halfWidth;
		float dz = axisZ * halfWidth;
		quad(vc, mat,
				centerX - dx, baseY, centerZ - dz,
				centerX - dx * 0.42f, topY, centerZ - dz * 0.42f,
				centerX + dx * 0.42f, topY, centerZ + dz * 0.42f,
				centerX + dx, baseY, centerZ + dz,
				r0, g0, b0, a0,
				r1, g1, b1, a1);
	}

	private static void ribbon(VertexConsumer vc, Matrix4f mat, float x0, float y0, float z0, float x1, float y1,
			float z1, float angle, float halfWidth, int r, int g, int b, int alpha) {
		float nx = -Mth.sin(angle) * halfWidth;
		float nz = Mth.cos(angle) * halfWidth;
		quad(vc, mat,
				x0 - nx, y0, z0 - nz,
				x1 - nx, y1, z1 - nz,
				x1 + nx, y1, z1 + nz,
				x0 + nx, y0, z0 + nz,
				r, g, b, alpha,
				80, 1, 1, Math.max(0, alpha / 3));
	}

	private static void taperedRibbon(VertexConsumer vc, Matrix4f mat, float x0, float y0, float z0, float x1,
			float y1, float z1, float width0, float width1, int r, int g, int b, int alpha0, int alpha1) {
		float dx = x1 - x0;
		float dz = z1 - z0;
		float len = Mth.sqrt(dx * dx + dz * dz);
		if (len < 0.0001f) {
			return;
		}
		float nx = -dz / len;
		float nz = dx / len;
		quad(vc, mat,
				x0 - nx * width0, y0, z0 - nz * width0,
				x1 - nx * width1, y1, z1 - nz * width1,
				x1 + nx * width1, y1, z1 + nz * width1,
				x0 + nx * width0, y0, z0 + nz * width0,
				r, g, b, alpha0,
				r, g, b, alpha1);
	}

	private static void quad(VertexConsumer vc, Matrix4f mat, float x0, float y0, float z0, float x1, float y1,
			float z1, float x2, float y2, float z2, float x3, float y3, float z3,
			int r0, int g0, int b0, int a0, int r1, int g1, int b1, int a1) {
		vc.addVertex(mat, x0, y0, z0).setColor(r0, g0, b0, a0);
		vc.addVertex(mat, x1, y1, z1).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x2, y2, z2).setColor(r1, g1, b1, a1);
		vc.addVertex(mat, x3, y3, z3).setColor(r0, g0, b0, a0);
	}

	@Override
	public int getViewDistance() {
		return 96;
	}
}
