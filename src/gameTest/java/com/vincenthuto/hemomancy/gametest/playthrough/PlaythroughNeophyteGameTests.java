package com.vincenthuto.hemomancy.gametest.playthrough;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopicState;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerVicarDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.IDispellable;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserRules;
import com.vincenthuto.hemomancy.common.tile.inscription.DiscoveryInscriptionBlockEntity;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import com.vincenthuto.hemomancy.gametest.PlaythroughFingerprints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;

/**
 * Band d1_neophyte: a Harbinger Neophyte from the end of the Vicar's ceremony to the First Bloodcraft claim and
 * Degree 2. The player reads the ledger, asks the Vicar for blueprints, conjures the two cell hands, earns the four
 * proofs by playing them (500 mL absorbed, a Sanguine Formation condensed in a cauldron, a Venous Stone projected, a
 * blood-structure craft), and claims through the acknowledged dialogue path. The edge cases are the ones a server
 * sees: premature, repeated and out-of-reach claims, proofs earned at D0, a death mid-band, a renamed ledger, a
 * bystander collecting a formation or picking up the kit, a logout during the stone's collapse, and the Hermit Road
 * side report before and after D2.
 * <p>
 * Everything under test goes through the entry point a client triggers: {@code interact} on the NPC, the dialogue
 * payload handlers, the cast and cycle keys' payloads, a held channel ({@code hold}), right-clicks on blocks
 * ({@code useOn}), block placement, item pickup, real damage and respawn. Preconditions that are not under test are
 * marked {@code // ARRANGE}: blood topped up before a conjuration, a reservoir pre-filled, proofs recorded through
 * their recorders where a test is about something else.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughNeophyteGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_d1";
	/** The long channels (the cow herd and the whole chain) run apart, so a slow one does not hold up the batch. */
	private static final String SLOW = "playthrough_d1_slow";

	// Events the Vicar offers at D1
	private static final String CLAIM = HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD;
	private static final String LIBER_BLUEPRINT = HarbingerVicarDialogueTrees.EVENT_LIBER_BLUEPRINT;
	private static final String IRON_BLUEPRINT = HarbingerVicarDialogueTrees.EVENT_IRON_BLUEPRINT;
	private static final String REPORT = HarbingerVicarDialogueTrees.EVENT_HERMIT_ROAD_REPORT;
	private static final String BEGIN = "vicar_begin_initiation";

	// Keys the player is shown or told
	private static final String ASK_HERMIT_ROAD = "hemomancy.dialogue.vicar.option.ask_about_hermit_road";
	private static final String LEDGER_GRANTED = "hemomancy.dialogue.event.vicar_assignment_ledger_granted";
	private static final String CLAIM_UNREADY = "hemomancy.dialogue.event.vicar_first_bloodcraft_reward_unready";
	private static final String CLAIM_KNOWN = "hemomancy.dialogue.event.vicar_first_bloodcraft_reward_known";
	private static final String CLAIM_GRANTED = "hemomancy.dialogue.event.vicar_first_bloodcraft_reward_granted";
	private static final String ROAD_REPORTED = "hemomancy.dialogue.event.vicar_hermit_road_reported";
	private static final String ROAD_KNOWN = "hemomancy.dialogue.event.vicar_hermit_road_known";
	private static final String ROAD_UNPROVEN = "hemomancy.dialogue.event.vicar_hermit_road_unproven";
	private static final String EMPTY_HAND = "message.hemomancy.use_manip_key_packet.conjuration_requires_an_empty_hand";
	private static final String NO_BLOOD_TO_SHED = "message.hemomancy.blood_manipulation.not_enough_blood_to_be_shed";

	/** The F-11 lines: the Hermit Road report gives ash and a record, never a ledger (HEMOMANCY_REFERENCE:227). */
	private static final List<String> HERMIT_ROAD_LEDGER_LINES = List.of(
			"hemomancy.vicar.hermit_road.report.line2",
			"hemomancy.vicar.hermit_road.report.prompt",
			"hemomancy.vicar.hermit_road.intro.line2",
			"screen.hemomancy.harbinger_assignment_ledger.step.ledger.desc",
			"advancements.hemomancy.hermit_road_ledger_granted.description");
	private static final Pattern LEDGER_PROMISE =
			Pattern.compile("(?i)\\b(give|given|gives|giving|receive|receives|take this|hand you)\\b[^.!?]*\\bledger\\b");

	private static final String ADV_D2 = HarbingerAdvancementGranter.ADV_DEGREE_2_VOTARY.getPath();
	private static final String ADV_CLAIMED = FirstBloodcraftAssignment.ADV_REWARD_CLAIMED.getPath();
	private static final String ADV_REMNANT = HarbingerAdvancementGranter.ADV_HERMIT_ROAD_FIRST_REMNANT.getPath();
	private static final String ADV_REPORTED = HarbingerAdvancementGranter.ADV_HERMIT_ROAD_REPORTED.getPath();

	private static final double PROOF_ML = 500.0D;
	/** ARRANGE: what a test tops the player up to before a conjuration; casting is not what it is about. */
	private static final double CONJURE_BLOOD = 2000.0D;
	/** One bare cauldron formation's worth, so a spent cauldron (collected or collapsed) goes back to iron. */
	private static final double FORMATION_FILL = SanguineCondenserRules.formationCost(0);
	private static final int MAX_COLLAPSES = 6;
	private static final int COW_CAP_TICKS = 6000;
	private static final int HERD = 3;

	private static Map<String, String> english;

	private PlaythroughNeophyteGameTests() {
	}

	// --- The chain from the previous band --------------------------------------------------------------------------

	/**
	 * The D0 Main steps played for real (the Hermit's blessing, the Display touch, the Vicar's ceremony ticked through),
	 * then a D1 talk with the Vicar. The player must end where {@link PlaythroughStates#neophyteD1} puts a fixture
	 * player: degree, blood, charm, advancements, items, known and equipped utilities, and First Bloodcraft progress.
	 */
	@GameTest(template = EMPTY, timeoutTicks = EarlyInitiation.DURATION + 200, batch = BATCH)
	public static void neophyteChainFromPreviousBand(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 6));
		PlaythroughClock clock = PlaythroughClock.start(helper);
		chain(helper, player, clock);
		clock.run();
	}

	/**
	 * The D0 to D1 chain as steps on {@code clock}, for {@link #neophyteChainFromPreviousBand} and the full run: sets
	 * up a temple and a Vicar on the template floor now, then plays the blessing, the touch and the ceremony, and ends
	 * comparing the player with {@link PlaythroughStates#neophyteD1}. {@code player} must be a fresh D0 player.
	 */
	public static void chain(GameTestHelper helper, PlaythroughPlayer player, PlaythroughClock clock) {
		PlaythroughSites.BloodTemple temple = PlaythroughSites.bloodTemple(helper, PlaythroughSites.stand(helper, 6, 6));
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 20, 12));
		player.moveTo(temple.stand());
		PlaythroughPlayer arranged = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 28, 20));
		clock
				.then(() -> {
					DialogueView hermit = player.talkTo(temple.hermit());
					helper.assertTrue(hermit.offers("hermit_heart_offered"), "the Hermit offers his heart: " + hermit.describe());
					player.choose(temple.hermit(), "hermit_heart_offered");
					emptyHand(player);
					Outcome touch = player.useOn(temple.display());
					helper.assertTrue(player.hasCharm() && player.bloodActive(), "the Display fits the charm; " + touch);
					player.approach(vicar);
					DialogueView offer = player.talkTo(vicar);
					helper.assertTrue(offer.offers(BEGIN), "the charm-bearer is offered initiation: " + offer.describe());
					Outcome begun = player.choose(vicar, BEGIN);
					helper.assertTrue(begun.rewardDelivered(), "the ceremony begins; " + begun);
				})
				.step(player.tickPlayer(EarlyInitiation.DURATION + 2))
				.then(() -> {
					helper.assertTrue(player.degree() == 1, "the ceremony ends at degree 1; " + player.last());
					Talk d1 = talk(player, vicar);
					d1.outcome().didNotSay(LEDGER_GRANTED);
					helper.assertTrue(d1.outcome().delta(ItemInit.harbinger_assignment_ledger.get()) == 0,
							"the D1 Vicar mints no second ledger for a player who carries the ceremony's; " + d1.outcome());
					PlaythroughStates.neophyteD1(arranged); // ARRANGE: the band's fixture end state, for comparison
					List<String> drift = stateDifferences(player, arranged);
					PlaythroughLog.note(helper, "played D1: " + stateSummary(player));
					PlaythroughLog.note(helper, "arranged D1: " + stateSummary(arranged));
					helper.assertTrue(drift.isEmpty(), "PlaythroughStates.neophyteD1 drifts from play: " + drift);
				});
	}

	// --- Review follow-ups -----------------------------------------------------------------------------------------

	/**
	 * The Vicar's First Bloodcraft reward card tells the truth about its state: before the four proofs it is not a
	 * turn-in (nothing can be handed in), and after the claim it is not a turn-in either.
	 */
	// FIX: F-12 the hub filed any option whose ids said "reward" as TURN_IN; only an actionable claim is one now.
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void vicarUnreadyRewardIsNotTurnIn(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE: no proofs yet
		DialogueView unready = talk(player, vicar).view();
		var unreadyCard = unready.topics(DialogueCategory.QUESTS).stream()
				.filter(topic -> topic.titleKey().equals("hemomancy.dialogue.vicar.option.first_bloodcraft_reward_unready"))
				.findFirst();
		PlaythroughLog.note(helper, "unready reward card: " + unreadyCard.map(t -> t.state() + "/" + t.attention()).orElse("absent")
				+ "; " + unready.describe());
		helper.assertTrue(unreadyCard.isPresent(), "the not-ready reward card is shown at D1: " + unready.describe());
		helper.assertTrue(unreadyCard.get().state() != DialogueTopicState.TURN_IN,
				"a reward that is not ready is not shown as a turn-in: " + unreadyCard.get().state());
		PlaythroughStates.votaryD2(player); // ARRANGE: the proofs in and the claim made
		DialogueView claimed = talk(player, vicar).view();
		List<String> turnIns = claimed.topics(DialogueCategory.QUESTS).stream()
				.filter(topic -> topic.titleKey().contains("first_bloodcraft_reward"))
				.filter(topic -> topic.state() == DialogueTopicState.TURN_IN).map(DialogueTopic::titleKey).toList();
		helper.assertTrue(turnIns.isEmpty(), "a claimed reward is not shown as a turn-in: " + turnIns);
		helper.succeed();
	}

	/**
	 * Dispelling a conjured cell hand gives blood back, and the refund reaches the client in the same tick, through
	 * the blood-flow ledger. The refund amount is logged (whether it should scale with wear is decision D-25).
	 */
	// FIX: UseManipKeyPacket filled the volume directly with no owner sync; it now goes through BloodFlowLedger.
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void dispelRefundIsSyncedAndRecorded(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		PlaythroughStates.blood(player, CONJURE_BLOOD); // ARRANGE
		emptyHand(player);
		player.select(ManipulationInit.blood_absorption.get().getName());
		Outcome conjure = player.castSelected();
		helper.assertTrue(player.player().getMainHandItem().is(ItemInit.blood_absorption.get()),
				"the cast conjures the hand; " + conjure);
		Outcome dispel = player.castSelected();
		PlaythroughLog.note(helper, "dispel refund: " + (dispel.bloodAfter() - dispel.bloodBefore()) + " mL after a conjure that cost "
				+ conjure.bloodSpent() + " mL; " + dispel.describe());
		helper.assertTrue(player.player().getMainHandItem().isEmpty(), "the second cast dispels the hand; " + dispel);
		helper.assertTrue(dispel.bloodAfter() > dispel.bloodBefore(), "dispelling gives blood back; " + dispel);
		helper.assertFalse(dispel.sent(BloodVolumeServerPacket.class).isEmpty(),
				"the refund is sent to the client in the same tick; " + dispel);
		helper.succeed();
	}

	// --- The D1 Vicar ----------------------------------------------------------------------------------------------

	/**
	 * At D1 the Vicar's hub carries the absorption, projection and crafting hints, both blueprints, the not-ready
	 * claim, the Hermit Road question and the tithe card; every key resolves and nothing is a dead end. A Neophyte who
	 * already carries a ledger is not given another.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void neophyteVicarOffersEveryD1Topic(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		Talk talk = talk(player, vicar);
		DialogueView view = talk.view();
		PlaythroughLog.note(helper, "D1 Vicar: " + view.describe());
		view.assertLangKeys(helper, "the D1 Vicar").assertNoDeadEnds(helper, "the D1 Vicar");
		for (String node : List.of("absorption_hint", "projection_hint", "crafting_hint",
				"first_bloodcraft_reward_unready", "hermit_road_intro")) {
			helper.assertTrue(view.hasNode(node), "the D1 Vicar reaches " + node + ": " + view.describe());
		}
		helper.assertTrue(view.offers(LIBER_BLUEPRINT) && view.offers(IRON_BLUEPRINT),
				"both blueprints are offered: " + view.reachableEvents());
		helper.assertTrue(!view.shownEvents().contains(CLAIM),
				"no claim is shown before the proofs: " + view.shownEvents());
		helper.assertTrue(view.optionByText(ASK_HERMIT_ROAD).isPresent(),
				"the Hermit Road question is asked before any remnant: " + view.describe());
		List<String> trades = view.tradeCards().stream().map(card -> card.offerId().toString()).toList();
		helper.assertTrue(trades.stream().anyMatch(id -> id.endsWith("tithe")), "the tithe card is on the board: " + trades);
		talk.outcome().didNotSay(LEDGER_GRANTED);
		helper.assertTrue(talk.outcome().delta(ItemInit.harbinger_assignment_ledger.get()) == 0
						&& player.count(ItemInit.harbinger_assignment_ledger.get()) == 1,
				"a carried ledger is not reissued; " + talk.outcome());
		helper.succeed();
	}

	/** The ceremony's ledger opens at degree 1 with no proof and the Hermit Road card at 0/2. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void ledgerOpensAtZero(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		OpenHarbingerAssignmentLedgerPacket page = readLedger(player);
		FirstBloodcraftLedgerProgress first = page.firstBloodcraft();
		PlaythroughLog.note(helper, "fresh D1 ledger: degree " + page.degree() + ", " + first + ", Hermit Road "
				+ hermitRoad(page) + "/2");
		helper.assertTrue(page.degree() == 1 && page.degreeOne(), "the ledger shows degree 1: " + page.degree());
		helper.assertTrue(first.completedProofs() == 0 && first.absorbedMl() == 0.0D && !first.readyForVicar(),
				"the ledger shows no First Bloodcraft proof yet: " + first);
		helper.assertTrue(hermitRoad(page) == 0, "the Hermit Road card reads 0/2: " + hermitRoad(page));
		helper.succeed();
	}

	/**
	 * Each blueprint drops one Mnemonic Blueprint at the Vicar's feet, aimed at its structure; the player picks it up.
	 * Asking again while it is carried drops nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void blueprintsDropOncePerTarget(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		Item blueprint = ItemInit.mnemonic_blueprint.get();
		Map<String, String> asks = Map.of(LIBER_BLUEPRINT, "liber_sanguinum", IRON_BLUEPRINT, "hematic_iron_block");
		for (String event : List.of(LIBER_BLUEPRINT, IRON_BLUEPRINT)) {
			MnemonicBlueprintTarget target = new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE,
					Hemomancy.rloc("blood_structure/" + asks.get(event)));
			player.approach(vicar);
			player.talkTo(vicar);
			Outcome ask = player.choose(vicar, event);
			ask.dropped(blueprint, 1, Outcome.Near.NPC);
			helper.assertTrue(pickUp(player, ask, blueprint) == 1, "the dropped blueprint can be picked up; " + player.last());
			helper.assertTrue(carriedBlueprints(player).contains(target),
					event + " gives a blueprint for " + target.recipeId() + ": " + carriedBlueprints(player));
			player.talkTo(vicar);
			Outcome again = player.choose(vicar, event);
			again.gaveNothing();
			PlaythroughLog.note(helper, event + " first: " + ask.describe() + " | again: " + again.describe());
		}
		helper.assertTrue(player.count(blueprint) == 2, "one blueprint per target: " + carriedBlueprints(player));
		helper.succeed();
	}

	// --- Blood in and out ------------------------------------------------------------------------------------------

	/** Drinking a starter flask fills the vessel; it is not absorption, so the First Bloodcraft count stays at 0. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void flaskBloodIsNotAbsorption(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughClock.start(helper)
				.then(() -> {
					PlaythroughStates.neophyteD1(player); // ARRANGE
					PlaythroughStates.blood(player, 0.0D); // ARRANGE: an empty vessel, so the drink shows
				})
				.step(() -> player.hold(ItemInit.bloody_flask.get(), 40, ahead(player)))
				.then(() -> {
					Outcome drink = player.last();
					PlaythroughLog.note(helper, "flask: " + drink.describe());
					helper.assertTrue(drink.bloodAfter() > drink.bloodBefore() && drink.delta(ItemInit.bloody_flask.get()) == -1,
							"the flask is drunk and fills the vessel; " + drink);
					helper.assertTrue(progress(player).absorbedMl() == 0.0D,
							"flask blood is not absorption: " + progress(player));
				})
				.run();
	}

	/**
	 * The cast key with an item in hand is refused with the empty-hand line; with no blood it is refused for want of
	 * blood; after a flask it conjures Blood Absorption into the hand and pays for it.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void conjureAbsorptionNeedsEmptyHandAndBlood(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		Item absorption = ItemInit.blood_absorption.get();
		player.select(ManipulationInit.blood_absorption.get().getName());

		player.give(new ItemStack(Items.STICK)); // ARRANGE: something to hold
		player.wield(Items.STICK);
		Outcome busyHand = player.castSelected();
		busyHand.said(EMPTY_HAND);
		helper.assertTrue(player.player().getMainHandItem().is(Items.STICK) && busyHand.bloodSpent() == 0.0D,
				"a full hand conjures nothing and costs nothing; " + busyHand);

		emptyHand(player);
		PlaythroughStates.blood(player, 0.0D); // ARRANGE: a drained vessel
		Outcome dry = player.castSelected();
		dry.said(NO_BLOOD_TO_SHED);
		helper.assertTrue(player.player().getMainHandItem().isEmpty(), "no blood conjures nothing; " + dry);

		player.wield(ItemInit.bloody_flask.get());
		Outcome drink = player.use(InteractionHand.MAIN_HAND);
		helper.assertTrue(drink.bloodAfter() > 0.0D, "the flask fills the vessel; " + drink);
		emptyHand(player);
		Outcome cast = player.castSelected();
		PlaythroughLog.note(helper, "conjure after a flask: " + cast.describe());
		helper.assertTrue(player.player().getMainHandItem().is(absorption),
				"with blood and an empty hand the cast conjures Blood Absorption; " + cast);
		helper.assertTrue(cast.bloodSpent() > 0.0D, "the conjuration is paid for; " + cast);
		helper.succeed();
	}

	/** Holding Blood Absorption on a filled reservoir earns the 500 mL proof, and the count stops at 500. */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH)
	public static void reservoirAbsorptionReachesFiveHundred(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		BlockPos reservoir = reservoir(helper, PlaythroughSites.stand(helper, 12, 8), 2000.0D);
		int[] started = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> PlaythroughStates.neophyteD1(player)) // ARRANGE
				.step(conjure(player, ManipulationInit.blood_absorption, ItemInit.blood_absorption.get()))
				.then(() -> {
					PlaythroughStates.blood(player, 100.0D); // ARRANGE: room to take the reservoir's blood
					started[0] = (int) helper.getTick();
				})
				.step(absorbFrom(player, reservoir, 200, () -> progress(player).absorbedMl() >= PROOF_ML))
				.then(() -> {
					Outcome hold = player.last();
					PlaythroughLog.note(helper, "reservoir absorption reached " + progress(player).absorbedMl() + " mL in "
							+ (helper.getTick() - started[0]) + " ticks; " + hold.describe());
					helper.assertTrue(progress(player).absorbedMl() >= PROOF_ML, "a reservoir pays the 500 mL proof; " + hold);
				})
				.step(absorbFrom(player, reservoir, 10, () -> false))
				.then(() -> helper.assertTrue(progress(player).absorbedMl() == PROOF_ML,
						"the absorption count caps at 500: " + progress(player)))
				.run();
	}

	/**
	 * D-1: a Neophyte without a reservoir absorbs from living cows. The proof should be reachable within the cap; the
	 * cows killed and the ticks spent are logged either way.
	 */
	// DECISION: D-1 living-creature absorption is 1 mL per 10-tick pulse (about 50 cows for the 500 mL proof).
	@GameTest(template = EMPTY, timeoutTicks = COW_CAP_TICKS + 400, batch = SLOW, required = false)
	public static void livingCowAbsorptionRate(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 12));
		List<Cow> herd = new ArrayList<>();
		int[] cows = new int[1];
		int[] started = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> PlaythroughStates.neophyteD1(player)) // ARRANGE
				.step(conjure(player, ManipulationInit.blood_absorption, ItemInit.blood_absorption.get()))
				.then(() -> {
					PlaythroughStates.blood(player, 0.0D); // ARRANGE: the room to take everything
					started[0] = (int) helper.getTick();
				})
				.step(cowAbsorption(player, COW_CAP_TICKS, herd, cows))
				.then(() -> {
					double absorbed = progress(player).absorbedMl();
					int ticks = (int) helper.getTick() - started[0];
					PlaythroughLog.note(helper, "D-1 living absorption: " + absorbed + " mL from " + cows[0]
							+ " cows in " + ticks + " ticks (cap " + COW_CAP_TICKS + "); vessel " + player.blood() + " mL");
					helper.assertTrue(absorbed >= PROOF_ML, "living cows pay the 500 mL proof within " + COW_CAP_TICKS
							+ " ticks; got " + absorbed + " mL from " + cows[0] + " cows");
				})
				.run();
	}

	/** D-2: a full vessel absorbing from a reservoir. The proof should count it; the count observed is logged. */
	// DECISION: D-2 absorption counts only the room below max, while the hint says it counts everything.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void fullVesselAbsorptionCounts(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		BlockPos reservoir = reservoir(helper, PlaythroughSites.stand(helper, 12, 8), 2000.0D);
		PlaythroughClock.start(helper)
				.then(() -> PlaythroughStates.neophyteD1(player)) // ARRANGE
				.step(conjure(player, ManipulationInit.blood_absorption, ItemInit.blood_absorption.get()))
				.then(() -> PlaythroughStates.blood(player, Double.MAX_VALUE)) // ARRANGE: a full vessel
				.step(absorbFrom(player, reservoir, 40, () -> false))
				.then(() -> {
					double absorbed = progress(player).absorbedMl();
					PlaythroughLog.note(helper, "D-2 full vessel: counted " + absorbed + " mL; " + player.last().describe());
					helper.assertTrue(absorbed > 0.0D, "a full vessel's absorption is counted toward the proof; got " + absorbed);
				})
				.run();
	}

	// --- Projection ------------------------------------------------------------------------------------------------

	/**
	 * Holding Blood Projection on plain stone feeds it until it collapses into a Venous Stone; the player picks the
	 * stone up and the ledger shows the proof. Cobblestone takes no blood and gives nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 500, batch = BATCH)
	public static void venousStoneProjection(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		BlockPos stone = PlaythroughSites.stand(helper, 12, 8);
		BlockPos cobble = PlaythroughSites.stand(helper, 12, 14);
		PlaythroughClock.start(helper)
				.then(() -> {
					PlaythroughStates.neophyteD1(player); // ARRANGE
					player.give(new ItemStack(Items.STONE)).give(new ItemStack(Items.COBBLESTONE)); // ARRANGE
					player.place(stone, Items.STONE);
					player.place(cobble, Items.COBBLESTONE);
				})
				.step(conjure(player, ManipulationInit.blood_projection, ItemInit.blood_projection.get()))
				.then(() -> topUp(player, PROOF_ML)) // ARRANGE
				.step(projectInto(player, stone, 200, () -> air(helper, stone)))
				.then(() -> {
					Outcome feed = player.last();
					PlaythroughLog.note(helper, "venous stone feed: " + feed.describe());
					helper.assertTrue(air(helper, stone), "the projected stone collapses; " + feed);
					player.pickUpNear(stone, BlockInit.venous_stone.get()).gave(BlockInit.venous_stone.get(), 1);
					helper.assertTrue(progress(player).venousStoneProjected(), "the Venous Stone proof is recorded: "
							+ progress(player));
					player.moveTo(PlaythroughSites.stand(helper, 10, 14));
				})
				.step(projectInto(player, cobble, 40, () -> false))
				.then(() -> {
					Outcome cobbled = player.last();
					PlaythroughLog.note(helper, "cobblestone projection: " + cobbled.describe());
					helper.assertTrue(helper.getLevel().getBlockState(cobble).is(Blocks.COBBLESTONE)
									&& cobbled.drops().isEmpty() && cobbled.bloodSpent() <= 1.0E-6D,
							"cobblestone takes no blood and gives nothing; " + cobbled);
					dispel(player);
					FirstBloodcraftLedgerProgress shown = readLedger(player).firstBloodcraft();
					PlaythroughLog.note(helper, "ledger after the stone: " + shown + " (" + shown.completedProofs() + "/4)");
					helper.assertTrue(shown.venousStoneProjected() && !shown.formationProjected()
									&& shown.completedProofs() == 1,
							"the ledger shows the Venous Stone proof alone: " + shown);
				})
				.run();
	}

	/**
	 * Projecting into an empty cauldron makes a Sanguine Cauldron; it condenses a formation (projecting again after a
	 * collapse), the player dispels the cell hand and gathers it with an empty hand. The formation proof is recorded and
	 * the spent cauldron is iron again.
	 */
	@GameTest(template = EMPTY, timeoutTicks = (MAX_COLLAPSES + 2) * 300, batch = BATCH)
	public static void cauldronFormationByContributor(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		BlockPos cauldron = cauldron(helper, PlaythroughSites.stand(helper, 12, 8));
		int[] collapses = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> PlaythroughStates.neophyteD1(player)) // ARRANGE
				.step(conjure(player, ManipulationInit.blood_projection, ItemInit.blood_projection.get()))
				.step(condenseOne(player, cauldron, collapses))
				.then(() -> {
					dispel(player);
					emptyHand(player);
					Outcome gather = player.useOn(cauldron);
					PlaythroughLog.note(helper, "formation gathered after " + collapses[0] + " collapses: " + gather.describe());
					gather.gave(ItemInit.sanguine_formation.get(), 1);
					helper.assertTrue(progress(player).formationProjected(), "the contributor's formation proof is recorded: "
							+ progress(player));
					helper.assertTrue(helper.getLevel().getBlockState(cauldron).is(Blocks.CAULDRON),
							"the spent cauldron is plain iron again: " + helper.getLevel().getBlockState(cauldron));
				})
				.run();
	}

	/**
	 * A second Neophyte gathers the first one's formation. The bystander gets the item and no proof; the contributor
	 * loses the formation and still has no proof.
	 */
	@GameTest(template = EMPTY, timeoutTicks = (MAX_COLLAPSES + 2) * 300, batch = BATCH)
	public static void bystanderCollectingFormation(GameTestHelper helper) {
		PlaythroughPlayer owner = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughPlayer bystander = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 10));
		BlockPos cauldron = cauldron(helper, PlaythroughSites.stand(helper, 12, 8));
		int[] collapses = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					PlaythroughStates.neophyteD1(owner); // ARRANGE
					PlaythroughStates.neophyteD1(bystander); // ARRANGE
				})
				.step(conjure(owner, ManipulationInit.blood_projection, ItemInit.blood_projection.get()))
				.step(condenseOne(owner, cauldron, collapses))
				.then(() -> {
					emptyHand(bystander);
					Outcome taken = bystander.useOn(cauldron);
					boolean gone = !(helper.getLevel().getBlockEntity(cauldron) instanceof SanguineCondenserBlockEntity held)
							|| held.heldFormations() == 0;
					PlaythroughLog.note(helper, "bystander gathered: " + taken.describe() + "; the owner's formation is "
							+ (gone ? "gone" : "still there") + "; owner " + progress(owner) + ", bystander " + progress(bystander));
					taken.gave(ItemInit.sanguine_formation.get(), 1);
					helper.assertTrue(!progress(bystander).formationProjected(), "a bystander earns no formation proof: "
							+ progress(bystander));
					helper.assertTrue(!progress(owner).formationProjected(), "the contributor has no proof without the item: "
							+ progress(owner));
				})
				.run();
	}

	/**
	 * Eight Befouling Ash Trails placed round an iron block, an ink sac in the offhand, and Blood Projection held on the
	 * iron: the craft takes its blood and the catalyst, the Hematic Iron Block comes out, and the structure proof is
	 * recorded.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 500, batch = BATCH)
	public static void hematicIronStructureCraft(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		BlockPos centre = PlaythroughSites.stand(helper, 14, 8);
		PlaythroughClock.start(helper)
				.then(() -> {
					PlaythroughStates.neophyteD1(player); // ARRANGE
					buildAshRing(player, centre, Items.IRON_BLOCK);
					player.player().setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.INK_SAC)); // ARRANGE: the catalyst in the offhand
				})
				.step(conjure(player, ManipulationInit.blood_projection, ItemInit.blood_projection.get()))
				.then(() -> topUp(player, PROOF_ML)) // ARRANGE
				.step(projectInto(player, centre, 200, () -> !helper.getLevel().getBlockState(centre).is(Blocks.IRON_BLOCK)))
				.idle(5)
				.then(() -> {
					Outcome craft = player.last();
					PlaythroughLog.note(helper, "hematic iron craft: " + craft.describe());
					helper.assertTrue(craft.bloodSpent() >= 50.0D - 1.0E-6D, "the craft takes its 50 mL; " + craft);
					helper.assertTrue(player.player().getOffhandItem().isEmpty(), "the ink sac is used: "
							+ player.player().getOffhandItem());
					boolean placed = helper.getLevel().getBlockState(centre).is(BlockInit.hematic_iron_block.get());
					int picked = placed ? 0 : player.pickUpNear(centre, BlockInit.hematic_iron_block.get())
							.delta(BlockInit.hematic_iron_block.get());
					PlaythroughLog.note(helper, "the Hematic Iron Block was " + (placed ? "placed" : "dropped (" + picked + " picked up)"));
					helper.assertTrue(placed || picked == 1, "the craft yields one Hematic Iron Block");
					helper.assertTrue(progress(player).structureCrafted(), "the structure proof is recorded: " + progress(player));
				})
				.run();
	}

	// --- The claim -------------------------------------------------------------------------------------------------

	/**
	 * The four proofs played in order (Venous Stone, a cauldron formation, the Hematic Iron craft, 500 mL from a
	 * reservoir), the ledger reads ready, and the Vicar's claim goes through the acknowledged path: delivered, degree 2,
	 * both advancements, and the kit (4 scrap, 8 ash, 2 formations) picked up from the Vicar's feet.
	 */
	@GameTest(template = EMPTY, timeoutTicks = (MAX_COLLAPSES + 2) * 300 + 1200, batch = SLOW)
	public static void fullChainClaimPromotesToD2(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 3, 20));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 5, 20));
		BlockPos stone = PlaythroughSites.stand(helper, 10, 4);
		BlockPos cauldron = cauldron(helper, PlaythroughSites.stand(helper, 10, 10));
		BlockPos centre = PlaythroughSites.stand(helper, 20, 10);
		BlockPos reservoir = reservoir(helper, PlaythroughSites.stand(helper, 20, 4), 2000.0D);
		int[] collapses = new int[1];
		PlaythroughClock.start(helper)
				// The Venous Stone
				.then(() -> {
					PlaythroughStates.neophyteD1(player); // ARRANGE
					player.moveTo(PlaythroughSites.stand(helper, 8, 4));
					player.give(new ItemStack(Items.STONE)); // ARRANGE
					player.place(stone, Items.STONE);
				})
				.step(conjure(player, ManipulationInit.blood_projection, ItemInit.blood_projection.get()))
				.then(() -> topUp(player, PROOF_ML)) // ARRANGE
				.step(projectInto(player, stone, 200, () -> air(helper, stone)))
				.then(() -> player.pickUpNear(stone, BlockInit.venous_stone.get()).gave(BlockInit.venous_stone.get(), 1))
				// A formation
				.then(() -> player.moveTo(PlaythroughSites.stand(helper, 8, 10)))
				.step(condenseOne(player, cauldron, collapses))
				.then(() -> {
					dispel(player);
					emptyHand(player);
					player.useOn(cauldron).gave(ItemInit.sanguine_formation.get(), 1);
				})
				// The structure craft
				.then(() -> {
					player.moveTo(PlaythroughSites.stand(helper, 17, 10));
					buildAshRing(player, centre, Items.IRON_BLOCK);
					player.player().setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.INK_SAC)); // ARRANGE: the catalyst
				})
				.step(conjure(player, ManipulationInit.blood_projection, ItemInit.blood_projection.get()))
				.then(() -> topUp(player, PROOF_ML)) // ARRANGE
				.step(projectInto(player, centre, 200, () -> !helper.getLevel().getBlockState(centre).is(Blocks.IRON_BLOCK)))
				.idle(5)
				.then(() -> {
					if (!helper.getLevel().getBlockState(centre).is(BlockInit.hematic_iron_block.get())) {
						player.pickUpNear(centre, BlockInit.hematic_iron_block.get());
					}
					helper.assertTrue(progress(player).structureCrafted(), "the structure proof is recorded: " + progress(player));
				})
				// 500 mL absorbed
				.then(() -> {
					player.moveTo(PlaythroughSites.stand(helper, 18, 4));
					dispel(player);
				})
				.step(conjure(player, ManipulationInit.blood_absorption, ItemInit.blood_absorption.get()))
				.then(() -> PlaythroughStates.blood(player, 100.0D)) // ARRANGE: room for the reservoir's blood
				.step(absorbFrom(player, reservoir, 200, () -> progress(player).absorbedMl() >= PROOF_ML))
				// The ledger and the claim
				.then(() -> {
					dispel(player);
					FirstBloodcraftLedgerProgress shown = readLedger(player).firstBloodcraft();
					PlaythroughLog.note(helper, "ledger before the claim: " + shown + " after " + collapses[0] + " collapses");
					helper.assertTrue(shown.readyForVicar(), "the ledger reads ready after four played proofs: " + shown);
					player.approach(vicar);
					DialogueView view = player.talkTo(vicar);
					helper.assertTrue(view.offers(CLAIM), "the Vicar offers the claim: " + view.describe());
					Outcome claim = player.choose(vicar, CLAIM);
					PlaythroughLog.note(helper, "claim: " + claim.describe());
					claim.deliveredIs(true).degreeIs(2).earned(ADV_D2).earned(ADV_CLAIMED).said(CLAIM_GRANTED);
					claim.dropped(ItemInit.hematic_iron_scrap.get(), 4, Outcome.Near.NPC)
							.dropped(BlockInit.befouling_ash_trail.get(), 8, Outcome.Near.NPC)
							.dropped(ItemInit.sanguine_formation.get(), 2, Outcome.Near.NPC);
					helper.assertTrue(pickUp(player, claim, ItemInit.hematic_iron_scrap.get()) == 4
									&& pickUp(player, claim, BlockInit.befouling_ash_trail.get()) == 8
									&& pickUp(player, claim, ItemInit.sanguine_formation.get()) == 2,
							"the whole kit can be picked up from the Vicar's feet; " + player.last());
				})
				.run();
	}

	/**
	 * The claim forged at 0, 1, 2 and 3 proofs: each time the Vicar says the proofs are not all in, the player stays
	 * D1 with nothing dropped, and the hub shows only the not-ready option.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void prematureClaimWithZeroToThreeProofs(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		for (int proofs = 0; proofs <= 3; proofs++) {
			if (proofs > 0) arrangeProof(player, proofs - 1); // ARRANGE: the next proof, through its recorder
			Outcome early = player.forge(CLAIM, vicar.getId());
			early.said(CLAIM_UNREADY).degreeIs(1).deliveredIs(false);
			helper.assertTrue(early.drops().isEmpty() && !player.has(ADV_CLAIMED),
					"a claim at " + proofs + " proofs drops nothing and records nothing; " + early);
			DialogueView view = player.talkTo(vicar);
			helper.assertTrue(view.hasNode("first_bloodcraft_reward_unready") && !view.shownEvents().contains(CLAIM)
							&& !view.hasNode("first_bloodcraft_reward_granted"),
					"at " + proofs + " proofs the hub shows only the not-ready option: " + view.describe());
		}
		helper.succeed();
	}

	/** At D2 the claim, sent again to the same Vicar and to a second one, says it is known and changes nothing. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void repeatClaimRefused(GameTestHelper helper) {
		HarbingerVicarEntity first = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		HarbingerVicarEntity second = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.votaryD2(player); // ARRANGE
		DialogueView view = player.talkTo(first);
		helper.assertTrue(!view.shownEvents().contains(CLAIM), "the D2 Vicar shows no claim: " + view.describe());
		for (HarbingerVicarEntity vicar : List.of(first, second)) {
			Outcome again = player.forge(CLAIM, vicar.getId());
			PlaythroughLog.note(helper, "repeat claim at Vicar #" + vicar.getId() + ": " + again.describe());
			again.said(CLAIM_KNOWN).degreeIs(2).deliveredIs(false).gaveNothing();
		}
		helper.succeed();
	}

	/**
	 * A ready claim sent from 9 blocks: the acknowledgement says not delivered and nothing about the player changes.
	 * The same claim from beside the Vicar then goes through.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void claimFromNineBlocks(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		for (int proof = 0; proof < 4; proof++) arrangeProof(player, proof); // ARRANGE: the four proofs
		player.moveTo(PlaythroughSites.farFrom(vicar, player.home()));
		Outcome far = player.forge(CLAIM, vicar.getId());
		PlaythroughLog.note(helper, "claim from " + String.format("%.1f", player.player().distanceTo(vicar)) + " blocks: "
				+ far.describe());
		far.deliveredIs(false).degreeIs(1).unchanged();
		helper.assertTrue(far.events().isEmpty(), "an out-of-reach claim posts no event; " + far);
		player.approach(vicar);
		Outcome near = player.forge(CLAIM, vicar.getId());
		near.deliveredIs(true).degreeIs(2);
		helper.succeed();
	}

	/**
	 * Absorption and a Liber craft attempted at D0 (the cell hands handed over, since a D0 player cannot conjure), then
	 * the Vicar's ceremony: at D1 every First Bloodcraft proof is still unset.
	 */
	@GameTest(template = EMPTY, timeoutTicks = EarlyInitiation.DURATION + 400, batch = BATCH)
	public static void proofsAtD0DoNotCount(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 6, 16));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		BlockPos reservoir = reservoir(helper, PlaythroughSites.stand(helper, 12, 8), 2000.0D);
		BlockPos shelf = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughClock.start(helper)
				.then(() -> {
					PlaythroughStates.charmBearerD0(player, (Entity) null); // ARRANGE
					PlaythroughStates.blood(player, 100.0D); // ARRANGE
					emptyHand(player);
					player.player().setItemInHand(InteractionHand.MAIN_HAND,
							new ItemStack(ItemInit.blood_absorption.get())); // ARRANGE: a cell hand handed to a D0 player
				})
				.step(absorbFrom(player, reservoir, 40, () -> false))
				.then(() -> {
					PlaythroughLog.note(helper, "D0 absorption: " + player.last().describe());
					player.moveTo(PlaythroughSites.stand(helper, 13, 12));
					player.player().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); // ARRANGE: put away
					buildAshRing(player, shelf, Items.BOOKSHELF);
					emptyHand(player);
					player.player().setItemInHand(InteractionHand.MAIN_HAND,
							new ItemStack(ItemInit.blood_projection.get())); // ARRANGE: a cell hand handed to a D0 player
					player.player().setItemInHand(InteractionHand.OFF_HAND,
							new ItemStack(ItemInit.sanguine_formation.get())); // ARRANGE: the Liber's catalyst
					topUp(player, PROOF_ML); // ARRANGE
				})
				.step(projectInto(player, shelf, 60, () -> !helper.getLevel().getBlockState(shelf).is(Blocks.BOOKSHELF)))
				.idle(5)
				.then(() -> {
					int libers = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(shelf).inflate(3.0D),
							drop -> drop.getItem().is(ItemInit.liber_sanguinum.get())).size();
					PlaythroughLog.note(helper, "D0 Liber attempt: " + player.last().describe() + "; shelf now "
							+ helper.getLevel().getBlockState(shelf).getBlock() + ", Liber drops " + libers);
					player.player().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); // ARRANGE: put away
					player.player().setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY); // ARRANGE: put away
					player.approach(vicar);
					DialogueView view = player.talkTo(vicar);
					helper.assertTrue(view.offers(BEGIN), "the charm-bearer is offered initiation: " + view.describe());
					helper.assertTrue(player.choose(vicar, BEGIN).rewardDelivered(), "the ceremony begins; " + player.last());
				})
				.step(player.tickPlayer(EarlyInitiation.DURATION + 2))
				.then(() -> {
					FirstBloodcraftLedgerProgress after = progress(player);
					PlaythroughLog.note(helper, "proofs after a D0 head start: " + after);
					helper.assertTrue(player.degree() == 1, "the ceremony ends at degree 1; " + player.last());
					helper.assertTrue(after.absorbedMl() == 0.0D && !after.formationProjected()
									&& !after.venousStoneProjected() && !after.structureCrafted(),
							"nothing done at D0 counts toward the D1 proofs: " + after);
				})
				.run();
	}

	/**
	 * D-17: the player logs out while the fed stone collapses. The proof should still be theirs; the stone drops either
	 * way and the observed proof is logged.
	 */
	// DECISION: D-17 a logout during the Venous Stone collapse (or a pending structure craft) loses the proof.
	@GameTest(template = EMPTY, timeoutTicks = 500, batch = BATCH, required = false)
	public static void proofLostWhenOfflineAtCollapse(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		BlockPos stone = PlaythroughSites.stand(helper, 12, 8);
		double[] start = new double[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					PlaythroughStates.neophyteD1(player); // ARRANGE
					player.give(new ItemStack(Items.STONE)); // ARRANGE
					player.place(stone, Items.STONE);
				})
				.step(conjure(player, ManipulationInit.blood_projection, ItemInit.blood_projection.get()))
				.then(() -> {
					topUp(player, PROOF_ML); // ARRANGE
					start[0] = player.blood();
				})
				.step(projectInto(player, stone, 100, () -> start[0] - player.blood() >= 50.0D - 1.0E-6D))
				.then(() -> {
					helper.assertTrue(!air(helper, stone), "the stone is fed but has not collapsed yet; " + player.last());
					player.offline();
				})
				.until(120, () -> air(helper, stone), "the fed stone collapses while its feeder is offline")
				.then(() -> {
					player.online();
					Outcome pickup = player.pickUpNear(stone, BlockInit.venous_stone.get());
					boolean proof = progress(player).venousStoneProjected();
					PlaythroughLog.note(helper, "D-17: offline at the collapse; stone dropped and picked up "
							+ pickup.delta(BlockInit.venous_stone.get()) + ", proof " + (proof ? "recorded" : "lost"));
					helper.assertTrue(proof, "a logout during the collapse keeps the Venous Stone proof");
				})
				.run();
	}

	// --- The Hermit Road -------------------------------------------------------------------------------------------

	/**
	 * F-11: before any remnant the report is unproven; reading the first invitation stone's inscription unlocks it;
	 * the report gives 4 ash and the advancement and the ledger card reads 2/2; a repeat says known. No Hermit Road
	 * line promises a ledger the report does not give.
	 */
	// FIX: F-11 the Hermit Road lines stop promising a ledger (HEMOMANCY_REFERENCE:227).
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void hermitRoadReportOnce(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		BlockPos remnant = remnant(helper, PlaythroughSites.stand(helper, 14, 6));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		Set<String> shownKeys = new LinkedHashSet<>(HERMIT_ROAD_LEDGER_LINES);

		DialogueView before = player.talkTo(vicar);
		shownKeys.addAll(hermitRoadKeys(before));
		helper.assertTrue(before.optionByText(ASK_HERMIT_ROAD).isPresent() && !before.offers(REPORT),
				"before a remnant the Vicar asks where to look and takes no report: " + before.describe());
		Outcome unproven = player.forge(REPORT, vicar.getId());
		unproven.said(ROAD_UNPROVEN).gaveNothing();

		player.moveTo(PlaythroughSites.stand(helper, 14, 8));
		emptyHand(player);
		Outcome read = player.useOn(remnant, Direction.SOUTH);
		read.earned(ADV_REMNANT);

		player.approach(vicar);
		DialogueView found = player.talkTo(vicar);
		shownKeys.addAll(hermitRoadKeys(found));
		helper.assertTrue(found.offers(REPORT), "after the remnant the Vicar takes the report: " + found.describe());
		Outcome report = player.choose(vicar, REPORT);
		PlaythroughLog.note(helper, "Hermit Road report: " + report.describe());
		report.said(ROAD_REPORTED).earned(ADV_REPORTED).dropped(BlockInit.befouling_ash_trail.get(), 4, Outcome.Near.NPC);
		helper.assertTrue(report.delta(ItemInit.harbinger_assignment_ledger.get()) == 0, "the report gives no ledger; " + report);
		helper.assertTrue(pickUp(player, report, BlockInit.befouling_ash_trail.get()) == 4, "the 4 ash can be picked up; "
				+ player.last());
		OpenHarbingerAssignmentLedgerPacket page = readLedger(player);
		helper.assertTrue(hermitRoad(page) == 2, "the Hermit Road card reads 2/2: " + hermitRoad(page));

		Outcome repeat = player.forge(REPORT, vicar.getId());
		repeat.said(ROAD_KNOWN).gaveNothing();
		DialogueView after = player.talkTo(vicar);
		shownKeys.addAll(hermitRoadKeys(after));
		helper.assertTrue(!after.offers(REPORT) && after.hasNode("hermit_road_followup"),
				"after the report the Vicar follows up instead: " + after.describe());

		List<String> promises = ledgerPromises(shownKeys);
		PlaythroughLog.note(helper, "Hermit Road lines checked: " + shownKeys);
		helper.assertTrue(promises.isEmpty(), "Hermit Road lines still promise a ledger: " + promises);
		helper.succeed();
	}

	/** D-15: a remnant first read at D2. The player should have a way to report it; the dead end observed is logged. */
	// DECISION: D-15 the Hermit Road report is D1-only, so a remnant found later leaves the card at 1/2.
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void hermitRoadAfterD2IsDeadEnd(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 14, 8));
		BlockPos remnant = remnant(helper, PlaythroughSites.stand(helper, 14, 6));
		PlaythroughStates.votaryD2(player); // ARRANGE
		emptyHand(player);
		player.useOn(remnant, Direction.SOUTH).earned(ADV_REMNANT);
		OpenHarbingerAssignmentLedgerPacket page = readLedger(player);
		player.approach(vicar);
		DialogueView view = player.talkTo(vicar);
		PlaythroughLog.note(helper, "D-15: Hermit Road card " + hermitRoad(page) + "/2 at D2; report offered "
				+ view.offers(REPORT) + "; Vicar events " + view.reachableEvents());
		helper.assertTrue(view.offers(REPORT), "a remnant read at D2 can still be reported: " + view.describe());
		helper.succeed();
	}

	// --- Death, relog and the ledger -------------------------------------------------------------------------------

	/** Two proofs, a death and a respawn: the proofs survive and the Vicar reissues the lost ledger exactly once. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void deathMidBandKeepsProofs(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		arrangeProof(player, 0); // ARRANGE: 500 mL absorbed
		arrangeProof(player, 2); // ARRANGE: the Venous Stone
		FirstBloodcraftLedgerProgress before = progress(player);
		Outcome death = player.die();
		helper.assertTrue(player.player().isDeadOrDying(), "real damage kills the player; " + death);
		player.respawn();
		Item ledger = ItemInit.harbinger_assignment_ledger.get();
		int expected = player.count(ledger) == 0 ? 1 : 0;
		Talk first = talk(player, vicar);
		Talk second = talk(player, vicar);
		PlaythroughLog.note(helper, "after death: " + progress(player) + "; first talk " + first.outcome().describe()
				+ " | second talk " + second.outcome().describe());
		helper.assertTrue(progress(player).equals(before), "the proofs survive death: " + before + " -> " + progress(player));
		helper.assertTrue(first.outcome().delta(ledger) == expected, "a lost ledger is reissued once; " + first.outcome());
		if (expected == 1) first.outcome().said(LEDGER_GRANTED);
		second.outcome().didNotSay(LEDGER_GRANTED);
		helper.assertTrue(second.outcome().delta(ledger) == 0 && player.count(ledger) == 1,
				"the second talk reissues nothing; " + second.outcome());
		helper.succeed();
	}

	/** F-14: a ledger renamed at an anvil is still the player's ledger, so the Vicar mints no second one. */
	// FIX: F-14 grantOrRecoverAssignmentLedger recognises a carried ledger by item, not by item and components.
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void renamedLedgerNotReminted(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		Item ledger = ItemInit.harbinger_assignment_ledger.get();
		ItemStack carried = player.wield(ledger);
		carried.set(DataComponents.CUSTOM_NAME, Component.literal("Field Notes")); // ARRANGE: an anvil rename
		Talk talk = talk(player, vicar);
		PlaythroughLog.note(helper, "renamed ledger talk: " + talk.outcome().describe());
		talk.outcome().didNotSay(LEDGER_GRANTED);
		helper.assertTrue(talk.outcome().delta(ledger) == 0 && player.count(ledger) == 1,
				"a renamed ledger is not reissued; " + talk.outcome());
		helper.succeed();
	}

	/**
	 * D-3: while the claimant talks, a second player stands at the Vicar's feet and walks over the kit first. The kit
	 * should reach the claimant; who got it is logged.
	 */
	// DECISION: D-3 NPC rewards drop at the NPC's feet, unowned, so anyone can take them.
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void secondPlayerTakesTheKit(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughPlayer claimant = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 11, 8));
		PlaythroughPlayer thief = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughStates.neophyteD1(claimant); // ARRANGE
		for (int proof = 0; proof < 4; proof++) arrangeProof(claimant, proof); // ARRANGE: the four proofs
		claimant.approach(vicar);
		thief.moveTo(vicar.blockPosition());
		claimant.talkTo(vicar);
		Outcome claim = claimant.choose(vicar, CLAIM);
		claim.deliveredIs(true).degreeIs(2);
		List<Item> kit = List.of(ItemInit.hematic_iron_scrap.get(), BlockInit.befouling_ash_trail.get().asItem(),
				ItemInit.sanguine_formation.get());
		for (Item item : kit) thief.pickUpNear(vicar.blockPosition(), item);
		for (Item item : kit) pickUp(claimant, claim, item);
		Map<String, String> got = new TreeMap<>();
		for (Item item : kit) {
			got.put(BuiltInRegistries.ITEM.getKey(item).getPath(), "claimant " + claimant.count(item) + ", other " + thief.count(item));
		}
		PlaythroughLog.note(helper, "D-3 kit landing: claim dropped " + claim.drops() + "; holders " + got);
		helper.assertTrue(claimant.count(ItemInit.hematic_iron_scrap.get()) == 4
						&& claimant.count(BlockInit.befouling_ash_trail.get()) == 8
						&& claimant.count(ItemInit.sanguine_formation.get()) == 2,
				"the claimant receives the kit even with another player at the Vicar's feet: " + got);
		helper.succeed();
	}

	// --- Band steps ------------------------------------------------------------------------------------------------

	private record Talk(DialogueView view, Outcome outcome) {
	}

	/** Right-clicks {@code npc} and keeps both the served tree and what the right-click itself did. */
	private static Talk talk(PlaythroughPlayer player, Entity npc) {
		Outcome.Probe probe = Outcome.begin(player, npc, "talk to " + PlaythroughPlayer.describe(npc));
		DialogueView view = player.talkTo(npc);
		return new Talk(view, probe.finish());
	}

	/**
	 * Waits out {@code manipulation}'s cooldown, then conjures it with the cast key into an empty main hand (a cell hand
	 * already held is dispelled first). Blood is topped up to {@link #CONJURE_BLOOD} first: ARRANGE, the cost is not
	 * what these tests are about.
	 */
	private static PlaythroughClock.Step conjure(PlaythroughPlayer player,
			DeferredHolder<BloodManipulation, BloodManipulation> manipulation, ItemLike conjured) {
		return new PlaythroughClock.Step() {
			@Override
			public boolean tick(int ticks) {
				if (player.player().getMainHandItem().is(conjured.asItem())) return true;
				if (manipulation.get().isOnCooldown(player.player())) {
					if (ticks > 200) player.helper().fail(manipulation.get().getName() + " is still cooling down after 200 ticks");
					return false;
				}
				dispel(player);
				emptyHand(player);
				topUp(player, CONJURE_BLOOD); // ARRANGE
				player.select(manipulation.get().getName());
				Outcome cast = player.castSelected();
				player.helper().assertTrue(player.player().getMainHandItem().is(conjured.asItem()),
						"casting " + manipulation.get().getName() + " conjures " + conjured.asItem() + "; " + cast);
				return true;
			}

			@Override
			public String label() {
				return "conjure " + manipulation.get().getName();
			}
		};
	}

	/** Dispels a held cell hand with the cast key, as a player does before using an empty hand. */
	private static void dispel(PlaythroughPlayer player) {
		if (!(player.player().getMainHandItem().getItem() instanceof IDispellable)) return;
		Outcome dispelled = player.castSelected();
		player.helper().assertTrue(!(player.player().getMainHandItem().getItem() instanceof IDispellable),
				"the cast key dispels the held cell hand; " + dispelled);
	}

	/** Holds Blood Projection on {@code target} until {@code done} or {@code maxTicks}. */
	private static PlaythroughClock.Step projectInto(PlaythroughPlayer player, BlockPos target, int maxTicks,
			BooleanSupplier done) {
		Vec3[] aim = new Vec3[1];
		return player.holdUntil(ItemInit.blood_projection.get(), maxTicks, () -> {
			if (aim[0] == null) aim[0] = aimPoint(player, target);
			return aim[0];
		}, done);
	}

	/** Holds Blood Absorption on the reservoir at {@code target} until {@code done} or {@code maxTicks}. */
	private static PlaythroughClock.Step absorbFrom(PlaythroughPlayer player, BlockPos target, int maxTicks,
			BooleanSupplier done) {
		Vec3[] aim = new Vec3[1];
		return player.holdUntil(ItemInit.blood_absorption.get(), maxTicks, () -> {
			if (aim[0] == null) aim[0] = aimPoint(player, target);
			return aim[0];
		}, done);
	}

	/**
	 * Fills the cauldron with one formation's worth of projected blood (ARRANGE: the vessel holds exactly that much
	 * first), then waits for a formation. A collapse turns the cauldron back to iron; the player projects again, up to
	 * {@link #MAX_COLLAPSES} times. The projection stays in hand.
	 */
	private static PlaythroughClock.Step condenseOne(PlaythroughPlayer player, BlockPos cauldron, int[] collapses) {
		GameTestHelper helper = player.helper();
		return new PlaythroughClock.Step() {
			private int phase;
			private int since;
			private Vec3 aim;

			@Override
			public boolean tick(int ticks) {
				ServerLevel level = helper.getLevel();
				if (phase == 0) {
					helper.assertTrue(player.player().getMainHandItem().is(ItemInit.blood_projection.get()),
							"Blood Projection is in hand to fill the cauldron: " + player.player().getMainHandItem());
					PlaythroughStates.blood(player, FORMATION_FILL); // ARRANGE: exactly one formation's worth
					if (aim == null) aim = aimPoint(player, cauldron);
					player.startHold(ItemInit.blood_projection.get(), aim);
					helper.assertTrue(player.player().isUsingItem(), "the projection starts on the cauldron");
					phase = 1;
					since = ticks;
					return false;
				}
				if (phase == 1) {
					if (player.blood() <= 0.01D || !player.player().isUsingItem() || ticks - since > 40) {
						Outcome fill = player.releaseHold();
						double pooled = level.getBlockEntity(cauldron) instanceof SanguineCondenserBlockEntity filled
								? filled.getBloodVolume() : 0.0D;
						helper.assertTrue(pooled >= FORMATION_FILL - 0.01D, "projection pools " + FORMATION_FILL
								+ " mL in the cauldron; it holds " + pooled + "; " + fill);
						phase = 2;
						since = ticks;
					} else {
						player.aim(aim);
						player.holdTick();
					}
					return false;
				}
				if (level.getBlockEntity(cauldron) instanceof SanguineCondenserBlockEntity condenser
						&& condenser.heldFormations() > 0) {
					return true;
				}
				if (level.getBlockState(cauldron).is(Blocks.CAULDRON)) {
					collapses[0]++;
					PlaythroughLog.note(helper, "the formation collapsed (" + collapses[0] + "); projecting again");
					helper.assertTrue(collapses[0] <= MAX_COLLAPSES, "more than " + MAX_COLLAPSES + " collapses in a row");
					phase = 0;
					return false;
				}
				if (ticks - since > SanguineCondenserRules.ticksPerFormation(0) + 60) {
					helper.fail("no formation and no collapse " + (ticks - since) + " ticks after filling the cauldron");
				}
				return false;
			}

			@Override
			public String label() {
				return "condense a formation at " + cauldron.toShortString();
			}
		};
	}

	/** Holds Blood Absorption among a penned herd of cows, replacing each one that dies, until 500 mL is counted. */
	private static PlaythroughClock.Step cowAbsorption(PlaythroughPlayer player, int maxTicks, List<Cow> herd, int[] cows) {
		GameTestHelper helper = player.helper();
		PlaythroughClock.Step hold = player.holdUntil(ItemInit.blood_absorption.get(), maxTicks,
				() -> herd.stream().filter(Cow::isAlive)
						.min(Comparator.comparingDouble(cow -> cow.distanceToSqr(player.player())))
						.map(cow -> cow.getBoundingBox().getCenter()).orElse(ahead(player)),
				() -> progress(player).absorbedMl() >= PROOF_ML);
		int[][] pens = {{2, 0}, {0, 2}, {-2, 0}};
		return new PlaythroughClock.Step() {
			@Override
			public boolean tick(int ticks) {
				herd.removeIf(cow -> !cow.isAlive());
				while (herd.size() < HERD) {
					int[] pen = pens[cows[0] % pens.length];
					herd.add(cow(helper, player.home().offset(pen[0], 0, pen[1])));
					cows[0]++;
				}
				return hold.tick(ticks);
			}

			@Override
			public String label() {
				return "absorb from cows for up to " + maxTicks;
			}
		};
	}

	/** Picks up every {@code item} the outcome dropped; returns how many reached the inventory. */
	private static int pickUp(PlaythroughPlayer player, Outcome from, ItemLike item) {
		int before = player.count(item);
		Set<BlockPos> spots = new LinkedHashSet<>();
		for (Outcome.Drop drop : from.drops()) {
			if (drop.stack().is(item.asItem())) spots.add(BlockPos.containing(drop.pos()));
		}
		for (BlockPos spot : spots) player.pickUpNear(spot, item);
		return player.count(item) - before;
	}

	/** Uses the ledger and returns the page it opened. */
	private static OpenHarbingerAssignmentLedgerPacket readLedger(PlaythroughPlayer player) {
		player.wield(ItemInit.harbinger_assignment_ledger.get());
		Outcome read = player.use(InteractionHand.MAIN_HAND);
		List<OpenHarbingerAssignmentLedgerPacket> pages = read.sent(OpenHarbingerAssignmentLedgerPacket.class);
		player.helper().assertTrue(!pages.isEmpty(), "using the ledger opens it; " + read);
		return pages.getLast();
	}

	/** The Hermit Road card's count: the remnant found, and the report made. */
	private static int hermitRoad(OpenHarbingerAssignmentLedgerPacket page) {
		return (page.firstRemnant() ? 1 : 0) + (page.ledgerGranted() ? 1 : 0);
	}

	// --- ARRANGE helpers -------------------------------------------------------------------------------------------

	/** ARRANGE: records proof {@code index} (0 absorption, 1 formation, 2 Venous Stone, 3 structure) by its recorder. */
	private static void arrangeProof(PlaythroughPlayer player, int index) {
		ServerPlayer server = player.player();
		switch (index) {
			case 0 -> FirstBloodcraftAssignment.recordAbsorption(server, PROOF_ML);
			case 1 -> FirstBloodcraftAssignment.recordFormation(server);
			case 2 -> FirstBloodcraftAssignment.recordVenousStone(server);
			default -> FirstBloodcraftAssignment.recordStructure(server, new ItemStack(ItemInit.liber_sanguinum.get()));
		}
	}

	/** ARRANGE: tops the vessel up to {@code ml}; never lowers it. */
	private static void topUp(PlaythroughPlayer player, double ml) {
		if (player.blood() < ml) PlaythroughStates.blood(player, ml);
	}

	/** Takes an empty hotbar slot, as a player scrolls to an empty hand; moves the held stack aside if there is none. */
	private static void emptyHand(PlaythroughPlayer player) {
		var inventory = player.player().getInventory();
		if (inventory.getSelected().isEmpty()) return;
		for (int slot = 0; slot < 9; slot++) {
			if (inventory.items.get(slot).isEmpty()) {
				inventory.selected = slot;
				return;
			}
		}
		for (int slot = 9; slot < inventory.items.size(); slot++) {
			if (inventory.items.get(slot).isEmpty()) {
				inventory.items.set(slot, inventory.getSelected());
				inventory.items.set(inventory.selected, ItemStack.EMPTY);
				return;
			}
		}
		player.helper().fail("the player has no free slot to empty a hand into");
	}

	/**
	 * Places {@code centre} and the eight Befouling Ash Trails round it on the floor by hand (the items are handed to
	 * the player first: ARRANGE).
	 */
	private static void buildAshRing(PlaythroughPlayer player, BlockPos centre, ItemLike centreBlock) {
		Item ash = BlockInit.befouling_ash_trail.get().asItem();
		player.give(new ItemStack(centreBlock)).give(new ItemStack(ash, 8)); // ARRANGE
		player.place(centre, centreBlock);
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				if (dx != 0 || dz != 0) player.place(centre.offset(dx, 0, dz), ash);
			}
		}
		GameTestHelper helper = player.helper();
		helper.assertTrue(helper.getLevel().getBlockState(centre).is(Block.byItem(centreBlock.asItem())),
				"the ring's centre is placed: " + helper.getLevel().getBlockState(centre));
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				BlockPos at = centre.offset(dx, 0, dz);
				if ((dx != 0 || dz != 0) && !helper.getLevel().getBlockState(at).is(BlockInit.befouling_ash_trail.get())) {
					helper.fail("ash was not placed at " + at.toShortString() + ": " + helper.getLevel().getBlockState(at));
				}
			}
		}
	}

	/** ARRANGE: a filled blood reservoir (a Ghastly Alembic holding {@code ml}) standing on the floor at {@code pos}. */
	private static BlockPos reservoir(GameTestHelper helper, BlockPos pos, double ml) {
		setBlock(helper, pos, BlockInit.ghastly_alembic.get().defaultBlockState());
		BlockEntity reservoir = helper.getLevel().getBlockEntity(pos);
		helper.assertTrue(reservoir != null && HemoCapabilityAccess.getBloodVolume(reservoir).isPresent(),
				"the reservoir holds blood: " + reservoir);
		reservoir.onLoad();
		HemoCapabilityAccess.getBloodVolume(reservoir).orElseThrow().setBloodVolume(ml);
		return pos;
	}

	/** ARRANGE: an empty iron cauldron on the floor at {@code pos}. */
	private static BlockPos cauldron(GameTestHelper helper, BlockPos pos) {
		setBlock(helper, pos, Blocks.CAULDRON.defaultBlockState());
		return pos;
	}

	/**
	 * ARRANGE: a Hermitage Remnant's Blood Echo inscription (the first invitation stone) on a wall: a stone block north
	 * of {@code pos} and the inscription on its south face.
	 */
	private static BlockPos remnant(GameTestHelper helper, BlockPos pos) {
		setBlock(helper, pos.north(), Blocks.STONE.defaultBlockState());
		setBlock(helper, pos, BlockInit.blood_echo_inscription.get().defaultBlockState());
		if (!(helper.getLevel().getBlockEntity(pos) instanceof DiscoveryInscriptionBlockEntity inscription)) {
			helper.fail("the inscription has its block entity at " + pos.toShortString());
			return pos;
		}
		inscription.setInscriptionId(Hemomancy.rloc("hermitage_remnant/first_invitation_stone"));
		return pos;
	}

	/** ARRANGE: a penned cow, standing still, at {@code pos}. */
	private static Cow cow(GameTestHelper helper, BlockPos pos) {
		ServerLevel level = helper.getLevel();
		Cow cow = EntityType.COW.create(level);
		helper.assertTrue(cow != null, "a cow can be made");
		cow.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
		cow.setNoAi(true);
		cow.setPersistenceRequired();
		helper.assertTrue(level.addFreshEntity(cow), "the cow joins the level at " + pos.toShortString());
		PlaythroughCleanup.onEnd(helper, cow::discard);
		return cow;
	}

	private static void setBlock(GameTestHelper helper, BlockPos pos, BlockState state) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(level.setBlock(pos, state, Block.UPDATE_ALL), "placed " + state.getBlock() + " at " + pos.toShortString());
		PlaythroughCleanup.onEnd(helper, () -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
	}

	// --- Reading ---------------------------------------------------------------------------------------------------

	private static FirstBloodcraftLedgerProgress progress(PlaythroughPlayer player) {
		return FirstBloodcraftAssignment.progress(player.player());
	}

	private static boolean air(GameTestHelper helper, BlockPos pos) {
		return helper.getLevel().getBlockState(pos).isAir();
	}

	/** A point a block in front of the player's eyes, for a hold that aims at nothing in particular. */
	private static Vec3 ahead(PlaythroughPlayer player) {
		return player.player().getEyePosition().add(player.player().getLookAngle());
	}

	/**
	 * A point on {@code pos} that the player's look ray (the one the cell hands trace) actually lands on. Tries the
	 * centre, then lower and higher, so a block with a partial shape (a cauldron, an alembic) is still hit.
	 */
	private static Vec3 aimPoint(PlaythroughPlayer player, BlockPos pos) {
		Vec3 centre = Vec3.atCenterOf(pos);
		List<Vec3> candidates = List.of(centre, centre.add(0.0D, -0.3D, 0.0D), centre.add(0.0D, 0.3D, 0.0D),
				Vec3.atBottomCenterOf(pos).add(0.0D, 0.1D, 0.0D));
		List<String> missed = new ArrayList<>();
		for (Vec3 candidate : candidates) {
			player.aim(candidate);
			HitResult hit = player.player().pick(5.0D, 0.0F, true);
			if (hit instanceof BlockHitResult block && hit.getType() == HitResult.Type.BLOCK && partOf(player, block.getBlockPos(), pos)) {
				return candidate;
			}
			missed.add(hit.getType() + (hit instanceof BlockHitResult block ? " " + block.getBlockPos().toShortString() + " "
					+ player.player().level().getBlockState(block.getBlockPos()).getBlock() : ""));
		}
		player.helper().fail("the player cannot look at " + pos.toShortString() + " from "
				+ player.player().blockPosition().toShortString() + ": " + missed);
		return centre;
	}

	/** True when {@code hit} is {@code pos} itself or one of its filler cells (the Ghastly Alembic's), which stand in for it. */
	private static boolean partOf(PlaythroughPlayer player, BlockPos hit, BlockPos pos) {
		return hit.equals(pos) || player.player().level().getBlockEntity(hit) instanceof FillerBlockEntity filler
				&& pos.equals(filler.getMainBlockPos());
	}

	private static Set<MnemonicBlueprintTarget> carriedBlueprints(PlaythroughPlayer player) {
		Set<MnemonicBlueprintTarget> targets = new LinkedHashSet<>();
		for (ItemStack stack : player.player().getInventory().items) {
			if (!stack.is(ItemInit.mnemonic_blueprint.get())) continue;
			MnemonicBlueprintTarget target = MnemonicBlueprintItem.getTarget(stack);
			if (target != null) targets.add(target);
		}
		return targets;
	}

	/** The lines and prompts of the Vicar's Hermit Road nodes (and the hub card that carries the report). */
	private static Set<String> hermitRoadKeys(DialogueView view) {
		Set<String> keys = new LinkedHashSet<>();
		for (DialogueNode node : view.nodes().values()) {
			boolean carriesReport = node.options().stream().anyMatch(option -> REPORT.equals(option.eventId()));
			boolean road = node.id().startsWith("hermit_road");
			if (!road && !carriesReport) continue;
			if (road || !node.id().equals(view.tree().startNodeId())) {
				for (String line : node.lines()) Dialogue.lineKey(line).ifPresent(keys::add);
			}
			for (DialogueOption option : node.options()) {
				if (option.presentation().promptKey() != null) keys.add(option.presentation().promptKey());
			}
		}
		return keys;
	}

	private static List<String> ledgerPromises(Collection<String> keys) {
		List<String> found = new ArrayList<>();
		for (String key : keys) {
			String text = english(key);
			if (LEDGER_PROMISE.matcher(text).find()) found.add(key + " = \"" + text + "\"");
		}
		return found;
	}

	/** The English the mod ships for {@code key} (the classpath {@code en_us.json}), or "" when it has none. */
	private static synchronized String english(String key) {
		if (english == null) {
			Map<String, String> lines = new HashMap<>();
			try (InputStream in = PlaythroughNeophyteGameTests.class.getResourceAsStream("/assets/hemomancy/lang/en_us.json")) {
				if (in == null) throw new IllegalStateException("en_us.json is not on the classpath");
				JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					if (entry.getValue().isJsonPrimitive()) lines.put(entry.getKey(), entry.getValue().getAsString());
				}
			} catch (IOException exception) {
				throw new IllegalStateException("Could not read en_us.json", exception);
			}
			english = lines;
		}
		return english.getOrDefault(key, "");
	}

	/** Everything a band state is made of that a player can see, minus identity. */
	private static List<String> stateDifferences(PlaythroughPlayer played, PlaythroughPlayer arranged) {
		List<String> drift = new ArrayList<>();
		compare(drift, "degree", played.degree(), arranged.degree());
		compare(drift, "blood active", played.bloodActive(), arranged.bloodActive());
		compare(drift, "charm", played.hasCharm(), arranged.hasCharm());
		compare(drift, "advancements", new TreeSet<>(PlaythroughFingerprints.earned(played.player())),
				new TreeSet<>(PlaythroughFingerprints.earned(arranged.player())));
		compare(drift, "items", itemCounts(played.player()), itemCounts(arranged.player()));
		compare(drift, "known manipulations", known(played), known(arranged));
		compare(drift, "equipped manipulations", equipped(played), equipped(arranged));
		compare(drift, "first bloodcraft", progress(played), progress(arranged));
		return drift;
	}

	private static String stateSummary(PlaythroughPlayer player) {
		return "degree " + player.degree() + ", active " + player.bloodActive() + ", charm " + player.hasCharm()
				+ ", advancements " + new TreeSet<>(PlaythroughFingerprints.earned(player.player()))
				+ ", items " + itemCounts(player.player()) + ", known " + known(player) + ", equipped " + equipped(player);
	}

	private static void compare(List<String> drift, String what, Object played, Object arranged) {
		if (!Objects.equals(played, arranged)) drift.add(what + ": played " + played + " vs arranged " + arranged);
	}

	private static Map<String, Integer> itemCounts(ServerPlayer player) {
		Map<String, Integer> counts = new TreeMap<>();
		for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
			ItemStack stack = player.getInventory().getItem(slot);
			if (!stack.isEmpty()) counts.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), Integer::sum);
		}
		return counts;
	}

	private static Set<String> known(PlaythroughPlayer player) {
		Set<String> names = new TreeSet<>();
		for (BloodManipulation manipulation : HemoCapabilityAccess.getKnownManipulations(player.player()).getManipList()) {
			names.add(manipulation.getName());
		}
		return names;
	}

	private static Set<String> equipped(PlaythroughPlayer player) {
		return new TreeSet<>(HemoCapabilityAccess.getKnownManipulations(player.player()).getEquippedManipNames());
	}
}
