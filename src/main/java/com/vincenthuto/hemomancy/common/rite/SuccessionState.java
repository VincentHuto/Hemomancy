package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.nbt.CompoundTag;

/**
 * A Succession rite's own state inside an {@link ActiveCardinalRite}, saved as its {@code Succession} compound. The
 * transaction, body, offerings and stage flags in {@link #tag()} belong to {@code SuccessionRites}; a rite is a
 * Succession once this holds anything.
 */
public final class SuccessionState {
	private CompoundTag tag = new CompoundTag();

	public boolean isPresent() {
		return !tag.isEmpty();
	}

	/** Ticks the Succession has run. */
	public int ticks() {
		return tag.getInt("Ticks");
	}

	void begin(CompoundTag context) {
		tag = context.copy();
	}

	/** The saved compound. */
	public CompoundTag tag() {
		return tag;
	}

	void write(CompoundTag rite) {
		rite.put("Succession", tag.copy());
	}

	void read(CompoundTag rite) {
		tag = rite.getCompound("Succession").copy();
	}
}
