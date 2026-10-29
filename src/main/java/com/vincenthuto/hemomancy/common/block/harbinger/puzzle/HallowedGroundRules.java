package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

/**
 * A Saint's mausoleum is consecrated ground: the Saint's residue rejects a body the hive speaks through. Only an
 * Apotheos burns there: spore load drains first, then health.
 */
public final class HallowedGroundRules {
	public static final float SPORE_DRAIN_PER_SECOND = 4f;
	public static final float BURN_DAMAGE = 1f;

	private HallowedGroundRules() {}

	public static boolean burns(boolean apotheos) {
		return apotheos;
	}
}
