package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

public enum PrismCuttleVariant {
	DEEP(0, 0x254C7B),
	SAND(1, 0xD8C988),
	CORAL(2, 0xC94C5E),
	KELP(3, 0x355E3B);

	private final int id;
	private final int color;

	PrismCuttleVariant(int id, int color) {
		this.id = id;
		this.color = color;
	}

	public int id() {
		return this.id;
	}

	public int color() {
		return this.color;
	}

	public static PrismCuttleVariant byId(int id) {
		for (PrismCuttleVariant variant : values()) {
			if (variant.id == id) {
				return variant;
			}
		}
		return DEEP;
	}

	public static PrismCuttleVariant fromBlockColor(int color) {
		PrismCuttleVariant closest = DEEP;
		int bestDistance = Integer.MAX_VALUE;
		for (PrismCuttleVariant variant : values()) {
			int distance = colorDistance(color, variant.color);
			if (distance < bestDistance) {
				bestDistance = distance;
				closest = variant;
			}
		}
		return closest;
	}

	private static int colorDistance(int first, int second) {
		int r = ((first >> 16) & 0xFF) - ((second >> 16) & 0xFF);
		int g = ((first >> 8) & 0xFF) - ((second >> 8) & 0xFF);
		int b = (first & 0xFF) - (second & 0xFF);
		return r * r + g * g + b * b;
	}
}
