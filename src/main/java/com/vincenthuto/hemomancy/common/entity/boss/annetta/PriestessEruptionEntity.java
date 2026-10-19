package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * The blood under the floor answering the Priestess's spear: the ground cracks red for a beat, then thorned
 * tendrils burst up and throw whoever stands on it. The client draws the thorns; this only keeps time and hurts.
 */
public class PriestessEruptionEntity extends Entity {
    private static final EntityDataAccessor<Integer> DELAY =
            SynchedEntityData.defineId(PriestessEruptionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> MAJOR =
            SynchedEntityData.defineId(PriestessEruptionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final ParticleColor BLOOD = new ParticleColor(150, 14, 26);
    private static final ParticleColor ICHOR = new ParticleColor(214, 150, 48);

    @Nullable private UUID ownerId;
    private float damage = 8.0F;

    public PriestessEruptionEntity(EntityType<? extends PriestessEruptionEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /** Sets an eruption on the floor under {@code at}, bursting after {@code delay} ticks. */
    public static void spawn(ServerLevel level, @Nullable LivingEntity owner, Vec3 at, int delay, boolean major, float damage) {
        PriestessEruptionEntity eruption = new PriestessEruptionEntity(EntityInit.priestess_eruption.get(), level);
        Vec3 floor = floor(level, at);
        if (floor == null) return;
        eruption.setPos(floor.x, floor.y, floor.z);
        eruption.entityData.set(DELAY, Math.max(1, delay));
        eruption.entityData.set(MAJOR, major);
        eruption.ownerId = owner == null ? null : owner.getUUID();
        eruption.damage = damage;
        level.addFreshEntity(eruption);
    }

    /** The first floor within a couple of blocks of the point, or null over a drop. */
    @Nullable
    private static Vec3 floor(ServerLevel level, Vec3 at) {
        Vec3 from = at.add(0.0D, 1.5D, 0.0D);
        HitResult hit = level.clip(new ClipContext(from, at.subtract(0.0D, 3.0D, 0.0D), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS ? null : hit.getLocation();
    }

    public int delay() {
        return this.entityData.get(DELAY);
    }

    public boolean isMajor() {
        return this.entityData.get(MAJOR);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DELAY, PriestessCombatRules.ERUPTION_TELL);
        builder.define(MAJOR, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel server)) return;
        int delay = delay();
        if (this.tickCount < delay && this.tickCount % 3 == 0) {
            server.sendParticles(HemoParticleData.bloodCell(BLOOD), getX(), getY() + 0.05D, getZ(), 2, 0.4D, 0.0D, 0.4D, 0.01D);
        }
        if (this.tickCount == delay) burst(server);
        if (this.tickCount > delay + PriestessCombatRules.ERUPTION_LIFE) discard();
    }

    private void burst(ServerLevel server) {
        boolean major = isMajor();
        double radius = major ? 2.2D : 1.3D;
        Entity owner = ownerId == null ? null : server.getEntity(ownerId);
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(position(), position()).inflate(radius, 2.6D, radius),
                e -> e.isAlive() && e != owner && !(e instanceof StainedPriestessEntity))) {
            if (victim.getY() > getY() + 2.6D) continue;
            boolean hurt = victim.hurt(owner instanceof LivingEntity living ? damageSources().mobAttack(living) : damageSources().magic(), damage);
            if (hurt) {
                Vec3 out = victim.position().subtract(position()).multiply(1.0D, 0.0D, 1.0D);
                Vec3 push = out.lengthSqr() < 1.0E-4D ? Vec3.ZERO : out.normalize().scale(0.25D);
                victim.push(push.x, major ? 0.85D : 0.6D, push.z);
                victim.hurtMarked = true;
            }
        }
        int count = major ? 40 : 18;
        server.sendParticles(HemoParticleData.bloodCell(BLOOD), getX(), getY() + 0.6D, getZ(), count, 0.35D, 0.9D, 0.35D, 0.16D);
        server.sendParticles(HemoParticleData.bloodCell(ICHOR), getX(), getY() + 0.4D, getZ(), count / 3, 0.3D, 0.6D, 0.3D, 0.08D);
        server.sendParticles(HemoParticleData.hitGlow(BLOOD), getX(), getY() + 0.3D, getZ(), major ? 4 : 1, 0.2D, 0.1D, 0.2D, 0.0D);
        server.playSound(null, blockPosition(), SoundEvents.EVOKER_FANGS_ATTACK, SoundSource.HOSTILE, major ? 1.4F : 0.9F,
                0.55F + this.random.nextFloat() * 0.15F);
        server.playSound(null, blockPosition(), SoundEvents.SLIME_SQUISH, SoundSource.HOSTILE, 0.8F, 0.5F);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}
