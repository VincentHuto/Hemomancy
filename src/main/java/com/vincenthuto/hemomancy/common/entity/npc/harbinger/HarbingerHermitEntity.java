package com.vincenthuto.hemomancy.common.entity.npc.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueHubFactory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerHermitDialogueTrees;
import com.vincenthuto.hemomancy.common.rite.TempleOathRules;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.client.particle.factory.HermitEdgeGlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * A reclusive hermit of the Harbinger order, found dwelling within blood temple
 * structures throughout the world. Provides lore, guidance, and hints about
 * hemomancy progression based on the player's current Initiation Tier.
 * <p>
 * Unlike the Unstained Zealot who guides players away from blood magic,
 * the Harbinger Hermit encourages deeper exploration of hemomancy.
 */
public class HarbingerHermitEntity extends PathfinderMob {

    public static final int FAREWELL_DEATH_DURATION = 250;
    private static final String TAG_FAREWELL_DEATH_TICKS = "FarewellDeathTicks";
    private static final EntityDataAccessor<Integer> DATA_FAREWELL_DEATH_TICKS =
            SynchedEntityData.defineId(HarbingerHermitEntity.class, EntityDataSerializers.INT);
    private static final ParticleColor FAREWELL_EDGE_GLOW = new ParticleColor(235, 6, 4);
    private static final ParticleColor FAREWELL_HOT_EDGE_GLOW = new ParticleColor(255, 74, 18);
    private static final ParticleColor FAREWELL_DEEP_EDGE_GLOW = new ParticleColor(156, 0, 0);
    private static final double FAREWELL_FRONT_SURFACE_Z = -0.46D;
    private static final double FAREWELL_BACK_SURFACE_Z = 0.28D;
    private static final double FAREWELL_SIDE_SURFACE_X = 0.38D;
    private static final double FAREWELL_FRONT_SURFACE_VARIANCE = 0.020D;
    private static final double FAREWELL_SIDE_SURFACE_VARIANCE = 0.025D;

    public final AnimationState idleAnimationState = new AnimationState();

