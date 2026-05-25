package com.vincenthuto.hemomancy.common.entity.summon;

public enum MorphlingPolypLayer {
	FUNGAL("fungal", 0xD0648A42, "hemomancy:infected_fungus"),
	LEECHES("leeches", 0xD07A2020, "hemomancy:swollen_leech"),
	CHITINITE("chitinite", 0xD09A6F42, "hemomancy:chitinous_husk"),
	SERPENT("serpent", 0xD0853E28, "hemomancy:serpent_scale"),
	PESTS("pests", 0xD06F5B22, "minecraft:beetroot_seeds"),
	SPIDER("spider", 0xD0C8C8C8, "minecraft:string"),
	BAT("bat", 0xD048405D, "minecraft:phantom_membrane"),
	CUTTLEFISH("cuttlefish", 0xD05C4738, "minecraft:ink_sac"),
	TICK("tick", 0xD07B1818, "minecraft:fermented_spider_eye"),
	URCHIN("urchin", 0xD04D7C83, "minecraft:prismarine_shard"),
	CENTIPEDE("centipede", 0xD0A0462B, "minecraft:iron_nugget"),
	MOLE("mole", 0xD06B5543, "minecraft:raw_iron");

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
