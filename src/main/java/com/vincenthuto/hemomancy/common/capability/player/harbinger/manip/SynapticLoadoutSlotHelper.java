package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import net.minecraft.world.entity.player.Player;

public final class SynapticLoadoutSlotHelper {
	public static final int BASE_LOADOUT_SLOTS = 3;
	public static final int MAX_SKILL_BONUS_SLOTS = 4;
	public static final int MAX_LOADOUT_SLOTS = BASE_LOADOUT_SLOTS + MAX_SKILL_BONUS_SLOTS;

	private SynapticLoadoutSlotHelper() {
	}

	public static int getUnlockedSlots(Player player) {
		return slotsForSkillLevel(SkillPointHelper.getSynapticMemoryLevel(player));
	}

	public static int getClientUnlockedSlots() {
		return slotsForSkillLevel(SkillPointHelper.getSynapticMemoryLevel());
	}

	public static int slotsForSkillLevel(int skillLevel) {
		int bonus = Math.max(0, Math.min(MAX_SKILL_BONUS_SLOTS, skillLevel));
		return Math.min(MAX_LOADOUT_SLOTS, BASE_LOADOUT_SLOTS + bonus);
	}

	public static boolean isSlotUnlocked(int slotIndex, int skillLevel) {
		return slotIndex >= 0 && slotIndex < slotsForSkillLevel(skillLevel);
	}
}
