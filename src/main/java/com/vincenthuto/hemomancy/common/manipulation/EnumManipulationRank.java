package com.vincenthuto.hemomancy.common.manipulation;

public enum EnumManipulationRank {
	HUMILIS(1.0F), MEDIOCRITAS(1.0F), SUMMA(2.0F), MAGISTER(2.0F), PERFECTUS(3.0F);

	private final float veinStrain;

	EnumManipulationRank(float veinStrain) {
		this.veinStrain = veinStrain;
	}

	/** Vascular health a cast of this rank costs its vein section, before the server strain multiplier. */
	public float veinStrain() {
		return veinStrain;
	}
}
