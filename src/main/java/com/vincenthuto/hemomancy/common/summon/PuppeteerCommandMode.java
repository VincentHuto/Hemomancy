package com.vincenthuto.hemomancy.common.summon;

import java.util.Optional;

public enum PuppeteerCommandMode {
	FOLLOW("follow"),
	GUARD("guard"),
	HUNT("hunt"),
	PASSIVE("passive");

	private final String serializedName;

	PuppeteerCommandMode(String serializedName) {
		this.serializedName = serializedName;
	}

	public String serializedName() {
		return serializedName;
	}

	public boolean retainsAutomaticTarget() {
		return this == FOLLOW || this == GUARD || this == HUNT;
	}

	public boolean automaticallyDefendsOwner() {
		return this == FOLLOW;
	}

	public static PuppeteerCommandMode fromSerializedName(String value) {
		return tryParse(value).orElse(FOLLOW);
	}

	public static Optional<PuppeteerCommandMode> tryParse(String value) {
		if (value != null) {
			for (PuppeteerCommandMode mode : values()) {
				if (mode.serializedName.equalsIgnoreCase(value)) {
					return Optional.of(mode);
				}
			}
		}
		return Optional.empty();
	}
}
