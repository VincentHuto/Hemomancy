package com.vincenthuto.hemomancy.gametest.journey;

public enum UnstainedJourneyStage {
	NOVITIATE_GATHER_REMEDIES("novitiate_gather_remedies"),
	NOVITIATE_GENTLE_SEPARATION("novitiate_gentle_separation"),
	NOVITIATE_STILLWATER_LABOR("novitiate_stillwater_labor"),
	NOVITIATE_CLEAN_LABOR("novitiate_clean_labor"),
	NOVITIATE_SHELTER_AFFLICTED("novitiate_shelter_afflicted"),
	PODIUM_SUPPRESSION("podium_suppression"),
	LETHEAN_BAPTISM("lethean_baptism"),
	GHOST_PIPE_OBSERVANCE("ghost_pipe_observance"),
	TAINTED_ACOLYTE_OBSERVANCES("tainted_acolyte_observances"),
	SILVER_VEIL("silver_veil"),
	CLEANSING_OBSERVANCES("cleansing_observances"),
	PALLID_ICON_OBSERVANCE("pallid_icon_observance"),
	SILTHMERE_REMEMBRANCE("silthmere_remembrance"),
	CLOSED_VEIN("closed_vein"),
	CONSECRATED_COPPER_OBSERVANCE("consecrated_copper_observance"),
	CLARITY_PREPARED("clarity_prepared"),
	CLARITY_ASCENSION("clarity_ascension"),
	GLASS_LUNGS("glass_lungs"),
	CHALICE_OBSERVANCE("chalice_observance"),
	DISCERNING("discerning"),
	PALE_VIGIL("pale_vigil"),
	MOON_WASHED_COPPER("moon_washed_copper"),
	PALE_WATCH_OBSERVANCE("pale_watch_observance"),
	RESOLUTE("resolute"),
	ENLIGHTENED("enlightened"),
	LETHEAN_FONT("lethean_font"),
	COMPLETE("complete");

	private final String id;

	UnstainedJourneyStage(String id) { this.id = id; }
	public String id() { return id; }

	public static UnstainedJourneyStage byId(String id) {
		for (UnstainedJourneyStage stage : values()) if (stage.id.equals(id)) return stage;
		return PODIUM_SUPPRESSION;
	}
}
