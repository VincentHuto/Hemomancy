package com.vincenthuto.hemomancy.item;

public enum EnumBloodGourdTiers {

	SIMPLE(1, 500, 8), CRIMSON(2, 1500, 12), ASHEN(3, 2500, 20), HORN(4, 5000, 5);

	private final int tierLevel;
	private final float maxVolume;
	private final int enchantability;

	private EnumBloodGourdTiers(int tierLevelIn, float maxVolumeIn, int enchantabilityIn) {
		this.tierLevel = tierLevelIn;
		this.maxVolume = maxVolumeIn;
		this.enchantability = enchantabilityIn;
	}

	public int getEnchantability() {
		return this.enchantability;
	}

	public float getMaxVolume() {
		return maxVolume;
	}

	public int getTierLevel() {
		return tierLevel;
	}
}
