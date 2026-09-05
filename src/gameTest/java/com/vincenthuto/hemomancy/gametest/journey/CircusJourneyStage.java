package com.vincenthuto.hemomancy.gametest.journey;

public enum CircusJourneyStage {
	DISCOVERY("discovery"), PERFORMERS("performers"), ACCLIMATING("acclimating"), ATTENTION("attention"), ROUTE("route"),
	ACTS("acts"), FINALE("finale"), REWARD("reward"), COMPLETE("complete");

	private final String id;

	CircusJourneyStage(String id) { this.id = id; }
	public String id() { return id; }

	public static CircusJourneyStage byId(String id) {
		for (CircusJourneyStage stage : values()) if (stage.id.equals(id)) return stage;
		return DISCOVERY;
	}
}
