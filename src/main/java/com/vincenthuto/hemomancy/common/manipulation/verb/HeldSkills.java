package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSkills;
import net.minecraft.world.entity.player.Player;

import java.util.OptionalDouble;

/**
 * Three strand skills also train the held manipulations that are not strands (CQ-20 B), by the same numbers as on a
 * lash ({@link StrandSkills}): Long Strand lifts their reach, Whipcrack their attack-button hit, and Free Hand their
 * pace. Taut Line and Second Strand stay the lashes' own, and Deep Brand stays Blood Binding's. Safe on both sides.
 */
public final class HeldSkills {
	private HeldSkills() {
	}

	/** Long Strand: the held manipulation's reach multiplier. */
	public static double reach(Player player) {
		return StrandSkills.lengthScale(SkillPointHelper.getLongStrandLevel(player));
	}

	/** Free Hand: the held manipulation's own pace, given back a tenth a level, never past walking. */
	public static OptionalDouble pace(Player player, double pace) {
		return OptionalDouble.of(StrandSkills.pace(pace, SkillPointHelper.getFreeHandLevel(player)));
	}

	/** Whipcrack: the attack-button hit's damage. */
	public static float accentDamage(Player player, float damage) {
		return (float) (damage * StrandSkills.accentDamage(SkillPointHelper.getWhipcrackLevel(player)));
	}

	/** Whipcrack: how long the attack-button hit's school state lasts. */
	public static int accentState(Player player, int ticks) {
		return (int) Math.round(ticks * StrandSkills.accentState(SkillPointHelper.getWhipcrackLevel(player)));
	}
}
