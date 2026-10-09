package com.vincenthuto.hemomancy.common.entity.summon;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;

/** A puppet body whose binding state lives in its {@link BoundSummonSync} entity data. */
public interface SyncedBoundSummon extends BoundPuppeteerSummon {
	BoundSummonSync hemomancy$sync();

	private SynchedEntityData data() {
		return ((Entity) this).getEntityData();
	}

	@Override default UUID hemomancy$getOwnerUUID() { return data().get(hemomancy$sync().owner()).orElse(null); }
	@Override default void hemomancy$setOwnerUUID(UUID ownerUuid) { data().set(hemomancy$sync().owner(), Optional.ofNullable(ownerUuid)); }
	@Override default UUID hemomancy$getCrossbarUUID() { return data().get(hemomancy$sync().crossbar()).orElse(null); }
	@Override default void hemomancy$setCrossbarUUID(UUID crossbarUuid) { data().set(hemomancy$sync().crossbar(), Optional.ofNullable(crossbarUuid)); }
	@Override default String hemomancy$getSummonName() { return data().get(hemomancy$sync().summonName()); }
	@Override default void hemomancy$setSummonName(String summonName) { data().set(hemomancy$sync().summonName(), summonName == null ? "" : summonName); }
	@Override default int hemomancy$getDismissalTicks() { return data().get(hemomancy$sync().dismissalTicks()); }
	@Override default void hemomancy$setDismissalTicks(int ticks) { data().set(hemomancy$sync().dismissalTicks(), Math.max(0, ticks)); }
	@Override default boolean hemomancy$isTrialSummon() { return data().get(hemomancy$sync().trialSummon()); }
	@Override default void hemomancy$setTrialSummon(boolean trialSummon) { data().set(hemomancy$sync().trialSummon(), trialSummon); }
	@Override default UUID hemomancy$getTrialCasterUUID() { return data().get(hemomancy$sync().trialCaster()).orElse(null); }
	@Override default void hemomancy$setTrialCasterUUID(UUID casterUuid) { data().set(hemomancy$sync().trialCaster(), Optional.ofNullable(casterUuid)); }
}
