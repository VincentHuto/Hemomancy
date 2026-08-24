package com.vincenthuto.hemomancy.gametest.journey;

import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;

public final class HemoJourneyController {
	public static final String VERIFIED_STAGE_KEY = "hemomancy.dev_test.journey.verified_stage";
	private HemoJourneyController() {
	}

	public static HemoJourneyResult start(ServerPlayer player) {
		if (ChamberVisitService.isActive(player)
				|| player.level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
			return HemoJourneyResult.fail(HemoJourneyStage.MORTAL_DISPLAY,
					"Return from the active Chamber of Will visit before starting the journey.");
		}
		HemoJourneyResult captured = HemoJourneySnapshot.capture(player);
		if (!captured.passed()) return captured;
		HemoJourneyResult reset = HemoJourneySnapshot.resetForJourney(player);
		if (!reset.passed()) return reset;
		player.getPersistentData().putString(JourneyRoute.KEY, JourneyRoute.HARBINGER);
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
			if (current == HemoJourneyStage.CHAMBER_RETURNED
					&& !HarbingerAdvancementGranter.hasAdvancement(player,
							HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED)) {
				if (!ChamberVisitService.isActive(player)) {
					if (!ChamberVisitService.beginRiteVisit(player)) {
						return HemoJourneyResult.fail(current, "The prepared Chamber of Will rite visit could not start.");
					}
					return new HemoJourneyResult(true, current,
							"Entered the Chamber of Will through the real rite path. Run journey harbinger next to return.");
				}
				ChamberVisitService.returnFromVisit(player);
			}
			if (current == HemoJourneyStage.FOUNDING_FANE
					&& HemoJourneyWorldState.accelerateFoundingFane(player, origin.above())) {
				return new HemoJourneyResult(true, current,
						"Founding Fane activation passed. The owned ceremony is completing now; run journey harbinger next once more.");
			}
			if (current == HemoJourneyStage.VEIN_MASON_D5_FORTIFICATION
					&& HemoJourneyWorldState.accelerateRite(player, origin.above(),
							Hemomancy.rloc("cardinal_rite/hematic_fortification"))) {
				return new HemoJourneyResult(true, current,
						"Hematic Fortification activation passed. The owned ceremony is completing now; run journey harbinger next once more.");
			}
			if (current == HemoJourneyStage.COVENANT_VIGIL
					&& HemoJourneyWorldState.accelerateCovenantVigil(player, origin.above())) {
				return new HemoJourneyResult(true, current,
						"Covenant Vigil activation and helper assignment passed. The owned ceremony is completing now; run journey harbinger next once more.");
			}
			if (current == HemoJourneyStage.APOTHEOS_RITE
					&& HemoJourneyWorldState.accelerateRite(player, origin.above(),
							Hemomancy.rloc("cardinal_rite/apotheos_rite"))) {
				return new HemoJourneyResult(true, current,
						"Rite of Apotheos activation passed. The owned ceremony is completing now; run journey harbinger next once more.");
			}
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
					? "All checkpoints passed. Run journey harbinger next once more to restore your snapshot."
					: action(next));
		} catch (RuntimeException exception) {
			HemoJourneyFixtures.cleanup(player, origin);
			player.getPersistentData().put(HemoJourneyFixtures.BASELINE_KEY, priorBaseline);
			player.getPersistentData().putString(HemoJourneySnapshot.STAGE_KEY,
					HemoJourneyTransition.next(current, true, false).id());
			return HemoJourneyResult.fail(current, "Next fixture failed: " + exceptionMessage(exception)
					+ ". The verified checkpoint is latched; run journey harbinger next to retry the transition.");
		}
	}

	public static HemoJourneyResult status(ServerPlayer player) {
		HemoJourneyResult active = requireActive(player);
		if (active != null) return active;
		HemoJourneyStage stage = currentStage(player);
		if (!HemoJourneyTransition.shouldVerify(stage,
				player.getPersistentData().getString(VERIFIED_STAGE_KEY))) {
			return new HemoJourneyResult(true, stage,
					"Checkpoint verified and latched. Run journey harbinger next to retry the pending transition.");
		}
		HemoJourneyResult check = HemoJourneyChecks.verify(player, stage, origin(player));
		return new HemoJourneyResult(check.passed(), stage,
				action(stage) + (check.passed() ? " Conditions currently pass." : "\nUnmet:\n" + check.message()));
	}

	public static HemoJourneyResult reset(ServerPlayer player) {
		HemoJourneyResult active = requireActive(player);
		if (active != null) return active;
		BlockPos origin = origin(player);
		HemoJourneyWorldState.leaveTemporaryChamber(player);
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
			HemoJourneyWorldState.leaveTemporaryChamber(player);
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
		if (!JourneyRoute.is(player, JourneyRoute.HARBINGER)) {
			return HemoJourneyResult.fail(currentStage(player), "The active journey belongs to the Unstained route.");
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
			case MORTAL_DISPLAY -> "Right-click the Mortal Display, then run journey harbinger next.";
			case SANGUINE_INITIATION -> "Aim at the center Hematic Iron block and invoke the blood-crafting key. After initiation, Blood Projection is selected from the lower Charm radial and normally conjured with the use-manipulation key.";
			case FIRST_REMNANT_DISCOVERED -> "Right-click the prepared First Remnant blood echo and read it, then run journey harbinger next.";
			case VICAR_HERMIT_ROAD_REPORT -> "Speak to the marked Vicar to receive the Assignment Ledger, then report the First Remnant and run journey harbinger next.";
			case VESSEL_FILLED -> "Use the supplied Bloody Jug to fill the vessel to 5,000 mL.";
			case FORMATION_PROJECTED -> "The fixture supplied the legitimate Blood Projection hand tool. Normally select the lower Blood Projection utility in the Charm radial and conjure it with the use-manipulation key. Hold it on the Venous Stone until one Sanguine Formation appears, then release. The checkpoint accepts the formation either in your inventory or on the fixture.";
			case LIBER_CRAFTED -> "The fixture supplied Blood Projection and one Sanguine Formation in your offhand. Hold projection on the center Bookshelf, then pick up the crafted Liber Sanguinum before running journey harbinger next.";
			case HEMATIC_IRON_CRAFTED -> "The fixture supplied Blood Projection and one Ink Sac in your offhand. Hold projection on the center Iron Block, then pick up the crafted Hematic Iron Block before running journey harbinger next.";
			case LIVING_STAFF_CRAFTED -> "Hold the supplied Blood Projection on the center Iron Bars until the Living Staff structure completes, then pick up the Staff and run journey harbinger next.";
			case VICAR_REWARD -> "Speak to the marked Vicar and claim the First Bloodcraft completion kit. Keep inventory rewards and leave any overflow drops beside the Vicar until journey harbinger next.";
			case VOTARY_RITE -> "Invoke the Rite of the Votary at its center Hematic Iron block, then run journey harbinger next.";
			case DEGREE_2_REACHED -> "You are now a Votary. Run journey harbinger next to meet the Alchemist.";
			case ALCHEMIST_BRIEFING -> "Speak to the marked Alchemist and accept The First Separation.";
			case CENTRIFUGE_PREPARED -> "Place the supplied Glass Bottle and Copper Ingot into the two Iron Braziers, light both with Blood Projection, then project the centrifuge structure with the Ferric Binder. Pick up and place the crafted Vial Centrifuge at the fixture center, then run journey harbinger next.";
			case SEPARATION_STARTED -> "Use the two loose Blood Vials from the briefing on the two cows without damaging them. Put the sampled vials in opposite centrifuge slots, press Start, then wait for the spin to finish before running journey harbinger next.";
			case ENZYME_RECOVERED -> "Open the centrifuge, take the Vivacious Enzyme from its output, then run journey harbinger next.";
			case ALCHEMIST_REWARD -> "Speak to the marked Alchemist and claim the First Separation sampling kit.";
			case BODY_ANSWERS_BRIEFING -> "Speak to the marked Alchemist and accept The Body Answers, then run journey harbinger next.";
			case BODY_ANSWERS_TINCTURE -> "The supplied ingredients are already loaded into the heated Ghastly Alembic. Wait for the tincture, take it from the output slot, and drink it.";
			case RED_TAXONOMY -> "Four distinct Red Taxonomy samples are in your hotbar. Hold each one in turn, speak to the marked Alchemist, and submit it.";
			case LIVING_BESTIARY_RECORD -> "Use the empty Specimen Jar on the marked Crimson Doe, keep the filled jar in hand, then ask the marked Alchemist to record it without surrendering it.";
			case LIVING_BESTIARY_SURRENDER -> "Keep the recorded Crimson Doe jar in hand and ask the same Alchemist to surrender it for study.";
			case HYPHAE_DISCOVERED -> "Walk over the supplied Fungal Spine and pick it up to unlock the Hyphae Liber entry.";
			case ARTIFICER_WORN_VOW_BRIEFING -> "Speak to the marked Artificer and accept The Worn Vow.";
			case ARTIFICER_ARMATURE_PLACED -> "Place the supplied Hematic Armature at the fixture center, then run journey harbinger next.";
			case ARTIFICER_HEMATIC_UPGRADE -> "The Armature is loaded with one Hematic Iron Scrap and 250 mL. Step onto it and wait for the supplied Iron Boots to be upgraded.";
			case ARTIFICER_WORN_VOW_REWARD -> "Speak to the marked Artificer and claim the four Hematic Iron Scrap reward.";
			case ARTIFICER_WORN_VOW_FITTING -> "The complete Hematic Iron set is equipped. Speak to the marked Artificer and claim The Worn Vow fitting.";
			case ENZYME_MASTERY -> "All eight enzyme expressions are in your inventory. Wait for The Eightfold Centrifuge milestone, then run journey harbinger next.";
			case INITIATE_RITE -> "Invoke the prepared Rite of the Incarnadine Fane and wait until Degree 3 is awarded.";
			case FIRST_CULTURE -> "The Mycelial Lantern is loaded with a recorded culture and enough blood. Wait for it to fruit, then take the enzyme from its output.";
			case WOVEN_VESSEL_TURN_IN -> "Speak to the marked Mnemonist and turn in the supplied Hematic Memory, Book, Ink Sac, and three Paper for The Woven Vessel.";
			case FIRST_MEMORY_WOVEN -> "The Somatic Loom is loaded for Blood Shot. Hold Blood Projection on it until the strand appears, then use the Living Staff to draw the strand into the loom.";
			case NOETIC_MARK_RECOGNIZED -> "Speak to the marked Mnemonist once so the first weave is recognized as a Noetic conductive mark.";
			case ARTIFICER_THREE_ANSWERS_BRIEFING -> "Speak to the marked Artificer and accept The Three Answers.";
			case ARTIFICER_FORK_UPGRADE -> "Step onto the prepared Armature and wait for the Hematic Iron Boots to become Barbed Boots.";
			case ARTIFICER_THREE_ANSWERS_INSPECTION -> "Speak to the marked Artificer and show the first fork upgrade.";
			case ARTIFICER_THREE_ANSWERS_COUNSEL -> "Speak to the marked Alchemist and claim the corresponding armor reagent.";
			case ARTIFICER_BARBED_RESEARCH -> "Capture the Barbed Urchin, Desiccant, and Venom-Rib Centipede with the three supplied jars. Keep each filled jar in hand and ask the marked Alchemist to record it.";
			case ARTIFICER_BARBED_RESEARCH_REWARD -> "Speak to the marked Alchemist and claim the completed Barbed research reward.";
			case ARTIFICER_FORK_DEMONSTRATION -> "Wear the supplied Barbed set and let the marked attacker hit you once.";
			case ARTIFICER_FORK_FITTING -> "Speak to the marked Artificer and claim the Barbed fitting.";
			case ADEPT_RITE -> "Invoke the prepared Rite of the Sanguine Brotherhood and wait until Degree 4 is awarded.";
			case VEIN_MASON_LESSON -> "Speak to the marked Vein-Mason and accept the supplied first scar lesson.";
			case FIRST_SCAR_CARVED -> "Open the prepared Cerebral Scarring Station and press Carve once.";
			case FIRST_SCAR_LEARNED -> "Hold Blood Absorption on the lit brazier until the supplied scar burns into memory.";
			case FIRST_EFFIGY_PATTERN -> "Press the supplied Motif Paper onto the prepared Mason's Effigy, swap to Blood Projection, and hold it on the Effigy until the pattern appears.";
			case FIRST_EFFIGY_LOADOUT -> "Hold Blood Absorption on the lit brazier until the prepared pattern commits the learned scar loadout.";
			case VEIN_MASON_REWARD -> "Speak to the marked Vein-Mason and claim the continuation kit.";
			case ILLUMINATUS_RITE -> "Invoke the prepared Rite of the Crimson Lodge and wait until Degree 5 is awarded.";
			case VEIN_MASON_D5_STRAIN -> "Hit the marked target once. Sanguine Fists is enabled and the arm vessels are one real activation away from Varicose flow.";
			case VEIN_MASON_D5_DIAGNOSIS -> "Speak to the marked Vein-Mason and accept the vascular diagnosis.";
			case VEIN_MASON_D5_TREATMENT -> "Hold use on the supplied Vascular Poultice until treatment completes.";
			case VEIN_MASON_D5_FORTIFICATION -> "Invoke the prepared Rite of Hematic Fortification with the Living Staff. Once it starts, run journey harbinger next to fast-complete the owned ceremony, then run next once more.";
			case VEIN_MASON_D5_REWARD -> "Speak to the marked Vein-Mason and claim the degree-five continuation kit.";
			case ARTIFICER_ASSUMED_LIMB_BRIEFING -> "Speak to the marked Artificer and accept The Assumed Limb.";
			case ARTIFICER_FIRST_LIVING_GRAFT -> "Hold the supplied Living Staff on the lit Blade Graft brazier until the limb is received.";
			case ARTIFICER_ASSUMED_LIMB_REWARD -> "Speak to the marked Artificer and claim the Hematic Memory inspection reward.";
			case ARTIFICER_LIVING_ARSENAL_DEMONSTRATION -> "Kill the marked one-health target with the supplied Living Blade.";
			case ARTIFICER_FULL_LIVING_ARSENAL -> "Hold the Living Staff on each of the six lit graft braziers until every remaining Living Weapon form is learned.";
			case ARTIFICER_LIVING_ARSENAL_FITTING -> "Speak to the marked Artificer and claim The Assumed Limb fitting.";
			case ARTIFICER_CRIMSON_VESTMENT_BRIEFING -> "Speak to the marked Artificer and accept The Crimson Vestment.";
			case VICAR_CONSECRATION_KIT -> "Speak to the marked Vicar, claim the Armature Consecration Kit, and pick it up before running journey harbinger next.";
			case ARTIFICER_FRAME_CONSECRATED -> "Use the Vicar's Consecration Kit on the prepared Hematic Armature.";
			case ARTIFICER_CRIMSON_VESTMENT_INSPECTION -> "Speak to the marked Artificer and show the consecrated frame.";
			case ARTIFICER_CRIMSON_VESTMENT_COUNSEL -> "Speak to the marked Alchemist, claim the Crimson Lacquer, and pick it up before running journey harbinger next.";
			case ARTIFICER_BLOOD_LUST_UPGRADE -> "Step onto the prepared consecrated Armature and wait for the Barbed Boots to become Blood Lust Boots.";
			case ARTIFICER_BLOOD_LUST_DEMONSTRATION -> "Wear the supplied Blood Lust set and hit the marked target once.";
			case ARTIFICER_BLOOD_LUST_FITTING -> "Speak to the marked Artificer and claim The Crimson Vestment fitting.";
			case FOUNDING_FANE -> "Invoke the prepared Founding Fane at its Cardinal Focus. Once the rite starts, run journey harbinger next to fast-complete its owned ceremony, then run next once more after the Bloodwell manifests.";
			case SANCTIFIED_RITE -> "Invoke the prepared Rite of the Bloodline Covenant and wait until Degree 6 is awarded.";
			case VEIN_MASON_D6_REFERRAL -> "Speak to the marked Vein-Mason and accept the Degree-6 referral.";
			case VEIN_MASON_D6_COUNSEL -> "Speak to the marked Mnemonist and receive the scar-routing counsel.";
			case VEIN_MASON_D6_FIRST_ROUTE -> "Press the use-manipulation key once. The selected manipulation matches the active cerebral scar.";
			case VEIN_MASON_D6_SCAR_CARVED -> "Open the prepared Cerebral Scarring Station and press Carve once.";
			case VEIN_MASON_D6_SCAR_LEARNED -> "Hold Blood Absorption on the lit brazier until the newly carved scar burns into memory.";
			case VEIN_MASON_D6_LOADOUT -> "Hold Blood Absorption on the lit brazier until the supplied pattern replaces the active scar loadout.";
			case VEIN_MASON_D6_SECOND_ROUTE -> "Press the use-manipulation key once more. The selected manipulation now matches the newly committed scar.";
			case VEIN_MASON_D6_REWARD -> "Speak to the marked Vein-Mason and claim the Degree-6 routing reward.";
			case CHAMBER_RETURNED -> "Run journey harbinger next to enter the Chamber of Will through the Degree-6 rite path, then run it again to return.";
			case COVENANT_THRONE_BOUND -> "Right-click the prepared Covenant Throne once to bind your return and take the seat, then run journey harbinger next.";
			case COVENANT_VIGIL -> "Invoke the prepared Covenant Vigil at its Cardinal Focus. Once it starts, run journey harbinger next to assign the sworn helper and fast-complete it, then run next once more.";
			case ARCHON_RITE -> "Invoke the prepared Rite of the Hematic Order and wait until Degree 7 is awarded.";
			case ARTIFICER_WEIGHT_OF_FRAME_BRIEFING -> "Speak to the marked Artificer and accept Weight of the Frame.";
			case ARTIFICER_MONOLITHIC_FRAME -> "Use the supplied Monolithic Cornerstone on the prepared consecrated Hematic Armature.";
			case ARTIFICER_D7_UPGRADE -> "Step onto the prepared monolithic Armature and wait for the Blood Lust Boots to become Edacious Blood Lust Boots.";
			case ARTIFICER_WEIGHT_OF_FRAME_INSPECTION -> "Speak to the marked Artificer, show the first Edacious piece, and collect the lineage material.";
			case ARTIFICER_D7_DEMONSTRATION -> "Wear the supplied Edacious set and press the armor-ability key once to activate Bloodburst.";
			case ARTIFICER_D7_FITTING -> "Speak to the marked Artificer and claim the Monolithic Frame fitting.";
			case QLIPHOTH_COMMUNION -> "Eat each of the nine supplied Qliphoth pomes, switching through hotbar slots 1-9 as each husk is consumed.";
			case APOTHEOS_CHOICE -> "In the opened fungal revelation, choose to pursue the Eighth Degree.";
			case APOTHEOS_RITE -> "Invoke the prepared Rite of Apotheos. Once it starts, run journey harbinger next to fast-complete the owned ceremony, then run next once more.";
			case COMPLETE -> "Run journey harbinger next to restore the pre-journey snapshot.";
		};
	}

	private static BlockPos target(HemoJourneyStage stage, BlockPos origin) {
		return switch (stage) {
			case MORTAL_DISPLAY, FIRST_REMNANT_DISCOVERED, FORMATION_PROJECTED -> origin.above();
			case SANGUINE_INITIATION -> origin.above();
			case LIBER_CRAFTED, HEMATIC_IRON_CRAFTED -> origin.above();
			case LIVING_STAFF_CRAFTED -> origin.above(2);
			case VESSEL_FILLED -> origin.above();
			case VICAR_HERMIT_ROAD_REPORT, VICAR_REWARD -> origin.above();
			case VOTARY_RITE -> origin.above();
			case DEGREE_2_REACHED, ALCHEMIST_BRIEFING, ALCHEMIST_REWARD, BODY_ANSWERS_BRIEFING,
					WOVEN_VESSEL_TURN_IN, NOETIC_MARK_RECOGNIZED,
					VEIN_MASON_LESSON, VEIN_MASON_REWARD -> origin.above();
			case VEIN_MASON_D5_STRAIN, VEIN_MASON_D5_DIAGNOSIS, VEIN_MASON_D5_TREATMENT,
					VEIN_MASON_D5_REWARD -> origin.above();
			case BODY_ANSWERS_TINCTURE -> origin.above();
			case RED_TAXONOMY -> origin.above();
			case LIVING_BESTIARY_RECORD, LIVING_BESTIARY_SURRENDER -> origin.above();
			case HYPHAE_DISCOVERED -> origin;
			case ARTIFICER_ASSUMED_LIMB_BRIEFING, ARTIFICER_FIRST_LIVING_GRAFT,
					ARTIFICER_ASSUMED_LIMB_REWARD, ARTIFICER_LIVING_ARSENAL_DEMONSTRATION,
					ARTIFICER_FULL_LIVING_ARSENAL, ARTIFICER_LIVING_ARSENAL_FITTING -> origin.above();
			case ARTIFICER_WORN_VOW_BRIEFING, ARTIFICER_ARMATURE_PLACED,
					ARTIFICER_HEMATIC_UPGRADE, ARTIFICER_WORN_VOW_REWARD,
					ARTIFICER_WORN_VOW_FITTING, ARTIFICER_THREE_ANSWERS_BRIEFING,
					ARTIFICER_FORK_UPGRADE, ARTIFICER_THREE_ANSWERS_INSPECTION,
					ARTIFICER_THREE_ANSWERS_COUNSEL, ARTIFICER_FORK_DEMONSTRATION,
					ARTIFICER_BARBED_RESEARCH, ARTIFICER_BARBED_RESEARCH_REWARD,
					ARTIFICER_FORK_FITTING, ARTIFICER_CRIMSON_VESTMENT_BRIEFING,
					VICAR_CONSECRATION_KIT, ARTIFICER_FRAME_CONSECRATED,
					ARTIFICER_CRIMSON_VESTMENT_INSPECTION, ARTIFICER_CRIMSON_VESTMENT_COUNSEL,
					ARTIFICER_BLOOD_LUST_UPGRADE, ARTIFICER_BLOOD_LUST_DEMONSTRATION,
					ARTIFICER_BLOOD_LUST_FITTING, ARTIFICER_WEIGHT_OF_FRAME_BRIEFING,
					ARTIFICER_MONOLITHIC_FRAME, ARTIFICER_D7_UPGRADE,
					ARTIFICER_WEIGHT_OF_FRAME_INSPECTION, ARTIFICER_D7_DEMONSTRATION,
					ARTIFICER_D7_FITTING -> origin.above();
			case ENZYME_MASTERY -> origin;
			case FIRST_CULTURE -> origin.above();
			case CENTRIFUGE_PREPARED, SEPARATION_STARTED, ENZYME_RECOVERED -> origin.above(2);
			case FIRST_MEMORY_WOVEN, FIRST_SCAR_CARVED, FIRST_SCAR_LEARNED,
					FIRST_EFFIGY_PATTERN, FIRST_EFFIGY_LOADOUT -> origin.above();
			case FOUNDING_FANE -> origin.above();
			case VEIN_MASON_D5_FORTIFICATION -> origin.above();
			case VEIN_MASON_D6_REFERRAL, VEIN_MASON_D6_COUNSEL, VEIN_MASON_D6_FIRST_ROUTE,
					VEIN_MASON_D6_SCAR_CARVED, VEIN_MASON_D6_SCAR_LEARNED, VEIN_MASON_D6_LOADOUT,
					VEIN_MASON_D6_SECOND_ROUTE, VEIN_MASON_D6_REWARD -> origin.above();
			case CHAMBER_RETURNED -> origin;
			case COVENANT_THRONE_BOUND, COVENANT_VIGIL -> origin.above();
			case INITIATE_RITE, ADEPT_RITE, ILLUMINATUS_RITE, SANCTIFIED_RITE, ARCHON_RITE, APOTHEOS_RITE -> origin.above(3);
			case QLIPHOTH_COMMUNION, APOTHEOS_CHOICE -> origin;
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
		player.getPersistentData().remove(JourneyRoute.KEY);
	}

	private static String exceptionMessage(RuntimeException exception) {
		return exception.getMessage() == null ? exception.getClass().getSimpleName() : exception.getMessage();
	}
}
