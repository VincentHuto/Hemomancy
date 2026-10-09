package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.circus.CinderBellowsRules;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
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

public final class CinderBellowsEntity extends GroundPuppetEntity implements SyncedBoundSummon {
	private static final BoundSummonSync SYNC = BoundSummonSync.define(CinderBellowsEntity.class);

    private static final EntityDataAccessor<Integer> BREATH_CYCLE = SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.INT);
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
    }
    public int getBreathCycle() { return entityData.get(BREATH_CYCLE); }
    public void setDemonstrationCycle(int cycle) { entityData.set(BREATH_CYCLE, cycle); }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) return;
        if (hemomancy$isTrialSummon()) BoundSummonBehavior.trialServerTick(this, this);
        else if (!BoundSummonBehavior.commonServerTick(this, this)) return;
        LivingEntity target = getTarget();
        if (isInWaterOrBubble() || target == null || !canAttack(target)) {
            entityData.set(BREATH_CYCLE, 0);
            if (target == null) BoundSummonBehavior.ownerFor(this, this).ifPresent(owner -> {
                if (BoundSummonBehavior.shouldFollowOwner((net.minecraft.server.level.ServerPlayer) owner, this))
                    getNavigation().moveTo(owner, 1);
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
}
