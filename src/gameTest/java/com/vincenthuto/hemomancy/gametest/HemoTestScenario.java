package com.vincenthuto.hemomancy.gametest;

import net.minecraft.server.level.ServerPlayer;

public record HemoTestScenario(
		String id,
		String description,
		ScenarioAction setup,
		ScenarioCheck verify,
		ScenarioAction clear) {

	@FunctionalInterface
	public interface ScenarioAction {
		void apply(ServerPlayer player);
	}

	@FunctionalInterface
	public interface ScenarioCheck {
		HemoTestResult check(ServerPlayer player);
	}
}
