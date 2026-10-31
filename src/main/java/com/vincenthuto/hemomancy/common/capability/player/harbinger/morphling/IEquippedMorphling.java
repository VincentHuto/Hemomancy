package com.vincenthuto.hemomancy.common.capability.player.harbinger.morphling;

import net.minecraft.world.item.ItemStack;

public interface IEquippedMorphling {

	ItemStack getEquippedMorphling();

	void setEquippedMorphling(ItemStack stack);

	void clearMorphling();

	boolean hasMorphling();

	/** {@code MorphlingActiveState} bits last synced for this host; transient, never saved. */
	int getActiveFlags();

	void setActiveFlags(int flags);

	/** Entity ids held by Dead Man's Rope strands, for tracking clients to draw; transient. */
	int[] getLinkedEntityIds();

	/** Block anchors held by Dead Man's Rope strands, as {@code BlockPos.asLong}; transient. */
	long[] getLinkedAnchors();

	void setLinks(int[] entityIds, long[] anchors);

}
