package com.vincenthuto.hemomancy.common.block.harbinger.rite;

public final class BrazierSpecialOfferingRules {
	public enum Effect {
		NONE,
		SCAR_TENDRILS,
		GRAFT_LIGHTNING,
		MEMORY_GLOW
	}

	private BrazierSpecialOfferingRules() {
	}

	public static Effect select(boolean lit, boolean successful, boolean scar, boolean graft, boolean memory) {
		if (!lit || !successful) {
			return Effect.NONE;
		}
		if (graft) {
			return Effect.GRAFT_LIGHTNING;
		}
		if (scar) {
			return Effect.SCAR_TENDRILS;
		}
		if (memory) {
			return Effect.MEMORY_GLOW;
		}
		return Effect.NONE;
	}

	public static boolean shouldEmitOnIgnition(boolean litBefore, boolean litAfter, boolean hasOffering) {
		return !litBefore && litAfter && hasOffering;
	}

	public static boolean shouldEmitPersistent(Effect effect, long gameTime) {
		return switch (effect) {
			case MEMORY_GLOW -> gameTime % 4L == 0L;
			case GRAFT_LIGHTNING -> gameTime % 8L == 0L;
			case SCAR_TENDRILS -> gameTime % 20L == 0L;
			case NONE -> false;
		};
	}
}
