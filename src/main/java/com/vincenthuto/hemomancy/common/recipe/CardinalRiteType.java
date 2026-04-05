package com.vincenthuto.hemomancy.common.recipe;

public enum CardinalRiteType {
	MINOR(3, "minor"),
	LESSER(5, "lesser"),
	GREATER(7, "greater"),
	GRAND(9, "grand");

	private final int size;
	private final String serializedName;

	CardinalRiteType(int size, String serializedName) {
		this.size = size;
		this.serializedName = serializedName;
	}

	public int getSize() {
		return size;
	}

	public String getSerializedName() {
		return serializedName;
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
