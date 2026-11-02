package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

/**
 * Lies on the ground as a corpse until something comes close, swells, then bursts: the growth climbs out and walks off
 * with the body hanging under it, hunting by lunging. Swelling can still back off; the eruption is one way. The synced
 * game tick it erupted on is the whole state, so a client that starts tracking late, or a reloaded save, lands on the
 * right frame. A lunge is synced the same way, by the tick its wind-up began.
 */
public class ErythromyceliumEruptusEntity extends Monster {
	private static final EntityDataAccessor<Integer> DATA_SWELL_DIR = SynchedEntityData
			.defineId(ErythromyceliumEruptusEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Long> DATA_ERUPTED_AT = SynchedEntityData
			.defineId(ErythromyceliumEruptusEntity.class, EntityDataSerializers.LONG);
	private static final EntityDataAccessor<Long> DATA_LUNGE_AT = SynchedEntityData
			.defineId(ErythromyceliumEruptusEntity.class, EntityDataSerializers.LONG);
	private static final long DORMANT = -1L;
	private static final long NO_LUNGE = -1L;
	private static final EntityDimensions CORPSE = EntityDimensions.scalable(1.5F, 0.5F);
	private static final int FUSE_TICKS = 30;
	public static final int ERUPT_TICKS = 30;
	/** How much of the eruption the corpse takes to let its swelling go. */
	private static final float DEFLATE_SPAN = 0.2F;

	/** The crouch that warns of a lunge, then the usual time in the air, then how long it stands open after. */
	public static final int LUNGE_WINDUP_TICKS = 14;
	public static final int LUNGE_AIR_TICKS = 13;
	public static final int LUNGE_RECOVER_TICKS = 20;
	private static final int LUNGE_COOLDOWN_TICKS = 40;
	private static final double LUNGE_RANGE = 8.0D;
	private static final double LUNGE_LIFT = 0.5D;
	private static final double LUNGE_MAX_SPEED = 1.1D;
	/** Blocks covered per unit of launch speed over a leap, with air drag: the sum of 0.91^t across its air time. */
	private static final double LUNGE_REACH_PER_SPEED = 7.5D;
	private static final double LUNGE_HIT_MARGIN = 0.6D;

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30.0D).add(Attributes.MOVEMENT_SPEED, 0.28D)
				.add(Attributes.ATTACK_DAMAGE, 6.0D).add(Attributes.ARMOR, 2.0D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.5D).add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	private int oldSwell;
	private int swell;

	public ErythromyceliumEruptusEntity(EntityType<? extends ErythromyceliumEruptusEntity> type, Level worldIn) {
		super(type, worldIn);
		// The base constructor sized it from the registered height, before it could ask whether it had erupted.
		refreshDimensions();
	}

	public boolean isErupted() {
		return this.entityData.get(DATA_ERUPTED_AT) != DORMANT;
	}

	/** How far through the eruption it is: 0 as a corpse, 1 once it is standing. */
	public float eruption(float partialTick) {
		long at = this.entityData.get(DATA_ERUPTED_AT);
		if (at == DORMANT) return 0.0F;
		return HemoEasing.clamp01((this.level().getGameTime() - at + partialTick) / ERUPT_TICKS);
	}

	/** Ticks since its current lunge's wind-up began, or -1 if it has not lunged or the lunge is long over. */
	public float lungeAge(float partialTick) {
		long at = this.entityData.get(DATA_LUNGE_AT);
		if (at == NO_LUNGE) return -1.0F;
		float age = this.level().getGameTime() - at + partialTick;
		return age > LUNGE_WINDUP_TICKS + LUNGE_AIR_TICKS + LUNGE_RECOVER_TICKS ? -1.0F : age;
	}

	public int getSwellDir() {
		return this.entityData.get(DATA_SWELL_DIR);
	}

	/** -1 lets the swelling go down, 1 builds it toward the eruption. */
	public void setSwellDir(int direction) {
		this.entityData.set(DATA_SWELL_DIR, direction);
	}

