package com.vincenthuto.hemomancy.common.recipe;

public enum CardinalRiteType {
	MINOR(3, "minor", 100),
	LESSER(5, "lesser", 200),
	GREATER(7, "greater", 400),
	GRAND(9, "grand", 800);

	private final int size;
	private final String serializedName;
	private final int castingDurationTicks;

	CardinalRiteType(int size, String serializedName, int castingDurationTicks) {
		this.size = size;
		this.serializedName = serializedName;
		this.castingDurationTicks = castingDurationTicks;
	}

	public int getSize() {
		return size;
	}

	public String getSerializedName() {
		return serializedName;
	}

	public int getCastingDurationTicks() {
		return castingDurationTicks;
	}

	public static CardinalRiteType byName(String name) {
		for (CardinalRiteType type : values()) {
			if (type.serializedName.equalsIgnoreCase(name)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown cardinal rite type: " + name);
	}

	public static CardinalRiteType bySize(int size) {
		for (CardinalRiteType type : values()) {
			if (type.size == size) {
				return type;
			}
		}
		throw new IllegalArgumentException("No cardinal rite type for size: " + size + ". Valid sizes are 3, 5, 7, 9.");
	}
}
