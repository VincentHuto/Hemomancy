package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;
import java.util.UUID;

public final class CinderBellowsEntity extends GroundPuppetEntity implements SyncedBoundSummon,
		com.vincenthuto.hemomancy.common.entity.npc.circus.TrueSelf {
	private static final BoundSummonSync SYNC = BoundSummonSync.define(CinderBellowsEntity.class);

    private static final EntityDataAccessor<Integer> BREATH_CYCLE = SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.INT);
    /** In the Fire Breather's Chamber the Bellows is its true self: wilder, tougher and a little quicker (2026-11-17). */
    private static final EntityDataAccessor<Boolean> TRUE_SELF = SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.BOOLEAN);
    private static final double TRUE_SELF_HEALTH = 1.5D, TRUE_SELF_SPEED = 1.1D;
    private Vec3 breathDirection = Vec3.ZERO;
    public CinderBellowsEntity(EntityType<? extends Zombie> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder setAttributes() {
        return BoundSummonBehavior.definitionAttributes(Zombie.createAttributes(),
                PuppeteerSummonDefinitions.CINDER_BELLOWS);
    }
    @Override protected void registerGoals() { goalSelector.addGoal(0, new FloatGoal(this)); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        SYNC.defineDefaults(builder, "cinder_bellows");
        builder.define(BREATH_CYCLE, 0);
        builder.define(TRUE_SELF, false);
    }
    public int getBreathCycle() { return entityData.get(BREATH_CYCLE); }
    public void setDemonstrationCycle(int cycle) { entityData.set(BREATH_CYCLE, cycle); }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (hemomancy$isTrialSummon()) BoundSummonBehavior.trialServerTick(this, this);
        else if (!BoundSummonBehavior.commonServerTick(this, this)) return;
        // In its ordeal a move of its own takes over from the breathing count while it runs.
        if (com.vincenthuto.hemomancy.common.encounter.ordeal.CinderBellowsOrdealMoves.busy(getUUID())) {
            entityData.set(BREATH_CYCLE, 0);
            return;
        }
        LivingEntity target = getTarget();
        if (isInWaterOrBubble() || target == null || !canAttack(target)) {
            entityData.set(BREATH_CYCLE, 0);
            if (target == null) BoundSummonBehavior.ownerFor(this, this).ifPresent(owner -> {
                if (BoundSummonBehavior.shouldFollowOwner((net.minecraft.server.level.ServerPlayer) owner, this))
                    BoundSummonBehavior.followOwnerOnFoot(this, owner, 1);
            });
            return;
        }
        if (getBreathCycle() == 0 && (distanceToSqr(target) > 25 || !hasLineOfSight(target))) {
            getNavigation().moveTo(target, 1); return;
        }
        getNavigation().stop();
        int cycle = getBreathCycle() % 100 + 1;
        if (cycle == 1) breathDirection = target.getBoundingBox().getCenter().subtract(getEyePosition()).normalize();
        entityData.set(BREATH_CYCLE, cycle == 100 ? 0 : cycle);
        setYRot((float) (Math.atan2(-breathDirection.x, breathDirection.z) * 180 / Math.PI));
        setYHeadRot(getYRot());
        // Pitch is synced so clients draw the Flammeus breath flames down the same cone the damage uses.
        setXRot((float) (-Math.asin(Mth.clamp(breathDirection.y, -1, 1)) * 180 / Math.PI));
        if (CinderBellowsRules.phase(cycle) == CinderBellowsRules.Phase.BREATHE) {
            if (CinderBellowsRules.damagePulse(cycle)) {
                var type = registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                        .getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Hemomancy.rloc("cinder_breath")));
                Entity owner = BoundSummonBehavior.ownerFor(this, this).map(Entity.class::cast).orElse(this);
                for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(5), this::canAttack)) {
                    Vec3 delta = victim.getBoundingBox().getCenter().subtract(getEyePosition());
                    if (CinderBellowsRules.inCone(delta.length(), delta.normalize().dot(breathDirection)) && hasLineOfSight(victim))
                        victim.hurt(new DamageSource(type, this, owner), (float) getAttributeValue(Attributes.ATTACK_DAMAGE) / 2);
                }
            }
        }
    }
    @Override public boolean canAttack(LivingEntity target) { return BoundSummonBehavior.canAttack(this, this, target) && super.canAttack(target); }
    @Override protected boolean shouldDespawnInPeaceful() { return PuppeteerSummonRules.shouldDespawnInPeaceful(hemomancy$isTrialSummon(), hemomancy$getOwnerUUID()); }
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

	/** Whether its ordinary breath is burning now (its inhale and recovery can be cut short by a move). */
	public boolean breathBurning() {
		int cycle = getBreathCycle();
		return cycle > 0 && CinderBellowsRules.phase(cycle) == CinderBellowsRules.Phase.BREATHE;
	}

	@Override public boolean isTrueSelf() { return entityData.get(TRUE_SELF); }

	@Override public void setTrueSelf(boolean trueSelf) {
		entityData.set(TRUE_SELF, trueSelf);
		if (!trueSelf) return;
		var health = getAttribute(Attributes.MAX_HEALTH);
		var speed = getAttribute(Attributes.MOVEMENT_SPEED);
		if (health != null) health.setBaseValue(health.getBaseValue() * TRUE_SELF_HEALTH);
		if (speed != null) speed.setBaseValue(speed.getBaseValue() * TRUE_SELF_SPEED);
		setHealth(getMaxHealth());
	}

	/** Its ordeal moves' poses ({@code CinderBellowsOrdealMoves}): an entity event per move, timed by the model from its arrival. */
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
