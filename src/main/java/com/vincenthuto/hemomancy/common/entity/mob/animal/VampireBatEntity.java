package com.vincenthuto.hemomancy.common.entity.mob.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.UUID;

public final class VampireBatEntity extends Bat {
	private static final EntityDataAccessor<Boolean> AGGREGATE = SynchedEntityData.defineId(
			VampireBatEntity.class, EntityDataSerializers.BOOLEAN);

	public VampireBatEntity(EntityType<? extends VampireBatEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Bat.createAttributes();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(AGGREGATE, false);
	}

	public void makeManifestedAggregate(UUID owner) {
		entityData.set(AGGREGATE, true);
		getPersistentData().putUUID("CircusSwarmOwner", owner);
		setResting(false);
		setPersistenceRequired();
	}

	public boolean isManifestedAggregate() {
		return entityData.get(AGGREGATE);
	}

	public static boolean canSpawnHere(EntityType<VampireBatEntity> type, LevelAccessor level,
			MobSpawnType reason, BlockPos pos, RandomSource random) {
		return !level.canSeeSky(pos) && pos.getY() < level.getSeaLevel() - 8
				&& level.getMaxLocalRawBrightness(pos) <= 7
				&& level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
	}

	@Override
	public int getMaxSpawnClusterSize() {
		return 20;
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			if (isManifestedAggregate() && random.nextBoolean()) level().addParticle(ParticleTypes.CRIMSON_SPORE,
					getRandomX(2.5D), getRandomY(), getRandomZ(2.5D), 0.0D, 0.01D, 0.0D);
			return;
		}
		LivingEntity prey = isManifestedAggregate() ? aggregateOwner() : bleedingPrey();
		if (isManifestedAggregate() && prey == null) {
			discard();
			return;
		}
		if (prey == null) return;
		setResting(false);
		Vec3 delta = prey.getEyePosition().subtract(position());
		if (delta.lengthSqr() > 0.01D) setDeltaMovement(getDeltaMovement().scale(0.75D)
				.add(delta.normalize().scale(isManifestedAggregate() ? 0.09D : 0.035D)));
		if (tickCount % (isManifestedAggregate() ? 25 : 60) == 0 && distanceToSqr(prey) < 2.25D)
			prey.hurt(damageSources().mobAttack(this), isManifestedAggregate() ? 2.0F : 1.0F);
	}

	private LivingEntity aggregateOwner() {
		if (!(level() instanceof ServerLevel server) || !getPersistentData().hasUUID("CircusSwarmOwner")) return null;
		ServerPlayer owner = server.getServer().getPlayerList().getPlayer(getPersistentData().getUUID("CircusSwarmOwner"));
		return owner != null && owner.isAlive() && owner.level() == level() ? owner : null;
	}

	private LivingEntity bleedingPrey() {
		return level().getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(8.0D),
				living -> living != this && living.isAlive() && living.getHealth() < living.getMaxHealth())
				.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putBoolean("ManifestedAggregate", isManifestedAggregate());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		entityData.set(AGGREGATE, tag.getBoolean("ManifestedAggregate"));
	}
}
