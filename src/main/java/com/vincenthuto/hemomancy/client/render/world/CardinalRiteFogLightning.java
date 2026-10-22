package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.common.rite.harbinger.ActiveRiteData;
import com.vincenthuto.hemomancy.client.rite.CardinalRiteEventClientState;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteLightningPalette;
import com.vincenthuto.hutoslib.client.particle.BoltRenderer;
import com.vincenthuto.hutoslib.common.lightning.LightningTestBoltFactory;
import com.vincenthuto.hutoslib.common.lightning.LightningTestConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class CardinalRiteFogLightning {
	private static final CardinalRiteFogLightningSchedule SCHEDULE =
			new CardinalRiteFogLightningSchedule();
	/** Ticks after a wave begins at which the fog bank strikes each spawn point. */
	private static final float[] WAVE_STRIKE_TICKS = {0.0F, 15.0F};
	/** Height above the rite plane that a telegraph bolt falls from. */
	private static final double WAVE_STRIKE_HEIGHT = 2.6D;
	private static final Set<StrikeKey> STRUCK = new HashSet<>();

	private CardinalRiteFogLightning() {
	}

	public static void tick(List<ActiveRiteData.RiteEntry> activeRites,
			long gameTick, float partialTick) {
		for (CardinalRiteFogLightningSchedule.Strike scheduled
				: SCHEDULE.update(activeRites, gameTick)) {
			ActiveRiteData.RiteEntry rite = scheduled.rite();
			float radius = CardinalRiteFogGeometry.perimeterRadius(
					rite.getFootprintRadius(), rite.getRiteSize(), rite.getCompletedRings(),
					rite.getTotalRings(), rite.isLegacy());
			StrikeGeometry strike = geometry(rite.getCenter(), radius, scheduled.seed());
			strike(strike.start(), strike.end(), scheduled.seed(), partialTick);
		}
		telegraphWaves(activeRites, partialTick);
	}

	/** One black-and-purple bolt between two world points, on the rite's shared palette. */
	public static void strike(Vec3 start, Vec3 end, long seed, float partialTick) {
		LightningTestConfig config = config(seed, start.distanceTo(end));
		BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(
				start, end, seed, config.outerColor(), config.size(), config), partialTick);
		BoltRenderer.INSTANCE.add(LightningTestBoltFactory.create(
				start, end, seed, config.innerColor(), Math.max(0.01F, config.size() * 0.45F), config),
				partialTick);
	}

	public static void clear() {
		SCHEDULE.clear();
		STRUCK.clear();
	}

	/**
	 * Wave telegraph: as a wave begins, the fog bank strikes down at each spawn
	 * point, and again just before the threats arrive.
	 */
	private static void telegraphWaves(List<ActiveRiteData.RiteEntry> activeRites, float partialTick) {
		List<CardinalRiteEventClientState.RiteEvent> events = CardinalRiteEventClientState.all();
		STRUCK.removeIf(key -> !events.contains(key.event()));
		for (CardinalRiteEventClientState.RiteEvent event : events) {
			if (event.kind() != CardinalRiteEventKind.WAVE_START || event.points().isEmpty()) continue;
			ActiveRiteData.RiteEntry rite = riteAt(activeRites, event.center());
			if (rite == null || rite.isUnstained()) continue;
			float age = event.age(partialTick);
			for (int beat = 0; beat < WAVE_STRIKE_TICKS.length; beat++) {
				if (age < WAVE_STRIKE_TICKS[beat] || age > WAVE_STRIKE_TICKS[beat] + 4.0F) continue;
				if (!STRUCK.add(new StrikeKey(event, beat))) continue;
				for (int index = 0; index < event.points().size(); index++) {
					Vec3 target = event.points().get(index);
					long seed = event.center().asLong() * 31L + index * 0x9E3779B97F4A7C15L + beat * 977L;
					StrikeGeometry bolt = waveStrikeGeometry(event.center(), target, seed);
					strike(bolt.start(), bolt.end(), seed, partialTick);
				}
			}
		}
	}

	private static ActiveRiteData.RiteEntry riteAt(List<ActiveRiteData.RiteEntry> rites,
			BlockPos center) {
		for (ActiveRiteData.RiteEntry rite : rites) {
			if (rite.getCenter().equals(center)) return rite;
		}
		return null;
	}

	/** A bolt that falls from the fog bank, leaning in from the rite edge, onto a spawn point. */
	static StrikeGeometry waveStrikeGeometry(BlockPos center, Vec3 target, long seed) {
		Random random = new Random(seed);
		double planeY = CardinalRiteBoundaryGeometry.boundaryPlaneY(center.getY());
		double outwardX = target.x - (center.getX() + 0.5D);
		double outwardZ = target.z - (center.getZ() + 0.5D);
		double length = Math.max(0.001D, Math.hypot(outwardX, outwardZ));
		double lean = 0.6D + random.nextDouble() * 0.7D;
		double side = (random.nextDouble() - 0.5D) * 0.8D;
		Vec3 start = new Vec3(
				target.x + outwardX / length * lean - outwardZ / length * side,
				planeY + WAVE_STRIKE_HEIGHT + random.nextDouble() * 0.6D,
				target.z + outwardZ / length * lean + outwardX / length * side);
		return new StrikeGeometry(start, target);
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
				CardinalRiteLightningPalette.OUTER_BLACK,
				CardinalRiteLightningPalette.OUTER_BLACK,
				CardinalRiteLightningPalette.INNER_PURPLE,
				(float) Math.max(8.0D, distance + 4.0D),
				0.0F, 0.0F, 0.0F,
				14.0F, 2.6F, 7, 6, 0.18F, 0.035F,
				true, seed, false, 20);
	}

	record StrikeGeometry(Vec3 start, Vec3 end) {
	}

	private record StrikeKey(CardinalRiteEventClientState.RiteEvent event, int beat) {
	}
}
