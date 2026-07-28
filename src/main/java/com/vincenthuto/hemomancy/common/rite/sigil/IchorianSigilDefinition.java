package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record IchorianSigilDefinition(
		ResourceLocation id,
		Kind kind,
		int tier,
		int color,
		String name,
		String purpose,
		int stability,
		int capacityMl,
		List<Node> nodes,
		List<Connection> connections,
		Optional<IchorianSigilAnatomy> awakenedForm) {

	public IchorianSigilDefinition {
		nodes = List.copyOf(nodes);
		connections = List.copyOf(connections);
		awakenedForm = awakenedForm == null ? Optional.empty() : awakenedForm;
	}

	public IchorianSigilDefinition(ResourceLocation id, Kind kind, int tier, int color,
			String name, String purpose, int stability, int capacityMl, List<Node> nodes) {
		this(id, kind, tier, color, name, purpose, stability, capacityMl,
				nodes, List.of(), Optional.empty());
	}

	public int bloodCostMl() {
		return nodes.size() * 50;
	}

	public enum Kind {
		SUPPORT,
		RESPONSE
	}

	public record Node(double x, double z) {
	}

	public record Connection(int from, int to) {
	}
}
