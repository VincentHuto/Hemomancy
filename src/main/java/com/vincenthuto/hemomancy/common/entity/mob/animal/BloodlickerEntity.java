package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteThreatRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Rite-only blood feeder. It exists to siphon consecrated anchors and cannot
 * breed, tempt, or enter the ordinary ecosystem.
 */
public final class BloodlickerEntity extends LeechEntity {
	public static final int MAX_STORED_BLOOD = 100;
	private static final String STORED_BLOOD_TAG = "StoredBlood";
	private static final EntityDataAccessor<Integer> STORED_BLOOD =
			SynchedEntityData.defineId(BloodlickerEntity.class, EntityDataSerializers.INT);

	public BloodlickerEntity(EntityType<? extends BloodlickerEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder setAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, CardinalRiteThreatRules.BLOODLICKER_MAX_HEALTH)
				.add(Attributes.MOVEMENT_SPEED, 0.22D);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(STORED_BLOOD, 0);
	}

	public void addSiphonedBlood(int amount) {
		if (amount > 0) {
			this.entityData.set(STORED_BLOOD, Math.min(MAX_STORED_BLOOD, getStoredBlood() + amount));
		}
	}

	public int getStoredBlood() {
		return this.entityData.get(STORED_BLOOD);
	}

	public float getBloodFullness() {
		return getStoredBlood() / (float) MAX_STORED_BLOOD;
	}

	@Override
	public void addAdditionalSaveData(CompoundTag tag) {
		super.addAdditionalSaveData(tag);
		tag.putInt(STORED_BLOOD_TAG, getStoredBlood());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag tag) {
		super.readAdditionalSaveData(tag);
		this.entityData.set(STORED_BLOOD, Math.clamp(tag.getInt(STORED_BLOOD_TAG), 0, MAX_STORED_BLOOD));
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return false;
	}

	@Override
	public LeechEntity getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}
}
