package com.vincenthuto.hemomancy.common.worldgen.arbor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Deterministic, Minecraft-independent layout for the six braided Arbor boughs. */
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

	public static List<FruitPlacement> place(List<SkillSpec> skills, double chamberRadius) {
		double maxRadius = Math.max(1.25, chamberRadius * 0.67);
		List<FruitPlacement> placements = new ArrayList<>();
		for (SkillSpec skill : skills.stream().sorted(Comparator.comparingInt(SkillSpec::id)).toList()) {
			int whorl = Math.max(1, Math.min(7, skill.requiredDegree()));
			int family = FAMILY_INDEX.getOrDefault(skill.family(), 0);
			double t = 0.18 + (whorl - 1) / 6.0 * 0.68;
			int slot = Math.max(0, skill.authoredSlot());
			double slotOffset = (slot % 2 == 0 ? 1.0 : -1.0) * (1 + slot / 2) * 0.15;
			double angle = family * (Math.PI * 2.0 / FAMILY_ORDER.size()) - Math.PI
					+ t * 4.05 + Math.sin(t * Math.PI) * .20 + slotOffset;
			double opening = smoothstep(Math.max(0.0, (t - .34) / .66));
			double limbRadius = lerp(.42 + .42 * t, maxRadius * .86, opening);
			double radius = Math.min(maxRadius, Math.max(.55, limbRadius + skill.depth() * .10
					+ (slot % 3 - 1) * .20));
			double height = .24 + 7.4 * (.06 + .76 * t) + Math.sin(t * Math.PI) * 7.4 * .12
					+ (slot % 4 - 1.5) * .12;
			placements.add(new FruitPlacement(skill.id(), skill.family(), whorl,
					Math.cos(angle) * radius, height, Math.sin(angle) * radius, angle, radius));
		}
		return List.copyOf(placements);
	}

	private static double smoothstep(double t) {
		t = Math.max(0.0, Math.min(1.0, t));
		return t * t * (3.0 - 2.0 * t);
	}

	private static double lerp(double a, double b, double t) {
		return a + (b - a) * t;
	}

	public record SkillSpec(int id, String family, int requiredDegree, int depth, int authoredSlot) {
	}

	public record FruitPlacement(int skillId, String family, int whorl,
			double x, double y, double z, double angle, double radius) {
	}
}
