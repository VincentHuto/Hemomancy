package com.vincenthuto.hemomancy.common.rite.sigil;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record IchorianSigilDefinition(
		ResourceLocation id,
		Kind kind,
		int tier,
		int color,
		String name,
		String purpose,
		int stability,
		int capacityMl,
		List<Node> nodes) {

	public IchorianSigilDefinition {
		nodes = List.copyOf(nodes);
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
}
