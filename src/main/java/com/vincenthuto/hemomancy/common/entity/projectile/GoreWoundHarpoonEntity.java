package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingCrossbowItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public final class GoreWoundHarpoonEntity extends AbstractArrow {
    private static final EntityDataAccessor<Integer> TARGET = SynchedEntityData.defineId(
            GoreWoundHarpoonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SHOOTER = SynchedEntityData.defineId(
            GoreWoundHarpoonEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(
            GoreWoundHarpoonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int TETHER_TICKS = 20;
    private boolean livingCrossbow;
    private boolean returnToShooter;
    private boolean damaged;
    private boolean retainingOnHit;
    private int tetherTicks;

    public GoreWoundHarpoonEntity(EntityType<? extends GoreWoundHarpoonEntity> type, Level level) {
        super(type, level);
        pickup = Pickup.DISALLOWED;
    }

    public GoreWoundHarpoonEntity(Level level, LivingEntity shooter, @Nullable ItemStack weapon) {
        super(EntityInit.gore_wound_harpoon.get(), shooter, level,
                new ItemStack(ItemInit.gore_wound_harpoon.get()), weapon);
        pickup = Pickup.DISALLOWED;
        livingCrossbow = weapon != null && weapon.getItem() instanceof LivingCrossbowItem;
        returnToShooter = GoreWoundHarpoonRules.shouldReturn(livingCrossbow, false, false);
        entityData.set(SHOOTER, shooter.getId());
    }

    public void setReturnPolicy(boolean creative, boolean multishotSide) {
        returnToShooter = GoreWoundHarpoonRules.shouldReturn(livingCrossbow, creative, multishotSide);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TARGET, -1);
        builder.define(SHOOTER, -1);
        builder.define(RETURNING, false);
    }

    public int targetId() {
        return entityData.get(TARGET);
    }

    public int shooterId() {
        return entityData.get(SHOOTER);
    }

    public boolean isReturning() {
        return entityData.get(RETURNING);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ItemInit.gore_wound_harpoon.get());
    }

    @Override
    public byte getPierceLevel() {
        // Keep the arrow for our tether, but do not continue vanilla's piercing hit loop.
        return (byte) (retainingOnHit ? 1 : 0);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        damaged = true;
        super.doPostHurtEffects(target);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (level().isClientSide) return;
        damaged = false;
        retainingOnHit = true;
        try {
            super.onHitEntity(hit);
        } finally {
            retainingOnHit = false;
        }
        if (!(hit.getEntity() instanceof LivingEntity target) || !damaged) {
            finishShot();
            return;
        }
        if (!target.isAlive()) {
            if (GoreWoundHarpoonRules.shouldDropOnKill(livingCrossbow, random.nextFloat())) {
                spawnAtLocation(new ItemStack(ItemInit.gore_wound_harpoon.get()));
            }
            finishShot();
            return;
        }
        if (!(getOwner() instanceof LivingEntity owner) || owner.level() != level()) {
            finishShot();
            return;
        }
        entityData.set(TARGET, target.getId());
        setNoPhysics(true);
        setDeltaMovement(Vec3.ZERO);
        setPos(target.getBoundingBox().getCenter());
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        super.onHitBlock(hit);
        if (!level().isClientSide) finishShot();
    }

    @Override
    public void tick() {
        if (targetId() >= 0) {
            baseTick();
            Entity found = level().getEntity(targetId());
            if (!(found instanceof LivingEntity target)) {
                if (!level().isClientSide) finishShot();
                return;
            }
            setPos(target.getBoundingBox().getCenter());
            setDeltaMovement(Vec3.ZERO);
            if (!level().isClientSide) tickTether(target);
            return;
        }
        if (isReturning()) {
            baseTick();
            if (!level().isClientSide) tickReturn();
            return;
        }
        if (!level().isClientSide && livingCrossbow && tickCount < 15) steerTowardTarget();
        super.tick();
        if (!level().isClientSide && !isRemoved()) {
            Entity owner = getOwner();
            if (livingCrossbow && (tickCount > 40 || owner == null || distanceToSqr(owner) > 32.0D * 32.0D)) {
                finishShot();
            } else if (!livingCrossbow && tickCount > 100) {
                finishShot();
            }
        }
    }

    private void tickTether(LivingEntity target) {
        Entity owner = getOwner();
        if (!(owner instanceof LivingEntity shooter) || !shooter.isAlive() || !target.isAlive()
                || shooter.level() != level() || target.level() != level()
                || shooter.distanceToSqr(target) > 24.0D * 24.0D
                || shooter.distanceToSqr(target) < 2.5D * 2.5D
                || ++tetherTicks >= TETHER_TICKS) {
            finishShot();
            return;
        }
        Vec3 direction = shooter.position().subtract(target.position());
        Vec3 velocity = target.getDeltaMovement().add(direction.normalize().scale(0.06D));
        velocity = new Vec3(velocity.x, Math.clamp(velocity.y, -0.12D, 0.12D), velocity.z);
        double horizontal = velocity.horizontalDistance();
        if (horizontal > 0.35D) {
            velocity = new Vec3(velocity.x * 0.35D / horizontal, velocity.y, velocity.z * 0.35D / horizontal);
        }
        target.setDeltaMovement(velocity);
        target.hasImpulse = true;
        target.hurtMarked = true;
    }

    private void steerTowardTarget() {
        if (!(getOwner() instanceof LivingEntity shooter) || getDeltaMovement().lengthSqr() < 0.01D) return;
        Vec3 flight = getDeltaMovement().normalize();
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(12.0D), target -> target != shooter && target.isAlive()
                        && !target.isAlliedTo(shooter) && shooter.canAttack(target))) {
            Vec3 toTarget = candidate.getBoundingBox().getCenter().subtract(position());
            double distance = toTarget.length();
            if (distance < bestDistance && distance > 0.01D
                    && GoreWoundHarpoonRules.canHome(distance, flight.dot(toTarget.scale(1.0D / distance)))) {
                best = candidate;
                bestDistance = distance;
            }
        }
        if (best != null) {
            Vec3 desired = best.getBoundingBox().getCenter().subtract(position()).normalize();
            double speed = getDeltaMovement().length();
            setDeltaMovement(flight.scale(1.0D - GoreWoundHarpoonRules.HOMING_BLEND)
                    .add(desired.scale(GoreWoundHarpoonRules.HOMING_BLEND)).normalize().scale(speed));
        }
    }

    private void finishShot() {
        entityData.set(TARGET, -1);
        if (!returnToShooter) {
            discard();
            return;
        }
        entityData.set(RETURNING, true);
        setNoPhysics(true);
        setDeltaMovement(Vec3.ZERO);
    }

    private void tickReturn() {
        Entity owner = getOwner();
        if (!(owner instanceof Player player) || !player.isAlive() || player.level() != level()) {
            spawnAtLocation(new ItemStack(ItemInit.gore_wound_harpoon.get()));
            discard();
            return;
        }
        Vec3 toOwner = player.getEyePosition().subtract(position());
        if (toOwner.lengthSqr() < 1.5D * 1.5D) {
            ItemStack returned = new ItemStack(ItemInit.gore_wound_harpoon.get());
            if (!player.addItem(returned)) spawnAtLocation(returned);
            discard();
            return;
        }
        Vec3 step = toOwner.normalize().scale(Math.min(1.2D, toOwner.length()));
        setPos(position().add(step));
        setDeltaMovement(step);
        if (tickCount > 180) {
            spawnAtLocation(new ItemStack(ItemInit.gore_wound_harpoon.get()));
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("LivingCrossbow", livingCrossbow);
        tag.putBoolean("ReturnToShooter", returnToShooter);
        tag.putBoolean("Returning", isReturning());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        livingCrossbow = tag.getBoolean("LivingCrossbow");
        returnToShooter = tag.contains("ReturnToShooter") ? tag.getBoolean("ReturnToShooter") : livingCrossbow;
        entityData.set(RETURNING, tag.getBoolean("Returning") || returnToShooter);
        entityData.set(TARGET, -1);
        pickup = Pickup.DISALLOWED;
    }
}
