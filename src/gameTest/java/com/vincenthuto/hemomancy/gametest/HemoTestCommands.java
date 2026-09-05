package com.vincenthuto.hemomancy.gametest;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.vincenthuto.hemomancy.gametest.journey.*;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class HemoTestCommands {
	private HemoTestCommands() {
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		JourneyAutoRunner.register();
		dispatcher.register(Commands.literal("hemo")
				.requires(source -> source.hasPermission(2))
				.then(Commands.literal("test")
						.then(Commands.literal("list").executes(context -> list(context.getSource())))
						.then(Commands.literal("setup").then(scenarioArgument()
								.executes(context -> setup(context.getSource(), scenarioId(context)))))
						.then(Commands.literal("verify").then(scenarioArgument()
								.executes(context -> verify(context.getSource(), scenarioId(context)))))
						.then(Commands.literal("run").then(scenarioArgument()
								.executes(context -> run(context.getSource(), scenarioId(context)))))
						.then(Commands.literal("run_all").executes(context -> runAll(context.getSource())))
						.then(Commands.literal("status").executes(context -> status(context.getSource())))
						.then(Commands.literal("journey")
								.then(Commands.literal("run_all").executes(context -> journeyRunAll(context.getSource())))
								.then(Commands.literal("harbinger")
										.then(Commands.literal("start").executes(context -> journeyStart(context.getSource())))
										.then(Commands.literal("run").executes(context -> journeyRun(context.getSource())))
										.then(Commands.literal("next").executes(context -> journeyNext(context.getSource())))
										.then(Commands.literal("status").executes(context -> journeyStatus(context.getSource())))
										.then(Commands.literal("reset").executes(context -> journeyReset(context.getSource()))))
								.then(Commands.literal("unstained")
										.then(Commands.literal("start").executes(context -> unstainedJourneyStart(context.getSource())))
										.then(Commands.literal("run").executes(context -> unstainedJourneyRun(context.getSource(), "cure")))
										.then(Commands.literal("cure")
												.then(Commands.literal("start").executes(context -> unstainedJourneyStart(context.getSource(), "cure")))
												.then(Commands.literal("run").executes(context -> unstainedJourneyRun(context.getSource(), "cure"))))
										.then(Commands.literal("novitiate")
												.then(Commands.literal("start").executes(context -> unstainedJourneyStart(context.getSource(), "novitiate")))
												.then(Commands.literal("run").executes(context -> unstainedJourneyRun(context.getSource(), "novitiate"))))
										.then(Commands.literal("next").executes(context -> unstainedJourneyNext(context.getSource())))
										.then(Commands.literal("status").executes(context -> unstainedJourneyStatus(context.getSource())))
										.then(Commands.literal("reset").executes(context -> unstainedJourneyReset(context.getSource()))))
								.then(Commands.literal("circus")
										.then(Commands.literal("run").executes(context -> circusJourneyRun(context.getSource())))
										.then(Commands.literal("succession")
												.then(Commands.literal("start").executes(context -> circusJourneyStart(context.getSource(), "succession")))
												.then(Commands.literal("run").executes(context -> circusJourneyRun(context.getSource(), "succession"))))
										.then(Commands.literal("liberation")
												.then(Commands.literal("start").executes(context -> circusJourneyStart(context.getSource(), "liberation")))
												.then(Commands.literal("run").executes(context -> circusJourneyRun(context.getSource(), "liberation"))))
										.then(Commands.literal("next").executes(context -> circusJourneyNext(context.getSource())))
										.then(Commands.literal("status").executes(context -> circusJourneyStatus(context.getSource())))
										.then(Commands.literal("reset").executes(context -> circusJourneyReset(context.getSource())))))
						.then(Commands.literal("clear").executes(context -> clear(context.getSource())))));
	}

	private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> scenarioArgument() {
		return Commands.argument("scenario", StringArgumentType.word()).suggests((context, builder) -> {
			HemoTestScenarioCatalog.all().forEach(scenario -> builder.suggest(scenario.id()));
			return builder.buildFuture();
		});
	}

	private static String scenarioId(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context) {
		return StringArgumentType.getString(context, "scenario");
	}

	private static int list(CommandSourceStack source) {
		HemoTestScenarioCatalog.all().forEach(scenario -> source.sendSuccess(
				() -> Component.literal(scenario.id() + " - " + scenario.description()), false));
		return HemoTestScenarioCatalog.all().size();
	}

	private static int setup(CommandSourceStack source, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		HemoTestScenario scenario = requireScenario(source, id);
		if (scenario == null) {
			return 0;
		}
		HemoTestScenarioCatalog.clearActive(player);
		scenario.setup().apply(player);
		HemoTestScenarioCatalog.markActive(player, scenario);
		source.sendSuccess(() -> Component.literal("Prepared " + scenario.id() + ". Run /hemo test verify "
				+ scenario.id()).withStyle(ChatFormatting.GOLD), false);
		return 1;
	}

	private static int verify(CommandSourceStack source, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		HemoTestScenario scenario = requireScenario(source, id);
		if (scenario == null) {
			return 0;
		}
		HemoTestResult result = scenario.verify().check(player);
		source.sendSuccess(() -> Component.literal((result.passed() ? "PASS: " : "FAIL: ") + result.message())
				.withStyle(result.passed() ? ChatFormatting.GREEN : ChatFormatting.RED), false);
		return result.passed() ? 1 : 0;
	}

	private static int run(CommandSourceStack source, String id) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		if (setup(source, id) == 0) {
			return 0;
		}
		return verify(source, id);
	}

	private static int runAll(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		HemoTestScenarioCatalog.clearActive(player);
		int passed = 0;
		var failed = new java.util.ArrayList<String>();
		for (HemoTestScenario scenario : HemoTestScenarioCatalog.all()) {
			HemoTestResult result;
			HemoTestScenarioCatalog.markActive(player, scenario);
			try {
				scenario.setup().apply(player);
				result = scenario.verify().check(player);
			} catch (RuntimeException exception) {
				result = HemoTestResult.fail(exception.getClass().getSimpleName() + ": "
						+ String.valueOf(exception.getMessage()));
			} finally {
				HemoTestScenarioCatalog.clearActive(player);
			}
			if (result.passed()) {
				passed++;
			}
			else {
				failed.add(scenario.id());
			}
			HemoTestResult report = result;
			source.sendSuccess(() -> Component.literal((report.passed() ? "PASS " : "FAIL ") + scenario.id()
					+ ": " + report.message()).withStyle(report.passed() ? ChatFormatting.GREEN : ChatFormatting.RED), false);
		}
		int total = HemoTestScenarioCatalog.all().size();
		boolean allPassed = failed.isEmpty();
		int passedCount = passed;
		source.sendSuccess(() -> Component.literal("Hemomancy tests: " + passedCount + "/" + total + " passed"
				+ (allPassed ? "." : "; failed: " + String.join(", ", failed)))
				.withStyle(allPassed ? ChatFormatting.GREEN : ChatFormatting.RED), false);
		return allPassed ? 1 : 0;
	}

	private static int status(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		var active = HemoTestScenarioCatalog.active(player);
		if (active.isEmpty()) {
			source.sendSuccess(() -> Component.literal("No active Hemomancy test scenario."), false);
			return 0;
		}
		HemoTestScenario scenario = active.get();
		source.sendSuccess(() -> Component.literal("Active: " + scenario.id() + " - " + scenario.description())
				.withStyle(ChatFormatting.GOLD), false);
		return 1;
	}

	private static int clear(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		JourneyAutoRunner.cancel(player);
		if (JourneyRoute.is(player, JourneyRoute.CIRCUS)) {
			CircusJourneyResult journeyClear = CircusJourneyController.clear(player);
			if (!journeyClear.passed()) {
				source.sendFailure(Component.literal(journeyClear.message()));
				return 0;
			}
		} else if (JourneyRoute.is(player, JourneyRoute.UNSTAINED)) {
			UnstainedJourneyResult journeyClear = UnstainedJourneyController.clear(player);
			if (!journeyClear.passed()) {
				source.sendFailure(Component.literal(journeyClear.message()));
				return 0;
			}
		} else {
			HemoJourneyResult journeyClear = HemoJourneyController.clear(player);
			if (!journeyClear.passed()) {
				source.sendFailure(Component.literal(journeyClear.message()));
				return 0;
			}
		}
		HemoTestScenarioCatalog.clearActive(player);
		source.sendSuccess(() -> Component.literal("Cleared the active Hemomancy test fixture."), false);
		return 1;
	}

	private static int journeyStart(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		JourneyAutoRunner.cancel(player);
		return reportJourney(source, HemoJourneyController.start(player));
	}

	private static int journeyRun(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		return JourneyAutoRunner.runHarbinger(player) ? 1 : 0;
	}

	private static int journeyRunAll(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		return JourneyAutoRunner.runAll(player) ? 1 : 0;
	}

	private static int journeyNext(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		JourneyAutoRunner.cancel(player);
		return reportJourney(source, HemoJourneyController.next(player));
	}

	private static int journeyStatus(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		HemoJourneyResult result = HemoJourneyController.status(player);
		int reported = reportJourney(source, result);
		String automation = JourneyAutoRunner.describe(player);
		if (!automation.isEmpty()) source.sendSuccess(() -> Component.literal(automation), false);
		return reported;
	}

	private static int journeyReset(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		JourneyAutoRunner.cancel(player);
		return reportJourney(source, HemoJourneyController.reset(player));
	}

	private static int unstainedJourneyStart(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		JourneyAutoRunner.cancel(player);
		return reportJourney(source, UnstainedJourneyController.start(player));
	}

	private static int unstainedJourneyStart(CommandSourceStack source, String mode)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		JourneyAutoRunner.cancel(player);
		return reportJourney(source, UnstainedJourneyController.start(player, mode));
	}

	private static int unstainedJourneyRun(CommandSourceStack source, String mode)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		return JourneyAutoRunner.runUnstained(player, mode) ? 1 : 0;
	}

	private static int unstainedJourneyNext(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		JourneyAutoRunner.cancel(player);
		return reportJourney(source, UnstainedJourneyController.next(player));
	}

	private static int unstainedJourneyStatus(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		UnstainedJourneyResult result = UnstainedJourneyController.status(player);
		int reported = reportJourney(source, result);
		String automation = JourneyAutoRunner.describe(player);
		if (!automation.isEmpty()) source.sendSuccess(() -> Component.literal(automation), false);
		return reported;
	}

	private static int unstainedJourneyReset(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		JourneyAutoRunner.cancel(player);
		return reportJourney(source, UnstainedJourneyController.reset(player));
	}

	private static int circusJourneyStart(CommandSourceStack source, String mode)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		JourneyAutoRunner.cancel(player);
		return reportJourney(source, CircusJourneyController.start(player, mode));
	}

	private static int circusJourneyRun(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		return JourneyAutoRunner.runCircus(source.getPlayerOrException()) ? 1 : 0;
	}

	private static int circusJourneyRun(CommandSourceStack source, String mode)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		return JourneyAutoRunner.runCircus(source.getPlayerOrException(), mode) ? 1 : 0;
	}

	private static int circusJourneyNext(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		JourneyAutoRunner.cancel(source.getPlayerOrException());
		return reportJourney(source, CircusJourneyController.next(source.getPlayerOrException()));
	}

	private static int circusJourneyStatus(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		CircusJourneyResult result = CircusJourneyController.status(source.getPlayerOrException());
		int reported = reportJourney(source, result);
		String automation = JourneyAutoRunner.describe(source.getPlayerOrException());
		if (!automation.isEmpty()) source.sendSuccess(() -> Component.literal(automation), false);
		return reported;
	}

	private static int circusJourneyReset(CommandSourceStack source)
			throws com.mojang.brigadier.exceptions.CommandSyntaxException {
		ServerPlayer player = source.getPlayerOrException();
		String mode = CircusJourneyController.mode(player);
		JourneyAutoRunner.cancel(player);
		CircusJourneyResult cleared = CircusJourneyController.clear(player);
		return cleared.passed() ? reportJourney(source, CircusJourneyController.start(player, mode)) : reportJourney(source, cleared);
	}

	private static int reportJourney(CommandSourceStack source, HemoJourneyResult result) {
		Component message = Component.literal(result.stage().id() + ": " + result.message())
				.withStyle(result.passed() ? ChatFormatting.GREEN : ChatFormatting.RED);
		if (result.passed()) {
			source.sendSuccess(() -> message, false);
			return 1;
		}
		source.sendFailure(message);
		return 0;
	}

	private static int reportJourney(CommandSourceStack source, UnstainedJourneyResult result) {
		Component message = Component.literal(result.stage().id() + ": " + result.message())
				.withStyle(result.passed() ? ChatFormatting.GREEN : ChatFormatting.RED);
		if (result.passed()) {
			source.sendSuccess(() -> message, false);
			return 1;
		}
		source.sendFailure(message);
		return 0;
	}

	private static int reportJourney(CommandSourceStack source, CircusJourneyResult result) {
		Component message = Component.literal(result.stage().id() + ": " + result.message())
				.withStyle(result.passed() ? ChatFormatting.GREEN : ChatFormatting.RED);
		if (result.passed()) {
			source.sendSuccess(() -> message, false);
			return 1;
		}
		source.sendFailure(message);
		return 0;
	}

	private static HemoTestScenario requireScenario(CommandSourceStack source, String id) {
		var scenario = HemoTestScenarioCatalog.find(id);
		if (scenario.isEmpty()) {
			source.sendFailure(Component.literal("Unknown scenario '" + id + "'. Use /hemo test list."));
		}
		return scenario.orElse(null);
	}
}
