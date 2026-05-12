package com.vincenthuto.hemomancy.common.worldgen;

public final class ErythrocoralReefTuning {
	public static final int PLACED_FEATURE_COUNT = 6;

	public static final int MIN_CLUSTERS_PER_PASS = 1;
	public static final int CLUSTER_VARIANCE = 2;
	public static final int CLUSTER_MIN_OFFSET = 3;
	public static final int CLUSTER_OFFSET_VARIANCE = 5;
	public static final int MIN_CLUSTER_RADIUS = 3;
	public static final int CLUSTER_RADIUS_VARIANCE = 2;
	public static final int STABLE_SHELF_MIN_MATCHES = 16;

	public static final int BLOOD_LANTERN_JELLY_WEIGHT = 36;
	public static final int BLOOD_LANTERN_JELLY_MIN_COUNT = 3;
	public static final int BLOOD_LANTERN_JELLY_MAX_COUNT = 6;
	public static final int BARBED_URCHIN_WEIGHT = 12;
	public static final int BARBED_URCHIN_MIN_COUNT = 1;
	public static final int BARBED_URCHIN_MAX_COUNT = 3;
	public static final int TROPICAL_FISH_WEIGHT = 16;
	public static final int TROPICAL_FISH_MIN_COUNT = 4;
	public static final int TROPICAL_FISH_MAX_COUNT = 8;
	public static final int PUFFERFISH_WEIGHT = 4;
	public static final int PUFFERFISH_MIN_COUNT = 1;
	public static final int PUFFERFISH_MAX_COUNT = 3;
	public static final int SQUID_WEIGHT = 2;
	public static final int SQUID_MIN_COUNT = 1;
	public static final int SQUID_MAX_COUNT = 3;
	public static final int DOLPHIN_WEIGHT = 1;
	public static final int DOLPHIN_MIN_COUNT = 1;
	public static final int DOLPHIN_MAX_COUNT = 2;
	public static final int MNEMONIC_WHALE_WEIGHT = 4;
	public static final int MNEMONIC_WHALE_MIN_COUNT = 1;
	public static final int MNEMONIC_WHALE_MAX_COUNT = 1;

	public static final int URCHIN_MIN_DEPTH_BELOW_SEA_LEVEL = 4;
	public static final int URCHIN_MAX_DEPTH_BELOW_SEA_LEVEL = 24;
	public static final int WHALE_MIN_DEPTH_BELOW_SEA_LEVEL = 6;
	public static final int WHALE_SHALLOW_WATER_PUSH_DEPTH = 8;

	private ErythrocoralReefTuning() {
	}

	public static int maxClusterRadius() {
		return MIN_CLUSTER_RADIUS + CLUSTER_RADIUS_VARIANCE - 1;
	}
}
