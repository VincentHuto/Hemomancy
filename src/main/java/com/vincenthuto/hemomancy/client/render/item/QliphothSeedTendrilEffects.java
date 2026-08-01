package com.vincenthuto.hemomancy.client.render.item;

import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilEffectData;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/** Shared radial root pulses for dropped and Cardinal-Focus Qliphoth Seeds. */
public final class QliphothSeedTendrilEffects {
	private static final int ROOTS_PER_PULSE = 12;
	private static final int ROOT_PULSE_INTERVAL = 6;
	private static final int ROOT_CACHE_LIMIT = 512;
	private static final double FULL_CIRCLE = Math.PI * 2.0D;
	private static final double ROOT_GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
	private static final double ROOT_PULSE_SPIN = Math.PI * (3.0D - Math.sqrt(5.0D));

	private static final Map<Integer, Long> NEXT_ENTITY_PULSE = new HashMap<>();
	private static final Map<Long, Long> NEXT_POSITION_PULSE = new HashMap<>();

	private QliphothSeedTendrilEffects() {
	}

	public static void spawnForEntity(ItemEntity entity, float partialTick) {
		long gameTime = entity.level().getGameTime();
		int entityId = entity.getId();
		if (!pulseReady(NEXT_ENTITY_PULSE, entityId, gameTime)) return;

		long pulse = gameTime / ROOT_PULSE_INTERVAL;
		for (int index = 0; index < ROOTS_PER_PULSE; index++) {
			Root root = root(entityId, gameTime, pulse, index);
			TendrilAnchor start = new TendrilAnchor.Entity(entityId,
					TendrilAnchor.AnchorPoint.CENTER, root.startOffset());
			TendrilAnchor end = new TendrilAnchor.Entity(entityId,
					TendrilAnchor.AnchorPoint.CENTER, root.endOffset());
			add(start, end, root.seed(), partialTick);
		}
	}

	public static void spawnAt(long positionKey, Vec3 origin, long gameTime, float partialTick) {
		if (!pulseReady(NEXT_POSITION_PULSE, positionKey, gameTime)) return;

		long pulse = gameTime / ROOT_PULSE_INTERVAL;
		for (int index = 0; index < ROOTS_PER_PULSE; index++) {
			Root root = root(positionKey, gameTime, pulse, index);
			add(new TendrilAnchor.Point(origin.add(root.startOffset())),
					new TendrilAnchor.Point(origin.add(root.endOffset())), root.seed(), partialTick);
		}
	}

	private static <K> boolean pulseReady(Map<K, Long> schedule, K key, long gameTime) {
		if (gameTime < schedule.getOrDefault(key, Long.MIN_VALUE)) return false;
		if (schedule.size() > ROOT_CACHE_LIMIT) {
			schedule.entrySet().removeIf(entry -> entry.getValue() + 200L < gameTime);
		}
		schedule.put(key, gameTime + ROOT_PULSE_INTERVAL);
		return true;
	}

	private static Root root(long identity, long gameTime, long pulse, int index) {
		long seed = rootSeed(identity, pulse, index);
		return new Root(rootStartOffset(gameTime, index, pulse),
				rootEndOffset(gameTime, index, pulse), seed);
	}

	private static void add(TendrilAnchor start, TendrilAnchor end, long seed, float partialTick) {
		TendrilRenderer.INSTANCE.add(new TendrilEffectData(start, end, rootConfig(seed), seed), partialTick);
	}

	private static Vec3 rootStartOffset(long gameTime, int index, long pulse) {
		double angle = animatedAngle(gameTime, index, pulse) + 0.18D;
		return new Vec3(Math.cos(angle) * 0.035D, 0.10D, Math.sin(angle) * 0.035D);
	}

	private static Vec3 rootEndOffset(long gameTime, int index, long pulse) {
		double angle = animatedAngle(gameTime, index, pulse);
		double sideAngle = angle + Math.PI * 0.5D;
		double sway = Math.sin(gameTime * 0.13D + index * 1.71D) * 0.08D;
		double reach = rootReach(index, pulse);
		return new Vec3(Math.cos(angle) * reach + Math.cos(sideAngle) * sway,
				-rootDrop(index, pulse),
				Math.sin(angle) * reach + Math.sin(sideAngle) * sway);
	}

	private static double animatedAngle(long gameTime, int index, long pulse) {
		double angle = index * ROOT_GOLDEN_ANGLE + pulse * ROOT_PULSE_SPIN
				+ Math.sin(gameTime * 0.075D + index * 2.37D) * 0.18D;
		return Math.floorMod((long) (angle * 1_000_000.0D),
				(long) (FULL_CIRCLE * 1_000_000.0D)) / 1_000_000.0D;
	}

	private static double rootReach(int index, long pulse) {
		return 0.46D + 0.18D * (0.5D + 0.5D * Math.sin(index * 1.91D + pulse * 0.73D));
	}

	private static double rootDrop(int index, long pulse) {
		return 0.08D + 0.08D * (0.5D + 0.5D * Math.cos(index * 2.17D + pulse * 0.61D));
	}

	private static TendrilEffectConfig rootConfig(long seed) {
		return TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withColors(0xF0050003, 0xB8D10B1A)
				.withBlendColors(false)
				.withLifecycle(4, 7, 8)
				.withShape(16, 1, 0.024F, 0.045F)
				.withBranching(1, 1, 0.12F, 0.45F)
				.withWrithe(0.04F, 0.09F, 0.08F, 0.0F)
				.withFixedSeed(true, seed);
	}

	private static long rootSeed(long identity, long pulse, int index) {
		long seed = 0x6A09E667F3BCC909L;
		seed ^= identity * 0x9E3779B97F4A7C15L;
		seed ^= pulse * 0xBF58476D1CE4E5B9L;
		seed ^= (long) index * 0x94D049BB133111EBL;
		return seed;
	}

	private record Root(Vec3 startOffset, Vec3 endOffset, long seed) {
	}
}
