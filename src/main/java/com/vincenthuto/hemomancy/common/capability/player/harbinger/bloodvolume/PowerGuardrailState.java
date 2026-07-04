package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

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

	private long circulationWindowStart;
	private double circulationWindowUsed;
	private double borrowedBlood;
	private String lastRiteArmedSource = "";
	private long lastRiteCooldownUntil;

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

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putLong(CIRCULATION_WINDOW_START_KEY, circulationWindowStart);
		tag.putDouble(CIRCULATION_WINDOW_USED_KEY, circulationWindowUsed);
		tag.putDouble(BORROWED_BLOOD_KEY, borrowedBlood);
		tag.putString(LAST_RITE_ARMED_SOURCE_KEY, lastRiteArmedSource);
		tag.putLong(LAST_RITE_COOLDOWN_UNTIL_KEY, lastRiteCooldownUntil);
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
	}
}
