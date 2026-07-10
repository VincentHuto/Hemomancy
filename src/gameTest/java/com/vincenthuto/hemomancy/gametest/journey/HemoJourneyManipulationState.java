package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulations;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.ListTag;

/** Pure manipulation attachment seam; packet synchronization remains outside. */
public final class HemoJourneyManipulationState {
	private HemoJourneyManipulationState() { }

	public static ListTag capture(KnownManipulations known, HolderLookup.Provider provider) {
		return known.serializeNBT(provider).copy();
	}

	public static void reset(KnownManipulations known, HolderLookup.Provider provider) {
		apply(known, new KnownManipulations().serializeNBT(provider), provider);
	}

	public static void apply(KnownManipulations known, ListTag state, HolderLookup.Provider provider) {
		known.deserializeNBT(provider, state.copy());
	}

	public static boolean matches(KnownManipulations known, ListTag state, HolderLookup.Provider provider) {
		return known.serializeNBT(provider).equals(state);
	}
}
