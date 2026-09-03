package com.vincenthuto.hemomancy.common.entity.mob.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class LuminalCicadaEntity extends PathfinderMob {
	private static final byte FLASH_EVENT = 62;
	private static final EntityDataAccessor<Byte> DATA_CLING_FACE = SynchedEntityData.defineId(
			LuminalCicadaEntity.class, EntityDataSerializers.BYTE);
	private BlockPos clingLog;
	private Direction clingFace;
	private int flashCooldown;
	private int flashTicks;

	public LuminalCicadaEntity(EntityType<? extends LuminalCicadaEntity> type, Level level) {
		super(type, level);
		this.setNoGravity(true);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 4.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.14D)
				.add(Attributes.FLYING_SPEED, 0.16D)
				.add(Attributes.FOLLOW_RANGE, 10.0D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_CLING_FACE, (byte) -1);
	}

	@Nullable
	public Direction getClingFace() {
		byte value = this.entityData.get(DATA_CLING_FACE);
		if (value < 0) return null;
		Direction face = Direction.from3DDataValue(value);
		return face.getAxis().isHorizontal() ? face : null;
	}

	private void setClingFace(@Nullable Direction face) {
		this.entityData.set(DATA_CLING_FACE, face == null ? (byte) -1 : (byte) face.get3DDataValue());
	}

	private void orientToTree(Direction face) {
		float yaw = LuminalCicadaRules.clingBodyYaw(face);
		this.setYRot(yaw);
		this.yBodyRot = yaw;
		this.yHeadRot = yaw;
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(1, new ClingToTreeGoal());
	}

	public static boolean canSpawnHere(EntityType<? extends LuminalCicadaEntity> type, LevelAccessor level,
			MobSpawnType reason, BlockPos pos, RandomSource random) {
		boolean openAir = level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
				&& level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty();
		return LuminalCicadaRules.canNaturalSpawn(hasNearbyTree(level, pos), openAir);
	}

	private static boolean hasNearbyTree(LevelAccessor level, BlockPos origin) {
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-5, -3, -5), origin.offset(5, 5, 5))) {
			if (level.getBlockState(pos).is(BlockTags.LOGS)) return true;
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.setNoGravity(true);
		if (this.level().isClientSide()) {
			if (this.flashTicks > 0) this.flashTicks--;
			return;
		}
		if (this.flashCooldown > 0) this.flashCooldown--;
		if (this.clingLog == null) return;

		Player player = this.level().getNearestPlayer(this,
				Math.sqrt(LuminalCicadaRules.FLASH_RANGE_SQUARED));
		if (player != null && LuminalCicadaRules.shouldFlash(this.distanceToSqr(player), this.flashCooldown)) {
			flash();
		}
	}

	private void flash() {
		this.flashCooldown = LuminalCicadaRules.FLASH_COOLDOWN_TICKS;
		this.level().broadcastEntityEvent(this, FLASH_EVENT);
		this.playSound(SoundEvents.FIREWORK_ROCKET_BLAST, 0.65F, 1.8F);
		if (this.level() instanceof ServerLevel server) {
			server.sendParticles(ParticleTypes.FLASH, this.getX(), this.getY() + 0.2D, this.getZ(),
					1, 0.0D, 0.0D, 0.0D, 0.0D);
			server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.2D, this.getZ(),
					18, 0.45D, 0.35D, 0.45D, 0.08D);
		}
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id == FLASH_EVENT) {
			this.flashTicks = 8;
		} else {
			super.handleEntityEvent(id);
		}
	}

	public boolean isFlashing() {
		return this.flashTicks > 0;
	}

	@Override
	public void travel(Vec3 travelVector) {
		if (this.isAlive()) {
			this.move(MoverType.SELF, this.getDeltaMovement());
			this.setDeltaMovement(this.getDeltaMovement().scale(0.82D));
		} else {
			super.travel(travelVector);
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
		return SoundEvents.BEE_LOOP;
	}

	@Override
	public int getAmbientSoundInterval() {
		return 60;
	}

	@Override
	protected float getSoundVolume() {
		return 0.22F;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt("FlashCooldown", this.flashCooldown);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.flashCooldown = tag.getInt("FlashCooldown");
	}

	private final class ClingToTreeGoal extends Goal {
		private Vec3 target;
		private BlockPos targetLog;
		private Direction targetFace;
		private Vec3 idleTarget;
		private int searchCooldown;
		private int idleTicks;

		private ClingToTreeGoal() {
			this.setFlags(EnumSet.of(Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return true;
		}

		@Override
		public void tick() {
			if (clingLog != null && validCling(clingLog, clingFace)) {
				Vec3 anchor = LuminalCicadaRules.clingAnchor(clingLog, clingFace, getBbWidth() * 0.5D);
				setPos(anchor.x, anchor.y, anchor.z);
				setDeltaMovement(Vec3.ZERO);
				orientToTree(clingFace);
				return;
			}
			clingLog = null;
			setClingFace(null);
			if (!validCling(targetLog, targetFace)) {
				target = null;
				if (searchCooldown-- <= 0) findTarget();
			}
			if (target == null) {
				tickIdleFlight();
				return;
			}
			idleTarget = null;

			Vec3 offset = target.subtract(position());
			if (offset.lengthSqr() < 0.08D) {
				setPos(target.x, target.y, target.z);
				setDeltaMovement(Vec3.ZERO);
				clingLog = targetLog;
				clingFace = targetFace;
				setClingFace(clingFace);
				orientToTree(clingFace);
				return;
			}
			steerToward(target, 0.025D, 0.15D);
		}

		private void tickIdleFlight() {
			double distance = idleTarget == null ? Double.MAX_VALUE : idleTarget.distanceToSqr(position());
			if (LuminalCicadaRules.shouldPickIdleDestination(false, idleTarget != null, distance, idleTicks)) {
				pickIdleDestination();
			} else {
				idleTicks--;
			}
			if (idleTarget != null) steerToward(idleTarget, 0.012D, 0.1D);
		}

		private void pickIdleDestination() {
			idleTicks = 25 + random.nextInt(35);
			for (int attempt = 0; attempt < 5; attempt++) {
				Vec3 candidate = position().add((random.nextDouble() - 0.5D) * 6.0D,
						(random.nextDouble() - 0.5D) * 2.0D,
						(random.nextDouble() - 0.5D) * 6.0D);
				if (!level().noCollision(LuminalCicadaEntity.this,
						getBoundingBox().move(candidate.subtract(position())))) continue;
				idleTarget = candidate;
				return;
			}
			idleTarget = null;
		}

		private void steerToward(Vec3 destination, double acceleration, double maxSpeed) {
			Vec3 offset = destination.subtract(position());
			if (offset.lengthSqr() < 1.0E-5D) return;
			Vec3 next = getDeltaMovement().add(offset.normalize().scale(acceleration));
			if (next.lengthSqr() > maxSpeed * maxSpeed) next = next.normalize().scale(maxSpeed);
			setDeltaMovement(next);
			setYRot((float) (Mth.atan2(next.z, next.x) * Mth.RAD_TO_DEG) - 90.0F);
		}

		private boolean findTarget() {
			searchCooldown = 40;
			target = null;
			targetLog = null;
			targetFace = null;
			BlockPos origin = blockPosition();
			double best = Double.MAX_VALUE;
			for (BlockPos log : BlockPos.betweenClosed(origin.offset(-6, -4, -6), origin.offset(6, 6, 6))) {
				if (!level().getBlockState(log).is(BlockTags.LOGS)) continue;
				for (Direction face : Direction.Plane.HORIZONTAL) {
					BlockPos air = log.relative(face);
					boolean open = level().getBlockState(air).getCollisionShape(level(), air).isEmpty();
					if (!LuminalCicadaRules.canCling(true, open)) continue;
					double distance = air.distToCenterSqr(getX(), getY(), getZ());
					if (distance >= best) continue;
					best = distance;
					targetLog = log.immutable();
					targetFace = face;
					target = LuminalCicadaRules.clingAnchor(targetLog, targetFace, getBbWidth() * 0.5D);
					setYRot(LuminalCicadaRules.clingBodyYaw(face));
				}
			}
			return target != null;
		}

		private boolean validCling(BlockPos log, Direction face) {
			if (log == null || face == null || !level().getBlockState(log).is(BlockTags.LOGS)) return false;
			BlockPos air = log.relative(face);
			return LuminalCicadaRules.canCling(true,
					level().getBlockState(air).getCollisionShape(level(), air).isEmpty());
		}
	}
}
