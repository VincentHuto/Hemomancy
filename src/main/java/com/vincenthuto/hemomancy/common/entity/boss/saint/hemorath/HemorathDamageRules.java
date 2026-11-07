package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

/**
 * The damage-taken factor Hemorath hands the kit (design section 6): his meter state times his patches, halved while
 * he drags himself to the pit for the Last Thirst. The open opening's factor and Resistance stack on top in the kit's
 * pipeline. JVM-tested.
 */
public final class HemorathDamageRules {
	/** While the Last Thirst drags him to the pit. */
	public static final float DRAGGING = 0.5F;
	/** A single hit this large cracks a rib in the Strained window (a short flinch). */
	public static final float RIB_CRACK = 8.0F;
	public static final int RIB_CRACK_FLINCH = 10;
	/** A single hit this large on him during the Marrow Siphon's draw chokes it. */
	public static final float SIPHON_CHOKE = 10.0F;

	private HemorathDamageRules() {
	}

	public static float factor(float meterFraction, int patches, boolean dragging) {
		float factor = HemorathMeterRules.state(meterFraction).damageTaken * HemorathIronRules.reduction(patches);
		return dragging ? factor * DRAGGING : factor;
	}
}
