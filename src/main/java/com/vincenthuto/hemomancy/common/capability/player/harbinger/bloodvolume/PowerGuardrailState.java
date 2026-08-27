package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Internal player state for shared power guardrails that should survive death
 * without living in raw player persistent-data tags.
 */
public class PowerGuardrailState implements INBTSerializable<CompoundTag> {

	private static final String CIRCULATION_WINDOW_START_KEY = "CirculationWindowStart";
	private static final String CIRCULATION_WINDOW_USED_KEY = "CirculationWindowUsed";
	private static final String BORROWED_BLOOD_KEY = "BorrowedBlood";
	private static final String LAST_RITE_ARMED_SOURCE_KEY = "LastRiteArmedSource";
	private static final String LAST_RITE_COOLDOWN_UNTIL_KEY = "LastRiteCooldownUntil";
	private static final String HEMATIC_ACCLIMATION_KEY = "HematicAcclimation";
	private static final String IRON_HEART_HEALTH_KEY = "IronHeartHealth";
	private static final String IRON_HEART_EXPIRY_KEY = "IronHeartExpiry";
	private static final String NECROTIC_SATURATION_KEY = "NecroticSaturation";
	private static final String BLACKHEARTED_COOLDOWN_KEY = "BlackheartedCooldown";
	private static final String ENTITY_TYPE_KEY = "EntityType";
	private static final String EXPOSURE_KEY = "Exposure";
	private static final String LAST_UPDATE_TICK_KEY = "LastUpdateTick";

	private long circulationWindowStart;
	private double circulationWindowUsed;
	private double borrowedBlood;
	private String lastRiteArmedSource = "";
	private long lastRiteCooldownUntil;
	private float ironHeartHealth;
	private long ironHeartExpiryTick;
	private float necroticSaturation;
	private long blackheartedCooldownUntil;
	private final Map<String, HematicExposure> hematicAcclimation = new HashMap<>();

	private record HematicExposure(double amount, long lastUpdateTick) {
	}

	public long getCirculationWindowStart() {
		return circulationWindowStart;
	}

	public void setCirculationWindowStart(long circulationWindowStart) {
		this.circulationWindowStart = Math.max(0L, circulationWindowStart);
	}

	public double getCirculationWindowUsed() {
		return circulationWindowUsed;
	}

	public void setCirculationWindowUsed(double circulationWindowUsed) {
		this.circulationWindowUsed = Math.max(0.0D, circulationWindowUsed);
	}

	public double getBorrowedBlood() {
		return borrowedBlood;
	}

	public void setBorrowedBlood(double borrowedBlood) {
		this.borrowedBlood = Math.max(0.0D, borrowedBlood);
	}

	public String getLastRiteArmedSource() {
		return lastRiteArmedSource;
	}

	public void setLastRiteArmedSource(String lastRiteArmedSource) {
		this.lastRiteArmedSource = lastRiteArmedSource == null ? "" : lastRiteArmedSource;
	}

	public void clearLastRiteArmedSource() {
		lastRiteArmedSource = "";
	}

	public boolean isLastRiteArmed(String sourceId) {
		return sourceId != null && !sourceId.isEmpty() && sourceId.equals(lastRiteArmedSource);
	}

	public long getLastRiteCooldownUntil() {
		return lastRiteCooldownUntil;
	}

	public void setLastRiteCooldownUntil(long lastRiteCooldownUntil) {
		this.lastRiteCooldownUntil = Math.max(0L, lastRiteCooldownUntil);
	}

	public float getIronHeartHealth() {
		return ironHeartHealth;
	}

	public void setIronHeartHealth(float ironHeartHealth) {
		this.ironHeartHealth = Math.max(0.0F, ironHeartHealth);
	}

	public long getIronHeartExpiryTick() {
		return ironHeartExpiryTick;
	}

	public void setIronHeartExpiryTick(long ironHeartExpiryTick) {
		this.ironHeartExpiryTick = Math.max(0L, ironHeartExpiryTick);
	}

	public void clearIronHearts() {
		ironHeartHealth = 0.0F;
		ironHeartExpiryTick = 0L;
	}

	public float getNecroticSaturation() {
		return necroticSaturation;
	}

	public void setNecroticSaturation(float necroticSaturation) {
		this.necroticSaturation = Math.max(0.0F, necroticSaturation);
	}

	public long getBlackheartedCooldownUntil() {
		return blackheartedCooldownUntil;
	}

	public void setBlackheartedCooldownUntil(long blackheartedCooldownUntil) {
		this.blackheartedCooldownUntil = Math.max(0L, blackheartedCooldownUntil);
	}

