package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.List;

/**
 * Server-side event handler for active Pale Consecration zones established by
 * the Rite of Pale Consecration.
 * <p>
 * Every 20 ticks, all hostile mobs inside an active zone take 1 damage and
 * receive Slowness I for 3 seconds.  Players are never affected.
 * Entries are cleaned up when their expiry tick passes.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.GAME)
public class PaleConsecrationEvents {

	/** How often (in ticks) the effect is applied to mobs in the zone. */
	private static final int EFFECT_INTERVAL = 20;

	/** Damage dealt to mobs per interval. */
	private static final float CONSECRATION_DAMAGE = 1.0f;

	/** Slowness I duration in ticks (3 seconds). */
	private static final int SLOWNESS_DURATION = 60;

	/** How often (in ticks) the cleanup pass runs. */
	private static final int CLEANUP_INTERVAL = 100;

	// ── Level tick ────────────────────────────────────────────────────────────

	@SubscribeEvent
	public static void onLevelTick(TickEvent.LevelTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		if (!(event.level instanceof ServerLevel sLevel)) return;
		if (sLevel != sLevel.getServer().overworld()) return;

		long tick = sLevel.getGameTime();

		// Cleanup
		if (tick % CLEANUP_INTERVAL == 0) {
			PaleConsecrationSavedData.get(sLevel).removeExpired(tick);
		}

		// Apply effects
		if (tick % EFFECT_INTERVAL != 0) return;

		PaleConsecrationSavedData data = PaleConsecrationSavedData.get(sLevel);
		if (data.getEntries().isEmpty()) return;

		String dimension = sLevel.dimension().location().toString();

		for (PaleConsecrationSavedData.ConsecrationEntry entry : data.getEntries()) {
			if (tick >= entry.expiryTick()) continue;
			if (!entry.dimension().equals(dimension)) continue;

			BlockPos center = entry.center();
			int r = entry.blockRadius();
			AABB zone = new AABB(center).inflate(r, 4, r);

			List<LivingEntity> mobs = sLevel.getEntitiesOfClass(
					LivingEntity.class, zone,
					e -> e.isAlive() && e instanceof Monster && !(e instanceof Player));

			for (LivingEntity mob : mobs) {
				mob.hurt(sLevel.damageSources().magic(), CONSECRATION_DAMAGE);
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						SLOWNESS_DURATION, 0, false, true, true));
			}

			// Spawn a few particles as visual feedback (server-side particle packet)
			sLevel.sendParticles(
					net.minecraft.core.particles.ParticleTypes.END_ROD,
					center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
					3, r * 0.4, 0.5, r * 0.4, 0.01);
		}
	}
}
