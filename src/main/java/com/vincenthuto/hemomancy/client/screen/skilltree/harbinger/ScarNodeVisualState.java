package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

enum ScarNodeVisualState {
	LOCKED,
	UNLEARNED,
	KNOWN,
	ACTIVE;

	static ScarNodeVisualState resolve(boolean degreeLocked, boolean known, boolean active) {
		if (degreeLocked) return LOCKED;
		if (active) return ACTIVE;
		return known ? KNOWN : UNLEARNED;
	}
}
