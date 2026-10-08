package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.Vec3;
import java.util.EnumSet;

/** Navigation and synchronized short-lived actions common to the small Pelagic animals. */
public abstract class PelagicAnimal extends WaterAnimal {
    private static final EntityDataAccessor<Integer> ACTION_TICKS = SynchedEntityData.defineId(PelagicAnimal.class, EntityDataSerializers.INT);
    protected int actionCooldown;
    private final PelagicSwimmingPose swimmingPose = new PelagicSwimmingPose();

    protected PelagicAnimal(EntityType<? extends PelagicAnimal> type, Level level) {
        super(type, level);
        moveControl = new SmoothSwimmingMoveControl(this, 45, 8, .06F, .02F, false);
        lookControl = new SmoothSwimmingLookControl(this, 8);
    }

    public abstract Species habitat();
    public boolean bottomDweller() { return false; }
    public int actionTicks() { return entityData.get(ACTION_TICKS); }
    public PelagicSwimmingPose swimmingPose() { return swimmingPose; }
    protected boolean mayWander() { return actionTicks() == 0; }
    protected void actFor(int ticks) { entityData.set(ACTION_TICKS, ticks); }
    public static AttributeSupplier.Builder attributes(double health, double speed) {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, health).add(Attributes.MOVEMENT_SPEED, speed)
                .add(Attributes.FOLLOW_RANGE, 12);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(ACTION_TICKS, 0);
    }
    @Override protected PathNavigation createNavigation(Level level) { return new WaterBoundPathNavigation(this, level); }
    @Override protected void registerGoals() { goalSelector.addGoal(4, new HabitatSwimGoal()); }
    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }
    @Override public int getMaxSpawnClusterSize() { return 2; }

    @Override public void tick() {
        double previousX = getX(), previousY = getY(), previousZ = getZ();
        super.tick();
        setNoGravity(isInWater());
        if (habitat() == Species.COMB_JELLY || habitat() == Species.VAMPIRE_SQUID) {
            // Actual displacement includes client interpolation and defensive squid retreats.
            swimmingPose.tick(getX()-previousX, getY()-previousY, getZ()-previousZ, isInWater());
        }
        if (!level().isClientSide) {
            if (actionTicks() > 0) actFor(actionTicks() - 1);
            if (actionCooldown > 0) actionCooldown--;
        }
    }
    @Override public void travel(Vec3 input) {
        if (isEffectiveAi() && isInWater()) {
            moveRelative(.035F, input);
            double dy = bottomDweller() ? -.012 : PelagicHabitat.depthCorrection(level(), blockPosition(), habitat());
            setDeltaMovement(getDeltaMovement().add(0, dy, 0));
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(.9));
        } else super.travel(input);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PelagicAction", actionTicks()); tag.putInt("PelagicCooldown", actionCooldown);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        actFor(Math.max(0, tag.getInt("PelagicAction"))); actionCooldown = Math.max(0, tag.getInt("PelagicCooldown"));
    }

    private final class HabitatSwimGoal extends Goal {
        private Vec3 destination;
        private int remaining;
        HabitatSwimGoal() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() {
            if (!mayWander() || random.nextInt(30) != 0) return false;
            destination = PelagicHabitat.target(PelagicAnimal.this, habitat(), bottomDweller());
            return destination != null;
        }
        @Override public void start() {
            remaining = 100;
            navigation.moveTo(destination.x, destination.y, destination.z, 1);
        }
        @Override public boolean canContinueToUse() { return remaining-- > 0 && mayWander() && !navigation.isDone(); }
        @Override public void stop() { navigation.stop(); }
    }
}
