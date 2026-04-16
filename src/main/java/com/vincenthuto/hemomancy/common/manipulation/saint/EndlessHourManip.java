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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Endless Hour — Canon Memory of Saint Velorum.
 * Doctrine: Martyrdom / Silence
 *
 * HP cannot drop below 1 temporarily. However, all delayed damage
 * returns at once when the effect expires. The player is granted
 * Absorption hearts that represent the "borrowed time", and when
 * the effect ends, they receive the accumulated damage as a burst.
 *
 * Imprinted, not learned. The player uses it uncomfortably.
 */
public class EndlessHourManip extends BloodManipulation {

	private static final String DEFERRED_DAMAGE_KEY = "hemomancy:endless_hour_deferred";
	private static final String EXPIRY_KEY = "hemomancy:endless_hour_expiry";
	private static final int DURATION_TICKS = 200; // 10 seconds

	public EndlessHourManip(String name, double cost, double alignLevel, double xpCost,
			EnumManipulationType type, EnumManipulationRank rank, EnumBloodTendency tendency,
			EnumVeinSections section) {
		super(name, cost, alignLevel, xpCost, type, rank, tendency, section);
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
		long currentTime = world.getGameTime();

		// Check if an Endless Hour is already active
		long existingExpiry = player.getPersistentData().getLong(EXPIRY_KEY);
		if (existingExpiry > currentTime) {
			player.displayClientMessage(
					net.minecraft.network.chat.Component.literal(
							"The hour has not yet ended. You cannot extend borrowed time.")
							.withStyle(net.minecraft.ChatFormatting.DARK_AQUA),
					true);
			return;
		}

		// Set the effect: absorption + resistance to simulate "can't die"
		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, DURATION_TICKS, 4, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION_TICKS, 3, false, true));

		// Mark the expiry time and reset deferred damage counter
		player.getPersistentData().putLong(EXPIRY_KEY, currentTime + DURATION_TICKS);
		player.getPersistentData().putFloat(DEFERRED_DAMAGE_KEY, 0.0f);

		player.displayClientMessage(
				net.minecraft.network.chat.Component.literal(
						"Velorum's hour begins. Death is deferred — but it remembers.")
						.withStyle(net.minecraft.ChatFormatting.DARK_AQUA, net.minecraft.ChatFormatting.ITALIC),
				true);

		world.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2f, 0.3f);

		if (world instanceof ServerLevel sLevel) {
			sLevel.sendParticles(
					GlowParticleFactory.createData(new ParticleColor(100, 180, 255)),
					player.getX(), player.getY() + 1.0, player.getZ(),
					25, 0.4, 0.8, 0.4, 0.03);
		}
	}

	/**
	 * Called from an event handler to check if the Endless Hour has expired
	 * and apply deferred damage. Should be called every tick for affected players.
	 */
	public static void tickEndlessHour(Player player) {
		long expiry = player.getPersistentData().getLong(EXPIRY_KEY);
		if (expiry <= 0) return;

		long currentTime = player.level().getGameTime();
		if (currentTime >= expiry) {
			// Time's up — all deferred damage returns at once
			float deferred = player.getPersistentData().getFloat(DEFERRED_DAMAGE_KEY);

			player.getPersistentData().remove(EXPIRY_KEY);
			player.getPersistentData().remove(DEFERRED_DAMAGE_KEY);

			if (deferred > 0) {
				player.hurt(player.damageSources().magic(), deferred);
				player.displayClientMessage(
						net.minecraft.network.chat.Component.literal(
								"The hour ends. Velorum collects: " + String.format("%.1f", deferred) + " damage returns.")
								.withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD),
						false);

				if (player.level() instanceof ServerLevel sLevel) {
					sLevel.sendParticles(
							GlowParticleFactory.createData(new ParticleColor(255, 50, 50)),
							player.getX(), player.getY() + 1.0, player.getZ(),
							40, 0.5, 0.5, 0.5, 0.1);
				}

				player.level().playSound(null, player.blockPosition(),
						SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS, 0.6f, 0.5f);
			}
		}
	}

	/**
	 * Called when the player takes damage during an active Endless Hour.
	 * Accumulates the damage for later payback.
	 */
	public static void accumulateDeferredDamage(Player player, float amount) {
		long expiry = player.getPersistentData().getLong(EXPIRY_KEY);
		if (expiry > 0 && player.level().getGameTime() < expiry) {
			float current = player.getPersistentData().getFloat(DEFERRED_DAMAGE_KEY);
			player.getPersistentData().putFloat(DEFERRED_DAMAGE_KEY, current + amount);
		}
	}
}
