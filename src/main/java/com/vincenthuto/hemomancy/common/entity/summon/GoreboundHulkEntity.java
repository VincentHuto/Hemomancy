package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
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

public class GoreboundHulkEntity extends GroundPuppetEntity implements SyncedBoundSummon {
	private static final BoundSummonSync SYNC = BoundSummonSync.define(GoreboundHulkEntity.class);

    private static final EntityDataAccessor<Integer> STRIKE_WINDUP = SynchedEntityData.defineId(GoreboundHulkEntity.class, EntityDataSerializers.INT);
    private UUID strikeTarget;
    public int getStrikeWindup() { return entityData.get(STRIKE_WINDUP); }
    public void setDemonstrationWindup(int tick) { entityData.set(STRIKE_WINDUP, tick); }
    public void beginStrike(net.minecraft.world.entity.LivingEntity target) {
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
					&& BoundSummonBehavior.shouldFollowOwner((net.minecraft.server.level.ServerPlayer) owner.get(), this)
					&& distanceToSqr(owner.get()) > 25.0) {
				getNavigation().moveTo(owner.get(), 0.85);
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
}
