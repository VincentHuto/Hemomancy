package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.vincenthuto.hemomancy.common.entity.mob.monster.EnthralledDollEntity;
import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class CircusCarouselEntity extends Entity {
	private static final EntityDataAccessor<Boolean> ACTIVE = SynchedEntityData.defineId(
			CircusCarouselEntity.class, EntityDataSerializers.BOOLEAN);
	private static final double TROUPE_RANGE = 16.0D;
	private float rotationSpeed = CircusCarouselRules.targetSpeed(false);
	private int activeTicks;

	public CircusCarouselEntity(EntityType<? extends CircusCarouselEntity> type, Level level) {
		super(type, level);
		noPhysics = true;
		setNoGravity(true);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(ACTIVE, false);
	}

	@Override
	public void tick() {
		super.tick();
		noPhysics = true;
		setNoGravity(true);
		setDeltaMovement(0.0D, 0.0D, 0.0D);
		if (level().isClientSide) return;

		List<CircusPerformerEntity> troupe = level().getEntitiesOfClass(CircusPerformerEntity.class,
				getBoundingBox().inflate(TROUPE_RANGE));
		boolean active = CircusCarouselRules.shouldActivate((int) troupe.stream()
				.filter(performer -> performer.getActState() == CircusPerformerEntity.ActState.ALERT)
				.count());
		if (active != isActive()) {
			entityData.set(ACTIVE, active);
			activeTicks = 0;
			level().playSound(null, blockPosition(), SoundEvents.CHAIN_HIT, SoundSource.HOSTILE,
					active ? 1.2F : 0.7F, active ? 0.65F : 1.0F);
		}

		rotationSpeed = CircusCarouselRules.nextSpeed(rotationSpeed, active);
		setYRot(Mth.wrapDegrees(getYRot() + rotationSpeed));
		if (active) {
			activeTicks++;
			if (activeTicks % 5 == 0) spawnHorseParticles((ServerLevel) level());
			if (CircusCarouselRules.canStrike(activeTicks)) strikeThreats(troupe);
		} else if (tickCount % 120 == 0) {
			level().playSound(null, blockPosition(), SoundInit.ENTITY_ENTHRALLED_DOLL_AMBIENT.get(),
					SoundSource.NEUTRAL, 0.45F, 0.7F);
		}
	}

	private void strikeThreats(List<CircusPerformerEntity> troupe) {
		for (CircusPerformerEntity performer : troupe) {
			LivingEntity target = performer.getTarget();
			if (target == null || !target.isAlive() || target instanceof CircusPerformerEntity
					|| target instanceof EnthralledDollEntity doll && doll.isOwnedByCircusPerformer()) continue;
			for (int horse = 0; horse < 3; horse++) {
				CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(getYRot(), horse);
				double x = getX() + pose.x();
				double y = getY() + 1.1D + pose.bob();
				double z = getZ() + pose.z();
				if (target.getBoundingBox().intersects(new AABB(x - 0.9D, y, z - 0.9D,
						x + 0.9D, y + 2.4D, z + 0.9D))
						&& target.hurt(damageSources().mobAttack(performer), 4.0F)) {
					target.knockback(0.9D, getX() - target.getX(), getZ() - target.getZ());
					level().playSound(null, target.blockPosition(), SoundEvents.IRON_GOLEM_ATTACK,
							SoundSource.HOSTILE, 0.8F, 1.25F);
				}
			}
		}
	}

	private void spawnHorseParticles(ServerLevel level) {
		for (int horse = 0; horse < 3; horse++) {
			CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(getYRot(), horse);
			level.sendParticles(ParticleTypes.CRIMSON_SPORE, getX() + pose.x(),
					getY() + 2.0D + pose.bob(), getZ() + pose.z(), 2, 0.35D, 0.5D, 0.35D, 0.01D);
		}
	}

	public boolean isActive() {
		return entityData.get(ACTIVE);
	}

	public AABB getCarouselRenderBounds() {
		return getBoundingBox().inflate(3.5D, 0.5D, 3.5D);
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		entityData.set(ACTIVE, false);
		activeTicks = 0;
		rotationSpeed = CircusCarouselRules.targetSpeed(false);
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	public boolean isPickable() {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	public boolean isAttackable() {
		return false;
	}
}
