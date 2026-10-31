package com.vincenthuto.hemomancy.common.entity.summon;

/**
 * The wild polyp families, one per house strain, named for the partner organism the strain's fungus
 * lives with (shallow to deep). A family's hint item is the catalyst its strain's incubation asks for.
 */
public enum MorphlingPolypLayer {
	SUN_ALGA("sun_alga", 0xD0E8B040, "hemomancy:hematic_algal_crust"),
	CORALLINE_ALGA("coralline_alga", 0xD0B08C88, "hemomancy:chitinous_husk"),
	GUT_BACTERIA("gut_bacteria", 0xD0B0303A, "hemomancy:raw_pelagic_herring"),
	LIGHT_BACTERIA("light_bacteria", 0xD08A7CE0, "hemomancy:ganglion_cluster"),
	BLACK_YEAST("black_yeast", 0xD0302C34, "minecraft:ink_sac"),
	COLD_BACTERIA("cold_bacteria", 0xD09DB8CC, "hemomancy:cleansing_hemolymph"),
	BONE_BACTERIA("bone_bacteria", 0xD0E6DED0, "hemomancy:bone_worm_colony"),
	VENT_BACTERIA("vent_bacteria", 0xD0E06A28, "hemomancy:giant_tube_worm_colony");

	private final String serializedName;
	private final int packedColor;
	private final String hintItemId;

	MorphlingPolypLayer(String serializedName, int packedColor, String hintItemId) {
		this.serializedName = serializedName;
		this.packedColor = packedColor;
		this.hintItemId = hintItemId;
	}

	public String serializedName() {
		return this.serializedName;
	}

	public int bit() {
		return 1 << this.ordinal();
	}

	public int packedColor() {
		return this.packedColor;
	}

	public String hintItemId() {
		return this.hintItemId;
	}
}
