package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.ILivingStaffProgress;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import net.minecraft.world.entity.player.Player;

/**
 * What shapes the Living Staff's channel: its skills, the Vesper memory, and the growth rings its handled blood has
 * laid down ({@link LivingStaffGrowthRules}, core-loop Track 8).
 */
public record LivingStaffFocusProfile(int livingConduitLevel, int vascularDrawLevel,
		int crimsonProjectionLevel, int hematicFocusLevel, int vespersRefusalLevel,
		boolean vesperMemoryAwakened, int growthRing) {
	public static final LivingStaffFocusProfile NONE = new LivingStaffFocusProfile(0, 0, 0, 0, 0, false, 0);

	public LivingStaffFocusProfile {
		livingConduitLevel = clampLevel(livingConduitLevel);
		vascularDrawLevel = clampLevel(vascularDrawLevel);
		crimsonProjectionLevel = clampLevel(crimsonProjectionLevel);
		hematicFocusLevel = clampLevel(hematicFocusLevel);
		vespersRefusalLevel = clampLevel(vespersRefusalLevel);
		growthRing = LivingStaffGrowthRules.clampRing(growthRing);
	}

	/** A Staff with no rings yet. */
	public LivingStaffFocusProfile(int livingConduitLevel, int vascularDrawLevel, int crimsonProjectionLevel,
			int hematicFocusLevel, int vespersRefusalLevel, boolean vesperMemoryAwakened) {
		this(livingConduitLevel, vascularDrawLevel, crimsonProjectionLevel, hematicFocusLevel, vespersRefusalLevel,
				vesperMemoryAwakened, 0);
	}

	public LivingStaffFocusProfile(int livingConduitLevel, int vascularDrawLevel,
			int crimsonProjectionLevel, boolean vesperMemoryAwakened) {
		this(livingConduitLevel, vascularDrawLevel, crimsonProjectionLevel, 0, 0, vesperMemoryAwakened, 0);
	}

	public static LivingStaffFocusProfile fromPlayer(Player player, ILivingStaffProgress progress) {
		return new LivingStaffFocusProfile(
				SkillPointHelper.getLivingConduitLevel(player),
				SkillPointHelper.getVascularDrawLevel(player),
				SkillPointHelper.getCrimsonProjectionLevel(player),
				SkillPointHelper.getHematicFocusLevel(player),
				SkillPointHelper.getVespersRefusalLevel(player),
				progress != null && progress.isVesperMemoryAwakened(),
				progress == null ? 0 : LivingStaffGrowthRules.ring(progress.getBloodHandled()));
	}

	/** The same profile with {@code ring} growth rings. */
	public LivingStaffFocusProfile withRing(int ring) {
		return new LivingStaffFocusProfile(livingConduitLevel, vascularDrawLevel, crimsonProjectionLevel,
				hematicFocusLevel, vespersRefusalLevel, vesperMemoryAwakened, ring);
	}

	private static int clampLevel(int level) {
		return Math.max(0, Math.min(3, level));
	}
}
