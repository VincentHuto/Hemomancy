package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.particle.data.HitColorParticleData;
import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class HagfishEntity extends PelagicAnimal {
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> FEEDING = net.minecraft.network.syncher.SynchedEntityData.defineId(HagfishEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private int feedingTicks;
    private Vec3 retreat = Vec3.ZERO;
    public HagfishEntity(EntityType<? extends HagfishEntity> type, Level level) { super(type, level); }
    @Override public Species habitat() { return Species.HAGFISH; }
    @Override public boolean bottomDweller() { return true; }
    @Override public int getMaxSpawnClusterSize() { return 4; }
    public boolean isSliming() { return actionTicks() > 0; }
    public int feedingTicks() { return feedingTicks; }
    public boolean isFeeding() { return entityData.get(FEEDING); }
    @Override protected boolean mayWander() { return super.mayWander() && !isFeeding(); }
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); builder.define(FEEDING, false);
    }
    @Override public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && isAlive() && !level().isClientSide && actionCooldown == 0) {
            actFor(40); actionCooldown = 200; navigation.stop();
            if (source.getEntity() instanceof LivingEntity attacker) {
                if (distanceToSqr(attacker) <= 9) attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 1));
                retreat = position().subtract(attacker.position()).multiply(1, 0, 1).normalize().scale(.035);
            }
            ((ServerLevel)level()).sendParticles(new HitColorParticleData(.58F, .48F, .62F),
                    getX(), getY() + .2, getZ(), 6, .45, .12, .45, .01);
        }
        return hurt;
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide) {
            if (feedingTicks > 0 && --feedingTicks == 0) entityData.set(FEEDING, false);
            if (isSliming()) setDeltaMovement(getDeltaMovement().add(retreat));
            else retreat = Vec3.ZERO;
        }
    }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (!stack.is(Items.ROTTEN_FLESH) || feedingTicks > 0) return super.mobInteract(player, hand);
        if (!level().isClientSide) {
            stack.consume(1, player); heal(2); feedingTicks = 80; entityData.set(FEEDING, true); navigation.stop();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putInt("FeedingTicks", feedingTicks);
        tag.putDouble("RetreatX", retreat.x); tag.putDouble("RetreatZ", retreat.z);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag); feedingTicks = Math.max(0, tag.getInt("FeedingTicks"));
        entityData.set(FEEDING, feedingTicks > 0);
        retreat = new Vec3(tag.getDouble("RetreatX"), 0, tag.getDouble("RetreatZ"));
    }
}
