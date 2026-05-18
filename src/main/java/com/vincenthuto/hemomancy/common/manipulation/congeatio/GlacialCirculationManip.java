package com.vincenthuto.hemomancy.common.manipulation.congeatio;

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

/**
 * Glacial Circulation — a T1 (HUMILIS) CONGEATIO quick manipulation that
 * chills the caster's blood to a near-crystalline state for 90 seconds.
 *
 * <p>The cold blood grants substantial fire resistance while it flows, but the
 * viscosity slows the caster's movement. A conscious tradeoff: armour against
 * heat at the cost of speed.
 *
 * <p>Unlike Glacial Grasp (which only works near water), Glacial Circulation
 * is usable anywhere and provides a consistent defensive identity for
 * Congeatio practitioners facing fire-heavy environments.
 */
public class GlacialCirculationManip extends BloodManipulation {

	private static final int DURATION_TICKS = 1800; // 90 seconds
	private static final int SLOWNESS_AMPLIFIER = 0; // Slowness I

	public GlacialCirculationManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, DURATION_TICKS, 0, false, true, true));
		player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DURATION_TICKS, SLOWNESS_AMPLIFIER, false, true, true));

		if (world instanceof ServerLevel sLevel) {
			BlockPos pos = player.blockPosition();
			RandomSource random = world.random;
			// Icy blue-white mist around the caster's feet
			for (int i = 0; i < 30; i++) {
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(
								120 + random.nextFloat() * 60,
								180 + random.nextFloat() * 60,
								255)),
						pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.4,
						pos.getY() + random.nextDouble() * 0.8,
						pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.4,
						1, (random.nextDouble() - 0.5) * 0.05, 0.04, (random.nextDouble() - 0.5) * 0.05, 0.01f);
			}
		}

		world.playSound(null, player.blockPosition(), SoundEvents.POWDER_SNOW_PLACE, SoundSource.PLAYERS, 0.8f, 0.6f);
		world.playSound(null, player.blockPosition(), SoundEvents.GLASS_PLACE, SoundSource.PLAYERS, 0.5f, 1.4f);
	}
}
