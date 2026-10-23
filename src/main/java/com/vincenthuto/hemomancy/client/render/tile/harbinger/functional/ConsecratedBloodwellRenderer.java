package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.render.BloodPoolSurface;
import com.vincenthuto.hemomancy.client.render.HemoRenderTypes;
import com.vincenthuto.hemomancy.client.render.world.BloodStreams;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ConsecratedBloodwellBlockEntity;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;

public class ConsecratedBloodwellRenderer implements BlockEntityRenderer<ConsecratedBloodwellBlockEntity> {
	private static final float TAU = (float) (Math.PI * 2.0);
	private static final int ARC_COUNT = 6;
	private static final int ARC_SEGMENTS = 16;
	private static final int JET_SEGMENTS = 10;
	private static final int FALL_SEGMENTS = 8;
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
	/** The pool surface's disc: out to the basin wall's corners, so it meets the wall all round; the wall hides the rest. */
	private static final float POOL_SURFACE_RADIUS = 5.8f / 16.0f;
	private static final float RIM_TOP_Y = 0.90f;
	/** Past the rim's outer corners: arcs reaching this far fall outside the font. */
	private static final float BASIN_OUTER_RADIUS = 0.46f;
	/** Just outside the 7.6 px spout lips, over the catch cups' blood (6.7 to 7.75 px). */
	private static final float SPOUT_LIP_RADIUS = 0.479f;
	private static final float SPOUT_CHANNEL_Y = 0.857f;
	private static final float CATCH_CUP_Y = 0.088f;
	/** Pressure above which the pool brims into the four cardinal spouts. */
	private static final float SPILL_PRESSURE = 0.6f;
	/** Below this drawn fullness the well is dry and nothing is drawn. */
	private static final float DRY = 0.002f;
	private final Map<Long, Long> lastParticleTickByPos = new HashMap<>();

