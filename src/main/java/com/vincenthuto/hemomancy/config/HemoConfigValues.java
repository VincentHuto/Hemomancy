package com.vincenthuto.hemomancy.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class HemoConfigValues {
	private HemoConfigValues() {
	}

	public static boolean get(ModConfigSpec.BooleanValue value, boolean fallback) {
		return value == null ? fallback : value.get();
	}

	public static int get(ModConfigSpec.IntValue value, int fallback) {
		return value == null ? fallback : value.get();
	}

	public static double get(ModConfigSpec.DoubleValue value, double fallback) {
		return value == null ? fallback : value.get();
	}
}
