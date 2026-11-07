package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintOpening.Delivery;

import java.util.EnumSet;
import java.util.Set;

/**
 * Putriciel's punish windows (design section 6, names after resolutions A-5 and C-1). The Offering is the only one
 * that absolves; the rot windows stoke the Kindling at their multiplier; the Embering gives the next fire its head
 * start. Ids are what {@code DATA_OPENING} carries to the client.
 */
public final class PutricielOpenings {
	public static final int OFFERING_ID = 1;
	public static final int SPENT_SACS_ID = 2;
	public static final int FOULED_CHAIN_ID = 3;
	public static final int BROKEN_COMMUNION_ID = 4;
	public static final int SPENT_BLOOM_ID = 5;
	public static final int EMBERING_ID = 6;

	private static final Set<Delivery> EVERY = EnumSet.allOf(Delivery.class);
	/** A palm takes a blow, a bolt or a cast; held Projection is read by the channel handler, not here. */
	private static final Set<Delivery> STRIKES = EnumSet.of(Delivery.MELEE, Delivery.PROJECTILE, Delivery.SCHOOL_CAST,
			Delivery.OTHER);

	public static final SaintOpening.Spec SPENT_SACS = new SaintOpening.Spec(SPENT_SACS_ID, "spent_sacs", 34, 1.0F, 0, 0, EVERY);
	public static final SaintOpening.Spec FOULED_CHAIN = new SaintOpening.Spec(FOULED_CHAIN_ID, "fouled_chain", 30, 1.25F, 0, 0, EVERY);
	public static final SaintOpening.Spec BROKEN_COMMUNION = new SaintOpening.Spec(BROKEN_COMMUNION_ID, "broken_communion", 40, 1.25F, 0, 0, EVERY);
	public static final SaintOpening.Spec SPENT_BLOOM = new SaintOpening.Spec(SPENT_BLOOM_ID, "spent_bloom", 40, 1.5F, 0, 0, EVERY);
	public static final SaintOpening.Spec EMBERING = new SaintOpening.Spec(EMBERING_ID, "embering", 60, 1.5F, 0, 0, EVERY);

	private PutricielOpenings() {
	}

	/** The Offering for {@code ticks} (50, or 60 after the Full Purge and under the Litany deed). */
	public static SaintOpening.Spec offering(int ticks) {
		return new SaintOpening.Spec(OFFERING_ID, "offering", ticks, 1.0F, 0, 0, STRIKES);
	}

	/** The Kindling multiplier hits take while {@code openingId} is open; 1 for none. */
	public static float kindlingMultiplier(int openingId) {
		return switch (openingId) {
			case SPENT_SACS_ID, FOULED_CHAIN_ID -> 1.5F;
			case BROKEN_COMMUNION_ID, SPENT_BLOOM_ID, EMBERING_ID -> 2.0F;
			default -> 1.0F;
		};
	}

	/** The lang key the HUD and the review stops name an opening by. */
	public static String key(int openingId) {
		return switch (openingId) {
			case OFFERING_ID -> "offering";
			case SPENT_SACS_ID -> "spent_sacs";
			case FOULED_CHAIN_ID -> "fouled_chain";
			case BROKEN_COMMUNION_ID -> "broken_communion";
			case SPENT_BLOOM_ID -> "spent_bloom";
			case EMBERING_ID -> "embering";
			default -> "";
		};
	}
}