	public ConsecratedBloodwellRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(ConsecratedBloodwellBlockEntity be, float partialTick, PoseStack poseStack,
			MultiBufferSource buffer, int packedLight, int packedOverlay) {
		if (be.getLevel() == null) {
			return;
		}

		double ticks = be.getLevel().getGameTime() + partialTick;
		float target = Mth.clamp((float) (be.getStoredBlood() / be.getMaxBlood()), 0.0f, 1.0f);
		// The level rises and drains smoothly between the once-a-second pool syncs, down to dry.
		float fullness = be.shownFullness(target, ticks);
		if (fullness <= DRY) {
			return;
		}
		float pressure = bloodPressure(fullness);
		float time = (float) ticks * 0.08f;
		int seed = (int) Math.floorMod(be.getBlockPos().asLong(), 1000L);

		poseStack.pushPose();
		Matrix4f mat = poseStack.last().pose();
		float poolY = poolSurface(pressure);
		// The pool goes first: it writes depth, so the jet and arcs plunge into its surface rather than through it.
		renderSurfacePool(buffer, mat, seed, pressure, poolY, (float) (ticks % 24000.0));
		var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 eye = camera.getPosition().subtract(Vec3.atLowerCornerOf(be.getBlockPos()));
		Vec3 right = new Vec3(new Vector3f(1, 0, 0).rotate(camera.rotation()));
		Vec3 up = new Vec3(new Vector3f(0, 1, 0).rotate(camera.rotation()));
		renderCentralBloodJet(poseStack, buffer, eye, right, up, seed, pressure, time, poolY);
		int activeArcCount = Mth.clamp(4 + Math.round(pressure * (ARC_COUNT - 4)), 4, ARC_COUNT);
		for (int i = 0; i < activeArcCount; i++) {
			renderFountainArc(poseStack, buffer, eye, seed, i, activeArcCount, pressure, time, poolY);
		}
		renderSpoutSpill(poseStack, buffer, eye, seed, pressure, time);
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

	/**
	 * The blood standing in the basin, in the shared blood pool shader: the jet rings ripples out from the centre,
	 * harder as the well fills. A shallow pool stays translucent so the ostium shows through it.
	 */
	private static void renderSurfacePool(MultiBufferSource buffer, Matrix4f mat, int seed, float pressure, float y,
			float shaderTime) {
		VertexConsumer pool = buffer.getBuffer(HemoRenderTypes.bloodPool(shaderTime, seed));
		BloodPoolSurface.draw(mat, pool, 0.5f, y, 0.5f, POOL_SURFACE_RADIUS,
				(int) (90 + pressure * 165.0f), 0, 0, (int) (170 + pressure * 85.0f));
	}

	/** A column of blood running up out of the heart, narrowing as it climbs and breaking into drops at its crown. */
	private static void renderCentralBloodJet(PoseStack poses, MultiBufferSource buffer, Vec3 eye, Vec3 right, Vec3 up,
			int seed, float pressure, float time, float baseY) {
		float width = jetWidth(pressure, time);
		float topY = baseY + jetHeight(pressure, time);
		List<Vec3> column = new ArrayList<>(JET_SEGMENTS + 1);
		for (int i = 0; i <= JET_SEGMENTS; i++) {
			float t = i / (float) JET_SEGMENTS;
			// The column sways a little more the higher it climbs, as a real jet does.
			float sway = t * t * (0.008f + pressure * 0.014f);
			column.add(new Vec3(0.5f + Mth.sin(time * 1.3f + t * 3.0f) * sway, Mth.lerp(t, baseY, topY),
					0.5f + Mth.cos(time * 1.1f + t * 2.6f) * sway));
		}
		float alpha = 0.7f + pressure * 0.25f;
		BloodStreams.stream(poses, buffer, column, eye, width, width * 0.45f, seed, alpha);
		// A thinner core on its own seed keeps the column reading as moving liquid rather than a painted strip.
		BloodStreams.stream(poses, buffer, column, eye, width * 0.55f, width * 0.25f, seed + 31, alpha * 0.8f);
		int drops = 2 + Math.round(pressure * 2.0f);
		for (int i = 0; i < drops; i++) {
			// Each drop is thrown off the crown and falls back on its own cycle.
			float cycle = (time * 0.9f + i / (float) drops) % 1.0f;
			float angle = i * TAU / drops + seed;
			float spread = cycle * (0.03f + pressure * 0.05f);
			Vec3 at = new Vec3(0.5f + Mth.cos(angle) * spread,
					topY - 0.02f + Mth.sin(cycle * (float) Math.PI) * 0.05f - cycle * cycle * 0.12f,
					0.5f + Mth.sin(angle) * spread);
			BloodStreams.droplet(poses, buffer, at, right, up, 0.012f + pressure * 0.012f, seed + 7 * i,
					alpha * (1.0f - cycle * 0.6f));
		}
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

	/** One of the streams thrown out of the heart, fullest at launch and thinning as it falls. */
	private static void renderFountainArc(PoseStack poses, MultiBufferSource buffer, Vec3 eye, int seed, int index,
			int activeArcCount, float pressure, float time, float poolY) {
		float angle = index * TAU / activeArcCount + time * (0.09f + pressure * 0.16f);
		float width = 0.007f + pressure * 0.024f;
		List<Vec3> path = new ArrayList<>(ARC_SEGMENTS + 1);
		for (int segment = 0; segment <= ARC_SEGMENTS; segment++) {
			float[] point = arcPoint(angle, segment / (float) ARC_SEGMENTS, pressure, time, index, poolY);
			path.add(new Vec3(point[0], point[1], point[2]));
		}
		BloodStreams.stream(poses, buffer, path, eye, width * 0.8f, width * 0.45f, seed + 13 * index,
				0.55f + pressure * 0.4f);
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
					HemoParticleData.absorbedBloodCell(new ParticleColor(245, 30, 14)),
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
					HemoParticleData.absorbedBloodCell(new ParticleColor(200, 12, 8)),
					x, pos.getY() + CATCH_CUP_Y + 0.03D, z,
					(level.random.nextDouble() - 0.5D) * 0.02D, 0.02D,
					(level.random.nextDouble() - 0.5D) * 0.02D);
		}
	}

	/**
	 * A brimming pool runs out along the four cardinal spout channels and pours off each lip into its catch cup: one
	 * continuous stream from the pool's edge, curling out over the lip as it starts to fall.
	 */
	private static void renderSpoutSpill(PoseStack poses, MultiBufferSource buffer, Vec3 eye, int seed, float pressure,
			float time) {
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
			List<Vec3> path = new ArrayList<>(FALL_SEGMENTS + 2);
			path.add(new Vec3(0.5f + cos * (POOL_RADIUS - 0.01f), SPOUT_CHANNEL_Y, 0.5f + sin * (POOL_RADIUS - 0.01f)));
			for (int k = 0; k <= FALL_SEGMENTS; k++) {
				float t = k / (float) FALL_SEGMENTS;
				// Off the lip the stream carries a little way out before it drops straight into the cup.
				float reach = SPOUT_LIP_RADIUS
						+ (0.02f + spill * 0.02f) * Mth.sin(Math.min(1.0f, t * 2.0f) * (float) Math.PI * 0.5f);
				float y = SPOUT_CHANNEL_Y - (SPOUT_CHANNEL_Y - CATCH_CUP_Y) * t * t;
				path.add(new Vec3(0.5f + cos * reach, y, 0.5f + sin * reach));
			}
			BloodStreams.stream(poses, buffer, path, eye, width, width * 0.7f, seed + 101 + i, 0.4f + spill * 0.55f);
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

	@Override
	public int getViewDistance() {
		return 96;
	}
}
