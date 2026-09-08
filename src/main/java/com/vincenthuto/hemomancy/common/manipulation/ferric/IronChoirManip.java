package com.vincenthuto.hemomancy.common.manipulation.ferric;

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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;

public class IronChoirManip extends BloodManipulation {
	private static final String SHOTS_LEFT = "hemomancy:iron_choir_shots";
	public IronChoirManip(String name, double cost, double alignment, double xpCost, EnumManipulationType type,
			EnumManipulationRank rank, EnumBloodTendency tendency, EnumVeinSections section) {
		super(name, cost, alignment, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position, float heldTicks) {
		if (!(world instanceof ServerLevel level)) return;
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 25, 0, false, true));
		player.getPersistentData().putInt(SHOTS_LEFT, 3);
		tickContinuousAction(player, world);
        ManipulationVisuals.attached(player, ManipulationVisuals.Form.CHOIR, 5, 25, player.getPersistentData().getInt(SHOTS_LEFT));
	}

	@Override
	public void tickContinuousAction(Player player, Level world) {
		if (!(world instanceof ServerLevel level)) return;
		int remaining = player.getPersistentData().getInt(SHOTS_LEFT);
        int before = remaining;
		if (remaining <= 0) return;
		for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(5),
				shot -> ManipulationCombatHelper.hostileProjectile(player, shot)).stream()
				.sorted(Comparator.comparingDouble(player::distanceToSqr)).limit(remaining).toList()) {
			Entity owner = projectile.getOwner();
			projectile.discard();
			remaining--;
			if (owner instanceof LivingEntity attacker) ManipulationCombatHelper.hurt(this, player, attacker, level, 4.0F);
			level.sendParticles(ParticleTypes.CRIT, projectile.getX(), projectile.getY(), projectile.getZ(), 12, .2, .2, .2, .03);
		}
		player.getPersistentData().putInt(SHOTS_LEFT, remaining);
        if (remaining != before) ManipulationVisuals.attached(player, ManipulationVisuals.Form.CHOIR, 5, 25, remaining);
	}

	@Override
	public void finishContinuousAction(Player player, boolean released) {
		player.getPersistentData().remove(SHOTS_LEFT);
		super.finishContinuousAction(player, released);
	}
}
