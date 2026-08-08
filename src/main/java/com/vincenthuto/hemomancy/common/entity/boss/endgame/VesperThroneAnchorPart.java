package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.neoforged.neoforge.entity.PartEntity;

final class VesperThroneAnchorPart extends PartEntity<VesperTheCrownedRefusalEntity> {
	private final int anchorIndex;
	private final EntityDimensions dimensions = EntityDimensions.scalable(1.35F, 1.75F);

	VesperThroneAnchorPart(VesperTheCrownedRefusalEntity parent, int anchorIndex) {
		super(parent);
		this.anchorIndex = anchorIndex;
		refreshDimensions();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		return !isInvulnerableTo(source) && getParent().hurtAnchor(anchorIndex, source, amount);
	}

	@Override
	public boolean isPickable() {
		return getParent().getActiveAnchor() == anchorIndex;
	}

	@Override
	public boolean is(Entity entity) {
		return this == entity || getParent() == entity;
	}

	@Override
	public EntityDimensions getDimensions(Pose pose) {
		return dimensions.scale(VesperCombatRules.anchorHitboxScale(anchorIndex, getParent().getActiveAnchor()));
	}

	void refreshAnchorDimensions() {
		refreshDimensions();
	}

	@Override
	public boolean shouldBeSaved() {
		return false;
	}
}
