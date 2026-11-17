package com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationDiagnosticsSync;

/**
 * Temper's lifecycle outside a charge: death keeps half (CL-11), and once a second every school is held to its cap
 * (alignment can fall, by the Rite of Attunement or a scar coming off) while an active scar refills its school toward
 * its floor. Changes raise the pool's dirty flag; the {@code TEMPER_POOL} row of {@code PlayerStateSync} sends them.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TemperEvents {
	private static final int SETTLE_INTERVAL_TICKS = 20;

	private TemperEvents() {
	}

	/** The attachment is copied on death; the new body keeps half of it. A return from the End keeps it all. */
	@SubscribeEvent
	public static void onPlayerClone(PlayerEvent.Clone event) {
		if (!event.isWasDeath() || event.getEntity().level().isClientSide) return;
		HemoCapabilityAccess.getBloodTemper(event.getEntity()).afterDeath();
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()) return;
		if ((player.tickCount + player.getId()) % SETTLE_INTERVAL_TICKS != 0) return;
		settle(player, SETTLE_INTERVAL_TICKS / 20.0F);
		// A catalyst picked up or used changes the cost preview without touching the pool.
		ManipulationDiagnosticsSync.refreshTemperPreview(player);
	}

	/** One settling step of {@code seconds}: caps, then each active scar's floor. */
	public static void settle(ServerPlayer player, float seconds) {
		BloodTemper temper = HemoCapabilityAccess.getBloodTemper(player);
		for (EnumBloodTemper school : EnumBloodTemper.values()) {
			float cap = TemperCharge.cap(player, school);
			float held = temper.get(school);
			float next = TemperRules.refillTowardFloor(Math.min(held, cap), TemperCharge.activeScarTier(player, school), seconds);
			if (next != held) temper.set(school, next, cap);
		}
	}
}
