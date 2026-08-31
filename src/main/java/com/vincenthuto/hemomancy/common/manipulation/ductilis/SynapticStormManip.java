package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.List;

public class SynapticStormManip extends BloodManipulation {
	private static final int CHARGE_TICKS = 60;

	public SynapticStormManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public int getRequiredChargeTicks() { return CHARGE_TICKS; }

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		float charge = ManipulationCastingRules.chargeFraction(heldTicks, CHARGE_TICKS);
		List<LivingEntity> targets = ManipulationCombatHelper.hostileTargets(player, level, 18).stream()
				.sorted(Comparator.comparingDouble(player::distanceToSqr))
				.limit(ManipulationScalingRules.scaledCount(1, 8, heldTicks, CHARGE_TICKS)).toList();
		LivingEntity previous = player;
		int paralysis = ManipulationScalingRules.scaledInt(10, 60, heldTicks, CHARGE_TICKS);
		for (int i = 0; i < targets.size(); i++) {
			LivingEntity target = targets.get(i);
			DuctilisLightningEffects.conductiveArc(previous, target, i);
			ManipulationCombatHelper.hurt(this, player, target, level, 2.0F + 6.0F * charge);
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, paralysis,
					ManipulationReactiveEvents.isBoss(target) ? 1 : 5, false, true));
			previous = target;
		}
	}
}
