package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ConcentricTreeGeometry;

import java.util.ArrayList;
import java.util.List;

record RecipeMapTracePlan(List<Ring> rings, List<Line> spokes, List<Connection> connections) {
	RecipeMapTracePlan {
		rings = List.copyOf(rings);
		spokes = List.copyOf(spokes);
		connections = List.copyOf(connections);
	}

	static RecipeMapTracePlan build(RecipeMapLayout.Result layout, List<RecipeMapLink> links,
			Integer degreeFilter, String familyFilter, int accent) {
		return build(layout, links, degreeFilter, familyFilter, accent, null);
	}

	static RecipeMapTracePlan build(RecipeMapLayout.Result layout, List<RecipeMapLink> links,
			Integer degreeFilter, String familyFilter, int accent, SkillTreeLayer layer) {
		List<Ring> rings = new ArrayList<>();
		for (int degree = 0; degree <= 8; degree++) {
			if (layer != null && SkillTreeLayerRules.layerForDegree(degree) != layer) continue;
			int alpha = degreeFilter == null || degreeFilter == degree ? 0x32 : 0x12;
			int radius = layer == null ? ConcentricTreeGeometry.radiusForDegree(degree)
					: SkillTreeLayerRules.ringRadiusForDegree(degree, layer);
			rings.add(new Ring(layout.centerX(), layout.centerY(), radius, withAlpha(accent, alpha)));
		}

		List<Line> spokes = new ArrayList<>();
		int outerRadius = layer == null ? ConcentricTreeGeometry.radiusForDegree(8)
				: SkillTreeLayerRules.outerRingRadius(layer);
		for (double angle : layout.familyAngles().values()) {
			spokes.add(new Line(
					layout.centerX() + (int) Math.round(Math.cos(angle) * 32),
					layout.centerY() + (int) Math.round(Math.sin(angle) * 32),
					layout.centerX() + (int) Math.round(Math.cos(angle) * outerRadius),
					layout.centerY() + (int) Math.round(Math.sin(angle) * outerRadius),
					withAlpha(accent, 0x24)));
		}

		List<Connection> connections = new ArrayList<>();
		for (RecipeMapLink link : links) {
			RecipeMapLayout.NodeBounds from = layout.node(link.from());
			RecipeMapLayout.NodeBounds to = layout.node(link.to());
			if (from == null || to == null || !included(from.entry(), degreeFilter, familyFilter, layer)
					|| !included(to.entry(), degreeFilter, familyFilter, layer)) {
				continue;
			}
			int alpha = link.kind() == RecipeMapLink.Kind.PROGRESSION ? 0xD8 : 0x54;
			connections.add(new Connection(from.centerX(), from.centerY(), to.centerX(), to.centerY(),
					link.kind(), withAlpha(accent, alpha)));
		}
		return new RecipeMapTracePlan(rings, spokes, connections);
	}

	private static boolean included(RecipeMapEntry entry, Integer degreeFilter, String familyFilter,
			SkillTreeLayer layer) {
		return entry.visible()
				&& (layer == null || SkillTreeLayerRules.layerForDegree(entry.column()) == layer)
				&& (degreeFilter == null || entry.column() == degreeFilter)
				&& (familyFilter == null || familyFilter.equals(entry.family()));
	}

	private static int withAlpha(int color, int alpha) {
		return (alpha << 24) | (color & 0x00FFFFFF);
	}

	record Ring(int centerX, int centerY, int radius, int color) {}
	record Line(int x0, int y0, int x1, int y1, int color) {}
	record Connection(int x0, int y0, int x1, int y1, RecipeMapLink.Kind kind, int color) {}
}
