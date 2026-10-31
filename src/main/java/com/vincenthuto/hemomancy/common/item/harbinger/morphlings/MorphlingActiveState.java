package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

/**
 * The active-state bits a worn strain shows, carried to the host and every tracker in
 * {@code SyncEquippedMorphlingPacket} for Track 3's attachments, world effects and HUD. Bits 0-23 are flags; bits
 * 24-27 carry Mermaid's Purse's banked-blood fill in sixteenths ({@link #withFill}). The layout is agreed in
 * {@code docs/brined_court/02-strain-powers.md} ("Synced active state"); append, never renumber.
 */
public final class MorphlingActiveState {
	/** The strain's home condition holds (tint, HUD). */
	public static final int HOME = 1;
	/** The host has stood still for the strain's still threshold. */
	public static final int STILL = 1 << 1;
	/** The host is anchored: sneaking without moving. */
	public static final int ANCHORED = 1 << 2;
	/** Sunburst: Lantern Fronds open. */
	public static final int LANTERN_FRONDS = 1 << 3;
	/** Sunburst: Arbor anchored, roots in the ground. */
	public static final int ARBOR = 1 << 4;
	/** Holdfast: Cling holding a wall or a current. */
	public static final int CLING = 1 << 5;
	/** Holdfast: Brace up. */
	public static final int BRACE = 1 << 6;
	/** Holdfast: Breakwater anchored. */
	public static final int BREAKWATER = 1 << 7;
	/** Holdfast: The Holding pinning. */
	public static final int HOLDING = 1 << 8;
	/** Mermaid's Purse: sail raised while sprint-swimming. */
	public static final int SAIL_RAISED = 1 << 9;
	/** Mermaid's Purse: Shared Wake open. */
	public static final int SHARED_WAKE = 1 << 10;
	/** Ink Cap: Folded Away. */
	public static final int FOLDED_AWAY = 1 << 11;
	/** Ink Cap: Deliquesce dissolved. */
	public static final int DELIQUESCE = 1 << 12;
	/** Hushbell: Fold Shut closed. */
	public static final int FOLD_SHUT = 1 << 13;
	/** Hushbell: Cryptobiosis sealed. */
	public static final int CRYPTOBIOSIS = 1 << 14;
	/** Kinbud: one Second Head budded. */
	public static final int SECOND_HEAD = 1 << 15;
	/** Kinbud: a second Second Head (Kindled at home). */
	public static final int SECOND_HEAD_TWIN = 1 << 16;
	/** Kinbud: Kindled window open. */
	public static final int KINDLED = 1 << 17;
	/** Marrowplume: Plume Cloud released (a short pulse). */
	public static final int PLUME_CLOUD = 1 << 18;
	/** Marrowplume: anchored inside a Whale Fall (stalks root). */
	public static final int WHALE_FALL_ROOTED = 1 << 19;
	/** Dead Man's Rope: reeling toward a block anchor. */
	public static final int REELING = 1 << 20;
	/** Dead Man's Rope: a Lifeline strand left at the landing point (5 s). */
	public static final int LIFELINE = 1 << 21;
	/** Sunburst: a Bloom seeded and still open (the patch itself is a field entity). */
	public static final int BLOOM = 1 << 22;

	private static final int FILL_SHIFT = 24;
	private static final int FILL_MASK = 0xF << FILL_SHIFT;

	private MorphlingActiveState() {
	}

	public static boolean has(int flags, int bit) {
		return (flags & bit) != 0;
	}

	public static int set(int flags, int bit, boolean on) {
		return on ? flags | bit : flags & ~bit;
	}

	/** Stores a 0-1 fill as sixteenths in bits 24-27. */
	public static int withFill(int flags, double fill) {
		int sixteenths = (int) Math.round(Math.max(0.0D, Math.min(1.0D, fill)) * 15.0D);
		return (flags & ~FILL_MASK) | (sixteenths << FILL_SHIFT);
	}

	public static double fill(int flags) {
		return ((flags & FILL_MASK) >>> FILL_SHIFT) / 15.0D;
	}
}
