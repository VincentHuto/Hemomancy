package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.data.ActiveRiteClientData;
import com.vincenthuto.hutoslib.client.particle.BoltRenderer;
import com.vincenthuto.hutoslib.common.lightning.LightningTestBoltFactory;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public final class CardinalRiteFogLightning {
	private static final int OUTER_BLACK = 0xE806020A;
	private static final int INNER_PURPLE = 0xFF5A167D;
	private static final CardinalRiteFogLightningSchedule SCHEDULE =
			new CardinalRiteFogLightningSchedule();

	private CardinalRiteFogLightning() {
	}

	public static void tick(List<ActiveRiteClientData.RiteEntry> activeRites,
			long gameTick, float partialTick) {
		for (CardinalRiteFogLightningSchedule.Strike scheduled
				: SCHEDULE.update(activeRites, gameTick)) {
			ActiveRiteClientData.RiteEntry rite = scheduled.rite();
			boolean legacy = "LEGACY".equals(rite.getPhase());
			float radius = CardinalRiteFogGeometry.perimeterRadius(
					rite.getFootprintRadius(), rite.getRiteSize(), rite.getCompletedRings(), legacy);
			StrikeGeometry strike = geometry(rite.getCenter(), radius, scheduled.seed());
			LightningTestConfig config = config(
					scheduled.seed(), strike.start().distanceTo(strike.end()));
			BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(
					strike.start(), strike.end(), scheduled.seed(),
					config.outerColor(), config.size(), config), partialTick);
			BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(
					strike.start(), strike.end(), scheduled.seed(),
					config.innerColor(), Math.max(0.01F, config.size() * 0.45F), config), partialTick);
		}
	}

	public static void clear() {
		SCHEDULE.clear();
	}

	static StrikeGeometry geometry(BlockPos center, float radius, long seed) {
		Random random = new Random(seed);
		double angle = random.nextDouble() * Math.PI * 2.0D;
		double desiredSpan = 0.80D + random.nextDouble();
		double angularSpan = Math.min(0.45D,
				Math.max(0.08D, desiredSpan / Math.max(1.0F, radius)));
		double endAngle = angle + angularSpan * (random.nextBoolean() ? 1.0D : -1.0D);
		double startRadius = radius + (random.nextDouble() - 0.5D) * 0.90D;
		double endRadius = radius + (random.nextDouble() - 0.5D) * 1.10D;
		double centerX = center.getX() + 0.5D;
		double centerZ = center.getZ() + 0.5D;
		double planeY = CardinalRiteBoundaryGeometry.boundaryPlaneY(center.getY());
		double cloudY = planeY + 0.38D + random.nextDouble() * 0.46D;
		double endY = cloudY + (random.nextDouble() - 0.5D) * 0.24D;
		Vec3 start = new Vec3(
				centerX + Math.cos(angle) * startRadius,
				cloudY,
				centerZ + Math.sin(angle) * startRadius);
		Vec3 end = new Vec3(
				centerX + Math.cos(endAngle) * endRadius,
				endY,
				centerZ + Math.sin(endAngle) * endRadius);
		return new StrikeGeometry(start, end);
	}

	static LightningTestConfig config(long seed, double distance) {
		return new LightningTestConfig(
				LightningTestConfig.Backend.BOLT,
				OUTER_BLACK,
				OUTER_BLACK,
				INNER_PURPLE,
				(float) Math.max(8.0D, distance + 4.0D),
				0.0F, 0.0F, 0.0F,
				14.0F, 2.6F, 7, 6, 0.18F, 0.035F,
				true, seed, false, 20);
	}

	record StrikeGeometry(Vec3 start, Vec3 end) {
	}
}
