package com.vincenthuto.hemomancy.common.entity.utility;

import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.UUID;

public final class UnsettledIchorEntity extends Entity {
	private static final EntityDataAccessor<Integer> BLOOD_ML =
			SynchedEntityData.defineId(UnsettledIchorEntity.class, EntityDataSerializers.INT);
	private UUID riteCaster;

	public UnsettledIchorEntity(EntityType<? extends UnsettledIchorEntity> type, Level level) {
		super(type, level);
	}

	public void initialize(UUID caster, int bloodMl) {
		riteCaster = caster;
		entityData.set(BLOOD_ML, Math.max(1, bloodMl));
	}

	public int getBloodMl() {
		return entityData.get(BLOOD_ML);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(BLOOD_ML, 1);
	}

	@Override
	public void tick() {
		super.tick();
		if (level().isClientSide) return;
		if (tickCount >= CardinalRiteCeremonyRules.ICHOR_TTL_TICKS || riteCaster == null) {
			discard();
			return;
		}
		setDeltaMovement(getDeltaMovement().scale(0.92D).add(0.0D, -0.025D, 0.0D));
		move(MoverType.SELF, getDeltaMovement());
		if (onGround()) setDeltaMovement(getDeltaMovement().multiply(0.7D, -0.2D, 0.7D));

		ServerLevel server = (ServerLevel) level();
		ActiveCardinalRite rite = CardinalRiteSavedData.get(server).getRite(riteCaster);
		if (rite == null) {
			discard();
			return;
		}
		AABB pickup = getBoundingBox().inflate(0.65D);
		Player player = server.getEntitiesOfClass(Player.class, pickup,
				candidate -> candidate.getUUID().equals(riteCaster)
						|| rite.getAllyRoles().containsKey(candidate.getUUID()))
				.stream().findFirst().orElse(null);
		if (player != null) {
			rite.carryIchor(getBloodMl());
			CardinalRiteSavedData.get(server).setDirty();
			server.playSound(null, blockPosition(), SoundEvents.SLIME_SQUISH_SMALL,
					SoundSource.PLAYERS, 0.6F, 0.8F);
			discard();
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag tag) {
		if (tag.hasUUID("RiteCaster")) riteCaster = tag.getUUID("RiteCaster");
		entityData.set(BLOOD_ML, Math.max(1, tag.getInt("BloodMl")));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag tag) {
		if (riteCaster != null) tag.putUUID("RiteCaster", riteCaster);
		tag.putInt("BloodMl", getBloodMl());
	}
}
