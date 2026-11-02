package com.vincenthuto.hemomancy.gametest.playthrough;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueCategory;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueOption;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerAlchemistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerMnemonistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.GhastlyAlembicMenu;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.VialCentrifugeMenu;
import com.vincenthuto.hemomancy.common.mission.alchemist.ClinicalBloodKnowledge;
import com.vincenthuto.hemomancy.common.mission.alchemist.ConcentratedBlood;
import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationAssignment;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.mission.mnemonist.MnemonicReliquaryProgression;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterMilestone;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.network.PlaceStructurePacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.manips.StartCentrifugeButtonPacket;
import com.vincenthuto.hemomancy.common.network.mausoleum.WardStrikePacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine;
import com.vincenthuto.hemomancy.common.rite.harbinger.RiteOfferingEscrow;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.VialCentrifugeStartupResult;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramBoard;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.gametest.LoomWeaveTestSupport;
import com.vincenthuto.hemomancy.gametest.PlaythroughFingerprints;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Band d3_initiate: a Degree 3 Harbinger's road to Degree 4 played as a person plays it. The Mnemonist's Woven
 * Vessel (blank recipe, archive turn-in, kit at the feet), the Somatic Loom raised by its Working craft, the first
 * Blood Shot weave that completes the WOVEN_VESSEL chapter, the Rite of the Sanguine Brotherhood (sealed without the
 * weave, then performed to Degree 4), the clinical Clairaudiograph lesson, the guided Chamber visit and its safety
 * (crafting grid, cursor, logout, lethal damage), the Warp Chair refusal, forged Mnemonist events, the Three Answers
 * inspection line, a death mid-band and the mausoleum's outer-hall ward at Degree 3.
 * <p>
 * Preconditions not under test are set up by fixture and marked {@code // ARRANGE}; every step under test goes
 * through the entry point a real client action reaches. Decision probes are {@code required = false} and say what
 * they observed with {@link PlaythroughLog}.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughInitiateGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_d3_initiate";
	private static final String CLOCK_BATCH = "playthrough_d3_initiate_clock";

	/** Where the outpost residents stand round the player in most tests (relative x, z). */
	private static final int WING_X = 9;
	private static final int WING_Z = 10;
	/** A rite floor's Cardinal Focus, laid into the template's floor layer (relative). */
	private static final BlockPos RITE_FOCUS = new BlockPos(21, 4, 13);
	/** Where a Somatic Loom is set down for a weave (relative), and where its weaver stands. */
	private static final BlockPos LOOM = new BlockPos(24, PlaythroughSites.STAND_Y, 4);
	private static final BlockPos LOOM_WEAVER = new BlockPos(24, PlaythroughSites.STAND_Y, 1);
	private static final double PLENTY_OF_BLOOD = 4000.0D;

	private static final String BLANK_RECIPE = HarbingerMnemonistDialogueTrees.EVENT_BLANK_MEMORY_RECIPE;
	private static final String TURN_IN = HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN;
	private static final String GUIDED = HarbingerMnemonistDialogueTrees.EVENT_GUIDED_CHAMBER;
	private static final String GUIDED_STARTED = "hemomancy.mnemonist.chamber.guided.started";
	private static final String GUIDED_UNAVAILABLE = "hemomancy.mnemonist.chamber.guided.unavailable";
	private static final String VISIT_REMAINING = "hemomancy:chamber_visit_remaining";
	private static final String SEALED = "message.hemomancy.cardinal_rite_activation.the_rank_rite_remains_sealed_complete";

	private static Map<String, String> english;

	private PlaythroughInitiateGameTests() {
	}

	// =============================================================================================================
	// The chain from D2
	// =============================================================================================================

	/**
	 * The real D2 Main replayed from a D2 votary: the First Separation brief, a Centrifuge spin of two cow samples,
	 * the enzyme recovered and reported, a first distillation over heat, the Concentrated Blood asked for, injected
	 * and slept off. The player ends in the state {@link PlaythroughStates#initiateD3} builds by fixture.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 2400, batch = CLOCK_BATCH)
	public static void initiateChainFromPreviousBand(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 6));
		PlaythroughStates.votaryD2(player); // ARRANGE: the D1 claim is the previous band's
		PlaythroughClock clock = PlaythroughClock.start(helper);
		chain(helper, player, clock);
		clock.run();
	}

	/**
	 * The D2 to D3 chain as steps on {@code clock}, for {@link #initiateChainFromPreviousBand} and the full run: sets up
	 * an Alchemist now, then plays the brief, the spin, the claim, the distillation, the Concentrated Blood and the
	 * sleep, and ends comparing the player with {@link PlaythroughStates#initiateD3}. {@code player} must be a fresh
	 * Votary.
	 */
	public static void chain(GameTestHelper helper, PlaythroughPlayer player, PlaythroughClock clock) {
		ServerLevel level = helper.getLevel();
		PlaythroughSites.OutpostWing wing = PlaythroughSites.outpostWing(helper, PlaythroughSites.stand(helper, 8, 6),
				PlaythroughNpcs.Role.ALCHEMIST);
		Entity alchemist = wing.get(PlaythroughNpcs.Role.ALCHEMIST);
		player.moveTo(wing.playerSpot());
		PlaythroughStates.blood(player, 2000.0D); // ARRANGE: a votary's pool, enough for the stations
		BlockPos centrifuge = PlaythroughSites.stand(helper, 10, 10);
		BlockPos alembic = PlaythroughSites.stand(helper, 12, 10);
		BlockPos bed = PlaythroughSites.stand(helper, 4, 6);
		List<Entity> cows = new ArrayList<>();
		VialCentrifugeBlockEntity[] station = new VialCentrifugeBlockEntity[1];
		GhastlyAlembicBlockEntity[] still = new GhastlyAlembicBlockEntity[1];

		clock
				.then(() -> {
					player.talkTo(alchemist);
					Outcome brief = player.choose(alchemist, HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_BRIEF);
					brief.earned(FirstSeparationAssignment.ADV_BRIEFED.getPath());
				})
				.step(player.tickPlayer(5))
				.then(() -> {
					helper.assertTrue(player.count(ItemInit.bloody_vial.get()) >= 2,
							"the brief hands over two vials; " + player.last());
					// ARRANGE: the votary formed this Centrifuge and this Alembic as blood structures at D1/D2
					// (the previous band), so the machines answer them and the items are theirs to place.
					MachineAccessEvents.awardMachineCrafted(player.player(), BlockInit.vial_centrifuge.get());
					MachineAccessEvents.awardMachineCrafted(player.player(), BlockInit.ghastly_alembic.get());
					player.give(new ItemStack(BlockInit.vial_centrifuge.get()));
					player.give(new ItemStack(BlockInit.ghastly_alembic.get()));
					player.give(new ItemStack(ItemInit.hematic_iron_scrap.get()));
					level.setBlock(alembic.below(), Blocks.MAGMA_BLOCK.defaultBlockState(), Block.UPDATE_ALL); // ARRANGE: heat
					player.place(centrifuge, BlockInit.vial_centrifuge.get());
					helper.assertTrue(level.getBlockEntity(centrifuge) instanceof VialCentrifugeBlockEntity,
							"the Centrifuge is placed; " + player.last());
					station[0] = (VialCentrifugeBlockEntity) level.getBlockEntity(centrifuge);
					for (int i = 0; i < 2; i++) cows.add(spawn(helper, EntityType.COW, PlaythroughSites.stand(helper, 12 + 2 * i, 14)));
					for (Entity cow : cows) {
						wieldWhere(player, stack -> stack.is(ItemInit.bloody_vial.get())
								&& BloodVialItem.getEntityType(stack) == null, "an empty vial");
						player.approach(cow);
						player.attack(cow);
					}
					player.moveTo(wing.playerSpot());
					int samples = countWhere(player, stack -> stack.is(ItemInit.bloody_vial.get())
							&& BloodVialItem.getEntityType(stack) == EntityType.COW);
					helper.assertTrue(samples == 2, "two cow samples are drawn; found " + samples + "; " + player.last());
				})
				.then(() -> {
					emptyHand(player);
					player.useOn(centrifuge);
					AbstractContainerMenu menu = player.player().containerMenu;
					helper.assertTrue(menu instanceof VialCentrifugeMenu, "the Centrifuge opens; " + player.last());
					int[] opposite = {VialCentrifugeMenu.VIAL_START, VialCentrifugeMenu.VIAL_START + 4};
					for (int target : opposite) {
						int from = menuSlotOf(menu, player.player(), stack -> stack.is(ItemInit.bloody_vial.get())
								&& BloodVialItem.getEntityType(stack) == EntityType.COW);
						helper.assertTrue(from >= 0, "a cow sample is in the inventory to load");
						player.click(menu, from, 0, ClickType.PICKUP);
						player.click(menu, target, 0, ClickType.PICKUP);
					}
					VialCentrifugeStartupResult start = StartCentrifugeButtonPacket.start(player.player(), station[0]);
					helper.assertTrue(start == VialCentrifugeStartupResult.SUCCESS, "the assigned spin starts: " + start);
				})
				.until(VialCentrifugeBlockEntity.SPIN_TOTAL_TIME + 200, () -> outputSlot(player.player().containerMenu) >= 0,
						"the Centrifuge to finish its spin")
				.then(() -> {
					AbstractContainerMenu menu = player.player().containerMenu;
					Outcome take = player.quickMove(menu, outputSlot(menu));
					take.earned(HarbingerAdvancementGranter.ADV_FIRST_SEPARATION_COMPLETE.getPath());
					player.closeMenu();
					player.talkTo(alchemist);
					Outcome claim = player.choose(alchemist, HarbingerAlchemistDialogueTrees.EVENT_FIRST_SEPARATION_CLAIM);
					helper.assertTrue(FirstSeparationAssignment.isClaimed(player.player()), "the First Separation is claimed; " + claim);
				})
				.then(() -> {
					player.place(alembic, BlockInit.ghastly_alembic.get());
					helper.assertTrue(level.getBlockEntity(alembic) instanceof GhastlyAlembicBlockEntity,
							"the Alembic is placed over the magma; " + player.last());
					still[0] = (GhastlyAlembicBlockEntity) level.getBlockEntity(alembic);
					emptyHand(player);
					player.useOn(alembic);
					AbstractContainerMenu menu = player.player().containerMenu;
					helper.assertTrue(menu instanceof GhastlyAlembicMenu, "the Alembic opens; " + player.last());
					placeOne(player, menu, GhastlyAlembicMenu.INGREDIENT_SLOT, ItemInit.hematic_iron_scrap.get());
				})
				.until(900, () -> !still[0].getItem(GhastlyAlembicBlockEntity.SLOT_RESULT).isEmpty(),
						"the first distillation to finish")
				.then(() -> {
					player.quickMove(player.player().containerMenu, GhastlyAlembicMenu.RESULT_SLOT);
					player.closeMenu();
					helper.assertTrue(HemoCapabilityAccess.stationUpgrades(player.player()).hasUsed(UpgradeStation.ALEMBIC,
							StationUpgradeCatalog.DISTILL), "taking the distillate records the distillation");
					player.talkTo(alchemist);
					Outcome ask = player.choose(alchemist, "alchemist_replace_concentrated_blood");
					helper.assertTrue(countWhere(player, ConcentratedBlood::is) == 1, "the Alchemist gives a Concentrated vial; " + ask);
					wieldWhere(player, ConcentratedBlood::is, "the Concentrated Blood");
				})
				.step(() -> player.holdUntil(ItemInit.bloody_vial.get(), 200,
						() -> player.player().getEyePosition().add(player.player().getLookAngle()),
						() -> ConcentratedBlood.pending(player.player())))
				.then(() -> {
					helper.assertTrue(ConcentratedBlood.pending(player.player()), "holding the vial injects it; " + player.last());
					placeBed(level, bed);
					player.moveTo(bed.west());
					Outcome sleep = player.sleepThroughNight(bed);
					sleep.degreeIs(3).said("hemomancy.initiation.degree");
				})
				.then(() -> {
					PlaythroughPlayer fixture = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 20));
					PlaythroughStates.initiateD3(fixture);
					Set<String> played = PlaythroughFingerprints.earned(player.player());
					Set<String> fixed = PlaythroughFingerprints.earned(fixture.player());
					Set<String> onlyFixture = new TreeSet<>(fixed);
					onlyFixture.removeAll(played);
					Set<String> onlyPlayed = new TreeSet<>(played);
					onlyPlayed.removeAll(fixed);
					PlaythroughLog.note(helper, "initiateChain", "fixture-only advancements " + onlyFixture
							+ "; play-only advancements " + onlyPlayed);
					helper.assertTrue(player.degree() == fixture.degree() && player.hasCharm() && player.bloodActive()
							&& !ConcentratedBlood.pending(player.player()), "the replay ends where initiateD3 does");
					helper.assertTrue(onlyFixture.isEmpty(),
							"initiateD3 grants advancements real play does not reach: " + onlyFixture);
				});
	}

	// =============================================================================================================
	// The Mnemonist and the Woven Vessel
	// =============================================================================================================

	/**
	 * A D3 player right-clicks the Mnemonist: the Woven Vessel is filed as Quest Work with a NOTICE sigil; the Loom,
	 * Reliquary, Scriptorium and Chamber cards are there; the guided Chamber lesson is offered; there is no Circus
	 * card yet; every key resolves and nothing is a dead end.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void mnemonistWovenTreeAtD3(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = d3(helper);
		DialogueView view = player.talkTo(mnemonist);
		PlaythroughLog.note(helper, "mnemonistWovenTreeAtD3", view.describe());
		view.assertLangKeys(helper, "the Mnemonist at D3").assertNoDeadEnds(helper, "the Mnemonist at D3");
		Optional<DialogueTopic> woven = view.topicById("quests/woven_vessel");
		helper.assertTrue(woven.isPresent() && woven.get().category() == DialogueCategory.QUESTS,
				"the Woven Vessel is Quest Work: " + view.describe());
		helper.assertTrue(woven.get().attention() == DialogueAttention.NOTICE,
				"the Woven Vessel card carries a NOTICE sigil, not " + woven.get().attention());
		helper.assertTrue(view.category(TURN_IN).orElse(null) == DialogueCategory.QUESTS,
				"the turn-in is reached from Quest Work: " + view.describe());
		for (String card : List.of("ask_about_loom", "ask_about_reliquary", "ask_about_scriptorium", "ask_about_chamber")) {
			String key = "hemomancy.dialogue.mnemonist.option." + card;
			helper.assertTrue(hasCard(view, key), "the Mnemonist shows the " + card + " card: " + view.describe());
		}
		helper.assertTrue(view.offers(GUIDED), "the guided Chamber lesson is offered: " + view.describe());
		helper.assertFalse(hasCard(view, "hemomancy.dialogue.mnemonist.option.ask_about_circus"),
				"the Circus card is still hidden at D3: " + view.describe());
		helper.succeed();
	}

	/** The blank-memory recipe is taught through the Woven Vessel card once; a second ask teaches nothing. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void blankRecipeAwardedOnce(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = d3(helper);
		helper.assertFalse(knowsRecipe(player, "hematic_memory"), "a fresh initiate has not been taught the blank yet");
		player.talkTo(mnemonist);
		Outcome first = player.choose(mnemonist, BLANK_RECIPE);
		helper.assertTrue(first.rewardDelivered() && knowsRecipe(player, "hematic_memory"),
				"the Mnemonist teaches the Hematic Memory recipe; " + first);
		player.talkTo(mnemonist);
		Outcome again = player.choose(mnemonist, BLANK_RECIPE);
		helper.assertFalse(again.rewardDelivered(), "a second ask delivers nothing; " + again);
		again.gaveNothing();
		helper.succeed();
	}

	/**
	 * The archive turn-in played through: the stained stone and the blank are crafted at a crafting table by menu
	 * clicks, then the turn-in takes the book, ink and three paper and leaves the blank. The Bleeding Bulb and three
	 * Vivacious Enzymes land at the Mnemonist's feet and are walked over; the chapter advancement is earned. A second
	 * turn-in says it is known.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void wovenVesselTurnIn(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = d3(helper);
		BlockPos table = PlaythroughSites.stand(helper, WING_X - 2, WING_Z);
		level.setBlock(table, Blocks.CRAFTING_TABLE.defaultBlockState(), Block.UPDATE_ALL); // ARRANGE: a table nearby
		// ARRANGE: the materials a D3 gathers (the neurotic enzyme from a Centrifuge spin) and the archive set.
		player.give(new ItemStack(BlockInit.venous_stone.get(), 4));
		player.give(new ItemStack(ItemInit.sanguine_formation.get(), 8));
		player.give(new ItemStack(Items.REDSTONE, 4));
		player.give(new ItemStack(ItemInit.neurotic_enzyme.get()));
		player.give(new ItemStack(Items.BOOK));
		player.give(new ItemStack(Items.INK_SAC));
		player.give(new ItemStack(Items.PAPER, 3));

		player.talkTo(mnemonist);
		player.choose(mnemonist, BLANK_RECIPE);
		craftBlank(player, table);
		helper.assertTrue(player.count(ItemInit.hematic_memory.get()) == 1,
				"the blank is crafted at the table; " + player.last());

		player.talkTo(mnemonist);
		Outcome turnIn = player.choose(mnemonist, TURN_IN);
		turnIn.earned(HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE.getPath())
				.said("hemomancy.dialogue.event.mnemonist_woven_vessel_complete");
		helper.assertTrue(turnIn.delta(Items.BOOK) == -1 && turnIn.delta(Items.INK_SAC) == -1
				&& turnIn.delta(Items.PAPER) == -3 && turnIn.delta(ItemInit.hematic_memory.get()) == 0,
				"the turn-in takes the archive set and leaves the blank; " + turnIn);
		helper.assertTrue(turnIn.dropped(ItemInit.bleeding_bulb.get(), Outcome.Near.ANY) == 1
				&& turnIn.dropped(ItemInit.vivacious_enzyme.get(), Outcome.Near.ANY) == 3,
				"the bulb and three enzymes are given; " + turnIn);
		PlaythroughLog.note(helper, "wovenVesselTurnIn", "kit landed: " + turnIn.drops());
		player.pickUpNear(mnemonist.blockPosition(), ItemInit.bleeding_bulb.get());
		player.pickUpNear(mnemonist.blockPosition(), ItemInit.vivacious_enzyme.get());
		helper.assertTrue(player.count(ItemInit.bleeding_bulb.get()) == 1 && player.count(ItemInit.vivacious_enzyme.get()) == 3,
				"the player walks over the kit and holds it; " + player.last());

		DialogueView after = player.talkTo(mnemonist);
		helper.assertFalse(after.offers(TURN_IN), "the turn-in is no longer offered: " + after.describe());
		Outcome repeat = player.forge(TURN_IN, mnemonist.getId());
		repeat.said("hemomancy.dialogue.event.mnemonist_woven_vessel_known");
		helper.assertTrue(repeat.drops().isEmpty() && !repeat.rewardDelivered(), "a repeat gives nothing; " + repeat);
		helper.succeed();
	}

	/** FIX: F-9. The blank held in the offhand still counts for the turn-in. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void turnInWithBlankInOffhand(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = d3(helper);
		archiveSet(player);
		player.player().setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ItemInit.hematic_memory.get())); // ARRANGE
		player.talkTo(mnemonist);
		Outcome turnIn = player.choose(mnemonist, TURN_IN);
		helper.assertTrue(turnIn.rewardDelivered()
						&& turnIn.earned().contains(HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE.getPath()),
				"a blank in the offhand is accepted for the Woven Vessel; " + turnIn);
		helper.assertTrue(player.player().getOffhandItem().is(ItemInit.hematic_memory.get()), "the blank is kept in the offhand");
		helper.succeed();
	}

	// =============================================================================================================
	// The Loom and the first weave
	// =============================================================================================================

	/**
	 * The Somatic Loom raised by its Working craft: the Working floor is laid (ARRANGE), then the player builds the
	 * subject block by block, offers string and a neurotic enzyme in the braziers and lights them with a held
	 * projection, plants the Living Staff and feeds the subject. One Loom condenses, the offerings are settled once,
	 * and the Loom the player sets down is a working Somatic Loom.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 4200, batch = BATCH)
	public static void loomRaisedByRealWorkingCraft(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer player = d3(helper);
		RiteSite site = layRite(player, helper.absolutePos(RITE_FOCUS), "working/somatic_loom");
		ActiveCardinalRite[] rite = new ActiveCardinalRite[1];
		BlockPos loomSpot = PlaythroughSites.stand(helper, 6, 20);
		PlaythroughClock clock = PlaythroughClock.start(helper).then(() -> buildPieces(player, site));
		for (int i = 0; i < site.braziers().size(); i++) {
			BlockPos brazier = site.braziers().get(i);
			ItemStack offering = site.offerings().get(i);
			clock.then(() -> {
				player.give(offering.copy());
				player.moveTo(outward(site.focus(), brazier, 2));
				player.wield(offering.getItem());
				player.useOn(brazier);
				helper.assertTrue(brazierAt(level, brazier).hasOffering(), "the offering goes in by hand; " + player.last());
			}).step(() -> player.holdUntil(ItemInit.blood_projection.get(), 200, () -> solidPoint(level, brazier),
					() -> BrazierBlock.isLit(level.getBlockState(brazier))))
					.then(() -> helper.assertTrue(BrazierBlock.isLit(level.getBlockState(brazier)),
							"a held projection lights the brazier at " + brazier.toShortString() + "; " + player.last()));
		}
		clock.then(() -> {
					player.moveTo(site.focus().north(2).above());
					player.wield(ItemInit.living_staff.get());
				})
				.step(() -> PlaythroughClock.rite(player, site.focus(), 3800, (active, recipe) -> {
					rite[0] = active;
					if (active.getPhase() != CardinalRitePhase.SUBJECT_FEEDING) return null;
					return site.pieces().stream().map(Vec3::atCenterOf)
							.min(Comparator.comparingDouble(point -> point.distanceToSqr(player.player().getEyePosition())))
							.orElse(null);
				}))
				.then(() -> {
					helper.assertTrue(rite[0] != null && rite[0].isWorkingCraft(), "the Staff started the Loom's Working craft");
					helper.assertTrue(RiteOfferingEscrow.isConsumed(rite[0].craft().tag()), "the escrowed offerings are settled");
					for (BlockPos brazier : site.braziers()) {
						helper.assertFalse(brazierAt(level, brazier).hasOffering(), "no offering is left in " + brazier.toShortString());
					}
					helper.assertTrue(player.count(Items.STRING) == 0 && player.count(ItemInit.neurotic_enzyme.get()) == 0,
							"consumed offerings do not come back");
					int looms = drops(level, site.focus(), BlockInit.somatic_loom.get().asItem(), 8.0D);
					helper.assertTrue(looms == 1, "exactly one Somatic Loom condenses, found " + looms);
					deliverStaff(player);
					helper.assertTrue(player.count(ItemInit.living_staff.get()) == 1, "the Staff comes back");
					helper.assertTrue(player.player().getStats().getValue(Stats.ITEM_CRAFTED.get(BlockInit.somatic_loom.get().asItem())) > 0,
							"the craft grants the player machine access");
					player.pickUpNear(site.focus(), BlockInit.somatic_loom.get());
					player.moveTo(loomSpot.north(3));
					player.place(loomSpot, BlockInit.somatic_loom.get());
					helper.assertTrue(level.getBlockEntity(loomSpot) instanceof SomaticLoomBlockEntity,
							"the condensed Loom sets down as a Somatic Loom; " + player.last());
				})
				.run();
	}

	/**
	 * The first Blood Shot weave: memory, bulb and enzymes go in by hand, an empty-hand click selects Blood Shot, a
	 * held projection pays its 50 mL, the engram is threaded and the climax ticks through. The memory drops, the
	 * first-weave advancement is earned and the WOVEN_VESSEL chapter is complete.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 800, batch = BATCH)
	public static void firstBloodShotWeaveCompletesChapter(GameTestHelper helper) {
		PlaythroughPlayer player = d3(helper);
		BlockPos loomPos = helper.absolutePos(LOOM);
		Weave weave = new Weave(player, loomPos);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		weave.addTo(clock, null);
		clock.then(() -> {
			Outcome climax = player.last();
			helper.assertTrue(weave.memoriesDropped() == 1, "the woven Blood Shot memory drops; " + climax);
			helper.assertTrue(weave.paid() >= 49.0D && weave.paid() <= 51.0D, "the weave costs 50 mL, paid " + weave.paid());
			helper.assertTrue(player.has(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE.getPath()),
					"the first weave is recorded");
			helper.assertTrue(HarbingerChapterProgression.completedChapters(player.player()).contains(HarbingerChapterMilestone.WOVEN_VESSEL),
					"the Woven Vessel chapter is complete");
			player.pickUpNear(loomPos, ItemInit.memory_blood_shot.get());
			helper.assertTrue(player.count(ItemInit.memory_blood_shot.get()) == 1, "the player picks up the memory");
		}).run();
	}

	// =============================================================================================================
	// The Rite of the Sanguine Brotherhood
	// =============================================================================================================

	/**
	 * Planting the Staff on a ready Brotherhood floor before the first weave: the rite stays sealed and names the
	 * Woven Vessel chapter, nothing is escrowed, the Staff stays in hand, and once the weave is recorded the same
	 * floor starts.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void brotherhoodSealedWithoutWeave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer player = d3(helper);
		RiteSite site = layRite(player, helper.absolutePos(RITE_FOCUS), "sanguine_brotherhood");
		clearRiteAtEnd(player);
		PlaythroughClock.start(helper)
				.then(() -> {
					buildRite(player, site);
					player.moveTo(site.focus().north(3).above());
					player.wield(ItemInit.living_staff.get());
					Outcome sealed = player.useOn(site.focus());
					sealed.said(SEALED).said("milestone.hemomancy.chapter.woven_vessel");
					helper.assertTrue(CardinalRiteSavedData.get(level).getRite(player.uuid()) == null, "no rite starts; " + sealed);
					helper.assertTrue(player.count(ItemInit.living_staff.get()) == 1
							&& player.player().getMainHandItem().is(ItemInit.living_staff.get()), "the Staff is kept in hand");
					for (BlockPos brazier : site.braziers()) {
						helper.assertTrue(brazierAt(level, brazier).hasOffering(), "the brazier offering is not escrowed");
					}
					// ARRANGE: the weave itself is firstBloodShotWeaveCompletesChapter's; record it.
					HarbingerAdvancementGranter.grantIfNotDone(player.player(),
							HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE);
					Outcome retry = player.useOn(site.focus());
					helper.assertTrue(CardinalRiteSavedData.get(level).getRite(player.uuid()) != null,
							"the same floor starts once the weave is recorded; " + retry);
				})
				.run();
	}

	/**
	 * The Brotherhood performed: the floor is laid (ARRANGE), the player sets the eight hematic iron pillars, puts the
	 * hematic iron powder in the brazier by hand, plants the Staff and feeds the anchors until the daemon is sealed.
	 * The player is Degree 4, is told so, earns the degree advancement and has the Staff back.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 6400, batch = BATCH)
	public static void brotherhoodCeremonyPromotesToD4(GameTestHelper helper) {
		PlaythroughPlayer player = d3(helper);
		// ARRANGE: the weave is firstBloodShotWeaveCompletesChapter's.
		HarbingerAdvancementGranter.grantIfNotDone(player.player(), HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE);
		RiteSite site = layRite(player, helper.absolutePos(RITE_FOCUS), "sanguine_brotherhood");
		PlaythroughClock.start(helper)
				.then(() -> {
					buildRite(player, site);
					player.moveTo(site.focus().north(3).above());
					player.wield(ItemInit.living_staff.get());
				})
				.step(() -> PlaythroughClock.rite(player, site.focus(), 6000, (rite, recipe) -> null))
				.idle(5)
				.then(() -> {
					helper.assertTrue(player.degree() == 4, "the Brotherhood promotes to Degree 4; " + player.chatSince(0));
					helper.assertTrue(player.chat().stream().anyMatch(line -> line.has("hemomancy.initiation.degree")),
							"the player is told their new degree");
					helper.assertTrue(player.has(HarbingerAdvancementGranter.ADV_DEGREE_4_ADEPT.getPath()), "the degree advancement is earned");
					deliverStaff(player);
					helper.assertTrue(player.count(ItemInit.living_staff.get()) == 1, "the Staff comes back");
				})
				.run();
	}

	/**
	 * DECISION: D-6 WOVEN_VESSEL is satisfied by any Blood Shot weave; the Mnemonist's index (turn-in) is optional.
	 * Expected by a player reading the ledger: the Brotherhood stays sealed until the vessel is indexed.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 800, batch = BATCH, required = false)
	public static void weaveWithoutMnemonistUnlocksD4(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer player = d3(helper);
		// ARRANGE: a blank crafted without the Mnemonist's lesson and a bulb the player found themselves.
		Weave weave = new Weave(player, helper.absolutePos(LOOM));
		RiteSite[] site = new RiteSite[1];
		clearRiteAtEnd(player);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		weave.addTo(clock, null);
		clock.then(() -> {
			helper.assertFalse(knowsRecipe(player, "hematic_memory"), "the blank recipe was never taught");
			helper.assertFalse(player.has(HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE.getPath()),
					"the vessel was never indexed");
			boolean chapter = HarbingerChapterProgression.completedChapters(player.player()).contains(HarbingerChapterMilestone.WOVEN_VESSEL);
			site[0] = layRite(player, helper.absolutePos(RITE_FOCUS), "sanguine_brotherhood");
			buildRite(player, site[0]);
			player.moveTo(site[0].focus().north(3).above());
			player.wield(ItemInit.living_staff.get());
			Outcome plant = player.useOn(site[0].focus());
			boolean started = CardinalRiteSavedData.get(level).getRite(player.uuid()) != null;
			PlaythroughLog.note(helper, "weaveWithoutMnemonistUnlocksD4", "WOVEN_VESSEL granted=" + chapter
					+ " without the index; Brotherhood started=" + started + "; " + plant.saidKeys());
			helper.assertTrue(!started && plant.saidKeys().contains(SEALED),
					"the Brotherhood stays sealed until the Mnemonist indexes the vessel; started=" + started);
		}).run();
	}

	/**
	 * DECISION: D-20 A weaver who is offline when the climax completes loses the first-weave milestone. Expected: the
	 * milestone is there when they come back.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 800, batch = BATCH, required = false)
	public static void weaverOfflineDuringClimax(GameTestHelper helper) {
		PlaythroughPlayer player = d3(helper);
		BlockPos loomPos = helper.absolutePos(LOOM);
		Weave weave = new Weave(player, loomPos);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		weave.addTo(clock, loom -> {
			// The weaver logs out as the climax runs: out of the player list and removed from the world.
			player.offline();
			player.player().remove(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
		});
		clock.then(() -> {
			player.relog();
			boolean milestone = player.has(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE.getPath());
			PlaythroughLog.note(helper, "weaverOfflineDuringClimax", "memory spawned=" + weave.memoriesDropped()
					+ ", first-weave milestone on return=" + milestone);
			helper.assertTrue(weave.memoriesDropped() == 1, "the memory still crystallises");
			helper.assertTrue(milestone, "the first weave is recorded for a weaver who was offline at the climax");
		}).run();
	}

	// =============================================================================================================
	// Hints and pointers
	// =============================================================================================================

	/**
	 * DECISION: D-7 The Vicar's D3 degree hint says to study the Bloodline system. Expected: it names the real gate,
	 * the Loom or the Woven Vessel weave.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void vicarD3HintNamesTheRealGate(GameTestHelper helper) {
		Entity vicar = wing(helper, PlaythroughNpcs.Role.VICAR).get(PlaythroughNpcs.Role.VICAR);
		PlaythroughPlayer player = d3(helper);
		DialogueView view = player.talkTo(vicar);
		view.assertLangKeys(helper, "the Vicar at D3");
		Optional<DialogueNode> hint = view.node("degree_hint");
		helper.assertTrue(hint.isPresent(), "the Vicar answers what degree comes next: " + view.describe());
		List<String> lines = new ArrayList<>();
		for (String line : hint.get().lines()) Dialogue.lineKey(line).ifPresent(key -> lines.add(key + " = " + english(key)));
		PlaythroughLog.note(helper, "vicarD3HintNamesTheRealGate", "D3 degree hint: " + lines);
		String text = String.join(" ", lines).toLowerCase(Locale.ROOT);
		helper.assertTrue(text.contains("loom") || text.contains("woven"),
				"the D3 hint names the Loom or the Woven Vessel weave: " + lines);
		helper.succeed();
	}

	/**
	 * DECISION: D-21 D3 puppetry is gated on Circus instruction, but the Circus is hidden until D4. Expected: a D3
	 * player's teachers point them toward the Circus.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void d3PuppetryHasNoPointer(GameTestHelper helper) {
		PlaythroughSites.OutpostWing wing = wing(helper, PlaythroughNpcs.Role.MNEMONIST, PlaythroughNpcs.Role.VICAR,
				PlaythroughNpcs.Role.ALCHEMIST, PlaythroughNpcs.Role.ARTIFICER);
		PlaythroughPlayer player = d3(helper);
		List<String> pointers = new ArrayList<>();
		for (PlaythroughNpcs.Role role : List.of(PlaythroughNpcs.Role.MNEMONIST, PlaythroughNpcs.Role.VICAR,
				PlaythroughNpcs.Role.ALCHEMIST, PlaythroughNpcs.Role.ARTIFICER)) {
			DialogueView view = player.talkTo(wing.get(role));
			for (String key : shownKeys(view)) {
				if (key.toLowerCase(Locale.ROOT).contains("circus") || english(key).toLowerCase(Locale.ROOT).contains("circus")) {
					pointers.add(role + ":" + key);
				}
			}
		}
		PlaythroughLog.note(helper, "d3PuppetryHasNoPointer", "Circus pointers a D3 player is shown: " + pointers);
		helper.assertFalse(pointers.isEmpty(), "some teacher points a D3 player toward the Circus");
		helper.succeed();
	}

	// =============================================================================================================
	// Clinical: the Clairaudiograph
	// =============================================================================================================

	/**
	 * With three sources examined, the Alchemist's Echo Referral and then the Mnemonist's Clairaudiograph lesson:
	 * the Clairaudiograph and Wax Cylinder recipes, the Liber pages and the clinical advancement.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void clairaudiographAfterEchoReferral(GameTestHelper helper) {
		PlaythroughSites.OutpostWing wing = wing(helper, PlaythroughNpcs.Role.MNEMONIST, PlaythroughNpcs.Role.ALCHEMIST);
		Entity mnemonist = wing.get(PlaythroughNpcs.Role.MNEMONIST);
		Entity alchemist = wing.get(PlaythroughNpcs.Role.ALCHEMIST);
		PlaythroughPlayer player = d3(helper);
		// ARRANGE: three sources examined under the Microscope (the D2 clinical chain is the previous band's).
		var clinical = HemoCapabilityAccess.clinicalBlood(player.player());
		for (String source : List.of("minecraft:cow", "minecraft:pig", "minecraft:sheep")) clinical.recordExamination(source);
		ClinicalBloodKnowledge.sync(player.player());

		DialogueView before = player.talkTo(mnemonist);
		helper.assertFalse(before.offers("clinical_lesson_clairaudiograph"),
				"the Mnemonist's lesson waits for the Echo Referral: " + before.describe());
		player.talkTo(alchemist);
		Outcome referral = player.choose(alchemist, "clinical_lesson_echo_referral");
		referral.earned("hemomancy/clinical_echo_referral");
		player.talkTo(mnemonist);
		Outcome lesson = player.choose(mnemonist, "clinical_lesson_clairaudiograph");
		lesson.earned("hemomancy/clinical_clairaudiograph");
		helper.assertTrue(knowsRecipe(player, "clairaudiograph") && knowsRecipe(player, "wax_cylinder"),
				"the Clairaudiograph and Wax Cylinder recipes are taught; " + lesson);
		helper.assertTrue(LiberKnowledgeHelper.hasEntry(player.player(), ClinicalBloodKnowledge.entry("clairaudiograph"))
						&& LiberKnowledgeHelper.hasEntry(player.player(),
								Hemomancy.rloc("libersanguinium/the_infection/pages/clairaudiograph")),
				"the Clairaudiograph pages open in the Liber");
		DialogueView after = player.talkTo(mnemonist);
		helper.assertFalse(after.offers("clinical_lesson_clairaudiograph"), "the lesson is not offered again");
		// FIX: F-12 a learned lesson's card stays readable but is filed as complete work, not an open quest.
		List<String> stillActive = after.topics(DialogueCategory.QUESTS).stream()
				.filter(topic -> topic.titleKey().equals("hemomancy.clinical.clairaudiograph.title"))
				.filter(topic -> topic.state() == com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopicState.ACTIVE)
				.map(DialogueTopic::id).toList();
		helper.assertTrue(stillActive.isEmpty(), "the learned Clairaudiograph card is not an ACTIVE quest: " + after.describe());
		helper.succeed();
	}

	// =============================================================================================================
	// Review follow-ups: Blood Shot already known, the weave interrupted or shared, and the Side chains played
	// =============================================================================================================

	/**
	 * A player who took Blood Shot as the Mnemonist's D2 crude starter memory, and learnt it, then weaves Blood Shot
	 * at D3. The weave must still count as the first weave (the ALREADY_KNOWN path) and the D4 chapter gate must be
	 * met; a failure here would leave every such player unable to reach the Sanguine Brotherhood.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = BATCH)
	public static void firstWeaveWhenBloodShotAlreadyKnown(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, WING_X, WING_Z));
		PlaythroughStates.votaryD2(player); // ARRANGE
		BlockPos loomPos = helper.absolutePos(LOOM);
		Weave weave = new Weave(player, loomPos);
		PlaythroughClock clock = PlaythroughClock.start(helper)
				.then(() -> {
					DialogueView view = player.talkTo(mnemonist);
					helper.assertTrue(view.offers("mnemonist_grant_crude_blood_shot"),
							"the D2 Mnemonist offers the crude Blood Shot memory: " + view.describe());
					player.choose(mnemonist, "mnemonist_grant_crude_blood_shot");
					player.pickUpNear(mnemonist.blockPosition(), ItemInit.crude_memory_blood_shot.get());
					helper.assertTrue(player.count(ItemInit.crude_memory_blood_shot.get()) == 1, "the crude memory is carried");
					PlaythroughStates.blood(player, PLENTY_OF_BLOOD); // ARRANGE: a votary's pool; absorbing the memory costs blood
					player.wield(ItemInit.crude_memory_blood_shot.get());
					player.use(InteractionHand.MAIN_HAND);
				})
				.step(() -> player.knows("blood_shot") ? (PlaythroughClock.Step) ticks -> true
						: player.hold(ItemInit.crude_memory_blood_shot.get(), 100, player.player().getEyePosition()
								.add(player.player().getLookAngle())))
				.then(() -> {
					helper.assertTrue(player.knows("blood_shot"), "using the crude memory teaches Blood Shot; " + player.last());
					PlaythroughStates.initiateD3(player); // ARRANGE: the D2 Main is its own band
					PlaythroughStates.blood(player, PLENTY_OF_BLOOD); // ARRANGE
				});
		weave.addTo(clock, null);
		clock.then(() -> {
			PlaythroughLog.note(helper, "firstWeaveWhenBloodShotAlreadyKnown", "memories dropped " + weave.memoriesDropped()
					+ ", paid " + weave.paid() + "; " + player.last());
			helper.assertTrue(player.has(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE.getPath()),
					"a Blood Shot weave by a player who already knows Blood Shot is still the first weave");
			helper.assertTrue(HarbingerChapterProgression.unmetChapterForTargetDegree(player.player(), 4) == null,
					"the D4 chapter gate is met: " + HarbingerChapterProgression.unmetChapterForTargetDegree(player.player(), 4));
		}).run();
	}

	/**
	 * The weaver logs out and back in after the projection has paid and before the engram is threaded. The weave waits:
	 * the Loom is still weaving, no second payment is taken, and the relogged weaver threads it home and earns the
	 * first-weave milestone.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = BATCH)
	public static void relogMidWeaveResumes(GameTestHelper helper) {
		PlaythroughPlayer player = d3(helper);
		BlockPos loomPos = helper.absolutePos(LOOM);
		Weave weave = new Weave(player, loomPos);
		double[] afterPay = new double[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		weave.addTo(clock, loom -> {
			afterPay[0] = player.blood();
			player.relog();
			helper.assertTrue(loom.isWeaving(), "a relog does not drop the weave in progress");
			helper.assertTrue(Math.abs(player.blood() - afterPay[0]) < 1.0D,
					"a relog takes no second payment: " + afterPay[0] + " -> " + player.blood());
		}, null);
		clock.then(() -> {
			helper.assertTrue(weave.memoriesDropped() == 1, "the relogged weaver's memory drops; " + player.last());
			helper.assertTrue(player.has(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE.getPath()),
					"the relogged weaver earns the first weave");
		}).run();
	}

	/**
	 * Two players at one Loom: the first stocks and pays for the weave, the second stands at it, right-clicks it with
	 * an empty hand and holds a projection at it mid-weave. Only the weaver who paid earns the first-weave milestone;
	 * what the second player's clicks and blood did is logged.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = BATCH)
	public static void twoPlayersAtOneLoom(GameTestHelper helper) {
		PlaythroughPlayer weaver = d3(helper);
		PlaythroughPlayer other = d3(helper, helper.absolutePos(LOOM_WEAVER.east(2)));
		other.give(new ItemStack(ItemInit.blood_projection.get())); // ARRANGE
		BlockPos loomPos = helper.absolutePos(LOOM);
		Weave weave = new Weave(weaver, loomPos);
		double[] otherBlood = new double[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		weave.addTo(clock, loom -> {
			otherBlood[0] = other.blood();
			emptyHand(other);
			Outcome click = other.useOn(loomPos);
			PlaythroughLog.note(helper, "twoPlayersAtOneLoom", "the second player's empty-hand click mid-weave: " + click.describe());
		}, null);
		clock.then(() -> {
			PlaythroughLog.note(helper, "twoPlayersAtOneLoom", "the second player's blood " + otherBlood[0] + " -> " + other.blood());
			helper.assertTrue(weaver.has(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE.getPath()),
					"the weaver who paid earns the first weave");
			helper.assertFalse(other.has(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE.getPath()),
					"the bystander does not");
		}).run();
	}

	/**
	 * The Reef Watch Side chain played: the Vicar's bearing toward the Voyagers, a Voyager observed at a field site
	 * (an Erythrocoral Reef, ARRANGE), and the report back to the Vicar. The Side work is recorded; the degree stays.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void reefWatchSide(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, WING_X - 3, WING_Z));
		BlockPos voyagerSpot = PlaythroughSites.stand(helper, 20, 16);
		reefBiome(helper, voyagerSpot); // ARRANGE: the Voyager stands in a reef, a documented field site
		var voyager = PlaythroughNpcs.voyager(helper, voyagerSpot);
		PlaythroughPlayer player = d3(helper);
		int degree = player.degree();
		DialogueView atVicar = player.approach(vicar).talkTo(vicar);
		atVicar.assertLangKeys(helper, "the D3 Vicar").assertNoDeadEnds(helper, "the D3 Vicar");
		String bearing = com.vincenthuto.hemomancy.common.entity.npc.dialogue.VoyagerIntroductionDialogue.BEARING;
		String observe = com.vincenthuto.hemomancy.common.entity.npc.dialogue.VoyagerIntroductionDialogue.OBSERVE;
		String report = com.vincenthuto.hemomancy.common.entity.npc.dialogue.VoyagerIntroductionDialogue.REPORT;
		helper.assertTrue(atVicar.offers(bearing), "the D3 Vicar offers the Reef Watch bearing: " + atVicar.describe());
		Outcome asked = player.choose(vicar, bearing);
		PlaythroughLog.note(helper, "reefWatchSide", "bearing: " + asked.describe());
		player.approach(voyager);
		DialogueView atVoyager = player.talkTo(voyager);
		atVoyager.assertLangKeys(helper, "the Voyager to a D3 player").assertNoDeadEnds(helper, "the Voyager");
		helper.assertTrue(atVoyager.offers(observe), "the Voyager at the reef can be observed: " + atVoyager.describe());
		player.choose(voyager, observe).said("hemomancy.voyager.introduction.observed");
		var mid = com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.progress(player.player());
		PlaythroughLog.note(helper, "reefWatchSide", "after the observation: " + mid);
		DialogueView back = player.approach(vicar).talkTo(vicar);
		helper.assertTrue(back.offers(report), "the Vicar takes the report: " + back.describe());
		player.choose(vicar, report).said("hemomancy.vicar.voyager_introduction.recorded");
		var done = com.vincenthuto.hemomancy.common.mission.vicar.VoyagerIntroduction.progress(player.player());
		helper.assertTrue(done.reported(), "the Reef Watch is recorded as reported: " + done);
		helper.assertTrue(player.degree() == degree, "Side work does not change the degree");
		DialogueView after = player.talkTo(vicar);
		helper.assertFalse(after.offers(report) || after.offers(bearing), "nothing is offered twice: " + after.describe());
		helper.succeed();
	}

	/**
	 * The Pallid Vessel Vocation played at the Alchemist: the brief, the rite and the fill (ARRANGE, each its own
	 * system), then the report with every slot full. The reward is owed, not lost, and arrives once there is room.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = CLOCK_BATCH)
	public static void pallidVesselVocation(GameTestHelper helper) {
		Entity alchemist = wing(helper, PlaythroughNpcs.Role.ALCHEMIST).get(PlaythroughNpcs.Role.ALCHEMIST);
		PlaythroughPlayer player = d3(helper);
		String brief = com.vincenthuto.hemomancy.common.entity.npc.dialogue.PallidVesselDialogue.BRIEF;
		String report = com.vincenthuto.hemomancy.common.entity.npc.dialogue.PallidVesselDialogue.REPORT;
		PlaythroughClock.start(helper)
				.then(() -> {
					DialogueView view = player.talkTo(alchemist);
					view.assertLangKeys(helper, "the D3 Alchemist").assertNoDeadEnds(helper, "the D3 Alchemist");
					helper.assertTrue(view.offers(brief), "the D3 Alchemist briefs the Pallid Vessel: " + view.describe());
					player.choose(alchemist, brief).said("hemomancy.alchemist.pallid_vessel.recorded.briefed");
					com.vincenthuto.hemomancy.common.mission.alchemist.PallidVesselAssignment.recordRite(player.player()); // ARRANGE
					ItemStack gourd = new ItemStack(ItemInit.blood_gourd_white.get()); // ARRANGE: the filled White Blood Gourd
					HemoCapabilityAccess.getBloodVolume(gourd).ifPresent(volume -> volume.fill(100.0D));
					player.give(gourd);
					DialogueView ready = player.talkTo(alchemist);
					helper.assertTrue(ready.offers(report), "the report is offered once the vessel is filled: " + ready.describe());
					player.fillInventory(); // ARRANGE: every slot full at the report
					Outcome reported = player.choose(alchemist, report);
					reported.said("hemomancy.alchemist.pallid_vessel.recorded.complete");
					helper.assertTrue(player.count(ItemInit.dried_gourd.get()) == 0, "with no room the gourds are owed; " + reported);
					var items = player.player().getInventory().items;
					for (int slot = 0, cleared = 0; slot < items.size() && cleared < 3; slot++) {
						if (items.get(slot).is(Items.SADDLE)) {
							items.set(slot, ItemStack.EMPTY);
							cleared++;
						}
					}
				})
				.step(player.tickPlayer(40))
				.then(() -> {
					helper.assertTrue(player.count(ItemInit.dried_gourd.get()) == 2 && player.count(Items.AMETHYST_SHARD) == 1,
							"the owed reward arrives once: gourds " + player.count(ItemInit.dried_gourd.get()) + ", shards "
									+ player.count(Items.AMETHYST_SHARD));
				})
				.step(player.tickPlayer(40))
				.then(() -> helper.assertTrue(player.count(ItemInit.dried_gourd.get()) == 2, "and only once"))
				.run();
	}

	/**
	 * The Artificer's Three Answers in order: the brief, the first fork upgrade at the Armature (ARRANGE, a station
	 * upgrade), the inspection, and the counsel claim wherever the hub offers it. The counsel reward comes once. The
	 * demonstration and fitting steps are not played here (logged as not covered).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void threeAnswersInOrder(GameTestHelper helper) {
		PlaythroughSites.OutpostWing wing = wing(helper, PlaythroughNpcs.Role.ARTIFICER, PlaythroughNpcs.Role.ALCHEMIST);
		Entity artificer = wing.get(PlaythroughNpcs.Role.ARTIFICER);
		Entity alchemist = wing.get(PlaythroughNpcs.Role.ALCHEMIST);
		PlaythroughPlayer player = d3(helper);
		String brief = HarbingerArtificerDialogueTrees.EVENT_BRIEF_THREE_ANSWERS;
		String inspect = HarbingerArtificerDialogueTrees.EVENT_INSPECT_THREE_ANSWERS;
		String counsel = HarbingerArtificerDialogueTrees.EVENT_CLAIM_THREE_ANSWERS_REWARD;
		DialogueView first = player.talkTo(artificer);
		first.assertLangKeys(helper, "the D3 Artificer").assertNoDeadEnds(helper, "the D3 Artificer");
		helper.assertTrue(first.offers(brief), "the Artificer briefs the Three Answers: " + first.describe());
		player.choose(artificer, brief);
		helper.assertTrue(player.has(ArtificerAssignments.THREE_ANSWERS_BRIEFED.getPath()), "the brief is recorded");
		Outcome early = player.forge(inspect, artificer.getId());
		helper.assertFalse(player.has(ArtificerAssignments.THREE_ANSWERS_INSPECTED.getPath()),
				"no inspection before the fork upgrade; " + early);
		HarbingerAdvancementGranter.grantIfNotDone(player.player(), HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_FORK_UPGRADE); // ARRANGE
		ArtificerAssignments.recordForkFamily(player.player(),
				com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules.ForkFamily.BARBED); // ARRANGE
		DialogueView upgraded = player.talkTo(artificer);
		helper.assertTrue(upgraded.offers(inspect), "after the fork upgrade the Artificer inspects it: " + upgraded.describe());
		player.choose(artificer, inspect);
		helper.assertTrue(player.has(ArtificerAssignments.THREE_ANSWERS_INSPECTED.getPath()), "the inspection is recorded");
		DialogueView atArtificer = player.talkTo(artificer);
		DialogueView atAlchemist = player.talkTo(alchemist);
		PlaythroughLog.note(helper, "threeAnswersInOrder", "counsel offered by the Artificer=" + atArtificer.offers(counsel)
				+ ", by the Alchemist=" + atAlchemist.offers(counsel));
		Entity counselor = atAlchemist.offers(counsel) ? alchemist : atArtificer.offers(counsel) ? artificer : null;
		helper.assertTrue(counselor != null, "someone offers the Three Answers counsel claim: Artificer "
				+ atArtificer.describe() + "; Alchemist " + atAlchemist.describe());
		Entity wrong = counselor == alchemist ? artificer : alchemist;
		Outcome misrouted = player.forge(counsel, wrong.getId());
		PlaythroughLog.note(helper, "threeAnswersInOrder", "the counsel sent to " + PlaythroughPlayer.describe(wrong) + ": "
				+ misrouted.describe());
		player.talkTo(counselor);
		Outcome claimed = player.choose(counselor, counsel);
		player.pickUpNear(counselor.blockPosition(), ItemInit.hematic_iron_scrap.get());
		PlaythroughLog.note(helper, "threeAnswersInOrder", "counsel: " + claimed.describe()
				+ "; the demonstration hit and the fitting are not played here");
		helper.assertTrue(player.has(ArtificerAssignments.THREE_ANSWERS_COUNSELED.getPath()) || misrouted.rewardDelivered(),
				"the counsel is recorded once; " + claimed);
		player.talkTo(counselor);
		Outcome again = player.forge(counsel, counselor.getId());
		helper.assertFalse(again.rewardDelivered(), "the counsel reward is given once; " + again);
		helper.succeed();
	}

	/** At D4 the Mnemonist's Circus topic gives one Scarlet Waybill; asking while it is carried gives none. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void d4CircusWaybill(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, WING_X, WING_Z));
		PlaythroughStates.adeptD4(player); // ARRANGE
		String waybill = HarbingerMnemonistDialogueTrees.EVENT_CIRCUS_WAYBILL;
		DialogueView view = player.talkTo(mnemonist);
		view.assertLangKeys(helper, "the D4 Mnemonist").assertNoDeadEnds(helper, "the D4 Mnemonist");
		helper.assertTrue(view.offers(waybill), "the D4 Mnemonist offers the Circus waybill: " + view.describe());
		Outcome taken = player.choose(mnemonist, waybill);
		player.pickUpNear(mnemonist.blockPosition(), ItemInit.circus_waybill.get());
		helper.assertTrue(player.count(ItemInit.circus_waybill.get()) == 1, "one Scarlet Waybill arrives; " + taken);
		player.talkTo(mnemonist);
		Outcome again = player.forge(waybill, mnemonist.getId());
		player.pickUpNear(mnemonist.blockPosition(), ItemInit.circus_waybill.get());
		helper.assertTrue(player.count(ItemInit.circus_waybill.get()) == 1, "no second waybill while one is carried; " + again);
		helper.succeed();
	}

	/** ARRANGE: the column at {@code pos} set to the Erythrocoral Reef biome, restored when the test ends. */
	private static void reefBiome(GameTestHelper helper, BlockPos pos) {
		ServerLevel level = helper.getLevel();
		var previous = level.getBiome(pos).unwrapKey().map(key -> key.location().toString()).orElse("minecraft:plains");
		String box = (pos.getX() - 2) + " " + (pos.getY() - 2) + " " + (pos.getZ() - 2) + " " + (pos.getX() + 2) + " "
				+ (pos.getY() + 3) + " " + (pos.getZ() + 2);
		var source = level.getServer().createCommandSourceStack().withLevel(level).withSuppressedOutput()
				.withPermission(4);
		level.getServer().getCommands().performPrefixedCommand(source, "fillbiome " + box + " "
				+ com.vincenthuto.hemomancy.common.init.BiomeInit.ERYTHROCORAL_REEF.location());
		PlaythroughCleanup.onEnd(helper, () -> level.getServer().getCommands().performPrefixedCommand(source,
				"fillbiome " + box + " " + previous));
		helper.assertTrue(level.getBiome(pos).is(com.vincenthuto.hemomancy.common.init.BiomeInit.ERYTHROCORAL_REEF),
				"the Voyager's spot reads as a reef");
	}

	// =============================================================================================================
	// The guided Chamber visit
	// =============================================================================================================

	/**
	 * On a server where the Chamber of Will is not loaded (the GameTest server loads only vanilla dimensions), asking
	 * for the guided lesson is refused with the unavailable line and leaves the inventory, armour, offhand, grid and
	 * cursor exactly as they were. Where the Chamber is loaded this has nothing to check.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void guidedChamberUnavailableLeavesStateUntouched(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = d3(helper);
		if (chamberLoaded(helper)) {
			PlaythroughLog.note(helper, "guidedChamberUnavailableLeavesStateUntouched", "the Chamber is loaded; nothing to check");
			helper.succeed();
			return;
		}
		kitOut(player, true);
		VisitSnapshot before = VisitSnapshot.of(player.player());
		player.talkTo(mnemonist);
		Outcome refused = player.choose(mnemonist, GUIDED);
		refused.said(GUIDED_UNAVAILABLE);
		helper.assertTrue(before.matches(player.player()),
				"a refused lesson leaves everything as it was: " + before.differences(player.player()));
		helper.assertFalse(ChamberVisitService.isActive(player.player())
				|| ChamberVisitService.hasCompletedGuidedVisit(player.player()), "a refusal neither starts nor completes it");
		helper.succeed();
	}

	/**
	 * The guided Chamber lesson with a crafting grid in use, armour on and a shield in the offhand: the player is
	 * taken in, waits the visit out, and comes back with the inventory, armour, offhand and grid exactly as they
	 * were. The lesson is complete, the player is not attuned, and the Mnemonist stops offering it. Needs the Chamber
	 * of Will loaded (the Chamber-loaded review client); on the GameTest server it fails so a green run is never read
	 * as restore coverage.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 3000, batch = BATCH, required = false)
	public static void guidedChamberRestoresEverything(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = d3(helper);
		if (!chamberLoaded(helper)) {
			helper.fail("NOT RUN: the Chamber of Will is not loaded on this server, so the restore was not exercised;"
					+ " run it from the Chamber-loaded review client (-PchamberLoadedValidation=true)");
			return;
		}
		VisitSnapshot[] before = new VisitSnapshot[1];
		PlaythroughClock clock = PlaythroughClock.start(helper).then(() -> {
			kitOut(player, true);
			before[0] = VisitSnapshot.of(player.player());
			player.talkTo(mnemonist);
			Outcome begin = player.choose(mnemonist, GUIDED);
			PlaythroughLog.note(helper, "guidedChamberRestoresEverything", "begin: " + begin);
		});
		clock.then(() -> {
					player.last().said(GUIDED_STARTED);
					helper.assertTrue(ChamberVisitService.isActive(player.player()) && inChamber(player),
							"the player is taken into the Chamber");
				})
				.step(tickUntil(player, 2600, () -> !ChamberVisitService.isActive(player.player()), "the guided visit to end"))
				.then(() -> {
					helper.assertFalse(inChamber(player), "the player comes back from the Chamber");
					helper.assertTrue(before[0].matches(player.player()),
							"inventory, armour, offhand, grid and cursor come back unchanged: " + before[0].differences(player.player()));
					helper.assertTrue(ChamberVisitService.hasCompletedGuidedVisit(player.player()), "the guided lesson is complete");
					helper.assertFalse(ChamberVisitService.isAttuned(player.player()), "the lesson does not attune the player");
					player.moveTo(player.home());
					DialogueView after = player.talkTo(mnemonist);
					helper.assertFalse(after.offers(GUIDED), "the guided lesson is not offered again: " + after.describe());
				})
				.run();
	}

	/**
	 * An item held on the cursor refuses the guided lesson with the unavailable line and touches nothing; with the
	 * item put down, the lesson can be taken.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void guidedChamberRefusedWithCursorItem(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = d3(helper);
		player.player().inventoryMenu.setCarried(new ItemStack(Items.EMERALD, 2)); // ARRANGE: picked up in the inventory screen
		player.player().containerMenu = player.player().inventoryMenu;
		player.talkTo(mnemonist);
		Outcome refused = player.choose(mnemonist, GUIDED);
		refused.said(GUIDED_UNAVAILABLE);
		helper.assertTrue(refused.inventoryDelta().isEmpty() && refused.drops().isEmpty()
						&& player.player().inventoryMenu.getCarried().is(Items.EMERALD),
				"a refused lesson leaves the inventory and cursor alone; " + refused);
		helper.assertFalse(ChamberVisitService.isActive(player.player()) || ChamberVisitService.hasCompletedGuidedVisit(player.player()),
				"a refusal neither starts nor completes the lesson");
		int empty = menuSlotOf(player.player().inventoryMenu, player.player(), ItemStack::isEmpty);
		player.click(player.player().inventoryMenu, empty, 0, ClickType.PICKUP);
		helper.assertTrue(player.player().inventoryMenu.getCarried().isEmpty() && player.count(Items.EMERALD) == 2,
				"the emeralds are put down in the inventory");
		DialogueView again = player.talkTo(mnemonist);
		helper.assertTrue(again.offers(GUIDED), "the lesson is still offered after the refusal: " + again.describe());
		Outcome retry = player.choose(mnemonist, GUIDED);
		if (!chamberLoaded(helper)) {
			PlaythroughLog.note(helper, "guidedChamberRefusedWithCursorItem", "the Chamber of Will is not loaded; retry said "
					+ retry.saidKeys());
			helper.succeed();
			return;
		}
		retry.said(GUIDED_STARTED);
		endVisitNow(player);
		helper.succeed();
	}

	/**
	 * Two players in the guided Chamber: one logs out mid-visit, the other takes lethal damage inside. Each gets the
	 * original inventory back, nothing is duplicated, and neither has the lesson completed by leaving early. Needs the
	 * Chamber of Will loaded; on the GameTest server it fails as NOT RUN. Read from the code, ChamberVisitEvents'
	 * logout and death handlers call returnFromVisit, which completes the guided lesson.
	 */
	// DECISION: D-24 a guided visit left early (logout or death protection) completes the lesson.
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH, required = false)
	public static void guidedChamberLogoutAndLethalDamage(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer leaver = d3(helper);
		PlaythroughPlayer struck = d3(helper, PlaythroughSites.stand(helper, WING_X, WING_Z + 2));
		if (!chamberLoaded(helper)) {
			helper.fail("NOT RUN: the Chamber of Will is not loaded on this server, so the logout and death branches"
					+ " were not exercised; run it from the Chamber-loaded review client (-PchamberLoadedValidation=true)");
			return;
		}
		VisitSnapshot[] before = new VisitSnapshot[2];
		Map<String, Object> seen = new HashMap<>();
		PlaythroughClock.start(helper)
				.then(() -> {
					// The leaver keeps nothing in the crafting grid: the harness's relog does not run the
					// logout-time grid return, so a grid there would measure the harness, not the game.
					kitOut(leaver, false);
					kitOut(struck, true);
					before[0] = VisitSnapshot.of(leaver.player());
					before[1] = VisitSnapshot.of(struck.player());
					leaver.talkTo(mnemonist);
					leaver.choose(mnemonist, GUIDED).said(GUIDED_STARTED);
					struck.talkTo(mnemonist);
					struck.choose(mnemonist, GUIDED).said(GUIDED_STARTED);
				})
				.step(() -> tickBoth(leaver, struck, 20))
				.then(() -> {
					leaver.relog();
					seen.put("logout completes", ChamberVisitService.hasCompletedGuidedVisit(leaver.player()));
					PlaythroughLog.note(helper, "guidedChamberLogoutAndLethalDamage", "a logout mid-visit leaves the guided lesson "
							+ (ChamberVisitService.hasCompletedGuidedVisit(leaver.player()) ? "complete" : "incomplete"));
					helper.assertFalse(ChamberVisitService.isActive(leaver.player()) || inChamber(leaver),
							"a logout ends the visit and brings the player back");
					helper.assertTrue(before[0].matches(leaver.player()),
							"a logout mid-visit restores the inventory: " + before[0].differences(leaver.player()));
					Outcome death = struck.die();
					seen.put("lethal damage", death.notes());
					helper.assertTrue(struck.player().isAlive(), "the Chamber protects a guided visitor from lethal damage; " + death);
					helper.assertFalse(ChamberVisitService.hasCompletedGuidedVisit(struck.player()),
							"lethal damage does not complete the lesson");
					helper.assertTrue(before[1].matches(struck.player()) || ChamberVisitService.isActive(struck.player()),
							"lethal damage does not change the restored inventory");
				})
				.then(() -> endVisitNow(struck))
				.step(() -> struck.tickPlayer(3))
				.then(() -> {
					PlaythroughLog.note(helper, "guidedChamberLogoutAndLethalDamage", "observed " + seen);
					helper.assertTrue(before[1].matches(struck.player()),
							"the struck visitor's inventory comes back unchanged: " + before[1].differences(struck.player()));
					helper.assertFalse((Boolean) seen.get("logout completes"),
							"logging out mid-visit does not complete the guided lesson");
				})
				.run();
	}

	/** FIX: F-13. Sneak-using a Warp Chair while a visit is already active says why nothing happens. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void warpChairSilentRefusal(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer player = d3(helper);
		BlockPos chair = PlaythroughSites.stand(helper, WING_X + 2, WING_Z);
		level.setBlock(chair, BlockInit.warp_chair.get().defaultBlockState(), Block.UPDATE_ALL); // ARRANGE: a chair
		// ARRANGE: a timed visit still on the books (the state a visit leaves while the player is between dimensions).
		CompoundTag data = player.player().getPersistentData();
		data.putBoolean("hemomancy:chamber_visit_active", true);
		data.putString("hemomancy:chamber_visit_mode", "TIMED_CHAIR");
		PlaythroughCleanup.onEnd(helper, () -> {
			data.remove("hemomancy:chamber_visit_active");
			data.remove("hemomancy:chamber_visit_mode");
		});
		emptyHand(player);
		Outcome use = player.sneakUseOn(chair);
		PlaythroughLog.note(helper, "warpChairSilentRefusal", "sneak-use during a visit: " + use);
		helper.assertTrue(use.saidKeys().stream().anyMatch(key -> key.startsWith("message.hemomancy.")),
				"the chair says why it refuses a second visit; " + use);
		helper.succeed();
	}

	// =============================================================================================================
	// Forged events and readable lines
	// =============================================================================================================

	/**
	 * FIX: F-3. The Reliquary lesson and the Anchorite counsel sent from afar (entity id 0) or to a Vicar teach
	 * nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void reliquaryTaughtFromAfarRefused(GameTestHelper helper) {
		Entity vicar = wing(helper, PlaythroughNpcs.Role.VICAR).get(PlaythroughNpcs.Role.VICAR);
		PlaythroughPlayer player = d3(helper);
		// ARRANGE: a referral on the books, so a counsel that slipped through would show.
		HarbingerAdvancementGranter.grantIfNotDone(player.player(), VeinMasonAssignments.D6_REFERRAL);
		List<String> leaks = new ArrayList<>();
		for (int speaker : new int[] {0, vicar.getId()}) {
			Outcome reliquary = player.forge(HarbingerMnemonistDialogueTrees.EVENT_RELIQUARY_TAUGHT, speaker);
			if (reliquary.earned().contains(MnemonicReliquaryProgression.ADV_TAUGHT.getPath())) leaks.add("reliquary@" + speaker);
			Outcome counsel = player.forge(HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL, speaker);
			if (counsel.earned().contains(VeinMasonAssignments.D6_COUNSEL.getPath())) leaks.add("counsel@" + speaker);
		}
		PlaythroughLog.note(helper, "reliquaryTaughtFromAfarRefused", "forged Mnemonist events that took: " + leaks);
		helper.assertTrue(leaks.isEmpty(), "forged Mnemonist lessons teach nothing: " + leaks);
		helper.assertFalse(MnemonicReliquaryProgression.isTaught(player.player()), "the Reliquary was not taught");
		helper.succeed();
	}

	/** FIX: F-8. The Three Answers inspection names the fork family in words, not as a raw id like {@code barbed}. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void threeAnswersForkNameIsReadable(GameTestHelper helper) {
		Entity artificer = wing(helper, PlaythroughNpcs.Role.ARTIFICER).get(PlaythroughNpcs.Role.ARTIFICER);
		PlaythroughPlayer player = d3(helper);
		player.talkTo(artificer);
		player.choose(artificer, HarbingerArtificerDialogueTrees.EVENT_BRIEF_THREE_ANSWERS);
		// ARRANGE: the first fork upgrade as the Armature records it (the Armature is not under test here).
		ArtificerAssignments.onArmatureUpgrade(player.player(), new ItemStack(ItemInit.barbed_helm.get()), 3);
		player.talkTo(artificer);
		Outcome inspect = player.choose(artificer, HarbingerArtificerDialogueTrees.EVENT_INSPECT_THREE_ANSWERS);
		String key = "hemomancy.dialogue.event.artificer_three_answers_inspected";
		Optional<Outcome.ChatLine> line = inspect.chat().stream().filter(chat -> chat.has(key)).findFirst();
		helper.assertTrue(line.isPresent(), "the inspection is reported; " + inspect);
		PlaythroughLog.note(helper, "threeAnswersForkNameIsReadable", "inspection line: " + line.get());
		Set<String> extra = new LinkedHashSet<>(line.get().keys());
		extra.remove(key);
		extra.removeIf(k -> k.startsWith("hemomancy.dialogue.event.artificer_equipment_"));
		helper.assertFalse(line.get().text().matches("(?s).*\\bbarbed\\b.*"),
				"the fork family is not shown as a raw id: " + line.get().text());
		helper.assertFalse(extra.isEmpty(), "the fork family is passed as a translated name: " + line.get());
		helper.succeed();
	}

	/**
	 * DECISION: D-3 Quest rewards drop unowned at the NPC's feet. Expected: a second player cannot take the first
	 * player's Woven Vessel kit.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void twoPlayersWovenVesselKit(GameTestHelper helper) {
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer first = d3(helper);
		PlaythroughPlayer second = d3(helper, mnemonist.blockPosition());
		archiveSet(first);
		first.give(new ItemStack(ItemInit.hematic_memory.get())); // ARRANGE: crafted (wovenVesselTurnIn crafts it)
		first.talkTo(mnemonist);
		Outcome turnIn = first.choose(mnemonist, TURN_IN);
		helper.assertTrue(turnIn.rewardDelivered(), "the first player's turn-in is accepted; " + turnIn);
		second.pickUpNear(mnemonist.blockPosition(), ItemInit.bleeding_bulb.get());
		second.pickUpNear(mnemonist.blockPosition(), ItemInit.vivacious_enzyme.get());
		first.pickUpNear(mnemonist.blockPosition(), ItemInit.bleeding_bulb.get());
		first.pickUpNear(mnemonist.blockPosition(), ItemInit.vivacious_enzyme.get());
		String who = "first has bulb=" + first.count(ItemInit.bleeding_bulb.get()) + " enzymes="
				+ first.count(ItemInit.vivacious_enzyme.get()) + "; second has bulb=" + second.count(ItemInit.bleeding_bulb.get())
				+ " enzymes=" + second.count(ItemInit.vivacious_enzyme.get()) + "; landed " + turnIn.drops();
		PlaythroughLog.note(helper, "twoPlayersWovenVesselKit", who);
		helper.assertTrue(first.count(ItemInit.bleeding_bulb.get()) == 1 && first.count(ItemInit.vivacious_enzyme.get()) == 3
				&& second.count(ItemInit.bleeding_bulb.get()) == 0, "the Woven Vessel kit stays the first player's: " + who);
		helper.succeed();
	}

	// =============================================================================================================
	// Death and the mausoleum
	// =============================================================================================================

	/**
	 * Weave, index the vessel and finish the guided lesson, then die and respawn: the first weave, the indexed vessel
	 * and the guided lesson are all still there, and the Brotherhood floor still starts.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = BATCH)
	public static void deathMidBandKeepsMilestones(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Entity mnemonist = wing(helper, PlaythroughNpcs.Role.MNEMONIST).get(PlaythroughNpcs.Role.MNEMONIST);
		PlaythroughPlayer player = d3(helper);
		Weave weave = new Weave(player, helper.absolutePos(LOOM));
		clearRiteAtEnd(player);
		PlaythroughClock clock = PlaythroughClock.start(helper).then(() -> {
			archiveSet(player);
			player.give(new ItemStack(ItemInit.hematic_memory.get())); // ARRANGE: the blank to index (crafted elsewhere)
			player.talkTo(mnemonist);
			player.choose(mnemonist, TURN_IN).earned(HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE.getPath());
			ChamberVisitService.access(player.player()).setGuidedComplete(true); // ARRANGE: guidedChamberRestoresEverything plays it
		});
		weave.addTo(clock, null);
		clock.then(() -> {
			helper.assertTrue(player.has(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE.getPath()), "woven before death");
			Outcome death = player.die();
			helper.assertTrue(player.player().isDeadOrDying(), "real damage kills the player; " + death);
			player.respawn();
			helper.assertTrue(player.has(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE.getPath()),
					"the first weave survives death");
			helper.assertTrue(player.has(HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE.getPath()),
					"the indexed vessel survives death");
			helper.assertTrue(ChamberVisitService.hasCompletedGuidedVisit(player.player()), "the guided lesson survives death");
			helper.assertTrue(player.degree() == 3 && player.hasCharm(), "still a D3 charm-bearer after respawn");
			RiteSite site = layRite(player, helper.absolutePos(RITE_FOCUS), "sanguine_brotherhood");
			// ARRANGE: layRite hands back the Staff and the projection, which were dropped with the inventory.
			buildRite(player, site);
			player.moveTo(site.focus().north(3).above());
			player.wield(ItemInit.living_staff.get());
			Outcome plant = player.useOn(site.focus());
			helper.assertTrue(CardinalRiteSavedData.get(level).getRite(player.uuid()) != null,
					"the Brotherhood still starts after the death; " + plant);
		}).run();
	}

	/**
	 * A D3 Harbinger in a mausoleum's outer hall: walking onto the threshold slab turns them back a block with the
	 * ward line, and breaking the consecrated stone is refused with a ward strike.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void mausoleumOuterHallsTurnBackD3(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer player = d3(helper, PlaythroughSites.stand(helper, 1, 3));
		WardAnchorBlockEntity anchor = settledAnchor(helper);
		BlockPos stone = PlaythroughSites.stand(helper, 2, 8);
		level.setBlock(stone, Blocks.POLISHED_DEEPSLATE.defaultBlockState(), Block.UPDATE_ALL); // ARRANGE: hall masonry
		Runnable unconsecrate = consecrate(helper, new BoundingBox(stone).inflatedBy(2));
		PlaythroughCleanup.onEnd(helper, unconsecrate);
		PlaythroughClock.start(helper)
				.idle(3)
				.then(() -> {
					AABB slab = anchor.threshold();
					helper.assertTrue(slab != null && anchor.settled(), "the ward anchor is settled");
					player.moveTo(PlaythroughSites.stand(helper, 3, 3));
					player.tickOnce();
					helper.assertTrue(player.player().getX() < slab.minX, "short of the line the player is not held");
					int mark = player.mark();
					int strikes = player.sent(WardStrikePacket.class).size();
					player.moveTo(Vec3.atBottomCenterOf(PlaythroughSites.stand(helper, 4, 3)));
					player.tickOnce();
					Vec3 back = player.player().position();
					helper.assertTrue(back.x < slab.minX && !player.player().getBoundingBox().intersects(slab),
							"stepping onto the slab sets the player back before the line, at " + back);
					helper.assertTrue(player.chatSince(mark).stream().anyMatch(line -> line.has("message.hemomancy.mausoleum_ward.refused")),
							"the ward line is said; " + player.chatSince(mark));
					helper.assertTrue(player.sent(WardStrikePacket.class).size() > strikes, "the threshold flares");

					player.moveTo(PlaythroughSites.stand(helper, 2, 10));
					player.aim(Vec3.atCenterOf(stone));
					int breakMark = player.mark();
					int before = player.sent(WardStrikePacket.class).size();
					boolean broke = player.player().gameMode.destroyBlock(stone);
					helper.assertFalse(broke || level.getBlockState(stone).isAir(), "consecrated stone does not break");
					List<WardStrikePacket> struck = player.sent(WardStrikePacket.class);
					helper.assertTrue(struck.size() > before && struck.getLast().kind() == WardStrikePacket.Kind.BREAK,
							"the refused break strikes the ward: " + struck);
					helper.assertTrue(player.chatSince(breakMark).stream().anyMatch(line -> line.has("message.hemomancy.mausoleum.consecrated")),
							"the player is told the stone is consecrated; " + player.chatSince(breakMark));
				})
				.run();
	}

	// =============================================================================================================
	// Players and places
	// =============================================================================================================

	/** A D3 initiate (ARRANGE: the D2 band's end state) with a full enough pool for this band's costs. */
	private static PlaythroughPlayer d3(GameTestHelper helper) {
		return d3(helper, PlaythroughSites.stand(helper, WING_X, WING_Z));
	}

	private static PlaythroughPlayer d3(GameTestHelper helper, BlockPos at) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, at);
		PlaythroughStates.initiateD3(player); // ARRANGE
		PlaythroughStates.blood(player, PLENTY_OF_BLOOD); // ARRANGE
		return player;
	}

	private static PlaythroughSites.OutpostWing wing(GameTestHelper helper, PlaythroughNpcs.Role... roles) {
		return PlaythroughSites.outpostWing(helper, PlaythroughSites.stand(helper, WING_X, WING_Z), roles);
	}

	private static Entity spawn(GameTestHelper helper, EntityType<?> type, BlockPos at) {
		Entity entity = type.create(helper.getLevel());
		helper.assertTrue(entity != null, type + " could be created");
		entity.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 0.0F, 0.0F);
		helper.assertTrue(helper.getLevel().addFreshEntity(entity), type + " joined the level");
		PlaythroughCleanup.onEnd(helper, entity::discard);
		return entity;
	}

	/** ARRANGE: the Woven Vessel's archive set (book, ink sac, three paper). */
	private static void archiveSet(PlaythroughPlayer player) {
		player.give(new ItemStack(Items.BOOK));
		player.give(new ItemStack(Items.INK_SAC));
		player.give(new ItemStack(Items.PAPER, 3));
	}

	private static boolean knowsRecipe(PlaythroughPlayer player, String path) {
		return player.player().getRecipeBook().contains(Hemomancy.rloc(path));
	}

	private static boolean hasCard(DialogueView view, String titleKey) {
		return view.topics().stream().anyMatch(topic -> titleKey.equals(topic.titleKey()));
	}

	/** Every key a player can see in {@code view}: reachable lines and option texts, card titles. */
	private static List<String> shownKeys(DialogueView view) {
		List<String> keys = new ArrayList<>(view.lineKeys());
		for (DialogueNode node : view.nodes().values()) {
			for (DialogueOption option : node.options()) Dialogue.lineKey(option.text()).ifPresent(keys::add);
		}
		for (DialogueTopic topic : view.topics()) keys.add(topic.titleKey());
		return keys;
	}

	// =============================================================================================================
	// Hands, menus and crafting
	// =============================================================================================================

	/** Takes the first stack matching {@code match} in the main hand, as a player picks it from the hotbar. */
	private static ItemStack wieldWhere(PlaythroughPlayer player, Predicate<ItemStack> match, String what) {
		var inventory = player.player().getInventory();
		if (match.test(inventory.getSelected())) return inventory.getSelected();
		for (int slot = 0; slot < inventory.items.size(); slot++) {
			if (!match.test(inventory.items.get(slot))) continue;
			if (slot < 9) {
				inventory.selected = slot;
			} else {
				ItemStack held = inventory.getSelected();
				inventory.items.set(inventory.selected, inventory.items.get(slot));
				inventory.items.set(slot, held);
			}
			return player.player().getMainHandItem();
		}
		player.helper().fail("the player has no " + what + " to hold");
		return ItemStack.EMPTY;
	}

	/** Selects an empty hotbar slot (moving the held stack into the backpack when the hotbar is full). */
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
		player.helper().fail("the player has no free hand");
	}

	private static int countWhere(PlaythroughPlayer player, Predicate<ItemStack> match) {
		int count = 0;
		var inventory = player.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (match.test(inventory.getItem(slot))) count += inventory.getItem(slot).getCount();
		}
		return count;
	}

	/** The menu slot over the player's own inventory holding a stack that matches; -1 when none does. */
	private static int menuSlotOf(AbstractContainerMenu menu, Player player, Predicate<ItemStack> match) {
		for (int index = 0; index < menu.slots.size(); index++) {
			Slot slot = menu.slots.get(index);
			if (slot.container == player.getInventory() && slot.getContainerSlot() < 36 && match.test(slot.getItem())) return index;
		}
		return -1;
	}

	/** The first full Centrifuge output slot of an open Centrifuge menu; -1 when none is full or no menu is open. */
	private static int outputSlot(AbstractContainerMenu menu) {
		if (!(menu instanceof VialCentrifugeMenu)) return -1;
		for (int slot = VialCentrifugeMenu.OUTPUT_START; slot <= VialCentrifugeMenu.OUTPUT_END; slot++) {
			if (!menu.getSlot(slot).getItem().isEmpty()) return slot;
		}
		return -1;
	}

	/** Puts {@code count} of {@code item} into {@code target}: pick the stack up, right-click it in, put the rest back. */
	private static void placeCount(PlaythroughPlayer player, AbstractContainerMenu menu, int target, ItemLike item, int count) {
		int from = menuSlotOf(menu, player.player(), stack -> stack.is(item.asItem()));
		player.helper().assertTrue(from >= 0, "the player has " + item.asItem() + " to put in slot " + target);
		player.click(menu, from, 0, ClickType.PICKUP);
		for (int i = 0; i < count; i++) player.click(menu, target, 1, ClickType.PICKUP);
		if (!menu.getCarried().isEmpty()) player.click(menu, from, 0, ClickType.PICKUP);
	}

	private static void placeOne(PlaythroughPlayer player, AbstractContainerMenu menu, int target, ItemLike item) {
		placeCount(player, menu, target, item, 1);
	}

	/**
	 * Crafts four Blood-Stained Stone and then a Hematic Memory at the crafting table {@code table}, by clicks: each
	 * ingredient into the grid, then a shift-click on the result.
	 */
	private static void craftBlank(PlaythroughPlayer player, BlockPos table) {
		GameTestHelper helper = player.helper();
		emptyHand(player);
		player.useOn(table);
		AbstractContainerMenu menu = player.player().containerMenu;
		helper.assertTrue(menu instanceof CraftingMenu, "the crafting table opens; " + player.last());
		placeCount(player, menu, 1, BlockInit.venous_stone.get(), 4);
		placeCount(player, menu, 2, ItemInit.sanguine_formation.get(), 4);
		placeCount(player, menu, 3, Items.REDSTONE, 4);
		player.quickMove(menu, 0);
		helper.assertTrue(player.count(ItemInit.blood_stained_stone.get()) == 4,
				"four Blood-Stained Stone are crafted; " + player.last());
		for (int slot : new int[] {1, 3, 7, 9}) placeOne(player, menu, slot, ItemInit.sanguine_formation.get());
		for (int slot : new int[] {2, 4, 6, 8}) placeOne(player, menu, slot, ItemInit.blood_stained_stone.get());
		placeOne(player, menu, 5, ItemInit.neurotic_enzyme.get());
		player.quickMove(menu, 0);
		player.closeMenu();
	}

	/**
	 * ARRANGE: a helmet on, a shield in the offhand, a few stacks carried and, when {@code grid}, planks and diamonds
	 * left in the 2x2 crafting grid by clicks.
	 */
	private static void kitOut(PlaythroughPlayer player, boolean grid) {
		ServerPlayer server = player.player();
		player.give(new ItemStack(Items.OAK_PLANKS, 17));
		player.give(new ItemStack(Items.DIAMOND, 3));
		player.give(new ItemStack(Items.IRON_INGOT, 7));
		server.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
		server.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
		var menu = server.inventoryMenu;
		server.containerMenu = menu;
		if (!grid) return;
		placeCount(player, menu, 1, Items.OAK_PLANKS, 17);
		placeCount(player, menu, 4, Items.DIAMOND, 3);
	}

	/** The inventory (armour and offhand included), the 2x2 grid and the cursor at one moment. */
	private record VisitSnapshot(ListTag inventory, List<ItemStack> grid, ItemStack cursor) {
		static VisitSnapshot of(ServerPlayer player) {
			List<ItemStack> grid = new ArrayList<>();
			for (int slot = 1; slot <= 4; slot++) grid.add(player.inventoryMenu.getSlot(slot).getItem().copy());
			return new VisitSnapshot(player.getInventory().save(new ListTag()), grid, player.inventoryMenu.getCarried().copy());
		}

		boolean matches(ServerPlayer player) {
			return differences(player).isEmpty();
		}

		String differences(ServerPlayer player) {
			VisitSnapshot now = of(player);
			List<String> out = new ArrayList<>();
			if (!inventory.equals(now.inventory)) out.add("inventory " + inventory + " -> " + now.inventory);
			for (int i = 0; i < 4; i++) {
				if (!ItemStack.matches(grid.get(i), now.grid.get(i))) out.add("grid " + (i + 1) + " " + grid.get(i) + " -> " + now.grid.get(i));
			}
			if (!ItemStack.matches(cursor, now.cursor)) out.add("cursor " + cursor + " -> " + now.cursor);
			return String.join("; ", out);
		}
	}

	// =============================================================================================================
	// The Chamber
	// =============================================================================================================

	private static boolean chamberLoaded(GameTestHelper helper) {
		return helper.getLevel().getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL) != null;
	}

	private static boolean inChamber(PlaythroughPlayer player) {
		return player.player().level().dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL);
	}

	/** ARRANGE: runs the visit's clock down to its last tick and lets one player tick end it. */
	private static void endVisitNow(PlaythroughPlayer player) {
		if (!ChamberVisitService.isActive(player.player())) return;
		player.player().getPersistentData().putInt(VISIT_REMAINING, 1);
		player.tickOnce();
	}

	/** Ticks the player each server tick until {@code done}, failing after {@code max} ticks. */
	private static PlaythroughClock.Step tickUntil(PlaythroughPlayer player, int max, BooleanSupplier done, String what) {
		return new PlaythroughClock.Step() {
			@Override
			public boolean tick(int ticks) {
				if (done.getAsBoolean()) return true;
				if (ticks >= max) player.helper().fail("Still waiting after " + max + " ticks for " + what);
				player.tickOnce();
				return done.getAsBoolean();
			}

			@Override
			public String label() {
				return "tick until " + what;
			}
		};
	}

	private static PlaythroughClock.Step tickBoth(PlaythroughPlayer first, PlaythroughPlayer second, int ticks) {
		return new PlaythroughClock.Step() {
			@Override
			public boolean tick(int spent) {
				if (spent >= ticks) return true;
				first.tickOnce();
				second.tickOnce();
				return spent + 1 >= ticks;
			}

			@Override
			public String label() {
				return "tick both players x" + ticks;
			}
		};
	}

	// =============================================================================================================
	// The weave
	// =============================================================================================================

	/**
	 * A first Blood Shot weave at a Loom the player sets down: the steps go onto a clock with {@link #addTo}. The
	 * player is given a Hematic Memory, a Bleeding Bulb, three Vivacious Enzymes and a Blood Projection (ARRANGE:
	 * the Woven Vessel kit and the conjured projection), and the Loom's machine access (ARRANGE: its Working craft).
	 */
	private static final class Weave {
		private final PlaythroughPlayer player;
		private final BlockPos pos;
		private SomaticLoomBlockEntity loom;
		private double bloodBefore;
		private double paid;
		private int memories = -1;

		Weave(PlaythroughPlayer player, BlockPos pos) {
			this.player = player;
			this.pos = pos;
		}

		double paid() {
			return paid;
		}

		int memoriesDropped() {
			return memories;
		}

		/** Adds the weave to {@code clock}; {@code inClimax} runs once the climax has begun, when given. */
		void addTo(PlaythroughClock clock, Consumer<SomaticLoomBlockEntity> inClimax) {
			addTo(clock, null, inClimax);
		}

		/**
		 * As {@link #addTo(PlaythroughClock, Consumer)}, with {@code paid} run once the projection has paid and the
		 * engram is out, before anything is threaded (a relog, a second player at the Loom).
		 */
		void addTo(PlaythroughClock clock, Consumer<SomaticLoomBlockEntity> paid, Consumer<SomaticLoomBlockEntity> inClimax) {
			GameTestHelper helper = player.helper();
			ServerLevel level = helper.getLevel();
			clock.then(() -> {
				ServerPlayer server = player.player();
				MachineAccessEvents.awardMachineCrafted(server, BlockInit.somatic_loom.get()); // ARRANGE
				player.give(new ItemStack(BlockInit.somatic_loom.get())); // ARRANGE
				player.give(new ItemStack(ItemInit.hematic_memory.get())); // ARRANGE
				player.give(new ItemStack(ItemInit.bleeding_bulb.get())); // ARRANGE
				player.give(new ItemStack(ItemInit.vivacious_enzyme.get(), 3)); // ARRANGE
				player.give(new ItemStack(ItemInit.blood_projection.get())); // ARRANGE
				player.moveTo(helper.absolutePos(LOOM_WEAVER));
				player.place(pos, BlockInit.somatic_loom.get());
				helper.assertTrue(level.getBlockEntity(pos) instanceof SomaticLoomBlockEntity, "the Loom is set down; " + player.last());
				loom = (SomaticLoomBlockEntity) level.getBlockEntity(pos);
				int memoriesHeld = player.count(ItemInit.hematic_memory.get());
				int bulbsHeld = player.count(ItemInit.bleeding_bulb.get());
				int enzymesHeld = player.count(ItemInit.vivacious_enzyme.get());
				player.wield(ItemInit.hematic_memory.get());
				player.useOn(pos);
				player.wield(ItemInit.bleeding_bulb.get());
				player.useOn(pos);
				for (int i = 0; i < 3; i++) {
					player.wield(ItemInit.vivacious_enzyme.get());
					player.useOn(pos);
				}
				helper.assertTrue(player.count(ItemInit.hematic_memory.get()) == memoriesHeld - 1
						&& player.count(ItemInit.bleeding_bulb.get()) == bulbsHeld - 1
						&& player.count(ItemInit.vivacious_enzyme.get()) == enzymesHeld - 3,
						"the memory, bulb and three enzymes go into the Loom; " + player.last());
				emptyHand(player);
				for (int click = 0; click < 8 && !loom.getResultItem().is(ItemInit.memory_blood_shot.get()); click++) player.useOn(pos);
				helper.assertTrue(loom.getResultItem().is(ItemInit.memory_blood_shot.get()),
						"an empty-hand click selects Blood Shot; " + player.last());
				bloodBefore = player.blood();
			}).step(() -> player.holdUntil(ItemInit.blood_projection.get(), 300, () -> solidPoint(level, pos), loom::isWeaving));
			if (paid != null) clock.then(() -> paid.accept(loom));
			clock.then(() -> {
						this.paid = bloodBefore - player.blood();
						helper.assertTrue(loom.isWeaving(), "the held projection pays for the weave; " + player.last());
						EngramBoard board = loom.getEngram();
						helper.assertTrue(board != null, "the engram unfurls");
						for (int fragment = 0; fragment < board.fragmentCount(); fragment++) {
							if (board.fragment(fragment).intrusion()) continue;
							helper.assertTrue(LoomWeaveTestSupport.thread(loom, player.player(), board.solution(fragment)),
									"fragment " + fragment + " threads home");
						}
						loom.releaseEngram(player.player());
					});
			if (inClimax != null) {
				clock.until(200, () -> loom.isInClimax() || !loom.isWeaving(), "the climax to begin")
						.then(() -> inClimax.accept(loom));
			}
			clock.until(400, () -> !loom.isWeaving(), "the weave to end")
					.then(() -> memories = drops(level, pos, ItemInit.memory_blood_shot.get(), 4.0D));
		}
	}

	/** A point inside the solid part of {@code pos}'s shape, so a held projection's ray lands on the block. */
	private static Vec3 solidPoint(ServerLevel level, BlockPos pos) {
		VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
		if (shape.isEmpty()) return Vec3.atCenterOf(pos);
		AABB part = shape.toAabbs().stream().max(Comparator.comparingDouble(AABB::getSize)).orElseThrow();
		return part.getCenter().add(pos.getX(), pos.getY(), pos.getZ());
	}

	private static int drops(ServerLevel level, BlockPos near, net.minecraft.world.item.Item item, double radius) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(near).inflate(radius), drop -> drop.getItem().is(item))
				.stream().mapToInt(drop -> drop.getItem().getCount()).sum();
	}

	// =============================================================================================================
	// Rite floors
	// =============================================================================================================

	/**
	 * A rite laid out and then taken apart for the player: {@code pieces} (the subject or the required structure)
	 * and their states, bottom first, to set back by hand; the {@code braziers} and the {@code offerings} each wants.
	 */
	private record RiteSite(CardinalRiteRecipe recipe, BlockPos focus, List<BlockPos> pieces, List<BlockState> states,
			List<BlockPos> braziers, List<ItemStack> offerings) {
	}

	/**
	 * ARRANGE: lays {@code cardinal_rite/<path>} at {@code focus} the way the debug placer does
	 * ({@code PlaceStructurePacket.placeLayeredCardinalRite}: floor, medium, structure or subject, braziers with their
	 * offerings), reads where its pieces and braziers are from the station matcher, then removes the pieces and the
	 * offerings so the player can put them back. A Working craft's braziers are also put out, for the player to light.
	 */
	private static RiteSite layRite(PlaythroughPlayer player, BlockPos focus, String path) {
		GameTestHelper helper = player.helper();
		ServerLevel level = helper.getLevel();
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, Hemomancy.rloc("cardinal_rite/" + path));
		helper.assertTrue(recipe != null, "cardinal_rite/" + path + " is loaded");
		helper.assertTrue(PlaceStructurePacket.placeLayeredCardinalRite(level, focus, recipe, player.player()) >= 0,
				"cardinal_rite/" + path + " can be laid at " + focus.toShortString());
		var resolution = CardinalRiteStationMatcher.resolve(level, focus, List.of(recipe));
		helper.assertTrue(resolution.status() == CardinalRiteStationMatcher.Status.MATCHED,
				"the laid " + path + " matches its own recipe: " + resolution.status());
		var station = resolution.matches().getFirst().station();
		List<BlockPos> pieces = new ArrayList<>();
		List<BlockState> states = new ArrayList<>();
		if (station.subject() != null) {
			pieces.addAll(station.subject().positions());
			states.addAll(station.subject().states());
		} else {
			for (BlockPos pos : BlockPos.betweenClosed(focus.offset(-3, 1, -3), focus.offset(3, 5, 3))) {
				if (level.getBlockState(pos).is(BlockInit.hematic_iron_pillar.get())) {
					pieces.add(pos.immutable());
					states.add(level.getBlockState(pos));
				}
			}
		}
		helper.assertTrue(!pieces.isEmpty(), path + " has pieces for the player to set");
		List<Integer> order = new ArrayList<>();
		for (int i = 0; i < pieces.size(); i++) order.add(i);
		order.sort(Comparator.comparingInt(i -> pieces.get(i).getY()));
		List<BlockPos> sortedPieces = order.stream().map(pieces::get).toList();
		List<BlockState> sortedStates = order.stream().map(states::get).toList();
		for (int i = sortedPieces.size() - 1; i >= 0; i--) {
			level.setBlock(sortedPieces.get(i), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		}
		List<BlockPos> braziers = new ArrayList<>();
		List<ItemStack> offerings = new ArrayList<>();
		for (var brazier : station.braziers()) {
			braziers.add(brazier.pos().immutable());
			offerings.add(brazier.stack().copyWithCount(1));
			IronBrazierBlockEntity entity = brazierAt(level, brazier.pos());
			entity.extractOffering();
			if (recipe.isWorkingCraft()) {
				entity.setPooledBlood(0.0D);
				level.setBlock(brazier.pos(), level.getBlockState(brazier.pos()).setValue(BrazierBlock.RITUAL_PHASE, 0),
						Block.UPDATE_ALL);
			}
		}
		riteTools(player);
		return new RiteSite(recipe, focus.immutable(), sortedPieces, sortedStates, braziers, offerings);
	}

	/**
	 * ARRANGE: the Living Staff (crafted at D1 in the previous band) and a conjured Blood Projection, which a rite's
	 * player plants and projects with; given only when the player does not carry one already.
	 */
	private static void riteTools(PlaythroughPlayer player) {
		if (player.count(ItemInit.living_staff.get()) == 0) player.give(new ItemStack(ItemInit.living_staff.get()));
		if (player.count(ItemInit.blood_projection.get()) == 0) player.give(new ItemStack(ItemInit.blood_projection.get()));
	}

	/** The player sets the rite's pieces (bottom first) and puts each brazier's offering in by hand. */
	private static void buildRite(PlaythroughPlayer player, RiteSite site) {
		buildPieces(player, site);
		ServerLevel level = player.helper().getLevel();
		for (int i = 0; i < site.braziers().size(); i++) {
			BlockPos brazier = site.braziers().get(i);
			ItemStack offering = site.offerings().get(i);
			player.give(offering.copy()); // ARRANGE: the offering is the player's
			player.wield(offering.getItem());
			player.useOn(brazier);
			player.helper().assertTrue(brazierAt(level, brazier).hasOffering(), "the offering goes into "
					+ brazier.toShortString() + " by hand; " + player.last());
		}
	}

	private static void buildPieces(PlaythroughPlayer player, RiteSite site) {
		ServerLevel level = player.helper().getLevel();
		for (int i = 0; i < site.pieces().size(); i++) {
			BlockPos pos = site.pieces().get(i);
			Block block = site.states().get(i).getBlock();
			player.give(new ItemStack(block)); // ARRANGE: the blocks are the player's
			player.place(pos, block);
			player.helper().assertTrue(level.getBlockState(pos).is(block),
					"the player sets " + block + " at " + pos.toShortString() + "; " + player.last());
		}
	}

	private static IronBrazierBlockEntity brazierAt(ServerLevel level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier) return brazier;
		throw new IllegalStateException("no iron brazier at " + pos.toShortString());
	}

	/** A spot {@code distance} blocks beyond {@code target}, on the side away from {@code from}, at the target's height. */
	private static BlockPos outward(BlockPos from, BlockPos target, int distance) {
		int dx = Integer.signum(target.getX() - from.getX());
		int dz = Integer.signum(target.getZ() - from.getZ());
		if (dx == 0 && dz == 0) dz = -1;
		return target.offset(dx * distance, 0, dz * distance);
	}

	/**
	 * The level's 20-tick rite recovery pass gives back an escrowed Staff to {@code level.players()}, which the
	 * captured player is not in; this is that pass for the player, run only when the Staff has not come back yet.
	 */
	private static void deliverStaff(PlaythroughPlayer player) {
		if (player.count(ItemInit.living_staff.get()) > 0) return;
		CardinalRiteSavedData.get(player.player().serverLevel()).deliverRecovery(player.player());
	}

	/** Clears a rite this test started without {@link PlaythroughClock#rite}, its threats, daemon and Staff escrow. */
	private static void clearRiteAtEnd(PlaythroughPlayer player) {
		PlaythroughCleanup.onEnd(player.helper(), () -> {
			ServerLevel level = player.player().serverLevel();
			CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
			ActiveCardinalRite rite = saved.getRite(player.uuid());
			if (rite == null) return;
			CardinalRiteOrdealEngine.clearThreats(level, rite);
			CardinalRiteStaffEscrow.restore(player.player(), rite);
			saved.removeRite(player.uuid());
			HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(level, player.uuid(), rite.getCenterPos());
			if (daemon != null) daemon.discard();
		});
	}

	// =============================================================================================================
	// The bed and the mausoleum
	// =============================================================================================================

	private static void placeBed(ServerLevel level, BlockPos foot) {
		int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
		level.setBlock(foot, Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH)
				.setValue(BedBlock.PART, BedPart.FOOT), flags); // ARRANGE: a bed in the player's room
		level.setBlock(foot.south(), Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.SOUTH)
				.setValue(BedBlock.PART, BedPart.HEAD), flags);
	}

	/**
	 * ARRANGE: a ward anchor settled as {@code MausoleumStructure.afterPlace} settles a generated one: in the wall at
	 * relative x 4, its threshold slab across x 4 from the floor up five blocks over z 1..5, the ward running on to
	 * x 9 and the outer hall hallowed from x 0.
	 */
	private static WardAnchorBlockEntity settledAnchor(GameTestHelper helper) {
		BlockPos anchorPos = new BlockPos(4, PlaythroughSites.STAND_Y + 2, 0);
		helper.setBlock(anchorPos, BlockInit.ward_anchor.get().defaultBlockState());
		WardAnchorBlockEntity anchor = helper.getBlockEntity(anchorPos);
		CompoundTag template = new CompoundTag();
		template.putString("Saint", "HEMORATH");
		template.putIntArray("ThresholdFrom", new int[] {0, -2, 1});
		template.putIntArray("ThresholdTo", new int[] {0, 2, 5});
		anchor.loadWithComponents(template, helper.getLevel().registryAccess());
		BoundingBox ward = BoundingBox.fromCorners(helper.absolutePos(new BlockPos(4, PlaythroughSites.STAND_Y - 1, 0)),
				helper.absolutePos(new BlockPos(9, PlaythroughSites.STAND_Y + 5, 6)));
		BoundingBox hallowed = BoundingBox.fromCorners(helper.absolutePos(new BlockPos(0, PlaythroughSites.STAND_Y - 1, 0)),
				helper.absolutePos(new BlockPos(9, PlaythroughSites.STAND_Y + 5, 6)));
		anchor.settle(Rotation.NONE, List.of(ward), List.of(hallowed));
		BlockPos absolute = helper.absolutePos(anchorPos);
		PlaythroughCleanup.onEnd(helper, () -> helper.getLevel().setBlock(absolute, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL));
		return anchor;
	}

	/**
	 * ARRANGE: registers a one-piece Hemorath mausoleum over {@code box} (as {@code MausoleumWardGameTests} does), so
	 * the ground there is hallowed as in a generated complex; the returned action puts the chunks back.
	 */
	private static Runnable consecrate(GameTestHelper helper, BoundingBox box) {
		ServerLevel level = helper.getLevel();
		Structure structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(Hemomancy.rloc("mausoleum_hemorath"));
		return PlaythroughSites.installStructure(level, structure, box);
	}

	// =============================================================================================================
	// English
	// =============================================================================================================

	/**
	 * The shipped English for {@code key}, read once from the classpath {@code en_us.json} (the resource the mod
	 * ships, as {@link LangKeys} reads it); the key itself when it has none.
	 */
	private static synchronized String english(String key) {
		if (english == null) {
			Map<String, String> map = new HashMap<>();
			try (InputStream in = PlaythroughInitiateGameTests.class.getResourceAsStream("/assets/hemomancy/lang/en_us.json")) {
				if (in != null) {
					JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
					for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
						if (entry.getValue().isJsonPrimitive()) map.put(entry.getKey(), entry.getValue().getAsString());
					}
				}
			} catch (IOException exception) {
				throw new IllegalStateException("Could not read en_us.json", exception);
			}
			english = map;
		}
		return english.getOrDefault(key, key);
	}
}
