package com.vincenthuto.hemomancy.common.worldevent;

import java.util.Map;
import java.util.UUID;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Applies passive buffs to Harbingers standing inside any active Founding Sanctum.
 * Sanctum locations are persisted in {@link FoundingSanctumSavedData}.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FoundingSanctumEvents {

	private static final int EFFECT_INTERVAL_TICKS = 40;
	private static final int EFFECT_DURATION = 60;

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;
		if (sLevel.getGameTime() % EFFECT_INTERVAL_TICKS != 0) return;

		FoundingSanctumSavedData data = FoundingSanctumSavedData.get(sLevel);
		Map<UUID, BlockPos> sanctums = data.getAllSanctums();
		if (sanctums.isEmpty()) return;

		for (ServerPlayer player : sLevel.getPlayers(p -> p.isAlive())) {
			int degree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
			if (degree < 1) continue;

			for (BlockPos center : sanctums.values()) {
				if (isInSanctum(player, center)) {
					applyBuffs(player);
					break;
				}
			}
		}
	}

	private static boolean isInSanctum(ServerPlayer player, BlockPos center) {
		double dx = player.getX() - (center.getX() + 0.5);
		double dz = player.getZ() - (center.getZ() + 0.5);
		return (dx * dx + dz * dz) <= FoundingSanctumSavedData.SANCTUM_RADIUS * FoundingSanctumSavedData.SANCTUM_RADIUS;
	}

	private static void applyBuffs(ServerPlayer player) {
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, EFFECT_DURATION, 0, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 0, true, false, true));
		player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_DURATION, 0, true, false, true));
	}
}
