package com.vincenthuto.hemomancy.common.worldgen;

public enum ChamberVisitMode {
	DREAM,
	TIMED_CHAIR,
	ATTUNED,
	ADMIN;

	public boolean timed() {
		return this == DREAM || this == TIMED_CHAIR;
	}
}
