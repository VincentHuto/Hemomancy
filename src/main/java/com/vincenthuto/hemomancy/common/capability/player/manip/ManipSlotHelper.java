package com.vincenthuto.hemomancy.common.capability.player.manip;

import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;

import net.minecraft.world.entity.player.Player;

/**
 * Utility for computing the maximum number of manipulation slots a player
 * may equip at any given time.
 * <p>
 * Formula: {@code 3 (base) + degree * 2 + skill_manip_slots.currentLevel}
 */
public final class ManipSlotHelper {

	/** The baseline number of manipulation slots every player starts with. */
	public static final int BASE_SLOTS = 3;

	/** Additional slots granted per initiatory degree rank. */
	public static final int SLOTS_PER_DEGREE = 2;

	private ManipSlotHelper() {}

	/**
	 * Returns the maximum number of manipulations the given player may equip.
	 */
	public static int getMaxSlots(Player player) {
		int degree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
		int skillBonus = 0;
		if (SkillPointInit.skill_manip_slots != null) {
			skillBonus = SkillPointInit.skill_manip_slots.getCurrentLevel();
		}
		return BASE_SLOTS + (degree * SLOTS_PER_DEGREE) + skillBonus;
	}
}
