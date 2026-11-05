package com.vincenthuto.hemomancy.common.encounter;

import java.util.LinkedHashSet;
import java.util.Set;

/** Pure geometry for non-destructive, outward-only Chamber floor growth. */
public final class ChamberExpansionRules {
	private ChamberExpansionRules() {
	}

	public static int nextBuiltRadius(int builtRadius, int requestedRadius) {
		return Math.max(builtRadius, requestedRadius);
	}

	/** The floor cells a room of {@code expandedRadius} has that a room of {@code previousRadius} does not. */
	public static Set<Offset> floorBand(int previousRadius, int expandedRadius) {
		Set<Offset> offsets = new LinkedHashSet<>();
		if (expandedRadius <= previousRadius) return offsets;
		for (int x = -expandedRadius; x <= expandedRadius; x++) {
			for (int z = -expandedRadius; z <= ChamberRoomShape.reach(expandedRadius); z++) {
				if (ChamberRoomShape.contains(x, z, expandedRadius) && !ChamberRoomShape.contains(x, z, previousRadius)) {
					offsets.add(new Offset(x, z));
				}
			}
		}
		return offsets;
	}

	/** The four light markers, set into the circle on its diagonals. */
	public static Set<Offset> markerOffsets(int radius) {
		int inset = Math.max(1, Math.round(radius * 0.66F));
		return Set.of(new Offset(inset, inset), new Offset(-inset, inset),
				new Offset(inset, -inset), new Offset(-inset, -inset));
	}

	public record Offset(int x, int z) {
	}
}
