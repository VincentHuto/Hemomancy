package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.Locale;

public record MaterialGate(Type type, int degree, float threshold) {
	private static final float[] PURITY_TIERS = {10.0F, 15.0F, 25.0F, 35.0F, 50.0F};
	private static final float[] CLARITY_TIERS = {10.0F, 25.0F, 35.0F, 50.0F};

	public enum Type {
		ALWAYS,
		DEGREE,
		PURITY,
		CLARITY
	}

	public static MaterialGate always() {
		return new MaterialGate(Type.ALWAYS, 0, 0.0F);
	}

	public static MaterialGate degree(int degree) {
		return new MaterialGate(Type.DEGREE, degree, 0.0F);
	}

	public static MaterialGate purity(float purity) {
		return new MaterialGate(Type.PURITY, 0, purity);
	}

	public static MaterialGate clarity(float clarity) {
		return new MaterialGate(Type.CLARITY, 0, clarity);
	}

	public MaterialVisibility visibilityFor(MaterialAtlasPath path, int playerDegree, float purity, float clarity) {
		return switch (type) {
			case ALWAYS -> MaterialVisibility.UNLOCKED;
			case DEGREE -> {
				if (playerDegree >= degree) {
					yield MaterialVisibility.UNLOCKED;
				}
				yield degree == playerDegree + 1 ? MaterialVisibility.NEXT_PREVIEW : MaterialVisibility.HIDDEN;
			}
			case PURITY -> thresholdVisibility(threshold, purity, PURITY_TIERS);
			case CLARITY -> thresholdVisibility(threshold, clarity, CLARITY_TIERS);
		};
	}

	public String requirementText() {
		return switch (type) {
			case ALWAYS -> "";
			case DEGREE -> "Requires Degree " + degree;
			case PURITY -> "Requires Purity " + compact(threshold);
			case CLARITY -> "Requires Clarity " + compact(threshold);
		};
	}

	private static MaterialVisibility thresholdVisibility(float required, float current, float[] tiers) {
		if (current + 0.001F >= required) {
			return MaterialVisibility.UNLOCKED;
		}
		float next = nextTierAfter(current, tiers);
		return Math.abs(required - next) < 0.001F ? MaterialVisibility.NEXT_PREVIEW : MaterialVisibility.HIDDEN;
	}

	private static float nextTierAfter(float current, float[] tiers) {
		for (float tier : tiers) {
			if (tier > current + 0.001F) {
				return tier;
			}
		}
		return Float.POSITIVE_INFINITY;
	}

	private static String compact(float value) {
		if (Math.abs(value - Math.round(value)) < 0.001F) {
			return Integer.toString(Math.round(value));
		}
		return String.format(Locale.ROOT, "%.1f", value);
	}
}
