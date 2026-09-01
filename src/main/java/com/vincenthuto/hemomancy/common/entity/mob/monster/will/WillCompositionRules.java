package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import java.util.*;

public final class WillCompositionRules {
	private WillCompositionRules() {
	}

	public static int tierFor(int degree, boolean qliphothCommunionDone) {
		if (degree < 4) return 0;
		if (degree == 4) return 1;
		if (degree == 5) return 2;
		if (degree == 6) return 3;
		return qliphothCommunionDone ? 4 : 3;
	}

	public static Composition compose(int tier, Random random, int nearbyPlayerCount) {
		int extraPlayers = Math.max(0, Math.min(2, nearbyPlayerCount - 1));
		return switch (Math.max(1, Math.min(4, tier))) {
		case 1 -> new Composition(1, 1 + extraPlayers, 1, false);
		case 2 -> new Composition(2, 1 + random.nextInt(2) + extraPlayers, 2, false);
		case 3 -> new Composition(3, random.nextInt(2) + extraPlayers, 2, true);
		default -> new Composition(4, 2 + random.nextInt(2) + extraPlayers, 2, true);
		};
	}

	public static Optional<PlayerSnapshot> chooseTarget(List<PlayerSnapshot> players) {
		return players.stream().max(Comparator.comparingInt(PlayerSnapshot::degree));
	}

	public record Composition(int tier, int brokenCount, int brokenTier, boolean sentPresent) {
	}

	public record PlayerSnapshot(UUID id, int degree) {
	}
}
