package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Server-side event handler for active Still Waters zones established by the
 * Rite of Still Waters.
 * <p>
 * Players inside a Still Waters zone take 30% less magic damage, partially
 * countering Sanguine Dominion's bleed and other blood-magic threats.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StillWatersEvents {

	/** How often (in ticks) the cleanup pass removes expired entries. */
	private static final int CLEANUP_INTERVAL = 100;

	/** Fraction of magic damage reduced for players inside a Still Waters zone. */
	private static final float MAGIC_DAMAGE_REDUCTION = 0.30f;

	// ── Level tick: clean up expired zones ───────────────────────────────────

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;
		if (sLevel != sLevel.getServer().overworld()) return;
		if (sLevel.getGameTime() % CLEANUP_INTERVAL != 0) return;

		StillWatersSavedData.get(sLevel).removeExpired(sLevel.getGameTime());
	}

	// ── Hurt event: reduce magic damage inside Still Waters zones ────────────

	@SubscribeEvent
	public static void onLivingHurt(LivingHurtEvent event) {
		// Only affect players
		if (!(event.getEntity() instanceof Player player)) return;
		if (player.level().isClientSide) return;

		// Only reduce magic-type damage
		if (!event.getSource().is(DamageTypes.MAGIC)) return;

		ServerLevel sLevel = (ServerLevel) player.level();
		ServerLevel overworld = sLevel.getServer().overworld();
		StillWatersSavedData data = StillWatersSavedData.get(overworld);

		if (data.getEntries().isEmpty()) return;

		BlockPos pos = player.blockPosition();
		String dimension = sLevel.dimension().location().toString();
		long tick = sLevel.getGameTime();

		if (data.isInZone(pos, dimension, tick)) {
			event.setAmount(event.getAmount() * (1.0f - MAGIC_DAMAGE_REDUCTION));
		}
	}
}
