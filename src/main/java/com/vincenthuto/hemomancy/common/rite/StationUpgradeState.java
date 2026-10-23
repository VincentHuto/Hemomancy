package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.nbt.CompoundTag;

/**
 * A station-upgrade rite's own state inside an {@link ActiveCardinalRite}, saved as its {@code StationUpgrade}
 * compound (read from the older {@code AlembicUpgrade} key too): the circuit being fed and the blood in it, plus the
 * subject snapshot and offering escrow kept in {@link #tag()} by {@code StationUpgradeRites} and
 * {@code RiteOfferingEscrow}.
 */
public final class StationUpgradeState {
	private static final String STAGE = "Stage";
	private static final String BLOOD_AT_STAGE = "BloodAtStage";
	private CompoundTag tag = new CompoundTag();

	/** Circuits already fed. */
	public int stage() {
		return tag.getInt(STAGE);
	}

	/** Blood in the circuit being fed. */
	public int bloodAtStage() {
		return tag.getInt(BLOOD_AT_STAGE);
	}

	void setBloodAtStage(int bloodMl) {
		tag.putInt(BLOOD_AT_STAGE, bloodMl);
	}

	/** Closes the circuit being fed and opens the next. */
	void advanceStage() {
		tag.putInt(STAGE, stage() + 1);
		tag.putInt(BLOOD_AT_STAGE, 0);
	}

	/** The saved compound, shared with the subject snapshot and offering escrow. */
	public CompoundTag tag() {
		return tag;
	}

	void write(CompoundTag rite) {
		if (!tag.isEmpty()) rite.put("StationUpgrade", tag.copy());
	}

	void read(CompoundTag rite) {
		tag = (rite.contains("StationUpgrade") ? rite.getCompound("StationUpgrade")
				: rite.getCompound("AlembicUpgrade")).copy();
	}
}
