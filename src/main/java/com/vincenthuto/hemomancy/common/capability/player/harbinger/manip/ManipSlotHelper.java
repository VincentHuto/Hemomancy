package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import net.minecraft.world.entity.player.Player;

/**
 * Utility for computing the maximum number of manipulation slots a player
 * may equip at any given time.
 * <p>
 * Formula: {@code min(9, 3 (base) + degree / 2 + skill_manip_slots.currentLevel)}
 * <p>
 * The player gains +1 slot every other degree rank (integer division).
 * The total is hard-capped at {@link #MAX_SLOTS}.
 */
public final class ManipSlotHelper {

	/** The baseline number of manipulation slots every player starts with. */
	public static final int BASE_SLOTS = 3;

	/** Absolute maximum number of manipulation slots a player can ever have. */
	public static final int MAX_SLOTS = 9;

	private ManipSlotHelper() {}

	/**
	 * Returns the maximum number of manipulations the given player may equip.
	 * Capped at {@link #MAX_SLOTS}.
	 */
	public static int getMaxSlots(Player player) {
		return ManipulationSlotLedger.getMaxSlots(player);
	}

	/**
	 * Returns the number of skill_manip_slots levels that are wasted because the
	 * player's slot count already hits the hard cap from base + degree alone.
	 * Each wasted level should be converted into a skill point refund.
	 */
	public static int getExcessSkillLevels(Player player) {
		return ManipulationSlotLedger.getExcessSkillLevels(player);
	}
}