	public double hematicExposure(String entityTypeId, long nowTick) {
		if (entityTypeId == null || entityTypeId.isBlank()) return 0.0D;
		HematicExposure stored = hematicAcclimation.get(entityTypeId);
		if (stored == null) return 0.0D;
		long tick = Math.max(0L, nowTick);
		double exposure = HematicAcclimationRules.decayedExposure(
				stored.amount(), tick - stored.lastUpdateTick());
		if (exposure <= 0.0D) {
			hematicAcclimation.remove(entityTypeId);
		} else if (tick > stored.lastUpdateTick()) {
			hematicAcclimation.put(entityTypeId, new HematicExposure(exposure, tick));
		}
		return exposure;
	}

	public void recordHematicExposure(String entityTypeId, double absorbedMl, long nowTick) {
		if (entityTypeId == null || entityTypeId.isBlank() || !Double.isFinite(absorbedMl) || absorbedMl <= 0.0D) {
			return;
		}
		long tick = Math.max(0L, nowTick);
		double exposure = Math.min(HematicAcclimationRules.MAX_EXPOSURE_ML,
				hematicExposure(entityTypeId, tick) + absorbedMl);
		hematicAcclimation.put(entityTypeId, new HematicExposure(exposure, tick));
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putLong(CIRCULATION_WINDOW_START_KEY, circulationWindowStart);
		tag.putDouble(CIRCULATION_WINDOW_USED_KEY, circulationWindowUsed);
		tag.putDouble(BORROWED_BLOOD_KEY, borrowedBlood);
		tag.putString(LAST_RITE_ARMED_SOURCE_KEY, lastRiteArmedSource);
		tag.putLong(LAST_RITE_COOLDOWN_UNTIL_KEY, lastRiteCooldownUntil);
		tag.putFloat(IRON_HEART_HEALTH_KEY, ironHeartHealth);
		tag.putLong(IRON_HEART_EXPIRY_KEY, ironHeartExpiryTick);
		tag.putFloat(NECROTIC_SATURATION_KEY, necroticSaturation);
		tag.putLong(BLACKHEARTED_COOLDOWN_KEY, blackheartedCooldownUntil);
		ListTag acclimationTag = new ListTag();
		for (Map.Entry<String, HematicExposure> entry : hematicAcclimation.entrySet()) {
			CompoundTag exposureTag = new CompoundTag();
			exposureTag.putString(ENTITY_TYPE_KEY, entry.getKey());
			exposureTag.putDouble(EXPOSURE_KEY, entry.getValue().amount());
			exposureTag.putLong(LAST_UPDATE_TICK_KEY, entry.getValue().lastUpdateTick());
			acclimationTag.add(exposureTag);
		}
		tag.put(HEMATIC_ACCLIMATION_KEY, acclimationTag);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
		if (nbt == null) {
			return;
		}
		setCirculationWindowStart(nbt.getLong(CIRCULATION_WINDOW_START_KEY));
		setCirculationWindowUsed(nbt.getDouble(CIRCULATION_WINDOW_USED_KEY));
		setBorrowedBlood(nbt.getDouble(BORROWED_BLOOD_KEY));
		setLastRiteArmedSource(nbt.getString(LAST_RITE_ARMED_SOURCE_KEY));
		setLastRiteCooldownUntil(nbt.getLong(LAST_RITE_COOLDOWN_UNTIL_KEY));
		setIronHeartHealth(nbt.getFloat(IRON_HEART_HEALTH_KEY));
		setIronHeartExpiryTick(nbt.getLong(IRON_HEART_EXPIRY_KEY));
		setNecroticSaturation(nbt.getFloat(NECROTIC_SATURATION_KEY));
		setBlackheartedCooldownUntil(nbt.getLong(BLACKHEARTED_COOLDOWN_KEY));
		hematicAcclimation.clear();
		ListTag acclimationTag = nbt.getList(HEMATIC_ACCLIMATION_KEY, Tag.TAG_COMPOUND);
		for (int index = 0; index < acclimationTag.size(); index++) {
			CompoundTag exposureTag = acclimationTag.getCompound(index);
			String entityTypeId = exposureTag.getString(ENTITY_TYPE_KEY);
			double exposure = HematicAcclimationRules.clampedExposure(exposureTag.getDouble(EXPOSURE_KEY));
			if (!entityTypeId.isBlank() && exposure > 0.0D) {
				hematicAcclimation.put(entityTypeId, new HematicExposure(
						exposure, Math.max(0L, exposureTag.getLong(LAST_UPDATE_TICK_KEY))));
			}
		}
	}
}
