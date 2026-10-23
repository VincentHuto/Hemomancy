package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Velorum — The Frozen Martyr.
 * <p>
 * Fourth of the entombed Saints; tendencies CONGEATIO + TENEBRIS. In life he
 * embraced silence and voluntary suffering as the highest form of devotion,
 * descending into a frozen-dark stillness so complete it became permanent.
 * His Canon Memory — Endless Hour — stretches the moment before an action to
 * its absolute limit; fitting for a Saint who never acted again.
 * <p>
 * Mechanics summary:
 * <ul>
 *   <li><b>Frost Nova</b>: Every {@link #FROST_NOVA_INTERVAL} ticks, a burst
 *       of congealing cold slows and briefly roots all players in range.</li>
 *   <li><b>Veil of Darkness</b>: Every {@link #DARKNESS_INTERVAL} ticks,
 *       Velorum floods the arena with umbral shadow, applying Blindness to
 *       nearby players. Blood-active (Harbinger) players also receive a brief
 *       Nausea effect — their blood recoils from the deep silence.</li>
 *   <li><b>Martyrdom Resistance</b>: Every time a player successfully damages
 *       Velorum he gains a short burst of Resistance I, reflecting the
 *       Saint's willingness to absorb suffering. This resistance wears off
 *       quickly, creating rhythm-based attack windows.</li>
 *   <li><b>Silence Pulse</b>: At low HP (≤ 25%), Velorum drains blood from
 *       blood-active players nearby every {@link #SILENCE_DRAIN_INTERVAL}
 *       ticks, representing the Endless Hour pulling at their power.</li>
 * </ul>
 */
public class VelorumEntity extends SaintTrialEntity {
    private static final BloodAccounting.Source SILENCE = BloodAccounting.Source.of("velorum_silence", Category.HAZARD, false, false);

    // ── synched data ──────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> DATA_MARTYRDOM =
            SynchedEntityData.defineId(VelorumEntity.class, EntityDataSerializers.BOOLEAN);

    public static final int VISUAL_FROST_NOVA = 1;
    public static final int VISUAL_VEIL = 2;
    public static final int VISUAL_SILENCE_DRAIN = 3;

    // ── frost nova ────────────────────────────────────────────────────────
    private static final int FROST_NOVA_INTERVAL = 100;
    private static final double FROST_NOVA_RADIUS = 10.0;
    private static final float FROST_NOVA_DAMAGE = 3.0F;

    // ── veil of darkness ──────────────────────────────────────────────────
    private static final int DARKNESS_INTERVAL = 160;
    private static final double DARKNESS_RADIUS = 12.0;

    // ── martyrdom resistance ──────────────────────────────────────────────
    private static final int MARTYRDOM_RESISTANCE_DURATION = 40;
    private static final int MARTYRDOM_RESISTANCE_AMP = 0;

    // ── silence drain (Phase 2, ≤ 25% HP) ────────────────────────────────
    private static final float SILENCE_PHASE_THRESHOLD = 0.25F;
    private static final int SILENCE_DRAIN_INTERVAL = 80;
    private static final int SILENCE_BLOOD_DRAIN = 200;
    private static final double SILENCE_DRAIN_RADIUS = 8.0;

    // ── animation states ──────────────────────────────────────────────────
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState martyrdomAnimationState = new AnimationState();

    private boolean defeated = false;
    private int martyrdomTimer = 0;

    // ── constructor ───────────────────────────────────────────────────────

    public VelorumEntity(EntityType<? extends VelorumEntity> type, Level level) {
        super(type, level, 120, new BossBarBinding("entity.hemomancy.velorum", BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10));
    }

    // ── attributes ────────────────────────────────────────────────────────

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 450.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 14.0D);
    }

    // ── goals ─────────────────────────────────────────────────────────────

    @Override
    protected void registerGoals() {
        registerTrialGoals(0.5D);
    }

    // ── synched data ──────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_MARTYRDOM, false);
    }

    public boolean isMartyrdom() {
        return this.entityData.get(DATA_MARTYRDOM);
    }

    private void setMartyrdom(boolean active) {
        this.entityData.set(DATA_MARTYRDOM, active);
    }

    // ── main tick ─────────────────────────────────────────────────────────

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            idleAnimationState.animateWhen(this.isAlive() && !isMartyrdom(), this.tickCount);
            martyrdomAnimationState.animateWhen(this.isAlive() && isMartyrdom(), this.tickCount);
            return;
        }

        if (defeated) return;
        ServerLevel server = (ServerLevel) this.level();
        tickVisualState();

        bossBar.trackHealth(this);

        if (martyrdomTimer > 0) {
            martyrdomTimer--;
            if (martyrdomTimer == 0) {
                setMartyrdom(false);
            }
        }

        // Frost nova
        if (this.tickCount % FROST_NOVA_INTERVAL == 0) {
            fireFrostNova(server);
        }

        // Veil of darkness
        if (this.tickCount % DARKNESS_INTERVAL == 0) {
            fireVeilOfDarkness(server);
        }

        // Silence drain at low HP
        boolean inSilencePhase = this.getHealth() <= this.getMaxHealth() * SILENCE_PHASE_THRESHOLD;
        if (inSilencePhase && this.tickCount % SILENCE_DRAIN_INTERVAL == 0) {
            fireSilenceDrain(server);
        }

        // Ambient frozen particles
        if (this.tickCount % 20 == 0) {
            server.sendParticles(ParticleTypes.SNOWFLAKE,
                    this.getX(), this.getY() + 1.2, this.getZ(),
                    8, 0.4, 0.6, 0.4, 0.01);
        }
    }

    private void fireFrostNova(ServerLevel server) {
        setVisualState(VISUAL_FROST_NOVA, 18);
        sendFloorRing(server, FROST_NOVA_RADIUS, ParticleTypes.SNOWFLAKE, 44, this.getY() + 0.08D);
        server.sendParticles(ParticleTypes.SNOWFLAKE,
                this.getX(), this.getY() + 0.5, this.getZ(),
                80, FROST_NOVA_RADIUS * 0.5, 0.3, FROST_NOVA_RADIUS * 0.5, 0.05);
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.POWDER_SNOW_FALL, SoundSource.HOSTILE, 2.0F, 0.5F);

        for (Player player : server.getEntitiesOfClass(Player.class,
                new AABB(this.blockPosition()).inflate(FROST_NOVA_RADIUS))) {
            player.hurt(this.damageSources().magic(), FROST_NOVA_DAMAGE);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, true));
            // Brief root via maximum slow
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 6, false, false));
        }
    }

    private void fireVeilOfDarkness(ServerLevel server) {
        setVisualState(VISUAL_VEIL, 22);
        sendFloorRing(server, DARKNESS_RADIUS, ParticleTypes.SQUID_INK, 48, this.getY() + 0.12D);
        server.sendParticles(ParticleTypes.SQUID_INK,
                this.getX(), this.getY() + 1.0, this.getZ(),
                60, DARKNESS_RADIUS * 0.4, 0.8, DARKNESS_RADIUS * 0.4, 0.04);
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.4F);

        for (Player player : server.getEntitiesOfClass(Player.class,
                new AABB(this.blockPosition()).inflate(DARKNESS_RADIUS))) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, true));

            // Blood-active players get Nausea — their blood recoils from the silence
            var vol = HemoCapabilityAccess.getBloodVolume(player);
            if (vol.isActive()) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, true));
            }
        }

        broadcastMessage("Darkness closes in — silence answers.", ChatFormatting.DARK_BLUE);
    }

    private void fireSilenceDrain(ServerLevel server) {
        setVisualState(VISUAL_SILENCE_DRAIN, 20);
        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY() + 1.0, this.getZ(),
                30, SILENCE_DRAIN_RADIUS * 0.3, 0.5, SILENCE_DRAIN_RADIUS * 0.3, 0.02);

        for (Player player : server.getEntitiesOfClass(Player.class,
                new AABB(this.blockPosition()).inflate(SILENCE_DRAIN_RADIUS))) {
            var vol = HemoCapabilityAccess.getBloodVolume(player);
            if (vol.isActive() && vol.getBloodVolume() >= SILENCE_BLOOD_DRAIN) {
                sendParticleLine(server, player.position().add(0.0D, 1.0D, 0.0D), this.position().add(0.0D, 1.2D, 0.0D), ParticleTypes.SOUL_FIRE_FLAME, 8);
                BloodAccounting.pay(player, SILENCE_BLOOD_DRAIN, SILENCE);
            }
        }
    }

    private void sendParticleLine(ServerLevel server, Vec3 from, Vec3 to, net.minecraft.core.particles.SimpleParticleType particle, int points) {
        Vec3 delta = to.subtract(from);
        for (int i = 1; i <= points; i++) {
            Vec3 point = from.add(delta.scale(i / (double) (points + 1)));
            server.sendParticles(particle, point.x, point.y, point.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
        }
    }

    // ── martyrdom resistance on being hit ─────────────────────────────────

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            // Grant brief resistance — the Saint absorbs pain willingly
            this.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, MARTYRDOM_RESISTANCE_DURATION,
                    MARTYRDOM_RESISTANCE_AMP, false, false));
            setMartyrdom(true);
            martyrdomTimer = MARTYRDOM_RESISTANCE_DURATION;
        }
        return hurt;
    }

    // ── drops ─────────────────────────────────────────────────────────────

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(new ItemStack(ItemInit.hallowed_residuum_velorum.get(), 1));
    }

    // ── sounds ────────────────────────────────────────────────────────────

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.GUARDIAN_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.ELDER_GUARDIAN_DEATH;
    }
}
