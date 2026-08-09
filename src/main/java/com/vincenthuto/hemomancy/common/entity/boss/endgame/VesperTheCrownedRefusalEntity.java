package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
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
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import net.neoforged.neoforge.entity.PartEntity;

import java.util.UUID;

public class VesperTheCrownedRefusalEntity extends Monster {
	private static final EntityDataAccessor<Integer> DATA_ATTACK = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ATTACK_TICK = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_BROKEN_ANCHORS = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_ACTIVE_ANCHOR = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TRANSITION_TICK = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);

    private boolean spawnedEveningStar = false;
	private final VesperThroneAnchorPart[] throneAnchors;
	private int attackStep;
	private int idleTicks = 30;
	private float activeAnchorDamage;
	private UUID ordealOwner;
	private long bloomOrigin;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.vesper_crowned_refusal"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_12);

    public VesperTheCrownedRefusalEntity(EntityType<? extends VesperTheCrownedRefusalEntity> type, Level level) {
        super(type, level);
		this.throneAnchors = new VesperThroneAnchorPart[] {
				new VesperThroneAnchorPart(this, 0),
				new VesperThroneAnchorPart(this, 1),
				new VesperThroneAnchorPart(this, 2)
		};
		this.setId(ENTITY_COUNTER.getAndAdd(this.throneAnchors.length + 1) + 1);
        this.setPersistenceRequired();
        this.xpReward = 0;
    }

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ATTACK, VesperPhaseOneAttack.IDLE.ordinal());
		builder.define(DATA_ATTACK_TICK, 0);
		builder.define(DATA_BROKEN_ANCHORS, 0);
		builder.define(DATA_ACTIVE_ANCHOR, -1);
		builder.define(DATA_TRANSITION_TICK, 0);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (DATA_ACTIVE_ANCHOR.equals(key)) refreshAnchorDimensions();
	}

	@Override
	public void setId(int id) {
		super.setId(id);
		for (int i = 0; i < throneAnchors.length; i++) throneAnchors[i].setId(id + i + 1);
	}

	@Override
	public boolean isMultipartEntity() {
		return true;
	}

	@Override
	public PartEntity<?>[] getParts() {
		return throneAnchors;
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
		updateAnchorPositions();
        if (!this.level().isClientSide) {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
            if (this.isAlive()) {
				tickEncounter();
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
		Vec3 landing = dismountLandingPosition();
		eveningStar.moveTo(landing.x, landing.y, landing.z, this.getYRot(), this.getXRot());
        eveningStar.setYHeadRot(this.getYHeadRot());
        eveningStar.setTarget(this.getTarget());
		VesperOrdealManager.copyOrdeal(this, eveningStar);
        eveningStar.finalizeSpawn(server, server.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
		eveningStar.beginAwakening();
        server.addFreshEntity(eveningStar);
    }

	private Vec3 dismountLandingPosition() {
		Vec3 forward = Vec3.directionFromRotation(0.0F, getYRot()).multiply(1.0D, 0.0D, 1.0D);
		if (forward.lengthSqr() < 0.01D) return position();
		return position().add(forward.normalize().scale(1.5D));
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
		tag.putInt("VesperAttack", getAttack().ordinal());
		tag.putInt("VesperAttackTick", getAttackTick());
		tag.putInt("BrokenThroneAnchors", getBrokenAnchorMask());
		tag.putInt("ActiveThroneAnchor", getActiveAnchor());
		tag.putFloat("ActiveAnchorDamage", activeAnchorDamage);
		tag.putInt("TransitionTick", getTransitionTick());
		tag.putInt("AttackStep", attackStep);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		ordealOwner = tag.hasUUID("OrdealOwner") ? tag.getUUID("OrdealOwner") : null;
		bloomOrigin = tag.getLong("BloomOrigin");
		setAttack(VesperPhaseOneAttack.values()[Math.max(0, Math.min(VesperPhaseOneAttack.values().length - 1,
				tag.getInt("VesperAttack")))]);
		entityData.set(DATA_ATTACK_TICK, tag.getInt("VesperAttackTick"));
		entityData.set(DATA_BROKEN_ANCHORS, tag.getInt("BrokenThroneAnchors"));
		entityData.set(DATA_ACTIVE_ANCHOR, tag.contains("ActiveThroneAnchor") ? tag.getInt("ActiveThroneAnchor") : -1);
		activeAnchorDamage = tag.getFloat("ActiveAnchorDamage");
		entityData.set(DATA_TRANSITION_TICK, tag.getInt("TransitionTick"));
		attackStep = tag.getInt("AttackStep");
		if (getActiveAnchor() >= 0) setNoAi(true);
		refreshAnchorDimensions();
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (getTransitionTick() > 0 || getActiveAnchor() >= 0) return false;
		float floor = VesperCombatRules.healthFloor(getMaxHealth(), getBrokenAnchorMask());
		float allowed = Math.max(0.0F, getHealth() - floor);
		return allowed > 0.0F && super.hurt(source, Math.min(amount, allowed));
	}

	boolean hurtAnchor(int anchorIndex, DamageSource source, float amount) {
		if (level().isClientSide || anchorIndex != getActiveAnchor() || amount <= 0.0F) return false;
		applyAnchorDamage(anchorIndex, amount);
		return true;
	}

	void applyPuppetBacklash() {
		if (!level().isClientSide && getActiveAnchor() >= 0) applyAnchorDamage(getActiveAnchor(), 10.0F);
	}

	private void applyAnchorDamage(int anchorIndex, float amount) {
		VesperCombatRules.AnchorHit hit = VesperCombatRules.hitAnchor(activeAnchorDamage, amount);
		activeAnchorDamage = hit.accumulatedDamage();
		if (hit.broken()) breakAnchor(anchorIndex);
	}

    @Override
    public boolean doHurtTarget(Entity target) {
		if (getActiveAnchor() >= 0 || getTransitionTick() > 0) return false;
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

	public VesperPhaseOneAttack getAttack() {
		return VesperPhaseOneAttack.values()[Math.max(0, Math.min(VesperPhaseOneAttack.values().length - 1,
				entityData.get(DATA_ATTACK)))];
	}

	public int getAttackTick() { return entityData.get(DATA_ATTACK_TICK); }
	public int getBrokenAnchorMask() { return entityData.get(DATA_BROKEN_ANCHORS); }
	public int getActiveAnchor() { return entityData.get(DATA_ACTIVE_ANCHOR); }
	public int getTransitionTick() { return entityData.get(DATA_TRANSITION_TICK); }

	private void tickEncounter() {
		if (getTransitionTick() > 0) {
			int transition = getTransitionTick() + 1;
			entityData.set(DATA_TRANSITION_TICK, transition);
			getNavigation().stop();
			EndgameBossActions.tickVesperTransformation(this, transition);
			if (VesperPhaseTransitionRules.isComplete(transition) && !spawnedEveningStar) {
				spawnedEveningStar = true;
				EndgameBossActions.finishVesperMountAbsorption(this);
				EndgameBossActions.clearVesperPuppets(this);
				spawnEveningStar();
				bossEvent.removeAllPlayers();
				discard();
			}
			return;
		}
		int anchor = VesperCombatRules.lockedAnchorIndex(getHealth(), getMaxHealth(), getBrokenAnchorMask());
		if (anchor >= 0 && getActiveAnchor() < 0) exposeAnchor(anchor);
		if (getActiveAnchor() >= 0) {
			getNavigation().stop();
			setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
			EndgameBossActions.tickExposedThroneAnchor(this, getActiveAnchor(), activeAnchorDamage);
			return;
		}
		if (getAttack() == VesperPhaseOneAttack.IDLE) {
			if (--idleTicks <= 0) {
				setAttack(VesperCombatRules.phaseOneAttack(attackStep++));
				entityData.set(DATA_ATTACK_TICK, 0);
			}
			return;
		}
		int attackTick = getAttackTick() + 1;
		entityData.set(DATA_ATTACK_TICK, attackTick);
		if (EndgameBossActions.tickVesperPhaseOneAttack(this, getAttack(), attackTick)) {
			setAttack(VesperPhaseOneAttack.IDLE);
			entityData.set(DATA_ATTACK_TICK, 0);
			idleTicks = getHealth() <= getMaxHealth() * 0.42F ? 24 : 36;
		}
	}

	private void exposeAnchor(int anchor) {
		entityData.set(DATA_ACTIVE_ANCHOR, anchor);
		activeAnchorDamage = 0.0F;
		setAttack(VesperPhaseOneAttack.IDLE);
		entityData.set(DATA_ATTACK_TICK, 0);
		setNoAi(true);
		setYHeadRot(getYRot());
		setYBodyRot(getYRot());
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		refreshAnchorDimensions();
		playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.8F, 0.55F + anchor * 0.12F);
	}

	private void breakAnchor(int anchor) {
		int mask = getBrokenAnchorMask() | (1 << anchor);
		entityData.set(DATA_BROKEN_ANCHORS, mask);
		entityData.set(DATA_ACTIVE_ANCHOR, -1);
		refreshAnchorDimensions();
		activeAnchorDamage = 0.0F;
		EndgameBossActions.breakThroneAnchor(this, anchor);
		if (mask == 7) {
			entityData.set(DATA_TRANSITION_TICK, 1);
			setInvulnerable(true);
			setAttack(VesperPhaseOneAttack.IDLE);
		} else setNoAi(false);
	}

	private void refreshAnchorDimensions() {
		for (VesperThroneAnchorPart anchor : throneAnchors) anchor.refreshAnchorDimensions();
	}

	private void setAttack(VesperPhaseOneAttack attack) {
		entityData.set(DATA_ATTACK, attack.ordinal());
	}

	private void updateAnchorPositions() {
		double[][] local = { { 0.0D, 3.0D, 0.0D }, { 0.0D, 3.0D, 0.0D }, { 0.0D, 3.0D, 0.0D } };
		float yaw = -getYRot() * Mth.DEG_TO_RAD;
		for (int i = 0; i < throneAnchors.length; i++) {
			double x = local[i][0] * Mth.cos(yaw) - local[i][2] * Mth.sin(yaw);
			double z = local[i][0] * Mth.sin(yaw) + local[i][2] * Mth.cos(yaw);
			VesperThroneAnchorPart part = throneAnchors[i];
			part.xo = part.getX();
			part.yo = part.getY();
			part.zo = part.getZ();
			part.xOld = part.getX();
			part.yOld = part.getY();
			part.zOld = part.getZ();
			part.setPos(getX() + x, getY() + local[i][1], getZ() + z);
		}
	}
}
