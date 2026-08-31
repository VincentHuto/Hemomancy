package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.manipulation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.List;

public class CarrionCommunionManip extends BloodManipulation {
	private static final DustParticleOptions BLOOD = new DustParticleOptions(new Vector3f(0.55F, 0.02F, 0.04F), 1.2F);

	public CarrionCommunionManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override public boolean canContinueChannel(Player player, Level world) { return !eligibleTargets(player, world).isEmpty(); }

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		float drained = 0;
		for (LivingEntity target : eligibleTargets(player, level).stream()
				.sorted(Comparator.comparingDouble(player::distanceToSqr)).limit(4).toList()) {
			if (ManipulationCombatHelper.hurt(this, player, target, level, 2.0F)) drained += 2.0F;
			level.sendParticles(BLOOD, target.getX(), target.getY() + .6, target.getZ(), 8, .2, .3, .2, .02);
		}
		player.heal(drained * .5F);
	}

	private static List<LivingEntity> eligibleTargets(Player player, Level world) {
		return ManipulationCombatHelper.hostileTargets(player, world, 8).stream().filter(target ->
				target.hasEffect(MobEffects.WITHER) || target.hasEffect(MobEffects.POISON)
						|| target.hasEffect(EffectInit.blood_loss) || target.hasEffect(EffectInit.grave_debt)).toList();
	}
}
