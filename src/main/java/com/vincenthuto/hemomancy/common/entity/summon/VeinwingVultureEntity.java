package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.entity.projectile.VeinwingFeatherEntity;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public class VeinwingVultureEntity extends Vex implements SyncedBoundSummon,
		com.vincenthuto.hemomancy.common.entity.npc.circus.TrueSelf {
	private static final int ATTACK_COOLDOWN_TICKS = 15;
	/** In the Acrobat's Chamber the Vulture is its true self: wilder, larger, a little stronger and quicker (2026-11-17). */
	private static final EntityDataAccessor<Boolean> TRUE_SELF = SynchedEntityData.defineId(VeinwingVultureEntity.class,
			EntityDataSerializers.BOOLEAN);
	private static final double TRUE_SELF_HEALTH = 1.3D, TRUE_SELF_SPEED = 1.1D;
	/** The true self is drawn and struck at twice the size. */
	public static final float TRUE_SELF_SIZE = 2.0F;
	private static final int VOLLEY_COOLDOWN_TICKS = 50;
	private static final double VOLLEY_MIN_RANGE = 3.5;
	private static final double VOLLEY_MAX_RANGE = 14.0;
	private static final BoundSummonSync SYNC = BoundSummonSync.define(VeinwingVultureEntity.class);
	private int attackCooldown;
	private int volleyCooldown;

	public VeinwingVultureEntity(EntityType<? extends Vex> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return BoundSummonBehavior.definitionAttributes(Vex.createAttributes(),
				PuppeteerSummonDefinitions.VEINWING_VULTURE)
				.add(Attributes.FLYING_SPEED, 0.43);
	}

	@Override
	protected void registerGoals() {
		// Vex navigation is move-controller driven; combat is handled in tickVultureCombat.
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(TRUE_SELF, false);
		SYNC.defineDefaults(builder, "veinwing_vulture");
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
		if (com.vincenthuto.hemomancy.common.encounter.ordeal.VultureOrdealMoves.busy(getUUID())) {
			// One of its ordeal moves has the body: hold still for it, no blows of its own.
			getMoveControl().setWantedPosition(getX(), getY(), getZ(), 0.0D);
			return;
		}
		Vec3 destination = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
		if (isTrueSelf()) {
			// The true self circles its quarry, swooping in and out: a wide ring that tightens to striking range.
			double angle = tickCount * 0.07D;
			double radius = 2.0D + 1.4D * (0.5D + 0.5D * Math.sin(tickCount * 0.05D));
			destination = target.position().add(Math.cos(angle) * radius, 1.6D + 0.8D * (0.5D + 0.5D * Math.sin(tickCount * 0.11D)),
					Math.sin(angle) * radius);
		}
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

	@Override public BoundSummonSync hemomancy$sync() { return SYNC; }

	@Override
	public boolean isTrueSelf() {
		return entityData.get(TRUE_SELF);
	}

	@Override
	public void setTrueSelf(boolean trueSelf) {
		entityData.set(TRUE_SELF, trueSelf);
		refreshDimensions();
		if (!trueSelf) return;
		var health = getAttribute(Attributes.MAX_HEALTH);
		var speed = getAttribute(Attributes.FLYING_SPEED);
		if (health != null) health.setBaseValue(health.getBaseValue() * TRUE_SELF_HEALTH);
		if (speed != null) speed.setBaseValue(speed.getBaseValue() * TRUE_SELF_SPEED);
		setHealth(getMaxHealth());
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		if (TRUE_SELF.equals(key)) refreshDimensions();
		super.onSyncedDataUpdated(key);
	}

	@Override
	public net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose) {
		var dimensions = super.getDefaultDimensions(pose);
		return isTrueSelf() ? dimensions.scale(TRUE_SELF_SIZE) : dimensions;
	}

	/** Its ordeal moves' poses ({@code VultureOrdealMoves}): an entity event per move, timed by the model from its arrival. */
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
