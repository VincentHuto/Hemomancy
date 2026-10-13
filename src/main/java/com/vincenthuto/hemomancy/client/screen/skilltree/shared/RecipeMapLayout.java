package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ConcentricTreeGeometry;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Predicate;

/**
 * Radial layout shared by the Harbinger Rites and Crafting maps.
 *
 * <p>Distance from the hub is the unlock degree, direction is the family. Each layer (D0-4 surface,
 * D5-8 deep) divides the circle, minus a gap at north reserved for degree labels, between the
 * families it shows; a family's sector is proportional to the arc its widest degree cell needs.
 * Nodes in a cell are spaced by chord length, and a cell too wide for its sector splits into two
 * staggered rows either side of the ring. Positions depend only on the entries passed in, never on
 * their visibility, so callers pass the full definition set and hidden nodes keep their slots.
 */
public final class RecipeMapLayout {
	public static final String MISC_FAMILY = "Miscellaneous";
	public static final int NODE_SIZE = 28;
	/** Centre-to-centre chord between neighbours: the 38 px minimum plus room for integer rounding. */
	static final int SLOT_SPACING = NODE_SIZE + 12;
	static final int STAGGER_OFFSET = 12;
	static final double STAGGER_THRESHOLD = 0.85;
	static final double LABEL_GAP_RADIANS = Math.toRadians(14.0);
	static final int LABEL_MARGIN = 100;
	/** Deep degrees skip the two innermost radii so D5, the busiest ring, gets a usable circumference. */
	private static final int DEEP_RING_SHIFT = 3;
	private static final double NORTH = -Math.PI / 2.0;

	private RecipeMapLayout() {}

	public static int ringRadius(int degree) {
		int clamped = Math.max(0, Math.min(8, degree));
		return ConcentricTreeGeometry.radiusForDegree(
				clamped <= SkillTreeLayerRules.SURFACE_MAX_DEGREE ? clamped : clamped - DEEP_RING_SHIFT);
	}

	public static int outerRingRadius(SkillTreeLayer layer) {
		return ringRadius(layer == SkillTreeLayer.DEEP ? 8 : SkillTreeLayerRules.SURFACE_MAX_DEGREE);
	}

	public static Result build(List<RecipeMapEntry> sourceEntries, List<String> authoredFamilies) {
		return build(sourceEntries, authoredFamilies, Map.of());
	}

	public static Result build(List<RecipeMapEntry> sourceEntries, List<String> authoredFamilies,
			Map<RecipeMapKey, AuthoredPosition> authoredPositions) {
		List<String> familyList = new ArrayList<>(new LinkedHashSet<>(authoredFamilies));
		if (!familyList.contains(MISC_FAMILY)) familyList.add(MISC_FAMILY);
		List<RecipeMapEntry> known = sourceEntries.stream()
				.map(entry -> familyList.contains(entry.family()) ? entry
						: new RecipeMapEntry(entry.key(), entry.displayName(), entry.description(), entry.column(), MISC_FAMILY,
								entry.order(), entry.visible(), entry.unlocked()))
				.toList();

		Map<String, Integer> earliestDegree = new HashMap<>();
		for (RecipeMapEntry entry : known) earliestDegree.merge(entry.family(), entry.column(), Math::min);
		List<String> familyOrder = familyList.stream().filter(earliestDegree::containsKey)
				.sorted(Comparator.comparingInt((String family) -> earliestDegree.get(family))
						.thenComparingInt(familyList::indexOf))
				.toList();
		List<RecipeMapEntry> entries = known.stream()
				.sorted(Comparator.comparingInt((RecipeMapEntry entry) -> familyOrder.indexOf(entry.family()))
						.thenComparingInt(RecipeMapEntry::column)
						.thenComparingInt(RecipeMapEntry::order)
						.thenComparing(entry -> entry.id().toString()))
				.toList();

		List<Sector> sectors = new ArrayList<>();
		Map<RecipeMapKey, Placement> placements = new HashMap<>();
		for (SkillTreeLayer layer : SkillTreeLayer.values()) {
			placeLayer(layer, entries, familyOrder, sectors, placements);
		}

		Map<SkillTreeLayer, Integer> rims = new EnumMap<>(SkillTreeLayer.class);
		for (SkillTreeLayer layer : SkillTreeLayer.values()) rims.put(layer, outerRingRadius(layer));
		for (RecipeMapEntry entry : entries) {
			rims.merge(SkillTreeLayerRules.layerForDegree(entry.column()), placements.get(entry.key()).radius(), Math::max);
		}
		int half = Collections.max(rims.values()) + LABEL_MARGIN;
		int contentSize = half * 2;

		Map<RecipeMapKey, NodeBounds> nodes = new LinkedHashMap<>();
		for (RecipeMapEntry entry : entries) {
			Placement placement = placements.get(entry.key());
			AuthoredPosition authored = authoredPositions.get(entry.key());
			int x = authored == null ? half + (int) Math.round(Math.cos(placement.angle()) * placement.radius())
					: clampAuthoredCoordinate(authored.x(), contentSize);
			int y = authored == null ? half + (int) Math.round(Math.sin(placement.angle()) * placement.radius())
					: clampAuthoredCoordinate(authored.y(), contentSize);
			nodes.put(entry.key(), new NodeBounds(entry, x - NODE_SIZE / 2, y - NODE_SIZE / 2, NODE_SIZE, NODE_SIZE));
		}
		return new Result(entries, Collections.unmodifiableMap(nodes), sectors, rims, half, half, contentSize, contentSize);
	}

