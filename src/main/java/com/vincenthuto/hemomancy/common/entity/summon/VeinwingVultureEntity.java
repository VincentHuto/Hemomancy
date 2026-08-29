package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.entity.projectile.VeinwingFeatherEntity;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class VeinwingVultureEntity extends Vex implements BoundPuppeteerSummon {
	private static final int ATTACK_COOLDOWN_TICKS = 15;
	private static final int VOLLEY_COOLDOWN_TICKS = 50;
	private static final double VOLLEY_MIN_RANGE = 3.5;
	private static final double VOLLEY_MAX_RANGE = 14.0;
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
			SynchedEntityData.defineId(VeinwingVultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_CROSSBAR_UUID =
			SynchedEntityData.defineId(VeinwingVultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<String> DATA_SUMMON_NAME =
			SynchedEntityData.defineId(VeinwingVultureEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> DATA_DISMISSAL_TICKS =
			SynchedEntityData.defineId(VeinwingVultureEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_TRIAL_SUMMON =
			SynchedEntityData.defineId(VeinwingVultureEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> DATA_TRIAL_CASTER_UUID =
			SynchedEntityData.defineId(VeinwingVultureEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private int attackCooldown;
	private int volleyCooldown;

	public VeinwingVultureEntity(EntityType<? extends Vex> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Vex.createAttributes()
				.add(Attributes.MAX_HEALTH, 14.0)
				.add(Attributes.ATTACK_DAMAGE, 4.0)
				.add(Attributes.MOVEMENT_SPEED, 0.36)
				.add(Attributes.FLYING_SPEED, 0.43);
	}

	@Override
	protected void registerGoals() {
		// Vex navigation is move-controller driven; combat is handled in tickVultureCombat.
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_OWNER_UUID, Optional.empty());
		builder.define(DATA_CROSSBAR_UUID, Optional.empty());
		builder.define(DATA_SUMMON_NAME, "veinwing_vulture");
		builder.define(DATA_DISMISSAL_TICKS, 0);
		builder.define(DATA_TRIAL_SUMMON, false);
		builder.define(DATA_TRIAL_CASTER_UUID, Optional.empty());
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			return;
		}
		if (hemomancy$isTrialSummon()) {
			BoundSummonBehavior.trialServerTick(this, this);
			tickVultureCombat();
			return;
		}
		if (BoundSummonBehavior.commonServerTick(this, this)) {
			Optional<Player> owner = BoundSummonBehavior.ownerFor(this, this);
			if (owner.isPresent()
					&& BoundSummonBehavior.shouldFollowOwner((net.minecraft.server.level.ServerPlayer) owner.get(), this)
					&& (getTarget() == null || distanceToSqr(owner.get()) > 144.0)) {
				BoundSummonBehavior.followFlyingOwner(this, owner.get(), 1.12, 18.0);
			}
			tickVultureCombat();
		}
	}

	private void tickVultureCombat() {
		if (attackCooldown > 0) attackCooldown--;
		if (volleyCooldown > 0) volleyCooldown--;
		LivingEntity target = getTarget();
		if (target == null || !target.isAlive() || !canAttack(target)) {
			return;
		}
		Vec3 destination = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
		Vec3 delta = destination.subtract(position());
		if (delta.lengthSqr() > 0.01) {
			getMoveControl().setWantedPosition(destination.x, destination.y, destination.z, 1.25);
			setDeltaMovement(getDeltaMovement().scale(0.82).add(delta.normalize().scale(0.11)));
		}
		double attackReach = getBbWidth() * 2.0 + target.getBbWidth() + 0.75;
		double distanceSquared = distanceToSqr(target);
		if (volleyCooldown == 0 && distanceSquared >= VOLLEY_MIN_RANGE * VOLLEY_MIN_RANGE
				&& distanceSquared <= VOLLEY_MAX_RANGE * VOLLEY_MAX_RANGE && hasLineOfSight(target)) {
			fireFeatherVolley(target);
			volleyCooldown = VOLLEY_COOLDOWN_TICKS;
		}
		if (attackCooldown == 0 && distanceSquared <= attackReach * attackReach) {
			swing(InteractionHand.MAIN_HAND);
			doHurtTarget(target);
			attackCooldown = ATTACK_COOLDOWN_TICKS;
		}
	}

	private void fireFeatherVolley(LivingEntity target) {
		int count = 4 + random.nextInt(3);
		for (int i = 0; i < count; i++) {
			VeinwingFeatherEntity feather = new VeinwingFeatherEntity(level(), this);
			double dx = target.getX() - getX();
			double dz = target.getZ() - getZ();
			double horizontal = Math.sqrt(dx * dx + dz * dz);
			double dy = target.getY(0.55) - feather.getY() + horizontal * 0.02;
			feather.shoot(dx, dy, dz, 1.45F, 7.0F);
			level().addFreshEntity(feather);
		}
		level().playSound(null, blockPosition(), SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE, 0.7F, 1.35F);
	}

	@Override
	public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
		return BoundSummonBehavior.canAttack(this, this, target) && super.canAttack(target);
	}

	@Override
	public void move(MoverType type, Vec3 movement) {
		// Vex movement deliberately enables no-clip. Trial vultures are physical enemies;
		// only a vulture bound to a Crossbar may retain that puppeteered phasing.
		if (hemomancy$isTrialSummon()) this.noPhysics = false;
		super.move(type, movement);
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return PuppeteerSummonRules.shouldDespawnInPeaceful(
				hemomancy$isTrialSummon(), hemomancy$getOwnerUUID());
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		BoundSummonBehavior.save(this, tag);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		BoundSummonBehavior.load(this, tag);
	}

	@Override public UUID hemomancy$getOwnerUUID() { return entityData.get(DATA_OWNER_UUID).orElse(null); }
	@Override public void hemomancy$setOwnerUUID(UUID ownerUuid) { entityData.set(DATA_OWNER_UUID, Optional.ofNullable(ownerUuid)); }
	@Override public UUID hemomancy$getCrossbarUUID() { return entityData.get(DATA_CROSSBAR_UUID).orElse(null); }
	@Override public void hemomancy$setCrossbarUUID(UUID crossbarUuid) { entityData.set(DATA_CROSSBAR_UUID, Optional.ofNullable(crossbarUuid)); }
	@Override public String hemomancy$getSummonName() { return entityData.get(DATA_SUMMON_NAME); }
	@Override public void hemomancy$setSummonName(String summonName) { entityData.set(DATA_SUMMON_NAME, summonName == null ? "" : summonName); }
	@Override public int hemomancy$getDismissalTicks() { return entityData.get(DATA_DISMISSAL_TICKS); }
	@Override public void hemomancy$setDismissalTicks(int ticks) {
		entityData.set(DATA_DISMISSAL_TICKS, Math.max(0, ticks));
	}
	@Override public boolean hemomancy$isTrialSummon() { return entityData.get(DATA_TRIAL_SUMMON); }
	@Override public void hemomancy$setTrialSummon(boolean trialSummon) { entityData.set(DATA_TRIAL_SUMMON, trialSummon); }
	@Override public UUID hemomancy$getTrialCasterUUID() { return entityData.get(DATA_TRIAL_CASTER_UUID).orElse(null); }
	@Override public void hemomancy$setTrialCasterUUID(UUID casterUuid) { entityData.set(DATA_TRIAL_CASTER_UUID, Optional.ofNullable(casterUuid)); }
}
