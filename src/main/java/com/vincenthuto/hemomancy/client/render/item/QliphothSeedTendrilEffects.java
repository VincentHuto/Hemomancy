package com.vincenthuto.hemomancy.client.render.item;

import com.vincenthuto.hutoslib.client.particle.TendrilRenderer;
import com.vincenthuto.hutoslib.client.particle.data.TendrilEffectData;
import com.vincenthuto.hutoslib.common.tendril.TendrilAnchor;
import com.vincenthuto.hutoslib.common.tendril.TendrilEffectConfig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/** The tentacles hanging from a Qliphoth Seed, dropped, on a Cardinal Focus or in the Monolith crossing. */
public final class QliphothSeedTendrilEffects {
	private static final int ROOTS_PER_PULSE = 7;
	private static final int ROOT_PULSE_INTERVAL = 6;
	private static final int ROOT_CACHE_LIMIT = 512;
	private static final double FULL_CIRCLE = Math.PI * 2.0D;
	private static final double ROOT_GOLDEN_ANGLE = Math.PI * (3.0D - Math.sqrt(5.0D));
	private static final double ROOT_PULSE_SPIN = Math.PI * (3.0D - Math.sqrt(5.0D));

	private static final Map<Integer, Long> NEXT_ENTITY_PULSE = new HashMap<>();
	private static final Map<Long, Long> NEXT_POSITION_PULSE = new HashMap<>();

	/** Forgets everything, so nothing outlives the world it was seen in. */
	public static void clear() {
		NEXT_ENTITY_PULSE.clear();
		NEXT_POSITION_PULSE.clear();
	}

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
			add(start, end, root, 1.0F, partialTick);
		}
	}

	public static void spawnAt(long positionKey, Vec3 origin, long gameTime, float partialTick) {
		spawnAt(positionKey, origin, gameTime, partialTick, 1.0F);
	}

	/** As {@link #spawnAt(long, Vec3, long, float)} for a Seed drawn {@code scale} times its usual size. */
	public static void spawnAt(long positionKey, Vec3 origin, long gameTime, float partialTick, float scale) {
		if (!pulseReady(NEXT_POSITION_PULSE, positionKey, gameTime)) return;

		long pulse = gameTime / ROOT_PULSE_INTERVAL;
		for (int index = 0; index < ROOTS_PER_PULSE; index++) {
			Root root = root(positionKey, gameTime, pulse, index);
			add(new TendrilAnchor.Point(origin.add(root.startOffset().scale(scale))),
					new TendrilAnchor.Point(origin.add(root.endOffset().scale(scale))), root, scale, partialTick);
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
		double angle = animatedAngle(gameTime, index, pulse);
		return new Root(rootStartOffset(gameTime, index, pulse), rootEndOffset(gameTime, index, pulse),
				new Vec3(Math.cos(angle), 0.0D, Math.sin(angle)), seed);
	}

	/**
	 * Each tentacle's own outward direction goes in as its preferred up. Without one the renderer falls back on a single
	 * world axis for anything near vertical, and steep tentacles all bend the same way into a one-sided fan.
	 */
	private static void add(TendrilAnchor start, TendrilAnchor end, Root root, float scale, float partialTick) {
		TendrilRenderer.INSTANCE.add(new TendrilEffectData(start, end, rootConfig(root.seed(), scale), root.seed()), partialTick,
				root.outward());
	}

	private static Vec3 rootStartOffset(long gameTime, int index, long pulse) {
		double angle = animatedAngle(gameTime, index, pulse) + 0.18D;
		// Rooted around the waist and lower body of the upright Seed.
		double height = 0.04D - 0.07D * (0.5D + 0.5D * Math.sin(index * 2.61D + pulse * 0.47D));
		return new Vec3(Math.cos(angle) * 0.035D, height, Math.sin(angle) * 0.035D);
	}

	private static Vec3 rootEndOffset(long gameTime, int index, long pulse) {
		double angle = animatedAngle(gameTime, index, pulse);
		double sideAngle = angle + Math.PI * 0.5D;
		double sway = Math.sin(gameTime * 0.13D + index * 1.71D) * 0.05D;
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
		return 0.09D + 0.13D * (0.5D + 0.5D * Math.sin(index * 1.91D + pulse * 0.73D));
	}

	private static double rootDrop(int index, long pulse) {
		return 0.36D + 0.28D * (0.5D + 0.5D * Math.cos(index * 2.17D + pulse * 0.61D));
	}

	private static TendrilEffectConfig rootConfig(long seed, float scale) {
		return TendrilEffectConfig.defaults()
				.withMode(TendrilEffectConfig.Mode.FREEFORM)
				.withColors(0xF0050003, 0xB8D10B1A)
				.withBlendColors(false)
				// Long-lived, so what the eye follows is a tentacle moving and not one being replaced.
				.withLifecycle(6, 20, 10)
				.withShape(24, 1, 0.024F * scale, 0.045F)
				.withBranching(0, 0, 0.12F, 0.45F)
				// Negative sag bows each one outward from the Seed before it hangs.
				.withWrithe(0.1F * scale, 0.2F, 0.13F * scale, -0.07F * scale)
				.withFixedSeed(true, seed);
	}

	private static long rootSeed(long identity, long pulse, int index) {
		long seed = 0x6A09E667F3BCC909L;
		seed ^= identity * 0x9E3779B97F4A7C15L;
		seed ^= pulse * 0xBF58476D1CE4E5B9L;
		seed ^= (long) index * 0x94D049BB133111EBL;
		return seed;
	}

	private record Root(Vec3 startOffset, Vec3 endOffset, Vec3 outward, long seed) {
	}
}
