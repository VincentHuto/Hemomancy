package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.projectile.SanguisLanceaEntity;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class StainedPriestessEntity extends Monster {
    private static final EntityDataAccessor<Integer> DATA_VISUAL_STATE =
            SynchedEntityData.defineId(StainedPriestessEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_VISUAL_TICKS =
            SynchedEntityData.defineId(StainedPriestessEntity.class, EntityDataSerializers.INT);

    public static final int VISUAL_NONE = 0;
    public static final int VISUAL_LANCE = 1;
    public static final int VISUAL_LUNGE = 2;
    public static final int VISUAL_PRESSURE_BLOOM = 3;

    private static final int LANCE_INTERVAL = 70;
    private static final int LUNGE_INTERVAL = 100;
    private static final int AREA_PRESSURE_INTERVAL = 85;
    private static final int BLOOD_DRAIN = 300;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.stained_priestess"),
            BossEvent.BossBarColor.RED,
            BossEvent.BossBarOverlay.NOTCHED_12);

    public StainedPriestessEntity(EntityType<? extends StainedPriestessEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 180;
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 420.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.32D)
                .add(Attributes.ATTACK_DAMAGE, 12.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.ARMOR, 10.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VISUAL_STATE, VISUAL_NONE);
        builder.define(DATA_VISUAL_TICKS, 0);
    }

    public int getVisualState() {
        return this.entityData.get(DATA_VISUAL_STATE);
    }

    public int getVisualTicks() {
        return this.entityData.get(DATA_VISUAL_TICKS);
    }

    private void setVisualState(int visualState, int ticks) {
        this.entityData.set(DATA_VISUAL_STATE, visualState);
        this.entityData.set(DATA_VISUAL_TICKS, Math.max(0, ticks));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }

        ServerLevel server = (ServerLevel) this.level();
        tickVisualState();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.tickCount % 10 == 0) {
            server.sendParticles(ParticleTypes.FALLING_LAVA, this.getX(), this.getY() + 1.0D, this.getZ(),
                    5, 0.3D, 0.5D, 0.3D, 0.01D);
        }

        LivingEntity target = this.getTarget();
        if (target != null && target.isAlive()) {
            if (this.tickCount % LANCE_INTERVAL == 15) {
                throwBloodLance(server, target);
            }
            if (this.tickCount % LUNGE_INTERVAL == 30) {
                lungeAt(target);
            }
        }

        if (this.tickCount % AREA_PRESSURE_INTERVAL == 45) {
            bloodPressureBloom(server);
        }
    }

    private void tickVisualState() {
        int ticks = getVisualTicks();
        if (ticks > 0) {
            this.entityData.set(DATA_VISUAL_TICKS, ticks - 1);
            if (ticks == 1) {
                this.entityData.set(DATA_VISUAL_STATE, VISUAL_NONE);
            }
        }
    }

    private void throwBloodLance(ServerLevel server, LivingEntity target) {
        setVisualState(VISUAL_LANCE, 18);
        ItemStack stack = new ItemStack(ItemInit.annettas_sanguis_lancea.get());
        SanguisLanceaEntity lance = new SanguisLanceaEntity(server, this, stack);
        lance.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
        lance.setPos(this.getX(), this.getEyeY() - 0.1D, this.getZ());
        Vec3 delta = target.getEyePosition().subtract(lance.position());
        lance.shoot(delta.x, delta.y, delta.z, 1.35F, 4.0F);
        server.addFreshEntity(lance);
        sendParticleLine(server, lance.position(), target.getEyePosition(), ParticleTypes.FALLING_LAVA, 10);
        server.playSound(null, this.blockPosition(), SoundEvents.TRIDENT_THROW.value(), SoundSource.HOSTILE, 1.0F, 0.55F);
    }

    private void lungeAt(LivingEntity target) {
        setVisualState(VISUAL_LUNGE, 14);
        Vec3 delta = target.position().subtract(this.position());
        if (delta.lengthSqr() > 0.001D) {
            this.setDeltaMovement(this.getDeltaMovement().add(delta.normalize().scale(0.75D)).add(0.0D, 0.12D, 0.0D));
            this.hasImpulse = true;
            if (this.level() instanceof ServerLevel server) {
                sendParticleLine(server, this.position().add(0.0D, 0.8D, 0.0D), target.position().add(0.0D, 0.8D, 0.0D), ParticleTypes.DRIPPING_LAVA, 8);
            }
        }
    }

    private void bloodPressureBloom(ServerLevel server) {
        setVisualState(VISUAL_PRESSURE_BLOOM, 16);
        AABB area = new AABB(this.blockPosition()).inflate(7.0D);
        sendFloorRing(server, 7.0D, ParticleTypes.FALLING_LAVA, 36);
        server.sendParticles(ParticleTypes.DRIPPING_LAVA, this.getX(), this.getY() + 1.0D, this.getZ(),
                70, 3.0D, 0.7D, 3.0D, 0.03D);
        server.playSound(null, this.blockPosition(), SoundEvents.GENERIC_SPLASH, SoundSource.HOSTILE, 1.0F, 0.45F);
        for (Player player : server.getEntitiesOfClass(Player.class, area)) {
            player.hurt(this.damageSources().magic(), 6.0F);
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1, false, true));
        }
    }

    private void sendFloorRing(ServerLevel server, double radius, net.minecraft.core.particles.SimpleParticleType particle, int points) {
        for (int i = 0; i < points; i++) {
            double angle = i * Math.PI * 2.0D / points;
            double x = this.getX() + Math.cos(angle) * radius;
            double z = this.getZ() + Math.sin(angle) * radius;
            server.sendParticles(particle, x, this.getY() + 0.12D, z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
        }
    }

    private void sendParticleLine(ServerLevel server, Vec3 from, Vec3 to, net.minecraft.core.particles.SimpleParticleType particle, int points) {
        Vec3 delta = to.subtract(from);
        for (int i = 1; i <= points; i++) {
            Vec3 point = from.add(delta.scale(i / (double) (points + 1)));
            server.sendParticles(particle, point.x, point.y, point.z, 1, 0.015D, 0.015D, 0.015D, 0.0D);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && target instanceof Player player && !this.level().isClientSide) {
            HemoCapabilityAccess.getBloodVolume(player).ifPresent(volume -> {
                if (volume.isActive() && volume.getBloodVolume() >= BLOOD_DRAIN) {
                    volume.setBloodVolume(volume.getBloodVolume() - BLOOD_DRAIN);
                    this.heal(3.0F);
                }
            });
        }
        return result;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(new ItemStack(ItemInit.annettas_sanguis_lancea.get()));
        this.spawnAtLocation(new ItemStack(ItemInit.hematic_iron_scrap.get(), 4));
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.ENDERMAN_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDERMAN_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.ENDERMAN_DEATH;
    }
}
