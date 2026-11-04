package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

/**
 * Moves like a Drowned: heavy enough to sink and walk the sea floor, and swims only while it closes on a target that is
 * itself in the water. It never floats up to bob at the surface.
 */
public class BrinedVotaryEntity extends Monster {
	private static final ParticleColor SEEP_COLOR = new ParticleColor(120, 24, 30);


	private final WaterBoundPathNavigation waterNavigation;
	private final GroundPathNavigation groundNavigation;

	public BrinedVotaryEntity(EntityType<? extends BrinedVotaryEntity> type, Level level) {
		super(type, level);
		this.setPathfindingMalus(PathType.WATER, 0.0F);
		this.moveControl = new VotaryMoveControl(this);
		this.waterNavigation = new WaterBoundPathNavigation(this, level);
		this.groundNavigation = new GroundPathNavigation(this, level);
		this.navigation = this.groundNavigation;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 14.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.18D)
				.add(Attributes.ATTACK_DAMAGE, BrinedVotaryBehaviorRules.ATTACK_DAMAGE)
				.add(Attributes.ARMOR, 2.0D)
				.add(Attributes.FOLLOW_RANGE, BrinedVotaryBehaviorRules.WAKE_RANGE_BLOCKS + 2.0D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(3, new MeleeAttackGoal(this, BrinedVotaryBehaviorRules.SWIM_SPEED, true));
		this.goalSelector.addGoal(6, new RandomStrollGoal(this, BrinedVotaryBehaviorRules.SWIM_SPEED));
		this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
				target -> BrinedVotaryBehaviorRules.shouldWakeForTarget(target instanceof Player, target.isAlive(),
						this.distanceTo(target))));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			MobSpawnType reason, SpawnGroupData spawnData) {
		if (reason == MobSpawnType.STRUCTURE) {
			this.setPersistenceRequired();
		}
		return super.finalizeSpawn(level, difficulty, reason, spawnData);
	}

	@Override
	public boolean isPushedByFluid() {
		return !this.isSwimming();
	}

	/** Swims only toward a live target that is in the water too; otherwise it walks the bottom. */
	private boolean wantsToSwim() {
		LivingEntity target = this.getTarget();
		return target != null && target.isAlive() && target.isInWater();
	}

	@Override
	public void updateSwimming() {
		if (this.level().isClientSide) {
			return;
		}
		if (this.isEffectiveAi() && this.isInWater() && this.wantsToSwim()) {
			this.navigation = this.waterNavigation;
			this.setSwimming(true);
		} else {
			this.navigation = this.groundNavigation;
			this.setSwimming(false);
		}
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.isControlledByLocalInstance() && this.isInWater() && this.wantsToSwim()) {
			this.moveRelative(0.01F, travelVector);
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
		} else {
			super.travel(travelVector);
		}
	}

	@Override
	public boolean isVisuallySwimming() {
		return this.isSwimming();
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundInit.ENTITY_BRINED_VOTARY_AMBIENT.get();
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundInit.ENTITY_BRINED_VOTARY_DEATH.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundInit.ENTITY_BRINED_VOTARY_HURT.get();
	}

	@Override
	protected float getSoundVolume() {
		return 0.35F;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			if (this.random.nextInt(16) == 0
					&& this.level().getFluidState(this.blockPosition()).is(FluidTags.WATER)) {
				this.level().addParticle(HemoParticleData.bloodCell(SEEP_COLOR), this.getX(), this.getY() + 1.25D,
						this.getZ(), 0.0D, 0.02D, 0.0D);
			}
		}
	}

	/** The Drowned's move control: swims in three dimensions toward its waypoint, otherwise walks as normal. */
	private static final class VotaryMoveControl extends MoveControl {
		private final BrinedVotaryEntity votary;

		VotaryMoveControl(BrinedVotaryEntity votary) {
			super(votary);
			this.votary = votary;
		}

		@Override
		public void tick() {
			LivingEntity target = this.votary.getTarget();
			if (this.votary.wantsToSwim() && this.votary.isInWater()) {
				if (target != null && target.getY() > this.votary.getY()) {
					this.votary.setDeltaMovement(this.votary.getDeltaMovement().add(0.0D, 0.002D, 0.0D));
				}
				if (this.operation != MoveControl.Operation.MOVE_TO || this.votary.getNavigation().isDone()) {
					this.votary.setSpeed(0.0F);
					return;
				}
				double dx = this.wantedX - this.votary.getX();
				double dy = this.wantedY - this.votary.getY();
				double dz = this.wantedZ - this.votary.getZ();
				double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
				dy /= distance;
				float yaw = (float) (Mth.atan2(dz, dx) * (180.0D / Math.PI)) - 90.0F;
				this.votary.setYRot(this.rotlerp(this.votary.getYRot(), yaw, 90.0F));
				this.votary.yBodyRot = this.votary.getYRot();
				float wanted = (float) (this.speedModifier * this.votary.getAttributeValue(Attributes.MOVEMENT_SPEED));
				float speed = Mth.lerp(0.125F, this.votary.getSpeed(), wanted);
				this.votary.setSpeed(speed);
				this.votary.setDeltaMovement(this.votary.getDeltaMovement().add(
						speed * dx * 0.005D, speed * dy * 0.1D, speed * dz * 0.005D));
			} else {
				if (!this.votary.onGround()) {
					this.votary.setDeltaMovement(this.votary.getDeltaMovement().add(0.0D, -0.008D, 0.0D));
				}
				super.tick();
			}
		}
	}
}
