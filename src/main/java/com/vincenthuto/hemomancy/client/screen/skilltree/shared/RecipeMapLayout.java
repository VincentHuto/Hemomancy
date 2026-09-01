package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ConcentricTreeGeometry;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Predicate;

public final class RecipeMapLayout {
	public static final String MISC_FAMILY = "Miscellaneous";
	public static final int NODE_SIZE = 28;
	private static final double START_ANGLE = -Math.PI / 2.0;
	private static final double SLOT_SPREAD_RADIANS = Math.toRadians(16.0);

	private RecipeMapLayout() {}

	public static Result build(List<RecipeMapEntry> sourceEntries, List<String> authoredFamilies) {
		return build(sourceEntries, authoredFamilies, Map.of());
	}

	public static Result build(List<RecipeMapEntry> sourceEntries, List<String> authoredFamilies,
			Map<RecipeMapKey, AuthoredPosition> authoredPositions) {
		Set<String> familySet = new LinkedHashSet<>(authoredFamilies);
		familySet.add(MISC_FAMILY);
		List<RecipeMapEntry> entries = sourceEntries.stream()
				.map(entry -> familySet.contains(entry.family()) ? entry
						: new RecipeMapEntry(entry.key(), entry.displayName(), entry.description(), entry.column(), MISC_FAMILY,
								entry.order(), entry.visible(), entry.unlocked()))
				.sorted(Comparator.comparingInt((RecipeMapEntry entry) -> familyIndex(familySet, entry.family()))
						.thenComparingInt(RecipeMapEntry::column)
						.thenComparingInt(RecipeMapEntry::order)
						.thenComparing(entry -> entry.id().toString()))
				.toList();

		List<String> activeFamilies = familySet.stream()
				.filter(family -> entries.stream().anyMatch(entry -> family.equals(entry.family())))
				.toList();
		Map<String, Double> familyAngles = new LinkedHashMap<>();
		double sectorWidth = Math.PI * 2.0 / Math.max(1, activeFamilies.size());
		for (int index = 0; index < activeFamilies.size(); index++) {
			familyAngles.put(activeFamilies.get(index), START_ANGLE + index * sectorWidth);
		}

		Map<RecipeMapKey, NodeBounds> nodes = new LinkedHashMap<>();
		for (String family : activeFamilies) {
			List<RecipeMapEntry> familyEntries = entries.stream()
					.filter(entry -> family.equals(entry.family())).toList();
			double familyAngle = familyAngles.get(family);
			for (int column = 0; column <= 8; column++) {
				int currentColumn = column;
				List<RecipeMapEntry> cell = familyEntries.stream()
						.filter(entry -> entry.column() == currentColumn)
						.sorted(Comparator.comparingInt(RecipeMapEntry::order).thenComparing(entry -> entry.id().toString()))
						.toList();
				double spread = Math.min(sectorWidth * 0.65,
						SLOT_SPREAD_RADIANS * Math.max(0, cell.size() - 1));
				for (int slot = 0; slot < cell.size(); slot++) {
					RecipeMapEntry entry = cell.get(slot);
					double offset = cell.size() <= 1 ? 0.0
							: -spread / 2.0 + spread * slot / (cell.size() - 1.0);
					SkillTreeLayer layer = SkillTreeLayerRules.layerForDegree(column);
					int radius = SkillTreeLayerRules.ringRadiusForDegree(column, layer);
					double angle = familyAngle + offset;
					AuthoredPosition authored = authoredPositions.get(entry.key());
					ConcentricTreeGeometry.Point point = authored == null
							? new ConcentricTreeGeometry.Point(
									ConcentricTreeGeometry.CENTER_X + (int) Math.round(Math.cos(angle) * radius),
									ConcentricTreeGeometry.CENTER_Y + (int) Math.round(Math.sin(angle) * radius))
							: new ConcentricTreeGeometry.Point(
									clampAuthoredCoordinate(authored.x(), contentSize()),
									clampAuthoredCoordinate(authored.y(), contentSize()));
					nodes.put(entry.key(), new NodeBounds(entry,
							point.x() - NODE_SIZE / 2, point.y() - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE));
				}
			}
		}
		int contentWidth = contentSize();
		int contentHeight = contentSize();
		return new Result(List.copyOf(entries), Map.copyOf(nodes), Map.copyOf(familyAngles),
				ConcentricTreeGeometry.CENTER_X, ConcentricTreeGeometry.CENTER_Y, contentWidth, contentHeight);
	}

	public record AuthoredPosition(int x, int y) {}

	private static int contentSize() {
		return ConcentricTreeGeometry.CENTER_X
				+ ConcentricTreeGeometry.radiusForDegree(8) + ConcentricTreeGeometry.CONTENT_PADDING;
	}

	private static int clampAuthoredCoordinate(int coordinate, int contentSize) {
		return Math.max(NODE_SIZE / 2, Math.min(contentSize - NODE_SIZE / 2, coordinate));
	}

	private static int familyIndex(Set<String> families, String family) {
		int index = 0;
		for (String candidate : families) {
			if (candidate.equals(family)) return index;
			index++;
		}
		return index;
	}

	public record NodeBounds(RecipeMapEntry entry, int x, int y, int width, int height) {
		public int centerX() { return x + width / 2; }
		public int centerY() { return y + height / 2; }
		public boolean contains(double pointX, double pointY) {
			if (pointX < x || pointX >= x + width || pointY < y || pointY >= y + height) return false;
			if (entry.key().kind() != RecipeMapEntry.Kind.FLOOR) return true;
			double halfWidth = width / 2.0;
			double halfHeight = height / 2.0;
			return Math.abs(pointX - centerX()) / halfWidth
					+ Math.abs(pointY - centerY()) / halfHeight <= 1.0;
		}
	}

	public record Result(List<RecipeMapEntry> entries, Map<RecipeMapKey, NodeBounds> nodes,
			Map<String, Double> familyAngles, int centerX, int centerY,
			int contentWidth, int contentHeight) {
		public NodeBounds node(ResourceLocation id) {
			return nodes.values().stream().filter(node -> node.entry().id().equals(id)).findFirst().orElse(null);
		}

		public NodeBounds node(RecipeMapKey key) { return nodes.get(key); }

		public NodeBounds nodeAt(double x, double y) {
			return nodes.values().stream().filter(node -> node.contains(x, y)).findFirst().orElse(null);
		}

		public NodeBounds nodeAt(double x, double y, Predicate<RecipeMapEntry> eligible) {
			return nodes.values().stream()
					.filter(node -> node.contains(x, y) && eligible.test(node.entry()))
					.findFirst().orElse(null);
		}

		public NodeBounds visibleNodeAt(double x, double y) {
			return visibleNodeAt(x, y, entry -> true);
		}

		public NodeBounds visibleNodeAt(double x, double y, Predicate<RecipeMapEntry> eligible) {
			return nodeAt(x, y, entry -> entry.visible() && eligible.test(entry));
		}
	}
}
