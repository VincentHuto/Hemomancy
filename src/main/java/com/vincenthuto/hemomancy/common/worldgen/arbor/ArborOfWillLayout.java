package com.vincenthuto.hemomancy.common.worldgen.arbor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Deterministic, Minecraft-independent fruit layout for the six Arbor family boughs. */
public final class ArborOfWillLayout {
	private static final List<String> FAMILY_ORDER = List.of(
			"core", "living_staff", "mycelial", "scars", "covenant", "summons");
	private static final Map<String, Integer> FAMILY_INDEX = Map.of(
			"core", 0, "living_staff", 1, "mycelial", 2,
			"scars", 3, "covenant", 4, "summons", 5);

	private ArborOfWillLayout() {
	}

	public static List<String> orderedFamilies() {
		return FAMILY_ORDER;
	}

	/** Hangs each skill from its family bough's spur on an Arbor of the given Degree; see {@link ArborSkeleton}. */
	public static List<FruitPlacement> place(List<SkillSpec> skills, double chamberRadius, int degree) {
		// The built floor reaches the full chamber radius; keep fruit over it with half a block to spare.
		double maxRadius = Math.max(1.25, chamberRadius - 0.5);
		Map<String, List<SkillSpec>> bands = new LinkedHashMap<>();
		for (SkillSpec skill : skills.stream().sorted(Comparator.comparingInt(SkillSpec::authoredSlot)
				.thenComparingInt(SkillSpec::id)).toList()) {
			bands.computeIfAbsent(familyName(skill) + ":" + ArborSkeleton.clampWhorl(skill.requiredDegree()),
					ignored -> new ArrayList<>()).add(skill);
		}
		List<FruitPlacement> placements = new ArrayList<>();
		for (List<SkillSpec> band : bands.values()) {
			for (int slot = 0; slot < band.size(); slot++) {
				SkillSpec skill = band.get(slot);
				int whorl = ArborSkeleton.clampWhorl(skill.requiredDegree());
				ArborCanopyGeometry.Point anchor = ArborSkeleton.fruitAnchor(
						FAMILY_INDEX.getOrDefault(skill.family(), 0), whorl, slot, band.size(), degree);
				double radius = anchor.radialDistance();
				double scale = radius > maxRadius ? maxRadius / radius : 1.0;
				double angle = Math.atan2(anchor.z(), anchor.x());
				placements.add(new FruitPlacement(skill.id(), skill.family(), whorl,
						anchor.x() * scale, anchor.y(), anchor.z() * scale, angle, radius * scale));
			}
		}
		placements.sort(Comparator.comparingInt(FruitPlacement::skillId));
		return List.copyOf(placements);
	}

	private static String familyName(SkillSpec skill) {
		return FAMILY_INDEX.containsKey(skill.family()) ? skill.family() : "core";
	}

	public record SkillSpec(int id, String family, int requiredDegree, int depth, int authoredSlot) {
	}

	public record FruitPlacement(int skillId, String family, int whorl,
			double x, double y, double z, double angle, double radius) {
	}
}
