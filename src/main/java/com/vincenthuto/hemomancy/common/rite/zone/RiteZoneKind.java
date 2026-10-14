package com.vincenthuto.hemomancy.common.rite.zone;

/**
 * Every persistent area a Cardinal Rite can leave behind, with the NBT layout
 * its world save has always used. The tag names are frozen: changing one
 * orphans the zones already stored in existing worlds.
 */
public enum RiteZoneKind {
	STILL_WATERS("entries", "BlockRadius", 1, true, false),
	PALE_CONSECRATION("entries", "BlockRadius", 1, true, false),
	LETHE_COVENANT("entries", "ChunkRadius", 16, true, false),
	SANGUINE_FERVOR("entries", "ChunkRadius", 16, true, true),
	SANGUINE_DOMINION("dominions", "ChunkRadius", 16, false, true);

	private final String listTag;
	private final String radiusTag;
	private final int blocksPerRadiusUnit;
	private final boolean timed;
	private final boolean shownToClients;

	RiteZoneKind(String listTag, String radiusTag, int blocksPerRadiusUnit, boolean timed, boolean shownToClients) {
		this.listTag = listTag;
		this.radiusTag = radiusTag;
		this.blocksPerRadiusUnit = blocksPerRadiusUnit;
		this.timed = timed;
		this.shownToClients = shownToClients;
	}

	/** Root list tag that holds this kind's zones. */
	public String listTag() {
		return listTag;
	}

	/** Per-zone radius tag; {@code ChunkRadius} kinds store chunks, the rest blocks. */
	public String radiusTag() {
		return radiusTag;
	}

	public int blocksPerRadiusUnit() {
		return blocksPerRadiusUnit;
	}

	/** Timed kinds store {@code ExpiryTick}; permanent kinds store {@code CreatedTick}. */
	public boolean timed() {
		return timed;
	}

	/** Whether clients are told about this kind so they can draw its boundary. */
	public boolean shownToClients() {
		return shownToClients;
	}

	public static RiteZoneKind byOrdinal(int ordinal) {
		RiteZoneKind[] kinds = values();
		return ordinal >= 0 && ordinal < kinds.length ? kinds[ordinal] : SANGUINE_DOMINION;
	}
}
