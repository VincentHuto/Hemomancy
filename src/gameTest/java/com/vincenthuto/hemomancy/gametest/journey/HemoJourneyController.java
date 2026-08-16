package com.vincenthuto.hemomancy.gametest.journey;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public final class HemoJourneyController {
	public static final String VERIFIED_STAGE_KEY = "hemomancy.dev_test.journey.verified_stage";
	private HemoJourneyController() {
	}

	public static HemoJourneyResult start(ServerPlayer player) {
		HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
		if (!captured.passed()) return captured;
		HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
		if (!reset.passed()) return reset;
		player.getPersistentData().remove(VERIFIED_STAGE_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.OWNED_OUTPUTS_KEY);
		BlockPos origin;
		try {
			origin = HemoJourneyFixtures.findClearOrigin(player);
		} catch (RuntimeException exception) {
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			if (restored.passed()) clearFixtureKeys(player);
			return HemoJourneyResult.fail(HemoJourneyStage.MORTAL_DISPLAY,
					"Journey fixture selection failed: " + exceptionMessage(exception)
							+ (restored.passed() ? " Pre-journey state was restored." : " " + restored.message()));
		}
		player.getPersistentData().putLong(HemoJourneyFixtures.ORIGIN_KEY, origin.asLong());
		player.getPersistentData().putString(HemoJourneyFixtures.DIMENSION_KEY,
				player.serverLevel().dimension().location().toString());
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.MORTAL_DISPLAY, origin);
			moveAndExplain(player, HemoJourneyStage.MORTAL_DISPLAY, origin);
			return new HemoJourneyResult(true, HemoJourneyStage.MORTAL_DISPLAY,
					"Journey started. " + action(HemoJourneyStage.MORTAL_DISPLAY));
		} catch (RuntimeException exception) {
			HemoJourneyFixtures.cleanup(player, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			if (restored.passed()) clearFixtureKeys(player);
			return HemoJourneyResult.fail(HemoJourneyStage.MORTAL_DISPLAY,
					"Journey fixture preparation failed: " + exceptionMessage(exception)
							+ (restored.passed() ? " Pre-journey state was restored." : " " + restored.message()));
		}
	}

	public static HemoJourneyResult next(ServerPlayer player) {
		HemoJourneyResult active = requireActive(player);
		if (active != null) return active;
		BlockPos origin = origin(player);
		HemoJourneyStage current = currentStage(player);
		if (current == HemoJourneyStage.COMPLETE) {
			HemoJourneyFixtures.cleanupForExit(player, current, origin);
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			if (restored.passed()) clearFixtureKeys(player);
			return restored.passed()
					? new HemoJourneyResult(true, HemoJourneyStage.COMPLETE,
							"Journey complete; restored pre-journey player state and removed fixtures.")
					: HemoJourneyResult.fail(HemoJourneyStage.COMPLETE, restored.message());
		}
		boolean shouldVerify = HemoJourneyTransition.shouldVerify(current,
				player.getPersistentData().getString(VERIFIED_STAGE_KEY));
		if (shouldVerify) {
			HemoJourneyResult verified = HemoJourneyChecks.verify(player, current, origin, true);
			if (!verified.passed()) return verified;
			player.getPersistentData().putString(VERIFIED_STAGE_KEY, current.id());
		}
		HemoJourneyStage next = HemoJourneyTransition.next(current, true, true);
		CompoundTag priorBaseline = player.getPersistentData().getCompound(HemoJourneyFixtures.BASELINE_KEY).copy();
		try {
			boolean retainCentrifuge = current == HemoJourneyStage.SEPARATION_STARTED
					&& next == HemoJourneyStage.ENZYME_RECOVERED;
			if (!retainCentrifuge) HemoJourneyFixtures.cleanupOwnedOutputs(player, origin);
			if (retainCentrifuge) {
				// The live centrifuge must survive so the player can recover its real output.
			} else if (next == HemoJourneyStage.COMPLETE) {
				HemoJourneyFixtures.cleanup(player, origin);
			} else {
				HemoJourneyFixtures.prepare(player, next, origin);
			}
			moveAndExplain(player, next, origin);
			player.getPersistentData().putString(HemoJourneySnapshot.STAGE_KEY, next.id());
			player.getPersistentData().remove(VERIFIED_STAGE_KEY);
			return new HemoJourneyResult(true, next, next == HemoJourneyStage.COMPLETE
					? "All checkpoints passed. Run journey next once more to restore your snapshot."
					: action(next));
		} catch (RuntimeException exception) {
			HemoJourneyFixtures.cleanup(player, origin);
			player.getPersistentData().put(HemoJourneyFixtures.BASELINE_KEY, priorBaseline);
			player.getPersistentData().putString(HemoJourneySnapshot.STAGE_KEY,
					HemoJourneyTransition.next(current, true, false).id());
			return HemoJourneyResult.fail(current, "Next fixture failed: " + exceptionMessage(exception)
					+ ". The verified checkpoint is latched; run journey next to retry the transition.");
		}
	}

	public static HemoJourneyResult status(ServerPlayer player) {
		HemoJourneyResult active = requireActive(player);
		if (active != null) return active;
		HemoJourneyStage stage = currentStage(player);
		if (!HemoJourneyTransition.shouldVerify(stage,
				player.getPersistentData().getString(VERIFIED_STAGE_KEY))) {
			return new HemoJourneyResult(true, stage,
					"Checkpoint verified and latched. Run journey next to retry the pending transition.");
		}
		HemoJourneyResult check = HemoJourneyChecks.verify(player, stage, origin(player));
		return new HemoJourneyResult(check.passed(), stage,
				action(stage) + (check.passed() ? " Conditions currently pass." : "\nUnmet:\n" + check.message()));
	}

	public static HemoJourneyResult reset(ServerPlayer player) {
		HemoJourneyResult active = requireActive(player);
		if (active != null) return active;
		BlockPos origin = origin(player);
		HemoJourneyFixtures.cleanupForExit(player, currentStage(player), origin);
		HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
		if (!reset.passed()) return reset;
		player.getPersistentData().remove(VERIFIED_STAGE_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.OWNED_OUTPUTS_KEY);
		try {
			HemoJourneyFixtures.prepare(player, HemoJourneyStage.MORTAL_DISPLAY, origin);
			moveAndExplain(player, HemoJourneyStage.MORTAL_DISPLAY, origin);
			return new HemoJourneyResult(true, HemoJourneyStage.MORTAL_DISPLAY,
					"Journey reset. " + action(HemoJourneyStage.MORTAL_DISPLAY));
		} catch (RuntimeException exception) {
			HemoJourneyFixtures.cleanup(player, origin);
			return HemoJourneyResult.fail(HemoJourneyStage.MORTAL_DISPLAY,
					"Journey reset fixture failed: " + exceptionMessage(exception));
		}
	}

	public static HemoJourneyResult clear(ServerPlayer player) {
		if (player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY)) {
			if (player.getPersistentData().contains(HemoJourneyFixtures.ORIGIN_KEY, Tag.TAG_LONG)) {
				HemoJourneyFixtures.cleanupForExit(player, currentStage(player), origin(player));
			}
			HemoJourneyResult restored = HemoJourneySnapshot.restore(player);
			if (restored.passed()) clearFixtureKeys(player);
			return restored;
		}
		return new HemoJourneyResult(true, HemoJourneyStage.MORTAL_DISPLAY, "No active journey snapshot.");
	}

	private static HemoJourneyResult requireActive(ServerPlayer player) {
		if (!player.getPersistentData().contains(HemoJourneySnapshot.SNAPSHOT_KEY, Tag.TAG_COMPOUND)) {
			return HemoJourneyResult.fail(HemoJourneyStage.MORTAL_DISPLAY, "No active journey snapshot.");
		}
		if (!player.getPersistentData().contains(HemoJourneyFixtures.ORIGIN_KEY, Tag.TAG_LONG)) {
			return HemoJourneyResult.fail(currentStage(player), "Active journey has no fixture origin.");
		}
		if (!player.getPersistentData().contains(HemoJourneyFixtures.DIMENSION_KEY, Tag.TAG_STRING)) {
			return HemoJourneyResult.fail(currentStage(player),
					"Active journey has no fixture dimension. Use /hemo test clear after restoring that persistent key from backup.");
		}
		try {
			HemoJourneyFixtures.fixtureLevel(player);
		} catch (RuntimeException exception) {
			return HemoJourneyResult.fail(currentStage(player), exceptionMessage(exception));
		}
		return null;
	}

	private static BlockPos origin(ServerPlayer player) {
		return BlockPos.of(player.getPersistentData().getLong(HemoJourneyFixtures.ORIGIN_KEY));
	}

	private static HemoJourneyStage currentStage(ServerPlayer player) {
		String id = player.getPersistentData().getString(HemoJourneySnapshot.STAGE_KEY);
		for (HemoJourneyStage stage : HemoJourneyStage.values()) if (stage.id().equals(id)) return stage;
		return HemoJourneyStage.MORTAL_DISPLAY;
	}

	public static BlockPos landing(BlockPos origin) {
		return origin.offset(0, 1, -2);
	}

	private static void moveAndExplain(ServerPlayer player, HemoJourneyStage stage, BlockPos origin) {
		if (stage != HemoJourneyStage.COMPLETE) {
			BlockPos landing = landing(origin);
			player.teleportTo(HemoJourneyFixtures.fixtureLevel(player), landing.getX() + 0.5D, landing.getY(),
					landing.getZ() + 0.5D, 0.0F, 0.0F);
			player.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.atCenterOf(target(stage, origin)));
		}
		player.sendSystemMessage(Component.literal(action(stage)));
	}

	private static String action(HemoJourneyStage stage) {
		return switch (stage) {
			case MORTAL_DISPLAY -> "Right-click the Mortal Display, then run journey next.";
			case SANGUINE_INITIATION -> "Aim at the center Hematic Iron block and invoke the blood-crafting key. After initiation, Blood Projection is selected from the lower Charm radial and normally conjured with the use-manipulation key.";
			case VESSEL_FILLED -> "Use the supplied Bloody Jug to fill the vessel to 5,000 mL.";
			case FORMATION_PROJECTED -> "The fixture supplied the legitimate Blood Projection hand tool. Normally select the lower Blood Projection utility in the Charm radial and conjure it with the use-manipulation key. Hold it on the Venous Stone until one Sanguine Formation appears, then release. The checkpoint accepts the formation either in your inventory or on the fixture.";
			case LIBER_CRAFTED -> "The fixture supplied Blood Projection and one Sanguine Formation in your offhand. Hold projection on the center Bookshelf, then pick up the crafted Liber Sanguinum before running journey next.";
			case HEMATIC_IRON_CRAFTED -> "The fixture supplied Blood Projection and one Ink Sac in your offhand. Hold projection on the center Iron Block, then pick up the crafted Hematic Iron Block before running journey next.";
			case VICAR_REWARD -> "Speak to the marked Vicar and claim the First Bloodcraft completion kit. Keep inventory rewards and leave any overflow drops beside the Vicar until journey next.";
			case VOTARY_RITE -> "Invoke the Rite of the Votary at its center Hematic Iron block, then run journey next.";
			case DEGREE_2_REACHED -> "You are now a Votary. Run journey next to meet the Alchemist.";
			case ALCHEMIST_BRIEFING -> "Speak to the marked Alchemist and accept The First Separation.";
			case CENTRIFUGE_PREPARED -> "Place the supplied Glass Bottle and Copper Ingot into the two Iron Braziers, light both with Blood Projection, then project the centrifuge structure with the Ferric Binder. Pick up and place the crafted Vial Centrifuge at the fixture center, then run journey next.";
			case SEPARATION_STARTED -> "Use the two loose Blood Vials from the briefing on the two cows without damaging them. Put the sampled vials in opposite centrifuge slots, press Start, then wait for the spin to finish before running journey next.";
			case ENZYME_RECOVERED -> "Open the centrifuge, take the Vivacious Enzyme from its output, then run journey next.";
			case ALCHEMIST_REWARD -> "Speak to the marked Alchemist and claim the First Separation sampling kit.";
			case INITIATE_RITE -> "Invoke the prepared Rite of the Incarnadine Fane and wait until Degree 3 is awarded.";
			case ADEPT_RITE -> "Invoke the prepared Rite of the Sanguine Brotherhood and wait until Degree 4 is awarded.";
			case ILLUMINATUS_RITE -> "Invoke the prepared Rite of the Crimson Lodge and wait until Degree 5 is awarded.";
			case SANCTIFIED_RITE -> "Invoke the prepared Rite of the Bloodline Covenant and wait until Degree 6 is awarded.";
			case ARCHON_RITE -> "Invoke the prepared Rite of the Hematic Order and wait until Degree 7 is awarded.";
			case COMPLETE -> "Run journey next to restore the pre-journey snapshot.";
		};
	}

	private static BlockPos target(HemoJourneyStage stage, BlockPos origin) {
		return switch (stage) {
			case MORTAL_DISPLAY, FORMATION_PROJECTED -> origin.above();
			case SANGUINE_INITIATION -> origin.above();
			case LIBER_CRAFTED, HEMATIC_IRON_CRAFTED -> origin.above();
			case VESSEL_FILLED -> origin.above();
			case VICAR_REWARD -> origin.above();
			case VOTARY_RITE -> origin.above();
			case DEGREE_2_REACHED, ALCHEMIST_BRIEFING, ALCHEMIST_REWARD -> origin.above();
			case CENTRIFUGE_PREPARED, SEPARATION_STARTED, ENZYME_RECOVERED -> origin.above(2);
			case INITIATE_RITE, ADEPT_RITE, ILLUMINATUS_RITE, SANCTIFIED_RITE, ARCHON_RITE -> origin.above(3);
			case COMPLETE -> origin;
		};
	}

	private static void clearFixtureKeys(ServerPlayer player) {
		player.getPersistentData().remove(HemoJourneyFixtures.ORIGIN_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.DIMENSION_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.OWNED_BLOCKS_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.BASELINE_KEY);
		player.getPersistentData().remove(HemoJourneyFixtures.OWNED_OUTPUTS_KEY);
		player.getPersistentData().remove(VERIFIED_STAGE_KEY);
	}

	private static String exceptionMessage(RuntimeException exception) {
		return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
	}
}
