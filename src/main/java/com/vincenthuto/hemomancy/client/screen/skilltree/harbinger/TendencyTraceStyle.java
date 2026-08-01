package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

enum TendencyTraceStyle {
	LOCKED,
	UNKNOWN,
	KNOWN;

	static TendencyTraceStyle resolve(boolean fromKnown, boolean fromLocked,
	                                  boolean toKnown, boolean toLocked) {
		if (fromLocked || toLocked) return LOCKED;
		return fromKnown && toKnown ? KNOWN : UNKNOWN;
	}
}
