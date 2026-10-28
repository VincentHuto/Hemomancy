package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.presentation.BossPresentationIds;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationCombatHelper;
import com.vincenthuto.hemomancy.common.block.harbinger.CrimsonFireHelper;
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
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Putriciel — The Befouled Saint.
 * <p>
 * Third of the entombed Saints; tendencies MORTEM + FLAMMEUS. In life a
 * wandering purifier who burned infection out, Putriciel drew each cleansing's
 * corruption into their own blood until the rot overran them. Their trial
 * requires players to endure a rot-fire environment while finding moments to
 * perform absolution acts that advance the victory condition. Absolved, they
 * rest a while in their tomb; the rot always reclaims them.
 * <p>
 * Mechanics summary:
 * <ul>
 *   <li><b>Rot Nova</b>: Every {@link #ROT_NOVA_INTERVAL} ticks, an expanding
 *       wave of rot-fire deals Wither + Fire damage to all players in range.</li>
 *   <li><b>Absolution Window</b>: Every {@link #ABSOLUTION_CYCLE} ticks,
 *       Putriciel briefly enters an absolved state. Dealing damage during this
 *       window increments the absolution counter. Reaching
 *       {@link #ABSOLUTIONS_NEEDED} completes the trial (victory).</li>
 *   <li><b>Escalation</b>: Below 50% HP the rot nova radius and Wither
 *       amplifier both increase.</li>
 * </ul>
 */
public class PutricielEntity extends SaintTrialEntity {

    // ── synched data ──────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> DATA_ABSOLVED =
            SynchedEntityData.defineId(PutricielEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ABSOLUTION_COUNT =
            SynchedEntityData.defineId(PutricielEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ABSOLUTION_TIMER =
            SynchedEntityData.defineId(PutricielEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_NOVA_TIMER =
            SynchedEntityData.defineId(PutricielEntity.class, EntityDataSerializers.INT);

    private static final EntityDataAccessor<Integer> DATA_NOVA_RELEASE =
            SynchedEntityData.defineId(PutricielEntity.class, EntityDataSerializers.INT);

    public static final int VISUAL_ROT_NOVA = 1;
    public static final int VISUAL_ABSOLUTION_HIT = 2;

    // ── victory condition ─────────────────────────────────────────────────
    private static final int ABSOLUTIONS_NEEDED = PutricielVisualRules.ABSOLUTIONS_NEEDED;

    // ── rot nova ──────────────────────────────────────────────────────────
    private static final int ROT_NOVA_INTERVAL = PutricielVisualRules.NOVA_INTERVAL;
    private static final double ROT_NOVA_RADIUS = 8.0;
    private static final double ROT_NOVA_RADIUS_ESCALATED = 12.0;
    private static final float ROT_NOVA_DAMAGE = 4.0F;

    // ── absolution window ─────────────────────────────────────────────────
    private static final int ABSOLUTION_CYCLE = PutricielVisualRules.ABSOLUTION_CYCLE;
    private static final int ABSOLUTION_WINDOW_DURATION = PutricielVisualRules.ABSOLUTION_WINDOW;

    // ── animation states ──────────────────────────────────────────────────
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState absolvedAnimationState = new AnimationState();

    // ── server-side state ─────────────────────────────────────────────────
    private int absolveCycleTimer = ABSOLUTION_CYCLE;
    private int absolveWindowTimer = 0;
    private boolean defeated = false;

    // ── constructor ───────────────────────────────────────────────────────

    public PutricielEntity(EntityType<? extends PutricielEntity> type, Level level) {
        super(type, level, 120, new BossBarBinding("entity.hemomancy.putriciel", BossPresentationIds.FRAME_PUTRICIEL, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_10));
    }

    @Override
    public EnumSaintType saintType() {
        return EnumSaintType.PUTRICIEL;
    }

    // ── attributes ────────────────────────────────────────────────────────

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.20D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ARMOR, 12.0D);
    }

    // ── goals ─────────────────────────────────────────────────────────────

    @Override
    protected void registerGoals() {
        registerTrialGoals(0.6D);
    }

    // ── synched data ──────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ABSOLVED, false);
        builder.define(DATA_ABSOLUTION_COUNT, 0);
        builder.define(DATA_ABSOLUTION_TIMER, 0);
        builder.define(DATA_NOVA_TIMER, ROT_NOVA_INTERVAL);
        builder.define(DATA_NOVA_RELEASE, 0);
    }

    public boolean isAbsolved() {
        return this.entityData.get(DATA_ABSOLVED);
    }

    private void setAbsolved(boolean absolved) {
        this.entityData.set(DATA_ABSOLVED, absolved);
    }

    public int getAbsolutionCount() {
        return this.entityData.get(DATA_ABSOLUTION_COUNT);
    }

    public int getAbsolutionWindowTimer() {
        return this.entityData.get(DATA_ABSOLUTION_TIMER);
    }

    public int getNovaTimer() { return this.entityData.get(DATA_NOVA_TIMER); }

    public int getNovaReleaseTicks() { return this.entityData.get(DATA_NOVA_RELEASE); }

    public boolean isEscalated() { return this.getHealth() < this.getMaxHealth() * .5F; }

    private void incrementAbsolution() {
        int next = this.entityData.get(DATA_ABSOLUTION_COUNT) + 1;
        this.entityData.set(DATA_ABSOLUTION_COUNT, next);
    }

    // ── main tick ─────────────────────────────────────────────────────────

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            idleAnimationState.animateWhen(this.isAlive() && !isAbsolved(), this.tickCount);
            absolvedAnimationState.animateWhen(this.isAlive() && isAbsolved(), this.tickCount);
            return;
        }

        if (tickRise()) return;
        if (defeated || !isAlive()) return;
        ServerLevel server = (ServerLevel) this.level();
        tickVisualState();
        if (getNovaReleaseTicks() > 0) this.entityData.set(DATA_NOVA_RELEASE, getNovaReleaseTicks() - 1);

        bossBar.setProgress((float) getAbsolutionCount() / ABSOLUTIONS_NEEDED);

        this.entityData.set(DATA_NOVA_TIMER, isAbsolved() ? ROT_NOVA_INTERVAL : ROT_NOVA_INTERVAL - Math.floorMod(this.tickCount, ROT_NOVA_INTERVAL));

        // Rot nova pulse
        if (!isAbsolved() && this.tickCount % ROT_NOVA_INTERVAL == 0) {
            fireRotNova(server);
        }

        tickSecondaryAttacks(!isAbsolved());
        // Absolution cycle
        if (isAbsolved()) {
            absolveWindowTimer--;
            this.entityData.set(DATA_ABSOLUTION_TIMER, Math.max(0, absolveWindowTimer));
            if (PutricielCleansingRules.burns(absolveWindowTimer)) {
                for (Player player : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(PutricielCleansingRules.RADIUS),
                        p -> ManipulationCombatHelper.canHarmFor(this, p)
                                && distanceToSqr(p) <= PutricielCleansingRules.RADIUS * PutricielCleansingRules.RADIUS && hasLineOfSight(p))) {
                    CrimsonFireHelper.igniteCrimson(player, 2);
                    player.hurt(damageSources().magic(), 2);
                }
                PutricielEffects.burst(server, position().add(0, 1, 0), PutricielVisualRules.CLEANSE, (float) PutricielCleansingRules.RADIUS);
            }
            if (absolveWindowTimer <= 0) {
                endAbsolutionWindow(server);
            }
        } else {
            absolveCycleTimer--;
            if (absolveCycleTimer <= 0) {
                openAbsolutionWindow(server);
            }
        }
    }

    private void fireRotNova(ServerLevel server) {
        boolean escalated = isEscalated();
        double radius = escalated ? ROT_NOVA_RADIUS_ESCALATED : ROT_NOVA_RADIUS;
        int witherAmp = escalated ? 1 : 0;

        this.entityData.set(DATA_NOVA_RELEASE, PutricielVisualRules.NOVA_RELEASE);
        if (getVisualState() != VISUAL_ABSOLUTION_HIT)
            setVisualState(VISUAL_ROT_NOVA, PutricielVisualRules.NOVA_RELEASE);
        Vec3 center = Vec3.atBottomCenterOf(this.blockPosition());
        PutricielEffects.burst(server, center, PutricielVisualRules.NOVA, (float) radius);
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 1.5F, 0.8F);

        for (Player player : server.getEntitiesOfClass(Player.class,
                new AABB(this.blockPosition()).inflate(radius), p -> ManipulationCombatHelper.canHarmFor(this, p))) {
            player.hurt(this.damageSources().magic(), ROT_NOVA_DAMAGE);
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, witherAmp, false, false, true));
        }
    }

    private void openAbsolutionWindow(ServerLevel server) {
        setAbsolved(true);
        tickSecondaryAttacks(false);
        this.entityData.set(DATA_NOVA_RELEASE, 0);
        absolveWindowTimer = ABSOLUTION_WINDOW_DURATION;
        this.entityData.set(DATA_ABSOLUTION_TIMER, absolveWindowTimer);
        absolveCycleTimer = ABSOLUTION_CYCLE;

        PutricielEffects.burst(server, this.position().add(0, 1.8, 0), PutricielVisualRules.OPEN, 2);
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.2F, 0.7F);

        broadcastMessage("message.hemomancy.putriciel.the_rot_stills_absolve_him_now", ChatFormatting.GREEN);
    }

    private void endAbsolutionWindow(ServerLevel server) {
        setAbsolved(false);
        PutricielEffects.burst(server, this.position().add(0, 1.8, 0), PutricielVisualRules.CLOSE, 2);
        absolveWindowTimer = 0;
        this.entityData.set(DATA_ABSOLUTION_TIMER, 0);

        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.0F, 0.9F);
        broadcastMessage("message.hemomancy.putriciel.the_window_closes", ChatFormatting.DARK_GREEN);
    }

    // ── victory condition via damage during absolution window ─────────────

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (rising()) return false;
        if (defeated) return false;
        if (!this.level().isClientSide && !isAbsolved() && this.getHealth() - amount <= 1.0F) {
            this.setHealth(1.0F);
            return false;
        }
        if (!this.level().isClientSide && isAbsolved()) {
            incrementAbsolution();
            setVisualState(VISUAL_ABSOLUTION_HIT, PutricielVisualRules.HIT_TICKS);
            if (this.level() instanceof ServerLevel server) {
                PutricielEffects.burst(server, this.position().add(0, 1.8, 0), PutricielVisualRules.ABSOLVE, 1.6F);
            }
            if (getAbsolutionCount() >= ABSOLUTIONS_NEEDED) {
                onTrialComplete(source);
                return true;
            }
            if (this.getHealth() - amount <= 1.0F) {
                amount = Math.max(0.0F, this.getHealth() - 1.0F);
            }
            return super.hurt(source, amount) || amount > 0.0F;
        }
        return super.hurt(source, amount);
    }

    private void onTrialComplete(DamageSource source) {
        defeated = true;
        if (this.level() instanceof ServerLevel server)
            PutricielEffects.burst(server, this.position().add(0, 1.8, 0), PutricielVisualRules.REST, 3);
        broadcastMessage("message.hemomancy.putriciel.the_saint_finds_rest", ChatFormatting.GOLD);
        endTrial(source.getEntity() instanceof Player player ? player : null, source);
        this.discard();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (isAbsolved()) return false;
        boolean hit = super.doHurtTarget(target);
        if (hit && this.level() instanceof ServerLevel server)
            PutricielEffects.burst(server, target.position().add(0, target.getBbHeight() * .6, 0), PutricielVisualRules.MELEE, .8F);
        return hit;
    }

    @Override
    public void die(DamageSource source) {
        if (this.level() instanceof ServerLevel server)
            PutricielEffects.burst(server, this.position().add(0, 1.8, 0), PutricielVisualRules.REST, 3);
        super.die(source);
    }

    @Override
    protected void tickDeath() {
        if (++this.deathTime >= 20 && !this.level().isClientSide && !this.isRemoved())
            this.remove(RemovalReason.KILLED);
    }

    // ── sounds ────────────────────────────────────────────────────────────

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }
}
