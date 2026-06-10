package com.vincenthuto.hemomancy.common.manipulation.tenebris;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BloodEclipseMantleManip extends BloodManipulation {
	public BloodEclipseMantleManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 360, 1, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 360, 0, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 360, 0, false, true));
		world.playSound(null, player.blockPosition(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.PLAYERS, 0.75F, 0.6F);

		if (world instanceof ServerLevel sLevel) {
			RandomSource random = world.random;
			for (int i = 0; i < 56; i++) {
				double angle = random.nextDouble() * Math.PI * 2.0;
				double radius = 0.7 + random.nextDouble() * 1.1;
				sLevel.sendParticles(GlowParticleFactory.createData(new ParticleColor(
								55 + random.nextFloat() * 45, 0, 85 + random.nextFloat() * 65)),
						player.getX() + Math.cos(angle) * radius,
						player.getY() + 0.2 + random.nextDouble() * 1.8,
						player.getZ() + Math.sin(angle) * radius,
						1, 0, 0.02, 0, 0.01);
			}
		}
	}
}
