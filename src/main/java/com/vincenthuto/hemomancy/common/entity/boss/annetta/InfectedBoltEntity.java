package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** A writhing knot the Stained Priestess spits from the air: a blood bolt that opens wounds, or a spore bolt that bursts. */
public class InfectedBoltEntity extends ThrowableProjectile {
    public enum Kind { BLOOD, SPORE }

    private static final EntityDataAccessor<Byte> KIND = SynchedEntityData.defineId(InfectedBoltEntity.class, EntityDataSerializers.BYTE);
    private static final ParticleColor BLOOD = new ParticleColor(150, 14, 26);
    private static final ParticleColor SPORE = new ParticleColor(214, 168, 70);
    private static final int MAX_AGE = 90;

    public InfectedBoltEntity(EntityType<? extends InfectedBoltEntity> type, Level level) {
        super(type, level);
    }

    public InfectedBoltEntity(Level level, LivingEntity owner, Kind kind) {
        super(EntityInit.infected_bolt.get(), owner, level);
        this.entityData.set(KIND, (byte) kind.ordinal());
    }

    public Kind kind() {
        return Kind.values()[Math.floorMod(this.entityData.get(KIND), Kind.values().length)];
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, (byte) 0);
    }

    @Override
    protected double getDefaultGravity() {
        return kind() == Kind.SPORE ? 0.02D : 0.0D;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !(entity instanceof StainedPriestessEntity)
                && !(entity instanceof AnnettaPoolEntity);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            var trail = kind() == Kind.BLOOD ? HemoParticleData.bloodCell(BLOOD) : HemoParticleData.sporiticSpore(SPORE);
            this.level().addParticle(trail, getX(), getY() + 0.1D, getZ(), 0.0D, 0.0D, 0.0D);
        } else if (this.tickCount > MAX_AGE) {
            discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide || kind() == Kind.SPORE) return;
        if (result.getEntity() instanceof LivingEntity target) {
            Entity owner = getOwner();
            if (target.hurt(owner instanceof LivingEntity living ? damageSources().mobProjectile(this, living)
                    : damageSources().magic(), 6.0F)) {
                target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 80, 0, false, true));
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(this.level() instanceof ServerLevel server)) return;
        if (kind() == Kind.SPORE) burst(server);
        else server.sendParticles(HemoParticleData.bloodCell(BLOOD), getX(), getY(), getZ(), 14, 0.25D, 0.25D, 0.25D, 0.05D);
        discard();
    }

    /** Spores burst on whatever they strike, sickening anyone in the cloud. */
    private void burst(ServerLevel server) {
        server.sendParticles(HemoParticleData.sporiticSpore(SPORE), getX(), getY() + 0.2D, getZ(), 30, 1.0D, 0.5D, 1.0D, 0.02D);
        server.playSound(null, blockPosition(), SoundEvents.FUNGUS_BREAK, SoundSource.HOSTILE, 1.0F, 0.6F);
        Entity owner = getOwner();
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(blockPosition()).inflate(2.2D),
                e -> e.isAlive() && e != owner && !(e instanceof StainedPriestessEntity))) {
            victim.hurt(damageSources().magic(), 3.0F);
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0, false, true));
            victim.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 100, 0, false, true));
        }
    }
}
