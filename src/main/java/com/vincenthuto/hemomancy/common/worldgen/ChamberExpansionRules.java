package com.vincenthuto.hemomancy.common.worldgen;

import java.util.LinkedHashSet;
import java.util.Set;

/** Pure geometry for non-destructive, outward-only Chamber floor growth. */
public final class ChamberExpansionRules {
	private ChamberExpansionRules() {
	}

	public static int nextBuiltRadius(int builtRadius, int requestedRadius) {
		return Math.max(builtRadius, requestedRadius);
	}

	public static Set<Offset> floorBand(int previousRadius, int expandedRadius) {
		Set<Offset> offsets = new LinkedHashSet<>();
		if (expandedRadius <= previousRadius) return offsets;
		for (int x = -expandedRadius; x <= expandedRadius; x++) {
			for (int z = -expandedRadius; z <= expandedRadius; z++) {
				if (Math.max(Math.abs(x), Math.abs(z)) > previousRadius) {
					offsets.add(new Offset(x, z));
				}
			}
		}
		return offsets;
	}

	public static Set<Offset> markerOffsets(int radius) {
		int inset = Math.max(1, radius - 1);
		return Set.of(new Offset(inset, inset), new Offset(-inset, inset),
				new Offset(inset, -inset), new Offset(-inset, -inset));
	}

	public record Offset(int x, int z) {
	}
}
