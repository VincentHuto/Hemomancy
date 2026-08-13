package com.vincenthuto.hemomancy.client.render.entity.misc;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgress;

import java.util.HashMap;
import java.util.Map;

/** Client-only interpolation memory; progression remains entirely server authoritative. */
final class ArborGrowthAnimations {
    private static final Map<Integer, Snapshot> SNAPSHOTS = new HashMap<>();

    private ArborGrowthAnimations() { }

    static float growthScale(SkillPoint skill, SkillProgress progress, int tick, float partial) {
        boolean unlocked = progress.isUnlocked(skill);
        int level = progress.getLevel(skill);
        Snapshot previous = SNAPSHOTS.get(skill.getId());
        if (previous == null) {
            SNAPSHOTS.put(skill.getId(), new Snapshot(unlocked, level, Integer.MIN_VALUE));
            return 1.0F;
        }
        if (previous.unlocked != unlocked || previous.level != level) {
            previous = new Snapshot(unlocked, level, tick);
            SNAPSHOTS.put(skill.getId(), previous);
        }
        if (previous.changedAt == Integer.MIN_VALUE) return 1.0F;
        float progressTicks = Math.max(0.0F, Math.min(1.0F, (tick + partial - previous.changedAt) / 24.0F));
        float overshoot = (float)Math.sin(progressTicks * Math.PI) * .18F;
        return unlocked ? .08F + .92F * progressTicks + overshoot : 1.0F;
    }

    private record Snapshot(boolean unlocked, int level, int changedAt) { }
}
