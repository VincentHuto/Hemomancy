package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;

import java.util.UUID;

public class VesperTheCrownedRefusalEntity extends Monster {
    private static final int EVENING_STAR_TRANSITION_TICKS = 120;

    private boolean spawnedEveningStar = false;
    private int deathTicks;
	private UUID ordealOwner;
	private long bloomOrigin;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.vesper_crowned_refusal"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_12);

    public VesperTheCrownedRefusalEntity(EntityType<? extends VesperTheCrownedRefusalEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 520.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.16D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 56.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                .add(Attributes.ARMOR, 16.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(2, new MoveTowardsTargetGoal(this, 1.25D, 56.0F));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.12D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 28.0F));
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
                EndgameBossActions.tickVesperPattern(this, 0);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        EndgameBossActions.tickVesperClientParticles(this);
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
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(0.0F);
        }
    }

    @Override
    protected void tickDeath() {
        this.deathTicks++;
        EndgameBossActions.tickVesperDeathParticles(this, this.deathTicks);
        if (this.deathTicks >= EVENING_STAR_TRANSITION_TICKS) {
            if (!this.level().isClientSide && !this.spawnedEveningStar) {
                this.spawnedEveningStar = true;
                EndgameBossActions.finishWithExplosion(this, 2.0F);
                spawnEveningStar();
                this.bossEvent.removeAllPlayers();
            }
            this.deathTime = 19;
            super.tickDeath();
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        // Phase one is a threshold, not the reward-bearing kill.
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    private void spawnEveningStar() {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        VesperTheEveningStarEntity eveningStar = EntityInit.vesper_evening_star.get().create(server);
        if (eveningStar == null) {
            return;
        }
        eveningStar.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
        eveningStar.setYHeadRot(this.getYHeadRot());
        eveningStar.setTarget(this.getTarget());
		VesperOrdealManager.copyOrdeal(this, eveningStar);
        eveningStar.finalizeSpawn(server, server.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        server.addFreshEntity(eveningStar);
    }

	public void setOrdeal(UUID owner, long bloomOrigin) {
		this.ordealOwner = owner;
		this.bloomOrigin = bloomOrigin;
	}

	public UUID getOrdealOwner() { return ordealOwner; }
	public long getBloomOrigin() { return bloomOrigin; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (ordealOwner != null) tag.putUUID("OrdealOwner", ordealOwner);
		tag.putLong("BloomOrigin", bloomOrigin);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		ordealOwner = tag.hasUUID("OrdealOwner") ? tag.getUUID("OrdealOwner") : null;
		bloomOrigin = tag.getLong("BloomOrigin");
	}

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hurt = super.doHurtTarget(target);
        if (hurt) {
            EndgameBossActions.disableShieldOnHit(this, target, 80);
        }
        return hurt;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(SoundEvents.COW_STEP, 0.18F, 0.45F);
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundInit.ENTITY_VESPER_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundInit.ENTITY_VESPER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundInit.ENTITY_VESPER_DEATH.get();
    }
}
