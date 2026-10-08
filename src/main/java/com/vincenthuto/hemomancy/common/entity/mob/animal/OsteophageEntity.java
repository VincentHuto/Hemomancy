package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/** Bone-perching scavenger; flight follows the Choir Keeper's pursuit/return pattern. */
public final class OsteophageEntity extends PathfinderMob {
    public static final TagKey<Biome> SPAWN_BIOMES = TagKey.create(Registries.BIOME,
            Hemomancy.rloc("osteophage_spawnlist"));
    private static final EntityDataAccessor<Integer> ACTIVITY = SynchedEntityData.defineId(
            OsteophageEntity.class, EntityDataSerializers.INT);
    private static final int PERCHED = 0, FORAGING = 1, HUNTING = 2, RETURNING = 3;
    private static final int SEARCH_RADIUS = 20;

    @Nullable private BlockPos perch;
    @Nullable private ItemEntity targetBone;
    private int feedingCooldown, pursuitTicks, attackCooldown;
    private boolean liningUp = true;
    private float previousFlightPose, flightPose;

    public OsteophageEntity(EntityType<? extends OsteophageEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 16)
                .add(Attributes.MOVEMENT_SPEED, .18).add(Attributes.FLYING_SPEED, .48)
                .add(Attributes.FOLLOW_RANGE, SEARCH_RADIUS).add(Attributes.ATTACK_DAMAGE, 6);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVITY, RETURNING);
    }

    @Override protected void registerGoals() {
        // One controller owns the perch, food pursuit, hunting passes and return flight.
    }

    public boolean isFlying() { return entityData.get(ACTIVITY) != PERCHED; }

    public float getFlightPoseAmount(float partialTick) {
        return Mth.lerp(partialTick, previousFlightPose, flightPose);
    }

    public static boolean canSpawnHere(EntityType<? extends OsteophageEntity> type, LevelAccessor level,
                                       MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBiome(pos).is(SPAWN_BIOMES) && level.getBlockState(pos).isAir()
                && level.noCollision(type.getSpawnAABB(pos.getX() + .5, pos.getY(), pos.getZ() + .5))
                && findPerch(type, level, pos, 8, null) != null;
    }

    private static boolean clearPerch(EntityType<?> type, LevelAccessor level, BlockPos seat) {
        if (!level.hasChunkAt(seat) || !level.getBlockState(seat.below()).is(Blocks.BONE_BLOCK)) return false;
        double x = seat.getX() + .5, y = seat.getY(), z = seat.getZ() + .5;
        return level.getBlockState(seat).isAir() && level.getBlockState(seat.above()).isAir()
                && level.noCollision(type.getSpawnAABB(x, y, z))
                && level.noCollision(new AABB(x - .7, y, z - .7, x + .7, y + 1.4, z + .7));
    }

    @Nullable private static BlockPos findPerch(EntityType<?> type, LevelAccessor level, BlockPos around,
                                               int radius, @Nullable OsteophageEntity seeker) {
        Set<BlockPos> claimed = new HashSet<>();
        if (level instanceof Level world) {
            for (OsteophageEntity bird : world.getEntitiesOfClass(OsteophageEntity.class,
                    new AABB(around).inflate(radius + SEARCH_RADIUS), bird -> bird != seeker && bird.isAlive())) {
                if (bird.perch != null) claimed.add(bird.perch);
            }
        }
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos seat : BlockPos.betweenClosed(around.offset(-radius, -8, -radius),
                around.offset(radius, 8, radius))) {
            if (claimed.contains(seat) || !clearPerch(type, level, seat)) continue;
            double distance = seat.distSqr(around);
            if (distance < bestDistance) { best = seat.immutable(); bestDistance = distance; }
        }
        return best;
    }

    private boolean validPerch() {
        return perch != null && clearPerch(getType(), level(), perch);
    }

    private boolean perchClaimedByEarlierBird() {
        return perch != null && !level().getEntitiesOfClass(OsteophageEntity.class,
                new AABB(perch).inflate(SEARCH_RADIUS + 16), bird -> bird != this && bird.isAlive()
                        && bird.getId() < getId() && perch.equals(bird.perch)).isEmpty();
    }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                 MobSpawnType reason, @Nullable SpawnGroupData data) {
        var result = super.finalizeSpawn(level, difficulty, reason, data);
        perch = findPerch(getType(), level, blockPosition(), 8, this);
        if (perch != null) {
            setPos(Vec3.atBottomCenterOf(perch));
            entityData.set(ACTIVITY, PERCHED);
        }
        return result;
    }

    @Override public void tick() {
        super.tick();
        setNoGravity(true);
        previousFlightPose = flightPose;
        flightPose = Mth.clamp(flightPose + (isFlying() ? .16F : -.12F), 0, 1);
        if (level().isClientSide || isNoAi()) return;
        if (feedingCooldown > 0) feedingCooldown--;
        if (attackCooldown > 0) attackCooldown--;

        if (!validPerch() || (tickCount % 20 == 0 && perchClaimedByEarlierBird())) {
            perch = null;
            if (entityData.get(ACTIVITY) == PERCHED) entityData.set(ACTIVITY, RETURNING);
        }
        if (perch == null && tickCount % 40 == 0)
            perch = findPerch(getType(), level(), blockPosition(), 12, this);

        int activity = entityData.get(ACTIVITY);
        if (activity == PERCHED) {
            setDeltaMovement(Vec3.ZERO);
            setPos(Vec3.atBottomCenterOf(perch));
        }
        if ((activity == PERCHED || activity == RETURNING) && feedingCooldown == 0 && tickCount % 10 == 0) {
            targetBone = findBone();
            if (targetBone != null) { pursuitTicks = 0; entityData.set(ACTIVITY, FORAGING); }
            else {
                AbstractSkeleton skeleton = findSkeleton();
                if (skeleton != null) {
                    setTarget(skeleton); pursuitTicks = 0; liningUp = true;
                    entityData.set(ACTIVITY, HUNTING);
                }
            }
        }
        switch (entityData.get(ACTIVITY)) {
            case FORAGING -> forage();
            case HUNTING -> hunt();
            case RETURNING -> returnToPerch();
            default -> { }
        }
    }

    @Nullable private ItemEntity findBone() {
        return level().getEntitiesOfClass(ItemEntity.class, getBoundingBox().inflate(SEARCH_RADIUS),
                item -> edibleBone(item) && hasLineOfSight(item)).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    private boolean edibleBone(@Nullable ItemEntity item) {
        return item != null && item.isAlive() && !item.hasPickUpDelay() && item.getItem().is(Items.BONE)
                && !item.isInWater() && !item.isInLava();
    }

    @Nullable private AbstractSkeleton findSkeleton() {
        return level().getEntitiesOfClass(AbstractSkeleton.class, getBoundingBox().inflate(SEARCH_RADIUS),
                skeleton -> skeleton.isAlive() && !skeleton.isInLava() && hasLineOfSight(skeleton)).stream()
                .min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    private void forage() {
        if (++pursuitTicks > 200) { startReturn(40); return; }
        if (!edibleBone(targetBone)) {
            targetBone = findBone();
            if (targetBone == null) { startReturn(20); return; }
            pursuitTicks = 0;
        }
        Vec3 destination = targetBone.position().add(0, .15, 0);
        flyToward(destination, .085, .55);
        if (position().distanceToSqr(destination) < 1.4 && hasLineOfSight(targetBone)) {
            ItemStack stack = targetBone.getItem().copy();
            stack.shrink(1);
            if (stack.isEmpty()) targetBone.discard(); else targetBone.setItem(stack);
            heal(2);
            level().playSound(null, blockPosition(), SoundEvents.FOX_EAT, SoundSource.NEUTRAL, .65F, .65F);
            if (level() instanceof ServerLevel server)
                server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.BONE)),
                        getX(), getY() + .7, getZ(), 4, .1, .1, .1, .02);
            startReturn(120);
        }
    }

    private void hunt() {
        if (++pursuitTicks > 360) { startReturn(60); return; }
        if (!(getTarget() instanceof AbstractSkeleton skeleton) || !skeleton.isAlive()) {
            startReturn(20); return;
        }
        if (liningUp) {
            Vec3 approach = skeleton.position().add(0, 4, 0);
            flyToward(approach, .075, .45);
            if (position().distanceToSqr(approach) < 1.0 && attackCooldown == 0) liningUp = false;
        } else {
            Vec3 strike = skeleton.position().add(0, .6, 0);
            flyToward(strike, .1, .6);
            if (position().distanceToSqr(strike) < 2.0 && attackCooldown == 0 && hasLineOfSight(skeleton)) {
                doHurtTarget(skeleton);
                attackCooldown = 24;
                liningUp = true;
            }
        }
    }

    private void startReturn(int cooldown) {
        targetBone = null;
        setTarget(null);
        pursuitTicks = 0;
        feedingCooldown = cooldown;
        entityData.set(ACTIVITY, RETURNING);
    }

    private void returnToPerch() {
        if (perch == null) { setDeltaMovement(getDeltaMovement().scale(.8)); return; }
        Vec3 destination = Vec3.atBottomCenterOf(perch);
        flyToward(destination, .06, .36);
        if (position().distanceToSqr(destination) < .35 && clearFlightLine(position(), destination)) {
            setPos(destination); setDeltaMovement(Vec3.ZERO); entityData.set(ACTIVITY, PERCHED);
        }
    }

    private void flyToward(Vec3 target, double acceleration, double maxSpeed) {
        Vec3 delta = flightWaypoint(target).subtract(position());
        if (delta.lengthSqr() < .0001) { setDeltaMovement(getDeltaMovement().scale(.6)); return; }
        Vec3 motion = getDeltaMovement().scale(.76).add(delta.normalize().scale(acceleration));
        if (motion.lengthSqr() > maxSpeed * maxSpeed) motion = motion.normalize().scale(maxSpeed);
        setDeltaMovement(motion);
        if (delta.horizontalDistanceSqr() > .0001) {
            setYRot((float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90);
            yBodyRot = getYRot();
            yHeadRot = yBodyRot;
        }
    }

    private Vec3 flightWaypoint(Vec3 target) {
        Vec3 current = position();
        if (clearFlightLine(current, target)) return target;
        double baseY = Math.max(current.y, target.y);
        for (double y = baseY; y <= baseY + 8; y += .5) {
            Vec3 climb = new Vec3(current.x, y, current.z), across = new Vec3(target.x, y, target.z);
            if (!clearFlightLine(current, climb) || !clearFlightLine(climb, across)
                    || !clearFlightLine(across, target)) continue;
            if (current.y < y - .25) return climb;
            if (current.subtract(across).horizontalDistanceSqr() > .5625) return across;
            return target;
        }
        Vec3 direction = target.subtract(current);
        Vec3 side = new Vec3(-direction.z, 0, direction.x).normalize().scale(2);
        for (Vec3 offset : new Vec3[] {side, side.scale(-1)}) {
            Vec3 around = current.add(offset);
            if (clearFlightLine(current, around) && clearFlightLine(around, target)) return around;
        }
        return current;
    }

    private boolean clearFlightLine(Vec3 from, Vec3 to) {
        Vec3 travel = to.subtract(from);
        int steps = Math.max(1, Mth.ceil(travel.length() * 4));
        AABB body = getBoundingBox().deflate(.01);
        for (int i = 1; i <= steps; i++) {
            Vec3 point = from.add(travel.scale((double) i / steps));
            if (!level().hasChunkAt(BlockPos.containing(point))
                    || !level().noCollision(body.move(point.subtract(position())))
                    || level().containsAnyLiquid(body.move(point.subtract(position())))) return false;
        }
        return true;
    }

    @Override public void travel(Vec3 input) {
        if (isAlive()) move(MoverType.SELF, getDeltaMovement()); else super.travel(input);
    }

    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }
    @Override protected int calculateFallDamage(float distance, float multiplier) { return 0; }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.PARROT_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.PARROT_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PARROT_DEATH; }
    @Override protected float getSoundVolume() { return .35F; }
    @Override public float getVoicePitch() { return .55F + random.nextFloat() * .15F; }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (perch != null) tag.putLong("Perch", perch.asLong());
        tag.putBoolean("Flying", isFlying());
        tag.putInt("FeedingCooldown", feedingCooldown);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        perch = tag.contains("Perch") ? BlockPos.of(tag.getLong("Perch")) : null;
        feedingCooldown = Mth.clamp(tag.getInt("FeedingCooldown"), 0, 120);
        targetBone = null; setTarget(null); pursuitTicks = 0; attackCooldown = 0; liningUp = true;
        boolean seated = perch != null && position().distanceToSqr(Vec3.atBottomCenterOf(perch)) < .35;
        // Jar display copies are moved to the origin and never tick; retain the captured pose.
        if (level().isClientSide && tag.contains("Flying")) seated = !tag.getBoolean("Flying");
        entityData.set(ACTIVITY, seated ? PERCHED : RETURNING);
        previousFlightPose = flightPose = seated ? 0 : 1;
    }
}
