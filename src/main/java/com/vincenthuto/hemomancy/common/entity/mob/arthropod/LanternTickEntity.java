package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.effect.LanternTickRules;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class LanternTickEntity extends PathfinderMob {
	public static final int ANIMATION_LURE = 0;
	public static final int ANIMATION_STALKING = 1;
	public static final int ANIMATION_LEAPING = 2;
	public static final int ANIMATION_LATCHED = 3;
	public static final int BITE_ANIMATION_TICKS = 14;
	public static final EntityDataAccessor<Integer> LATCHED_PLAYER_ID =
			SynchedEntityData.defineId(LanternTickEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> ANIMATION_STATE =
			SynchedEntityData.defineId(LanternTickEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Integer> BITE_TICKS =
			SynchedEntityData.defineId(LanternTickEntity.class, EntityDataSerializers.INT);

	public final net.minecraft.world.entity.AnimationState idleAnimationState = new net.minecraft.world.entity.AnimationState();
	private int drainCooldown;

	public LanternTickEntity(EntityType<? extends LanternTickEntity> type, Level level) {
		super(type, level);
		this.xpReward = 3;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 8.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.24D)
				.add(Attributes.FOLLOW_RANGE, 18.0D)
				.add(Attributes.ATTACK_DAMAGE, 1.0D);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(3, new RandomStrollGoal(this, 0.55D));
		this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(LATCHED_PLAYER_ID, -1);
		builder.define(ANIMATION_STATE, ANIMATION_LURE);
		builder.define(BITE_TICKS, 0);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.idleAnimationState.startIfStopped(this.tickCount);
			return;
		}
		Player latched = getLatchedPlayer();
		if (latched != null) {
			tickLatched(latched);
		} else {
			if (this.entityData.get(LATCHED_PLAYER_ID) >= 0) {
				clearLatch();
			}
			tickLureAndLeap();
		}
		int biteTicks = this.entityData.get(BITE_TICKS);
		if (biteTicks > 0) {
			this.entityData.set(BITE_TICKS, biteTicks - 1);
		}
	}

	private void tickLureAndLeap() {
		this.noPhysics = false;
		Player target = this.level().getNearestPlayer(this, LanternTickRules.LURE_PLAYER_DISTANCE);
		if (target == null || target.isCreative() || target.isSpectator()) {
			setLanternTickAnimationState(ANIMATION_LURE);
			return;
		}
		this.getLookControl().setLookAt(target, 30.0F, 30.0F);
		double distance = this.distanceTo(target);
		if (distance <= LanternTickRules.LATCH_DISTANCE) {
			setLanternTickAnimationState(ANIMATION_LEAPING);
			Vec3 leap = target.getEyePosition().subtract(this.position()).normalize()
					.scale(LanternTickRules.LATCH_LEAP_STRENGTH);
			this.setDeltaMovement(leap.x, Math.max(0.25D, leap.y), leap.z);
			this.hurtMarked = true;
		} else {
			setLanternTickAnimationState(ANIMATION_STALKING);
		}
		if (distance <= LanternTickRules.ATTACH_DISTANCE) {
			latchTo(target);
		} else {
			this.getNavigation().moveTo(target, 1.1D);
		}
	}

	private void tickLatched(Player player) {
		if (!player.isAlive() || player.isRemoved() || player.isCreative() || player.isSpectator()
				|| player.distanceToSqr(this) > 64.0D) {
			clearLatch();
			return;
		}
		setLanternTickAnimationState(ANIMATION_LATCHED);
		this.noPhysics = true;
		this.getNavigation().stop();
		this.setDeltaMovement(Vec3.ZERO);
		Vec3 head = player.position().add(0.0D, player.getBbHeight() + 0.18D, 0.0D);
		this.setPos(head.x, head.y, head.z);
		this.setYRot(player.getYRot());
		this.setXRot(player.getXRot());
		if (--this.drainCooldown <= 0) {
			this.drainCooldown = LanternTickRules.LATCH_DRAIN_INTERVAL_TICKS;
			drainOrDamage(player);
		}
	}

	private void drainOrDamage(Player player) {
		this.entityData.set(BITE_TICKS, BITE_ANIMATION_TICKS);
		boolean drained = HemoCapabilityAccess.getBloodVolume(player)
				.filter(volume -> volume.isActive() && volume.getBloodVolume() > 0.0D)
				.map(volume -> volume.drain(LanternTickRules.BLOOD_DRAIN_PER_TICK_BITE))
				.orElse(false);
		if (!drained) {
			player.hurt(this.damageSources().mobAttack(this), LanternTickRules.FALLBACK_DAMAGE_PER_BITE);
		}
		this.heal(1.0F);
		this.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,
				SoundSource.HOSTILE, 0.55F, 0.7F + this.random.nextFloat() * 0.2F);
	}

	private void latchTo(Player player) {
		this.entityData.set(LATCHED_PLAYER_ID, player.getId());
		this.drainCooldown = LanternTickRules.LATCH_DRAIN_INTERVAL_TICKS;
		this.level().playSound(null, player.blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE,
				0.7F, 1.25F);
	}

	private void clearLatch() {
		this.noPhysics = false;
		this.entityData.set(LATCHED_PLAYER_ID, -1);
		setLanternTickAnimationState(ANIMATION_LURE);
	}

	public boolean isLatched() {
		return this.entityData.get(LATCHED_PLAYER_ID) >= 0;
	}

	private Player getLatchedPlayer() {
		int id = this.entityData.get(LATCHED_PLAYER_ID);
		if (id < 0) {
			return null;
		}
		Entity entity = this.level().getEntity(id);
		return entity instanceof Player player ? player : null;
	}

	public boolean shouldGlowAsLure() {
		return !isLatched()
				&& this.level().getNearestPlayer(this, LanternTickRules.LURE_PLAYER_DISTANCE) == null;
	}

	public int getLanternTickAnimationState() {
		return this.entityData.get(ANIMATION_STATE);
	}

	public int getBiteAnimationTicks() {
		return this.entityData.get(BITE_TICKS);
	}

	private void setLanternTickAnimationState(int animationState) {
		if (this.entityData.get(ANIMATION_STATE) != animationState) {
			this.entityData.set(ANIMATION_STATE, animationState);
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("LatchedPlayerId", this.entityData.get(LATCHED_PLAYER_ID));
		tag.putInt("DrainCooldown", this.drainCooldown);
		tag.putInt("AnimationState", this.entityData.get(ANIMATION_STATE));
		tag.putInt("BiteTicks", this.entityData.get(BITE_TICKS));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.entityData.set(LATCHED_PLAYER_ID, tag.getInt("LatchedPlayerId"));
		this.drainCooldown = tag.getInt("DrainCooldown");
		this.entityData.set(ANIMATION_STATE, tag.getInt("AnimationState"));
		this.entityData.set(BITE_TICKS, tag.getInt("BiteTicks"));
	}

	public static boolean canSpawnHere(EntityType<? extends LanternTickEntity> type, LevelAccessor level,
			MobSpawnType reason, BlockPos pos, RandomSource random) {
		BlockPos below = pos.below();
		return pos.getY() < level.getSeaLevel() - 8
				&& level.getBrightness(LightLayer.SKY, pos) == 0
				&& level.getBlockState(below).isSolidRender(level, below)
				&& random.nextInt(5) == 0;
	}
}
