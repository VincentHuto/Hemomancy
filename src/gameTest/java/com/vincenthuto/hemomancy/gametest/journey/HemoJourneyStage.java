package com.vincenthuto.hemomancy.gametest.journey;

public enum HemoJourneyStage {
	MORTAL_DISPLAY("mortal_display"),
	SANGUINE_INITIATION("sanguine_initiation"),
	VESSEL_FILLED("vessel_filled"),
	FORMATION_PROJECTED("formation_projected"),
	LIBER_CRAFTED("liber_crafted"),
	HEMATIC_IRON_CRAFTED("hematic_iron_crafted"),
	VICAR_REWARD("vicar_reward"),
	COMPLETE("complete");

	private final String id;

	HemoJourneyStage(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}
}
