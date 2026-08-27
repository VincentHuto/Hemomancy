package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
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
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import net.neoforged.neoforge.entity.PartEntity;

import java.util.UUID;
import java.util.Optional;

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
	private static final EntityDataAccessor<Float> DATA_VULNERABLE_YAW = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_ACTIVE_ANCHOR_DAMAGE = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Integer> DATA_ANCHOR_FLASH_TICKS = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_CARAPACE_EXPOSED = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_CARAPACE_COOLDOWN = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_RESTRAINED_VICTIM_ID = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_WINGS_GROWN = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_FLIGHT_MODE = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_FLIGHT_TICK = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_LOCKED_AIM_X = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_LOCKED_AIM_Y = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_LOCKED_AIM_Z = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.FLOAT);

    private boolean spawnedEveningStar = false;
	private final VesperThroneAnchorPart[] throneAnchors;
	private int attackStep;
	private int idleTicks = 30;
	private VesperPhaseOneAttack lastAttack = VesperPhaseOneAttack.IDLE;
	private UUID restrainedVictim;
	private int grabHitMask;
	private int groundedFlightTicks;
	private int airborneTicks;
	private int sortieCount;
	private int flightHitMask;
	private int postAttackCircleTicks;
	private boolean summonedFlightArenaBound;
	private double summonedFlightArenaCenterX;
	private double summonedFlightArenaFloorY;
	private double summonedFlightArenaCenterZ;
	private String summonedFlightArenaDimension = "";
	private UUID ordealOwner;
	private long bloomOrigin;
	public final AnimationState transformationAnimationState = new AnimationState();

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
		builder.define(DATA_VULNERABLE_YAW, 0.0F);
		builder.define(DATA_ACTIVE_ANCHOR_DAMAGE, 0.0F);
		builder.define(DATA_ANCHOR_FLASH_TICKS, 0);
		builder.define(DATA_CARAPACE_EXPOSED, false);
		builder.define(DATA_CARAPACE_COOLDOWN, 0);
		builder.define(DATA_RESTRAINED_VICTIM_ID, -1);
		builder.define(DATA_WINGS_GROWN, false);
		builder.define(DATA_FLIGHT_MODE, VesperWingedFlightRules.FlightMode.GROUNDED.ordinal());
		builder.define(DATA_FLIGHT_TICK, 0);
		builder.define(DATA_LOCKED_AIM_X, 0.0F);
		builder.define(DATA_LOCKED_AIM_Y, 0.0F);
		builder.define(DATA_LOCKED_AIM_Z, 0.0F);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (DATA_ACTIVE_ANCHOR.equals(key)) refreshAnchorDimensions();
		if ((DATA_ACTIVE_ANCHOR.equals(key) || DATA_VULNERABLE_YAW.equals(key)) && getActiveAnchor() >= 0) {
			enforceVulnerableYawLock();
		}
		if (DATA_TRANSITION_TICK.equals(key) && level().isClientSide()) {
			VesperPhaseTransitionRules.syncAnimationState(
					transformationAnimationState, tickCount, getTransitionTick());
		}
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
		if (getActiveAnchor() >= 0) enforceVulnerableYawLock();
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
		if (level().isClientSide()) {
			VesperPhaseTransitionRules.syncAnimationState(
					transformationAnimationState, tickCount, getTransitionTick());
		}
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

    private boolean spawnEveningStar() {
        if (!(this.level() instanceof ServerLevel server)) {
            return false;
        }
        VesperTheEveningStarEntity eveningStar = EntityInit.vesper_evening_star.get().create(server);
        if (eveningStar == null) {
            return false;
        }
		Vec3 landing = dismountLandingPosition();
		eveningStar.moveTo(landing.x, landing.y, landing.z, this.getYRot(), this.getXRot());
        eveningStar.setYHeadRot(this.getYHeadRot());
        eveningStar.setTarget(this.getTarget());
		VesperOrdealManager.copyOrdeal(this, eveningStar);
        eveningStar.finalizeSpawn(server, server.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        return server.addFreshEntity(eveningStar);
    }

	private Vec3 dismountLandingPosition() {
		Vec3 forward = Vec3.directionFromRotation(0.0F, getYRot()).multiply(1.0D, 0.0D, 1.0D);
		if (forward.lengthSqr() < 0.01D) return position();
		return position().add(forward.normalize().scale(1.5D));
	}

	public void setOrdeal(UUID owner, long bloomOrigin) {
		this.ordealOwner = owner;
		this.bloomOrigin = bloomOrigin;
		this.summonedFlightArenaBound = false;
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
		tag.putFloat("ActiveAnchorDamage", getActiveAnchorDamage());
		new VesperVulnerableRotation(getActiveAnchor() >= 0, getVulnerableYaw()).save(tag);
		tag.putInt("TransitionTick", getTransitionTick());
		tag.putInt("AttackStep", attackStep);
		tag.putInt("LastVesperAttack", lastAttack.ordinal());
		tag.putInt("CarapaceCooldown", getCarapaceCooldown());
		tag.putInt("GrabHitMask", grabHitMask);
		tag.putBoolean("WingsGrown", hasWingsGrown());
		tag.putInt("FlightMode", getFlightMode().ordinal());
		tag.putInt("FlightTick", getFlightTick());
		tag.putInt("GroundedFlightTicks", groundedFlightTicks);
		tag.putInt("AirborneTicks", airborneTicks);
		tag.putInt("SortieCount", sortieCount);
		tag.putInt("FlightHitMask", flightHitMask);
		tag.putInt("PostAttackCircleTicks", postAttackCircleTicks);
		tag.putFloat("LockedFlightAimX", entityData.get(DATA_LOCKED_AIM_X));
		tag.putFloat("LockedFlightAimY", entityData.get(DATA_LOCKED_AIM_Y));
		tag.putFloat("LockedFlightAimZ", entityData.get(DATA_LOCKED_AIM_Z));
		tag.putBoolean("SummonedFlightArenaBound", summonedFlightArenaBound);
		if (summonedFlightArenaBound) {
			tag.putDouble("SummonedFlightArenaCenterX", summonedFlightArenaCenterX);
			tag.putDouble("SummonedFlightArenaFloorY", summonedFlightArenaFloorY);
			tag.putDouble("SummonedFlightArenaCenterZ", summonedFlightArenaCenterZ);
			tag.putString("SummonedFlightArenaDimension", summonedFlightArenaDimension);
		}
		if (restrainedVictim != null) tag.putUUID("RestrainedVictim", restrainedVictim);
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
		entityData.set(DATA_ACTIVE_ANCHOR_DAMAGE,
				VesperCombatRules.clampAnchorDamage(tag.getFloat("ActiveAnchorDamage")));
		entityData.set(DATA_TRANSITION_TICK, tag.getInt("TransitionTick"));
		attackStep = tag.getInt("AttackStep");
		lastAttack = VesperPhaseOneAttack.values()[Math.max(0, Math.min(VesperPhaseOneAttack.values().length - 1,
				tag.getInt("LastVesperAttack")))];
		entityData.set(DATA_CARAPACE_COOLDOWN, Math.max(0, tag.getInt("CarapaceCooldown")));
		grabHitMask = tag.getInt("GrabHitMask");
		entityData.set(DATA_WINGS_GROWN, tag.getBoolean("WingsGrown"));
		entityData.set(DATA_FLIGHT_MODE, clampFlightMode(tag.getInt("FlightMode")).ordinal());
		entityData.set(DATA_FLIGHT_TICK, Math.max(0, tag.getInt("FlightTick")));
		groundedFlightTicks = Math.max(0, tag.getInt("GroundedFlightTicks"));
		airborneTicks = Math.max(0, tag.getInt("AirborneTicks"));
		sortieCount = Math.max(0, tag.getInt("SortieCount"));
		flightHitMask = tag.getInt("FlightHitMask");
		postAttackCircleTicks = Math.max(0, tag.getInt("PostAttackCircleTicks"));
		entityData.set(DATA_LOCKED_AIM_X, tag.getFloat("LockedFlightAimX"));
		entityData.set(DATA_LOCKED_AIM_Y, tag.getFloat("LockedFlightAimY"));
		entityData.set(DATA_LOCKED_AIM_Z, tag.getFloat("LockedFlightAimZ"));
		summonedFlightArenaBound = ordealOwner == null && tag.getBoolean("SummonedFlightArenaBound");
		summonedFlightArenaCenterX = tag.getDouble("SummonedFlightArenaCenterX");
		summonedFlightArenaFloorY = tag.getDouble("SummonedFlightArenaFloorY");
		summonedFlightArenaCenterZ = tag.getDouble("SummonedFlightArenaCenterZ");
		summonedFlightArenaDimension = tag.getString("SummonedFlightArenaDimension");
		setInvulnerable(getTransitionTick() > 0);
		restrainedVictim = tag.hasUUID("RestrainedVictim") ? tag.getUUID("RestrainedVictim") : null;
		entityData.set(DATA_RESTRAINED_VICTIM_ID, -1);
		entityData.set(DATA_CARAPACE_EXPOSED,
				getAttack() == VesperPhaseOneAttack.CARAPACE_ANEURYSM
						&& VesperMountAttackRules.isCarapaceExposed(getAttackTick()));
		VesperVulnerableRotation lock = VesperVulnerableRotation.load(
				tag, getActiveAnchor() >= 0, getYRot());
		entityData.set(DATA_VULNERABLE_YAW, lock.yaw());
		if (lock.active()) {
			setNoAi(true);
			enforceVulnerableYawLock();
		}
		refreshAnchorDimensions();
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (getTransitionTick() > 0 || getActiveAnchor() >= 0) return false;
		amount *= isCarapaceExposed() ? 1.5F : 1.0F;
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
		VesperCombatRules.AnchorHit hit = VesperCombatRules.hitAnchor(getActiveAnchorDamage(), amount);
		entityData.set(DATA_ACTIVE_ANCHOR_DAMAGE, hit.accumulatedDamage());
		entityData.set(DATA_ANCHOR_FLASH_TICKS, VesperCombatRules.ANCHOR_HIT_FLASH_TICKS);
		EndgameBossActions.hitThroneAnchor(this, anchorIndex);
		if (hit.broken()) breakAnchor(anchorIndex);
	}

    @Override
    public boolean doHurtTarget(Entity target) {
		if (getActiveAnchor() >= 0 || getTransitionTick() > 0) return false;
		if (getFlightMode() != VesperWingedFlightRules.FlightMode.GROUNDED) return false;
		if (getAttack() == VesperPhaseOneAttack.CARAPACE_ANEURYSM
				|| getAttack() == VesperPhaseOneAttack.GRAB_IMPALEMENT) return false;
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
	public float getVulnerableYaw() { return entityData.get(DATA_VULNERABLE_YAW); }
	public float getActiveAnchorDamage() { return entityData.get(DATA_ACTIVE_ANCHOR_DAMAGE); }
	public int getAnchorFlashTicks() { return entityData.get(DATA_ANCHOR_FLASH_TICKS); }
	public boolean isCarapaceExposed() { return entityData.get(DATA_CARAPACE_EXPOSED); }
	public int getCarapaceCooldown() { return entityData.get(DATA_CARAPACE_COOLDOWN); }
	public int getRestrainedVictimId() { return entityData.get(DATA_RESTRAINED_VICTIM_ID); }
	public boolean hasWingsGrown() { return entityData.get(DATA_WINGS_GROWN); }
	public VesperWingedFlightRules.FlightMode getFlightMode() {
		return clampFlightMode(entityData.get(DATA_FLIGHT_MODE));
	}
	public int getFlightTick() { return entityData.get(DATA_FLIGHT_TICK); }
	public Vec3 getLockedFlightAim() {
		return new Vec3(entityData.get(DATA_LOCKED_AIM_X), entityData.get(DATA_LOCKED_AIM_Y),
				entityData.get(DATA_LOCKED_AIM_Z));
	}

	public Optional<VesperOrdealManager.FlightArena> summonedFlightArena() {
		if (ordealOwner != null || !summonedFlightArenaBound
				|| !level().dimension().location().toString().equals(summonedFlightArenaDimension)) {
			return Optional.empty();
		}
		return Optional.of(new VesperOrdealManager.FlightArena(
				summonedFlightArenaCenterX, summonedFlightArenaFloorY, summonedFlightArenaCenterZ));
	}

	public void ensureSummonedFlightArena() {
		if (level().isClientSide() || ordealOwner != null || summonedFlightArenaBound) return;
		summonedFlightArenaCenterX = getX();
		summonedFlightArenaFloorY = Mth.floor(getY()) - 1.0D;
		summonedFlightArenaCenterZ = getZ();
		summonedFlightArenaDimension = level().dimension().location().toString();
		summonedFlightArenaBound = true;
	}

	public VesperCombatRules.AnchorCenter getInterpolatedAnchorCenter(float partialTick) {
		double entityX = Mth.lerp(partialTick, xOld, getX());
		double entityY = Mth.lerp(partialTick, yOld, getY());
		double entityZ = Mth.lerp(partialTick, zOld, getZ());
		float yaw = Mth.rotLerp(partialTick, yRotO, getYRot());
		VesperCombatRules.AnchorCenter fallback = VesperCombatRules.anchorCenter(entityX, entityY, entityZ, yaw);
		int activeAnchor = getActiveAnchor();
		if (activeAnchor < 0 || activeAnchor >= throneAnchors.length) return fallback;
		VesperThroneAnchorPart part = throneAnchors[activeAnchor];
		return new VesperCombatRules.AnchorCenter(
				Mth.lerp(partialTick, part.xOld, part.getX()),
				Mth.lerp(partialTick, part.yOld, part.getY()) + part.getBbHeight() * 0.5D,
				Mth.lerp(partialTick, part.zOld, part.getZ()));
	}

	private void tickEncounter() {
		if (getCarapaceCooldown() > 0) {
			entityData.set(DATA_CARAPACE_COOLDOWN, VesperMountAttackRules.tickCooldown(getCarapaceCooldown()));
		}
		reconcileRestrainedVictim();
		if (getAnchorFlashTicks() > 0) {
			entityData.set(DATA_ANCHOR_FLASH_TICKS, getAnchorFlashTicks() - 1);
		}
		Optional<VesperOrdealManager.FlightArena> flightArena = VesperOrdealManager.flightArena(this);
		if (getTransitionTick() > 0 && getFlightMode().airborne()) {
			beginLanding(flightArena.orElse(null));
			tickFlight(flightArena.orElse(null));
			return;
		}
		if (getTransitionTick() > 0) {
			forceGroundedFlightState();
			int transition = getTransitionTick() + 1;
			entityData.set(DATA_TRANSITION_TICK, transition);
			getNavigation().stop();
			EndgameBossActions.tickVesperTransformation(this, transition);
			if (VesperPhaseTransitionRules.isComplete(transition) && !spawnedEveningStar) {
				if (!spawnEveningStar()) return;
				spawnedEveningStar = true;
				EndgameBossActions.finishVesperCocoonReveal(this);
				EndgameBossActions.clearVesperPuppets(this);
				bossEvent.removeAllPlayers();
				discard();
			}
			return;
		}
		if (VesperWingedFlightRules.shouldDeferAnchorExposure(getFlightMode())) {
			tickFlight(flightArena.orElse(null));
			return;
		}
		int anchor = VesperCombatRules.lockedAnchorIndex(getHealth(), getMaxHealth(), getBrokenAnchorMask());
		if (anchor >= 0 && getActiveAnchor() < 0 && getFlightMode().airborne()) {
			beginLanding(flightArena.orElse(null));
			tickFlight(flightArena.orElse(null));
			return;
		}
		if (anchor >= 0 && getActiveAnchor() < 0) exposeAnchor(anchor);
		if (getActiveAnchor() >= 0) {
			forceGroundedFlightState();
			enforceVulnerableYawLock();
			setDeltaMovement(0.0D, getDeltaMovement().y, 0.0D);
			EndgameBossActions.tickExposedThroneAnchor(this, getActiveAnchor(), getActiveAnchorDamage());
			return;
		}
		if (VesperWingedFlightRules.shouldStartWingGrowth(getHealth(), getMaxHealth(), hasWingsGrown(),
				false, false)) {
			startWingGrowth();
			return;
		}
		if (getFlightMode() != VesperWingedFlightRules.FlightMode.GROUNDED) {
			tickFlight(flightArena.orElse(null));
			return;
		}
		setNoGravity(false);
		if (hasWingsGrown()) groundedFlightTicks++;
		if (getAttack() == VesperPhaseOneAttack.IDLE) {
			if (--idleTicks <= 0) {
				if (VesperWingedFlightRules.mayStartSortie(groundedFlightTicks, hasWingsGrown(), flightArena.isPresent())) {
					startTakeoff(flightArena.get());
					return;
				}
				LivingEntity candidate = getTarget();
				boolean grabEligible = isGrabEligible(candidate);
				VesperPhaseOneAttack selected = VesperMountAttackRules.selectAttack(attackStep++, lastAttack,
						getCarapaceCooldown(), grabEligible, false);
				setAttack(selected);
				if (selected == VesperPhaseOneAttack.CARAPACE_ANEURYSM) {
					entityData.set(DATA_CARAPACE_COOLDOWN, VesperMountAttackRules.startAneurysmCooldown());
				}
				if (selected == VesperPhaseOneAttack.GRAB_IMPALEMENT) grabHitMask = 0;
				entityData.set(DATA_ATTACK_TICK, 0);
			}
			return;
		}
		int attackTick = getAttackTick() + 1;
		entityData.set(DATA_ATTACK_TICK, attackTick);
		if (EndgameBossActions.tickVesperPhaseOneAttack(this, getAttack(), attackTick)) {
			lastAttack = getAttack();
			entityData.set(DATA_CARAPACE_EXPOSED, false);
			releaseRestrainedVictim(false);
			setAttack(VesperPhaseOneAttack.IDLE);
			entityData.set(DATA_ATTACK_TICK, 0);
			idleTicks = getHealth() <= getMaxHealth() * 0.42F ? 24 : 36;
		}
	}

	private void exposeAnchor(int anchor) {
		releaseRestrainedVictim(false);
		forceGroundedFlightState();
		entityData.set(DATA_CARAPACE_EXPOSED, false);
		VesperVulnerableRotation lock = VesperVulnerableRotation.capture(getYRot());
		entityData.set(DATA_VULNERABLE_YAW, lock.yaw());
		entityData.set(DATA_ACTIVE_ANCHOR, anchor);
		entityData.set(DATA_ACTIVE_ANCHOR_DAMAGE, 0.0F);
		entityData.set(DATA_ANCHOR_FLASH_TICKS, 0);
		setAttack(VesperPhaseOneAttack.IDLE);
		entityData.set(DATA_ATTACK_TICK, 0);
		setNoAi(true);
		enforceVulnerableYawLock();
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		refreshAnchorDimensions();
		playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.8F, 0.55F + anchor * 0.12F);
	}

	private void breakAnchor(int anchor) {
		int mask = getBrokenAnchorMask() | (1 << anchor);
		entityData.set(DATA_BROKEN_ANCHORS, mask);
		entityData.set(DATA_ACTIVE_ANCHOR, -1);
		clearVulnerableYawLock();
		refreshAnchorDimensions();
		entityData.set(DATA_ACTIVE_ANCHOR_DAMAGE, 0.0F);
		entityData.set(DATA_ANCHOR_FLASH_TICKS, 0);
		EndgameBossActions.breakThroneAnchor(this, anchor);
		if (mask == 7) {
			forceGroundedFlightState();
			releaseRestrainedVictim(false);
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

	private static VesperWingedFlightRules.FlightMode clampFlightMode(int ordinal) {
		VesperWingedFlightRules.FlightMode[] values = VesperWingedFlightRules.FlightMode.values();
		return values[Math.max(0, Math.min(values.length - 1, ordinal))];
	}

	private void setFlightMode(VesperWingedFlightRules.FlightMode mode) {
		entityData.set(DATA_FLIGHT_MODE, mode.ordinal());
		entityData.set(DATA_FLIGHT_TICK, 0);
	}

	private void setLockedFlightAim(Vec3 aim) {
		entityData.set(DATA_LOCKED_AIM_X, (float) aim.x);
		entityData.set(DATA_LOCKED_AIM_Y, (float) aim.y);
		entityData.set(DATA_LOCKED_AIM_Z, (float) aim.z);
	}

	private void startWingGrowth() {
		releaseRestrainedVictim(false);
		getNavigation().stop();
		setDeltaMovement(Vec3.ZERO);
		entityData.set(DATA_WINGS_GROWN, true);
		setFlightMode(VesperWingedFlightRules.FlightMode.WING_GROWTH);
		setAttack(VesperPhaseOneAttack.WING_GROWTH);
		entityData.set(DATA_ATTACK_TICK, 0);
		groundedFlightTicks = 0;
		playSound(SoundEvents.ENDER_DRAGON_GROWL, 1.6F, 0.55F);
	}

	private void startTakeoff(VesperOrdealManager.FlightArena arena) {
		getNavigation().stop();
		setNoGravity(true);
		setFlightMode(VesperWingedFlightRules.FlightMode.TAKEOFF);
		setAttack(VesperPhaseOneAttack.IDLE);
		entityData.set(DATA_ATTACK_TICK, 0);
		airborneTicks = 0;
		flightHitMask = 0;
		postAttackCircleTicks = 0;
		groundedFlightTicks = 0;
		sortieCount++;
		setLockedFlightAim(new Vec3(getX(), arena.floorY() + 1.0D, getZ()));
		playSound(SoundEvents.ENDER_DRAGON_FLAP, 1.4F, 0.8F);
	}

	private void tickFlight(VesperOrdealManager.FlightArena arena) {
		VesperWingedFlightRules.FlightMode mode = getFlightMode();
		LivingEntity target = getTarget();
		boolean targetValid = target != null && target.isAlive() && target.level() == level()
				&& (!(target instanceof Player player) || (!player.isCreative() && !player.isSpectator()));
		boolean arenaValid = arena != null;
		VesperWingedFlightRules.FlightMode recovered = VesperWingedFlightRules.recoverMode(
				mode, arenaValid, targetValid || mode == VesperWingedFlightRules.FlightMode.LANDING);
		if (recovered != mode) {
			beginLanding(arena);
			mode = VesperWingedFlightRules.FlightMode.LANDING;
		}
		int flightTick = getFlightTick() + 1;
		entityData.set(DATA_FLIGHT_TICK, flightTick);
		if (mode == VesperWingedFlightRules.FlightMode.WING_GROWTH) {
			getNavigation().stop();
			setDeltaMovement(Vec3.ZERO);
			tickWingGrowthEffects(flightTick);
			if (flightTick >= VesperWingedFlightRules.WING_GROWTH_TICKS) {
				setFlightMode(VesperWingedFlightRules.FlightMode.GROUNDED);
				setAttack(VesperPhaseOneAttack.IDLE);
				entityData.set(DATA_ATTACK_TICK, 0);
				idleTicks = 24;
			}
			return;
		}
		setNoGravity(true);
		getNavigation().stop();
		if (mode != VesperWingedFlightRules.FlightMode.TAKEOFF) airborneTicks++;
		if (VesperWingedFlightRules.mustLand(airborneTicks, arenaValid)
				&& mode != VesperWingedFlightRules.FlightMode.LANDING) {
			beginLanding(arena);
			mode = VesperWingedFlightRules.FlightMode.LANDING;
			flightTick = 1;
			entityData.set(DATA_FLIGHT_TICK, 1);
		}
		switch (mode) {
			case TAKEOFF -> tickTakeoff(arena, flightTick);
			case CIRCLING -> tickCircling(arena, target, flightTick);
			case DIVE_TELEGRAPH -> tickDiveTelegraph(arena, flightTick);
			case DIVING_REND -> tickDivingRend(arena, flightTick);
			case TAIL_FUSILLADE -> tickTailFusillade(arena, flightTick);
			case LANDING -> tickLanding(arena, flightTick);
			default -> { }
		}
	}

	private void tickTakeoff(VesperOrdealManager.FlightArena arena, int tick) {
		if (arena == null) {
			beginLanding(null);
			return;
		}
		steerToward(clampFlightPoint(getX(), arena.floorY() + 6.0D, getZ(), arena), 0.24D);
		if (tick >= VesperWingedFlightRules.TAKEOFF_TICKS) {
			setFlightMode(VesperWingedFlightRules.FlightMode.CIRCLING);
		}
	}

	private void tickCircling(VesperOrdealManager.FlightArena arena, LivingEntity target, int tick) {
		if (arena == null || target == null) {
			beginLanding(arena);
			return;
		}
		double angle = (airborneTicks * 0.11D) + sortieCount * 1.7D;
		Vec3 desired = clampFlightPoint(target.getX() + Math.cos(angle) * 8.0D,
				arena.floorY() + 7.0D, target.getZ() + Math.sin(angle) * 8.0D, arena);
		steerToward(desired, 0.32D);
		if (postAttackCircleTicks > 0) {
			if (--postAttackCircleTicks <= 0) beginLanding(arena);
			return;
		}
		if (tick < 12) return;
		Vec3 aim = clampGroundPoint(target.getX(), target.getZ(), arena);
		setLockedFlightAim(aim);
		VesperWingedFlightRules.AerialAttack attack = VesperWingedFlightRules.selectAerialAttack(
				sortieCount - 1, VesperWingedFlightRules.MAX_AIRBORNE_TICKS - airborneTicks);
		if (attack == VesperWingedFlightRules.AerialAttack.DIVING_REND) {
			setFlightMode(VesperWingedFlightRules.FlightMode.DIVE_TELEGRAPH);
			setAttack(VesperPhaseOneAttack.DIVING_REND);
		} else {
			setFlightMode(VesperWingedFlightRules.FlightMode.TAIL_FUSILLADE);
			setAttack(VesperPhaseOneAttack.TAIL_NEEDLE_FUSILLADE);
		}
		entityData.set(DATA_ATTACK_TICK, 0);
	}

	private void tickDiveTelegraph(VesperOrdealManager.FlightArena arena, int tick) {
		Vec3 aim = getLockedFlightAim();
		if (level() instanceof ServerLevel server && tick % 2 == 0) {
			VesperVisualEffects.telegraphRing(server, aim, 3.5D, VesperVisualEffects.BLOOD, 32);
			VesperVisualEffects.telegraphLine(server, position(), aim, VesperVisualEffects.BLOOD);
		}
		if (arena != null) steerToward(clampFlightPoint(getX(), arena.floorY() + 8.0D, getZ(), arena), 0.18D);
		if (tick >= VesperWingedFlightRules.DIVE_TELEGRAPH_TICKS) {
			setFlightMode(VesperWingedFlightRules.FlightMode.DIVING_REND);
		}
	}

	private void tickDivingRend(VesperOrdealManager.FlightArena arena, int tick) {
		Vec3 aim = arena == null ? getLockedFlightAim() : clampGroundPoint(
				getLockedFlightAim().x, getLockedFlightAim().z, arena);
		steerToward(aim, 1.25D);
		if ((distanceToSqr(aim) <= 12.25D || tick >= VesperWingedFlightRules.DIVE_TICKS)
				&& (flightHitMask & 1) == 0) {
			flightHitMask |= 1;
			applyDivingRendImpact(aim);
			beginLanding(arena);
		}
	}

	private void tickTailFusillade(VesperOrdealManager.FlightArena arena, int tick) {
		if (arena != null) {
			Vec3 aim = getLockedFlightAim();
			Vec3 away = position().subtract(aim).multiply(1.0D, 0.0D, 1.0D);
			if (away.lengthSqr() < 0.01D) away = new Vec3(1.0D, 0.0D, 0.0D);
			steerToward(clampFlightPoint(aim.x + away.normalize().x * 8.0D, arena.floorY() + 7.0D,
					aim.z + away.normalize().z * 8.0D, arena), 0.22D);
		}
		if (VesperWingedFlightRules.fusilladeNeedleCount(tick) == 5) fireNeedleBurst(tick);
		if (tick >= VesperWingedFlightRules.TAIL_FUSILLADE_TICKS) {
			setAttack(VesperPhaseOneAttack.IDLE);
			if (VesperWingedFlightRules.MAX_AIRBORNE_TICKS - airborneTicks
					<= VesperWingedFlightRules.CIRCLE_TICKS + VesperWingedFlightRules.LANDING_TICKS) {
				beginLanding(arena);
			} else {
				postAttackCircleTicks = VesperWingedFlightRules.CIRCLE_TICKS;
				setFlightMode(VesperWingedFlightRules.FlightMode.CIRCLING);
			}
		}
	}

	private void beginLanding(VesperOrdealManager.FlightArena arena) {
		if (getFlightMode() == VesperWingedFlightRules.FlightMode.LANDING) return;
		if (arena != null) setLockedFlightAim(clampGroundPoint(getX(), getZ(), arena));
		else setLockedFlightAim(new Vec3(getX(), Math.min(getY(), getLockedFlightAim().y), getZ()));
		setFlightMode(VesperWingedFlightRules.FlightMode.LANDING);
		setAttack(VesperPhaseOneAttack.IDLE);
		entityData.set(DATA_ATTACK_TICK, 0);
		postAttackCircleTicks = 0;
	}

	private void tickLanding(VesperOrdealManager.FlightArena arena, int tick) {
		Vec3 aim = getLockedFlightAim();
		if (arena != null) aim = clampGroundPoint(aim.x, aim.z, arena);
		Vec3 direction = aim.subtract(position());
		if (direction.lengthSqr() > 0.01D) setDeltaMovement(direction.normalize().scale(Math.min(0.38D, direction.length())));
		else setDeltaMovement(Vec3.ZERO);
		if (onGround() || tick >= VesperWingedFlightRules.LANDING_TICKS) {
			if (arena != null) setPos(aim.x, arena.floorY() + 1.0D, aim.z);
			forceGroundedFlightState();
			idleTicks = 25;
		}
	}

	private Vec3 clampFlightPoint(double x, double y, double z, VesperOrdealManager.FlightArena arena) {
		VesperWingedFlightRules.Point point = VesperWingedFlightRules.clampFlightPoint(
				x, y, z, arena.centerX(), arena.floorY(), arena.centerZ());
		return new Vec3(point.x(), point.y(), point.z());
	}

	private Vec3 clampGroundPoint(double x, double z, VesperOrdealManager.FlightArena arena) {
		VesperWingedFlightRules.Point point = VesperWingedFlightRules.clampGroundPoint(
				x, z, arena.centerX(), arena.floorY(), arena.centerZ());
		return new Vec3(point.x(), point.y(), point.z());
	}

	private void steerToward(Vec3 desired, double speed) {
		Vec3 delta = desired.subtract(position());
		if (delta.lengthSqr() < 1.0E-4D) {
			setDeltaMovement(Vec3.ZERO);
			return;
		}
		Vec3 motion = delta.normalize().scale(Math.min(speed, delta.length()));
		setDeltaMovement(motion);
		float yaw = (float) (Mth.atan2(motion.z, motion.x) * Mth.RAD_TO_DEG) - 90.0F;
		setYRot(yaw);
		setYBodyRot(yaw);
		hasImpulse = true;
	}

	private void forceGroundedFlightState() {
		setNoGravity(false);
		setFlightMode(VesperWingedFlightRules.FlightMode.GROUNDED);
		airborneTicks = 0;
		postAttackCircleTicks = 0;
		setDeltaMovement(getDeltaMovement().multiply(1.0D, 0.0D, 1.0D));
	}

	private void applyDivingRendImpact(Vec3 impact) {
		AABB area = new AABB(impact, impact).inflate(3.5D, 2.5D, 3.5D);
		for (LivingEntity living : level().getEntitiesOfClass(LivingEntity.class, area,
				entity -> entity.isAlive() && entity != this && !(entity instanceof BoundPuppeteerSummon))) {
			if (!living.hurt(damageSources().mobAttack(this), 14.0F)) continue;
			Vec3 away = living.position().subtract(impact).multiply(1.0D, 0.0D, 1.0D);
			if (away.lengthSqr() < 0.01D) away = new Vec3(1.0D, 0.0D, 0.0D);
			away = away.normalize().scale(1.45D);
			living.push(away.x, 0.42D, away.z);
		}
		if (level() instanceof ServerLevel server) {
			VesperVisualEffects.bloodCells(server, impact, VesperVisualEffects.BLOOD, 52, 3.2D, 0.4D, 3.2D, 0.12D);
			VesperVisualEffects.darkGlow(server, impact, VesperVisualEffects.BLACK, 30, 3.5D, 0.6D, 3.5D, 0.08D);
			VesperVisualEffects.lightning(server, position(), impact, false, tickCount * 197L + getId());
		}
		playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.6F, 0.65F);
	}

	private void fireNeedleBurst(int tick) {
		if (!(level() instanceof ServerLevel server)) return;
		Vec3 origin = position().add(0.0D, 0.55D, 0.0D);
		Vec3 base = getLockedFlightAim().subtract(origin).normalize();
		double sweep = (tick - 16) * 0.75D;
		for (int i = 0; i < 5; i++) {
			double angle = Math.toRadians(sweep + (i - 2) * 3.0D);
			double cos = Math.cos(angle);
			double sin = Math.sin(angle);
			Vec3 direction = new Vec3(base.x * cos - base.z * sin, base.y, base.x * sin + base.z * cos).normalize();
			BloodNeedleEntity needle = new BloodNeedleEntity(server, this);
			needle.setPos(origin.x, origin.y, origin.z);
			needle.setBaseDamage(4.0D);
			needle.shoot(direction.x, direction.y, direction.z, 1.35F, 0.0F);
			server.addFreshEntity(needle);
		}
		VesperVisualEffects.bloodCells(server, origin, VesperVisualEffects.BLOOD, 14, 0.7D, 0.5D, 0.7D, 0.08D);
		playSound(SoundEvents.CROSSBOW_SHOOT, 1.1F, 1.35F);
	}

	private void tickWingGrowthEffects(int tick) {
		if (!(level() instanceof ServerLevel server) || tick % 3 != 0) return;
		Vec3 thorax = position().add(0.0D, getBbHeight() * 0.48D, 0.0D);
		VesperVisualEffects.bloodCells(server, thorax, VesperVisualEffects.BLOOD, 10, 2.4D, 1.2D, 1.7D, 0.05D);
		VesperVisualEffects.darkGlow(server, thorax, VesperVisualEffects.BLACK, 7, 2.6D, 1.3D, 1.9D, 0.035D);
		if (tick % 12 == 0) {
			Vec3 edge = thorax.add((tick % 24 == 0 ? 1.0D : -1.0D) * 3.2D, 0.8D, 0.0D);
			VesperVisualEffects.voidTendril(server, thorax, edge, tickCount * 131L + tick);
			VesperVisualEffects.lightning(server, thorax, edge, false, tickCount * 173L + tick);
		}
	}

	void setCarapaceExposed(boolean exposed) {
		entityData.set(DATA_CARAPACE_EXPOSED, exposed);
	}

	boolean isGrabEligible(LivingEntity target) {
		if (target == null) return false;
		return VesperMountAttackRules.mayGrab(distanceToSqr(target), target.onGround(), target.isAlive(),
				target instanceof Player player && player.isCreative(),
				target instanceof Player player && player.isSpectator(),
				target instanceof BoundPuppeteerSummon, target instanceof VesperTheCrownedRefusalEntity
						|| target instanceof VesperTheEveningStarEntity);
	}

	boolean tryRestrain(LivingEntity target) {
		if (!isGrabEligible(target) || distanceToSqr(target) > 10.24D) return false;
		restrainedVictim = target.getUUID();
		entityData.set(DATA_RESTRAINED_VICTIM_ID, target.getId());
		target.setDeltaMovement(Vec3.ZERO);
		target.hurtMarked = true;
		return true;
	}

	LivingEntity getRestrainedVictim() {
		if (level().isClientSide()) {
			Entity entity = level().getEntity(getRestrainedVictimId());
			return entity instanceof LivingEntity living ? living : null;
		}
		if (restrainedVictim == null || !(level() instanceof ServerLevel server)) return null;
		Entity entity = server.getEntity(restrainedVictim);
		return entity instanceof LivingEntity living ? living : null;
	}

	void tickRestrainedVictim(int attackTick) {
		LivingEntity victim = getRestrainedVictim();
		if (victim == null) return;
		Vec3 socket = grabSocketPosition(attackTick);
		Vec3 next = victim.position().lerp(socket, attackTick < 30 ? 0.34D : 0.72D);
		victim.setPos(next.x, next.y, next.z);
		victim.setDeltaMovement(Vec3.ZERO);
		victim.fallDistance = 0.0F;
		victim.hurtMarked = true;
	}

	void applyGrabBite() {
		LivingEntity victim = getRestrainedVictim();
		if (victim == null || !VesperMountAttackRules.shouldApply(grabHitMask, VesperMountAttackRules.Hit.BITE)) return;
		grabHitMask = VesperMountAttackRules.markApplied(grabHitMask, VesperMountAttackRules.Hit.BITE);
		victim.hurt(damageSources().mobAttack(this), 6.0F);
	}

	void applyGrabImpale() {
		LivingEntity victim = getRestrainedVictim();
		if (victim == null || !VesperMountAttackRules.shouldApply(grabHitMask, VesperMountAttackRules.Hit.IMPALE)) return;
		grabHitMask = VesperMountAttackRules.markApplied(grabHitMask, VesperMountAttackRules.Hit.IMPALE);
		victim.hurt(HemoDamageTypes.vesperImpale(level(), this), 10.0F);
		victim.addEffect(new MobEffectInstance(EffectInit.blood_loss, VesperMountAttackRules.BLOOD_LOSS_TICKS, 0));
		victim.addEffect(new MobEffectInstance(MobEffects.POISON, VesperMountAttackRules.POISON_TICKS, 0));
	}

	void releaseRestrainedVictim(boolean throwVictim) {
		LivingEntity victim = getRestrainedVictim();
		if (victim != null && throwVictim
				&& VesperMountAttackRules.shouldApply(grabHitMask, VesperMountAttackRules.Hit.RELEASE)) {
			grabHitMask = VesperMountAttackRules.markApplied(grabHitMask, VesperMountAttackRules.Hit.RELEASE);
			Vec3 away = victim.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
			if (away.lengthSqr() < 1.0E-4D) away = Vec3.directionFromRotation(0.0F, getYRot());
			away = away.normalize().scale(VesperMountAttackRules.RELEASE_HORIZONTAL_SPEED);
			victim.setDeltaMovement(away.x, VesperMountAttackRules.RELEASE_UPWARD_SPEED, away.z);
			victim.hurtMarked = true;
		}
		restrainedVictim = null;
		entityData.set(DATA_RESTRAINED_VICTIM_ID, -1);
	}

	private void reconcileRestrainedVictim() {
		if (restrainedVictim == null) return;
		LivingEntity victim = getRestrainedVictim();
		boolean sameLevel = victim != null && victim.level() == level();
		double distance = victim == null ? Double.POSITIVE_INFINITY : distanceToSqr(victim);
		boolean invalidPlayer = victim instanceof Player player && (player.isCreative() || player.isSpectator());
		if (invalidPlayer || VesperMountAttackRules.shouldReleaseRestraint(isAlive(), victim != null,
				victim != null && victim.isAlive(), sameLevel, distance, getActiveAnchor() >= 0,
				getTransitionTick() > 0, getAttack() == VesperPhaseOneAttack.GRAB_IMPALEMENT)) {
			releaseRestrainedVictim(false);
		} else entityData.set(DATA_RESTRAINED_VICTIM_ID, victim.getId());
	}

	Vec3 grabSocketPosition(int attackTick) {
		Vec3 forward = Vec3.directionFromRotation(0.0F, getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
		double lift = VesperMountAttackRules.liftProgress(attackTick);
		return position().add(forward.scale(2.35D)).add(0.0D, 0.65D + lift * 2.35D, 0.0D);
	}

	private void updateAnchorPositions() {
		VesperCombatRules.AnchorCenter center = VesperCombatRules.anchorCenter(
				getX(), getY(), getZ(), getYRot());
		for (int i = 0; i < throneAnchors.length; i++) {
			VesperThroneAnchorPart part = throneAnchors[i];
			part.xo = part.getX();
			part.yo = part.getY();
			part.zo = part.getZ();
			part.xOld = part.getX();
			part.yOld = part.getY();
			part.zOld = part.getZ();
			part.setPos(center.x(), center.y() - VesperCombatRules.ANCHOR_HITBOX_HEIGHT * 0.5D, center.z());
		}
	}

	private void enforceVulnerableYawLock() {
		float yaw = getVulnerableYaw();
		setYRot(yaw);
		yRotO = yaw;
		setYBodyRot(yaw);
		yBodyRotO = yaw;
		setYHeadRot(yaw);
		yHeadRotO = yaw;
		goalSelector.disableControlFlag(Goal.Flag.LOOK);
		getLookControl().setLookAt(getX(), getEyeY(), getZ());
		getNavigation().stop();
	}

	private void clearVulnerableYawLock() {
		entityData.set(DATA_VULNERABLE_YAW, VesperVulnerableRotation.inactive().yaw());
		goalSelector.enableControlFlag(Goal.Flag.LOOK);
	}

	@Override
	public void remove(RemovalReason reason) {
		if (!level().isClientSide()) releaseRestrainedVictim(false);
		clearVulnerableYawLock();
		super.remove(reason);
	}
}
