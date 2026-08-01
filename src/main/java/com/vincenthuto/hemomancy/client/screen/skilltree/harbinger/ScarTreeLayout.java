package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Pure layout and lineage rules for the radial cerebral-scar families. */
final class ScarTreeLayout {
	static final int CENTER_X = 480;
	static final int CENTER_Y = 480;
	static final int CONTENT_W = 960;
	static final int CONTENT_H = 960;
	private static final int[] TIER_RADII = {200, 250, 300};
	private static final int SIDE_BRANCH_OFFSET = 40;
	private static final List<AuthoredNode> AUTHORED_NODES = List.of(
			authored("hemomancy:scar_heart", 480, 280),
			authored("hemomancy:scar_marrow", 480, 230, "hemomancy:scar_heart"),
			authored("hemomancy:scar_phoenix", 480, 180, "hemomancy:scar_marrow"),
			authored("hemomancy:scar_pyre", 621, 339),
			authored("hemomancy:scar_sol", 657, 303, "hemomancy:scar_pyre"),
			authored("hemomancy:scar_corona", 692, 268, "hemomancy:scar_sol"),
			authored("hemomancy:scar_feral", 680, 480),
			authored("hemomancy:scar_flux", 730, 480, "hemomancy:scar_feral"),
			authored("hemomancy:scar_chimera", 780, 480, "hemomancy:scar_flux"),
			authored("hemomancy:scar_halo", 621, 621),
			authored("hemomancy:scar_veil", 657, 657, "hemomancy:scar_halo"),
			authored("hemomancy:scar_transcendence", 692, 692, "hemomancy:scar_veil"),
			authored("hemomancy:scar_blight", 480, 680),
			authored("hemomancy:scar_wither", 480, 730, "hemomancy:scar_blight"),
			authored("hemomancy:scar_oblivion", 480, 780, "hemomancy:scar_wither"),
			authored("hemomancy:scar_rime", 339, 621),
			authored("hemomancy:scar_glacier", 303, 657, "hemomancy:scar_rime"),
			authored("hemomancy:scar_descendence", 268, 692, "hemomancy:scar_glacier"),
			authored("hemomancy:scar_thorn", 280, 480),
			authored("hemomancy:scar_anvil", 230, 480, "hemomancy:scar_thorn"),
			authored("hemomancy:scar_blood_honed", 230, 440, "hemomancy:scar_thorn"),
			authored("hemomancy:scar_crucible", 180, 480, "hemomancy:scar_anvil"),
			authored("hemomancy:scar_shade", 339, 339),
			authored("hemomancy:scar_moon", 303, 303, "hemomancy:scar_shade"),
			authored("hemomancy:scar_eye", 268, 268, "hemomancy:scar_moon")
	);

	private ScarTreeLayout() {}

	static Result arrange(List<Node> nodes) {
		return arrange(nodes, AUTHORED_NODES);
	}

	static Result arrange(List<Node> nodes, List<AuthoredNode> authoredNodes) {
		Map<String, Point> points = new LinkedHashMap<>();
		for (Node node : nodes) {
			double angle = Math.toRadians(-90.0 + node.tendency().ordinal() * 45.0);
			int radius = TIER_RADII[Math.max(0, Math.min(TIER_RADII.length - 1, node.tier() - 1))];
			int x = CENTER_X + (int) Math.round(Math.cos(angle) * radius);
			int y = CENTER_Y + (int) Math.round(Math.sin(angle) * radius);
			if (node.sideBranch()) {
				x += (int) Math.round(-Math.sin(angle) * SIDE_BRANCH_OFFSET);
				y += (int) Math.round(Math.cos(angle) * SIDE_BRANCH_OFFSET);
			}
			points.put(node.id(), new Point(x, y));
		}

		List<Edge> edges = new ArrayList<>();
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			List<Node> family = nodes.stream()
					.filter(node -> node.tendency() == tendency && !node.sideBranch())
					.sorted(Comparator.comparingInt(Node::tier).thenComparing(Node::id))
					.toList();
			for (int i = 1; i < family.size(); i++) {
				if (family.get(i).tier() > family.get(i - 1).tier()) {
					edges.add(new Edge(family.get(i - 1).id(), family.get(i).id()));
				}
			}
			Node root = family.stream().filter(node -> node.tier() == 1).findFirst().orElse(null);
			if (root != null) {
				nodes.stream()
						.filter(node -> node.tendency() == tendency && node.sideBranch())
						.sorted(Comparator.comparing(Node::id))
						.forEach(node -> edges.add(new Edge(root.id(), node.id())));
			}
		}
		Map<String, AuthoredNode> authoredById = new LinkedHashMap<>();
		for (AuthoredNode authored : authoredNodes) authoredById.put(authored.id(), authored);
		Set<String> presentIds = nodes.stream().map(Node::id).collect(java.util.stream.Collectors.toSet());
		for (AuthoredNode authored : authoredNodes) {
			if (presentIds.contains(authored.id())) points.put(authored.id(), new Point(authored.x(), authored.y()));
		}
		edges.removeIf(edge -> authoredById.containsKey(edge.toId()));
		for (AuthoredNode authored : authoredNodes) {
			if (!presentIds.contains(authored.id())) continue;
			for (String parentId : authored.parentIds()) {
				if (presentIds.contains(parentId)) edges.add(new Edge(parentId, authored.id()));
			}
		}
		return new Result(Map.copyOf(points), List.copyOf(edges));
	}

	private static AuthoredNode authored(String id, int x, int y, String... parentIds) {
		return new AuthoredNode(id, x, y, List.of(parentIds));
	}

	record Node(String id, EnumBloodTendency tendency, int tier, boolean sideBranch) {}
	record AuthoredNode(String id, int x, int y, List<String> parentIds) {
		AuthoredNode {
			parentIds = List.copyOf(parentIds);
		}
	}
	record Point(int x, int y) {}
	record Edge(String fromId, String toId) {}
	record Result(Map<String, Point> points, List<Edge> edges) {
		Point pointFor(String id) {
			return points.get(id);
		}
	}
}
