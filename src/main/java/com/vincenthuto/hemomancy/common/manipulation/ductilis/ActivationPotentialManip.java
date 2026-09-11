package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPointHelper;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


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
        if (!(world instanceof net.minecraft.server.level.ServerLevel level)) return;
        float strength = ManipulationCastingRules.chargeFraction(chargeTicks, CHARGE_TICKS);
        Discharge discharge = new Discharge();
        java.util.List<LivingEntity> struck = new java.util.ArrayList<>();
        int index = 0;
        float damage = (float)(5.0F * strength * SkillPointHelper.getCrimsonMasteryMultiplier(player));
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(5),
                e -> ConductionManager.canHarm(player,e)
                        && ConductionManager.visible(level,player.getEyePosition(),e.getEyePosition(),player)).stream()
                .sorted(java.util.Comparator.comparing(LivingEntity::getUUID)).toList()) {
            if (!ConductionManager.claimHit(player,target,discharge)) continue;
            DuctilisLightningEffects.activationPotential(player,target,index++);
            if (ManipulationCombatHelper.hurt(this,player,target,level,damage)) {
                Paralysis.interrupt(target);
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                        ManipulationScalingRules.scaledInt(5,30,chargeTicks,CHARGE_TICKS),1,false,true));
                ConductionManager.energizeTouching(player,target,discharge);
                struck.add(target);
            }
        }
        ConductionManager.energizeVisibleBounds(player,player.getBoundingBox().inflate(5),discharge);
        for (LivingEntity target:struck) SchoolHitHelper.tryTriggerConductiveArc(player,target,
                EnumBloodTendency.DUCTILIS,getSecondaryTend(),damage,discharge);
    }
}
