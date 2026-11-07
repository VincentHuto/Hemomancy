package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.ChapelRules;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.RingDoorBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SaintSarcophagusBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.HarbingerPathPermissions;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.livingstaff.LivingStaffBondHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.ApotheosBodyRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.spore.SporeLoadRules;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.encounter.MycophantEncounterManager;
import com.vincenthuto.hemomancy.common.encounter.MycophantEncounterRules;
import com.vincenthuto.hemomancy.common.encounter.VesperOrdealManager;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantEntity;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.MycophantYieldRules;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.entity.boss.saint.SaintTrialEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingSicklePruning;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormHelper;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffWeaponFormRules;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BurnSporeLoadKeyPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ImprintMnemonicBlueprintPacket;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRecipe;
import com.vincenthuto.hemomancy.common.recipe.RiteDiscoveryRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import java.util.function.BiConsumer;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.SeveredQliphothState;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.SaintSarcophagusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchorBlockEntity;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Band {@code d8_endgame}: the Harbinger path's end, from the revelation choice to Degree 8 on both roads, played
 * the way a player plays it. Apotheos: the Rite of Apotheos planted on its grand floor, the body that no longer eats,
 * the heart-node and synapse refusals outside the Gardens, the Visitation and Possession refusal chains, the Mycophant
 * fought and yielding in the test level, its Tendril. Silentium: the Severance cut with a Living Arsenal weapon, the
 * open wound that has nowhere to go here, the Memory of Vesper delivered and reissued, drawn in at a lit Iron Brazier,
 * and what a Silentium can no longer do. Then the teachers' ending lines, the ledger's Blood Beneath bar, and the
 * Saint mausoleums: ward, Chapels, ring door, tomb and Recollection cysts.
 * <p>
 * The Fungal Gardens and the Chamber of Will are not loaded on the GameTest server, so the Gardens travel, the
 * heart-node respawn trap and the Vesper ordeal are {@code NOT RUN} probes; their proof is owed by the review clients.
 * Every step under test enters through the player's own entry point; fixtures are labelled {@code ARRANGE}.
 * {@link #apotheosChain} and {@link #silentiumChain} are the two late-run segments for the full run.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughEndgameGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_d8_endgame";
	private static final String RITES = "playthrough_d8_endgame_rites";

	/** The grand floors: relative y 3 puts the Focus in the template's floor layer; x 16, z 12 leaves the +-11 sockets inside. */
	private static final BlockPos GRAND_ORIGIN = new BlockPos(16, 3, 12);
	private static final int GRAND_TIMEOUT = PlaythroughRites.GRAND_TICKS + 2400;

	// --- Lines the player reads ----------------------------------------------------------------------------------------
	private static final String DEGREE_LINE = "hemomancy.initiation.degree";
	private static final String TITLE_APOTHEOS = "hemomancy.degree.8.apotheos";
	private static final String TITLE_SILENTIUM = "hemomancy.degree.8.silentium";
	private static final String NO_COMMUNION = "message.hemomancy.cardinal_rite_activation.the_eighth_degree_remains_sealed_consume";
	private static final String WRONG_PATH_RITE = "message.hemomancy.cardinal_rite_activation.this_rite_cannot_answer_your_current";
	private static final String RITE_REQUIRES = "message.hemomancy.cardinal_rite_activation.this_rite_requires";
	/** The layered floor's line when it filters out every rite the player may not attempt (the D-32 family). */
	private static final String FLOOR_NO_RITE = "message.hemomancy.cardinal_rite_activation.the_floor_answers_but_no_structure";
	private static final String ANCESTRAL_SILENT = "message.hemomancy.cardinal_rite_activation.ancestral_communion_silentium";
	private static final String IMPRINTED = "item.hemomancy.mnemonic_blueprint.imprinted";
	private static final String BODY_REFUSES = "message.hemomancy.apotheos_body.refuses_it";
	private static final String SPORES_WHOLE = "message.hemomancy.spore_load.whole";
	private static final String SPORES_COOLING = "message.hemomancy.spore_load.cooling";
	private static final String SPORES_TOO_FEW = "message.hemomancy.spore_load.too_few";
	private static final String HEART_NODE_REFUSED = "message.hemomancy.heart_node.refused";
	private static final String SYNAPSE_NOT_APOTHEOS = "block.hemomancy.mycelial_synapse.not_apotheos";
	private static final String SYNAPSE_NOT_IN_GARDENS = "block.hemomancy.mycelial_synapse.not_in_gardens";
	private static final String STRUCTURE_DEGREE = "message.hemomancy.blood_structure_crafting_helper.this_formation_requires_and_demands";
	private static final String VISITATION_NOT_APOTHEOS = "message.hemomancy.visitation.not_apotheos";
	private static final String NO_GARDENS = "message.hemomancy.possession.no_gardens";
	private static final String POSSESSION_NOT_SILENTIUM = "message.hemomancy.possession.not_silentium";
	private static final String POSSESSION_MIND_BROKEN = "message.hemomancy.possession.mind_broken";
	private static final String POSSESSION_NO_SURROGATE = "message.hemomancy.possession.no_surrogate";
	private static final String POSSESSION_NOT_YOUR_PET = "message.hemomancy.possession.not_your_pet";
	private static final String POSSESSION_NOT_BOUND = "message.hemomancy.possession.not_bound";
	private static final String MYCOPHANT_VICTORY = "message.hemomancy.mycophant.victory";
	private static final String MYCOPHANT_EXPELLED = "message.hemomancy.mycophant.expelled";
	/** LF-6: the twin of Vesper's make-room line, not shipped yet. */
	private static final String MYCOPHANT_MAKE_ROOM = "message.hemomancy.mycophant.make_room";
	private static final String TREE_WAITS = "message.hemomancy.living_sickle_pruning.the_tree_waits_for_a_valid";
	private static final String SICKLE_SHAPED = "message.hemomancy.living_sickle_pruning.the_assumed_limb_hooks_inward_remembering";
	private static final String TRUNK_PARTS = "message.hemomancy.living_sickle_pruning.the_living_edge_parts_the_trunk";
	private static final String SICKLE_REFUSES = "message.hemomancy.living_sickle_pruning.the_sickle_refuses_a_wound_outside";
	private static final String NO_PLACE_TO_OPEN = "message.hemomancy.vesper_ordeal_manager.the_refusal_has_no_place_to";
	private static final String WOUND_NO_MEMORY = "message.hemomancy.qliphoth_bloom_block.the_wound_has_no_memory_of";
	private static final String WOUND_REJECTS_PATH = "message.hemomancy.qliphoth_bloom_block.the_wound_rejects_the_path_you";
	private static final String SCAR_GIVES_BACK = "message.hemomancy.qliphoth_bloom_block.the_scar_gives_back_his_memory";
	private static final String SCAR_STILL_YOURS = "message.hemomancy.qliphoth_bloom_block.the_severed_scar_is_still_your";
	private static final String LIGHT_FIRST = "message.hemomancy.living_weapon_graft_rite.light_the_brazier_before_receiving_its";
	private static final String HOLD_THE_STAFF = "message.hemomancy.living_weapon_graft_rite.hold_the_living_staff_to_receive";
	private static final String MEMORY_ANSWERS_ONLY = "message.hemomancy.living_weapon_graft_rite.vesper_s_memory_answers_only_the";
	private static final String MEMORY_AWAKENED = "hemomancy.memory_of_vesper.awakened";
	private static final String WHISPER_SILENCED = "hemomancy.whisper.silenced";
	private static final String WHISPER_PENDING = "hemomancy.whisper.pending";
	private static final String SPINE_REFUSED = "message.hemomancy.fungal_garden_travel_helper.the_spine_will_not_carry_you";
	private static final String ENDING_PREFIX = "hemomancy.dialogue.ending.";
	private static final String WARD_REFUSED = "message.hemomancy.mausoleum_ward.refused";
	private static final String HALLOWED_BURN = "message.hemomancy.hallowed_ground";
	private static final String CHAPEL_REFUSED = "message.hemomancy.chapel.refused";
	private static final String CHAPEL_BEGIN = "message.hemomancy.chapel.begin.";
	private static final String CHAPEL_GATE = "message.hemomancy.chapel.downfall.gate";
	private static final String RING_TOUCH = "message.hemomancy.ring_door.touch";
	private static final String RING_REFUSED = "message.hemomancy.ring_door.refused";
	private static final String RING_NODES = "message.hemomancy.ring_door.nodes";
	private static final String RING_CONTESTED = "message.hemomancy.ring_door.contested";
	private static final String RECOLLECTION_TAKEN = "message.hemomancy.recollection.taken";
	private static final String MYCOPHANT_REMATCH_KEY = "hemomancy:mycophant_rematch";

	private static final String[] TEACHERS = {"vicar", "alchemist", "artificer", "mnemonist"};
	private static final PlaythroughNpcs.Role[] TEACHER_ROLES = {PlaythroughNpcs.Role.VICAR, PlaythroughNpcs.Role.ALCHEMIST,
			PlaythroughNpcs.Role.ARTIFICER, PlaythroughNpcs.Role.MNEMONIST};

	private PlaythroughEndgameGameTests() {
	}

	// =================================================================================================================
	// The late-run segments
	// =================================================================================================================

	/**
	 * The Apotheos road from the revelation choice, as steps on {@code clock}: the Spine opens the core-witness
	 * whisper and the player chooses the Eighth Degree, the Rite of Apotheos is played on its grand floor at
	 * {@code riteOrigin}, then the four teachers (laid round {@code wingSpot}) greet the Apotheos with their ending
	 * lines and the ledger reads 13/13. Gardens travel is logged as NOT RUN, not failed. {@code p} must be at
	 * {@code CHOICE_PENDING_D7} and carry the Spine.
	 */
	public static void apotheosChain(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock, BlockPos riteOrigin,
			BlockPos wingSpot) {
		clock.then(() -> {
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.CHOICE_PENDING_D7);
			chooseByTheSpine(helper, p, "archon_choice_eighth_degree");
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.APOTHEOS_PENDING_D7);
		});
		clock.segment("the Rite of Apotheos", c -> apotheosRite(helper, p, c, riteOrigin));
		clock.then(() -> {
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.APOTHEOS_D8);
			PlaythroughSites.OutpostWing wing = PlaythroughSites.outpostWing(helper, wingSpot, TEACHER_ROLES);
			for (int i = 0; i < TEACHERS.length; i++) {
				Entity teacher = wing.get(TEACHER_ROLES[i]);
				p.approach(teacher);
				List<String> endings = endingKeys(p.talkTo(teacher));
				helper.assertTrue(endings.equals(List.of(ENDING_PREFIX + TEACHERS[i] + ".apotheos")),
						"the " + TEACHERS[i] + " greets an Apotheos with its ending line, not " + endings);
			}
			LedgerView ledger = p.ledger();
			helper.assertTrue(ledger.bloodBeneathProgress() == ledger.bloodBeneathTotal() && ledger.bloodBeneathComplete(),
					"the ledger reads the Blood Beneath bar full at Apotheos: " + ledger);
			PlaythroughLog.note(helper, "NOT RUN (logged, not a failure of the run): Apotheos travel into the Fungal Gardens; "
					+ "proof owed: " + PlaythroughNotRun.HEART_NODE);
		});
	}

	/**
	 * The Silentium road from the revelation choice, as steps on {@code clock}: the Spine's whisper and the choice of
	 * silence, the Severance by two cuts on the player's own Bloom at {@code bloomRoot} (a Living Arsenal blade is
	 * ARRANGE), the open wound refused here, then the Vesper victory by ARRANGE (NOT RUN: the ordeal is in the Chamber
	 * of Will), the Memory delivered by a real delivery tick and drawn in with the Living Staff at a lit Iron Brazier
	 * set at {@code brazierPos}. {@code p} must be at {@code CHOICE_PENDING_D7}, owning the Bloom, carrying the Spine.
	 */
	public static void silentiumChain(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock, BlockPos bloomRoot,
			BlockPos brazierPos) {
		clock.then(() -> {
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.CHOICE_PENDING_D7);
			chooseByTheSpine(helper, p, "archon_choice_silence");
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.SILENT_PENDING_D7);
			QliphothBloomSavedData.BloomEntry bloom = bloomAt(helper, p, bloomRoot);
			p.player().setItemInHand(InteractionHand.MAIN_HAND, livingBlade()); // ARRANGE: the Living Arsenal blade, conjured
			p.moveTo(bloomRoot.south(2));
			cut(p, bloomRoot).said(SICKLE_SHAPED);
			cut(p, bloomRoot).said(TRUNK_PARTS);
			helper.assertTrue(blooms(p).getState(bloom) == SeveredQliphothState.OPEN, "two cuts open the wound");
			PlaythroughRites.emptyHand(p);
			p.useOn(bloomRoot).said(NO_PLACE_TO_OPEN);
			PlaythroughLog.note(helper, "NOT RUN (ARRANGE stands in): the Vesper ordeal; proof owed: "
					+ PlaythroughNotRun.VESPER + ", " + PlaythroughNotRun.CHAMBER);
			PlaythroughStates.vesperBeatenD7(p); // ARRANGE (NOT RUN: Vesper ordeal)
			p.tickOnce();
			helper.assertTrue(p.count(ItemInit.memory_of_vesper.get()) == 1, "the Memory is delivered on the delivery tick");
			bond(p); // ARRANGE: the Staff bond earned at the Staff's own craft
			tools(p);
			fillBlood(p);
			PlaythroughSites.ironBrazier(helper, brazierPos, false);
			p.moveTo(brazierPos.south(2));
			PlaythroughRites.insertOffering(p, brazierPos, stack -> stack.is(ItemInit.memory_of_vesper.get()), "the Memory of Vesper");
		})
				.step(() -> PlaythroughRites.lightBrazier(p, brazierPos))
				.then(() -> p.select(ManipulationInit.blood_absorption.get().getName()))
				.step(() -> p.holdUntil(ItemInit.living_staff.get(), 300, () -> PlaythroughRites.bowl(brazierPos),
						() -> p.degree() == 8))
				.then(() -> {
					PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.SILENTIUM_D8);
					helper.assertTrue(p.knows(ManipulationInit.conjure_sickle.get().getName()),
							"drawing the Memory in teaches conjure_sickle");
				});
	}

	/** The late run's Apotheos segment on its own (optional until stable, as the D0-D5 full run is). */
	@GameTest(template = EMPTY, timeoutTicks = GRAND_TIMEOUT, batch = "playthrough_d8_apotheos_chain", required = false)
	public static void apotheosChainFromTheChoice(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 22));
		PlaythroughStates.choicePendingD7(p); // ARRANGE
		PlaythroughClock clock = PlaythroughClock.start(helper);
		apotheosChain(helper, p, clock, helper.absolutePos(GRAND_ORIGIN), PlaythroughSites.stand(helper, 26, 20));
		clock.run();
	}

	/** The late run's Silentium segment on its own (optional until stable, as the D0-D5 full run is). */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = "playthrough_d8_bloom_chain", required = false)
	public static void silentiumChainFromTheChoice(GameTestHelper helper) {
		BlockPos root = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.communedD7(p, root); // ARRANGE: a real Bloom, nine pomes, the Spine delivered
		PlaythroughStates.choicePendingD7(p); // ARRANGE
		PlaythroughClock clock = PlaythroughClock.start(helper);
		silentiumChain(helper, p, clock, root, PlaythroughSites.stand(helper, 24, 18));
		clock.run();
	}

	// =================================================================================================================
	// Apotheos: the rite
	// =================================================================================================================

	/**
	 * From {@code apotheosPendingD7}, the Rite of Apotheos by play on its grand floor: the hematic iron pillars set by
	 * hand, charcoal in the offering brazier lit by projection, the Staff planted, the sixteen-plus anchors fed, the
	 * waves fought. The player ends at Degree 8 Apotheos, told the Apotheos title, with the Truth in their Liber.
	 */
	@GameTest(template = EMPTY, timeoutTicks = GRAND_TIMEOUT, batch = "playthrough_d8_apotheos_rite")
	public static void apotheosRiteByPlay(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 22));
		PlaythroughStates.apotheosPendingD7(p); // ARRANGE
		PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.APOTHEOS_PENDING_D7);
		int[] mark = new int[1];
		int[] rescues = new int[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(() -> mark[0] = p.mark());
		// The ORDEAL deadlock CHAIN-1 is not the deck's doing: with the deck pinned free of the draining waves the woken
		// rings still came up dry and damaged and needed 2 rescues. So this required test keeps the ARRANGE rescue and
		// reports its count (a pass with rescues is DEGRADED); apotheosRiteWithoutRescue keeps the question open.
		apotheosRite(helper, p, clock, helper.absolutePos(GRAND_ORIGIN), rescues);
		clock.then(() -> {
			if (rescues[0] > 0) {
				PlaythroughNotRun.degraded(helper, "the Rite of Apotheos passed with " + rescues[0] + " ARRANGE rescue(s) of"
						+ " dry anchors (decision CHAIN-1)");
			}
			helper.assertTrue(p.degree() == 8, "the Rite of Apotheos promotes to degree 8, not " + p.degree() + "; " + p.last());
			helper.assertTrue(path(p) == EnumArchonPath.APOTHEOS, "the path is APOTHEOS, not " + path(p));
			helper.assertTrue(saidSince(p, mark[0], DEGREE_LINE) && saidSince(p, mark[0], TITLE_APOTHEOS),
					"the promotion line names the Apotheos title: " + p.chatSince(mark[0]));
			helper.assertTrue(LiberKnowledgeHelper.hasEntry(p.player(), LiberEntryDefinitions.TRUTH),
					"an Apotheos has the Truth in the Liber");
			helper.assertTrue(p.count(ItemInit.living_staff.get()) == 1, "the planted Staff comes back once");
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.APOTHEOS_D8);
			PlaythroughLog.note(helper, "apotheosRiteByPlay: passed with CHAIN-1 rescues=" + rescues[0]);
		}).run();
	}

	/**
	 * The Rite of Apotheos by a solo caster with no ARRANGE rescue: once the woken ring runs dry in the ordeal (the
	 * covenant pressure drains every anchor until its Seal is traced; a missed wave adds instability), can the caster
	 * still climb out with bare Projection?
	 */
	// DECISION CHAIN-1: in a grand rite's ORDEAL, dry anchors raise the deficit pressure every second
	// (ActiveCardinalRite.applyAnchorDeficitPressure from CardinalRiteOrdealEngine), which re-breaks or re-threatens the
	// same anchors by damage priority, and CardinalRiteInteractionHandler.tryProject takes a 50 mL repair before it fills
	// a damaged anchor. Two repairs a second are 100 mL, all bare Projection gives (5 mL a tick), so a solo caster never
	// refills and the wave never clears ("The ordeal cannot advance while the cardinal stations run dry"). A floor's
	// sleeping support sigil (the Hematic Lattice, stability 20) buys one window, not enough. Seen on the Rite of Apotheos
	// and the Bloom of the Qliphoth; the deck is not the cause (with the draining waves pinned out, the rings woken at
	// waves 2 and 3 still came up dry and damaged and deadlocked). Options: let Projection fill a dry anchor before (or while) repairing it / press only
	// anchors that are not under repair / slow the pressure or cheapen the repair at grand rites / keep (helpers or a
	// planned Seal trace are the answer, and say so).
	@GameTest(template = EMPTY, timeoutTicks = GRAND_TIMEOUT, batch = "playthrough_d8_apotheos_rite_solo", required = false)
	public static void apotheosRiteWithoutRescue(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 22));
		PlaythroughStates.apotheosPendingD7(p); // ARRANGE
		PlaythroughClock clock = PlaythroughClock.start(helper);
		apotheosRite(helper, p, clock, helper.absolutePos(GRAND_ORIGIN), null);
		clock.then(() -> helper.assertTrue(p.degree() == 8,
				"CHAIN-1 expected: a solo caster finishes the Rite of Apotheos without help; degree " + p.degree())).run();
	}

	/**
	 * The Staff planted on a ready Apotheos floor by three players who may not take it: an Archon without Communion
	 * (sealed, consume the husks), a player whose choice is still pending, and a Silence (both told the rite does not
	 * answer their path). No rite starts and each keeps the Staff. The completion-stage twins of these refusals
	 * ({@code commonRefusal}) are only reachable if the path changes mid-rite, which no player action does.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1500, batch = RITES)
	public static void apotheosRiteRefusals(GameTestHelper helper) {
		PlaythroughPlayer archon = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 22));
		PlaythroughPlayer undecided = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 22));
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 14, 22));
		PlaythroughStates.archonD7(archon); // ARRANGE
		PlaythroughStates.choicePendingD7(undecided); // ARRANGE
		PlaythroughStates.silentPendingD7(silent); // ARRANGE
		for (PlaythroughPlayer p : List.of(archon, undecided, silent)) {
			tools(p);
			fillBlood(p);
		}
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(archon, helper.absolutePos(GRAND_ORIGIN), "apotheos_rite");
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, archon, floor);
		clock.then(() -> {
			refusedAtFocus(helper, archon, floor, NO_COMMUNION, "an Archon without Communion");
			refusedAtFocus(helper, undecided, floor, WRONG_PATH_RITE, "an Archon whose choice is pending");
			refusedAtFocus(helper, silent, floor, WRONG_PATH_RITE, "a Silence");
		}).run();
	}

	/**
	 * A death mid-rite: the rite ends, the Staff comes back once, and the player keeps Degree 7 and the
	 * APOTHEOS_PENDING path, so the rite can be tried again.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 3000, batch = "playthrough_d8_apotheos_death")
	public static void apotheosRiteDeathKeepsThePath(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 22));
		PlaythroughStates.apotheosPendingD7(p); // ARRANGE
		tools(p);
		fillBlood(p);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(GRAND_ORIGIN), "apotheos_rite");
		int[] fed = new int[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().fightThreats().releaseOnStill()
						.pauseWhen(() -> ++fed[0] > 100)))
				.then(() -> {
					helper.assertTrue(CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) != null,
							"the rite runs 100 ticks in");
					BlockPos spot = p.player().blockPosition();
					Outcome died = p.die();
					helper.assertTrue(CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) == null,
							"a death ends the rite; " + died);
					p.respawn();
					p.moveTo(spot);
					p.pickUpNear(spot, ItemInit.living_staff.get());
					int staffs = p.count(ItemInit.living_staff.get()) + dropsNear(p, spot, ItemInit.living_staff.get());
					PlaythroughLog.note(helper, "death mid-Apotheos: said " + died.saidKeys() + "; degree " + p.degree() + "; path "
							+ path(p) + "; Staffs " + staffs);
					helper.assertTrue(p.degree() == 7, "a death keeps degree 7, not " + p.degree());
					helper.assertTrue(path(p) == EnumArchonPath.APOTHEOS_PENDING, "a death keeps the APOTHEOS_PENDING path, not "
							+ path(p));
					helper.assertTrue(staffs == 1, "the Staff is neither lost nor doubled: " + staffs);
					var degree = HemoCapabilityAccess.getInitiatoryDegree(p.player());
					helper.assertTrue(HarbingerPathPermissions.canPerformRite(degree.getTotalPomesConsumed(), degree.getArchonPath(), true)
							&& degree.isQliphothCommunionDone(), "the respawned player may plant the Rite of Apotheos again");
				}).run();
	}

	/**
	 * At APOTHEOS_PENDING without the Truth memo, the Rite of Apotheos is not discovered (its Liber gate is the
	 * Truth entry) and a blank Mnemonic Blueprint cannot be imprinted with it, though planting the Staff on its floor
	 * would start it. The Truth comes from an optional whisper memo, or at Degree 8 itself.
	 */
	// DECISION D8-3: the Apotheos rite is discoverable only through an optional memo (or at D8, too late). Should the
	// Truth unlock at APOTHEOS_PENDING? Options: unlock the entry at APOTHEOS_PENDING / a pointer line from a teacher or
	// the choice whisper / keep (memo or discovery by building the floor).
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void apotheosRiteNotInTheLiber(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 6, 6));
		PlaythroughStates.apotheosPendingD7(p); // ARRANGE (no memo read)
		ResourceLocation rite = Hemomancy.rloc("cardinal_rite/apotheos_rite");
		boolean memo = LiberKnowledgeHelper.knowsMemo(p.player(), MemoDefinitions.FUNGAL_WHISPER_TRUTH.id());
		boolean entry = LiberKnowledgeHelper.hasEntry(p.player(), LiberEntryDefinitions.TRUTH);
		boolean discovered = RiteDiscoveryRules.isDiscovered(p.player(), rite);
		p.give(new ItemStack(ItemInit.mnemonic_blueprint.get())); // ARRANGE: a blank blueprint
		Outcome.Probe probe = Outcome.begin(p, null, "imprint the Rite of Apotheos");
		ImprintMnemonicBlueprintPacket.handle(new ImprintMnemonicBlueprintPacket(new MnemonicBlueprintTarget(
				MnemonicBlueprintTarget.Type.CARDINAL_RITE, rite)), PayloadContexts.of(p.player()));
		Outcome imprint = probe.finish();
		PlaythroughLog.note(helper, "D8-3: at APOTHEOS_PENDING memo=" + memo + " truthEntry=" + entry + " riteDiscovered="
				+ discovered + "; imprint " + imprint.describe());
		helper.assertTrue(discovered && imprint.saidKeys().contains(IMPRINTED),
				"DECISION D8-3: a player who chose the Eighth Degree can find the Rite of Apotheos in the Liber and imprint it; "
						+ "discovered=" + discovered + ", imprint said " + imprint.saidKeys());
		helper.succeed();
	}

	// =================================================================================================================
	// Apotheos: the body, the Gardens, the heart-node, Visitation and Possession
	// =================================================================================================================

	/**
	 * An Apotheos body: hunger pinned at 17, food refused everywhere, milk drunk outside the Gardens and refused in
	 * the Overworld Gardens biome, where the Morphic Tonic purges and mends health and blood (outside the Gardens it is
	 * refused); spores gathered there and nowhere else; the
	 * Burn Spores key refused at full health, healing once, cooling, then refused with too few spores.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH)
	public static void apotheosBody(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.apotheosD8(p); // ARRANGE
		BlockPos outside = PlaythroughSites.stand(helper, 4, 8);
		BlockPos garden = PlaythroughSites.stand(helper, 24, 12);
		PlaythroughSites.gardensBiome(helper, BoundingBox.fromCorners(garden.offset(-6, -2, -6), garden.offset(6, 4, 6)));
		float[] spores = new float[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					p.moveTo(outside);
					p.tickOnce();
					helper.assertTrue(p.player().getFoodData().getFoodLevel() == ApotheosBodyRules.HUNGER_PIN,
							"an Apotheos's hunger is pinned at " + ApotheosBodyRules.HUNGER_PIN + ", not "
									+ p.player().getFoodData().getFoodLevel());
					p.give(new ItemStack(Items.BREAD)); // ARRANGE
					Outcome bread = p.consume(Items.BREAD);
					bread.said(BODY_REFUSES);
					helper.assertTrue(p.count(Items.BREAD) == 1, "the bread is refused, not eaten; " + bread);

					p.player().addEffect(new MobEffectInstance(MobEffects.POISON, 600));
					p.give(new ItemStack(Items.MILK_BUCKET)); // ARRANGE
					Outcome milkOutside = p.consume(Items.MILK_BUCKET);
					helper.assertTrue(p.count(Items.BUCKET) == 1 && !p.player().hasEffect(MobEffects.POISON),
							"outside the Gardens an Apotheos drinks milk; " + milkOutside);
					p.give(new ItemStack(ItemInit.morphic_tonic.get())); // ARRANGE
					Outcome tonicOutside = p.consume(ItemInit.morphic_tonic.get());
					tonicOutside.said("message.hemomancy.morphic_tonic.outside_gardens");
					helper.assertTrue(p.count(ItemInit.morphic_tonic.get()) == 1
									&& !p.player().hasEffect(EffectInit.morphic_mending),
							"outside the Gardens the Morphic Tonic is refused; " + tonicOutside);

					p.moveTo(garden);
					helper.assertTrue(FungalGardensPlaces.isInGardens(level, p.player().blockPosition()),
							"the arranged biome is the Overworld Gardens");
					p.player().addEffect(new MobEffectInstance(MobEffects.POISON, 600));
					p.give(new ItemStack(Items.MILK_BUCKET)); // ARRANGE
					Outcome milkInside = p.consume(Items.MILK_BUCKET);
					milkInside.said(BODY_REFUSES);
					helper.assertTrue(p.count(Items.MILK_BUCKET) == 1 && p.player().hasEffect(MobEffects.POISON),
							"in the Gardens the milk is refused; " + milkInside);
					Outcome tonic = p.consume(ItemInit.morphic_tonic.get());
					helper.assertTrue(!p.player().hasEffect(MobEffects.POISON) && p.player().hasEffect(EffectInit.morphic_mending),
							"the Morphic Tonic purges and mends in the Gardens; " + tonic);

					var load = HemoCapabilityAccess.getSporeLoad(p.player());
					float before = load.load();
					for (int second = 1; second <= 60; second++) {
						p.player().tickCount = second * 20; // a second of standing in the Gardens
						p.tickOnce();
					}
					float gathered = load.load() - before;
					p.moveTo(outside);
					float beforeOutside = load.load();
					for (int second = 61; second <= 70; second++) {
						p.player().tickCount = second * 20;
						p.tickOnce();
					}
					PlaythroughLog.note(helper, "spores: +" + gathered + " in 60 s in the Overworld Gardens, +"
							+ (load.load() - beforeOutside) + " in 10 s outside");
					helper.assertTrue(Math.abs(gathered - 60 * SporeLoadRules.gainPerSecond(false, true)) < 0.01F,
							"spores gather at the biome's rate: +" + gathered);
					helper.assertTrue(load.load() == beforeOutside, "no spores gather outside the Gardens");

					p.player().setHealth(p.player().getMaxHealth());
					burnSpores(p).said(SPORES_WHOLE);
					p.damage(10.0F);
					float hurt = p.player().getHealth();
					spores[0] = load.load();
					Outcome burn = burnSpores(p);
					helper.assertTrue(p.player().getHealth() >= hurt + SporeLoadRules.BURN_HEAL - 0.01F
									&& Math.abs(load.load() - (spores[0] - SporeLoadRules.BURN_COST)) < 0.01F,
							"a burn heals " + SporeLoadRules.BURN_HEAL + " for " + SporeLoadRules.BURN_COST + " spores; " + burn);
					burnSpores(p).said(SPORES_COOLING);
				})
				.idle(SporeLoadRules.BURN_COOLDOWN_TICKS + 2)
				.then(() -> {
					p.damage(4.0F);
					burnSpores(p).said(SPORES_TOO_FEW);
				})
				.run();
	}

	/**
	 * Apotheos travel through the Spine into the Fungal Gardens in the flesh. The Gardens dimension is not loaded on
	 * the GameTest server: what the Spine does here is logged as degraded, then the test says NOT RUN.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void gardensTravel(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 6, 6));
		PlaythroughStates.apotheosD8(p); // ARRANGE
		fillBlood(p);
		p.wield(ItemInit.fungal_spine.get());
		Outcome used = p.use(InteractionHand.MAIN_HAND);
		PlaythroughNotRun.degraded(helper, "an Apotheos uses the Spine with no Gardens: spent " + used.bloodSpent()
				+ " mL, still in " + p.player().level().dimension().location() + "; " + used.describe());
		ServerLevel gardens = PlaythroughNotRun.require(helper, FungalGardenTravelHelper.FUNGAL_GARDENS,
				"Apotheos travel into the Fungal Gardens", PlaythroughNotRun.HEART_NODE);
		helper.assertTrue(p.player().level() == gardens, "the Spine carries an Apotheos into the Gardens; " + used);
		helper.succeed();
	}

	/**
	 * The heart-node cannot be planted outside the Gardens dimension: an Apotheos on fungal network ground in the
	 * Overworld (and in the Overworld Gardens biome), an Archon and a Silentium are all refused and keep the node.
	 * A Mycelial Synapse raised by projection (a Sanguine Formation in the offhand, hematic iron against a Sporite
	 * Crystal) is refused in order: the degree for a D7, not an Apotheos for a Silentium, not in the Gardens for an
	 * Apotheos.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH)
	public static void heartNodeRefusedOutsideGardens(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 2));
		PlaythroughPlayer archon = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 10));
		PlaythroughPlayer silentium = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 18));
		PlaythroughStates.apotheosD8(apotheos); // ARRANGE
		PlaythroughStates.archonD7(archon); // ARRANGE
		PlaythroughStates.silentiumD8(silentium); // ARRANGE
		Item nodeItem = BlockInit.heart_node.get().asItem();
		helper.assertTrue(nodeItem != Items.AIR, "the heart-node has an item to plant");
		BlockPos node = PlaythroughSites.stand(helper, 12, 6);
		PlaythroughRites.set(helper, node.below(), BlockInit.erythrocytic_dirt.get()); // ARRANGE: fungal network ground
		BlockPos gardenNode = PlaythroughSites.stand(helper, 24, 12);
		PlaythroughRites.set(helper, gardenNode.below(), BlockInit.erythrocytic_dirt.get()); // ARRANGE
		PlaythroughSites.gardensBiome(helper, BoundingBox.fromCorners(gardenNode.offset(-6, -2, -6), gardenNode.offset(6, 4, 6)));

		for (PlaythroughPlayer p : List.of(apotheos, archon, silentium)) {
			p.give(new ItemStack(nodeItem)); // ARRANGE: a heart-node to plant
			p.moveTo(node.south(2));
			Outcome planted = p.place(node, BlockInit.heart_node.get());
			PlaythroughLog.note(helper, "heart-node at degree " + p.degree() + " " + path(p) + ": " + planted.describe());
			planted.said(HEART_NODE_REFUSED);
			helper.assertTrue(!level.getBlockState(node).is(BlockInit.heart_node.get()) && p.count(nodeItem) == 1,
					"the refused node is not planted and is kept; " + planted);
		}
		apotheos.moveTo(gardenNode.south(2));
		Outcome inBiome = apotheos.place(gardenNode, BlockInit.heart_node.get());
		PlaythroughLog.note(helper, "an Apotheos in the Overworld Gardens biome plants a heart-node: " + inBiome.describe()
				+ " (the body counts the biome as the Gardens; the node does not)");
		inBiome.said(HEART_NODE_REFUSED);

		BlockPos iron = PlaythroughSites.stand(helper, 12, 16);
		PlaythroughRites.set(helper, iron, BlockInit.hematic_iron_block.get()); // ARRANGE: the synapse's frame
		PlaythroughRites.set(helper, iron.east(), BlockInit.sporite_crystal.get()); // ARRANGE
		PlaythroughClock clock = PlaythroughClock.start(helper);
		synapseRefusal(clock, helper, archon, iron, STRUCTURE_DEGREE);
		synapseRefusal(clock, helper, silentium, iron, SYNAPSE_NOT_APOTHEOS);
		synapseRefusal(clock, helper, apotheos, iron, SYNAPSE_NOT_IN_GARDENS);
		clock.then(() -> helper.assertTrue(level.getBlockState(iron).is(BlockInit.hematic_iron_block.get()),
				"no synapse was raised")).run();
	}

	/** Holds Blood Projection on the synapse frame with a Sanguine Formation in the offhand and expects {@code key}. */
	private static void synapseRefusal(PlaythroughClock clock, GameTestHelper helper, PlaythroughPlayer p, BlockPos iron,
			String key) {
		int[] mark = new int[1];
		double[] blood = new double[1];
		clock.then(() -> {
					tools(p);
					fillBlood(p);
					p.player().setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ItemInit.sanguine_formation.get())); // ARRANGE
					p.moveTo(iron.south(2));
					mark[0] = p.mark();
					blood[0] = p.blood();
				})
				.step(() -> p.hold(ItemInit.blood_projection.get(), 25, Vec3.atCenterOf(iron)))
				.then(() -> {
					PlaythroughLog.note(helper, "synapse at degree " + p.degree() + " " + path(p) + ": " + p.chatSince(mark[0]));
					helper.assertTrue(saidSince(p, mark[0], key), "the synapse refusal says " + key + "; said " + p.chatSince(mark[0]));
					helper.assertTrue(p.blood() >= blood[0] - 0.01D, "a refused synapse takes no blood: "
							+ blood[0] + " -> " + p.blood());
				});
	}

	/**
	 * The heart-node respawn trap: an Apotheos whose node is planted in the Gardens respawns there after every death.
	 * The Gardens are not loaded here, so the test says NOT RUN.
	 */
	// DECISION D8-1: an Apotheos whose heart-node forces respawn has no way home after any death (HR 5.6 says return is
	// never gated). Which destination should a stranded Apotheos return to? Options: world spawn / their bed / the last
	// Overworld position (the Spine's return point) / keep.
	@GameTest(template = EMPTY, timeoutTicks = 20, batch = BATCH, required = false)
	public static void heartNodeRespawnTrap(GameTestHelper helper) {
		PlaythroughNotRun.require(helper, FungalGardenTravelHelper.FUNGAL_GARDENS,
				"the heart-node respawn trap (a node planted in the Gardens, a death, the forced respawn)",
				PlaythroughNotRun.HEART_NODE);
		PlaythroughNotRun.notRun(helper, "DECISION D8-1 awaits the user before the trap has an expected destination",
				PlaythroughNotRun.HEART_NODE);
	}

	/**
	 * The Rite of Visitation and the Rite of Possession planted on their floors, refused in order. Visitation: a D7 is
	 * below the rite, a Silentium is not an Apotheos, an Apotheos is told the Gardens cannot be reached. Possession:
	 * an Apotheos is not a Silentium; a Silentium with a broken mind, with no surrogate, with another's pet, with an
	 * unbound bat, and with an armor stand (held by nature) that ends at {@code no_gardens}.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 2400, batch = RITES)
	public static void visitationAndPossessionRefusals(GameTestHelper helper) {
		PlaythroughPlayer archon = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 22));
		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 22));
		PlaythroughPlayer silentium = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 14, 22));
		PlaythroughStates.archonD7(archon); // ARRANGE
		PlaythroughStates.apotheosD8(apotheos); // ARRANGE
		PlaythroughStates.silentiumD8(silentium); // ARRANGE
		for (PlaythroughPlayer p : List.of(archon, apotheos, silentium)) {
			tools(p);
			fillBlood(p);
		}
		PlaythroughSites.RiteFloor visit = PlaythroughSites.riteFloor(apotheos, helper.absolutePos(new BlockPos(5, 3, 12)),
				"visitation_rite");
		PlaythroughSites.RiteFloor possess = PlaythroughSites.riteFloor(silentium, helper.absolutePos(new BlockPos(22, 3, 12)),
				"possession_rite");
		BlockPos surrogateAt = possess.focus().above().east(2);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, apotheos, visit);
		PlaythroughRites.prepare(clock, silentium, possess);
		clock.then(() -> {
			// A D7 below the rite: the degree line, the wrong-path line, or (pending D-32, not re-decided here) the layered
			// floor's "no rite matches", which it says when it filters out every rite the player may not attempt.
			refusedAtFocus(helper, archon, visit, List.of(RITE_REQUIRES, WRONG_PATH_RITE, FLOOR_NO_RITE),
					"a D7 Archon at the Rite of Visitation");
			refusedAtFocus(helper, silentium, visit, VISITATION_NOT_APOTHEOS, "a Silentium at the Rite of Visitation");
			refusedAtFocus(helper, apotheos, visit, NO_GARDENS, "an Apotheos at the Rite of Visitation");

			refusedAtFocus(helper, apotheos, possess, POSSESSION_NOT_SILENTIUM, "an Apotheos at the Rite of Possession");
			silentium.player().addEffect(new MobEffectInstance(EffectInit.mind_broken, 600)); // ARRANGE: a broken possession
			refusedAtFocus(helper, silentium, possess, POSSESSION_MIND_BROKEN, "a Silentium with a broken mind");
			silentium.player().removeEffect(EffectInit.mind_broken);
			refusedAtFocus(helper, silentium, possess, POSSESSION_NO_SURROGATE, "a Silentium with no surrogate");

			var wolf = spawn(helper, EntityType.WOLF, surrogateAt);
			wolf.setTame(true, false);
			wolf.setOwnerUUID(UUID.randomUUID()); // ARRANGE: another player's pet
			refusedAtFocus(helper, silentium, possess, POSSESSION_NOT_YOUR_PET, "a Silentium with another's wolf");
			wolf.discard();
			var bat = spawn(helper, EntityType.BAT, surrogateAt);
			refusedAtFocus(helper, silentium, possess, POSSESSION_NOT_BOUND, "a Silentium with an unbound bat");
			bat.discard();
			spawn(helper, EntityType.ARMOR_STAND, surrogateAt);
			refusedAtFocus(helper, silentium, possess, NO_GARDENS, "a Silentium with an armor stand");
		}).run();
	}

	/**
	 * The Rite of Ancestral Communion by a solo D7 Archon, the late game's hardest helper requirement: three recruits,
	 * one NPC per type and per outpost, so a Vicar, an Alchemist and a Mnemonist from three different outposts are
	 * pledged by their own dialogue option. The ledger's summon is used, then at INSCRIPTION two are assigned by a
	 * right-click; the seal is refused while the third station is empty (the helpers line), then the third is assigned
	 * and the rite completes. The fungal voice opens (its tree has English and no dead end) and an ending option is
	 * chosen; {@code voices_in_the_vein} is earned. The wave deck is pinned without the draining waves (ARRANGE,
	 * CHAIN-1); a recruit who has not reached his station 200 ticks after his assignment is set on it (ARRANGE: the walk).
	 */
	@GameTest(template = EMPTY, timeoutTicks = GRAND_TIMEOUT, batch = "playthrough_d8_ancestral")
	public static void ancestralCommunionWithThreeRecruits(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 22));
		PlaythroughStates.archonD7(p); // ARRANGE: a line, its Fane and D7
		tools(p);
		armed(helper, p);
		fillBlood(p);
		List<Entity> recruits = new ArrayList<>();
		recruits.add(PlaythroughNpcs.stampOutpost(helper, PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 4, 2)), 1));
		recruits.add(PlaythroughNpcs.stampOutpost(helper, PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 8, 2)), 2));
		recruits.add(PlaythroughNpcs.stampOutpost(helper, PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 12, 2)), 3));
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(GRAND_ORIGIN), "ancestral_communion");
		String helpersRefused = "message.hemomancy.cardinal_rite_interaction_handler.required_bloodline_helpers_must_take_their";
		int[] mark = new int[1];
		boolean[] refusedWithTwo = new boolean[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(() -> {
			for (Entity npc : recruits) {
				DialogueView view = p.approach(npc).talkTo(npc);
				if (view.offers("recruit_harbinger")) {
					Outcome pledged = p.choose(npc, "recruit_harbinger");
					PlaythroughLog.note(helper, "ancestral: pledge " + PlaythroughPlayer.describe(npc) + ": " + pledged.describe());
				} else {
					PlaythroughLog.note(helper, "ancestral: " + PlaythroughPlayer.describe(npc) + " offers no pledge at D7: "
							+ view.describe() + "; ARRANGE records the pledge");
					PlaythroughStates.recruited(p, npc); // ARRANGE
				}
			}
			var line = PlaythroughStates.lineOf(p);
			helper.assertTrue(line != null && line.getNpcMemberCount() == 3, "three recruits from three outposts: "
					+ (line == null ? "no line" : line.getNpcMemberCount()));
		});
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
			p.moveTo(PlaythroughRites.riteStand(floor));
			Outcome summon = p.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS);
			PlaythroughLog.note(helper, "ancestral: the ledger's summon: " + summon.describe());
			fillBlood(p);
			mark[0] = p.mark();
			p.wield(ItemInit.living_staff.get());
		}).step(() -> {
			BiConsumer<ActiveCardinalRite, Integer> assign = (rite, ticks) -> {
				if (p.blood() < 1500.0D) fillBlood(p); // ARRANGE: a rested pool between strokes
				refusedWithTwo[0] |= saidSince(p, mark[0], helpersRefused);
				// Two at once; the third only after the seal has been refused for want of him.
				int wanted = refusedWithTwo[0] ? 3 : 2;
				for (int i = 0; i < wanted; i++) assignAndPlace(p, floor, recruits.get(i), rite, ticks);
			};
			RitePlan plan = refilling(RitePlan.standard().fightThreats().releaseOnStill().weapon(Items.NETHERITE_SWORD)
					.extraAim(PlaythroughArchonGameTests.sigilsAndTraces(p)).traceFirst(), p, new int[1])
					.onPhase(CardinalRitePhase.INSCRIPTION, assign);
			return PlaythroughClock.rite(p, floor.focus(), PlaythroughRites.withoutDrainingWaves(plan, p));
		}).then(() -> {
			Outcome rite = p.last();
			PlaythroughLog.note(helper, "ancestral: the rite with three recruits: " + rite.describe() + "; seal refused with two: "
					+ refusedWithTwo[0]);
			helper.assertTrue(refusedWithTwo[0], "with two recruits the seal is refused with the helpers line");
			helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_VOICES_IN_THE_VEIN)), "the communion earns voices_in_the_vein; "
					+ rite);
			var opened = p.sent(com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket.class);
			helper.assertTrue(!opened.isEmpty(), "the fungal voice opens a dialogue");
			DialogueView voice = new DialogueView(opened.getLast().tree());
			voice.assertLangKeys(helper, "the Ancestral Communion voice").assertNoDeadEnds(helper, "the Ancestral Communion voice");
			String end = voice.reachableEvents().stream().filter(event -> event.startsWith("communion_")).findFirst().orElse(null);
			helper.assertTrue(end != null, "the voice offers a way to end the communion: " + voice.describe());
			Outcome chosen = p.forgeOption(end, 0); // the option the open screen offers, sent as the screen sends it
			PlaythroughLog.note(helper, "ancestral: chose " + end + ": " + chosen.describe());
		}).run();
	}

	/**
	 * At INSCRIPTION the caster right-clicks {@code npc} with an empty hand (EntityInteract), every 40 ticks until he is
	 * assigned; 200 ticks after his assignment, if he has not reached his station, he is set on it (ARRANGE: the walk).
	 */
	private static void assignAndPlace(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor, Entity npc, ActiveCardinalRite rite,
			int ticks) {
		ServerLevel level = p.player().serverLevel();
		var role = rite.getAllyRoles().get(npc.getUUID());
		if (role == null) {
			if (ticks % 40 != 0) return;
			boolean projecting = p.player().isUsingItem();
			if (projecting) p.releaseHold();
			PlaythroughRites.emptyHand(p);
			Outcome assigned = p.interactOn(npc);
			if (projecting) p.startHold(ItemInit.blood_projection.get(), npc.getEyePosition());
			PlaythroughLog.note(p.helper(), "assign " + PlaythroughPlayer.describe(npc) + " at INSCRIPTION: " + assigned.describe()
					+ " roles " + rite.getAllyRoles());
			return;
		}
		if (ticks % 200 != 100 || CardinalRiteAllyService.isAvailable(level, rite, npc.getUUID())) return;
		BlockPos station = floor.focus().offset(CardinalRiteAllyService.markers(floor.recipe()).get(role));
		PlaythroughLog.note(p.helper(), PlaythroughPlayer.describe(npc) + " has not taken the " + role + " station at "
				+ station.toShortString() + " (at " + npc.position() + "); ARRANGE sets him on it");
		npc.teleportTo(station.getX() + 0.5D, station.getY(), station.getZ() + 0.5D); // ARRANGE: the walk to the station
	}

	// =================================================================================================================
	// Apotheos: the Mycophant in the test level
	// =================================================================================================================

	/**
	 * The Spawn Mother owned by an Apotheos, fought to her last blow in the test level (ARRANGE: the Nursery is in the
	 * Chamber of Will; the fight up to that blow is skipped), then the real killing blow: she yields for 120 ticks,
	 * the owner is told the victory, earns {@code mycophant_defeated} and the heir advancement, and the unique Tendril
	 * lands. A second victory pays the repeat reward (Quintessence and spore sacs) and no second Tendril.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1000, batch = "playthrough_d8_mycophant")
	public static void mycophantVictoryInTestLevel(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.apotheosD8(p); // ARRANGE
		BlockPos lair = PlaythroughSites.stand(helper, 16, 12);
		MycophantEntity[] boss = {PlaythroughNpcs.ownedMycophant(helper, p, lair)};
		int[] mark = new int[1];
		long[] fell = new long[1];
		int[] before = new int[2];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(() -> mark[0] = p.mark())
				.step(lastBlows(helper, p, () -> boss[0]))
				.then(() -> fell[0] = helper.getTick())
				.until(MycophantYieldRules.length() + 60, () -> boss[0].isRemoved(), "the Mycophant's yield ends")
				.then(() -> {
					long yielded = helper.getTick() - fell[0];
					PlaythroughLog.note(helper, "the Mycophant yielded over " + yielded + " ticks; said " + p.chatSince(mark[0]));
					var degree = HemoCapabilityAccess.getInitiatoryDegree(p.player());
					helper.assertTrue(degree.isMycophantDefeated(), "the victory is recorded");
					helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_MYCOPHANT_DEFEATED))
							&& p.has(adv(HarbingerAdvancementGranter.ADV_HIVE_HEIR)), "mycophant_defeated and the heir advancement");
					helper.assertTrue(saidSince(p, mark[0], MYCOPHANT_VICTORY), "the owner is told the victory");
					helper.assertTrue(p.count(ItemInit.mycophant_tendril.get()) == 1
							&& p.owed(MycophantEncounterManager.TENDRIL).isEmpty(), "the unique Tendril lands once");
					helper.assertTrue(!p.player().getPersistentData().getBoolean(PlaythroughNpcs.MYCOPHANT_ACTIVE_KEY),
							"the encounter is over");
					before[0] = p.count(ItemInit.sanguine_quintessence.get());
					before[1] = p.count(ItemInit.spore_sac.get());
					boss[0] = PlaythroughNpcs.ownedMycophant(helper, p, lair); // ARRANGE: a rematch already lured
					mark[0] = p.mark();
				})
				.step(lastBlows(helper, p, () -> boss[0]))
				.until(MycophantYieldRules.length() + 60, () -> boss[0].isRemoved(), "the rematch's yield ends")
				.then(() -> {
					int quintessence = p.count(ItemInit.sanguine_quintessence.get()) - before[0];
					int sacs = p.count(ItemInit.spore_sac.get()) - before[1];
					PlaythroughLog.note(helper, "repeat victory: +" + quintessence + " Quintessence, +" + sacs + " spore sacs; "
							+ p.chatSince(mark[0]));
					helper.assertTrue(quintessence == 1 && sacs >= 2 && sacs <= 4, "the repeat victory pays one Quintessence and "
							+ "two to four spore sacs: +" + quintessence + ", +" + sacs);
					helper.assertTrue(p.count(ItemInit.mycophant_tendril.get()) == 1, "no second Tendril");
				})
				.run();
	}

	/** A full pack at the first victory: the Tendril is owed, never dropped, and lands once a slot frees. */
	// FIX: LF-6 - the first Tendril queued on a full pack says so (message.hemomancy.mycophant.make_room), as Vesper's
	// Memory does (message.hemomancy.vesper_ordeal_manager.make_room_in_your_inventory_to).
	@GameTest(template = EMPTY, timeoutTicks = 600, batch = "playthrough_d8_mycophant_room")
	public static void tendrilMakeRoom(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.apotheosD8(p); // ARRANGE
		MycophantEntity boss = PlaythroughNpcs.ownedMycophant(helper, p, PlaythroughSites.stand(helper, 16, 12));
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					p.fillInventory(); // ARRANGE: a full pack
					mark[0] = p.mark();
				})
				.step(lastBlows(helper, p, () -> boss))
				.until(MycophantYieldRules.length() + 60, boss::isRemoved, "the Mycophant's yield ends")
				.then(() -> {
					PlaythroughLog.note(helper, "full-pack victory said " + p.chatSince(mark[0]));
					helper.assertTrue(p.count(ItemInit.mycophant_tendril.get()) == 0
									&& !p.owed(MycophantEncounterManager.TENDRIL).isEmpty(),
							"the Tendril is owed, not in a full pack");
					helper.assertTrue(dropsNear(p, p.player().blockPosition(), ItemInit.mycophant_tendril.get()) == 0,
							"the Tendril is not dropped on the floor");
					p.player().getInventory().items.set(0, ItemStack.EMPTY); // the player makes room
					p.tickOnce();
					helper.assertTrue(p.count(ItemInit.mycophant_tendril.get()) == 1
							&& p.owed(MycophantEncounterManager.TENDRIL).isEmpty(), "the owed Tendril lands once room is made");
					helper.assertTrue(saidSince(p, mark[0], MYCOPHANT_MAKE_ROOM),
							"LF-6: a full pack at the victory is told to make room: said " + p.chatSince(mark[0]));
				})
				.run();
	}

	/**
	 * The Mycophant yields for 120 ticks before the victory is settled. One owner logs out in that time and another
	 * dies (the death is caught by the encounter and expels them): neither keeps the win.
	 */
	// DECISION D8-2 (the D-20 pattern): a Mycophant victory is lost if the owner logs out or dies during the 120-tick
	// yield. Options: record the win by UUID and settle it at the next login / settle at the killing blow / keep.
	@GameTest(template = EMPTY, timeoutTicks = 1000, batch = "playthrough_d8_mycophant_yield", required = false)
	public static void mycophantYieldLogout(GameTestHelper helper) {
		PlaythroughPlayer leaver = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughPlayer dier = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 20));
		PlaythroughStates.apotheosD8(leaver); // ARRANGE
		PlaythroughStates.apotheosD8(dier); // ARRANGE
		MycophantEntity first = PlaythroughNpcs.ownedMycophant(helper, leaver, PlaythroughSites.stand(helper, 14, 4));
		MycophantEntity second = PlaythroughNpcs.ownedMycophant(helper, dier, PlaythroughSites.stand(helper, 14, 20));
		PlaythroughClock.start(helper)
				.step(lastBlows(helper, leaver, () -> first))
				.then(leaver::offline)
				.step(lastBlows(helper, dier, () -> second))
				.then(() -> {
					Outcome died = dier.die();
					PlaythroughLog.note(helper, "the owner dies in the yield: " + died.describe());
				})
				.until(MycophantYieldRules.length() + 80, () -> first.isRemoved() && second.isRemoved(), "both yields end")
				.then(() -> {
					leaver.online();
					leaver.relog();
					boolean leaverWon = HemoCapabilityAccess.getInitiatoryDegree(leaver.player()).isMycophantDefeated();
					boolean dierWon = HemoCapabilityAccess.getInitiatoryDegree(dier.player()).isMycophantDefeated();
					PlaythroughLog.note(helper, "D8-2: logged out in the yield won=" + leaverWon + " tendril="
							+ leaver.count(ItemInit.mycophant_tendril.get()) + "; died in the yield won=" + dierWon + " tendril="
							+ dier.count(ItemInit.mycophant_tendril.get()) + " said " + dier.chat());
					helper.assertTrue(leaverWon && dierWon, "DECISION D8-2: a Mycophant felled by the owner stays won through a "
							+ "logout or a death in the yield; logout won=" + leaverWon + ", death won=" + dierWon);
				})
				.run();
	}

	/**
	 * A failed rematch (the owner dies in the Nursery after a lure): the expelled line promises the hive will remember
	 * after five minutes in the Gardens, but a defeated Apotheos's hunt never re-arms on its own; only a new lure
	 * starts a rematch.
	 */
	// DECISION D8-5: the expelled line ("It will remember after five minutes in the Gardens") promises a retry that a
	// rematch does not give. Options: reword the line for a rematch / re-arm the rematch after the cooldown / keep.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "playthrough_d8_mycophant_rematch", required = false)
	public static void mycophantRematchLine(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.apotheosD8(p); // ARRANGE
		var degree = HemoCapabilityAccess.getInitiatoryDegree(p.player());
		degree.setMycophantDefeated(true); // ARRANGE: the first victory won long ago
		InitiatoryDegreeEvents.syncDegree(p.player(), degree);
		PlaythroughNpcs.ownedMycophant(helper, p, PlaythroughSites.stand(helper, 16, 12));
		p.player().getPersistentData().putBoolean(MYCOPHANT_REMATCH_KEY, true); // ARRANGE: as a lure's claim leaves it
		Outcome died = p.die();
		died.said(MYCOPHANT_EXPELLED);
		int cooldown = degree.getMycophantRetryCooldownTicks();
		boolean rearms = MycophantEncounterRules.shouldAccumulate(degree.getDegreeNumber(), degree.getArchonPath(),
				degree.isMycophantDefeated(), true, true, false, false, false);
		boolean lure = MycophantEncounterRules.canUseLure(degree.getDegreeNumber(), degree.getArchonPath(),
				degree.isMycophantDefeated(), 0, false, true, true);
		PlaythroughLog.note(helper, "D8-5: expelled after a rematch: " + died.describe() + "; cooldown " + cooldown
				+ " ticks; after it the hunt re-arms=" + rearms + ", a new lure works=" + lure);
		helper.assertTrue(rearms, "DECISION D8-5: the expelled line promises the Gardens remember the player after five "
				+ "minutes, so a defeated Apotheos's rematch comes back by itself; re-arms=" + rearms + ", lure=" + lure);
		helper.succeed();
	}

	// =================================================================================================================
	// Silentium: the Severance, the wound, the Memory
	// =================================================================================================================

	/**
	 * The Severance on the player's own Bloom with a base Living Arsenal weapon: refused while the choice is pending
	 * and at APOTHEOS_PENDING, refused to a stranger; for a Silence the first cut shapes the Sickle and the second
	 * severs. A relog between the cuts loses nothing; a Sickle shaped on a Bloom that has since regrown is refused;
	 * a fresh Sickle severs the new Bloom and the weapon comes back.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "playthrough_d8_bloom_cut")
	public static void severanceCuts(GameTestHelper helper) {
		BlockPos root = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughPlayer owner = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.communedD7(owner, root); // ARRANGE: the owner's real Bloom, nine pomes eaten
		PlaythroughStates.choicePendingD7(owner); // ARRANGE
		PlaythroughPlayer stranger = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 10));
		PlaythroughStates.silentPendingD7(stranger); // ARRANGE
		QliphothBloomSavedData blooms = blooms(owner);
		QliphothBloomSavedData.BloomEntry bloom = bloomAt(helper, owner, root);
		owner.moveTo(root.south(2));
		stranger.moveTo(root.north(2));

		owner.player().setItemInHand(InteractionHand.MAIN_HAND, livingBlade()); // ARRANGE: a Living Arsenal blade
		cut(owner, root).said(TREE_WAITS);
		helper.assertTrue(!LivingSicklePruning.isTemporarySickle(owner.player().getMainHandItem()), "no Sickle while undecided");
		PlaythroughStates.apotheosPendingD7(owner); // ARRANGE: the Eighth Degree chosen
		cut(owner, root).said(TREE_WAITS);
		setPath(owner, EnumArchonPath.SILENT_PENDING); // ARRANGE: the same owner as a Silence

		stranger.player().setItemInHand(InteractionHand.MAIN_HAND, livingBlade()); // ARRANGE
		cut(stranger, root).said(TREE_WAITS);
		helper.assertTrue(blooms.getState(bloom) == SeveredQliphothState.LIVING, "a stranger cannot cut another's Bloom");

		cut(owner, root).said(SICKLE_SHAPED);
		helper.assertTrue(LivingSicklePruning.isTemporarySickle(owner.player().getMainHandItem()), "the first cut makes the Sickle");
		owner.relog();
		boolean sickle = carries(owner, LivingSicklePruning::isTemporarySickle);
		boolean blade = carries(owner, LivingSicklePruning::isBaseLivingWeapon);
		// A logout folds a conjured Living Arsenal form (the Sickle included) back into the Living Staff it came from.
		boolean staff = carries(owner, stack -> stack.is(ItemInit.living_staff.get()));
		PlaythroughLog.note(helper, "after a relog between the cuts: Sickle=" + sickle + " base weapon=" + blade
				+ " Staff=" + staff + " main hand=" + owner.player().getMainHandItem() + " state=" + blooms.getState(bloom));
		helper.assertTrue(sickle || blade || staff, "a relog between the cuts loses neither the Sickle, the weapon nor the Staff");
		helper.assertTrue(blooms.getState(bloom) == SeveredQliphothState.LIVING, "a relog between the cuts leaves the Bloom whole");
		if (sickle) {
			PlaythroughRites.wieldStack(owner, LivingSicklePruning::isTemporarySickle, "the Sickle");
		} else if (!blade) {
			removeAll(owner, ItemInit.living_staff.get()); // ARRANGE: the Staff conjures its blade again
			owner.player().setItemInHand(InteractionHand.MAIN_HAND, livingBlade());
			cut(owner, root).said(SICKLE_SHAPED);
		} else {
			PlaythroughRites.wieldStack(owner, LivingSicklePruning::isBaseLivingWeapon, "the weapon");
			cut(owner, root).said(SICKLE_SHAPED);
		}

		// ARRANGE: the Bloom regrown at the same root, a new lifecycle (the old Sickle was shaped on the one before).
		String dimension = owner.player().level().dimension().location().toString();
		blooms.removeBloomInChunk(root, dimension);
		QliphothBloomSavedData.BloomEntry regrown = new QliphothBloomSavedData.BloomEntry(owner.uuid(), root, dimension, 3,
				helper.getLevel().getGameTime());
		blooms.addBloom(regrown);
		HarbingerCardinalRiteEvents.syncQliphothBlooms(helper.getLevel().getServer());
		cut(owner, root).said(SICKLE_REFUSES);
		helper.assertTrue(blooms.getState(regrown) == SeveredQliphothState.LIVING, "the old Sickle cannot cut the regrown Bloom");

		owner.player().setItemInHand(InteractionHand.MAIN_HAND, livingBlade()); // ARRANGE: the weapon conjured again
		cut(owner, root).said(SICKLE_SHAPED);
		cut(owner, root).said(TRUNK_PARTS);
		helper.assertTrue(blooms.getState(regrown) == SeveredQliphothState.OPEN, "the second cut severs the Bloom");
		helper.assertTrue(owner.player().getMainHandItem().is(ItemInit.living_blade.get()), "the weapon comes back after the cut");
		helper.assertTrue(path(owner) == EnumArchonPath.SILENT_PENDING, "severing grants nothing on its own");
		helper.succeed();
	}

	/**
	 * The open wound refused here: its owner, a Silence, is told the refusal has no place to open (the Chamber of
	 * Will is not loaded) and the wound stays open; a stranger is told the wound has no memory of them; the owner on
	 * the wrong path is turned away.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "playthrough_d8_bloom_wound")
	public static void vesperWoundRefusal(GameTestHelper helper) {
		BlockPos root = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughPlayer owner = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.communedD7(owner, root); // ARRANGE
		PlaythroughStates.silentPendingD7(owner); // ARRANGE
		PlaythroughPlayer stranger = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 10));
		PlaythroughStates.silentPendingD7(stranger); // ARRANGE
		QliphothBloomSavedData.BloomEntry bloom = bloomAt(helper, owner, root);
		owner.moveTo(root.south(2));
		stranger.moveTo(root.north(2));
		owner.player().setItemInHand(InteractionHand.MAIN_HAND, livingBlade()); // ARRANGE
		cut(owner, root).said(SICKLE_SHAPED);
		cut(owner, root).said(TRUNK_PARTS);
		helper.assertTrue(blooms(owner).getState(bloom) == SeveredQliphothState.OPEN, "the wound is open");

		PlaythroughRites.emptyHand(owner);
		Outcome refused = owner.useOn(root);
		refused.said(NO_PLACE_TO_OPEN);
		helper.assertTrue(blooms(owner).getState(bloom) == SeveredQliphothState.OPEN && !VesperOrdealManager.isActive(owner.player())
				&& path(owner) == EnumArchonPath.SILENT_PENDING, "the wound is kept for when the Chamber can open; " + refused);

		PlaythroughRites.emptyHand(stranger);
		stranger.useOn(root).said(WOUND_NO_MEMORY);

		setPath(owner, EnumArchonPath.APOTHEOS_PENDING); // ARRANGE: the owner on the other road
		owner.useOn(root).said(WOUND_REJECTS_PATH);
		setPath(owner, EnumArchonPath.SILENT_PENDING);
		helper.assertTrue(blooms(owner).getState(bloom) == SeveredQliphothState.OPEN, "the wound is still open");
		helper.succeed();
	}

	/** The Vesper ordeal itself runs in the Chamber of Will, which this server does not load: NOT RUN. */
	@GameTest(template = EMPTY, timeoutTicks = 20, batch = BATCH, required = false)
	public static void vesperOrdeal(GameTestHelper helper) {
		PlaythroughNotRun.require(helper, ChamberOfWillManager.CHAMBER_OF_WILL, "the Vesper ordeal (throne anchors, the "
				+ "Evening Star, the drain on the downed body)", PlaythroughNotRun.VESPER, PlaythroughNotRun.CHAMBER);
		PlaythroughNotRun.notRun(helper, "the ordeal needs its arena and boss; play it in the review client",
				PlaythroughNotRun.VESPER);
	}

	/**
	 * The Memory of Vesper owed after the victory lands exactly once whatever the player is doing: a full pack keeps
	 * it owed until a slot frees; a Creative full pack does not count as delivered; a dead player keeps the claim and
	 * gets it on respawn; a relog delivers it at login. Later ticks and relogs never give a second.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void vesperMemoryDelivery(GameTestHelper helper) {
		Item memory = ItemInit.memory_of_vesper.get();

		PlaythroughPlayer full = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 2));
		PlaythroughStates.vesperBeatenD7(full); // ARRANGE
		full.fillInventory(); // ARRANGE
		full.tickOnce();
		helper.assertTrue(full.count(memory) == 0 && !full.owed(VesperOrdealManager.MEMORY).isEmpty(), "a full pack keeps it owed");
		full.player().getInventory().items.set(0, ItemStack.EMPTY);
		full.tickOnce();
		helper.assertTrue(full.count(memory) == 1 && full.owed(VesperOrdealManager.MEMORY).isEmpty(), "it lands once room is made");
		full.tickOnce();
		full.relog();
		helper.assertTrue(full.count(memory) == 1, "no second Memory after more ticks and a relog");

		PlaythroughPlayer creative = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 7, 2));
		PlaythroughStates.vesperBeatenD7(creative); // ARRANGE
		creative.creative().fillInventory(); // ARRANGE
		creative.tickOnce();
		helper.assertTrue(!creative.owed(VesperOrdealManager.MEMORY).isEmpty(), "a Creative discard is not a delivery");
		creative.survival();
		creative.player().getInventory().items.set(0, ItemStack.EMPTY);
		creative.tickOnce();
		helper.assertTrue(creative.count(memory) == 1 && creative.owed(VesperOrdealManager.MEMORY).isEmpty(),
				"the Creative player gets it once there is a real slot");

		PlaythroughPlayer dead = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 2));
		PlaythroughStates.vesperBeatenD7(dead); // ARRANGE
		dead.die();
		dead.tickOnce();
		helper.assertTrue(!dead.owed(VesperOrdealManager.MEMORY).isEmpty(), "a dead player keeps the claim");
		dead.respawn();
		dead.tickOnce();
		helper.assertTrue(dead.count(memory) == 1 && dead.owed(VesperOrdealManager.MEMORY).isEmpty(),
				"the respawned player gets the Memory once: " + dead.count(memory));

		PlaythroughPlayer relog = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 17, 2));
		PlaythroughStates.vesperBeatenD7(relog); // ARRANGE
		relog.relog();
		helper.assertTrue(relog.count(memory) == 1, "a login delivers the owed Memory");
		relog.relog();
		relog.tickOnce();
		helper.assertTrue(relog.count(memory) == 1, "a second login gives no second Memory");
		helper.succeed();
	}

	/**
	 * The sealed scar of the player's own Bloom reissues a lost Memory: refused while it is carried, reissued once it
	 * is gone, and while a reissue is still owed (a full pack) a second ask gives nothing more. Exactly one lands.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "playthrough_d8_bloom_reissue")
	public static void vesperMemoryReissue(GameTestHelper helper) {
		Item memory = ItemInit.memory_of_vesper.get();
		BlockPos root = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.communedD7(p, root); // ARRANGE
		PlaythroughStates.vesperBeatenD7(p); // ARRANGE (NOT RUN: Vesper ordeal); the Bloom severed and sealed
		QliphothBloomSavedData.BloomEntry bloom = bloomAt(helper, p, root);
		helper.assertTrue(blooms(p).getState(bloom).isSealedTrophy(), "the Bloom is a sealed scar after the victory");
		p.tickOnce();
		helper.assertTrue(p.count(memory) == 1, "the Memory is delivered");
		p.moveTo(root.south(2));

		PlaythroughRites.emptyHand(p);
		p.useOn(root).said(SCAR_STILL_YOURS);
		helper.assertTrue(p.count(memory) == 1, "no reissue while the Memory is carried");

		removeAll(p, memory); // ARRANGE: the Memory lost (burnt)
		PlaythroughRites.emptyHand(p);
		p.useOn(root).said(SCAR_GIVES_BACK);
		helper.assertTrue(p.count(memory) == 1, "the scar gives the lost Memory back once");

		removeAll(p, memory); // ARRANGE: lost again
		p.fillInventory(); // ARRANGE: and the pack full
		Outcome reissued = p.useOn(root);
		reissued.said(SCAR_GIVES_BACK);
		helper.assertTrue(p.count(memory) == 0 && !p.owed(VesperOrdealManager.MEMORY).isEmpty(),
				"a full pack keeps the reissue owed; " + reissued);
		Outcome again = p.useOn(root);
		again.said(SCAR_STILL_YOURS);
		helper.assertTrue(p.owed(VesperOrdealManager.MEMORY).size() == 1, "a second ask while owed queues nothing more");
		p.player().getInventory().items.set(0, ItemStack.EMPTY);
		p.tickOnce();
		helper.assertTrue(p.count(memory) == 1 && p.owed(VesperOrdealManager.MEMORY).isEmpty(), "exactly one Memory lands");
		helper.succeed();
	}

	/** A Memory stashed in a chest is not carried, so the scar reissues it: two Memories exist. */
	// DECISION D-19 (late instance): reissue checks look only at the carried inventory, so a stashed Memory of Vesper is
	// reissued. Options: once per claim (track the issued item) / keep as a lost-item safety net.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "playthrough_d8_bloom_stash", required = false)
	public static void vesperMemoryStashedInAChest(GameTestHelper helper) {
		Item memory = ItemInit.memory_of_vesper.get();
		BlockPos root = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.communedD7(p, root); // ARRANGE
		PlaythroughStates.vesperBeatenD7(p); // ARRANGE
		p.tickOnce();
		helper.assertTrue(p.count(memory) == 1, "the Memory is delivered");
		BlockPos chestPos = PlaythroughSites.stand(helper, 12, 16);
		PlaythroughRites.set(helper, chestPos, Blocks.CHEST); // ARRANGE: the player's chest
		helper.assertTrue(helper.getLevel().getBlockEntity(chestPos) instanceof ChestBlockEntity, "the chest has its inventory");
		((ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos)).setItem(0, new ItemStack(memory));
		removeAll(p, memory); // ARRANGE: moved from the pack into the chest
		p.moveTo(root.south(2));
		PlaythroughRites.emptyHand(p);
		Outcome asked = p.useOn(root);
		int total = p.count(memory) + ((ChestBlockEntity) helper.getLevel().getBlockEntity(chestPos)).getItem(0).getCount();
		PlaythroughLog.note(helper, "D-19: the scar asked with the Memory in a chest: " + asked.describe() + "; Memories in all " + total);
		helper.assertTrue(total == 1, "DECISION D-19: a stashed Memory is not reissued; there are " + total);
		helper.succeed();
	}

	/**
	 * Silentium by play: the Memory of Vesper set in an Iron Brazier, refused while the brazier is unlit, refused to
	 * the Blood Absorption utility in hand (the Staff must draw it), then drawn in with the Living Staff on
	 * absorption: Degree 8 Silentium, the Silentium title, {@code conjure_sickle} known. A player on the Apotheos road
	 * holding a Memory is refused.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = BATCH)
	public static void silentiumAbsorption(GameTestHelper helper) {
		Item memory = ItemInit.memory_of_vesper.get();
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.vesperBeatenD7(silent); // ARRANGE (NOT RUN: Vesper ordeal)
		silent.tickOnce();
		helper.assertTrue(silent.count(memory) == 1, "the Memory is delivered");
		bond(silent); // ARRANGE: the Staff bond from the Staff's own craft
		tools(silent);
		fillBlood(silent);
		PlaythroughPlayer other = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 18));
		PlaythroughStates.apotheosPendingD7(other); // ARRANGE
		other.give(new ItemStack(memory)); // ARRANGE: a Memory that is not theirs to draw
		bond(other); // ARRANGE
		tools(other);
		fillBlood(other);
		BlockPos brazier = PlaythroughSites.ironBrazier(helper, PlaythroughSites.stand(helper, 14, 8), false);
		BlockPos otherBrazier = PlaythroughSites.ironBrazier(helper, PlaythroughSites.stand(helper, 14, 18), false);
		String absorption = ManipulationInit.blood_absorption.get().getName();
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					silent.moveTo(brazier.south(2));
					PlaythroughRites.insertOffering(silent, brazier, stack -> stack.is(memory), "the Memory of Vesper");
					silent.select(absorption);
					mark[0] = silent.mark();
				})
				.step(() -> silent.hold(ItemInit.living_staff.get(), 45, PlaythroughRites.bowl(brazier)))
				.then(() -> {
					helper.assertTrue(saidSince(silent, mark[0], LIGHT_FIRST), "an unlit brazier says to light it: "
							+ silent.chatSince(mark[0]));
					helper.assertTrue(silent.degree() == 7, "nothing is drawn from an unlit brazier");
				})
				.step(() -> PlaythroughRites.lightBrazier(silent, brazier))
				.then(() -> {
					helper.assertTrue(PlaythroughRites.lit(silent, brazier), "the brazier lights; " + silent.last());
					mark[0] = silent.mark();
				})
				.step(() -> silent.hold(ItemInit.blood_absorption.get(), 45, PlaythroughRites.bowl(brazier)))
				.then(() -> {
					helper.assertTrue(saidSince(silent, mark[0], HOLD_THE_STAFF), "the Blood Absorption utility in hand is told to "
							+ "hold the Living Staff: " + silent.chatSince(mark[0]));
					helper.assertTrue(silent.degree() == 7 && PlaythroughRites.brazier(silent, brazier).hasOffering(),
							"the Memory stays in the brazier");
					silent.select(absorption);
					mark[0] = silent.mark();
				})
				.step(() -> silent.holdUntil(ItemInit.living_staff.get(), 300, () -> PlaythroughRites.bowl(brazier),
						() -> silent.degree() == 8))
				.then(() -> {
					PlaythroughLog.note(helper, "Silentium absorption: " + silent.last() + "; said " + silent.chatSince(mark[0]));
					helper.assertTrue(silent.degree() == 8 && path(silent) == EnumArchonPath.SILENTIUM,
							"drawing the Memory in is Degree 8 Silentium; degree " + silent.degree() + " " + path(silent));
					helper.assertTrue(saidSince(silent, mark[0], MEMORY_AWAKENED) && saidSince(silent, mark[0], TITLE_SILENTIUM),
							"the player is told the Memory woke and the Silentium title: " + silent.chatSince(mark[0]));
					helper.assertTrue(silent.knows(ManipulationInit.conjure_sickle.get().getName()), "conjure_sickle is known");
					helper.assertTrue(!PlaythroughRites.brazier(silent, brazier).hasOffering() && silent.count(memory) == 0,
							"the Memory is drawn in, not left behind");
					PlaythroughStates.assertAt(helper, silent, PlaythroughStates.Checkpoint.SILENTIUM_D8);
					other.moveTo(otherBrazier.south(2));
					PlaythroughRites.insertOffering(other, otherBrazier, stack -> stack.is(memory), "the Memory of Vesper");
				})
				.step(() -> PlaythroughRites.lightBrazier(other, otherBrazier))
				.then(() -> {
					other.select(absorption);
					mark[0] = other.mark();
				})
				.step(() -> other.hold(ItemInit.living_staff.get(), 45, PlaythroughRites.bowl(otherBrazier)))
				.then(() -> {
					helper.assertTrue(saidSince(other, mark[0], MEMORY_ANSWERS_ONLY), "the Memory answers only a Silence that beat "
							+ "him: " + other.chatSince(mark[0]));
					helper.assertTrue(other.degree() == 7 && path(other) == EnumArchonPath.APOTHEOS_PENDING,
							"the Apotheos road does not become Silentium");
				})
				.run();
	}

	/**
	 * What a Silentium can no longer do: the Rite of Ancestral Communion is refused at planting, a whisper queued
	 * before the cut is not heard (the inbox says the thread was cut and clears), the Spine will not carry them, and
	 * the Silent Archon armature recipes answer a Silentium only (not a Silence still pending, not an Apotheos).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1500, batch = RITES)
	public static void silentiumRestrictions(GameTestHelper helper) {
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 22));
		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 22));
		PlaythroughPlayer pending = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 14, 22));
		PlaythroughStates.silentiumD8(silent); // ARRANGE
		PlaythroughStates.apotheosD8(apotheos); // ARRANGE
		PlaythroughStates.silentPendingD7(pending); // ARRANGE
		tools(silent);
		fillBlood(silent);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(silent, helper.absolutePos(GRAND_ORIGIN), "ancestral_communion");
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, silent, floor);
		clock.then(() -> {
			refusedAtFocus(helper, silent, floor, ANCESTRAL_SILENT, "a Silentium at Ancestral Communion");

			Outcome.Probe queued = Outcome.begin(silent, null, "a whisper queued before the cut");
			PendingWhispers.enqueue(silent.player(), FungalWhisperDialogueTrees.forDegree(7, 0, "")); // ARRANGE
			queued.finish().didNotSay(WHISPER_PENDING);
			Outcome opened = silent.openWhisper();
			opened.said(WHISPER_SILENCED);
			helper.assertTrue(silent.pendingWhispers().isEmpty(), "the unheard whisper is cleared");
			Outcome.Probe control = Outcome.begin(apotheos, null, "the same whisper for an Apotheos");
			PendingWhispers.enqueue(apotheos.player(), FungalWhisperDialogueTrees.forDegree(7, 0, "")); // ARRANGE
			control.finish().said(WHISPER_PENDING);
			apotheos.openWhisper();
			helper.assertTrue(apotheos.openWhisperId().isPresent(), "an Apotheos still hears the hive");

			silent.wield(ItemInit.fungal_spine.get());
			silent.use(InteractionHand.MAIN_HAND).said(SPINE_REFUSED);

			ResourceLocation recipeId = Hemomancy.rloc("armature_upgrade/blood_lust_to_silent_archon_helm");
			var holder = helper.getLevel().getRecipeManager().byKey(recipeId);
			helper.assertTrue(holder.isPresent() && holder.get().value() instanceof ArmatureUpgradeRecipe,
					recipeId + " is loaded");
			ArmatureUpgradeRecipe recipe = (ArmatureUpgradeRecipe) holder.get().value();
			var gate = recipe.getPersistentDataGate();
			helper.assertTrue(gate != null, "the Silent Archon helm has a path gate");
			boolean silentium = gate.matches(silent.player());
			boolean silence = gate.matches(pending.player());
			boolean apotheosMatches = gate.matches(apotheos.player());
			PlaythroughLog.note(helper, "Silent Archon helm gate: Silentium=" + silentium + " SILENT_PENDING=" + silence
					+ " Apotheos=" + apotheosMatches);
			helper.assertTrue(silentium && !silence && !apotheosMatches,
					"Silent Archon recipes answer a Silentium only: " + silentium + "/" + silence + "/" + apotheosMatches);
		}).run();
	}

	// =================================================================================================================
	// The teachers and the ledger
	// =================================================================================================================

	/**
	 * The Vicar, Alchemist, Artificer and Mnemonist each add exactly one ending line to their greeting for each of the
	 * six late states (undecided, silent_pending, silent_absorb, silent_archon, apotheos_pending, apotheos): 24 keys,
	 * each with English, each at its own state only.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void endingLinesPerPath(GameTestHelper helper) {
		Map<String, Entity> teachers = teachers(helper);
		Map<String, PlaythroughPlayer> states = endingStates(helper);
		List<String> problems = new ArrayList<>();
		for (Map.Entry<String, PlaythroughPlayer> state : states.entrySet()) {
			PlaythroughPlayer p = state.getValue();
			for (Map.Entry<String, Entity> teacher : teachers.entrySet()) {
				p.approach(teacher.getValue());
				DialogueView view = p.talkTo(teacher.getValue());
				String want = ENDING_PREFIX + teacher.getKey() + "." + state.getKey();
				List<String> endings = endingKeys(view);
				if (!endings.equals(List.of(want))) problems.add(teacher.getKey() + " at " + state.getKey() + " shows " + endings);
				if (!LangKeys.exists(want)) problems.add(want + " has no English");
				problems.addAll(view.missingLangKeys().stream().map(key -> teacher.getKey() + " at " + state.getKey() + ": " + key)
						.toList());
			}
		}
		PlaythroughLog.note(helper, "ending lines: " + (problems.isEmpty() ? "all 24 at their states" : problems));
		helper.assertTrue(problems.isEmpty(), "the teachers' ending lines: " + problems);
		helper.succeed();
	}

	/**
	 * A D8 Silentium greets the teachers and hears the same greeting tree as a D7 Archon (only the ending line
	 * differs), while an Apotheos gets the eighth-degree tree.
	 */
	// DECISION D8-4: a D8 Silentium is greeted with the D7 Archon tree (HarbingerPathPermissions.greetingDegree). Options:
	// a Silentium tree of its own / keep the Archon tree with the ending line as the only acknowledgement.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void silentiumGreetedAsArchon(GameTestHelper helper) {
		Map<String, Entity> teachers = teachers(helper);
		PlaythroughPlayer archon = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 2));
		PlaythroughPlayer silentium = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 2));
		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 14, 2));
		PlaythroughStates.archonD7(archon); // ARRANGE
		PlaythroughStates.silentiumD8(silentium); // ARRANGE
		PlaythroughStates.apotheosD8(apotheos); // ARRANGE
		List<String> sameAsArchon = new ArrayList<>();
		for (Map.Entry<String, Entity> teacher : teachers.entrySet()) {
			Set<String> asArchon = signature(archon, teacher.getValue());
			Set<String> asSilentium = signature(silentium, teacher.getValue());
			Set<String> asApotheos = signature(apotheos, teacher.getValue());
			PlaythroughLog.note(helper, "D8-4 " + teacher.getKey() + ": Silentium tree "
					+ (asSilentium.equals(asArchon) ? "is the Archon's" : "differs from the Archon's") + "; Apotheos tree "
					+ (asApotheos.equals(asArchon) ? "is the Archon's" : "differs"));
			if (asSilentium.equals(asArchon)) sameAsArchon.add(teacher.getKey());
		}
		helper.assertTrue(sameAsArchon.isEmpty(), "DECISION D8-4: a D8 Silentium is greeted as what they are, not with the D7 "
				+ "Archon tree; the same tree from " + sameAsArchon);
		helper.succeed();
	}

	/**
	 * The ledger's Blood Beneath Blood card read at every movement: 0 to 9 pomes, Communion (10), the silent choice
	 * (11), Vesper beaten (12), Silentium (13), and on the other road the Eighth Degree chosen (11) then Apotheos (13).
	 * Each step shows the card the screen would and every key has English.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void ledgerBloodBeneath(GameTestHelper helper) {
		List<String> problems = new ArrayList<>();
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.archonD7(silent); // ARRANGE
		var degree = HemoCapabilityAccess.getInitiatoryDegree(silent.player());
		for (int pomes = 0; pomes <= 9; pomes++) {
			expectLedger(silent, pomes, "pomes", problems);
			if (pomes < 9) {
				degree.incrementTotalPomesConsumed(); // ARRANGE: one more husk eaten
				InitiatoryDegreeEvents.syncDegree(silent.player(), degree);
			}
		}
		degree.setQliphothCommunionDone(true); // ARRANGE: Communion
		InitiatoryDegreeEvents.syncDegree(silent.player(), degree);
		expectLedger(silent, 10, "revelation", problems);
		PlaythroughStates.choicePendingD7(silent); // ARRANGE
		expectLedger(silent, 10, "revelation", problems);
		PlaythroughStates.silentPendingD7(silent); // ARRANGE
		expectLedger(silent, 11, "prune", problems);
		PlaythroughStates.vesperBeatenD7(silent); // ARRANGE
		expectLedger(silent, 12, "absorb", problems);
		PlaythroughStates.silentiumD8(silent); // ARRANGE
		expectLedger(silent, 13, "complete", problems);

		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 16));
		PlaythroughStates.choicePendingD7(apotheos); // ARRANGE
		expectLedger(apotheos, 10, "revelation", problems);
		PlaythroughStates.apotheosPendingD7(apotheos); // ARRANGE
		expectLedger(apotheos, 11, "apotheos_pending", problems);
		PlaythroughStates.apotheosD8(apotheos); // ARRANGE
		expectLedger(apotheos, 13, "apotheos_complete", problems);
		PlaythroughLog.note(helper, "ledger Blood Beneath: " + (problems.isEmpty() ? "every movement reads right" : problems)
				+ " (the Apotheos road goes 11 -> 13: there is no twelfth movement on it)");
		helper.assertTrue(problems.isEmpty(), "the Blood Beneath card: " + problems);
		helper.succeed();
	}

	private static void expectLedger(PlaythroughPlayer p, int progress, String card, List<String> problems) {
		LedgerView ledger = p.ledger();
		String key = "screen.hemomancy.harbinger_assignment_ledger.blood_beneath." + card;
		if (ledger.bloodBeneathProgress() != progress || !ledger.bloodBeneathKey().equals(key)) {
			problems.add("expected " + progress + "/13 " + card + ", read " + ledger);
		}
		if (!LangKeys.exists(ledger.bloodBeneathKey())) problems.add(ledger.bloodBeneathKey() + " has no English");
	}

	// =================================================================================================================
	// The Saint mausoleums
	// =================================================================================================================

	/**
	 * Hemorath's assembled complex: a D7 on the ward line is turned back and told why; a D8 Silentium crosses; each of
	 * the three Chapels (Beholding, Litany, Downfall) refuses the D7 and begins for the D8 (the Downfall from its
	 * gate); an Apotheos standing on hallowed ground with no spores burns, with spores pays them instead.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = "playthrough_d8_mausoleum")
	public static void mausoleumWardAndChapels(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughSites.Mausoleum m = PlaythroughSites.mausoleum(helper, EnumSaintType.HEMORATH, 11L);
		PlaythroughPlayer archon = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 2));
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 2));
		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 14, 2));
		PlaythroughStates.archonD7(archon); // ARRANGE
		PlaythroughStates.silentiumD8(silent); // ARRANGE
		PlaythroughStates.apotheosD8(apotheos); // ARRANGE

		// The anchors register with the level on their first tick after the build, as a generated chunk's do.
		PlaythroughClock.start(helper).idle(3).then(() -> {
			List<WardAnchorBlockEntity> anchors = blockEntitiesIn(level, m.box(), WardAnchorBlockEntity.class);
			helper.assertTrue(!anchors.isEmpty() && anchors.get(0).settled() && anchors.get(0).threshold() != null,
					"the threshold hall's ward anchor is settled: " + anchors.size() + " anchors");
			AABB slab = anchors.get(0).threshold();
			Vec3 onLine = new Vec3(slab.getCenter().x, slab.minY, slab.getCenter().z);
			archon.moveTo(onLine);
			Outcome turned = tick(archon, "a D7 steps onto the ward line");
			turned.said(WARD_REFUSED);
			helper.assertTrue(!archon.player().getBoundingBox().intersects(slab), "the D7 is put back behind the line; " + turned);
			silent.moveTo(onLine);
			Outcome crossed = tick(silent, "a D8 steps onto the ward line");
			crossed.didNotSay(WARD_REFUSED);
			helper.assertTrue(silent.player().position().distanceTo(onLine) < 0.01D, "the D8 is not moved; " + crossed);

			Set<ChapelRules.Kind> begun = chapels(helper, m, archon, silent);
			helper.assertTrue(begun.equals(EnumSet.of(ChapelRules.Kind.BEHOLDING, ChapelRules.Kind.LITANY, ChapelRules.Kind.DOWNFALL)),
					"Hemorath's three Chapels begin for a D8: " + begun);

			BlockPos hallowed = m.layout().first("nave").map(piece -> piece.getBoundingBox().getCenter())
					.orElse(BlockPos.containing(onLine));
			apotheos.moveTo(hallowed);
			var spores = HemoCapabilityAccess.getSporeLoad(apotheos.player());
			spores.spend(spores.load()); // ARRANGE: an empty spore load
			apotheos.player().setHealth(apotheos.player().getMaxHealth());
			apotheos.player().invulnerableTime = 0;
			apotheos.player().tickCount = 100; // a whole second on the ground, the burn's notice second
			float full = apotheos.player().getHealth();
			Outcome burned = tick(apotheos, "an Apotheos on hallowed ground with no spores");
			burned.said(HALLOWED_BURN);
			helper.assertTrue(apotheos.player().getHealth() < full, "the hallowed ground burns an empty Apotheos; " + burned);
			spores.add(30.0F); // ARRANGE: a spore load to pay with
			float health = apotheos.player().getHealth();
			apotheos.player().invulnerableTime = 0;
			apotheos.player().tickCount = 120;
			tick(apotheos, "an Apotheos on hallowed ground with spores");
			helper.assertTrue(apotheos.player().getHealth() >= health && spores.load() < 30.0F,
					"spores pay the burn first: health " + health + " -> " + apotheos.player().getHealth() + ", spores " + spores.load());
		}).run();
	}

	/** Velorum's complex: its Chapels (Beholding, Litany, Repose) refuse a D7 and begin for a D8. */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = "playthrough_d8_mausoleum_velorum")
	public static void mausoleumChapelsVelorum(GameTestHelper helper) {
		PlaythroughSites.Mausoleum m = PlaythroughSites.mausoleum(helper, EnumSaintType.VELORUM, 11L);
		PlaythroughPlayer archon = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 2));
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 2));
		PlaythroughStates.archonD7(archon); // ARRANGE
		PlaythroughStates.silentiumD8(silent); // ARRANGE
		Set<ChapelRules.Kind> begun = chapels(helper, m, archon, silent);
		helper.assertTrue(begun.equals(EnumSet.of(ChapelRules.Kind.BEHOLDING, ChapelRules.Kind.LITANY, ChapelRules.Kind.REPOSE)),
				"Velorum's three Chapels begin for a D8: " + begun);
		helper.succeed();
	}

	/**
	 * The ring door by held projection: an empty hand is told it answers blood; a D7 is refused; a Silentium with two
	 * of three Chapels done is told the node count; an Apotheos forces it, paying exactly 1000 mL through
	 * {@code BloodAccounting}, while a Silentium projecting into the forcing is told the door is contested; the tomb
	 * records the forced door.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = "playthrough_d8_mausoleum_door")
	public static void ringDoorProperAndForced(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughSites.Mausoleum m = PlaythroughSites.mausoleum(helper, EnumSaintType.HEMORATH, 29L);
		PlaythroughPlayer archon = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 2));
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 2));
		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 14, 2));
		PlaythroughStates.archonD7(archon); // ARRANGE
		PlaythroughStates.silentiumD8(silent); // ARRANGE
		PlaythroughStates.apotheosD8(apotheos); // ARRANGE
		for (PlaythroughPlayer p : List.of(archon, silent, apotheos)) {
			tools(p);
			fillBlood(p);
		}
		RingDoorBlockEntity door = ringDoor(helper, m);
		BlockPos stand = standBefore(level, door.getBlockPos());
		Vec3 aim = Vec3.atCenterOf(door.getBlockPos());
		List<ChapelFocusBlockEntity> chapels = RingDoorBlockEntity.chapelsNear(level, door.getBlockPos());
		helper.assertTrue(chapels.size() >= 3, "the door finds its three Chapels: " + chapels.size());
		int[] mark = new int[3];
		double[] before = new double[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					for (PlaythroughPlayer p : List.of(archon, silent, apotheos)) p.moveTo(stand);
					PlaythroughRites.emptyHand(silent);
					silent.useOn(door.getBlockPos()).said(RING_TOUCH);
					mark[0] = archon.mark();
				})
				.step(() -> archon.hold(ItemInit.blood_projection.get(), 6, aim))
				.then(() -> {
					helper.assertTrue(saidSince(archon, mark[0], RING_REFUSED), "a D7 is refused: " + archon.chatSince(mark[0]));
					completeChapel(helper, chapels.get(0)); // ARRANGE: two of the three Chapels seen through
					completeChapel(helper, chapels.get(1)); // ARRANGE
					mark[1] = silent.mark();
				})
				.step(() -> silent.hold(ItemInit.blood_projection.get(), 6, aim))
				.then(() -> {
					helper.assertTrue(saidSince(silent, mark[1], RING_NODES), "a Silentium with two nodes is told the count: "
							+ silent.chatSince(mark[1]));
					helper.assertTrue(door.phase() == RingDoorBlockEntity.Phase.IDLE, "the door does not start for two nodes");
					mark[2] = silent.mark();
					before[0] = apotheos.blood();
					apotheos.startHold(ItemInit.blood_projection.get(), aim);
				})
				.step(new PlaythroughClock.Step() {
					@Override
					public boolean tick(int ticks) {
						RingDoorBlockEntity.Phase phase = door.phase();
						boolean done = ticks > 0 && phase != RingDoorBlockEntity.Phase.IDLE && phase != RingDoorBlockEntity.Phase.OPENING;
						if (done || ticks > 400 || !apotheos.player().isUsingItem()) {
							apotheos.releaseHold();
							if (silent.player().isUsingItem()) silent.releaseHold();
							return true;
						}
						apotheos.aim(aim);
						apotheos.holdTick();
						if (ticks == 40) silent.startHold(ItemInit.blood_projection.get(), aim);
						if (ticks > 40 && ticks <= 46) {
							silent.aim(aim);
							silent.holdTick();
						}
						if (ticks == 47) silent.releaseHold();
						return false;
					}

					@Override
					public String label() {
						return "an Apotheos forces the ring door while a Silentium projects into it";
					}
				})
				.then(() -> {
					double spent = before[0] - apotheos.blood();
					PlaythroughLog.note(helper, "forced ring door: phase " + door.phase() + " path " + doorWay(door) + "; the Apotheos "
							+ "spent " + spent + " mL; the Silentium heard " + silent.chatSince(mark[2]));
					helper.assertTrue(door.phase() != RingDoorBlockEntity.Phase.IDLE && door.phase() != RingDoorBlockEntity.Phase.OPENING
							&& doorWay(door) == SaintSarcophagusBlockEntity.DoorMode.FORCED, "the Apotheos forces the door: " + door.phase());
					helper.assertTrue(spent >= 999.0D && spent <= 1001.0D, "forcing the door costs 1000 mL, paid " + spent);
					helper.assertTrue(saidSince(silent, mark[2], RING_CONTESTED), "the Silentium is told the door is contested: "
							+ silent.chatSince(mark[2]));
					List<SaintSarcophagusBlockEntity> tombs = blockEntitiesIn(level, m.box(), SaintSarcophagusBlockEntity.class);
					PlaythroughLog.note(helper, "the tomb's door record: " + tombs.stream().map(tomb -> tomb.doorMode() + " skipped "
							+ tomb.skippedChapels()).toList());
					helper.assertTrue(!tombs.isEmpty() && tombs.get(0).doorMode() == SaintSarcophagusBlockEntity.DoorMode.FORCED
							&& tombs.get(0).skippedChapels().size() == 1, "the tomb records a forced door and the skipped Chapel");
				})
				.run();
	}

	/** A Silentium with all three Chapels seen through opens the ring door properly for 1000 mL; the tomb records it. */
	@GameTest(template = EMPTY, timeoutTicks = 600, batch = "playthrough_d8_mausoleum_door_proper")
	public static void ringDoorOpenedProperly(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughSites.Mausoleum m = PlaythroughSites.mausoleum(helper, EnumSaintType.HEMORATH, 42L);
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 2));
		PlaythroughStates.silentiumD8(silent); // ARRANGE
		tools(silent);
		fillBlood(silent);
		RingDoorBlockEntity door = ringDoor(helper, m);
		Vec3 aim = Vec3.atCenterOf(door.getBlockPos());
		for (ChapelFocusBlockEntity chapel : RingDoorBlockEntity.chapelsNear(level, door.getBlockPos())) {
			completeChapel(helper, chapel); // ARRANGE: every Chapel seen through
		}
		double[] before = new double[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					silent.moveTo(standBefore(level, door.getBlockPos()));
					before[0] = silent.blood();
				})
				.step(() -> silent.holdUntil(ItemInit.blood_projection.get(), 400, () -> aim,
						() -> door.phase() != RingDoorBlockEntity.Phase.IDLE && door.phase() != RingDoorBlockEntity.Phase.OPENING))
				.then(() -> {
					double spent = before[0] - silent.blood();
					PlaythroughLog.note(helper, "proper ring door: phase " + door.phase() + " path " + doorWay(door) + " nodes "
							+ door.nodes() + "; spent " + spent + "; " + silent.last());
					helper.assertTrue(doorWay(door) == SaintSarcophagusBlockEntity.DoorMode.PROPER
							&& door.phase() != RingDoorBlockEntity.Phase.OPENING, "the door opens properly: " + door.phase());
					helper.assertTrue(spent >= 999.0D && spent <= 1001.0D, "opening it costs 1000 mL, paid " + spent);
					List<SaintSarcophagusBlockEntity> tombs = blockEntitiesIn(level, m.box(), SaintSarcophagusBlockEntity.class);
					helper.assertTrue(!tombs.isEmpty() && tombs.get(0).doorMode() == SaintSarcophagusBlockEntity.DoorMode.PROPER,
							"the tomb records a proper door");
				})
				.run();
	}

	/**
	 * Velorum's tomb after an Apotheos forced the door: three 100 mL offerings consecrate it, Foul Paste wakes the
	 * Saint enraged (the forced record), a {@code /kill} sends it back to its tomb unrewarded, and when it is woken
	 * again and felled its reward rolls once.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1500, batch = "playthrough_d8_mausoleum_tomb")
	public static void saintTombWake(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughSites.Mausoleum m = PlaythroughSites.mausoleum(helper, EnumSaintType.VELORUM, 11L);
		AABB complex = AABB.of(m.box()).inflate(8.0D);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 2));
		PlaythroughStates.apotheosD8(p); // ARRANGE
		tools(p);
		fillBlood(p);
		RingDoorBlockEntity door = ringDoor(helper, m);
		Vec3 aim = Vec3.atCenterOf(door.getBlockPos());
		List<SaintSarcophagusBlockEntity> tombs = blockEntitiesIn(level, m.box(), SaintSarcophagusBlockEntity.class);
		helper.assertTrue(!tombs.isEmpty(), "the complex has its tomb");
		BlockPos tomb = tombs.get(0).getBlockPos();
		Direction bowl = level.getBlockState(tomb).getValue(SaintSarcophagusBlock.FACING);
		SaintTrialEntity[] saint = new SaintTrialEntity[1];
		int[] rewards = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> p.moveTo(standBefore(level, door.getBlockPos())))
				.step(() -> p.holdUntil(ItemInit.blood_projection.get(), 400, () -> aim,
						() -> door.phase() != RingDoorBlockEntity.Phase.IDLE && door.phase() != RingDoorBlockEntity.Phase.OPENING))
				.then(() -> {
					helper.assertTrue(doorWay(door) == SaintSarcophagusBlockEntity.DoorMode.FORCED, "the Apotheos forced the door");
					p.moveTo(tomb.relative(bowl, 2));
					fillBlood(p);
					double before = p.blood();
					for (int offering = 0; offering < SaintSarcophagusBlockEntity.OFFERING_THRESHOLD; offering++) {
						PlaythroughRites.emptyHand(p);
						Outcome given = p.useOn(tomb, bowl);
						PlaythroughLog.note(helper, "tomb offering " + (offering + 1) + ": " + given.describe());
					}
					helper.assertTrue(level.getBlockEntity(tomb) instanceof SaintSarcophagusBlockEntity sarcophagus
							&& sarcophagus.isConsecrated(), "three offerings consecrate the tomb");
					helper.assertTrue(Math.abs(before - p.blood() - 300.0D) < 0.01D, "the three offerings cost 300 mL, paid "
							+ (before - p.blood()));
					saint[0] = wake(helper, p, tomb, complex);
					PlaythroughLog.note(helper, "the Saint woke as " + saint[0].awakening());
					helper.assertTrue("FORCED".equals(saint[0].awakening()), "after a forced door the Saint wakes enraged, not "
							+ saint[0].awakening());
					saint[0].kill(); // as /kill does
				})
				.until(100, () -> saint[0].isRemoved(), "the killed Saint leaves the world")
				.then(() -> {
					helper.assertTrue(level.getBlockState(tomb).is(BlockInit.saint_sarcophagus.get()),
							"a /kill sends the Saint back into its tomb");
					helper.assertTrue(residuum(level, complex) == 0, "a /kill rolls no reward");
					saint[0] = wake(helper, p, tomb, complex);
				})
				.step(new PlaythroughClock.Step() {
					@Override
					public boolean tick(int ticks) {
						SaintTrialEntity body = saint[0];
						if (body.hasTrialEnded() || body.isRemoved()) return true;
						if (ticks > 600) helper.fail("the woken Saint could not be felled in 600 ticks: " + body.getHealth());
						// ARRANGE: the fight already fought to its last blow; the blow itself is the player's
						if (!body.isInvulnerableTo(p.player().damageSources().playerAttack(p.player()))) {
							body.hurt(p.player().damageSources().playerAttack(p.player()), body.getMaxHealth() * 10.0F);
						}
						return false;
					}

					@Override
					public String label() {
						return "the woken Saint felled";
					}
				})
				.then(() -> rewards[0] = residuum(level, complex))
				.idle(60)
				.then(() -> {
					int later = residuum(level, complex);
					PlaythroughLog.note(helper, "the felled Saint's reward: " + rewards[0] + " residuum (fragments when forced); "
							+ later + " a little later");
					helper.assertTrue(rewards[0] >= 1 && later == rewards[0], "the reward rolls once: " + rewards[0] + " then " + later);
				})
				.run();
	}

	/**
	 * A Recollection cyst on fungal network ground in the test level: a D7 Archon and a Silentium who is not
	 * possessing walk through it and get nothing; an Apotheos draws the Recollection out and the cyst withers; an
	 * Apotheos with a full pack has it dropped at their feet.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH)
	public static void recollectionCyst(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Item recollection = ItemInit.recollection_hemorath.get();
		BlockPos cyst = cyst(helper, PlaythroughSites.stand(helper, 12, 12));
		PlaythroughPlayer archon = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 2));
		PlaythroughPlayer silentium = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 7, 2));
		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 2));
		PlaythroughStates.archonD7(archon); // ARRANGE
		PlaythroughStates.silentiumD8(silentium); // ARRANGE
		PlaythroughStates.apotheosD8(apotheos); // ARRANGE
		walkInto(archon, cyst).gaveNothing();
		walkInto(silentium, cyst).gaveNothing();
		helper.assertTrue(level.getBlockState(cyst).is(BlockInit.recollection_cyst.get()), "the cyst keeps its Recollection");
		Outcome taken = walkInto(apotheos, cyst);
		taken.gave(recollection, 1).said(RECOLLECTION_TAKEN);
		helper.assertTrue(level.getBlockState(cyst).isAir(), "the cyst withers once drawn");

		BlockPos second = cyst(helper, PlaythroughSites.stand(helper, 20, 12));
		PlaythroughPlayer packed = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 2));
		PlaythroughStates.apotheosD8(packed); // ARRANGE
		packed.fillInventory(); // ARRANGE
		Outcome dropped = walkInto(packed, second);
		helper.assertTrue(dropped.droppedNear(second, recollection) == 1 || dropsNear(packed, second, recollection) == 1,
				"a full pack has the Recollection dropped at the player's feet; " + dropped);
		helper.succeed();
	}

	/** A Creative player with a full pack draws a cyst: the Recollection is discarded and the cyst is gone. */
	// DECISION D-16 (late instance): a Creative full pack swallows a one-time reward (here the cyst withers and the
	// Recollection is gone). Options: treat a Creative discard as undelivered, as PendingDelivery does / keep (Creative only).
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void recollectionCystCreativeFullPack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		Item recollection = ItemInit.recollection_hemorath.get();
		BlockPos cyst = cyst(helper, PlaythroughSites.stand(helper, 12, 12));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.apotheosD8(p); // ARRANGE
		p.creative().fillInventory(); // ARRANGE
		Outcome drawn = walkInto(p, cyst);
		boolean kept = p.count(recollection) > 0 || dropsNear(p, cyst, recollection) > 0
				|| level.getBlockState(cyst).is(BlockInit.recollection_cyst.get());
		PlaythroughLog.note(helper, "D-16: a Creative full pack at a cyst: " + drawn.describe() + "; cyst "
				+ level.getBlockState(cyst).getBlock() + "; Recollection kept=" + kept);
		helper.assertTrue(kept, "DECISION D-16: the Recollection is not lost to a Creative full pack");
		helper.succeed();
	}

	// =================================================================================================================
	// Helpers
	// =================================================================================================================

	/** The Rite of Apotheos played on its grand floor at {@code origin}, as steps on {@code clock}. Returns the floor. */
	static PlaythroughSites.RiteFloor apotheosRite(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock, BlockPos origin) {
		return apotheosRite(helper, p, clock, origin, new int[1]);
	}

	/**
	 * As {@link #apotheosRite(GameTestHelper, PlaythroughPlayer, PlaythroughClock, BlockPos)}; an ORDEAL deadlock that
	 * outlasts the floor's sleeping support sigil is rescued (ARRANGE, DEGRADED, decision CHAIN-1) when {@code rescues}
	 * is not null, each rescue counted in it.
	 */
	static PlaythroughSites.RiteFloor apotheosRite(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock,
			BlockPos origin, @Nullable int[] rescues) {
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, origin, "apotheos_rite");
		tools(p);
		armed(helper, p);
		fillBlood(p);
		PlaythroughRites.prepare(clock, p, floor);
		int[] refills = new int[1];
		clock.then(() -> {
					fillBlood(p);
					p.wield(ItemInit.living_staff.get());
				})
				.step(() -> PlaythroughClock.rite(p, floor.focus(),
						refilling(RitePlan.standard().fightThreats().releaseOnStill()
								.extraAim(PlaythroughRites.stabilizeWhenDeadlocked(p, PlaythroughArchonGameTests.sigilsAndTraces(p), rescues))
								.traceFirst().weapon(Items.NETHERITE_SWORD), p, refills)))
				.then(() -> PlaythroughLog.note(helper, "the Rite of Apotheos ended at degree " + p.degree() + " path " + path(p)
						+ "; the pool was refilled " + refills[0] + " times (ARRANGE)"
						+ (rescues == null ? "" : "; CHAIN-1 rescues " + rescues[0] + (rescues[0] > 0 ? " (DEGRADED)" : ""))
						+ "; " + p.last()));
		return floor;
	}

	/** Each phase tops the player's pool back up when it runs low (ARRANGE: a rested pool between anchors). */
	private static RitePlan refilling(RitePlan plan, PlaythroughPlayer p, int[] refills) {
		RitePlan out = plan;
		for (CardinalRitePhase phase : CardinalRitePhase.values()) {
			out = out.onPhase(phase, (rite, ticks) -> {
				if (ticks % 600 == 300) {
					var recipe = com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe.getRiteByLocation(p.player().level(),
							rite.getRecipeId());
					List<String> hungry = new ArrayList<>();
					if (recipe != null && recipe.getCeremony() != null) {
						for (int i = 0; i < recipe.getCeremony().anchors().size(); i++) {
							int need = rite.bloodNeededForAnchor(i);
							int repair = rite.instabilityRepairBloodNeeded(i);
							if (need > 0 || repair > 0) hungry.add(i + "@" + recipe.getCeremony().anchors().get(i).offset().toShortString()
									+ " " + need + "/" + repair);
						}
					}
					PlaythroughLog.note(p.helper(), "rite " + rite.getPhase() + " +" + ticks + " wave " + rite.getCurrentWave()
							+ ": blood " + p.blood() + " at " + p.player().position() + " using " + p.player().isUsingItem() + " "
							+ p.player().getUseItem().getItem() + " instability " + rite.getInstability() + " hungry " + hungry);
				}
				var volume = HemoCapabilityAccess.getBloodVolume(p.player());
				if (volume.getBloodVolume() < Math.min(1500.0D, volume.getMaxBloodVolume() * 0.3D)) {
					fillBlood(p);
					refills[0]++;
				}
			});
		}
		return out;
	}

	/** The Spine used while the revelation choice is pending opens the core-witness whisper; the player chooses. */
	private static void chooseByTheSpine(GameTestHelper helper, PlaythroughPlayer p, String choice) {
		p.wield(ItemInit.fungal_spine.get());
		Outcome used = p.use(InteractionHand.MAIN_HAND);
		Optional<DialogueView> whisper = p.whisper();
		if (whisper.isEmpty() && used.saidKeys().contains(WHISPER_PENDING)) {
			// The Spine queues the whisper; the player takes up the chat line's "Listen when ready".
			Outcome listened = p.openWhisper();
			PlaythroughLog.note(helper, "the Spine queued the core-witness whisper; listened: " + listened.describe());
			whisper = p.whisper();
		} else if (whisper.isEmpty() && used.saidKeys().isEmpty() && !p.pendingWhispers().isEmpty()) {
			// Straight after the forced return the Spine is still cooling down and says nothing; the whisper the return
			// queued waits in the inbox, and the player opens it from there ("Listen when ready").
			PlaythroughLog.note(helper, "the Spine said nothing (cooldown " + p.player().getCooldowns().isOnCooldown(
					ItemInit.fungal_spine.get()) + "); opening the queued whisper from the inbox");
			// The inbox opens oldest first; the player listens to each and closes it until the choice comes up.
			for (int opened = 0; opened < 8 && !p.pendingWhispers().isEmpty(); opened++) {
				Outcome listened = p.openWhisper();
				whisper = p.whisper();
				PlaythroughLog.note(helper, "listened to inbox whisper " + (opened + 1) + ": " + listened.describe() + " showing "
						+ whisper.map(DialogueView::describe).orElse("nothing"));
				if (whisper.isPresent() && whisper.get().offers(choice)) break;
				Optional<UUID> id = p.openWhisperId();
				if (id.isEmpty()) break;
				p.closeWhisper(id.get());
			}
		}
		helper.assertTrue(whisper.isPresent(), "the Spine opens the core-witness whisper while the choice is pending; " + used);
		whisper.get().assertLangKeys(helper, "the core-witness whisper");
		Outcome chosen = p.chooseWhisper(choice);
		PlaythroughLog.note(helper, "the revelation choice " + choice + ": " + chosen.describe());
	}

	/**
	 * Plants the held Living Staff on {@code floor}'s Focus from the rite stand and expects no rite, the Staff kept,
	 * and {@code key} said (any line when {@code key} is null; what was said is logged).
	 */
	private static void refusedAtFocus(GameTestHelper helper, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor,
			List<String> anyOf, String who) {
		p.moveTo(PlaythroughRites.riteStand(floor));
		p.wield(ItemInit.living_staff.get());
		Outcome planted = p.useOn(floor.focus());
		PlaythroughLog.note(helper, who + " plants the Staff on " + floor.recipe().getId() + ": " + planted.describe());
		helper.assertTrue(CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) == null,
				who + ": no rite starts; " + planted);
		helper.assertTrue(anyOf.stream().anyMatch(planted.saidKeys()::contains), who + " is told one of " + anyOf + "; " + planted);
		helper.assertTrue(p.count(ItemInit.living_staff.get()) == 1, who + " keeps the Staff");
	}

	private static void refusedAtFocus(GameTestHelper helper, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor,
			@Nullable String key, String who) {
		p.moveTo(PlaythroughRites.riteStand(floor));
		p.wield(ItemInit.living_staff.get());
		Outcome planted = p.useOn(floor.focus());
		PlaythroughLog.note(helper, who + " plants the Staff on " + floor.recipe().getId() + ": " + planted.describe());
		helper.assertTrue(CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) == null,
				who + ": no rite starts; " + planted);
		if (key != null) planted.said(key);
		else helper.assertTrue(!planted.chat().isEmpty(), who + " is told something; " + planted);
		helper.assertTrue(p.count(ItemInit.living_staff.get()) == 1, who + " keeps the Staff");
	}

	/** One Living Arsenal blade as the Staff conjures it (the form recorded on the stack). */
	private static ItemStack livingBlade() {
		ItemStack blade = new ItemStack(ItemInit.living_blade.get());
		CompoundTag tag = new CompoundTag();
		tag.putString(LivingStaffWeaponFormHelper.FORM_KEY, LivingStaffWeaponFormRules.CONJURE_BLADE);
		blade.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return blade;
	}

	/** A right-click on the Bloom's root with what is in hand: a cut with the weapon or Sickle. */
	private static Outcome cut(PlaythroughPlayer p, BlockPos root) {
		return p.useOn(root);
	}

	private static QliphothBloomSavedData blooms(PlaythroughPlayer p) {
		return QliphothBloomSavedData.get(p.player().server.overworld());
	}

	private static QliphothBloomSavedData.BloomEntry bloomAt(GameTestHelper helper, PlaythroughPlayer p, BlockPos root) {
		QliphothBloomSavedData.BloomEntry bloom = blooms(p).getBloomAt(root, p.player().level().dimension().location().toString());
		helper.assertTrue(bloom != null && bloom.ownerUUID().equals(p.uuid()), "the player owns the Bloom at " + root.toShortString());
		return bloom;
	}

	private static EnumArchonPath path(PlaythroughPlayer p) {
		return HemoCapabilityAccess.getInitiatoryDegree(p.player()).getArchonPath();
	}

	/** ARRANGE: the player's endgame path set directly (for refusals of the path at the moment of the action). */
	private static void setPath(PlaythroughPlayer p, EnumArchonPath path) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(p.player());
		degree.setArchonPath(path);
		InitiatoryDegreeEvents.syncDegree(p.player(), degree);
	}

	/** ARRANGE: the Living Staff bond, earned when the Staff's Blood Structure was first crafted. */
	private static void bond(PlaythroughPlayer p) {
		var progress = HemoCapabilityAccess.getLivingStaffProgress(p.player());
		if (!progress.hasLivingStaffBond()) progress.unlockLivingStaffBond();
		LivingStaffBondHelper.syncProgress(p.player());
	}

	/** ARRANGE: the Living Staff and the two utilities a late Harbinger carries. */
	private static void tools(PlaythroughPlayer p) {
		for (Item tool : List.of(ItemInit.blood_projection.get(), ItemInit.blood_absorption.get(), ItemInit.living_staff.get())) {
			if (p.count(tool) == 0) p.give(new ItemStack(tool));
		}
	}

	/**
	 * ARRANGE: a netherite sword in the pack; the rite's puppeteers are fought with it (the harness's attack applies the
	 * held sword's damage).
	 */
	private static void armed(GameTestHelper helper, PlaythroughPlayer p) {
		if (p.count(Items.NETHERITE_SWORD) == 0) p.give(new ItemStack(Items.NETHERITE_SWORD));
	}

	/** ARRANGE: a full, rested pool. */
	private static void fillBlood(PlaythroughPlayer p) {
		PlaythroughStates.blood(p, HemoCapabilityAccess.getBloodVolume(p.player()).getMaxBloodVolume());
	}

	private static String adv(ResourceLocation advancement) {
		return advancement.getPath();
	}

	private static boolean saidSince(PlaythroughPlayer p, int mark, String key) {
		return p.chatSince(mark).stream().anyMatch(line -> line.has(key));
	}

	/** One player tick (the tick events the level would post), measured as an outcome. */
	private static Outcome tick(PlaythroughPlayer p, String label) {
		Outcome.Probe probe = Outcome.begin(p, null, label);
		p.tickOnce();
		return probe.finish();
	}

	/** The Burn Spores key, as the client sends it. */
	private static Outcome burnSpores(PlaythroughPlayer p) {
		Outcome.Probe probe = Outcome.begin(p, null, "burn spores");
		BurnSporeLoadKeyPacket.handle(new BurnSporeLoadKeyPacket(), PayloadContexts.of(p.player()));
		return probe.finish();
	}

	private static boolean carries(PlaythroughPlayer p, java.util.function.Predicate<ItemStack> what) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) if (what.test(inventory.getItem(slot))) return true;
		return what.test(p.player().containerMenu.getCarried());
	}

	/** ARRANGE: every {@code item} the player carries is gone (lost, burnt, given away). */
	private static void removeAll(PlaythroughPlayer p, Item item) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(item)) inventory.setItem(slot, ItemStack.EMPTY);
		}
	}

	private static int dropsNear(PlaythroughPlayer p, BlockPos pos, Item item) {
		return p.player().serverLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3.0D),
				drop -> drop.getItem().is(item)).stream().mapToInt(drop -> drop.getItem().getCount()).sum();
	}

	/** An ARRANGE mob that stays where it is put, removed when the test ends. */
	private static <T extends Entity> T spawn(GameTestHelper helper, EntityType<T> type, BlockPos pos) {
		T entity = type.create(helper.getLevel());
		helper.assertTrue(entity != null, "a " + type.getDescriptionId() + " could be created");
		entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
		if (entity instanceof Mob mob) {
			mob.setNoAi(true);
			mob.setPersistenceRequired();
		}
		helper.assertTrue(helper.getLevel().addFreshEntity(entity), "the " + type.getDescriptionId() + " joined the level");
		PlaythroughCleanup.onEnd(helper, entity::discard);
		return entity;
	}

	/**
	 * The last blows on an owned Mycophant: ARRANGE holds her at one health (the fight before them is not under
	 * test), and the player's real melee attack, swung each tick, lands the killing blow.
	 */
	private static PlaythroughClock.Step lastBlows(GameTestHelper helper, PlaythroughPlayer p, Supplier<MycophantEntity> boss) {
		return new PlaythroughClock.Step() {
			@Override
			public boolean tick(int ticks) {
				MycophantEntity body = boss.get();
				if (body.isDeadOrDying() || body.isRemoved()) return true;
				if (ticks > 300) helper.fail("the Mycophant still stands after 300 ticks of last blows: " + body.getHealth());
				if (body.getHealth() > 1.0F) body.setHealth(1.0F); // ARRANGE: the fight fought to her last blow
				p.approach(body);
				p.attack(body);
				return false;
			}

			@Override
			public String label() {
				return "the last blows on the Mycophant";
			}
		};
	}

	/** The teachers, by dialogue id, standing in a row inside dialogue reach of the players' approach. */
	private static Map<String, Entity> teachers(GameTestHelper helper) {
		Map<String, Entity> teachers = new LinkedHashMap<>();
		for (int i = 0; i < TEACHERS.length; i++) {
			teachers.put(TEACHERS[i], PlaythroughNpcs.spawn(helper, TEACHER_ROLES[i], PlaythroughSites.stand(helper, 5 + i * 6, 14)));
		}
		return teachers;
	}

	/** One player at each of the six late states the ending lines answer, each standing apart. */
	private static Map<String, PlaythroughPlayer> endingStates(GameTestHelper helper) {
		Map<String, PlaythroughPlayer> states = new LinkedHashMap<>();
		states.put("undecided", stateAt(helper, 2, PlaythroughStates::choicePendingD7));
		states.put("silent_pending", stateAt(helper, 7, PlaythroughStates::silentPendingD7));
		states.put("silent_absorb", stateAt(helper, 12, PlaythroughStates::vesperBeatenD7));
		states.put("silent_archon", stateAt(helper, 17, PlaythroughStates::silentiumD8));
		states.put("apotheos_pending", stateAt(helper, 22, PlaythroughStates::apotheosPendingD7));
		states.put("apotheos", stateAt(helper, 27, PlaythroughStates::apotheosD8));
		return states;
	}

	private static PlaythroughPlayer stateAt(GameTestHelper helper, int x, Consumer<PlaythroughPlayer> state) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, x, 2));
		state.accept(p); // ARRANGE
		return p;
	}

	private static List<String> endingKeys(DialogueView view) {
		return view.lineKeys().stream().filter(key -> key.startsWith(ENDING_PREFIX)).toList();
	}

	/** A greeting tree as a player sees it: each reachable node and its lines, the ending line left out. */
	private static Set<String> signature(PlaythroughPlayer p, Entity teacher) {
		p.approach(teacher);
		DialogueView view = p.talkTo(teacher);
		Set<String> signature = new TreeSet<>();
		view.nodes().forEach((id, node) -> {
			signature.add("node " + id);
			for (String line : node.lines()) {
				String key = Dialogue.lineKey(line).orElse(line);
				if (!key.startsWith(ENDING_PREFIX)) signature.add(id + ": " + key);
			}
		});
		return signature;
	}

	/** Walks into {@code pos}: the player's box now overlaps the cell, as {@code Entity.checkInsideBlocks} sees it. */
	private static Outcome walkInto(PlaythroughPlayer p, BlockPos pos) {
		p.moveTo(pos);
		ServerLevel level = p.player().serverLevel();
		BlockState state = level.getBlockState(pos);
		Outcome.Probe probe = Outcome.begin(p, null, "walk into " + state.getBlock() + " " + pos.toShortString());
		state.entityInside(level, pos, p.player());
		return probe.finish();
	}

	/** ARRANGE: a Hemorath Recollection cyst on erythrocytic dirt (fungal network ground) at {@code pos}. */
	private static BlockPos cyst(GameTestHelper helper, BlockPos pos) {
		PlaythroughRites.set(helper, pos.below(), BlockInit.erythrocytic_dirt.get());
		PlaythroughRites.set(helper, pos, BlockInit.recollection_cyst.get());
		helper.assertTrue(helper.getLevel().getBlockState(pos).is(BlockInit.recollection_cyst.get()), "the cyst stands");
		return pos.immutable();
	}

	/** Every block entity of {@code type} inside {@code box}. */
	private static <T extends BlockEntity> List<T> blockEntitiesIn(ServerLevel level, BoundingBox box, Class<T> type) {
		List<T> found = new ArrayList<>();
		ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()), SectionPos.blockToSectionCoord(box.minZ()));
		ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()), SectionPos.blockToSectionCoord(box.maxZ()));
		ChunkPos.rangeClosed(min, max).forEach(chunk -> {
			for (BlockEntity entity : List.copyOf(level.getChunk(chunk.x, chunk.z).getBlockEntities().values())) {
				if (type.isInstance(entity) && box.isInside(entity.getBlockPos())) found.add(type.cast(entity));
			}
		});
		return found;
	}

	/** The complex's ring door controller. */
	private static RingDoorBlockEntity ringDoor(GameTestHelper helper, PlaythroughSites.Mausoleum m) {
		List<RingDoorBlockEntity> doors = blockEntitiesIn(helper.getLevel(), m.box(), RingDoorBlockEntity.class);
		helper.assertTrue(!doors.isEmpty(), "the " + m.saint() + " complex has its ring door");
		return doors.get(0);
	}

	/** The way the door is being opened, or was opened once it stands open (the door clears its path when it opens). */
	private static SaintSarcophagusBlockEntity.DoorMode doorWay(RingDoorBlockEntity door) {
		return door.path() != SaintSarcophagusBlockEntity.DoorMode.NONE ? door.path() : door.openedBy();
	}

	/** A standing cell in front of the door (either face), on solid ground, within projection reach of its centre. */
	private static BlockPos standBefore(ServerLevel level, BlockPos door) {
		Direction facing = level.getBlockState(door).hasProperty(RingDoorBlock.FACING)
				? level.getBlockState(door).getValue(RingDoorBlock.FACING) : Direction.NORTH;
		for (Direction side : List.of(facing, facing.getOpposite())) {
			for (int distance = 2; distance <= 4; distance++) {
				for (int dy = 0; dy >= -3; dy--) {
					BlockPos cell = door.relative(side, distance).above(dy);
					if (level.getBlockState(cell).isAir() && level.getBlockState(cell.above()).isAir()
							&& !level.getBlockState(cell.below()).isAir()) return cell;
				}
			}
		}
		return door.relative(facing, 3);
	}

	/** ARRANGE: a Chapel seen through (its trial is not under test here), as its own finish leaves it. */
	private static void completeChapel(GameTestHelper helper, ChapelFocusBlockEntity focus) {
		try {
			Method finish = ChapelFocusBlockEntity.class.getDeclaredMethod("finish", Player.class);
			finish.setAccessible(true);
			finish.invoke(focus, (Object) null);
		} catch (ReflectiveOperationException exception) {
			helper.fail("could not complete the Chapel at " + focus.getBlockPos().toShortString() + " (ARRANGE): " + exception);
		}
		helper.assertTrue(focus.complete(), "the Chapel at " + focus.getBlockPos().toShortString() + " is complete");
	}

	/**
	 * Touches every Chapel focus of the complex with an empty hand: the D7 is refused, the D8 begins it (a Downfall
	 * with a gate says to walk it from the gate). Returns the kinds the D8 could begin.
	 */
	private static Set<ChapelRules.Kind> chapels(GameTestHelper helper, PlaythroughSites.Mausoleum m, PlaythroughPlayer below,
			PlaythroughPlayer eighth) {
		List<ChapelFocusBlockEntity> foci = blockEntitiesIn(helper.getLevel(), m.box(), ChapelFocusBlockEntity.class);
		helper.assertTrue(foci.size() == 3, "the " + m.saint() + " complex has three Chapel foci, not " + foci.size());
		Set<ChapelRules.Kind> begun = EnumSet.noneOf(ChapelRules.Kind.class);
		for (ChapelFocusBlockEntity focus : foci) {
			BlockPos pos = focus.getBlockPos();
			String kind = focus.kind().name().toLowerCase(java.util.Locale.ROOT);
			below.moveTo(pos.above());
			PlaythroughRites.emptyHand(below);
			Outcome refused = below.useOn(pos);
			if (focus.kind() == ChapelRules.Kind.DOWNFALL && refused.saidKeys().contains(CHAPEL_GATE)) {
				// Observation: a gated Downfall points anyone at its gate before the degree check (the gate itself
				// then stays silent for a D7). A D7 cannot pass the ward line to stand here in play.
				PlaythroughLog.note(helper, "a D7 at the gated Downfall focus is pointed to the gate, not refused: " + refused.describe());
			} else {
				refused.said(CHAPEL_REFUSED);
			}
			helper.assertTrue(!refused.saidKeys().contains(CHAPEL_BEGIN + kind), "the D7 does not begin the " + kind + " Chapel");
			eighth.moveTo(pos.above());
			PlaythroughRites.emptyHand(eighth);
			Outcome touched = eighth.useOn(pos);
			PlaythroughLog.note(helper, m.saint() + " " + kind + " Chapel at " + pos.toShortString() + ": " + touched.describe());
			if (touched.saidKeys().contains(CHAPEL_BEGIN + kind) || touched.saidKeys().contains(CHAPEL_GATE)) {
				begun.add(focus.kind());
			}
		}
		return begun;
	}

	/** Foul Paste on the tomb: the Saint rises. Returns the risen Saint. */
	private static SaintTrialEntity wake(GameTestHelper helper, PlaythroughPlayer p, BlockPos tomb, AABB complex) {
		p.give(new ItemStack(ItemInit.foul_paste.get())); // ARRANGE: the paste is made elsewhere
		p.wield(ItemInit.foul_paste.get());
		Outcome pasted = p.useOn(tomb);
		List<SaintTrialEntity> risen = helper.getLevel().getEntitiesOfClass(SaintTrialEntity.class, complex, Entity::isAlive);
		helper.assertTrue(!risen.isEmpty(), "Foul Paste wakes the Saint; " + pasted);
		return risen.get(0);
	}

	/** Hallowed Residuum (whole or fragments) lying in the complex. */
	private static int residuum(ServerLevel level, AABB complex) {
		return level.getEntitiesOfClass(ItemEntity.class, complex, drop -> BuiltInRegistries.ITEM.getKey(drop.getItem().getItem())
				.getPath().startsWith("hallowed_residuum")).stream().mapToInt(drop -> drop.getItem().getCount()).sum();
	}
}
