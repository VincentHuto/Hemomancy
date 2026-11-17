package com.vincenthuto.hemomancy.common.manipulation.verb;

import java.util.EnumSet;
import java.util.Set;

/** What a verb can be aimed at. A {@link VerbSculpting} declares the kinds it wants; the pick looks for no others. */
public enum VerbTargetKind {
	BLOCK,
	FLUID,
	CREATURE,
	ITEMS,
	AIR;

	/** The plain Projection pick: a block (a fluid surface reads as its block, as it always has). */
	public static final Set<VerbTargetKind> PLAIN = EnumSet.of(BLOCK);

	public static Set<VerbTargetKind> of(VerbTargetKind first, VerbTargetKind... rest) {
		return EnumSet.of(first, rest);
	}
}