	private static void placeLayer(SkillTreeLayer layer, List<RecipeMapEntry> entries, List<String> familyOrder,
			List<Sector> sectors, Map<RecipeMapKey, Placement> placements) {
		Map<String, Map<Integer, List<RecipeMapEntry>>> cells = new LinkedHashMap<>();
		for (String family : familyOrder) {
			for (RecipeMapEntry entry : entries) {
				if (!family.equals(entry.family()) || SkillTreeLayerRules.layerForDegree(entry.column()) != layer) continue;
				cells.computeIfAbsent(family, ignored -> new TreeMap<>())
						.computeIfAbsent(entry.column(), ignored -> new ArrayList<>()).add(entry);
			}
		}
		if (cells.isEmpty()) return;

		Map<String, Double> single = new LinkedHashMap<>();
		Map<String, Double> least = new LinkedHashMap<>();
		cells.forEach((family, byDegree) -> byDegree.forEach((degree, cell) -> {
			int radius = ringRadius(degree);
			single.merge(family, singleRowDemand(cell.size(), radius), Math::max);
			least.merge(family, leastDemand(cell.size(), radius), Math::max);
		}));
		double available = Math.PI * 2.0 - LABEL_GAP_RADIANS;
		double singleTotal = single.values().stream().mapToDouble(Double::doubleValue).sum();
		double leastTotal = least.values().stream().mapToDouble(Double::doubleValue).sum();

		double start = NORTH + LABEL_GAP_RADIANS / 2.0;
		for (String family : cells.keySet()) {
			double width;
			if (singleTotal <= available) {
				width = single.get(family) * available / singleTotal;
			} else if (leastTotal < singleTotal && leastTotal <= available) {
				double share = (available - leastTotal) / (singleTotal - leastTotal);
				width = least.get(family) + share * (single.get(family) - least.get(family));
			} else {
				width = least.get(family) * available / leastTotal;
			}
			sectors.add(new Sector(family, layer, start, start + width));
			start += width;
		}

		for (Sector sector : sectors) {
			if (sector.layer() != layer) continue;
			for (Map.Entry<Integer, List<RecipeMapEntry>> cellEntry : cells.get(sector.family()).entrySet()) {
				List<RecipeMapEntry> cell = cellEntry.getValue();
				cell.sort(Comparator.comparingInt(RecipeMapEntry::order).thenComparing(entry -> entry.id().toString()));
				int radius = ringRadius(cellEntry.getKey());
				int size = cell.size();
				boolean staggered = size >= 3 && singleRowDemand(size, radius) > STAGGER_THRESHOLD * sector.width();
				double step = staggered ? staggerStep(radius) : slotAngle(radius);
				for (int slot = 0; slot < size; slot++) {
					double angle = sector.midAngle() + (slot - (size - 1) / 2.0) * step;
					int rowRadius = !staggered ? radius : slot % 2 == 0 ? radius - STAGGER_OFFSET : radius + STAGGER_OFFSET;
					placements.put(cell.get(slot).key(), new Placement(angle, rowRadius));
				}
			}
		}
	}

