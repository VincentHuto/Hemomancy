package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.worldgen.MycophantEncounterManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class MycophantEntity extends Monster {
    public enum NurseryAttack { TENDRIL_SWEEP, COCOON, NECTAR_SURGE, MORPHIC_SHIFT }
    private static final int FINAL_DEATH_TICKS = 100;

    private int attackTimer;
    private int deathTicks;
    public boolean clawStrikeFlag;
    @Nullable private UUID encounterOwner;
    private float nectarPressure;
    private int cocoonNodes;
    private int cocoonTicks;
    private int cocoonNodeHealth;
    private int interruptTicks;
    private int phaseTransitionTicks;
    private int pendingSweep = -1;
    private int pendingCocoon = -1;
    private int pendingSurge = -1;
    private boolean enteredPhaseTwo;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.mycophant"),
            BossEvent.BossBarColor.YELLOW,
            BossEvent.BossBarOverlay.NOTCHED_12);

    public MycophantEntity(EntityType<? extends MycophantEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 180;
    }
    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        if (this.encounterOwner == null) super.dropCustomDeathLoot(level, damageSource, recentlyHit);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 720.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.ATTACK_DAMAGE, 16.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.15D)
                .add(Attributes.ARMOR, 20.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.35D, 64.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.12D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 36.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            if (this.isAlive()) {
                if (this.encounterOwner == null) EndgameBossActions.tickMycophantPattern(this);
                else tickNurseryCombat((ServerLevel) this.level());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.attackTimer > 0) {
            this.attackTimer--;
        }
        this.clawStrikeFlag = this.tickCount % 50 > 10;
        EndgameBossActions.tickMycophantClientParticles(this);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    protected void tickDeath() {
        this.deathTicks++;
        EndgameBossActions.tickMycophantDeathParticles(this, this.deathTicks);
        if (this.deathTicks >= FINAL_DEATH_TICKS) {
            if (!this.level().isClientSide) {
                if (this.encounterOwner != null) MycophantEncounterManager.completeVictory(this);
                else EndgameBossActions.finishWithExplosion(this, 3.0F);
                this.bossEvent.removeAllPlayers();
            }
            this.deathTime = 19;
            super.tickDeath();
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            this.attackTimer = 10;
            this.playSound(SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.25F);
            EndgameBossActions.disableShieldOnHit(this, target, 80);
        }
        return hurt;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public int getAttackTimer() {
        return this.attackTimer;
    }

    @Nullable
    public UUID getEncounterOwner() { return this.encounterOwner; }

    public void setEncounterOwner(@Nullable UUID owner) {
        this.encounterOwner = owner;
        if (owner != null) this.xpReward = 0;
    }

    public float getNectarPressure() { return this.nectarPressure; }
    public int getCocoonNodes() { return this.cocoonNodes; }

    public void ruptureCocoon(boolean empowered) {
        if (this.cocoonNodes <= 0) return;
        if (empowered) {
            this.cocoonNodes = Math.max(0, this.cocoonNodes - 2);
            this.cocoonNodeHealth = 4;
        } else if (--this.cocoonNodeHealth <= 0) {
            this.cocoonNodes--;
            this.cocoonNodeHealth = 4;
        }
        if (this.cocoonNodes == 0) {
            this.cocoonTicks = 0;
            this.nectarPressure = MycophantCombatRules.pressureAfterEscape(this.nectarPressure, empowered);
            if (empowered) {
                this.interruptTicks = 40;
                Vec3 away = this.position().subtract(this.getTarget() == null ? this.position() : this.getTarget().position()).normalize().scale(1.25);
                this.setDeltaMovement(away.x, 0.35, away.z);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.encounterOwner != null && this.cocoonNodes > 0 && source.getEntity() instanceof ServerPlayer player
                && this.encounterOwner.equals(player.getUUID())) {
            ruptureCocoon(false);
            return true;
        }
        return super.hurt(source, amount);
    }

    private void tickNurseryCombat(ServerLevel level) {
        ServerPlayer owner = this.encounterOwner == null ? null : level.getServer().getPlayerList().getPlayer(this.encounterOwner);
        if (owner == null || !owner.isAlive()) return;
        this.setTarget(owner);
        int phase = MycophantCombatRules.phase(this.getHealth(), this.getMaxHealth());
        if (phase == 2 && !this.enteredPhaseTwo) {
            this.enteredPhaseTwo = true;
            this.phaseTransitionTicks = 60;
            this.nectarPressure = Math.max(50.0F, this.nectarPressure);
            level.playSound(null, this.blockPosition(), SoundInit.ENTITY_MYCOPHANT_TELEPORT.get(), SoundSource.HOSTILE, 1.1F, 0.55F);
            owner.displayClientMessage(Component.translatable("message.hemomancy.mycophant.tender_phase")
                    .withStyle(net.minecraft.ChatFormatting.DARK_GREEN, net.minecraft.ChatFormatting.ITALIC), true);
        }
        if (this.phaseTransitionTicks > 0) { this.phaseTransitionTicks--; return; }
        if (this.interruptTicks > 0) { this.interruptTicks--; return; }

        if (this.tickCount % 20 == 0) this.nectarPressure = MycophantCombatRules.pressureAfterSecond(this.nectarPressure, phase);
        applyPressureHazard(owner);
        tickCocoon(owner);
        this.pendingSweep = tickTelegraph(this.pendingSweep, () -> sweep(owner));
        this.pendingCocoon = tickTelegraph(this.pendingCocoon, () -> startCocoon(owner, phase));
        this.pendingSurge = tickTelegraph(this.pendingSurge, () -> surge(owner));
        if (this.cocoonNodes == 0) {
            if (this.pendingSweep < 0 && this.tickCount % MycophantCombatRules.sweepCadenceTicks(phase) == 0) this.pendingSweep = 30;
            if (this.pendingCocoon < 0 && this.tickCount % MycophantCombatRules.cocoonCadenceTicks(phase) == 0) this.pendingCocoon = 30;
            if (this.pendingSurge < 0 && this.tickCount % MycophantCombatRules.surgeCadenceTicks(phase) == 0) this.pendingSurge = 30;
        }
        this.bossEvent.setName(Component.translatable("entity.hemomancy.mycophant")
                .append(Component.literal("  Nectar " + Math.round(this.nectarPressure) + "%")));
    }

    private int tickTelegraph(int ticks, Runnable action) {
        if (ticks < 0) return -1;
        if (ticks == 30) this.playSound(SoundInit.ENTITY_MYCOPHANT_HURT_OTHER.get(), 0.8F, 0.65F);
        if (ticks == 0) { action.run(); return -1; }
        return ticks - 1;
    }

    private void sweep(ServerPlayer owner) {
        if (this.distanceToSqr(owner) <= 100.0) {
            owner.hurt(this.damageSources().mobAttack(this), 10.0F);
            Vec3 push = owner.position().subtract(this.position()).normalize().scale(1.5);
            owner.push(push.x, 0.3, push.z);
        }
    }

    private void startCocoon(ServerPlayer owner, int phase) {
        this.cocoonNodes = MycophantCombatRules.cocoonNodeCount(phase);
        this.cocoonNodeHealth = 4;
        this.cocoonTicks = 200;
        owner.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 50, 0, false, false));
    }

    private void tickCocoon(ServerPlayer owner) {
        if (this.cocoonNodes <= 0) return;
        Vec3 motion = owner.getDeltaMovement();
        owner.setDeltaMovement(0.0, motion.y, 0.0);
        if (--this.cocoonTicks <= 0) {
            this.cocoonNodes = 0;
            owner.hurt(this.damageSources().mobAttack(this), 8.0F);
            this.nectarPressure = MycophantCombatRules.pressureAfterFailedCocoon(this.nectarPressure);
        }
    }

    private void surge(ServerPlayer owner) {
        this.nectarPressure = MycophantCombatRules.pressureAfterSurge(this.nectarPressure);
        for (Player player : this.level().getEntitiesOfClass(Player.class, new AABB(this.blockPosition()).inflate(24)))
            if (player == owner) player.hurt(this.damageSources().mobAttack(this), 6.0F);
    }

    private void applyPressureHazard(ServerPlayer owner) {
        switch (MycophantCombatRules.nectarHazard(this.nectarPressure)) {
            case SLOW -> owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 0, false, false));
            case DEEP -> owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1, false, false));
            case ENGULFING -> {
                owner.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 2, false, false));
                if (this.tickCount % 20 == 0) owner.hurt(this.damageSources().drown(), 2.0F);
            }
            default -> { }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.encounterOwner != null) tag.putUUID("MycophantOwner", this.encounterOwner);
        tag.putFloat("NectarPressure", this.nectarPressure);
        tag.putInt("CocoonNodes", this.cocoonNodes);
        tag.putInt("CocoonTicks", this.cocoonTicks);
        tag.putInt("CocoonNodeHealth", this.cocoonNodeHealth);
        tag.putBoolean("EnteredPhaseTwo", this.enteredPhaseTwo);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.encounterOwner = tag.hasUUID("MycophantOwner") ? tag.getUUID("MycophantOwner") : null;
        this.nectarPressure = tag.getFloat("NectarPressure");
        this.cocoonNodes = tag.getInt("CocoonNodes");
        this.cocoonTicks = tag.getInt("CocoonTicks");
        this.cocoonNodeHealth = Math.max(1, tag.getInt("CocoonNodeHealth"));
        this.enteredPhaseTwo = tag.getBoolean("EnteredPhaseTwo");
        if (this.encounterOwner != null) this.xpReward = 0;
    }

    @Override
    protected float getSoundVolume() {
        return 0.65F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundInit.ENTITY_MYCOPHANT_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundInit.ENTITY_MYCOPHANT_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundInit.ENTITY_MYCOPHANT_DEATH.get();
    }
}
