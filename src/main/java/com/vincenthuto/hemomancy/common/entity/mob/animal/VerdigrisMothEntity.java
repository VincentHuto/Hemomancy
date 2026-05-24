package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class VerdigrisMothEntity extends PathfinderMob {
	public final AnimationState flyAnimationState = new AnimationState();
	public final AnimationState idleAnimationState = new AnimationState();

	private int naturalShedCooldownTicks;
	private int brushShedCooldownTicks;

	public VerdigrisMothEntity(EntityType<? extends VerdigrisMothEntity> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
		this.naturalShedCooldownTicks = VerdigrisMothRules.MIN_NATURAL_SHED_COOLDOWN_TICKS;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 4.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.16D)
				.add(Attributes.FLYING_SPEED, 0.18D)
				.add(Attributes.FOLLOW_RANGE, 12.0D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new VerdigrisMothFlutterGoal(this));
		this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
	}

	public static boolean canSpawnHere(EntityType<? extends VerdigrisMothEntity> type, LevelAccessor level,
			MobSpawnType spawnReason, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		boolean solidBelow = level.getBlockState(below).isSolidRender(level, below);
		boolean openAir = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
				&& level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
		return VerdigrisMothRules.canNaturalSpawn(VerdigrisMothRules.isNight(level.getLevelData().getDayTime()),
				solidBelow, openAir, level.getMaxLocalRawBrightness(pos));
	}

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		this.naturalShedCooldownTicks = VerdigrisMothRules.nextNaturalShedCooldown(this.random);
		return super.finalizeSpawn(level, difficulty, reason, spawnData);
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);

		if (this.level().isClientSide()) {
			this.updateClientAnimation();
			return;
		}

		if (this.naturalShedCooldownTicks > 0) {
			this.naturalShedCooldownTicks--;
		}
		if (this.brushShedCooldownTicks > 0) {
			this.brushShedCooldownTicks--;
		}
		if (VerdigrisMothRules.canShedNaturally(false, this.isAlive(), this.onGround(),
				this.isInWaterOrBubble(), this.naturalShedCooldownTicks)
				&& this.random.nextInt(VerdigrisMothRules.NATURAL_SHED_ROLL_BOUND) == 0) {
			this.shedViridSalis(0.35F, 1.15F);
			this.naturalShedCooldownTicks = VerdigrisMothRules.nextNaturalShedCooldown(this.random);
		}
	}

	private void updateClientAnimation() {
		if (this.onGround()) {
			this.flyAnimationState.stop();
			this.idleAnimationState.startIfStopped(this.tickCount);
		} else {
			this.idleAnimationState.stop();
			this.flyAnimationState.startIfStopped(this.tickCount);
		}
		if (this.random.nextInt(14) == 0) {
			this.level().addParticle(ParticleTypes.END_ROD,
					this.getX() + (this.random.nextDouble() - 0.5D) * 0.4D,
					this.getY() + 0.2D,
					this.getZ() + (this.random.nextDouble() - 0.5D) * 0.4D,
					0.0D, 0.004D, 0.0D);
		}
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.isAlive()) {
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(VerdigrisMothRules.FLIGHT_DAMPING));
		} else {
			super.travel(travelVector);
		}
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		ItemStack held = player.getItemInHand(hand);
		if (!held.is(Items.BRUSH)) {
			return super.mobInteract(player, hand);
		}
		if (!this.level().isClientSide()
				&& VerdigrisMothRules.canBrushShed(false, this.isAlive(), this.brushShedCooldownTicks)) {
			this.shedViridSalis(0.45F, 1.35F);
			this.brushShedCooldownTicks = VerdigrisMothRules.BRUSH_SHED_COOLDOWN_TICKS;
			this.naturalShedCooldownTicks = Math.max(this.naturalShedCooldownTicks,
					VerdigrisMothRules.MIN_NATURAL_SHED_COOLDOWN_TICKS);
			if (!player.getAbilities().instabuild) {
				held.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
			}
		}
		this.level().playSound(null, this.blockPosition(), SoundEvents.AXE_SCRAPE, SoundSource.NEUTRAL,
				0.55F, 1.4F + this.random.nextFloat() * 0.2F);
		return InteractionResult.sidedSuccess(this.level().isClientSide());
	}

	private void shedViridSalis(float soundVolume, float pitch) {
		this.spawnAtLocation(new ItemStack(BlockInit.virid_salis_trail.get()));
		this.level().playSound(null, this.blockPosition(), SoundEvents.AXE_SCRAPE, SoundSource.NEUTRAL,
				soundVolume, pitch);
		if (this.level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.SCRAPE,
					this.getX(), this.getY() + 0.25D, this.getZ(),
					8, 0.25D, 0.15D, 0.25D, 0.02D);
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					this.getX(), this.getY() + 0.25D, this.getZ(),
					3, 0.18D, 0.08D, 0.18D, 0.004D);
		}
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader level) {
		return level.isUnobstructed(this);
	}

	@Override
	protected int calculateFallDamage(float distance, float damageMultiplier) {
		return 0;
	}

	@Nullable
	@Override
	protected SoundEvent getAmbientSound() {
		return this.random.nextInt(4) == 0 ? SoundEvents.BAT_AMBIENT : null;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.BEEHIVE_SHEAR;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.BEEHIVE_EXIT;
	}

	@Override
	protected float getSoundVolume() {
		return 0.15F;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("NaturalShedCooldown", this.naturalShedCooldownTicks);
		tag.putInt("BrushShedCooldown", this.brushShedCooldownTicks);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.naturalShedCooldownTicks = tag.getInt("NaturalShedCooldown");
		this.brushShedCooldownTicks = tag.getInt("BrushShedCooldown");
	}

	private static class VerdigrisMothFlutterGoal extends Goal {
		private final VerdigrisMothEntity moth;
		private Vec3 target;
		private int retargetTicks;

		VerdigrisMothFlutterGoal(VerdigrisMothEntity moth) {
			this.moth = moth;
			this.setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return true;
		}

		@Override
		public void tick() {
			if (this.target == null || this.retargetTicks-- <= 0
					|| this.moth.position().distanceToSqr(this.target) < 1.0D) {
				this.target = this.pickTarget();
				this.retargetTicks = 40 + this.moth.random.nextInt(60);
			}
			Vec3 toTarget = this.target.subtract(this.moth.position());
			if (toTarget.lengthSqr() > 0.01D) {
				Vec3 impulse = toTarget.normalize().scale(VerdigrisMothRules.FLIGHT_ACCELERATION);
				Vec3 next = this.moth.getDeltaMovement().add(impulse);
				if (next.length() > VerdigrisMothRules.MAX_FLIGHT_SPEED) {
					next = next.normalize().scale(VerdigrisMothRules.MAX_FLIGHT_SPEED);
				}
				this.moth.setDeltaMovement(next);
				if (next.horizontalDistanceSqr() > 1.0E-4D) {
					this.moth.setYRot((float) (Mth.atan2(next.z, next.x) * Mth.RAD_TO_DEG) - 90.0F);
				}
			}
		}

		private Vec3 pickTarget() {
			BlockPos base = this.moth.blockPosition();
			for (int i = 0; i < 10; i++) {
				int x = base.getX() + this.moth.random.nextInt(13) - 6;
				int y = Mth.clamp(base.getY() + this.moth.random.nextInt(7) - 2,
						this.moth.level().getMinBuildHeight() + 2,
						this.moth.level().getMaxBuildHeight() - 2);
				int z = base.getZ() + this.moth.random.nextInt(13) - 6;
				BlockPos candidate = new BlockPos(x, y, z);
				if (this.moth.level().getBlockState(candidate).getCollisionShape(this.moth.level(), candidate).isEmpty()
						&& this.moth.level().getBlockState(candidate.above())
						.getCollisionShape(this.moth.level(), candidate.above()).isEmpty()) {
					return Vec3.atCenterOf(candidate);
				}
			}
			return this.moth.position().add(
					this.moth.random.nextDouble() - 0.5D,
					(this.moth.random.nextDouble() - 0.25D) * 1.5D,
					this.moth.random.nextDouble() - 0.5D);
		}
	}
}
