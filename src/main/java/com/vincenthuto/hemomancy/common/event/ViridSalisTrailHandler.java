package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.mob.animal.LeechEntity;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VenousStriderEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.*;
import com.vincenthuto.hemomancy.common.entity.summon.IBloodConstruct;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Virid Salis Trail ward: any hostile mob that steps onto a
 * {@code hemomancy:virid_salis_trail} block takes magic damage once per
 * second. Blood constructs and visibly blood-type mobs take double damage and
 * receive Slowness II for three seconds.
 *
 * <p>Harbinger players at Initiatory Degree 5 (Perfected) or higher are also
 * affected: they take base damage and Slowness I, reflecting the salt's
 * reaction to deeply-saturated blood-work.
 *
 * <h3>Blood-mob classification</h3>
 * The following are treated as blood-type mobs for the enhanced penalty:
 * <ul>
 *   <li>Any entity implementing {@link IBloodConstruct} (player-summoned constructs)</li>
 *   <li>{@link HematicConstructEntity} &mdash; wild hematic construct</li>
 *   <li>{@link CruorFiendEntity} &mdash; cruor (dark blood) fiend</li>
 *   <li>{@link FrozenClotEntity} &mdash; blood clot creature</li>
 *   <li>{@link BloodDrunkPuppeteerEntity} &mdash; blood-drunk puppeteer</li>
 *   <li>{@link ThirsterEntity} &mdash; blood thirster</li>
 *   <li>{@link AbyssalSiphonEntity} &mdash; abyssal blood siphon</li>
 *   <li>{@link LeechEntity} &mdash; leech (blood sucker)</li>
 *   <li>{@link VenousStriderEntity} &mdash; venous (blood-vessel) strider</li>
 * </ul>
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ViridSalisTrailHandler {

    /** Minimum Harbinger degree (inclusive) at which players are affected. */
    private static final int HARBINGER_DEGREE_THRESHOLD = 5;
    /** Base damage applied once per second to monsters walking over the trail. */
    private static final float BASE_DAMAGE = 1.0f;
    /** Damage applied to blood-type mobs (double the base). */
    private static final float BLOOD_MOB_DAMAGE = 2.0f;
    /** Slowness duration applied to blood mobs (3 seconds = 60 ticks). */
    private static final int SLOWNESS_DURATION = 60;
    /** Slowness II applied to blood-type mobs. */
    private static final int SLOWNESS_AMPLIFIER = 1;
    /** Slowness I applied to qualifying Harbinger players (softer penalty). */
    private static final int PLAYER_SLOWNESS_AMPLIFIER = 0;
    /** Tick interval between damage pulses (once per second). */
    private static final int DAMAGE_INTERVAL = 20;

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;

        Level level = living.level();
        if (level.isClientSide()) return;

        // Throttle to once per second; spread across the tick using a hash of entity id
        if ((living.tickCount + living.getId() * 31) % DAMAGE_INTERVAL != 0) return;

        // Check the block the entity is standing on before any expensive logic
        BlockPos pos = living.blockPosition();
        if (!level.getBlockState(pos).is(BlockInit.virid_salis_trail.get())) return;

        if (living instanceof Player player) {
            // Only Harbingers at degree 5+ are affected
            if (!isHighDegreeHarbinger(player)) return;
            player.hurt(player.damageSources().magic(), BASE_DAMAGE);
            player.addEffect(new MobEffectInstance(
                    MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, PLAYER_SLOWNESS_AMPLIFIER, false, true));
        } else {
            // Only targetable hostile mobs and blood-type mobs
            if (!isTargetable(living)) return;

            if (isBloodMob(living)) {
                living.hurt(living.damageSources().magic(), BLOOD_MOB_DAMAGE);
                living.addEffect(new MobEffectInstance(
                        MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, false, true));
            } else {
                living.hurt(living.damageSources().magic(), BASE_DAMAGE);
            }
        }

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SCRAPE,
                    pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                    4, 0.35, 0.05, 0.35, 0.01);
        }
    }

    /**
     * Returns {@code true} if the player is a Harbinger at degree
     * {@value #HARBINGER_DEGREE_THRESHOLD} or higher.
     */
    private static boolean isHighDegreeHarbinger(Player player) {
        return HemoCapabilityAccess.getInitiatoryDegree(player)
                .map(d -> d.getDegreeNumber() >= HARBINGER_DEGREE_THRESHOLD)
                .orElse(false);
    }

    /**
     * Returns {@code true} for non-player entities that the trail ward affects:
     * vanilla {@link Monster} mobs and known blood-type mobs (which may extend
     * {@link net.minecraft.world.entity.PathfinderMob} rather than Monster).
     */
    private static boolean isTargetable(LivingEntity entity) {
        return entity instanceof Monster || isBloodMob(entity);
    }

    /** Returns {@code true} for entities that represent a blood construct or blood-type mob. */
    private static boolean isBloodMob(LivingEntity entity) {
        return entity instanceof IBloodConstruct
                || entity instanceof HematicConstructEntity
                || entity instanceof CruorFiendEntity
                || entity instanceof FrozenClotEntity
                || entity instanceof BloodDrunkPuppeteerEntity
                || entity instanceof ThirsterEntity
                || entity instanceof AbyssalSiphonEntity
                || entity instanceof LeechEntity
                || entity instanceof VenousStriderEntity;
    }
}