	/** 0 at rest, 1 about to burst, draining away as the eruption starts. */
	public float getSwelling(float partialTick) {
		if (isErupted()) return Math.max(0.0F, 1.0F - eruption(partialTick) / DEFLATE_SPAN);
		return Math.min(1.0F, Mth.lerp(partialTick, (float) this.oldSwell, (float) this.swell) / (FUSE_TICKS - 2));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_SWELL_DIR, -1);
		builder.define(DATA_ERUPTED_AT, DORMANT);
		builder.define(DATA_LUNGE_AT, NO_LUNGE);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		if (DATA_ERUPTED_AT.equals(key)) refreshDimensions();
	}

	/** Flat as a corpse; only the standing growth takes up the full registered height. */
	@Override
	public EntityDimensions getDefaultDimensions(Pose pose) {
		return isErupted() ? super.getDefaultDimensions(pose) : CORPSE;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("Erupted", isErupted());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		// Game tick zero is long past, so a reloaded growth is already standing.
		if (tag.getBoolean("Erupted")) this.entityData.set(DATA_ERUPTED_AT, 0L);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_ERYTHROMYCELIUM_ERUPTUS_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_ERYTHROMYCELIUM_ERUPTUS_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundInit.ENTITY_ERYTHROMYCELIUM_ERUPTUS_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.3f;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(2, new AmbushEruptGoal(this));
		this.goalSelector.addGoal(3, new LungeGoal(this));
		this.goalSelector.addGoal(5, new EruptedStrollGoal(this, 0.8D));
		this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
	}

	@Override
	public void tick() {
		super.tick();
		if (!isAlive() || isErupted()) return;
		this.oldSwell = this.swell;
		int direction = getSwellDir();
		if (direction > 0 && this.swell == 0) {
			playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
			gameEvent(GameEvent.PRIME_FUSE);
		}
		this.swell = Math.clamp(this.swell + direction, 0, FUSE_TICKS);
		if (this.swell == FUSE_TICKS && !level().isClientSide) erupt();
	}

	private void erupt() {
		this.entityData.set(DATA_ERUPTED_AT, level().getGameTime());
		spores(30, 0.5D, 0.04D);
	}

	private void spores(int count, double spread, double speed) {
		((ServerLevel) level()).sendParticles(HemoParticleData.sporiticSpore(new ParticleColor(214, 150, 40)),
				getX(), getY() + 0.3D, getZ(), count, spread, 0.2D, spread, speed);
	}

	/** Walks the growth at its target, then crouches, leaps along the line it last saw, and stands open where it lands. */
	public static class LungeGoal extends Goal {
		private enum Phase { CHASE, WINDUP, AIRBORNE, RECOVER }

		private final ErythromyceliumEruptusEntity eruptus;
		private Phase phase = Phase.CHASE;
		private int phaseTicks;
		private int cooldown;
		private boolean hit;

		public LungeGoal(ErythromyceliumEruptusEntity eruptus) {
			this.eruptus = eruptus;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			LivingEntity target = this.eruptus.getTarget();
			return this.eruptus.eruption(0.0F) >= 1.0F && target != null && target.isAlive();
		}

		/** A leap in the air always finishes, whatever became of its target. */
		@Override
		public boolean canContinueToUse() {
			return this.phase == Phase.AIRBORNE || this.phase == Phase.RECOVER || canUse();
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void stop() {
			if (this.phase == Phase.WINDUP) this.eruptus.entityData.set(DATA_LUNGE_AT, NO_LUNGE);
			enter(Phase.CHASE);
			this.eruptus.getNavigation().stop();
		}

		@Override
		public void tick() {
			if (this.cooldown > 0) this.cooldown--;
			this.phaseTicks++;
			LivingEntity target = this.eruptus.getTarget();
			switch (this.phase) {
				case CHASE -> chase(target);
				case WINDUP -> {
					if (target != null) face(target);
					if (this.phaseTicks >= LUNGE_WINDUP_TICKS) leap(target);
				}
				case AIRBORNE -> {
					strike(target);
					if (this.phaseTicks > 2 && this.eruptus.onGround() || this.phaseTicks >= LUNGE_AIR_TICKS * 2) land();
				}
				case RECOVER -> {
					if (this.phaseTicks >= LUNGE_RECOVER_TICKS) enter(Phase.CHASE);
				}
			}
		}

		private void enter(Phase next) {
			this.phase = next;
			this.phaseTicks = 0;
		}

		private void chase(@Nullable LivingEntity target) {
			if (target == null) return;
			this.eruptus.getLookControl().setLookAt(target, 30.0F, 30.0F);
			if (this.cooldown == 0 && this.eruptus.onGround() && this.eruptus.distanceToSqr(target) <= LUNGE_RANGE * LUNGE_RANGE
					&& this.eruptus.getSensing().hasLineOfSight(target)) {
				this.eruptus.getNavigation().stop();
				this.eruptus.entityData.set(DATA_LUNGE_AT, this.eruptus.level().getGameTime());
				this.eruptus.playSound(SoundInit.ENTITY_ERYTHROMYCELIUM_ERUPTUS_AMBIENT.get(), 1.0F, 0.6F);
				enter(Phase.WINDUP);
			} else if (this.phaseTicks % 10 == 1) {
				this.eruptus.getNavigation().moveTo(target, 1.1D);
			}
		}

		private void face(LivingEntity target) {
			float yaw = (float) (Mth.atan2(target.getZ() - this.eruptus.getZ(), target.getX() - this.eruptus.getX()) * Mth.RAD_TO_DEG) - 90.0F;
			this.eruptus.setYRot(yaw);
			this.eruptus.yBodyRot = yaw;
			this.eruptus.yHeadRot = yaw;
		}

		private void leap(@Nullable LivingEntity target) {
			Vec3 along = target == null ? Vec3.ZERO : target.position().subtract(this.eruptus.position()).multiply(1.0D, 0.0D, 1.0D);
			double speed = Math.min(along.length() / LUNGE_REACH_PER_SPEED, LUNGE_MAX_SPEED);
			Vec3 launch = along.lengthSqr() < 1.0E-4D ? Vec3.ZERO : along.normalize().scale(speed);
			this.eruptus.setDeltaMovement(launch.x, LUNGE_LIFT, launch.z);
			this.eruptus.hasImpulse = true;
			this.eruptus.playSound(SoundInit.ENTITY_ERYTHROMYCELIUM_ERUPTUS_HURT.get(), 1.0F, 0.7F);
			this.eruptus.spores(14, 0.5D, 0.03D);
			this.hit = false;
			enter(Phase.AIRBORNE);
		}

		private void strike(@Nullable LivingEntity target) {
			if (this.hit || target == null || !target.isAlive()) return;
			if (!this.eruptus.getBoundingBox().inflate(LUNGE_HIT_MARGIN).intersects(target.getBoundingBox())) return;
			this.hit = true;
			this.eruptus.doHurtTarget(target);
		}

		private void land() {
			this.eruptus.spores(26, 0.9D, 0.05D);
			this.cooldown = LUNGE_COOLDOWN_TICKS;
			enter(Phase.RECOVER);
		}
	}

	public static class AmbushEruptGoal extends Goal {
		private final ErythromyceliumEruptusEntity eruptus;
		@Nullable
		private LivingEntity target;

		public AmbushEruptGoal(ErythromyceliumEruptusEntity eruptus) {
			this.eruptus = eruptus;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			if (this.eruptus.isErupted()) return false;
			LivingEntity living = this.eruptus.getTarget();
			return this.eruptus.getSwellDir() > 0 || living != null && this.eruptus.distanceToSqr(living) < 9.0D;
		}

		@Override
		public void start() {
			this.eruptus.getNavigation().stop();
			this.target = this.eruptus.getTarget();
		}

		@Override
		public void stop() {
			this.target = null;
		}

		@Override
		public boolean requiresUpdateEveryTick() {
			return true;
		}

		@Override
		public void tick() {
			boolean inReach = this.target != null && this.eruptus.distanceToSqr(this.target) <= 49.0D
					&& this.eruptus.getSensing().hasLineOfSight(this.target);
			this.eruptus.setSwellDir(inReach ? 1 : -1);
		}
	}

	/** A corpse stays where it lies. */
	public static class EruptedStrollGoal extends WaterAvoidingRandomStrollGoal {
		private final ErythromyceliumEruptusEntity eruptus;

		public EruptedStrollGoal(ErythromyceliumEruptusEntity eruptus, double speed) {
			super(eruptus, speed);
			this.eruptus = eruptus;
		}

		@Override
		public boolean canUse() {
			return this.eruptus.isErupted() && super.canUse();
		}
	}
}
