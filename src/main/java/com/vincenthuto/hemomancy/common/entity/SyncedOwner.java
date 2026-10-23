package com.vincenthuto.hemomancy.common.entity;

import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

/**
 * O-3: the synced owner UUID an owned entity carries, modelled on {@code BoundSummonSync}. Accessor ids are
 * allocated per entity class, so each entity defines its own instance as a static field where its old accessor
 * stood. The owner is saved under {@code "Owner"} when present.
 */
public record SyncedOwner(EntityDataAccessor<Optional<UUID>> accessor) {
	public static final String NBT_KEY = "Owner";

	public static SyncedOwner define(Class<? extends Entity> type) {
		return new SyncedOwner(SynchedEntityData.defineId(type, EntityDataSerializers.OPTIONAL_UUID));
	}

	public void defineDefault(SynchedEntityData.Builder builder) {
		builder.define(accessor, Optional.empty());
	}

	@Nullable
	public UUID get(Entity entity) {
		return entity.getEntityData().get(accessor).orElse(null);
	}

	public void set(Entity entity, @Nullable UUID owner) {
		entity.getEntityData().set(accessor, Optional.ofNullable(owner));
	}

	public void save(Entity entity, CompoundTag tag) {
		UUID owner = get(entity);
		if (owner != null) tag.putUUID(NBT_KEY, owner);
	}

	public void load(Entity entity, CompoundTag tag) {
		if (tag.hasUUID(NBT_KEY)) set(entity, tag.getUUID(NBT_KEY));
	}
}
