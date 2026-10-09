package com.vincenthuto.hemomancy.common.entity.summon;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/**
 * The synced binding state every puppet body carries. Accessor ids are allocated per entity class, so each
 * body defines its own instance as a static field in the position its old per-class accessors held.
 */
public record BoundSummonSync(
		EntityDataAccessor<Optional<UUID>> owner,
		EntityDataAccessor<Optional<UUID>> crossbar,
		EntityDataAccessor<String> summonName,
		EntityDataAccessor<Integer> dismissalTicks,
		EntityDataAccessor<Boolean> trialSummon,
		EntityDataAccessor<Optional<UUID>> trialCaster) {

	public static BoundSummonSync define(Class<? extends Entity> type) {
		return new BoundSummonSync(
				SynchedEntityData.defineId(type, EntityDataSerializers.OPTIONAL_UUID),
				SynchedEntityData.defineId(type, EntityDataSerializers.OPTIONAL_UUID),
				SynchedEntityData.defineId(type, EntityDataSerializers.STRING),
				SynchedEntityData.defineId(type, EntityDataSerializers.INT),
				SynchedEntityData.defineId(type, EntityDataSerializers.BOOLEAN),
				SynchedEntityData.defineId(type, EntityDataSerializers.OPTIONAL_UUID));
	}

	public void defineDefaults(SynchedEntityData.Builder builder, String summonName) {
		builder.define(owner, Optional.empty());
		builder.define(crossbar, Optional.empty());
		builder.define(this.summonName, summonName);
		builder.define(dismissalTicks, 0);
		builder.define(trialSummon, false);
		builder.define(trialCaster, Optional.empty());
	}
}
