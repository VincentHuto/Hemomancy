package com.vincenthuto.hemomancy.common.block.harbinger.plant;

/** The Archive yields a Saint's Recollection only to the eighth degree: in the flesh, or through a facsimile. */
public final class RecollectionRules {
	private RecollectionRules() {}

	public static boolean mayTake(boolean apotheos, boolean possessing) {
		return apotheos || possessing;
	}
}
