package com.vincenthuto.hemomancy.client.render.world;

/** Controls the short-lived completion message without ending the projection itself. */
public final class MnemonicBlueprintCompletionNotice {
	private static final int DISPLAY_TICKS = 50;

	private boolean complete;
	private int ticksRemaining;

	public void updateRemaining(int remaining) {
		boolean nowComplete = remaining <= 0;
		if (nowComplete && !complete) ticksRemaining = DISPLAY_TICKS;
		else if (!nowComplete) ticksRemaining = 0;
		complete = nowComplete;
	}

	public void tick() {
		if (ticksRemaining > 0) ticksRemaining--;
	}

	public boolean shouldRender(int remaining) {
		return remaining > 0 || ticksRemaining > 0;
	}

	public void reset() {
		complete = false;
		ticksRemaining = 0;
	}
}
