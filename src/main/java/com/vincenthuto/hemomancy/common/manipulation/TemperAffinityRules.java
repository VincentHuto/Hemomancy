package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTemperDefenseRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TemperWeaponHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public final class TemperAffinityRules {
	private static final float PRIMARY_DAMAGE_WEIGHT = 0.75f;
	private static final float SECONDARY_DAMAGE_WEIGHT = 0.25f;
	private static final float NEUTRAL_DAMAGE_MULTIPLIER = 1.0f;
	private static final float SECONDARY_GAIN_MULTIPLIER = 0.5f;

	private TemperAffinityRules() {
	}

	public static float composeDamageMultiplier(float primaryAffinityMultiplier,
			@Nullable Float secondaryAffinityMultiplier) {
		if (secondaryAffinityMultiplier == null) {
			return primaryAffinityMultiplier;
		}
		// A secondary school can only add to the primary matchup, never dilute it.
		return Math.max(primaryAffinityMultiplier, primaryAffinityMultiplier * PRIMARY_DAMAGE_WEIGHT
				+ secondaryAffinityMultiplier * SECONDARY_DAMAGE_WEIGHT);
	}

	public static float secondaryTemperGain(float primaryGain) {
		return primaryGain * SECONDARY_GAIN_MULTIPLIER;
	}

	public static float manipulationDamageMultiplier(Player player, LivingEntity target, BloodManipulation manip) {
		return damageMultiplier(player, target, manip.getTend(), manip.getSecondaryTend());
	}

	public static float adjustManipulationDamage(Player player, LivingEntity target, BloodManipulation manip,
			float baseDamage) {
		return baseDamage * manipulationDamageMultiplier(player, target, manip);
	}

	public static float damageMultiplier(Player player, LivingEntity target, EnumBloodTemper primaryTemper,
			@Nullable EnumBloodTemper secondaryTemper) {
		float primaryMultiplier = affinityMultiplier(player, target, primaryTemper);
		Float secondaryMultiplier = null;
		if (secondaryTemper != null && secondaryTemper != primaryTemper) {
			secondaryMultiplier = affinityMultiplier(player, target, secondaryTemper);
		}
		return composeDamageMultiplier(primaryMultiplier, secondaryMultiplier);
	}

	private static float affinityMultiplier(Player player, LivingEntity target, @Nullable EnumBloodTemper temper) {
		if (temper == null) {
			return NEUTRAL_DAMAGE_MULTIPLIER;
		}
		if (target instanceof VesperTheEveningStarEntity vesper) {
			return VesperTemperDefenseRules.damageMultiplier(vesper.getActiveTemper(), temper,
					TemperWeaponHelper.getDamageMultiplier(player, temper));
		}
		if (!TemperWeaponHelper.isOpposingTarget(target, temper)) return NEUTRAL_DAMAGE_MULTIPLIER;
		return TemperWeaponHelper.getDamageMultiplier(player, temper);
	}
}
