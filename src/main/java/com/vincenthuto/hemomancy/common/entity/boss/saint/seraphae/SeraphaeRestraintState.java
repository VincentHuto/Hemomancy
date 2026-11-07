package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

/**
 * How her irons travel to the client in two ints: six slots (the four binding restraints in {@link SeraphaeRestraint}
 * order, then the two reserves), three bits of state each, and four bits of re-binding progress each. The HUD's
 * restraints row and the chain geometry read them. Pure, so it is JVM-tested.
 */
public final class SeraphaeRestraintState {
	public static final int SLOTS = 6;
	public static final int RESERVE_SLOT = 4;
	public static final int ABSENT = 0, HELD = 1, SLIPPED = 2, BINDING = 3, DORMANT = 4;
	private static final int STATE_BITS = 3;
	private static final int PROGRESS_BITS = 4;
	private static final int PROGRESS_MAX = (1 << PROGRESS_BITS) - 1;

	private SeraphaeRestraintState() {
	}

	public static int pack(int[] states) {
		int packed = 0;
		for (int slot = 0; slot < Math.min(SLOTS, states.length); slot++) {
			packed |= (states[slot] & ((1 << STATE_BITS) - 1)) << (slot * STATE_BITS);
		}
		return packed;
	}

	public static int state(int packed, int slot) {
		return (packed >>> (slot * STATE_BITS)) & ((1 << STATE_BITS) - 1);
	}

	public static int packProgress(float[] progress) {
		int packed = 0;
		for (int slot = 0; slot < Math.min(SLOTS, progress.length); slot++) {
			int step = Math.round(Math.max(0.0F, Math.min(1.0F, progress[slot])) * PROGRESS_MAX);
			packed |= step << (slot * PROGRESS_BITS);
		}
		return packed;
	}

	public static float progress(int packed, int slot) {
		return ((packed >>> (slot * PROGRESS_BITS)) & PROGRESS_MAX) / (float) PROGRESS_MAX;
	}

	/** Slipped binding anchors: each lengthens her chain. */
	public static int slipped(int packed) {
		int count = 0;
		for (int slot = 0; slot < RESERVE_SLOT; slot++) {
			int state = state(packed, slot);
			if (state == SLIPPED || state == BINDING) count++;
		}
		return count;
	}

	/** Woken reserves that hold her waist: each shortens her chain. */
	public static int reservesHeld(int packed) {
		int count = 0;
		for (int slot = RESERVE_SLOT; slot < SLOTS; slot++) if (state(packed, slot) == HELD) count++;
		return count;
	}
}
