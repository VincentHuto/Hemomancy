package com.vincenthuto.hemomancy.common.entity.boss.saint;

/**
 * What a move's timeline, footprint and telegraph may read: only values both sides have (the synced phase, the move's
 * variant, the deed mask and the ratcheted challenger count), so the client draws what the server tests.
 */
public record SaintMoveInputs(int phase, int variant, int deedMask, int challengers) {
	public static final SaintMoveInputs NONE = new SaintMoveInputs(1, 0, 0, 1);
}
