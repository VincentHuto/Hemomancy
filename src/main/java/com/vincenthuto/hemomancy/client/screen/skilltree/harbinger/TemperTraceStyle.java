package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

enum TemperTraceStyle {
	LOCKED,
	UNKNOWN,
	KNOWN;

	static TemperTraceStyle resolve(boolean fromKnown, boolean fromLocked,
	                                  boolean toKnown, boolean toLocked) {
		if (fromLocked || toLocked) return LOCKED;
		return fromKnown && toKnown ? KNOWN : UNKNOWN;
	}
}
