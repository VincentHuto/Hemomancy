package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.PowerGuardrailState;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationReactiveEvents;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class IronheartedEvents {

	private IronheartedEvents() {
	}

	/** Spends Iron Hearts against incoming damage and returns what reaches health. */
	public static float absorb(ServerPlayer player, float damage) {
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		if (state.getIronHeartHealth() <= 0.0F) return damage;
		IronheartedRules.IronHeartAbsorption result = IronheartedRules.absorbWithIronHearts(
				state.getIronHeartHealth(), damage, IronheartedRules.maxIronHeartHealth(player));
		state.setIronHeartHealth(result.ironHeartHealth());
		if (state.getIronHeartHealth() == 0.0F) state.setIronHeartExpiryTick(0L);
		return result.remainingDamage();
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		reconcileIronHeartCapacity(player);
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		if (state.getIronHeartHealth() > 0.0F
				&& player.level().getGameTime() >= state.getIronHeartExpiryTick()) {
			state.clearIronHearts();
			ManipulationReactiveEvents.syncHearts(player);
		}
	}

	public static void reconcileIronHeartCapacity(ServerPlayer player) {
		PowerGuardrailState state = HemoCapabilityAccess.getPowerGuardrails(player);
		float maxIronHeartHealth = IronheartedRules.maxIronHeartHealth(player);
		if (state.getIronHeartHealth() <= maxIronHeartHealth) return;
		state.setIronHeartHealth(maxIronHeartHealth);
		ManipulationReactiveEvents.syncHearts(player);
	}

	@SubscribeEvent
	public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer player)) return;
		HemoCapabilityAccess.getPowerGuardrails(player).clearIronHearts();
	}
}