    public HarbingerHermitEntity(EntityType<? extends HarbingerHermitEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setInvulnerable(true);
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.15D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 10.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        // No wandering — hermits stay near their temple
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FAREWELL_DEATH_TICKS, 0);
    }

    public int getFarewellDeathTicks() {
        return this.entityData.get(DATA_FAREWELL_DEATH_TICKS);
    }

    public boolean isFarewellDying() {
        return getFarewellDeathTicks() > 0;
    }

    public float getFarewellDeathProgress(float partialTick) {
        int ticks = getFarewellDeathTicks();
        if (ticks <= 0) return 0.0F;
        return Math.min(1.0F, (ticks + partialTick) / (float) FAREWELL_DEATH_DURATION);
    }

    public void beginFarewellDeath() {
        if (isFarewellDying()) return;
        this.setInvulnerable(true);
        this.setNoAi(true);
        this.setDeltaMovement(Vec3.ZERO);
        this.entityData.set(DATA_FAREWELL_DEATH_TICKS, 1);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL)) {
            return super.hurt(source, amount);
        }
        if (isFarewellDying()) {
            return false;
        }
        // Allow damage when invulnerability has been explicitly cleared (e.g. farewell sequence)
        if (!this.isInvulnerable()) {
            return super.hurt(source, amount);
        }
        if (source.getEntity() instanceof Player player && player.isCreative()) {
            return super.hurt(source, amount);
        }
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        if (this.level().isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
        if (isFarewellDying()) {
            this.setDeltaMovement(Vec3.ZERO);
        }
        super.tick();
        if (isFarewellDying()) {
            tickFarewellDeath();
        }
    }

    private void tickFarewellDeath() {
        int ticks = getFarewellDeathTicks();
        if (this.level().isClientSide()) {
            tickFarewellDeathClient(ticks);
            return;
        }

        if (ticks >= FAREWELL_DEATH_DURATION) {
            this.remove(RemovalReason.KILLED);
            return;
        }

        this.entityData.set(DATA_FAREWELL_DEATH_TICKS, ticks + 1);
    }

    private void tickFarewellDeathClient(int ticks) {
        if (ticks <= 0) return;

        float progress = getFarewellDeathProgress(0.0F);
        spawnFarewellDissolveEdgeParticles(ticks, progress);
    }

    private void spawnFarewellDissolveEdgeParticles(int ticks, float progress) {
        if (progress < 0.08F) return;

        int targetCount = 10 + (int) (progress * 28.0F);
        if (ticks % 2 == 0) {
            targetCount += 3;
        }

        int emitted = 0;
        int attempts = targetCount * 32;
        double time = this.tickCount + progress;
        double seed = this.getId() * 0.137D;

        for (int i = 0; i < attempts && emitted < targetCount; i++) {
            FarewellSurfaceSample sample = sampleFarewellDissolveSurface();
            double field = farewellHoleField(sample.uvX(), sample.uvY(), sample.localX(), sample.localY(), sample.localZ(),
                    time, seed, progress);
            if (!isNearFarewellDissolveEdge(field, progress)) {
                continue;
            }

            Vec3 position = transformFarewellLocalToWorld(sample.localX(), sample.localY(), sample.localZ());
            Vec3 outward = transformFarewellDirectionToWorld(sample.normalX(), 0.0D, sample.normalZ()).normalize();
            Vec3 tangent = transformFarewellDirectionToWorld(sample.tangentX(), 0.0D, sample.tangentZ()).normalize();
            double outwardSpeed = 0.001D + this.random.nextDouble() * 0.002D + progress * 0.001D;
            double tangentSpeed = (this.random.nextDouble() - 0.5D) * (0.002D + progress * 0.002D);
            Vec3 velocity = outward.scale(outwardSpeed)
                    .add(tangent.scale(tangentSpeed));

            spawnFarewellEdgeGlowParticle(position, velocity, progress);
            emitted++;
        }
    }

    private FarewellSurfaceSample sampleFarewellDissolveSurface() {
        double localY = 0.58D + this.random.nextDouble() * 1.03D;
        double yNorm = clamp01((localY - 0.58D) / 1.03D);
        double torsoBias = 0.78D + Math.sin(yNorm * Math.PI) * 0.22D;
        double roll = this.random.nextDouble();

        if (roll < 0.45D) {
            double localX = (this.random.nextDouble() - 0.5D) * 0.62D * torsoBias;
            double localZ = FAREWELL_FRONT_SURFACE_Z
                    + (this.random.nextDouble() - 0.5D) * FAREWELL_FRONT_SURFACE_VARIANCE;
            double uvX = 0.50D + localX * 1.16D;
            return new FarewellSurfaceSample(localX, localY, localZ, uvX, 1.00D - yNorm,
                    localX * 0.16D, -1.0D, 1.0D, 0.0D);
        }

        if (roll < 0.68D) {
            double localX = (this.random.nextDouble() - 0.5D) * 0.62D * torsoBias;
            double localZ = FAREWELL_BACK_SURFACE_Z
                    + (this.random.nextDouble() - 0.5D) * FAREWELL_SIDE_SURFACE_VARIANCE;
            double uvX = 0.50D - localX * 0.92D;
            return new FarewellSurfaceSample(localX, localY, localZ, uvX, 1.00D - yNorm,
                    localX * 0.12D, 1.0D, 1.0D, 0.0D);
        }

        double side = roll < 0.84D ? -1.0D : 1.0D;
        double localX = side * FAREWELL_SIDE_SURFACE_X
                + (this.random.nextDouble() - 0.5D) * FAREWELL_SIDE_SURFACE_VARIANCE;
        double localZ = -0.16D + (this.random.nextDouble() - 0.5D) * 0.58D * torsoBias;
        double uvX = 0.50D + localZ * 0.95D * side;
        return new FarewellSurfaceSample(localX, localY, localZ, uvX, 1.00D - yNorm,
                side, localZ * 0.12D, 0.0D, 1.0D);
    }

    private void spawnFarewellEdgeGlowParticle(Vec3 position, Vec3 velocity, float progress) {
        ParticleColor color = FAREWELL_EDGE_GLOW;
        float roll = this.random.nextFloat();
        if (roll < 0.20F + progress * 0.12F) {
            color = FAREWELL_HOT_EDGE_GLOW;
        } else if (roll < 0.43F) {
            color = FAREWELL_DEEP_EDGE_GLOW;
        }

        double jitter = 0.008D + progress * 0.010D;
        this.level().addParticle(HermitEdgeGlowParticleFactory.createData(color),
                position.x() + (this.random.nextDouble() - 0.5D) * jitter,
                position.y() + (this.random.nextDouble() - 0.5D) * jitter,
                position.z() + (this.random.nextDouble() - 0.5D) * jitter,
                velocity.x() * 0.08D, 0.0D, velocity.z() * 0.08D);
    }

    private boolean isNearFarewellDissolveEdge(double field, float progress) {
        double cut = farewellCutForProgress(progress);
        double edgeBand = 0.046D + progress * 0.034D;
        return field > 0.13D && field < 1.12D && Math.abs(field - cut) <= edgeBand;
    }

    private double farewellHoleField(double uvX, double uvY, double localX, double localY, double localZ,
                                     double time, double seed, double progress) {
        double driftX = Math.sin(time * 0.017D + seed * 6.1D) * 0.12D;
        double driftY = Math.cos(time * 0.013D + seed * 4.7D) * 0.12D;
        double bodyUvX = uvX + localX * 0.018D + localY * 0.006D + driftX;
        double bodyUvY = uvY + localZ * 0.018D - localY * 0.004D + driftY;
        double fine = farewellSpotCell(bodyUvX * 17.0D + seed * 9.0D, bodyUvY * 17.0D + seed * 9.0D, seed, progress);
        double broad = farewellSpotCell(bodyUvX * 8.5D + 2.4D + seed * 5.0D,
                bodyUvY * 8.5D - 1.7D + seed * 5.0D, seed, progress);
        double torn = farewellSpotCell((uvY + localX * 0.012D) * 12.0D - 3.1D,
                (uvX + localY * 0.012D) * 12.0D + 1.8D, seed, progress);
        return Math.max(Math.max(fine, broad * 0.92D), torn * 0.72D);
    }

    private static double farewellSpotCell(double xCoord, double yCoord, double seed, double progress) {
        double cellX = Math.floor(xCoord);
        double cellY = Math.floor(yCoord);
        double fracX = fract(xCoord) - 0.5D;
        double fracY = fract(yCoord) - 0.5D;
        double best = 0.0D;
        double growth = farewellSpotGrowthForProgress(progress);

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                double idX = cellX + x;
                double idY = cellY + y;
                double centerX = (farewellHash21(idX + 7.13D, idY + 2.91D, seed) - 0.5D) * 0.56D;
                double centerY = (farewellHash21(idX + 3.77D, idY + 9.41D, seed) - 0.5D) * 0.56D;
                double radius = (0.11D + farewellHash21(idX + 11.7D, idY + 5.2D, seed) * 0.24D) * growth;
                double deltaX = fracX - x - centerX;
                double deltaY = fracY - y - centerY;
                double angle = Math.atan2(deltaY, deltaX);
                double angularWobble = Math.sin(angle * 5.0D + farewellHash21(idX + 13.7D, idY + 4.9D, seed) * Math.PI * 2.0D)
                        + Math.sin(angle * 11.0D + farewellHash21(idX + 1.7D, idY + 14.9D, seed) * Math.PI * 2.0D) * 0.55D;
                double tornNoise = farewellHash21(Math.floor((deltaX + idX) * 19.0D) + idX * 1.37D,
                        Math.floor((deltaY + idY) * 19.0D) + idY * 1.37D, seed) - 0.5D;
                double distance = Math.hypot(deltaX, deltaY) * (1.0D + angularWobble * 0.075D) + tornNoise * 0.018D;
                double softness = lerp(0.022D, 0.070D, growth);
                best = Math.max(best, smoothstep(radius + softness * 0.55D, radius - softness, distance));
            }
        }

        return best;
    }

    private static double farewellSpotGrowthForProgress(double progress) {
        return lerp(0.24D, 1.12D, smoothstep(0.04D, 0.86D, clamp01(progress)));
    }

    private static double farewellHash21(double x, double y, double seed) {
        double px = fract(x * 123.34D);
        double py = fract(y * 456.21D);
        double dot = px * (px + 45.32D + seed) + py * (py + 45.32D + seed);
        px += dot;
        py += dot;
        return fract(px * py);
    }

    private static double farewellCutForProgress(double progress) {
        return lerp(1.08D, 0.14D, smoothstep(0.08D, 0.94D, clamp01(progress)));
    }

    private Vec3 transformFarewellLocalToWorld(double localX, double localY, double localZ) {
        Vec3 rotated = transformFarewellDirectionToWorld(localX, localY, localZ);
        return new Vec3(this.getX() + rotated.x(), this.getY() + rotated.y(), this.getZ() + rotated.z());
    }

    private Vec3 transformFarewellDirectionToWorld(double localX, double localY, double localZ) {
        double yaw = Math.toRadians(180.0D - this.yBodyRot);
        double cos = Math.cos(yaw);
        double sin = Math.sin(yaw);
        double worldX = localX * cos + localZ * sin;
        double worldZ = localZ * cos - localX * sin;
        return new Vec3(worldX, localY, worldZ);
    }

    private record FarewellSurfaceSample(double localX, double localY, double localZ,
                                         double uvX, double uvY,
                                         double normalX, double normalZ,
                                         double tangentX, double tangentZ) {
    }

    private static double smoothstep(double edge0, double edge1, double value) {
        double t = clamp01((value - edge0) / (edge1 - edge0));
        return t * t * (3.0D - 2.0D * t);
    }

    private static double lerp(double from, double to, double amount) {
        return from + (to - from) * amount;
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }

    private static double fract(double value) {
        return value - Math.floor(value);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (isFarewellDying()) {
            compound.putInt(TAG_FAREWELL_DEATH_TICKS, getFarewellDeathTicks());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        int ticks = compound.getInt(TAG_FAREWELL_DEATH_TICKS);
        if (ticks > 0) {
            this.setInvulnerable(true);
            this.setNoAi(true);
            this.entityData.set(DATA_FAREWELL_DEATH_TICKS, Math.min(ticks, FAREWELL_DEATH_DURATION));
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isFarewellDying()) {
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }
        if (!player.level().isClientSide && hand == InteractionHand.MAIN_HAND && player instanceof ServerPlayer serverPlayer) {
            int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
            IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
            boolean hasActiveBlood = volume != null && volume.isActive();

            boolean hasClaimedThisHeart = TempleOathRules.hasClaimedHeartFrom(player, this.getUUID());
            DialogueTree tree = HarbingerHermitDialogueTrees.forDegree(
                    degree, hasActiveBlood, hasClaimedThisHeart, this.getId());
            tree = DialogueHubFactory.decorate(tree, "hermit", serverPlayer);

            PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }
}
