package com.vincenthuto.hemomancy.common.manipulation.flammeus;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hemomancy.common.manipulation.TendencyAffinityRules;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ScaldingUpdraftManip extends BloodManipulation {
	private static final double RADIUS = 4.0;

	public ScaldingUpdraftManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		if (!(world instanceof ServerLevel sLevel)) return;

		Vec3 look = new Vec3(player.getLookAngle().x, 0.0, player.getLookAngle().z);
		if (look.lengthSqr() > 0.01) look = look.normalize().scale(0.45);
		player.push(look.x, 1.1, look.z);
		player.hurtMarked = true;
		player.fallDistance = 0;
		player.resetFallDistance();
		player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 0, false, true));

		for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, new AABB(player.blockPosition()).inflate(RADIUS),
				e -> e != player && e.isAlive())) {
			target.igniteForSeconds(3);
			target.hurt(world.damageSources().onFire(),
					TendencyAffinityRules.adjustManipulationDamage(player, target, this, 1.5F));
		}
		world.playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 0.9F, 0.6F);
		for (int i = 0; i < 60; i++) {
			sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(255, 120 + world.random.nextFloat() * 80, 20)),
					player.getX() + (world.random.nextDouble() - 0.5) * 2.0,
					player.getY() + world.random.nextDouble() * 1.2,
					player.getZ() + (world.random.nextDouble() - 0.5) * 2.0,
					1, 0, 0.3, 0, 0.03);
		}
	}
}
