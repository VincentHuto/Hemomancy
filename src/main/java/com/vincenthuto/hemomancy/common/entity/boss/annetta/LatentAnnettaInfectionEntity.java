package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class LatentAnnettaInfectionEntity extends Monster {
    private static final int INFECTION_BLOOM_INTERVAL = 70;
    private static final int PRESSURE_SPIKE_INTERVAL = 110;

    private final ServerBossEvent bossEvent = new ServerBossEvent(
            Component.translatable("entity.hemomancy.latent_annetta_infection"),
            BossEvent.BossBarColor.WHITE,
            BossEvent.BossBarOverlay.NOTCHED_10);

    public LatentAnnettaInfectionEntity(EntityType<? extends LatentAnnettaInfectionEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
        this.xpReward = 160;
    }

    public static AttributeSupplier.Builder setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 360.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 10.0D)
                .add(Attributes.FOLLOW_RANGE, 48.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.85D)
                .add(Attributes.ARMOR, 8.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.05D, false));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.75D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
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
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        if (this.tickCount % 12 == 0) {
            server.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY() + 1.0D, this.getZ(),
                    8, 0.45D, 0.7D, 0.45D, 0.01D);
        }
        if (this.tickCount % INFECTION_BLOOM_INTERVAL == 20) {
            infectionBloom(server);
        }
        if (this.tickCount % PRESSURE_SPIKE_INTERVAL == 55) {
            pressureSpike(server);
        }
    }

    private void infectionBloom(ServerLevel server) {
        AABB area = new AABB(this.blockPosition()).inflate(6.0D);
        server.sendParticles(ParticleTypes.MYCELIUM, this.getX(), this.getY() + 0.8D, this.getZ(),
                85, 2.5D, 0.7D, 2.5D, 0.04D);
        server.playSound(null, this.blockPosition(), SoundEvents.SCULK_SHRIEKER_SHRIEK, SoundSource.HOSTILE, 1.0F, 0.7F);
        for (Player player : server.getEntitiesOfClass(Player.class, area)) {
            player.hurt(this.damageSources().magic(), 5.0F);
            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 120, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0, false, true));
        }
    }

    private void pressureSpike(ServerLevel server) {
        AABB area = new AABB(this.blockPosition()).inflate(9.0D);
        server.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0D, this.getZ(),
                55, 4.0D, 0.5D, 4.0D, 0.03D);
        for (Player player : server.getEntitiesOfClass(Player.class, area)) {
            player.hurt(this.damageSources().indirectMagic(this, this), 4.0F);
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true));
        }
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && target instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0, false, true));
        }
        return result;
    }

    @Override
    public void die(DamageSource source) {
        if (!this.level().isClientSide && this.level() instanceof ServerLevel server) {
            for (AnnettaKnowlesEntity annetta : server.getEntitiesOfClass(AnnettaKnowlesEntity.class,
                    new AABB(this.blockPosition()).inflate(32.0D))) {
                if (annetta.isCuredSupport()) {
                    annetta.markResolvedAfterCure();
                }
            }
        }
        super.die(source);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        this.spawnAtLocation(new ItemStack(ItemInit.annettas_absolution_dagger.get()));
        this.spawnAtLocation(new ItemStack(ItemInit.pale_silver_ingot.get(), 3));
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getAmbientSound() {
        return SoundEvents.WARDEN_AMBIENT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WARDEN_HURT;
    }

    @Override
    protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return SoundEvents.WARDEN_DEATH;
    }
}
