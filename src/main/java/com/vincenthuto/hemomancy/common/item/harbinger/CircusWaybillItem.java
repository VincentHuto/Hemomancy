package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;

public final class CircusWaybillItem extends CovenantWaybillItem {
	public CircusWaybillItem(Properties properties) {
		super(properties, Hemomancy.rloc("circus_waybill_targets"),
				"item.hemomancy.circus_waybill", 160);
	}

    @Override protected net.minecraft.core.BlockPos findTarget(net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.entity.player.Player player) {
        String tagName = "circus_waybill_targets";
        if (!player.isShiftKeyDown()) {
            int degree = com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess.getPlayerDegreeNumber(player);
            var lesson = com.vincenthuto.hemomancy.common.circus.CircusCurriculum.lessons().stream()
                    .filter(l -> l.degree() <= degree && !com.vincenthuto.hemomancy.common.circus.CircusApprenticeshipProgress.instructionSatisfied(player, l.summon()))
                    .findFirst().orElse(null);
            if (lesson != null) tagName = "troupe_teacher_" + switch (lesson.teacher()) {
                case "circus_acrobat" -> "air";
                case "circus_knife_thrower" -> "blades";
                case "circus_stilt_walker", "circus_strongman" -> "pageantry";
                case "circus_fire_eater", "circus_beast_tamer" -> "embers";
                default -> "memory";
            };
        }
        var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.STRUCTURE, Hemomancy.rloc(tagName));
        var origin = player.blockPosition();
        var found = level.findNearestMapStructure(tag, origin, 160, false);
        if (!tagName.equals("circus_waybill_targets") || found == null) return found;
        var sites = com.vincenthuto.hemomancy.common.circus.CircusPavilionSavedData.get(level);
        if (!sites.isCompletedNear(level, found)) return found;
        net.minecraft.core.BlockPos best = null;
        for (int dx : new int[]{-1536, 0, 1536}) for (int dz : new int[]{-1536, 0, 1536}) {
            if (dx == 0 && dz == 0) continue;
            var next = level.findNearestMapStructure(tag, origin.offset(dx, 0, dz), 96, false);
            if (next == null || sites.isCompletedNear(level, next) || origin.distSqr(next) > 2560D * 2560D) continue;
            if (best == null || origin.distSqr(next) < origin.distSqr(best)) best = next;
        }
        return best;
    }
}
