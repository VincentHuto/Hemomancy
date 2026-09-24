package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.vincenthuto.hemomancy.common.init.SoundInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public final class ChoirKeeperEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> ACTIVITY = SynchedEntityData.defineId(
            ChoirKeeperEntity.class, EntityDataSerializers.INT);
    private static final int PERCHED = 0;
    private static final int CHASING = 1;
    private static final int RETURNING = 2;
    private static final int PLANTING = 3;

    @Nullable private BlockPos perch;
    @Nullable private BlockPos plantTarget;
    @Nullable private ItemEntity targetEye;
    private int eyeCooldown;
    private int chaseTicks;
    private int propagationCooldown = 3600;
    private float previousFlare;
    private float flare;
    private float previousFlightPose;
    private float flightPose;

    public ChoirKeeperEntity(EntityType<? extends ChoirKeeperEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.18D)
                .add(Attributes.FLYING_SPEED, 0.48D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTIVITY, PERCHED);
    }

    public boolean isFlying() {
        return this.entityData.get(ACTIVITY) != PERCHED;
    }

    public float getFlareAmount(float partialTick) {
        return Mth.lerp(partialTick, previousFlare, flare);
    }

    public float getFlightPoseAmount(float partialTick) {
        return Mth.lerp(partialTick, previousFlightPose, flightPose);
    }

    @Override
    protected void registerGoals() {
        // Perch, eye pursuit, and return are driven by the single flight state below.
    }

    public static boolean canSpawnHere(EntityType<? extends ChoirKeeperEntity> type, LevelAccessor level,
                                       MobSpawnType reason, BlockPos pos, RandomSource random) {
        return level.getBlockState(pos).isAir()
                && level.noCollision(type.getSpawnAABB(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D))
                && findPerch(type, level, pos, 8, null) != null;
    }

    @Nullable
    private static BlockPos findPerch(EntityType<?> type,
                                      LevelAccessor level, BlockPos around, int radius,
                                      @Nullable ChoirKeeperEntity seeker) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        Set<BlockPos> claimed = claimedPerches(level, seeker);
        for (BlockPos flower : BlockPos.betweenClosed(around.offset(-radius, -3, -radius),
                around.offset(radius, 10, radius))) {
            BlockPos seat = flower.above();
            if (claimed.contains(seat) || !clearPerch(type, level, seat)) continue;
            double distance = seat.distSqr(around);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = seat.immutable();
            }
        }
        return best;
    }

    private boolean validPerch() {
        return perch != null && clearPerch(this.getType(), this.level(), perch);
    }

    private static Set<BlockPos> claimedPerches(LevelAccessor level, @Nullable ChoirKeeperEntity seeker) {
        Set<BlockPos> claimed = new HashSet<>();
        if (level instanceof ServerLevel server) {
            for (var entity : server.getAllEntities()) {
                if (entity instanceof ChoirKeeperEntity keeper && keeper != seeker
                        && keeper.isAlive() && keeper.perch != null) claimed.add(keeper.perch);
            }
        }
        return claimed;
    }

    private boolean claimedByEarlierKeeper(BlockPos seat) {
        if (this.level() instanceof ServerLevel server) {
            for (var entity : server.getAllEntities()) {
                if (entity instanceof ChoirKeeperEntity keeper && keeper != this && keeper.isAlive()
                        && seat.equals(keeper.perch) && keeper.getId() < this.getId()) return true;
            }
        }
        return false;
    }

    private static boolean clearPerch(EntityType<?> type,
                                      LevelAccessor level, BlockPos seat) {
        double x = seat.getX() + 0.5D;
        double y = seat.getY();
        double z = seat.getZ() + 0.5D;
        return level.getBlockState(seat.below()).is(Blocks.CHORUS_FLOWER)
                && level.getBlockState(seat).isAir()
                && level.getBlockState(seat.above()).isAir()
                && level.noCollision(type.getSpawnAABB(x, y, z))
                && level.noCollision(new AABB(x - 1.25D, y, z - 1.25D,
                        x + 1.25D, y + 1.75D, z + 1.25D));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        this.perch = findPerch(this.getType(), level, this.blockPosition(), 8, this);
        if (this.perch != null) {
            this.setPos(Vec3.atBottomCenterOf(this.perch));
        } else {
            this.entityData.set(ACTIVITY, RETURNING);
            if (level.getBlockState(this.blockPosition().below()).is(Blocks.CHORUS_FLOWER)) this.discard();
        }
        return result;
    }

    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        this.previousFlare = this.flare;
        this.flare = Mth.clamp(this.flare + (this.entityData.get(ACTIVITY) == CHASING ? 0.12F : -0.08F),
                0.0F, 1.0F);
        this.previousFlightPose = this.flightPose;
        this.flightPose = Mth.clamp(this.flightPose + (this.isFlying() ? 0.16F : -0.12F), 0.0F, 1.0F);
        if (this.level().isClientSide) return;

        if (eyeCooldown > 0) eyeCooldown--;
        if (propagationCooldown > 0) propagationCooldown--;
        if (perch != null && claimedByEarlierKeeper(perch)) {
            BlockPos alternate = findPerch(this.getType(), this.level(), this.blockPosition(), 16, this);
            if (alternate == null) {
                this.discard();
                return;
            }
            perch = alternate;
            this.entityData.set(ACTIVITY, RETURNING);
        }
        if (!validPerch()) {
            perch = null;
            if (this.entityData.get(ACTIVITY) == PERCHED) this.entityData.set(ACTIVITY, RETURNING);
        }
        if (perch == null && this.tickCount % 40 == 0) {
            perch = findPerch(this.getType(), this.level(), this.blockPosition(), 12, this);
        }

        int activity = this.entityData.get(ACTIVITY);
        if (activity == PERCHED) {
            this.setDeltaMovement(Vec3.ZERO);
            if (this.perch != null) this.setPos(Vec3.atBottomCenterOf(this.perch));
            Player player = this.level().getNearestPlayer(this, 14.0D);
            if (player != null) face(player.position());
            if (eyeCooldown == 0 && this.tickCount % 4 == 0) {
                targetEye = findNearbyEye(true);
                if (targetEye != null) {
                    chaseTicks = 0;
                    this.entityData.set(ACTIVITY, CHASING);
                    this.level().playSound(null, this.blockPosition(),
                            SoundInit.ENTITY_CHOIR_KEEPER_SQUEAK.get(), SoundSource.AMBIENT, 0.9F, 1.0F);
                }
            }
            if (this.entityData.get(ACTIVITY) == PERCHED && propagationCooldown == 0
                    && this.level().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)) {
                plantTarget = findPlantSpot();
                propagationCooldown = 3600 + this.random.nextInt(2400);
                if (plantTarget != null) {
                    this.entityData.set(ACTIVITY, PLANTING);
                    if (this.level() instanceof ServerLevel server) {
                        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,
                                        new ItemStack(Items.CHORUS_FRUIT)),
                                this.getX(), this.getY() + 1.0D, this.getZ(),
                                5, 0.18D, 0.15D, 0.18D, 0.03D);
                    }
                }
            }
        } else if (activity == CHASING) {
            if (++chaseTicks > 160) {
                startReturn();
                return;
            }
            if (targetEye == null || !targetEye.isAlive() || !targetEye.getItem().is(Items.ENDER_EYE)) {
                targetEye = findNearbyEye(false);
                if (targetEye == null) {
                    startReturn();
                    return;
                }
                chaseTicks = 0;
            }
            Vec3 destination = targetEye.position().add(0.0D, 0.25D, 0.0D);
            flyToward(destination, 0.085D, 0.55D);
            if (this.position().distanceToSqr(destination) < 4.0D) {
                ItemStack stack = targetEye.getItem();
                stack.shrink(1);
                if (stack.isEmpty()) targetEye.discard();
                else targetEye.setItem(stack);
                this.spawnAtLocation(Items.ENDER_PEARL);
                this.level().playSound(null, this.blockPosition(), SoundEvents.ENDER_EYE_DEATH,
                        SoundSource.AMBIENT, 0.8F, 1.3F);
                if (this.level() instanceof ServerLevel server) {
                    server.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 0.5D, this.getZ(),
                            14, 0.3D, 0.3D, 0.3D, 0.08D);
                    server.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.5D, this.getZ(),
                            5, 0.2D, 0.2D, 0.2D, 0.03D);
                }
                startReturn();
            }
        } else if (activity == PLANTING) {
            if (plantTarget == null || !validPlantSpot(plantTarget)) {
                startReturn();
                return;
            }
            Vec3 destination = Vec3.atBottomCenterOf(plantTarget).add(0.0D, 1.1D, 0.0D);
            flyToward(destination, 0.07D, 0.4D);
            if (this.position().distanceToSqr(destination) < 1.0D) {
                this.level().setBlockAndUpdate(plantTarget, Blocks.CHORUS_FLOWER.defaultBlockState());
                this.level().playSound(null, plantTarget, SoundEvents.CHORUS_FLOWER_GROW,
                        SoundSource.AMBIENT, 0.65F, 1.2F);
                plantTarget = null;
                startReturn();
            }
        } else {
            if (!validPerch()) perch = findPerch(this.getType(), this.level(), this.blockPosition(), 12, this);
            if (perch == null) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.8D));
                return;
            }
            Vec3 destination = Vec3.atBottomCenterOf(perch);
            flyToward(destination, 0.06D, 0.36D);
            if (this.position().distanceToSqr(destination) < 0.35D) {
                this.setPos(destination);
                this.setDeltaMovement(Vec3.ZERO);
                this.entityData.set(ACTIVITY, PERCHED);
            }
        }
    }

    private void startReturn() {
        targetEye = null;
        plantTarget = null;
        chaseTicks = 0;
        eyeCooldown = 80;
        this.entityData.set(ACTIVITY, RETURNING);
    }

    @Nullable
    private ItemEntity findNearbyEye(boolean freshOnly) {
        return this.level().getEntitiesOfClass(ItemEntity.class,
                        this.getBoundingBox().inflate(22.0D),
                        eye -> eye.isAlive() && eye.getItem().is(Items.ENDER_EYE)
                                && (!freshOnly || eye.tickCount < 120))
                .stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);
    }

    @Nullable
    private BlockPos findPlantSpot() {
        if (perch == null) return null;
        for (int attempt = 0; attempt < 24; attempt++) {
            int x = perch.getX() + this.random.nextInt(15) - 7;
            int z = perch.getZ() + this.random.nextInt(15) - 7;
            BlockPos candidate = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    new BlockPos(x, perch.getY(), z));
            if (validPlantSpot(candidate)) return candidate;
        }
        return null;
    }

    private boolean validPlantSpot(BlockPos pos) {
        if (!this.level().getBlockState(pos.below()).is(Blocks.END_STONE)
                || !this.level().getBlockState(pos).isAir()
                || !this.level().getBlockState(pos.above()).isAir()
                || pos.distSqr(perch == null ? this.blockPosition() : perch) <= 9.0D) return false;
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 5, 2))) {
            if (this.level().getBlockState(nearby).is(Blocks.CHORUS_PLANT)
                    || this.level().getBlockState(nearby).is(Blocks.CHORUS_FLOWER)) return false;
        }
        return true;
    }

    private void flyToward(Vec3 target, double acceleration, double maxSpeed) {
        Vec3 waypoint = flightWaypoint(target);
        Vec3 delta = waypoint.subtract(this.position());
        if (delta.lengthSqr() < 0.0001D) return;
        Vec3 motion = this.getDeltaMovement().scale(0.76D).add(delta.normalize().scale(acceleration));
        if (motion.lengthSqr() > maxSpeed * maxSpeed) motion = motion.normalize().scale(maxSpeed);
        this.setDeltaMovement(motion);
        face(target);
    }

    private Vec3 flightWaypoint(Vec3 target) {
        Vec3 current = this.position();
        if (clearFlightLine(current, target)) return target;

        double baseY = Math.max(current.y, target.y);
        for (double y = baseY; y <= baseY + 8.0D; y += 0.5D) {
            Vec3 climb = new Vec3(current.x, y, current.z);
            Vec3 across = new Vec3(target.x, y, target.z);
            if (!clearFlightLine(current, climb) || !clearFlightLine(climb, across)) continue;
            if (current.y < y - 0.25D) return climb;
            if (Mth.square(current.x - target.x) + Mth.square(current.z - target.z) > 0.75D * 0.75D)
                return across;
            return target;
        }

        Vec3 direction = target.subtract(current);
        Vec3 side = new Vec3(-direction.z, 0.0D, direction.x).normalize().scale(2.0D);
        for (Vec3 offset : new Vec3[] {side, side.scale(-1.0D)}) {
            Vec3 around = current.add(offset);
            if (clearFlightLine(current, around) && clearFlightLine(around, target)) return around;
        }
        return target;
    }

    private boolean clearFlightLine(Vec3 from, Vec3 to) {
        Vec3 travel = to.subtract(from);
        int steps = Math.max(1, Mth.ceil(travel.length() * 4.0D));
        AABB body = this.getBoundingBox().deflate(0.01D);
        for (int i = 1; i <= steps; i++) {
            Vec3 point = from.add(travel.scale((double) i / steps));
            if (!this.level().noCollision(body.move(point.subtract(this.position())))) return false;
        }
        return true;
    }

    private void face(Vec3 target) {
        Vec3 direction = target.subtract(this.position());
        if (direction.horizontalDistanceSqr() > 0.0001D) {
            this.setYRot((float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F);
            this.yBodyRot = this.getYRot();
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) this.move(MoverType.SELF, this.getDeltaMovement());
        else super.travel(travelVector);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader level) {
        return level.isUnobstructed(this);
    }

    @Override
    protected int calculateFallDamage(float distance, float multiplier) {
        return 0;
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return this.random.nextInt(5) == 0 ? SoundInit.ENTITY_CHOIR_KEEPER_SQUEAK.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.BAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BAT_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.35F;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (perch != null) tag.putLong("Perch", perch.asLong());
        tag.putInt("EyeCooldown", eyeCooldown);
        tag.putInt("PropagationCooldown", propagationCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        perch = tag.contains("Perch") ? BlockPos.of(tag.getLong("Perch")) : null;
        eyeCooldown = tag.getInt("EyeCooldown");
        propagationCooldown = tag.contains("PropagationCooldown") ? tag.getInt("PropagationCooldown") : 3600;
        targetEye = null;
        plantTarget = null;
        this.entityData.set(ACTIVITY, perch == null ? RETURNING : PERCHED);
    }
}
