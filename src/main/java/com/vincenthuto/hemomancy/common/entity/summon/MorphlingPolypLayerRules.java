package com.vincenthuto.hemomancy.common.entity.summon;

import java.util.*;

public final class MorphlingPolypLayerRules {
	public static final int MAX_NATURAL_LAYERS = 3;

	private MorphlingPolypLayerRules() {
	}

	public static SpawnContext context() {
		return new SpawnContext();
	}

	public static EnumSet<MorphlingPolypLayer> candidateLayers(SpawnContext context) {
		EnumSet<MorphlingPolypLayer> candidates = EnumSet.noneOf(MorphlingPolypLayer.class);
		if (context.inWater || context.ocean) {
			candidates.add(MorphlingPolypLayer.CUTTLEFISH);
			candidates.add(MorphlingPolypLayer.URCHIN);
		}
		if (context.riverOrSwamp || context.swamp) {
			candidates.add(MorphlingPolypLayer.LEECHES);
			candidates.add(MorphlingPolypLayer.TICK);
			candidates.add(MorphlingPolypLayer.FUNGAL);
		}
		if (context.desertOrBadlands) {
			candidates.add(MorphlingPolypLayer.SERPENT);
			candidates.add(MorphlingPolypLayer.CHITINITE);
		}
		if (context.jungle) {
			candidates.add(MorphlingPolypLayer.SERPENT);
			candidates.add(MorphlingPolypLayer.TICK);
		}
		if (context.forest || context.darkForest || context.taigaOrOldGrowth) {
			candidates.add(MorphlingPolypLayer.SPIDER);
			candidates.add(MorphlingPolypLayer.TICK);
		}
		if (context.darkForest || context.mushroom || context.fungalNearby || context.taigaOrOldGrowth) {
			candidates.add(MorphlingPolypLayer.FUNGAL);
		}
		if (context.openLand) {
			candidates.add(MorphlingPolypLayer.PESTS);
		}
		if (context.mountainOrStony || context.stoneNearby) {
			candidates.add(MorphlingPolypLayer.CENTIPEDE);
			candidates.add(MorphlingPolypLayer.CHITINITE);
		}
		if (context.underground || context.lowLight) {
			candidates.add(MorphlingPolypLayer.BAT);
			candidates.add(MorphlingPolypLayer.SPIDER);
		}
		if (context.underground && (context.dirtNearby || context.mountainOrStony || context.stoneNearby)) {
			candidates.add(MorphlingPolypLayer.MOLE);
		}
		if (context.underground && context.stoneNearby) {
			candidates.add(MorphlingPolypLayer.CENTIPEDE);
		}
		if (candidates.isEmpty()) {
			candidates.add(MorphlingPolypLayer.PESTS);
		}
		return candidates;
	}

	public static int selectLayerMask(Collection<MorphlingPolypLayer> candidates, Random random, int maxLayers) {
		if (candidates == null || candidates.isEmpty()) {
			return MorphlingPolypLayer.PESTS.bit();
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
