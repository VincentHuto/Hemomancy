package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTendencyDefenseRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheEveningStarEntity;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.TendencyWeaponHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public final class TendencyAffinityRules {
	private static final float PRIMARY_DAMAGE_WEIGHT = 0.75f;
	private static final float SECONDARY_DAMAGE_WEIGHT = 0.25f;
	private static final float NEUTRAL_DAMAGE_MULTIPLIER = 1.0f;
	private static final float SECONDARY_GAIN_MULTIPLIER = 0.5f;

	private TendencyAffinityRules() {
	}

	public static float composeDamageMultiplier(float primaryAffinityMultiplier,
			@Nullable Float secondaryAffinityMultiplier) {
		if (secondaryAffinityMultiplier == null) {
			return primaryAffinityMultiplier;
		}
		return primaryAffinityMultiplier * PRIMARY_DAMAGE_WEIGHT
				+ secondaryAffinityMultiplier * SECONDARY_DAMAGE_WEIGHT;
	}

	public static float secondaryTendencyGain(float primaryGain) {
		return primaryGain * SECONDARY_GAIN_MULTIPLIER;
	}

	public static float manipulationDamageMultiplier(Player player, LivingEntity target, BloodManipulation manip) {
		return damageMultiplier(player, target, manip.getTend(), manip.getSecondaryTend());
	}

	public static float adjustManipulationDamage(Player player, LivingEntity target, BloodManipulation manip,
			float baseDamage) {
		return baseDamage * manipulationDamageMultiplier(player, target, manip);
	}

	public static float damageMultiplier(Player player, LivingEntity target, EnumBloodTendency primaryTendency,
			@Nullable EnumBloodTendency secondaryTendency) {
		float primaryMultiplier = affinityMultiplier(player, target, primaryTendency);
		Float secondaryMultiplier = null;
		if (secondaryTendency != null && secondaryTendency != primaryTendency) {
			secondaryMultiplier = affinityMultiplier(player, target, secondaryTendency);
		}
		return composeDamageMultiplier(primaryMultiplier, secondaryMultiplier);
	}

	private static float affinityMultiplier(Player player, LivingEntity target, @Nullable EnumBloodTendency tendency) {
		if (tendency == null) {
			return NEUTRAL_DAMAGE_MULTIPLIER;
		}
		if (target instanceof VesperTheEveningStarEntity vesper) {
			return VesperTendencyDefenseRules.damageMultiplier(vesper.getActiveTendency(), tendency,
					TendencyWeaponHelper.getDamageMultiplier(player, tendency));
		}
		if (!TendencyWeaponHelper.isOpposingTarget(target, tendency)) return NEUTRAL_DAMAGE_MULTIPLIER;
		return TendencyWeaponHelper.getDamageMultiplier(player, tendency);
	}
}
