package com.vincenthuto.hemomancy.gametest.journey;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Runs the development-only operator journeys through their real server hooks. */
public final class JourneyAutoRunner {
	private static final int STAGE_TIMEOUT_TICKS = 1200;
	private static final Map<UUID, RunState> ACTIVE = new HashMap<>();
	private static final Map<UUID, String> LAST_FAILURE = new HashMap<>();
	private static boolean registered;

	private JourneyAutoRunner() { }

	public static void register() {
		if (registered) return;
		registered = true;
		NeoForge.EVENT_BUS.addListener(JourneyAutoRunner::onServerTick);
	}

	public static boolean runHarbinger(ServerPlayer player) {
		return start(player, List.of(Route.HARBINGER), true);
	}

	public static boolean runUnstained(ServerPlayer player, String mode) {
		return start(player, List.of("novitiate".equals(mode) ? Route.UNSTAINED_NOVITIATE : Route.UNSTAINED_CURE), true);
	}

	public static boolean runCircus(ServerPlayer player) {
		return start(player, List.of(Route.CIRCUS_SUCCESSION, Route.CIRCUS_LIBERATION), false);
	}

	public static boolean runCircus(ServerPlayer player, String mode) {
		return start(player, List.of("liberation".equals(mode) ? Route.CIRCUS_LIBERATION : Route.CIRCUS_SUCCESSION), true);
	}

	public static boolean runAll(ServerPlayer player) {
		return start(player, List.of(Route.HARBINGER, Route.UNSTAINED_CURE, Route.UNSTAINED_NOVITIATE,
				Route.CIRCUS_SUCCESSION, Route.CIRCUS_LIBERATION), false);
	}

	public static void cancel(ServerPlayer player) {
		LAST_FAILURE.remove(player.getUUID());
		if (ACTIVE.remove(player.getUUID()) != null) {
			player.sendSystemMessage(Component.literal("Journey automation stopped; the active snapshot and fixture were retained.")
					.withStyle(ChatFormatting.YELLOW));
		}
	}

	public static String describe(ServerPlayer player) {
		RunState state = ACTIVE.get(player.getUUID());
		if (state == null) return LAST_FAILURE.getOrDefault(player.getUUID(), "");
		return "Automation running: " + state.route().label + " at " + state.stageId
				+ " (route " + (state.routeIndex + 1) + "/" + state.routes.size() + ").";
	}

