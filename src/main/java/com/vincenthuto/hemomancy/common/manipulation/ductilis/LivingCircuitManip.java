package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public class LivingCircuitManip extends BloodManipulation {
	public LivingCircuitManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		for (Player ally : level.getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(10),
				candidate -> candidate != player && candidate.isAlive()).stream()
				.sorted(Comparator.comparingDouble(player::distanceToSqr)).limit(3).toList()) {
			ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 25, 1, false, true));
			ally.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 25, 1, false, true));
			ManipulationReactiveEvents.armLivingCircuit(ally);
			DuctilisLightningEffects.conductiveArc(player, ally, 0);
		}
	}
}
