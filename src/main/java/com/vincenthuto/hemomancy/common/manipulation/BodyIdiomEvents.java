package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncBodyIdiomState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BodyIdiomEvents {
	private static final Set<UUID> RUPTURING = new HashSet<>();

	private BodyIdiomEvents() {
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || event.getAmount() <= 0.0F
				|| RUPTURING.contains(player.getUUID())) return;
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		float damage = event.getAmount();
		boolean changed = false;

		if (event.getSource().is(DamageTypes.WITHER) && hasBlackheartedEquipped(player)) {
			long now = player.level().getGameTime();
			BodyIdiomRules.BlackheartedResult result = BodyIdiomRules.metabolizeWither(damage,
					state.getNecroticSaturation(), now < state.getBlackheartedCooldownUntil());
			damage = result.remainingDamage();
			if (result.healing() > 0.0F) player.heal(result.healing());
			if (result.saturation() != state.getNecroticSaturation()) {
				state.setNecroticSaturation(result.saturation());
				changed = true;
			}
			if (result.ruptured()) {
				state.setBlackheartedCooldownUntil(now + BodyIdiomRules.BLACKHEARTED_COOLDOWN_TICKS);
				rupture(player);
				changed = true;
			}
		}

		if (state.getIronHeartHealth() > 0.0F) {
			BodyIdiomRules.IronHeartAbsorption result = BodyIdiomRules.absorbWithIronHearts(
					state.getIronHeartHealth(), damage, BodyIdiomRules.maxIronHeartHealth(player));
			state.setIronHeartHealth(result.ironHeartHealth());
			damage = result.remainingDamage();
			if (state.getIronHeartHealth() == 0.0F) state.setIronHeartExpiryTick(0L);
			changed = true;
		}

		event.setAmount(damage);
		if (changed) sync(player);
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		reconcileIronHeartCapacity(player);
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		if (state.getIronHeartHealth() > 0.0F
				&& player.level().getGameTime() >= state.getIronHeartExpiryTick()) {
			state.clearIronHearts();
			sync(player);
		}
	}

	public static void reconcileIronHeartCapacity(ServerPlayer player) {
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		float maxIronHeartHealth = BodyIdiomRules.maxIronHeartHealth(player);
		if (state.getIronHeartHealth() <= maxIronHeartHealth) return;
		state.setIronHeartHealth(maxIronHeartHealth);
		sync(player);
	}

	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) sync(player);
	}

	@SubscribeEvent
	public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) sync(player);
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		state.clearIronHearts();
		state.setNecroticSaturation(0.0F);
		state.setBlackheartedCooldownUntil(0L);
		sync(player);
	}

	public static void sync(ServerPlayer player) {
		PacketHandler.sendToPlayer(player,
				new PacketSyncBodyIdiomState(HemoCapabilityAccess.getPowerGuardrails(player)));
	}

	private static boolean hasBlackheartedEquipped(ServerPlayer player) {
		return HemoCapabilityAccess.getKnownManipulations(player)
				.map(known -> known.getEquippedManipNames().contains("blackhearted"))
				.orElse(false);
	}

	private static void rupture(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1.0D, player.getZ(),
				24, 0.45D, 0.65D, 0.45D, 0.06D);
		level.playSound(null, player.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.8F, 0.65F);
		RUPTURING.add(player.getUUID());
		try {
			player.hurt(player.damageSources().magic(), BodyIdiomRules.NECROTIC_RUPTURE_DAMAGE);
		} finally {
			RUPTURING.remove(player.getUUID());
		}
	}
}
