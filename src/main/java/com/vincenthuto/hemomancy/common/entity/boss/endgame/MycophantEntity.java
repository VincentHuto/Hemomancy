package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class MycophantEntity extends Monster {
    private static final int FINAL_DEATH_TICKS = 150;

    private int attackTimer;
    private int deathTicks;
    public boolean clawStrikeFlag;

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
        //super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        ItemEntity itementity = this.spawnAtLocation(ItemInit.mycophant_tendril.get());
        if (itementity != null) {
            itementity.setExtendedLifetime();
            itementity.setInvulnerable(true);
        }
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
                EndgameBossActions.tickMycophantPattern(this);
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
                EndgameBossActions.finishWithExplosion(this, 3.0F);
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
