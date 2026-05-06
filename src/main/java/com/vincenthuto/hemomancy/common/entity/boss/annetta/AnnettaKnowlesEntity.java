package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Annetta Knowles — The Stained Priestess.
 * <p>
 * A high-ranking Unstained cleric whose sanity shattered when a Chthonian bit
 * her — proving she was infected all along. Her long-suppressed blood power
 * erupts in two phases.
 * <p>
 * Phase 1 (100% → 30% HP): Melee combat augmented by a silver aura. Every
 * {@link #SILVER_AURA_INTERVAL} ticks she pulses radiant silver energy that
 * harms nearby players whose blood is active (Harbingers), reflecting the
 * Unstained doctrine she still half-believes.
 * <p>
 * Phase 2 (≤ 30% HP): She tears a hyper-solidified blood spear from her own
 * body. Attack speed increases, each hit drains the target's blood capability,
 * and carmine particles trail her movements.
 */
public class AnnettaKnowlesEntity extends Monster {

    // ── synched data ──────────────────────────────────────────────────────
    private static final EntityDataAccessor<Integer> DATA_PHASE =
            SynchedEntityData.defineId(AnnettaKnowlesEntity.class, EntityDataSerializers.INT);

    // ── phase thresholds ──────────────────────────────────────────────────
    /** HP fraction at which Phase 2 triggers. */
    private static final float PHASE_2_THRESHOLD = 0.30F;

    // ── silver aura (Phase 1) ─────────────────────────────────────────────
    /** Ticks between silver aura pulses. */
    private static final int SILVER_AURA_INTERVAL = 60;
    /** Radius of the silver aura pulse. */
    private static final double SILVER_AURA_RADIUS = 6.0;
    /** Damage dealt by a silver aura pulse to blood-active players. */
    private static final float SILVER_AURA_DAMAGE = 3.0F;

    // ── blood spear (Phase 2) ─────────────────────────────────────────────
    /** Blood drained from target on each blood-spear hit. */
    private static final int BLOOD_SPEAR_DRAIN = 250;
    /** Interval at which Phase 2 carmine particles emit. */
    private static final int PHASE_2_PARTICLE_INTERVAL = 10;

    // ── animation states ──────────────────────────────────────────────────
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState phase2AnimationState = new AnimationState();

    // ── server-side state ─────────────────────────────────────────────────
    private boolean enteredPhase2 = false;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.annetta_knowles"),
            BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.NOTCHED_10);

    // ── constructor ───────────────────────────────────────────────────────

    public AnnettaKnowlesEntity(EntityType<? extends AnnettaKnowlesEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 100;
    }

    // ── attributes ────────────────────────────────────────────────────────

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 350.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.26D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.ARMOR, 8.0D);
    }

    // ── goals ─────────────────────────────────────────────────────────────

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ── synched data ──────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PHASE, 1);
    }

    public int getPhase() {
        return this.entityData.get(DATA_PHASE);
    }

    private void setPhase(int phase) {
        this.entityData.set(DATA_PHASE, phase);
    }

    // ── boss bar ──────────────────────────────────────────────────────────

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    // ── main tick ─────────────────────────────────────────────────────────

    @Override
    public void aiStep() {
        super.aiStep();

        if (this.level().isClientSide) {
            idleAnimationState.animateWhen(this.isAlive() && getPhase() == 1, this.tickCount);
            phase2AnimationState.animateWhen(this.isAlive() && getPhase() == 2, this.tickCount);
            return;
        }

        ServerLevel server = (ServerLevel) this.level();

        // Boss bar progress
        bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        // Phase transition
        if (!enteredPhase2 && this.getHealth() <= this.getMaxHealth() * PHASE_2_THRESHOLD) {
            enterPhase2(server);
        }

        if (getPhase() == 1) {
            tickPhase1(server);
        } else {
            tickPhase2(server);
        }
    }

    private void tickPhase1(ServerLevel server) {
        // Silver aura pulse — harms blood-active players nearby
        if (this.tickCount % SILVER_AURA_INTERVAL == 0) {
            server.sendParticles(ParticleTypes.SOUL,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    30, SILVER_AURA_RADIUS * 0.5, 0.8, SILVER_AURA_RADIUS * 0.5, 0.02);
            server.playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.BEACON_POWER_SELECT, SoundSource.HOSTILE, 1.2F, 1.6F);

            for (Player player : server.getEntitiesOfClass(Player.class,
                    new AABB(this.blockPosition()).inflate(SILVER_AURA_RADIUS))) {
                HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
                    if (vol.isActive()) {
                        player.hurt(this.damageSources().magic(), SILVER_AURA_DAMAGE);
                        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0, false, true));
                    }
                });
            }
        }
    }

    private void tickPhase2(ServerLevel server) {
        // Carmine particle trail
        if (this.tickCount % PHASE_2_PARTICLE_INTERVAL == 0) {
            server.sendParticles(ParticleTypes.FALLING_LAVA,
                    this.getX(), this.getY() + 1.0, this.getZ(),
                    4, 0.3, 0.5, 0.3, 0.01);
        }
    }

    // ── phase 2 transition ────────────────────────────────────────────────

    private void enterPhase2(ServerLevel server) {
        enteredPhase2 = true;
        setPhase(2);

        // Speed boost — she becomes faster and more desperate
        var speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.setBaseValue(0.36D);
        var attackAttr = this.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) attackAttr.setBaseValue(11.0D);

        server.sendParticles(ParticleTypes.LARGE_SMOKE,
                this.getX(), this.getY() + 1.0, this.getZ(),
                60, 0.6, 1.0, 0.6, 0.05);
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_SHOOT, SoundSource.HOSTILE, 1.5F, 0.6F);

        broadcastPhase2Message(server);
    }

    private void broadcastPhase2Message(ServerLevel server) {
        Component msg = Component.literal("She tears the spear from herself — ")
                .withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal("\"Was I ever clean?\"").withStyle(ChatFormatting.ITALIC, ChatFormatting.RED));
        for (ServerPlayer player : bossEvent.getPlayers()) {
            player.sendSystemMessage(msg);
        }
    }

    // ── melee hit — blood drain in Phase 2 ───────────────────────────────

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && getPhase() == 2 && target instanceof Player player
                && !this.level().isClientSide) {
            HemoCapabilityAccess.getBloodVolume(player).ifPresent(vol -> {
                if (vol.isActive() && vol.getBloodVolume() >= BLOOD_SPEAR_DRAIN) {
                    vol.setBloodVolume(vol.getBloodVolume() - BLOOD_SPEAR_DRAIN);
                }
            });
        }
        return result;
    }

    // ── drops ─────────────────────────────────────────────────────────────

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        // Stained Priestess drops reflect the collision of both paths
        this.spawnAtLocation(new ItemStack(ItemInit.pale_silver_ingot.get(), 2));
        this.spawnAtLocation(new ItemStack(ItemInit.hematic_iron_scrap.get(), 3));
    }

    // ── sounds ────────────────────────────────────────────────────────────

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }
}
