package com.vincenthuto.hemomancy.common.entity.projectile;

import java.util.List;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.entity.summon.IBloodConstruct;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hutoslib.client.particle.factory.GlowParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

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
            applyBurstEffects();
            level().playSound(null, blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 1.2f, 0.7f);
        } else {
            spawnBurstParticles();
        }
        this.remove(RemovalReason.KILLED);
    }

    private void applyBurstEffects() {
        AABB area = new AABB(
                getX() - BURST_RADIUS, getY() - BURST_RADIUS, getZ() - BURST_RADIUS,
                getX() + BURST_RADIUS, getY() + BURST_RADIUS, getZ() + BURST_RADIUS);
        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity target : targets) {
            if (target == getOwner()) continue;

            if (target instanceof IBloodConstruct) {
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

    private void spawnBurstParticles() {
        for (int i = 0; i < 40; i++) {
            level().addParticle(
                    GlowParticleFactory.createData(new ParticleColor(200, 235, 255)),
                    getX() + HLParticleUtils.inRange(-BURST_RADIUS, BURST_RADIUS),
                    getY() + HLParticleUtils.inRange(0.0, BURST_RADIUS),
                    getZ() + HLParticleUtils.inRange(-BURST_RADIUS, BURST_RADIUS),
                    0, 0.015, 0);
        }
        for (int i = 0; i < 20; i++) {
            level().addParticle(ParticleTypes.SPLASH,
                    getX() + HLParticleUtils.inRange(-2.0, 2.0),
                    getY() + HLParticleUtils.inRange(-0.5, 0.5),
                    getZ() + HLParticleUtils.inRange(-2.0, 2.0),
                    0, 0.05, 0);
        }
        for (int i = 0; i < 10; i++) {
            level().addParticle(ParticleTypes.SNOWFLAKE,
                    getX() + HLParticleUtils.inRange(-1.5, 1.5),
                    getY() + HLParticleUtils.inRange(0.0, 2.0),
                    getZ() + HLParticleUtils.inRange(-1.5, 1.5),
                    0, 0.03, 0);
        }
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
