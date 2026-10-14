package com.vincenthuto.hemomancy.common.rite.unstained;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.zone.RiteZone;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Pale Consecration zones: every 20 ticks, hostile mobs inside an active zone
 * take 1 damage and Slowness I for 3 seconds. Players are never affected.
 * Each level processes the zones cast in its own dimension; expiry is shared
 * upkeep in {@link com.vincenthuto.hemomancy.common.rite.zone.RiteZoneEvents}.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class PaleConsecrationEvents {

	/** How often (in ticks) the effect is applied to mobs in the zone. */
	private static final int EFFECT_INTERVAL = 20;

	/** Damage dealt to mobs per interval. */
	private static final float CONSECRATION_DAMAGE = 1.0f;

	/** Slowness I duration in ticks (3 seconds). */
	private static final int SLOWNESS_DURATION = 60;

	private static final ParticleColor PALE_GLOW = new ParticleColor(214, 232, 236);

	@SubscribeEvent
	public static void onLevelTick(LevelTickEvent.Post event) {
		if (!(event.getLevel() instanceof ServerLevel sLevel)) return;
		long tick = sLevel.getGameTime();
		if (tick % EFFECT_INTERVAL != 0) return;

		PaleConsecrationSavedData data = PaleConsecrationSavedData.get(sLevel.getServer().overworld());
		if (data.zones().isEmpty()) return;

		for (RiteZone zone : data.activeIn(sLevel.dimension(), tick)) {
			BlockPos center = zone.center();
			int r = zone.blockRadius();
			AABB area = new AABB(center).inflate(r, 4, r);

			for (LivingEntity mob : sLevel.getEntitiesOfClass(Monster.class, area, Monster::isAlive)) {
				mob.hurt(sLevel.damageSources().magic(), CONSECRATION_DAMAGE);
				mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
						SLOWNESS_DURATION, 0, false, true, true));
			}

			sLevel.sendParticles(HemoParticleData.glow(PALE_GLOW),
					center.getX() + 0.5, center.getY() + 1.0, center.getZ() + 0.5,
					3, r * 0.4, 0.5, r * 0.4, 0.01);
		}
	}
}
