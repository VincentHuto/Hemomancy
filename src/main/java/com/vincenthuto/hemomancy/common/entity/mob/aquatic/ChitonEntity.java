package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

import com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.Species;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ChitonEntity extends PelagicAnimal {
    public ChitonEntity(EntityType<? extends ChitonEntity> type, Level level) {
        super(type, level); moveControl = new MoveControl(this);
    }
    @Override public Species habitat() { return Species.CHITON; }
    @Override public boolean bottomDweller() { return true; }
    @Override public int getMaxSpawnClusterSize() { return 4; }
    @Override protected PathNavigation createNavigation(Level level) { return new AmphibiousPathNavigation(this, level); }
    @Override protected void handleAirSupply(int air) { setAirSupply(300); }
    public boolean isClamped() { return actionTicks() > 0; }
    public boolean isGrazing() {
        return !isClamped() && tickCount % 160 < 50 && onGround()
                && com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitat.nearBlock(level(), blockPosition(), 1, 0,
                        state -> state.is(com.vincenthuto.hemomancy.common.init.BlockInit.hematic_algal_crust.get()));
    }
    public void clamp() { actFor(80); navigation.stop(); setDeltaMovement(Vec3.ZERO); }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount % 10 == 0) {
            var player = level().getNearestPlayer(this, 2);
            if (player != null && !player.isCreative() && !player.isSpectator()) clamp();
        }
    }
    @Override public void travel(Vec3 input) {
        if (isClamped()) { setDeltaMovement(0, isInWater() ? -.015 : getDeltaMovement().y, 0); input = Vec3.ZERO; }
        super.travel(input);
    }
    @Override public boolean hurt(DamageSource source, float amount) {
        boolean clamped = isClamped();
        if (!level().isClientSide) clamp();
        return super.hurt(source, clamped ? amount * .3F : amount);
    }
}
