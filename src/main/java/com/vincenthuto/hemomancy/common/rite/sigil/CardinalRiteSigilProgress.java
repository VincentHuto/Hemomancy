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

	public static List<Connection> completedConnections(
			IchorianSigilDefinition definition, int completedNodes) {
		if (definition == null || definition.connections().isEmpty()) {
			return definition == null ? List.of()
					: completedConnections(definition.nodes(), completedNodes);
		}
		int visibleNodes = Math.min(definition.nodes().size(), Math.max(0, completedNodes));
		List<Connection> result = new ArrayList<>(definition.connections().size());
		for (IchorianSigilDefinition.Connection authored : definition.connections()) {
			if (authored.from() < 0 || authored.to() < 0
					|| authored.from() >= visibleNodes || authored.to() >= visibleNodes) continue;
			result.add(new Connection(
					definition.nodes().get(authored.from()),
					definition.nodes().get(authored.to())));
		}
		return List.copyOf(result);
	}

	public record Connection(IchorianSigilDefinition.Node start, IchorianSigilDefinition.Node end) {
	}
}