	private static boolean start(ServerPlayer player, List<Route> routes, boolean allowResume) {
		RunState active = ACTIVE.get(player.getUUID());
		if (active != null) {
			if (!routes.equals(active.routes)) {
				player.sendSystemMessage(Component.literal("Different journey automation is already active: "
						+ active.route().label + ". Stop or clear it before changing routes.").withStyle(ChatFormatting.RED));
				return false;
			}
			player.sendSystemMessage(Component.literal(describe(player)).withStyle(ChatFormatting.AQUA));
			return true;
		}
		boolean hasSnapshot = player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY, Tag.TAG_COMPOUND);
		if (hasSnapshot && (!allowResume || !routes.getFirst().owns(player))) {
			player.sendSystemMessage(Component.literal(
					"Another journey snapshot is active. Resume its matching run command or use /hemo test clear first.")
					.withStyle(ChatFormatting.RED));
			return false;
		}
		RunState state = new RunState(routes);
		LAST_FAILURE.remove(player.getUUID());
		ACTIVE.put(player.getUUID(), state);
		if (!hasSnapshot && !startRoute(player, state.route())) {
			ACTIVE.remove(player.getUUID());
			return false;
		}
		player.sendSystemMessage(Component.literal("Journey automation started: " + state.route().label + ".")
				.withStyle(ChatFormatting.AQUA));
		return true;
	}

	private static void onServerTick(ServerTickEvent.Post event) {
		for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
			RunState state = ACTIVE.get(player.getUUID());
			if (state != null) tick(player, state);
		}
	}

	private static void tick(ServerPlayer player, RunState state) {
		try {
			if (!player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
				advanceRoute(player, state);
				return;
			}
			String stageId = player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY);
			if (!stageId.equals(state.stageId)) {
				state.stageId = stageId;
				state.stageTicks = 0;
				state.acted = false;
				state.lastFailure = "";
				player.sendSystemMessage(Component.literal("AUTO " + state.route().label + ": " + stageId)
						.withStyle(ChatFormatting.GRAY));
			}
			state.stageTicks++;
			if (state.stageTicks > STAGE_TIMEOUT_TICKS) {
				fail(player, state, "Timed out at " + stageId + (state.lastFailure.isEmpty() ? "." : ": " + state.lastFailure));
				return;
			}
			BlockPos origin = BlockPos.of(player.getPersistentData().getLong(HemoJourneyFixtures.ORIGIN_KEY));
			if ((!state.acted || state.route().runsContinuously(stageId)) && !"complete".equals(stageId)) {
				state.route().perform(player, stageId, origin);
				state.acted = true;
			}
			if (state.stageTicks % 2 != 0) return;
			boolean passed;
			String message;
			if (state.route() == Route.HARBINGER) {
				HemoJourneyResult result = HemoJourneyController.next(player);
				passed = result.passed();
				message = result.message();
			} else if (!state.route().circus()) {
				UnstainedJourneyResult result = UnstainedJourneyController.next(player);
				passed = result.passed();
				message = result.message();
			} else {
				CircusJourneyResult result = CircusJourneyController.next(player);
				passed = result.passed();
				message = result.message();
			}
			if (!passed) {
				state.lastFailure = message;
				if (state.route() == Route.HARBINGER
						&& HemoJourneyStage.FORMATION_PROJECTED.id().equals(stageId)) state.acted = false;
			}
		} catch (RuntimeException exception) {
			fail(player, state, exception.getClass().getSimpleName() + ": "
					+ String.valueOf(exception.getMessage()));
		}
	}

	private static void advanceRoute(ServerPlayer player, RunState state) {
		player.sendSystemMessage(Component.literal("PASS " + state.route().label).withStyle(ChatFormatting.GREEN));
		state.routeIndex++;
		if (state.routeIndex >= state.routes.size()) {
			ACTIVE.remove(player.getUUID());
			LAST_FAILURE.remove(player.getUUID());
			player.sendSystemMessage(Component.literal("PASS all automated journeys; the original snapshot was restored.")
					.withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
			return;
		}
		state.resetStage();
		if (!startRoute(player, state.route())) {
			ACTIVE.remove(player.getUUID());
		}
	}

	private static boolean startRoute(ServerPlayer player, Route route) {
		boolean passed;
		String message;
		if (route == Route.HARBINGER) {
			HemoJourneyResult result = HemoJourneyController.start(player);
			passed = result.passed();
			message = result.message();
		} else if (!route.circus()) {
			UnstainedJourneyResult result = UnstainedJourneyController.start(player, route.mode);
			passed = result.passed();
			message = result.message();
		} else {
			CircusJourneyResult result = CircusJourneyController.start(player, route.mode);
			passed = result.passed();
			message = result.message();
		}
		if (!passed) {
			player.sendSystemMessage(Component.literal("Journey automation could not start " + route.label + ": " + message)
					.withStyle(ChatFormatting.RED));
		}
		return passed;
	}

	private static void fail(ServerPlayer player, RunState state, String message) {
		ACTIVE.remove(player.getUUID());
		String failure = "Automation failed: " + state.route().label + " at " + state.stageId + ": " + message;
		LAST_FAILURE.put(player.getUUID(), failure);
		player.sendSystemMessage(Component.literal("FAIL " + state.route().label + " at " + state.stageId + ": " + message
				+ " The fixture and snapshot were retained; rerun this route to retry or use /hemo test clear.")
				.withStyle(ChatFormatting.RED));
	}

	static void tickForTest(ServerPlayer player) {
		RunState state = ACTIVE.get(player.getUUID());
		if (state != null) tick(player, state);
	}

	static boolean activeForTest(ServerPlayer player) {
		return ACTIVE.containsKey(player.getUUID());
	}

	private enum Route {
		HARBINGER("Harbinger", "harbinger"),
		UNSTAINED_CURE("Unstained cure", "cure"),
		UNSTAINED_NOVITIATE("Unstained novitiate", "novitiate"),
		CIRCUS_SUCCESSION("Circus succession", "succession"),
		CIRCUS_LIBERATION("Circus liberation", "liberation");

		private final String label;
		private final String mode;

		Route(String label, String mode) {
			this.label = label;
			this.mode = mode;
		}

		private boolean owns(ServerPlayer player) {
			if (this == HARBINGER) return JourneyRoute.is(player, JourneyRoute.HARBINGER);
			if (circus()) return JourneyRoute.is(player, JourneyRoute.CIRCUS)
					&& mode.equals(CircusJourneyController.mode(player));
			return JourneyRoute.is(player, JourneyRoute.UNSTAINED)
					&& mode.equals(UnstainedJourneyController.mode(player));
		}

		private void perform(ServerPlayer player, String stageId, BlockPos origin) {
			if (this == HARBINGER) HarbingerJourneyAutomation.perform(player, stageId, origin);
			else if (circus()) CircusJourneyAutomation.perform(player);
			else UnstainedJourneyAutomation.perform(player, UnstainedJourneyStage.byId(stageId), origin);
		}

		private boolean circus() { return this == CIRCUS_SUCCESSION || this == CIRCUS_LIBERATION; }
		private boolean runsContinuously(String stageId) {
			return circus() && (CircusJourneyStage.ATTENTION.id().equals(stageId)
					|| CircusJourneyStage.ACTS.id().equals(stageId)
					|| CircusJourneyStage.FINALE.id().equals(stageId));
		}
	}

	private static final class RunState {
		private final List<Route> routes;
		private int routeIndex;
		private String stageId = "";
		private int stageTicks;
		private boolean acted;
		private String lastFailure = "";

		private RunState(List<Route> routes) {
			this.routes = routes;
		}

		private Route route() {
			return routes.get(routeIndex);
		}

		private void resetStage() {
			stageId = "";
			stageTicks = 0;
			acted = false;
			lastFailure = "";
		}
	}
}
