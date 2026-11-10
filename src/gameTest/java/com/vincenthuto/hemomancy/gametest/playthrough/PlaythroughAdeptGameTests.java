package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtDiscoveryDialogue;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.thelemicmemory.ThelemicMemoryActivationService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.BloodTemperamentEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumBloodFlow;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularDiagnosisRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.VascularSystemEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerCicatrixAnchoriteDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerMnemonistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerVicarDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VeinMasonScarLesson;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ScarStationMenu;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MasonsEffigyMenu;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterMilestone;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketPrepareScarPattern;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketScarCraftingEvent;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketUpdateScarPattern;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ScarStationBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.config.HemoServerConfig;
import com.vincenthuto.hemomancy.gametest.LoomWeaveTestSupport;
import com.vincenthuto.hutoslib.common.item.ItemKnapper;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Band d4_adept: the Harbinger player from the Sanguine Brotherhood (Degree 4) through the Vein-Mason chapter and the
 * Crimson Lodge into Degree 5, played as a person plays it. The Vicar's Mason's Respite directive and map, the Cicatrix
 * Anchorite's lessons at the Respite yard (station, Iron Brazier, Mason's Effigy), the Crimson Lodge rank rite, the
 * continuation kit, and the Mason's fifth-Degree strain, diagnosis, treatment and reward.
 *
 * <p>Preconditions that are not under test are set by {@link PlaythroughStates} or a labelled {@code // ARRANGE}
 * line; every step under test goes through the entry point a real client reaches: the dialogue payload handlers,
 * {@code useOn} of a block, a held channel ({@code hold}), the station and Effigy payloads through
 * {@link PayloadContexts}, menu slot clicks, a planted Living Staff and Blood Projection for rites, and real damage
 * then respawn. Decision probes are {@code required = false} and carry a {@code // DECISION:} line: they assert what
 * a player would expect and log what happens.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughAdeptGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_d4_adept";
	private static final String RITES = "playthrough_d4_adept_rites";

	private static final String UNREADY = "hemomancy.dialogue.event.vein_mason_unready";
	private static final String KNOWN = "hemomancy.dialogue.event.vein_mason_known";
	private static final String REWARD_UNREADY = "hemomancy.dialogue.event.vein_mason_reward_unready";
	private static final String REWARD_KNOWN = "hemomancy.dialogue.event.vein_mason_reward_known";
	private static final String RESPITE_KNOWN = "hemomancy.dialogue.event.vicar_masons_respite_known";
	private static final String RESPITE_MAP_SAID = "hemomancy.dialogue.event.vicar_masons_respite_map";
	private static final String MAP_NOT_FOUND = "item.hemomancy.masons_respite_map.not_found";
	private static final String MAP_POINTS = "item.hemomancy.masons_respite_map.points";
	private static final String TEMPLATE_NOT_PROOF = "message.hemomancy.scar_brazier_rite.the_loadout_takes_but_the_vein";
	private static final String RANK_SEALED = "message.hemomancy.cardinal_rite_activation.the_rank_rite_remains_sealed_complete";
	private static final String VEIN_MASON_CHAPTER = "milestone.hemomancy.chapter.vein_mason";
	private static final String MACHINE_LOCKED = "hemomancy.machine_access.locked";
	private static final String DIAGNOSIS_SAID = "hemomancy.dialogue.event.anchorite_diagnosis";

	/** Where every test that uses the Respite yard builds it (the Anchorite stands here). */
	private static final int RESPITE_X = 8;
	private static final int RESPITE_Z = 8;
	/** The yard spot in reach of the Anchorite, station, Effigy and Brazier. */
	private static final int YARD_X = 10;
	private static final int YARD_Z = 10;
	/** The rite ground: relative y 3 puts the Focus in the template's floor layer (y 4). */
	private static final BlockPos RITE_ORIGIN = new BlockPos(24, 3, 12);

	private PlaythroughAdeptGameTests() {
	}

	// =================================================================================================================
	// The chain from the previous band
	// =================================================================================================================

	/**
	 * The real D3 Main replayed from {@code initiateD3}: the Mnemonist teaches and indexes the Woven Vessel, the Loom
	 * weaves Blood Shot (stocked, chosen and paid by the player, the engram threaded), and the Sanguine Brotherhood
	 * runs its ceremony on its floor with the pillars and powder brazier the player sets. The result must be the state
	 * {@link PlaythroughStates#adeptD4} claims.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 6000, batch = RITES)
	public static void adeptChainFromPreviousBand(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 6));
		PlaythroughStates.initiateD3(p); // ARRANGE: the D3 band's end state
		PlaythroughClock clock = PlaythroughClock.start(helper);
		chain(helper, p, clock);
		clock.run();
	}

	/**
	 * The D3 to D4 chain as steps on {@code clock}, for {@link #adeptChainFromPreviousBand} and the full run: sets up a
	 * Mnemonist, the Loom spot and the Sanguine Brotherhood floor now, then plays the Woven Vessel, the Blood Shot weave
	 * and the Brotherhood, and ends checking the player against {@link PlaythroughStates#adeptD4}. {@code p} must be a
	 * fresh Initiate.
	 */
	public static void chain(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock) {
		BlockPos spot = PlaythroughSites.stand(helper, 8, 6);
		PlaythroughSites.OutpostWing wing = PlaythroughSites.outpostWing(helper, spot, PlaythroughNpcs.Role.MNEMONIST);
		Entity mnemonist = wing.get(PlaythroughNpcs.Role.MNEMONIST);
		p.moveTo(spot);
		PlaythroughStates.blood(p, 2400); // ARRANGE
		tools(p); // ARRANGE: the Living Staff and the two utilities a D3 player carries
		BlockPos loomPos = PlaythroughSites.stand(helper, 8, 18);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "sanguine_brotherhood");
		clock
				.then(() -> {
					DialogueView first = p.talkTo(mnemonist);
					first.assertLangKeys(helper, "the Mnemonist at D3");
					if (first.offers(HarbingerMnemonistDialogueTrees.EVENT_BLANK_MEMORY_RECIPE)) {
						p.choose(mnemonist, HarbingerMnemonistDialogueTrees.EVENT_BLANK_MEMORY_RECIPE);
					}
					// ARRANGE: the blank is crafted and the archive bought (the D3 band covers both).
					p.give(new ItemStack(ItemInit.hematic_memory.get()));
					p.give(new ItemStack(Items.BOOK));
					p.give(new ItemStack(Items.INK_SAC));
					p.give(new ItemStack(Items.PAPER, 3));
					DialogueView ready = p.talkTo(mnemonist);
					helper.assertTrue(ready.offers(HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN),
							"the Mnemonist takes the Woven Vessel turn-in: " + ready);
					Outcome turnIn = p.choose(mnemonist, HarbingerMnemonistDialogueTrees.EVENT_WOVEN_VESSEL_TURN_IN);
					turnIn.earned(adv(HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE));
					Outcome kit = pickUp(p, mnemonist, ItemInit.bleeding_bulb.get(), ItemInit.vivacious_enzyme.get());
					kit.gave(ItemInit.bleeding_bulb.get(), 1).gave(ItemInit.vivacious_enzyme.get(), 3);
					// ARRANGE: the Loom raised (its Working craft is the D3 band's).
					set(helper, loomPos, BlockInit.somatic_loom.get());
					MachineAccessEvents.awardMachineCrafted(p.player(), BlockInit.somatic_loom.get()); // ARRANGE: raised by this player
					p.moveTo(loomPos.north(2));
					wieldStack(p, s -> s.is(ItemInit.hematic_memory.get()), "blank memory");
					p.useOn(loomPos);
					wieldStack(p, s -> s.is(ItemInit.bleeding_bulb.get()), "bleeding bulb");
					p.useOn(loomPos);
					for (int i = 0; i < 3 && p.count(ItemInit.vivacious_enzyme.get()) > 0; i++) {
						wieldStack(p, s -> s.is(ItemInit.vivacious_enzyme.get()), "vivacious enzyme");
						p.useOn(loomPos);
					}
					SomaticLoomBlockEntity loom = loom(p, loomPos);
					for (int click = 0; click < 6 && !weavesBloodShot(p, loom); click++) {
						emptyHand(p);
						p.useOn(loomPos);
					}
					helper.assertTrue(weavesBloodShot(p, loom), "empty-hand clicks choose Blood Shot on the Loom");
				})
				.step(() -> p.holdUntil(ItemInit.blood_projection.get(), 200, () -> Vec3.atCenterOf(loomPos),
						() -> loom(p, loomPos).isWeaving()))
				.then(() -> {
					SomaticLoomBlockEntity loom = loom(p, loomPos);
					helper.assertTrue(loom.isWeaving(), "Projection pays the Blood Shot weave; " + p.last());
					helper.assertTrue(LoomWeaveTestSupport.weaveAll(p.player().serverLevel(), loomPos, loom, p.player()),
							"the engram threads and the weave finishes");
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE)),
							"the first weave is recorded for the weaver");
					p.moveTo(riteStand(floor));
				});
		buildStructure(clock, p, floor);
		braziers(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus()))
				.then(() -> {
					List<String> drift = new ArrayList<>();
					if (p.degree() != 4) drift.add("degree " + p.degree());
					for (ResourceLocation id : List.of(HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE,
							HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE,
							HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_FINISHED,
							HarbingerAdvancementGranter.ADV_DEGREE_4_ADEPT)) {
						if (!p.has(adv(id))) drift.add("missing " + id);
					}
					PlaythroughLog.note(helper, "real D3 Main end state vs adeptD4: " + (drift.isEmpty() ? "same" : drift)
							+ "; rite " + p.last());
					helper.assertTrue(drift.isEmpty(), "the real D3 Main ends where adeptD4 says: " + drift);
				});
	}


	/**
	 * The real D4 Main from {@code adeptD4}: the Vicar's directive and map, the Anchorite's first lesson at the Respite
	 * yard, the supervised carve, the Scar burnt in, the Effigy's personal pattern, the loadout committed, then the
	 * Crimson Lodge. The player must end where {@link PlaythroughStates#illuminatusD5} puts a fixture player, short of
	 * the continuation kit (that is claimed after the Lodge, and is its own test).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 9000, batch = RITES)
	public static void illuminatusChainFromPreviousBand(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, yard(helper));
		PlaythroughStates.adeptD4(p); // ARRANGE: the D3 band's end state
		PlaythroughClock clock = PlaythroughClock.start(helper);
		lodgeChain(helper, p, clock);
		clock.run();
	}

	/**
	 * The D4 to D5 chain as steps on {@code clock}, for {@link #illuminatusChainFromPreviousBand} and the full run: sets
	 * up a Vicar, the Mason's Respite yard and the Crimson Lodge floor now, then plays the Vein-Mason chapter and the
	 * Lodge and ends comparing the player's advancements with {@link PlaythroughStates#illuminatusD5}. {@code p} must
	 * be a fresh Adept.
	 */
	public static void lodgeChain(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 4, 16));
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "illuminatus_rite");
		p.moveTo(PlaythroughSites.stand(helper, 6, 16));
		PlaythroughStates.blood(p, 2000); // ARRANGE: a rested Adept's pool
		tools(p); // ARRANGE: the Living Staff and the two utilities an Adept carries
		VeinMasonScarLesson.Lesson[] lesson = new VeinMasonScarLesson.Lesson[1];
		clock
				.then(() -> {
					DialogueView view = p.approach(vicar).talkTo(vicar);
					view.assertLangKeys(helper, "the D4 Vicar").assertNoDeadEnds(helper, "the D4 Vicar");
					helper.assertTrue(view.offers(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE),
							"the D4 Vicar sends the player to the Mason's Respite: " + view);
					Outcome directive = p.choose(vicar, HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE);
					directive.earned(adv(HarbingerAdvancementGranter.ADV_VICAR_MASONS_RESPITE_DIRECTIVE));
					p.pickUpNear(vicar.blockPosition(), ItemInit.masons_respite_map.get())
							.gave(ItemInit.masons_respite_map.get(), 1);
					p.moveTo(yard(helper));
					DialogueView anchorite = p.talkTo(respite.anchorite());
					anchorite.assertLangKeys(helper, "the Anchorite at D4").assertNoDeadEnds(helper, "the Anchorite at D4");
					lesson[0] = takeFirstLesson(p, respite);
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON)), "the first lesson");
					Outcome carved = carve(p, respite.station(), lesson[0]);
					carved.earned(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_CARVED));
					p.quickMove(p.player().containerMenu, 4).gave(lesson[0].scar().get(), 1);
					p.closeMenu();
				});
		// The scar to burn in is known only once the lesson is taken, so these stretches are built when reached.
		clock.segment("burn the scar in", c -> learnAtBrazier(c, p, respite.brazier(), s -> s.is(lesson[0].scar().get()),
				"the lesson's scar"));
		clock.then(() -> {
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED)),
							"the scar is learnt");
					// ARRANGE: one sheet of motif paper; the first lesson's kit carries none (logged for the user).
					PlaythroughLog.note(helper, "the D4 kit carries " + p.count(ItemInit.runic_motif_paper.get())
							+ " motif paper; the Effigy needs one");
					if (p.count(ItemInit.runic_motif_paper.get()) == 0) p.give(new ItemStack(ItemInit.runic_motif_paper.get()));
					prepareEffigy(p, respite.effigy(), lesson[0].patternScarId());
				})
				.step(() -> p.holdUntil(ItemInit.blood_projection.get(), 400, () -> Vec3.atCenterOf(respite.effigy()),
						() -> !effigy(p, respite.effigy()).hasPendingMotif()))
				.idle(2)
				.then(() -> {
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN)),
							"the charger earns first_effigy_pattern");
					p.pickUpNear(respite.effigy(), ItemInit.scar_pattern.get());
					helper.assertTrue(carries(p, pattern(lesson[0].patternScarId())), "the prepared pattern is carried");
				})
				.segment("commit the loadout", c -> learnAtBrazier(c, p, respite.brazier(),
						pattern(lesson[0].patternScarId()), "the prepared pattern"))
				.then(() -> {
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT)),
							"the committed loadout earns first_effigy_loadout");
					helper.assertTrue(HarbingerChapterProgression.completedChapters(p.player())
							.contains(HarbingerChapterMilestone.VEIN_MASON), "the Vein-Mason chapter is complete");
					PlaythroughStates.blood(p, 2500); // ARRANGE: the pool rested before the Lodge
					p.moveTo(riteStand(floor));
				});
		buildStructure(clock, p, floor);
		braziers(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), 7000, supportSigilNodes(p)))
				.then(() -> {
					helper.assertTrue(p.degree() == 5, "the Crimson Lodge promotes to degree 5; " + p.last());
					PlaythroughPlayer fixture = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 22));
					PlaythroughStates.illuminatusD5(fixture); // ARRANGE: the state this chain must match
					Set<String> played = com.vincenthuto.hemomancy.gametest.PlaythroughFingerprints.earned(p.player());
					Set<String> fixed = com.vincenthuto.hemomancy.gametest.PlaythroughFingerprints.earned(fixture.player());
					Set<String> missing = new java.util.TreeSet<>(fixed);
					missing.removeAll(played);
					// The continuation kit is claimed after the Lodge, so its advancement is not expected here.
					missing.remove(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_REWARD_CLAIMED));
					Set<String> extra = new java.util.TreeSet<>(played);
					extra.removeAll(fixed);
					PlaythroughLog.note(helper, "played D5 vs illuminatusD5: missing=" + missing + " extra=" + extra);
					helper.assertTrue(missing.isEmpty(), "illuminatusD5 grants advancements real play does not reach: "
							+ missing);
				});
	}

	// =================================================================================================================
	// The Vicar's directive and the map
	// =================================================================================================================

	/** The D4 Vicar sends the player to the Mason's Respite: the map drops once, and the next talk shows the follow-up. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void vicarDirectiveDropsTheMapOnce(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 8));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 11));
		PlaythroughStates.adeptD4(p); // ARRANGE
		DialogueView view = p.talkTo(vicar);
		view.assertLangKeys(helper, "the D4 Vicar").assertNoDeadEnds(helper, "the D4 Vicar");
		helper.assertTrue(view.offers(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE),
				"the D4 Vicar offers the Mason's Respite directive: " + view);
		Outcome directive = p.choose(vicar, HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE);
		directive.said(RESPITE_MAP_SAID).earned(adv(HarbingerAdvancementGranter.ADV_VICAR_MASONS_RESPITE_DIRECTIVE));
		p.pickUpNear(vicar.blockPosition(), ItemInit.masons_respite_map.get()).gave(ItemInit.masons_respite_map.get(), 1);
		DialogueView after = p.talkTo(vicar);
		helper.assertFalse(after.offers(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE),
				"the directive is not offered twice: " + after);
		helper.assertTrue(after.hasNode("masons_respite_followup") && !after.hasNode("masons_respite_directive"),
				"the next talk shows only the follow-up: " + after.nodes().keySet());
		after.assertLangKeys(helper, "the Vicar after the directive");
		helper.succeed();
	}

	/** The map, used in a world with no Mason's Respite near, says so (or points at one when the test world has one). */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void mapNotFoundInTheTestWorld(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 11));
		PlaythroughStates.adeptD4(p); // ARRANGE
		p.give(new ItemStack(ItemInit.masons_respite_map.get())); // ARRANGE: the Vicar's map in hand
		p.wield(ItemInit.masons_respite_map.get());
		Outcome used = p.use(InteractionHand.MAIN_HAND);
		boolean notFound = used.saidKeys().contains(MAP_NOT_FOUND);
		boolean points = used.saidKeys().contains(MAP_POINTS);
		PlaythroughLog.note(helper, "map use said " + used.saidKeys());
		helper.assertTrue(notFound || points, "the map always answers its holder; " + used);
		helper.assertTrue(LangKeys.exists(MAP_NOT_FOUND) && LangKeys.exists(MAP_POINTS), "both map lines have English");
		helper.assertTrue(p.count(ItemInit.masons_respite_map.get()) == 1, "using the map keeps it");
		helper.succeed();
	}

	// DECISION: D-4 the Mason's Respite map is never reissued.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void lostMapIsNotReissued(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 8));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 11));
		PlaythroughStates.adeptD4(p); // ARRANGE
		p.talkTo(vicar);
		p.choose(vicar, HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE);
		p.pickUpNear(vicar.blockPosition(), ItemInit.masons_respite_map.get());
		helper.assertTrue(p.count(ItemInit.masons_respite_map.get()) == 1, "the first map is picked up");
		removeAll(p, ItemInit.masons_respite_map.get()); // ARRANGE: the map is lost (burnt, despawned, stolen)
		DialogueView again = p.talkTo(vicar);
		boolean offered = again.offers(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE);
		if (offered) p.choose(vicar, HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE);
		Outcome asked = p.forge(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE, vicar.getId());
		p.pickUpNear(vicar.blockPosition(), ItemInit.masons_respite_map.get());
		PlaythroughLog.note(helper, "lost map: directive offered again=" + offered + ", said known="
				+ asked.saidKeys().contains(RESPITE_KNOWN) + ", follow-up only="
				+ again.hasNode("masons_respite_followup") + ", asking again said " + asked.saidKeys()
				+ ", maps now " + p.count(ItemInit.masons_respite_map.get()));
		helper.assertTrue(p.count(ItemInit.masons_respite_map.get()) >= 1,
				"a player who lost the map can get another from the Vicar; asking said " + asked.saidKeys());
		helper.succeed();
	}

	// =================================================================================================================
	// The Anchorite's first lesson
	// =================================================================================================================

	/** Below D4 the Anchorite shows only the locked greeting, and a forged first lesson is refused as unready. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void anchoriteLockedBelowD4(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, yard(helper));
		PlaythroughStates.initiateD3(p); // ARRANGE
		DialogueView view = p.talkTo(respite.anchorite());
		view.assertLangKeys(helper, "the Anchorite at D3").assertNoDeadEnds(helper, "the Anchorite at D3");
		helper.assertTrue(view.lineKeys().contains("hemomancy.anchorite.locked.line1"), "the D3 Anchorite is locked: " + view);
		helper.assertFalse(view.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON),
				"no first lesson below D4: " + view);
		Outcome forged = p.forge(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON, respite.anchorite().getId());
		forged.said(UNREADY).gaveNothing();
		helper.assertTrue(forged.earned().isEmpty(), "a refused lesson earns nothing; " + forged);
		helper.succeed();
	}

	/**
	 * The first lesson: the kit drops at the Anchorite (pattern for the strongest temper, a blank, its catalyst and a
	 * knapper), the lesson advancement and the Scar Practice Liber page. Asking again says known and gives nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void firstLessonDropsTheKit(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(p.player());
		PlaythroughClock.start(helper)
				.then(() -> {
					DialogueView view = p.talkTo(respite.anchorite());
					view.assertLangKeys(helper, "the Anchorite's first lesson").assertNoDeadEnds(helper, "the first lesson");
					Outcome taught = p.choose(respite.anchorite(), HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON);
					taught.earned(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON));
					helper.assertTrue(taught.rewardDelivered(), "the lesson is delivered; " + taught);
					Outcome kit = pickUp(p, respite.anchorite(), ItemInit.scar_pattern.get(), ItemInit.scar_blank.get(),
							lesson.catalyst(), ItemInit.hematic_iron_knapper.get());
					kit.gave(ItemInit.scar_pattern.get(), 1).gave(ItemInit.scar_blank.get(), 1)
							.gave(lesson.catalyst(), 1).gave(ItemInit.hematic_iron_knapper.get(), 1);
					helper.assertTrue(carries(p, pattern(lesson.patternScarId())),
							"the pattern is for the strongest temper's scar " + lesson.patternScarId());
				})
				.idle(2)
				.then(() -> {
					helper.assertTrue(LiberKnowledgeHelper.hasEntry(p.player(), LiberEntryDefinitions.SCAR_PRACTICE),
							"the lesson unlocks the Liber's Scar Practice page");
					DialogueView after = p.talkTo(respite.anchorite());
					helper.assertFalse(after.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON),
							"the lesson is not offered twice: " + after);
					Outcome repeat = p.forge(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON,
							respite.anchorite().getId());
					repeat.said(KNOWN).gaveNothing();
				})
				.run();
	}

	// FIX: F-10 the Anchorite's trees carry an item_hint node so dialogue_inquiry/cicatrix_anchorite can show.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void anchoriteItemInquiryReachable(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		p.give(new ItemStack(ItemInit.scar_blank.get())); // ARRANGE: carrying an item the Anchorite can speak to
		DialogueView view = p.talkTo(respite.anchorite());
		String node = "item_inquiry/hemomancy/scar_blank";
		helper.assertTrue(view.hasNode(node), "carrying a scar blank shows the Anchorite's inquiry " + node
				+ "; reachable " + view.nodes().keySet());
		DialogueNode inquiry = view.node(node).orElseThrow();
		for (String line : inquiry.lines()) {
			String key = Dialogue.lineKey(line).orElse(line);
			helper.assertTrue(LangKeys.exists(key), "the inquiry line " + key + " has English");
		}
		view.assertLangKeys(helper, "the Anchorite with an inquiry").assertNoDeadEnds(helper, "the Anchorite inquiry");
		helper.succeed();
	}

	// =================================================================================================================
	// Station, Brazier, Effigy
	// =================================================================================================================

	/**
	 * The supervised carve: with the Anchorite at the yard the student opens the Respite's station, loads it by
	 * clicking slots, traces the stencil and carves. The lesson's Scar comes out and the knapper takes 8 wear.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void supervisedCarveAtTheRespiteStation(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = takeFirstLesson(p, respite);
		Outcome carve = carve(p, respite.station(), lesson);
		ScarStationBlockEntity station = station(p, respite.station());
		helper.assertTrue(station.getItem(2).is(lesson.scar().get()),
				"the lesson's scar is in the output slot: " + station.getItem(2) + "; " + carve);
		carve.earned(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_CARVED));
		ItemStack knapper = station.getItem(3);
		helper.assertTrue(knapper.getItem() instanceof ItemKnapper && knapper.getDamageValue() == 8,
				"an 8-row stencil wears the knapper by 8: " + knapper + " damage " + knapper.getDamageValue());
		AbstractContainerMenu menu = p.player().containerMenu;
		p.quickMove(menu, 4).gave(lesson.scar().get(), 1);
		p.closeMenu();
		helper.succeed();
	}

	/**
	 * The Scar burnt in: inserted with a click, the Brazier lit by held Projection (50 mL), then held Absorption takes
	 * the Scar (100 mL). The scar is known, the offering gone, and the station's supervised access is over.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 500, batch = BATCH)
	public static void scarLearnedAtLitBrazier(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = lessonTaken(p); // ARRANGE: lesson taken
		scarCarved(p, lesson); // ARRANGE: and carved
		double[] before = new double[1];
		PlaythroughClock clock = PlaythroughClock.start(helper).then(() -> before[0] = p.blood());
		learnAtBrazier(clock, p, respite.brazier(), s -> s.is(lesson.scar().get()), "the lesson's scar");
		clock.then(() -> {
			double spent = before[0] - p.blood();
			helper.assertTrue(Math.abs(spent - 150.0D) < 1.0D, "lighting and learning cost 150 mL, not " + spent);
			helper.assertTrue(HemoCapabilityAccess.getScarState(p.player()).knowsCerebralScar(lesson.patternScarId()),
					"the scar " + lesson.patternScarId() + " is learnt");
			helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED)), "first_scar_learned");
			helper.assertFalse(brazier(p, respite.brazier()).hasOffering(), "the Scar is consumed");
			emptyHand(p);
			Outcome reopen = p.useOn(respite.station());
			helper.assertFalse(p.player().containerMenu instanceof ScarStationMenu,
					"supervised access ends once the scar is learnt; " + reopen);
			reopen.said(MACHINE_LOCKED);
		}).run();
	}

	/**
	 * The Effigy prepares the personal pattern: the student opens it, selects the learnt scar (the selection payload),
	 * sets motif paper on it and holds Projection until 500 mL is in. The prepared pattern pops and the advancement goes
	 * to the one who charged it.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 600, batch = BATCH)
	public static void effigyPreparationCreditsTheCharger(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = lessonTaken(p); // ARRANGE
		scarCarved(p, lesson); // ARRANGE
		scarLearned(p, lesson); // ARRANGE: the Brazier lesson is its own test
		p.give(new ItemStack(ItemInit.runic_motif_paper.get())); // ARRANGE: the paper the kit leaves out
		double[] before = new double[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					before[0] = p.blood();
					prepareEffigy(p, respite.effigy(), lesson.patternScarId());
				})
				.step(() -> p.holdUntil(ItemInit.blood_projection.get(), 400, () -> Vec3.atCenterOf(respite.effigy()),
						() -> !effigy(p, respite.effigy()).hasPendingMotif()))
				.idle(2)
				.then(() -> {
					helper.assertFalse(effigy(p, respite.effigy()).hasPendingMotif(), "the motif is charged; " + p.last());
					double spent = before[0] - p.blood();
					helper.assertTrue(Math.abs(spent - MasonsEffigyBlockEntity.BLOOD_PER_SCAR) < 1.0D,
							"one scar costs 500 mL, not " + spent);
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN)),
							"the charger earns first_effigy_pattern");
					p.pickUpNear(respite.effigy(), ItemInit.scar_pattern.get()).gave(ItemInit.scar_pattern.get(), 1);
					helper.assertTrue(carries(p, pattern(lesson.patternScarId())), "the prepared pattern names the scar");
				})
				.run();
	}

	/**
	 * The loadout committed: the prepared pattern burnt in a lit Brazier by held Absorption (50 mL). The active scars
	 * are the pattern's, and first_effigy_loadout, continuation_ready and the VEIN_MASON chapter are done.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 500, batch = BATCH)
	public static void commitLoadoutCompletesVeinMason(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = lessonTaken(p); // ARRANGE
		scarCarved(p, lesson); // ARRANGE
		scarLearned(p, lesson); // ARRANGE
		effigyPrepared(p, lesson); // ARRANGE: the Effigy's charge is its own test
		PlaythroughClock clock = PlaythroughClock.start(helper);
		learnAtBrazier(clock, p, respite.brazier(), pattern(lesson.patternScarId()), "the prepared pattern");
		clock.then(() -> {
			Set<ResourceLocation> active = HemoCapabilityAccess.getScarState(p.player()).getActiveCerebralScars();
			helper.assertTrue(active.equals(Set.of(lesson.patternScarId())), "the pattern's scars are active: " + active);
			helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT)), "first_effigy_loadout");
			helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY)), "continuation_ready");
			helper.assertTrue(HarbingerChapterProgression.completedChapters(p.player()).contains(HarbingerChapterMilestone.VEIN_MASON),
					"the Vein-Mason chapter is complete");
		}).run();
	}

	/** The Mason's template pattern, committed without a personal Effigy pattern, takes but proves nothing. */
	@GameTest(template = EMPTY, timeoutTicks = 500, batch = BATCH)
	public static void templatePatternCommitIsNotProof(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = lessonTaken(p); // ARRANGE: the lesson kit, template pattern included
		scarCarved(p, lesson); // ARRANGE
		scarLearned(p, lesson); // ARRANGE
		int mark = p.mark();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		learnAtBrazier(clock, p, respite.brazier(), pattern(lesson.patternScarId()), "the template pattern");
		clock.then(() -> {
			List<Outcome.ChatLine> said = p.chatSince(mark);
			helper.assertTrue(said.stream().anyMatch(line -> line.has(TEMPLATE_NOT_PROOF)),
					"the Brazier says the template is not the Mason's proof: " + said);
			helper.assertFalse(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT)),
					"a template commit is not first_effigy_loadout");
			helper.assertFalse(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY)),
					"a template commit is not continuation_ready");
			helper.assertFalse(HarbingerChapterProgression.completedChapters(p.player()).contains(HarbingerChapterMilestone.VEIN_MASON),
					"the chapter stays open");
		}).run();
	}

	/**
	 * Held Absorption on a lit Brazier holding the template pattern of a scar not yet learnt, and on an unlit Brazier
	 * holding the Scar, burns nothing. Logs whether the player was told why.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 600, batch = BATCH)
	public static void wrongOrUnlitBrazierOffering(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = lessonTaken(p); // ARRANGE
		scarCarved(p, lesson); // ARRANGE
		BlockPos brazierPos = respite.brazier();
		int[] mark = new int[1];
		double[] blood = new double[1];
		PlaythroughClock.start(helper)
				// Lit, the template of an unlearnt scar.
				.then(() -> insert(p, brazierPos, pattern(lesson.patternScarId()), "the template pattern"))
				.step(() -> lightBrazier(p, brazierPos))
				.then(() -> {
					helper.assertTrue(lit(p, brazierPos), "the Brazier lights; " + p.last());
					mark[0] = p.mark();
					blood[0] = p.blood();
				})
				.step(() -> p.hold(ItemInit.blood_absorption.get(), 100, bowl(brazierPos)))
				.then(() -> {
					List<Outcome.ChatLine> said = p.chatSince(mark[0]);
					PlaythroughLog.note(helper, "lit brazier + template of an unlearnt scar: feedback="
							+ (said.isEmpty() ? "none" : said) + ", blood " + (p.blood() - blood[0]));
					helper.assertTrue(brazier(p, brazierPos).hasOffering(), "the template is not consumed");
					helper.assertTrue(HemoCapabilityAccess.getScarState(p.player()).getActiveCerebralScars().isEmpty(),
							"nothing is committed");
					helper.assertFalse(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT)), "no proof");
					emptyHand(p);
					p.sneakUseOn(brazierPos); // take the template back
					helper.assertFalse(brazier(p, brazierPos).hasOffering(), "sneaking takes the offering back");
				})
				// Unlit, the Scar. Absorbing the lit, empty bowl puts it out first.
				.step(() -> p.holdUntil(ItemInit.blood_absorption.get(), 100, () -> bowl(brazierPos),
						() -> !lit(p, brazierPos)))
				.then(() -> {
					helper.assertFalse(lit(p, brazierPos), "drawing on the empty bowl puts the fire out; " + p.last());
					insert(p, brazierPos, s -> s.is(lesson.scar().get()), "the lesson's scar");
					mark[0] = p.mark();
					blood[0] = p.blood();
				})
				.step(() -> p.hold(ItemInit.blood_absorption.get(), 100, bowl(brazierPos)))
				.then(() -> {
					List<Outcome.ChatLine> said = p.chatSince(mark[0]);
					PlaythroughLog.note(helper, "unlit brazier + scar: feedback=" + (said.isEmpty() ? "none" : said)
							+ ", blood " + (p.blood() - blood[0]));
					helper.assertTrue(brazier(p, brazierPos).hasOffering(), "the Scar is not consumed unlit");
					helper.assertFalse(HemoCapabilityAccess.getScarState(p.player()).knowsCerebralScar(lesson.patternScarId()),
							"an unlit Brazier teaches nothing");
					helper.assertFalse(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED)), "no proof");
				})
				.run();
	}

	// DECISION: D-14 the Vein-Mason lesson is recomputed from live temper (refused carve, free extra patterns).
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void temperDriftDuplicatesPatterns(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson given = takeFirstLesson(p, respite);
		// ARRANGE: the player's blood drifts after the lesson (manipulation casts move alignment in play).
		EnumBloodTemper drift = VeinMasonScarLesson.forPlayer(p.player()).patternScarId().getPath().equals("scar_blight")
				? EnumBloodTemper.TENEBRIS : EnumBloodTemper.MORTEM;
		var temper = HemoCapabilityAccess.getBloodTemperament(p.player());
		temper.addTemperAlignment(drift, 5.0F);
		BloodTemperamentEvents.syncTemperament(p.player(), temper);
		VeinMasonScarLesson.Lesson now = VeinMasonScarLesson.forPlayer(p.player());
		helper.assertTrue(!now.patternScarId().equals(given.patternScarId()), "the drift changes the strongest temper");
		Outcome carve = carve(p, respite.station(), given);
		boolean carved = station(p, respite.station()).getItem(2).is(given.scar().get());
		p.closeMenu();
		DialogueView view = p.talkTo(respite.anchorite());
		boolean freeExtra = view.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D4);
		PlaythroughLog.note(helper, "drift " + given.patternScarId() + " -> " + now.patternScarId() + ": carve of the given "
				+ "pattern " + (carved ? "worked" : "refused, said " + carve.saidKeys()) + "; the Anchorite offers a "
				+ "replacement pattern=" + freeExtra + " (" + view.reachableEvents() + ")");
		helper.assertTrue(carved, "the pattern the Mason gave stays carvable after a drift; said " + carve.saidKeys());
		helper.assertFalse(freeExtra, "no free extra pattern is offered while the given one is carried: " + view);
		helper.succeed();
	}

	// DECISION: D-3 a shared Effigy credits whoever finishes the charge, and its pattern drops unowned.
	@GameTest(template = EMPTY, timeoutTicks = 600, batch = BATCH, required = false)
	public static void sharedEffigyChargeCredit(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p1 = adept(helper, yard(helper));
		PlaythroughPlayer p2 = adept(helper, PlaythroughSites.stand(helper, 11, 12));
		VeinMasonScarLesson.Lesson lesson = lessonTaken(p1); // ARRANGE
		scarCarved(p1, lesson); // ARRANGE
		scarLearned(p1, lesson); // ARRANGE
		learnSameScar(p2, lesson); // ARRANGE: P2 knows the same scar
		p1.give(new ItemStack(ItemInit.runic_motif_paper.get())); // ARRANGE
		PlaythroughClock.start(helper)
				.then(() -> prepareEffigy(p1, respite.effigy(), lesson.patternScarId()))
				.step(() -> p1.holdUntil(ItemInit.blood_projection.get(), 300, () -> Vec3.atCenterOf(respite.effigy()),
						() -> effigy(p1, respite.effigy()).getPendingBlood() >= 300.0D))
				.then(() -> helper.assertTrue(effigy(p1, respite.effigy()).hasPendingMotif(),
						"P1's 300 mL leaves the motif unfinished; " + p1.last()))
				.step(() -> p2.holdUntil(ItemInit.blood_projection.get(), 300, () -> Vec3.atCenterOf(respite.effigy()),
						() -> !effigy(p2, respite.effigy()).hasPendingMotif()))
				.idle(2)
				.then(() -> {
					p2.pickUpNear(respite.effigy(), ItemInit.scar_pattern.get());
					boolean p1Credit = p1.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN));
					boolean p2Credit = p2.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN));
					PlaythroughLog.note(helper, "shared charge P1 paid 300, P2 finished: P1 advancement=" + p1Credit
							+ " P2 advancement=" + p2Credit + "; patterns P1=" + p1.count(ItemInit.scar_pattern.get())
							+ " P2=" + p2.count(ItemInit.scar_pattern.get()) + " (P2 walked over the drop)");
					helper.assertTrue(p1Credit, "P1, who set the motif and paid most of it, is credited");
					helper.assertTrue(p2.count(ItemInit.scar_pattern.get()) == 0,
							"P1's pattern is not P2's to take");
				})
				.run();
	}

	/** Learn the scar, die, respawn: the scar stays known, the pattern is in the death drops, and recovered it commits. */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = BATCH)
	public static void deathBetweenLearnAndCommit(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = lessonTaken(p); // ARRANGE
		scarCarved(p, lesson); // ARRANGE
		BlockPos deathSpot = yard(helper);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		learnAtBrazier(clock, p, respite.brazier(), s -> s.is(lesson.scar().get()), "the lesson's scar");
		clock.then(() -> {
			helper.assertTrue(HemoCapabilityAccess.getScarState(p.player()).knowsCerebralScar(lesson.patternScarId()), "learnt");
			removeAll(p, ItemInit.scar_pattern.get()); // ARRANGE: the template is used up elsewhere
			effigyPrepared(p, lesson); // ARRANGE: the Effigy charge is its own test
			p.moveTo(deathSpot);
			Outcome death = p.die();
			helper.assertTrue(p.player().isDeadOrDying(), "real damage kills the player; " + death);
			p.respawn();
			helper.assertTrue(HemoCapabilityAccess.getScarState(p.player()).knowsCerebralScar(lesson.patternScarId()),
					"a death keeps the learnt scar");
			helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN)),
					"a death keeps first_effigy_pattern");
			PlaythroughLog.note(helper, "after respawn: blood active=" + p.bloodActive() + ", blood=" + p.blood()
					+ ", tools in drops: projection=" + dropsOf(p, deathSpot, ItemInit.blood_projection.get())
					+ " absorption=" + dropsOf(p, deathSpot, ItemInit.blood_absorption.get()));
			p.moveTo(deathSpot);
			Outcome recovered = p.pickUpNear(deathSpot, ItemInit.scar_pattern.get());
			recovered.gave(ItemInit.scar_pattern.get(), 1);
			helper.assertTrue(carries(p, pattern(lesson.patternScarId())), "the recovered pattern still names the scar");
			p.moveTo(yard(helper));
			p.pickUpNear(deathSpot, ItemInit.blood_projection.get());
			p.pickUpNear(deathSpot, ItemInit.blood_absorption.get());
			tools(p); // ARRANGE: whatever conjured tool did not survive the drop is remade
			PlaythroughStates.blood(p, 2000); // ARRANGE: the respawned pool refilled
		});
		learnAtBrazier(clock, p, respite.brazier(), pattern(lesson.patternScarId()), "the recovered pattern");
		clock.then(() -> {
			helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT)),
					"the recovered pattern commits the loadout after a death");
			helper.assertTrue(HemoCapabilityAccess.getScarState(p.player()).getActiveCerebralScars()
					.equals(Set.of(lesson.patternScarId())), "the scar is active");
		}).run();
	}

	// =================================================================================================================
	// The Crimson Lodge
	// =================================================================================================================

	/** Without the loadout the Crimson Lodge stays sealed, names the Vein-Mason chapter, and takes nothing. */
	@GameTest(template = EMPTY, timeoutTicks = 600, batch = RITES)
	public static void crimsonLodgeSealedWithoutLoadout(GameTestHelper helper) {
		PlaythroughPlayer p = adept(helper, PlaythroughSites.stand(helper, 20, 12));
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "illuminatus_rite");
		p.moveTo(riteStand(floor));
		PlaythroughClock clock = PlaythroughClock.start(helper);
		buildStructure(clock, p, floor);
		braziers(clock, p, floor);
		clock.then(() -> {
			ItemStack staff = p.wield(ItemInit.living_staff.get()).copy();
			Outcome start = p.useOn(floor.focus());
			start.said(RANK_SEALED).said(VEIN_MASON_CHAPTER);
			helper.assertTrue(CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) == null,
					"a sealed rite does not start; " + start);
			helper.assertTrue(p.player().getMainHandItem().is(staff.getItem()), "the Staff is not escrowed");
			for (BlockPos socket : usedSockets(floor)) {
				helper.assertTrue(brazier(p, socket).hasOffering(), "the offering at " + socket.toShortString() + " stays");
			}
			helper.assertTrue(p.degree() == 4, "still degree 4");
		}).run();
	}

	/**
	 * The Crimson Lodge played through: the player sets the twelve pillars and lights the two offering braziers, plants
	 * the Staff, feeds the anchors and the required Bastion sigil and seals at the daemon, and the ceremony runs out.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 8000, batch = RITES)
	public static void crimsonLodgeCeremonyPromotesToD5(GameTestHelper helper) {
		PlaythroughPlayer p = adept(helper, PlaythroughSites.stand(helper, 20, 12));
		VeinMasonScarLesson.Lesson lesson = lessonTaken(p); // ARRANGE: the Vein-Mason chapter done, by its proofs
		scarCarved(p, lesson); // ARRANGE
		scarLearned(p, lesson); // ARRANGE
		grant(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT);
		PlaythroughStates.blood(p, 2500); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "illuminatus_rite");
		p.moveTo(riteStand(floor));
		PlaythroughClock clock = PlaythroughClock.start(helper);
		buildStructure(clock, p, floor);
		braziers(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), 7000, supportSigilNodes(p)))
				.then(() -> {
					Outcome rite = p.last();
					PlaythroughLog.note(helper, "Crimson Lodge: " + rite);
					helper.assertTrue(p.degree() == 5, "the Crimson Lodge promotes to degree 5; " + rite);
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_DEGREE_5_ILLUMINATUS)), "degree_5_illuminatus");
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_CRIMSON_LODGE_CONSECRATED)),
							"crimson_lodge_consecrated");
					helper.assertTrue(p.count(ItemInit.living_staff.get()) >= 1, "the Staff comes back after the rite");
				})
				.run();
	}

	/** Before the loadout the continuation is unready; after it, exactly one kit; asking again says known. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void continuationRewardOnce(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		Entity mason = respite.anchorite();
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = lessonTaken(p); // ARRANGE
		scarCarved(p, lesson); // ARRANGE
		scarLearned(p, lesson); // ARRANGE
		grant(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN); // ARRANGE: the loadout not committed
		DialogueView early = p.talkTo(mason);
		helper.assertFalse(early.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD),
				"no continuation before the loadout: " + early);
		p.forge(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD, mason.getId())
				.said(REWARD_UNREADY).gaveNothing();
		grant(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_CONTINUATION_READY); // ARRANGE: the loadout committed
		VeinMasonScarLesson.Lesson next = VeinMasonScarLesson.continuationForPlayer(p.player());
		DialogueView ready = p.talkTo(mason);
		ready.assertLangKeys(helper, "the continuation").assertNoDeadEnds(helper, "the continuation");
		Outcome claim = p.choose(mason, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD);
		claim.earned(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_REWARD_CLAIMED));
		helper.assertTrue(claim.rewardDelivered(), "the kit is delivered; " + claim);
		pickUp(p, mason, ItemInit.scar_pattern.get(), ItemInit.scar_blank.get(), next.catalyst(),
				ItemInit.runic_motif_paper.get())
				.gave(ItemInit.scar_pattern.get(), 1).gave(ItemInit.scar_blank.get(), 1).gave(next.catalyst(), 1)
				.gave(ItemInit.runic_motif_paper.get(), 4);
		helper.assertTrue(carries(p, pattern(next.patternScarId())),
				"the continuation pattern is the second temper's " + next.patternScarId());
		DialogueView after = p.talkTo(mason);
		helper.assertFalse(after.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD),
				"the kit is offered once: " + after);
		p.forge(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_CONTINUATION_REWARD, mason.getId())
				.said(REWARD_KNOWN).gaveNothing();
		helper.succeed();
	}

	// =================================================================================================================
	// Forged packets
	// =================================================================================================================

	// FIX: F-3 progression events refuse entityId 0 and a speaker of the wrong type.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void forgedMasonEventsRefused(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 12));
		Entity mason = respite.anchorite();
		PlaythroughPlayer d5 = PlaythroughPlayer.create(helper, yard(helper));
		PlaythroughStates.illuminatusD5(d5); // ARRANGE
		strainToVaricose(d5); // ARRANGE: the diagnosis would apply
		for (int speaker : new int[] {0, vicar.getId()}) {
			Outcome diagnosis = d5.forge(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS, speaker);
			helper.assertFalse(d5.has(adv(VeinMasonAssignments.D5_DIAGNOSED)),
					"a diagnosis forged at speaker " + speaker + " records nothing; " + diagnosis);
		}
		grant(d5, VeinMasonAssignments.D5_DIAGNOSED, VeinMasonAssignments.D5_TREATED, VeinMasonAssignments.D5_READY); // ARRANGE
		removeAll(d5, ItemInit.scar_pattern.get()); // ARRANGE: the continuation pattern is gone, so a replacement would apply
		for (int speaker : new int[] {0, vicar.getId()}) {
			Outcome reward = d5.forge(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD, speaker);
			reward.gaveNothing();
			helper.assertFalse(d5.has(adv(VeinMasonAssignments.D5_REWARD)),
					"a D5 reward forged at speaker " + speaker + " is refused; " + reward);
			Outcome replace = d5.forge(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_REPLACE_D4, speaker);
			replace.gaveNothing();
		}
		PlaythroughPlayer d4 = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 10));
		PlaythroughStates.adeptD4(d4); // ARRANGE: the directive would apply
		for (int speaker : new int[] {0, mason.getId()}) {
			Outcome directive = d4.forge(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE, speaker);
			directive.gaveNothing();
			helper.assertFalse(d4.has(adv(HarbingerAdvancementGranter.ADV_VICAR_MASONS_RESPITE_DIRECTIVE)),
					"a directive forged at speaker " + speaker + " is refused; " + directive);
		}
		helper.succeed();
	}

	// =================================================================================================================
	// Degree 5
	// =================================================================================================================

	/**
	 * The Mason's fifth-Degree work: strained to Varicose (ARRANGE by section health), the diagnosis chosen, Vascular
	 * Mending performed as a rite, Hematic Fortification performed, then the D5 reward chosen and its tier-2 kit picked
	 * up.
	 */
	// FIX: F-5 Vascular Mending records the Mason's prescribed treatment.
	@GameTest(template = EMPTY, timeoutTicks = 9000, batch = RITES)
	public static void d5MasonChainWithVascularMending(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		Entity mason = respite.anchorite();
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, yard(helper));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.blood(p, 2500); // ARRANGE
		tools(p); // ARRANGE
		strainToVaricose(p); // ARRANGE: the Thelemic strain itself is preStrainedSectionNeverCounts' subject
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "vascular_mending");
		CardinalRiteRecipe fortification = CardinalRiteRecipe.getRiteByLocation(p.player().level(),
				Hemomancy.rloc("cardinal_rite/hematic_fortification"));
		helper.assertTrue(fortification != null, "hematic_fortification is loaded");
		PlaythroughClock clock = PlaythroughClock.start(helper)
				.then(() -> {
					DialogueView view = p.talkTo(mason);
					view.assertLangKeys(helper, "the D5 Mason").assertNoDeadEnds(helper, "the D5 Mason");
					Outcome diagnosis = p.choose(mason, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS);
					diagnosis.said(DIAGNOSIS_SAID);
					helper.assertTrue(p.has(adv(VeinMasonAssignments.D5_DIAGNOSED)), "the diagnosis is recorded; " + diagnosis);
					p.closeMenu();
					p.moveTo(riteStand(floor));
				});
		braziers(clock, p, floor, offerings(floor.recipe()));
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus()))
				.then(() -> {
					PlaythroughLog.note(helper, "Vascular Mending: " + p.last());
					helper.assertTrue(p.has(adv(VeinMasonAssignments.D5_TREATED)),
							"Vascular Mending, the Mason's prescription, records the treatment");
				});
		braziers(clock, p, floor, offerings(fortification));
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus()))
				.then(() -> {
					PlaythroughLog.note(helper, "Hematic Fortification: " + p.last());
					helper.assertTrue(HemoCapabilityAccess.getInitiatoryDegree(p.player()).hasHematicFortification(),
							"the fortification takes");
					p.moveTo(yard(helper));
					DialogueView view = p.talkTo(mason);
					helper.assertTrue(view.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD),
							"the Mason offers the D5 reward: " + view);
					VeinMasonScarLesson.Lesson tier2 = VeinMasonScarLesson.strongestForPlayer(p.player(), 2);
					p.choose(mason, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD);
					helper.assertTrue(p.has(adv(VeinMasonAssignments.D5_REWARD)), "the D5 reward is recorded");
					pickUp(p, mason, ItemInit.scar_pattern.get(), ItemInit.scar_blank.get(), tier2.catalyst(),
							ItemInit.runic_motif_paper.get())
							.gave(ItemInit.scar_pattern.get(), 1).gave(ItemInit.scar_blank.get(), 1)
							.gave(tier2.catalyst(), 1).gave(ItemInit.runic_motif_paper.get(), 4);
					helper.assertTrue(carries(p, pattern(tier2.patternScarId())), "the tier-2 pattern " + tier2.patternScarId());
				})
				.run();
	}

	// FIX: F-7 the diagnosis names sections, flows and treatments in English, not enum constants.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void diagnosisMessageIsReadable(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, yard(helper));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		strainToVaricose(p); // ARRANGE
		p.talkTo(respite.anchorite());
		Outcome diagnosis = p.choose(respite.anchorite(), HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS);
		Outcome.ChatLine line = diagnosis.chat().stream().filter(chat -> chat.has(DIAGNOSIS_SAID)).findFirst().orElse(null);
		helper.assertTrue(line != null, "the diagnosis is said; " + diagnosis);
		List<String> raw = new ArrayList<>();
		List<Enum<?>> constants = new ArrayList<>();
		constants.addAll(List.of(EnumVeinSections.values()));
		constants.addAll(List.of(EnumBloodFlow.values()));
		constants.addAll(List.of(VascularDiagnosisRules.Treatment.values()));
		for (Enum<?> constant : constants) {
			if (Pattern.compile("\\b" + Pattern.quote(constant.name()) + "\\b").matcher(line.text()).find()) raw.add(constant.name());
		}
		PlaythroughLog.note(helper, "diagnosis reads: " + line.text());
		helper.assertTrue(raw.isEmpty(), "the diagnosis shows raw enum names " + raw + ": " + line.text());
		helper.succeed();
	}

	// DECISION: D-22 the D5 strain needs a not-Varicose to Varicose change; a pre-strained section never counts.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void preStrainedSectionNeverCounts(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, yard(helper));
		PlaythroughStates.adeptD4(p); // ARRANGE
		ServerPlayer sp = p.player();
		var vascular = HemoCapabilityAccess.getVascularSystem(sp);
		vascular.getVascularSystem().put(EnumVeinSections.ARMS, 30.0F); // ARRANGE: the arms strained before D5
		VascularSystemEvents.syncVascular(sp, vascular);
		PlaythroughStates.illuminatusD5(p); // ARRANGE: promoted with the arms still Varicose
		var memory = HemoCapabilityAccess.thelemicMemory(sp);
		memory.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 1_200); // ARRANGE: a Thelemic memory to strain with
		memory.activate(ThelemicMemory.SANGUINE_FISTS);
		PlaythroughStates.blood(p, 1500); // ARRANGE
		boolean cast = ThelemicMemoryActivationService.tryTrigger(sp, ThelemicMemory.SANGUINE_FISTS, 20);
		boolean counted = p.has(adv(VeinMasonAssignments.D5_VARICOSE));
		DialogueView view = p.talkTo(respite.anchorite());
		PlaythroughLog.note(helper, "pre-strained arms: cast=" + cast + ", degradation config="
				+ HemoServerConfig.VASCULAR_DEGRADATION_ON_MANIP_ENABLED.get() + ", arms now "
				+ HemoCapabilityAccess.getVascularSystem(p.player()).getVascularSystem().get(EnumVeinSections.ARMS)
				+ ", strain recorded=" + counted + ", the Mason says " + view.lineKeys());
		helper.assertTrue(counted, "a section already Varicose counts as the Mason's strain");
		helper.assertTrue(view.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_DIAGNOSIS),
				"the Mason offers the diagnosis: " + view);
		helper.succeed();
	}

	// DECISION: D-5 the Monolith speaks from D5, but it is a D6 craft and the D5 Vicar gives its blueprint.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void d5MonolithCannotSpeak(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 8));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 11));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		DialogueView view = p.talkTo(vicar);
		helper.assertTrue(view.offers("give_sanguine_monolith_blueprint"), "the D5 Vicar hands out the Monolith's scrap: " + view);
		p.choose(vicar, "give_sanguine_monolith_blueprint");
		p.pickUpNear(vicar.blockPosition(), ItemInit.mnemonic_blueprint.get()).gave(ItemInit.mnemonic_blueprint.get(), 1);
		p.wield(ItemInit.mnemonic_blueprint.get());
		Outcome imprint = p.use(InteractionHand.MAIN_HAND);
		CardinalRiteRecipe monolith = CardinalRiteRecipe.getRiteByLocation(p.player().level(),
				Hemomancy.rloc("cardinal_rite/working/sanguine_monolith"));
		helper.assertTrue(monolith != null, "the Monolith's Working craft is loaded");
		int required = RecipeDegreeGates.getRequiredDegree(monolith);
		PlaythroughLog.note(helper, "D5 blueprint: imprint " + imprint + "; the Working craft requires degree " + required
				+ " while the Monolith has a D5 tree; a D5 player cannot raise it");
		helper.assertTrue(required <= 5, "the Monolith a D5 player is sent after can be raised at D5 (craft needs D"
				+ required + ")");
		helper.succeed();
	}

	/** At D5 the Vicar steps back: Lodge, Fane, Armature, Monolith and the covenant hint; no Mason's Respite. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void d5VicarStepsBack(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 8));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 11));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		DialogueView view = p.talkTo(vicar);
		view.assertLangKeys(helper, "the D5 Vicar").assertNoDeadEnds(helper, "the D5 Vicar");
		for (String node : List.of("lodge_lore", "founding_fane_lore", "armature_consecration", "monolith_rumor", "degree_hint")) {
			helper.assertTrue(view.hasNode(node), "the D5 Vicar shows " + node + "; reachable " + view.nodes().keySet());
		}
		List<String> hint = view.node("degree_hint").map(DialogueNode::lines).orElse(List.of());
		helper.assertTrue(hint.contains("hemomancy.vicar.covenant.found_bloodline"),
				"the D5 degree hint is the covenant guidance: " + hint);
		helper.assertFalse(view.offers(HarbingerVicarDialogueTrees.EVENT_MASONS_RESPITE_DIRECTIVE),
				"no Mason's Respite directive at D5");
		helper.assertFalse(view.hasNode("masons_respite_followup") || view.hasNode("masons_respite_directive"),
				"no Mason's Respite option at D5: " + view.nodes().keySet());
		helper.succeed();
	}

	// =================================================================================================================
	// Band-private helpers (candidates for the harness)
	// =================================================================================================================

	// =================================================================================================================
	// Review follow-ups: rank rites interrupted, relogs in the Vein-Mason chapter, the D5 hand-off, decision probes
	// =================================================================================================================

	/** The Sanguine Brotherhood (failure safe_retry): the caster dies mid-ceremony, then tries again. */
	@GameTest(template = EMPTY, timeoutTicks = 9000, batch = RITES)
	public static void brotherhoodInterruptedByDeath(GameTestHelper helper) {
		riteInterrupted(helper, false, true);
	}

	/** The Sanguine Brotherhood: the caster relogs mid-ceremony; the rite waits for them and completes. */
	@GameTest(template = EMPTY, timeoutTicks = 9000, batch = RITES)
	public static void brotherhoodInterruptedByRelog(GameTestHelper helper) {
		riteInterrupted(helper, false, false);
	}

	/** The Crimson Lodge (failure offering_loss): the caster dies mid-ceremony, then tries again. */
	@GameTest(template = EMPTY, timeoutTicks = 9000, batch = RITES)
	public static void crimsonLodgeInterruptedByDeath(GameTestHelper helper) {
		riteInterrupted(helper, true, true);
	}

	/** The Crimson Lodge: the caster relogs mid-ceremony; the rite waits for them and completes. */
	@GameTest(template = EMPTY, timeoutTicks = 9000, batch = RITES)
	public static void crimsonLodgeInterruptedByRelog(GameTestHelper helper) {
		riteInterrupted(helper, true, false);
	}

	/**
	 * A rank rite interrupted as a player interrupts one. The player lays the floor's pillars and braziers, plants the
	 * Staff and feeds the rite for 60 ticks, then either dies to real damage and respawns, or logs out and back in.
	 * After a death: the degree is unchanged, the rite is gone from the saved data, the Living Staff is not lost or
	 * duplicated (it comes back with the death drops), no offering is duplicated, and with the braziers re-armed a
	 * second planting starts the rite again. After a relog (inside the disconnect grace): the rite is still there and
	 * the player finishes it to the next Degree.
	 */
	private static void riteInterrupted(GameTestHelper helper, boolean lodge, boolean death) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 12));
		if (lodge) {
			PlaythroughStates.adeptD4(p); // ARRANGE
			VeinMasonScarLesson.Lesson lesson = lessonTaken(p); // ARRANGE: the Vein-Mason chapter done, by its proofs
			scarCarved(p, lesson); // ARRANGE
			scarLearned(p, lesson); // ARRANGE
			grant(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN,
					HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT);
		} else {
			PlaythroughStates.initiateD3(p); // ARRANGE
			grant(p, HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_COMPLETE,
					HarbingerAdvancementGranter.ADV_MNEMONIST_FIRST_WEAVE_COMPLETE,
					HarbingerAdvancementGranter.ADV_MNEMONIST_WOVEN_VESSEL_FINISHED); // ARRANGE: the Woven Vessel done
		}
		tools(p); // ARRANGE
		PlaythroughStates.blood(p, 2500); // ARRANGE
		int from = p.degree();
		String label = (lodge ? "Crimson Lodge" : "Sanguine Brotherhood") + (death ? " death" : " relog");
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN),
				lodge ? "illuminatus_rite" : "sanguine_brotherhood");
		BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> aim = lodge ? supportSigilNodes(p) : (rite, recipe) -> null;
		p.moveTo(riteStand(floor));
		int[] fed = new int[1];
		int[] offeringsBefore = new int[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		buildStructure(clock, p, floor);
		braziers(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), 7000, aim, true, () -> ++fed[0] > 60))
				.then(() -> {
					CardinalRiteSavedData saved = CardinalRiteSavedData.get(p.player().serverLevel());
					helper.assertTrue(saved.getRite(p.uuid()) != null, label + ": the rite is still running after 60 ticks");
					helper.assertTrue(p.count(ItemInit.living_staff.get()) == 0, "the planted Staff is held by the rite");
					offeringsBefore[0] = offeringsAround(p, floor);
				});
		if (death) {
			clock.then(() -> {
						BlockPos deathSpot = p.player().blockPosition();
						Outcome died = p.die();
						CardinalRiteSavedData saved = CardinalRiteSavedData.get(p.player().serverLevel());
						helper.assertTrue(saved.getRite(p.uuid()) == null, label + ": a death ends the rite; " + died);
						p.respawn();
						p.moveTo(deathSpot);
						for (Item item : List.of(ItemInit.living_staff.get(), ItemInit.blood_projection.get(),
								ItemInit.blood_absorption.get())) {
							p.pickUpNear(deathSpot, item);
						}
						int staffs = p.count(ItemInit.living_staff.get())
								+ dropsOf(p, deathSpot, ItemInit.living_staff.get());
						int offeringsAfter = offeringsAround(p, floor);
						PlaythroughLog.note(helper, label + ": died saying " + died.saidKeys() + "; degree " + p.degree()
								+ "; Staffs held " + p.count(ItemInit.living_staff.get()) + " (all " + staffs
								+ "); offerings " + offeringsBefore[0] + " -> " + offeringsAfter + " ("
								+ floor.recipe().getCeremony().failureProfile() + ")");
						helper.assertTrue(p.degree() == from, "a death keeps the degree at " + from + ", not " + p.degree());
						helper.assertTrue(p.count(ItemInit.living_staff.get()) == 1 && staffs == 1,
								"the Living Staff comes back once, not lost or doubled: held " + p.count(ItemInit.living_staff.get())
										+ ", in all " + staffs);
						helper.assertTrue(offeringsAfter <= offeringsBefore[0],
								"no offering is duplicated: " + offeringsBefore[0] + " -> " + offeringsAfter);
						tools(p); // ARRANGE: whatever conjured tool the death took is remade
						PlaythroughStates.blood(p, 2500); // ARRANGE: the respawned pool rested
					});
			braziers(clock, p, floor);
			clock.then(() -> {
				p.wield(ItemInit.living_staff.get());
				Outcome again = p.useOn(floor.focus());
				CardinalRiteSavedData saved = CardinalRiteSavedData.get(p.player().serverLevel());
				helper.assertTrue(saved.getRite(p.uuid()) != null, label + ": a second planting starts the rite; " + again);
			}).run();
			return;
		}
		clock.then(() -> {
					p.relog();
					CardinalRiteSavedData saved = CardinalRiteSavedData.get(p.player().serverLevel());
					helper.assertTrue(saved.getRite(p.uuid()) != null, label + ": a relog inside the grace keeps the rite");
				})
				.step(() -> PlaythroughClock.rite(p, floor.focus(), 7000, aim, false, () -> false))
				.then(() -> {
					PlaythroughLog.note(helper, label + ": after the relog the rite ended at degree " + p.degree() + "; " + p.last());
					helper.assertTrue(p.degree() == from + 1, "the relogged caster finishes the rite to degree " + (from + 1)
							+ ", not " + p.degree());
					helper.assertTrue(p.count(ItemInit.living_staff.get()) == 1, "the Staff comes back once");
				})
				.run();
	}

	/** Offerings this rite takes, wherever they are: in the floor's braziers, lying near it, or in the pack. */
	private static int offeringsAround(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) {
		int total = 0;
		for (ItemStack offering : offerings(floor.recipe())) {
			for (BlockPos socket : floor.brazierSockets()) {
				if (p.player().serverLevel().getBlockEntity(socket) instanceof IronBrazierBlockEntity brazier
						&& brazier.hasOffering() && ItemStack.isSameItem(brazier.getOfferingForMatching(), offering)) {
					total++;
				}
			}
			total += dropsOf(p, floor.focus(), offering.getItem()) + p.count(offering.getItem());
		}
		return total;
	}

	/**
	 * A prepared pattern naming a scar the player has not learned, committed at a lit Brazier with held Absorption:
	 * the refusal names the scar by its display name, not its id path.
	 */
	// FIX: ScarBrazierRite and ItemScarPattern showed the raw scar id path; they now show item.hemomancy.<scar>.
	@GameTest(template = EMPTY, timeoutTicks = 500, batch = BATCH)
	public static void scarRefusalNamesTheScar(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(p.player());
		p.give(ItemScarPattern.createPreparedPattern(List.of(lesson.patternScarId()))); // ARRANGE: a pattern from a friend
		String nameKey = "item." + lesson.patternScarId().getNamespace() + "." + lesson.patternScarId().getPath();
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					insert(p, respite.brazier(), pattern(lesson.patternScarId()), "the unlearnt pattern");
					mark[0] = p.mark();
				})
				.step(() -> lightBrazier(p, respite.brazier()))
				.step(() -> p.hold(ItemInit.blood_absorption.get(), 30, bowl(respite.brazier())))
				.then(() -> {
					List<Outcome.ChatLine> said = p.chatSince(mark[0]);
					PlaythroughLog.note(helper, "unlearnt-scar refusal: " + said);
					boolean named = said.stream().anyMatch(line -> line.keys().contains(
							"message.hemomancy.scar_brazier_rite.the_pattern_contains_a_scar_you") && line.keys().contains(nameKey));
					helper.assertTrue(LangKeys.exists(nameKey), "the scar has a display name: " + nameKey);
					helper.assertTrue(named, "the refusal names the scar by " + nameKey + ": " + said);
				})
				.run();
	}

	/**
	 * Relogs through the Vein-Mason chapter: after the first lesson with the kit still on the ground, with the Effigy
	 * charge 300 of 500 mL in, and with the scar learnt but the loadout not committed. Nothing is lost: the lesson
	 * advancement stays, the kit is still there to pick up, the Effigy keeps the charge and finishes it, the pattern
	 * pops once and the commit still earns first_effigy_loadout.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 2000, batch = BATCH)
	public static void relogThroughTheVeinMasonChapter(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(p.player());
		double[] charged = new double[1];
		PlaythroughClock clock = PlaythroughClock.start(helper)
				.then(() -> {
					p.talkTo(respite.anchorite());
					Outcome taught = p.choose(respite.anchorite(), HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON);
					helper.assertTrue(taught.rewardDelivered(), "the first lesson is given; " + taught);
					p.relog();
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON)),
							"a relog keeps the first lesson");
					DialogueView after = p.talkTo(respite.anchorite());
					PlaythroughLog.note(helper, "after a relog with the kit on the ground the Anchorite offers "
							+ after.reachableEvents());
					pickUp(p, respite.anchorite(), ItemInit.scar_pattern.get(), ItemInit.scar_blank.get(), lesson.catalyst(),
							ItemInit.hematic_iron_knapper.get()).gave(ItemInit.scar_blank.get(), 1);
					carve(p, respite.station(), lesson).earned(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_CARVED));
					p.quickMove(p.player().containerMenu, 4);
					p.closeMenu();
				});
		learnAtBrazier(clock, p, respite.brazier(), s -> s.is(lesson.scar().get()), "the lesson's scar");
		clock.then(() -> {
					p.give(new ItemStack(ItemInit.runic_motif_paper.get())); // ARRANGE: the paper the kit leaves out
					prepareEffigy(p, respite.effigy(), lesson.patternScarId());
				})
				.step(() -> p.holdUntil(ItemInit.blood_projection.get(), 400, () -> Vec3.atCenterOf(respite.effigy()),
						() -> effigy(p, respite.effigy()).getPendingBlood() >= 300.0D))
				.then(() -> {
					charged[0] = effigy(p, respite.effigy()).getPendingBlood();
					p.relog();
					helper.assertTrue(effigy(p, respite.effigy()).hasPendingMotif()
							&& effigy(p, respite.effigy()).getPendingBlood() >= charged[0] - 0.01D,
							"the Effigy keeps the motif and the " + charged[0] + " mL across a relog");
				})
				.step(() -> p.holdUntil(ItemInit.blood_projection.get(), 400, () -> Vec3.atCenterOf(respite.effigy()),
						() -> !effigy(p, respite.effigy()).hasPendingMotif()))
				.idle(2)
				.then(() -> {
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN)),
							"the relogged charger earns first_effigy_pattern");
					p.pickUpNear(respite.effigy(), ItemInit.scar_pattern.get());
					helper.assertTrue(dropsOf(p, respite.effigy(), ItemInit.scar_pattern.get()) == 0
							&& carries(p, pattern(lesson.patternScarId())), "the pattern pops once and is picked up");
					p.relog();
					helper.assertFalse(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT)),
							"nothing is committed yet");
				});
		learnAtBrazier(clock, p, respite.brazier(), pattern(lesson.patternScarId()), "the prepared pattern");
		clock.then(() -> helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_LOADOUT)),
				"after the relogs the commit still earns first_effigy_loadout")).run();
	}

	/**
	 * The D5 hand-off to the Brined Court: the Vicar's "Who else keeps to the sea?" topic, then a Voyager's Court
	 * explanation, which issues exactly one Relay Chart. Asking again while it is carried gives nothing; with the chart
	 * lost, whether it is reissued is logged.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void d5CourtReferralReachesTheVoyager(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 8));
		HarbingerVoyagerEntity voyager = PlaythroughNpcs.voyager(helper, PlaythroughSites.stand(helper, 6, 8));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 11));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		DialogueView atVicar = p.talkTo(vicar);
		atVicar.assertLangKeys(helper, "the D5 Vicar").assertNoDeadEnds(helper, "the D5 Vicar");
		helper.assertTrue(atVicar.hasNode("court"), "the D5 Vicar has the Court topic: " + atVicar);
		helper.assertTrue(atVicar.optionByText("hemomancy.dialogue.vicar.option.ask_about_court").isPresent(),
				"the Court question is asked in the Vicar's tree: " + atVicar);
		p.approach(voyager);
		DialogueView atVoyager = p.talkTo(voyager);
		atVoyager.assertLangKeys(helper, "the Voyager to a D5 player").assertNoDeadEnds(helper, "the Voyager");
		helper.assertTrue(atVoyager.offers(CourtDiscoveryDialogue.VOYAGER_LEARN), "the Voyager explains the Court: " + atVoyager);
		Outcome learn = p.choose(voyager, CourtDiscoveryDialogue.VOYAGER_LEARN);
		p.pickUpNear(voyager.blockPosition(), ItemInit.relay_chart.get());
		PlaythroughLog.note(helper, "court learn: " + learn.describe() + "; charts carried " + p.count(ItemInit.relay_chart.get()));
		helper.assertTrue(p.count(ItemInit.relay_chart.get()) == 1, "exactly one Relay Chart arrives; " + learn);
		DialogueView again = p.talkTo(voyager);
		helper.assertFalse(again.offers(CourtDiscoveryDialogue.VOYAGER_CHART), "no chart is offered while one is carried: " + again);
		Outcome forged = p.forge(CourtDiscoveryDialogue.VOYAGER_CHART, voyager.getId());
		p.pickUpNear(voyager.blockPosition(), ItemInit.relay_chart.get());
		helper.assertTrue(p.count(ItemInit.relay_chart.get()) == 1, "asking again while carrying gives nothing; " + forged);
		removeAll(p, ItemInit.relay_chart.get()); // ARRANGE: the chart is lost
		DialogueView lost = p.talkTo(voyager);
		boolean reoffered = lost.offers(CourtDiscoveryDialogue.VOYAGER_CHART);
		if (reoffered) p.choose(voyager, CourtDiscoveryDialogue.VOYAGER_CHART);
		p.pickUpNear(voyager.blockPosition(), ItemInit.relay_chart.get());
		PlaythroughLog.note(helper, "lost Relay Chart: reoffered=" + reoffered + ", charts now " + p.count(ItemInit.relay_chart.get()));
		helper.succeed();
	}

	/** Founding a Bloodline, planted on its floor: at D4 it does not start; at D5 the same floor starts it. */
	@GameTest(template = EMPTY, timeoutTicks = 3000, batch = RITES)
	public static void bloodlineFoundingOpensAtD5(GameTestHelper helper) {
		covenantRiteOpensAtD5(helper, "bloodline_founding");
	}

	/** Founding a Fane, planted on its floor: at D4 it does not start; at D5 the same floor starts it. */
	@GameTest(template = EMPTY, timeoutTicks = 3000, batch = RITES)
	public static void foundingFaneOpensAtD5(GameTestHelper helper) {
		covenantRiteOpensAtD5(helper, "founding_fane");
	}

	/**
	 * A D5 Covenant rite's floor laid, its pillars and braziers set by the player. A D4 player plants the Staff and
	 * the rite does not start (what they are told is logged); promoted to D5, the same planting starts it.
	 */
	private static void covenantRiteOpensAtD5(GameTestHelper helper, String recipe) {
		PlaythroughPlayer p = adept(helper, PlaythroughSites.stand(helper, 20, 12));
		PlaythroughStates.blood(p, 3000); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), recipe);
		p.moveTo(riteStand(floor));
		PlaythroughClock clock = PlaythroughClock.start(helper);
		buildStructure(clock, p, floor);
		braziers(clock, p, floor);
		if (floor.recipe().hasMedium()) {
			clock.then(() -> {
				Item medium = floor.recipe().getMedium().getItems()[0].getItem();
				p.give(new ItemStack(medium)); // ARRANGE: the medium is made elsewhere (the Fane's Sanguine Quintessence)
				p.wield(medium);
				Outcome seated = p.useOn(floor.focus());
				PlaythroughLog.note(helper, recipe + ": seating the medium " + medium + ": " + seated.describe());
				helper.assertTrue(p.count(medium) == 0, "the player seats the medium in the Focus; " + seated);
			});
		}
		clock.then(() -> {
			p.wield(ItemInit.living_staff.get());
			Outcome early = p.useOn(floor.focus());
			CardinalRiteSavedData saved = CardinalRiteSavedData.get(p.player().serverLevel());
			PlaythroughLog.note(helper, recipe + " at D4 said " + early.saidKeys());
			helper.assertTrue(saved.getRite(p.uuid()) == null, recipe + " does not start at D4; " + early);
			PlaythroughStates.illuminatusD5(p); // ARRANGE: the Crimson Lodge is its own test
			tools(p); // ARRANGE
			PlaythroughStates.blood(p, 3000); // ARRANGE
			p.moveTo(riteStand(floor));
			p.wield(ItemInit.living_staff.get());
			Outcome started = p.useOn(floor.focus());
			if (recipe.equals("founding_fane")) {
				// The Fane comes after the Bloodline: without one the planting is refused with a line saying so.
				helper.assertTrue(saved.getRite(p.uuid()) == null && started.saidKeys().contains(
						"message.hemomancy.cardinal_rite_activation.the_founding_fane_requires_an_established"),
						"a D5 player with no bloodline is told the Fane needs one; " + started);
				foundedBloodline(p); // ARRANGE: the Bloodline Founding is its own test
				p.wield(ItemInit.living_staff.get());
				started = p.useOn(floor.focus());
			}
			PlaythroughLog.note(helper, recipe + " at D5: " + started.describe());
			helper.assertTrue(saved.getRite(p.uuid()) != null, recipe + " starts at D5; " + started);
			clearRiteNow(p);
		}).run();
	}

	/** ARRANGE: the player founded and leads a registered bloodline (disbanded when the test ends). */
	private static void foundedBloodline(PlaythroughPlayer p) {
		ServerPlayer server = p.player();
		HemoCapabilityAccess.getInitiatoryDegree(server).setHasFoundedBloodline(true);
		var data = com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData
				.get(server.server.overworld());
		var line = new com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline(
				"Fane test line", server.getUUID(), java.util.UUID.randomUUID(), new ArrayList<>());
		data.registerBloodline(line);
		PlaythroughCleanup.onEnd(p.helper(), () -> data.disbandBloodline(line.getBloodlineUUID()));
		var volume = HemoCapabilityAccess.getBloodVolume(server);
		volume.setBloodLine(line);
		com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents.syncVolume(server, volume);
	}

	/** Ends the player's running rite at once, Staff restored, as the clock's cleanup does at the end of a test. */
	private static void clearRiteNow(PlaythroughPlayer p) {
		ServerLevel level = p.player().serverLevel();
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
		ActiveCardinalRite rite = saved.getRite(p.uuid());
		if (rite == null) return;
		com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine.clearThreats(level, rite);
		com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow.restore(p.player(), rite);
		saved.removeRite(p.uuid());
	}

	// --- Decision probes -------------------------------------------------------------------------------------------

	/** The Vicar's Monolith scrap asked for twice at D5: one per player, or one only while none is carried. */
	// DECISION: D-27 the D5 Monolith scrap can be taken again and again.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void monolithScrapIsRepeatable(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 8));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 11));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		for (int ask = 0; ask < 3; ask++) {
			p.talkTo(vicar);
			p.choose(vicar, "give_sanguine_monolith_blueprint");
			p.pickUpNear(vicar.blockPosition(), ItemInit.mnemonic_blueprint.get());
		}
		int carried = p.count(ItemInit.mnemonic_blueprint.get());
		PlaythroughLog.note(helper, "D-27 three asks for the Monolith scrap: " + carried + " carried");
		helper.assertTrue(carried <= 1, "D-27 expected: one Monolith scrap at a time; " + carried + " carried");
		helper.succeed();
	}

	/**
	 * A reward the Anchorite refuses (the D5 reward before D5_READY) moves the dialogue to a node that may say it was
	 * given. Logs the option's next node and what the server did; expects a refusal line at least.
	 */
	// DECISION: D-28 a refused reward leaves the client on a "claimed" node with no refusal line.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void refusedRewardNodeContradictsServer(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, yard(helper));
		PlaythroughStates.illuminatusD5(p); // ARRANGE: D5, the strain not yet caused
		DialogueView view = p.talkTo(respite.anchorite());
		var option = view.option(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD);
		Outcome asked = option.isPresent() && option.get().presentation().enabled()
				? p.choose(respite.anchorite(), HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD)
				: p.forge(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D5_REWARD, respite.anchorite().getId());
		String next = option.map(o -> o.nextNodeId()).orElse("(not shown)");
		PlaythroughLog.note(helper, "D-28 early D5 reward: shown=" + option.isPresent() + ", next node " + next + ", server "
				+ asked.describe());
		// The premise holds only when the option is shown before it is ready; a hidden option leaves no player there.
		helper.assertTrue(option.isEmpty() || asked.rewardDelivered() || !asked.saidKeys().isEmpty(),
				"D-28 expected: a refused reward says why; the client was sent to " + next);
		helper.succeed();
	}

	/** With the vascular-degradation server config off, the D5 Mason strain can never be recorded. */
	// DECISION: D-29 the D5 Mason chapter needs VASCULAR_DEGRADATION_ON_MANIP_ENABLED on.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void d5StrainWithDegradationOff(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, yard(helper));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		ServerPlayer sp = p.player();
		var memory = HemoCapabilityAccess.thelemicMemory(sp);
		memory.learnAndAddReserve(ThelemicMemory.SANGUINE_FISTS, 1_200); // ARRANGE: a Thelemic memory to strain with
		memory.activate(ThelemicMemory.SANGUINE_FISTS);
		var vascular = HemoCapabilityAccess.getVascularSystem(sp);
		vascular.getVascularSystem().put(EnumVeinSections.ARMS, 30.4F); // ARRANGE: one cast from Varicose
		VascularSystemEvents.syncVascular(sp, vascular);
		boolean before = HemoServerConfig.VASCULAR_DEGRADATION_ON_MANIP_ENABLED.get();
		int casts = 0;
		try {
			HemoServerConfig.VASCULAR_DEGRADATION_ON_MANIP_ENABLED.set(false); // ARRANGE: a server that turned it off
			for (int i = 0; i < 6; i++) {
				PlaythroughStates.blood(p, 1500); // ARRANGE
				if (ThelemicMemoryActivationService.tryTrigger(sp, ThelemicMemory.SANGUINE_FISTS, 20 + i)) casts++;
			}
		} finally {
			HemoServerConfig.VASCULAR_DEGRADATION_ON_MANIP_ENABLED.set(before);
		}
		boolean counted = p.has(adv(VeinMasonAssignments.D5_VARICOSE));
		DialogueView view = p.talkTo(respite.anchorite());
		PlaythroughLog.note(helper, "D-29 degradation off: " + casts + " casts, arms "
				+ HemoCapabilityAccess.getVascularSystem(sp).getVascularSystem().get(EnumVeinSections.ARMS)
				+ ", strain recorded=" + counted + ", the Mason says " + view.lineKeys());
		helper.assertTrue(counted, "D-29 expected: a server with degradation off still has a way through the D5 strain");
		helper.succeed();
	}

	/** The scar blank lost before the first carve: the Anchorite replaces only the pattern. */
	// DECISION: D-30 a lost scar_blank before the first carve cannot be replaced (loot only).
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void lostBlankBeforeCarve(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		takeFirstLesson(p, respite);
		removeAll(p, ItemInit.scar_blank.get()); // ARRANGE: the blank is lost
		DialogueView view = p.talkTo(respite.anchorite());
		List<String> offered = view.reachableEvents();
		for (String event : offered) {
			if (event.startsWith("vein_mason_replace")) p.choose(respite.anchorite(), event);
		}
		p.pickUpNear(respite.anchorite().blockPosition(), ItemInit.scar_blank.get());
		PlaythroughLog.note(helper, "D-30 lost blank: the Anchorite offers " + offered + "; blanks now "
				+ p.count(ItemInit.scar_blank.get()));
		helper.assertTrue(p.count(ItemInit.scar_blank.get()) >= 1, "D-30 expected: a lost blank can be replaced");
		helper.succeed();
	}

	/** Which D4 teacher, if any, names the Sporitic Thurible, including item inquiries while holding a spore sac. */
	// DECISION: D-31 no Harbinger NPC teaches the D4 Sporitic Thurible.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void d4ThuribleHasATeacher(GameTestHelper helper) {
		PlaythroughSites.MasonsRespite respite = respite(helper);
		PlaythroughPlayer p = adept(helper, yard(helper));
		p.give(new ItemStack(ItemInit.spore_sac.get())); // ARRANGE: a spore sac in hand
		p.wield(ItemInit.spore_sac.get());
		List<Entity> teachers = List.of(PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 14, 6)),
				PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 14, 8)),
				PlaythroughNpcs.artificer(helper, PlaythroughSites.stand(helper, 6, 12)),
				PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 6, 14)), respite.anchorite());
		List<String> pointers = new ArrayList<>();
		for (Entity teacher : teachers) {
			p.approach(teacher);
			DialogueView view = p.talkTo(teacher);
			for (String key : view.lineKeys()) {
				String text = net.minecraft.locale.Language.getInstance().getOrDefault(key, key);
				if (key.contains("thurible") || text.contains("Thurible")) pointers.add(PlaythroughPlayer.describe(teacher) + ": " + key);
			}
		}
		PlaythroughLog.note(helper, "D-31 Thurible pointers at D4: " + pointers);
		helper.assertFalse(pointers.isEmpty(), "D-31 expected: some D4 teacher names the Sporitic Thurible");
		helper.succeed();
	}

	private static String adv(ResourceLocation id) {
		return id.getPath();
	}

	private static void grant(PlaythroughPlayer p, ResourceLocation... ids) {
		for (ResourceLocation id : ids) HarbingerAdvancementGranter.grantIfNotDone(p.player(), id);
	}

	private static BlockPos yard(GameTestHelper helper) {
		return PlaythroughSites.stand(helper, YARD_X, YARD_Z);
	}

	private static PlaythroughSites.MasonsRespite respite(GameTestHelper helper) {
		return PlaythroughSites.masonsRespite(helper, PlaythroughSites.stand(helper, RESPITE_X, RESPITE_Z));
	}

	/** ARRANGE: a degree 4 player with a full pool and the blood tools. */
	private static PlaythroughPlayer adept(GameTestHelper helper, BlockPos at) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, at);
		PlaythroughStates.adeptD4(p);
		PlaythroughStates.blood(p, 2000);
		tools(p);
		return p;
	}

	/** ARRANGE: Blood Projection, Blood Absorption and a Living Staff, each given only when missing. */
	private static void tools(PlaythroughPlayer p) {
		for (Item tool : List.of(ItemInit.blood_projection.get(), ItemInit.blood_absorption.get(), ItemInit.living_staff.get())) {
			if (p.count(tool) == 0) p.give(new ItemStack(tool));
		}
	}

	/** ARRANGE: the first lesson as the Anchorite gives it, the kit already in the inventory. */
	private static VeinMasonScarLesson.Lesson lessonTaken(PlaythroughPlayer p) {
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(p.player());
		grant(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON);
		p.give(lesson.patternStack());
		p.give(new ItemStack(ItemInit.scar_blank.get()));
		p.give(new ItemStack(lesson.catalyst()));
		p.give(new ItemStack(ItemInit.hematic_iron_knapper.get()));
		return lesson;
	}

	/** ARRANGE: the carve done, its Scar in the inventory and the blank and catalyst spent. */
	private static void scarCarved(PlaythroughPlayer p, VeinMasonScarLesson.Lesson lesson) {
		removeOne(p, ItemInit.scar_blank.get());
		removeOne(p, lesson.catalyst());
		grant(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_CARVED);
		p.give(new ItemStack(lesson.scar().get()));
	}

	/** ARRANGE: the Scar burnt in at the Brazier. */
	private static void scarLearned(PlaythroughPlayer p, VeinMasonScarLesson.Lesson lesson) {
		removeOne(p, lesson.scar().get());
		HemoCapabilityAccess.getScarState(p.player()).addKnownCerebralScar(lesson.patternScarId());
		grant(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED);
	}

	/** ARRANGE: a second student who learnt the same scar. */
	private static void learnSameScar(PlaythroughPlayer p, VeinMasonScarLesson.Lesson lesson) {
		grant(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_LESSON,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_CARVED,
				HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_SCAR_LEARNED);
		HemoCapabilityAccess.getScarState(p.player()).addKnownCerebralScar(lesson.patternScarId());
	}

	/** ARRANGE: the Effigy's prepared pattern in the inventory and its advancement. */
	private static void effigyPrepared(PlaythroughPlayer p, VeinMasonScarLesson.Lesson lesson) {
		grant(p, HarbingerAdvancementGranter.ADV_VEIN_MASON_FIRST_EFFIGY_PATTERN);
		p.give(ItemScarPattern.createPreparedPattern(List.of(lesson.patternScarId())));
	}

	/** ARRANGE: three sections Varicose and the strain recorded through its recorder (the cast is not under test). */
	private static void strainToVaricose(PlaythroughPlayer p) {
		ServerPlayer sp = p.player();
		var vascular = HemoCapabilityAccess.getVascularSystem(sp);
		for (EnumVeinSections section : List.of(EnumVeinSections.HEAD, EnumVeinSections.ARMS, EnumVeinSections.BODY)) {
			vascular.getVascularSystem().put(section, 30.0F);
		}
		VascularSystemEvents.syncVascular(sp, vascular);
		VeinMasonAssignments.onThelemicStrain(sp, EnumBloodFlow.STABLE, EnumBloodFlow.VARICOSE);
		p.helper().assertTrue(p.has(adv(VeinMasonAssignments.D5_VARICOSE)), "the strain is recorded for the Mason");
	}

	/** The first lesson taken for real: talk, choose, pick the kit up at the Anchorite's feet. */
	private static VeinMasonScarLesson.Lesson takeFirstLesson(PlaythroughPlayer p, PlaythroughSites.MasonsRespite respite) {
		VeinMasonScarLesson.Lesson lesson = VeinMasonScarLesson.forPlayer(p.player());
		p.talkTo(respite.anchorite());
		Outcome taught = p.choose(respite.anchorite(), HarbingerCicatrixAnchoriteDialogueTrees.EVENT_FIRST_LESSON);
		p.helper().assertTrue(taught.rewardDelivered(), "the first lesson is delivered; " + taught);
		pickUp(p, respite.anchorite(), ItemInit.scar_pattern.get(), ItemInit.scar_blank.get(), lesson.catalyst(),
				ItemInit.hematic_iron_knapper.get());
		return lesson;
	}

	/**
	 * The carve, as the station screen plays it: open with an empty hand, click the knapper, blank, catalyst and the
	 * pattern into their slots, send the traced stencil, then the carve payload. Returns the carve's outcome; the menu
	 * stays open.
	 */
	private static Outcome carve(PlaythroughPlayer p, BlockPos stationPos, VeinMasonScarLesson.Lesson lesson) {
		GameTestHelper helper = p.helper();
		emptyHand(p);
		Outcome open = p.useOn(stationPos);
		helper.assertTrue(p.player().containerMenu instanceof ScarStationMenu,
				"the supervised student opens the Respite station; " + open);
		AbstractContainerMenu menu = p.player().containerMenu;
		putInSlot(p, menu, s -> s.getItem() instanceof ItemKnapper, 0, "knapper");
		putInSlot(p, menu, s -> s.is(ItemInit.scar_blank.get()), 1, "scar blank");
		putInSlot(p, menu, s -> s.is(lesson.catalyst()), 2, "catalyst");
		putInSlot(p, menu, s -> s.is(ItemInit.scar_pattern.get()), 3, "scar pattern");
		ScarStationBlockEntity station = station(p, stationPos);
		ScarRecipe recipe = station.getCurrentRecipe();
		helper.assertTrue(recipe != null && recipe.getPattern() != null, "the loaded station has a scar recipe");
		byte[][] trace = new byte[recipe.getPattern().length][];
		for (int row = 0; row < trace.length; row++) trace[row] = recipe.getPattern()[row].clone();
		PacketUpdateScarPattern.handle(new PacketUpdateScarPattern(trace), PayloadContexts.of(p.player()));
		Outcome.Probe probe = Outcome.begin(p, null, "carve at " + stationPos.toShortString());
		PacketScarCraftingEvent.handle(new PacketScarCraftingEvent(), PayloadContexts.of(p.player()));
		return probe.finish();
	}

	/**
	 * Adds the Brazier's offering lifecycle to {@code clock}: the offering clicked in, the fire lit by held
	 * Projection, then held Absorption until the offering is drawn out (or 200 ticks).
	 */
	private static void learnAtBrazier(PlaythroughClock clock, PlaythroughPlayer p, BlockPos brazierPos,
			Predicate<ItemStack> offering, String label) {
		clock.then(() -> insert(p, brazierPos, offering, label))
				.step(() -> lightBrazier(p, brazierPos))
				.then(() -> p.helper().assertTrue(lit(p, brazierPos), "held Projection lights the Brazier; " + p.last()))
				.step(() -> p.holdUntil(ItemInit.blood_absorption.get(), 200, () -> bowl(brazierPos),
						() -> !brazier(p, brazierPos).hasOffering()))
				.then(() -> p.helper().assertFalse(brazier(p, brazierPos).hasOffering(),
						"held Absorption draws out " + label + "; " + p.last()));
	}

	/** Opens the Effigy, selects {@code scarId} through its payload, closes it and sets motif paper on it. */
	private static void prepareEffigy(PlaythroughPlayer p, BlockPos effigyPos, ResourceLocation scarId) {
		GameTestHelper helper = p.helper();
		emptyHand(p);
		Outcome open = p.useOn(effigyPos);
		helper.assertTrue(p.player().containerMenu instanceof MasonsEffigyMenu, "the Effigy opens; " + open);
		PacketPrepareScarPattern.handle(new PacketPrepareScarPattern(effigyPos, List.of(scarId)), PayloadContexts.of(p.player()));
		helper.assertTrue(effigy(p, effigyPos).getSelectedScarIds().equals(List.of(scarId)),
				"the Effigy holds the selected scar: " + effigy(p, effigyPos).getSelectedScarIds());
		p.closeMenu();
		p.wield(ItemInit.runic_motif_paper.get());
		Outcome motif = p.useOn(effigyPos);
		helper.assertTrue(effigy(p, effigyPos).hasPendingMotif(), "the motif paper settles on the Effigy; " + motif);
	}

	private static void insert(PlaythroughPlayer p, BlockPos brazierPos, Predicate<ItemStack> offering, String label) {
		wieldStack(p, offering, label);
		Outcome put = p.useOn(brazierPos);
		p.helper().assertTrue(brazier(p, brazierPos).hasOffering(), label + " goes into the Brazier; " + put);
	}

	private static PlaythroughClock.Step lightBrazier(PlaythroughPlayer p, BlockPos brazierPos) {
		return p.holdUntil(ItemInit.blood_projection.get(), 160, () -> bowl(brazierPos), () -> lit(p, brazierPos));
	}

	/** A point inside the bowl of a floor brazier, so the look ray meets the brazier and not the floor behind it. */
	private static Vec3 bowl(BlockPos pos) {
		return Vec3.atBottomCenterOf(pos).add(0.0D, 0.3D, 0.0D);
	}

	private static boolean lit(PlaythroughPlayer p, BlockPos pos) {
		return BrazierBlock.isLit(p.player().serverLevel().getBlockState(pos));
	}

	private static IronBrazierBlockEntity brazier(PlaythroughPlayer p, BlockPos pos) {
		if (p.player().serverLevel().getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier) return brazier;
		p.helper().fail("no Iron Brazier at " + pos.toShortString());
		return null;
	}

	private static ScarStationBlockEntity station(PlaythroughPlayer p, BlockPos pos) {
		if (p.player().serverLevel().getBlockEntity(pos) instanceof ScarStationBlockEntity station) return station;
		p.helper().fail("no scar station at " + pos.toShortString());
		return null;
	}

	private static MasonsEffigyBlockEntity effigy(PlaythroughPlayer p, BlockPos pos) {
		if (p.player().serverLevel().getBlockEntity(pos) instanceof MasonsEffigyBlockEntity effigy) return effigy;
		p.helper().fail("no Mason's Effigy at " + pos.toShortString());
		return null;
	}

	private static SomaticLoomBlockEntity loom(PlaythroughPlayer p, BlockPos pos) {
		if (p.player().serverLevel().getBlockEntity(pos) instanceof SomaticLoomBlockEntity loom) return loom;
		p.helper().fail("no Somatic Loom at " + pos.toShortString());
		return null;
	}

	private static boolean weavesBloodShot(PlaythroughPlayer p, SomaticLoomBlockEntity loom) {
		var recipe = loom.getCurRecipe();
		return recipe != null && recipe.getResultItem(p.player().level().registryAccess()).is(ItemInit.memory_blood_shot.get());
	}

	private static Predicate<ItemStack> pattern(ResourceLocation scarId) {
		return stack -> stack.is(ItemInit.scar_pattern.get()) && ItemScarPattern.getScarIds(stack).contains(scarId);
	}

	private static boolean carries(PlaythroughPlayer p, Predicate<ItemStack> what) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (what.test(inventory.getItem(slot))) return true;
		}
		return false;
	}

	/** Walks over each of {@code items} lying at {@code npc}'s feet; one outcome for the whole kit. */
	private static Outcome pickUp(PlaythroughPlayer p, Entity npc, ItemLike... items) {
		Outcome.Probe probe = Outcome.begin(p, npc, "pick up the kit at " + PlaythroughPlayer.describe(npc));
		for (ItemLike item : items) p.pickUpNear(npc.blockPosition(), item);
		return probe.finish();
	}

	private static int dropsOf(PlaythroughPlayer p, BlockPos near, ItemLike item) {
		return p.player().serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
				new net.minecraft.world.phys.AABB(near).inflate(3.0D), drop -> drop.getItem().is(item.asItem())).size();
	}

	/** Selects an empty hotbar slot, or moves what is held into a free inventory slot. */
	private static void emptyHand(PlaythroughPlayer p) {
		var inventory = p.player().getInventory();
		if (inventory.getSelected().isEmpty()) return;
		for (int slot = 0; slot < 9; slot++) {
			if (inventory.items.get(slot).isEmpty()) {
				inventory.selected = slot;
				return;
			}
		}
		int free = inventory.getFreeSlot();
		p.helper().assertTrue(free >= 0, "the player has a free slot to empty their hand into");
		inventory.items.set(free, inventory.getSelected().copy());
		inventory.items.set(inventory.selected, ItemStack.EMPTY);
	}

	/** Brings the first stack matching {@code what} into the main hand, as {@code wield} does for an item. */
	private static ItemStack wieldStack(PlaythroughPlayer p, Predicate<ItemStack> what, String label) {
		var inventory = p.player().getInventory();
		if (!inventory.getSelected().isEmpty() && what.test(inventory.getSelected())) return inventory.getSelected();
		for (int slot = 0; slot < inventory.items.size(); slot++) {
			ItemStack stack = inventory.items.get(slot);
			if (stack.isEmpty() || !what.test(stack)) continue;
			if (slot < 9) {
				inventory.selected = slot;
			} else {
				ItemStack held = inventory.getSelected();
				inventory.items.set(inventory.selected, stack);
				inventory.items.set(slot, held);
			}
			return inventory.getSelected();
		}
		p.helper().fail("the player has no " + label + " to hold");
		return ItemStack.EMPTY;
	}

	/** Clicks the first inventory stack matching {@code what} onto the cursor, then into menu slot {@code target}. */
	private static void putInSlot(PlaythroughPlayer p, AbstractContainerMenu menu, Predicate<ItemStack> what, int target,
			String label) {
		var inventory = p.player().getInventory();
		int from = -1;
		for (int index = 0; index < menu.slots.size(); index++) {
			Slot slot = menu.slots.get(index);
			if (slot.container == inventory && !slot.getItem().isEmpty() && what.test(slot.getItem())) {
				from = index;
				break;
			}
		}
		p.helper().assertTrue(from >= 0, "the menu shows the player's " + label);
		p.click(menu, from, 0, ClickType.PICKUP);
		p.click(menu, target, 0, ClickType.PICKUP);
		if (!menu.getCarried().isEmpty()) {
			p.click(menu, from, 0, ClickType.PICKUP);
			p.helper().fail("menu slot " + target + " refused the " + label);
		}
	}

	private static void removeOne(PlaythroughPlayer p, ItemLike item) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(item.asItem())) {
				inventory.getItem(slot).shrink(1);
				return;
			}
		}
	}

	private static void removeAll(PlaythroughPlayer p, ItemLike item) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(item.asItem())) inventory.setItem(slot, ItemStack.EMPTY);
		}
	}

	/** ARRANGE: a block set directly (a structure piece the test does not play), removed when the test ends. */
	private static void set(GameTestHelper helper, BlockPos pos, Block block) {
		ServerLevel level = helper.getLevel();
		level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_ALL);
		PlaythroughCleanup.onEnd(helper, () -> {
			if (level.getBlockState(pos).is(block)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		});
	}

	// --- Rite ground (lifted to PlaythroughRites) -------------------------------------------------------------------

	private static Vec3 riteStand(PlaythroughSites.RiteFloor floor) { return PlaythroughRites.riteStand(floor); }

	private static List<BlockPos> structureCells(CardinalRiteRecipe recipe, BlockPos focus, List<Block> blocks) { return PlaythroughRites.structureCells(recipe, focus, blocks); }

	private static void buildStructure(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) { PlaythroughRites.buildStructure(clock, p, floor); }

	private static void build(PlaythroughPlayer p, BlockPos pos, Block block) { PlaythroughRites.build(p, pos, block); }

	private static List<ItemStack> offerings(CardinalRiteRecipe recipe) { return PlaythroughRites.offerings(recipe); }

	private static List<BlockPos> usedSockets(PlaythroughSites.RiteFloor floor) { return PlaythroughRites.usedSockets(floor); }

	private static void braziers(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) { PlaythroughRites.braziers(clock, p, floor); }

	private static void braziers(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor, List<ItemStack> offerings) { PlaythroughRites.braziers(clock, p, floor, offerings); }

	private static Vec3 besideSocket(PlaythroughSites.RiteFloor floor, BlockPos socket) { return PlaythroughRites.besideSocket(floor, socket); }

	private static BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> supportSigilNodes(PlaythroughPlayer p) { return PlaythroughRites.supportSigilNodes(p); }
}
