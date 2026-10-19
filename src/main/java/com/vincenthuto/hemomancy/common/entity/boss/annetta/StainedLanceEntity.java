package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * The lancea the Stained Priestess throws. It flies point first, runs through what it hits, and plants itself; the
 * blood under the spot wakes into a burst of thorns, and the spent spear comes apart and runs back to her.
 */
public class StainedLanceEntity extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> PLANTED_AT =
            SynchedEntityData.defineId(StainedLanceEntity.class, EntityDataSerializers.INT);
    private static final ParticleColor BLOOD = new ParticleColor(150, 14, 26);
    private static final int MAX_FLIGHT = 80;
    /** Planted, the spear holds while the thorns burst, then comes apart. */
    public static final int PLANTED_LIFE = PriestessCombatRules.ERUPTION_TELL + 22;
    private static final int TRAIL = 8;

    /** Recent flight positions, newest first, for the client's streak. */
    public final Vec3[] trail = new Vec3[TRAIL];

    public StainedLanceEntity(EntityType<? extends StainedLanceEntity> type, Level level) {
        super(type, level);
    }

    public StainedLanceEntity(Level level, LivingEntity owner) {
        super(EntityInit.stained_lance.get(), owner, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PLANTED_AT, -1);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.015D;
    }

    public boolean isPlanted() {
        return this.entityData.get(PLANTED_AT) >= 0;
    }

    /** Ticks since the spear planted itself, or -1 in flight. */
    public float plantedAge(float partialTick) {
        int at = this.entityData.get(PLANTED_AT);
        return at < 0 ? -1.0F : this.tickCount - at + partialTick;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof StainedPriestessEntity) && !(entity instanceof StainedLanceEntity)
                && !(entity instanceof PriestessEruptionEntity) && !(entity instanceof AnnettaPoolEntity);
    }

    @Override
    public void tick() {
        if (isPlanted()) {
            // Planted, it stays exactly where it stuck; only the age moves.
            this.baseTick();
            int age = (int) plantedAge(0.0F);
            if (this.level() instanceof ServerLevel server) {
                if (age == PriestessCombatRules.ERUPTION_TELL + 10) unravel(server);
                if (age >= PLANTED_LIFE) discard();
            }
            return;
        }
        float yaw = getYRot(), pitch = getXRot();
        super.tick();
        if (isPlanted()) {
            // It struck this tick: keep the heading it flew in on, not the zero of a stopped spear.
            setYRot(yaw);
            setXRot(pitch);
            this.yRotO = yaw;
            this.xRotO = pitch;
            return;
        }
        if (this.level().isClientSide) {
            System.arraycopy(trail, 0, trail, 1, TRAIL - 1);
            trail[0] = position();
            this.level().addParticle(HemoParticleData.bloodCell(BLOOD), getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        } else if (this.tickCount > MAX_FLIGHT) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;
        if (result.getEntity() instanceof LivingEntity target) {
            Entity owner = getOwner();
            if (target.hurt(owner instanceof LivingEntity living ? damageSources().mobProjectile(this, living)
                    : damageSources().magic(), 12.0F)) {
                Vec3 push = getDeltaMovement().multiply(1.0D, 0.0D, 1.0D).normalize();
                target.knockback(0.8D, -push.x, -push.z);
            }
            plant(target.position());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) return;
        // Driven a hand's width into whatever it struck.
        plant(result.getLocation().add(getDeltaMovement().normalize().scale(0.25D)));
    }

    private void plant(Vec3 at) {
        if (isPlanted() || !(this.level() instanceof ServerLevel server)) return;
        this.setPos(at.x, at.y, at.z);
        this.setDeltaMovement(Vec3.ZERO);
        this.setNoGravity(true);
        this.entityData.set(PLANTED_AT, this.tickCount);
        Entity owner = getOwner();
        PriestessEruptionEntity.spawn(server, owner instanceof LivingEntity living ? living : null, at,
                PriestessCombatRules.ERUPTION_TELL, false, 8.0F);
        server.sendParticles(HemoParticleData.bloodCell(BLOOD), at.x, at.y + 0.2D, at.z, 24, 0.3D, 0.2D, 0.3D, 0.15D);
        server.sendParticles(HemoParticleData.hitGlow(BLOOD), at.x, at.y + 0.3D, at.z, 3, 0.1D, 0.1D, 0.1D, 0.0D);
        server.playSound(null, blockPosition(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.HOSTILE, 1.4F, 0.6F);
    }

    /** The spent lancea comes apart and its blood streams home to her hand. */
    private void unravel(ServerLevel server) {
        Entity owner = getOwner();
        if (owner == null) return;
        Vec3 from = position().add(0.0D, 0.4D, 0.0D);
        Vec3 path = owner.position().add(0.0D, 1.3D, 0.0D).subtract(from);
        for (int i = 0; i < 18; i++) {
            Vec3 at = from.add(path.scale(i / 18.0D * 0.25D));
            Vec3 drift = path.scale(0.045D + this.random.nextDouble() * 0.02D);
            server.sendParticles(HemoParticleData.bloodCell(BLOOD), at.x, at.y, at.z, 0, drift.x, drift.y, drift.z, 1.0D);
        }
        server.playSound(null, blockPosition(), SoundEvents.TRIDENT_RETURN, SoundSource.HOSTILE, 0.8F, 0.8F);
    }
}
