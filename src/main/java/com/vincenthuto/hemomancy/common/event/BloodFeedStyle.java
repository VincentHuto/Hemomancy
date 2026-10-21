package com.vincenthuto.hemomancy.common.event;

/** How the client dresses a blood feed shell. */
public enum BloodFeedStyle {
	/** A multi-block Blood Structure craft: overlay and melt, plus the Abocipher spiral. */
	STRUCTURE,
	/** A single-block infusion: veins crawl from the projection point, then the block cracks and melts. */
	INFUSION,
	/** The bare overlay, as Lignum Mortis uses it. */
	PLAIN;

	public static BloodFeedStyle byId(int id) {
		BloodFeedStyle[] values = values();
		return id >= 0 && id < values.length ? values[id] : STRUCTURE;
	}
}
