package com.vincenthuto.hemomancy.common.rite.zone;

/**
 * The single precedence rule for rite zones that change natural spawning.
 * <p>
 * A Lethe Covenant domain (including the Lethean Font) wins inside its own
 * bounds: it denies half of the natural spawns there and a Sanguine Fervor
 * zone overlapping it forces nothing. Outside every Lethe domain, a Fervor
 * zone forces the position check through. Spawner blocks are never touched.
 */
public final class RiteZoneSpawnRules {
	private RiteZoneSpawnRules() {}

	public enum Verdict {
		/** Leave the spawn to vanilla and any other mod. */
		DEFAULT,
		/** Force the position check to pass. */
		FORCE,
		/** Deny the spawn. */
		DENY
	}

	/**
	 * @param inLetheDomain the position lies in an active Lethe Covenant domain
	 * @param inFervorZone  the position lies in an active Sanguine Fervor zone
	 * @param letheDenies   the domain's 50% roll came up deny; ignored outside a domain
	 */
	public static Verdict resolve(boolean inLetheDomain, boolean inFervorZone, boolean letheDenies) {
		if (inLetheDomain) return letheDenies ? Verdict.DENY : Verdict.DEFAULT;
		return inFervorZone ? Verdict.FORCE : Verdict.DEFAULT;
	}
}