	static double slotAngle(int radius) {
		return 2.0 * Math.asin(Math.min(1.0, SLOT_SPACING / (2.0 * radius)));
	}

	/** Angle between consecutive slots when alternate slots sit on the inner and outer staggered rows. */
	static double staggerStep(int radius) {
		double inner = radius - STAGGER_OFFSET;
		double outer = radius + STAGGER_OFFSET;
		double cosAlternate = (inner * inner + outer * outer - SLOT_SPACING * SLOT_SPACING) / (2.0 * inner * outer);
		double alternateRows = Math.acos(Math.max(-1.0, Math.min(1.0, cosAlternate)));
		double sameRow = Math.asin(Math.min(1.0, SLOT_SPACING / (2.0 * inner)));
		return Math.max(alternateRows, sameRow);
	}

	private static double singleRowDemand(int size, int radius) {
		return size * slotAngle(radius);
	}

	private static double leastDemand(int size, int radius) {
		if (size < 3) return singleRowDemand(size, radius);
		double staggered = (size - 1) * staggerStep(radius) + slotAngle(radius - STAGGER_OFFSET);
		return Math.min(singleRowDemand(size, radius), staggered);
	}

	public record AuthoredPosition(int x, int y) {}

	private record Placement(double angle, int radius) {}

	private static int clampAuthoredCoordinate(int coordinate, int contentSize) {
		return Math.max(NODE_SIZE / 2, Math.min(contentSize - NODE_SIZE / 2, coordinate));
	}

	/** One family's wedge on one layer; angles in radians, clockwise from east, {@code end > start}. */
	public record Sector(String family, SkillTreeLayer layer, double startAngle, double endAngle) {
		public double width() { return endAngle - startAngle; }
		public double midAngle() { return (startAngle + endAngle) / 2.0; }
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
			List<Sector> sectors, Map<SkillTreeLayer, Integer> rimRadii, int centerX, int centerY,
			int contentWidth, int contentHeight) {
		public Result {
			entries = List.copyOf(entries);
			sectors = List.copyOf(sectors);
			rimRadii = Map.copyOf(rimRadii);
		}

		public List<Sector> sectors(SkillTreeLayer layer) {
			return sectors.stream().filter(sector -> sector.layer() == layer).toList();
		}

		/** Outer ring or outermost generated row of the layer, whichever is further out; family labels sit beyond it. */
		public int rimRadius(SkillTreeLayer layer) {
			return rimRadii.getOrDefault(layer, outerRingRadius(layer));
		}

		public NodeBounds node(ResourceLocation id) {
			return nodes.values().stream().filter(node -> node.entry().id().equals(id)).findFirst().orElse(null);
		}

		public NodeBounds node(RecipeMapKey key) { return nodes.get(key); }

		public NodeBounds nodeAt(double x, double y) {
			return nodeAt(x, y, entry -> true);
		}

		/** The eligible node under the point whose centre is closest, so overlapping bounds resolve predictably. */
		public NodeBounds nodeAt(double x, double y, Predicate<RecipeMapEntry> eligible) {
			NodeBounds closest = null;
			double closestDistance = Double.MAX_VALUE;
			for (NodeBounds node : nodes.values()) {
				if (!node.contains(x, y) || !eligible.test(node.entry())) continue;
				double distance = Math.hypot(x - node.centerX(), y - node.centerY());
				if (distance < closestDistance) {
					closest = node;
					closestDistance = distance;
				}
			}
			return closest;
		}

		public NodeBounds visibleNodeAt(double x, double y) {
			return visibleNodeAt(x, y, entry -> true);
		}

		public NodeBounds visibleNodeAt(double x, double y, Predicate<RecipeMapEntry> eligible) {
			return nodeAt(x, y, entry -> entry.visible() && eligible.test(entry));
		}
	}
}
