package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import com.vincenthuto.hemomancy.common.entity.projectile.BloodShotEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class MarrowSpitterEntity extends Skeleton implements BoundPuppeteerSummon {
	private static final int SHOT_INTERVAL_TICKS = 35;
	private static final double ORBIT_RADIUS = 3.25;
	private static final double ORBIT_HEIGHT = 1.8;
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
			SynchedEntityData.defineId(MarrowSpitterEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_CROSSBAR_UUID =
			SynchedEntityData.defineId(MarrowSpitterEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<String> DATA_SUMMON_NAME =
			SynchedEntityData.defineId(MarrowSpitterEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> DATA_DISMISSAL_TICKS =
			SynchedEntityData.defineId(MarrowSpitterEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_TRIAL_SUMMON =
			SynchedEntityData.defineId(MarrowSpitterEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> DATA_TRIAL_CASTER_UUID =
			SynchedEntityData.defineId(MarrowSpitterEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private int shotCooldown = 10;

	public MarrowSpitterEntity(EntityType<? extends Skeleton> type, Level level) {
		super(type, level);
		setNoGravity(true);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Skeleton.createAttributes()
				.add(Attributes.MAX_HEALTH, 22.0)
				.add(Attributes.ATTACK_DAMAGE, 5.0)
				.add(Attributes.MOVEMENT_SPEED, 0.24);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_OWNER_UUID, Optional.empty());
		builder.define(DATA_CROSSBAR_UUID, Optional.empty());
		builder.define(DATA_SUMMON_NAME, "marrow_spitter");
		builder.define(DATA_DISMISSAL_TICKS, 0);
		builder.define(DATA_TRIAL_SUMMON, false);
		builder.define(DATA_TRIAL_CASTER_UUID, Optional.empty());
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
										MobSpawnType reason, @Nullable SpawnGroupData spawnData) {
		setNoGravity(true);
		SpawnGroupData data = super.finalizeSpawn(level, difficulty, reason, spawnData);
		setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
		return data;
	}

	@Override
	public void tick() {
		super.tick();
		setNoGravity(true);
		if (level().isClientSide) {
			return;
		}
		if (hemomancy$isTrialSummon()) {
			BoundSummonBehavior.trialServerTick(this, this);
			LivingEntity target = getTarget();
			if (target != null) tickHoverAround(target, 6.0, 2.2);
			tickSentryFire();
			return;
		}
		if (BoundSummonBehavior.commonServerTick(this, this)) {
			Optional<Player> owner = BoundSummonBehavior.ownerFor(this, this);
			getNavigation().stop();
			if (owner.isPresent()
					&& BoundSummonBehavior.shouldFollowOwner((net.minecraft.server.level.ServerPlayer) owner.get(), this)) {
				tickBoundOrbit(owner.get());
			}
			tickSentryFire();
		}
	}

	private void tickBoundOrbit(Player owner) {
		tickHoverAround(owner, ORBIT_RADIUS, ORBIT_HEIGHT);
	}

	private void tickHoverAround(LivingEntity anchor, double radius, double height) {
		double phase = tickCount * 0.025 + Math.floorMod(getUUID().hashCode(), 360) * (Math.PI / 180.0);
		double bob = Math.sin(tickCount * 0.055 + phase) * 0.22;
		Vec3 desired = anchor.position().add(Math.cos(phase) * radius, height + bob, Math.sin(phase) * radius);
		Vec3 correction = desired.subtract(position());
		Vec3 velocity = getDeltaMovement().scale(0.78).add(correction.scale(0.035));
		double speed = velocity.length();
		if (speed > 0.18) velocity = velocity.scale(0.18 / speed);
		setDeltaMovement(velocity);
		hurtMarked = true;
	}

	private void tickSentryFire() {
		if (shotCooldown > 0) shotCooldown--;
		LivingEntity target = getTarget();
		if (target == null || !target.isAlive() || !canAttack(target)) return;
		getLookControl().setLookAt(target, 30.0F, 30.0F);
		if (shotCooldown <= 0 && distanceToSqr(target) <= 24.0 * 24.0) {
			performRangedAttack(target, 1.0F);
			shotCooldown = SHOT_INTERVAL_TICKS;
		}
	}

	@Override
	public void performRangedAttack(LivingEntity target, float distanceFactor) {
		BloodShotEntity shot = new BloodShotEntity(level(), this);
		double dx = target.getX() - getX();
		double dz = target.getZ() - getZ();
		double horizontal = Math.sqrt(dx * dx + dz * dz);
		double dy = target.getY(0.55) - shot.getY() + horizontal * 0.06;
		shot.setBaseDamage(Math.max(2.0, getAttributeValue(Attributes.ATTACK_DAMAGE)));
		shot.shoot(dx, dy, dz, 1.7F, 2.5F);
		level().addFreshEntity(shot);
		level().playSound(null, blockPosition(), SoundEvents.LLAMA_SPIT, SoundSource.HOSTILE, 0.65F, 0.75F);
	}

	@Override
	protected boolean isSunBurnTick() {
		return false;
	}

	@Override
	public boolean canAttack(net.minecraft.world.entity.LivingEntity target) {
		return BoundSummonBehavior.canAttack(this, this, target) && super.canAttack(target);
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
		setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
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
