package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.common.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.circus.CircusProgressRules;
import com.vincenthuto.hemomancy.common.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class CircusJourneyController {
	private static final String MODE = "hemomancy.dev_test.journey.circus_mode";
	private static final String SUCCESSION = "succession";
	private static final String LIBERATION = "liberation";

	private CircusJourneyController() { }

	public static CircusJourneyResult start(ServerPlayer player, String mode) {
		HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
		if (!captured.passed()) return fail(captured.message());
		HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
		if (!reset.passed()) return fail(reset.message());
		String selected = LIBERATION.equals(mode) ? LIBERATION : SUCCESSION;
		player.getPersistentData().putString(JourneyRoute.KEY, JourneyRoute.CIRCUS);
		player.getPersistentData().putString(MODE, selected);
		try {
			BlockPos origin = CircusJourneyFixtures.findClearOrigin(player, LIBERATION.equals(selected));
			player.getPersistentData().putLong(HemoJourneyFixtures.ORIGIN_KEY, origin.asLong());
			player.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
					player.serverLevel().dimension().location().toString());
			CircusJourneyFixtures.prepare(player, origin);
			setStage(player, CircusJourneyStage.DISCOVERY);
			return pass(player, "Circus " + selected + " journey started. ");
		} catch (RuntimeException exception) {
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			clearKeys(player);
			return fail("Circus fixture preparation failed: " + exception.getMessage()
					+ (restored.passed() ? " Pre-journey state was restored." : " " + restored.message()));
		}
	}

	public static CircusJourneyResult next(ServerPlayer player) {
		CircusJourneyResult inactive = requireActive(player);
		if (inactive != null) return inactive;
		CircusJourneyStage current = stage(player);
		if (current == CircusJourneyStage.COMPLETE) return clear(player);
		CircusJourneyResult check = check(player, current);
		if (!check.passed()) return check;
		setStage(player, CircusJourneyStage.values()[current.ordinal() + 1]);
		if (stage(player) == CircusJourneyStage.ACCLIMATING) {
			CircusPlayerProgress.addAcclimation(player,
					CircusProgressRules.MAX_ACCLIMATION - CircusPlayerProgress.acclimation(player));
			CircusPlayerProgress.sync(player, true);
		}
		moveAndExplain(player);
		return pass(player, "");
	}

	public static CircusJourneyResult status(ServerPlayer player) {
		CircusJourneyResult inactive = requireActive(player);
		if (inactive != null) return inactive;
		CircusJourneyResult check = check(player, stage(player));
		return new CircusJourneyResult(check.passed(), stage(player), action(player)
				+ (check.passed() ? " Conditions currently pass." : " Unmet: " + check.message()));
	}

	public static CircusJourneyResult clear(ServerPlayer player) {
		if (!player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY, Tag.TAG_COMPOUND))
			return new CircusJourneyResult(true, CircusJourneyStage.COMPLETE, "No active Circus journey snapshot.");
		if (!JourneyRoute.is(player, JourneyRoute.CIRCUS)) return fail("The active journey belongs to another route.");
		if (player.getPersistentData().contains(HemoJourneyFixtures.ORIGIN_KEY, Tag.TAG_LONG)) {
			BlockPos origin = origin(player);
			var level = HemoJourneyFixtures.fixtureLevel(player);
			HemoJourneyFixtures.cleanup(player, origin);
			CircusPavilionSavedData.get(level).removeSite(level, origin.above());
		}
		HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
		if (restored.passed()) clearKeys(player);
		return new CircusJourneyResult(restored.passed(), CircusJourneyStage.COMPLETE,
				restored.passed() ? "Circus journey complete; restored the pre-journey state." : restored.message());
	}

	public static String mode(ServerPlayer player) {
		return LIBERATION.equals(player.getPersistentData().getString(MODE)) ? LIBERATION : SUCCESSION;
	}

	static CircusJourneyResult check(ServerPlayer player, CircusJourneyStage stage) {
		return switch (stage) {
			case DISCOVERY -> require(stage, CircusDiscoveryProgress.hasDiscovered(player), "Visit a generated Circus pavilion first.");
			case PERFORMERS -> require(stage, CircusPlayerProgress.acclimation(player) >= 200,
					"Speak to each of the four performers.");
			case ACCLIMATING -> require(stage, CircusProgressRules.stage(CircusPlayerProgress.acclimation(player))
					== CircusProgressRules.Stage.ATTUNED, "The acclimation fixture has not reached Attuned.");
			case ATTENTION -> require(stage, (CircusPlayerProgress.challenges(player) & 1) != 0,
					"Complete the neutral peacock-spider attention act before choosing a route.");
			case ROUTE -> require(stage, CircusPlayerProgress.route(player) == route(player), "Choose the requested Ringmaster route.");
			case ACTS -> require(stage, LIBERATION.equals(mode(player))
					|| (CircusPlayerProgress.challenges(player) & CircusRouteRules.ALL_CHALLENGES) == CircusRouteRules.ALL_CHALLENGES,
					"Complete all four controlled acts.");
			case FINALE -> require(stage, CircusPlayerProgress.route(player) == completedRoute(player), "Finish the Ringmaster encounter.");
			case REWARD -> require(stage, CircusJourneyAutomation.hasReward(player, mode(player)), "The route reward was not awarded.");
			case COMPLETE -> new CircusJourneyResult(true, stage, "Ready to restore the snapshot.");
		};
	}

	private static CircusJourneyResult requireActive(ServerPlayer player) {
		if (!player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY, Tag.TAG_COMPOUND)) return fail("No active journey snapshot.");
		if (!JourneyRoute.is(player, JourneyRoute.CIRCUS)) return fail("The active journey belongs to another route.");
		if (!player.getPersistentData().contains(HemoJourneyFixtures.ORIGIN_KEY, Tag.TAG_LONG)) return fail("Active journey has no fixture origin.");
		return null;
	}

	static BlockPos origin(ServerPlayer player) { return BlockPos.of(player.getPersistentData().getLong(HemoJourneyFixtures.ORIGIN_KEY)); }
	static CircusJourneyStage stage(ServerPlayer player) { return CircusJourneyStage.byId(player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY)); }
	static CircusRouteRules.Route route(ServerPlayer player) { return LIBERATION.equals(mode(player)) ? CircusRouteRules.Route.LIBERATION : CircusRouteRules.Route.SUCCESSION; }
	private static CircusRouteRules.Route completedRoute(ServerPlayer player) { return LIBERATION.equals(mode(player)) ? CircusRouteRules.Route.LIBERATION_COMPLETE : CircusRouteRules.Route.SUCCESSION_COMPLETE; }

	private static void setStage(ServerPlayer player, CircusJourneyStage stage) {
		player.getPersistentData().putString(HemoJourneySnapshot.STAGE_KEY, stage.id());
	}

	private static void moveAndExplain(ServerPlayer player) {
		if (stage(player) != CircusJourneyStage.COMPLETE) {
			BlockPos origin = origin(player);
			player.teleportTo(HemoJourneyFixtures.fixtureLevel(player), origin.getX() + 0.5D, origin.getY() + 1.0D,
					origin.getZ() - 7.5D, 0.0F, 0.0F);
			player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(origin.above()));
		}
		player.sendSystemMessage(Component.literal(action(player)));
	}

	private static String action(ServerPlayer player) {
		return switch (stage(player)) {
			case DISCOVERY -> "Use the Scarlet Waybill or /locate structure hemomancy:circus_pavilion, enter a generated pavilion, then return and run circus next.";
			case PERFORMERS -> "Right-click the Fire Eater, Acrobat, Stilt Walker, and Knife Thrower once each.";
			case ACCLIMATING -> "Inspect the Attuned perception treatment, entity presentation, carousel, particles, and sound.";
			case ATTENTION -> "Ask the Ringmaster to begin the neutral attention act and keep the trained peacock spider in view.";
			case ROUTE -> LIBERATION.equals(mode(player)) ? "Tell the Ringmaster you reject the pact." : "Tell the Ringmaster you accept the succession pact.";
			case ACTS -> LIBERATION.equals(mode(player)) ? "Liberation has no further controlled acts; advance to the finale." : "Ask the Ringmaster for the remaining three controlled acts and complete them.";
			case FINALE -> LIBERATION.equals(mode(player)) ? "Begin the finale, sever all three riders with Thread Ripper, break their anchors, then kill the Ringmaster." : "Begin the finale, down the troupe after the rafter phase, then defeat the descended Ringmaster nonlethally.";
			case REWARD -> LIBERATION.equals(mode(player)) ? "Confirm Thread Ripper and the Ringmaster Topper were awarded, then cast Thread Ripper on a hostile tethered puppet." : "Confirm the Ringmaster Pattern and Topper were awarded, then summon the Pattern and relay a Crossbar command.";
			case COMPLETE -> "Run circus next once more to restore the pre-journey snapshot.";
		};
	}

	private static CircusJourneyResult pass(ServerPlayer player, String prefix) { return new CircusJourneyResult(true, stage(player), prefix + action(player)); }
	private static CircusJourneyResult require(CircusJourneyStage stage, boolean passed, String message) { return passed ? new CircusJourneyResult(true, stage, "Passed.") : CircusJourneyResult.fail(stage, message); }
	private static CircusJourneyResult fail(String message) { return CircusJourneyResult.fail(CircusJourneyStage.DISCOVERY, message); }

	private static void clearKeys(ServerPlayer player) {
		player.getPersistentData().remove(HemoJourneyFixtures.ORIGIN_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.DIMENSION_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.OWNED_BLOCKS_KEY);
		player.getPersistentData().remove(MODE);
		player.getPersistentData().remove(JourneyRoute.KEY);
	}
}
