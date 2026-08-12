package com.vincenthuto.hemomancy.common.event.worldevent;

/** Pure ordering and rejection policy for an Orb tossed beyond the platform. */
public final class OrbOfPerspectiveRules {
	private OrbOfPerspectiveRules() {
	}

	public static Activation activation(boolean handled, boolean ownerPresent, boolean beyondPlatform,
			boolean outsideOwnerCell,
			boolean vesperActive, boolean mycophantActive, int availableThemeCount) {
		if (handled) return Activation.HANDLED;
		if (!ownerPresent) return Activation.REJECT_NO_OWNER;
		if (vesperActive || mycophantActive) return Activation.REJECT_ENCOUNTER;
		if (outsideOwnerCell) return Activation.REJECT_OUTSIDE_CELL;
		if (!beyondPlatform) return Activation.REJECT_NOT_BEYOND_PLATFORM;
		if (availableThemeCount <= 1) return Activation.NO_OTHER_THEME;
		return Activation.CYCLE;
	}

	public static int nextThemeIndex(int currentIndex, int themeCount) {
		if (themeCount <= 0 || currentIndex < 0) return 0;
		return Math.floorMod(currentIndex + 1, themeCount);
	}

	public static ReturnTarget returnTarget(boolean inventoryAccepted) {
		return inventoryAccepted ? ReturnTarget.INVENTORY : ReturnTarget.BESIDE_OWNER;
	}

	public static SafePosition ownerlessReturnPosition(double cellX, double floorY, double cellZ) {
		return new SafePosition(cellX + 0.5D, floorY + 1.0D, cellZ + 0.5D);
	}

	public enum Activation {
		HANDLED,
		REJECT_NO_OWNER,
		REJECT_OUTSIDE_CELL,
		REJECT_NOT_BEYOND_PLATFORM,
		REJECT_ENCOUNTER,
		NO_OTHER_THEME,
		CYCLE
	}

	public enum ReturnTarget {
		INVENTORY,
		BESIDE_OWNER
	}

	public record SafePosition(double x, double y, double z) { }
}
