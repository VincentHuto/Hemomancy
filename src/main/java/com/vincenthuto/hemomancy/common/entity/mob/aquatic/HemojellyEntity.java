package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import java.util.Comparator;
import java.util.EnumSet;

/** Surface bell with a submerged, short-lived feeding net. */
public class HemojellyEntity extends WaterAnimal {
    private static final EntityDataAccessor<Integer> CAPTURE_TICKS = SynchedEntityData.defineId(HemojellyEntity.class, EntityDataSerializers.INT);
    public final AnimationState idleAnimationState = new AnimationState();
    private LivingEntity prey;
    private int feedingCooldown;

    public HemojellyEntity(EntityType<? extends HemojellyEntity> type, Level level) {
        super(type, level);
        moveControl = new SmoothSwimmingMoveControl(this, 45, 8, .04F, .02F, false);
        lookControl = new SmoothSwimmingLookControl(this, 8);
    }
    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6).add(Attributes.MOVEMENT_SPEED, .15);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(CAPTURE_TICKS, 0);
    }
    public int captureTicks() { return entityData.get(CAPTURE_TICKS); }
    @Override protected PathNavigation createNavigation(Level level) { return new WaterBoundPathNavigation(this, level); }
    @Override protected void registerGoals() { goalSelector.addGoal(4, new SurfaceDriftGoal()); }
    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }
    @Override public int getMaxSpawnClusterSize() { return 2; }
    public static boolean canSpawnHere(EntityType<HemojellyEntity> type, LevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getFluidState(pos).is(FluidTags.WATER)
                && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).isAir();
    }
    private double surfaceY(BlockPos pos) {
        var cursor = pos.mutable();
        for (int i = 0; i < 32 && level().hasChunkAt(cursor); i++, cursor.move(0, 1, 0)) {
            if (!level().getFluidState(cursor).is(FluidTags.WATER))
                return level().getBlockState(cursor).isAir() ? cursor.getY() : Double.NaN;
        }
        return Double.NaN;
    }
    @Override public void travel(Vec3 input) {
        if (isEffectiveAi() && isInWater()) {
            moveRelative(.025F, input);
            double surface = surfaceY(blockPosition());
            double rise = Double.isNaN(surface) ? .015 : Math.clamp((surface - .625 - getY()) * .12, -.04, .04);
            // Navigation chooses a horizontal destination; buoyancy owns vertical movement.
            setDeltaMovement(new Vec3(getDeltaMovement().x, rise, getDeltaMovement().z));
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(.9));
        } else super.travel(input);
    }
    private AABB feedingNet() {
        double bell = getY() + .625;
        return new AABB(getX()-.6, bell-2.1, getZ()-.6, getX()+.6, bell-.1, getZ()+.6);
    }
    private boolean edible(LivingEntity fish) {
        return fish instanceof AbstractFish && fish.isAlive() && !fish.isRemoved() && fish.level() == level()
                && fish.isInWater() && fish.getBbWidth() < .7F && feedingNet().intersects(fish.getBoundingBox()) && hasLineOfSight(fish);
    }
    private void releasePrey() { prey = null; entityData.set(CAPTURE_TICKS, 0); }
    @Override public void tick() {
        super.tick();
        setNoGravity(isInWater());
        if (level().isClientSide()) { idleAnimationState.startIfStopped(tickCount); return; }
        if (feedingCooldown > 0) feedingCooldown--;
        if (!isAlive() || !isInWater() || isNoAi()) { releasePrey(); return; }
        if (prey != null) {
            if (!edible(prey)) { releasePrey(); feedingCooldown = 100; return; }
            navigation.stop();
            prey.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 1));
            Vec3 pull = new Vec3(getX(), getY()-.7, getZ()).subtract(prey.position());
            if (pull.lengthSqr() > .01) {
                Vec3 motion = prey.getDeltaMovement().add(pull.normalize().scale(.02));
                prey.setDeltaMovement(motion.lengthSqr() > .0064 ? motion.normalize().scale(.08) : motion);
            }
            entityData.set(CAPTURE_TICKS, captureTicks()-1);
            if (captureTicks() <= 0) { prey.hurt(damageSources().mobAttack(this), 2); releasePrey(); feedingCooldown = 100; }
        } else if (feedingCooldown == 0 && tickCount % 10 == 0) {
            prey = level().getEntitiesOfClass(LivingEntity.class, feedingNet(), this::edible).stream()
                    .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
            if (prey != null) { navigation.stop(); entityData.set(CAPTURE_TICKS, 30); }
        }
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putInt("FeedingCooldown", feedingCooldown);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); releasePrey(); feedingCooldown = Math.max(100, tag.getInt("FeedingCooldown"));
    }
    @Override protected int calculateFallDamage(float distance, float multiplier) { return 0; }
    @Override protected SoundEvent getAmbientSound() { return SoundInit.ENTITY_HEMOJELLY_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundInit.ENTITY_HEMOJELLY_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return SoundInit.ENTITY_HEMOJELLY_DEATH.get(); }
    @Override protected float getSoundVolume() { return .25F; }
    @Override public AABB getBoundingBoxForCulling() { return getBoundingBox().expandTowards(0,-2.1,0); }
    private final class SurfaceDriftGoal extends Goal {
        private Vec3 destination;
        SurfaceDriftGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            if (!isInWater() || prey != null || random.nextInt(80) != 0) return false;
            var pos = blockPosition().offset(random.nextInt(9)-4,0,random.nextInt(9)-4);
            if (!level().getFluidState(pos).is(FluidTags.WATER)) return false;
            double surface = surfaceY(pos);
            if (Double.isNaN(surface)) return false;
            destination = new Vec3(pos.getX()+.5,surface-.625,pos.getZ()+.5); return true;
        }
        @Override public void start() { navigation.moveTo(destination.x,destination.y,destination.z,.5); }
        @Override public boolean canContinueToUse() { return prey == null && isInWater() && !navigation.isDone(); }
        @Override public void stop() { navigation.stop(); }
    }
}
