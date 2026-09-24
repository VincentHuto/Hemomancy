package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.neoforged.neoforge.entity.PartEntity;

final class NaeglerophaeonGripPart extends PartEntity<NaeglerophaeonEntity> {
    NaeglerophaeonGripPart(NaeglerophaeonEntity parent) { super(parent); refreshDimensions(); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) {}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {}
    @Override protected void addAdditionalSaveData(CompoundTag tag) {}
    @Override public boolean shouldBeSaved() { return false; }
    @Override public boolean isPickable() { return getParent().isAlive() && getParent().isGrabbing(); }
    @Override public boolean hurt(DamageSource source,float amount) { return isPickable() && getParent().hurt(source,amount); }
    @Override public boolean is(Entity e) { return e==this || e==getParent(); }
    @Override public EntityDimensions getDimensions(Pose pose) { return EntityDimensions.scalable(.75F,.65F); }
}
