package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialAtlasPath;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialAtlasSpec;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialEntry;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.MaterialsData;
import com.vincenthuto.hemomancy.common.armor.ability.ArmorSetAbilityRegistry;
import com.vincenthuto.hemomancy.common.block.harbinger.crafting.HematicArmatureBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MonolithFragmentDropRules;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberEntryDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.QliphothPomeItem;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerProgressionRules;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ActivateArmorSetAbilityC2SPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncPomeProgress;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRules;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RiteDiscoveryRules;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteWaveRules;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteInteractionHandler;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteTargetGeometry;
import com.vincenthuto.hemomancy.common.rite.harbinger.HarbingerCardinalRiteEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomEvents;
import com.vincenthuto.hemomancy.common.rite.harbinger.QliphothBloomSavedData;
import com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilPlacementRules;
import com.vincenthuto.hemomancy.common.rite.sigil.CardinalRiteSigilRules;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.HematicArmatureBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.SanguineMonolithBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.locale.Language;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * The D7 Archon band, played from a fresh Living Covenant (D6) onward: the Rite of the Hematic Order with a
 * bloodline helper (and without one, or with one who leaves or dies), the D7 Vicar, the Sanguine Monolith as a block
 * speaker at every degree, its cornerstone and its shatter, the Weight of the Frame, the Bloom of the Qliphoth, the
 * nine pomes and the Fungal Spine, the first projection (degraded here: the Fungal Gardens are not loaded) and the
 * revelation choice from the whisper inbox. Decision probes carry a {@code // DECISION D7-n:} line; the LF-5 fix test a
 * {@code // FIX: LF-5} line.
 * <p>
 * Every Bloom is level-global (one per 3-chunk radius, its aura reaching any player in range), so each test that
 * places one runs in a batch of its own.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughArchonGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_d7_archon";
	private static final String RITES = "playthrough_d7_archon_rites";
	private static final String CLOCK = "playthrough_d7_archon_clock";
	private static final String BLOOM_RITE = "playthrough_d7_bloom_rite";
	private static final String BLOOM_PICKING = "playthrough_d7_bloom_picking";
	private static final String BLOOM_NINE = "playthrough_d7_bloom_nine";
	private static final String BLOOM_RECLAIM = "playthrough_d7_bloom_reclaim";
	private static final String BLOOM_CHAIN = "playthrough_d7_bloom_chain";

	/** Ticks until a dropped Qliphoth Seed's own item entity is in the level (it is added on the next task run). */
	private static final int SEED_ENTITY_TICKS = 5;

	/** A grand rite's timeout plus the time to ready its floor. */
	private static final int GRAND_TEST_TICKS = PlaythroughRites.GRAND_TICKS + 2000;

	/** The rite ground: relative y 3 puts the Focus in the template's floor layer (y 4), mid-template. */
	private static final BlockPos RITE_ORIGIN = new BlockPos(16, 3, 12);

	// --- Keys the player is shown -------------------------------------------------------------------------------------
	private static final String RANK_SEALED = "message.hemomancy.cardinal_rite_activation.the_rank_rite_remains_sealed_complete";
	private static final String LIVING_COVENANT = "milestone.hemomancy.chapter.living_covenant";
	private static final String HELPERS_AT_SEAL = "message.hemomancy.cardinal_rite_interaction_handler.required_bloodline_helpers_must_take_their";
	private static final String HELPERS_AT_FINALE = "message.hemomancy.cardinal_rite_completion_effects.required_bloodline_helpers_must_still_hold";
	private static final String RITE_COLLAPSES = "message.hemomancy.harbinger_cardinal_rite_events.the_cardinal_boundary_collapses_into_backlash";
	private static final String ASSIGNED_AS = "message.hemomancy.cardinal_rite_ally_service.assigned_as";
	private static final String TAKE_THE_STATION = "message.hemomancy.cardinal_rite_ally_service.you_take_the_station";

	private static final String MONOLITH_UNWORTHY = "hemomancy.monolith.unworthy";
	private static final String MONOLITH_KINSHIP = "hemomancy.monolith.what_are_you.archon";
	private static final String MONOLITH_PRESS_AGAIN = "hemomancy.monolith.archon.press_again";
	private static final String MONOLITH_ITEM_UNKNOWN = "hemomancy.monolith.item_inquiry.unknown";
	private static final String CORNERSTONE = "hemomancy.dialogue.event.monolith_cornerstone";
	private static final String MACHINE_LOCKED = "hemomancy.machine_access.locked";

	private static final String VICAR_ARCHON_LINE = "hemomancy.vicar.archon.line1";
	private static final String VICAR_FINAL_TRUTH = "hemomancy.vicar.archon.final_truth";
	private static final String VICAR_POME_LINE = "hemomancy.vicar.archon.pome_empowered.line1";
	private static final String VICAR_MAP_EVENT = "give_stained_church_map";
	private static final String VICAR_MAP_SAID = "hemomancy.dialogue.event.gives_stained_church_map";

	private static final String POME_STRANGER = "message.hemomancy.qliphoth_bloom_block.the_fruit_tightens_against_another_covenant";
	private static final String POME_PICKED = "message.hemomancy.qliphoth_bloom_block.the_qliphoth_pome_comes_away_warm";
	private static final String POME_ALREADY = "message.hemomancy.qliphoth_bloom_block.the_tree_has_already_given_you";
	private static final String POME_WAITS = "message.hemomancy.qliphoth_bloom_block.the_ripe_pome_waits_make_room";
	private static final String POME_GROWING = "message.hemomancy.qliphoth_bloom_block.the_next_pome_is_growing_return";
	private static final String POME_NOT_YOURS = "message.hemomancy.qliphoth_pome_item.the_pome_resists_your_mouth_it";
	private static final String POME_OUT_OF_ORDER = "message.hemomancy.qliphoth_pome_item.this_husk_is_already_consumed_or";
	private static final String POME_RESISTS = "message.hemomancy.qliphoth_pome_inventory_events.the_pome_resists_being_taken_away";
	private static final String POME_EAT_PREFIX = "message.hemomancy.qliphoth_pome.consume.";
	private static final String NINTH_HUSK = "message.hemomancy.qliphoth_pome_item.the_ninth_husk_opens_a_calcified";
	private static final String SPINE_MAKE_ROOM = "message.hemomancy.qliphoth_pome_item.make_room_in_your_inventory_to";
	private static final String SPINE_RETURNED = "message.hemomancy.qliphoth_bloom_block.the_spent_husks_return_your_fungal";
	private static final String SPINE_CARRIED = "message.hemomancy.qliphoth_bloom_block.your_fungal_spine_is_already_with";
	private static final String SPINE_RECLAIM_ROOM = "message.hemomancy.qliphoth_bloom_block.make_room_to_reclaim_your_fungal";

	private static final String PROJECTION_STARTS = "message.hemomancy.fungal_garden_travel_helper.your_body_remains_behind_you_have";
	private static final String SPINE_THIN = "message.hemomancy.fungal_garden_travel_helper.your_veins_run_too_thin_to";
	private static final String SPINE_WRONG = "message.hemomancy.fungal_garden_travel_helper.you_have_found_the_wrong_spine";
	private static final String SPINE_SHAPE = "message.hemomancy.fungal_garden_travel_helper.the_spine_still_needs_the_shape";
	private static final String SPINE_WILL_NOT_CARRY = "message.hemomancy.fungal_garden_travel_helper.the_spine_will_not_carry_you";

	private static final String CORE_WITNESS = "hemomancy.whisper.core_witness.line1";
	private static final String POST_SHATTER = "hemomancy.whisper.post_shatter.line1";
	private static final String POST_BLOOM = "hemomancy.whisper.post_bloom.line1";
	private static final String COMMUNION_WHISPER = "hemomancy.whisper.communion.line1";
	private static final String WHISPER_PENDING = "hemomancy.whisper.pending";
	private static final String CHOICE_SILENCE = "archon_choice_silence";
	private static final String CHOICE_EIGHTH = "archon_choice_eighth_degree";
	private static final String POWER_LOCKED = "message.hemomancy.blood_manipulation.your_blood_no_longer_answers_to";

	private static final String BLOOM_OVERLAP = "message.hemomancy.cardinal_rite_completion_effects.a_qliphoth_bloom_already_exists_within";
	private static final String BLOOM_NO_ROOM = "message.hemomancy.cardinal_rite_completion_effects.the_qliphoth_needs_more_room_to";
	private static final String BLOOM_GROWN = "message.hemomancy.cardinal_rite_completion_effects.the_qliphoth_blooms_a_dark_tree";

	private static final List<CardinalRitePhase> RITE_PHASES = List.of(CardinalRitePhase.CONSECRATION,
			CardinalRitePhase.INSCRIPTION, CardinalRitePhase.ORDEAL, CardinalRitePhase.STILL_INTERVAL,
			CardinalRitePhase.OFFERING_PROCESSION, CardinalRitePhase.CULMINATION);
	private static final Set<CardinalRitePhase> AFTER_SEAL = Set.of(CardinalRitePhase.ORDEAL,
			CardinalRitePhase.STILL_INTERVAL, CardinalRitePhase.OFFERING_PROCESSION, CardinalRitePhase.CULMINATION);

	private PlaythroughArchonGameTests() {
	}

	// =================================================================================================================
	// The chain from the previous band (for the late run)
	// =================================================================================================================

	/**
	 * The D7 Main replayed from {@code livingCovenantD6}: the Rite of the Hematic Order with a recruited Vicar at its
	 * station, the Monolith's shatter and the Seed picked up, the Bloom of the Qliphoth on its own floor, nine pomes
	 * picked and eaten, the Spine's first projection (degraded: forced back on the next tick), ending where
	 * {@link PlaythroughStates#choicePendingD7} puts a fixture player. Optional until it has run stable a few times.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 2 * GRAND_TEST_TICKS, batch = BLOOM_CHAIN, required = false)
	public static void archonChainFromPreviousBand(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.livingCovenantD6(p); // ARRANGE: the D6 band's end state
		PlaythroughClock clock = PlaythroughClock.start(helper);
		chain(helper, p, clock);
		clock.run();
	}

	/**
	 * The D6 to D7-choice chain as steps on {@code clock}: lays the archon floor in the template and the Bloom floor
	 * 48 blocks east (its own chunk, so the Bloom's 3-chunk radius stays clear of the template), recruits a Vicar
	 * helper, then plays the Archon rite, the shatter, the Bloom rite, the nine pomes, the Spine and the forced return.
	 * {@code p} must be at {@code LIVING_COVENANT_D6}; the batch must be the player's own (the Bloom).
	 */
	public static void chain(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock) {
		chain(helper, p, clock, null);
	}

	/**
	 * As {@link #chain(GameTestHelper, PlaythroughPlayer, PlaythroughClock)}, with the line's own pledged
	 * {@code recruit} as the helper when one is given (the late run's Vicar, pledged and summoned at D5): the ledger's
	 * summon brings him to the rite before it starts, instead of a Vicar placed at the station and recruited by
	 * ARRANGE. Checks {@code ARCHON_D7} after the rite and {@code COMMUNED_D7} after the ninth pome.
	 */
	public static void chain(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock, @Nullable Entity recruit) {
		readyForRites(p);
		PlaythroughSites.RiteFloor archonFloor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "archon_rite");
		BlockPos station = station(archonFloor);
		Entity vicar;
		if (recruit == null) {
			vicar = PlaythroughNpcs.vicar(helper, station);
			PlaythroughStates.recruited(p, vicar); // ARRANGE: recruited at D5 (the D5 band plays recruit_harbinger)
		} else {
			vicar = recruit;
		}
		PlaythroughRites.prepare(clock, p, archonFloor);
		if (recruit != null) {
			clock.then(() -> {
				p.moveTo(PlaythroughRites.riteStand(archonFloor));
				Outcome summon = p.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS);
				double after = recruit.distanceTo(p.player());
				PlaythroughLog.note(helper, "the ledger summon before the Archon rite: " + summon.describe() + " recruit "
						+ after + " blocks away");
				// The summon leaves a recruit already within 16 blocks where he is ("already nearby"); the rite walks him to
				// his station once assigned.
				helper.assertTrue(recruit.isAlive() && (after < 6.0D || summon.saidKeys().contains("hemomancy.ledger.summon.already_near")
						&& after < 16.0D), "the ledger's summon brings the recruit or finds him near; " + summon);
			});
		}
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, archonFloor.focus(), ritePlan(p, recruit == null ? assignNpc(p, vicar)
						: assignAndWatch(p, vicar, station), null)))
				.then(() -> {
					helper.assertTrue(p.degree() == 7, "the Archon rite promotes to degree 7; " + p.last());
					PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.ARCHON_D7);
				});
		BlockPos monolith = PlaythroughSites.stand(helper, 4, 18);
		clock.then(() -> {
			PlaythroughSites.monolith(p, monolith); // ARRANGE: raised by the D6 Working craft (the D6 band plays it)
			p.moveTo(monolith.south(2));
			DialogueView stone = p.talkToBlock(monolith);
			helper.assertTrue(stone.offers(SanguineMonolithDialogueTrees.EVENT_SHATTER), "the D7 Monolith lets the player press further: " + stone);
			Outcome shatter = p.chooseAtBlock(monolith, SanguineMonolithDialogueTrees.EVENT_SHATTER);
			helper.assertTrue(shatter.rewardDelivered(), "the second choice shatters the stone; " + shatter);
		}).idle(SEED_ENTITY_TICKS).then(() -> {
			helper.assertTrue(seedsNear(helper, monolith.above()) == 1, "the shatter drops the Seed");
			p.pickUpNear(monolith.above(), ItemInit.qliphoth_seed.get());
			p.pickUpNear(monolith.above(), ItemInit.monolith_fragment.get());
			helper.assertTrue(p.count(ItemInit.qliphoth_seed.get()) == 1, "the Seed is picked up");
		});
		// On the ground beside the template (its floor layer is 5 above the flat world): a floating floor lets the
		// ordeal's threats fall out of reach.
		BlockPos bloomOrigin = PlaythroughSites.farOrigin(helper, 48).offset(8, -5, 8);
		PlaythroughSites.RiteFloor[] bloomFloor = new PlaythroughSites.RiteFloor[1];
		clock.segment("the Bloom of the Qliphoth", c -> {
			bloomFloor[0] = PlaythroughSites.riteFloor(p, bloomOrigin, "bloom_of_qliphoth");
			// The ordeal's puppeteers flee the caster; a player's own presence keeps the ground round them loaded, which a
			// captured player's does not, so the chunks round the floor are kept ticking (ARRANGE).
			for (int dx = -32; dx <= 32; dx += 16) {
				for (int dz = -32; dz <= 32; dz += 16) {
					PlaythroughSites.keepTicking(helper, helper.getLevel(), bloomOrigin.offset(dx, 0, dz));
				}
			}
			BlockPos bloomStation = station(bloomFloor[0]);
			// ARRANGE: this floor lies outside the template, over open air; a real site has ground under the station.
			if (!helper.getLevel().getBlockState(bloomStation.below()).isFaceSturdy(helper.getLevel(), bloomStation.below(), Direction.UP)) {
				PlaythroughRites.set(helper, bloomStation.below(), Blocks.STONE);
				PlaythroughLog.note(helper, "ARRANGE gave the Bloom floor's Anchor station ground to stand on");
			}
			vicar.teleportTo(bloomStation.getX() + 0.5D, bloomStation.getY(), bloomStation.getZ() + 0.5D); // ARRANGE: the recruit walks over
			PlaythroughRites.prepare(c, p, bloomFloor[0]);
			int[] rescues = new int[1];
			c.then(() -> p.wield(ItemInit.living_staff.get()))
					.step(() -> PlaythroughClock.rite(p, bloomFloor[0].focus(), ritePlan(p, assignNpc(p, vicar), null)
							.extraAim(PlaythroughRites.stabilizeWhenDeadlocked(p, sigilsAndTraces(p), rescues)).traceFirst()))
					.then(() -> {
						PlaythroughLog.note(helper, "the chain's Bloom rite ended; CHAIN-1 rescues " + rescues[0]
								+ (rescues[0] > 0 ? " (DEGRADED: the dry anchors were filled by ARRANGE)" : ""));
						BlockPos root = bloomFloor[0].focus().above(2);
						QliphothBloomSavedData.BloomEntry bloom = blooms(helper).getBloomAt(root, dimension(helper));
						helper.assertTrue(bloom != null && bloom.ownerUUID().equals(p.uuid()) && bloom.center().equals(root),
								"the Bloom rite grows an owned Bloom at " + root.toShortString() + "; " + p.last());
						eatNineFromBloom(p, bloom);
						PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.COMMUNED_D7);
						Outcome projection = useSpine(p);
						projection.said(PROJECTION_STARTS);
						tick(p, "the forced return");
						PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.CHOICE_PENDING_D7);
					});
		});
	}

	// =================================================================================================================
	// The Rite of the Hematic Order
	// =================================================================================================================

	/**
	 * From Sanctified: planting the Staff before the Living Covenant is refused with the chapter named; then, with the
	 * Living Covenant (its Chamber part ARRANGE), the rite is played on threshold_grand with its five offerings, the
	 * Bastion and Reservoir sigils traced, and a recruited Vicar assigned to a station at INSCRIPTION by right-clicking
	 * him (EntityInteract). It ends at degree 7 with {@code degree_7_archon}, the offerings consumed and the Staff back.
	 */
	@GameTest(template = EMPTY, timeoutTicks = GRAND_TEST_TICKS, batch = RITES)
	public static void archonRiteWithHelper(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE: D6, the Living Covenant not yet done
		readyForRites(p);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "archon_rite");
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, station(floor));
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
			p.wield(ItemInit.living_staff.get());
			Outcome sealed = p.useOn(floor.focus());
			PlaythroughLog.note(helper, "archon rite before the Living Covenant: " + sealed.describe() + " keys=" + sealed.saidKeys());
			sealed.said(RANK_SEALED);
			helper.assertTrue(sealed.saidKeys().contains(LIVING_COVENANT), "the sealed line names the Living Covenant; " + sealed);
			helper.assertTrue(rite(p) == null, "no rite starts before the Living Covenant");
			PlaythroughStates.livingCovenantD6(p); // ARRANGE: chamber_returned cannot be earned here (Chamber not loaded)
			PlaythroughStates.recruited(p, vicar); // ARRANGE: recruited at D5
			p.wield(ItemInit.living_staff.get());
		}).step(() -> PlaythroughClock.rite(p, floor.focus(), ritePlan(p, assignNpc(p, vicar), null)))
				.then(() -> {
					Outcome rite = p.last();
					PlaythroughLog.note(helper, "archon rite with a Vicar at his station: " + rite.describe());
					helper.assertTrue(p.degree() == 7, "the Archon rite promotes to degree 7; " + rite);
					helper.assertTrue(p.has(HarbingerAdvancementGranter.ADV_DEGREE_7_ARCHON.getPath()), "degree_7_archon is earned");
					helper.assertTrue(p.count(ItemInit.living_staff.get()) >= 1, "the Staff comes back to the player");
					for (BlockPos socket : PlaythroughRites.usedSockets(floor)) {
						helper.assertTrue(!PlaythroughRites.brazier(p, socket).hasOffering(),
								"the offering at " + socket.toShortString() + " is consumed");
					}
					PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.ARCHON_D7);
					p.wield(ItemInit.living_staff.get());
					Outcome again = p.useOn(floor.focus());
					ActiveCardinalRite second = rite(p);
					PlaythroughLog.note(helper, "the Staff planted again at D7 (braziers spent): " + again.describe()
							+ " started " + (second == null ? "nothing" : second.getRecipeId()));
					helper.assertTrue(second == null || !second.getRecipeId().equals(Hemomancy.rloc("cardinal_rite/archon_rite")),
							"no second Archon rite starts at D7 (the bare floor may take another rite: "
									+ (second == null ? "none" : second.getRecipeId()) + ")");
				})
				.run();
	}

	/**
	 * No helper at all: the anchors and sigils are fed, but the daemon will not seal and the player is told the
	 * bloodline helpers must take their stations. The rite does not promote.
	 */
	@GameTest(template = EMPTY, timeoutTicks = GRAND_TEST_TICKS, batch = RITES)
	public static void archonRiteWithoutHelper(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.livingCovenantD6(p); // ARRANGE
		readyForRites(p);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "archon_rite");
		int mark = p.mark();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), ritePlan(p, null, null)
						.pauseWhen(() -> saidAny(p, mark, HELPERS_AT_SEAL, HELPERS_AT_FINALE))))
				.then(() -> {
					List<String> heard = keysSince(p, mark, "helper", "collapse");
					PlaythroughLog.note(helper, "archon rite with no helper: degree " + p.degree() + ", heard " + heard
							+ ", rite " + (rite(p) == null ? "gone" : rite(p).getPhase()));
					helper.assertTrue(saidAny(p, mark, HELPERS_AT_SEAL, HELPERS_AT_FINALE),
							"the player is told the rite needs its bloodline helpers; heard " + heard);
					helper.assertTrue(p.degree() == 6, "no helper, no Archon: degree " + p.degree());
				})
				.run();
	}

	/**
	 * The only helper is a second player of the line, who takes the Anchor station by right-clicking it during
	 * INSCRIPTION; once the altar is sealed the leader kicks them from the line (the ledger's kick). The rite must not
	 * promote, and the leader must be told why (which line is shown is logged).
	 */
	@GameTest(template = EMPTY, timeoutTicks = GRAND_TEST_TICKS, batch = RITES)
	public static void archonRiteHelperLeaves(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.livingCovenantD6(p); // ARRANGE
		readyForRites(p);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "archon_rite");
		BlockPos station = station(floor);
		PlaythroughPlayer member = PlaythroughPlayer.create(helper, station);
		PlaythroughStates.illuminatusD5(member); // ARRANGE
		PlaythroughStates.memberOf(member, p); // ARRANGE: joined by the signed ledger (the D5 band plays it)
		member.joinLevel();
		int mark = p.mark();
		boolean[] kicked = new boolean[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), ritePlan(p, claimStation(member, station), () -> {
					if (kicked[0]) return;
					kicked[0] = true;
					Outcome kick = p.kick(member.uuid());
					PlaythroughLog.note(helper, "the leader kicks the only helper mid-rite: " + kick.describe()
							+ "; the member heard " + member.chat());
				}).pauseWhen(() -> saidAny(p, mark, HELPERS_AT_SEAL, HELPERS_AT_FINALE, RITE_COLLAPSES))))
				.then(() -> {
					List<String> heard = keysSince(p, mark, "helper", "collapse", "falls_silent");
					PlaythroughLog.note(helper, "archon rite after the helper left the line: degree " + p.degree()
							+ ", kicked " + kicked[0] + ", heard " + heard + ", rite "
							+ (rite(p) == null ? "gone" : rite(p).getPhase()));
					helper.assertTrue(kicked[0], "the helper was kicked after the seal");
					helper.assertTrue(p.degree() == 6, "the rite does not promote once its only helper left the line");
					helper.assertTrue(saidAny(p, mark, HELPERS_AT_SEAL, HELPERS_AT_FINALE, RITE_COLLAPSES),
							"the leader is told why the rite cannot finish; heard " + heard);
				})
				.run();
	}

	/** As {@link #archonRiteHelperLeaves}, with the only helper a recruited Vicar who dies after the seal. */
	@GameTest(template = EMPTY, timeoutTicks = GRAND_TEST_TICKS, batch = RITES)
	public static void archonRiteHelperDies(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.livingCovenantD6(p); // ARRANGE
		readyForRites(p);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "archon_rite");
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, station(floor));
		PlaythroughStates.recruited(p, vicar); // ARRANGE
		int mark = p.mark();
		boolean[] killed = new boolean[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), ritePlan(p, assignNpc(p, vicar), () -> {
					if (killed[0]) return;
					killed[0] = true;
					PlaythroughNpcs.kill(vicar); // ARRANGE: the helper dies (nothing the player did)
					PlaythroughLog.note(helper, "the Vicar helper died mid-rite; alive=" + vicar.isAlive());
				}).pauseWhen(() -> saidAny(p, mark, HELPERS_AT_SEAL, HELPERS_AT_FINALE, RITE_COLLAPSES))))
				.then(() -> {
					List<String> heard = keysSince(p, mark, "helper", "collapse", "falls_silent");
					PlaythroughLog.note(helper, "archon rite after the helper died: degree " + p.degree() + ", heard "
							+ heard + ", rite " + (rite(p) == null ? "gone" : rite(p).getPhase()));
					helper.assertTrue(killed[0], "the helper died after the seal");
					helper.assertTrue(p.degree() == 6, "the rite does not promote once its only helper is dead");
					helper.assertTrue(saidAny(p, mark, HELPERS_AT_SEAL, HELPERS_AT_FINALE, RITE_COLLAPSES),
							"the caster is told why the rite cannot finish; heard " + heard);
				})
				.run();
	}

	// =================================================================================================================
	// The D7 Vicar
	// =================================================================================================================

	/**
	 * The D7 Vicar bows and gives the final saying as hearsay; his lines resolve and lead nowhere dead. The stained
	 * church map is handed out on every ask (logged under D-27, not re-decided here) and lands at his feet with a full
	 * pack (D-3 pattern, logged). After a pome the greeting is the pome-empowered one.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void d7VicarFinalTruthAndMap(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 8));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 11));
		PlaythroughStates.archonD7(p); // ARRANGE
		p.moveTo(PlaythroughSites.stand(helper, 12, 11));
		DialogueView view = p.approach(vicar).talkTo(vicar);
		view.assertLangKeys(helper, "the D7 Vicar").assertNoDeadEnds(helper, "the D7 Vicar");
		PlaythroughLog.note(helper, "D7 Vicar: " + view.describe() + " degree_hint=" + view.hasNode("degree_hint"));
		helper.assertTrue(view.lineKeys().contains(VICAR_ARCHON_LINE), "the D7 Vicar greets the Archon: " + view.lineKeys());
		helper.assertTrue(view.lineKeys().contains(VICAR_FINAL_TRUTH), "the final saying is reachable: " + view.lineKeys());
		helper.assertTrue(view.offers(VICAR_MAP_EVENT), "the D7 Vicar offers the stained church map: " + view);

		int asks = 3;
		for (int ask = 0; ask < asks; ask++) {
			p.talkTo(vicar);
			Outcome map = p.choose(vicar, VICAR_MAP_EVENT);
			if (ask == 0) map.said(VICAR_MAP_SAID);
			p.pickUpNear(vicar.blockPosition(), ItemInit.stained_church_map.get());
		}
		int carried = p.count(ItemInit.stained_church_map.get());
		PlaythroughLog.note(helper, "D-27 (late): " + asks + " asks for the stained church map, " + carried + " carried");
		helper.assertTrue(carried >= 1, "asking for the map gives one");

		p.fillInventory();
		p.talkTo(vicar);
		Outcome full = p.choose(vicar, VICAR_MAP_EVENT);
		int landed = full.delta(ItemInit.stained_church_map.get()) + full.dropped(ItemInit.stained_church_map.get(), Outcome.Near.ANY);
		PlaythroughLog.note(helper, "D-3 (late): the map with a full pack: " + full.describe());
		helper.assertTrue(landed == 1, "with a full pack the map is not lost: it lands at the Vicar's feet; " + full);

		p.player().getInventory().clearContent();
		p.give(new ItemStack(ItemInit.qliphoth_pome.get())); // ARRANGE: an unbound pome (food, not Communion)
		Outcome eaten = p.consume(ItemInit.qliphoth_pome.get());
		PlaythroughLog.note(helper, "an unbound pome eaten for the empowerment: " + eaten.describe());
		DialogueView empowered = p.talkTo(vicar);
		empowered.assertLangKeys(helper, "the pome-empowered D7 Vicar");
		helper.assertTrue(empowered.lineKeys().contains(VICAR_POME_LINE),
				"after a pome the Vicar greets the empowered Archon: " + empowered.lineKeys());
		helper.succeed();
	}

	// =================================================================================================================
	// The Sanguine Monolith
	// =================================================================================================================

	/**
	 * One player and one Monolith they raised, spoken to with an empty hand at D4 (unworthy), D5, D6 and D7 (the
	 * kinship line under "what are you"). At D7 an item in hand gives only the item inquiry, which hides the
	 * cornerstone and the shatter. Every tree's keys resolve.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void monolithAcrossDegrees(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.adeptD4(p); // ARRANGE
		BlockPos stone = PlaythroughSites.monolith(p, PlaythroughSites.stand(helper, 16, 12)); // ARRANGE: raised by its craft
		p.moveTo(stone.south(2));

		DialogueView d4 = p.talkToBlock(stone);
		d4.assertLangKeys(helper, "the Monolith at D4");
		PlaythroughLog.note(helper, "Monolith at D4: " + d4.describe());
		helper.assertTrue(d4.entityId() == SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID, "the Monolith speaks as a block");
		helper.assertTrue(d4.lineKeys().contains(MONOLITH_UNWORTHY), "a D4 player is unworthy: " + d4.lineKeys());

		PlaythroughStates.illuminatusD5(p); // ARRANGE
		DialogueView d5 = p.talkToBlock(stone);
		d5.assertLangKeys(helper, "the Monolith at D5").assertNoDeadEnds(helper, "the Monolith at D5");
		helper.assertTrue(d5.lineKeys().contains("hemomancy.monolith.guidance.illuminatus.line1"), "the D5 tree: " + d5.lineKeys());

		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		p.moveTo(stone.south(2));
		DialogueView d6 = p.talkToBlock(stone);
		d6.assertLangKeys(helper, "the Monolith at D6").assertNoDeadEnds(helper, "the Monolith at D6");
		helper.assertTrue(d6.lineKeys().contains("hemomancy.monolith.guidance.sanctified.line1"), "the D6 tree: " + d6.lineKeys());

		PlaythroughStates.archonD7(p); // ARRANGE
		p.moveTo(stone.south(2));
		DialogueView d7 = p.talkToBlock(stone);
		d7.assertLangKeys(helper, "the Monolith at D7");
		PlaythroughLog.note(helper, "Monolith at D7: " + d7.describe() + " deadEnds=" + d7.deadEnds());
		helper.assertTrue(d7.lineKeys().contains("hemomancy.monolith.guidance.archon.line1"), "the D7 tree: " + d7.lineKeys());
		helper.assertTrue(d7.lineKeys().contains(MONOLITH_KINSHIP), "what_are_you gives the kinship line: " + d7.lineKeys());
		helper.assertTrue(d7.offers(SanguineMonolithDialogueTrees.EVENT_SHATTER)
				&& d7.offers(SanguineMonolithDialogueTrees.EVENT_CORNERSTONE), "the D7 tree carries the shatter and cornerstone: " + d7);

		p.give(new ItemStack(Items.STICK)); // ARRANGE: something in hand
		p.wield(Items.STICK);
		var inquiry = p.useOnForDialogue(stone);
		helper.assertTrue(inquiry.isPresent(), "an item in hand still opens the Monolith; " + p.last());
		inquiry.get().assertLangKeys(helper, "the Monolith's item inquiry");
		PlaythroughLog.note(helper, "the Monolith with a stick in hand: " + inquiry.get().describe());
		helper.assertTrue(!inquiry.get().offers(SanguineMonolithDialogueTrees.EVENT_SHATTER)
						&& !inquiry.get().offers(SanguineMonolithDialogueTrees.EVENT_CORNERSTONE),
				"an item in hand gives only the inquiry: " + inquiry.get());
		helper.assertTrue(inquiry.get().lineKeys().contains(MONOLITH_ITEM_UNKNOWN), "an unknown item has the unknown line: "
				+ inquiry.get().lineKeys());
		helper.succeed();
	}

	/**
	 * The Monolithic Cornerstone from the D7 Monolith: refused before the Armature work ({@code _unready}), granted
	 * once the consecrated armor upgrade is done ({@code _granted}, the item in hand), then the node shows
	 * {@code _known} and the claim is gone. A second player claims with a full pack: the claim is kept and the
	 * cornerstone arrives at the next login.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void cornerstoneClaim(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.archonD7(p); // ARRANGE
		BlockPos stone = PlaythroughSites.monolith(p, PlaythroughSites.stand(helper, 16, 12)); // ARRANGE
		p.moveTo(stone.south(2));

		p.talkToBlock(stone);
		Outcome unready = p.chooseAtBlock(stone, SanguineMonolithDialogueTrees.EVENT_CORNERSTONE);
		unready.said(CORNERSTONE + "_unready").gaveNothing();

		armatureWorkDone(p); // ARRANGE: an armor upgrade at a consecrated Armature (the D5 and D6 bands play it)
		p.talkToBlock(stone);
		Outcome granted = p.chooseAtBlock(stone, SanguineMonolithDialogueTrees.EVENT_CORNERSTONE);
		granted.said(CORNERSTONE + "_granted").gave(ItemInit.monolithic_cornerstone.get(), 1);

		DialogueView known = p.talkToBlock(stone);
		known.assertLangKeys(helper, "the Monolith after the cornerstone");
		PlaythroughLog.note(helper, "the Monolith after the claim: " + known.describe());
		helper.assertTrue(known.lineKeys().contains(CORNERSTONE + "_known"), "the cornerstone node says it is known: " + known.lineKeys());
		helper.assertTrue(!known.offers(SanguineMonolithDialogueTrees.EVENT_CORNERSTONE), "the claim is not offered twice: " + known);

		PlaythroughPlayer full = PlaythroughPlayer.create(helper, stone.east(2));
		PlaythroughStates.archonD7(full); // ARRANGE
		armatureWorkDone(full); // ARRANGE
		MachineAccessEvents.awardMachineCrafted(full.player(), BlockInit.sanguine_monolith.get()); // ARRANGE: raised one too
		full.moveTo(stone.east(2));
		full.talkToBlock(stone);
		full.fillInventory();
		Outcome packed = full.chooseAtBlock(stone, SanguineMonolithDialogueTrees.EVENT_CORNERSTONE);
		PlaythroughLog.note(helper, "the cornerstone with a full pack: " + packed.describe() + " pending="
				+ HemoCapabilityAccess.stationUpgrades(full.player()).hasPending(UpgradeStation.ARMATURE, 2));
		helper.assertTrue(full.count(ItemInit.monolithic_cornerstone.get()) == 0
				&& HemoCapabilityAccess.stationUpgrades(full.player()).hasPending(UpgradeStation.ARMATURE, 2),
				"a full pack keeps the cornerstone owed; " + packed);
		full.player().getInventory().clearContent();
		full.relog();
		helper.assertTrue(full.count(ItemInit.monolithic_cornerstone.get()) == 1,
				"the owed cornerstone arrives at the next login once there is room");
		helper.succeed();
	}

	/**
	 * Pressing further twice (the press_again node, then the event) shatters the Monolith: the block and its filler
	 * go, one Qliphoth Seed and 5-8 fragments pop at {@code pos.above()}, the post-shatter whisper is queued while its
	 * memo is unknown, and picking up the Seed makes the Bloom rite discoverable. Refusals: from 9 blocks away, at a
	 * replaced block, and a second player whose conversation the first shatter ended. The refusals are silent (logged
	 * under D-28, not re-decided).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void pressFurtherShatters(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.archonD7(p); // ARRANGE
		BlockPos stone = PlaythroughSites.monolith(p, PlaythroughSites.stand(helper, 16, 12)); // ARRANGE
		p.moveTo(stone.south(2));

		DialogueView view = p.talkToBlock(stone);
		helper.assertTrue(view.hasNode("press_again") && view.lineKeys().contains(MONOLITH_PRESS_AGAIN),
				"press further leads to the warning node: " + view);
		p.moveTo(stone.south(9));
		Outcome far = p.chooseAtBlock(stone, SanguineMonolithDialogueTrees.EVENT_SHATTER);
		helper.assertTrue(monolithAt(p, stone) && !far.rewardDelivered(), "from 9 blocks the stone does not break; " + far);
		PlaythroughLog.note(helper, "D-28 (late): press further from 9 blocks: said " + far.chat());

		p.moveTo(stone.south(2));
		p.talkToBlock(stone);
		helper.getLevel().setBlock(stone, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL); // ARRANGE: the stone is replaced
		PlaythroughSites.monolith(p, stone);
		p.moveTo(stone.south(2));
		Outcome replaced = p.chooseAtBlock(stone, SanguineMonolithDialogueTrees.EVENT_SHATTER);
		helper.assertTrue(monolithAt(p, stone) && !replaced.rewardDelivered(), "a replaced stone does not break; " + replaced);
		PlaythroughLog.note(helper, "D-28 (late): press further at a replaced stone: said " + replaced.chat());

		PlaythroughPlayer second = PlaythroughPlayer.create(helper, stone.east(2));
		PlaythroughStates.archonD7(second); // ARRANGE
		MachineAccessEvents.awardMachineCrafted(second.player(), BlockInit.sanguine_monolith.get()); // ARRANGE
		second.moveTo(stone.east(2));
		second.talkToBlock(stone);
		p.talkToBlock(stone);
		boolean whisperExpected = FungalWhisperDialogueTrees.shouldOfferMemoWhisper(p.player(), MemoDefinitions.FUNGAL_WHISPER_TRUTH);
		boolean discoveredBefore = RiteDiscoveryRules.isDiscovered(p.player(), Hemomancy.rloc("cardinal_rite/bloom_of_qliphoth"));
		Outcome shatter = p.chooseAtBlock(stone, SanguineMonolithDialogueTrees.EVENT_SHATTER);
		int fragments = shatter.droppedNear(stone.above(), ItemInit.monolith_fragment.get());
		PlaythroughLog.note(helper, "the shatter: " + shatter.describe() + " fragments=" + fragments
				+ " whisperExpected=" + whisperExpected + " queued=" + shatter.whispersQueued().size());
		helper.assertTrue(shatter.rewardDelivered(), "the second choice shatters the stone; " + shatter);
		helper.assertTrue(!monolithAt(p, stone) && !helper.getLevel().getBlockState(stone.above()).is(BlockInit.filler_block.get()),
				"the Monolith and its filler are gone");
		helper.assertTrue(fragments >= MonolithFragmentDropRules.MIN_FRAGMENTS && fragments <= MonolithFragmentDropRules.MAX_FRAGMENTS,
				"5-8 Monolith Fragments pop out (" + fragments + ")");
		if (whisperExpected) shatter.whisperQueued(POST_SHATTER);

		Outcome late = second.chooseAtBlock(stone, SanguineMonolithDialogueTrees.EVENT_SHATTER);
		helper.assertTrue(!late.rewardDelivered() && late.droppedNear(stone.above(), ItemInit.monolith_fragment.get()) == 0,
				"a second player's open conversation does not shatter the gone stone again; " + late);
		PlaythroughLog.note(helper, "D-28 (late): the second player's press further after the shatter: said " + late.chat());

		// The Seed's own item entity replaces the plain drop on the server's next task run, so it is counted a few
		// ticks later rather than in the shatter's outcome.
		PlaythroughClock.start(helper).idle(SEED_ENTITY_TICKS).then(() -> {
			int seeds = seedsNear(helper, stone.above());
			PlaythroughLog.note(helper, "the shatter's Seed after " + SEED_ENTITY_TICKS + " ticks: " + seeds);
			helper.assertTrue(seeds == 1, "one Qliphoth Seed pops out (" + seeds + ")");
			Outcome seed = p.pickUpNear(stone.above(), ItemInit.qliphoth_seed.get());
			seed.gave(ItemInit.qliphoth_seed.get(), 1);
			p.pickUpNear(stone.above(), ItemInit.monolith_fragment.get());
			boolean discovered = RiteDiscoveryRules.isDiscovered(p.player(), Hemomancy.rloc("cardinal_rite/bloom_of_qliphoth"));
			PlaythroughLog.note(helper, "Seed picked up: QLIPHOTH entry " + LiberKnowledgeHelper.hasEntry(p.player(), LiberEntryDefinitions.QLIPHOTH)
					+ ", Bloom rite discovered before=" + discoveredBefore + " after=" + discovered);
			helper.assertTrue(LiberKnowledgeHelper.hasEntry(p.player(), LiberEntryDefinitions.QLIPHOTH) && discovered,
					"with the Seed in hand the Qliphoth entry is known and the Bloom rite discoverable");
		}).run();
	}

	/** The open conversation expires after 1200 ticks: pressing further then breaks nothing (silent, D-28 family). */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = CLOCK)
	public static void pressFurtherAfterTheTalkExpires(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.archonD7(p); // ARRANGE
		BlockPos stone = PlaythroughSites.monolith(p, PlaythroughSites.stand(helper, 16, 12)); // ARRANGE
		p.moveTo(stone.south(2));
		p.talkToBlock(stone);
		PlaythroughTime.skipGameTime(helper, 1201); // ARRANGE: the player wanders off for a minute
		Outcome expired = p.chooseAtBlock(stone, SanguineMonolithDialogueTrees.EVENT_SHATTER);
		PlaythroughLog.note(helper, "D-28 (late): press further after 1201 ticks: " + expired.describe());
		helper.assertTrue(monolithAt(p, stone) && !expired.rewardDelivered(), "an expired conversation breaks nothing; " + expired);
		helper.succeed();
	}

	/**
	 * A second D7 player at someone else's Monolith: one who never raised a Monolith is locked out; one who raised
	 * their own elsewhere can press further and break it. And when the owner shatters their own, a stranger can walk
	 * off with the Seed.
	 */
	// DECISION D7-2: Monoliths have no owner, so any D7 player who raised one can shatter another's and take its
	// unowned Seed and fragments (6000-tick item lifespan, unlike the owner-bound pomes). Options: bind the Monolith to
	// its builder / give the Seed and fragments to the presser's inventory / keep.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void strangerShattersYourMonolith(GameTestHelper helper) {
		PlaythroughPlayer owner = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.archonD7(owner); // ARRANGE
		BlockPos first = PlaythroughSites.monolith(owner, PlaythroughSites.stand(helper, 20, 6)); // ARRANGE: the owner's
		BlockPos second = PlaythroughSites.monolith(owner, PlaythroughSites.stand(helper, 20, 18)); // ARRANGE: the owner's
		PlaythroughPlayer stranger = PlaythroughPlayer.create(helper, first.east(2));
		PlaythroughStates.archonD7(stranger); // ARRANGE
		stranger.moveTo(first.east(2));

		var locked = stranger.tryTalkToBlock(first);
		PlaythroughLog.note(helper, "D7-2: a stranger who never raised a Monolith: opened=" + locked.isPresent() + " said "
				+ stranger.last().saidKeys());
		MachineAccessEvents.awardMachineCrafted(stranger.player(), BlockInit.sanguine_monolith.get()); // ARRANGE: raised their own elsewhere
		DialogueView view = stranger.talkToBlock(first);
		Outcome broke = view.offers(SanguineMonolithDialogueTrees.EVENT_SHATTER)
				? stranger.chooseAtBlock(first, SanguineMonolithDialogueTrees.EVENT_SHATTER) : null;
		boolean strangerBroke = !monolithAt(owner, first);
		Outcome[] taken = new Outcome[1];
		PlaythroughClock.start(helper).idle(SEED_ENTITY_TICKS).then(() -> { // the Seed's entity lands a tick later
			if (strangerBroke) stranger.pickUpNear(first.above(), ItemInit.qliphoth_seed.get());
			PlaythroughLog.note(helper, "D7-2: a credited stranger at the owner's Monolith: offered shatter="
					+ view.offers(SanguineMonolithDialogueTrees.EVENT_SHATTER) + " broke=" + strangerBroke + " stranger seeds="
					+ stranger.count(ItemInit.qliphoth_seed.get()) + (broke == null ? "" : " " + broke.describe()));
			owner.moveTo(second.south(2));
			owner.talkToBlock(second);
			owner.chooseAtBlock(second, SanguineMonolithDialogueTrees.EVENT_SHATTER);
		}).idle(SEED_ENTITY_TICKS).then(() -> {
			stranger.moveTo(second.east(2));
			taken[0] = stranger.pickUpNear(second.above(), ItemInit.qliphoth_seed.get());
			PlaythroughLog.note(helper, "D7-2: the owner's shatter, then the stranger walks over: " + taken[0].describe());
			helper.assertTrue(!strangerBroke, "D7-2 expected: a stranger cannot shatter another player's Monolith");
			helper.assertTrue(taken[0].delta(ItemInit.qliphoth_seed.get()) == 0, "D7-2 expected: the owner's Seed is not the stranger's to take");
		}).run();
	}

	/**
	 * The fragment budget: shattering before claiming the free cornerstone leaves only the crafted cornerstone, which
	 * needs fragments of its own besides the two the Monolithic Armature rite takes. On a low roll that is more than
	 * one shatter gives, and only another Monolith recovers it. A player would expect a warning or enough fragments.
	 */
	// DECISION D7-3: MonolithFragmentDropRules rolls 5-8; recipe/monolithic_cornerstone needs 4 fragments + 2 cloth
	// (1 fragment each) and the Monolithic Armature rite 1 fragment + 1 cloth. Shattering first on a 5 roll leaves the
	// Weight of the Frame short, and no line warns. Options: a warning line in the press_again node / raise the minimum
	// roll / keep.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void shatterBeforeCornerstone(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.archonD7(p); // ARRANGE
		armatureWorkDone(p); // ARRANGE: eligible for the free cornerstone, not yet claimed
		BlockPos stone = PlaythroughSites.monolith(p, PlaythroughSites.stand(helper, 16, 12)); // ARRANGE
		p.moveTo(stone.south(2));
		DialogueView view = p.talkToBlock(stone);
		Outcome shatter = p.chooseAtBlock(stone, SanguineMonolithDialogueTrees.EVENT_SHATTER);
		int rolled = shatter.droppedNear(stone.above(), ItemInit.monolith_fragment.get());

		int perCloth = fragmentsIn(helper, "monolith_imbued_cloth");
		int craftedCornerstone = fragmentsIn(helper, "monolithic_cornerstone") + clothIn(helper, "monolithic_cornerstone") * perCloth;
		CardinalRiteRecipe frameRite = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(), Hemomancy.rloc("cardinal_rite/monolithic_armature"));
		int riteFragments = 0;
		for (ItemStack offering : PlaythroughRites.offerings(frameRite)) {
			if (offering.is(ItemInit.monolith_fragment.get())) riteFragments++;
			if (offering.is(ItemInit.monolith_imbued_cloth.get())) riteFragments += perCloth;
		}
		int needed = craftedCornerstone + riteFragments;
		String warning = english(MONOLITH_PRESS_AGAIN) + " | " + english("hemomancy.artificer.assignment.weight_of_frame.cornerstone");
		boolean warned = warning.toLowerCase(Locale.ROOT).contains("cornerstone") && warning.toLowerCase(Locale.ROOT).contains("fragment");
		PlaythroughLog.note(helper, "D7-3: rolled " + rolled + " fragments (min " + MonolithFragmentDropRules.MIN_FRAGMENTS
				+ "); a crafted cornerstone takes " + craftedCornerstone + " and the frame rite " + riteFragments + " (total "
				+ needed + "); the Monolith can no longer give its cornerstone (block gone=" + !monolithAt(p, stone)
				+ "); warning text: " + warning + "; tree " + view.describe());
		helper.assertTrue(warned || MonolithFragmentDropRules.MIN_FRAGMENTS >= needed,
				"D7-3 expected: a shatter before the cornerstone either warns or always leaves enough fragments ("
						+ MonolithFragmentDropRules.MIN_FRAGMENTS + " < " + needed + ")");
		helper.succeed();
	}

	// =================================================================================================================
	// The Weight of the Frame
	// =================================================================================================================

	/**
	 * The Artificer's D7 assignment end to end: the brief, the reminder naming the cornerstone, the Rite of the
	 * Monolithic Armature on working_greater (the Armature behind the Focus, six offerings), the first D7 armor
	 * upgrade by standing in the monolithic Armature, the inspection and its lineage material (once), the full set, the
	 * armor ability through its payload, the fitting, and the fitting reissued with a full pack.
	 */
	@GameTest(template = EMPTY, timeoutTicks = PlaythroughRites.GREATER_TICKS + 3000, batch = RITES)
	public static void weightOfFrameChain(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.archonD7(p); // ARRANGE
		readyForRites(p);
		armatureWorkDone(p); // ARRANGE: the consecrated Armature's work (D5 and D6 bands)
		HarbingerArtificerEntity artificer = PlaythroughNpcs.artificer(helper, PlaythroughSites.stand(helper, 4, 20));
		BlockPos talkSpot = PlaythroughSites.stand(helper, 6, 20);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "monolithic_armature");
		BlockPos[] seat = new BlockPos[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(() -> {
			p.moveTo(talkSpot);
			DialogueView first = p.approach(artificer).talkTo(artificer);
			first.assertLangKeys(helper, "the D7 Artificer").assertNoDeadEnds(helper, "the D7 Artificer");
			helper.assertTrue(first.offers(HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME),
					"the D7 Artificer briefs the Weight of the Frame: " + first);
			p.choose(artificer, HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME);
			helper.assertTrue(ArtificerAssignments.has(p.player(), ArtificerAssignments.WEIGHT_OF_FRAME_BRIEFED), "briefed");
			DialogueView reminder = p.talkTo(artificer);
			PlaythroughLog.note(helper, "the Artificer after the brief: " + reminder.describe() + " lines=" + reminder.lineKeys());
			helper.assertTrue(reminder.lineKeys().contains("hemomancy.artificer.assignment.weight_of_frame.cornerstone"),
					"the reminder names the cornerstone step: " + reminder.lineKeys());

			seat[0] = PlaythroughSites.stationBehindFocus(p, floor, UpgradeStation.ARMATURE);
			faceNorth(helper, seat[0]); // ARRANGE: turned to face the Focus
			HematicArmatureBlockEntity armature = armature(helper, seat[0]);
			armature.setRiteLocked(true); // ARRANGE: consecrated by the D5 rite
			helper.assertTrue(armature.completeUpgrade(ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED, UUID.randomUUID()),
					"the Armature can be ARRANGEd consecrated");
			armature.setRiteLocked(false);
			ArtificerAssignments.onArmatureTierApplied(p.player(), ArmatureUpgradeRules.ArmatureTier.VICAR_CONSECRATED);
		});
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), ritePlan(p, null, null).extraAim(stationCircuits(p))))
				.then(() -> {
					PlaythroughLog.note(helper, "the Monolithic Armature rite: " + p.last().describe());
					helper.assertTrue(armature(helper, seat[0]).getArmatureTier() == ArmatureUpgradeRules.ArmatureTier.MONOLITHIC,
							"the rite raises the Armature to its monolithic tier; " + p.last());
					helper.assertTrue(p.has(HarbingerAdvancementGranter.ADV_ARTIFICER_MONOLITHIC_FRAME.getPath()), "monolithic frame recorded");
					HematicArmatureBlockEntity armature = armature(helper, seat[0]);
					armature.setItem(HematicArmatureBlockEntity.SLOT_FEET_REAGENT, new ItemStack(ItemInit.fargone_proboscis.get())); // ARRANGE
					var reservoir = armature.getBloodCapability();
					reservoir.setActive(true); // ARRANGE: the player filled the Armature
					reservoir.setMaxBloodVolume(HematicArmatureBlockEntity.MAX_BLOOD);
					reservoir.setBloodVolume(2000.0D);
					p.player().setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.blood_lust_boots.get())); // ARRANGE: D5 boots worn
					p.joinLevel(); // the Armature finds its restrained player through the server's player list
					p.moveTo(seat[0].above());
					BlockState state = helper.getLevel().getBlockState(seat[0]);
					BlockInit.hematic_armature.get().stepOn(helper.getLevel(), seat[0], state, p.player());
				})
				.step(new PlaythroughClock.Step() {
					@Override
					public boolean tick(int ticks) {
						if (HarbingerAdvancementGranter.isArtificerFirstD7Upgrade(p.player())) return true;
						if (ticks < 400) return false;
						PlaythroughNotRun.degraded(helper, "the Armature cycle did not finish for the captured player in 400 ticks;"
								+ " ARRANGE recorded the first D7 upgrade through ArtificerAssignments.onArmatureUpgrade");
						ItemStack boots = new ItemStack(ItemInit.edacious_blood_lust_boots.get());
						p.player().setItemSlot(EquipmentSlot.FEET, boots);
						ArtificerAssignments.onArmatureUpgrade(p.player(), boots, 7);
						return true;
					}

					@Override
					public String label() {
						return "the first D7 upgrade at the Armature";
					}
				})
				.then(() -> {
					p.player().stopRiding();
					p.moveTo(talkSpot);
					PlaythroughLog.note(helper, "first D7 upgrade: feet " + p.player().getItemBySlot(EquipmentSlot.FEET)
							+ " lineage " + ArtificerAssignments.firstD7Lineage(p.player()));
					helper.assertTrue(ArtificerAssignments.firstD7Lineage(p.player()) != ArtificerProgressionRules.D7Lineage.NONE,
							"the first D7 upgrade records its lineage");
					DialogueView inspect = p.approach(artificer).talkTo(artificer);
					helper.assertTrue(inspect.offers(HarbingerArtificerDialogueTrees.EVENT_INSPECT_WEIGHT_OF_FRAME),
							"the Artificer offers the inspection: " + inspect);
					Outcome inspected = p.choose(artificer, HarbingerArtificerDialogueTrees.EVENT_INSPECT_WEIGHT_OF_FRAME);
					inspected.said("hemomancy.dialogue.event.artificer_weight_of_frame_inspected");
					int material = inspected.inventoryDelta().values().stream().mapToInt(Integer::intValue).filter(v -> v > 0).sum()
							+ inspected.drops().size();
					PlaythroughLog.note(helper, "the inspection: " + inspected.describe());
					helper.assertTrue(material >= 1, "the inspection gives the lineage material; " + inspected);
					DialogueView after = p.talkTo(artificer);
					helper.assertTrue(!after.offers(HarbingerArtificerDialogueTrees.EVENT_INSPECT_WEIGHT_OF_FRAME), "inspected once: " + after);
					Outcome again = p.forge(HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_REWARD, artificer.getId());
					helper.assertTrue(again.drops().isEmpty() && again.inventoryDelta().values().stream().noneMatch(v -> v > 0),
							"the lineage material is given once; " + again);

					p.player().setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.edacious_blood_lust_helm.get())); // ARRANGE: the rest of the set
					p.player().setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.edacious_blood_lust_chest.get()));
					p.player().setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.edacious_blood_lust_legs.get()));
					if (!p.player().getItemBySlot(EquipmentSlot.FEET).is(ItemInit.edacious_blood_lust_boots.get())) {
						p.player().setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.edacious_blood_lust_boots.get()));
					}
					PlaythroughStates.blood(p, 2000.0D); // ARRANGE
					Outcome.Probe probe = Outcome.begin(p, null, "armor ability " + ArmorSetAbilityRegistry.EDACIOUS_BLOODBURST);
					ActivateArmorSetAbilityC2SPacket.handle(new ActivateArmorSetAbilityC2SPacket(ArmorSetAbilityRegistry.EDACIOUS_BLOODBURST),
							PayloadContexts.of(p.player()));
					Outcome ability = probe.finish();
					PlaythroughLog.note(helper, "the Edacious ability: " + ability.describe());
					helper.assertTrue(ArtificerAssignments.has(p.player(), ArtificerAssignments.WEIGHT_OF_FRAME_DEMONSTRATED),
							"the full set's ability demonstrates the frame; " + ability);

					DialogueView fitting = p.talkTo(artificer);
					helper.assertTrue(fitting.offers(HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING), "the fitting is offered: " + fitting);
					Outcome claimed = p.choose(artificer, HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING);
					p.pickUpNear(artificer.blockPosition(), ItemInit.monolithic_frame_fitting.get());
					claimed.said("hemomancy.dialogue.event.artificer_d7_fitting_granted");
					helper.assertTrue(p.count(ItemInit.monolithic_frame_fitting.get()) == 1, "the fitting is in hand; " + claimed);

					removeMatching(p, s -> s.is(ItemInit.monolithic_frame_fitting.get())); // ARRANGE: the fitting is lost
					p.fillInventory();
					DialogueView replace = p.talkTo(artificer);
					PlaythroughLog.note(helper, "the Artificer after the fitting is lost: " + replace.describe());
					helper.assertTrue(replace.offers(HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING), "a lost fitting can be replaced: " + replace);
					Outcome reissued = p.choose(artificer, HarbingerArtificerDialogueTrees.EVENT_CLAIM_D7_FITTING);
					PlaythroughLog.note(helper, "D-3 (late): the fitting reissued with a full pack: " + reissued.describe());
					helper.assertTrue(reissued.dropped(ItemInit.monolithic_frame_fitting.get(), Outcome.Near.ANY) == 1,
							"with a full pack the reissued fitting is not lost; " + reissued);
				})
				.run();
	}

	/**
	 * The Artificer's lineage recovery at D7 (the first D7 upgrade done, its lineage unknown): the Silent Archon
	 * lineage is not among the choices, since there is no seventh-Degree Silent Archon; the other three are.
	 */
	// FIX: was probe D7-7. HEMOMANCY_REFERENCE §22.8 (rule 2026-10-31): Silent Archon power is Silentium's alone and the
	// Weight of the Frame records that lineage only for a Silentium, so a D7 Archon was offered an option that silently
	// recorded nothing. The option is now shown to a D8 Silentium only (ArtificerProgressSnapshot.silentium).
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void silentArchonLineageAtD7(GameTestHelper helper) {
		HarbingerArtificerEntity artificer = PlaythroughNpcs.artificer(helper, PlaythroughSites.stand(helper, 12, 8));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 11));
		PlaythroughStates.archonD7(p); // ARRANGE
		grant(p, ArtificerAssignments.WEIGHT_OF_FRAME_BRIEFED, HarbingerAdvancementGranter.ADV_ARTIFICER_MONOLITHIC_FRAME,
				HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_D7_UPGRADE); // ARRANGE: upgraded before the lineage was kept
		p.moveTo(PlaythroughSites.stand(helper, 12, 11));
		DialogueView view = p.approach(artificer).talkTo(artificer);
		view.assertLangKeys(helper, "the D7 Artificer in recovery");
		String silent = HarbingerArtificerDialogueTrees.EVENT_RECOVER_D7_PREFIX + "silent_archon";
		boolean offered = view.offers(silent);
		Outcome chosen = offered ? p.choose(artificer, silent) : null;
		PlaythroughLog.note(helper, "D7-7: recovery options " + view.reachableEvents() + "; silent_archon offered=" + offered
				+ (chosen == null ? "" : " chosen: " + chosen.describe() + " lineage now " + ArtificerAssignments.firstD7Lineage(p.player())));
		helper.assertTrue(!offered, "the Silent Archon lineage is not offered to a D7 Archon");
		for (String lineage : List.of("edacious", "sheolic", "phantasmal")) {
			helper.assertTrue(view.offers(HarbingerArtificerDialogueTrees.EVENT_RECOVER_D7_PREFIX + lineage),
					"the " + lineage + " lineage is still offered for recovery: " + view.reachableEvents());
		}
		helper.succeed();
	}

	// =================================================================================================================
	// The Bloom of the Qliphoth
	// =================================================================================================================

	/**
	 * The Seed seated in the Focus of a qliphoth_grand floor: refused while a stranger's Bloom stands within three
	 * chunks, and refused while the Bloom's cell is blocked, the Seed staying in the Focus each time; then played with
	 * a line member at the Anchor station, the support sigils and the ordeal's demanded sigils traced. The Bloom stands
	 * two above the Focus, owned by the caster, the Seed spent, and the post-bloom whisper queued while its memo is
	 * unknown.
	 */
	@GameTest(template = EMPTY, timeoutTicks = GRAND_TEST_TICKS, batch = BLOOM_RITE, skyAccess = true)
	public static void bloomRiteAndRefusals(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.archonD7(p); // ARRANGE
		readyForRites(p);
		p.give(new ItemStack(ItemInit.qliphoth_seed.get())); // ARRANGE: the Seed from the shatter (pressFurtherShatters plays it)
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "bloom_of_qliphoth");
		BlockPos station = station(floor);
		BlockPos root = floor.focus().above(2);
		PlaythroughPlayer member = PlaythroughPlayer.create(helper, station);
		PlaythroughStates.illuminatusD5(member); // ARRANGE
		PlaythroughStates.memberOf(member, p); // ARRANGE
		member.joinLevel();
		PlaythroughPlayer stranger = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 28, 3));
		PlaythroughStates.archonD7(stranger); // ARRANGE
		QliphothBloomSavedData.BloomEntry[] theirs = new QliphothBloomSavedData.BloomEntry[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
			theirs[0] = PlaythroughSites.qliphothBloom(stranger, root.east(20)); // ARRANGE: a stranger's Bloom in range
			p.wield(ItemInit.living_staff.get());
			Outcome overlap = p.useOn(floor.focus());
			PlaythroughLog.note(helper, "the Bloom rite beside a stranger's Bloom: " + overlap.describe());
			overlap.said(BLOOM_OVERLAP);
			helper.assertTrue(rite(p) == null && focusHoldsMedium(helper, floor), "no rite starts and the Seed stays in the Focus");
			removeBloom(helper, theirs[0]); // ARRANGE: the stranger's Bloom is gone

			PlaythroughRites.set(helper, root, Blocks.STONE); // ARRANGE: something stands in the Bloom's cell
			p.wield(ItemInit.living_staff.get());
			Outcome blocked = p.useOn(floor.focus());
			PlaythroughLog.note(helper, "the Bloom rite with its cell blocked: " + blocked.describe());
			blocked.said(BLOOM_NO_ROOM);
			helper.assertTrue(rite(p) == null && focusHoldsMedium(helper, floor), "no rite starts and the Seed stays in the Focus");
			helper.getLevel().setBlock(root, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			p.wield(ItemInit.living_staff.get());
		});
		boolean[] whisperExpected = new boolean[1];
		int mark = p.mark();
		clock.then(() -> whisperExpected[0] = FungalWhisperDialogueTrees.shouldOfferMemoWhisper(p.player(), MemoDefinitions.QLIPHOTH_COMMUNION))
				// ARRANGE: the deck pinned without the draining waves; the random deck is decision CHAIN-1.
				.step(() -> PlaythroughClock.rite(p, floor.focus(), PlaythroughRites.withoutDrainingWaves(
						ritePlan(p, claimStation(member, station), null).extraAim(sigilsAndTraces(p)).traceFirst(), p)))
				.then(() -> {
					QliphothBloomSavedData.BloomEntry bloom = blooms(helper).getBloomAt(root, dimension(helper));
					PlaythroughLog.note(helper, "the Bloom rite: " + p.last().describe() + " bloom=" + bloom + " heard "
							+ keysSince(p, mark, "bloom", "qliphoth", "helper"));
					helper.assertTrue(bloom != null && bloom.center().equals(root) && bloom.ownerUUID().equals(p.uuid()),
							"an owned Bloom stands two above the Focus");
					helper.assertTrue(helper.getLevel().getBlockState(root).is(BlockInit.qliphoth_bloom.get())
							&& helper.getLevel().getBlockState(root.above()).is(BlockInit.filler_block.get()), "the Bloom and its fillers are placed");
					helper.assertTrue(saidAny(p, mark, BLOOM_GROWN), "the player is told the Qliphoth blooms");
					helper.assertTrue(!focusHoldsMedium(helper, floor) && p.count(ItemInit.qliphoth_seed.get()) == 0,
							"the Seed is spent only by the completed rite");
					if (whisperExpected[0]) {
						helper.assertTrue(hasQueued(p, POST_BLOOM), "the post-bloom whisper is queued while its memo is unknown");
					}
				})
				.run();
	}

	/**
	 * Picking at the owner's Bloom (ripening ARRANGE through the saved data): nothing ripe yet says the next pome is
	 * growing; a stranger is refused and given nothing; a full pack leaves the ripe pome waiting; the owner then picks
	 * a bound pome; a second pick is refused. A Creative owner with a full pack is logged (D-16 family).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BLOOM_PICKING)
	public static void pomePicking(GameTestHelper helper) {
		PlaythroughPlayer owner = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.archonD7(owner); // ARRANGE
		BlockPos root = PlaythroughSites.stand(helper, 16, 12);
		QliphothBloomSavedData.BloomEntry bloom = PlaythroughSites.qliphothBloom(owner, root); // ARRANGE: grown by the rite
		PlaythroughPlayer stranger = PlaythroughPlayer.create(helper, root.east(2));
		PlaythroughStates.archonD7(stranger); // ARRANGE
		owner.moveTo(root.south(2));
		stranger.moveTo(root.east(2));
		QliphothBloomSavedData blooms = blooms(helper);

		PlaythroughRites.emptyHand(owner);
		owner.useOn(root).said(POME_GROWING).gaveNothing();

		ripen(blooms, bloom, 0); // ARRANGE: the first husk ripens (random 1/80 every 40 ticks)
		PlaythroughRites.emptyHand(stranger);
		stranger.useOn(root).said(POME_STRANGER).gaveNothing();
		helper.assertTrue(!blooms.isPendingPomeClaimed(bloom), "the stranger's touch claims nothing");

		owner.fillInventory();
		Outcome waits = owner.useOn(root);
		waits.said(POME_WAITS);
		helper.assertTrue(blooms.hasPendingPome(bloom) && !blooms.isPendingPomeClaimed(bloom), "a full pack leaves the pome on the tree");
		owner.player().getInventory().clearContent();

		Outcome picked = owner.useOn(root);
		picked.said(POME_PICKED).gave(ItemInit.qliphoth_pome.get(), 1);
		ItemStack pome = firstPome(owner);
		helper.assertTrue(QliphothPomeItem.isBoundPomeFromBloom(pome, bloom), "the picked pome is bound to this Bloom and owner");
		owner.useOn(root).said(POME_ALREADY).gaveNothing();

		owner.consume(ItemInit.qliphoth_pome.get());
		ripen(blooms, bloom, 1); // ARRANGE
		owner.creative().fillInventory();
		Outcome creative = owner.useOn(root);
		owner.survival();
		owner.player().getInventory().clearContent();
		Outcome after = owner.useOn(root);
		PlaythroughLog.note(helper, "D-16 (late): a Creative owner with a full pack picks: " + creative.describe() + " claimed="
				+ blooms.isPendingPomeClaimed(bloom) + "; back in survival: " + after.describe());
		helper.succeed();
	}

	/**
	 * Nine pomes picked from the owner's Bloom and eaten in husk order. On the way: a pome bound to another player
	 * resists the mouth; an unbound pome is food with no progress; a later husk out of order is refused; a bound pome
	 * resists a toss, comes back out of a chest and back after a death; a relog keeps it. The ninth opens Communion,
	 * delivers the Fungal Spine (with room) and sends the Communion whisper.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BLOOM_NINE)
	public static void ninePomesAndTheSpine(GameTestHelper helper) {
		PlaythroughPlayer owner = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.archonD7(owner); // ARRANGE
		BlockPos root = PlaythroughSites.stand(helper, 16, 12);
		QliphothBloomSavedData.BloomEntry bloom = PlaythroughSites.qliphothBloom(owner, root); // ARRANGE
		QliphothBloomSavedData blooms = blooms(helper);
		var degree = HemoCapabilityAccess.getInitiatoryDegree(owner.player());
		owner.moveTo(root.south(2));

		owner.give(QliphothPomeItem.createPickedPomeStack(root.east(40).asLong(), 0, UUID.randomUUID())); // ARRANGE: another player's pome
		Outcome foreign = owner.consume(ItemInit.qliphoth_pome.get());
		foreign.said(POME_NOT_YOURS);
		helper.assertTrue(owner.count(ItemInit.qliphoth_pome.get()) == 1, "another player's pome is not eaten");
		owner.player().getInventory().clearContent();

		owner.give(new ItemStack(ItemInit.qliphoth_pome.get())); // ARRANGE: an unbound pome
		Outcome unbound = owner.consume(ItemInit.qliphoth_pome.get());
		PlaythroughLog.note(helper, "an unbound pome: " + unbound.describe());
		helper.assertTrue(owner.count(ItemInit.qliphoth_pome.get()) == 0 && degree.getTotalPomesConsumed() == 0,
				"an unbound pome is eaten but counts for nothing; total " + degree.getTotalPomesConsumed());
		unbound.said(POME_EAT_PREFIX + "default");

		for (int husk = 0; husk < 9; husk++) {
			ripen(blooms, bloom, husk); // ARRANGE
			PlaythroughRites.emptyHand(owner);
			owner.useOn(root).said(POME_PICKED).gave(ItemInit.qliphoth_pome.get(), 1);
			if (husk == 0) {
				owner.give(QliphothPomeItem.createPickedPomeStack(bloom, 2)); // ARRANGE: a later husk
				PlaythroughRites.wieldStack(owner, s -> huskOf(s) == 2, "the third husk");
				Outcome early = owner.consume(ItemInit.qliphoth_pome.get());
				early.said(POME_OUT_OF_ORDER);
				helper.assertTrue(degree.getPomesConsumedFromBloom(bloom.bloomId()) == 0, "an out-of-order husk is refused");
				removeMatching(owner, s -> huskOf(s) == 2);

				PlaythroughRites.wieldStack(owner, QliphothPomeItem::isBoundPome, "the bound pome");
				Outcome.Probe probe = Outcome.begin(owner, null, "toss the bound pome");
				owner.player().drop(true);
				Outcome tossed = probe.finish();
				tossed.said(POME_RESISTS);
				helper.assertTrue(owner.count(ItemInit.qliphoth_pome.get()) == 1 && tossed.drops().isEmpty(),
						"a bound pome cannot be tossed; " + tossed);

				SimpleContainer chest = new SimpleContainer(27);
				ChestMenu menu = ChestMenu.threeRows(1, owner.player().getInventory(), chest);
				owner.quickMove(menu, menuSlot(owner, QliphothPomeItem::isBoundPome));
				tick(owner, "the pome slips back out of the chest");
				owner.closeMenu();
				helper.assertTrue(owner.count(ItemInit.qliphoth_pome.get()) == 1 && chest.isEmpty(),
						"a bound pome cannot stay in a chest; chest empty=" + chest.isEmpty());

				owner.die();
				owner.respawn();
				helper.assertTrue(owner.count(ItemInit.qliphoth_pome.get()) == 1, "a bound pome comes back after a death; " + owner.last());
				owner.relog();
				helper.assertTrue(owner.count(ItemInit.qliphoth_pome.get()) == 1, "a relog keeps the bound pome");
				degree = HemoCapabilityAccess.getInitiatoryDegree(owner.player());
				owner.moveTo(root.south(2));
			}
			Outcome eaten = owner.consume(ItemInit.qliphoth_pome.get());
			eaten.said(POME_EAT_PREFIX + (husk + 1));
			degree = HemoCapabilityAccess.getInitiatoryDegree(owner.player());
			helper.assertTrue(degree.getPomesConsumedFromBloom(bloom.bloomId()) == husk + 1,
					"husk " + husk + " counts; " + eaten);
			if (husk == 8) {
				PlaythroughLog.note(helper, "the ninth pome: " + eaten.describe() + " HUD " + pomeProgress(eaten));
				eaten.said(NINTH_HUSK).gave(ItemInit.fungal_spine.get(), 1);
				helper.assertTrue(degree.isQliphothCommunionDone() && degree.hasFungalSpineGranted(), "Communion done and the Spine granted");
				helper.assertTrue(owner.owed(QliphothBloomEvents.FUNGAL_SPINE).isEmpty(), "nothing more is owed");
				eaten.whisperQueued(COMMUNION_WHISPER);
				helper.assertTrue(pomeProgress(eaten) == 9, "the HUD is told 9");
			}
		}
		helper.succeed();
	}

	/**
	 * The ninth pome's Spine when it cannot simply land (the first eight recorded and the ninth husk picked, ARRANGE):
	 * a full pack keeps the claim and says so, and a tick after room opens delivers it; a carried Spine settles the
	 * claim without a second one; a full pack then a death keeps the claim through the clone (and while dead) and the
	 * respawn delivers it; a Creative full pack keeps the claim (the discard is not a delivery).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void ninthPomeSpineVariants(GameTestHelper helper) {
		PlaythroughPlayer full = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.archonD7(full); // ARRANGE
		QliphothBloomSavedData.BloomEntry fullBloom = eightEaten(full);
		eatNinthWithFullPack(full, fullBloom).said(NINTH_HUSK).said(SPINE_MAKE_ROOM);
		helper.assertTrue(!full.owed(QliphothBloomEvents.FUNGAL_SPINE).isEmpty() && full.count(ItemInit.fungal_spine.get()) == 0,
				"a full pack keeps the Spine owed");
		removeMatching(full, QliphothPomeItem::isBoundPome);
		tick(full, "a delivery tick with room");
		helper.assertTrue(full.count(ItemInit.fungal_spine.get()) == 1
				&& HemoCapabilityAccess.getInitiatoryDegree(full.player()).hasFungalSpineGranted(), "room opens and the Spine lands");

		PlaythroughPlayer carrying = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 11));
		PlaythroughStates.archonD7(carrying); // ARRANGE
		QliphothBloomSavedData.BloomEntry carriedBloom = eightEaten(carrying);
		carrying.give(new ItemStack(ItemInit.fungal_spine.get())); // ARRANGE: an old Spine already carried
		carrying.give(QliphothPomeItem.createPickedPomeStack(carriedBloom, 8)); // ARRANGE: the ninth husk picked
		carrying.consume(ItemInit.qliphoth_pome.get()).said(NINTH_HUSK);
		helper.assertTrue(carrying.count(ItemInit.fungal_spine.get()) == 1
				&& HemoCapabilityAccess.getInitiatoryDegree(carrying.player()).hasFungalSpineGranted(),
				"a carried Spine settles the claim with no duplicate: " + carrying.count(ItemInit.fungal_spine.get()));

		PlaythroughPlayer dying = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 19));
		PlaythroughStates.archonD7(dying); // ARRANGE
		QliphothBloomSavedData.BloomEntry deathBloom = eightEaten(dying);
		eatNinthWithFullPack(dying, deathBloom).said(SPINE_MAKE_ROOM);
		dying.die();
		tick(dying, "a tick while dead");
		helper.assertTrue(!dying.owed(QliphothBloomEvents.FUNGAL_SPINE).isEmpty(), "a dead player keeps the Spine owed");
		dying.respawn();
		tick(dying, "the first tick after the respawn");
		helper.assertTrue(dying.count(ItemInit.fungal_spine.get()) == 1
				&& HemoCapabilityAccess.getInitiatoryDegree(dying.player()).hasFungalSpineGranted(),
				"the respawn delivers the owed Spine; " + dying.last());

		PlaythroughPlayer creative = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 3));
		PlaythroughStates.archonD7(creative); // ARRANGE
		QliphothBloomSavedData.BloomEntry creativeBloom = eightEaten(creative);
		creative.creative();
		Outcome discard = eatNinthWithFullPack(creative, creativeBloom);
		PlaythroughLog.note(helper, "the ninth pome in Creative with a full pack: " + discard.describe());
		helper.assertTrue(!creative.owed(QliphothBloomEvents.FUNGAL_SPINE).isEmpty()
				&& !HemoCapabilityAccess.getInitiatoryDegree(creative.player()).hasFungalSpineGranted(),
				"a Creative discard is not a delivery: the Spine stays owed");
		creative.survival();
		helper.succeed();
	}

	/**
	 * A lost Spine is reclaimed at the completed living Bloom by sneak-using it empty-handed; while one is carried the
	 * Bloom says so. The "make room" line cannot be reached (an empty main hand is a free slot); that is logged.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BLOOM_RECLAIM)
	public static void spineReclaimAtBloom(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		BlockPos root = PlaythroughSites.stand(helper, 16, 12);
		PlaythroughStates.communedD7(p, root); // ARRANGE: nine eaten from a real Bloom, the Spine delivered
		p.moveTo(root.south(2));
		removeMatching(p, s -> s.is(ItemInit.fungal_spine.get())); // ARRANGE: the Spine is lost
		PlaythroughRites.emptyHand(p);
		p.sneakUseOn(root).said(SPINE_RETURNED).gave(ItemInit.fungal_spine.get(), 1);
		PlaythroughRites.emptyHand(p);
		p.sneakUseOn(root).said(SPINE_CARRIED).gaveNothing();

		removeMatching(p, s -> s.is(ItemInit.fungal_spine.get()));
		p.fillInventory(p.player().getInventory().selected);
		Outcome full = p.sneakUseOn(root);
		PlaythroughLog.note(helper, "a full pack bar the empty hand: " + full.describe() + " (the make-room line "
				+ SPINE_RECLAIM_ROOM + " needs a full pack and an empty hand, which cannot both hold)");
		p.player().getInventory().clearContent();
		p.creative().fillInventory(p.player().getInventory().selected);
		p.player().getInventory().setItem(p.player().getInventory().selected, ItemStack.EMPTY);
		Outcome creative = p.sneakUseOn(root);
		p.survival();
		PlaythroughLog.note(helper, "D-16 (late): the Spine reclaimed in Creative: " + creative.describe());
		helper.succeed();
	}

	// =================================================================================================================
	// The Spine, the projection and the revelation choice
	// =================================================================================================================

	/**
	 * The first Spine use after Communion pays 500 mL through BloodAccounting and starts the projection; with no
	 * Fungal Gardens on this server the next player tick forces the return, which witnesses the revelation, sets the
	 * choice pending and queues the core-witness whisper. Refusals: too little blood, a Spine before the grant, a
	 * player below D7. A Creative use is logged. Degraded: the projection resolves in one tick here.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void firstProjectionDegraded(GameTestHelper helper) {
		ServerLevel gardens = helper.getLevel().getServer().getLevel(FungalGardenTravelHelper.FUNGAL_GARDENS);
		PlaythroughPlayer poor = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.communedD7(poor); // ARRANGE
		PlaythroughStates.blood(poor, 100.0D); // ARRANGE
		Outcome thin = useSpine(poor);
		thin.said(SPINE_THIN);
		helper.assertTrue(thin.bloodSpent() == 0.0D && !FungalGardenTravelHelper.isProjectionActive(poor.player()), "too little blood spends nothing");

		PlaythroughPlayer early = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 11));
		PlaythroughStates.archonD7(early); // ARRANGE
		early.give(new ItemStack(ItemInit.fungal_spine.get())); // ARRANGE: a Spine before Communion granted one
		PlaythroughStates.blood(early, 2000.0D);
		useSpine(early).said(SPINE_WRONG);

		PlaythroughPlayer low = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 19));
		PlaythroughStates.sanctifiedD6(low); // ARRANGE
		low.give(new ItemStack(ItemInit.fungal_spine.get())); // ARRANGE
		PlaythroughStates.blood(low, 2000.0D);
		useSpine(low).said(SPINE_SHAPE);

		PlaythroughPlayer creative = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 3));
		PlaythroughStates.communedD7(creative); // ARRANGE
		creative.creative();
		Outcome creativeUse = useSpine(creative);
		PlaythroughLog.note(helper, "a Creative Spine use: " + creativeUse.describe() + " projection="
				+ FungalGardenTravelHelper.isProjectionActive(creative.player()));
		helper.assertTrue(!FungalGardenTravelHelper.isProjectionActive(creative.player()), "Creative travel starts no projection");
		creative.survival();

		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 11));
		PlaythroughStates.communedD7(p); // ARRANGE
		PlaythroughStates.blood(p, 2000.0D); // ARRANGE
		Outcome start = useSpine(p);
		start.said(PROJECTION_STARTS);
		helper.assertTrue(Math.abs(start.bloodSpent() - FungalGardenTravelHelper.TRAVEL_BLOOD_COST) < 1.0D,
				"the first projection pays 500 mL; " + start);
		if (gardens != null) {
			PlaythroughLog.note(helper, "the Fungal Gardens are loaded here: the projection is real; " + start.describe());
			helper.succeed();
			return;
		}
		PlaythroughNotRun.degraded(helper, "the Fungal Gardens are not loaded: the first projection resolves on the next tick");
		helper.assertTrue(FungalGardenTravelHelper.isProjectionActive(p.player()), "the projection starts");
		Outcome back = tick(p, "the forced return");
		var degree = HemoCapabilityAccess.getInitiatoryDegree(p.player());
		PlaythroughLog.note(helper, "the forced return: " + back.describe());
		helper.assertTrue(!FungalGardenTravelHelper.isProjectionActive(p.player()) && degree.hasWitnessedFungalRevelation()
				&& p.player().getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING),
				"the return witnesses the revelation and sets the choice pending");
		back.whisperQueued(CORE_WITNESS);
		PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.CHOICE_PENDING_D7);
		helper.succeed();
	}

	/**
	 * Communion by play from an owned Bloom (nine husks ripened by ARRANGE, each picked and eaten), the Spine used and
	 * the forced return ticked; then the player takes up "Listen when ready" once. The whisper that opens should be
	 * the revelation choice, the one decision waiting for them. The inbox in order is logged.
	 */
	// DECISION CHAIN-2: after the forced return the revelation-choice whisper can wait behind memo whispers that
	// repeat (in the late run: the Qliphoth Communion memo four times, the whisper-truth memo twice), and "Listen when
	// ready" opens the oldest, so the player clicks through them first. Options: open decision whispers first / fold
	// repeated memo whispers into one / keep.
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "playthrough_d7_whisper_order", required = false)
	public static void revelationWhisperOpensFirst(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.archonD7(p); // ARRANGE
		QliphothBloomSavedData.BloomEntry bloom = PlaythroughSites.qliphothBloom(p, PlaythroughSites.stand(helper, 16, 12)); // ARRANGE: grown by the rite
		eatNineFromBloom(p, bloom);
		PlaythroughStates.blood(p, 2000.0D); // ARRANGE
		useSpine(p).said(PROJECTION_STARTS);
		tick(p, "the forced return");
		ListTag inbox = p.pendingWhispers();
		List<String> order = new ArrayList<>();
		for (int i = 0; i < inbox.size(); i++) order.add(inbox.getCompound(i).toString());
		Outcome listened = p.openWhisper();
		Optional<DialogueView> first = p.whisper();
		boolean choice = first.map(view -> view.offers(CHOICE_SILENCE) || view.offers(CHOICE_EIGHTH)).orElse(false);
		PlaythroughLog.note(helper, "CHAIN-2: the inbox after the forced return holds " + inbox.size() + " whisper(s) "
				+ order + "; the first opened: " + listened.describe() + " showing "
				+ first.map(DialogueView::describe).orElse("nothing"));
		helper.assertTrue(choice, "DECISION CHAIN-2: the first whisper the player opens after the forced return is the "
				+ "revelation choice; the inbox held " + inbox.size() + " whisper(s) and the first showed "
				+ first.map(DialogueView::describe).orElse("nothing"));
		helper.succeed();
	}

	/** The real first projection: two minutes in the Gardens, no hands, no damage, then the forced return. */
	@GameTest(template = EMPTY, timeoutTicks = 2800, batch = CLOCK, required = false)
	public static void firstProjectionInTheGardens(GameTestHelper helper) {
		ServerLevel gardens = PlaythroughNotRun.require(helper, FungalGardenTravelHelper.FUNGAL_GARDENS,
				"the first projection's two minutes, hand lock and damage immunity need the Fungal Gardens",
				"a Fungal Gardens review client", PlaythroughNotRun.HEART_NODE);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.communedD7(p); // ARRANGE
		PlaythroughStates.blood(p, 2000.0D); // ARRANGE
		useSpine(p).said(PROJECTION_STARTS);
		helper.assertTrue(p.player().level() == gardens, "the player is projected into the Gardens");
		Outcome hurt = p.damage(5.0F);
		helper.assertTrue(hurt.notes().contains("not hurt"), "the projected body takes no damage; " + hurt);
		PlaythroughClock.start(helper)
				.step(p.tickPlayer(FungalProjectionRulesTicks.FIRST_VISIT + 2))
				.then(() -> helper.assertTrue(HemoCapabilityAccess.getInitiatoryDegree(p.player()).hasWitnessedFungalRevelation()
						&& !FungalGardenTravelHelper.isProjectionActive(p.player()), "two minutes on, the projection returns"))
				.run();
	}

	/**
	 * The revelation choice from the whisper inbox, reached by the real projection: opened and closed (the decision
	 * stays queued), re-sent at login, refused while busy (a machine, an item in use, combat), then chosen. A forged
	 * choice from a player with no choice pending changes nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void revelationChoice(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		reachTheChoice(p);
		helper.assertTrue(decisionQueued(p), "the core-witness whisper waits in the inbox");

		p.openWhisper();
		DialogueView whisper = p.whisper().orElseThrow();
		whisper.assertLangKeys(helper, "the core-witness whisper").assertNoDeadEnds(helper, "the core-witness whisper");
		helper.assertTrue(whisper.offers(CHOICE_SILENCE) && whisper.offers(CHOICE_EIGHTH), "both choices are offered: " + whisper);
		UUID id = p.openWhisperId().orElseThrow();
		p.closeWhisper(id);
		helper.assertTrue(decisionQueued(p), "closing without choosing keeps the decision queued");

		p.relog();
		PlaythroughLog.note(helper, "after a relog: inbox " + p.pendingWhispers().size() + ", said at login " + p.last().saidKeys());
		helper.assertTrue(decisionQueued(p) && p.last().saidKeys().contains(WHISPER_PENDING),
				"the login re-sends the decision and says a whisper waits; " + p.last());

		p.player().containerMenu = ChestMenu.threeRows(2, p.player().getInventory(), new SimpleContainer(27));
		p.openWhisper().said("hemomancy.whisper.busy.machine");
		p.closeMenu();
		p.give(new ItemStack(ItemInit.blood_projection.get())); // ARRANGE
		p.startHold(ItemInit.blood_projection.get(), p.player().getEyePosition().add(p.player().getLookAngle().scale(4.0D)));
		boolean using = p.player().isUsingItem();
		Outcome busyItem = p.openWhisper();
		p.releaseHold();
		if (using) busyItem.said("hemomancy.whisper.busy.item");
		else PlaythroughLog.note(helper, "Blood Projection did not stay in use with nothing in range; busy.item not checked");
		LivingEntity zombie = EntityType.ZOMBIE.create(helper.getLevel());
		// ARRANGE: a zombie just hit the player. The captured player is never ticked, so its tick count (which the
		// combat check compares against, and which must be positive) is moved on first.
		p.player().tickCount = 200;
		p.player().setLastHurtByMob(zombie);
		p.openWhisper().said("hemomancy.whisper.busy.combat");
		p.player().setLastHurtByMob(null);

		PlaythroughPlayer other = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 4));
		PlaythroughStates.communedD7(other); // ARRANGE: Communion, no revelation yet
		Outcome forged = other.forgeOption(CHOICE_EIGHTH, 0);
		helper.assertTrue(HemoCapabilityAccess.getInitiatoryDegree(other.player()).getArchonPath() == EnumArchonPath.NONE,
				"a forged choice without the pending flag changes nothing; " + forged);

		p.openWhisper();
		Outcome chose = p.chooseWhisper(CHOICE_EIGHTH);
		chose.said("hemomancy.dialogue.event.archon_choice_eighth_degree");
		p.closeWhisper(p.openWhisperId().orElseThrow());
		helper.assertTrue(HemoCapabilityAccess.getInitiatoryDegree(p.player()).getArchonPath() == EnumArchonPath.APOTHEOS_PENDING,
				"the Eighth Degree chosen sets APOTHEOS_PENDING");
		helper.assertTrue(!decisionQueued(p), "the decided whisper leaves the inbox");
		PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.APOTHEOS_PENDING_D7);
		helper.succeed();
	}

	/**
	 * A death between the forced return and the choice: the choice must still be possible. After the respawn the
	 * Spine re-serves the whisper rather than refusing, and Silence chosen from the inbox sets SILENT_PENDING.
	 */
	// FIX: LF-5 the revelation choice survives death (FungalWhisperDialogueEvents, FungalGardenTravelHelper and
	// FungalProjectionEvents.onPlayerLogin treat "witnessed, path NONE, Communion done, nine pomes" as choice pending).
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void revelationChoiceSurvivesDeath(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		reachTheChoice(p);
		BlockPos diedAt = p.player().blockPosition();
		p.die();
		p.respawn();
		p.moveTo(diedAt);
		p.pickUpNear(diedAt, ItemInit.fungal_spine.get()); // the Spine dropped with the rest of the pack
		helper.assertTrue(p.count(ItemInit.fungal_spine.get()) == 1, "the dropped Spine is picked back up; " + p.last());
		PlaythroughStates.blood(p, 2000.0D); // ARRANGE
		Outcome spine = useSpine(p);
		PlaythroughLog.note(helper, "LF-5: the Spine after a death with the choice pending: " + spine.describe()
				+ " inbox decision=" + decisionQueued(p));
		spine.didNotSay(SPINE_WILL_NOT_CARRY);
		p.relog();
		helper.assertTrue(decisionQueued(p), "the choice is still waiting after a death and a relog; inbox " + p.pendingWhispers());
		p.openWhisper();
		Outcome chose = p.chooseWhisper(CHOICE_SILENCE);
		PlaythroughLog.note(helper, "LF-5: Silence chosen after the death: " + chose.describe());
		helper.assertTrue(HemoCapabilityAccess.getInitiatoryDegree(p.player()).getArchonPath() == EnumArchonPath.SILENT_PENDING,
				"Silence chosen after a death sets SILENT_PENDING; " + chose);
		helper.succeed();
	}

	/**
	 * From the ninth pome until D8, casts and rites are refused. Probed for a SILENT_PENDING Archon heading into the
	 * Vesper fight and for a player whose nine pomes came from two Blooms with no Communion; a player with the choice
	 * still pending is logged, and conjure_staff (the documented exception) is logged when known.
	 */
	// DECISION D7-1: HarbingerPathPermissions.isChoiceUnresolved locks every cast but conjure_staff and every rite but
	// the Apotheos from the ninth pome to D8, undocumented, reading a total that adds up across Blooms while Communion
	// is per Bloom. Options: document and warn before the ninth pome / lock only before the revelation / gate on
	// Communion instead of the total.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void powerLockAfterTheNinthPome(GameTestHelper helper) {
		PlaythroughPlayer silent = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 3));
		PlaythroughStates.silentPendingD7(silent); // ARRANGE
		Outcome silentCast = castAbsorption(silent);

		PlaythroughPlayer split = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 11));
		PlaythroughStates.archonD7(split); // ARRANGE
		var degree = HemoCapabilityAccess.getInitiatoryDegree(split.player());
		UUID first = UUID.randomUUID();
		UUID second = UUID.randomUUID();
		for (int husk = 0; husk < 9; husk++) { // ARRANGE: five pomes from one Bloom, four from another
			if (husk < 5) degree.recordPomeConsumed(first);
			else degree.recordPomeConsumed(second);
			degree.incrementTotalPomesConsumed();
		}
		InitiatoryDegreeEvents.syncDegree(split.player(), degree);
		Outcome splitCast = castAbsorption(split);

		PlaythroughPlayer pending = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 3, 19));
		PlaythroughStates.choicePendingD7(pending); // ARRANGE
		Outcome pendingCast = castAbsorption(pending);
		String staff = "conjure_staff";
		String staffCast = "not known";
		if (pending.knows(staff)) {
			PlaythroughRites.emptyHand(pending);
			pending.select(staff);
			staffCast = pending.castSelected().describe();
		}
		PlaythroughLog.note(helper, "D7-1: SILENT_PENDING cast " + silentCast.describe() + "; nine split pomes (communion="
				+ degree.isQliphothCommunionDone() + ") cast " + splitCast.describe() + "; choice pending cast "
				+ pendingCast.describe() + "; conjure_staff " + staffCast);
		helper.assertTrue(!silentCast.saidKeys().contains(POWER_LOCKED), "D7-1 expected: a SILENT_PENDING Archon can still cast");
		helper.assertTrue(!splitCast.saidKeys().contains(POWER_LOCKED), "D7-1 expected: nine pomes without Communion do not lock casting");
		helper.succeed();
	}

	/** Logging out mid-projection: a player would expect the ordeal to wait, not to be counted as witnessed. */
	// DECISION D7-4: FungalProjectionEvents.onPlayerLogout runs performForcedProjectionReturn, so a relog completes the
	// first projection and grants the revelation without the two minutes (the D-24 analogue); with no Gardens the
	// 500 mL is also spent for a projection that never happens. Options: pause and resume the projection / refuse
	// before spending when the Gardens are missing / keep.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void logoutCompletesProjection(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.communedD7(p); // ARRANGE
		PlaythroughStates.blood(p, 2000.0D); // ARRANGE
		useSpine(p).said(PROJECTION_STARTS);
		p.relog();
		var degree = HemoCapabilityAccess.getInitiatoryDegree(p.player());
		PlaythroughLog.note(helper, "D7-4: relog mid-projection: witnessed=" + degree.hasWitnessedFungalRevelation()
				+ " projection=" + FungalGardenTravelHelper.isProjectionActive(p.player()) + " choicePending="
				+ p.player().getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING));
		helper.assertTrue(!degree.hasWitnessedFungalRevelation(),
				"D7-4 expected: a logout mid-projection does not count as witnessing the revelation");
		helper.succeed();
	}

	/**
	 * What a fresh Archon hears about the next step: every reachable line of the D7 Vicar, Alchemist, Mnemonist and
	 * Artificer and of the Monolith's root and kinship nodes (its press_again warning excepted), read in English. A
	 * player would expect one of them to name the shatter, the Seed or the Bloom.
	 */
	// DECISION D7-5: no D7 NPC names the Monolith shatter, the Qliphoth Seed or the Bloom; the only hint is the
	// Monolith's "press further" option and the Vicar's "I do not know what the Monolith has shown you". Options: a
	// Vicar or Monolith line naming the shatter / leave it to discovery and the Liber.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void noPointerToTheShatter(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.archonD7(p); // ARRANGE
		Map<String, DialogueView> views = new LinkedHashMap<>();
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 10, 18));
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 14, 18));
		HarbingerMnemonistEntity mnemonist = PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 18, 18));
		HarbingerArtificerEntity artificer = PlaythroughNpcs.artificer(helper, PlaythroughSites.stand(helper, 22, 18));
		for (Entity npc : List.<Entity>of(vicar, alchemist, mnemonist, artificer)) {
			p.moveTo(npc.blockPosition().south(2));
			views.put(npc.getType().getDescriptionId(), p.approach(npc).talkTo(npc));
		}
		BlockPos stone = PlaythroughSites.monolith(p, PlaythroughSites.stand(helper, 16, 10)); // ARRANGE
		p.moveTo(stone.south(2));
		views.put("monolith", p.talkToBlock(stone));
		List<String> pointers = new ArrayList<>();
		for (var entry : views.entrySet()) {
			for (var node : entry.getValue().nodes().values()) {
				if (entry.getKey().equals("monolith") && node.id().equals("press_again")) continue;
				for (String line : node.lines()) {
					String key = Dialogue.lineKey(line).orElse(line);
					String text = english(key).toLowerCase(Locale.ROOT);
					if (text.contains("shatter") || text.contains("seed") || text.contains("bloom") || text.contains("qliphoth")) {
						pointers.add(entry.getKey() + "/" + node.id() + ": " + key);
					}
				}
			}
		}
		PlaythroughLog.note(helper, "D7-5: D7 lines naming the shatter, Seed or Bloom: " + pointers + "; Vicar degree_hint="
				+ views.get(vicar.getType().getDescriptionId()).hasNode("degree_hint"));
		helper.assertTrue(!pointers.isEmpty(), "D7-5 expected: some D7 speaker points at the shatter, the Seed or the Bloom");
		helper.succeed();
	}

	/**
	 * The atlas rules for the Monolith fragments, the imbued cloth and the four hallowed residua, read for a D5 player
	 * and against each degree's gate. Fragments exist only after the D7 shatter and residuum only from the D8 Saints,
	 * so a player would expect them hidden at D5.
	 */
	// DECISION D7-6: MaterialsData unlocks monolith_fragment, monolith_imbued_cloth and the four hallowed_residuum
	// entries at D5 (MaterialAtlasSpec traces them from blood_trial_altar), against the D7 shatter and the 2026-10-27
	// Saint ruling (Saints D8). Options: move them to D7/D8 with Monolith/Saint traces / keep as previews.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void atlasFragmentsAndResiduum(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		Map<String, Integer> expected = new LinkedHashMap<>();
		expected.put("monolith_fragment", 7);
		expected.put("monolith_imbued_cloth", 7);
		for (String saint : List.of("hemorath", "putriciel", "seraphae", "velorum")) expected.put("hallowed_residuum_" + saint, 8);
		expected.put("qliphoth_seed", 7);
		List<String> early = new ArrayList<>();
		for (var want : expected.entrySet()) {
			MaterialEntry entry = MaterialsData.getBloodEntries().stream().filter(e -> e.name().equals(want.getKey())).findFirst().orElse(null);
			if (entry == null) {
				PlaythroughLog.note(helper, "D7-6: no atlas entry " + want.getKey());
				continue;
			}
			var gate = MaterialAtlasSpec.entryFor(MaterialAtlasPath.HARBINGER, entry).gate();
			List<String> byDegree = new ArrayList<>();
			for (int d = 4; d <= 8; d++) byDegree.add(d + "=" + gate.visibilityFor(MaterialAtlasPath.HARBINGER, d, 0.0F, 0.0F));
			boolean unlockedAtD5 = entry.unlockPredicate().isUnlocked(p.player());
			PlaythroughLog.note(helper, "D7-6: " + want.getKey() + " unlocked for a D5 player=" + unlockedAtD5 + " gate " + byDegree
					+ " (obtainable from D" + want.getValue() + ")");
			if (unlockedAtD5 && want.getValue() > 5) early.add(want.getKey());
		}
		helper.assertTrue(early.isEmpty(), "D7-6 expected: nothing a D5 player cannot yet obtain is unlocked in the atlas: " + early);
		helper.succeed();
	}

	// =================================================================================================================
	// Helpers
	// =================================================================================================================

	/** The first projection's length, mirrored so the NOT RUN test needs no extra import if the rules class moves. */
	private static final class FungalProjectionRulesTicks {
		static final int FIRST_VISIT = com.vincenthuto.hemomancy.common.encounter.FungalProjectionRules.FIRST_VISIT_TICKS;
	}

	/** ARRANGE: a deep pool and the tools a late Harbinger carries: Living Staff, Projection, Absorption, a sword. */
	private static void readyForRites(PlaythroughPlayer p) {
		PlaythroughStates.blood(p, 1.0E7D);
		for (Item tool : List.of(ItemInit.living_staff.get(), ItemInit.blood_projection.get(), ItemInit.blood_absorption.get(),
				Items.NETHERITE_SWORD)) {
			if (p.count(tool) == 0) p.give(new ItemStack(tool));
		}
	}

	/** ARRANGE: an armor upgrade done at a consecrated Armature (the personal eligibility the D7 Armature tier reads). */
	private static void armatureWorkDone(PlaythroughPlayer p) {
		var progress = HemoCapabilityAccess.stationUpgrades(p.player());
		progress.recordUse(UpgradeStation.ARMATURE, StationUpgradeCatalog.ARMOR_UPGRADE);
		progress.recordUse(UpgradeStation.ARMATURE, StationUpgradeCatalog.ARMOR_UPGRADE_CONSECRATED);
	}

	/** The Anchor helper station of the floor's rite: above the Focus, at the role marker. */
	private static BlockPos station(PlaythroughSites.RiteFloor floor) {
		return floor.focus().offset(CardinalRiteAllyService.markers(floor.recipe()).get(CardinalRiteAllyRole.ANCHOR));
	}

	@Nullable
	private static ActiveCardinalRite rite(PlaythroughPlayer p) {
		return CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid());
	}

	/**
	 * The standard plan with what late rites need: fight threats with the sword, top the pool up when it runs low
	 * (ARRANGE: the player drinks between strokes; counted in the log), the {@code inscription} hook during
	 * INSCRIPTION (helper assignment) and {@code afterSeal} once the altar is sealed.
	 */
	private static RitePlan ritePlan(PlaythroughPlayer p, @Nullable BiConsumer<ActiveCardinalRite, Integer> inscription,
			@Nullable Runnable afterSeal) {
		int[] refills = new int[1];
		RitePlan plan = RitePlan.standard().fightThreats().weapon(Items.NETHERITE_SWORD);
		for (CardinalRitePhase phase : RITE_PHASES) {
			plan = plan.onPhase(phase, (rite, ticks) -> {
				if (p.blood() < 1500.0D) {
					PlaythroughStates.blood(p, 1.0E7D);
					if (refills[0]++ == 0 || refills[0] % 10 == 0) {
						PlaythroughLog.note(p.helper(), "ARRANGE refilled the pool mid-rite (" + refills[0] + " times, phase " + phase + ")");
					}
				}
				if (phase == CardinalRitePhase.INSCRIPTION && inscription != null) inscription.accept(rite, ticks);
				if (afterSeal != null && AFTER_SEAL.contains(phase)) afterSeal.run();
			});
		}
		return plan;
	}

	/** At INSCRIPTION the caster right-clicks the recruit with an empty main hand (EntityInteract), retried every 40 ticks. */
	private static BiConsumer<ActiveCardinalRite, Integer> assignNpc(PlaythroughPlayer p, Entity npc) {
		int[] tries = new int[1];
		return (rite, ticks) -> {
			if (rite.getAllyRoles().containsKey(npc.getUUID()) || ticks % 40 != 0 || tries[0] >= 6) return;
			tries[0]++;
			// As the D6 band does: let go of Projection, right-click with an empty main hand, then take it up again.
			// (An empty off hand did not reach the assignment: the EntityInteract stack check never passed.)
			boolean projecting = p.player().isUsingItem();
			if (projecting) p.releaseHold();
			PlaythroughRites.emptyHand(p);
			Outcome assigned = p.interactOn(npc);
			if (projecting) p.startHold(ItemInit.blood_projection.get(), npc.getEyePosition());
			PlaythroughLog.note(p.helper(), "assign " + PlaythroughPlayer.describe(npc) + " at INSCRIPTION: " + assigned.describe()
					+ " roles " + rite.getAllyRoles());
			if (tries[0] == 1 && !assigned.saidKeys().contains(ASSIGNED_AS)) {
				PlaythroughLog.note(p.helper(), "the recruit was not assigned on the first right-click");
			}
		};
	}

	/**
	 * {@link #assignNpc}, then watches the assigned recruit: if he has not taken his station 200 ticks later, where he
	 * stands is logged and he is set on it (ARRANGE: the walk from the summon spot to the station).
	 */
	private static BiConsumer<ActiveCardinalRite, Integer> assignAndWatch(PlaythroughPlayer p, Entity npc, BlockPos station) {
		BiConsumer<ActiveCardinalRite, Integer> assign = assignNpc(p, npc);
		int[] assignedAt = {-1};
		return (rite, ticks) -> {
			assign.accept(rite, ticks);
			if (!rite.getAllyRoles().containsKey(npc.getUUID())) return;
			if (assignedAt[0] < 0) assignedAt[0] = ticks;
			if (ticks - assignedAt[0] != 200) return;
			ServerLevel level = p.player().serverLevel();
			if (CardinalRiteAllyService.isAvailable(level, rite, npc.getUUID())) {
				PlaythroughLog.note(p.helper(), "the summoned recruit reached his station on his own");
				return;
			}
			PlaythroughLog.note(p.helper(), "the summoned recruit has not taken his station 200 ticks after assignment: at "
					+ npc.position() + ", station " + station.toShortString() + ", below " + level.getBlockState(station.below())
					+ ", at " + level.getBlockState(station) + "; ARRANGE sets him on it");
			npc.teleportTo(station.getX() + 0.5D, station.getY(), station.getZ() + 0.5D); // ARRANGE: the walk to the station
		};
	}

	/** At INSCRIPTION a line member right-clicks the station's floor with an empty hand, retried every 40 ticks. */
	private static BiConsumer<ActiveCardinalRite, Integer> claimStation(PlaythroughPlayer member, BlockPos station) {
		int[] tries = new int[1];
		return (rite, ticks) -> {
			if (rite.getAllyRoles().containsKey(member.uuid()) || ticks % 40 != 0 || tries[0] >= 6) return;
			tries[0]++;
			member.moveTo(station);
			PlaythroughRites.emptyHand(member);
			Outcome claimed = member.useOn(station.below());
			PlaythroughLog.note(member.helper(), "the member claims the Anchor station: " + claimed.describe() + " took="
					+ claimed.saidKeys().contains(TAKE_THE_STATION) + " roles " + rite.getAllyRoles());
		};
	}

	/**
	 * The extra aim for a rite with demanded sigils: the required support sigils during INSCRIPTION
	 * ({@link PlaythroughRites#supportSigilNodes}), then during an ORDEAL wave whose objective is to trace, the next
	 * unpaid node of the demanded sigil, placed as {@code CardinalRiteInteractionHandler.resolveWaveSigilPlacement}
	 * places it (that method is package-private, so its placement is worked out here the same way).
	 */
	static BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> sigilsAndTraces(PlaythroughPlayer p) {
		BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> support = PlaythroughRites.supportSigilNodes(p);
		return (rite, recipe) -> {
			Vec3 aim = support.apply(rite, recipe);
			if (aim != null) return aim;
			if (recipe == null || recipe.getCeremony() == null || rite.getPhase() != CardinalRitePhase.ORDEAL) return null;
			return traceAim(p, rite, recipe);
		};
	}

	/** The next unpaid node of the sigil an ORDEAL wave demands, or null when the wave demands none. */
	@Nullable
	private static Vec3 traceAim(PlaythroughPlayer p, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (rite.getCurrentWave() >= rite.getWaveDeck().size()) return null;
		String wave = rite.getWaveDeck().get(rite.getCurrentWave());
		if (CardinalRiteWaveRules.objective(wave) != CardinalRiteWaveRules.Objective.TRACE) return null;
		ResourceLocation id = CardinalRiteInteractionHandler.sigilForWave(rite);
		IchorianSigilDefinition sigil = id == null ? null : IchorianSigilRegistry.get(id);
		if (sigil == null || sigil.nodes().isEmpty()) return null;
		Set<BlockPos> supportOccupied = new HashSet<>();
		Set<BlockPos> targets = new HashSet<>();
		for (var anchor : recipe.getCeremony().anchors()) {
			supportOccupied.add(new BlockPos(anchor.x(), 0, anchor.z()));
			targets.add(new BlockPos(anchor.x(), 0, anchor.z()));
		}
		BlockPos discovered = null;
		for (var socket : recipe.getCeremony().supportSockets()) {
			ResourceLocation socketId = Hemomancy.rloc(socket.suggestedSigil());
			IchorianSigilDefinition definition = IchorianSigilRegistry.get(socketId);
			BlockPos requested = new BlockPos(socket.x(), 0, socket.z());
			if (definition == null) continue;
			BlockPos resolved = CardinalRiteSigilPlacementRules.resolveSupportPlacement(requested, definition.nodes(), supportOccupied);
			supportOccupied.addAll(CardinalRiteSigilPlacementRules.footprint(resolved, definition.nodes()));
			targets.addAll(CardinalRiteSigilPlacementRules.footprint(resolved, definition.nodes()));
			if (wave.startsWith("discover_") && socketId.equals(id) && discovered == null) discovered = resolved;
		}
		BlockPos placement = discovered != null ? discovered
				: CardinalRiteSigilPlacementRules.resolveNearestPlacement(BlockPos.ZERO, sigil.nodes(), targets);
		String progressKey = wave.startsWith("discover_") ? id.toString()
				: CardinalRiteSigilRules.responseProgressKey(rite.getCurrentWave(), id);
		int paid = rite.getSigilProgress().getOrDefault(progressKey, 0);
		if (paid >= sigil.nodes().size()) return null;
		IchorianSigilDefinition.Node node = sigil.nodes().get(paid);
		BlockPos surface = CardinalRiteSigilRules.surfaceAirPosition(p.player().serverLevel(), rite.getCenterPos(),
				placement.getX() + (int) Math.round(node.x()), placement.getZ() + (int) Math.round(node.z()));
		return CardinalRiteTargetGeometry.sigilAimPoint(rite.getCenterPos(), surface, placement.getX(), placement.getZ(),
				node.x(), node.z());
	}

	/** {@code extraAim} for a station-upgrade rite: the open circuit target during STATION_PROJECTION, else the sigils. */
	private static BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> stationCircuits(PlaythroughPlayer p) {
		BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> sigils = PlaythroughRites.supportSigilNodes(p);
		return (rite, recipe) -> rite.getPhase() == CardinalRitePhase.STATION_PROJECTION
				? StationUpgradeRites.targetSurface(p.player().serverLevel(), rite, rite.upgrade().stage())
				: sigils.apply(rite, recipe);
	}

	/** Whether any line said since {@code mark} carried one of {@code keys}. */
	private static boolean saidAny(PlaythroughPlayer p, int mark, String... keys) {
		for (Outcome.ChatLine line : p.chatSince(mark)) for (String key : keys) if (line.has(key)) return true;
		return false;
	}

	/** Every key said since {@code mark} whose name contains one of {@code fragments}. */
	private static List<String> keysSince(PlaythroughPlayer p, int mark, String... fragments) {
		List<String> keys = new ArrayList<>();
		for (Outcome.ChatLine line : p.chatSince(mark)) {
			for (String key : line.keys()) {
				for (String fragment : fragments) {
					if (key.contains(fragment) && !keys.contains(key)) keys.add(key);
				}
			}
		}
		return keys;
	}

	/** One server tick of the player (the PendingDelivery driver included), as an outcome. */
	private static Outcome tick(PlaythroughPlayer p, String label) {
		Outcome.Probe probe = Outcome.begin(p, null, label);
		p.tickOnce();
		return probe.finish();
	}

	/** Uses the carried Fungal Spine in the air. */
	private static Outcome useSpine(PlaythroughPlayer p) {
		p.wield(ItemInit.fungal_spine.get());
		return p.use(InteractionHand.MAIN_HAND);
	}

	/** ARRANGE then play: Communion (no Bloom) and 2000 mL, the Spine used, the forced return ticked: the choice is pending. */
	private static void reachTheChoice(PlaythroughPlayer p) {
		PlaythroughStates.communedD7(p); // ARRANGE
		PlaythroughStates.blood(p, 2000.0D); // ARRANGE
		useSpine(p).said(PROJECTION_STARTS);
		tick(p, "the forced return");
		p.helper().assertTrue(p.player().getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING),
				"the forced return sets the choice pending");
	}

	/** Whether the inbox holds an Archon decision entry. */
	private static boolean decisionQueued(PlaythroughPlayer p) {
		ListTag inbox = p.pendingWhispers();
		for (int i = 0; i < inbox.size(); i++) if (inbox.getCompound(i).getBoolean("decision")) return true;
		return false;
	}

	/** Whether the inbox holds a whisper showing {@code key}. */
	private static boolean hasQueued(PlaythroughPlayer p, String key) {
		ListTag inbox = p.pendingWhispers();
		for (int i = 0; i < inbox.size(); i++) {
			byte[] bytes = inbox.getCompound(i).getByteArray("tree");
			var buffer = new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.wrappedBuffer(bytes));
			try {
				var tree = com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree.fromNetwork(buffer);
				if (new DialogueView(tree).lineKeys().contains(key)) return true;
			} catch (RuntimeException ignored) {
				// an entry this harness cannot read does not count
			} finally {
				buffer.release();
			}
		}
		return false;
	}

	/** Selects Blood Absorption and casts it with an empty hand and a full pool. */
	private static Outcome castAbsorption(PlaythroughPlayer p) {
		PlaythroughStates.blood(p, 3000.0D); // ARRANGE
		p.select(ManipulationInit.blood_absorption.get().getName());
		PlaythroughRites.emptyHand(p);
		return p.castSelected();
	}

	private static QliphothBloomSavedData blooms(GameTestHelper helper) {
		return QliphothBloomSavedData.get(helper.getLevel().getServer().overworld());
	}

	private static String dimension(GameTestHelper helper) {
		return helper.getLevel().dimension().location().toString();
	}

	/** ARRANGE: the next husk ripens on {@code bloom}, as the Bloom tick does at random. */
	private static void ripen(QliphothBloomSavedData blooms, QliphothBloomSavedData.BloomEntry bloom, int husk) {
		blooms.incrementPomesDropped(bloom);
		blooms.setPendingPome(bloom, husk);
	}

	/** ARRANGE: takes a Bloom (blocks, entry, sync) down mid-test. */
	private static void removeBloom(GameTestHelper helper, QliphothBloomSavedData.BloomEntry bloom) {
		ServerLevel level = helper.getLevel();
		for (int up = 0; up <= 7; up++) {
			BlockState state = level.getBlockState(bloom.center().above(up));
			if (state.is(BlockInit.qliphoth_bloom.get()) || state.is(BlockInit.filler_block.get())) {
				level.setBlock(bloom.center().above(up), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			}
		}
		blooms(helper).removeBloomInChunk(bloom.center(), bloom.dimension());
		HarbingerCardinalRiteEvents.syncQliphothBlooms(level.getServer());
	}

	/** Picks (empty hand) and eats each of the Bloom's nine husks in order, ripening each first (ARRANGE). */
	private static void eatNineFromBloom(PlaythroughPlayer p, QliphothBloomSavedData.BloomEntry bloom) {
		QliphothBloomSavedData blooms = blooms(p.helper());
		p.moveTo(bloom.center().south(2));
		for (int husk = 0; husk < 9; husk++) {
			ripen(blooms, bloom, husk); // ARRANGE
			PlaythroughRites.emptyHand(p);
			p.useOn(bloom.center()).said(POME_PICKED);
			p.consume(ItemInit.qliphoth_pome.get()).said(POME_EAT_PREFIX + (husk + 1));
		}
		p.helper().assertTrue(HemoCapabilityAccess.getInitiatoryDegree(p.player()).isQliphothCommunionDone()
				&& p.count(ItemInit.fungal_spine.get()) == 1, "nine pomes complete Communion and deliver the Spine");
	}

	/**
	 * ARRANGE: eight pomes recorded against a Bloom that has no block (so the test needs no own batch), as
	 * {@code PlaythroughStates.communedD7} records them; returns that Bloom's entry (never registered).
	 */
	private static QliphothBloomSavedData.BloomEntry eightEaten(PlaythroughPlayer p) {
		var bloom = new QliphothBloomSavedData.BloomEntry(p.uuid(), p.home().east(40), dimension(p.helper()), 3,
				p.helper().getLevel().getGameTime());
		var degree = HemoCapabilityAccess.getInitiatoryDegree(p.player());
		for (int husk = 0; husk < 8; husk++) {
			degree.recordPomeConsumed(bloom.bloomId());
			degree.incrementTotalPomesConsumed();
		}
		InitiatoryDegreeEvents.syncDegree(p.player(), degree);
		return bloom;
	}

	/**
	 * ARRANGE then play: the ninth husk picked as a stack of two (so its slot stays taken once one is eaten), every
	 * other slot filled, then one eaten for real.
	 */
	private static Outcome eatNinthWithFullPack(PlaythroughPlayer p, QliphothBloomSavedData.BloomEntry bloom) {
		p.player().getInventory().clearContent();
		p.player().getInventory().setItem(0, QliphothPomeItem.createPickedPomeStack(bloom, 8).copyWithCount(2)); // ARRANGE
		p.player().getInventory().selected = 0;
		p.fillInventory(0);
		return p.consume(ItemInit.qliphoth_pome.get());
	}

	private static int huskOf(ItemStack stack) {
		if (!stack.is(ItemInit.qliphoth_pome.get())) return -1;
		var tag = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
				net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
		return tag.contains(QliphothPomeItem.HUSK_INDEX_KEY) ? tag.getInt(QliphothPomeItem.HUSK_INDEX_KEY) : -1;
	}

	private static ItemStack firstPome(PlaythroughPlayer p) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (inventory.getItem(slot).is(ItemInit.qliphoth_pome.get())) return inventory.getItem(slot);
		}
		return ItemStack.EMPTY;
	}

	/** ARRANGE: removes every carried stack matching {@code what} (lost, eaten elsewhere, stored). */
	private static void removeMatching(PlaythroughPlayer p, java.util.function.Predicate<ItemStack> what) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (what.test(inventory.getItem(slot))) inventory.setItem(slot, ItemStack.EMPTY);
		}
	}

	/** The three-row chest menu slot that shows the player's first stack matching {@code what}. */
	private static int menuSlot(PlaythroughPlayer p, java.util.function.Predicate<ItemStack> what) {
		var items = p.player().getInventory().items;
		for (int slot = 0; slot < items.size(); slot++) {
			if (what.test(items.get(slot))) return slot < 9 ? 54 + slot : 27 + slot - 9;
		}
		p.helper().fail("the player carries no matching stack for the chest");
		return -1;
	}

	/** The pome count the last PacketSyncPomeProgress of {@code outcome} told the HUD, or -1. */
	private static int pomeProgress(Outcome outcome) {
		List<PacketSyncPomeProgress> sent = outcome.sent(PacketSyncPomeProgress.class);
		if (sent.isEmpty()) return -1;
		try {
			Field field = PacketSyncPomeProgress.class.getDeclaredField("progress");
			field.setAccessible(true);
			return field.getInt(sent.getLast());
		} catch (ReflectiveOperationException exception) {
			return -1;
		}
	}

	/** Qliphoth Seeds lying within three blocks of {@code pos}. */
	private static int seedsNear(GameTestHelper helper, BlockPos pos) {
		int seeds = 0;
		for (net.minecraft.world.entity.item.ItemEntity drop : helper.getLevel().getEntitiesOfClass(
				net.minecraft.world.entity.item.ItemEntity.class, new net.minecraft.world.phys.AABB(pos).inflate(3.0D),
				drop -> drop.getItem().is(ItemInit.qliphoth_seed.get()))) {
			seeds += drop.getItem().getCount();
		}
		return seeds;
	}

	private static boolean monolithAt(PlaythroughPlayer p, BlockPos pos) {
		return p.player().serverLevel().getBlockEntity(pos) instanceof SanguineMonolithBlockEntity;
	}

	private static boolean focusHoldsMedium(GameTestHelper helper, PlaythroughSites.RiteFloor floor) {
		return helper.getLevel().getBlockEntity(floor.focus()) instanceof CardinalFocusBlockEntity focus && focus.hasMedium();
	}

	private static HematicArmatureBlockEntity armature(GameTestHelper helper, BlockPos pos) {
		if (helper.getLevel().getBlockEntity(pos) instanceof HematicArmatureBlockEntity armature) return armature;
		helper.fail("no Hematic Armature at " + pos.toShortString());
		return null;
	}

	/** ARRANGE: turns the Armature at {@code pos} to face north (toward the Focus), keeping its block entity. */
	private static void faceNorth(GameTestHelper helper, BlockPos pos) {
		BlockState state = helper.getLevel().getBlockState(pos);
		if (state.hasProperty(HematicArmatureBlock.FACING) && state.getValue(HematicArmatureBlock.FACING) != Direction.NORTH) {
			helper.getLevel().setBlock(pos, state.setValue(HematicArmatureBlock.FACING, Direction.NORTH), Block.UPDATE_ALL);
			PlaythroughLog.note(helper, "ARRANGE turned the Armature to face the Focus (it was placed facing "
					+ state.getValue(HematicArmatureBlock.FACING) + ")");
		}
	}

	/** How many {@code monolith_fragment} slots the crafting recipe {@code hemomancy:<path>} takes. */
	private static int fragmentsIn(GameTestHelper helper, String path) {
		return countIngredients(helper, path, ItemInit.monolith_fragment.get());
	}

	private static int clothIn(GameTestHelper helper, String path) {
		return countIngredients(helper, path, ItemInit.monolith_imbued_cloth.get());
	}

	private static int countIngredients(GameTestHelper helper, String path, Item item) {
		var recipe = helper.getLevel().getRecipeManager().byKey(Hemomancy.rloc(path));
		helper.assertTrue(recipe.isPresent(), "the recipe hemomancy:" + path + " is loaded");
		int count = 0;
		for (Ingredient ingredient : recipe.get().value().getIngredients()) if (ingredient.test(new ItemStack(item))) count++;
		return count;
	}

	/** The English the server has for {@code key} (the key itself when it has none). */
	private static String english(String key) {
		return Language.getInstance().getOrDefault(key);
	}

	private static void grant(PlaythroughPlayer p, ResourceLocation... ids) {
		for (ResourceLocation id : ids) HarbingerAdvancementGranter.grantIfNotDone(p.player(), id);
	}
}
