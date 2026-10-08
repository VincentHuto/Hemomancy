package com.vincenthuto.hemomancy.common.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.circus.CinderBellowsRules;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import java.util.Optional;
import java.util.UUID;

public final class CinderBellowsEntity extends GroundPuppetEntity implements BoundPuppeteerSummon {
	private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
			SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<Optional<UUID>> DATA_CROSSBAR_UUID =
			SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.OPTIONAL_UUID);
	private static final EntityDataAccessor<String> DATA_SUMMON_NAME =
			SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.STRING);
	private static final EntityDataAccessor<Integer> DATA_DISMISSAL_TICKS =
			SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_TRIAL_SUMMON =
			SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Optional<UUID>> DATA_TRIAL_CASTER_UUID =
			SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final EntityDataAccessor<Integer> BREATH_CYCLE = SynchedEntityData.defineId(CinderBellowsEntity.class, EntityDataSerializers.INT);
    private Vec3 breathDirection = Vec3.ZERO;
    public CinderBellowsEntity(EntityType<? extends Zombie> type, Level level) { super(type, level); }
    public static AttributeSupplier.Builder setAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.MOVEMENT_SPEED, .24);
    }
    @Override protected void registerGoals() { goalSelector.addGoal(0, new FloatGoal(this)); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty()); builder.define(DATA_CROSSBAR_UUID, Optional.empty());
        builder.define(DATA_SUMMON_NAME, "cinder_bellows"); builder.define(DATA_DISMISSAL_TICKS, 0);
        builder.define(DATA_TRIAL_SUMMON, false); builder.define(DATA_TRIAL_CASTER_UUID, Optional.empty());
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
        if (CinderBellowsRules.phase(cycle) == CinderBellowsRules.Phase.BREATHE) {
            ServerLevel server = (ServerLevel) level();
            server.sendParticles(ParticleTypes.FLAME, getX(), getEyeY(), getZ(), 4,
                    breathDirection.x * .6, .08, breathDirection.z * .6, .04);
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
