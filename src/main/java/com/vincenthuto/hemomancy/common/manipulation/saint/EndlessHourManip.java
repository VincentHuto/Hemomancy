package com.vincenthuto.hemomancy.common.manipulation.saint;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType;
import com.vincenthuto.hutoslib.client.particle.data.ColorParticleData;
import com.vincenthuto.hutoslib.common.registry.HLParticleInit;
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
	protected boolean canPerformAction(Player player, ItemStack heldItemMainhand, float chargeTicks) {
		return player.getPersistentData().getLong(EXPIRY_KEY) <= 0
				&& super.canPerformAction(player, heldItemMainhand, chargeTicks);
	}

	public static void deferDamage(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Pre event) {
		if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
				|| player.getPersistentData().getLong(EXPIRY_KEY) <= player.level().getGameTime()) return;
		// This hook follows armor/resistance but precedes absorption consumption.
		float payable = player.getAbsorptionAmount() + Math.max(0, player.getHealth() - 1.0F);
		float deferred = Math.max(0, event.getNewDamage() - payable);
		if (deferred > 0) {
			accumulateDeferredDamage(player, deferred);
			event.setNewDamage(event.getNewDamage() - deferred);
		}
	}

	public static void clearDebt(Player player) {
		player.getPersistentData().remove(EXPIRY_KEY);
		player.getPersistentData().remove(DEFERRED_DAMAGE_KEY);
	}

	public static void transferDebt(Player original, Player replacement) {
		long expiry = original.getPersistentData().getLong(EXPIRY_KEY);
		if (expiry > 0) {
			replacement.getPersistentData().putLong(EXPIRY_KEY, expiry);
			replacement.getPersistentData().putFloat(DEFERRED_DAMAGE_KEY,
					original.getPersistentData().getFloat(DEFERRED_DAMAGE_KEY));
			clearDebt(original);
		}
	}

	@Override
	public void getAction(Player player, Level world, ItemStack heldItemMainhand, BlockPos position) {
        try (var schoolCast = com.vincenthuto.hemomancy.common.damage.SchoolDamage.cast(this, player, 1)) {

		if (world.isClientSide) return;
		long currentTime = world.getGameTime();

		long existingExpiry = player.getPersistentData().getLong(EXPIRY_KEY);
		if (existingExpiry > 0) {
			player.displayClientMessage(
					net.minecraft.network.chat.Component.literal(
							"The hour has not yet ended. You cannot extend borrowed time.")
							.withStyle(net.minecraft.ChatFormatting.DARK_AQUA),
					true);
			return;
		}

		player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, DURATION_TICKS, 4, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION_TICKS, 3, false, true));

		player.getPersistentData().putLong(EXPIRY_KEY, currentTime + DURATION_TICKS);
		player.getPersistentData().putFloat(DEFERRED_DAMAGE_KEY, 0.0f);

		player.displayClientMessage(
				net.minecraft.network.chat.Component.literal(
						"Velorum's hour begins. Death is deferred — but it remembers.")
						.withStyle(net.minecraft.ChatFormatting.DARK_AQUA, net.minecraft.ChatFormatting.ITALIC),
				true);

		world.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.2f, 0.3f);

		if (world instanceof ServerLevel sLevel) {
            com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.attached(player,
                    com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.HOUR,0,DURATION_TICKS,1);
		}
	        }
    }

	/**
	 * Called from an event handler to check if the Endless Hour has expired
	 * and apply deferred damage. Should be called every tick for affected players.
	 */
	public static void tickEndlessHour(Player player) {
		long expiry = player.getPersistentData().getLong(EXPIRY_KEY);
		if (expiry <= 0) return;

		if (player.level().getGameTime() >= expiry) settleDebt(player);
	}

	/** Collect on expiry or logout. Debt is already mitigated health loss. */
	public static void settleDebt(Player player) {
		if (player.level().isClientSide) return;
		boolean active = player.getPersistentData().getLong(EXPIRY_KEY) > 0;
		float deferred = player.getPersistentData().getFloat(DEFERRED_DAMAGE_KEY);
		clearDebt(player);
        if(active && player.level() instanceof ServerLevel level) {
            com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.attached(player,
                    com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.HOUR,0,0,0);
            com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.burst(level,
                    com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals.Form.HOUR_BREAK,
                    player.position(),player.position(),deferred,24);
        }
		if (deferred <= 0 || !player.isAlive()) return;
		player.setHealth(Math.max(0, player.getHealth() - deferred));
		if (!player.isAlive()) player.die(player.damageSources().magic());
		player.displayClientMessage(net.minecraft.network.chat.Component.literal(
				"The hour ends. Velorum collects: " + String.format("%.1f", deferred) + " damage returns.")
				.withStyle(net.minecraft.ChatFormatting.RED, net.minecraft.ChatFormatting.BOLD), false);
		if (player.level() instanceof ServerLevel level) {
			level.playSound(null, player.blockPosition(), SoundEvents.LIGHTNING_BOLT_THUNDER,
					SoundSource.PLAYERS, .6F, .5F);
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
