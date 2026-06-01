package com.vincenthuto.hemomancy.common.item.harbinger.tool;

public enum EnumBloodGourdTiers {
	SIMPLE(1, 1000, 8, 1.0, 0.75, 0.0, "Steady Vessel"),
	CRIMSON(2, 1800, 12, 3.0, 1.25, 0.0, "Combat Siphon"),
	ASHEN(3, 3500, 20, 0.75, 1.0, 0.0, "Deep Reservoir"),
	HORN(4, 1500, 5, 5.0, 0.5, 0.0, "Burst Vessel"),
	RIB(5, 5000, 5, 2.5, 1.25, 0.25, "Living Marrow"),
	DRIED(0, 0, 0, 0, 0, 0, "Crafting Material");


	private final int tierLevel;
	private final float maxVolume;
	private final int enchantability;
	private final double transferRate;
	private final double killSiphonMultiplier;
	private final double passiveGenerationRate;
	private final String roleName;

	private EnumBloodGourdTiers(int tierLevelIn, float maxVolumeIn, int enchantabilityIn, double transferRateIn,
			double killSiphonMultiplierIn, double passiveGenerationRateIn, String roleNameIn) {
		this.tierLevel = tierLevelIn;
		this.maxVolume = maxVolumeIn;
		this.enchantability = enchantabilityIn;
		this.transferRate = transferRateIn;
		this.killSiphonMultiplier = killSiphonMultiplierIn;
		this.passiveGenerationRate = passiveGenerationRateIn;
		this.roleName = roleNameIn;
	}

	public int getEnchantability() {
		return this.enchantability;
	}

	public float getMaxVolume() {
		return maxVolume;
	}

	public double getTransferRate() {
		return transferRate;
	}

	public double getKillSiphonMultiplier() {
		return killSiphonMultiplier;
	}

	public double getPassiveGenerationRate() {
		return passiveGenerationRate;
	}

	public String getRoleName() {
		return roleName;
	}

	public int getTierLevel() {
		return tierLevel;
	}
}
