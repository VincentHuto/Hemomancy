package com.vincenthuto.hemomancy.common.entity.mob.arthropod;

import com.vincenthuto.hemomancy.common.init.BiomeInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class PhlegethonticBombardier extends PathfinderMob implements NeutralMob {
    public static final int WINDUP_DURATION = 30;
    public static final int AIM_TRACKING_TICKS = 22;
    public static final int FLAME_DURATION = 16;
    public static final double MIN_FLAME_RANGE = 3.0;
    public static final double MAX_FLAME_RANGE = 10.0;
    public static final int FULL_CHARGE = 240;
    private static final UniformInt ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);

    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> STATE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(PhlegethonticBombardier.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> STATE_TICK =
            net.minecraft.network.syncher.SynchedEntityData.defineId(PhlegethonticBombardier.class,
                    net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Float> LOCKED_YAW =
            net.minecraft.network.syncher.SynchedEntityData.defineId(PhlegethonticBombardier.class,
                    net.minecraft.network.syncher.EntityDataSerializers.FLOAT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Float> LOCKED_PITCH =
            net.minecraft.network.syncher.SynchedEntityData.defineId(PhlegethonticBombardier.class,
                    net.minecraft.network.syncher.EntityDataSerializers.FLOAT);

    public final AnimationState presentationAnimationState = new AnimationState();
    private int remainingPersistentAngerTime;
    @Nullable private UUID persistentAngerTarget;
    @Nullable private BlockPos territoryAnchor;
    private int chemicalCharge = FULL_CHARGE;
    private int peacefulTicks;
    private int warningClearTicks;
    private int habitatCheckTicks;
    private int ticksToGraze;

    public PhlegethonticBombardier(EntityType<? extends PhlegethonticBombardier> type, Level level) {
        super(type, level);
        this.xpReward = 5;
        this.ticksToGraze = 200 + random.nextInt(201);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 28.0)
                .add(Attributes.ARMOR, 8.0)
                .add(Attributes.MOVEMENT_SPEED, .22)
                .add(Attributes.FOLLOW_RANGE, 20.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, .45);
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, BombardierState.IDLE.ordinal());
        builder.define(STATE_TICK, 0);
        builder.define(LOCKED_YAW, 0.0F);
        builder.define(LOCKED_PITCH, 0.0F);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(5, new RandomStrollGoal(this, .8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new ResetUniversalAngerTargetGoal<>(this, true));
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            presentationAnimationState.startIfStopped(tickCount);
            return;
        }
        if (level() instanceof ServerLevel server) updatePersistentAnger(server, true);
        maintainTerritory();
        returnToTerritory();
        tickBehaviour();
    }

    private void maintainTerritory() {
        if (territoryAnchor != null && BombardierHabitat.isGrowth(level(), territoryAnchor)) return;
        if (++habitatCheckTicks < 100) return;
        habitatCheckTicks = 0;
        territoryAnchor = BombardierHabitat.findNearestSupport(level(), blockPosition(), 12, 6)
                .flatMap(feet -> BombardierHabitat.resolveComponentAnchor(level(), feet.below(), 256)).orElse(null);
    }

    private void returnToTerritory() {
        BombardierState state = getBombardierState();
        if (territoryAnchor == null || state == BombardierState.WINDUP || state == BombardierState.FIRING
                || distanceToSqr(Vec3.atCenterOf(territoryAnchor)) <= 14 * 14 || !navigation.isDone()) return;
        BombardierHabitat.findNearestSupport(level(), territoryAnchor.above(), 12, 6)
                .ifPresent(pos -> navigation.moveTo(pos.getX() + .5, pos.getY(), pos.getZ() + .5, 1.15));
    }

    private void tickBehaviour() {
        LivingEntity target = getTarget();
        if (target != null && (!target.isAlive() || !isAngryAt(target))) {
            setTarget(null);
            target = null;
        }
        switch (getBombardierState()) {
            case WINDUP -> tickWindup(target);
            case FIRING -> tickFiring();
            case COOLING -> tickCooling();
            case CAMOUFLAGED -> tickCamouflaged(target);
            case WARNING -> tickWarning(target);
            case GRAZING -> tickGrazing(target);
            case IDLE -> tickIdle(target);
        }
    }

    private void tickIdle(@Nullable LivingEntity target) {
        if (canBeginAttack(target)) { beginWindup(); return; }
        if (target == null && BombardierHabitat.isValidSupport(level(), blockPosition())
                && navigation.isDone() && getLastHurtByMob() == null && ++peacefulTicks >= 80) {
            setBombardierState(BombardierState.CAMOUFLAGED);
        } else if (target != null || !navigation.isDone()) peacefulTicks = 0;
    }

    private void tickCamouflaged(@Nullable LivingEntity target) {
        if (canBeginAttack(target)) { beginWindup(); return; }
        Player nearby = level().getNearestPlayer(this, 5.0);
        if (nearby != null && !nearby.isCreative() && !nearby.isSpectator()) {
            setBombardierState(BombardierState.WARNING);
            playSound(SoundEvents.SILVERFISH_AMBIENT, .65F, .7F);
        } else if (--ticksToGraze <= 0) {
            setBombardierState(BombardierState.GRAZING);
        }
    }

    private void tickWarning(@Nullable LivingEntity target) {
        if (canBeginAttack(target)) { beginWindup(); return; }
        Player nearby = level().getNearestPlayer(this, 5.0);
        if (nearby != null && !nearby.isCreative() && !nearby.isSpectator()) {
            getLookControl().setLookAt(nearby, 30, 30);
            warningClearTicks = 0;
        } else if (++warningClearTicks >= 40) setBombardierState(BombardierState.IDLE);
        incrementStateTick();
    }

    private void tickGrazing(@Nullable LivingEntity target) {
        if (canBeginAttack(target)) { beginWindup(); return; }
        if (getStateTick() >= 40) {
            ticksToGraze = 200 + random.nextInt(201);
            peacefulTicks = 80;
            setBombardierState(BombardierState.CAMOUFLAGED);
        } else incrementStateTick();
    }

    private boolean canBeginAttack(@Nullable LivingEntity target) {
        if (target == null || chemicalCharge < FULL_CHARGE || !onGround() || isInWaterOrBubble()) return false;
        double distance = distanceTo(target);
        return distance >= MIN_FLAME_RANGE && distance <= MAX_FLAME_RANGE && hasLineOfSight(target);
    }

    private void beginWindup() {
        navigation.stop();
        setBombardierState(BombardierState.WINDUP);
        level().playSound(null, blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, .55F, .55F);
    }

    private void tickWindup(@Nullable LivingEntity target) {
        int tick = getStateTick();
        if (target == null || !target.isAlive()) { enterCooling(); return; }
        navigation.stop();
        if (tick < AIM_TRACKING_TICKS) getLookControl().setLookAt(target, 30, 30);
        if (tick == AIM_TRACKING_TICKS) lockAim(target.getBoundingBox().getCenter().subtract(
                getAbdominalNozzlePosition(1.0F)).normalize());
        if (tick >= WINDUP_DURATION - 1) {
            setBombardierState(BombardierState.FIRING);
            level().playSound(null, blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.HOSTILE, 1.0F, .75F);
        } else incrementStateTick();
    }

    private void tickFiring() {
        navigation.stop();
        BombardierFlameAttack.tickServer(this);
        if (getStateTick() >= FLAME_DURATION - 1) enterCooling(); else incrementStateTick();
    }

    private void enterCooling() {
        chemicalCharge = 0;
        setBombardierState(BombardierState.COOLING);
        playSound(SoundEvents.FIRE_EXTINGUISH, .7F, .8F);
    }

    private void tickCooling() {
        chemicalCharge = Math.min(FULL_CHARGE, chemicalCharge
                + (BombardierHabitat.isValidSupport(level(), blockPosition()) ? 2 : 1));
        if (chemicalCharge >= FULL_CHARGE) setBombardierState(BombardierState.IDLE); else incrementStateTick();
    }

    public boolean canSpray(LivingEntity target) {
        if (target == this || isAlliedTo(target)) return false;
        return !(target instanceof PhlegethonticBombardier other
                && BombardierHabitat.sharesOutcropping(this, other));
    }

    public Vec3 getAbdominalNozzlePosition(float partialTick) {
        float yaw = Mth.lerp(partialTick, yBodyRotO, yBodyRot);
        double radians = Math.toRadians(yaw);
        return position().add(-Math.sin(radians) * -.65, 1.05, Math.cos(radians) * -.65);
    }

    public Vec3 getLockedDirection() {
        double yaw = Math.toRadians(entityData.get(LOCKED_YAW));
        double pitch = Math.toRadians(entityData.get(LOCKED_PITCH));
        double horizontal = Math.cos(pitch);
        return new Vec3(Math.sin(yaw) * horizontal, Math.sin(pitch), Math.cos(yaw) * horizontal).normalize();
    }
    public float getLockedYaw() { return entityData.get(LOCKED_YAW); }
    public float getLockedPitch() { return entityData.get(LOCKED_PITCH); }

    private void lockAim(Vec3 direction) {
        entityData.set(LOCKED_YAW, (float) Math.toDegrees(Math.atan2(direction.x, direction.z)));
        entityData.set(LOCKED_PITCH, (float) Math.toDegrees(Math.asin(Mth.clamp(direction.y, -1, 1))));
    }

    public BombardierState getBombardierState() {
        int index = Mth.clamp(entityData.get(STATE), 0, BombardierState.values().length - 1);
        return BombardierState.values()[index];
    }

    public void setBombardierState(BombardierState state) {
        entityData.set(STATE, state.ordinal());
        entityData.set(STATE_TICK, 0);
        presentationAnimationState.stop();
    }

    public int getStateTick() { return entityData.get(STATE_TICK); }
    public int getChemicalCharge() { return chemicalCharge; }
    private void incrementStateTick() { entityData.set(STATE_TICK, getStateTick() + 1); }
    public Optional<BlockPos> getTerritoryAnchor() { return Optional.ofNullable(territoryAnchor); }
    public void setTerritoryAnchor(@Nullable BlockPos anchor) { territoryAnchor = anchor == null ? null : anchor.immutable(); }

    @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (!hurt || level().isClientSide || !(source.getEntity() instanceof LivingEntity attacker)) return hurt;
        setTarget(attacker);
        setPersistentAngerTarget(attacker.getUUID());
        startPersistentAngerTimer();
        for (PhlegethonticBombardier other : level().getEntitiesOfClass(PhlegethonticBombardier.class,
                getBoundingBox().inflate(18), mob -> mob != this && BombardierHabitat.sharesOutcropping(this, mob))) {
            other.setTarget(attacker);
            other.setPersistentAngerTarget(attacker.getUUID());
            other.startPersistentAngerTimer();
        }
        return true;
    }

    public static boolean canSpawn(EntityType<PhlegethonticBombardier> type, ServerLevelAccessor level,
                                   MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL
                && level.getBiome(pos).is(BiomeInit.PHLEGETHONTIC_BASIN)
                && BombardierHabitat.isValidSupport(level, pos)
                && level.noCollision(type.getSpawnAABB(pos.getX() + .5, pos.getY(), pos.getZ() + .5));
    }

    @Override @Nullable public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                             MobSpawnType reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        territoryAnchor = BombardierHabitat.findNearestSupport(level, blockPosition(), 12, 6)
                .flatMap(feet -> BombardierHabitat.resolveComponentAnchor(level, feet.below(), 256)).orElse(null);
        if ((reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION) && territoryAnchor == null) discard();
        return result;
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        addPersistentAngerSaveData(tag);
        if (territoryAnchor != null) tag.putLong("TerritoryAnchor", territoryAnchor.asLong());
        tag.putString("BombardierState", getBombardierState().name());
        tag.putInt("StateTick", getStateTick());
        tag.putInt("ChemicalCharge", chemicalCharge);
        tag.putFloat("LockedYaw", entityData.get(LOCKED_YAW));
        tag.putFloat("LockedPitch", entityData.get(LOCKED_PITCH));
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readPersistentAngerSaveData(level(), tag);
        territoryAnchor = tag.contains("TerritoryAnchor") ? BlockPos.of(tag.getLong("TerritoryAnchor")) : null;
        BombardierState loaded;
        try { loaded = BombardierState.valueOf(tag.getString("BombardierState")); }
        catch (IllegalArgumentException ignored) { loaded = BombardierState.IDLE; }
        boolean interruptedAttack = loaded == BombardierState.WINDUP || loaded == BombardierState.FIRING;
        if (interruptedAttack) loaded = BombardierState.COOLING;
        setBombardierState(loaded);
        entityData.set(STATE_TICK, Math.max(0, tag.getInt("StateTick")));
        chemicalCharge = Mth.clamp(tag.getInt("ChemicalCharge"), 0, FULL_CHARGE);
        if (interruptedAttack) chemicalCharge = 0;
        entityData.set(LOCKED_YAW, tag.getFloat("LockedYaw"));
        entityData.set(LOCKED_PITCH, tag.getFloat("LockedPitch"));
    }

    @Override public int getRemainingPersistentAngerTime() { return remainingPersistentAngerTime; }
    @Override public void setRemainingPersistentAngerTime(int time) { remainingPersistentAngerTime = time; }
    @Override @Nullable public UUID getPersistentAngerTarget() { return persistentAngerTarget; }
    @Override public void setPersistentAngerTarget(@Nullable UUID target) { persistentAngerTarget = target; }
    @Override public void startPersistentAngerTimer() { remainingPersistentAngerTime = ANGER_TIME.sample(random); }
}
