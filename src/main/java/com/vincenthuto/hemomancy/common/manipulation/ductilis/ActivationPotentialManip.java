package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

public class ActivationPotentialManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 30;

	public ActivationPotentialManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		getAction(player, world, heldItemMainhand, position, CHARGE_TICKS);
	}

	@Override
	public int getRequiredChargeTicks() {
		return CHARGE_TICKS;
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position,
			float chargeTicks) {
		float strength = ManipulationCastingRules.chargeFraction(chargeTicks, CHARGE_TICKS);
		List<Entity> targets = player.level().getEntities(player, player.getBoundingBox().inflate(5.0));
		if (targets.size() > 0) {
			int targetIndex = 0;
			for (Entity target2 : targets) {
				if (target2 instanceof LivingEntity) {
					LivingEntity target = (LivingEntity) target2;
					DuctilisLightningEffects.activationPotential(player, target, targetIndex++);
					float damage = (float) (5.0f * strength * SkillPointHelper.getCrimsonMasteryMultiplier(player));
					float adjusted = TendencyAffinityRules.adjustManipulationDamage(player, target, this, damage);
					if (target.hurt(player.damageSources().magic(), adjusted)) {
						SchoolHitHelper.tryTriggerConductiveArc(player, target, EnumBloodTendency.DUCTILIS,
								getSecondaryTend(), adjusted);
					}
				}
			}
		}
	}

}
