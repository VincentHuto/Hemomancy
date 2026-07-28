package com.vincenthuto.hemomancy.common.rite.sigil;

import java.util.ArrayList;
import java.util.List;

public final class CardinalRiteSigilProgress {
	private CardinalRiteSigilProgress() {
	}

	public static List<Connection> completedConnections(
			List<IchorianSigilDefinition.Node> nodes, int completedNodes) {
		if (nodes == null || nodes.size() < 2 || completedNodes < 2) return List.of();
		int visibleNodes = Math.min(nodes.size(), completedNodes);
		List<Connection> result = new ArrayList<>(visibleNodes - 1);
		for (int index = 1; index < visibleNodes; index++) {
			result.add(new Connection(nodes.get(index - 1), nodes.get(index)));
		}
		return List.copyOf(result);
	}

	public record Connection(IchorianSigilDefinition.Node start, IchorianSigilDefinition.Node end) {
	}
}
