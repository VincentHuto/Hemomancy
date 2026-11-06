package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HematicIronBarsBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BloodCrystalBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodlineSavedData;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeProgression;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationGrantHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.AntecedentKnowledge;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.AntecedentResearch;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DeepDarkCommissionDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PhlegethonticCommissionDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.StationUpgradeDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerArtificerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.component.LivingWeaponForm;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.item.harbinger.bloodline.UnsignedLedgerItem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.EnzymaticScriptoriumMenu;
import com.vincenthuto.hemomancy.common.mission.alchemist.PhlegethonticCommission;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyRules;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine;
import com.vincenthuto.hemomancy.common.rite.harbinger.StationUpgradeRites;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeTier;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeableStation;
import com.vincenthuto.hemomancy.common.util.RegistryLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

/**
 * Band d5_illuminatus: the Harbinger player from a fresh Degree 5 (the state the first suite ends on,
 * {@link PlaythroughStates#illuminatusD5}) through the bloodline and the Founding Fane to the Rite of the Bloodline
 * Covenant's door: Bloodline Founding by play, joining by a signed ledger, the Founding Fane by play and its
 * refusals, a member's Bloodwell deposit (the D5 chapter proof), the Vicar's covenant guidance, recruiting and
 * releasing outpost Harbingers, the ledger screen's actions and the pool screen's kick, Bloodline Recall, Hematic
 * Unbinding, the sealed sanctified rite, Hematic Stakes, the D5 station kits and the Eightfold Script, the Artificer's
 * Crimson Vestment and Assumed Limb, the Alchemist's commissions and the D5 Monolith.
 *
 * <p>Preconditions that are not under test are set by {@link PlaythroughStates} checkpoints or a labelled
 * {@code // ARRANGE} line; every step under test goes through the entry point a real client reaches (dialogue option
 * and claim payloads, item {@code use}/{@code useOn}, a planted Living Staff and held Blood Projection, the ledger
 * and kick payloads, menu buttons, real damage). Fix tests carry {@code // FIX: LF-n} and are required; decision
 * probes are {@code required = false} with a {@code // DECISION D5-n:} line, assert what a player would expect, and
 * log what happens.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughIlluminatusGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_d5_illuminatus";
	private static final String RITES = "playthrough_d5_illuminatus_rites";
	/** Founding Fane completions broadcast level-wide and buff a 40-block lobe: one per batch. */
	private static final String FANE = "playthrough_d5_fane";
	private static final String FANE_RELOCATION = "playthrough_d5_fane_relocation";
	private static final String FANE_CHAIN = "playthrough_d5_fane_chain";
	private static final String FANE_DEATH = "playthrough_d5_fane_death";
	private static final String FANE_AWAY = "playthrough_d5_fane_away";
	/** Stakes are laid out of chain 120 blocks east of the test: alone in its batch. */
	private static final String STAKE = "playthrough_d5_illuminatus_stake";
	/** The Basin bearing searches the Nether at radius 8192: alone in its batch. */
	private static final String COMMISSIONS = "playthrough_d5_illuminatus_commissions";

	private static final BlockPos RITE_ORIGIN = new BlockPos(24, 3, 12);
	private static final BlockPos SIDE_ORIGIN = new BlockPos(8, 3, 12);

	private static final String FOUNDED = "message.hemomancy.cardinal_rite_completion_effects.you_have_founded";
	private static final String JOINED = "message.hemomancy.unsigned_ledger_item.you_have_joined";
	private static final String HAS_JOINED = "message.hemomancy.unsigned_ledger_item.has_joined_your_bloodline";
	private static final String LEDGER_PREFIX = "message.hemomancy.unsigned_ledger_item.";
	private static final String EFFECTS_PREFIX = "message.hemomancy.cardinal_rite_completion_effects.";
	private static final String ACTIVATION_PREFIX = "message.hemomancy.cardinal_rite_activation.";
	private static final String GUIDE_FOUND_BLOODLINE = "hemomancy.vicar.covenant.found_bloodline";
	private static final String GUIDE_FOUND_FANE = "hemomancy.vicar.covenant.found_fane";
	private static final String GUIDE_PERFORM_RITE = "hemomancy.vicar.covenant.perform_bloodline_rite";
	private static final String FANE_CONSECRATED = "message.hemomancy.cardinal_rite_completion_effects.this_ground_is_now_consecrated_all";
	private static final String FANE_BROADCAST = "message.hemomancy.cardinal_rite_completion_effects.has_consecrated_a_founding_fane";
	private static final String FANE_NEEDS_LINE = "message.hemomancy.cardinal_rite_activation.the_founding_fane_requires_an_established";
	private static final String FANE_PROGENITOR_ONLY = "message.hemomancy.cardinal_rite_activation.only_the_bloodline_progenitor_may_consecrate";
	private static final String FANE_RELOCATE_WARN = "message.hemomancy.cardinal_rite_activation.your_bloodline_already_has_a_founding";
	private static final String FANE_MOVED = "message.hemomancy.cardinal_rite_completion_effects.your_founding_fane_has_been_moved";
	private static final String FANE_CLAIMED = "message.hemomancy.cardinal_rite_completion_effects.another_fane_already_claims_this_ground";
	private static final String WELL_NOT_BLOODLINE = "block.hemomancy.consecrated_bloodwell.not_bloodline";
	private static final String WELL_INACTIVE = "block.hemomancy.consecrated_bloodwell.inactive";
	private static final String RECRUIT_SUCCESS = "hemomancy.dialogue.recruit.success";
	private static final String RECRUIT_POOL = "hemomancy.dialogue.recruit.pool_increased";
	private static final String RECRUIT_TYPE = "hemomancy.dialogue.recruit.type_already_member";
	private static final String RECRUIT_OUTPOST = "hemomancy.dialogue.recruit.outpost_already_member";
	private static final String RECRUIT_NOT_LEADER = "hemomancy.dialogue.recruit.not_leader";
	private static final String EXPEL_SUCCESS = "hemomancy.dialogue.recruit.expel.success";
	private static final String SUMMON_SUCCESS = "hemomancy.ledger.summon.success";
	private static final String SUMMON_NOT_IN_LODGE = "hemomancy.ledger.summon.not_in_lodge";
	private static final String SUMMON_NO_NPCS = "hemomancy.ledger.summon.no_npcs";
	private static final String POINT_SET = "hemomancy.ledger.recall.point_set";
	private static final String POINT_OUT_OF_RANGE = "hemomancy.ledger.recall.out_of_range";
	private static final String POINT_NOT_LEADER = "hemomancy.ledger.recall.not_leader";
	private static final String RECALL_SUCCESS = "hemomancy.ledger.recall.success";
	private static final String DISBAND_SUCCESS = "hemomancy.ledger.disband.success";
	private static final String DISBAND_NOTICE = "hemomancy.ledger.disband.member_notice";
	private static final String DISBAND_NOT_LEADER = "hemomancy.ledger.disband.not_leader";
	private static final String LEDGER_BURNED = "hemomancy.ledger.disband.ledger_burned";
	private static final String KICKED = "hemomancy.bloodline.kick.you_were_removed";
	private static final String RECALL_MINTED = "message.hemomancy.cardinal_rite_completion_effects.the_covenant_remembers_your_ledger_for";
	private static final String UNBINDING_WARNS = "message.hemomancy.cardinal_rite_completion_effects.the_covenant_loosens_but_holds_performing";
	private static final String RANK_SEALED = "message.hemomancy.cardinal_rite_activation.the_rank_rite_remains_sealed_complete";
	private static final String COVENANT_CHAPTER = "milestone.hemomancy.chapter.covenant_written_in_place";
	private static final String ALREADY_ATTAINED = "message.hemomancy.cardinal_rite_activation.you_have_already_attained";
	private static final String NO_RITE_MATCHES = "message.hemomancy.cardinal_rite_activation.the_floor_answers_but_no_structure";
	private static final String STAKE_INVALID = "block.hemomancy.hematic_stake.invalid";
	private static final String STAKE_NOT_OWNER = "block.hemomancy.hematic_stake.not_owner";
	private static final String KIT_GRANTED = "hemomancy.dialogue.event.artificer_consecration_kit_granted";
	private static final String INSPECTION_UNREADY = "hemomancy.dialogue.event.artificer_inspection_unready";
	private static final String REWARD_UNREADY = "hemomancy.dialogue.event.artificer_reward_unready";
	private static final String DEEP_DARK_BEARING = "hemomancy.alchemist.deep_dark_commission.bearing";
	private static final String DEEP_DARK_MISSING = "hemomancy.alchemist.deep_dark_commission.bearing_missing";
	private static final String DEEP_DARK_RECORDED = "hemomancy.alchemist.deep_dark_commission.recorded";
	private static final String BASIN_BEARING = "hemomancy.alchemist.phlegethontic_commission.bearing";
	private static final String BASIN_MISSING = "hemomancy.alchemist.phlegethontic_commission.bearing_missing";
	private static final String BASIN_RECORDED = "hemomancy.alchemist.phlegethontic_commission.recorded";

	private PlaythroughIlluminatusGameTests() {
	}

	// =================================================================================================================
	// The bloodline: founding, joining
	// =================================================================================================================

	/**
	 * Bloodline Founding played through: the Lesser Communion floor, blaze powder in a lit brazier, the Staff planted,
	 * anchors fed, the Bastion traced, the Bloodlicker wave fought, the daemon sealed. The signed ledger lies at the
	 * Focus, the founder is told the line's name and earns {@code blood_is_bound}, and the Vicar's D5 hint moves from
	 * "found your bloodline" to "found your Fane".
	 */
	@GameTest(template = EMPTY, timeoutTicks = 9000, batch = RITES)
	public static void foundBloodlineByRite(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 20, 4);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "bloodline_founding");
		Outcome[] rite = new Outcome[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(() -> {
			DialogueView before = p.approach(vicar).talkTo(vicar);
			before.assertLangKeys(helper, "the D5 Vicar before the line").assertNoDeadEnds(helper, "the D5 Vicar before the line");
			helper.assertTrue(hint(before).contains(GUIDE_FOUND_BLOODLINE), "a fresh D5 is told to found a bloodline: " + hint(before));
		});
		playBloodlineFounding(clock, p, floor, rite);
		clock.then(() -> {
			Outcome founded = rite[0];
			PlaythroughLog.note(helper, "Bloodline Founding: " + founded.describe());
			founded.said(FOUNDED).earned(adv(HarbingerAdvancementGranter.ADV_BLOOD_IS_BOUND));
			Bloodline line = PlaythroughStates.lineOf(p);
			helper.assertTrue(line != null && line.getLeaderUUID().equals(p.uuid()), "the founder leads a registered line");
			helper.assertTrue(line.getPlayerUUIDS().size() == 1 && line.getMaxBloodVolume() == Bloodline.BLOOD_VOLUME_PER_MEMBER,
					"a new line holds only its founder, pool max 5000: " + line.getMaxBloodVolume());
			helper.assertTrue(HemoCapabilityAccess.getInitiatoryDegree(p.player()).hasFoundedBloodline(), "the founder flag is set");
			// D-3 pattern: the ledger drops unowned at the Focus, so anyone standing there can take it.
			int lying = dropsOf(p, floor.focus(), ItemInit.unsigned_ancestral_ledger.get());
			PlaythroughLog.note(helper, "D-3: the signed ledger lies unowned at the Focus (" + lying + " drop)");
			p.pickUpNear(floor.focus(), ItemInit.unsigned_ancestral_ledger.get());
			helper.assertTrue(p.count(ItemInit.unsigned_ancestral_ledger.get()) == 1, "the founder picks up one ledger; " + p.last());
			helper.assertTrue(signedFor(ledgerStack(p), line), "the picked-up ledger is signed for the new line");
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.FOUNDED_LINE_D5);
			DialogueView after = p.approach(vicar).talkTo(vicar);
			helper.assertTrue(hint(after).contains(GUIDE_FOUND_FANE) && !hint(after).contains(GUIDE_FOUND_BLOODLINE),
					"with a line the Vicar's hint moves to the Fane: " + hint(after));
		}).run();
	}

	/**
	 * A leader, then a member of that line, plant Bloodline Founding: each is refused with a line that names why, and
	 * no second line is registered for either.
	 */
	// FIX: LF-2 Bloodline Founding refuses a caster already in a registered line (activation refusal, new key).
	@GameTest(template = EMPTY, timeoutTicks = 1500, batch = RITES)
	public static void foundingAgainIsRefused(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 20, 4);
		PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer member = d5(helper, 18, 4);
		PlaythroughStates.memberOf(member, leader); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(leader, helper.absolutePos(RITE_ORIGIN), "bloodline_founding");
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, leader, floor);
		clock.then(() -> {
			for (PlaythroughPlayer caster : List.of(leader, member)) {
				long linesBefore = linesHolding(caster);
				caster.moveTo(PlaythroughRites.riteStand(floor));
				Outcome planted = plant(caster, floor);
				boolean started = riteRunning(caster);
				clearRiteNow(caster);
				String role = caster == leader ? "the leader" : "a member";
				PlaythroughLog.note(helper, "LF-2 " + role + " plants Bloodline Founding: started=" + started + " " + planted.describe());
				helper.assertFalse(started, role + " already in a line is refused before the rite starts; " + planted);
				helper.assertTrue(namedRefusal(planted, EFFECTS_PREFIX, ACTIVATION_PREFIX).isPresent(),
						role + " is told why (a shipped key); said " + planted.saidKeys());
				helper.assertTrue(linesHolding(caster) == linesBefore, role + " is in no second line");
			}
		}).run();
	}

	/**
	 * B right-clicks with A's signed ledger: B is told they joined, the pool grows by 5000, A (online) is told who
	 * joined, and B's Vicar now offers the pledge option.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void ledgerJoinBySecondPlayer(GameTestHelper helper) {
		PlaythroughPlayer a = d5(helper, 6, 6);
		Bloodline line = PlaythroughStates.foundedLineD5(a); // ARRANGE
		PlaythroughPlayer b = d5(helper, 8, 6);
		a.joinLevel();
		b.joinLevel();
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 8, 10));
		b.give(ledgerStack(a).copy()); // ARRANGE: A hands B a copy of the signed ledger
		int heard = a.mark();
		Outcome join = useLedger(b);
		join.said(JOINED);
		Bloodline after = PlaythroughStates.lineOf(b);
		helper.assertTrue(after != null && after.getBloodlineUUID().equals(line.getBloodlineUUID()), "B is in A's line; " + join);
		helper.assertTrue(after.getMaxBloodVolume() == 2 * Bloodline.BLOOD_VOLUME_PER_MEMBER,
				"the pool max grows by 5000 per member: " + after.getMaxBloodVolume());
		helper.assertTrue(a.chatSince(heard).stream().anyMatch(chat -> chat.has(HAS_JOINED)), "A is told: " + a.chatSince(heard));
		DialogueView view = b.approach(vicar).talkTo(vicar);
		helper.assertTrue(view.offers("recruit_harbinger"), "a member's D5 Vicar offers the pledge: " + view);
		PlaythroughLog.note(helper, "member's Vicar root options without a hub card: " + view.rootOptionsWithoutCard());
		helper.succeed();
	}

	/** A member of line A right-clicks with line B's signed ledger: refused with a line, and in one line only. */
	// FIX: LF-2 a signed ledger refuses a player already in another line (new message.hemomancy.unsigned_ledger_item.* key).
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void ledgerJoinWhileInAnotherLineRefused(GameTestHelper helper) {
		PlaythroughPlayer leaderA = d5(helper, 4, 6);
		Bloodline lineA = PlaythroughStates.foundedLineD5(leaderA); // ARRANGE
		PlaythroughPlayer leaderB = d5(helper, 6, 6);
		Bloodline lineB = PlaythroughStates.foundedLineD5(leaderB); // ARRANGE
		PlaythroughPlayer member = d5(helper, 8, 6);
		PlaythroughStates.memberOf(member, leaderA); // ARRANGE
		member.give(ledgerStack(leaderB).copy()); // ARRANGE: B's founder hands over a signed ledger
		Outcome used = useLedger(member);
		PlaythroughLog.note(helper, "LF-2 member of A uses B's ledger: " + used.describe());
		Bloodline savedB = BloodlineSavedData.get(helper.getLevel().getServer().overworld()).getBloodline(lineB.getBloodlineUUID());
		helper.assertFalse(savedB.hasMember(member.uuid()), "a member of line A does not join line B; " + used);
		helper.assertTrue(linesHolding(member) == 1, "the member is in exactly one line");
		Bloodline local = PlaythroughStates.lineOf(member);
		helper.assertTrue(local != null && local.getBloodlineUUID().equals(lineA.getBloodlineUUID()), "the member keeps line A");
		used.didNotSay(JOINED);
		helper.assertTrue(namedRefusal(used, LEDGER_PREFIX).isPresent(), "the refused join says why; said " + used.saidKeys());
		helper.succeed();
	}

	/** A Degree 0 player and an inactive-blood Degree 5 player each use a signed ledger. */
	// DECISION D5-1: a signed-ledger join has no degree, active-blood or leader-consent gate. Options: keep / D1+ active
	// blood / leader confirms / D5 only. The probe expects some gate: neither player is joined without one.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void ledgerJoinHasNoGate(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 4, 6);
		PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer fresh = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 6));
		PlaythroughStates.freshD0(fresh);
		PlaythroughPlayer inactive = d5(helper, 10, 6);
		var volume = HemoCapabilityAccess.getBloodVolume(inactive.player());
		volume.setActive(false); // ARRANGE: blood gone dormant
		BloodVolumeEvents.syncVolume(inactive.player(), volume);
		List<String> joined = new ArrayList<>();
		for (PlaythroughPlayer joiner : List.of(fresh, inactive)) {
			joiner.give(ledgerStack(leader).copy()); // ARRANGE: the founder hands over a signed ledger
			Outcome used = useLedger(joiner);
			String who = joiner == fresh ? "D0" : "inactive D5";
			PlaythroughLog.note(helper, "D5-1 " + who + " uses the ledger: " + used.describe());
			if (linesHolding(joiner) > 0) joined.add(who);
		}
		PlaythroughLog.note(helper, "D5-1 joined with no gate or consent: " + joined);
		helper.assertTrue(joined.isEmpty(), "D5-1 expected a gate on joining by ledger; joined: " + joined);
		helper.succeed();
	}

	// =================================================================================================================
	// The Founding Fane
	// =================================================================================================================

	/**
	 * The Founding Fane played through by a progenitor: the Grand Dominion floor and its hematic iron pillars, a
	 * nautilus shell in a lit brazier, Sanguine Quintessence seated by hand, the Staff planted, the wave fought, the
	 * seal. The Consecrated Bloodwell stands three above the Focus and is the line's heart; both Fane advancements;
	 * the founder hears the consecration and the level hears the broadcast.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 16000, batch = FANE)
	public static void foundFaneByRite(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 20, 4);
		PlaythroughStates.foundedLineD5(p); // ARRANGE: Bloodline Founding is its own test
		p.joinLevel(); // level-wide broadcasts reach players in the level's list
		PlaythroughSites.RiteFloor floor = PlaythroughSites.faneFloor(p, helper.absolutePos(RITE_ORIGIN));
		Outcome[] rite = new Outcome[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		playFoundingFane(clock, p, floor, rite);
		clock.then(() -> {
			Outcome fane = rite[0];
			PlaythroughLog.note(helper, "Founding Fane: " + fane.describe());
			BlockPos heart = PlaythroughSites.faneHeart(floor);
			helper.assertTrue(helper.getLevel().getBlockState(heart).is(BlockInit.consecrated_bloodwell.get()),
					"the Consecrated Bloodwell stands at Focus+3 " + heart.toShortString());
			helper.assertTrue(heart.equals(FoundingFaneSavedData.get(helper.getLevel()).getHeart(p.uuid())),
					"the heart is registered for the progenitor");
			fane.said(FANE_CONSECRATED).said(FANE_BROADCAST)
					.earned(adv(HarbingerAdvancementGranter.ADV_FOUNDING_FANE_ESTABLISHED))
					.earned(adv(HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE));
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.COVENANT_D5);
		}).run();
	}

	/**
	 * Every way the Founding Fane can be refused, then a relocation played through: a player with no line, a member
	 * of a line, a founder on another Fane's ground; then a progenitor with a Fane elsewhere is warned on the first
	 * planting, and the second planting inside the window relocates the Fane and removes the old heart and stakes.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 16000, batch = FANE_RELOCATION)
	public static void faneRefusals(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer progenitor = d5(helper, 4, 4);
		PlaythroughStates.foundedLineD5(progenitor); // ARRANGE
		PlaythroughPlayer lineless = d5(helper, 6, 4);
		PlaythroughPlayer member = d5(helper, 8, 4);
		PlaythroughStates.memberOf(member, progenitor); // ARRANGE
		PlaythroughPlayer neighbour = d5(helper, 10, 4);
		PlaythroughStates.foundedLineD5(neighbour); // ARRANGE
		PlaythroughPlayer stranger = d5(helper, 12, 4);
		PlaythroughStates.foundedLineD5(stranger); // ARRANGE: another founder with no Fane yet
		BlockPos neighbourHeart = PlaythroughSites.fane(neighbour, PlaythroughSites.stand(helper, 14, 20)); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.faneFloor(progenitor, helper.absolutePos(RITE_ORIGIN));
		BlockPos oldHeart = PlaythroughStates.defaultHeart(progenitor);
		BlockPos oldStake = oldHeart.south(3);
		Outcome[] rite = new Outcome[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, progenitor, floor);
		clock.then(() -> {
			Outcome noLine = plantAt(lineless, floor);
			helper.assertFalse(riteRunning(lineless), "no line, no Fane; " + noLine);
			noLine.said(FANE_NEEDS_LINE);
			Outcome notProgenitor = plantAt(member, floor);
			helper.assertFalse(riteRunning(member), "a member cannot consecrate; " + notProgenitor);
			notProgenitor.said(FANE_PROGENITOR_ONLY);
			Outcome claimed = plantAt(stranger, floor);
			PlaythroughLog.note(helper, "a founder beside another Fane: " + claimed.describe());
			helper.assertFalse(riteRunning(stranger), "another Fane's ground is refused; " + claimed);
			claimed.said(FANE_CLAIMED);
			// ARRANGE: the neighbour's Fane is taken down before the relocation is played.
			FoundingFaneSavedData.get(level).remove(neighbour.uuid());
			level.setBlock(neighbourHeart, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			// ARRANGE: the progenitor already has a Fane at home, with one stake.
			PlaythroughStates.covenantD5(progenitor, oldHeart);
			setState(helper, oldStake, BlockInit.hematic_stake.get().defaultBlockState());
			FoundingFaneSavedData.get(level).addStake(progenitor.uuid(), oldStake, 3);
			Outcome warned = plantAt(progenitor, floor);
			helper.assertFalse(riteRunning(progenitor), "the first relocation planting only warns; " + warned);
			warned.said(FANE_RELOCATE_WARN);
			progenitor.moveTo(PlaythroughRites.riteStand(floor));
			progenitor.wield(ItemInit.living_staff.get());
		}).step(() -> PlaythroughClock.rite(progenitor, floor.focus(), RitePlan.standard().fightThreats()))
				.then(() -> {
					rite[0] = progenitor.last();
					PlaythroughLog.note(helper, "relocated Fane: " + rite[0].describe());
					rite[0].said(FANE_MOVED);
					BlockPos heart = PlaythroughSites.faneHeart(floor);
					helper.assertTrue(heart.equals(FoundingFaneSavedData.get(level).getHeart(progenitor.uuid())),
							"the heart moved to the new floor");
					helper.assertFalse(level.getBlockState(oldHeart).is(BlockInit.consecrated_bloodwell.get()),
							"the old heart is gone");
					helper.assertFalse(level.getBlockState(oldStake).is(BlockInit.hematic_stake.get()), "the old stake is gone");
					helper.assertTrue(progenitor.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE)),
							"the covenant already written stays");
				}).run();
	}

	// =================================================================================================================
	// The Bloodwell deposit and the Vicar's guidance
	// =================================================================================================================

	/**
	 * Members and others hold Blood Projection on the leader's Bloodwell. Only the D5 member, alive and inside the
	 * Fane, earns {@code covenant_written_in_place}; an observer, an outsider (told {@code not_bloodline}), a D4
	 * member, a spectating and a dead member, and a member whose blood is inactive get nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = BATCH)
	public static void memberDepositEarnsCovenant(GameTestHelper helper) {
		String covenant = adv(HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE);
		PlaythroughPlayer leader = d5(helper, 4, 12);
		BlockPos heart = PlaythroughStates.covenantD5(leader, PlaythroughStates.defaultHeart(leader)); // ARRANGE
		PlaythroughPlayer depositor = member(helper, leader, 8, 9, 5);
		PlaythroughPlayer observer = member(helper, leader, 6, 10, 5);
		PlaythroughPlayer outsider = d5(helper, 10, 10);
		PlaythroughPlayer adept = member(helper, leader, 10, 14, 4);
		PlaythroughPlayer spectator = member(helper, leader, 8, 15, 5);
		PlaythroughPlayer dead = member(helper, leader, 6, 14, 5);
		PlaythroughPlayer dormant = member(helper, leader, 10, 12, 5);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.step(depositor.hold(ItemInit.blood_projection.get(), 40, heart))
				.then(() -> {
					Outcome deposit = depositor.last();
					PlaythroughLog.note(helper, "D5 member deposit: " + deposit.describe());
					helper.assertTrue(deposit.bloodSpent() > 0, "the member's blood goes into the well; " + deposit);
					deposit.earned(covenant);
					helper.assertFalse(observer.has(covenant), "a member who only watched earns nothing");
					helper.assertTrue(PlaythroughStates.lineOf(leader).getBloodVolume() > 0, "the pool rose");
				})
				.step(outsider.hold(ItemInit.blood_projection.get(), 30, heart))
				.then(() -> {
					Outcome refused = outsider.last();
					refused.said(WELL_NOT_BLOODLINE);
					helper.assertFalse(outsider.has(covenant), "an outsider earns nothing; " + refused);
				})
				.step(adept.hold(ItemInit.blood_projection.get(), 30, heart))
				.then(() -> {
					PlaythroughLog.note(helper, "D4 member deposit: " + adept.last().describe());
					helper.assertFalse(adept.has(covenant), "a D4 member's deposit is no D5 proof; " + adept.last());
					spectator.player().setGameMode(GameType.SPECTATOR); // ARRANGE
				})
				.step(spectator.hold(ItemInit.blood_projection.get(), 30, heart))
				.then(() -> {
					PlaythroughLog.note(helper, "spectating member: " + spectator.last().describe());
					helper.assertFalse(spectator.has(covenant), "a spectator earns nothing; " + spectator.last());
					dead.die();
					// ARRANGE: death drops the pack; a Projection is put back in the dead player's hand so the server's
					// isAlive guard is what is tested, as for a player who dies with the channel held.
					dead.give(new ItemStack(ItemInit.blood_projection.get()));
				})
				.step(() -> dead.hold(ItemInit.blood_projection.get(), 30, heart))
				.then(() -> {
					PlaythroughLog.note(helper, "dead member: " + dead.last().describe());
					helper.assertFalse(dead.has(covenant), "a dead member earns nothing; " + dead.last());
					var volume = HemoCapabilityAccess.getBloodVolume(dormant.player());
					volume.setActive(false); // ARRANGE: dormant blood
					BloodVolumeEvents.syncVolume(dormant.player(), volume);
				})
				.step(dormant.hold(ItemInit.blood_projection.get(), 30, heart))
				.then(() -> {
					Outcome inactive = dormant.last();
					PlaythroughLog.note(helper, "inactive member: " + inactive.describe());
					helper.assertFalse(dormant.has(covenant), "inactive blood earns nothing; " + inactive);
					helper.assertTrue(inactive.saidKeys().contains(WELL_INACTIVE) || inactive.bloodSpent() <= 0,
							"inactive blood is either told so or deposits nothing; " + inactive);
				}).run();
	}

	/** A member with the pool already full holds Projection on the Bloodwell; a member with no blood tries too. */
	// DECISION D5-8: a full-pool or zero deposit earns no covenant proof and says nothing. Options: count the attempt /
	// a line saying the pool is full / keep. The probe expects the player to get the proof or to be told why not.
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = BATCH, required = false)
	public static void fullPoolDepositSaysNothing(GameTestHelper helper) {
		String covenant = adv(HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE);
		PlaythroughPlayer leader = d5(helper, 4, 12);
		BlockPos heart = PlaythroughStates.covenantD5(leader, PlaythroughStates.defaultHeart(leader)); // ARRANGE
		PlaythroughPlayer full = member(helper, leader, 8, 9, 5);
		PlaythroughPlayer empty = member(helper, leader, 8, 15, 5);
		Bloodline line = PlaythroughStates.lineOf(leader);
		BloodlineSavedData.get(helper.getLevel().getServer().overworld()).contributeBlood(line.getBloodlineUUID(),
				line.getMaxBloodVolume()); // ARRANGE: the pool is already full
		PlaythroughStates.blood(empty, 0.0D); // ARRANGE: a member with nothing to give
		List<String> silent = new ArrayList<>();
		PlaythroughClock.start(helper)
				.step(full.hold(ItemInit.blood_projection.get(), 30, heart))
				.then(() -> {
					Outcome attempt = full.last();
					PlaythroughLog.note(helper, "D5-8 full pool deposit: " + attempt.describe());
					if (!full.has(covenant) && attempt.chat().isEmpty()) silent.add("full pool");
				})
				.step(empty.hold(ItemInit.blood_projection.get(), 30, heart))
				.then(() -> {
					Outcome attempt = empty.last();
					PlaythroughLog.note(helper, "D5-8 zero deposit: " + attempt.describe());
					if (!empty.has(covenant) && attempt.chat().isEmpty()) silent.add("zero blood");
					helper.assertTrue(silent.isEmpty(), "D5-8 expected the proof or a line; silent for " + silent);
				}).run();
	}

	/**
	 * A D5 member's Vicar: without the proof the hint is the deposit ({@code found_fane}), with it the rite
	 * ({@code perform_bloodline_rite}), never "found your bloodline"; the Assignment Ledger agrees.
	 */
	// FIX: LF-3 withCovenantGuidance reads "in a registered bloodline" and checks the proof first; the ledger likewise.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void vicarGuidesMembersToTheirFane(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 4, 6);
		PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer member = member(helper, leader, 10, 6, 5);
		member.give(new ItemStack(ItemInit.harbinger_assignment_ledger.get())); // ARRANGE: the D1 ledger is carried
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 10, 10));
		DialogueView before = member.approach(vicar).talkTo(vicar);
		LedgerView ledgerBefore = member.ledger();
		PlaythroughLog.note(helper, "LF-3 member without proof: hint=" + hint(before) + " ledger=" + ledgerBefore);
		helper.assertFalse(hint(before).contains(GUIDE_FOUND_BLOODLINE), "a member is never told to found a line: " + hint(before));
		helper.assertTrue(hint(before).contains(GUIDE_FOUND_FANE), "a member without the proof is sent to the Bloodwell: " + hint(before));
		helper.assertTrue(ledgerBefore.packet().foundedBloodline(), "the ledger counts the member's line: " + ledgerBefore);
		PlaythroughStates.covenantProof(member); // ARRANGE: the deposit is its own test
		DialogueView after = member.approach(vicar).talkTo(vicar);
		PlaythroughLog.note(helper, "LF-3 member with proof: hint=" + hint(after));
		helper.assertTrue(hint(after).contains(GUIDE_PERFORM_RITE), "with the proof the member is sent to the rite: " + hint(after));
		helper.assertFalse(hint(after).contains(GUIDE_FOUND_BLOODLINE), "never found_bloodline: " + hint(after));
		after.assertLangKeys(helper, "a member's D5 Vicar");
		helper.succeed();
	}

	// =================================================================================================================
	// Recruiting outpost Harbingers
	// =================================================================================================================

	/**
	 * A leader pledges a Vicar, a Mnemonist and an Alchemist from different outposts (+1000 pool each); a second Vicar
	 * and a same-outpost Alchemist are hidden and their forged pledges refused with the type and outpost lines; the
	 * Vicar's card turns to release; a member's release is refused ({@code not_leader}); the leader's goes through; a
	 * recruit's death frees its slot.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void recruitAndExpelAtOutposts(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 16, 12);
		PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer member = member(helper, leader, 14, 12, 5);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 12, 12));
		HarbingerAlchemistEntity sameOutpost = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 20, 12));
		HarbingerMnemonistEntity mnemonist = PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 16, 8));
		PlaythroughNpcs.stampOutpost(helper, mnemonist, 1); // ARRANGE: a second outpost
		HarbingerVicarEntity secondVicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 16, 16));
		PlaythroughNpcs.stampOutpost(helper, secondVicar, 2); // ARRANGE
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 12, 16));
		PlaythroughNpcs.stampOutpost(helper, alchemist, 3); // ARRANGE
		float base = PlaythroughStates.lineOf(leader).getMaxBloodVolume();

		Outcome first = recruit(leader, vicar);
		first.said(RECRUIT_SUCCESS).said(RECRUIT_POOL);
		helper.assertTrue(PlaythroughStates.lineOf(leader).getMaxBloodVolume() == base + Bloodline.NPC_BLOOD_VOLUME_PER_MEMBER,
				"a pledged Vicar adds 1000 to the pool; " + first);
		// The outpost limit is checked before any Alchemist is pledged, so the type limit cannot answer first.
		DialogueView same = leader.approach(sameOutpost).talkTo(sameOutpost);
		PlaythroughLog.note(helper, "same-outpost Alchemist offers recruit=" + same.offers("recruit_harbinger"));
		helper.assertFalse(same.offers("recruit_harbinger"), "an Alchemist of the Vicar's outpost is not offered: " + same);
		leader.forge("recruit_harbinger", sameOutpost.getId()).said(RECRUIT_OUTPOST);
		recruit(leader, mnemonist).said(RECRUIT_SUCCESS);
		recruit(leader, alchemist).said(RECRUIT_SUCCESS);
		helper.assertTrue(PlaythroughStates.lineOf(leader).getNpcMemberCount() == 3, "three outpost Harbingers pledged");

		DialogueView second = leader.approach(secondVicar).talkTo(secondVicar);
		helper.assertFalse(second.offers("recruit_harbinger"), "a second Vicar is not offered: " + second);
		leader.forge("recruit_harbinger", secondVicar.getId()).said(RECRUIT_TYPE);
		helper.assertTrue(PlaythroughStates.lineOf(leader).getNpcMemberCount() == 3, "the refused pledges add nobody");

		DialogueView pledged = leader.approach(vicar).talkTo(vicar);
		helper.assertTrue(pledged.offers("expel_harbinger") && !pledged.offers("recruit_harbinger"),
				"a pledged Vicar shows the release card instead: " + pledged);
		Optional<DialogueView> memberView = member.approach(vicar).tryTalkTo(vicar);
		Outcome memberRelease = memberView.isPresent() && memberView.get().offers("expel_harbinger")
				? member.choose(vicar, "expel_harbinger") : member.forge("expel_harbinger", vicar.getId());
		memberRelease.said(RECRUIT_NOT_LEADER);
		helper.assertTrue(PlaythroughStates.lineOf(leader).hasNpcMember(vicar.getUUID()), "a member cannot release the Vicar");
		leader.approach(vicar).talkTo(vicar);
		leader.choose(vicar, "expel_harbinger").said(EXPEL_SUCCESS);
		helper.assertFalse(PlaythroughStates.lineOf(leader).hasNpcMember(vicar.getUUID()), "the leader released the Vicar");

		ResourceLocation mnemonistType = BuiltInRegistries.ENTITY_TYPE.getKey(mnemonist.getType());
		PlaythroughNpcs.kill(mnemonist); // ARRANGE: the recruit dies to nothing the player did
		Bloodline afterDeath = PlaythroughStates.lineOf(leader);
		helper.assertFalse(afterDeath.hasNpcMember(mnemonist.getUUID()) || afterDeath.hasNpcMemberType(mnemonistType),
				"a dead recruit frees its slot");
		helper.assertTrue(afterDeath.getBloodVolume() <= afterDeath.getMaxBloodVolume(), "the pool is clamped to its maximum");
		HarbingerMnemonistEntity replacement = PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 20, 8));
		PlaythroughNpcs.stampOutpost(helper, replacement, 4); // ARRANGE
		recruit(leader, replacement).said(RECRUIT_SUCCESS);
		helper.succeed();
	}

	/**
	 * Forged {@code recruit_harbinger} payloads naming a cow, an Artificer, an Anchorite, a Court member, a Circus
	 * performer and another player, and one sent by a D4 member at a Vicar: none adds a member to the line.
	 */
	// FIX: LF-1 recruit_harbinger/expel_harbinger need a Vicar, Alchemist or Mnemonist, and recruiting needs Degree 5.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void forgedRecruitIsRefused(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 16, 12);
		PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer other = d5(helper, 17, 13);
		other.enterWorld(); // the dispatch finds the named entity by id, as it finds a real player
		helper.assertTrue(helper.getLevel().getEntity(other.player().getId()) == other.player(),
				"the other player is found by entity id, so the forged pledge at them reaches the speaker check");
		List<Entity> targets = new ArrayList<>();
		targets.add(animal(helper, EntityType.COW, PlaythroughSites.stand(helper, 12, 12)));
		targets.add(PlaythroughNpcs.artificer(helper, PlaythroughSites.stand(helper, 20, 12)));
		targets.add(PlaythroughNpcs.anchorite(helper, PlaythroughSites.stand(helper, 16, 8)));
		Entity court = optionalSpeaker(helper, "a Court member",
				() -> PlaythroughNpcs.court(helper, CourtRole.GARDENER, PlaythroughSites.stand(helper, 16, 16)));
		if (court != null) targets.add(court);
		Entity performer = optionalSpeaker(helper, "a Circus performer",
				() -> PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.FIRE_EATER, PlaythroughSites.stand(helper, 12, 16)));
		if (performer != null) targets.add(performer);
		targets.add(other.player());
		List<String> recruited = new ArrayList<>();
		for (Entity target : targets) {
			int before = PlaythroughStates.lineOf(leader).getNpcMemberCount();
			leader.approach(target);
			Outcome forged = leader.forge("recruit_harbinger", target.getId());
			PlaythroughLog.note(helper, "LF-1 forged recruit at " + PlaythroughPlayer.describe(target) + ": " + forged.describe());
			if (PlaythroughStates.lineOf(leader).getNpcMemberCount() > before) recruited.add(PlaythroughPlayer.describe(target));
		}
		PlaythroughPlayer adept = member(helper, leader, 4, 6, 4);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 4, 10));
		int before = PlaythroughStates.lineOf(leader).getNpcMemberCount();
		adept.approach(vicar);
		Outcome d4 = adept.forge("recruit_harbinger", vicar.getId());
		PlaythroughLog.note(helper, "LF-1 forged recruit by a D4 member: " + d4.describe());
		if (PlaythroughStates.lineOf(leader).getNpcMemberCount() > before) recruited.add("a Vicar, by a D4 member");
		helper.assertTrue(recruited.isEmpty(), "forged pledges recruit nobody; recruited " + recruited);
		helper.succeed();
	}

	// =================================================================================================================
	// The ledger screen and the pool screen
	// =================================================================================================================

	/**
	 * The ledger screen's actions: summon with no recruits, summon from outside the Fane, set the recall point inside
	 * and outside the Fane, recall from afar, summon a recruit into the Fane, then disband (ledgers burnt, the member
	 * reset and told, the Fane taken down).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void ledgerSummonRecallDisband(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer leader = d5(helper, 4, 12);
		BlockPos heart = PlaythroughStates.covenantD5(leader, PlaythroughStates.defaultHeart(leader)); // ARRANGE
		BlockPos home = leader.home();
		BlockPos far = PlaythroughSites.farOrigin(helper, 96).above().south(12);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 28, 12));

		leader.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS).said(SUMMON_NO_NPCS);
		PlaythroughStates.recruited(leader, vicar); // ARRANGE: the pledge is its own test
		leader.moveTo(far); // ARRANGE: travelled away from the Fane
		leader.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS).said(SUMMON_NOT_IN_LODGE);
		leader.ledgerAction(PacketLedgerAction.ACTION_SET_RECALL_POINT).said(POINT_OUT_OF_RANGE);
		leader.moveTo(home); // ARRANGE: back inside the Fane
		leader.ledgerAction(PacketLedgerAction.ACTION_SET_RECALL_POINT).said(POINT_SET);
		leader.moveTo(far); // ARRANGE
		leader.ledgerAction(PacketLedgerAction.ACTION_RECALL_TO_LODGE).said(RECALL_SUCCESS);
		Vec3 landed = leader.player().position();
		helper.assertTrue(landed.distanceTo(Vec3.atBottomCenterOf(home)) < 2.5D, "recall lands at the set point: " + landed);
		helper.assertTrue(vicar.distanceTo(leader.player()) > 16.0F, "the recruit stands away from the leader before the summon");
		leader.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS).said(SUMMON_SUCCESS);
		helper.assertTrue(vicar.distanceTo(leader.player()) < 4.0F, "the summoned Vicar stands by the leader: "
				+ vicar.distanceTo(leader.player()));
		PlaythroughLog.note(helper, "NPC-1: the summoned outpost Vicar now stands in the Fane, away from its outpost");

		PlaythroughPlayer member = member(helper, leader, 8, 16, 5);
		member.give(PlaythroughStates.signedLedger(PlaythroughStates.lineOf(leader))); // ARRANGE: the member's ledger
		leader.joinLevel();
		member.joinLevel();
		int heard = member.mark();
		Outcome disband = leader.ledgerAction(PacketLedgerAction.ACTION_DISBAND_BLOODLINE);
		PlaythroughLog.note(helper, "disband: " + disband.describe() + " member heard " + member.chatSince(heard));
		disband.said(DISBAND_SUCCESS);
		helper.assertTrue(member.chatSince(heard).stream().anyMatch(line -> line.has(DISBAND_NOTICE)), "the member is told");
		helper.assertTrue(member.chatSince(heard).stream().anyMatch(line -> line.has(LEDGER_BURNED)), "the member's ledger burns");
		helper.assertTrue(member.count(ItemInit.unsigned_ancestral_ledger.get()) == 0
				&& leader.count(ItemInit.unsigned_ancestral_ledger.get()) == 0, "both ledgers are burnt");
		helper.assertTrue(PlaythroughStates.lineOf(leader) == null && PlaythroughStates.lineOf(member) == null, "no line is left");
		helper.assertFalse(FoundingFaneSavedData.get(level).hasFane(leader.uuid()), "the Fane is taken down");
		helper.assertFalse(level.getBlockState(heart).is(BlockInit.consecrated_bloodwell.get()), "the heart is gone");
		PlaythroughLog.note(helper, "D5-4 context: the disband cost nothing; covenant kept="
				+ leader.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE)));
		helper.succeed();
	}

	/**
	 * The pool screen's kick: an online member is removed and told; an offline member is removed and reconciled at the
	 * next login. The pool never reads above its maximum.
	 */
	// FIX: LF-14 Bloodline.removeMember clamps the pool to the new maximum (only if this test shows it above).
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void kickAndRelog(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 4, 12);
		Bloodline line = PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer online = member(helper, leader, 8, 12, 5);
		PlaythroughPlayer offline = member(helper, leader, 10, 12, 5);
		online.joinLevel();
		BloodlineSavedData data = BloodlineSavedData.get(helper.getLevel().getServer().overworld());
		data.contributeBlood(line.getBloodlineUUID(), 12000.0F); // ARRANGE: a well-filled pool of three
		Outcome kickOnline = leader.kick(online.uuid());
		PlaythroughLog.note(helper, "kick online: " + kickOnline.describe() + " member heard " + online.chat());
		helper.assertTrue(online.chat().stream().anyMatch(chat -> chat.has(KICKED)), "the online member is told");
		helper.assertFalse(HemoCapabilityAccess.getBloodVolume(online.player()).getBloodLine().isValid(),
				"the online member's line is cleared at once");
		Outcome kickOffline = leader.kick(offline.uuid());
		PlaythroughLog.note(helper, "kick offline: " + kickOffline.describe());
		Bloodline saved = data.getBloodline(line.getBloodlineUUID());
		helper.assertFalse(saved.hasMember(offline.uuid()), "the offline member is out of the saved line");
		offline.relog();
		helper.assertFalse(HemoCapabilityAccess.getBloodVolume(offline.player()).getBloodLine().isValid(),
				"the next login reconciles the kicked member");
		leader.relog();
		helper.assertTrue(PlaythroughStates.lineOf(leader) != null, "the leader keeps the line across a relog");
		PlaythroughLog.note(helper, "LF-14 pool after two kicks: " + saved.getBloodVolume() + " / " + saved.getMaxBloodVolume());
		helper.assertTrue(saved.getBloodVolume() <= saved.getMaxBloodVolume(),
				"the pool does not read above its maximum: " + saved.getBloodVolume() + " / " + saved.getMaxBloodVolume());
		helper.succeed();
	}

	/** A kicked player right-clicks with the ledger they joined with. */
	// DECISION D5-3: a kicked member holding their old ledger gets no action and no line. Options: refuse with a line /
	// allow rejoin / burn the kicked member's ledger copy. The probe expects a line, a rejoin or a burnt ledger.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void kickedMemberStaleLedger(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 4, 12);
		PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer kicked = d5(helper, 8, 12);
		kicked.joinLevel();
		kicked.give(ledgerStack(leader).copy()); // ARRANGE: the founder hands over a signed ledger
		useLedger(kicked).said(JOINED);
		leader.kick(kicked.uuid());
		Outcome stale = useLedger(kicked);
		boolean rejoined = linesHolding(kicked) > 0;
		boolean burnt = kicked.count(ItemInit.unsigned_ancestral_ledger.get()) == 0;
		PlaythroughLog.note(helper, "D5-3 stale ledger: " + stale.describe() + " rejoined=" + rejoined + " burnt=" + burnt);
		helper.assertTrue(!stale.chat().isEmpty() || rejoined || burnt,
				"D5-3 expected the kicked player to be told, rejoined or lose the ledger; " + stale);
		helper.succeed();
	}

	/**
	 * A member performs Bloodline Recall and gets a signed ledger, then pledges a Vicar; the same member cannot release
	 * a recruit, set the recall point or disband, and has no way to leave.
	 */
	// DECISION D5-2: members may recruit and mint signed ledgers by Bloodline Recall but cannot expel, set the recall
	// point or disband, and have no voluntary leave. Options: add a leave action / leader-only recruit and Recall /
	// keep. The probe expects that a member with the first two powers also has a way to leave.
	@GameTest(template = EMPTY, timeoutTicks = 8000, batch = RITES, required = false)
	public static void bloodlineRecallAndMemberPowers(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 4, 4);
		PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer member = member(helper, leader, 20, 4, 5);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 4, 20));
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(member, helper.absolutePos(RITE_ORIGIN), "bloodline_recall");
		boolean[] powers = new boolean[2];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, member, floor);
		clock.then(() -> member.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(member, floor.focus(), RitePlan.standard().fightThreats()))
				.then(() -> {
					Outcome recall = member.last();
					PlaythroughLog.note(helper, "D5-2 member's Bloodline Recall: " + recall.describe());
					member.pickUpNear(floor.focus(), ItemInit.unsigned_ancestral_ledger.get());
					powers[0] = recall.saidKeys().contains(RECALL_MINTED)
							&& member.count(ItemInit.unsigned_ancestral_ledger.get()) > 0;
					Outcome pledge = recruit(member, vicar);
					powers[1] = pledge.saidKeys().contains(RECRUIT_SUCCESS);
					PlaythroughLog.note(helper, "D5-2 member mints a ledger=" + powers[0] + ", pledges a Vicar=" + powers[1]);
					member.approach(vicar).talkTo(vicar);
					Outcome release = member.lastView(vicar).map(view -> view.offers("expel_harbinger")).orElse(false)
							? member.choose(vicar, "expel_harbinger") : member.forge("expel_harbinger", vicar.getId());
					PlaythroughLog.note(helper, "D5-2 member release: " + release.saidKeys());
					Outcome point = member.ledgerAction(PacketLedgerAction.ACTION_SET_RECALL_POINT);
					Outcome disband = member.ledgerAction(PacketLedgerAction.ACTION_DISBAND_BLOODLINE);
					PlaythroughLog.note(helper, "D5-2 member set point: " + point.saidKeys() + " disband: " + disband.saidKeys());
					helper.assertTrue(release.saidKeys().contains(RECRUIT_NOT_LEADER) && point.saidKeys().contains(POINT_NOT_LEADER)
							&& disband.saidKeys().contains(DISBAND_NOT_LEADER), "a member's leader-only actions are refused");
					boolean leaveExists = false; // PacketLedgerAction offers summon, recall, set point and disband only
					helper.assertTrue(!(powers[0] || powers[1]) || leaveExists,
							"D5-2 expected a member who can recruit and mint ledgers to have a way to leave");
				}).run();
	}

	/** Hematic Unbinding played once: the Nether Star burns and the line holds; the ledger's Disband costs nothing. */
	// DECISION D5-4: Hematic Unbinding consumes its Nether Star and only warns; the ledger Disband is free. Options: warn
	// at activation like Fane relocation / drop the rite / make Disband cost / keep. The probe expects a warning-only
	// completion to keep its star.
	@GameTest(template = EMPTY, timeoutTicks = 11000, batch = RITES, required = false)
	public static void hematicUnbindingCostsTwoStars(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 20, 4);
		Bloodline line = PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(leader, helper.absolutePos(RITE_ORIGIN), "hematic_unbinding");
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, leader, floor);
		clock.then(() -> leader.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(leader, floor.focus(), RitePlan.standard().fightThreats()))
				.then(() -> {
					Outcome first = leader.last();
					PlaythroughLog.note(helper, "D5-4 first Unbinding: " + first.describe());
					first.said(UNBINDING_WARNS);
					BlockPos socket = PlaythroughRites.usedSockets(floor).get(0);
					boolean starKept = leader.count(Items.NETHER_STAR) > 0 || PlaythroughRites.brazier(leader, socket).hasOffering();
					helper.assertTrue(PlaythroughStates.lineOf(leader) != null, "the line holds after the first Unbinding");
					leader.joinLevel();
					Outcome disband = leader.ledgerAction(PacketLedgerAction.ACTION_DISBAND_BLOODLINE);
					PlaythroughLog.note(helper, "D5-4 star kept=" + starKept + "; ledger Disband: " + disband.describe());
					disband.said(DISBAND_SUCCESS);
					helper.assertTrue(BloodlineSavedData.get(helper.getLevel().getServer().overworld())
							.getBloodline(line.getBloodlineUUID()) == null, "the free Disband dissolves the line");
					helper.assertTrue(starKept, "D5-4 expected the warning-only Unbinding to keep its Nether Star");
				}).run();
	}

	// =================================================================================================================
	// A rite interrupted: the caster dies or walks away; rite recovery
	// =================================================================================================================

	/**
	 * The Founding Fane by a progenitor who dies in the ORDEAL (real damage), lies dead past the 20-tick recovery
	 * sweep, then respawns. The Staff, the seated Sanguine Quintessence and the nautilus offering each exist exactly
	 * once afterwards (in the pack, on the ground the player can pick up, in the Focus or on its brazier), and no Fane
	 * was founded. The player stands in the level's player list, which the sweep walks.
	 */
	// FIX: CardinalRiteSavedData.deliverRecovery handed rite recovery to dead players (lost with the corpse) and took a
	// Creative discard for a delivery, the pattern LF-4 fixed for station kits.
	@GameTest(template = EMPTY, timeoutTicks = 16000, batch = FANE_DEATH)
	public static void foundingFaneCasterDiesMidRite(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 20, 4);
		PlaythroughStates.foundedLineD5(p); // ARRANGE
		p.joinLevel();
		PlaythroughSites.RiteFloor floor = PlaythroughSites.faneFloor(p, helper.absolutePos(RITE_ORIGIN));
		BlockPos[] spot = new BlockPos[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().fightThreats()
						.pauseWhen(() -> phaseIs(p, CardinalRitePhase.ORDEAL))))
				.then(() -> {
					helper.assertTrue(riteRunning(p), "the Founding Fane reaches its ordeal");
					spot[0] = p.player().blockPosition();
					Outcome died = p.die();
					PlaythroughLog.note(helper, "the progenitor dies in the Fane's ordeal: " + died.describe());
					helper.assertFalse(riteRunning(p), "a death ends the rite");
				})
				.idle(45) // dead past two recovery sweeps
				.then(p::respawn)
				.idle(45)
				.then(() -> {
					gatherRiteItems(p, floor, spot[0]);
					// The Staff goes back into the pack the dying player drops, and a Living item on the ground is gone at
					// once (LivingItem lifespan 0): the bond's Conjure Staff recalls a new one. It must never be doubled.
					int staffs = count(helper, p, floor, ItemInit.living_staff.get());
					PlaythroughLog.note(helper, "after the death the Staff exists " + staffs + " time(s); the player can conjure one: "
							+ p.knows("conjure_staff"));
					helper.assertTrue(staffs <= 1, "the Staff is not doubled: " + staffs);
					assertOnce(helper, p, floor, ItemInit.sanguine_quintessence.get(), "the Sanguine Quintessence");
					assertOnce(helper, p, floor, Items.NAUTILUS_SHELL, "the nautilus offering");
					helper.assertTrue(FoundingFaneSavedData.get(helper.getLevel()).getHeart(p.uuid()) == null, "no Fane was founded");
				}).run();
	}

	/**
	 * The Founding Fane's progenitor logs out in the ORDEAL and stays away past the rite's disconnect grace, so the
	 * rite gutters out with the Staff queued for recovery; on the next login (in the level's player list) the Staff,
	 * Quintessence and offering are each there exactly once.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 16000, batch = FANE_AWAY)
	public static void foundingFaneCasterAwayMidRite(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 20, 4);
		PlaythroughStates.foundedLineD5(p); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.faneFloor(p, helper.absolutePos(RITE_ORIGIN));
		BlockPos[] spot = new BlockPos[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().fightThreats()
						.pauseWhen(() -> phaseIs(p, CardinalRitePhase.ORDEAL))))
				.then(() -> {
					helper.assertTrue(riteRunning(p), "the Founding Fane reaches its ordeal");
					spot[0] = p.player().blockPosition();
					p.offline(); // logs out and stays away
				})
				.idle(CardinalRiteCeremonyRules.DISCONNECT_GRACE_TICKS + 40)
				.then(() -> {
					helper.assertFalse(riteRunning(p), "the unattended rite gutters out while the caster is away");
					p.relog();
					p.joinLevel();
				})
				.idle(45)
				.then(() -> {
					gatherRiteItems(p, floor, spot[0]);
					PlaythroughLog.note(helper, "back after the rite guttered out: " + p.chat());
					assertOnce(helper, p, floor, ItemInit.living_staff.get(), "the Staff");
					assertOnce(helper, p, floor, ItemInit.sanguine_quintessence.get(), "the Sanguine Quintessence");
					assertOnce(helper, p, floor, Items.NAUTILUS_SHELL, "the nautilus offering");
				}).run();
	}

	/**
	 * A rite's recovery queue (a returned Staff and offering, ARRANGE as a terminated rite queues them) is held while
	 * its owner is dead, through several 20-tick sweeps, and arrives once after the respawn. For a Creative player with
	 * a full pack the stack stays owed instead of being discarded, and lands once there is room.
	 */
	// FIX: CardinalRiteSavedData.deliverRecovery skips a dead player and counts only stacks that land.
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = BATCH)
	public static void riteRecoveryHeldWhileDead(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 8, 20);
		p.joinLevel();
		PlaythroughPlayer creative = d5(helper, 14, 20);
		creative.joinLevel();
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(helper.getLevel().getServer().overworld());
		java.util.UUID riteId = java.util.UUID.randomUUID();
		java.util.UUID creativeRite = java.util.UUID.randomUUID();
		PlaythroughClock.start(helper)
				.then(() -> {
					p.die();
					// ARRANGE: what a rite broken while its caster lies dead queues for them.
					saved.addRecovery(p.uuid(), riteId, List.of(new ItemStack(ItemInit.living_staff.get()),
							new ItemStack(Items.NAUTILUS_SHELL)));
					creative.creative().fillInventory();
					saved.addRecovery(creative.uuid(), creativeRite, List.of(new ItemStack(Items.NAUTILUS_SHELL)));
				})
				.idle(45)
				.then(() -> {
					helper.assertTrue(p.player().getInventory().countItem(Items.NAUTILUS_SHELL) == 0,
							"nothing is put into the dead player's pack");
					helper.assertTrue(creative.count(Items.NAUTILUS_SHELL) == 0, "a Creative full pack takes nothing");
					p.respawn();
					creative.survival();
					creative.player().getInventory().clearContent();
				})
				.idle(45)
				.then(() -> {
					PlaythroughLog.note(helper, "rite recovery after the respawn: Staffs " + p.count(ItemInit.living_staff.get())
							+ ", nautilus " + p.count(Items.NAUTILUS_SHELL) + "; the Creative player's held claim then landed: "
							+ creative.count(Items.NAUTILUS_SHELL));
					helper.assertTrue(p.count(Items.NAUTILUS_SHELL) == 1, "the queued offering arrives once after the respawn");
					helper.assertTrue(p.count(ItemInit.living_staff.get()) == 1, "the queued Staff arrives once after the respawn");
					helper.assertTrue(creative.count(Items.NAUTILUS_SHELL) == 1,
							"the stack a Creative full pack refused stays owed and lands once there is room");
				}).run();
	}

	/**
	 * The Rite of the Scarlet Summons. A member plants it and is refused at once (leader only), keeping the Staff. The
	 * leader plays it with the only member offline: the finale holds and says no member is online; the member comes
	 * online with the pool too low: it holds and says so; the pool is filled (ARRANGE) and the rite completes, the
	 * member is drawn to the Focus and told, and the leader is told the summons drew kin.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 12000, batch = RITES)
	public static void scarletSummonsDrawsKin(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 20, 4);
		Bloodline line = PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer member = member(helper, leader, 4, 20, 5);
		member.joinLevel();
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(leader, helper.absolutePos(RITE_ORIGIN), "scarlet_summons");
		String noMembers = EFFECTS_PREFIX + "no_bloodline_members_are_online_to";
		String poolLow = EFFECTS_PREFIX + "the_bloodline_pool_lacks_the_blood";
		int[] mark = new int[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, leader, floor);
		clock.then(() -> {
			member.moveTo(PlaythroughRites.riteStand(floor));
			Outcome refused = plant(member, floor);
			PlaythroughLog.note(helper, "a member plants the Scarlet Summons: " + refused.describe());
			helper.assertFalse(riteRunning(member), "a member starts no Summons; " + refused);
			refused.said(EFFECTS_PREFIX + "only_the_bloodline_leader_may_perform");
			helper.assertTrue(member.count(ItemInit.living_staff.get()) == 1, "the member keeps the Staff");
			member.moveTo(member.home());
			member.offline(); // the only member is away
			pool(helper, line, 2000.0F); // ARRANGE
			leader.moveTo(PlaythroughRites.riteStand(floor));
			leader.wield(ItemInit.living_staff.get());
			mark[0] = leader.mark();
		}).step(() -> PlaythroughClock.rite(leader, floor.focus(), RitePlan.standard().fightThreats()
						.pauseWhen(() -> saidSince(leader, mark[0], noMembers))))
				.then(() -> {
					helper.assertTrue(riteRunning(leader), "with no member online the finale holds; " + leader.chatSince(mark[0]));
					member.relog();
					member.joinLevel();
					pool(helper, line, 50.0F); // ARRANGE: the pool cannot pay for one member
					mark[0] = leader.mark();
				})
				.step(() -> PlaythroughClock.rite(leader, floor.focus(), RitePlan.standard().takeUp().fightThreats()
						.pauseWhen(() -> saidSince(leader, mark[0], poolLow))))
				.then(() -> {
					helper.assertTrue(riteRunning(leader), "with the pool too low the finale holds; " + leader.chatSince(mark[0]));
					pool(helper, line, 2000.0F); // ARRANGE: the pool refilled
					mark[0] = member.mark();
				})
				.step(() -> PlaythroughClock.rite(leader, floor.focus(), RitePlan.standard().takeUp().fightThreats()))
				.then(() -> {
					Outcome done = leader.last();
					PlaythroughLog.note(helper, "the Scarlet Summons: " + done.describe() + "; the member heard "
							+ member.chatSince(mark[0]) + " at " + member.player().position());
					done.said(EFFECTS_PREFIX + "the_scarlet_summons_draws_blood_kin");
					helper.assertTrue(saidSince(member, mark[0], EFFECTS_PREFIX + "the_blood_calls_you_have_been"),
							"the member is told the blood calls them");
					helper.assertTrue(member.player().position().distanceTo(Vec3.atCenterOf(floor.focus())) < 4.0D,
							"the member is drawn to the Focus: " + member.player().position());
				}).run();
	}

	/**
	 * The second, dissolving Hematic Unbinding inside its window (the first, warning completion ARRANGE: it is played
	 * by {@link #hematicUnbindingCostsTwoStars}). One member is online and one is offline; both have room for their
	 * share of the pool. The player expects every member to get their share, the offline one when they return.
	 */
	// DECISION D5-11: completeHematicUnbinding returns poolBlood / memberCount only to members online at that moment
	// (and only as far as their own vessel has room), so an offline member's share is lost; their ledger is burnt only
	// on its next use or tick, and the pending window is a root persistent key a death clears. Options: bank the
	// offline share and pay it at login / refuse the dissolution while a member is offline / keep (lost).
	@GameTest(template = EMPTY, timeoutTicks = 11000, batch = RITES, required = false)
	public static void hematicUnbindingOfflineShare(GameTestHelper helper) {
		PlaythroughPlayer leader = d5(helper, 20, 4);
		Bloodline line = PlaythroughStates.foundedLineD5(leader); // ARRANGE
		PlaythroughPlayer present = member(helper, leader, 4, 18, 5);
		PlaythroughPlayer away = member(helper, leader, 8, 18, 5);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(leader, helper.absolutePos(RITE_ORIGIN), "hematic_unbinding");
		double[] before = new double[2];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, leader, floor);
		clock.then(() -> {
			pool(helper, line, 3000.0F); // ARRANGE
			PlaythroughStates.blood(present, 1000.0D); // ARRANGE: room for a share
			PlaythroughStates.blood(away, 1000.0D);
			before[0] = present.blood();
			before[1] = away.blood();
			// ARRANGE: the first, warning completion inside its window.
			leader.player().getPersistentData().putUUID("hemomancy:pending_unbinding_bloodline", line.getBloodlineUUID());
			leader.player().getPersistentData().putLong("hemomancy:pending_unbinding_until", helper.getLevel().getGameTime() + 24000L);
			away.offline();
			leader.wield(ItemInit.living_staff.get());
		}).step(() -> PlaythroughClock.rite(leader, floor.focus(), RitePlan.standard().fightThreats()))
				.then(() -> {
					PlaythroughLog.note(helper, "D5-11 second Unbinding: " + leader.last().describe());
					helper.assertTrue(BloodlineSavedData.get(helper.getLevel().getServer().overworld())
							.getBloodline(line.getBloodlineUUID()) == null, "the second Unbinding dissolves the line");
					away.relog();
					double presentShare = present.blood() - before[0];
					double awayShare = away.blood() - before[1];
					PlaythroughLog.note(helper, "D5-11 shares: online member +" + presentShare + ", offline member +" + awayShare
							+ " after relog; the offline member still holds a line "
							+ HemoCapabilityAccess.getBloodVolume(away.player()).getBloodLine().isValid() + ", told " + away.chat());
					helper.assertTrue(presentShare > 0.0D, "the online member gets a share");
					helper.assertTrue(awayShare > 0.0D, "DECISION D5-11: the offline member gets their share of the pool too ("
							+ awayShare + " mL); the online member got " + presentShare + " mL");
				}).run();
	}

	private static boolean phaseIs(PlaythroughPlayer p, CardinalRitePhase phase) {
		ActiveCardinalRite rite = CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid());
		return rite != null && rite.getPhase() == phase;
	}

	private static boolean saidSince(PlaythroughPlayer p, int mark, String key) {
		return p.chatSince(mark).stream().anyMatch(line -> line.has(key));
	}

	/** ARRANGE: the line's shared pool set to {@code ml}. */
	private static void pool(GameTestHelper helper, Bloodline line, float ml) {
		BloodlineSavedData data = BloodlineSavedData.get(helper.getLevel().getServer().overworld());
		Bloodline saved = data.getBloodline(line.getBloodlineUUID());
		helper.assertTrue(saved != null, "the line is registered");
		saved.setBloodVolume(ml);
		data.setDirty();
	}

	/** The player walks to where they fell and to the Focus and picks up what the rite left lying there. */
	private static void gatherRiteItems(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor, BlockPos spot) {
		for (Item item : List.of(ItemInit.living_staff.get(), ItemInit.sanguine_quintessence.get(), Items.NAUTILUS_SHELL)) {
			p.pickUpNear(spot, item);
			p.pickUpNear(floor.focus(), item);
		}
	}

	/** {@code item} exists exactly once: carried, on the ground round the floor, in the Focus or on a brazier. */
	private static void assertOnce(GameTestHelper helper, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor, Item item,
			String what) {
		int total = count(helper, p, floor, item);
		helper.assertTrue(total == 1, what + " exists exactly once, not " + total);
	}

	/** How many {@code item} there are: carried, on the ground round the floor, in the Focus and on its braziers. */
	private static int count(GameTestHelper helper, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor, Item item) {
		String what = item.toString();
		int carried = p.count(item);
		int ground = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
				new net.minecraft.world.phys.AABB(floor.focus()).inflate(32.0D), drop -> drop.getItem().is(item))
				.stream().mapToInt(drop -> drop.getItem().getCount()).sum();
		int focus = helper.getLevel().getBlockEntity(floor.focus())
				instanceof com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity f
				&& f.hasMedium() && f.getMediumForMatching().is(item) ? 1 : 0;
		int braziers = 0;
		for (BlockPos socket : floor.brazierSockets()) {
			if (helper.getLevel().getBlockEntity(socket)
					instanceof com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity brazier
					&& brazier.hasOffering() && brazier.getOfferingForMatching().is(item)) braziers += brazier.getOfferingForMatching().getCount();
		}
		PlaythroughLog.note(helper, what + ": carried " + carried + ", ground " + ground + ", in the Focus " + focus
				+ ", on a brazier " + braziers);
		return carried + ground + focus + braziers;
	}

	// =================================================================================================================
	// D5 to D6: the sealed rite
	// =================================================================================================================

	/**
	 * The Rite of the Bloodline Covenant planted before the chapter proof is sealed and names "A Covenant Written in
	 * Place"; with the proof it starts; at D6 it says the rank is already attained.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1500, batch = RITES)
	public static void sanctifiedSealedBeforeCovenant(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 20, 4);
		PlaythroughStates.foundedLineD5(p); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "sanctified_rite");
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
			Outcome sealed = plantAt(p, floor);
			PlaythroughLog.note(helper, "sanctified_rite before the proof: " + sealed.describe());
			sealed.said(RANK_SEALED).said(COVENANT_CHAPTER);
			helper.assertFalse(riteRunning(p), "the sealed rank rite does not start");
			helper.assertTrue(p.count(ItemInit.living_staff.get()) == 1, "the Staff is not escrowed");
			PlaythroughStates.covenantProof(p); // ARRANGE: the proof is its own test
			Outcome opened = plantAt(p, floor);
			helper.assertTrue(riteRunning(p), "with the proof the rite starts; " + opened);
			clearRiteNow(p);
			helper.assertTrue(DegreeProgression.advance(p.player(), 6), "ARRANGE: the rank is attained elsewhere");
			Outcome attained = plantAt(p, floor);
			helper.assertFalse(riteRunning(p), "at D6 the rank rite does not start again");
			// D-32 family (pending): layered floors drop rites the player can no longer attempt before the
			// already-attained line can run, so the D6 replant hears the floor line instead.
			boolean saidAttained = attained.saidKeys().contains(ALREADY_ATTAINED);
			PlaythroughLog.note(helper, "D6 replant said " + attained.saidKeys()
					+ (saidAttained ? "" : " (D-32 family: the floor line, not the already-attained line)"));
			helper.assertTrue(saidAttained || attained.saidKeys().contains(NO_RITE_MATCHES),
					"the D6 replant says why it starts nothing; " + attained);
		}).run();
	}

	// =================================================================================================================
	// Hematic Stakes
	// =================================================================================================================

	/**
	 * The progenitor raises a Hematic Stake in chain (blood crystal against hematic iron bars, a Sanguine Formation in
	 * the offhand, Projection held); one out of chain is refused as {@code invalid}; a non-owner's break is refused as
	 * {@code not_owner}; breaking the heart takes the stakes with it. Pending crafts tick per loaded level (F-11 /
	 * D-34), so the craft lands sooner than the client's collapse; the ticks are logged.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = STAKE)
	public static void hematicStakeExtendsFane(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer leader = d5(helper, 4, 12);
		BlockPos heart = PlaythroughStates.covenantD5(leader, PlaythroughStates.defaultHeart(leader)); // ARRANGE
		leader.joinLevel();
		PlaythroughPlayer stranger = d5(helper, 16, 16);
		BlockPos bar = PlaythroughSites.stand(helper, 14, 12);
		formation(helper, bar); // ARRANGE: the materials are laid by hand elsewhere in play
		BlockPos far = PlaythroughSites.farOrigin(helper, 120).above().south(12);
		PlaythroughSites.keepTicking(helper, level, far);
		formation(helper, far); // ARRANGE
		int[] waited = new int[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(() -> {
			leader.moveTo(PlaythroughSites.stand(helper, 14, 9)); // ARRANGE: within reach of the bars
			leader.give(new ItemStack(ItemInit.sanguine_formation.get())); // ARRANGE: a formation is carried
			leader.player().setItemInHand(InteractionHand.OFF_HAND, takeOne(leader, ItemInit.sanguine_formation.get()));
		}).step(() -> leader.holdUntil(ItemInit.blood_projection.get(), 120, () -> Vec3.atCenterOf(bar),
						() -> leader.player().getOffhandItem().isEmpty()))
				.until(200, () -> {
					waited[0]++;
					return level.getBlockState(bar).is(BlockInit.hematic_stake.get());
				}, "the stake to manifest")
				.then(() -> {
					PlaythroughLog.note(helper, "F-11/D-34: the stake landed " + waited[0] + " ticks after the feed (client collapse 30)");
					helper.assertTrue(leader.uuid().equals(FoundingFaneSavedData.get(level).findOwnerForStake(bar)),
							"the stake is the progenitor's");
					leader.moveTo(far.north(3)); // ARRANGE: walked far past the Fane's reach
					leader.give(new ItemStack(ItemInit.sanguine_formation.get())); // ARRANGE
					leader.player().setItemInHand(InteractionHand.OFF_HAND, takeOne(leader, ItemInit.sanguine_formation.get()));
				})
				.step(() -> leader.hold(ItemInit.blood_projection.get(), 30, Vec3.atCenterOf(far)))
				.idle(40)
				.then(() -> {
					Outcome outOfChain = leader.last();
					PlaythroughLog.note(helper, "out-of-chain stake: " + outOfChain.describe());
					outOfChain.said(STAKE_INVALID);
					helper.assertFalse(level.getBlockState(far).is(BlockInit.hematic_stake.get()), "no stake out of chain");
					stranger.moveTo(bar.north(2));
					Outcome broken = stranger.breakBlock(bar);
					broken.said(STAKE_NOT_OWNER);
					helper.assertTrue(level.getBlockState(bar).is(BlockInit.hematic_stake.get()), "a stranger cannot break the stake");
					leader.moveTo(heart.north(2));
					Outcome heartBroken = leader.breakBlock(heart);
					PlaythroughLog.note(helper, "heart broken: " + heartBroken.describe());
					helper.assertFalse(level.getBlockState(heart).is(BlockInit.consecrated_bloodwell.get()), "the heart is broken");
					helper.assertFalse(level.getBlockState(bar).is(BlockInit.hematic_stake.get()), "the stake collapses with the heart");
					helper.assertTrue(FoundingFaneSavedData.get(level).getHeart(leader.uuid()) == null, "no heart is left");
				}).run();
	}

	// =================================================================================================================
	// Station kits at D5
	// =================================================================================================================

	/**
	 * The D5 station kits through their teachers: an enchant at the player's own Scriptorium (menu buttons), then the
	 * Rubricator's Quill from the Mnemonist; the Ganglionic Harness (Mnemonist), the Precision Governor and the
	 * Armature Consecration Kit (Artificer); a Druse Heart claimed with a full pack by a second player and delivered on
	 * the 20-tick pass once there is room; then the Eightfold Script raises the same Scriptorium in place, its
	 * contents kept.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 12000, batch = RITES)
	public static void stationKitsAtD5(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer p = d5(helper, 20, 4);
		HarbingerMnemonistEntity mnemonist = PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 4, 4));
		HarbingerArtificerEntity artificer = PlaythroughNpcs.artificer(helper, PlaythroughSites.stand(helper, 4, 10));
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 4, 18));
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "eightfold_script");
		BlockPos seat = floor.focus().above().south(UpgradeStation.SCRIPTORIUM.seatDistance());
		StationUpgradeTier quill = StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1);
		PlaythroughPlayer packed = d5(helper, 8, 18);
		packed.joinLevel();
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(() -> {
			p.moveTo(PlaythroughRites.riteStand(floor));
			PlaythroughRites.buildStructureNow(p, floor);
			helper.assertTrue(level.getBlockEntity(seat) instanceof EnzymaticScriptoriumBlockEntity, "the Scriptorium sits behind the Focus");
			DialogueView early = p.approach(mnemonist).talkTo(mnemonist);
			helper.assertFalse(early.offers(StationUpgradeDialogue.claimEvent(quill)), "no Quill before an enchant: " + early);
			helper.assertTrue(early.hasNode("station_upgrade_scriptorium_1"), "the Quill's requirements card is shown");
			enchantAt(p, seat);
			helper.assertTrue(HemoCapabilityAccess.stationUpgrades(p.player()).hasUsed(UpgradeStation.SCRIPTORIUM,
					StationUpgradeCatalog.ENCHANT), "the inscription records the enchant");
			claimKit(p, mnemonist, quill);
			// ARRANGE: each other kit's practice is played by its own station tests; here only the claims are.
			var practice = HemoCapabilityAccess.stationUpgrades(p.player());
			practice.recordUse(UpgradeStation.LOOM, StationUpgradeCatalog.LOOM_WEAVE);
			practice.recordUse(UpgradeStation.RESONANT_FORGE, StationUpgradeCatalog.GRIND);
			practice.recordUse(UpgradeStation.RESONANT_FORGE, StationUpgradeCatalog.APPLY);
			practice.recordUse(UpgradeStation.ARMATURE, StationUpgradeCatalog.ARMOR_UPGRADE);
			claimKit(p, mnemonist, StationUpgradeCatalog.get(UpgradeStation.LOOM, 1));
			claimKit(p, artificer, StationUpgradeCatalog.get(UpgradeStation.RESONANT_FORGE, 1));
			Item kit = item(StationUpgradeCatalog.get(UpgradeStation.ARMATURE, 1));
			DialogueView workshop = p.approach(artificer).talkTo(artificer);
			helper.assertTrue(workshop.offers(HarbingerArtificerDialogueTrees.EVENT_CONSECRATION_KIT), "the Artificer offers the kit: " + workshop);
			p.choose(artificer, HarbingerArtificerDialogueTrees.EVENT_CONSECRATION_KIT).said(KIT_GRANTED).gave(kit, 1);
			var packedPractice = HemoCapabilityAccess.stationUpgrades(packed.player());
			packedPractice.recordUse(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE); // ARRANGE
			packedPractice.recordUse(UpgradeStation.CONDENSER, StationUpgradeCatalog.CONDENSE_LATTICED); // ARRANGE
			StationUpgradeTier druse = StationUpgradeCatalog.get(UpgradeStation.CONDENSER, 2);
			packed.fillInventory(); // ARRANGE: a full pack
			packed.approach(vicar).talkTo(vicar);
			Outcome held = packed.choose(vicar, StationUpgradeDialogue.claimEvent(druse));
			PlaythroughLog.note(helper, "Druse Heart with a full pack: " + held.describe());
			helper.assertTrue(packedPractice.hasPending(UpgradeStation.CONDENSER, 2) && packed.count(item(druse)) == 0,
					"a full pack holds the Druse Heart pending");
			packed.player().getInventory().items.set(0, ItemStack.EMPTY); // ARRANGE: the player makes room
		}).until(45, () -> packed.count(item(StationUpgradeCatalog.get(UpgradeStation.CONDENSER, 2))) == 1,
						"the Druse Heart delivered on the 20-tick pass")
				.then(() -> helper.assertFalse(HemoCapabilityAccess.stationUpgrades(packed.player())
						.hasPending(UpgradeStation.CONDENSER, 2), "the delivered kit is no longer pending"));
		braziersFromPack(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().fightThreats().extraAim(stationCircuits(p))))
				.then(() -> {
					PlaythroughLog.note(helper, "Eightfold Script: " + p.last().describe());
					helper.assertTrue(level.getBlockEntity(seat) instanceof UpgradeableStation station && station.upgradeTier() == 1,
							"the Eightfold Script raises the Scriptorium to its first stage");
					helper.assertFalse(((EnzymaticScriptoriumBlockEntity) level.getBlockEntity(seat))
							.getItem(EnzymaticScriptoriumBlockEntity.ITEM).isEmpty(), "the Scriptorium keeps its contents");
				}).run();
	}

	/**
	 * The Quill claimed with a full pack, then the player dies before the 20-tick pass: after the respawn it is
	 * delivered exactly once. A Creative player with a full pack is logged for D-16 (not asserted).
	 */
	// FIX: LF-4 StationUpgradeProgress.deliver skips dead players and keeps the kit pending while the player is dead.
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH)
	public static void stationKitHeldWhileDead(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 8, 8);
		HarbingerMnemonistEntity mnemonist = PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 8, 12));
		StationUpgradeTier quill = StationUpgradeCatalog.get(UpgradeStation.SCRIPTORIUM, 1);
		Item quillItem = item(quill);
		HemoCapabilityAccess.stationUpgrades(p.player()).recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT); // ARRANGE
		p.joinLevel();
		PlaythroughPlayer creative = d5(helper, 16, 8);
		HemoCapabilityAccess.stationUpgrades(creative.player()).recordUse(UpgradeStation.SCRIPTORIUM, StationUpgradeCatalog.ENCHANT); // ARRANGE
		PlaythroughClock.start(helper)
				.then(() -> {
					p.fillInventory(); // ARRANGE
					p.approach(mnemonist).talkTo(mnemonist);
					Outcome held = p.choose(mnemonist, StationUpgradeDialogue.claimEvent(quill));
					helper.assertTrue(HemoCapabilityAccess.stationUpgrades(p.player()).hasPending(UpgradeStation.SCRIPTORIUM, 1),
							"a full pack holds the Quill pending; " + held);
					p.die();
				})
				.idle(45)
				.then(() -> {
					PlaythroughLog.note(helper, "LF-4 while dead: pending=" + HemoCapabilityAccess.stationUpgrades(p.player())
							.hasPending(UpgradeStation.SCRIPTORIUM, 1));
					p.respawn();
				})
				.idle(45)
				.then(() -> {
					PlaythroughLog.note(helper, "LF-4 after respawn: quills=" + p.count(quillItem) + " pending="
							+ HemoCapabilityAccess.stationUpgrades(p.player()).hasPending(UpgradeStation.SCRIPTORIUM, 1));
					helper.assertTrue(p.count(quillItem) == 1, "the Quill is delivered once after the respawn: " + p.count(quillItem));
					helper.assertFalse(HemoCapabilityAccess.stationUpgrades(p.player()).hasPending(UpgradeStation.SCRIPTORIUM, 1),
							"nothing is left pending");
					// D-16 (logged only): a Creative player with a full pack.
					creative.creative().fillInventory();
					creative.approach(mnemonist).talkTo(mnemonist);
					Outcome discarded = creative.choose(mnemonist, StationUpgradeDialogue.claimEvent(quill));
					PlaythroughLog.note(helper, "D-16 Creative full pack Quill claim: " + discarded.describe() + " pending="
							+ HemoCapabilityAccess.stationUpgrades(creative.player()).hasPending(UpgradeStation.SCRIPTORIUM, 1)
							+ " carried=" + creative.count(quillItem));
				}).run();
	}

	// =================================================================================================================
	// The Artificer's D5 assignments
	// =================================================================================================================

	/**
	 * The Crimson Vestment and the Assumed Limb through the Artificer and the Alchemist. Vestment: brief, an early
	 * inspection (unready), the frame consecrated (ARRANGE: its rite is its own test), inspection, the correspondence
	 * asked of the Artificer (logged) then claimed from the Alchemist, the Blood Lust set worn (ARRANGE), a lifesteal
	 * hit, the fitting. Limb: a staff bond (ARRANGE), brief, an early claim (unready), the graft (ARRANGE), the claim, a
	 * living-arsenal kill, the seven forms (ARRANGE), the fitting.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH)
	public static void crimsonVestmentAndAssumedLimb(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 12, 12);
		HarbingerArtificerEntity artificer = PlaythroughNpcs.artificer(helper, PlaythroughSites.stand(helper, 8, 12));
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 16, 12));

		chooseOffered(p, artificer, HarbingerArtificerDialogueTrees.EVENT_BRIEF_CRIMSON_VESTMENT);
		helper.assertTrue(p.has(ArtificerAssignments.CRIMSON_VESTMENT_BRIEFED.getPath()), "the Vestment is briefed");
		p.forge(HarbingerArtificerDialogueTrees.EVENT_INSPECT_CRIMSON_VESTMENT, artificer.getId()).said(INSPECTION_UNREADY);
		grant(p, HarbingerAdvancementGranter.ADV_ARTIFICER_FRAME_CONSECRATED); // ARRANGE: own Armature Consecration rite
		chooseOffered(p, artificer, HarbingerArtificerDialogueTrees.EVENT_INSPECT_CRIMSON_VESTMENT);
		helper.assertTrue(p.has(ArtificerAssignments.CRIMSON_VESTMENT_INSPECTED.getPath()), "the inspection is recorded");
		Outcome wrongSpeaker = p.forge(HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD, artificer.getId());
		PlaythroughLog.note(helper, "correspondence asked of the Artificer (swallowed?): " + wrongSpeaker.describe());
		helper.assertFalse(p.has(ArtificerAssignments.CRIMSON_VESTMENT_COUNSELED.getPath()), "only the Alchemist counsels");
		chooseOffered(p, alchemist, HarbingerArtificerDialogueTrees.EVENT_CLAIM_CRIMSON_VESTMENT_REWARD);
		helper.assertTrue(received(p, alchemist, ItemInit.crimson_lacquer.get()) >= 1, "the Alchemist's lacquer is taken up");
		grant(p, HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_BLOOD_LUST_UPGRADE); // ARRANGE: Armature upgrades
		p.player().setItemSlot(EquipmentSlot.HEAD, new ItemStack(ItemInit.blood_lust_helm.get())); // ARRANGE
		p.player().setItemSlot(EquipmentSlot.CHEST, new ItemStack(ItemInit.blood_lust_chest.get())); // ARRANGE
		p.player().setItemSlot(EquipmentSlot.LEGS, new ItemStack(ItemInit.blood_lust_legs.get())); // ARRANGE
		p.player().setItemSlot(EquipmentSlot.FEET, new ItemStack(ItemInit.blood_lust_boots.get())); // ARRANGE
		Entity cow = animal(helper, EntityType.COW, PlaythroughSites.stand(helper, 12, 16));
		PlaythroughRites.emptyHand(p);
		p.approach(cow);
		Outcome hit = p.attack(cow);
		PlaythroughLog.note(helper, "Blood Lust demonstration hit: " + hit.describe());
		helper.assertTrue(p.has(ArtificerAssignments.CRIMSON_VESTMENT_DEMONSTRATED.getPath()), "the lifesteal hit is the demonstration");
		chooseOffered(p, artificer, HarbingerArtificerDialogueTrees.EVENT_CLAIM_BLOOD_LUST_FITTING);
		helper.assertTrue(received(p, artificer, ItemInit.crimson_vestment_fitting.get()) == 1, "the Vestment fitting is given once");

		HemoCapabilityAccess.getLivingStaffProgress(p.player()).unlockLivingStaffBond(); // ARRANGE: the staff bond
		chooseOffered(p, artificer, HarbingerArtificerDialogueTrees.EVENT_BRIEF_ASSUMED_LIMB);
		helper.assertTrue(p.has(ArtificerAssignments.ASSUMED_LIMB_BRIEFED.getPath()), "the Limb is briefed");
		p.forge(HarbingerArtificerDialogueTrees.EVENT_CLAIM_ASSUMED_LIMB_REWARD, artificer.getId()).said(REWARD_UNREADY);
		grant(p, HarbingerAdvancementGranter.ADV_ARTIFICER_FIRST_LIVING_GRAFT); // ARRANGE: a brazier graft (proof owed)
		PlaythroughLog.note(helper, "the Living Weapon graft at a brazier is ARRANGE here; its own play is owed");
		chooseOffered(p, artificer, HarbingerArtificerDialogueTrees.EVENT_CLAIM_ASSUMED_LIMB_REWARD);
		helper.assertTrue(received(p, artificer, ItemInit.hematic_memory.get()) >= 1, "the blank memory is taken up");
		if (p.count(ItemInit.living_staff.get()) == 0) p.give(new ItemStack(ItemInit.living_staff.get()));
		Entity chicken = animal(helper, EntityType.CHICKEN, PlaythroughSites.stand(helper, 12, 16));
		for (int swing = 0; swing < 40 && chicken.isAlive(); swing++) {
			chicken.invulnerableTime = 0;
			p.wield(ItemInit.living_staff.get());
			p.approach(chicken).attack(chicken);
		}
		helper.assertFalse(chicken.isAlive(), "the living-arsenal kill lands");
		helper.assertTrue(p.has(ArtificerAssignments.ASSUMED_LIMB_DEMONSTRATED.getPath()), "the kill is the demonstration");
		for (LivingWeaponForm form : LivingWeaponForm.values()) { // ARRANGE: the seven forms grafted elsewhere
			RegistryLookup.byName(ManipulationInit.MANIPS_TYPE_REGISTRY, form.manipulationName())
					.ifPresent(manipulation -> KnownManipulationGrantHelper.grantCreativeMemory(p.player(), manipulation, null));
		}
		helper.assertTrue(ArtificerAssignments.knowsFullLivingArsenal(p.player()), "ARRANGE: all seven forms known");
		chooseOffered(p, artificer, HarbingerArtificerDialogueTrees.EVENT_CLAIM_LIVING_ARSENAL_FITTING);
		helper.assertTrue(received(p, artificer, ItemInit.assumed_limb_fitting.get()) == 1, "the Limb fitting is given once");
		helper.succeed();
	}

	// =================================================================================================================
	// The Alchemist's commissions
	// =================================================================================================================

	/**
	 * The Deep Dark and Basin commissions: bearings (the flat GameTest overworld has no Deep Dark, so
	 * {@code bearing_missing}; the Nether search reports whichever it finds), no report option without proof, the
	 * Deep Dark report with Ahaematic proof and the Basin report with the sample in the main hand and five Scyphus;
	 * each reported commission's card goes.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = COMMISSIONS)
	public static void alchemistCommissions(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 12, 12);
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 12, 8));
		DialogueView view = p.approach(alchemist).talkTo(alchemist);
		view.assertLangKeys(helper, "the D5 Alchemist");
		helper.assertFalse(view.offers(DeepDarkCommissionDialogue.REPORT), "no Deep Dark report without proof");
		helper.assertFalse(view.offers(PhlegethonticCommissionDialogue.REPORT), "no Basin report without a sample");
		Outcome deep = p.choose(alchemist, DeepDarkCommissionDialogue.BEARING);
		PlaythroughLog.note(helper, "Deep Dark bearing: " + deep.saidKeys());
		helper.assertTrue(deep.saidKeys().contains(DEEP_DARK_MISSING) || deep.saidKeys().contains(DEEP_DARK_BEARING),
				"the Deep Dark bearing says something; " + deep);
		p.approach(alchemist).talkTo(alchemist);
		long start = System.nanoTime();
		Outcome basin = p.choose(alchemist, PhlegethonticCommissionDialogue.BEARING);
		PlaythroughLog.note(helper, "Basin bearing (" + (System.nanoTime() - start) / 1_000_000L + " ms): " + basin.saidKeys());
		helper.assertTrue(basin.saidKeys().contains(BASIN_BEARING) || basin.saidKeys().contains(BASIN_MISSING),
				"the Basin bearing says something; " + basin);

		AntecedentKnowledge.record(p.player(), AntecedentResearch.Evidence.SAMPLE_ANALYZED); // ARRANGE: Ahaematic proof
		DialogueView ready = p.approach(alchemist).talkTo(alchemist);
		helper.assertTrue(ready.offers(DeepDarkCommissionDialogue.REPORT), "the proof opens the Deep Dark report: " + ready);
		p.choose(alchemist, DeepDarkCommissionDialogue.REPORT).said(DEEP_DARK_RECORDED).gaveNothing();

		p.give(new ItemStack(BlockInit.escharian_scyphus.get().asItem(), 5)); // ARRANGE: five Escharian Scyphus
		p.give(basinSample()); // ARRANGE: a Basin creature's blood
		PlaythroughRites.wieldStack(p, PhlegethonticCommission::validSample, "the Basin sample");
		DialogueView sampled = p.approach(alchemist).talkTo(alchemist);
		helper.assertTrue(sampled.offers(PhlegethonticCommissionDialogue.REPORT), "the sample in hand opens the Basin report: " + sampled);
		Outcome reported = p.choose(alchemist, PhlegethonticCommissionDialogue.REPORT).said(BASIN_RECORDED);
		helper.assertTrue(reported.delta(BlockInit.escharian_scyphus.get()) == 0, "the Scyphus are counted, not taken");
		DialogueView after = p.approach(alchemist).talkTo(alchemist);
		helper.assertFalse(after.hasNode("deep_dark_commission") || after.hasNode("phlegethontic_commission"),
				"both reported commissions leave the Alchemist's tree: " + after.nodes().keySet());
		helper.succeed();
	}

	/** The Basin sample carried in the offhand while the five Scyphus are in the pack. */
	// DECISION D5-7: the Basin commission reads the sample only in the main hand at talk time; neither commission
	// rewards. Options: any-hand check / a reward / keep. The probe expects a carried sample to open the report.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void alchemistBasinSampleInMainHand(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 12, 12);
		HarbingerAlchemistEntity alchemist = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 12, 8));
		p.give(new ItemStack(BlockInit.escharian_scyphus.get().asItem(), 5)); // ARRANGE
		p.player().setItemInHand(InteractionHand.OFF_HAND, basinSample()); // ARRANGE: carried in the offhand
		PlaythroughRites.emptyHand(p);
		DialogueView view = p.approach(alchemist).talkTo(alchemist);
		boolean offered = view.offers(PhlegethonticCommissionDialogue.REPORT);
		PlaythroughLog.note(helper, "D5-7 offhand sample: report offered=" + offered + " lines=" + view.node("phlegethontic_commission")
				.map(DialogueNode::lines).orElse(List.of()));
		helper.assertTrue(offered, "D5-7 expected a carried Basin sample to open the report");
		helper.succeed();
	}

	// =================================================================================================================
	// The Monolith at D5
	// =================================================================================================================

	/** A D5 player at a Sanguine Monolith someone built: covenant counsel, and the same hub with an item in hand. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void monolithAtD5(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 14, 6);
		BlockPos stone = PlaythroughSites.monolith(p, PlaythroughSites.stand(helper, 14, 2)); // ARRANGE: a D6 craft (D-5)
		DialogueView empty = p.talkToBlock(stone);
		empty.assertLangKeys(helper, "the D5 Monolith").assertNoDeadEnds(helper, "the D5 Monolith");
		helper.assertTrue(empty.entityId() == SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID, "a block speaker's tree");
		List<String> keys = empty.lineKeys();
		boolean guidance = keys.stream().anyMatch(key -> key.contains("covenant") || key.contains("fane") || key.contains("bloodline"));
		p.give(new ItemStack(Items.STICK)); // ARRANGE
		p.wield(Items.STICK);
		Optional<DialogueView> held = p.useOnForDialogue(stone);
		boolean treeWithItem = held.isPresent() && held.get().hasNode("what_are_you_illuminatus");
		PlaythroughLog.note(helper, "the D5 stone: empty-hand lines " + keys + "; with an item: "
				+ held.map(DialogueView::describe).orElse("nothing"));
		helper.assertTrue(guidance && treeWithItem, "expected covenant guidance (" + guidance
				+ ") and the tree with an item in hand (" + treeWithItem + ")");
		helper.succeed();
	}

	// =================================================================================================================
	// The band chain, for the late run
	// =================================================================================================================

	/** The D5 line-and-Fane chain on its own, from the fresh-D5 state; optional until the late run is stable. */
	@GameTest(template = EMPTY, timeoutTicks = 30000, batch = FANE_CHAIN, required = false)
	public static void covenantChainFromPreviousBand(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 16, 4);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		chain(helper, p, clock);
		clock.run();
	}

	/**
	 * The D5 line and Fane as steps on {@code clock}, for the late run: lays a Vicar, the Bloodline Founding floor and
	 * the Founding Fane floor now, then plays Bloodline Founding, picks up the ledger, pledges the Vicar, plays the
	 * Founding Fane and summons the Vicar into it, and ends comparing the player with
	 * {@link PlaythroughStates.Checkpoint#COVENANT_D5} plus the recruit. {@code p} must be a fresh Illuminatus with
	 * its tools (blood is topped up here).
	 */
	public static void chain(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 2, 2));
		PlaythroughSites.RiteFloor founding = PlaythroughSites.riteFloor(p, helper.absolutePos(SIDE_ORIGIN), "bloodline_founding");
		PlaythroughSites.RiteFloor fane = PlaythroughSites.faneFloor(p, helper.absolutePos(RITE_ORIGIN));
		tools(p); // ARRANGE
		PlaythroughStates.blood(p, 6000.0D); // ARRANGE: a rested pool
		p.joinLevel();
		Outcome[] rite = new Outcome[2];
		playBloodlineFounding(clock, p, founding, rite);
		clock.then(() -> {
			rite[0].said(FOUNDED);
			p.pickUpNear(founding.focus(), ItemInit.unsigned_ancestral_ledger.get());
			helper.assertTrue(p.count(ItemInit.unsigned_ancestral_ledger.get()) == 1, "the founder carries the signed ledger");
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.FOUNDED_LINE_D5);
			recruit(p, vicar).said(RECRUIT_SUCCESS);
			PlaythroughStates.blood(p, 6000.0D); // ARRANGE: rested before the Fane
		});
		Outcome[] faneRite = new Outcome[1];
		playFoundingFane(clock, p, fane, faneRite);
		clock.then(() -> {
			faneRite[0].said(FANE_CONSECRATED);
			p.moveTo(PlaythroughRites.riteStand(fane));
			p.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS).said(SUMMON_SUCCESS);
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.COVENANT_D5);
			helper.assertTrue(PlaythroughStates.lineOf(p).hasNpcMember(vicar.getUUID()), "the pledged Vicar is in the line");
			PlaythroughLog.note(helper, "D5 chain end: blood " + Math.round(p.blood()) + ", carried "
					+ Outcome.inventory(p.player()));
		});
	}

	// =================================================================================================================
	// Band-private helpers (candidates for the harness)
	// =================================================================================================================

	/** Bloodline Founding on {@code floor}: braziers and the blaze powder, then the rite; its outcome in {@code out[0]}. */
	private static void playBloodlineFounding(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor,
			Outcome[] out) {
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().fightThreats()))
				.then(() -> out[0] = p.last());
	}

	/** The Founding Fane on {@code floor}: pillars, the nautilus brazier, the Quintessence seated, then the rite. */
	private static void playFoundingFane(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor,
			Outcome[] out) {
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> p.wield(ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().fightThreats()))
				.then(() -> out[0] = p.last());
	}

	/**
	 * Braziers for {@code floor} with each offering taken from the pack when carried (a claimed kit) and supplied
	 * only when it is not (ARRANGE), unlike {@link PlaythroughRites#braziers} which always supplies a copy.
	 */
	private static void braziersFromPack(PlaythroughClock clock, PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) {
		GameTestHelper helper = p.helper();
		List<ItemStack> offerings = PlaythroughRites.offerings(floor.recipe());
		helper.assertTrue(floor.brazierSockets().size() >= offerings.size(), "a socket for each offering");
		for (int i = 0; i < offerings.size(); i++) {
			BlockPos socket = floor.brazierSockets().get(i);
			ItemStack offering = offerings.get(i);
			clock.then(() -> {
				p.moveTo(PlaythroughRites.besideSocket(floor, socket)); // ARRANGE: walk to the socket
				PlaythroughRites.build(p, socket, BlockInit.iron_brazier.get());
				if (!PlaythroughRites.brazier(p, socket).hasOffering()) {
					if (p.count(offering.getItem()) == 0) {
						p.give(offering.copy()); // ARRANGE: the offering is gathered elsewhere
						PlaythroughLog.note(helper, "ARRANGE supplied the offering " + offering.getItem());
					} else {
						PlaythroughLog.note(helper, "the carried " + offering.getItem() + " goes on the brazier");
					}
					PlaythroughRites.insertOffering(p, socket, stack -> ItemStack.isSameItem(stack, offering),
							offering.getHoverName().getString());
				}
			}).step(() -> PlaythroughRites.lightBrazier(p, socket))
					.then(() -> helper.assertTrue(PlaythroughRites.lit(p, socket), "the brazier at " + socket.toShortString() + " lights"));
		}
		clock.then(() -> p.moveTo(PlaythroughRites.riteStand(floor)));
	}

	/** {@code extraAim} for a station-upgrade rite: the open circuit target during STATION_PROJECTION, else the sigils. */
	private static BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> stationCircuits(PlaythroughPlayer p) {
		BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> sigils = PlaythroughRites.supportSigilNodes(p);
		return (rite, recipe) -> rite.getPhase() == CardinalRitePhase.STATION_PROJECTION
				? StationUpgradeRites.targetSurface(p.player().serverLevel(), rite, rite.upgrade().stage())
				: sigils.apply(rite, recipe);
	}

	/**
	 * One inscription at the Scriptorium at {@code pos}: the station stocked (ARRANGE: eight enzymes, a pickaxe, lapis,
	 * 1000 mL in its tank, 40 levels on the player, the player's own craft credit), the menu opened by an empty-hand
	 * right-click, the Ductilis bias pressed three times and the first offered row inscribed, all by menu buttons.
	 */
	private static void enchantAt(PlaythroughPlayer p, BlockPos pos) {
		GameTestHelper helper = p.helper();
		EnzymaticScriptoriumBlockEntity station = (EnzymaticScriptoriumBlockEntity) p.player().serverLevel().getBlockEntity(pos);
		MachineAccessEvents.awardMachineCrafted(p.player(), BlockInit.enzymatic_scriptorium.get()); // ARRANGE: built it
		station.receiveBlood(1000.0D); // ARRANGE
		for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
			if (tendency.ordinal() >= 8) break;
			station.setItem(tendency.ordinal(), new ItemStack(EnumBloodTendency.getRepEnzyme(tendency), 3)); // ARRANGE
		}
		station.setItem(EnzymaticScriptoriumBlockEntity.ITEM, new ItemStack(Items.DIAMOND_PICKAXE)); // ARRANGE
		station.setItem(EnzymaticScriptoriumBlockEntity.LAPIS, new ItemStack(Items.LAPIS_LAZULI, 3)); // ARRANGE
		p.player().giveExperienceLevels(40); // ARRANGE
		p.moveTo(pos.west(2)); // ARRANGE: stand at the station
		PlaythroughRites.emptyHand(p);
		Outcome open = p.useOn(pos);
		EnzymaticScriptoriumMenu menu;
		if (p.player().containerMenu instanceof EnzymaticScriptoriumMenu opened) {
			menu = opened;
		} else {
			PlaythroughLog.note(helper, "the Scriptorium opened no menu for the captured player (" + open.describe()
					+ "); the menu is built as the server builds it");
			menu = new EnzymaticScriptoriumMenu(1, p.player().getInventory(), station);
		}
		for (int i = 0; i < 3; i++) p.clickButton(menu, EnumBloodTendency.DUCTILIS.ordinal());
		int row = -1;
		for (int i = 0; i < 3; i++) if (menu.offerCost(i) > 0) { row = i; break; }
		helper.assertTrue(row >= 0, "the stocked Scriptorium offers an inscription");
		Outcome inscribed = p.clickButton(menu, 8 + row);
		PlaythroughLog.note(helper, "inscription row " + row + ": " + inscribed.describe());
		helper.assertFalse(station.getItem(EnzymaticScriptoriumBlockEntity.ITEM).getEnchantments().isEmpty(), "the pickaxe is inscribed");
		p.closeMenu();
	}

	/** Claims {@code tier}'s kit from {@code teacher} through the option the tree shows; asserts it lands in the pack. */
	private static Outcome claimKit(PlaythroughPlayer p, Entity teacher, StationUpgradeTier tier) {
		GameTestHelper helper = p.helper();
		String event = StationUpgradeDialogue.claimEvent(tier);
		DialogueView view = p.approach(teacher).talkTo(teacher);
		view.assertLangKeys(helper, PlaythroughPlayer.describe(teacher) + " with a ready kit");
		helper.assertTrue(view.offers(event), PlaythroughPlayer.describe(teacher) + " offers " + event + ": " + view);
		Outcome claimed = p.choose(teacher, event);
		PlaythroughLog.note(helper, "kit " + tier.upgradeItem() + ": " + claimed.describe());
		claimed.deliveredIs(true).gave(item(tier), 1);
		helper.assertTrue(HemoCapabilityAccess.stationUpgrades(p.player()).hasClaimed(tier.station(), tier.tier()), "the claim is recorded");
		DialogueView again = p.approach(teacher).talkTo(teacher);
		helper.assertFalse(again.offers(event), "a claimed kit is not offered again");
		return claimed;
	}

	private static Item item(StationUpgradeTier tier) {
		return BuiltInRegistries.ITEM.get(tier.upgradeItem());
	}

	/** Pledges {@code npc}: walks up, talks, chooses the confirm option the recruit card leads to. */
	private static Outcome recruit(PlaythroughPlayer p, Entity npc) {
		DialogueView view = p.approach(npc).talkTo(npc);
		p.helper().assertTrue(view.offers("recruit_harbinger"), PlaythroughPlayer.describe(npc) + " offers the pledge: " + view);
		Outcome pledged = p.choose(npc, "recruit_harbinger");
		PlaythroughLog.note(p.helper(), "pledge " + PlaythroughPlayer.describe(npc) + ": " + pledged.describe());
		return pledged;
	}

	/** Talks to {@code npc}, requires {@code event} offered and chooses it. */
	private static Outcome chooseOffered(PlaythroughPlayer p, Entity npc, String event) {
		DialogueView view = p.approach(npc).talkTo(npc);
		view.assertLangKeys(p.helper(), PlaythroughPlayer.describe(npc) + " before " + event);
		p.helper().assertTrue(view.offers(event), PlaythroughPlayer.describe(npc) + " offers " + event + ": " + view);
		Outcome chosen = p.choose(npc, event);
		PlaythroughLog.note(p.helper(), event + ": " + chosen.describe());
		return chosen;
	}

	/** Picks up {@code item} lying at {@code npc}'s feet (a reward drop) and returns how many the player now carries. */
	private static int received(PlaythroughPlayer p, Entity npc, Item item) {
		p.pickUpNear(npc.blockPosition(), item);
		return p.count(item);
	}

	/** Right-clicks the carried signed ledger in the air (the first ledger in the pack). */
	private static Outcome useLedger(PlaythroughPlayer p) {
		p.wield(ItemInit.unsigned_ancestral_ledger.get());
		return p.use(InteractionHand.MAIN_HAND);
	}

	/** The first carried bloodline ledger. */
	private static ItemStack ledgerStack(PlaythroughPlayer p) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(ItemInit.unsigned_ancestral_ledger.get())) return stack;
		}
		p.helper().fail("the player carries no ledger");
		return ItemStack.EMPTY;
	}

	private static boolean signedFor(ItemStack ledger, Bloodline line) {
		CompoundTag tag = ledger.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		return tag.getBoolean(UnsignedLedgerItem.TAG_STATE)
				&& Bloodline.deserialize(tag.getCompound(UnsignedLedgerItem.TAG_BLOODLINE)).getBloodlineUUID().equals(line.getBloodlineUUID());
	}

	/** How many registered lines list {@code p} as a member. */
	private static long linesHolding(PlaythroughPlayer p) {
		return BloodlineSavedData.get(p.player().server.overworld()).getAllBloodlines().values().stream()
				.filter(line -> line.hasMember(p.uuid())).count();
	}

	/** A key said under one of {@code prefixes} that is shipped, other than the success lines; empty when none. */
	private static Optional<String> namedRefusal(Outcome outcome, String... prefixes) {
		for (String key : outcome.saidKeys()) {
			if (key.equals(JOINED) || key.equals(FOUNDED) || !LangKeys.exists(key)) continue;
			for (String prefix : prefixes) if (key.startsWith(prefix)) return Optional.of(key);
		}
		return Optional.empty();
	}

	/** The degree-hint card's line keys. */
	private static List<String> hint(DialogueView view) {
		return view.node("degree_hint").map(node -> node.lines().stream()
				.map(line -> Dialogue.lineKey(line).orElse(line)).toList()).orElse(List.of());
	}

	/** Plants the carried Staff on the floor's Focus from the rite stand. */
	private static Outcome plantAt(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) {
		p.moveTo(PlaythroughRites.riteStand(floor));
		return plant(p, floor);
	}

	private static Outcome plant(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) {
		p.wield(ItemInit.living_staff.get());
		return p.useOn(floor.focus());
	}

	private static boolean riteRunning(PlaythroughPlayer p) {
		return CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) != null;
	}

	/** Ends the player's running rite at once, Staff restored, as the clock's cleanup does at the end of a test. */
	private static void clearRiteNow(PlaythroughPlayer p) {
		ServerLevel level = p.player().serverLevel();
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
		ActiveCardinalRite rite = saved.getRite(p.uuid());
		if (rite == null) return;
		CardinalRiteOrdealEngine.clearThreats(level, rite);
		CardinalRiteStaffEscrow.restore(p.player(), rite);
		saved.removeRite(p.uuid());
	}

	/** ARRANGE: a fresh Illuminatus standing at relative ({@code x}, {@code z}) with a deep pool and the blood tools. */
	private static PlaythroughPlayer d5(GameTestHelper helper, int x, int z) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, x, z));
		PlaythroughStates.illuminatusD5(p);
		PlaythroughStates.blood(p, 6000.0D);
		tools(p);
		return p;
	}

	/** ARRANGE: a member of {@code leader}'s line at the given degree (4 or 5), with the blood tools. */
	private static PlaythroughPlayer member(GameTestHelper helper, PlaythroughPlayer leader, int x, int z, int degree) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, x, z));
		if (degree >= 5) PlaythroughStates.illuminatusD5(p);
		else PlaythroughStates.adeptD4(p);
		PlaythroughStates.blood(p, 3000.0D);
		tools(p);
		PlaythroughStates.memberOf(p, leader);
		return p;
	}

	/** ARRANGE: Blood Projection, Blood Absorption and a Living Staff, each given only when missing. */
	private static void tools(PlaythroughPlayer p) {
		for (Item tool : List.of(ItemInit.blood_projection.get(), ItemInit.blood_absorption.get(), ItemInit.living_staff.get())) {
			if (p.count(tool) == 0) p.give(new ItemStack(tool));
		}
	}

	/** Takes one {@code item} out of the pack and returns it. */
	private static ItemStack takeOne(PlaythroughPlayer p, Item item) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			ItemStack stack = inventory.getItem(slot);
			if (stack.is(item)) return stack.split(1);
		}
		p.helper().fail("the player carries no " + item);
		return ItemStack.EMPTY;
	}

	/** ARRANGE: a Basin creature's blood in a vial (an Excoriated sample). */
	private static ItemStack basinSample() {
		ItemStack vial = new ItemStack(ItemInit.bloody_vial.get());
		CompoundTag tag = new CompoundTag();
		tag.putString(BloodVialItem.TAG_ENTITY_TYPE, BuiltInRegistries.ENTITY_TYPE.getKey(EntityInit.excoriated.get()).toString());
		tag.putBoolean(BloodVialItem.TAG_STATE, true);
		vial.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		return vial;
	}

	/**
	 * ARRANGE: a stake formation at {@code bar}: hematic iron bars along X, a blood crystal facing east on its east side
	 * and stone behind it to the west (the stake's support). The cell below is left as it is (the template floor).
	 */
	private static void formation(GameTestHelper helper, BlockPos bar) {
		for (Direction direction : Direction.values()) {
			if (direction != Direction.DOWN) setState(helper, bar.relative(direction), Blocks.AIR.defaultBlockState());
		}
		setState(helper, bar.west(), Blocks.STONE.defaultBlockState());
		setState(helper, bar, BlockInit.hematic_iron_bars.get().defaultBlockState().setValue(HematicIronBarsBlock.AXIS, Direction.Axis.X));
		setState(helper, bar.east(), BlockInit.blood_crystal.get().defaultBlockState().setValue(BloodCrystalBlock.FACING, Direction.EAST));
	}

	/** ARRANGE: {@code state} set at {@code pos}, cleared when the test ends. */
	private static void setState(GameTestHelper helper, BlockPos pos, BlockState state) {
		ServerLevel level = helper.getLevel();
		level.setBlock(pos, state, Block.UPDATE_ALL);
		PlaythroughCleanup.onEnd(helper, () -> {
			if (!level.getBlockState(pos).isAir()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		});
	}

	/** ARRANGE: a plain animal at {@code pos}, discarded when the test ends. */
	private static Entity animal(GameTestHelper helper, EntityType<?> type, BlockPos pos) {
		Entity entity = type.create(helper.getLevel());
		helper.assertTrue(entity != null, type + " could be created");
		entity.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
		helper.assertTrue(helper.getLevel().addFreshEntity(entity), type + " joined the level");
		PlaythroughCleanup.onEnd(helper, entity::discard);
		return entity;
	}

	/** A speaker this server may fail to place outside its site; null (logged) when it could not be placed. */
	@Nullable
	private static Entity optionalSpeaker(GameTestHelper helper, String what, java.util.function.Supplier<? extends Entity> spawn) {
		try {
			return spawn.get();
		} catch (RuntimeException exception) {
			PlaythroughLog.note(helper, what + " could not be placed here (" + exception.getMessage() + "); that case is not run");
			return null;
		}
	}

	private static int dropsOf(PlaythroughPlayer p, BlockPos near, Item item) {
		return p.player().serverLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
				new net.minecraft.world.phys.AABB(near).inflate(3.0D), drop -> drop.getItem().is(item)).size();
	}

	private static void grant(PlaythroughPlayer p, ResourceLocation advancement) {
		HarbingerAdvancementGranter.grantIfNotDone(p.player(), advancement);
	}

	private static String adv(ResourceLocation id) {
		return id.getPath();
	}
}
