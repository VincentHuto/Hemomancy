package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A quiet deep-water swimmer which folds its barbed arm web around its mantle when hurt. */
public class VampireSquidEntity extends PelagicAnimal {
    private Vec3 retreat = Vec3.ZERO;
    private float cloak, previousCloak;

    public VampireSquidEntity(EntityType<? extends VampireSquidEntity> type, Level level) { super(type, level); }
    @Override public Species habitat() { return Species.VAMPIRE_SQUID; }
    public boolean isCloaked() { return actionTicks() > 0; }
    public float cloakAmount(float partialTick) { return Mth.lerp(partialTick, previousCloak, cloak); }

    @Override public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && isAlive() && !level().isClientSide && actionCooldown == 0) {
            actFor(60);
            actionCooldown = 100;
            navigation.stop();
            if (source.getEntity() instanceof LivingEntity attacker)
                retreat = position().subtract(attacker.position()).multiply(1, 0, 1).normalize().scale(.025);
        }
        return hurt;
    }

    @Override public void tick() {
        super.tick();
        previousCloak = cloak;
        cloak = Mth.clamp(cloak + (isCloaked() ? .1F : -.06F), 0, 1);
        if (!level().isClientSide) {
            if (isCloaked() && isInWater() && !isNoAi()) setDeltaMovement(getDeltaMovement().add(retreat));
            else if (!isCloaked()) retreat = Vec3.ZERO;
        }
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("SquidRetreatX", retreat.x); tag.putDouble("SquidRetreatZ", retreat.z);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Jar render copies advance visual age without ticking the stored entity.
        previousCloak = cloak = isCloaked() ? 1 : 0;
        retreat = new Vec3(tag.getDouble("SquidRetreatX"), 0, tag.getDouble("SquidRetreatZ"));
    }
}
