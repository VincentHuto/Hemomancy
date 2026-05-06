package com.vincenthuto.hemomancy.common.entity.projectile;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.FargoneEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.AbhorentThoughtEntity;
import com.vincenthuto.hemomancy.common.entity.summon.IBloodConstruct;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class HemolyticVialEntity extends ThrowableItemProjectile {

    private static final float BURST_RADIUS = 4.0f;
    private static final float HARBINGER_DAMAGE = 10.0f;
    private static final float BLOOD_MOB_DAMAGE = 8.0f;
    private static final float CONSTRUCT_DAMAGE = 14.0f;

    public HemolyticVialEntity(EntityType<? extends HemolyticVialEntity> type, Level level) {
        super(type, level);
    }

    public HemolyticVialEntity(Level level, LivingEntity shooter) {
        super(EntityInit.hemolytic_vial_projectile.get(), shooter, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ItemInit.hemolytic_vial.get();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!level().isClientSide) {
            Vec3 impactPos = hitResult.getLocation();
            applyBurstEffects(impactPos);
            spawnBurstParticles((ServerLevel) level(), impactPos);
            level().playSound(null, blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.2f, 0.7f);
        }
        this.remove(RemovalReason.KILLED);
    }

    private void applyBurstEffects(Vec3 impactPos) {
        AABB area = new AABB(
                impactPos.x - BURST_RADIUS, impactPos.y - BURST_RADIUS, impactPos.z - BURST_RADIUS,
                impactPos.x + BURST_RADIUS, impactPos.y + BURST_RADIUS, impactPos.z + BURST_RADIUS);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity target : targets) {
            if (target == getOwner()) continue;

            if (target instanceof IBloodConstruct || target instanceof AbhorentThoughtEntity || target instanceof FargoneEntity) {
                target.hurt(target.damageSources().magic(), CONSTRUCT_DAMAGE);
                target.addEffect(new MobEffectInstance(EffectInit.hemolysis, 400, 1));
            } else if (isHarbingerPlayer(target)) {
                target.hurt(target.damageSources().magic(), HARBINGER_DAMAGE);
                target.addEffect(new MobEffectInstance(EffectInit.hemolysis, 300, 1));
                target.addEffect(new MobEffectInstance(EffectInit.blood_loss, 200, 1));
            } else if (hasActiveBloodVolume(target)) {
                target.hurt(target.damageSources().magic(), BLOOD_MOB_DAMAGE);
                target.addEffect(new MobEffectInstance(EffectInit.hemolysis, 200, 0));
            } else {
                // Cleanse blood effects from uninfected bystanders
                target.removeEffect(EffectInit.blood_loss);
            }
        }
    }

    private boolean isHarbingerPlayer(LivingEntity entity) {
        if (!(entity instanceof Player player)) return false;
        return HemoCapabilityAccess.getInitiatoryDegree(player)
                .map(d -> d.getDegreeNumber() > 0)
                .orElse(false);
    }

    private boolean hasActiveBloodVolume(LivingEntity entity) {
        return HemoCapabilityAccess.getBloodVolume(entity)
                .map(IBloodVolume::isActive)
                .orElse(false);
    }

    private void spawnBurstParticles(ServerLevel sLevel, Vec3 impactPos) {
        var glowData = GlowParticleFactory.createData(new ParticleColor(200, 235, 255));
        emitSphericalBurst(sLevel, impactPos, glowData, 140, 1.0f, 0.12f);
        emitSphericalBurst(sLevel, impactPos, ParticleTypes.SPLASH, 70, 0.9f, 0.085f);
        emitSphericalBurst(sLevel, impactPos, ParticleTypes.SNOWFLAKE, 40, 0.85f, 0.06f);
    }

    private void emitSphericalBurst(ServerLevel sLevel, Vec3 center, ParticleOptions particle,
                                    int count, float radiusScale, float velocityScale) {
        RandomSource random = sLevel.random;
        for (int i = 0; i < count; i++) {
            Vec3 offset = randomPointInSphere(random, BURST_RADIUS * radiusScale);
            Vec3 direction = offset.lengthSqr() > 1.0E-4 ? offset.normalize() : Vec3.ZERO;
            double velocityRamp = 0.4 + random.nextDouble() * 0.6;
            sLevel.sendParticles(
                    particle,
                    center.x + offset.x,
                    center.y + offset.y,
                    center.z + offset.z,
                    0,
                    direction.x * velocityScale * velocityRamp,
                    direction.y * velocityScale * velocityRamp,
                    direction.z * velocityScale * velocityRamp,
                    1.0);
        }
    }

    private Vec3 randomPointInSphere(RandomSource random, float radius) {
        double x;
        double y;
        double z;
        do {
            x = random.nextDouble() * 2.0 - 1.0;
            y = random.nextDouble() * 2.0 - 1.0;
            z = random.nextDouble() * 2.0 - 1.0;
        } while (x * x + y * y + z * z > 1.0);
        return new Vec3(x * radius, y * radius, z * radius);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            level().addParticle(
                    GlowParticleFactory.createData(new ParticleColor(180, 225, 255)),
                    getX() + HLParticleUtils.inRange(-0.15, 0.15),
                    getY() + HLParticleUtils.inRange(-0.15, 0.15),
                    getZ() + HLParticleUtils.inRange(-0.15, 0.15),
                    0, 0.01, 0);
        }
    }
}
