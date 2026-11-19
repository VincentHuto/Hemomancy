package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonRules;
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

public class SanguineHoundEntity extends Wolf implements SyncedBoundSummon,
		com.vincenthuto.hemomancy.common.entity.npc.circus.TrueSelf {
	private static final BoundSummonSync SYNC = BoundSummonSync.define(SanguineHoundEntity.class);
	private static final EntityDataAccessor<Boolean> DATA_CUR =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> DATA_CUR_OWNER_UUID =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Integer> DATA_CUR_AGE =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.INT);
	/** In the Beast Tamer's Chamber the Hound is its true self: bigger, wilder, and it never ruptures (2026-11-17). */
	private static final EntityDataAccessor<Boolean> TRUE_SELF =
			SynchedEntityData.defineId(SanguineHoundEntity.class, EntityDataSerializers.BOOLEAN);
	private static final double TRUE_SELF_HEALTH = 1.5D, TRUE_SELF_SPEED = 1.15D, TRUE_SELF_SCALE = 1.5D;
	private boolean ruptured;

	public SanguineHoundEntity(EntityType<? extends Wolf> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return BoundSummonBehavior.definitionAttributes(Wolf.createAttributes(),
				PuppeteerSummonDefinitions.SANGUINE_HOUND);
	}

	@Override
	protected void registerGoals() {
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(2, new HighStrungMeleeAttackGoal(this, 1.25D, true));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		SYNC.defineDefaults(builder, "sanguine_hound");
		builder.define(DATA_CUR, false);
		builder.define(DATA_CUR_OWNER_UUID, Optional.empty());
		builder.define(DATA_CUR_AGE, 0);
		builder.define(TRUE_SELF, false);
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
						&& BoundSummonBehavior.shouldFollowOwner(serverOwner, this)) {
					BoundSummonBehavior.followOwnerOnFoot(this, owner, 1.15D);
				}
			});
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		boolean hurt = super.hurt(source, amount);
		// Its true self holds together: a rupture would leave the ordeal without its puppet.
		if (hurt && !level().isClientSide && !isTrueSelf() && SanguineHoundRules.shouldRupture(
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
            cur.hemomancy$setCrossbarUUID(hemomancy$getCrossbarUUID());
			cur.setPos(getX() + (random.nextDouble() - 0.5D) * 1.4D, getY(),
					getZ() + (random.nextDouble() - 0.5D) * 1.4D);
			if (inheritedTarget != null && cur.canAttack(inheritedTarget)) cur.setTarget(inheritedTarget);
			serverLevel.addFreshEntity(cur);
		}
		serverLevel.sendParticles(HemoParticleData.bloodCell(ParticleColor.BLOOD), getX(), getY() + 0.45D, getZ(),
				24, 0.55D, 0.35D, 0.55D, 0.045D);
		serverLevel.sendParticles(HemoParticleData.glow(ParticleColor.BLOOD), getX(), getY() + 0.45D, getZ(),
				12, 0.45D, 0.3D, 0.45D, 0.02D);
		playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.9F, 0.55F);
		discard();
	}

	public UUID curOwner() { return entityData.get(DATA_CUR_OWNER_UUID).orElse(null); }

	public boolean isBloodCur() {
		return entityData.get(DATA_CUR);
	}

	/** A Blood Cur loosed by the Evening Star's borrowed Hound attack: a trial cur that hunts his ordeal owner. */
	public void configureEncounterCur(UUID ordealOwner) {
		configureCur(null, true, ordealOwner);
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
			serverLevel.sendParticles(HemoParticleData.bloodCell(ParticleColor.BLOOD), getX(), getY() + 0.25D, getZ(),
					10, 0.22D, 0.15D, 0.22D, 0.01D);
		}
		discard();
	}

	@Override
	public boolean canAttack(LivingEntity target) {
		if (!isBloodCur()) return BoundSummonBehavior.canAttack(this, this, target) && super.canAttack(target);
		if (target == null || !target.isAlive() || target == this) return false;
		if (target instanceof net.minecraft.world.entity.Mob mob && com.vincenthuto.hemomancy.common.mission.circus.CircusDemonstrations.isStage(mob)) return false;
		if (target instanceof SanguineHoundEntity cur && cur.isBloodCur() && cur.curOwner() != null) return false;
		if (target.getPersistentData().hasUUID(com.vincenthuto.hemomancy.common.mission.circus.CircusPracticalController.EXAM)
				&& !target.getPersistentData().getUUID(com.vincenthuto.hemomancy.common.mission.circus.CircusPracticalController.EXAM).equals(curOwner())) return false;
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

	@Override public BoundSummonSync hemomancy$sync() { return SYNC; }

	@Override
	public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
		// Its ordinary bite waits while one of its ordeal moves runs.
		if (com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealHoundMoves.busy(getUUID())) return false;
		return super.doHurtTarget(target);
	}

	@Override public boolean isTrueSelf() { return entityData.get(TRUE_SELF); }

	@Override public void setTrueSelf(boolean trueSelf) {
		entityData.set(TRUE_SELF, trueSelf);
		if (!trueSelf) return;
		var health = getAttribute(Attributes.MAX_HEALTH);
		var speed = getAttribute(Attributes.MOVEMENT_SPEED);
		var scale = getAttribute(Attributes.SCALE);
		if (health != null) health.setBaseValue(health.getBaseValue() * TRUE_SELF_HEALTH);
		if (speed != null) speed.setBaseValue(speed.getBaseValue() * TRUE_SELF_SPEED);
		if (scale != null) scale.setBaseValue(TRUE_SELF_SCALE);
		setHealth(getMaxHealth());
	}

	/** Its ordeal moves' poses ({@code OrdealHoundMoves}): an entity event per move, timed by the model from its arrival. */
	private static final byte ORDEAL_MOVE_EVENT = 80;
	private static final int ORDEAL_MOVES = 4;
	private int ordealMove = -1;
	private int ordealMoveStart;

	/** Starts move {@code kind}'s pose on every watching client, or stops it with -1. */
	public void ordealMove(int kind) {
		level().broadcastEntityEvent(this, (byte) (ORDEAL_MOVE_EVENT + (kind < 0 ? ORDEAL_MOVES : kind)));
	}

	public int ordealMove() {
		return ordealMove;
	}

	public float ordealMoveAge(float partialTick) {
		return tickCount - ordealMoveStart + partialTick;
	}

	@Override
	public void handleEntityEvent(byte id) {
		if (id >= ORDEAL_MOVE_EVENT && id <= ORDEAL_MOVE_EVENT + ORDEAL_MOVES) {
			ordealMove = id - ORDEAL_MOVE_EVENT == ORDEAL_MOVES ? -1 : id - ORDEAL_MOVE_EVENT;
			ordealMoveStart = tickCount;
		} else {
			super.handleEntityEvent(id);
		}
	}
}
