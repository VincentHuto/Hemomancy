package com.vincenthuto.hemomancy.common.rite.harbinger;

public enum SeveredQliphothState {
	LIVING,
	OPEN,
	SEALED;

	public SeveredQliphothState sever() {
		return this == LIVING ? OPEN : this;
	}

	public SeveredQliphothState seal() {
		return this == OPEN ? SEALED : this;
	}

	public boolean isPortalOpen() {
		return this == OPEN;
	}

	public boolean isSealedTrophy() {
		return this == SEALED;
	}

	public static SeveredQliphothState byName(String name) {
		try {
			return valueOf(name);
		} catch (IllegalArgumentException | NullPointerException ignored) {
			return LIVING;
		}
	}
}
