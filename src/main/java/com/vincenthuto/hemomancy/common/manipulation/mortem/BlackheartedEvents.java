package com.vincenthuto.hemomancy.common.manipulation.mortem;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationVisuals;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class BlackheartedEvents {
	private static final Set<UUID> RUPTURING = new HashSet<>();

	private BlackheartedEvents() {
	}

	/** True while the player is taking their own rupture damage, which nothing may soften. */
	public static boolean isRupturing(ServerPlayer player) {
		return RUPTURING.contains(player.getUUID());
	}

	/** Metabolises wither damage into healing and saturation and returns what reaches health. */
	public static float metabolize(ServerPlayer player, DamageSource source, float damage) {
		if (!source.is(DamageTypes.WITHER) || !isActive(player)) return damage;
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		long now = player.level().getGameTime();
		BlackheartedRules.BlackheartedResult result = BlackheartedRules.metabolizeWither(damage,
				state.getNecroticSaturation(), now < state.getBlackheartedCooldownUntil());
		if (result.healing() > 0.0F) player.heal(result.healing());
		if (result.saturation() != state.getNecroticSaturation()) state.setNecroticSaturation(result.saturation());
		if (result.ruptured()) {
			state.setBlackheartedCooldownUntil(now + BlackheartedRules.BLACKHEARTED_COOLDOWN_TICKS);
			rupture(player);
		}
		return result.remainingDamage();
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
		var necrotic = HemoCapabilityAccess.getPowerGuardrails(player);
		ManipulationVisuals.attached(player, ManipulationVisuals.Form.BLACK_HEART, necrotic.getNecroticSaturation(),
				isActive(player) && necrotic.getNecroticSaturation() > 0 ? 25 : 0, 1);
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		state.setNecroticSaturation(0.0F);
		state.setBlackheartedCooldownUntil(0L);
	}

	private static boolean isActive(ServerPlayer player) {
		return ManipulationInit.blackhearted.get().isPassiveReady(player);
	}

	private static void rupture(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		ManipulationVisuals.burst(level, ManipulationVisuals.Form.BLACKHEART_RUPTURE,
				player.position().add(0, .8, 0), player.position(), 1.2, 24);
		level.playSound(null, player.blockPosition(), SoundEvents.WITHER_HURT, SoundSource.PLAYERS, 0.8F, 0.65F);
		RUPTURING.add(player.getUUID());
		try {
			player.hurt(player.damageSources().magic(), BlackheartedRules.NECROTIC_RUPTURE_DAMAGE);
		} finally {
			RUPTURING.remove(player.getUUID());
		}
	}
}
