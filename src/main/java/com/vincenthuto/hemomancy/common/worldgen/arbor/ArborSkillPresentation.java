package com.vincenthuto.hemomancy.common.worldgen.arbor;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Adapts the authored 2D skill graph to stable six-bough Arbor slots. */
public final class ArborSkillPresentation {
    private ArborSkillPresentation() { }

    public static List<ArborOfWillLayout.SkillSpec> specs() {
        List<ArborOfWillLayout.SkillSpec> result = new ArrayList<>();
        Map<String, Integer> slots = new HashMap<>();
        for (SkillPoint skill : SkillPointInit.getAllSkills()) {
            if (skill.getId() == 0 || "base".equals(skill.getName()) || "deep_base".equals(skill.getName())) continue;
            String family = normalizeFamily(skill.getBranch());
            String band = family + ":" + Math.max(1, skill.getRequiredDegree());
            int slot = slots.merge(band, 1, Integer::sum) - 1;
            result.add(new ArborOfWillLayout.SkillSpec(skill.getId(), family,
                    Math.max(1, skill.getRequiredDegree()), depth(skill), slot));
        }
        return List.copyOf(result);
    }

    public static List<ArborOfWillLayout.FruitPlacement> placements(double chamberRadius) {
        return ArborOfWillLayout.place(specs(), chamberRadius);
    }

    private static int depth(SkillPoint skill) {
        int depth = 0;
        SkillPoint cursor = skill;
        while (cursor.getParent() != null && depth < 16) { depth++; cursor = cursor.getParent(); }
        return depth;
    }

    private static String normalizeFamily(String branch) {
        String value = branch == null ? "core" : branch.toLowerCase();
        if (value.contains("staff")) return "living_staff";
        if (value.contains("mycel")) return "mycelial";
        if (value.contains("scar")) return "scars";
        if (value.contains("coven")) return "covenant";
        if (value.contains("summon")) return "summons";
        return "core";
    }
}
