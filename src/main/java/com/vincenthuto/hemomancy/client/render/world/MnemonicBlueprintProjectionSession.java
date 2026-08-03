package com.vincenthuto.hemomancy.client.render.world;

import javax.annotation.Nullable;

/** Owns the client-world lifetime of one local mnemonic projection. */
final class MnemonicBlueprintProjectionSession<W> {
	private W activeWorld;
	private boolean active;

	void activate(W world) {
		activeWorld = world;
		active = world != null;
	}

	boolean isActive() {
		return active;
	}

	boolean clearIfWorldChanged(@Nullable W currentWorld) {
		if (!active || currentWorld == activeWorld) return false;
		clear();
		return true;
	}

	boolean clearIfComplete(int remaining) {
		if (!active || remaining > 0) return false;
		clear();
		return true;
	}

	boolean disconnect() {
		return clear();
	}

	boolean clear() {
		boolean wasActive = active;
		activeWorld = null;
		active = false;
		return wasActive;
	}
}
