package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

public class SanguineHoundEntity extends Wolf implements BoundPuppeteerSummon {
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_CROSSBAR_UUID =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<String> DATA_SUMMON_NAME =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> DATA_DISMISSAL_TICKS =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_TRIAL_SUMMON =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> DATA_TRIAL_CASTER_UUID =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Boolean> DATA_CUR =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> DATA_CUR_OWNER_UUID =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Integer> DATA_CUR_AGE =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.INT);
	private boolean ruptured;

	public SanguineHoundEntity(EntityType<? extends Wolf> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Wolf.createAttributes()
				.add(Attributes.MAX_HEALTH, 30.0D)
				.add(Attributes.ATTACK_DAMAGE, 6.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.36D);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new HighStrungMeleeAttackGoal(this, 1.25D, true));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_OWNER_UUID, Optional.empty());
		builder.define(DATA_CROSSBAR_UUID, Optional.empty());
		builder.define(DATA_SUMMON_NAME, "sanguine_hound");
		builder.define(DATA_DISMISSAL_TICKS, 0);
		builder.define(DATA_TRIAL_SUMMON, false);
		builder.define(DATA_TRIAL_CASTER_UUID, Optional.empty());
		builder.define(DATA_CUR, false);
		builder.define(DATA_CUR_OWNER_UUID, Optional.empty());
		builder.define(DATA_CUR_AGE, 0);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) return;
		if (isBloodCur()) {
			tickBloodCur();
			return;
		}
		if (hemomancy$isTrialSummon()) {
			BoundSummonBehavior.trialServerTick(this, this);
			return;
		}
		if (BoundSummonBehavior.commonServerTick(this, this)) {
			BoundSummonBehavior.ownerFor(this, this).ifPresent(owner -> {
				if (getTarget() == null && owner instanceof net.minecraft.server.level.ServerPlayer serverOwner
						&& BoundSummonBehavior.shouldFollowOwner(serverOwner, this) && distanceToSqr(owner) > 20.0D) {
					getNavigation().moveTo(owner, 1.15D);
				}
			});
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean hurt = super.hurt(source, amount);
		if (hurt && !level().isClientSide && SanguineHoundRules.shouldRupture(
				isBloodCur(), ruptured, getHealth(), getMaxHealth())) ruptureIntoCurs();
		return hurt;
	}

	public void ruptureIntoCurs() {
		if (ruptured || isBloodCur() || !(level() instanceof ServerLevel serverLevel)) return;
		ruptured = true;
		LivingEntity inheritedTarget = getTarget();
		int count = SanguineHoundRules.curCount(random.nextInt());
		for (int index = 0; index < count; index++) {
			SanguineHoundEntity cur = EntityInit.sanguine_hound.get().create(serverLevel);
			if (cur == null) continue;
			cur.configureCur(hemomancy$getOwnerUUID(), hemomancy$isTrialSummon(), hemomancy$getTrialCasterUUID());
			cur.setPos(getX() + (random.nextDouble() - 0.5D) * 1.4D, getY(),
					getZ() + (random.nextDouble() - 0.5D) * 1.4D);
			if (inheritedTarget != null && cur.canAttack(inheritedTarget)) cur.setTarget(inheritedTarget);
			serverLevel.addFreshEntity(cur);
		}
		serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, getX(), getY() + 0.45D, getZ(),
				36, 0.55D, 0.35D, 0.55D, 0.045D);
		playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.9F, 0.55F);
		discard();
	}

	public boolean isBloodCur() {
		return entityData.get(DATA_CUR);
	}

	private void configureCur(UUID ownerId, boolean trial, UUID trialCaster) {
		entityData.set(DATA_CUR, true);
		entityData.set(DATA_CUR_OWNER_UUID, Optional.ofNullable(ownerId));
		entityData.set(DATA_CUR_AGE, 0);
		hemomancy$setTrialSummon(trial);
		hemomancy$setTrialCasterUUID(trialCaster);
		hemomancy$setSummonName("blood_cur");
		getAttribute(Attributes.MAX_HEALTH).setBaseValue(5.0D);
		getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(2.0D);
		getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.43D);
		getAttribute(Attributes.SCALE).setBaseValue(0.68D);
		setHealth(getMaxHealth());
	}

	private void tickBloodCur() {
		int age = entityData.get(DATA_CUR_AGE) + 1;
		entityData.set(DATA_CUR_AGE, age);
		if (age >= SanguineHoundRules.CUR_LIFETIME_TICKS) {
			dissolveCur();
			return;
		}
		if (getTarget() == null || !getTarget().isAlive() || !canAttack(getTarget())) {
			setTarget(findCurTarget().orElse(null));
		}
	}

	private Optional<LivingEntity> findCurTarget() {
		if (hemomancy$isTrialSummon() && level() instanceof ServerLevel serverLevel) {
			UUID casterId = hemomancy$getTrialCasterUUID();
			Player caster = casterId == null ? null : serverLevel.getPlayerByUUID(casterId);
			if (caster != null && canAttack(caster)) return Optional.of(caster);
		}
		return level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(18.0D), this::canAttack)
				.stream().min(Comparator.comparingDouble(this::distanceToSqr));
	}

	private void dissolveCur() {
		if (level() instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.FALLING_LAVA, getX(), getY() + 0.25D, getZ(),
					12, 0.22D, 0.15D, 0.22D, 0.01D);
		}
		discard();
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (!isBloodCur()) return BoundSummonBehavior.canAttack(this, this, target) && super.canAttack(target);
		if (target == null || !target.isAlive() || target == this) return false;
		if (hemomancy$isTrialSummon()) return target instanceof Player player
				&& !player.isCreative() && !player.isSpectator();
		UUID ownerId = entityData.get(DATA_CUR_OWNER_UUID).orElse(null);
		if (ownerId != null && ownerId.equals(target.getUUID())) return false;
		if (target instanceof BoundPuppeteerSummon bound && ownerId != null
				&& ownerId.equals(bound.hemomancy$getOwnerUUID())) return false;
		return target instanceof Enemy;
	}

	@Override
	public InteractionResult mobInteract(Player player, InteractionHand hand) {
		return InteractionResult.PASS;
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
		tag.putBoolean("BloodCur", isBloodCur());
		entityData.get(DATA_CUR_OWNER_UUID).ifPresent(uuid -> tag.putUUID("BloodCurOwner", uuid));
		tag.putInt("BloodCurAge", entityData.get(DATA_CUR_AGE));
		tag.putBoolean("Ruptured", ruptured);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		BoundSummonBehavior.load(this, tag);
		entityData.set(DATA_CUR, tag.getBoolean("BloodCur"));
		entityData.set(DATA_CUR_OWNER_UUID, tag.hasUUID("BloodCurOwner")
				? Optional.of(tag.getUUID("BloodCurOwner")) : Optional.empty());
		entityData.set(DATA_CUR_AGE, tag.getInt("BloodCurAge"));
		ruptured = tag.getBoolean("Ruptured");
	}

	@Override public UUID hemomancy$getOwnerUUID() { return entityData.get(DATA_OWNER_UUID).orElse(null); }
	@Override public void hemomancy$setOwnerUUID(UUID ownerUuid) { entityData.set(DATA_OWNER_UUID, Optional.ofNullable(ownerUuid)); }
	@Override public UUID hemomancy$getCrossbarUUID() { return entityData.get(DATA_CROSSBAR_UUID).orElse(null); }
	@Override public void hemomancy$setCrossbarUUID(UUID crossbarUuid) { entityData.set(DATA_CROSSBAR_UUID, Optional.ofNullable(crossbarUuid)); }
	@Override public String hemomancy$getSummonName() { return entityData.get(DATA_SUMMON_NAME); }
	@Override public void hemomancy$setSummonName(String summonName) { entityData.set(DATA_SUMMON_NAME, summonName == null ? "" : summonName); }
	@Override public int hemomancy$getDismissalTicks() { return entityData.get(DATA_DISMISSAL_TICKS); }
	@Override public void hemomancy$setDismissalTicks(int ticks) { entityData.set(DATA_DISMISSAL_TICKS, Math.max(0, ticks)); }
	@Override public boolean hemomancy$isTrialSummon() { return entityData.get(DATA_TRIAL_SUMMON); }
	@Override public void hemomancy$setTrialSummon(boolean trialSummon) { entityData.set(DATA_TRIAL_SUMMON, trialSummon); }
	@Override public UUID hemomancy$getTrialCasterUUID() { return entityData.get(DATA_TRIAL_CASTER_UUID).orElse(null); }
	@Override public void hemomancy$setTrialCasterUUID(UUID casterUuid) { entityData.set(DATA_TRIAL_CASTER_UUID, Optional.ofNullable(casterUuid)); }
}
