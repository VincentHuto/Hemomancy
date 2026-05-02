package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

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
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Putriciel — The Rotting Saint.
 * <p>
 * Third of the entombed Saints; tendencies MORTEM + FLAMMEUS. In life he was
 * a practitioner whose mastery of death-adjacent blood produced the Bloom of
 * Rot — the seeding of the Qliphoth in living ground. His Mausoleum trial
 * requires players to endure a rot-fire environment while finding moments to
 * perform absolution acts that advance the victory condition.
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
public class PutricielEntity extends Monster {

    // ── synched data ──────────────────────────────────────────────────────
    private static final EntityDataAccessor<Boolean> DATA_ABSOLVED =
            SynchedEntityData.defineId(PutricielEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_ABSOLUTION_COUNT =
            SynchedEntityData.defineId(PutricielEntity.class, EntityDataSerializers.INT);

    // ── victory condition ─────────────────────────────────────────────────
    private static final int ABSOLUTIONS_NEEDED = 5;

    // ── rot nova ──────────────────────────────────────────────────────────
    private static final int ROT_NOVA_INTERVAL = 120;
    private static final double ROT_NOVA_RADIUS = 8.0;
    private static final double ROT_NOVA_RADIUS_ESCALATED = 12.0;
    private static final float ROT_NOVA_DAMAGE = 4.0F;

    // ── absolution window ─────────────────────────────────────────────────
    private static final int ABSOLUTION_CYCLE = 300;
    private static final int ABSOLUTION_WINDOW_DURATION = 80;

    // ── animation states ──────────────────────────────────────────────────
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState absolvedAnimationState = new AnimationState();

    // ── server-side state ─────────────────────────────────────────────────
    private int absolveCycleTimer = ABSOLUTION_CYCLE;
    private int absolveWindowTimer = 0;
    private boolean defeated = false;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.putriciel"),
            BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.NOTCHED_10);

    // ── constructor ───────────────────────────────────────────────────────

    public PutricielEntity(EntityType<? extends PutricielEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 120;
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
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.6D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 24.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // ── synched data ──────────────────────────────────────────────────────

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ABSOLVED, false);
        builder.define(DATA_ABSOLUTION_COUNT, 0);
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

    private void incrementAbsolution() {
        int next = this.entityData.get(DATA_ABSOLUTION_COUNT) + 1;
        this.entityData.set(DATA_ABSOLUTION_COUNT, next);
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
            idleAnimationState.animateWhen(this.isAlive() && !isAbsolved(), this.tickCount);
            absolvedAnimationState.animateWhen(this.isAlive() && isAbsolved(), this.tickCount);
            return;
        }

        if (defeated) return;
        ServerLevel server = (ServerLevel) this.level();

        bossEvent.setProgress((float) getAbsolutionCount() / ABSOLUTIONS_NEEDED);

        // Rot nova pulse
        if (this.tickCount % ROT_NOVA_INTERVAL == 0) {
            fireRotNova(server);
        }

        // Absolution cycle
        if (isAbsolved()) {
            absolveWindowTimer--;
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
        boolean escalated = this.getHealth() < this.getMaxHealth() * 0.5F;
        double radius = escalated ? ROT_NOVA_RADIUS_ESCALATED : ROT_NOVA_RADIUS;
        int witherAmp = escalated ? 1 : 0;

        server.sendParticles(ParticleTypes.SOUL,
                this.getX(), this.getY() + 0.5, this.getZ(),
                60, radius * 0.5, 0.3, radius * 0.5, 0.05);
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.WITHER_AMBIENT, SoundSource.HOSTILE, 1.5F, 0.8F);

        for (Player player : server.getEntitiesOfClass(Player.class,
                new AABB(this.blockPosition()).inflate(radius))) {
            player.hurt(this.damageSources().magic(), ROT_NOVA_DAMAGE);
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, witherAmp, false, true));
            player.igniteForSeconds(3);
        }
    }

    private void openAbsolutionWindow(ServerLevel server) {
        setAbsolved(true);
        absolveWindowTimer = ABSOLUTION_WINDOW_DURATION;
        absolveCycleTimer = ABSOLUTION_CYCLE;

        server.sendParticles(ParticleTypes.MYCELIUM,
                this.getX(), this.getY() + 1.5, this.getZ(),
                50, 0.5, 1.0, 0.5, 0.02);
        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 1.2F, 0.7F);

        broadcastMessage("The rot stills — absolve him now!", ChatFormatting.GREEN);
    }

    private void endAbsolutionWindow(ServerLevel server) {
        setAbsolved(false);
        absolveWindowTimer = 0;

        server.playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.BEACON_DEACTIVATE, SoundSource.HOSTILE, 1.0F, 0.9F);
        broadcastMessage("The window closes.", ChatFormatting.DARK_GREEN);
    }

    // ── victory condition via damage during absolution window ─────────────

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!this.level().isClientSide && !isAbsolved() && this.getHealth() - amount <= 1.0F) {
            this.setHealth(1.0F);
            return false;
        }
        if (!this.level().isClientSide && isAbsolved()) {
            incrementAbsolution();
            if (getAbsolutionCount() >= ABSOLUTIONS_NEEDED) {
                onTrialComplete((ServerLevel) this.level());
                return true;
            }
            if (this.getHealth() - amount <= 1.0F) {
                amount = Math.max(0.0F, this.getHealth() - 1.0F);
            }
            return super.hurt(source, amount) || amount > 0.0F;
        }
        return super.hurt(source, amount);
    }

    private void onTrialComplete(ServerLevel server) {
        defeated = true;
        broadcastMessage("The Saint finds rest.", ChatFormatting.GOLD);
        // Spawn Hallowed Residuum of Putriciel
        this.spawnAtLocation(new ItemStack(ItemInit.hallowed_residuum_putriciel.get(), 1));
        this.discard();
    }

    // ── drops ─────────────────────────────────────────────────────────────

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        // Normal death (outside trial) drops nothing; trial completion handled above
    }

    // ── messages ─────────────────────────────────────────────────────────

    private void broadcastMessage(String text, ChatFormatting color) {
        Component msg = Component.literal(text).withStyle(color);
        for (ServerPlayer player : bossEvent.getPlayers()) {
            player.sendSystemMessage(msg);
        }
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
