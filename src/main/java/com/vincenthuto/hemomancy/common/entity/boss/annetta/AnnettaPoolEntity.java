package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * What is knocked out of Annetta lies where it falls: yellow-orange infected blood that opens wounds, white-blue
 * Unstained humor that weighs on anyone whose blood is awake, and the dark pool that reanimates her body.
 */
public class AnnettaPoolEntity extends Entity {
    public enum Kind { INFECTED, HUMOR, REANIMATION }

    private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(AnnettaPoolEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Float> RADIUS = SynchedEntityData.defineId(AnnettaPoolEntity.class, EntityDataSerializers.FLOAT);
    private static final ParticleColor ICHOR = new ParticleColor(214, 150, 48);
    private static final ParticleColor HUMOR = new ParticleColor(196, 228, 246);
    private static final ParticleColor BLOOD = new ParticleColor(120, 10, 22);
    /** A fight lays down a lot; the oldest pools give way so the floor never fills with entities. */
    public static final int MAX_PER_ENCOUNTER = 14;

    private int life;
    private float growFrom;
    private float growTo;
    private int growTicks;
    private int grown;

    public AnnettaPoolEntity(EntityType<? extends AnnettaPoolEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static AnnettaPoolEntity spawn(ServerLevel level, Vec3 at, Kind kind, float radius, int lifeTicks) {
        return spawnGrowing(level, at, kind, radius, radius, 0, lifeTicks);
    }

    public static AnnettaPoolEntity spawnGrowing(ServerLevel level, Vec3 at, Kind kind, float from, float to, int growTicks,
            int lifeTicks) {
        AnnettaPoolEntity pool = new AnnettaPoolEntity(EntityInit.annetta_pool.get(), level);
        pool.entityData.set(KIND, (byte) kind.ordinal());
        pool.entityData.set(RADIUS, from);
        pool.growFrom = from;
        pool.growTo = to;
        pool.growTicks = growTicks;
        pool.life = lifeTicks;
        pool.setYRot(level.random.nextFloat() * 360.0F);
        pool.moveTo(at.x, at.y + 0.01D, at.z, pool.getYRot(), 0.0F);
        trimOldest(level, at);
        level.addFreshEntity(pool);
        return pool;
    }

    private static void trimOldest(ServerLevel level, Vec3 at) {
        var pools = level.getEntitiesOfClass(AnnettaPoolEntity.class, new AABB(at, at).inflate(48.0D),
                pool -> pool.kind() != Kind.REANIMATION);
        if (pools.size() < MAX_PER_ENCOUNTER) return;
        pools.stream().min((a, b) -> Integer.compare(a.life, b.life)).ifPresent(Entity::discard);
    }

    public Kind kind() {
        return Kind.values()[Math.floorMod(this.entityData.get(KIND), Kind.values().length)];
    }

    public float radius() {
        return this.entityData.get(RADIUS);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, (byte) 0);
        builder.define(RADIUS, 1.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            tickClient();
            return;
        }
        if (grown < growTicks) {
            grown++;
            float t = HemoEasing.smoothstep(grown / (float) growTicks);
            this.entityData.set(RADIUS, growFrom + (growTo - growFrom) * t);
        }
        if (--life <= 0) {
            discard();
            return;
        }
        if (this.tickCount % 20 == 0 && this.level() instanceof ServerLevel server) affect(server);
    }

    private void tickClient() {
        float r = radius();
        Kind kind = kind();
        if (this.random.nextInt(kind == Kind.HUMOR ? 3 : 5) != 0) return;
        double a = this.random.nextDouble() * Math.PI * 2.0D, d = Math.sqrt(this.random.nextDouble()) * r * 0.9D;
        var particle = switch (kind) {
            // Mostly blood rising off it, now and then a bead of the infection.
            case INFECTED -> HemoParticleData.bloodCell(this.random.nextInt(4) == 0 ? ICHOR : BLOOD);
            case HUMOR -> HemoParticleData.glow(HUMOR);
            case REANIMATION -> HemoParticleData.bloodCell(BLOOD);
        };
        this.level().addParticle(particle, getX() + Math.cos(a) * d, getY() + 0.05D, getZ() + Math.sin(a) * d,
                0.0D, kind == Kind.HUMOR ? 0.02D : 0.005D, 0.0D);
    }

    private void affect(ServerLevel server) {
        Kind kind = kind();
        if (kind == Kind.REANIMATION) return;
        float r = radius();
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(r, 0.6D, r),
                e -> e.isAlive() && e.distanceToSqr(this) <= r * r + 0.6D && !isEncounterBody(e))) {
            if (kind == Kind.INFECTED) {
                victim.addEffect(new MobEffectInstance(EffectInit.blood_loss, 60, 0, false, true));
            } else if (victim instanceof Player player && bloodActive(player)) {
                // Still humor weighs on blood that is awake.
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 0, false, true));
                victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 0, false, true));
                victim.hurt(damageSources().magic(), 1.0F);
            }
        }
    }

    private static boolean isEncounterBody(LivingEntity entity) {
        return entity instanceof AnnettaKnowlesEntity || entity instanceof StainedPriestessEntity
                || entity instanceof LatentAnnettaInfectionEntity;
    }

    private static boolean bloodActive(Player player) {
        return HemoCapabilityAccess.getBloodVolume(player).isActive();
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    // Pools are the residue of a fight in progress; they are never written to disk.
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
