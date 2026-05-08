package com.vincenthuto.hemomancy.common.item.harbinger.tool;

public final class BloodGourdTierStatsTest {
	private BloodGourdTierStatsTest() {
	}

	public static void main(String[] args) {
		assertDouble("white capacity", 1000.0, EnumBloodGourdTiers.SIMPLE.getMaxVolume());
		assertDouble("white steady flow", 1.0, EnumBloodGourdTiers.SIMPLE.getTransferRate());
		assertDouble("white modest kill siphon", 0.75, EnumBloodGourdTiers.SIMPLE.getKillSiphonMultiplier());
		assertDouble("white does not generate blood", 0.0, EnumBloodGourdTiers.SIMPLE.getPassiveGenerationRate());

		assertDouble("red combat capacity", 1800.0, EnumBloodGourdTiers.CRIMSON.getMaxVolume());
		assertDouble("red fast flow", 3.0, EnumBloodGourdTiers.CRIMSON.getTransferRate());
		assertDouble("red aggressive kill siphon", 1.25, EnumBloodGourdTiers.CRIMSON.getKillSiphonMultiplier());

		assertDouble("black reservoir capacity", 3500.0, EnumBloodGourdTiers.ASHEN.getMaxVolume());
		assertDouble("black reluctant flow", 0.75, EnumBloodGourdTiers.ASHEN.getTransferRate());
		assertDouble("black full kill siphon", 1.0, EnumBloodGourdTiers.ASHEN.getKillSiphonMultiplier());

		assertDouble("horn burst flow", 5.0, EnumBloodGourdTiers.HORN.getTransferRate());
		assertDouble("horn weak kill siphon", 0.5, EnumBloodGourdTiers.HORN.getKillSiphonMultiplier());

		assertDouble("rib capacity", 5000.0, EnumBloodGourdTiers.RIB.getMaxVolume());
		assertDouble("rib covenant flow", 2.5, EnumBloodGourdTiers.RIB.getTransferRate());
		assertDouble("rib strong kill siphon", 1.25, EnumBloodGourdTiers.RIB.getKillSiphonMultiplier());
		assertDouble("rib grows marrow blood", 0.25, EnumBloodGourdTiers.RIB.getPassiveGenerationRate());
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
