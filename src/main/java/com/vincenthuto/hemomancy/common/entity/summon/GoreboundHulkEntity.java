package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public class GoreboundHulkEntity extends GroundPuppetEntity implements SyncedBoundSummon,
		com.vincenthuto.hemomancy.common.entity.npc.circus.TrueSelf {
	private static final BoundSummonSync SYNC = BoundSummonSync.define(GoreboundHulkEntity.class);

    /** In a professor's Chamber the Hulk is its true self: wilder, a little stronger and quicker (2026-11-17). */
    private static final EntityDataAccessor<Boolean> TRUE_SELF = SynchedEntityData.defineId(GoreboundHulkEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> STRIKE_WINDUP = SynchedEntityData.defineId(GoreboundHulkEntity.class, EntityDataSerializers.INT);
    private UUID strikeTarget;
    public int getStrikeWindup() { return entityData.get(STRIKE_WINDUP); }
    public void setDemonstrationWindup(int tick) { entityData.set(STRIKE_WINDUP, tick); }
    public void beginStrike(net.minecraft.world.entity.LivingEntity target) {
        if (com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealHulkMoves.busy(getUUID())) return;
        if (getStrikeWindup() == 0) { strikeTarget = target.getUUID(); entityData.set(STRIKE_WINDUP, 1); }
    }
    private void tickStrike() {
        int windup = getStrikeWindup();
        if (windup == 0) return;
        if (windup >= 10) {
            if (strikeTarget != null && level() instanceof net.minecraft.server.level.ServerLevel server && server.getEntity(strikeTarget) instanceof net.minecraft.world.entity.LivingEntity target
                    && target.isAlive() && canAttack(target) && isWithinMeleeAttackRange(target) && hasLineOfSight(target)) {
                swing(net.minecraft.world.InteractionHand.MAIN_HAND); doHurtTarget(target);
            }
            entityData.set(STRIKE_WINDUP, 0); strikeTarget = null;
        } else entityData.set(STRIKE_WINDUP, windup + 1);
    }

	public GoreboundHulkEntity(EntityType<? extends Zombie> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return BoundSummonBehavior.definitionAttributes(Zombie.createAttributes(),
				PuppeteerSummonDefinitions.GOREBOUND_HULK)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.55);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new HighStrungMeleeAttackGoal(this, 0.85, false));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
        builder.define(STRIKE_WINDUP, 0);
        builder.define(TRUE_SELF, false);
		SYNC.defineDefaults(builder, "gorebound_hulk");
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) {
			return;
		}
		tickStrike();
		if (hemomancy$isTrialSummon()) {
			BoundSummonBehavior.trialServerTick(this, this);
			return;
		}
		if (BoundSummonBehavior.commonServerTick(this, this)) {
			Optional<Player> owner = BoundSummonBehavior.ownerFor(this, this);
			if (getTarget() == null && owner.isPresent()
					&& BoundSummonBehavior.shouldFollowOwner((net.minecraft.server.level.ServerPlayer) owner.get(), this)) {
				BoundSummonBehavior.followOwnerOnFoot(this, owner.get(), 0.85);
			}
		}
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
	}

	@Override public BoundSummonSync hemomancy$sync() { return SYNC; }

	@Override public boolean isTrueSelf() { return entityData.get(TRUE_SELF); }

	/** Its ordeal moves' poses ({@code OrdealHulkMoves}): an entity event per move, timed by the model from its arrival. */
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

	@Override public void setTrueSelf(boolean trueSelf) {
		entityData.set(TRUE_SELF, trueSelf);
		if (!trueSelf) return;
		var health = getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
		var speed = getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
		if (health != null) health.setBaseValue(health.getBaseValue() * TRUE_SELF_HEALTH);
		if (speed != null) speed.setBaseValue(speed.getBaseValue() * TRUE_SELF_SPEED);
		setHealth(getMaxHealth());
	}

	private static final double TRUE_SELF_HEALTH = 1.25D, TRUE_SELF_SPEED = 1.1D;
}
