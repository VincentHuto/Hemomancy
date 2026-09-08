package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AbsoluteStillnessManip extends BloodManipulation {
	public AbsoluteStillnessManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		for (LivingEntity target : ManipulationCombatHelper.hostileTargets(player, level, 6)) {
			target.clearFire();
			target.setTicksFrozen(Math.max(target.getTicksFrozen(), 80));
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25,
					com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents.isBoss(target) ? 0 : 3, false, true));
		}
		player.clearFire();
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.STILLNESS, 6, 25, 1);
	}

	@Override
	public void tickContinuousAction(Player player, Level world) {
		if (world.isClientSide) return;
		for (Projectile projectile : world.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(6),
				shot -> ManipulationCombatHelper.hostileProjectile(player, shot))) {
			projectile.setDeltaMovement(projectile.getDeltaMovement().scale(0.75D));
		}
	}
}
