package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Locale;

public enum EnumBloodGourdTiers {
	SIMPLE(1, 1000, 8, 1.0, 0.75, 0.0),
	CRIMSON(2, 1800, 12, 3.0, 1.25, 0.0),
	ASHEN(3, 3500, 20, 0.75, 1.0, 0.0),
	HORN(4, 1500, 5, 5.0, 0.5, 0.0),
	RIB(5, 5000, 5, 2.5, 1.25, 0.25),
	DRIED(0, 0, 0, 0, 0, 0);


	private final int tierLevel;
	private final float maxVolume;
	private final int enchantability;
	private final double transferRate;
	private final double killSiphonMultiplier;
	private final double passiveGenerationRate;

	private EnumBloodGourdTiers(int tierLevelIn, float maxVolumeIn, int enchantabilityIn, double transferRateIn,
			double killSiphonMultiplierIn, double passiveGenerationRateIn) {
		this.tierLevel = tierLevelIn;
		this.maxVolume = maxVolumeIn;
		this.enchantability = enchantabilityIn;
		this.transferRate = transferRateIn;
		this.killSiphonMultiplier = killSiphonMultiplierIn;
		this.passiveGenerationRate = passiveGenerationRateIn;
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

	public MutableComponent getRoleName() {
		return Component.translatable("tooltip.hemomancy.blood_gourd.role." + name().toLowerCase(Locale.ROOT));
	}

	public int getTierLevel() {
		return tierLevel;
	}
}
