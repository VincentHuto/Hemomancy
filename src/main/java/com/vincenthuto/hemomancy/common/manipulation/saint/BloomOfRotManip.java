package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
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

/**
 * Bloom of Rot — Canon Memory of Saint Putriciel.
 * Doctrine: Absolution / Witness
 *
 * Spreads hematic fungus in a radius — enemies decay (wither + poison),
 * and flora blooms. However, the terrain becomes semi-hostile and
 * the player also takes minor poison.
 *
 * Imprinted, not learned. The player uses it uncomfortably.
 */
public class BloomOfRotManip extends BloodManipulation {

	private static final double RADIUS = 8.0;
	private static final int ENEMY_EFFECT_DURATION = 200; // 10 seconds
	private static final int SELF_POISON_DURATION = 100; // 5 seconds

	public BloomOfRotManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		// Apply wither and poison to all nearby enemies
		world.getEntitiesOfClass(LivingEntity.class,
				player.getBoundingBox().inflate(RADIUS), e -> e != player)
				.forEach(entity -> {
					entity.addEffect(new MobEffectInstance(MobEffects.WITHER, ENEMY_EFFECT_DURATION, 1, false, true));
					entity.addEffect(new MobEffectInstance(MobEffects.POISON, ENEMY_EFFECT_DURATION, 0, false, true));
					entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ENEMY_EFFECT_DURATION, 1, false, true));
				});

		// Self-inflicted minor poison — the rot does not discriminate
		player.addEffect(new MobEffectInstance(MobEffects.POISON, SELF_POISON_DURATION, 0, false, true));

		player.displayClientMessage(
				net.minecraft.network.chat.Component.literal(
						"Putriciel's bloom erupts. Decay and life intertwine — the rot spreads to all, including you.")
						.withStyle(net.minecraft.ChatFormatting.DARK_GREEN, net.minecraft.ChatFormatting.ITALIC),
				true);

		world.playSound(null, player.blockPosition(), SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.PLAYERS, 1.0f, 0.7f);

		// Spawn fungal particles in a wide radius
		if (world instanceof ServerLevel sLevel) {
			for (int i = 0; i < 60; i++) {
				double offsetX = (world.random.nextDouble() - 0.5) * RADIUS * 2;
				double offsetZ = (world.random.nextDouble() - 0.5) * RADIUS * 2;
				sLevel.sendParticles(
						GlowParticleFactory.createData(new ParticleColor(
								30 + world.random.nextFloat() * 50,
								100 + world.random.nextFloat() * 80,
								20)),
						player.getX() + offsetX,
						player.getY() + 0.5 + world.random.nextDouble() * 1.5,
						player.getZ() + offsetZ,
						1, 0f, 0.05f, 0f, 0.01f);
			}
		}
	}
}
