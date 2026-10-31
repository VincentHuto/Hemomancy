package com.vincenthuto.hemomancy.common.entity.summon;

import java.util.*;

public final class MorphlingPolypLayerRules {
	public static final int MAX_NATURAL_LAYERS = 3;

	private MorphlingPolypLayerRules() {
	}

	public static SpawnContext context() {
		return new SpawnContext();
	}

	/**
	 * Which partner families a wild polyp can carry where it spawns. Each family follows its strain's
	 * house water where the land has an analogue: sunlit open ground and shallows for the sun alga,
	 * stone for the coralline alga, cold or high ground for the cold bacteria, heat for the vent bacteria.
	 */
	public static EnumSet<MorphlingPolypLayer> candidateLayers(SpawnContext context) {
		EnumSet<MorphlingPolypLayer> candidates = EnumSet.noneOf(MorphlingPolypLayer.class);
		if (context.inWater || context.ocean) {
			candidates.add(MorphlingPolypLayer.SUN_ALGA);
			candidates.add(MorphlingPolypLayer.GUT_BACTERIA);
			candidates.add(MorphlingPolypLayer.LIGHT_BACTERIA);
		}
		if (context.riverOrSwamp || context.swamp) {
			candidates.add(MorphlingPolypLayer.GUT_BACTERIA);
			candidates.add(MorphlingPolypLayer.BLACK_YEAST);
		}
		if (context.desertOrBadlands || context.jungle) {
			candidates.add(MorphlingPolypLayer.VENT_BACTERIA);
			candidates.add(MorphlingPolypLayer.BONE_BACTERIA);
		}
		if (context.forest || context.darkForest) {
			candidates.add(MorphlingPolypLayer.BONE_BACTERIA);
			candidates.add(MorphlingPolypLayer.LIGHT_BACTERIA);
		}
		if (context.darkForest || context.mushroom || context.fungalNearby) {
			candidates.add(MorphlingPolypLayer.BLACK_YEAST);
		}
		if (context.taigaOrOldGrowth) {
			candidates.add(MorphlingPolypLayer.COLD_BACTERIA);
		}
		if (context.openLand) {
			candidates.add(MorphlingPolypLayer.SUN_ALGA);
		}
		if (context.mountainOrStony || context.stoneNearby) {
			candidates.add(MorphlingPolypLayer.CORALLINE_ALGA);
			candidates.add(MorphlingPolypLayer.COLD_BACTERIA);
		}
		if (context.underground || context.lowLight) {
			candidates.add(MorphlingPolypLayer.BLACK_YEAST);
			candidates.add(MorphlingPolypLayer.LIGHT_BACTERIA);
		}
		if (context.underground && (context.dirtNearby || context.mountainOrStony || context.stoneNearby)) {
			candidates.add(MorphlingPolypLayer.BONE_BACTERIA);
		}
		if (context.underground && context.stoneNearby) {
			candidates.add(MorphlingPolypLayer.VENT_BACTERIA);
		}
		if (candidates.isEmpty()) {
			candidates.add(MorphlingPolypLayer.SUN_ALGA);
		}
		return candidates;
	}

	public static int selectLayerMask(Collection<MorphlingPolypLayer> candidates, Random random, int maxLayers) {
		if (candidates == null || candidates.isEmpty()) {
			return MorphlingPolypLayer.SUN_ALGA.bit();
		}
		List<MorphlingPolypLayer> pool = new ArrayList<>(candidates);
		int layersToPick = Math.max(1, Math.min(Math.min(maxLayers, MAX_NATURAL_LAYERS), pool.size()));
		int mask = 0;
		for (int i = 0; i < layersToPick; i++) {
			MorphlingPolypLayer picked = pool.remove(random.nextInt(pool.size()));
			mask |= picked.bit();
		}
		return sanitizeMask(mask);
	}

	public static List<MorphlingPolypLayer> layersFromMask(int mask) {
		int sanitized = sanitizeMask(mask);
		List<MorphlingPolypLayer> layers = new ArrayList<>();
		for (MorphlingPolypLayer layer : MorphlingPolypLayer.values()) {
			if ((sanitized & layer.bit()) != 0) {
				layers.add(layer);
			}
		}
		return layers;
	}

	public static int maskFor(Collection<MorphlingPolypLayer> layers) {
		int mask = 0;
		for (MorphlingPolypLayer layer : layers) {
			mask |= layer.bit();
		}
		return sanitizeMask(mask);
	}

	public static int sanitizeMask(int mask) {
		int allowed = 0;
		for (MorphlingPolypLayer layer : MorphlingPolypLayer.values()) {
			allowed |= layer.bit();
		}
		return mask & allowed;
	}

	public static boolean hasLayer(int mask, MorphlingPolypLayer layer) {
		return (sanitizeMask(mask) & layer.bit()) != 0;
	}

	public static final class SpawnContext {
		private boolean inWater;
		private boolean ocean;
		private boolean riverOrSwamp;
		private boolean swamp;
		private boolean desertOrBadlands;
		private boolean jungle;
		private boolean forest;
		private boolean darkForest;
		private boolean taigaOrOldGrowth;
		private boolean mushroom;
		private boolean openLand;
		private boolean underground;
		private boolean lowLight;
		private boolean stoneNearby;
		private boolean dirtNearby;
		private boolean mountainOrStony;
		private boolean fungalNearby;

		public SpawnContext inWater() {
			this.inWater = true;
			return this;
		}

		public SpawnContext ocean() {
			this.ocean = true;
			return this;
		}

		public SpawnContext riverOrSwamp() {
			this.riverOrSwamp = true;
			return this;
		}

		public SpawnContext swamp() {
			this.swamp = true;
			return this;
		}

		public SpawnContext desertOrBadlands() {
			this.desertOrBadlands = true;
			return this;
		}

		public SpawnContext jungle() {
			this.jungle = true;
			return this;
		}

		public SpawnContext forest() {
			this.forest = true;
			return this;
		}

		public SpawnContext darkForest() {
			this.darkForest = true;
			return this;
		}

		public SpawnContext taigaOrOldGrowth() {
			this.taigaOrOldGrowth = true;
			return this;
		}

		public SpawnContext mushroom() {
			this.mushroom = true;
			return this;
		}

		public SpawnContext openLand() {
			this.openLand = true;
			return this;
		}

		public SpawnContext underground() {
			this.underground = true;
			return this;
		}

		public SpawnContext lowLight() {
			this.lowLight = true;
			return this;
		}

		public SpawnContext stoneNearby() {
			this.stoneNearby = true;
			return this;
		}

		public SpawnContext dirtNearby() {
			this.dirtNearby = true;
			return this;
		}

		public SpawnContext mountainOrStony() {
			this.mountainOrStony = true;
			return this;
		}

		public SpawnContext fungalNearby() {
			this.fungalNearby = true;
			return this;
		}
	}
}
