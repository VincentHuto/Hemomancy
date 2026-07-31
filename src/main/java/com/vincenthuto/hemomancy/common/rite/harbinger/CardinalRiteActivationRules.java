package com.vincenthuto.hemomancy.common.rite.harbinger;

/**
 * Intent and pattern-selection rules for deliberately beginning a Cardinal
 * Rite. The activation cell is the occupied ground-layer cell nearest the
 * horizontal center, allowing both solid and hollow altar designs.
 */
public final class CardinalRiteActivationRules {
	public enum Trigger {
		LIVING_STAFF_BLOCK_USE,
		SANGUINE_FORMATION_BLOCK_USE,
		HEMATIC_MEDIUM_BLOCK_USE,
		BLOOD_CRAFTING_KEY
	}

	public enum ActivationAttempt {
		NOT_HANDLED,
		HANDLED,
		STARTED;

		public boolean handled() {
			return this != NOT_HANDLED;
		}

		public boolean shouldConsumeActivator(boolean creativeMode) {
			return this == STARTED && !creativeMode;
		}
	}

	private CardinalRiteActivationRules() {
	}

	public static boolean mayInitiate(Trigger trigger, boolean unstained, int requiredDegree) {
		if (unstained) return trigger == Trigger.BLOOD_CRAFTING_KEY;
		if (requiredDegree < 1) return trigger == Trigger.SANGUINE_FORMATION_BLOCK_USE;
		return trigger == Trigger.LIVING_STAFF_BLOCK_USE;
	}

	public static boolean mayInitiate(Trigger trigger, boolean unstained, int requiredDegree,
			String focusMode) {
		if (unstained) return trigger == Trigger.BLOOD_CRAFTING_KEY;
		if ("temple_medium".equals(focusMode)) return false;
		if ("hematic_medium".equals(focusMode)) {
			return trigger == Trigger.HEMATIC_MEDIUM_BLOCK_USE;
		}
		if ("living_staff".equals(focusMode)) {
			return trigger == Trigger.LIVING_STAFF_BLOCK_USE;
		}
		return mayInitiate(trigger, false, requiredDegree);
	}

	public static Cell activationCell(String[][] pattern) {
		if (pattern == null || pattern.length == 0 || pattern[0] == null || pattern[0].length == 0) {
			return null;
		}
		int depth = pattern.length;
		int height = pattern[0].length;
		int width = pattern[0][0].length();
		double centerX = (width - 1) * 0.5D;
		double centerZ = (depth - 1) * 0.5D;

		for (int y = height - 1; y >= 0; y--) {
			Cell best = null;
			double bestDistance = Double.MAX_VALUE;
			double bestDepthOffset = Double.MAX_VALUE;
			for (int z = 0; z < depth; z++) {
				if (pattern[z] == null || y >= pattern[z].length || pattern[z][y] == null) continue;
				String row = pattern[z][y];
				for (int x = 0; x < row.length(); x++) {
					if (row.charAt(x) == ' ') continue;
					double dx = x - centerX;
					double dz = z - centerZ;
					double distance = dx * dx + dz * dz;
					double depthOffset = Math.abs(dz);
					if (distance < bestDistance
							|| distance == bestDistance && depthOffset < bestDepthOffset) {
						best = new Cell(x, y, z);
						bestDistance = distance;
						bestDepthOffset = depthOffset;
					}
				}
			}
			if (best != null) return best;
		}
		return null;
	}

	public record Cell(int x, int y, int z) {
	}
}
