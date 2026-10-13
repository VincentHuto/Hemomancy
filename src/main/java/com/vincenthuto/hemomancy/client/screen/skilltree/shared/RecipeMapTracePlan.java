package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

record RecipeMapTracePlan(List<Ring> rings, List<Line> spokes, List<Connection> connections) {
	private static final int SPOKE_INNER_RADIUS = 32;
	private static final double STUB_CLEARANCE = RecipeMapLayout.NODE_SIZE * 0.6;

	RecipeMapTracePlan {
		rings = List.copyOf(rings);
		spokes = List.copyOf(spokes);
		connections = List.copyOf(connections);
	}

	static RecipeMapTracePlan build(RecipeMapLayout.Result layout, List<RecipeMapLink> links,
			Integer degreeFilter, String familyFilter, int accent, SkillTreeLayer layer) {
		List<Ring> rings = new ArrayList<>();
		for (int degree = 0; degree <= 8; degree++) {
			if (SkillTreeLayerRules.layerForDegree(degree) != layer) continue;
			rings.add(new Ring(layout.centerX(), layout.centerY(), RecipeMapLayout.ringRadius(degree),
					ConcentricRingStyle.withBaseAlpha(accent)));
		}

		List<Line> spokes = new ArrayList<>();
		int outerRadius = RecipeMapLayout.outerRingRadius(layer);
		List<RecipeMapLayout.Sector> sectors = layout.sectors(layer);
		for (RecipeMapLayout.Sector sector : sectors) spokes.add(spoke(layout, sector.startAngle(), outerRadius, accent));
		if (!sectors.isEmpty()) spokes.add(spoke(layout, sectors.getLast().endAngle(), outerRadius, accent));

		List<Connection> connections = new ArrayList<>();
		Set<RecipeMapKey> linkedInLayer = new HashSet<>();
		for (RecipeMapLink link : links) {
			RecipeMapLayout.NodeBounds from = layout.node(link.from());
			RecipeMapLayout.NodeBounds to = layout.node(link.to());
			if (from == null || to == null || !included(from.entry(), degreeFilter, familyFilter, layer)
					|| !included(to.entry(), degreeFilter, familyFilter, layer)) {
				continue;
			}
			connections.add(new Connection(from.centerX(), from.centerY(), to.centerX(), to.centerY(),
					link.kind(), withAlpha(accent, alpha(link.kind())), false));
			linkedInLayer.add(link.to());
		}
		if (layer == SkillTreeLayer.DEEP) {
			connections.addAll(hubStubs(layout, links, linkedInLayer, degreeFilter, familyFilter, accent));
		}
		return new RecipeMapTracePlan(rings, spokes, connections);
	}

	/**
	 * A deep node whose only parents live on the surface is joined to the hub instead of being left
	 * floating: one stub per child, solid if any of those parents is a progression link. Stubs that
	 * would run through another deep node are left out.
	 */
	private static List<Connection> hubStubs(RecipeMapLayout.Result layout, List<RecipeMapLink> links,
			Set<RecipeMapKey> linkedInLayer, Integer degreeFilter, String familyFilter, int accent) {
		Map<RecipeMapKey, RecipeMapLink.Kind> stubKinds = new LinkedHashMap<>();
		for (RecipeMapLink link : links) {
			RecipeMapLayout.NodeBounds from = layout.node(link.from());
			RecipeMapLayout.NodeBounds to = layout.node(link.to());
			if (from == null || to == null || linkedInLayer.contains(link.to())
					|| !from.entry().visible() || layerOf(from.entry()) != SkillTreeLayer.SURFACE
					|| !included(to.entry(), degreeFilter, familyFilter, SkillTreeLayer.DEEP)) {
				continue;
			}
			stubKinds.merge(link.to(), link.kind(), (current, next) ->
					current == RecipeMapLink.Kind.PROGRESSION ? current : next);
		}
		List<Connection> stubs = new ArrayList<>();
		for (Map.Entry<RecipeMapKey, RecipeMapLink.Kind> stub : stubKinds.entrySet()) {
			RecipeMapLayout.NodeBounds child = layout.node(stub.getKey());
			if (!pathIsClear(layout, child, degreeFilter, familyFilter)) continue;
			stubs.add(new Connection(layout.centerX(), layout.centerY(), child.centerX(), child.centerY(),
					stub.getValue(), withAlpha(accent, alpha(stub.getValue())), true));
		}
		return stubs;
	}

	private static boolean pathIsClear(RecipeMapLayout.Result layout, RecipeMapLayout.NodeBounds child,
			Integer degreeFilter, String familyFilter) {
		for (RecipeMapLayout.NodeBounds other : layout.nodes().values()) {
			if (other == child || !included(other.entry(), degreeFilter, familyFilter, SkillTreeLayer.DEEP)) continue;
			if (distanceToSegment(other.centerX(), other.centerY(), layout.centerX(), layout.centerY(),
					child.centerX(), child.centerY()) < STUB_CLEARANCE) {
				return false;
			}
		}
		return true;
	}

	static double distanceToSegment(double px, double py, double x0, double y0, double x1, double y1) {
		double dx = x1 - x0;
		double dy = y1 - y0;
		double lengthSquared = dx * dx + dy * dy;
		double t = lengthSquared == 0 ? 0 : Math.max(0, Math.min(1, ((px - x0) * dx + (py - y0) * dy) / lengthSquared));
		return Math.hypot(px - x0 - t * dx, py - y0 - t * dy);
	}

	private static Line spoke(RecipeMapLayout.Result layout, double angle, int outerRadius, int accent) {
		return new Line(
				layout.centerX() + (int) Math.round(Math.cos(angle) * SPOKE_INNER_RADIUS),
				layout.centerY() + (int) Math.round(Math.sin(angle) * SPOKE_INNER_RADIUS),
				layout.centerX() + (int) Math.round(Math.cos(angle) * outerRadius),
				layout.centerY() + (int) Math.round(Math.sin(angle) * outerRadius),
				withAlpha(accent, 0x24));
	}

	private static boolean included(RecipeMapEntry entry, Integer degreeFilter, String familyFilter,
			SkillTreeLayer layer) {
		return entry.visible()
				&& layerOf(entry) == layer
				&& (degreeFilter == null || entry.column() == degreeFilter)
				&& (familyFilter == null || familyFilter.equals(entry.family()));
	}

	private static SkillTreeLayer layerOf(RecipeMapEntry entry) {
		return SkillTreeLayerRules.layerForDegree(entry.column());
	}

	private static int alpha(RecipeMapLink.Kind kind) {
		return kind == RecipeMapLink.Kind.PROGRESSION ? 0xD8 : 0x54;
	}

	private static int withAlpha(int color, int alpha) {
		return (alpha << 24) | (color & 0x00FFFFFF);
	}

	record Ring(int centerX, int centerY, int radius, int color) {}
	record Line(int x0, int y0, int x1, int y1, int color) {}
	record Connection(int x0, int y0, int x1, int y1, RecipeMapLink.Kind kind, int color, boolean hubStub) {}
}
