package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.damage.HemoDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class PhantasmalEchoEntity extends PathfinderMob implements OwnableEntity {
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
			SynchedEntityData.defineId(PhantasmalEchoEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Integer> DATA_BEHAVIOR_MODE =
			SynchedEntityData.defineId(PhantasmalEchoEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_LIFETIME_TICKS =
			SynchedEntityData.defineId(PhantasmalEchoEntity.class, EntityDataSerializers.INT);
	private static final double SMOKE_RADIUS = 3.5D;
	private static final int BLINDNESS_TICKS = 100;
	private static final int CONFUSION_TICKS = 100;
	private static final int SLOWNESS_TICKS = 60;
	private static final float ECHO_DAMAGE = 2.0F;
	private Vec3 initialMovement = Vec3.ZERO;

	public enum BehaviorMode {
		AGGRESSIVE,
		FLEEING,
		CIRCLING,
		MIRRORING
	}

	public PhantasmalEchoEntity(EntityType<? extends PhantasmalEchoEntity> type, Level level) {
		super(type, level);
		this.xpReward = 0;
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 1.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.36D)
				.add(Attributes.ATTACK_DAMAGE, ECHO_DAMAGE)
				.add(Attributes.FOLLOW_RANGE, 24.0D);
	}

	public void configureFromOwner(ServerPlayer owner, BehaviorMode behaviorMode, int lifetimeTicks,
			Vec3 movementVector) {
		setOwnerUUID(owner.getUUID());
		setBehaviorMode(behaviorMode);
		entityData.set(DATA_LIFETIME_TICKS, Math.max(1, lifetimeTicks));
		initialMovement = movementVector;
		setDeltaMovement(movementVector);
		setItemSlot(EquipmentSlot.HEAD, owner.getItemBySlot(EquipmentSlot.HEAD).copy());
		setItemSlot(EquipmentSlot.CHEST, owner.getItemBySlot(EquipmentSlot.CHEST).copy());
		setItemSlot(EquipmentSlot.LEGS, owner.getItemBySlot(EquipmentSlot.LEGS).copy());
		setItemSlot(EquipmentSlot.FEET, owner.getItemBySlot(EquipmentSlot.FEET).copy());
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			setDropChance(slot, 0.0F);
		}
		setHealth(1.0F);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.18D, true));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_OWNER_UUID, Optional.empty());
		builder.define(DATA_BEHAVIOR_MODE, BehaviorMode.AGGRESSIVE.ordinal());
		builder.define(DATA_LIFETIME_TICKS, 200);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			tickClientParticles();
			return;
		}
		if (tickCount >= entityData.get(DATA_LIFETIME_TICKS)) {
			dissolveQuietly();
			return;
		}
		if (getHealth() > 1.0F) {
			setHealth(1.0F);
		}
		tickBehavior();
		if (tickCount % 20 == 0) {
			forceNearbyHostilesToConsiderEcho();
		}
	}

	private void tickClientParticles() {
		if (random.nextInt(4) == 0) {
			level().addParticle(ParticleTypes.REVERSE_PORTAL,
					getX() + (random.nextDouble() - 0.5D) * 0.6D,
					getY() + random.nextDouble() * getBbHeight(),
					getZ() + (random.nextDouble() - 0.5D) * 0.6D,
					(random.nextDouble() - 0.5D) * 0.02D,
					random.nextDouble() * 0.02D,
					(random.nextDouble() - 0.5D) * 0.02D);
		}
		if (random.nextInt(10) == 0) {
			level().addParticle(ParticleTypes.SMOKE,
					getX(), getY() + 0.35D + random.nextDouble(), getZ(),
					0.0D, 0.01D, 0.0D);
		}
	}

	private void tickBehavior() {
		if (getBehaviorMode() == BehaviorMode.MIRRORING && tickCount < 30) {
			setDeltaMovement(getDeltaMovement().scale(0.86D).add(initialMovement.scale(0.08D)));
			return;
		}
		if (getBehaviorMode() == BehaviorMode.MIRRORING) {
			setBehaviorMode(random.nextBoolean() ? BehaviorMode.AGGRESSIVE : BehaviorMode.FLEEING);
		}
		switch (getBehaviorMode()) {
			case AGGRESSIVE -> tickAggressive();
			case FLEEING -> tickFleeing();
			case CIRCLING -> tickCircling();
			case MIRRORING -> {
			}
		}
	}

	private void tickAggressive() {
		if (getTarget() == null || !canAttack(getTarget())) {
			findNearestHostile(18.0D).ifPresent(this::setTarget);
		}
	}

	private void tickFleeing() {
		if (tickCount % 15 != 0) {
			return;
		}
		Vec3 away = position().subtract(ownerPositionOrLookAnchor());
		if (away.lengthSqr() < 0.01D) {
			away = initialMovement.lengthSqr() > 0.01D ? initialMovement : getLookAngle();
		}
		Vec3 destination = position().add(away.normalize().scale(5.0D));
		getNavigation().moveTo(destination.x, destination.y, destination.z, 1.15D);
	}

	private void tickCircling() {
		if (tickCount % 10 != 0) {
			return;
		}
		Vec3 center = findNearestHostile(12.0D)
				.map(Entity::position)
				.orElse(ownerPositionOrLookAnchor());
		Vec3 fromCenter = position().subtract(center);
		if (fromCenter.lengthSqr() < 0.01D) {
			fromCenter = getLookAngle();
		}
		Vec3 tangent = new Vec3(-fromCenter.z, 0.0D, fromCenter.x).normalize();
		Vec3 destination = center.add(fromCenter.normalize().scale(3.0D)).add(tangent.scale(2.0D));
		getNavigation().moveTo(destination.x, destination.y, destination.z, 1.05D);
		findNearestHostile(2.4D).ifPresent(this::setTarget);
	}

	private Vec3 ownerPositionOrLookAnchor() {
		Player owner = getOwnerPlayer();
		if (owner != null) {
			return owner.position();
		}
		return position().subtract(getLookAngle());
	}

	private Optional<Monster> findNearestHostile(double range) {
		AABB search = getBoundingBox().inflate(range);
		return level().getEntitiesOfClass(Monster.class, search, this::canAttack)
				.stream()
				.min(Comparator.comparingDouble(this::distanceToSqr));
	}

	private void forceNearbyHostilesToConsiderEcho() {
		AABB search = getBoundingBox().inflate(8.0D);
		for (Monster monster : level().getEntitiesOfClass(Monster.class, search, this::canAttack)) {
			LivingEntity currentTarget = monster.getTarget();
			if (currentTarget == null || currentTarget instanceof Player || currentTarget == getOwnerPlayer()) {
				monster.setTarget(this);
			}
		}
	}

	@Override
	public boolean doHurtTarget(Entity entity) {
		if (!(entity instanceof LivingEntity target) || !canAttack(target)) {
			return false;
		}
		Entity owner = getOwnerPlayer();
		boolean hurt = target.hurt(HemoDamageTypes.phantasmalEcho(level(), this, owner), ECHO_DAMAGE);
		if (hurt) {
			level().playSound(null, blockPosition(), SoundEvents.PHANTOM_BITE, SoundSource.HOSTILE, 0.45F, 1.6F);
		}
		return hurt;
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		return target instanceof Monster
				&& target != this
				&& target.isAlive()
				&& !(target instanceof PhantasmalEchoEntity)
				&& !target.getUUID().equals(getOwnerUUID())
				&& super.canAttack(target);
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (level().isClientSide) {
			return true;
		}
		if (source.getEntity() == this || source.getDirectEntity() == this) {
			return false;
		}
		burstSmokeCloud();
		discard();
		return true;
	}

	@Override
	public void die(DamageSource source) {
		if (!level().isClientSide) {
			burstSmokeCloud();
			discard();
		}
	}

	public void burstSmokeCloud() {
		if (!(level() instanceof ServerLevel serverLevel)) {
			return;
		}
		serverLevel.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 0.8D, getZ(),
				80, 0.85D, 0.45D, 0.85D, 0.04D);
		serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 0.8D, getZ(),
				48, 0.7D, 0.55D, 0.7D, 0.06D);
		serverLevel.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
				SoundSource.HOSTILE, 0.45F, 1.45F);
		for (Monster monster : serverLevel.getEntitiesOfClass(Monster.class,
				getBoundingBox().inflate(SMOKE_RADIUS), Monster::isAlive)) {
			monster.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, BLINDNESS_TICKS, 0, false, true, true));
			monster.addEffect(new MobEffectInstance(MobEffects.CONFUSION, CONFUSION_TICKS, 0, false, true, true));
			monster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_TICKS, 0, false, true, true));
			monster.setTarget(null);
			monster.getNavigation().stop();
		}
	}

	public void dissolveQuietly() {
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, getX(), getY() + 0.8D, getZ(),
					24, 0.45D, 0.35D, 0.45D, 0.03D);
			serverLevel.playSound(null, blockPosition(), SoundEvents.SOUL_ESCAPE.value(),
					SoundSource.HOSTILE, 0.35F, 1.25F);
		}
		discard();
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void doPush(Entity entity) {
		if (!(entity instanceof Player) && !(entity instanceof PhantasmalEchoEntity)) {
			super.doPush(entity);
		}
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		UUID owner = getOwnerUUID();
		if (owner != null) {
			tag.putUUID("Owner", owner);
		}
		tag.putInt("BehaviorMode", getBehaviorMode().ordinal());
		tag.putInt("LifetimeTicks", entityData.get(DATA_LIFETIME_TICKS));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		if (tag.hasUUID("Owner")) {
			setOwnerUUID(tag.getUUID("Owner"));
		}
		if (tag.contains("BehaviorMode")) {
			setBehaviorMode(behaviorById(tag.getInt("BehaviorMode")));
		}
		if (tag.contains("LifetimeTicks")) {
			entityData.set(DATA_LIFETIME_TICKS, Math.max(1, tag.getInt("LifetimeTicks")));
		}
	}

	public BehaviorMode getBehaviorMode() {
		return behaviorById(entityData.get(DATA_BEHAVIOR_MODE));
	}

	public void setBehaviorMode(BehaviorMode behaviorMode) {
		entityData.set(DATA_BEHAVIOR_MODE, behaviorMode.ordinal());
	}

	private static BehaviorMode behaviorById(int id) {
		BehaviorMode[] modes = BehaviorMode.values();
		return modes[Mth.clamp(id, 0, modes.length - 1)];
	}

	private void setOwnerUUID(@Nullable UUID ownerUuid) {
		entityData.set(DATA_OWNER_UUID, Optional.ofNullable(ownerUuid));
	}

	@Nullable
	private Player getOwnerPlayer() {
		UUID owner = getOwnerUUID();
		if (owner == null || !(level() instanceof ServerLevel serverLevel)) {
			return null;
		}
		return serverLevel.getPlayerByUUID(owner);
	}

	@Nullable
	@Override
	public UUID getOwnerUUID() {
		return entityData.get(DATA_OWNER_UUID).orElse(null);
	}
}
