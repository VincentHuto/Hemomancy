package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodNeedleEntity;
import com.vincenthuto.hemomancy.common.entity.summon.BoundPuppeteerSummon;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.worldgen.VesperOrdealManager;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import com.vincenthuto.hemomancy.common.entity.boss.BossBarBinding;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;
import java.util.Optional;
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
	private static final EntityDataAccessor<Float> DATA_WOUND_DRAINED = SynchedEntityData.defineId(
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
	private static final EntityDataAccessor<Integer> DATA_STAGGER_TICK = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_BURROW_TICK = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_BURROW_HUNT = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Float> DATA_BANK = SynchedEntityData.defineId(
			VesperTheCrownedRefusalEntity.class, EntityDataSerializers.FLOAT);
	private static final EntityDataAccessor<Float> DATA_PITCH = SynchedEntityData.defineId(
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
	private UUID bloomId;
	public final AnimationState transformationAnimationState = new AnimationState();
	private final VesperMountController controller = new VesperMountController(this);
	private final java.util.List<Vec3> plannedPoints = new java.util.ArrayList<>();
	private final java.util.List<String> plannedShapes = new java.util.ArrayList<>();
	private VesperPuppetRoster roster = new VesperPuppetRoster();
	private double circleAngle;
	@Nullable private VesperWoundRules.Wound wound;
	@Nullable private UUID lastDrainer;
	private final VesperWound.GuardState guardState = new VesperWound.GuardState();
	@Nullable private VesperBurrowRules.State burrowState;
	private double burrowFloorY;
	private int burrowCooldown;
	private int diveCount;
	private Vec3 passDirection = new Vec3(1.0D, 0.0D, 0.0D);
	private Vec3 diveVelocity = new Vec3(0.0D, -1.25D, 0.0D);

    private final BossBarBinding bossBar = new BossBarBinding("entity.hemomancy.vesper_crowned_refusal", BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_12);

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
		this.moveControl = new VesperMountMoveControl(this);
    }

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_ATTACK, VesperPhaseOneAttack.IDLE.ordinal());
		builder.define(DATA_ATTACK_TICK, 0);
		builder.define(DATA_BROKEN_ANCHORS, 0);
		builder.define(DATA_ACTIVE_ANCHOR, -1);
		builder.define(DATA_TRANSITION_TICK, 0);
		builder.define(DATA_WOUND_DRAINED, 0.0F);
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
		builder.define(DATA_STAGGER_TICK, 0);
		builder.define(DATA_BURROW_TICK, -1);
		builder.define(DATA_BURROW_HUNT, 0);
		builder.define(DATA_BANK, 0.0F);
		builder.define(DATA_PITCH, 0.0F);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (DATA_ACTIVE_ANCHOR.equals(key)) refreshAnchorDimensions();
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
                .add(Attributes.ARMOR, 16.0D)
                .add(Attributes.STEP_HEIGHT, 1.5D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        // Movement, facing and attacks belong to VesperMountController; no vanilla goal competes for the body.
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
		updateAnchorPositions();
        if (!this.level().isClientSide) {
            this.bossBar.trackHealth(this);
            if (this.isAlive()) {
				tickEncounter();
            }
        }
    }

    /**
     * The mount turns as one rigid body on the controller's yaw. Vanilla body rotation would let the client body lag
     * the head by up to 75 degrees while standing and then lurch round, and swing toward a sidestep's direction.
     */
    @Override
    protected float tickHeadTurn(float yRot, float animStep) {
        this.yBodyRot = this.getYRot();
        return animStep;
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
        this.bossBar.show(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossBar.hide(player);
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
		setOrdeal(owner, bloomOrigin, null);
	}

	public void setOrdeal(UUID owner, long bloomOrigin, UUID bloomId) {
		this.ordealOwner = owner;
		this.bloomOrigin = bloomOrigin;
		this.bloomId = bloomId;
		this.summonedFlightArenaBound = false;
	}

	public UUID getOrdealOwner() { return ordealOwner; }
	public long getBloomOrigin() { return bloomOrigin; }
	public UUID getBloomId() { return bloomId; }

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		if (ordealOwner != null) tag.putUUID("OrdealOwner", ordealOwner);
		tag.putLong("BloomOrigin", bloomOrigin);
		if (bloomId != null) tag.putUUID("BloomId", bloomId);
		tag.putInt("VesperAttack", getAttack().ordinal());
		tag.putInt("VesperAttackTick", getAttackTick());
		tag.putInt("BrokenThroneAnchors", getBrokenAnchorMask());
		VesperWoundRules.save(wound, tag);
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
		if (burrowState != null) burrowState.save(tag);
		tag.putDouble("BurrowFloorY", burrowFloorY);
		tag.put("VesperRoster", roster.save());
		tag.putInt("BurrowCooldown", burrowCooldown);
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
		bloomId = tag.hasUUID("BloomId") ? tag.getUUID("BloomId") : null;
		setAttack(VesperPhaseOneAttack.values()[Math.max(0, Math.min(VesperPhaseOneAttack.values().length - 1,
				tag.getInt("VesperAttack")))]);
		entityData.set(DATA_ATTACK_TICK, tag.getInt("VesperAttackTick"));
		entityData.set(DATA_BROKEN_ANCHORS, tag.getInt("BrokenThroneAnchors"));
		// Reads the new wound or migrates an old save's exposed throne anchor into an opened wound.
		wound = VesperWoundRules.load(tag);
		entityData.set(DATA_ACTIVE_ANCHOR, wound == null ? -1 : wound.site().ordinal());
		syncWound();
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
		burrowFloorY = tag.contains("BurrowFloorY") ? tag.getDouble("BurrowFloorY") : getY();
		roster = tag.contains("VesperRoster") ? VesperPuppetRoster.load(tag.getCompound("VesperRoster"))
				: new VesperPuppetRoster();
		burrowCooldown = Math.max(0, tag.getInt("BurrowCooldown"));
		VesperBurrowRules.State savedBurrow = VesperBurrowRules.State.load(tag);
		if (savedBurrow != null) {
			// Never resume an unseen hunt after a reload: surface where it was and gather itself.
			VesperBurrowRules.State resumed = VesperBurrowRules.afterReload(savedBurrow);
			setBurrowState(resumed);
			setAttack(VesperPhaseOneAttack.BURROW);
			noPhysics = false;
			setNoGravity(false);
			setInvisible(false);
			setPos(resumed.x(), burrowFloorY, resumed.z());
		} else if (getAttack() == VesperPhaseOneAttack.BURROW) {
			setAttack(VesperPhaseOneAttack.IDLE);
		}
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
		// Older saves persisted NoAI during a vulnerable window; the controller never needs it.
		setNoAi(false);
		refreshAnchorDimensions();
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (getTransitionTick() > 0 || getActiveAnchor() >= 0 || isSubmerged()) return false;
		amount *= isCarapaceExposed() ? 1.5F : 1.0F;
		float floor = VesperCombatRules.healthFloor(getMaxHealth(), getBrokenAnchorMask());
		float allowed = Math.max(0.0F, getHealth() - floor);
		return allowed > 0.0F && super.hurt(source, Math.min(amount, allowed));
	}

	/** A hit on the open wound keeps it from scabbing and drains it only at the fallback rate. */
	boolean hurtAnchor(int anchorIndex, DamageSource source, float amount) {
		if (level().isClientSide || wound == null || anchorIndex != getActiveAnchor() || amount <= 0.0F) return false;
		updateWound(VesperWoundRules.hit(wound, amount));
		EndgameBossActions.hitThroneAnchor(this, anchorIndex);
		return true;
	}

	/** A puppet of his killed while a wound is open tears it further. */
	void applyPuppetBacklash() {
		if (level().isClientSide || wound == null) return;
		int anchor = getActiveAnchor();
		updateWound(VesperWoundRules.tear(wound));
		EndgameBossActions.hitThroneAnchor(this, anchor);
	}

	public boolean canDrainWound(LivingEntity user) {
		if (level().isClientSide) return getActiveAnchor() >= 0;
		return user instanceof ServerPlayer player && VesperAbsorptionEligibilityRules.canDrainWound(
				wound != null && getTransitionTick() <= 0, ordealOwner, player.getUUID());
	}

	/** Blood Absorption held on the open wound: pulls his blood out, weakening him and feeding the drainer. */
	public float drainWound(ServerPlayer player, float progress) {
		if (!canDrainWound(player) || progress <= 0.0F) return 0.0F;
		lastDrainer = player.getUUID();
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		if (volume.isActive()) {
			volume.fill(progress * 2.0D);
			BloodVolumeEvents.syncVolume(player, volume);
		}
		VesperWound.drainStream(this, player);
		updateWound(VesperWoundRules.drain(wound, progress));
		return progress;
	}

	private void updateWound(VesperWoundRules.Wound next) {
		int anchor = getActiveAnchor();
		boolean surged = next.drained() > (wound == null ? 0.0F : wound.drained());
		wound = next;
		if (surged) entityData.set(DATA_ANCHOR_FLASH_TICKS, VesperCombatRules.ANCHOR_HIT_FLASH_TICKS);
		syncWound();
		if (VesperWoundRules.broken(next)) breakAnchor(anchor);
	}

	private void syncWound() {
		entityData.set(DATA_WOUND_DRAINED, wound == null ? 0.0F : wound.drained());
	}

	/** The open wound's centre in world space, at its socket on the mount. */
	public Vec3 woundCenter() {
		return woundCenter(getActiveAnchor());
	}

	/** A given wound site's centre in world space; a rupture uses this after the active anchor is cleared. */
	public Vec3 woundCenter(int anchor) {
		anchor = Mth.clamp(anchor, 0, 2);
		double[] offset = VesperWoundRules.worldOffset(
				VesperWoundRules.socket(VesperWoundRules.Site.fromIndex(anchor)), getYRot());
		return position().add(offset[0], offset[1], offset[2]);
	}

	VesperWound.GuardState guardState() {
		return guardState;
	}

	@Nullable
	UUID lastDrainer() {
		return lastDrainer;
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
	/** Drain progress of the open wound, 0..{@link VesperWoundRules#DRAIN_REQUIRED}. */
	public float getWoundDrained() { return entityData.get(DATA_WOUND_DRAINED); }
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

	private void tickEncounter() {
		if (burrowCooldown > 0) burrowCooldown--;
		if (tickCount % 40 == 0) reconcileRoster();
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
				bossBar.hideAll();
				discard();
			}
			return;
		}
		if (VesperWingedFlightRules.shouldDeferAnchorExposure(getFlightMode())) {
			tickFlight(flightArena.orElse(null));
			return;
		}
		if (burrowState != null) {
			VesperBurrow.tick(this, controller.target(), flightArena.orElse(null));
			return;
		}
		int anchor = VesperCombatRules.lockedAnchorIndex(getHealth(), getMaxHealth(), getBrokenAnchorMask());
		if (anchor >= 0 && getActiveAnchor() < 0 && getFlightMode().airborne()) {
			beginLanding(flightArena.orElse(null));
			tickFlight(flightArena.orElse(null));
			return;
		}
		if (anchor >= 0 && getActiveAnchor() < 0) controller.request(VesperMountControllerRules.Priority.WOUND);
		if (VesperWingedFlightRules.shouldStartWingGrowth(getHealth(), getMaxHealth(), hasWingsGrown(),
				false, false)) {
			controller.request(VesperMountControllerRules.Priority.WING_GROWTH);
		}
		VesperMountControllerRules.Priority granted = controller.takeGranted();
		if (granted == VesperMountControllerRules.Priority.WOUND && anchor >= 0 && getActiveAnchor() < 0) {
			exposeAnchor(anchor);
		} else if (granted == VesperMountControllerRules.Priority.WING_GROWTH && !hasWingsGrown()
				&& getActiveAnchor() < 0) {
			startWingGrowth();
			return;
		}
		if (getActiveAnchor() >= 0 && wound != null) {
			if (getFlightMode() != VesperWingedFlightRules.FlightMode.GROUNDED) forceGroundedFlightState();
			updateWound(VesperWoundRules.tick(wound));
			if (getActiveAnchor() < 0) return;
			EndgameBossActions.tickExposedThroneAnchor(this, getActiveAnchor(), getWoundDrained());
			VesperWound.tickGuard(this, controller, controller.target());
			return;
		}
		if (getFlightMode() != VesperWingedFlightRules.FlightMode.GROUNDED) {
			tickFlight(flightArena.orElse(null));
			return;
		}
		setNoGravity(false);
		if (hasWingsGrown()) groundedFlightTicks++;
		LivingEntity target = controller.target();
		if (controller.tickGround(target, flightArena.orElse(null))) return;
		if (--idleTicks > 0) return;
		if (VesperWingedFlightRules.mayStartSortie(groundedFlightTicks, hasWingsGrown(), flightArena.isPresent())) {
			startTakeoff(flightArena.get());
			return;
		}
		if (burrowCooldown <= 0 && target != null && lastAttack != VesperPhaseOneAttack.BURROW
				&& (hasWingsGrown() || getHealth() <= getMaxHealth() * 0.7F) && getRandom().nextFloat() < 0.35F) {
			startBurrow(false);
			return;
		}
		VesperPhaseOneAttack selected = VesperMountAttackRules.selectAttack(attackStep++, lastAttack,
				getCarapaceCooldown(), isGrabEligible(target), false);
		if (selected == VesperPhaseOneAttack.IDLE) return;
		// A muster with every slot filled (or nothing left to call) recalls tired puppets instead, if any.
		boolean musterBlocked = roster.nextMuster(getRandom()).isEmpty();
		if (selected == VesperPhaseOneAttack.PUPPET_MUSTER && musterBlocked) {
			selected = VesperMountMoves.hasRecallablePuppet(this) ? VesperPhaseOneAttack.RECALL
					: VesperPhaseOneAttack.ROYAL_SCUTTLE;
		} else if (selected != VesperPhaseOneAttack.CARAPACE_ANEURYSM && getRandom().nextFloat() < 0.15F
				&& VesperMountMoves.hasRecallablePuppet(this)) {
			selected = VesperPhaseOneAttack.RECALL;
		}
		if (selected == VesperPhaseOneAttack.CARAPACE_ANEURYSM) {
			entityData.set(DATA_CARAPACE_COOLDOWN, VesperMountAttackRules.startAneurysmCooldown());
		}
		if (selected == VesperPhaseOneAttack.GRAB_IMPALEMENT) grabHitMask = 0;
		controller.begin(selected, target);
	}

	void beginGroundAttack(VesperPhaseOneAttack attack) {
		setAttack(attack);
		entityData.set(DATA_ATTACK_TICK, 0);
	}

	void setAttackTick(int tick) {
		entityData.set(DATA_ATTACK_TICK, tick);
	}

	/** Ends the current grounded move, whether it completed or was staggered out of its wind-up. */
	void endGroundAttack() {
		plannedPoints.clear();
		plannedShapes.clear();
		lastAttack = getAttack();
		entityData.set(DATA_CARAPACE_EXPOSED, false);
		releaseRestrainedVictim(false);
		setAttack(VesperPhaseOneAttack.IDLE);
		entityData.set(DATA_ATTACK_TICK, 0);
		idleTicks = getHealth() <= getMaxHealth() * 0.42F ? 24 : 36;
	}

	/** Telegraphed spawn points for the current move (spike volleys, puppet landings); never persisted. */
	java.util.List<Vec3> plannedPoints() {
		return plannedPoints;
	}

	java.util.List<String> plannedShapes() {
		return plannedShapes;
	}

	public VesperPuppetRoster roster() {
		return roster;
	}

	/** A shape marked out whose body is gone without being killed (unloaded, cleared) goes back to the roster. */
	private void reconcileRoster() {
		java.util.Set<String> live = new java.util.HashSet<>();
		for (Mob puppet : VesperMountMoves.ownedPuppets(this)) {
			if (puppet instanceof BoundPuppeteerSummon bound) live.add(bound.hemomancy$getSummonName());
		}
		for (String shape : VesperPuppetRoster.SHAPES) {
			if (roster.slot(shape) == VesperPuppetRoster.Slot.OUT && !live.contains(shape)) roster.recall(shape);
		}
	}

	/** A puppet the player killed is struck from the roster for the rest of the fight. */
	void strikePuppet(String shape) {
		roster.strike(shape);
	}

	public int getStaggerTick() { return entityData.get(DATA_STAGGER_TICK); }

	/** True from the dive until the eruption; the rider tucks and grips the nape throughout. */
	public boolean isBurrowing() {
		VesperBurrowRules.Stage stage = getBurrowStage();
		return stage != null && stage != VesperBurrowRules.Stage.RECOVERY && stage != VesperBurrowRules.Stage.COMPLETE;
	}

	/** Underground and out of reach: invisible, unhittable, not collidable. */
	public boolean isSubmerged() {
		return isBurrowing() && isInvisible();
	}

	@Nullable
	public VesperBurrowRules.Stage getBurrowStage() {
		int tick = entityData.get(DATA_BURROW_TICK);
		if (tick < 0) return null;
		return VesperBurrowRules.stageAt(new VesperBurrowRules.Plan(entityData.get(DATA_BURROW_HUNT), false), tick);
	}

	public int getBurrowHuntTicks() {
		return entityData.get(DATA_BURROW_HUNT);
	}

	public int getBurrowTick() {
		return entityData.get(DATA_BURROW_TICK);
	}

	@Nullable
	VesperBurrowRules.State getBurrowState() {
		return burrowState;
	}

	void setBurrowState(@Nullable VesperBurrowRules.State state) {
		burrowState = state;
		entityData.set(DATA_BURROW_TICK, state == null ? -1 : state.tick());
		entityData.set(DATA_BURROW_HUNT, state == null ? 0 : state.plan().huntTicks());
	}

	double getBurrowFloorY() {
		return burrowFloorY;
	}

	void faceFlat(Vec3 point) {
		controller.face(point, VesperMountMoveControl.GROUND_TURN_DEGREES);
	}

	private void startBurrow(boolean fromDive) {
		releaseRestrainedVictim(false);
		burrowFloorY = getY();
		setAttack(VesperPhaseOneAttack.BURROW);
		entityData.set(DATA_ATTACK_TICK, 0);
		VesperBurrow.begin(this, fromDive);
	}

	void endBurrow() {
		lastAttack = VesperPhaseOneAttack.BURROW;
		setAttack(VesperPhaseOneAttack.IDLE);
		entityData.set(DATA_ATTACK_TICK, 0);
		burrowCooldown = 400;
		idleTicks = 20;
	}

	@Override
	public boolean isPickable() {
		return super.isPickable() && !isSubmerged();
	}

	@Override
	public boolean isPushable() {
		return super.isPushable() && !isSubmerged();
	}

	/** Flight roll in degrees, positive banking right; zero on the ground. */
	public float getFlightBank() { return entityData.get(DATA_BANK); }

	/** Flight pitch in degrees, negative nose-down. */
	public float getFlightPitch() { return entityData.get(DATA_PITCH); }

	void setFlightAttitude(float bank, float pitch) {
		entityData.set(DATA_BANK, bank);
		entityData.set(DATA_PITCH, pitch);
	}

	void setStaggerTick(int ticks) {
		entityData.set(DATA_STAGGER_TICK, Math.max(0, ticks));
	}

	/** The threshold splits a wound open: the mount staggers and shrieks, and he is drainable there. */
	private void exposeAnchor(int anchor) {
		releaseRestrainedVictim(false);
		forceGroundedFlightState();
		entityData.set(DATA_CARAPACE_EXPOSED, false);
		wound = VesperWoundRules.open(VesperWoundRules.Site.fromIndex(anchor));
		entityData.set(DATA_ACTIVE_ANCHOR, anchor);
		syncWound();
		entityData.set(DATA_ANCHOR_FLASH_TICKS, 0);
		setAttack(VesperPhaseOneAttack.IDLE);
		entityData.set(DATA_ATTACK_TICK, 0);
		setStaggerTick(VesperMountControllerRules.STAGGER_TICKS * 2);
		VesperWound.resetGuard(this);
		refreshAnchorDimensions();
		updateAnchorPositions();
		EndgameBossActions.openThroneWound(this, anchor);
		playSound(SoundInit.ENTITY_VESPER_HURT.get(), 2.0F, 0.6F + anchor * 0.1F);
		playSound(SoundEvents.IRON_GOLEM_DAMAGE, 1.8F, 0.55F + anchor * 0.12F);
	}

	private void breakAnchor(int anchor) {
		int mask = getBrokenAnchorMask() | (1 << anchor);
		entityData.set(DATA_BROKEN_ANCHORS, mask);
		entityData.set(DATA_ACTIVE_ANCHOR, -1);
		wound = null;
		syncWound();
		refreshAnchorDimensions();
		entityData.set(DATA_ANCHOR_FLASH_TICKS, 0);
		setStaggerTick(VesperMountControllerRules.STAGGER_TICKS);
		EndgameBossActions.breakThroneAnchor(this, anchor);
		if (mask == 7) {
			forceGroundedFlightState();
			releaseRestrainedVictim(false);
			entityData.set(DATA_TRANSITION_TICK, 1);
			setInvulnerable(true);
			setAttack(VesperPhaseOneAttack.IDLE);
		}
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
		fly(clampFlightPoint(getX(), arena.floorY() + 6.0D, getZ(), arena), 0.3D, 0.045D, 8.0F);
		if (tick >= VesperWingedFlightRules.TAKEOFF_TICKS) {
			Vec3 offset = position().subtract(arena.centerX(), 0.0D, arena.centerZ());
			circleAngle = Math.atan2(offset.z, offset.x);
			setFlightMode(VesperWingedFlightRules.FlightMode.CIRCLING);
		}
	}

	private void tickCircling(VesperOrdealManager.FlightArena arena, LivingEntity target, int tick) {
		if (arena == null || target == null) {
			beginLanding(arena);
			return;
		}
		// The orbit point leads the mount around the target; steering turns it into a banked arc.
		circleAngle += 0.055D;
		Vec3 desired = VesperFlightSteeringRules.circlePoint(target.position(), 9.0D, circleAngle + 0.6D,
				arena.floorY() + 7.0D);
		fly(clampFlightPoint(desired.x, desired.y, desired.z, arena), 0.55D, 0.05D, 6.0F);
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
			Vec3 across = aim.subtract(position()).multiply(1.0D, 0.0D, 1.0D);
			if (across.lengthSqr() < 0.01D) across = new Vec3(1.0D, 0.0D, 0.0D);
			across = across.normalize();
			passDirection = new Vec3(-across.z, 0.0D, across.x).scale((sortieCount & 1) == 0 ? 1.0D : -1.0D);
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
		// Wings cup and brake: it bleeds speed, climbs a little and swings its head onto the mark.
		if (arena != null) fly(clampFlightPoint(getX(), arena.floorY() + 8.5D, getZ(), arena), 0.12D, 0.07D, 0.0F);
		controller.face(aim, 12.0F);
		setFlightAttitude(0.0F, -12.0F * Math.min(1.0F, tick / 10.0F));
		if (tick >= VesperWingedFlightRules.DIVE_TELEGRAPH_TICKS) {
			Vec3 line = aim.subtract(position());
			diveVelocity = line.lengthSqr() < 0.01D ? new Vec3(0.0D, -1.25D, 0.0D) : line.normalize().scale(1.25D);
			setFlightMode(VesperWingedFlightRules.FlightMode.DIVING_REND);
		}
	}

	private void tickDivingRend(VesperOrdealManager.FlightArena arena, int tick) {
		Vec3 aim = arena == null ? getLockedFlightAim() : clampGroundPoint(
				getLockedFlightAim().x, getLockedFlightAim().z, arena);
		// Committed: no steering once the dive starts, so a sidestep at the last moment dodges it.
		setDeltaMovement(diveVelocity);
		hasImpulse = true;
		float pitch = (float) (Mth.atan2(diveVelocity.y, diveVelocity.horizontalDistance()) * Mth.RAD_TO_DEG);
		setFlightAttitude(0.0F, Math.max(-VesperFlightSteeringRules.PITCH_MAX, pitch));
		if ((distanceToSqr(aim) <= 12.25D || onGround() || tick >= VesperWingedFlightRules.DIVE_TICKS)
				&& (flightHitMask & 1) == 0) {
			flightHitMask |= 1;
			applyDivingRendImpact(new Vec3(getX(), aim.y, getZ()));
			// Every second dive drives on into the floor and becomes a short burrow.
			if (arena != null && (diveCount++ & 1) == 1) {
				setPos(getX(), arena.floorY() + 1.0D, getZ());
				forceGroundedFlightState();
				startBurrow(true);
			} else {
				beginLanding(arena);
			}
		}
	}

	private void tickTailFusillade(VesperOrdealManager.FlightArena arena, int tick) {
		if (arena != null) {
			// A strafing run across the mark, firing as it passes over.
			Vec3 aim = getLockedFlightAim();
			Vec3 desired = aim.add(passDirection.scale(14.0D));
			fly(clampFlightPoint(desired.x, arena.floorY() + 7.0D, desired.z, arena), 0.6D, 0.06D, 7.0F);
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
		// Wings flare and brake into a short skid instead of a straight drop onto the mark.
		fly(aim, 0.38D, 0.05D, 9.0F);
		if (onGround() || tick >= VesperWingedFlightRules.LANDING_TICKS) {
			if (arena != null && !onGround()) setPos(getX(), arena.floorY() + 1.0D, getZ());
			Vec3 skid = getDeltaMovement().multiply(0.8D, 0.0D, 0.8D);
			forceGroundedFlightState();
			setDeltaMovement(skid);
			setStaggerTick(VesperMountControllerRules.STAGGER_TICKS);
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

	/** One steered flight tick: capped turn, eased acceleration, and the bank and pitch the model leans into. */
	private void fly(Vec3 desired, double maxSpeed, double accel, float maxTurn) {
		VesperFlightSteeringRules.Steer steer = VesperFlightSteeringRules.step(position(), getDeltaMovement(),
				getYRot(), desired, maxSpeed, accel, Math.max(0.001F, maxTurn));
		setDeltaMovement(steer.velocity());
		if (maxTurn > 0.0F) {
			setYRot(steer.yaw());
			setYBodyRot(steer.yaw());
			setYHeadRot(steer.yaw());
		}
		setFlightAttitude(steer.bank(), steer.pitch());
		hasImpulse = true;
	}

	private void forceGroundedFlightState() {
		setNoGravity(false);
		setFlightMode(VesperWingedFlightRules.FlightMode.GROUNDED);
		airborneTicks = 0;
		postAttackCircleTicks = 0;
		setFlightAttitude(0.0F, 0.0F);
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
		for (int i = 0; i < throneAnchors.length; i++) {
			double[] offset = VesperWoundRules.worldOffset(
					VesperWoundRules.socket(VesperWoundRules.Site.fromIndex(i)), getYRot());
			VesperThroneAnchorPart part = throneAnchors[i];
			part.xo = part.getX();
			part.yo = part.getY();
			part.zo = part.getZ();
			part.xOld = part.getX();
			part.yOld = part.getY();
			part.zOld = part.getZ();
			part.setPos(getX() + offset[0], getY() + offset[1] - VesperCombatRules.ANCHOR_HITBOX_HEIGHT * 0.5D,
					getZ() + offset[2]);
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		if (!level().isClientSide()) releaseRestrainedVictim(false);
		super.remove(reason);
	}
}
