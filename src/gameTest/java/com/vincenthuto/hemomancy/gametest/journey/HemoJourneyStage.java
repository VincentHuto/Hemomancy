package com.vincenthuto.hemomancy.gametest.journey;

public enum HemoJourneyStage {
	MORTAL_DISPLAY("mortal_display"),
	SANGUINE_INITIATION("sanguine_initiation"),
	VESSEL_FILLED("vessel_filled"),
	FORMATION_PROJECTED("formation_projected"),
	LIBER_CRAFTED("liber_crafted"),
	HEMATIC_IRON_CRAFTED("hematic_iron_crafted"),
	VICAR_REWARD("vicar_reward"),
	VOTARY_RITE("votary_rite"),
	DEGREE_2_REACHED("degree_2_reached"),
	ALCHEMIST_BRIEFING("alchemist_briefing"),
	CENTRIFUGE_PREPARED("centrifuge_prepared"),
	SEPARATION_STARTED("separation_started"),
	ENZYME_RECOVERED("enzyme_recovered"),
	ALCHEMIST_REWARD("alchemist_reward"),
	INITIATE_RITE("initiate_rite"),
	ADEPT_RITE("adept_rite"),
	ILLUMINATUS_RITE("illuminatus_rite"),
	SANCTIFIED_RITE("sanctified_rite"),
	ARCHON_RITE("archon_rite"),
	COMPLETE("complete");

	private final String id;

	HemoJourneyStage(String id) {
		this.id = id;
	}

	public String id() {
		return id;
	}
}
