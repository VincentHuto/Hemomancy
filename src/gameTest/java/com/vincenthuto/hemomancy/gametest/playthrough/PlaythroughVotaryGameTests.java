package com.vincenthuto.hemomancy.gametest.playthrough;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerVicarDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.MnemonistStarterMemoryChoice;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodInjectionRules;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.item.harbinger.tile.functional.SpecimenJarData;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.VialRackItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.GhastlyAlembicMenu;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.mission.alchemist.BodyAnswersAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodProgress;
import com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationLedgerProgress;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftAssignment;
import com.vincenthuto.hemomancy.common.mission.vicar.FirstBloodcraftLedgerProgress;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.StartCentrifugeButtonPacket;
import com.vincenthuto.hemomancy.common.network.mission.OpenHarbingerAssignmentLedgerPacket;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineCondenserRules;
import com.vincenthuto.hemomancy.gametest.PlaythroughFingerprints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/**
 * Band d2_votary: the Votary's chapter as a player plays it, from the Vicar's first-bloodcraft claim to the Concentrated
 * Blood rest that wakes an Initiate. The Vicar's D2 hint, the Alchemist's First Separation (brief, a placed centrifuge,
 * two cow samples, an assigned spin, the tagged output taken by hand, the claim), the first Alembic distillation, the
 * Concentrated Blood vial, its injection and a night's sleep; the clinical lessons (microscope, injection, three
 * sources, cabinet) and the red taxonomy turn-ins; and the edges a real server sees: an early ask, a lost charm, a
 * second vial, a stashed vial, leaving bed, relog and End return while the blood settles, a death after the Artificer's
 * and the Mnemonist's one-time rewards, forged packets, two players on one centrifuge, two centrifuges for one player,
 * and a server where one player stays awake.
 * <p>
 * Every step under test enters the server where the client's action enters it (right-click an NPC, choose an offered
 * option, place a block, attack with a vial, click a menu slot, press the centrifuge's start button, hold a vial or a
 * microscope, sleep in a bed). Anything not under test is arranged and says so with {@code ARRANGE}. Tests for a
 * pending user decision are {@code required = false} with a {@code DECISION} line; tests for an identified fix carry
 * a {@code FIX} line and assert the fixed behaviour. Clock tests (night and sleep) each run in their own batch, so no
 * two of them move the day at once.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughVotaryGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_d2_votary";
	private static final String CLOCK = "playthrough_d2_votary_clock_";
	private static final String SCENARIO = "d2_votary";

	// Relative (x, z) spots on the template floor. Everyone and everything stands at PlaythroughSites.STAND_Y.
	private static final int[] HOME = {10, 8};
	private static final int[] SECOND_HOME = {16, 8};
	private static final int[] ALCHEMIST = {13, 8};
	private static final int[] VICAR = {7, 8};
	private static final int[] ARTIFICER = {10, 11};
	private static final int[] MNEMONIST = {10, 5};
	private static final int[] CENTRIFUGE = {12, 11};
	private static final int[] SECOND_CENTRIFUGE = {8, 11};
	private static final int[] ALEMBIC = {14, 12};
	private static final int[] BED_FOOT = {8, 6};
	private static final int[] CHEST = {6, 10};
	private static final int[] COW_A = {18, 4};
	private static final int[] COW_B = {20, 5};
	private static final int[] PIG = {18, 16};
	private static final int[] SHEEP = {20, 17};
	private static final int[] CHAIN_STONE = {12, 5};
	private static final int[] CHAIN_IRON = {15, 16};
	private static final int[] CHAIN_CAULDRON = {6, 15};

	private static final String BRIEF = HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF;
	private static final String CLAIM = HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM;
	private static final String BODY_ANSWERS = HarbingerAlchemistDialogueTrees.EVENT_BODY_ANSWERS_BRIEF;
	private static final String REPLACE = "alchemist_replace_concentrated_blood";
	private static final String CONCENTRATED_NODE = "concentrated_blood";
	private static final String VICAR_CLAIM = HarbingerVicarDialogueTrees.EVENT_CLAIM_FIRST_BLOODCRAFT_REWARD;
	private static final String BLOOD_REST = "hemomancy.initiation.blood_rest";
	private static final String AWAKENED = "hemomancy.initiation.awakened";
	private static final String NEXT_DISTILLATION = "hemomancy.alchemist.first_separation.next_distillation";
	private static final String VOTARY_HINT = "hemomancy.vicar.votary.degree_hint";
	private static final String INITIATE_GREETING = "hemomancy.alchemist.initiate.line1";
	private static final String MISSING_SAMPLE = "hemomancy.dialogue.event.alchemist_red_taxonomy_missing_sample";
	private static final String TAXONOMY_KNOWN = "hemomancy.dialogue.event.alchemist_red_taxonomy_known";
	private static final String CLINICAL_NEXT_NODE = "clinical_assignment_next";
	private static final String PROJECTION = "blood_projection";
	private static final String ABSORPTION = "blood_absorption";
	private static final double PROOF_ML = 500.0D;

	private static Map<String, String> english;

	private PlaythroughVotaryGameTests() {
	}

	// =================================================================================================================
	// The chain from the band before
	// =================================================================================================================

	/**
	 * A fresh Neophyte earns the four First Bloodcraft proofs by play: Venous Stone projected out of a practice stone,
	 * a Hematic Iron Block crafted by projection with the ink sac in the offhand, 500 mL absorbed back out of their own
	 * Sanguine Cauldron, and a formation condensed and taken by hand. Then the Vicar's claim promotes them, and the
	 * result matches {@code PlaythroughStates.votaryD2} (the state the rest of this band starts from).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 4000, batch = BATCH)
	public static void votaryChainFromPreviousBand(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.neophyteD1(player); // ARRANGE: initiation is the d0 band's
		PlaythroughClock clock = PlaythroughClock.start(helper);
		chain(helper, player, clock);
		clock.run();
	}

	/**
	 * The D1 to D2 chain as steps on {@code clock}, for {@link #votaryChainFromPreviousBand} and the full run: sets up
	 * the Vicar, the practice stone, the iron pattern and the cauldron now, then plays the four proofs and the claim
	 * and ends comparing the player with {@link PlaythroughStates#votaryD2}. {@code player} must be a fresh Neophyte.
	 */
	public static void chain(GameTestHelper helper, PlaythroughPlayer player, PlaythroughClock clock) {
		ServerLevel level = helper.getLevel();
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, at(helper, VICAR));
		player.moveTo(at(helper, HOME));
		PlaythroughPlayer fixture = PlaythroughPlayer.create(helper, at(helper, SECOND_HOME));
		PlaythroughStates.votaryD2(fixture); // ARRANGE: the state this chain must match
		BlockPos stone = at(helper, CHAIN_STONE);
		BlockPos iron = at(helper, CHAIN_IRON);
		BlockPos cauldron = at(helper, CHAIN_CAULDRON);
		// ARRANGE: a practice stone, the Hematic Iron Block pattern and an empty cauldron, laid out on the floor (laying
		// blocks is not what this test is about), and an ink sac from the player's pack.
		setBlock(helper, stone, Blocks.STONE.defaultBlockState());
		layHematicIronPattern(helper, iron);
		setBlock(helper, cauldron, Blocks.CAULDRON.defaultBlockState());
		player.give(new ItemStack(Items.INK_SAC));
		clock
				.then(() -> {
					PlaythroughStates.blood(player, maxBlood(player)); // ARRANGE: the starter flasks drunk
					toOffhand(player, stack -> stack.is(Items.INK_SAC), "the ink sac");
					emptyMainHand(player);
					player.select(PROJECTION);
					Outcome cast = player.castSelected();
					helper.assertTrue(player.player().getMainHandItem().is(ItemInit.blood_projection.get()),
							"casting Blood Projection with an empty hand conjures the cell hand; " + cast);
					player.moveTo(stone.west(2));
				})
				.step(() -> player.holdUntil(ItemInit.blood_projection.get(), 400, () -> Vec3.atCenterOf(stone),
						() -> level.getBlockState(stone).isAir() || bloodcraft(player).venousStoneProjected()))
				.until(120, () -> bloodcraft(player).venousStoneProjected(),
						"the projected stone collapses into Venous Stone and records the proof")
				.then(() -> {
					Outcome pickup = player.pickUpNear(stone, BlockInit.venous_stone.get());
					PlaythroughLog.note(helper, SCENARIO, "venous stone: " + pickup.describe());
					player.moveTo(iron.west(3));
				})
				.step(() -> player.holdUntil(ItemInit.blood_projection.get(), 400, () -> Vec3.atCenterOf(iron),
						() -> bloodcraft(player).structureCrafted()))
				.until(200, () -> bloodcraft(player).structureCrafted(), "the Hematic Iron Block craft settles")
				.then(() -> {
					PlaythroughLog.note(helper, SCENARIO, "structure: " + bloodcraft(player) + ", at the pattern "
							+ level.getBlockState(iron).getBlock());
					player.moveTo(cauldron.west(2));
				})
				.step(absorbFromOwnCauldron(player, cauldron))
				.step(condenseFormation(player, cauldron))
				.then(() -> {
					Outcome dispel = player.castSelected();
					helper.assertTrue(player.player().getMainHandItem().isEmpty(),
							"casting the selected cell hand again dispels it; " + dispel);
					Outcome collect = player.useOn(cauldron);
					helper.assertTrue(bloodcraft(player).formationProjected(),
							"an empty-handed right-click on a condenser holding the player's formation takes it and"
									+ " records the proof (SanguineCondenserBlock#useWithoutItem collects it); " + collect);
				})
				.then(() -> {
					FirstBloodcraftLedgerProgress ready = bloodcraft(player);
					helper.assertTrue(ready.readyForVicar(), "all four proofs are in by play: " + ready);
					DialogueView view = player.approach(vicar).talkTo(vicar);
					keys(helper, view, "the Vicar with the four proofs in");
					helper.assertTrue(view.offers(VICAR_CLAIM), "the Vicar offers the claim: " + view.describe());
					Outcome claim = player.choose(vicar, VICAR_CLAIM);
					claim.deliveredIs(true).degreeIs(2).earned(FirstBloodcraftAssignment.ADV_REWARD_CLAIMED.getPath());
					for (ItemLike kit : List.of(ItemInit.hematic_iron_scrap.get(), BlockInit.befouling_ash_trail.get(),
							ItemInit.sanguine_formation.get())) {
						player.pickUpNear(vicar.blockPosition(), kit);
					}
					PlaythroughLog.note(helper, SCENARIO, "claim: " + claim.describe());
				})
				.then(() -> {
					Set<String> state = PlaythroughFingerprints.earned(fixture.player());
					Set<String> played = PlaythroughFingerprints.earned(player.player());
					Set<String> missing = new TreeSet<>(state);
					missing.removeAll(played);
					Set<String> extra = new TreeSet<>(played);
					extra.removeAll(state);
					PlaythroughLog.note(helper, SCENARIO, "played vs PlaythroughStates.votaryD2: missing=" + missing
							+ " extra=" + extra + " progress played=" + bloodcraft(player) + " state="
							+ bloodcraft(fixture));
					helper.assertTrue(player.degree() == fixture.degree(), "both are degree " + fixture.degree());
					helper.assertTrue(missing.isEmpty(), "play earns every advancement votaryD2 sets; missing " + missing);
					FirstBloodcraftLedgerProgress a = bloodcraft(player);
					FirstBloodcraftLedgerProgress b = bloodcraft(fixture);
					helper.assertTrue(a.completedProofs() == b.completedProofs() && a.votaryReached() == b.votaryReached(),
							"the ledger reads the same for play and for the state: " + a + " vs " + b);
					helper.assertTrue(player.count(ItemInit.hematic_iron_scrap.get()) >= 4
									&& player.count(BlockInit.befouling_ash_trail.get()) >= 8
									&& player.count(ItemInit.sanguine_formation.get()) >= 2,
							"the Vicar's kit reached the inventory by pickup");
				});
	}

	// =================================================================================================================
	// Happy path
	// =================================================================================================================

	/** At D2 the Vicar's "what next" hint names the Alchemist, Concentrated Blood and sleep, and every key resolves. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void votaryVicarPointsToTheAlchemist(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, at(helper, VICAR));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		DialogueView view = player.approach(vicar).talkTo(vicar);
		keys(helper, view, "the Vicar at D2");
		Optional<DialogueNode> hint = view.node("degree_hint");
		helper.assertTrue(hint.isPresent(), "the D2 Vicar can be asked what comes next: " + view.describe());
		helper.assertTrue(view.topics().stream().anyMatch(topic -> "degree_hint".equals(topic.targetNodeId())),
				"a hub card opens the degree hint: " + view.describe());
		List<String> lines = hint.get().lines().stream().map(Dialogue::lineKey).flatMap(Optional::stream).toList();
		helper.assertTrue(lines.contains(VOTARY_HINT), "the hint is the votary one: " + lines);
		String text = english(VOTARY_HINT);
		for (String word : List.of("Alchemist", "Concentrated Blood", "sleep")) {
			helper.assertTrue(text.contains(word), "the D2 hint names '" + word + "': " + text);
		}
		PlaythroughLog.note(helper, SCENARIO, "vicar D2 hint: " + text);
		helper.succeed();
	}

	/** Choosing the First Separation brief inserts two empty vials and records the briefing; the offer then goes. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void firstSeparationBriefDeliversTwoVials(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		DialogueView view = player.approach(alchemist).talkTo(alchemist);
		keys(helper, view, "the Alchemist at D2");
		helper.assertTrue(view.offers(BRIEF), "a fresh Votary is offered First Separation: " + view.describe());
		Outcome brief = player.choose(alchemist, BRIEF);
		brief.gave(ItemInit.bloody_vial.get(), 2).earned(FirstSeparationAssignment.ADV_BRIEFED.getPath());
		helper.assertTrue(count(player, PlaythroughVotaryGameTests::emptyVial) == 2,
				"both briefing vials are empty vials; " + brief);
		DialogueView after = player.talkTo(alchemist);
		keys(helper, after, "the Alchemist after the brief");
		helper.assertFalse(after.offers(BRIEF), "the brief is not offered twice: " + after.describe());
		helper.succeed();
	}

	/**
	 * The First Separation as played: place a crafted centrifuge, fill the two briefing vials from two cows, set them
	 * in opposite slots, press start, wait out the spin, take the tagged enzyme out by hand, and claim. The syringe and a
	 * rack of eight vials are inserted and the Alchemist points at the distillation.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 800, batch = BATCH)
	public static void centrifugeSpinRecoverAndClaim(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		Entity first = mob(helper, EntityType.COW, COW_A);
		Entity second = mob(helper, EntityType.COW, COW_B);
		BlockPos centrifuge = at(helper, CENTRIFUGE);
		craftedStation(player, BlockInit.vial_centrifuge.get()); // ARRANGE: the Blood Structure craft is the D1 band's
		player.give(new ItemStack(BlockInit.vial_centrifuge.get()));
		VialCentrifugeMenu[] menu = new VialCentrifugeMenu[1];
		PlaythroughClock.start(helper)
				.then(() -> brief(player, alchemist))
				.then(() -> {
					Outcome placed = player.place(centrifuge, BlockInit.vial_centrifuge.get());
					helper.assertTrue(level.getBlockEntity(centrifuge) instanceof VialCentrifugeBlockEntity,
							"the centrifuge is placed; " + placed);
					helper.assertTrue(FirstSeparationAssignment.hasCentrifugeAcquired(player.player()),
							"placing it records the centrifuge step");
					sample(player, first);
					sample(player, second);
					helper.assertTrue(FirstSeparationAssignment.hasSampleAcquired(player.player()),
							"filling a vial records the sample step");
					menu[0] = openStation(player, centrifuge, VialCentrifugeMenu.class);
					Outcome start = loadAndSpin(player, menu[0], EntityType.COW);
					helper.assertTrue(menu[0].getTe().isProcessing(), "the centrifuge spins; " + start);
					start.earned(HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED.getPath());
				})
				.until(VialCentrifugeBlockEntity.SPIN_TOTAL_TIME + 80, () -> !menu[0].getTe().isProcessing(),
						"the spin finishes")
				.then(() -> {
					Outcome took = takeOutput(player, menu[0]);
					took.earned(HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE.getPath());
					player.closeMenu();
					DialogueView view = player.approach(alchemist).talkTo(alchemist);
					keys(helper, view, "the Alchemist with First Separation done");
					helper.assertTrue(view.offers(CLAIM), "the claim is offered: " + view.describe());
					Outcome claim = player.choose(alchemist, CLAIM);
					claim.deliveredIs(true).gave(ItemInit.living_syringe.get(), 1).gave(ItemInit.vial_rack.get(), 1)
							.said(NEXT_DISTILLATION).earned(FirstSeparationAssignment.ADV_REWARD_CLAIMED.getPath());
					ItemStack rack = find(player, stack -> stack.is(ItemInit.vial_rack.get()));
					helper.assertTrue(VialRackItem.countEmptyVials(rack) == VialRackItem.MAX_VIALS,
							"the rack comes with eight empty vials; it holds " + VialRackItem.countEmptyVials(rack));
				})
				.run();
	}

	/**
	 * The first distillation: an Alembic set over a lit campfire, an infected fungus clicked into its input, and the
	 * result shift-clicked out. The distill use is recorded and the Alchemist's Concentrated Blood card appears.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 500, batch = BATCH)
	public static void firstDistillationNeedsHeat(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		firstSeparationClaimed(player); // ARRANGE
		craftedStation(player, BlockInit.ghastly_alembic.get()); // ARRANGE: crafted by Blood Structure
		player.give(new ItemStack(BlockInit.ghastly_alembic.get()));
		player.give(new ItemStack(Items.CAMPFIRE));
		player.give(new ItemStack(BlockInit.infected_fungus.get()));
		BlockPos campfire = at(helper, ALEMBIC);
		BlockPos alembic = campfire.above();
		GhastlyAlembicMenu[] menu = new GhastlyAlembicMenu[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					player.place(campfire, Items.CAMPFIRE);
					Outcome placed = player.place(alembic, BlockInit.ghastly_alembic.get());
					helper.assertTrue(level.getBlockEntity(alembic) instanceof GhastlyAlembicBlockEntity,
							"the Alembic sits on the campfire; " + placed);
					helper.assertFalse(HemoCapabilityAccess.stationUpgrades(player.player())
							.hasUsed(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL), "nothing distilled yet");
					menu[0] = openStation(player, alembic, GhastlyAlembicMenu.class);
					putInto(player, menu[0], stack -> stack.is(BlockInit.infected_fungus.get().asItem()),
							GhastlyAlembicMenu.INGREDIENT_SLOT, "the infected fungus");
				})
				.until(400, () -> !menu[0].getTe().getItem(GhastlyAlembicBlockEntity.SLOT_RESULT).isEmpty(),
						"the heated Alembic distills the fungus")
				.then(() -> {
					Outcome taken = player.quickMove(menu[0], GhastlyAlembicMenu.RESULT_SLOT);
					helper.assertTrue(taken.delta(ItemInit.foul_paste.get()) > 0, "the result is in the inventory; " + taken);
					helper.assertTrue(HemoCapabilityAccess.stationUpgrades(player.player())
							.hasUsed(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL),
							"taking the result records the distill use; " + taken);
					player.closeMenu();
					DialogueView view = player.approach(alchemist).talkTo(alchemist);
					keys(helper, view, "the Alchemist after the first distillation");
					helper.assertTrue(view.offers(REPLACE) && view.topic(REPLACE).isPresent(),
							"the Concentrated Blood card appears: " + view.describe());
				})
				.run();
	}

	/**
	 * Concentrated Blood from the Alchemist, injected by holding the vial, then a night's sleep: the blood is pending
	 * and the saturation applies, the sleep wakes the player at D3 with the awakened line, and the Alchemist now
	 * greets an Initiate without the Concentrated Blood card.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = CLOCK + "inject_sleep")
	public static void injectAndSleepPromotesToD3(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		BlockPos bed = bed(helper, at(helper, BED_FOOT));
		PlaythroughClock.start(helper)
				.then(() -> {
					askForConcentrated(player, alchemist);
					wieldConcentrated(player);
				})
				.step(() -> player.hold(ItemInit.bloody_vial.get(), BloodInjectionRules.USE_TICKS + 10, ahead(player)))
				.then(() -> {
					Outcome injected = player.last();
					helper.assertTrue(ConcentratedBlood.pending(player.player()),
							"holding the vial injects it and the blood is pending; " + injected);
					helper.assertTrue(player.player().hasEffect(EffectInit.transfusion_saturation),
							"the injection applies transfusion saturation");
					helper.assertTrue(concentrated(player) == 0, "the vial is spent; " + injected);
					injected.degreeIs(2);
					player.moveTo(player.home());
					night(helper);
				})
				.idle(3)
				.then(() -> {
					Outcome slept = player.sleepThroughNight(bed);
					slept.degreeIs(3).said(AWAKENED);
					helper.assertFalse(ConcentratedBlood.pending(player.player()), "the rest settles the blood");
					DialogueView view = player.approach(alchemist).talkTo(alchemist);
					keys(helper, view, "the Alchemist at D3");
					List<String> greeting = startLines(view);
					helper.assertTrue(greeting.contains(INITIATE_GREETING),
							"the Alchemist greets an Initiate: " + greeting);
					helper.assertFalse(concentratedShown(view), "the Concentrated Blood card is gone: " + view.describe());
				})
				.run();
	}

	/**
	 * The clinical chapter in order: a vial filled from a cow and held for the microscope lesson, the microscope held
	 * 40 ticks over it, the injection lesson, two more sources examined, and the cabinet lesson. Each lesson teaches its
	 * recipe, Liber page and advancement, and the Alchemist's "next study" card moves on each time.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = BATCH)
	public static void clinicalMicroscopeToCabinet(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		player.give(new ItemStack(ItemInit.bloody_vial.get(), 3)); // ARRANGE: three empty vials
		Entity cow = mob(helper, EntityType.COW, COW_A);
		Entity pig = mob(helper, EntityType.PIG, PIG);
		Entity sheep = mob(helper, EntityType.SHEEP, SHEEP);
		List<String> next = new ArrayList<>();
		ClinicalBloodProgress.Lesson microscope = ClinicalBloodProgress.Lesson.MICROSCOPE;
		ClinicalBloodProgress.Lesson injection = ClinicalBloodProgress.Lesson.INJECTION;
		ClinicalBloodProgress.Lesson cabinet = ClinicalBloodProgress.Lesson.CABINET;
		PlaythroughClock.start(helper)
				.then(() -> {
					sample(player, pig);
					sample(player, sheep);
					sample(player, cow);
					helper.assertTrue(player.has(clinical("collected")), "a filled vial records the collection");
					DialogueView view = player.approach(alchemist).talkTo(alchemist);
					keys(helper, view, "the Alchemist with a sample in hand");
					next.add(nextStudy(view));
					helper.assertTrue(view.offers(microscope.event()), "holding a sample unlocks the microscope lesson: "
							+ view.describe());
					Outcome taught = player.choose(alchemist, microscope.event());
					taught.earned(clinicalPath("microscope"));
					assertLesson(helper, player, "microscope", "hematic_microscope");
					next.add(nextStudy(player.talkTo(alchemist)));
					player.give(new ItemStack(ItemInit.hematic_microscope.get())); // ARRANGE: the recipe is now known
					toOffhand(player, stack -> sampleOf(stack, EntityType.COW), "the cow sample");
					player.wield(ItemInit.hematic_microscope.get());
				})
				.step(() -> player.hold(ItemInit.hematic_microscope.get(), 50, ahead(player)))
				.then(() -> {
					helper.assertTrue(HemoCapabilityAccess.clinicalBlood(player.player()).sourceCount() == 1,
							"forty ticks under the microscope examine the cow sample; " + player.last());
					helper.assertTrue(player.has(clinical("examined")), "the first examination is recorded");
					DialogueView view = player.approach(alchemist).talkTo(alchemist);
					next.add(nextStudy(view));
					helper.assertTrue(view.offers(injection.event()), "an examined source unlocks the injection lesson: "
							+ view.describe());
					player.choose(alchemist, injection.event()).earned(clinicalPath("injection"));
					helper.assertTrue(LiberKnowledgeHelperBridge.hasEntry(player, "injection"), "the injection page unlocks");
					next.add(nextStudy(player.talkTo(alchemist)));
					toOffhand(player, stack -> sampleOf(stack, EntityType.PIG), "the pig sample");
					player.wield(ItemInit.hematic_microscope.get());
				})
				.step(() -> player.hold(ItemInit.hematic_microscope.get(), 50, ahead(player)))
				.then(() -> {
					toOffhand(player, stack -> sampleOf(stack, EntityType.SHEEP), "the sheep sample");
					player.wield(ItemInit.hematic_microscope.get());
				})
				.step(() -> player.hold(ItemInit.hematic_microscope.get(), 50, ahead(player)))
				.then(() -> {
					helper.assertTrue(HemoCapabilityAccess.clinicalBlood(player.player()).sourceCount() == 3,
							"three sources examined, has " + HemoCapabilityAccess.clinicalBlood(player.player()).sourceCount());
					helper.assertTrue(player.has(clinical("three_sources")), "three sources are recorded");
					DialogueView view = player.approach(alchemist).talkTo(alchemist);
					next.add(nextStudy(view));
					helper.assertTrue(view.offers(cabinet.event()), "three sources unlock the cabinet lesson: "
							+ view.describe());
					player.choose(alchemist, cabinet.event()).earned(clinicalPath("cabinet"));
					assertLesson(helper, player, "cabinet", "phlebotomists_cabinet");
					next.add(nextStudy(player.talkTo(alchemist)));
					PlaythroughLog.note(helper, SCENARIO, "clinical next-study lines: " + next);
					List<String> expected = List.of("show_vial", "examine", "injection", "three_sources", "cabinet",
							"storage").stream().map(step -> "hemomancy.clinical.next." + step).toList();
					helper.assertTrue(next.equals(expected), "the next-study card follows the lessons: " + next
							+ " expected " + expected);
				})
				.run();
	}

	// =================================================================================================================
	// Edges
	// =================================================================================================================

	/**
	 * The Concentrated Blood card stays hidden before the First Separation claim, and after the claim until the first
	 * distillation; a forged ask in either state gives nothing and changes nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void concentratedTopicHiddenUntilReady(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		firstSeparationDone(player); // ARRANGE: briefed, spun and recovered, not yet claimed
		DialogueView before = player.approach(alchemist).talkTo(alchemist);
		helper.assertFalse(concentratedShown(before), "no Concentrated Blood before the claim: " + before.describe());
		Outcome early = player.forge(REPLACE, alchemist.getId());
		helper.assertTrue(concentrated(player) == 0, "a forged early ask gives no vial; " + early);
		early.didNotSay(BLOOD_REST).unchanged();
		helper.assertTrue(FirstSeparationAssignment.claimRewards(player.player()), "ARRANGE: the claim is made");
		DialogueView claimed = player.talkTo(alchemist);
		helper.assertFalse(concentratedShown(claimed), "no Concentrated Blood before the first distillation: "
				+ claimed.describe());
		Outcome undistilled = player.forge(REPLACE, alchemist.getId());
		helper.assertTrue(concentrated(player) == 0, "a forged ask before distilling gives no vial; " + undistilled);
		undistilled.didNotSay(BLOOD_REST).unchanged();
		helper.succeed();
	}

	/** Getting out of bed before morning leaves the player at D2 with the blood still pending. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = CLOCK + "leave_bed")
	public static void leaveBedDoesNotSettle(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		helper.assertTrue(ConcentratedBlood.inject(player.player()), "ARRANGE: the vial is injected");
		BlockPos bed = bed(helper, at(helper, BED_FOOT));
		PlaythroughClock.start(helper)
				.then(() -> night(helper))
				.idle(3)
				.then(() -> {
					lieDown(player, bed);
					Outcome left = player.leaveBed();
					left.degreeIs(2).didNotSay(AWAKENED);
					helper.assertTrue(ConcentratedBlood.pending(player.player()),
							"leaving bed keeps the blood pending; " + left);
				})
				.run();
	}

	/**
	 * The pending blood survives a relog and an End return, and the one completed sleep after them gives exactly one
	 * promotion. (A second sleep is not taken: at D3 an ordinary bed may begin a Chamber dream, which is another band's.)
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = CLOCK + "relog_end")
	public static void relogAndEndReturnKeepPending(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		helper.assertTrue(ConcentratedBlood.inject(player.player()), "ARRANGE: the vial is injected");
		BlockPos bed = bed(helper, at(helper, BED_FOOT));
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					player.relog();
					helper.assertTrue(ConcentratedBlood.pending(player.player()) && player.degree() == 2,
							"a relog keeps the blood pending at D2; " + player.last());
					player.endReturn();
					helper.assertTrue(ConcentratedBlood.pending(player.player()) && player.degree() == 2,
							"an End return keeps the blood pending at D2; " + player.last());
					night(helper);
					mark[0] = player.mark();
				})
				.idle(3)
				.then(() -> {
					Outcome slept = player.sleepThroughNight(bed);
					slept.degreeIs(3).said(AWAKENED);
					long awakened = player.chatSince(mark[0]).stream().filter(line -> line.has(AWAKENED)).count();
					helper.assertTrue(awakened == 1, "exactly one awakening; said " + awakened + " times");
					helper.assertFalse(ConcentratedBlood.pending(player.player()), "the rest clears the pending blood");
					helper.assertTrue(player.has(HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE.toString()),
							"the earlier degree advancement is still there after relog and End return");
				})
				.run();
	}

	/** A sample held in the offhand when the turn-in is clicked is refused; four main-hand samples complete it. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void redTaxonomyMainHandOnly(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		List<HarbingerAlchemistDialogueTrees.RedTaxonomySample> samples = List.of(
				HarbingerAlchemistDialogueTrees.RedTaxonomySample.INFECTED_FUNGUS,
				HarbingerAlchemistDialogueTrees.RedTaxonomySample.STINKHORN_FUNGUS,
				HarbingerAlchemistDialogueTrees.RedTaxonomySample.SARCODES,
				HarbingerAlchemistDialogueTrees.RedTaxonomySample.BLEEDING_HEART);
		// ARRANGE: the samples the player gathered, two of the first for the repeat.
		player.give(new ItemStack(samples.getFirst().block(), 2));
		for (int i = 1; i < samples.size(); i++) player.give(new ItemStack(samples.get(i).block()));
		var first = samples.getFirst();
		player.wield(first.block());
		DialogueView view = player.approach(alchemist).talkTo(alchemist);
		helper.assertTrue(view.offers(first.eventId()), "a held sample can be submitted: " + view.describe());
		toOffhand(player, stack -> stack.is(first.block().asItem()), "the fungus");
		Outcome offhand = player.choose(alchemist, first.eventId());
		offhand.said(MISSING_SAMPLE);
		helper.assertTrue(offhand.delta(first.block()) == 0 && offhand.earned().isEmpty(),
				"an offhand sample is neither taken nor recorded; " + offhand);
		Outcome last = null;
		for (var sample : samples) {
			player.wield(sample.block());
			DialogueView held = player.talkTo(alchemist);
			helper.assertTrue(held.offers(sample.eventId()), sample + " can be submitted: " + held.describe());
			last = player.choose(alchemist, sample.eventId());
			last.earned(sample.advancement().getPath());
			helper.assertTrue(last.delta(sample.block()) == -1, "the submitted " + sample + " is taken; " + last);
			for (ItemLike reward : List.of(ItemInit.bloody_vial.get(), BlockInit.specimen_jar.get())) {
				player.pickUpNear(player.player().blockPosition(), reward);
			}
		}
		last.earned(HarbingerAdvancementGranter.ADV_RED_TAXONOMY_COMPLETE.getPath());
		int jars = last.delta(BlockInit.specimen_jar.get()) + last.dropped(BlockInit.specimen_jar.get(), Outcome.Near.ANY);
		int vials = last.delta(ItemInit.bloody_vial.get()) + last.dropped(ItemInit.bloody_vial.get(), Outcome.Near.ANY);
		helper.assertTrue(jars == 4 && vials == 4, "completing the taxonomy gives 4 jars and 4 vials, got " + jars
				+ " and " + vials + "; " + last);
		player.wield(first.block());
		player.talkTo(alchemist);
		Outcome repeat = player.choose(alchemist, first.eventId());
		repeat.said(TAXONOMY_KNOWN);
		helper.assertTrue(repeat.delta(first.block()) == 0, "a repeat sample is not taken; " + repeat);
		helper.succeed();
	}

	// =================================================================================================================
	// Fixes (required; they assert the fixed behaviour)
	// =================================================================================================================

	/**
	 * The Worn Vow inspection pays its 4 scrap once. After a death and respawn the Artificer does not offer it again and
	 * a forged claim drops nothing.
	 */
	// FIX: F-4 Artificer lesson claim keys move from root persistent data into PlayerPersisted.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void wornVowDeathDoesNotPayAgain(GameTestHelper helper) {
		HarbingerArtificerEntity artificer = PlaythroughNpcs.artificer(helper, at(helper, ARTIFICER));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		String brief = HarbingerArtificerDialogueTrees.EVENT_BRIEF_WORN_VOW;
		String claim = HarbingerArtificerDialogueTrees.EVENT_CLAIM_WORN_VOW_REWARD;
		DialogueView view = player.approach(artificer).talkTo(artificer);
		keys(helper, view, "the Artificer at D2");
		helper.assertTrue(view.offers(brief), "the Artificer briefs the Worn Vow: " + view.describe());
		player.choose(artificer, brief);
		// ARRANGE: the armature placed and its first hematic upgrade made (the Armature is not this test's subject).
		HarbingerAdvancementGranter.grantIfNotDone(player.player(), HarbingerAdvancementGranter.ADV_ARTIFICER_ARMATURE_PLACED);
		HarbingerAdvancementGranter.grantIfNotDone(player.player(),
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_HEMATIC_UPGRADE);
		DialogueView ready = player.talkTo(artificer);
		helper.assertTrue(ready.offers(claim), "the inspection is offered: " + ready.describe());
		Outcome paid = player.choose(artificer, claim);
		int scrap = paid.delta(ItemInit.hematic_iron_scrap.get()) + paid.dropped(ItemInit.hematic_iron_scrap.get(), Outcome.Near.ANY);
		helper.assertTrue(scrap == 4, "the inspection pays 4 scrap; " + paid);
		player.pickUpNear(artificer.blockPosition(), ItemInit.hematic_iron_scrap.get());
		player.die();
		player.respawn();
		DialogueView after = player.approach(artificer).talkTo(artificer);
		PlaythroughLog.note(helper, SCENARIO, "artificer after death: " + after.describe());
		helper.assertFalse(after.offers(claim), "F-4: the paid inspection is not offered after death: " + after.describe());
		Outcome forged = player.forge(claim, artificer.getId());
		int again = forged.delta(ItemInit.hematic_iron_scrap.get()) + forged.dropped(ItemInit.hematic_iron_scrap.get(), Outcome.Near.ANY);
		helper.assertTrue(again == 0, "F-4: a forged claim after death pays nothing; " + forged);
		helper.succeed();
	}

	/**
	 * The Mnemonist's starter crude memory is one choice for life: claimed, stashed unlearned, and after a death and
	 * respawn the choice is not offered and a forged claim delivers nothing.
	 */
	// FIX: F-4 the Mnemonist starter claim moves from root persistent data into PlayerPersisted.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void mnemonistStarterDeathDoesNotReoffer(GameTestHelper helper) {
		HarbingerMnemonistEntity mnemonist = PlaythroughNpcs.mnemonist(helper, at(helper, MNEMONIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		BlockPos chest = at(helper, CHEST);
		setBlock(helper, chest, Blocks.CHEST.defaultBlockState());
		String starter = MnemonistStarterMemoryChoice.BLOOD_SHOT.eventId();
		DialogueView view = player.approach(mnemonist).talkTo(mnemonist);
		keys(helper, view, "the Mnemonist at D2");
		helper.assertTrue(view.offers(starter), "a Votary may choose a starter memory: " + view.describe());
		Outcome claimed = player.choose(mnemonist, starter);
		claimed.deliveredIs(true);
		player.pickUpNear(mnemonist.blockPosition(), ItemInit.crude_memory_blood_shot.get());
		helper.assertTrue(player.count(ItemInit.crude_memory_blood_shot.get()) == 1, "the crude memory reached the player; "
				+ claimed);
		stash(player, chest, stack -> stack.is(ItemInit.crude_memory_blood_shot.get())); // ARRANGE: kept, unlearned
		player.die();
		player.respawn();
		DialogueView after = player.approach(mnemonist).talkTo(mnemonist);
		PlaythroughLog.note(helper, SCENARIO, "mnemonist after death: " + after.describe());
		for (MnemonistStarterMemoryChoice choice : MnemonistStarterMemoryChoice.values()) {
			helper.assertFalse(after.offers(choice.eventId()), "F-4: the starter choice is not offered after death: "
					+ after.describe());
		}
		Outcome forged = player.forgeClaim(starter, mnemonist.getId());
		forged.deliveredIs(false);
		int again = forged.delta(ItemInit.crude_memory_blood_shot.get())
				+ forged.dropped(ItemInit.crude_memory_blood_shot.get(), Outcome.Near.ANY);
		helper.assertTrue(again == 0, "F-4: a forged starter claim after death gives nothing; " + forged);
		helper.succeed();
	}

	/** A second Concentrated vial injected while the first is pending is refused with a line that says why, and kept. */
	// FIX: F-13 a refused Concentrated injection says why with a system message.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void refusedConcentratedInjectionExplains(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		PlaythroughClock.start(helper)
				.then(() -> {
					askForConcentrated(player, alchemist);
					wieldConcentrated(player);
				})
				.step(() -> player.hold(ItemInit.bloody_vial.get(), BloodInjectionRules.USE_TICKS + 10, ahead(player)))
				.then(() -> {
					helper.assertTrue(ConcentratedBlood.pending(player.player()), "the first vial is pending; " + player.last());
					player.give(ConcentratedBlood.create()); // ARRANGE: a second vial (how one is had is D-19's)
					wieldConcentrated(player);
				})
				.step(() -> player.hold(ItemInit.bloody_vial.get(), BloodInjectionRules.USE_TICKS + 10, ahead(player)))
				.then(() -> {
					Outcome refused = player.last();
					PlaythroughLog.note(helper, SCENARIO, "second injection while pending: " + refused.describe());
					helper.assertTrue(concentrated(player) == 1, "the refused vial is kept; " + refused);
					helper.assertTrue(ConcentratedBlood.pending(player.player()), "the first injection is still pending");
					helper.assertFalse(refused.chat().isEmpty(), "F-13: a refused injection says why; " + refused);
					for (Outcome.ChatLine line : refused.chat()) {
						for (String key : line.keys()) helper.assertTrue(LangKeys.exists(key), "the refusal key exists: " + key);
					}
				})
				.run();
	}

	/** Once both proofs are in, Ask for Concentrated Blood is a Quest card, and the Alchemist's sigil is NOTICE. */
	// FIX: F-12 Main-step options are filed as quests with a NOTICE sigil when actionable.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void concentratedTopicIsAQuestWithSigil(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		DialogueView view = player.approach(alchemist).talkTo(alchemist);
		helper.assertTrue(view.topic(REPLACE).isPresent(), "the Concentrated Blood card is shown: " + view.describe());
		// A curious player reads every unread lore card first.
		for (DialogueTopic topic : view.topics()) {
			if (topic.lore() && topic.unread()) player.openTopic(alchemist, topic.id());
		}
		DialogueView read = player.talkTo(alchemist);
		DialogueTopic card = read.topic(REPLACE).orElseThrow();
		DialogueAttention sigil = Dialogue.attention(alchemist, player.player());
		PlaythroughLog.note(helper, SCENARIO, "concentrated card " + card.category() + "/" + card.attention()
				+ ", Alchemist sigil " + sigil + "; " + read.describe());
		List<String> problems = new ArrayList<>();
		if (card.category() != DialogueCategory.QUESTS) problems.add("the card is filed in " + card.category());
		if (card.attention() != DialogueAttention.NOTICE) problems.add("the card's sigil is " + card.attention());
		if (sigil != DialogueAttention.NOTICE) problems.add("the Alchemist's sigil is " + sigil);
		helper.assertTrue(problems.isEmpty(), "F-12: " + problems);
		helper.succeed();
	}

	/** With the charm moved out of the vascular slot, the Alchemist does not offer Concentrated Blood. */
	// FIX: F-13 EarlyInitiationDialogue.alchemist hides the Concentrated Blood option when the charm is not attached.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void charmRemovedHidesConcentratedOption(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		// ARRANGE: the charm taken off through the Vanity path.
		HemoCapabilityAccess.getEquipment(player.player()).setStackInSlot(5, ItemStack.EMPTY);
		helper.assertFalse(player.hasCharm(), "the charm is off");
		DialogueView view = player.approach(alchemist).talkTo(alchemist);
		PlaythroughLog.note(helper, SCENARIO, "no charm: " + view.describe());
		helper.assertFalse(view.offers(REPLACE), "F-13: no Concentrated Blood without the charm: " + view.describe());
		helper.succeed();
	}

	/**
	 * Forged Alchemist events (the First Separation and Body Answers briefs, a red taxonomy turn-in, the bestiary record
	 * and surrender) sent with entityId 0 and with a Vicar as the speaker are refused at dispatch: no event, no new
	 * state, nothing taken, nothing given.
	 */
	// FIX: F-3 progression events refuse entityId == 0 and require their speaker type.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void forgedAlchemistEventsRefused(GameTestHelper helper) {
		PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST)); // the rightful speaker stands at the outpost
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, at(helper, VICAR));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		int[] speakers = {0, vicar.getId()};
		List<String> problems = new ArrayList<>();
		var fungus = HarbingerAlchemistDialogueTrees.RedTaxonomySample.INFECTED_FUNGUS;
		player.give(new ItemStack(fungus.block(), 2)); // ARRANGE
		player.wield(fungus.block());
		for (int speaker : speakers) {
			refuse(problems, player, BRIEF, speaker,
					() -> !FirstSeparationAssignment.isBriefed(player.player()) && player.count(ItemInit.bloody_vial.get()) == 0);
			refuse(problems, player, fungus.eventId(), speaker,
					() -> !player.has(fungus.advancement().toString()) && player.count(fungus.block()) == 2);
		}
		// ARRANGE: a jar holding a research specimen in the main hand.
		CompoundTag chiton = new CompoundTag();
		chiton.putString("id", "hemomancy:chiton");
		player.give(SpecimenJarData.createStackWithSpecimen(chiton));
		player.wield(BlockInit.specimen_jar.get());
		int recorded = HemoCapabilityAccess.getSpecimenBestiary(player.player()).recordedSpecimenCount();
		for (int speaker : speakers) {
			refuse(problems, player, HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_RECORD, speaker,
					() -> HemoCapabilityAccess.getSpecimenBestiary(player.player()).recordedSpecimenCount() == recorded);
			refuse(problems, player, HarbingerAlchemistDialogueTrees.EVENT_BESTIARY_SURRENDER, speaker,
					() -> SpecimenJarData.hasSpecimen(player.player().getMainHandItem()));
		}
		firstSeparationClaimed(player); // ARRANGE: Body Answers becomes briefable
		int formations = player.count(ItemInit.sanguine_formation.get());
		int enzymes = player.count(ItemInit.fervent_enzyme.get());
		int flasks = player.count(ItemInit.bloody_flask.get());
		for (int speaker : speakers) {
			refuse(problems, player, BODY_ANSWERS, speaker,
					() -> !player.has(BodyAnswersAssignment.ADV_BRIEFED.toString())
							&& player.count(ItemInit.sanguine_formation.get()) == formations
							&& player.count(ItemInit.fervent_enzyme.get()) == enzymes
							&& player.count(ItemInit.bloody_flask.get()) == flasks);
		}
		helper.assertTrue(problems.isEmpty(), "F-3: forged Alchemist events reached the player: " + problems);
		helper.succeed();
	}

	// =================================================================================================================
	// Pending decisions (not required; each asserts what a player would expect and logs what happens)
	// =================================================================================================================

	/** A vial stashed in a chest, then asked for again: a player expects one vial per rest, not a second one. */
	// DECISION: D-19 the Concentrated Blood reissue checks only the carried inventory.
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = BATCH, required = false)
	public static void stashedConcentratedVialIsReissued(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		BlockPos chest = at(helper, CHEST);
		setBlock(helper, chest, Blocks.CHEST.defaultBlockState());
		int[] reissued = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					askForConcentrated(player, alchemist);
					stash(player, chest, ConcentratedBlood::is); // ARRANGE: put away in a chest
					DialogueView view = player.talkTo(alchemist);
					boolean offered = view.offers(REPLACE);
					Outcome again = offered ? player.choose(alchemist, REPLACE) : player.forge(REPLACE, alchemist.getId());
					reissued[0] = concentrated(player);
					PlaythroughLog.note(helper, SCENARIO, "D-19 after stashing the vial: offered again=" + offered
							+ ", carried now=" + reissued[0] + "; " + again.describe());
					unstash(player, chest); // ARRANGE: fetched back
					if (concentrated(player) > 0) wieldConcentrated(player);
				})
				.step(() -> concentrated(player) > 0
						? player.hold(ItemInit.bloody_vial.get(), BloodInjectionRules.USE_TICKS + 10, ahead(player))
						: ticks -> true)
				.then(() -> {
					PlaythroughLog.note(helper, SCENARIO, "D-19 first injection: pending=" + ConcentratedBlood.pending(player.player())
							+ ", vials left=" + concentrated(player));
					if (concentrated(player) > 0) wieldConcentrated(player);
				})
				.step(() -> concentrated(player) > 0
						? player.hold(ItemInit.bloody_vial.get(), BloodInjectionRules.USE_TICKS + 10, ahead(player))
						: ticks -> true)
				.then(() -> {
					PlaythroughLog.note(helper, SCENARIO, "D-19 the extra vial: still carried=" + concentrated(player)
							+ " (it cannot be injected while the first is pending, nor after D3); last="
							+ player.last().describe());
					helper.assertTrue(reissued[0] == 0, "D-19 expected: no second vial while one is stashed; the"
							+ " Alchemist issued one (" + reissued[0] + " carried)");
				})
				.run();
	}

	/** An Alembic set on plain floor, as at the outpost, with fungus in it: a player expects it to distill. */
	// DECISION: D-12 the outpost Alembic has no heat source and no line says the base Alembic needs heat.
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH, required = false)
	public static void outpostAlembicHasNoHeat(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		firstSeparationClaimed(player); // ARRANGE
		craftedStation(player, BlockInit.ghastly_alembic.get()); // ARRANGE
		player.give(new ItemStack(BlockInit.ghastly_alembic.get()));
		player.give(new ItemStack(BlockInit.infected_fungus.get()));
		BlockPos alembic = at(helper, ALEMBIC);
		GhastlyAlembicMenu[] menu = new GhastlyAlembicMenu[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					Outcome placed = player.place(alembic, BlockInit.ghastly_alembic.get());
					helper.assertTrue(level.getBlockEntity(alembic) instanceof GhastlyAlembicBlockEntity,
							"the Alembic stands on the floor; " + placed);
					menu[0] = openStation(player, alembic, GhastlyAlembicMenu.class);
					putInto(player, menu[0], stack -> stack.is(BlockInit.infected_fungus.get().asItem()),
							GhastlyAlembicMenu.INGREDIENT_SLOT, "the infected fungus");
				})
				.idle(250)
				.then(() -> {
					GhastlyAlembicBlockEntity te = menu[0].getTe();
					ItemStack result = te.getItem(GhastlyAlembicBlockEntity.SLOT_RESULT);
					PlaythroughLog.note(helper, SCENARIO, "D-12 unheated Alembic after 250 ticks: status="
							+ te.getProcessingStatus() + ", menu heated=" + menu[0].isHeated() + ", result=" + result
							+ ", said=" + player.chat());
					helper.assertFalse(result.isEmpty(), "D-12 expected: the outpost-style Alembic distills; it reports "
							+ te.getProcessingStatus());
				})
				.run();
	}

	/**
	 * The chapter played on the outpost's own centrifuge (one the player never placed), then carried to D3: a player
	 * expects the First Separation card to read complete.
	 */
	// DECISION: D-13 using only outpost stations leaves First Separation at 7/8.
	@GameTest(template = EMPTY, timeoutTicks = 800, batch = BATCH, required = false)
	public static void outpostOnlyStationsLeaveLedgerIncomplete(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		Entity first = mob(helper, EntityType.COW, COW_A);
		Entity second = mob(helper, EntityType.COW, COW_B);
		BlockPos centrifuge = at(helper, CENTRIFUGE);
		// ARRANGE: the outpost's centrifuge, already standing; the player did not place it.
		setBlock(helper, centrifuge, BlockInit.vial_centrifuge.get().defaultBlockState());
		VialCentrifugeMenu[] menu = new VialCentrifugeMenu[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					brief(player, alchemist);
					sample(player, first);
					sample(player, second);
					Outcome uncrafted = player.useOn(centrifuge, faceToward(player, centrifuge));
					boolean opened = player.player().containerMenu instanceof VialCentrifugeMenu;
					PlaythroughLog.note(helper, SCENARIO, "D-13 a player who never crafted a centrifuge "
							+ (opened ? "opens" : "cannot open") + " the outpost's: " + uncrafted.describe());
					if (opened) player.closeMenu();
					// ARRANGE: give them the craft credit the access gate wants (crafting one is the D1 band's).
					craftedStation(player, BlockInit.vial_centrifuge.get());
					menu[0] = openStation(player, centrifuge, VialCentrifugeMenu.class);
					loadAndSpin(player, menu[0], EntityType.COW);
				})
				.until(VialCentrifugeBlockEntity.SPIN_TOTAL_TIME + 80, () -> !menu[0].getTe().isProcessing(),
						"the outpost spin finishes")
				.then(() -> {
					takeOutput(player, menu[0]);
					player.closeMenu();
					player.approach(alchemist).talkTo(alchemist);
					player.choose(alchemist, CLAIM).deliveredIs(true);
					// ARRANGE: the rest of the chapter (distill, inject, rest) is covered above.
					HemoCapabilityAccess.stationUpgrades(player.player()).recordUse(UpgradeStation.ALEMBIC,
							StationUpgradeCatalog.DISTILL);
					helper.assertTrue(ConcentratedBlood.inject(player.player()), "ARRANGE: inject");
					helper.assertTrue(ConcentratedBlood.completeSleep(player.player(), true), "ARRANGE: rest to D3");
					FirstSeparationLedgerProgress ledger = readLedger(player);
					PlaythroughLog.note(helper, SCENARIO, "D-13 ledger at D3 on the outpost centrifuge: "
							+ ledger.completedSteps() + "/" + ledger.totalSteps() + " " + ledger
							+ " (the craft credit the access gate needs also counts as acquiring a centrifuge)");
					helper.assertTrue(ledger.complete(), "D-13 expected: First Separation reads complete at D3; it reads "
							+ ledger.completedSteps() + "/" + ledger.totalSteps());
				})
				.run();
	}

	/** P2 opens P1's centrifuge and takes P1's tagged output: a player expects P1's proof to be safe. */
	// DECISION: D-3 rewards and proofs that lie unowned can be taken by another player.
	@GameTest(template = EMPTY, timeoutTicks = 800, batch = BATCH, required = false)
	public static void twoPlayersOneCentrifuge(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer owner = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughPlayer other = PlaythroughPlayer.create(helper, at(helper, SECOND_HOME));
		PlaythroughStates.votaryD2(owner); // ARRANGE
		PlaythroughStates.votaryD2(other); // ARRANGE
		craftedStation(owner, BlockInit.vial_centrifuge.get()); // ARRANGE
		craftedStation(other, BlockInit.vial_centrifuge.get()); // ARRANGE
		owner.give(new ItemStack(BlockInit.vial_centrifuge.get()));
		Entity first = mob(helper, EntityType.COW, COW_A);
		Entity second = mob(helper, EntityType.COW, COW_B);
		BlockPos centrifuge = at(helper, CENTRIFUGE);
		VialCentrifugeMenu[] menu = new VialCentrifugeMenu[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					brief(owner, alchemist);
					owner.place(centrifuge, BlockInit.vial_centrifuge.get());
					sample(owner, first);
					sample(owner, second);
					menu[0] = openStation(owner, centrifuge, VialCentrifugeMenu.class);
					loadAndSpin(owner, menu[0], EntityType.COW);
				})
				.until(VialCentrifugeBlockEntity.SPIN_TOTAL_TIME + 80, () -> !menu[0].getTe().isProcessing(),
						"the owner's spin finishes")
				.then(() -> {
					owner.closeMenu();
					VialCentrifugeMenu theirs = openStation(other, centrifuge, VialCentrifugeMenu.class);
					Outcome taken = takeOutputIfAny(other, theirs);
					other.closeMenu();
					VialCentrifugeMenu mine = openStation(owner, centrifuge, VialCentrifugeMenu.class);
					Outcome left = takeOutputIfAny(owner, mine);
					owner.closeMenu();
					boolean proof = owner.has(HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE.toString());
					PlaythroughLog.note(helper, SCENARIO, "D-3 the other player took " + taken.inventoryDelta()
							+ " (their proof: " + other.has(HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE.toString())
							+ "); the owner then took " + left.inventoryDelta() + " and has the proof: " + proof);
					helper.assertTrue(proof, "D-3 expected: the owner's assigned output stays theirs; the other player"
							+ " took it and the owner has no First Separation proof");
				})
				.run();
	}

	/** Two assigned spins on two centrifuges: a player expects the first spin's output to still count. */
	// DECISION: D-18 a second assigned spin overwrites the First Separation proof.
	@GameTest(template = EMPTY, timeoutTicks = 800, batch = BATCH, required = false)
	public static void secondSpinOverwritesTheProof(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		craftedStation(player, BlockInit.vial_centrifuge.get()); // ARRANGE
		player.give(new ItemStack(BlockInit.vial_centrifuge.get(), 2));
		player.give(new ItemStack(ItemInit.bloody_vial.get(), 2)); // ARRANGE: two more empty vials
		Entity first = mob(helper, EntityType.COW, COW_A);
		Entity second = mob(helper, EntityType.COW, COW_B);
		BlockPos a = at(helper, CENTRIFUGE);
		BlockPos b = at(helper, SECOND_CENTRIFUGE);
		boolean[] firstCounts = new boolean[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					brief(player, alchemist);
					player.place(a, BlockInit.vial_centrifuge.get());
					player.place(b, BlockInit.vial_centrifuge.get());
					for (int i = 0; i < 2; i++) {
						sample(player, first);
						sample(player, second);
					}
					loadAndSpin(player, openStation(player, a, VialCentrifugeMenu.class), EntityType.COW);
					loadAndSpin(player, openStation(player, b, VialCentrifugeMenu.class), EntityType.COW);
					player.closeMenu();
				})
				.until(VialCentrifugeBlockEntity.SPIN_TOTAL_TIME + 80, () -> !centrifuge(level, a).isProcessing()
						&& !centrifuge(level, b).isProcessing(), "both spins finish")
				.then(() -> {
					Outcome fromFirst = takeOutputIfAny(player, openStation(player, a, VialCentrifugeMenu.class));
					player.closeMenu();
					firstCounts[0] = player.has(HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE.toString());
					Outcome fromSecond = takeOutputIfAny(player, openStation(player, b, VialCentrifugeMenu.class));
					player.closeMenu();
					PlaythroughLog.note(helper, SCENARIO, "D-18 the first spin's output " + fromFirst.inventoryDelta()
							+ " completes the step: " + firstCounts[0] + "; the second's " + fromSecond.inventoryDelta()
							+ " completes it: " + player.has(HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE.toString()));
					helper.assertTrue(firstCounts[0], "D-18 expected: the first assigned spin's output still completes"
							+ " First Separation; the second spin overwrote it");
				})
				.run();
	}

	/**
	 * The control for {@link #sleepNeedsTheWholeServer}: the pending player sleeps alone. Harness players never join
	 * the level, so vanilla never counts them; the harness does the night skip itself exactly when
	 * {@code SleepStatus.areEnoughSleeping} says the server would, waking the player as the skip does
	 * ({@code stopSleepInBed(false, false)}). The Concentrated Blood settles and the player wakes at degree 3.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = CLOCK + "solo_night")
	public static void sleepAloneSettlesTheBlood(GameTestHelper helper) {
		PlaythroughPlayer sleeper = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(sleeper); // ARRANGE
		helper.assertTrue(ConcentratedBlood.inject(sleeper.player()), "ARRANGE: the vial is injected");
		BlockPos bed = bed(helper, at(helper, BED_FOOT));
		PlaythroughClock.start(helper)
				.then(() -> night(helper))
				.idle(3)
				.then(() -> {
					lieDown(sleeper, bed);
					boolean skips = nightWouldPass(helper, List.of(sleeper.player()));
					helper.assertTrue(skips, "one sleeper alone is enough for the night to pass");
				})
				.idle(20)
				.then(() -> {
					skipNight(helper, sleeper);
					helper.assertTrue(sleeper.degree() == 3, "the night passing settles the blood: degree "
							+ sleeper.degree() + ", pending=" + ConcentratedBlood.pending(sleeper.player()));
					helper.assertFalse(ConcentratedBlood.pending(sleeper.player()), "nothing is left pending");
				})
				.run();
	}

	/**
	 * Two players, the default sleep percentage, and only the pending player in bed. The harness skips the night only
	 * when {@code areEnoughSleeping} says the server would, and with one player awake it does not, so the pending
	 * player stays D2. A player expects either a way to settle the blood or a line saying the night has to pass.
	 */
	// DECISION: D-11 D3 needs a server night skip; one awake player blocks it on multiplayer.
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = CLOCK + "shared_night", required = false)
	public static void sleepNeedsTheWholeServer(GameTestHelper helper) {
		PlaythroughPlayer sleeper = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughPlayer awake = PlaythroughPlayer.create(helper, at(helper, SECOND_HOME));
		readyForConcentrated(sleeper); // ARRANGE
		helper.assertTrue(ConcentratedBlood.inject(sleeper.player()), "ARRANGE: the vial is injected");
		BlockPos bed = bed(helper, at(helper, BED_FOOT));
		int[] mark = new int[1];
		boolean[] skips = new boolean[1];
		PlaythroughClock.start(helper)
				.then(() -> night(helper))
				.idle(3)
				.then(() -> {
					mark[0] = sleeper.mark();
					lieDown(sleeper, bed);
					skips[0] = nightWouldPass(helper, List.of(sleeper.player(), awake.player()));
					helper.assertFalse(skips[0], "with one of two players awake the night does not pass");
				})
				.idle(120)
				.then(() -> {
					if (skips[0]) skipNight(helper, sleeper);
					List<Outcome.ChatLine> said = sleeper.chatSince(mark[0]);
					PlaythroughLog.note(helper, SCENARIO, "D-11 after 120 ticks in bed: degree=" + sleeper.degree()
							+ ", pending=" + ConcentratedBlood.pending(sleeper.player()) + ", said=" + said);
					sleeper.leaveBed();
					// Vanilla's "Respawn point set" is not an explanation of why the night will not pass.
					boolean told = said.stream().anyMatch(line -> line.keys().stream()
							.anyMatch(key -> !key.equals("block.minecraft.set_spawn")));
					helper.assertTrue(sleeper.degree() == 3 || told, "D-11 expected: the pending player"
							+ " settles or is told why the night will not pass; they stay D2 with no line");
				})
				.run();
	}

	/** Whether the server's sleep rule would pass the night with {@code players} online, as {@code SleepStatus} sees it. */
	private static boolean nightWouldPass(GameTestHelper helper, List<ServerPlayer> players) {
		SleepStatus status = new SleepStatus();
		status.update(players);
		int percent = helper.getLevel().getGameRules().getInt(GameRules.RULE_PLAYERS_SLEEPING_PERCENTAGE);
		PlaythroughLog.note(helper, SCENARIO, "sleep: " + status.amountSleeping() + " of " + players.size()
				+ " asleep at " + percent + "%: " + status.sleepersNeeded(percent) + " needed, night passes="
				+ status.areEnoughSleeping(percent));
		return status.areEnoughSleeping(percent);
	}

	/** The night skip as the server does it: morning, then every sleeper woken with {@code stopSleepInBed(false, false)}. */
	private static void skipNight(GameTestHelper helper, PlaythroughPlayer sleeper) {
		ServerLevel level = helper.getLevel();
		long now = level.getDayTime();
		level.setDayTime(now - Math.floorMod(now, 24000L) + 24000L);
		level.updateSkyBrightness();
		sleeper.player().stopSleepInBed(false, false);
	}

	// =================================================================================================================
	// Review follow-ups: the carried vial, the early spin, a full pack, and the charm off while pending
	// =================================================================================================================

	/**
	 * A player who already carries the Concentrated vial (the first one comes with the distillation) is not pointed at
	 * the Alchemist: the card is not NOTICE and asking again says they already carry it and gives nothing.
	 */
	// FIX: F-12 the Concentrated Blood card is NOTICE only while the vial can actually be issued.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void carriedConcentratedVialDropsTheSigil(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		player.give(ConcentratedBlood.create()); // ARRANGE: the vial the distillation left in the pack
		DialogueView view = player.approach(alchemist).talkTo(alchemist);
		keys(helper, view, "the Alchemist to a player who carries the vial");
		DialogueTopic card = view.topic(REPLACE).orElse(null);
		helper.assertTrue(card != null, "the Concentrated Blood card is still there to read: " + view.describe());
		PlaythroughLog.note(helper, SCENARIO, "carried vial: card " + card.category() + "/" + card.state() + "/"
				+ card.attention());
		helper.assertTrue(card.attention() != DialogueAttention.NOTICE,
				"a player who carries the vial is not called back for it: " + card.attention());
		Outcome again = player.choose(alchemist, REPLACE);
		again.said("hemomancy.initiation.blood_already_carried");
		helper.assertTrue(concentrated(player) == 1, "no second vial is given; " + again);
		helper.succeed();
	}

	/**
	 * A Neophyte who spins two samples at a centrifuge before any brief does not start First Separation: the
	 * advancement says "your assigned separation at Degree 2", and the ledger card stays unchecked.
	 */
	// FIX: StartCentrifugeButtonPacket granted first_separation_started at Degree 1; it now needs Degree 2 and the brief.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void d1SpinDoesNotStartFirstSeparation(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		Entity first = mob(helper, EntityType.COW, COW_A);
		Entity second = mob(helper, EntityType.COW, COW_B);
		BlockPos centrifuge = at(helper, CENTRIFUGE);
		craftedStation(player, BlockInit.vial_centrifuge.get()); // ARRANGE: formed by this player
		player.give(new ItemStack(BlockInit.vial_centrifuge.get()));
		player.give(new ItemStack(ItemInit.bloody_vial.get(), 2)); // ARRANGE: two empty vials from a chest
		player.place(centrifuge, BlockInit.vial_centrifuge.get());
		helper.assertTrue(level.getBlockEntity(centrifuge) instanceof VialCentrifugeBlockEntity, "the centrifuge is placed");
		for (Entity cow : List.of(first, second)) {
			wieldMatching(player, PlaythroughVotaryGameTests::emptyVial, "an empty vial");
			player.approach(cow);
			player.attack(cow);
		}
		player.moveTo(at(helper, HOME));
		int samples = count(player, stack -> sampleOf(stack, EntityType.COW));
		PlaythroughLog.note(helper, SCENARIO, "a D1 player filled " + samples + " cow samples");
		if (samples >= 2) {
			VialCentrifugeMenu menu = openStation(player, centrifuge, VialCentrifugeMenu.class);
			putInto(player, menu, stack -> sampleOf(stack, EntityType.COW), VialCentrifugeMenu.VIAL_START, "a sample");
			putInto(player, menu, stack -> sampleOf(stack, EntityType.COW), VialCentrifugeMenu.VIAL_START + 4, "a sample");
			Outcome.Probe probe = Outcome.begin(player, null, "press start at D1");
			var result = StartCentrifugeButtonPacket.start(player.player(), menu.getTe());
			Outcome start = probe.finish();
			PlaythroughLog.note(helper, SCENARIO, "D1 spin: " + result + "; " + start.describe());
			player.closeMenu();
		}
		helper.assertFalse(player.has(HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED.getPath()),
				"a D1 spin before the brief does not start First Separation");
		FirstSeparationLedgerProgress card = readLedger(player);
		PlaythroughLog.note(helper, SCENARIO, "D1 ledger after the spin: " + card);
		helper.succeed();
	}

	/**
	 * The First Separation claim with every slot full: the tools are owed, not lost. Once space is made they arrive
	 * on the next ticks, exactly once.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = CLOCK + "full_claim")
	public static void firstSeparationClaimWithFullPack(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		firstSeparationDone(player); // ARRANGE: briefed, spun and recovered
		PlaythroughClock.start(helper)
				.then(() -> {
					player.approach(alchemist).talkTo(alchemist);
					player.fillInventory(); // ARRANGE: every slot full
					Outcome claim = player.choose(alchemist, CLAIM);
					PlaythroughLog.note(helper, SCENARIO, "full-pack claim: " + claim.describe());
					helper.assertTrue(FirstSeparationAssignment.isClaimed(player.player()), "the claim is recorded; " + claim);
					helper.assertTrue(player.count(ItemInit.living_syringe.get()) == 0
							&& claim.dropped(ItemInit.living_syringe.get(), Outcome.Near.ANY) == 0,
							"with no room the syringe is owed, not dropped; " + claim);
					clearSlots(player, 4); // ARRANGE: the player makes room
				})
				.step(player.tickPlayer(40))
				.then(() -> {
					helper.assertTrue(player.count(ItemInit.living_syringe.get()) == 1
							&& player.count(ItemInit.vial_rack.get()) == 1,
							"the owed tools arrive once there is room: syringe " + player.count(ItemInit.living_syringe.get())
									+ ", rack " + player.count(ItemInit.vial_rack.get()));
				})
				.step(player.tickPlayer(40))
				.then(() -> helper.assertTrue(player.count(ItemInit.living_syringe.get()) == 1
						&& player.count(ItemInit.vial_rack.get()) == 1, "the tools arrive exactly once"))
				.run();
	}

	/** The same owed tools survive a death between the full-pack claim and the space being made. */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = CLOCK + "full_claim_death")
	public static void firstSeparationOwedToolsSurviveDeath(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		firstSeparationDone(player); // ARRANGE
		PlaythroughClock.start(helper)
				.then(() -> {
					player.approach(alchemist).talkTo(alchemist);
					player.fillInventory(); // ARRANGE
					player.choose(alchemist, CLAIM);
					helper.assertTrue(player.count(ItemInit.living_syringe.get()) == 0, "the tools are owed");
					player.die();
					player.respawn();
				})
				.step(player.tickPlayer(40))
				.then(() -> {
					helper.assertTrue(player.count(ItemInit.living_syringe.get()) == 1
							&& player.count(ItemInit.vial_rack.get()) == 1,
							"after a death the owed tools arrive in the empty respawned pack: syringe "
									+ player.count(ItemInit.living_syringe.get()) + ", rack " + player.count(ItemInit.vial_rack.get()));
				})
				.run();
	}

	/**
	 * Creative with every slot full: the claim's tools must not count as delivered when Creative discards them.
	 * Back in survival with room, they arrive.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = CLOCK + "creative_claim")
	public static void creativeFullPackDoesNotSwallowOwedTools(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		firstSeparationDone(player); // ARRANGE
		PlaythroughClock.start(helper)
				.then(() -> {
					player.approach(alchemist).talkTo(alchemist);
					player.creative().fillInventory(); // ARRANGE
					Outcome claim = player.choose(alchemist, CLAIM);
					PlaythroughLog.note(helper, SCENARIO, "creative full-pack claim: " + claim.describe());
				})
				.step(player.tickPlayer(20))
				.then(() -> {
					player.survival();
					clearSlots(player, 4); // ARRANGE
				})
				.step(player.tickPlayer(40))
				.then(() -> helper.assertTrue(player.count(ItemInit.living_syringe.get()) == 1
						&& player.count(ItemInit.vial_rack.get()) == 1,
						"tools Creative discarded are still owed and arrive in survival: syringe "
								+ player.count(ItemInit.living_syringe.get()) + ", rack " + player.count(ItemInit.vial_rack.get())))
				.run();
	}

	/** Asking for Concentrated Blood with every slot full drops exactly one vial at the player's feet. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void concentratedAskWithFullPackDropsOnce(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		player.approach(alchemist).talkTo(alchemist);
		player.fillInventory(); // ARRANGE
		Outcome asked = player.choose(alchemist, REPLACE);
		long dropped = asked.drops().stream().filter(drop -> ConcentratedBlood.is(drop.stack())).count();
		PlaythroughLog.note(helper, SCENARIO, "full-pack Concentrated ask: " + asked.describe());
		helper.assertTrue(dropped == 1, "one Concentrated vial lands at the player's feet; " + asked);
		helper.succeed();
	}

	/** The vial lying at the player's feet is not reissued while it lies there. */
	// DECISION: D-19 reissue checks look only at the carried inventory.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void droppedConcentratedVialIsReissued(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		player.approach(alchemist).talkTo(alchemist);
		player.fillInventory(); // ARRANGE
		player.choose(alchemist, REPLACE);
		clearSlots(player, 2); // ARRANGE: room made, the dropped vial still on the ground
		player.talkTo(alchemist);
		Outcome again = player.choose(alchemist, REPLACE);
		PlaythroughLog.note(helper, SCENARIO, "D-19 ask again while the vial lies on the ground: " + again.describe());
		helper.assertTrue(concentrated(player) == 0, "D-19 expected: no second vial while the first lies on the ground");
		helper.succeed();
	}

	/** Body Answers' supplies with every slot full: each lands at the player's feet; nothing is lost. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void bodyAnswersSuppliesWithFullPack(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		firstSeparationClaimed(player); // ARRANGE: Body Answers opens after the claim
		DialogueView view = player.approach(alchemist).talkTo(alchemist);
		helper.assertTrue(view.offers(BODY_ANSWERS), "Body Answers is offered after the claim: " + view.describe());
		player.fillInventory(); // ARRANGE
		Outcome brief = player.choose(alchemist, BODY_ANSWERS);
		PlaythroughLog.note(helper, SCENARIO, "full-pack Body Answers: " + brief.describe());
		for (ItemLike item : List.of(ItemInit.sanguine_formation.get(), ItemInit.fervent_enzyme.get(), ItemInit.bloody_flask.get())) {
			helper.assertTrue(brief.dropped(item, Outcome.Near.ANY) == 1, "the " + item.asItem() + " lands at the feet; " + brief);
		}
		helper.succeed();
	}

	/** The Specimen Jar lesson's two jars with every slot full land at the player's feet. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void specimenJarsWithFullPack(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.votaryD2(player); // ARRANGE
		DialogueView view = player.approach(alchemist).talkTo(alchemist);
		String teach = com.vincenthuto.hemomancy.common.entity.npc.dialogue.SpecimenJarLessonDialogue.TEACH;
		helper.assertTrue(view.offers(teach), "the D2 Alchemist offers the Specimen Jar lesson: " + view.describe());
		player.fillInventory(); // ARRANGE
		Outcome taught = player.choose(alchemist, teach);
		PlaythroughLog.note(helper, SCENARIO, "full-pack Specimen Jar lesson: " + taught.describe());
		helper.assertTrue(taught.dropped(BlockInit.specimen_jar.get(), Outcome.Near.ANY) == 2,
				"both jars land at the feet; " + taught);
		helper.succeed();
	}

	/**
	 * A full pack and a held stack of 64 formations: the collect at a Sanguine Condenser has nowhere to put the
	 * formation. It stays in the condenser and the player is told why, not left in silence.
	 */
	// FIX: F-13 style: SanguineCondenserBlockEntity#collect refused a full pack in silence; it now says no_room.
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = CLOCK + "condenser_full")
	public static void condenserCollectWithFullPackExplains(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		BlockPos cauldron = at(helper, CHAIN_CAULDRON);
		setBlock(helper, cauldron, Blocks.CAULDRON.defaultBlockState());
		player.give(new ItemStack(ItemInit.blood_projection.get())); // ARRANGE: the conjured hand, from the cast
		PlaythroughClock.start(helper)
				.then(() -> {
					PlaythroughStates.blood(player, maxBlood(player)); // ARRANGE
					player.moveTo(cauldron.west(2));
				})
				.step(condenseFormation(player, cauldron))
				.then(() -> {
					int before = held(helper.getLevel(), cauldron);
					helper.assertTrue(before > 0, "a formation waits in the condenser");
					var inventory = player.player().getInventory();
					player.fillInventory(); // ARRANGE
					inventory.items.set(0, new ItemStack(ItemInit.sanguine_formation.get(), 64)); // ARRANGE: a full stack held
					inventory.selected = 0;
					Outcome collect = player.useOn(cauldron);
					PlaythroughLog.note(helper, SCENARIO, "full-pack collect: " + collect.describe());
					helper.assertTrue(held(helper.getLevel(), cauldron) == before, "the formation stays in the condenser");
					collect.said("message.hemomancy.sanguine_condenser.no_room");
				})
				.run();
	}

	/**
	 * The charm taken off while a Concentrated injection is pending. The topic is hidden and the night's rest refuses
	 * without the charm. Expected: something tells the player why they did not wake an Initiate.
	 */
	// DECISION: D-26 charm removed while Concentrated Blood is pending leaves the player at D2 with no explanation.
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = CLOCK + "charm_off_pending", required = false)
	public static void charmRemovedWhilePending(GameTestHelper helper) {
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, at(helper, ALCHEMIST));
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, at(helper, VICAR));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		readyForConcentrated(player); // ARRANGE
		helper.assertTrue(ConcentratedBlood.inject(player.player()), "ARRANGE: the vial is injected");
		HemoCapabilityAccess.getEquipment(player.player()).setStackInSlot(5, ItemStack.EMPTY); // ARRANGE: through the Vanity
		BlockPos bed = bed(helper, at(helper, BED_FOOT));
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					mark[0] = player.mark();
					player.moveTo(bed.west());
					player.sleepThroughNight(bed);
				})
				.then(() -> {
					DialogueView atAlchemist = player.approach(alchemist).talkTo(alchemist);
					DialogueView atVicar = player.approach(vicar).talkTo(vicar);
					List<Outcome.ChatLine> said = player.chatSince(mark[0]);
					PlaythroughLog.note(helper, SCENARIO, "D-26 charm off while pending: degree " + player.degree()
							+ ", pending " + ConcentratedBlood.pending(player.player()) + ", said " + said
							+ "; Alchemist " + atAlchemist.describe() + "; Vicar " + atVicar.describe());
					boolean explained = said.stream().anyMatch(line -> line.keys().stream()
							.anyMatch(key -> key.contains("charm") || key.contains("concentrated")))
							|| atAlchemist.lineKeys().stream().anyMatch(key -> key.contains("charm"))
							|| atVicar.lineKeys().stream().anyMatch(key -> key.contains("charm"));
					helper.assertTrue(player.degree() == 3 || explained,
							"D-26 expected: the player settles or is told the charm is missing; they stay D2 with no line");
				})
				.run();
	}

	/**
	 * Blood the player projects into their own cauldron and then absorbs back out counts toward the 500 mL First
	 * Absorption proof, so a project-then-absorb loop meets it with no creature. Logs the count and the ticks it took.
	 */
	// DECISION: D-1b absorbing the player's own projected blood back out of their condenser counts as First Absorption.
	@GameTest(template = EMPTY, timeoutTicks = 3000, batch = CLOCK + "round_trip", required = false)
	public static void condenserRoundTripCountsAsAbsorption(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at(helper, HOME));
		PlaythroughStates.neophyteD1(player); // ARRANGE
		BlockPos cauldron = at(helper, CHAIN_CAULDRON);
		setBlock(helper, cauldron, Blocks.CAULDRON.defaultBlockState());
		player.give(new ItemStack(ItemInit.blood_projection.get())); // ARRANGE: the conjured hands
		player.give(new ItemStack(ItemInit.blood_absorption.get())); // ARRANGE
		long[] start = new long[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					PlaythroughStates.blood(player, maxBlood(player)); // ARRANGE
					player.moveTo(cauldron.west(2));
					start[0] = helper.getTick();
				})
				.step(absorbFromOwnCauldron(player, cauldron))
				.then(() -> {
					double absorbed = bloodcraft(player).absorbedMl();
					PlaythroughLog.note(helper, SCENARIO, "D-1b round trip: absorbedMl=" + absorbed + " after "
							+ (helper.getTick() - start[0]) + " ticks, with no creature");
					helper.assertTrue(absorbed < PROOF_ML,
							"D-1b expected (if own blood should not count): the round trip does not meet the proof; got "
									+ absorbed);
				})
				.run();
	}

	// =================================================================================================================
	// Band-private actions
	// =================================================================================================================

	private static BlockPos at(GameTestHelper helper, int[] spot) {
		return PlaythroughSites.stand(helper, spot[0], spot[1]);
	}

	/** The Alchemist's First Separation brief, chosen the way a player chooses it. */
	private static void brief(PlaythroughPlayer player, Entity alchemist) {
		DialogueView view = player.approach(alchemist).talkTo(alchemist);
		player.helper().assertTrue(view.offers(BRIEF), "First Separation is offered: " + view.describe());
		player.choose(alchemist, BRIEF).gave(ItemInit.bloody_vial.get(), 2);
	}

	/** Asks the Alchemist for Concentrated Blood through the offered option; one vial arrives with the rest line. */
	private static Outcome askForConcentrated(PlaythroughPlayer player, Entity alchemist) {
		DialogueView view = player.approach(alchemist).talkTo(alchemist);
		player.helper().assertTrue(view.offers(REPLACE), "Concentrated Blood is offered once both proofs are in: "
				+ view.describe());
		int before = concentrated(player);
		Outcome asked = player.choose(alchemist, REPLACE);
		player.helper().assertTrue(concentrated(player) == before + 1, "asking gives one vial of Concentrated Blood; "
				+ asked);
		asked.said(BLOOD_REST);
		return asked;
	}

	/** Fills an empty vial from {@code mob} by hitting it with the vial in hand. */
	private static void sample(PlaythroughPlayer player, Entity mob) {
		wieldMatching(player, PlaythroughVotaryGameTests::emptyVial, "an empty vial");
		Outcome hit = player.attack(mob);
		ItemStack held = player.player().getMainHandItem();
		player.helper().assertTrue(BloodSampleData.isFilled(held) && BloodSampleData.entityType(held) == mob.getType(),
				"hitting " + PlaythroughPlayer.describe(mob) + " with an empty vial fills it; " + hit);
	}

	/** Sets two samples of {@code source} in opposite slots (top and bottom) by click, then presses start. */
	private static Outcome loadAndSpin(PlaythroughPlayer player, VialCentrifugeMenu menu, EntityType<?> source) {
		putInto(player, menu, stack -> sampleOf(stack, source), VialCentrifugeMenu.VIAL_START, "a sample");
		putInto(player, menu, stack -> sampleOf(stack, source), VialCentrifugeMenu.VIAL_START + 4, "a second sample");
		Outcome.Probe probe = Outcome.begin(player, null, "press the centrifuge's start button");
		StartCentrifugeButtonPacket.handle(new StartCentrifugeButtonPacket(), PayloadContexts.of(player.player()));
		Outcome start = probe.finish();
		player.helper().assertTrue(menu.getTe().isProcessing(), "the balanced load spins; " + start);
		return start;
	}

	/** Takes the first centrifuge output by hand; fails when there is none. */
	private static Outcome takeOutput(PlaythroughPlayer player, VialCentrifugeMenu menu) {
		for (int i = 0; i < 8; i++) {
			int slot = VialCentrifugeMenu.OUTPUT_START + i;
			if (menu.getSlot(slot).hasItem()) return takeOut(player, menu, slot);
		}
		player.helper().fail("the centrifuge shows no output to take");
		return player.last();
	}

	/** Takes the first centrifuge output by hand when there is one; an empty outcome otherwise. */
	private static Outcome takeOutputIfAny(PlaythroughPlayer player, VialCentrifugeMenu menu) {
		for (int i = 0; i < 8; i++) {
			int slot = VialCentrifugeMenu.OUTPUT_START + i;
			if (menu.getSlot(slot).hasItem()) return takeOut(player, menu, slot);
		}
		return Outcome.begin(player, null, "find no centrifuge output").note("the outputs were empty").finish();
	}

	/** Right-clicks the station's side that faces the player and returns the menu it opened. */
	private static <M extends AbstractContainerMenu> M openStation(PlaythroughPlayer player, BlockPos pos, Class<M> type) {
		Outcome used = player.useOn(pos, faceToward(player, pos));
		AbstractContainerMenu open = player.player().containerMenu;
		player.helper().assertTrue(type.isInstance(open), "right-clicking the station at " + pos.toShortString()
				+ " opens its menu; " + used);
		return type.cast(open);
	}

	private static Direction faceToward(PlaythroughPlayer player, BlockPos pos) {
		Vec3 offset = player.player().position().subtract(Vec3.atCenterOf(pos));
		return Direction.getNearest((float) offset.x, 0.0F, (float) offset.z);
	}

	/** Picks a matching stack up from the inventory by click and drops it into {@code target}, returning any rest. */
	private static void putInto(PlaythroughPlayer player, AbstractContainerMenu menu, Predicate<ItemStack> match,
			int target, String what) {
		ServerPlayer server = player.player();
		int slot = inventorySlot(server, match);
		player.helper().assertTrue(slot >= 0, "the player carries " + what);
		int from = menuSlotOf(menu, server, slot);
		player.helper().assertTrue(from >= 0, "the menu shows inventory slot " + slot);
		player.click(menu, from, 0, ClickType.PICKUP);
		player.click(menu, target, 0, ClickType.PICKUP);
		if (!menu.getCarried().isEmpty()) player.click(menu, from, 0, ClickType.PICKUP);
		player.helper().assertTrue(menu.getSlot(target).hasItem(), what + " went into menu slot " + target);
	}

	/** Picks {@code menuSlot} up by click (its {@code onTake} runs) and puts it down in an empty inventory slot. */
	private static Outcome takeOut(PlaythroughPlayer player, AbstractContainerMenu menu, int menuSlot) {
		ServerPlayer server = player.player();
		Outcome.Probe probe = Outcome.begin(player, null, "take menu slot " + menuSlot);
		player.click(menu, menuSlot, 0, ClickType.PICKUP);
		int free = server.getInventory().getFreeSlot();
		int to = free < 0 ? -1 : menuSlotOf(menu, server, free);
		if (to >= 0) player.click(menu, to, 0, ClickType.PICKUP);
		else probe.note("no free inventory slot; the stack stays on the cursor");
		return probe.finish();
	}

	private static int menuSlotOf(AbstractContainerMenu menu, ServerPlayer player, int inventorySlot) {
		for (int i = 0; i < menu.slots.size(); i++) {
			Slot slot = menu.slots.get(i);
			if (slot.container == player.getInventory() && slot.getContainerSlot() == inventorySlot) return i;
		}
		return -1;
	}

	private static int inventorySlot(ServerPlayer player, Predicate<ItemStack> match) {
		Inventory inventory = player.getInventory();
		for (int slot = 0; slot < inventory.items.size(); slot++) {
			if (match.test(inventory.items.get(slot))) return slot;
		}
		return -1;
	}

	/** Uses the assignment ledger and reads the First Separation card it sent. */
	/** ARRANGE: empties {@code n} of the filler slots {@code fillInventory} left, as a player makes room. */
	private static void clearSlots(PlaythroughPlayer player, int n) {
		var items = player.player().getInventory().items;
		int cleared = 0;
		for (int slot = 0; slot < items.size() && cleared < n; slot++) {
			if (items.get(slot).is(Items.SADDLE)) {
				items.set(slot, ItemStack.EMPTY);
				cleared++;
			}
		}
	}

	private static FirstSeparationLedgerProgress readLedger(PlaythroughPlayer player) {
		player.wield(ItemInit.harbinger_assignment_ledger.get());
		Outcome read = player.use(InteractionHand.MAIN_HAND);
		List<OpenHarbingerAssignmentLedgerPacket> sent = read.sent(OpenHarbingerAssignmentLedgerPacket.class);
		player.helper().assertTrue(!sent.isEmpty(), "using the ledger opens it; " + read);
		return sent.getLast().firstSeparation();
	}

	/** Brings a matching stack into the main hand from the hotbar, the inventory or the offhand. */
	private static ItemStack wieldMatching(PlaythroughPlayer player, Predicate<ItemStack> match, String what) {
		ServerPlayer server = player.player();
		Inventory inventory = server.getInventory();
		if (match.test(server.getMainHandItem())) return server.getMainHandItem();
		for (int slot = 0; slot < inventory.items.size(); slot++) {
			if (!match.test(inventory.items.get(slot))) continue;
			if (slot < Inventory.getSelectionSize()) {
				inventory.selected = slot;
			} else {
				ItemStack held = inventory.getSelected();
				inventory.items.set(inventory.selected, inventory.items.get(slot));
				inventory.items.set(slot, held);
			}
			return server.getMainHandItem();
		}
		if (match.test(server.getOffhandItem())) {
			ItemStack off = server.getOffhandItem();
			server.setItemInHand(InteractionHand.OFF_HAND, server.getMainHandItem());
			server.setItemInHand(InteractionHand.MAIN_HAND, off);
			return off;
		}
		player.helper().fail("the player has no " + what + " to hold");
		return ItemStack.EMPTY;
	}

	/** Swaps a matching stack into the offhand (the swap-hands key), putting the old offhand where it was. */
	private static void toOffhand(PlaythroughPlayer player, Predicate<ItemStack> match, String what) {
		ServerPlayer server = player.player();
		Inventory inventory = server.getInventory();
		if (match.test(server.getOffhandItem())) return;
		for (int slot = 0; slot < inventory.items.size(); slot++) {
			ItemStack stack = inventory.items.get(slot);
			if (!match.test(stack)) continue;
			inventory.items.set(slot, server.getOffhandItem());
			server.setItemInHand(InteractionHand.OFF_HAND, stack);
			return;
		}
		player.helper().fail("the player has no " + what + " to move to the offhand");
	}

	/** Selects an empty hotbar slot, or moves the held stack into a free slot. */
	private static void emptyMainHand(PlaythroughPlayer player) {
		Inventory inventory = player.player().getInventory();
		if (inventory.getSelected().isEmpty()) return;
		for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
			if (inventory.items.get(slot).isEmpty()) {
				inventory.selected = slot;
				return;
			}
		}
		int free = inventory.getFreeSlot();
		player.helper().assertTrue(free >= 0, "the player has room to empty a hand");
		inventory.items.set(free, inventory.getSelected());
		inventory.items.set(inventory.selected, ItemStack.EMPTY);
	}

	private static void wieldConcentrated(PlaythroughPlayer player) {
		wieldMatching(player, ConcentratedBlood::is, "vial of Concentrated Blood");
	}

	/** A point two blocks ahead of the player's eyes, for holds that aim at nothing in particular. */
	private static Vec3 ahead(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		return server.getEyePosition().add(server.getLookAngle().scale(2.0D));
	}

	/** Lies down in the bed without the night passing; fails when the bed refuses. */
	private static void lieDown(PlaythroughPlayer player, BlockPos bed) {
		var result = player.player().startSleepInBed(bed);
		player.helper().assertTrue(result.right().isPresent(), "the bed takes the player: " + result.left().orElse(null));
	}

	// --- ARRANGE helpers -----------------------------------------------------------------------------------------

	/** ARRANGE: the player has crafted one {@code block}, the credit the station access gate wants. */
	private static void craftedStation(PlaythroughPlayer player, Block block) {
		MachineAccessEvents.awardMachineCrafted(player.player(), block);
	}

	/** ARRANGE: D2, briefed with the two vials, the separation started and recovered; not yet claimed. */
	private static void firstSeparationDone(PlaythroughPlayer player) {
		ServerPlayer server = player.player();
		if (player.degree() < 2) PlaythroughStates.votaryD2(player);
		FirstSeparationAssignment.markBriefed(server);
		FirstSeparationAssignment.giveBriefingSupplies(server);
		HarbingerAdvancementGranter.grantIfNotDone(server, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_STARTED);
		HarbingerAdvancementGranter.grantIfNotDone(server, HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE);
	}

	/** ARRANGE: {@link #firstSeparationDone} and the claim, its tools delivered. */
	private static void firstSeparationClaimed(PlaythroughPlayer player) {
		firstSeparationDone(player);
		if (!FirstSeparationAssignment.isClaimed(player.player())) {
			player.helper().assertTrue(FirstSeparationAssignment.claimRewards(player.player()), "First Separation is claimed");
		}
	}

	/** ARRANGE: {@link #firstSeparationClaimed} and the first distillation taken, so Concentrated Blood is ready. */
	private static void readyForConcentrated(PlaythroughPlayer player) {
		firstSeparationClaimed(player);
		HemoCapabilityAccess.stationUpgrades(player.player()).recordUse(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL);
	}

	/** ARRANGE: a bed with its foot at {@code foot} and its head one block east; returns the head. */
	private static BlockPos bed(GameTestHelper helper, BlockPos foot) {
		ServerLevel level = helper.getLevel();
		BlockPos head = foot.east();
		BlockState base = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.EAST);
		level.setBlock(foot, base.setValue(BedBlock.PART, BedPart.FOOT), Block.UPDATE_ALL);
		level.setBlock(head, base.setValue(BedBlock.PART, BedPart.HEAD), Block.UPDATE_ALL);
		PlaythroughCleanup.onEnd(helper, () -> {
			level.setBlock(head, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
			level.setBlock(foot, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		});
		return head;
	}

	/** Midnight today; the next morning comes back when the test ends. Clock batches only. */
	private static void night(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		long day = level.getDayTime();
		level.setDayTime(day - Math.floorMod(day, 24000L) + 18000L);
		PlaythroughCleanup.onEnd(helper, () -> {
			long now = level.getDayTime();
			if (Math.floorMod(now, 24000L) >= 12000L) level.setDayTime(now - Math.floorMod(now, 24000L) + 25000L);
		});
	}

	/** ARRANGE: a block placed by fixture, cleared (its contents emptied first) when the test ends. */
	private static void setBlock(GameTestHelper helper, BlockPos pos, BlockState state) {
		ServerLevel level = helper.getLevel();
		helper.assertTrue(level.setBlock(pos, state, Block.UPDATE_ALL), "placed " + state.getBlock() + " at " + pos.toShortString());
		PlaythroughCleanup.onEnd(helper, () -> {
			if (level.getBlockEntity(pos) instanceof Container container) container.clearContent();
			level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		});
	}

	/** ARRANGE: the Hematic Iron Block pattern on the floor, an iron block ringed by eight Befouling Ash Trails. */
	private static void layHematicIronPattern(GameTestHelper helper, BlockPos centre) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				BlockState state = x == 0 && z == 0 ? Blocks.IRON_BLOCK.defaultBlockState()
						: BlockInit.befouling_ash_trail.get().defaultBlockState();
				setBlock(helper, centre.offset(x, 0, z), state);
			}
		}
	}

	/** ARRANGE: the player puts a matching stack in the chest. */
	private static void stash(PlaythroughPlayer player, BlockPos chest, Predicate<ItemStack> match) {
		ServerPlayer server = player.player();
		int slot = inventorySlot(server, match);
		player.helper().assertTrue(slot >= 0, "there is something to stash");
		ChestBlockEntity box = (ChestBlockEntity) server.serverLevel().getBlockEntity(chest);
		ItemStack stack = server.getInventory().items.get(slot);
		server.getInventory().items.set(slot, ItemStack.EMPTY);
		for (int i = 0; i < box.getContainerSize(); i++) {
			if (box.getItem(i).isEmpty()) {
				box.setItem(i, stack);
				return;
			}
		}
		player.helper().fail("the chest is full");
	}

	/** ARRANGE: the player takes everything back out of the chest. */
	private static void unstash(PlaythroughPlayer player, BlockPos chest) {
		ChestBlockEntity box = (ChestBlockEntity) player.player().serverLevel().getBlockEntity(chest);
		for (int i = 0; i < box.getContainerSize(); i++) {
			ItemStack stack = box.removeItemNoUpdate(i);
			if (!stack.isEmpty()) player.give(stack);
		}
	}

	private static <E extends Entity> E mob(GameTestHelper helper, EntityType<E> type, int[] spot) {
		E entity = helper.spawn(type, new BlockPos(spot[0], PlaythroughSites.STAND_Y, spot[1]));
		PlaythroughCleanup.onEnd(helper, entity::discard);
		return entity;
	}

	// --- Reading -------------------------------------------------------------------------------------------------

	private static void keys(GameTestHelper helper, DialogueView view, String label) {
		view.assertLangKeys(helper, label).assertNoDeadEnds(helper, label);
	}

	/** Whether the Alchemist shows anything of Concentrated Blood: the option, its card or its node. */
	private static boolean concentratedShown(DialogueView view) {
		return view.offers(REPLACE) || view.topic(REPLACE).isPresent() || view.hasNode(CONCENTRATED_NODE);
	}

	private static List<String> startLines(DialogueView view) {
		return view.node(view.tree().startNodeId()).map(node -> node.lines().stream().map(Dialogue::lineKey)
				.flatMap(Optional::stream).toList()).orElse(List.of());
	}

	/** The line the Alchemist's "next study" card shows, or a note that there is none. */
	private static String nextStudy(DialogueView view) {
		return view.node(CLINICAL_NEXT_NODE).flatMap(node -> node.lines().stream().findFirst())
				.flatMap(Dialogue::lineKey).orElse("<no next-study card>");
	}

	private static void assertLesson(GameTestHelper helper, PlaythroughPlayer player, String lesson, String recipe) {
		helper.assertTrue(player.player().getRecipeBook().contains(com.vincenthuto.hemomancy.Hemomancy.rloc(recipe)),
				"the " + lesson + " lesson teaches the " + recipe + " recipe");
		helper.assertTrue(LiberKnowledgeHelperBridge.hasEntry(player, lesson), "the " + lesson + " Liber page unlocks");
	}

	private static String clinical(String key) {
		return com.vincenthuto.hemomancy.Hemomancy.rloc("hemomancy/clinical_" + key).toString();
	}

	private static String clinicalPath(String key) {
		return "hemomancy/clinical_" + key;
	}

	private static FirstBloodcraftLedgerProgress bloodcraft(PlaythroughPlayer player) {
		return FirstBloodcraftAssignment.progress(player.player());
	}

	private static double maxBlood(PlaythroughPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player.player()).getMaxBloodVolume();
	}

	private static VialCentrifugeBlockEntity centrifuge(ServerLevel level, BlockPos pos) {
		return (VialCentrifugeBlockEntity) level.getBlockEntity(pos);
	}

	private static boolean emptyVial(ItemStack stack) {
		return stack.is(ItemInit.bloody_vial.get()) && !BloodSampleData.isFilled(stack);
	}

	private static boolean sampleOf(ItemStack stack, EntityType<?> source) {
		return stack.is(ItemInit.bloody_vial.get()) && BloodSampleData.isFilled(stack)
				&& BloodSampleData.entityType(stack) == source;
	}

	/** How many matching items the player holds anywhere in the inventory or on the cursor. */
	private static int count(PlaythroughPlayer player, Predicate<ItemStack> match) {
		ServerPlayer server = player.player();
		int total = 0;
		for (int slot = 0; slot < server.getInventory().getContainerSize(); slot++) {
			ItemStack stack = server.getInventory().getItem(slot);
			if (match.test(stack)) total += stack.getCount();
		}
		ItemStack carried = server.containerMenu.getCarried();
		return total + (match.test(carried) ? carried.getCount() : 0);
	}

	private static int concentrated(PlaythroughPlayer player) {
		return count(player, ConcentratedBlood::is);
	}

	private static ItemStack find(PlaythroughPlayer player, Predicate<ItemStack> match) {
		ServerPlayer server = player.player();
		for (int slot = 0; slot < server.getInventory().getContainerSize(); slot++) {
			ItemStack stack = server.getInventory().getItem(slot);
			if (match.test(stack)) return stack;
		}
		player.helper().fail("the player carries no such item");
		return ItemStack.EMPTY;
	}

	/** Sends a forged event and records every way it reached the player. */
	private static void refuse(List<String> problems, PlaythroughPlayer player, String eventId, int speaker,
			BooleanSupplier untouched) {
		Outcome forged = player.forge(eventId, speaker);
		String label = eventId + " @" + (speaker == 0 ? "entity 0" : "the Vicar");
		PlaythroughLog.note(player.helper(), SCENARIO, "forged " + label + ": " + forged.describe());
		if (forged.event().isPresent()) problems.add(label + " was dispatched");
		if (!untouched.getAsBoolean()) problems.add(label + " changed the player's state");
		if (!forged.earned().isEmpty()) problems.add(label + " earned " + forged.earned());
		if (!forged.drops().isEmpty()) problems.add(label + " dropped " + forged.drops());
		if (forged.inventoryDelta().values().stream().anyMatch(change -> change != 0)) {
			problems.add(label + " changed the inventory " + forged.inventoryDelta());
		}
	}

	/** The shipped English for {@code key}, read once from the classpath {@code en_us.json}. */
	private static synchronized String english(String key) {
		if (english == null) {
			try (InputStream in = PlaythroughVotaryGameTests.class.getResourceAsStream("/assets/hemomancy/lang/en_us.json")) {
				if (in == null) throw new IllegalStateException("en_us.json is not on the classpath");
				JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
				Map<String, String> map = new HashMap<>();
				for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
					if (entry.getValue().isJsonPrimitive()) map.put(entry.getKey(), entry.getValue().getAsString());
				}
				english = map;
			} catch (IOException exception) {
				throw new IllegalStateException("Could not read en_us.json", exception);
			}
		}
		return english.getOrDefault(key, "");
	}

	// --- Chain steps ---------------------------------------------------------------------------------------------

	private static double pool(ServerLevel level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof SanguineCondenserBlockEntity condenser ? condenser.getBloodVolume() : 0.0D;
	}

	private static int held(ServerLevel level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof SanguineCondenserBlockEntity condenser ? condenser.heldFormations() : 0;
	}

	/**
	 * Projects into the cauldron until it pools near its brim, switches the cell hand to Blood Absorption by selecting
	 * it (the selection swaps the held form), and draws the pool back out, until the ledger counts 500 mL.
	 */
	private static PlaythroughClock.Step absorbFromOwnCauldron(PlaythroughPlayer player, BlockPos cauldron) {
		GameTestHelper helper = player.helper();
		double brim = SanguineCondenserRules.poolCapacity(0) - 10.0D;
		return new PlaythroughClock.Step() {
			private int phase;
			private int phaseTicks;
			private int cycles;

			@Override
			public boolean tick(int ticks) {
				ServerLevel level = player.player().serverLevel();
				Vec3 aim = Vec3.atCenterOf(cauldron);
				if (bloodcraft(player).absorbedMl() >= PROOF_ML) {
					if (player.player().isUsingItem()) player.releaseHold();
					PlaythroughLog.note(helper, SCENARIO, "absorbed " + bloodcraft(player).absorbedMl() + " mL back from"
							+ " the player's own cauldron in " + (cycles + 1) + " cycles, " + ticks + " ticks");
					return true;
				}
				if (cycles > 8) helper.fail("500 mL was not counted after " + cycles + " project-and-absorb cycles: "
						+ bloodcraft(player) + ", pool " + pool(level, cauldron));
				switch (phase) {
					case 0 -> {
						player.select(PROJECTION);
						player.startHold(ItemInit.blood_projection.get(), aim);
						phase = 1;
						phaseTicks = 0;
					}
					case 1 -> {
						player.aim(aim);
						player.holdTick();
						if (pool(level, cauldron) >= brim || ++phaseTicks > 80 || !player.player().isUsingItem()) {
							player.releaseHold();
							phase = 2;
						}
					}
					case 2 -> {
						player.select(ABSORPTION);
						player.startHold(ItemInit.blood_absorption.get(), aim);
						phase = 3;
						phaseTicks = 0;
					}
					default -> {
						player.aim(aim);
						player.holdTick();
						if (pool(level, cauldron) < 1.0D || ++phaseTicks > 40 || !player.player().isUsingItem()) {
							player.releaseHold();
							phase = 0;
							cycles++;
						}
					}
				}
				return false;
			}

			@Override
			public String label() {
				return "absorb 500 mL back from the cauldron at " + cauldron.toShortString();
			}
		};
	}

	/** Projects one formation's worth into the cauldron and waits for it to condense, refilling after a collapse. */
	private static PlaythroughClock.Step condenseFormation(PlaythroughPlayer player, BlockPos cauldron) {
		GameTestHelper helper = player.helper();
		double cost = SanguineCondenserRules.formationCost(0);
		int cycle = SanguineCondenserRules.ticksPerFormation(0);
		return new PlaythroughClock.Step() {
			private boolean filling;
			private int fillTicks;
			private int waited;
			private int attempts;

			@Override
			public boolean tick(int ticks) {
				ServerLevel level = player.player().serverLevel();
				Vec3 aim = Vec3.atCenterOf(cauldron);
				if (held(level, cauldron) > 0) {
					if (filling) player.releaseHold();
					PlaythroughLog.note(helper, SCENARIO, "a formation condensed after " + attempts + " fill(s), "
							+ ticks + " ticks");
					return true;
				}
				if (!filling && pool(level, cauldron) < cost) {
					helper.assertTrue(++attempts <= 8, "the cauldron condensed no formation in 8 fills");
					player.select(PROJECTION);
					player.startHold(ItemInit.blood_projection.get(), aim);
					filling = true;
					fillTicks = 0;
				}
				if (filling) {
					player.aim(aim);
					player.holdTick();
					if (pool(level, cauldron) >= cost || ++fillTicks > 120 || !player.player().isUsingItem()) {
						player.releaseHold();
						filling = false;
						waited = 0;
					}
					return false;
				}
				if (++waited > cycle * 3) helper.fail("a pooled cauldron (" + pool(level, cauldron) + " mL) made no"
						+ " formation in " + waited + " ticks");
				return false;
			}

			@Override
			public String label() {
				return "condense a formation at " + cauldron.toShortString();
			}
		};
	}

	/** The Liber pages the clinical lessons unlock, read the way the Liber reads them. */
	private static final class LiberKnowledgeHelperBridge {
		private LiberKnowledgeHelperBridge() {
		}

		static boolean hasEntry(PlaythroughPlayer player, String lesson) {
			ResourceLocation entry = ClinicalBloodKnowledge.entry(lesson);
			return com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper
					.hasEntry(player.player(), entry);
		}
	}
}
