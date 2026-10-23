package com.vincenthuto.hemomancy.common.encounter;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * What a player has earned toward the Chamber of Will: the chair binding, attunement, the first dream and the guided
 * visit. Read it through {@link ChamberVisitService}; a visit in progress keeps its own session state there.
 */
public class ChamberAccess implements INBTSerializable<CompoundTag> {
	private static final String CHAIR_BOUND = "ChairBound";
	private static final String ATTUNED = "Attuned";
	private static final String DREAM_SEEN = "DreamSeen";
	private static final String DREAM_ATTEMPTS = "DreamAttempts";
	private static final String GUIDED_COMPLETE = "GuidedComplete";

	private boolean chairBound;
	private boolean attuned;
	private boolean dreamSeen;
	private int dreamAttempts;
	private boolean guidedComplete;

	public boolean chairBound() {
		return chairBound;
	}

	public void setChairBound(boolean chairBound) {
		this.chairBound = chairBound;
	}

	public boolean attuned() {
		return attuned;
	}

	public void setAttuned(boolean attuned) {
		this.attuned = attuned;
	}

	public boolean dreamSeen() {
		return dreamSeen;
	}

	public void setDreamSeen(boolean dreamSeen) {
		this.dreamSeen = dreamSeen;
	}

	public int dreamAttempts() {
		return dreamAttempts;
	}

	public void setDreamAttempts(int dreamAttempts) {
		this.dreamAttempts = dreamAttempts;
	}

	public boolean guidedComplete() {
		return guidedComplete;
	}

	public void setGuidedComplete(boolean guidedComplete) {
		this.guidedComplete = guidedComplete;
	}

	@Override
	public CompoundTag serializeNBT(HolderLookup.Provider provider) {
		CompoundTag tag = new CompoundTag();
		tag.putBoolean(CHAIR_BOUND, chairBound);
		tag.putBoolean(ATTUNED, attuned);
		tag.putBoolean(DREAM_SEEN, dreamSeen);
		tag.putInt(DREAM_ATTEMPTS, dreamAttempts);
		tag.putBoolean(GUIDED_COMPLETE, guidedComplete);
		return tag;
	}

	@Override
	public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
		chairBound = tag.getBoolean(CHAIR_BOUND);
		attuned = tag.getBoolean(ATTUNED);
		dreamSeen = tag.getBoolean(DREAM_SEEN);
		dreamAttempts = tag.getInt(DREAM_ATTEMPTS);
		guidedComplete = tag.getBoolean(GUIDED_COMPLETE);
	}
}
