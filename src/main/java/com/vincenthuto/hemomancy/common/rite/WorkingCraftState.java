package com.vincenthuto.hemomancy.common.rite;

import net.minecraft.nbt.CompoundTag;

/**
 * A Working craft's own state inside an {@link ActiveCardinalRite}, saved as its {@code Craft} compound: the blood the
 * built subject needs and has taken, plus the subject snapshot and offering escrow kept in {@link #tag()} by
 * {@code WorkingCraftRites} and {@code RiteOfferingEscrow}. A rite is a Working craft when this holds a feed
 * requirement.
 */
public final class WorkingCraftState {
	private static final String FEED_REQUIRED = "FeedRequired";
	private static final String FED = "Fed";
	private CompoundTag tag = new CompoundTag();

	public boolean isPresent() {
		return tag.contains(FEED_REQUIRED);
	}

	void require(int feedMl) {
		tag.putInt(FEED_REQUIRED, Math.max(1, feedMl));
	}

	public int feedRequired() {
		return tag.getInt(FEED_REQUIRED);
	}

	public int fed() {
		return tag.getInt(FED);
	}

	public int feedNeeded() {
		return Math.max(0, feedRequired() - fed());
	}

	void feed(int acceptedMl) {
		tag.putInt(FED, fed() + acceptedMl);
	}

	public float fill() {
		int required = feedRequired();
		return required <= 0 ? 0.0F : Math.min(1.0F, fed() / (float) required);
	}

	/** The saved compound, shared with the subject snapshot and offering escrow. */
	public CompoundTag tag() {
		return tag;
	}

	void write(CompoundTag rite) {
		if (!tag.isEmpty()) rite.put("Craft", tag.copy());
	}

	void read(CompoundTag rite) {
		tag = rite.getCompound("Craft").copy();
	}
}
