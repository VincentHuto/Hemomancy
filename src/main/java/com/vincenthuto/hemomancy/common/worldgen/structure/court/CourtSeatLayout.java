package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Where a Court site's start piece stands relative to its marker. These numbers are the template frames written by
 * tools/worldgen/brined_court/ (garden.py, sanctum.py, outposts.py); CourtSeatTemplateTest and
 * CourtOutpostTemplateTest pin them against the saved NBT.
 *
 * <p>The jigsaw start lands one block below the start position it is given. The Lighthouse Garden's start jigsaw is
 * its waterline row, so the Garden is pinned to the sea surface (Y62, the top water layer) whatever the depth of the
 * shelf under it: its rock reaches 24 blocks down. The Vent Sanctum's start jigsaw is {@link #SANCTUM_LIFT} above its
 * marker on the centre column, clear of every chimney and crown.
 *
 * <p>Outposts (Track 8) are square, odd-sized templates whose start jigsaw {@code hemomancy:court_outpost_start} stands
 * on the centre column at the marker row, {@link #outpostBelow} rows above the template's bottom (footings that reach
 * down to the real seabed under the corners). The Tide Refuge is pinned to the sea surface like the Garden: its start
 * jigsaw is its waterline row.
 */
public final class CourtSeatLayout {
	/** The Garden's waterline: its start jigsaw lands here. */
	public static final int GARDEN_WATERLINE_Y = 62;
	/** Rows of the Garden's start piece under its waterline row. */
	public static final int GARDEN_DEPTH = 24;
	public static final int SANCTUM_LIFT = 14;
	/** Rows of the Sanctum's start piece under its marker row. */
	public static final int SANCTUM_BELOW = 10;
	/** The Sanctum's start piece is structure void within this radius of its centre column at every height. */
	public static final double SANCTUM_CHIMNEY_ZONE = 12.5;
	/** Rows of the Tide Refuge's rock stack under its waterline row. */
	public static final int REFUGE_DEPTH = 8;

	private CourtSeatLayout() {
	}

	/** The position handed to the jigsaw assembler for a site whose marker the placement rules chose. */
	public static BlockPos start(CourtSite site, BlockPos marker) {
		return switch (site) {
			case LIGHTHOUSE_GARDEN, TIDE_REFUGE -> new BlockPos(marker.getX(), GARDEN_WATERLINE_Y + 1, marker.getZ());
			case VENT_SANCTUM -> marker.above(SANCTUM_LIFT + 1);
			default -> marker.above();
		};
	}

	/** Rows of an outpost's template under its marker row (for the Tide Refuge, under its waterline row). */
	public static int outpostBelow(CourtSite site) {
		return switch (site) {
			case TIDE_REFUGE -> REFUGE_DEPTH;
			case LINE_CAMP -> 6;
			case SCREENED_CLEFT -> 12;
			case WAKE_NURSERY, STILL_BELL, WHALE_FALL -> 4;
			default -> throw new IllegalArgumentException(site + " is not an outpost");
		};
	}

	/**
	 * The centre column at the start jigsaw's row, read back from an outpost's start piece: the marker for a deep
	 * outpost, the waterline (Y62) for the Tide Refuge.
	 */
	public static BlockPos outpostCentre(CourtSite site, BoundingBox startPiece) {
		return new BlockPos((startPiece.minX() + startPiece.maxX()) / 2, startPiece.minY() + outpostBelow(site),
				(startPiece.minZ() + startPiece.maxZ()) / 2);
	}

	/** The Sanctum's marker, read back from its start piece (a square template centred on the marker column). */
	public static BlockPos sanctumMarker(BoundingBox startPiece) {
		return new BlockPos((startPiece.minX() + startPiece.maxX()) / 2, startPiece.minY() + SANCTUM_BELOW,
				(startPiece.minZ() + startPiece.maxZ()) / 2);
	}
}
