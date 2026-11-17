package com.vincenthuto.hemomancy.common.capability.player.harbinger.cling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Whether a player is rooted to a wall or a ceiling by Venous Cling, and to which face (core-loop Track 9). Transient
 * player state: never saved, not kept through death or a dimension change. The server owns it; the owner and the
 * players tracking them get it through {@code PacketSyncCling}, so the owner's client can move them along the face and
 * everyone can draw the pose and the veins. Read and written through {@code HemoCapabilityAccess.getCling}.
 */
public final class ClingState {
	private ClingRules.Surface surface = ClingRules.Surface.NONE;
	private Direction face = Direction.NORTH;
	private BlockPos anchor = BlockPos.ZERO;
	private boolean cleats;
	private long rootedAt;
	private long nextUpkeep;
	private boolean leftGround;

	public boolean isClinging() {
		return surface != ClingRules.Surface.NONE;
	}

	public ClingRules.Surface surface() {
		return surface;
	}

	/** The face held, pointing out of its block toward the player. */
	public Direction face() {
		return face;
	}

	/** The block the body holds now; it changes as the player crawls. */
	public BlockPos anchor() {
		return anchor;
	}

	/** Whether fitted Hematic Cleats were on at the last sync, which sets the crawl's pace on the owner's client. */
	public boolean cleats() {
		return cleats;
	}

	public long rootedAt() {
		return rootedAt;
	}

	/** The game time the next second of upkeep falls due; the root paid for the first. */
	public long nextUpkeep() {
		return nextUpkeep;
	}

	public void setNextUpkeep(long nextUpkeep) {
		this.nextUpkeep = nextUpkeep;
	}

	/** Whether the body has been off the ground since it rooted; a wall cling rooted from the floor holds until then. */
	public boolean leftGround() {
		return leftGround;
	}

	public void markLeftGround() {
		leftGround = true;
	}

	public void root(BlockPos anchor, Direction face, boolean cleats, long now) {
		this.surface = ClingRules.Surface.forFace(face);
		this.face = face;
		this.anchor = anchor.immutable();
		this.cleats = cleats;
		this.rootedAt = now;
		this.nextUpkeep = now + ClingRules.UPKEEP_INTERVAL;
		this.leftGround = false;
	}

	public void moveAnchor(BlockPos anchor) {
		this.anchor = anchor.immutable();
	}

	public void setCleats(boolean cleats) {
		this.cleats = cleats;
	}

	public void clear() {
		surface = ClingRules.Surface.NONE;
		anchor = BlockPos.ZERO;
		cleats = false;
		rootedAt = 0L;
		nextUpkeep = 0L;
		leftGround = false;
	}

	/** Sets the whole state from a sync. */
	public void set(ClingRules.Surface surface, Direction face, BlockPos anchor, boolean cleats) {
		this.surface = surface;
		this.face = face;
		this.anchor = anchor.immutable();
		this.cleats = cleats;
	}
}
