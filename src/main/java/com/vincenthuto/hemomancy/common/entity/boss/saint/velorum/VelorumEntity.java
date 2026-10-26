package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Velorum — Saint of the Endless Hour.
 * <p>
 * Fourth of the entombed Saints; tendencies CONGEATIO + TENEBRIS. In life a
 * lucid ascetic who forced stillness on his congregation, he descended into a
 * frozen-dark stillness so complete it became his dormancy. His Canon Memory —
 * Endless Hour — stretches the moment before an action to its absolute limit.
 * Felled, he always reverts to that dormancy and returns to his tomb.
 * <p>
 * Mechanics summary:
 * <ul>
 *   <li><b>Frost Nova</b>: Every {@link #FROST_NOVA_INTERVAL} ticks, a burst
 *       of congealing cold slows and briefly roots all players in range.</li>
 *   <li><b>Veil of Darkness</b>: Every {@link #DARKNESS_INTERVAL} ticks,
 *       Velorum floods the arena with umbral shadow, applying Blindness to
 *       nearby players. Blood-active (Harbinger) players also receive a brief
 *       Nausea effect — their blood recoils from the deep silence.</li>
 *   <li><b>Stillness Resistance</b>: Every time a player successfully damages
 *       Velorum he gains a short burst of Resistance I, reflecting the
 *       stillness he forces on any body, his own included. This wears off
 *       quickly, creating rhythm-based attack windows.</li>
 *   <li><b>Silence Pulse</b>: At low HP (≤ 25%), Velorum drains blood from
 *       blood-active players nearby every {@link #SILENCE_DRAIN_INTERVAL}
 *       ticks, representing the Endless Hour pulling at their power.</li>
 * </ul>
 */
public class VelorumEntity extends SaintTrialEntity {
    private static final BloodAccounting.Source SILENCE = BloodAccounting.Source.of("velorum_silence", Category.HAZARD, false, false);

    // ── synched data ──────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> DATA_STILL_GUARD =
            SynchedEntityData.defineId(VelorumEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Integer> DATA_FROST_UNTIL = SynchedEntityData.defineId(VelorumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VEIL_UNTIL = SynchedEntityData.defineId(VelorumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DRAIN_UNTIL = SynchedEntityData.defineId(VelorumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_FROST_RELEASE = SynchedEntityData.defineId(VelorumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VEIL_RELEASE = SynchedEntityData.defineId(VelorumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_DRAIN_RELEASE = SynchedEntityData.defineId(VelorumEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STILL_GUARD_TICKS = SynchedEntityData.defineId(VelorumEntity.class, EntityDataSerializers.INT);

    public static final int VISUAL_FROST_NOVA = 1;
    public static final int VISUAL_VEIL = 2;
    public static final int VISUAL_SILENCE_DRAIN = 3;

    // ── frost nova ────────────────────────────────────────────────────────
    private static final int FROST_NOVA_INTERVAL = VelorumVisualRules.FROST_INTERVAL;
    private static final double FROST_NOVA_RADIUS = 10.0;
    private static final float FROST_NOVA_DAMAGE = 3.0F;

    // ── veil of darkness ──────────────────────────────────────────────────
    private static final int DARKNESS_INTERVAL = VelorumVisualRules.VEIL_INTERVAL;
    private static final double DARKNESS_RADIUS = 12.0;

    // ── stillness resistance ──────────────────────────────────────────────
    private static final int STILL_GUARD_DURATION = VelorumVisualRules.MARTYRDOM;
    private static final int STILL_GUARD_AMP = 0;

    // ── silence drain (Phase 2, ≤ 25% HP) ────────────────────────────────
    private static final float SILENCE_PHASE_THRESHOLD = 0.25F;
    private static final int SILENCE_DRAIN_INTERVAL = VelorumVisualRules.DRAIN_INTERVAL;
    private static final int SILENCE_BLOOD_DRAIN = 200;
    private static final double SILENCE_DRAIN_RADIUS = 8.0;

    // ── animation states ──────────────────────────────────────────────────
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState martyrdomAnimationState = new AnimationState();

    private int stillGuardTimer = 0;

    // ── constructor ───────────────────────────────────────────────────────

    public VelorumEntity(EntityType<? extends VelorumEntity> type, Level level) {
        super(type, level, 120, new BossBarBinding("entity.hemomancy.velorum", BossPresentationIds.FRAME_VELORUM, BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10));
    }

    @Override
    public EnumSaintType saintType() {
        return EnumSaintType.VELORUM;
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
        builder.define(DATA_STILL_GUARD, false);
        builder.define(DATA_FROST_UNTIL, 100);
        builder.define(DATA_VEIL_UNTIL, 160);
        builder.define(DATA_DRAIN_UNTIL, 80);
        builder.define(DATA_FROST_RELEASE, 0);
        builder.define(DATA_VEIL_RELEASE, 0);
        builder.define(DATA_DRAIN_RELEASE, 0);
        builder.define(DATA_STILL_GUARD_TICKS, 0);

    }

    /** The brief stillness resistance after a hit (public names kept for the client model). */
    public boolean isMartyrdom() {
        return this.entityData.get(DATA_STILL_GUARD);
    }

    private void setStillGuard(boolean active) {
        this.entityData.set(DATA_STILL_GUARD, active);
    }

    public int getFrostUntil() { return this.entityData.get(DATA_FROST_UNTIL); }
    public int getVeilUntil() { return this.entityData.get(DATA_VEIL_UNTIL); }
    public int getDrainUntil() { return this.entityData.get(DATA_DRAIN_UNTIL); }
    public int getFrostRelease() { return this.entityData.get(DATA_FROST_RELEASE); }
    public int getVeilRelease() { return this.entityData.get(DATA_VEIL_RELEASE); }
    public int getDrainRelease() { return this.entityData.get(DATA_DRAIN_RELEASE); }
    public int getMartyrdomTicks() { return this.entityData.get(DATA_STILL_GUARD_TICKS); }

    public boolean isSilencePhase() { return getHealth() <= getMaxHealth() * SILENCE_PHASE_THRESHOLD; }

    // ── main tick ─────────────────────────────────────────────────────────

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            idleAnimationState.animateWhen(this.isAlive() && !isMartyrdom(), this.tickCount);
            martyrdomAnimationState.animateWhen(this.isAlive() && isMartyrdom(), this.tickCount);
            return;
        }

        if (tickRise()) return;
        if (!isAlive()) return;
        ServerLevel server = (ServerLevel) this.level();
        tickVisualState();
        this.entityData.set(DATA_FROST_UNTIL, FROST_NOVA_INTERVAL - Math.floorMod(tickCount, FROST_NOVA_INTERVAL));
        this.entityData.set(DATA_VEIL_UNTIL, DARKNESS_INTERVAL - Math.floorMod(tickCount, DARKNESS_INTERVAL));
        this.entityData.set(DATA_DRAIN_UNTIL, isSilencePhase() ? SILENCE_DRAIN_INTERVAL - Math.floorMod(tickCount, SILENCE_DRAIN_INTERVAL) : SILENCE_DRAIN_INTERVAL);
        if (getFrostRelease() > 0) this.entityData.set(DATA_FROST_RELEASE, getFrostRelease() - 1);
        if (getVeilRelease() > 0) this.entityData.set(DATA_VEIL_RELEASE, getVeilRelease() - 1);
        if (getDrainRelease() > 0) this.entityData.set(DATA_DRAIN_RELEASE, getDrainRelease() - 1);

        bossBar.trackHealth(this);

        if (stillGuardTimer > 0) {
            stillGuardTimer--;
            this.entityData.set(DATA_STILL_GUARD_TICKS, stillGuardTimer);
            if (stillGuardTimer == 0) {
                setStillGuard(false);
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
        boolean inSilencePhase = isSilencePhase();
        if (inSilencePhase && this.tickCount % SILENCE_DRAIN_INTERVAL == 0) {
            fireSilenceDrain(server);
        }

    }

    private void fireFrostNova(ServerLevel server) {
        setVisualState(VISUAL_FROST_NOVA, VelorumVisualRules.FROST_RELEASE);
        this.entityData.set(DATA_FROST_RELEASE, VelorumVisualRules.FROST_RELEASE);
        VelorumEffects.burst(server, Vec3.atBottomCenterOf(blockPosition()), VelorumVisualRules.FROST, (float) FROST_NOVA_RADIUS);
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.POWDER_SNOW_FALL, SoundSource.HOSTILE, 2.0F, 0.5F);

        for (Player player : server.getEntitiesOfClass(Player.class,
                new AABB(this.blockPosition()).inflate(FROST_NOVA_RADIUS))) {
            player.hurt(this.damageSources().magic(), FROST_NOVA_DAMAGE);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false, true));
            // Brief root via maximum slow
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 6, false, false, false));
        }
    }

    private void fireVeilOfDarkness(ServerLevel server) {
        setVisualState(VISUAL_VEIL, VelorumVisualRules.VEIL_RELEASE);
        this.entityData.set(DATA_VEIL_RELEASE, VelorumVisualRules.VEIL_RELEASE);
        VelorumEffects.burst(server, Vec3.atBottomCenterOf(blockPosition()), VelorumVisualRules.VEIL, (float) DARKNESS_RADIUS);
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.4F);

        for (Player player : server.getEntitiesOfClass(Player.class,
                new AABB(this.blockPosition()).inflate(DARKNESS_RADIUS))) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0, false, false, true));

            // Blood-active players get Nausea — their blood recoils from the silence
            var vol = HemoCapabilityAccess.getBloodVolume(player);
            if (vol.isActive()) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 80, 0, false, false, true));
            }
        }

        broadcastMessage("message.hemomancy.velorum.darkness_closes_in_silence_answers", ChatFormatting.DARK_BLUE);
    }

    private void fireSilenceDrain(ServerLevel server) {
        setVisualState(VISUAL_SILENCE_DRAIN, VelorumVisualRules.DRAIN_RELEASE);
        this.entityData.set(DATA_DRAIN_RELEASE, VelorumVisualRules.DRAIN_RELEASE);

        for (Player player : server.getEntitiesOfClass(Player.class,
                new AABB(this.blockPosition()).inflate(SILENCE_DRAIN_RADIUS))) {
            var vol = HemoCapabilityAccess.getBloodVolume(player);
            if (vol.isActive() && vol.getBloodVolume() >= SILENCE_BLOOD_DRAIN) {
                var payment = BloodAccounting.pay(player, SILENCE_BLOOD_DRAIN, SILENCE);
                if (payment.paid() > 0)
                    VelorumEffects.drain(server, player.position().add(0, 1, 0), this.position().add(0, 1.5, 0));
            }
        }
    }

    // ── stillness resistance on being hit ─────────────────────────────────

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide) {
            // Grant brief resistance — the Saint stills around the wound
            this.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE, STILL_GUARD_DURATION,
                    STILL_GUARD_AMP, false, false));
            setStillGuard(true);
            stillGuardTimer = STILL_GUARD_DURATION;
            this.entityData.set(DATA_STILL_GUARD_TICKS, stillGuardTimer);
            VelorumEffects.burst((ServerLevel) this.level(), this.position().add(0, 1.5, 0), VelorumVisualRules.MARTYR, 1);
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && this.level() instanceof ServerLevel server)
            VelorumEffects.burst(server, target.position().add(0, target.getBbHeight() * .6, 0), VelorumVisualRules.MELEE, .8F);
        return hit;
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel server)
            VelorumEffects.burst(server, this.position().add(0, 1.5, 0), VelorumVisualRules.REST, 3);
        super.die(source);
    }

    @Override
    protected void tickDeath() {
        if (++this.deathTime >= 20 && !this.level().isClientSide && !this.isRemoved())
            this.remove(RemovalReason.KILLED);
    }

    /** Felled, he goes still again; the tomb closes over him after. */
    @Override
    protected void announceTrialEnd(Player victor) {
        broadcastMessage("message.hemomancy.velorum.returns_to_stillness", ChatFormatting.DARK_AQUA);
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
