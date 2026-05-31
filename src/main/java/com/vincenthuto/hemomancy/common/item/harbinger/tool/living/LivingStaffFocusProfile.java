package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import net.minecraft.world.entity.player.Player;

public record LivingStaffFocusProfile(int livingConduitLevel, int vascularDrawLevel,
		int crimsonProjectionLevel, int hematicFocusLevel, int vespersRefusalLevel,
		boolean vesperMemoryAwakened) {
	public static final LivingStaffFocusProfile NONE = new LivingStaffFocusProfile(0, 0, 0, 0, 0, false);

	public LivingStaffFocusProfile {
		livingConduitLevel = clampLevel(livingConduitLevel);
		vascularDrawLevel = clampLevel(vascularDrawLevel);
		crimsonProjectionLevel = clampLevel(crimsonProjectionLevel);
		hematicFocusLevel = clampLevel(hematicFocusLevel);
		vespersRefusalLevel = clampLevel(vespersRefusalLevel);
	}

	public LivingStaffFocusProfile(int livingConduitLevel, int vascularDrawLevel,
			int crimsonProjectionLevel, boolean vesperMemoryAwakened) {
		this(livingConduitLevel, vascularDrawLevel, crimsonProjectionLevel, 0, 0, vesperMemoryAwakened);
	}

	public static LivingStaffFocusProfile fromPlayer(Player player, ILivingStaffProgress progress) {
		return new LivingStaffFocusProfile(
				SkillPointHelper.getLivingConduitLevel(player),
				SkillPointHelper.getVascularDrawLevel(player),
				SkillPointHelper.getCrimsonProjectionLevel(player),
				SkillPointHelper.getHematicFocusLevel(player),
				SkillPointHelper.getVespersRefusalLevel(player),
				progress != null && progress.isVesperMemoryAwakened());
	}

	private static int clampLevel(int level) {
		return Math.max(0, Math.min(3, level));
	}
}
