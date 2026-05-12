package com.vincenthuto.hemomancy.common.worldgen.terrablender;

import terrablender.api.ParameterUtils.Continentalness;
import terrablender.api.ParameterUtils.Erosion;
import terrablender.api.ParameterUtils.Humidity;
import terrablender.api.ParameterUtils.Temperature;
import terrablender.api.ParameterUtils.Weirdness;

public final class ErythrocoralReefClimateRules {
	private static final String[] ALLOWED_TEMPERATURES = {
			"WARM"
	};
	private static final String[] ALLOWED_HUMIDITIES = {
			"HUMID"
	};
	private static final String[] ALLOWED_CONTINENTALNESS = {
			"DEEP_OCEAN"
	};
	private static final String[] ALLOWED_EROSIONS = {
			"EROSION_6"
	};
	private static final String[] ALLOWED_WEIRDNESS = {
			"VALLEY"
	};

	private ErythrocoralReefClimateRules() {
	}

	static Continentalness[] continentalness() {
		return enumValues(ALLOWED_CONTINENTALNESS, Continentalness[]::new, Continentalness::valueOf);
	}

	static Temperature[] temperatures() {
		return enumValues(ALLOWED_TEMPERATURES, Temperature[]::new, Temperature::valueOf);
	}

	static Humidity[] humidities() {
		return enumValues(ALLOWED_HUMIDITIES, Humidity[]::new, Humidity::valueOf);
	}

	static Erosion[] erosions() {
		return enumValues(ALLOWED_EROSIONS, Erosion[]::new, Erosion::valueOf);
	}

	static Weirdness[] weirdnesses() {
		return enumValues(ALLOWED_WEIRDNESS, Weirdness[]::new, Weirdness::valueOf);
	}

	public static boolean allowsContinentalness(Continentalness continentalness) {
		return allowsContinentalnessName(continentalness.name());
	}

	public static boolean allowsTemperatureName(String temperatureName) {
		return contains(ALLOWED_TEMPERATURES, temperatureName);
	}

	public static boolean allowsHumidityName(String humidityName) {
		return contains(ALLOWED_HUMIDITIES, humidityName);
	}

	public static boolean allowsContinentalnessName(String continentalnessName) {
		return contains(ALLOWED_CONTINENTALNESS, continentalnessName);
	}

	public static boolean allowsErosionName(String erosionName) {
		return contains(ALLOWED_EROSIONS, erosionName);
	}

	public static boolean allowsWeirdnessName(String weirdnessName) {
		return contains(ALLOWED_WEIRDNESS, weirdnessName);
	}

	private static boolean contains(String[] allowedNames, String name) {
		for (String allowed : allowedNames) {
			if (allowed.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private static <T extends Enum<T>> T[] enumValues(String[] names, java.util.function.IntFunction<T[]> arrayFactory,
			java.util.function.Function<String, T> valueFactory) {
		T[] values = arrayFactory.apply(names.length);
		for (int i = 0; i < names.length; i++) {
			values[i] = valueFactory.apply(names[i]);
		}
		return values;
	}
}
