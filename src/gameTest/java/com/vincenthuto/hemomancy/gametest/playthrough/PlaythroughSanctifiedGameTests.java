package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.CovenantThroneBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.MaxBloodLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.DegreeProgression;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.KnownManipulationEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.LiberKnowledgeHelper;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.discovery.MemoHelper;
import com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.encounter.VagrantMindEncounterData;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopic;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerArtificerDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerCicatrixAnchoriteDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerMnemonistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VagrantMindInquiryDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.VeinMasonScarLesson;
import com.vincenthuto.hemomancy.common.entity.utility.HumanitySpriteEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScarPattern;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.menu.tile.functional.MasonsEffigyMenu;
import com.vincenthuto.hemomancy.common.mission.artificer.ArtificerAssignments;
import com.vincenthuto.hemomancy.common.mission.cicatrix_anchorite.VeinMasonAssignments;
import com.vincenthuto.hemomancy.common.mission.shared.HarbingerChapterProgression;
import com.vincenthuto.hemomancy.common.mission.shared.VagrantMindInquiry;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketLedgerAction;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketPrepareScarPattern;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteAllyRole;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePhase;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStaffEscrow;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteAllyService;
import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteOrdealEngine;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MasonsEffigyBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.structure.vagrantmind.VagrantMindPiece;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Band d6_sanctified: the Harbinger player from a written covenant at Degree 5 through the Rite of the Bloodline
 * Covenant into Degree 6, then the three parts of the Living Covenant (the Chamber of Will, the Covenant Throne, the
 * Covenant Vigil) up to the sealed Archon rite, plus the D6 work around them: the Sanguine Monolith's Working craft and
 * its D6 voice, the Vein-Mason's sixth-Degree chain (referral, the Mnemonist's counsel, two Noetic routes, the
 * tier-3 reward), the Vagrant Mind inquiry, the Eternal Covenant, the D6 whisper memo and an NPC sweep at D6.
 *
 * <p>Preconditions that are not under test come from {@link PlaythroughStates} checkpoints or a labelled
 * {@code // ARRANGE} line. Every step under test goes through what a client reaches: the dialogue option and claim
 * payloads, {@code useOn} of blocks (the Focus, a Brazier, the Throne, the Monolith, a rite station), a held channel,
 * {@code interactOn} for a rite helper, the ledger payload, the whisper inbox, the manipulation cast key, real damage
 * then respawn, relog. Decision probes are {@code required = false} with a {@code // DECISION <id>:} line; they assert
 * what a player would expect and log what happens. Scenarios the GameTest server cannot host fail as
 * {@code NOT RUN}.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughSanctifiedGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_d6_sanctified";
	private static final String RITES = "playthrough_d6_sanctified_rites";
	/** The Vagrant Mind visit moves a player into the End and searches its structures: kept apart. */
	private static final String END = "playthrough_d6_sanctified_end";

	/** The rite ground: relative y 3 puts the Focus in the template's floor layer (y 4); sigils reach x 14 to 26. */
	private static final BlockPos RITE_ORIGIN = new BlockPos(20, 3, 12);

	private static final String ALREADY_ATTAINED = "message.hemomancy.cardinal_rite_activation.you_have_already_attained";
	private static final String NO_STRUCTURE = "message.hemomancy.cardinal_rite_activation.the_floor_answers_but_no_structure";
	private static final String NO_FLOOR = "message.hemomancy.cardinal_rite_activation.the_cardinal_focus_cannot_find_a";
	private static final String RANK_SEALED = "message.hemomancy.cardinal_rite_activation.the_rank_rite_remains_sealed_complete";
	private static final String LIVING_COVENANT_CHAPTER = "milestone.hemomancy.chapter.living_covenant";
	private static final String VIGIL_CLOSES = "message.hemomancy.cardinal_rite_completion_effects.the_vigil_closes_around_every_survivor";
	private static final String HELPERS_HOLD = "message.hemomancy.cardinal_rite_completion_effects.required_bloodline_helpers_must_still_hold";
	private static final String COVENANT_ALREADY_SEALED = "message.hemomancy.cardinal_rite_completion_effects.the_covenant_is_already_sealed_its";
	private static final String NOT_PROGENITOR = "block.hemomancy.covenant_throne.not_progenitor";
	private static final String RESPAWN_SET = "block.hemomancy.covenant_throne.respawn_set";
	private static final String TRANCE_ENTERED = "block.hemomancy.covenant_throne.trance_entered";
	private static final String TRANCE_COOLDOWN = "block.hemomancy.covenant_throne.trance_cooldown";
	private static final String MACHINE_LOCKED = "hemomancy.machine_access.locked";
	private static final String ROUTE_RECORDED = "message.hemomancy.vein_mason_assignments.your_current_scar_route_is_recorded";
	private static final String D6_REWARD_SAID = "hemomancy.dialogue.event.anchorite_d6_reward";
	private static final String MONOLITH_D6_LINE1 = "hemomancy.monolith.guidance.sanctified.line1";
	/** The D6 Monolith line that names the Chamber of Will. */
	private static final String MONOLITH_D6_CHAMBER = "hemomancy.monolith.guidance.sanctified.line2";
	private static final String COVENANT_LINE_PREFIX = "hemomancy.vicar.covenant.";
	private static final String PERFORM_ORDER_RITE = "hemomancy.vicar.covenant.perform_hematic_order_rite";
	private static final String MEMORY_RECORDED = "hemomancy.vagrant_mind_inquiry.memory_recorded";
	private static final String BIOLOGY_RECORDED = "hemomancy.vagrant_mind_inquiry.biology_recorded";
	private static final String BEARING = "hemomancy.vagrant_mind_inquiry.bearing";
	private static final String BEARING_MISSING = "hemomancy.vagrant_mind_inquiry.bearing_missing";
	/** Chamber lines that are not the LF-12 explanation: the visit's own timer and warnings, the degree gate. */
	private static final List<String> CHAMBER_ROUTINE_KEYS = List.of("message.hemomancy.chamber_visit.timer",
			"message.hemomancy.chamber_visit.warning", "message.hemomancy.chamber_visit.rite_degree",
			"message.hemomancy.chamber_visit.no_ordeals");
	/** The fungal whisper's check interval at Degree 6 ({@code FungalWhisperEvents.SANCTIFIED_INTERVAL}). */
	private static final int SANCTIFIED_WHISPER_INTERVAL = 18000;

	private PlaythroughSanctifiedGameTests() {
	}

	// =================================================================================================================
	// The chain from the previous band: the Rite of the Bloodline Covenant
	// =================================================================================================================

	/**
	 * From {@code covenantD5}: the Rite of the Bloodline Covenant on its Greater Threshold floor, played. The player
	 * sets the six hematic iron pillars, offers Sanguine Quintessence, puppeteering thread and an amethyst shard in
	 * the braziers and lights them, plants the Staff, pays the anchors and the Bastion sigil, fights the Bloodlicker
	 * wave and seals the daemon. Degree 6, {@code degree_6_sanctified}, the Staff back in the pack. Replanting on the
	 * same refilled floor starts nothing and says why (the already-attained line, or the D-32 floor line).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 11000, batch = RITES)
	public static void sanctifiedRiteByPlay(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.covenantD5(p, PlaythroughStates.defaultHeart(p)); // ARRANGE: the D5 band's end state
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughSites.RiteFloor floor = playSanctifiedRite(helper, p, clock);
		clock.then(() -> {
			returnStaff(p);
			Outcome rite = p.last();
			PlaythroughLog.note(helper, "sanctifiedRiteByPlay", "rite: " + rite.describe());
			helper.assertTrue(p.degree() == 6, "the Bloodline Covenant promotes to degree 6; " + rite);
			helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_DEGREE_6_SANCTIFIED)), "degree_6_sanctified is earned");
			helper.assertTrue(p.count(ItemInit.living_staff.get()) == 1, "the Living Staff comes back once");
			helper.assertTrue(CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) == null, "no rite is left running");
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.SANCTIFIED_D6);
		});
		// The same floor refilled and the Staff planted again at D6.
		clock.then(() -> PlaythroughStates.blood(p, 5000.0D)); // ARRANGE: a rested pool to light the braziers again
		PlaythroughRites.braziers(clock, p, floor);
		clock.then(() -> {
			p.moveTo(PlaythroughRites.riteStand(floor));
			p.wield(ItemInit.living_staff.get());
			Outcome again = p.useOn(floor.focus());
			boolean attained = said(again, ALREADY_ATTAINED);
			boolean noStructure = said(again, NO_STRUCTURE) || said(again, NO_FLOOR);
			PlaythroughLog.note(helper, "sanctifiedRiteByPlay", "replant at D6 said " + again.saidKeys()
					+ (attained ? "" : " (D-32 family: the floor line, not the already-attained line)"));
			helper.assertTrue(CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) == null,
					"a second Bloodline Covenant does not start at degree 6; " + again);
			helper.assertTrue(attained || noStructure, "the replant says why it starts nothing; " + again);
			helper.assertTrue(p.player().getMainHandItem().is(ItemInit.living_staff.get()), "the Staff stays in hand");
		}).run();
	}

	/**
	 * The D5 to D6 chain as steps on {@code clock}, for {@link #sanctifiedRiteByPlay} and the late run: lays the
	 * Greater Threshold floor now, plays the Rite of the Bloodline Covenant and ends checking the player against
	 * {@link PlaythroughStates.Checkpoint#SANCTIFIED_D6}. {@code p} must be at
	 * {@link PlaythroughStates.Checkpoint#COVENANT_D5}.
	 */
	public static void chain(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock) {
		playSanctifiedRite(helper, p, clock);
		clock.then(() -> {
			returnStaff(p);
			PlaythroughStates.assertAt(helper, p, PlaythroughStates.Checkpoint.SANCTIFIED_D6);
		});
	}

	/**
	 * The Living Covenant as steps on {@code clock}, for the late run, each part played: two Sanguine Monoliths raised
	 * by their Working craft (the second is carried on for the D7 shatter), the Covenant Throne's Working craft fed with
	 * the first, the Throne set down in the Fane and bound, the Covenant Vigil with {@code recruit} summoned by the
	 * ledger and assigned to a station at INSCRIPTION, then the Rite of the Chamber of Will to attunement. Only the
	 * Chamber return itself is ARRANGE (the Chamber of Will is not loaded here; logged as proof owed). {@code p} must
	 * be at {@link PlaythroughStates.Checkpoint#SANCTIFIED_D6} with {@code recruit} pledged to the line;
	 * {@code clearFloor} readies the template floor between rites (the late run keeps the Fane heart and recruit).
	 */
	public static void livingCovenantChain(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock, Entity recruit,
			Runnable clearFloor) {
		ServerLevel level = helper.getLevel();
		Item monolith = BlockInit.sanguine_monolith.get().asItem();
		for (int craft = 1; craft <= 2; craft++) {
			int made = craft;
			clock.segment("Sanguine Monolith " + made, c -> {
				tools(p); // ARRANGE
				PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "working/sanguine_monolith");
				List<BlockPos> subject = new ArrayList<>();
				c.then(() -> PlaythroughStates.blood(p, 5000.0D)); // ARRANGE: a rested pool to light the braziers
				PlaythroughRites.prepare(c, p, floor);
				c.then(() -> {
							subject.addAll(PlaythroughRites.workingSubject(p, floor, "working/sanguine_monolith"));
							PlaythroughStates.blood(p, 5000.0D); // ARRANGE
							p.moveTo(PlaythroughRites.riteStand(floor));
							p.wield(ItemInit.living_staff.get());
						})
						.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().maxTicks(8000)
								.extraAim(feedSubject(p, subject, false))))
						.then(() -> {
							returnStaff(p);
							int condensed = dropsNear(level, floor.focus(), monolith, 8.0D);
							helper.assertTrue(condensed == 1, "Monolith craft " + made + " condenses one Monolith, found "
									+ condensed + "; " + p.last());
							p.pickUpNear(floor.focus(), BlockInit.sanguine_monolith.get());
							helper.assertTrue(p.count(monolith) == made, "the player carries " + made + " Monolith(s), not "
									+ p.count(monolith));
							helper.assertTrue(p.player().getStats().getValue(Stats.ITEM_CRAFTED.get(monolith)) > 0,
									"the Working craft credits the player with the Monolith");
							clearFloor.run();
						});
			});
		}
		BlockPos throne = PlaythroughSites.stand(helper, 8, 18);
		clock.segment("the Covenant Throne", c -> {
			PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "working/covenant_throne");
			List<BlockPos> subject = new ArrayList<>();
			c.then(() -> PlaythroughStates.blood(p, 5000.0D)); // ARRANGE
			PlaythroughRites.prepare(c, p, floor);
			c.then(() -> {
						subject.addAll(PlaythroughRites.workingSubject(p, floor, "working/covenant_throne"));
						helper.assertTrue(p.count(monolith) == 1, "the Throne's subject takes one of the two carried Monoliths; "
								+ p.count(monolith) + " left");
						PlaythroughStates.blood(p, 5000.0D); // ARRANGE
						p.moveTo(PlaythroughRites.riteStand(floor));
						p.wield(ItemInit.living_staff.get());
					})
					.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().maxTicks(9500)
							.extraAim(feedSubject(p, subject, true))))
					.then(() -> {
						returnStaff(p);
						int thrones = dropsNear(level, floor.focus(), BlockInit.covenant_throne.get().asItem(), 8.0D);
						helper.assertTrue(thrones == 1, "exactly one Covenant Throne condenses, found " + thrones + "; " + p.last());
						p.pickUpNear(floor.focus(), BlockInit.covenant_throne.get());
						p.moveTo(throne.south(2));
						Outcome placed = p.place(throne, BlockInit.covenant_throne.get());
						helper.assertTrue(level.getBlockState(throne).is(BlockInit.covenant_throne.get()), "the Throne sets down; " + placed);
						PlaythroughRites.emptyHand(p);
						Outcome sit = p.useOn(throne);
						PlaythroughLog.note(helper, "livingCovenantChain", "Throne bound: " + sit.describe());
						sit.said(RESPAWN_SET).earned(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND));
						p.player().stopRiding(); // the player gets up off the Throne (sneak)
						LedgerView ledger = p.ledger();
						helper.assertTrue(ledger.packet().covenantThroneBound(), "the ledger shows the Throne bound: " + ledger);
						helper.assertTrue(p.count(monolith) == 1, "one Monolith is carried on for the D7 shatter");
						clearFloor.run();
					});
		});
		long[] assignedAt = {-1L};
		clock.segment("the Covenant Vigil", c -> {
			PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "covenant_vigil");
			c.then(() -> PlaythroughStates.blood(p, 5000.0D)); // ARRANGE
			PlaythroughRites.prepare(c, p, floor);
			c.then(() -> {
						PlaythroughStates.blood(p, 5000.0D); // ARRANGE
						p.moveTo(PlaythroughRites.riteStand(floor));
						Outcome summon = p.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS);
						double after = recruit.distanceTo(p.player());
						PlaythroughLog.note(helper, "livingCovenantChain", "the ledger summon before the Vigil: " + summon.describe()
								+ " recruit " + after + " blocks away");
						// The summon leaves a recruit already within 16 blocks where he is ("already nearby"); the rite walks him to
						// his station once assigned.
						helper.assertTrue(recruit.isAlive() && (after < 6.0D || summon.saidKeys().contains("hemomancy.ledger.summon.already_near")
								&& after < 16.0D), "the ledger's summon brings the recruit or finds him near; " + summon);
						p.wield(ItemInit.living_staff.get());
					})
					.step(() -> PlaythroughClock.rite(p, floor.focus(), fighting(p).releaseOnStill()
							.onPhase(CardinalRitePhase.INSCRIPTION, (rite, ticks) -> {
								if (assignedAt[0] >= 0 || ticks % 20 != 0) return;
								assignNpc(p, recruit);
								if (rite.getAllyRoles().containsKey(recruit.getUUID())) assignedAt[0] = helper.getTick();
							})))
					.then(() -> {
						returnStaff(p);
						PlaythroughLog.note(helper, "livingCovenantChain", "Vigil: " + p.last().describe() + " recruit assigned at "
								+ assignedAt[0]);
						helper.assertTrue(assignedAt[0] >= 0, "the summoned recruit was assigned to a station at INSCRIPTION");
						helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED)),
								"the kept vigil earns covenant_vigil_completed; " + p.last());
						clearFloor.run();
					});
		});
		int[] mark = new int[1];
		clock.segment("the Rite of the Chamber of Will", c -> {
			PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "chamber_of_will");
			c.then(() -> PlaythroughStates.blood(p, 5000.0D)); // ARRANGE
			PlaythroughRites.prepare(c, p, floor);
			c.then(() -> {
						PlaythroughStates.blood(p, 5000.0D); // ARRANGE
						mark[0] = p.mark();
						p.wield(ItemInit.living_staff.get());
					})
					.step(() -> PlaythroughClock.rite(p, floor.focus(), fighting(p).maxTicks(8000)))
					.then(() -> {
						returnStaff(p);
						List<String> said = new ArrayList<>();
						for (Outcome.ChatLine line : p.chatSince(mark[0])) said.addAll(line.keys());
						PlaythroughLog.note(helper, "livingCovenantChain", "Chamber rite: attuned=" + ChamberVisitService.isAttuned(p.player())
								+ " said " + said);
						helper.assertTrue(ChamberVisitService.isAttuned(p.player()), "the Chamber rite attunes the player; " + p.last());
						helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_CHAMBER_RITE_ATTUNED)), "chamber_rite_attuned is earned");
						if (helper.getLevel().getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL) == null) {
							helper.assertTrue(said.contains("message.hemomancy.chamber_visit.rite_unavailable"),
									"LF-12: the player is told no visit began; said " + said);
						}
						// ARRANGE (NOT RUN: Chamber of Will not loaded): the visit and its paired exit, recorded as the
						// return path records it (the part, then the chapter check that path runs).
						PlaythroughLog.note(helper, "NOT RUN (ARRANGE stands in): the Chamber of Will visit and return "
								+ "(chamber_returned); proof owed: " + PlaythroughNotRun.CHAMBER);
						HarbingerAdvancementGranter.grantIfNotDone(p.player(), HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED);
						HarbingerChapterProgression.tryCompleteLivingCovenant(p.player());
						clearFloor.run();
					});
		});
	}

	/**
	 * A member's route: the leader founded the line and the Fane; member B (Degree 5) stands inside the Fane and
	 * holds Blood Projection on the leader's Consecrated Bloodwell, which writes B's own covenant proof, then performs
	 * the Rite of the Bloodline Covenant on their own floor and reaches Degree 6.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 11500, batch = RITES)
	public static void memberRouteToD6(GameTestHelper helper) {
		PlaythroughPlayer leader = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.covenantD5(leader, PlaythroughStates.defaultHeart(leader)); // ARRANGE
		BlockPos heart = PlaythroughStates.defaultHeart(leader);
		PlaythroughPlayer member = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 6, 8));
		PlaythroughStates.illuminatusD5(member); // ARRANGE
		PlaythroughStates.memberOf(member, leader); // ARRANGE: the signed-ledger join is the D5 band's
		tools(member); // ARRANGE
		PlaythroughStates.blood(member, 3000.0D); // ARRANGE
		helper.assertFalse(member.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE)),
				"the member has no covenant proof before the deposit");
		PlaythroughClock clock = PlaythroughClock.start(helper)
				.then(() -> member.moveTo(heart.south(2))) // ARRANGE: walks up to the Bloodwell inside the Fane
				.step(() -> member.holdUntil(ItemInit.blood_projection.get(), 200, () -> Vec3.atCenterOf(heart),
						() -> member.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE))))
				.then(() -> {
					Outcome deposit = member.last();
					PlaythroughLog.note(helper, "memberRouteToD6", "deposit: " + deposit.describe());
					helper.assertTrue(member.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_WRITTEN_IN_PLACE)),
							"the member's own deposit into the leader's Bloodwell writes the covenant; " + deposit);
					helper.assertTrue(deposit.bloodSpent() > 0.0D, "the deposit pays from the member's pool; " + deposit);
				});
		playSanctifiedRite(helper, member, clock);
		clock.then(() -> {
			returnStaff(member);
			PlaythroughLog.note(helper, "memberRouteToD6", "member rite: " + member.last().describe());
			helper.assertTrue(member.degree() == 6, "the member reaches degree 6 by the rite; " + member.last());
			helper.assertTrue(member.has(adv(HarbingerAdvancementGranter.ADV_DEGREE_6_SANCTIFIED)), "the member earns degree_6_sanctified");
			helper.assertTrue(PlaythroughStates.lineOf(member) != null
					&& PlaythroughStates.lineOf(member).getLeaderUUID().equals(leader.uuid()), "the member is still in the leader's line");
		}).run();
	}

	// =================================================================================================================
	// The D6 Vicar's guidance
	// =================================================================================================================

	/**
	 * The D6 Vicar's Living Covenant list in all six orders: one fresh Sanctified player per order, the three parts
	 * recorded one by one (ARRANGE, each as its own path records it). After each part the Vicar's hint loses exactly
	 * that line, the Assignment Ledger's Living Covenant bar counts the same parts, and once all three are done the
	 * hint is the Rite of the Hematic Order and the ledger reads the chapter complete.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void d6VicarGuidanceShrinks(GameTestHelper helper) {
		Entity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 20, 12));
		List<List<CovenantPart>> orders = List.of(
				List.of(CovenantPart.CHAMBER, CovenantPart.THRONE, CovenantPart.VIGIL),
				List.of(CovenantPart.CHAMBER, CovenantPart.VIGIL, CovenantPart.THRONE),
				List.of(CovenantPart.THRONE, CovenantPart.CHAMBER, CovenantPart.VIGIL),
				List.of(CovenantPart.THRONE, CovenantPart.VIGIL, CovenantPart.CHAMBER),
				List.of(CovenantPart.VIGIL, CovenantPart.CHAMBER, CovenantPart.THRONE),
				List.of(CovenantPart.VIGIL, CovenantPart.THRONE, CovenantPart.CHAMBER));
		for (int i = 0; i < orders.size(); i++) {
			List<CovenantPart> order = orders.get(i);
			PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 2, 2 + 3 * i));
			PlaythroughStates.sanctifiedD6(p); // ARRANGE: hearts at x 6, clear of the others
			List<CovenantPart> done = new ArrayList<>();
			checkCovenantGuidance(helper, p, vicar, done, order);
			for (CovenantPart part : order) {
				// ARRANGE: the part as its own path records it (the grant, then the chapter check that path runs).
				HarbingerAdvancementGranter.grantIfNotDone(p.player(), part.advancement);
				HarbingerChapterProgression.tryCompleteLivingCovenant(p.player());
				done.add(part);
				checkCovenantGuidance(helper, p, vicar, done, order);
			}
			helper.assertTrue(p.has(adv(HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE)),
					"the three parts complete the Living Covenant in the order " + order);
		}
		helper.succeed();
	}

	/** The Vicar's covenant lines and the ledger bar for a player who has done {@code done}. */
	private static void checkCovenantGuidance(GameTestHelper helper, PlaythroughPlayer p, Entity vicar,
			List<CovenantPart> done, List<CovenantPart> order) {
		p.approach(vicar);
		DialogueView view = p.talkTo(vicar);
		String label = "the D6 Vicar after " + done + " (order " + order + ")";
		view.assertLangKeys(helper, label).assertNoDeadEnds(helper, label);
		helper.assertTrue(view.hasNode("degree_hint"), label + ": the covenant hint can be reached: " + view.describe());
		List<String> lines = covenantLines(view);
		List<String> expected = new ArrayList<>();
		for (CovenantPart part : CovenantPart.values()) if (!done.contains(part)) expected.add(part.line);
		if (expected.isEmpty()) expected.add(PERFORM_ORDER_RITE);
		if (p.count(ItemInit.harbinger_assignment_ledger.get()) == 0) {
			p.give(new ItemStack(ItemInit.harbinger_assignment_ledger.get())); // ARRANGE: the Vicar normally hands it out
			PlaythroughLog.note(helper, "d6VicarGuidanceShrinks", "the D6 Vicar did not hand out an Assignment Ledger");
		}
		LedgerView ledger = p.ledger();
		PlaythroughLog.note(helper, "d6VicarGuidanceShrinks", label + ": lines " + lines + ", ledger " + ledger);
		helper.assertTrue(lines.equals(expected), label + ": the hint shows " + lines + ", expected " + expected);
		helper.assertTrue(ledger.livingCovenantProgress() == done.size(),
				label + ": the ledger counts " + ledger.livingCovenantProgress() + " parts, expected " + done.size());
		helper.assertTrue(ledger.livingCovenantComplete() == (done.size() == CovenantPart.values().length),
				label + ": the ledger's chapter state agrees: " + ledger);
	}

	/** The Vicar's covenant guidance lines, as the degree hint node carries them. */
	private static List<String> covenantLines(DialogueView view) {
		List<String> keys = new ArrayList<>();
		DialogueNode hint = view.tree().getNode("degree_hint");
		if (hint == null) return keys;
		for (String line : hint.lines()) {
			Dialogue.lineKey(line).filter(key -> key.startsWith(COVENANT_LINE_PREFIX)).ifPresent(keys::add);
		}
		return keys;
	}

	private enum CovenantPart {
		CHAMBER(HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED, "hemomancy.vicar.covenant.chamber_return"),
		THRONE(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND, "hemomancy.vicar.covenant.bind_throne"),
		VIGIL(HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED, "hemomancy.vicar.covenant.complete_vigil");

		private final ResourceLocation advancement;
		private final String line;

		CovenantPart(ResourceLocation advancement, String line) {
			this.advancement = advancement;
			this.line = line;
		}
	}

	// =================================================================================================================
	// The Sanguine Monolith
	// =================================================================================================================

	/**
	 * The Sanguine Monolith raised by its Working craft at D6: the Greater Working floor laid (ARRANGE), the subject
	 * built block by block, Sanguine Quintessence and an echo shard offered and lit, a Sanguine Formation seated, the
	 * Staff planted and the subject fed. One Monolith condenses, the craft credits the player, and the placed
	 * Monolith speaks the D6 tree, which points to the Chamber of Will. A held item gets the item inquiry only.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 11000, batch = RITES)
	public static void monolithWorkingAndD6Tree(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		tools(p); // ARRANGE
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "working/sanguine_monolith");
		List<BlockPos> subject = new ArrayList<>();
		BlockPos spot = PlaythroughSites.stand(helper, 6, 18);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
					subject.addAll(PlaythroughRites.workingSubject(p, floor, "working/sanguine_monolith"));
					p.moveTo(PlaythroughRites.riteStand(floor));
					p.wield(ItemInit.living_staff.get());
				})
				.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().maxTicks(8000)
						.extraAim(feedSubject(p, subject, false))))
				.then(() -> {
					returnStaff(p);
					int monoliths = dropsNear(level, floor.focus(), BlockInit.sanguine_monolith.get().asItem(), 8.0D);
					PlaythroughLog.note(helper, "monolithWorkingAndD6Tree", "craft: " + p.last().describe() + " monoliths=" + monoliths);
					helper.assertTrue(monoliths == 1, "exactly one Sanguine Monolith condenses, found " + monoliths);
					helper.assertTrue(p.player().getStats().getValue(Stats.ITEM_CRAFTED.get(BlockInit.sanguine_monolith.get().asItem())) > 0,
							"the Working craft credits the player with the Monolith");
					p.pickUpNear(floor.focus(), BlockInit.sanguine_monolith.get());
					helper.assertTrue(p.count(BlockInit.sanguine_monolith.get()) == 1, "the player carries the Monolith");
					p.moveTo(spot.south(2));
					Outcome placed = p.place(spot, BlockInit.sanguine_monolith.get());
					helper.assertTrue(level.getBlockState(spot).is(BlockInit.sanguine_monolith.get()),
							"the condensed Monolith sets down; " + placed);
					DialogueView stone = p.talkToBlock(spot);
					PlaythroughLog.note(helper, "monolithWorkingAndD6Tree", "D6 Monolith: " + stone.describe() + " lines " + stone.lineKeys());
					helper.assertTrue(stone.entityId() == SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID, "the Monolith speaks as a block");
					stone.assertLangKeys(helper, "the D6 Monolith").assertNoDeadEnds(helper, "the D6 Monolith");
					helper.assertTrue(stone.lineKeys().contains(MONOLITH_D6_LINE1) && stone.lineKeys().contains(MONOLITH_D6_CHAMBER),
							"the D6 Monolith speaks the Sanctified guidance, which names the Chamber of Will: " + stone.lineKeys());
					helper.assertFalse(said(placed, MACHINE_LOCKED) || p.chat().stream().anyMatch(line -> line.has(MACHINE_LOCKED)),
							"the crafted Monolith never answers 'craft one yourself'");
					p.give(new ItemStack(Items.BONE)); // ARRANGE: something in hand to ask about
					p.wield(Items.BONE);
					Optional<DialogueView> held = p.useOnForDialogue(spot);
					helper.assertTrue(held.isPresent(), "a held item opens the Monolith's hub");
					PlaythroughLog.note(helper, "monolithWorkingAndD6Tree", "held bone: " + held.get().describe()
							+ " lines " + held.get().lineKeys());
					held.get().assertLangKeys(helper, "the Monolith with an item in hand");
					helper.assertTrue(held.get().lineKeys().contains(MONOLITH_D6_CHAMBER),
							"a held item keeps the D6 guidance: " + held.get().lineKeys());
				})
				.run();
	}

	/**
	 * A Sanguine Formation carried to a Working floor's Focus and right-clicked, as the Focus's own line ("the item
	 * seats in the Cardinal Focus as a rite medium") leads a player to expect. The Formation is also an activation
	 * tool, so the plain click tries the floor instead and only a sneak-click seats it.
	 */
	// DECISION D6-6: a Sanguine Formation medium seats in the Cardinal Focus only with sneak; a plain right-click tries
	// to start a rite and says "no rite matches this structure", and nothing points to sneaking. Options: seat a
	// Formation on a plain click when the floor wants it as a medium / say "sneak to seat it" on that refusal / keep.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = RITES, required = false)
	public static void formationSeatsOnPlainClick(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "working/sanguine_monolith");
		p.give(new ItemStack(ItemInit.sanguine_formation.get())); // ARRANGE: made elsewhere
		p.moveTo(PlaythroughRites.riteStand(floor));
		p.wield(ItemInit.sanguine_formation.get());
		Outcome click = p.useOn(floor.focus());
		boolean seated = helper.getLevel().getBlockEntity(floor.focus())
				instanceof com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity focus && focus.hasMedium();
		PlaythroughLog.note(helper, "formationSeatsOnPlainClick", "D6-6: plain click seated=" + seated + " said " + click.saidKeys());
		helper.assertTrue(seated, "D6-6 expected: a plain right-click seats the Formation the floor wants; said " + click.saidKeys());
		helper.succeed();
	}

	// =================================================================================================================
	// The Chamber of Will
	// =================================================================================================================

	/**
	 * The Rite of the Chamber of Will at D6, played on its Grand Threshold floor (pillars, an echo shard, Bastion and
	 * Reservoir sigils, the Bloodlicker wave). The GameTest server has no Chamber level, so the visit cannot start:
	 * the rite still attunes (unchanged, D6-5), the player stays where they are, and now a line says why no visit
	 * began.
	 */
	// FIX: LF-12 the rite completed with no line and no visit; it now says why (a new message.hemomancy.chamber* key,
	// for example message.hemomancy.chamber_visit.rite_unavailable). Attunement is unchanged (D6-5).
	@GameTest(template = EMPTY, timeoutTicks = 10500, batch = RITES)
	public static void chamberRiteSaysWhyNoVisit(GameTestHelper helper) {
		playChamberRite(helper, (p, said) -> {
			boolean chamberLoaded = helper.getLevel().getServer().getLevel(ChamberOfWillManager.CHAMBER_OF_WILL) != null;
			List<String> chamberKeys = new ArrayList<>();
			for (Outcome.ChatLine line : said) {
				for (String key : line.keys()) {
					if (key.startsWith("message.hemomancy.chamber") && !CHAMBER_ROUTINE_KEYS.contains(key)) chamberKeys.add(key);
				}
			}
			PlaythroughLog.note(helper, "chamberRiteSaysWhyNoVisit", "chamber loaded=" + chamberLoaded + " attuned="
					+ ChamberVisitService.isAttuned(p.player()) + " level=" + p.player().level().dimension().location()
					+ " chamber lines " + chamberKeys + " all said " + said);
			if (chamberLoaded) {
				PlaythroughLog.note(helper, "chamberRiteSaysWhyNoVisit", "the Chamber is loaded here; the visit premise does not hold");
				return;
			}
			helper.assertTrue(ChamberVisitService.isAttuned(p.player()), "the completed rite attunes the player (unchanged)");
			helper.assertTrue(p.player().level() == helper.getLevel(), "no visit moves the player");
			helper.assertFalse(chamberKeys.isEmpty(), "LF-12: the player is told why no Chamber visit began; said " + said);
			for (String key : chamberKeys) helper.assertTrue(LangKeys.exists(key), "the Chamber line " + key + " has English");
		});
	}

	/** Should the Rite of the Chamber of Will attune when the visit cannot start? A player would expect not. */
	// DECISION D6-5: the Chamber of Will rite attunes even when the visit cannot start. Options: refuse before
	// completion (check the Chamber can take the player before the rite resolves) / attune and say so (LF-12 adds the
	// line) / keep.
	@GameTest(template = EMPTY, timeoutTicks = 10500, batch = RITES, required = false)
	public static void chamberRiteAttunesWithoutVisit(GameTestHelper helper) {
		playChamberRite(helper, (p, said) -> {
			boolean attuned = ChamberVisitService.isAttuned(p.player());
			PlaythroughLog.note(helper, "chamberRiteAttunesWithoutVisit", "D6-5: attuned=" + attuned + " chamber_rite_attuned="
					+ p.has(adv(HarbingerAdvancementGranter.ADV_CHAMBER_RITE_ATTUNED)) + " level="
					+ p.player().level().dimension().location() + " said " + said);
			helper.assertFalse(attuned, "D6-5 expected: a rite whose visit cannot start does not attune");
		});
	}

	/** The Living Covenant's Chamber part: enter and come back out. The GameTest server has no Chamber of Will. */
	@GameTest(template = EMPTY, timeoutTicks = 20, batch = BATCH, required = false)
	public static void chamberReturn(GameTestHelper helper) {
		PlaythroughNotRun.require(helper, ChamberOfWillManager.CHAMBER_OF_WILL,
				"the Chamber of Will entry and return (ADV_CHAMBER_RETURNED)", PlaythroughNotRun.CHAMBER);
		PlaythroughNotRun.notRun(helper, "the Chamber return needs the paired exit a client drives (ADV_CHAMBER_RETURNED)",
				PlaythroughNotRun.CHAMBER);
	}

	/** Plays the Chamber of Will rite for a fresh Sanctified player, then hands the player and what was said to {@code check}. */
	private static void playChamberRite(GameTestHelper helper, BiConsumer<PlaythroughPlayer, List<Outcome.ChatLine>> check) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		tools(p); // ARRANGE
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "chamber_of_will");
		int[] mark = new int[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
					mark[0] = p.mark();
					p.wield(ItemInit.living_staff.get());
				})
				.step(() -> PlaythroughClock.rite(p, floor.focus(), fighting(p).maxTicks(8000)))
				.then(() -> {
					returnStaff(p);
					check.accept(p, p.chatSince(mark[0]));
				})
				.run();
	}

	// =================================================================================================================
	// The Covenant Throne
	// =================================================================================================================

	/**
	 * The Covenant Throne at D6: its Working craft (a Sanguine Monolith in the subject, fed 8000 mL), the condensed
	 * Throne placed, a first right-click claims the respawn point and seats the progenitor ({@code respawn_set},
	 * {@code covenant_throne_bound}, the ledger's Throne row), a second enters the Covenant Trance for 2500 mL through
	 * the blood ledger, a third is refused on cooldown, and after a death the respawn point is the Throne's.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 12000, batch = RITES)
	public static void covenantThroneCraftBindTrance(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		tools(p); // ARRANGE
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE
		// ARRANGE: the Monolith that goes into the subject was raised by this player (monolithWorkingAndD6Tree plays it).
		MachineAccessEvents.awardMachineCrafted(p.player(), BlockInit.sanguine_monolith.get());
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "working/covenant_throne");
		List<BlockPos> subject = new ArrayList<>();
		BlockPos throne = PlaythroughSites.stand(helper, 8, 18);
		BlockPos[] respawnAt = new BlockPos[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
					subject.addAll(PlaythroughRites.workingSubject(p, floor, "working/covenant_throne"));
					p.moveTo(PlaythroughRites.riteStand(floor));
					p.wield(ItemInit.living_staff.get());
				})
				.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().maxTicks(9500)
						.extraAim(feedSubject(p, subject, true))))
				.then(() -> {
					returnStaff(p);
					int thrones = dropsNear(level, floor.focus(), BlockInit.covenant_throne.get().asItem(), 8.0D);
					int monolithsLeft = 0;
					for (BlockPos cell : subject) if (level.getBlockState(cell).is(BlockInit.sanguine_monolith.get())) monolithsLeft++;
					PlaythroughLog.note(helper, "covenantThroneCraftBindTrance", "craft: " + p.last().describe() + " thrones=" + thrones
							+ " monoliths left in the subject=" + monolithsLeft);
					helper.assertTrue(thrones == 1, "exactly one Covenant Throne condenses, found " + thrones);
					helper.assertTrue(monolithsLeft == 0, "the craft consumes the Monolith in its subject");
					p.pickUpNear(floor.focus(), BlockInit.covenant_throne.get());
					p.moveTo(throne.south(2));
					Outcome placed = p.place(throne, BlockInit.covenant_throne.get());
					helper.assertTrue(level.getBlockState(throne).is(BlockInit.covenant_throne.get()), "the Throne sets down; " + placed);
					PlaythroughRites.emptyHand(p);
					Outcome sit = p.useOn(throne);
					PlaythroughLog.note(helper, "covenantThroneCraftBindTrance", "sit: " + sit.describe());
					sit.said(RESPAWN_SET).earned(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND));
					helper.assertTrue(p.player().getVehicle() != null, "the progenitor is seated on the Throne");
					if (p.count(ItemInit.harbinger_assignment_ledger.get()) == 0) {
						p.give(new ItemStack(ItemInit.harbinger_assignment_ledger.get())); // ARRANGE: the Vicar hands it out
					}
					LedgerView ledger = p.ledger();
					helper.assertTrue(ledger.packet().covenantThroneBound() && ledger.livingCovenantProgress() == 1,
							"the ledger shows the Throne bound: " + ledger);
					PlaythroughStates.blood(p, 5000.0D); // ARRANGE: a rested pool
					PlaythroughRites.emptyHand(p);
					Outcome trance = p.useOn(throne);
					PlaythroughLog.note(helper, "covenantThroneCraftBindTrance", "trance: " + trance.describe());
					trance.said(TRANCE_ENTERED);
					helper.assertTrue(Math.abs(trance.bloodSpent() - CovenantThroneBlock.TRANCE_BLOOD_COST) < 1.0D,
							"the Trance costs 2500 mL; " + trance);
					var recorded = BloodFlowLedger.collect(p.player(), HemoCapabilityAccess.getBloodVolume(p.player())).contributions();
					PlaythroughLog.note(helper, "covenantThroneCraftBindTrance", "blood ledger after the Trance: "
							+ recorded.stream().map(c -> c.sourceId() + "=" + c.appliedPerTick()).toList());
					helper.assertTrue(recorded.stream().anyMatch(c -> c.sourceId().contains("covenant_throne_trance")),
							"the Trance is recorded in the blood ledger: " + recorded);
					Outcome cooldown = p.useOn(throne);
					cooldown.said(TRANCE_COOLDOWN);
					helper.assertTrue(cooldown.bloodSpent() < 1.0D, "a Trance on cooldown costs nothing; " + cooldown);
					BlockState state = level.getBlockState(throne);
					respawnAt[0] = throne.relative(state.getValue(CovenantThroneBlock.FACING));
					p.die();
					p.respawn();
					PlaythroughLog.note(helper, "covenantThroneCraftBindTrance", "respawn point after death: "
							+ p.player().getRespawnPosition() + " in " + p.player().getRespawnDimension().location()
							+ ", expected " + respawnAt[0]);
					helper.assertTrue(respawnAt[0].equals(p.player().getRespawnPosition())
							&& level.dimension().equals(p.player().getRespawnDimension()),
							"after a death the player respawns at the Throne: " + p.player().getRespawnPosition());
				})
				.run();
	}

	/** A Degree 6 member of the line at the leader's Throne: what a player would expect is some way to bind it. */
	// DECISION D6-1: only the progenitor can bind a Covenant Throne, so a member who reached D6 is told not_progenitor
	// and can never complete the Living Covenant (D7 is shut for members). Options: members bind the leader's Throne /
	// another member proof / keep and say so.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void memberCannotBindThrone(GameTestHelper helper) {
		PlaythroughPlayer leader = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(leader); // ARRANGE
		PlaythroughPlayer member = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 8));
		PlaythroughStates.illuminatusD5(member); // ARRANGE
		PlaythroughStates.memberOf(member, leader); // ARRANGE
		PlaythroughStates.covenantProof(member); // ARRANGE: the member's Bloodwell deposit (memberRouteToD6 plays it)
		helper.assertTrue(DegreeProgression.advance(member.player(), 6), "ARRANGE: the member's Bloodline Covenant");
		BlockPos throne = PlaythroughSites.covenantThrone(leader, PlaythroughSites.stand(helper, 12, 16)); // ARRANGE
		// ARRANGE: the member crafted a Throne too, so the refusal under test is the progenitor rule.
		MachineAccessEvents.awardMachineCrafted(member.player(), BlockInit.covenant_throne.get());
		member.moveTo(throne.south(2));
		PlaythroughRites.emptyHand(member);
		Outcome sat = member.useOn(throne);
		member.give(new ItemStack(ItemInit.harbinger_assignment_ledger.get())); // ARRANGE
		LedgerView ledger = member.ledger();
		PlaythroughLog.note(helper, "memberCannotBindThrone", "D6-1: member at the leader's Throne said " + sat.saidKeys()
				+ " bound=" + member.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND)) + " ledger " + ledger
				+ " not_progenitor=" + said(sat, NOT_PROGENITOR));
		helper.assertTrue(member.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND)),
				"D6-1 expected: a D6 member has a way to bind the Throne part of the Living Covenant; said " + sat.saidKeys());
		helper.succeed();
	}

	/** A progenitor of another line at this line's Throne: the Vicar says "your Fane", so a player would expect a refusal. */
	// DECISION D6-2: any progenitor can bind on any Throne, another line's included, though the Vicar says "your
	// Fane". Options: require the binder's own Fane / owner-stamp the Throne / reword.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void anyProgenitorBindsAnyThrone(GameTestHelper helper) {
		PlaythroughPlayer owner = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(owner); // ARRANGE: line A, its Fane at home + 4 east
		PlaythroughPlayer other = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 12));
		PlaythroughStates.foundedLineD5(other); // ARRANGE: line B, founded by this player
		PlaythroughStates.covenantProof(other); // ARRANGE: B's own Fane is elsewhere
		helper.assertTrue(DegreeProgression.advance(other.player(), 6), "ARRANGE: B's Bloodline Covenant");
		BlockPos throne = PlaythroughSites.covenantThrone(owner, PlaythroughSites.stand(helper, 14, 16)); // ARRANGE: in A's Fane
		MachineAccessEvents.awardMachineCrafted(other.player(), BlockInit.covenant_throne.get()); // ARRANGE
		other.moveTo(throne.south(2));
		PlaythroughRites.emptyHand(other);
		Outcome sat = other.useOn(throne);
		var fanes = com.vincenthuto.hemomancy.common.event.worldevent.FoundingFaneSavedData.get(helper.getLevel());
		PlaythroughLog.note(helper, "anyProgenitorBindsAnyThrone", "D6-2: B at A's Throne said " + sat.saidKeys()
				+ " bound=" + other.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND)) + " respawn="
				+ other.player().getRespawnPosition() + " throne in A's Fane=" + fanes.isWithinFane(owner.uuid(), throne)
				+ " in B's Fane=" + fanes.isWithinFane(other.uuid(), throne));
		helper.assertFalse(other.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND)),
				"D6-2 expected: another line's progenitor cannot bind a Throne inside this line's Fane; said " + sat.saidKeys());
		helper.succeed();
	}

	// =================================================================================================================
	// The Living Covenant's last part, and the line's edges at D6
	// =================================================================================================================

	/**
	 * The Throne bound last: the Chamber and the Vigil parts are already done (ARRANGE, with no chapter check run), and
	 * binding the Throne by its own right-click completes the Living Covenant by itself; nothing else calls the chapter
	 * check.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void livingCovenantByFinalThrone(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		// ARRANGE: the Chamber and Vigil parts as their own paths record them, without the chapter check.
		HarbingerAdvancementGranter.grantIfNotDone(p.player(), HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED);
		HarbingerAdvancementGranter.grantIfNotDone(p.player(), HarbingerAdvancementGranter.ADV_CHAMBER_RITE_ATTUNED);
		ChamberVisitService.access(p.player()).setAttuned(true);
		HarbingerAdvancementGranter.grantIfNotDone(p.player(), HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED);
		helper.assertFalse(p.has(adv(HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE)), "two parts are not the chapter");
		BlockPos throne = PlaythroughSites.covenantThrone(p, PlaythroughSites.stand(helper, 12, 16)); // ARRANGE: crafted and set down
		p.moveTo(throne.south(2));
		PlaythroughRites.emptyHand(p);
		Outcome sat = p.useOn(throne);
		PlaythroughLog.note(helper, "livingCovenantByFinalThrone", "the Throne bound last: " + sat.describe());
		sat.said(RESPAWN_SET).earned(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND))
				.earned(adv(HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE));
		p.player().stopRiding();
		helper.succeed();
	}

	/**
	 * The Vigil kept last: the Chamber and Throne parts are already done (ARRANGE, no chapter check), and the Covenant
	 * Vigil played with its recruit assigned by {@code interactOn} completes the Living Covenant by itself.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 11000, batch = RITES)
	public static void livingCovenantByFinalVigil(GameTestHelper helper) {
		Vigil vigil = Vigil.withRecruit(helper);
		ServerPlayer leader = vigil.leader.player();
		// ARRANGE: the Chamber and Throne parts as their own paths record them, without the chapter check.
		HarbingerAdvancementGranter.grantIfNotDone(leader, HarbingerAdvancementGranter.ADV_CHAMBER_RETURNED);
		HarbingerAdvancementGranter.grantIfNotDone(leader, HarbingerAdvancementGranter.ADV_CHAMBER_RITE_ATTUNED);
		ChamberVisitService.access(leader).setAttuned(true);
		HarbingerAdvancementGranter.grantIfNotDone(leader, HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND);
		helper.assertFalse(vigil.leader.has(adv(HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE)), "two parts are not the chapter");
		PlaythroughClock clock = PlaythroughClock.start(helper);
		vigil.prepare(clock);
		clock.step(() -> PlaythroughClock.rite(vigil.leader, vigil.floor.focus(), vigil.plan()))
				.then(() -> {
					returnStaff(vigil.leader);
					PlaythroughLog.note(helper, "livingCovenantByFinalVigil", "the Vigil kept last: " + vigil.leader.last().describe());
					helper.assertTrue(vigil.assignedAt[0] >= 0, "the recruit was assigned to a station during inscription");
					helper.assertTrue(vigil.leader.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED)),
							"the kept vigil earns covenant_vigil_completed");
					helper.assertTrue(vigil.leader.has(adv(HarbingerAdvancementGranter.ADV_LIVING_COVENANT_COMPLETE)),
							"the Vigil, kept last, completes the Living Covenant by itself");
				})
				.run();
	}

	/**
	 * A D6 member is expelled from their line, then founds a line of their own by play (Bloodline Founding), gets a
	 * Fane (ARRANGE: the Founding Fane is the D5 band's) and binds a Throne by its right-click. With one line per player
	 * (LF-2) and the Throne bound only by a progenitor (D6-1), this is a member's only road to D7; no stale founder,
	 * line or covenant flag left by the old line may block it.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 9000, batch = RITES)
	public static void formerMemberReachesTheThrone(GameTestHelper helper) {
		PlaythroughPlayer leader = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(leader); // ARRANGE: the old line with its Fane
		PlaythroughPlayer member = d6Member(helper, leader, 4, 8);
		member.joinLevel();
		tools(member); // ARRANGE
		PlaythroughStates.blood(member, 5000.0D); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(member, helper.absolutePos(RITE_ORIGIN), "bloodline_founding");
		PlaythroughClock clock = PlaythroughClock.start(helper);
		clock.then(() -> {
			Outcome kicked = leader.kick(member.uuid());
			PlaythroughLog.note(helper, "formerMemberReachesTheThrone", "the member is expelled: " + kicked.describe()
					+ "; the member heard " + member.chat());
			helper.assertTrue(PlaythroughStates.lineOf(member) == null, "the expelled member holds no line");
		});
		PlaythroughRites.prepare(clock, member, floor);
		clock.then(() -> {
					member.moveTo(PlaythroughRites.riteStand(floor));
					member.wield(ItemInit.living_staff.get());
				})
				.step(() -> PlaythroughClock.rite(member, floor.focus(), RitePlan.standard().fightThreats()))
				.then(() -> {
					returnStaff(member);
					PlaythroughLog.note(helper, "formerMemberReachesTheThrone", "the former member's Bloodline Founding: "
							+ member.last().describe());
					var line = PlaythroughStates.lineOf(member);
					helper.assertTrue(line != null && line.getLeaderUUID().equals(member.uuid()),
							"the former member founds and leads a line of their own; " + member.last());
					PlaythroughStates.covenantD5(member, PlaythroughStates.defaultHeart(member)); // ARRANGE: the Founding Fane
					BlockPos throne = PlaythroughSites.covenantThrone(member, PlaythroughSites.stand(helper, 12, 18)); // ARRANGE
					member.moveTo(throne.south(2));
					PlaythroughRites.emptyHand(member);
					Outcome sat = member.useOn(throne);
					PlaythroughLog.note(helper, "formerMemberReachesTheThrone", "the new progenitor at their Throne: " + sat.describe());
					sat.said(RESPAWN_SET).earned(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND));
					member.player().stopRiding();
				}).run();
	}

	/** A D6 player with the covenant written but no line any more (expelled) asks the Vicar what comes next. */
	// DECISION D6-7: the D6 Vicar's covenant hint ignores the line: a lineless D6 who kept covenant_written_in_place
	// (expelled, or their line dissolved) is told to bind a Throne "to your Fane", which they cannot do without a line
	// of their own. Options: tell them to found a line first / a line about the lost line / keep.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void linelessD6VicarGuidance(GameTestHelper helper) {
		Entity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 20, 12));
		PlaythroughPlayer leader = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(leader); // ARRANGE
		PlaythroughPlayer member = d6Member(helper, leader, 4, 8);
		member.joinLevel();
		leader.kick(member.uuid());
		helper.assertTrue(PlaythroughStates.lineOf(member) == null, "the expelled member holds no line");
		DialogueView view = member.approach(vicar).talkTo(vicar);
		view.assertLangKeys(helper, "the D6 Vicar to a lineless D6");
		DialogueNode hint = view.tree().getNode("degree_hint");
		List<String> lines = hint == null ? List.of() : hint.lines().stream().map(line -> Dialogue.lineKey(line).orElse(line)).toList();
		PlaythroughLog.note(helper, "linelessD6VicarGuidance", "D6-7: the Vicar's hint to a lineless D6: " + lines);
		helper.assertTrue(lines.contains(COVENANT_LINE_PREFIX + "found_bloodline") || !lines.contains(COVENANT_LINE_PREFIX + "bind_throne"),
				"DECISION D6-7: a lineless D6 is not sent to bind a Throne to a Fane they no longer have; the hint says " + lines);
		helper.succeed();
	}

	/**
	 * A D6 progenitor binds their Throne, then dissolves the line from the ledger (its Fane goes with it). What becomes
	 * of the Throne bond is logged (the respawn point, the Living Covenant part).
	 */
	// DECISION D6-8: a disbanded progenitor keeps covenant_throne_bound and the Throne's respawn point though the line
	// and its Fane are gone, and nothing says so. Options: clear the Throne part with the Fane / keep it and say so on
	// disband / keep as is.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void disbandedProgenitorThrone(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		p.joinLevel();
		BlockPos throne = PlaythroughSites.covenantThrone(p, PlaythroughSites.stand(helper, 12, 16)); // ARRANGE
		p.moveTo(throne.south(2));
		PlaythroughRites.emptyHand(p);
		p.useOn(throne).said(RESPAWN_SET).earned(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND));
		p.player().stopRiding();
		if (p.count(ItemInit.harbinger_assignment_ledger.get()) == 0) p.give(new ItemStack(ItemInit.harbinger_assignment_ledger.get())); // ARRANGE
		Outcome disband = p.ledgerAction(PacketLedgerAction.ACTION_DISBAND_BLOODLINE);
		boolean bound = p.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND));
		PlaythroughLog.note(helper, "disbandedProgenitorThrone", "D6-8: disband " + disband.describe() + "; line "
				+ PlaythroughStates.lineOf(p) + ", Throne still bound " + bound + ", respawn " + p.player().getRespawnPosition());
		helper.assertTrue(PlaythroughStates.lineOf(p) == null, "the ledger's Disband dissolves the line");
		helper.assertTrue(!bound || disband.saidKeys().stream().anyMatch(key -> key.contains("throne")),
				"DECISION D6-8: a Throne bond kept past the line's end is at least named when the line is dissolved; said "
						+ disband.saidKeys());
		helper.succeed();
	}

	/**
	 * A D6 member of a line looks for any road to D7: binding the leader's Throne (D6-1), founding a line of their own
	 * (refused while in a line, LF-2), or leaving the line (no ledger or dialogue action, D5-2). A player would expect
	 * at least one.
	 */
	// DECISION CHAIN-3 (settles D5-2 and D6-1 together): with LF-2 (one line per player) a member can no longer found a
	// line, only a progenitor binds a Covenant Throne, and members have no way to leave (EarlyInitiation.release takes
	// off the charm and deactivates blood; it does not call removeMember). So every member, a D1 initiate brought in by
	// a founder included, stops at D6 for good unless their leader expels them. Options: add a leave action / let
	// members bind their leader's Throne / keep LF-2 and accept that members stop at D6 (and say so).
	@GameTest(template = EMPTY, timeoutTicks = 1500, batch = RITES, required = false)
	public static void memberRouteToD7(GameTestHelper helper) {
		PlaythroughPlayer leader = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(leader); // ARRANGE
		PlaythroughPlayer member = d6Member(helper, leader, 4, 8);
		tools(member); // ARRANGE
		PlaythroughStates.blood(member, 5000.0D); // ARRANGE
		BlockPos throne = PlaythroughSites.covenantThrone(leader, PlaythroughSites.stand(helper, 12, 18)); // ARRANGE
		MachineAccessEvents.awardMachineCrafted(member.player(), BlockInit.covenant_throne.get()); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(member, helper.absolutePos(RITE_ORIGIN), "bloodline_founding");
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, member, floor);
		clock.then(() -> {
			member.moveTo(throne.south(2));
			PlaythroughRites.emptyHand(member);
			Outcome sat = member.useOn(throne);
			member.player().stopRiding();
			boolean bound = member.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_THRONE_BOUND));
			member.moveTo(PlaythroughRites.riteStand(floor));
			member.wield(ItemInit.living_staff.get());
			Outcome founding = member.useOn(floor.focus());
			boolean founds = CardinalRiteSavedData.get(member.player().serverLevel()).getRite(member.uuid()) != null;
			boolean leave = false; // PacketLedgerAction offers summon, recall, set point and disband only; no dialogue leaves
			PlaythroughLog.note(helper, "memberRouteToD7", "CHAIN-3: the leader's Throne " + sat.saidKeys() + " bound=" + bound
					+ "; Bloodline Founding " + founding.saidKeys() + " started=" + founds + "; a leave action " + leave);
			helper.assertTrue(bound || founds || leave, "DECISION CHAIN-3: a D6 member has some road to D7 (bind the leader's"
					+ " Throne, found a line, or leave the line); the Throne said " + sat.saidKeys() + ", Founding said "
					+ founding.saidKeys());
		}).run();
	}

	/** ARRANGE: a member of {@code leader}'s line at D6 (their own covenant deposit and Bloodline Covenant). */
	private static PlaythroughPlayer d6Member(GameTestHelper helper, PlaythroughPlayer leader, int x, int z) {
		PlaythroughPlayer member = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, x, z));
		PlaythroughStates.illuminatusD5(member); // ARRANGE
		PlaythroughStates.memberOf(member, leader); // ARRANGE
		PlaythroughStates.covenantProof(member); // ARRANGE: the member's Bloodwell deposit (memberRouteToD6 plays it)
		helper.assertTrue(DegreeProgression.advance(member.player(), 6), "ARRANGE: the member's Bloodline Covenant");
		return member;
	}

	// =================================================================================================================
	// The Covenant Vigil
	// =================================================================================================================

	/**
	 * The Covenant Vigil with a recruited Vicar: the recruit stands far off in the Fane, the ledger's summon brings
	 * them, the Staff is planted on the Greater Communion floor, the recruit is assigned to a station by an
	 * {@code interactOn} during inscription, the waves are fought and the still intervals held still, and the daemon
	 * seals the vigil: {@code covenant_vigil_completed} and the closing line.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 11000, batch = RITES)
	public static void covenantVigilWithRecruit(GameTestHelper helper) {
		Vigil vigil = Vigil.withRecruit(helper);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		vigil.prepare(clock);
		clock.step(() -> PlaythroughClock.rite(vigil.leader, vigil.floor.focus(), vigil.plan()))
				.then(() -> {
					returnStaff(vigil.leader);
					Outcome rite = vigil.leader.last();
					PlaythroughLog.note(helper, "covenantVigilWithRecruit", "vigil: " + rite.describe() + " helper assigned at "
							+ vigil.assignedAt[0]);
					helper.assertTrue(vigil.assignedAt[0] >= 0, "the recruit was assigned to a station during inscription");
					helper.assertTrue(vigil.leader.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED)),
							"the kept vigil earns covenant_vigil_completed; " + rite);
					helper.assertTrue(vigil.leader.chat().stream().anyMatch(line -> line.has(VIGIL_CLOSES)), "the vigil's closing line is said");
				})
				.run();
	}

	/**
	 * The Covenant Vigil with a member player instead of a recruit: during inscription the member (a joined player of
	 * the line) stands on a station marker and right-clicks it with an empty hand, claiming the station, and holds it
	 * to the end. Logged: whether the member, who is not in the level's entity storage here, shares the vigil's reward.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 11000, batch = RITES)
	public static void covenantVigilWithMemberAtStation(GameTestHelper helper) {
		Vigil vigil = Vigil.withMember(helper);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		vigil.prepare(clock);
		clock.step(() -> PlaythroughClock.rite(vigil.leader, vigil.floor.focus(), vigil.plan()))
				.then(() -> {
					returnStaff(vigil.leader);
					PlaythroughLog.note(helper, "covenantVigilWithMemberAtStation", "vigil: " + vigil.leader.last().describe()
							+ " member claimed at " + vigil.assignedAt[0] + " member said " + vigil.member.chat()
							+ " member effects " + vigil.member.player().getActiveEffects());
					helper.assertTrue(vigil.assignedAt[0] >= 0, "the member claimed a station during inscription");
					helper.assertTrue(vigil.leader.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED)),
							"a member player at a station completes the vigil; " + vigil.leader.last());
				})
				.run();
	}

	/**
	 * The vigil's sworn helper falls in the ordeal (ARRANGE: out-of-world damage, nothing the player did): the vigil
	 * earns no proof. What the caster is told is logged.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 11000, batch = RITES)
	public static void vigilHelperLost(GameTestHelper helper) {
		Vigil vigil = Vigil.withRecruit(helper);
		long[] lostAt = {-1L};
		PlaythroughClock clock = PlaythroughClock.start(helper);
		vigil.prepare(clock);
		RitePlan plan = vigil.plan()
				.onPhase(CardinalRitePhase.ORDEAL, (rite, ticks) -> {
					if (lostAt[0] < 0 && rite.getAllyRoles().containsKey(vigil.helperNpc.getUUID())) {
						PlaythroughNpcs.kill(vigil.helperNpc); // ARRANGE
						lostAt[0] = helper.getTick();
					}
				})
				.pauseWhen(() -> lostAt[0] >= 0 && helper.getTick() - lostAt[0] > 2400);
		clock.step(() -> PlaythroughClock.rite(vigil.leader, vigil.floor.focus(), plan))
				.then(() -> {
					ActiveCardinalRite left = CardinalRiteSavedData.get(helper.getLevel()).getRite(vigil.leader.uuid());
					PlaythroughLog.note(helper, "vigilHelperLost", "helper killed at " + lostAt[0] + "; rite now "
							+ (left == null ? "ended" : left.getPhase()) + "; caster said " + vigil.leader.chat());
					helper.assertTrue(lostAt[0] >= 0, "the helper was assigned and fell during the ordeal");
					helper.assertFalse(vigil.leader.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED)),
							"a vigil whose helper fell earns no proof");
					helper.assertFalse(vigil.leader.chat().stream().anyMatch(line -> line.has(VIGIL_CLOSES)),
							"the vigil does not close around a fallen helper");
					if (vigil.leader.chat().stream().noneMatch(line -> line.has(HELPERS_HOLD))) {
						PlaythroughLog.note(helper, "vigilHelperLost", "the caster was not told the helpers must still hold");
					}
				})
				.run();
	}

	/**
	 * The vigil's helper walks off mid-ordeal (ARRANGE: moved 30 blocks away). Logged: whether the rite recalls the
	 * helper to its station. The proof is earned exactly when the vigil closes, never silently and never lost.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 11000, batch = RITES)
	public static void vigilHelperWalksOff(GameTestHelper helper) {
		Vigil vigil = Vigil.withRecruit(helper);
		long[] walkedAt = {-1L};
		double[] distanceAfter = {-1.0D};
		PlaythroughClock clock = PlaythroughClock.start(helper);
		vigil.prepare(clock);
		RitePlan plan = vigil.plan()
				.onPhase(CardinalRitePhase.ORDEAL, (rite, ticks) -> {
					if (walkedAt[0] < 0 && rite.getAllyRoles().containsKey(vigil.helperNpc.getUUID())) {
						Vec3 away = vigil.helperNpc.position().add(30.0D, 0.0D, 0.0D);
						vigil.helperNpc.teleportTo(away.x, away.y, away.z); // ARRANGE: the helper wanders off
						walkedAt[0] = helper.getTick();
					} else if (walkedAt[0] >= 0 && distanceAfter[0] < 0 && helper.getTick() - walkedAt[0] >= 40) {
						distanceAfter[0] = vigil.helperNpc.position().distanceTo(Vec3.atCenterOf(rite.getCenterPos()));
					}
				})
				.pauseWhen(() -> walkedAt[0] >= 0 && helper.getTick() - walkedAt[0] > 4000);
		clock.step(() -> PlaythroughClock.rite(vigil.leader, vigil.floor.focus(), plan))
				.then(() -> {
					boolean proof = vigil.leader.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED));
					boolean closed = vigil.leader.chat().stream().anyMatch(line -> line.has(VIGIL_CLOSES));
					PlaythroughLog.note(helper, "vigilHelperWalksOff", "helper moved off at " + walkedAt[0]
							+ ", 40 ticks later " + distanceAfter[0] + " blocks from the Focus; proof=" + proof
							+ " closed=" + closed + "; caster said " + vigil.leader.chat());
					helper.assertTrue(walkedAt[0] >= 0, "the helper was assigned and walked off during the ordeal");
					helper.assertTrue(proof == closed, "the proof follows the vigil's close: proof=" + proof + " closed=" + closed);
				})
				.run();
	}

	/**
	 * The caster relogs mid-ordeal, then takes the rite up again if it is still there. The proof is earned exactly
	 * when the vigil closes; what the relog did to the rite is logged.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 12000, batch = RITES)
	public static void vigilCasterRelogs(GameTestHelper helper) {
		Vigil vigil = Vigil.withRecruit(helper);
		ServerLevel level = helper.getLevel();
		boolean[] relogged = {false};
		PlaythroughClock clock = PlaythroughClock.start(helper);
		vigil.prepare(clock);
		RitePlan toOrdeal = vigil.plan().pauseWhen(() -> {
			ActiveCardinalRite rite = CardinalRiteSavedData.get(level).getRite(vigil.leader.uuid());
			return rite != null && rite.getPhase() == CardinalRitePhase.ORDEAL;
		});
		clock.step(() -> PlaythroughClock.rite(vigil.leader, vigil.floor.focus(), toOrdeal))
				.then(() -> {
					vigil.leader.relog();
					relogged[0] = true;
					ActiveCardinalRite rite = CardinalRiteSavedData.get(level).getRite(vigil.leader.uuid());
					PlaythroughLog.note(helper, "vigilCasterRelogs", "after the relog the rite is "
							+ (rite == null ? "gone" : rite.getPhase().toString()));
				})
				.step(() -> CardinalRiteSavedData.get(level).getRite(vigil.leader.uuid()) == null
						? (PlaythroughClock.Step) ticks -> true
						: PlaythroughClock.rite(vigil.leader, vigil.floor.focus(), fighting(vigil.leader).releaseOnStill()
								.maxTicks(7000).takeUp()))
				.then(() -> {
					returnStaff(vigil.leader);
					boolean proof = vigil.leader.has(adv(HarbingerAdvancementGranter.ADV_COVENANT_VIGIL_COMPLETED));
					boolean closed = vigil.leader.chat().stream().anyMatch(line -> line.has(VIGIL_CLOSES));
					PlaythroughLog.note(helper, "vigilCasterRelogs", "relogged=" + relogged[0] + " proof=" + proof
							+ " closed=" + closed + " staff=" + vigil.leader.count(ItemInit.living_staff.get())
							+ "; said " + vigil.leader.chat());
					helper.assertTrue(relogged[0], "the caster relogged mid-vigil");
					helper.assertTrue(proof == closed, "the proof follows the vigil's close: proof=" + proof + " closed=" + closed);
				})
				.run();
	}

	/** A Covenant Vigil laid out: the leader in their Fane, the floor, and either a recruit or a member as helper. */
	private static final class Vigil {
		final GameTestHelper helper;
		final PlaythroughPlayer leader;
		final PlaythroughSites.RiteFloor floor;
		final Entity helperNpc;
		final PlaythroughPlayer member;
		final long[] assignedAt = {-1L};

		private Vigil(GameTestHelper helper, PlaythroughPlayer leader, PlaythroughSites.RiteFloor floor, Entity helperNpc,
				PlaythroughPlayer member) {
			this.helper = helper;
			this.leader = leader;
			this.floor = floor;
			this.helperNpc = helperNpc;
			this.member = member;
		}

		private static PlaythroughPlayer leader(GameTestHelper helper) {
			PlaythroughPlayer leader = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
			PlaythroughStates.sanctifiedD6(leader); // ARRANGE: the Fane's heart at home + 4 east, 40 blocks of Fane
			tools(leader); // ARRANGE
			PlaythroughStates.blood(leader, 5000.0D); // ARRANGE
			return leader;
		}

		/** A recruited Vicar standing far off in the Fane, more than 16 blocks from the rite, for the summon. */
		static Vigil withRecruit(GameTestHelper helper) {
			PlaythroughPlayer leader = leader(helper);
			Entity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 2, 22));
			PlaythroughStates.recruited(leader, vicar); // ARRANGE: recruit_harbinger is the D5 band's
			PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(leader, helper.absolutePos(RITE_ORIGIN), "covenant_vigil");
			return new Vigil(helper, leader, floor, vicar, null);
		}

		/** A member of the line, joined so the rite's station checks find them. */
		static Vigil withMember(GameTestHelper helper) {
			PlaythroughPlayer leader = leader(helper);
			PlaythroughPlayer member = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 6, 20));
			PlaythroughStates.illuminatusD5(member); // ARRANGE
			PlaythroughStates.memberOf(member, leader); // ARRANGE
			member.joinLevel();
			PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(leader, helper.absolutePos(RITE_ORIGIN), "covenant_vigil");
			return new Vigil(helper, leader, floor, null, member);
		}

		/** The floor readied, the recruit summoned (when there is one), the Staff in hand. */
		void prepare(PlaythroughClock clock) {
			PlaythroughRites.prepare(clock, leader, floor);
			clock.then(() -> {
				if (helperNpc != null) {
					double before = helperNpc.distanceTo(leader.player());
					Outcome summon = leader.ledgerAction(PacketLedgerAction.ACTION_SUMMON_NPCS);
					double after = helperNpc.distanceTo(leader.player());
					PlaythroughLog.note(helper, "the ledger summon: " + summon.describe() + " distance " + before + " -> " + after);
					helper.assertTrue(after < 6.0D, "the ledger's summon brings the recruit to the caster; " + summon);
				}
				leader.wield(ItemInit.living_staff.get());
			});
		}

		/** The vigil's plan: waves fought, still intervals held still, the helper taken on during inscription. */
		RitePlan plan() {
			return fighting(leader).releaseOnStill().onPhase(CardinalRitePhase.INSCRIPTION, (rite, ticks) -> {
				if (assignedAt[0] >= 0 || ticks % 20 != 0) return;
				if (helperNpc != null) {
					assignNpc(leader, helperNpc);
					if (rite.getAllyRoles().containsKey(helperNpc.getUUID())) assignedAt[0] = helper.getTick();
				} else if (member != null) {
					claimStation(member, rite);
					if (rite.getAllyRoles().containsKey(member.uuid())) assignedAt[0] = helper.getTick();
				}
			});
		}
	}

	/**
	 * The caster takes their hand off Projection, empties it and right-clicks {@code npc} (the EntityInteract path
	 * that assigns a rite helper), then takes Projection up again when they were holding it.
	 */
	private static void assignNpc(PlaythroughPlayer caster, Entity npc) {
		boolean projecting = caster.player().isUsingItem();
		if (projecting) caster.releaseHold();
		PlaythroughRites.emptyHand(caster);
		Outcome assigned = caster.interactOn(npc);
		PlaythroughLog.note(caster.helper(), "assign helper: " + assigned.describe());
		if (projecting) caster.startHold(ItemInit.blood_projection.get(), npc.getEyePosition());
	}

	/** A member walks onto the vigil's anchor station marker and right-clicks it with an empty hand. */
	private static void claimStation(PlaythroughPlayer member, ActiveCardinalRite rite) {
		ServerLevel level = member.player().serverLevel();
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(level, rite.getRecipeId());
		BlockPos offset = CardinalRiteAllyService.markers(recipe).get(CardinalRiteAllyRole.ANCHOR);
		member.helper().assertTrue(offset != null, "the vigil has an anchor station marker");
		BlockPos station = rite.getCenterPos().offset(offset);
		member.moveTo(station); // ARRANGE: walks onto the marker
		PlaythroughRites.emptyHand(member);
		Outcome claimed = member.useOn(station.below());
		PlaythroughLog.note(member.helper(), "member claims the station at " + station.toShortString() + ": " + claimed.describe());
	}

	// =================================================================================================================
	// The Archon rite before the Living Covenant
	// =================================================================================================================

	/**
	 * The Archon rite planted at D6 before the Living Covenant: the Grand Threshold floor readied (pillars, the five
	 * offerings lit), the Staff planted. The rite stays sealed and names The Living Covenant, nothing starts, and the
	 * Staff stays in hand.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 2500, batch = RITES)
	public static void archonSealedBeforeLivingCovenant(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		tools(p); // ARRANGE
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "archon_rite");
		clearRiteAtEnd(p);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
			p.wield(ItemInit.living_staff.get());
			Outcome sealed = p.useOn(floor.focus());
			PlaythroughLog.note(helper, "archonSealedBeforeLivingCovenant", "plant: " + sealed.describe());
			sealed.said(RANK_SEALED).said(LIVING_COVENANT_CHAPTER);
			helper.assertTrue(CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) == null,
					"the Archon rite does not start before the Living Covenant; " + sealed);
			helper.assertTrue(p.player().getMainHandItem().is(ItemInit.living_staff.get()), "the Staff stays in hand");
			helper.assertTrue(p.degree() == 6, "the player stays at degree 6");
		}).run();
	}

	// =================================================================================================================
	// The Vein-Mason at Degree 6
	// =================================================================================================================

	/**
	 * The Vein-Mason's sixth-Degree chain from a D6 player who claimed the D5 reward: the Anchorite's referral, the
	 * Mnemonist's counsel, a cast matching the active scar (the first route), the same build cast again (not counted),
	 * a new loadout prepared at the Effigy and committed at the Brazier, a second matching cast on the new build, then
	 * the tier-3 reward once (a second claim gives nothing).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1800, batch = BATCH)
	public static void veinMasonD6Chain(GameTestHelper helper) {
		Mason mason = Mason.at(helper);
		PlaythroughPlayer p = mason.player;
		Entity anchorite = mason.respite.anchorite();
		ResourceLocation second = VeinMasonScarLesson.continuationForPlayer(p.player()).patternScarId();
		// ARRANGE: the continuation scar was carved and burnt in at D5 (that band plays it).
		HemoCapabilityAccess.getScarState(p.player()).addKnownCerebralScar(second);
		PlaythroughClock clock = PlaythroughClock.start(helper)
				.then(() -> {
					p.moveTo(mason.yard);
					DialogueView d6 = p.talkTo(anchorite);
					d6.assertLangKeys(helper, "the D6 Anchorite").assertNoDeadEnds(helper, "the D6 Anchorite");
					helper.assertTrue(d6.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL),
							"the D6 Anchorite offers the referral: " + d6.describe());
					p.choose(anchorite, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL)
							.earned(adv(VeinMasonAssignments.D6_REFERRAL));
					DialogueView next = p.talkTo(anchorite);
					PlaythroughLog.note(helper, "veinMasonD6Chain", "after the referral: " + next.lineKeys());
					p.approach(mason.mnemonist);
					DialogueView mnemonist = p.talkTo(mason.mnemonist);
					mnemonist.assertLangKeys(helper, "the D6 Mnemonist with the referral");
					helper.assertTrue(mnemonist.offers(HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL),
							"the Mnemonist offers the Anchorite's counsel: " + mnemonist.describe());
					p.choose(mason.mnemonist, HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL)
							.earned(adv(VeinMasonAssignments.D6_COUNSEL));
					Outcome first = noeticCast(p, target(helper, PlaythroughSites.stand(helper, 4, 14)));
					PlaythroughLog.note(helper, "veinMasonD6Chain", "first matching cast: " + first.describe());
					first.earned(adv(VeinMasonAssignments.D6_FIRST_ROUTE));
					Outcome same = noeticCast(p, target(helper, PlaythroughSites.stand(helper, 4, 14)));
					PlaythroughLog.note(helper, "veinMasonD6Chain", "same-build recast: " + same.describe());
					helper.assertFalse(p.has(adv(VeinMasonAssignments.D6_LOADOUT)) || p.has(adv(VeinMasonAssignments.D6_SECOND_ROUTE)),
							"a recast on the same build counts for nothing; " + same);
					same.didNotSay(ROUTE_RECORDED);
					p.moveTo(mason.yard);
					prepareEffigy(p, mason.respite.effigy(), second);
				})
				.step(() -> p.holdUntil(ItemInit.blood_projection.get(), 400, () -> Vec3.atCenterOf(mason.respite.effigy()),
						() -> !effigy(p, mason.respite.effigy()).hasPendingMotif()))
				.idle(2)
				.then(() -> {
					p.pickUpNear(mason.respite.effigy(), ItemInit.scar_pattern.get());
					helper.assertTrue(carries(p, prepared(second)), "the Effigy's prepared pattern for the new loadout is carried");
				});
		learnAtBrazier(clock, p, mason.respite.brazier(), prepared(second), "the new loadout's prepared pattern");
		clock.then(() -> {
			helper.assertTrue(p.has(adv(VeinMasonAssignments.D6_LOADOUT)), "committing a different loadout is recorded; "
					+ HemoCapabilityAccess.getScarState(p.player()).getActiveCerebralScars());
			Outcome secondCast = noeticCast(p, target(helper, PlaythroughSites.stand(helper, 4, 14)));
			PlaythroughLog.note(helper, "veinMasonD6Chain", "second matching cast: " + secondCast.describe());
			secondCast.earned(adv(VeinMasonAssignments.D6_SECOND_ROUTE));
			helper.assertTrue(p.has(adv(VeinMasonAssignments.D6_READY)), "the second route makes the D6 work ready");
			p.moveTo(mason.yard);
			DialogueView ready = p.talkTo(anchorite);
			ready.assertLangKeys(helper, "the D6 Anchorite, ready").assertNoDeadEnds(helper, "the D6 Anchorite, ready");
			helper.assertTrue(ready.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD),
					"the ready student is offered the D6 reward: " + ready.describe());
			Outcome reward = p.choose(anchorite, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD);
			int blanks = reward.delta(ItemInit.scar_blank.get()) + reward.dropped(ItemInit.scar_blank.get(), Outcome.Near.ANY);
			int papers = reward.delta(ItemInit.runic_motif_paper.get()) + reward.dropped(ItemInit.runic_motif_paper.get(), Outcome.Near.ANY);
			int patterns = reward.delta(ItemInit.scar_pattern.get()) + reward.dropped(ItemInit.scar_pattern.get(), Outcome.Near.ANY);
			PlaythroughLog.note(helper, "veinMasonD6Chain", "tier-3 reward: " + reward.describe());
			reward.said(D6_REWARD_SAID).earned(adv(VeinMasonAssignments.D6_REWARD));
			helper.assertTrue(blanks == 1 && papers == 8 && patterns == 1,
					"the tier-3 kit is a pattern, a blank, a catalyst and 8 motif papers; " + reward);
			DialogueView after = p.talkTo(anchorite);
			helper.assertFalse(after.offers(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD), "the reward is offered once");
			Outcome again = p.forgeClaim(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD, anchorite.getId());
			int againBlanks = again.delta(ItemInit.scar_blank.get()) + again.dropped(ItemInit.scar_blank.get(), Outcome.Near.ANY);
			helper.assertTrue(againBlanks == 0, "a repeated claim gives nothing; " + again);
		}).run();
	}

	/**
	 * Forged D6 Mason events: the tier-3 reward claimed before the work is ready, the Mnemonist's counsel without the
	 * referral, and the referral sent to the Mnemonist. None of them records or gives anything. Whether a line says
	 * why is logged (D-28 family: a refused reward is silent).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void veinMasonD6Forged(GameTestHelper helper) {
		Mason mason = Mason.at(helper);
		PlaythroughPlayer p = mason.player;
		Entity anchorite = mason.respite.anchorite();
		p.moveTo(mason.yard);
		Outcome early = p.forgeClaim(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REWARD, anchorite.getId());
		PlaythroughLog.note(helper, "veinMasonD6Forged", "early reward (D-28 family): said " + early.saidKeys() + "; " + early.describe());
		early.gaveNothing();
		helper.assertFalse(p.has(adv(VeinMasonAssignments.D6_REWARD)), "an early D6 claim records nothing");
		p.approach(mason.mnemonist);
		Outcome counsel = p.forge(HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL, mason.mnemonist.getId());
		PlaythroughLog.note(helper, "veinMasonD6Forged", "counsel without referral (D-28 family): said " + counsel.saidKeys());
		helper.assertFalse(p.has(adv(VeinMasonAssignments.D6_COUNSEL)), "counsel without the referral records nothing");
		Outcome wrongSpeaker = p.forge(HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL, mason.mnemonist.getId());
		PlaythroughLog.note(helper, "veinMasonD6Forged", "referral sent to the Mnemonist: said " + wrongSpeaker.saidKeys());
		helper.assertFalse(p.has(adv(VeinMasonAssignments.D6_REFERRAL)), "the referral is the Anchorite's alone");
		helper.succeed();
	}

	/** The Mnemonist's Anchorite counsel while the chain waits on it: a player would expect its sigil to stay. */
	// DECISION NPC-4: the Mnemonist's Anchorite counsel is a lore card, so its sigil clears once read while the chain
	// still waits on the counsel. Options: a Quest Work card with a sigil until the counsel is recorded / keep.
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void counselIsALoreCard(GameTestHelper helper) {
		Mason mason = Mason.at(helper);
		PlaythroughPlayer p = mason.player;
		Entity anchorite = mason.respite.anchorite();
		p.moveTo(mason.yard);
		p.talkTo(anchorite);
		p.choose(anchorite, HarbingerCicatrixAnchoriteDialogueTrees.EVENT_D6_REFERRAL);
		p.approach(mason.mnemonist);
		DialogueView before = p.talkTo(mason.mnemonist);
		Optional<DialogueTopic> card = before.topic(HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL);
		DialogueAttention sigilBefore = Dialogue.attention(mason.mnemonist, p.player());
		PlaythroughLog.note(helper, "counselIsALoreCard", "NPC-4: card " + card.map(PlaythroughSanctifiedGameTests::card).orElse("none")
				+ " sigil " + sigilBefore);
		helper.assertTrue(card.isPresent(), "the counsel has a card in the Mnemonist's hub: " + before.describe());
		p.openTopic(mason.mnemonist, card.get().id());
		DialogueView after = p.talkTo(mason.mnemonist);
		Optional<DialogueTopic> read = after.topic(HarbingerMnemonistDialogueTrees.EVENT_ANCHORITE_COUNSEL);
		DialogueAttention sigilAfter = Dialogue.attention(mason.mnemonist, p.player());
		PlaythroughLog.note(helper, "counselIsALoreCard", "NPC-4: after reading, card " + read.map(PlaythroughSanctifiedGameTests::card).orElse("none")
				+ " sigil " + sigilAfter + " counsel recorded=" + p.has(adv(VeinMasonAssignments.D6_COUNSEL)));
		helper.assertFalse(p.has(adv(VeinMasonAssignments.D6_COUNSEL)), "reading the card is not the counsel");
		helper.assertTrue(sigilAfter != DialogueAttention.NONE
						&& read.map(topic -> topic.attention() != DialogueAttention.NONE).orElse(false),
				"NPC-4 expected: the counsel still flags while the chain waits on it; card "
						+ read.map(PlaythroughSanctifiedGameTests::card).orElse("none") + ", sigil " + sigilAfter);
		helper.succeed();
	}

	/** The Respite yard, a Mnemonist beside it, and a D6 student who claimed the D5 reward. */
	private static final class Mason {
		final PlaythroughPlayer player;
		final PlaythroughSites.MasonsRespite respite;
		final Entity mnemonist;
		final BlockPos yard;

		private Mason(PlaythroughPlayer player, PlaythroughSites.MasonsRespite respite, Entity mnemonist, BlockPos yard) {
			this.player = player;
			this.respite = respite;
			this.mnemonist = mnemonist;
			this.yard = yard;
		}

		static Mason at(GameTestHelper helper) {
			PlaythroughSites.MasonsRespite respite = PlaythroughSites.masonsRespite(helper, PlaythroughSites.stand(helper, 8, 8));
			Entity mnemonist = PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 12, 5));
			BlockPos yard = PlaythroughSites.stand(helper, 10, 10);
			PlaythroughPlayer p = PlaythroughPlayer.create(helper, yard);
			PlaythroughStates.sanctifiedD6(p, PlaythroughSites.stand(helper, 26, 20)); // ARRANGE: the Fane clear of the yard
			// ARRANGE: the D5 Mason chapter (strain, diagnosis, treatment, fortification, reward) is the D5 band's.
			for (ResourceLocation id : List.of(VeinMasonAssignments.D5_VARICOSE, VeinMasonAssignments.D5_DIAGNOSED,
					VeinMasonAssignments.D5_TREATED, VeinMasonAssignments.D5_READY, VeinMasonAssignments.D5_REWARD)) {
				VeinMasonAssignments.grant(p.player(), id);
			}
			tools(p); // ARRANGE
			PlaythroughStates.blood(p, 5000.0D); // ARRANGE
			return new Mason(p, respite, mnemonist, yard);
		}
	}

	/**
	 * A cast of the manipulation that matches the first active cerebral scar's temper, through the cast key's
	 * payload, aimed at {@code target}. ARRANGE: the manipulation is known and equipped (it is learnt elsewhere), the
	 * pool is full and the cooldown has passed. The selection itself is pressed through the cycle key.
	 */
	private static Outcome noeticCast(PlaythroughPlayer p, Entity target) {
		var scars = HemoCapabilityAccess.getScarState(p.player());
		p.helper().assertTrue(!scars.getActiveCerebralScars().isEmpty(), "the student has an active cerebral scar");
		ResourceLocation active = scars.getActiveCerebralScars().iterator().next();
		var definition = ScarInit.getByName(active.toString());
		p.helper().assertTrue(definition != null, "the active scar " + active + " is registered");
		BloodManipulation manipulation = matchingManipulation(definition.getAssignedTemper());
		var known = HemoCapabilityAccess.getKnownManipulations(p.player());
		known.getKnownManips().putIfAbsent(manipulation, new ManipLevel(0, 0)); // ARRANGE
		known.setEquippedManipNames(List.of(manipulation.getName())); // ARRANGE
		KnownManipulationEvents.syncPlayerEvent(p.player());
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE
		BloodManipulation.clearSessionState(); // ARRANGE: the cooldown has passed
		PlaythroughRites.emptyHand(p);
		p.select(manipulation.getName());
		p.aim(target.getBoundingBox().getCenter());
		Outcome cast = p.castSelected();
		PlaythroughLog.note(p.helper(), "noetic cast of " + manipulation.getName() + " for scar " + active + " ("
				+ definition.getAssignedTemper() + ")");
		return cast;
	}

	/** The manipulation per temper the journey fixtures cast for the Noetic route. */
	private static BloodManipulation matchingManipulation(EnumBloodTemper temper) {
		return switch (temper) {
			case DUCTILIS -> ManipulationInit.deadly_gaze.get();
			case FERRIC -> ManipulationInit.sanguine_mending.get();
			case FLAMMEUS -> ManipulationInit.crimson_flame_conjuration.get();
			case LUX -> ManipulationInit.prismatic_reproof.get();
			case CONGEATIO -> ManipulationInit.glacial_grasp.get();
			case TENEBRIS -> ManipulationInit.void_shroud.get();
			case MORTEM -> ManipulationInit.hemorrhage.get();
			default -> ManipulationInit.blood_shot.get();
		};
	}

	/** ARRANGE: a still zombie to aim a cast at, removed when the test ends. */
	private static Entity target(GameTestHelper helper, BlockPos at) {
		Zombie zombie = EntityType.ZOMBIE.create(helper.getLevel());
		helper.assertTrue(zombie != null, "a target zombie can be made");
		zombie.setPos(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D);
		zombie.setNoAi(true);
		zombie.setPersistenceRequired();
		helper.getLevel().addFreshEntity(zombie);
		PlaythroughCleanup.onEnd(helper, zombie::discard);
		return zombie;
	}

	/** Opens the Effigy, selects {@code scarId} through its payload, closes it and sets motif paper on it. */
	private static void prepareEffigy(PlaythroughPlayer p, BlockPos effigyPos, ResourceLocation scarId) {
		GameTestHelper helper = p.helper();
		PlaythroughRites.emptyHand(p);
		Outcome open = p.useOn(effigyPos);
		helper.assertTrue(p.player().containerMenu instanceof MasonsEffigyMenu, "the Effigy opens; " + open);
		PacketPrepareScarPattern.handle(new PacketPrepareScarPattern(effigyPos, List.of(scarId)), PayloadContexts.of(p.player()));
		helper.assertTrue(effigy(p, effigyPos).getSelectedScarIds().equals(List.of(scarId)),
				"the Effigy holds the selected scar: " + effigy(p, effigyPos).getSelectedScarIds());
		p.closeMenu();
		if (p.count(ItemInit.runic_motif_paper.get()) == 0) {
			p.give(new ItemStack(ItemInit.runic_motif_paper.get())); // ARRANGE: the D5 kit's papers were spent elsewhere
		}
		p.wield(ItemInit.runic_motif_paper.get());
		Outcome motif = p.useOn(effigyPos);
		helper.assertTrue(effigy(p, effigyPos).hasPendingMotif(), "the motif paper settles on the Effigy; " + motif);
	}

	/**
	 * Adds the Brazier's offering lifecycle to {@code clock}: the offering clicked in, the fire lit by held
	 * Projection, then held Absorption until the offering is drawn out (or 200 ticks).
	 */
	private static void learnAtBrazier(PlaythroughClock clock, PlaythroughPlayer p, BlockPos brazierPos,
			Predicate<ItemStack> offering, String label) {
		clock.then(() -> PlaythroughRites.insertOffering(p, brazierPos, offering, label))
				.step(() -> PlaythroughRites.lightBrazier(p, brazierPos))
				.then(() -> p.helper().assertTrue(PlaythroughRites.lit(p, brazierPos), "held Projection lights the Brazier; " + p.last()))
				.step(() -> p.holdUntil(ItemInit.blood_absorption.get(), 200, () -> PlaythroughRites.bowl(brazierPos),
						() -> !PlaythroughRites.brazier(p, brazierPos).hasOffering()))
				.then(() -> p.helper().assertFalse(PlaythroughRites.brazier(p, brazierPos).hasOffering(),
						"held Absorption draws out " + label + "; " + p.last()));
	}

	private static MasonsEffigyBlockEntity effigy(PlaythroughPlayer p, BlockPos pos) {
		if (p.player().serverLevel().getBlockEntity(pos) instanceof MasonsEffigyBlockEntity effigy) return effigy;
		p.helper().fail("no Mason's Effigy at " + pos.toShortString());
		return null;
	}

	/** A prepared (Effigy-made) scar pattern carrying {@code scarId}. */
	private static Predicate<ItemStack> prepared(ResourceLocation scarId) {
		return stack -> stack.is(ItemInit.scar_pattern.get()) && ItemScarPattern.hasPreparedLoadout(stack)
				&& ItemScarPattern.getScarIds(stack).contains(scarId);
	}

	private static boolean carries(PlaythroughPlayer p, Predicate<ItemStack> what) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) if (what.test(inventory.getItem(slot))) return true;
		return false;
	}

	private static String card(DialogueTopic topic) {
		return topic.id() + "[" + topic.category() + "," + topic.state() + "," + topic.attention()
				+ (topic.lore() ? ",lore" : "") + (topic.unread() ? ",unread" : "") + "]";
	}

	// =================================================================================================================
	// The Vagrant Mind inquiry
	// =================================================================================================================

	/**
	 * The Vagrant Mind inquiry at D6: the Mnemonist gives a bearing, the player stands inside a Vagrant Mind in the End
	 * (a real {@code VagrantMindPiece} registered as a generated start there, ARRANGE) beside chorus, the inquiry's tick
	 * records the visit and the biology, then back in the Overworld the Mnemonist takes the memory report and the
	 * Alchemist the biology report. Both inquiries close afterwards and neither gives anything.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = END)
	public static void vagrantMindInquiry(GameTestHelper helper) {
		ServerLevel end = helper.getLevel().getServer().getLevel(Level.END);
		if (end == null) {
			PlaythroughNotRun.notRun(helper, "the End is not loaded, so no Vagrant Mind can be visited");
			return;
		}
		Entity mnemonist = PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 8, 8));
		Entity alchemist = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 8, 14));
		BlockPos home = PlaythroughSites.stand(helper, 12, 10);
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, home);
		PlaythroughStates.sanctifiedD6(p, PlaythroughSites.stand(helper, 26, 20)); // ARRANGE
		helper.assertTrue(VagrantMindInquiry.eligible(p.player()), "a D6 Harbinger with active blood is eligible");

		p.approach(mnemonist);
		DialogueView lead = p.talkTo(mnemonist);
		lead.assertLangKeys(helper, "the D6 Mnemonist's Vagrant Mind lead").assertNoDeadEnds(helper, "the D6 Mnemonist");
		PlaythroughLog.note(helper, "vagrantMindInquiry", "lead: " + lead.describe() + " root options without a card "
				+ lead.rootOptionsWithoutCard());
		helper.assertTrue(lead.offers(VagrantMindInquiryDialogue.BEARING), "the Mnemonist offers a bearing: " + lead.describe());
		helper.assertFalse(lead.offers(VagrantMindInquiryDialogue.MEMORY_REPORT), "no memory report before the visit");
		Outcome bearing = p.choose(mnemonist, VagrantMindInquiryDialogue.BEARING);
		PlaythroughLog.note(helper, "vagrantMindInquiry", "bearing: " + bearing.describe());
		helper.assertTrue(said(bearing, BEARING) || said(bearing, BEARING_MISSING), "the bearing is said; " + bearing);
		bearing.gaveNothing();

		BlockPos inside = vagrantMind(helper, end);
		p.moveTo(end, inside); // ARRANGE: travelled there
		setEnd(helper, end, inside.offset(2, -1, 0), Blocks.END_STONE.defaultBlockState()); // ARRANGE: chorus on end stone
		setEnd(helper, end, inside.offset(2, 0, 0), Blocks.CHORUS_PLANT.defaultBlockState());
		p.player().tickCount = 100; // ARRANGE: five seconds stood there (the inquiry looks every 100 ticks)
		p.tickOnce();
		var progress = VagrantMindInquiry.progress(p.player());
		PlaythroughLog.note(helper, "vagrantMindInquiry", "in the Mind at " + inside.toShortString() + ": " + progress);
		helper.assertTrue(progress.mindVisited(), "standing inside the Vagrant Mind records the visit");
		helper.assertTrue(progress.biologyObserved(), "chorus beside the player records the biology");
		p.moveTo(helper.getLevel(), home); // ARRANGE: travelled back

		p.approach(mnemonist);
		DialogueView found = p.talkTo(mnemonist);
		found.assertLangKeys(helper, "the Mnemonist after the visit");
		helper.assertTrue(found.offers(VagrantMindInquiryDialogue.MEMORY_REPORT), "the memory report is offered: " + found.describe());
		Outcome memory = p.choose(mnemonist, VagrantMindInquiryDialogue.MEMORY_REPORT);
		memory.said(MEMORY_RECORDED).gaveNothing();
		DialogueView closed = p.talkTo(mnemonist);
		helper.assertFalse(closed.offers(VagrantMindInquiryDialogue.MEMORY_REPORT) || closed.offers(VagrantMindInquiryDialogue.BEARING),
				"the Mnemonist's inquiry closes once reported: " + closed.describe());

		p.approach(alchemist);
		DialogueView biology = p.talkTo(alchemist);
		biology.assertLangKeys(helper, "the Alchemist after the visit").assertNoDeadEnds(helper, "the D6 Alchemist");
		PlaythroughLog.note(helper, "vagrantMindInquiry", "Alchemist: " + biology.describe() + " root options without a card "
				+ biology.rootOptionsWithoutCard());
		helper.assertTrue(biology.offers(VagrantMindInquiryDialogue.BIOLOGY_REPORT), "the biology report is offered: " + biology.describe());
		Outcome report = p.choose(alchemist, VagrantMindInquiryDialogue.BIOLOGY_REPORT);
		report.said(BIOLOGY_RECORDED).gaveNothing();
		DialogueView done = p.talkTo(alchemist);
		helper.assertFalse(done.offers(VagrantMindInquiryDialogue.BIOLOGY_REPORT), "the Alchemist's inquiry closes once reported");
		var finished = VagrantMindInquiry.progress(p.player());
		helper.assertTrue(finished.memoryReported() && finished.biologyReported(), "both reports are recorded: " + finished);
		helper.succeed();
	}

	/**
	 * ARRANGE: a real {@code VagrantMindPiece} far out in the End, registered as a generated start in its chunk with
	 * references in every node chunk, as generation would. Returns a cell inside its interior. Chunk data and blocks
	 * are put back when the test ends.
	 */
	private static BlockPos vagrantMind(GameTestHelper helper, ServerLevel end) {
		int x = 44000 + (UUID.randomUUID().hashCode() & 0x3ff) * 512;
		VagrantMindPiece piece = new VagrantMindPiece(new BlockPos(x, 160, 44000), 37L);
		Structure structure = end.registryAccess().registryOrThrow(Registries.STRUCTURE).get(Hemomancy.rloc("vagrant_mind"));
		helper.assertTrue(structure != null, "the vagrant_mind structure is registered");
		BlockPos inside = null;
		for (BlockPos node : piece.nodePositions()) {
			if (piece.containsInterior(node.above())) {
				inside = node.above();
				break;
			}
			if (inside == null && piece.containsInterior(node)) inside = node;
		}
		if (inside == null && piece.awakeningPosition() != null && piece.containsInterior(piece.awakeningPosition())) {
			inside = piece.awakeningPosition();
		}
		helper.assertTrue(inside != null, "the Vagrant Mind piece has an interior cell");
		Map<LevelChunk, Map<Structure, StructureStart>> starts = new HashMap<>();
		Map<LevelChunk, Map<Structure, LongSet>> references = new HashMap<>();
		List<BlockPos> chunks = new ArrayList<>(piece.nodePositions());
		chunks.add(piece.origin());
		chunks.add(inside);
		LevelChunk startChunk = end.getChunkAt(piece.origin());
		for (BlockPos pos : chunks) {
			LevelChunk chunk = end.getChunkAt(pos);
			if (starts.containsKey(chunk)) continue;
			starts.put(chunk, new HashMap<>(chunk.getAllStarts()));
			Map<Structure, LongSet> old = new HashMap<>(chunk.getAllReferences());
			old.replaceAll((key, value) -> new LongOpenHashSet(value));
			references.put(chunk, old);
		}
		startChunk.setStartForStructure(structure, new StructureStart(structure, startChunk.getPos(), 0,
				new PiecesContainer(List.of(piece))));
		for (LevelChunk chunk : starts.keySet()) chunk.addReferenceForStructure(structure, startChunk.getPos().toLong());
		BlockPos origin = piece.origin().immutable();
		PlaythroughCleanup.onEnd(helper, () -> {
			starts.forEach(LevelChunk::setAllStarts);
			references.forEach(LevelChunk::setAllReferences);
			VagrantMindEncounterData.get(end).abandon(origin);
		});
		return inside.immutable();
	}

	/** ARRANGE: a block set in the End, put back when the test ends. */
	private static void setEnd(GameTestHelper helper, ServerLevel end, BlockPos pos, BlockState state) {
		BlockState old = end.getBlockState(pos);
		end.setBlock(pos, state, 2);
		PlaythroughCleanup.onEnd(helper, () -> end.setBlock(pos, old, 2));
	}

	// =================================================================================================================
	// The Eternal Covenant
	// =================================================================================================================

	/** The Eternal Covenant across a death: a player would expect the sealed covenant and its bonus to stay. */
	// DECISION D6-3 (S-1): Eternal Covenant is a root persistent key, so its +500 mL bonus is lost on death and the
	// rite can be performed again. Options: move it with S-1 (PlayerPersisted or its own attachment) / keep until S-1.
	@GameTest(template = EMPTY, timeoutTicks = 1500, batch = RITES, required = false)
	public static void eternalCovenantAcrossDeath(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		tools(p); // ARRANGE
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "eternal_covenant");
		clearRiteAtEnd(p);
		// ARRANGE: the covenant sealed as its completion seals it (the rite needs a sworn helper; the Vigil tests play
		// that ceremony kind): the root key, the max-blood ledger applied, the advancement.
		p.player().getPersistentData().putBoolean(MaxBloodLedger.ETERNAL_COVENANT_TAG, true);
		MaxBloodLedger.apply(p.player(), HemoCapabilityAccess.getBloodVolume(p.player()));
		HarbingerAdvancementGranter.grantIfNotDone(p.player(), HarbingerAdvancementGranter.ADV_ETERNAL_COVENANT_SEALED);
		double sealedMax = HemoCapabilityAccess.getBloodVolume(p.player()).getMaxBloodVolume();
		double[] afterMax = new double[1];
		boolean[] keyAfter = new boolean[1];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
			p.wield(ItemInit.living_staff.get());
			Outcome sealed = p.useOn(floor.focus());
			PlaythroughLog.note(helper, "eternalCovenantAcrossDeath", "sealed max " + sealedMax + "; replant before death: "
					+ sealed.saidKeys());
			helper.assertTrue(CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) == null
					&& said(sealed, COVENANT_ALREADY_SEALED), "a sealed covenant refuses a second rite; " + sealed);
			p.die();
			p.respawn();
			tools(p); // ARRANGE: the pack was dropped on death
			PlaythroughStates.blood(p, 5000.0D); // ARRANGE
			p.tickOnce();
			afterMax[0] = HemoCapabilityAccess.getBloodVolume(p.player()).getMaxBloodVolume();
			keyAfter[0] = p.player().getPersistentData().getBoolean(MaxBloodLedger.ETERNAL_COVENANT_TAG);
			p.moveTo(PlaythroughRites.riteStand(floor));
			p.wield(ItemInit.living_staff.get());
			Outcome again = p.useOn(floor.focus());
			boolean started = CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) != null;
			PlaythroughLog.note(helper, "eternalCovenantAcrossDeath", "D6-3: after death max " + afterMax[0] + " (sealed "
					+ sealedMax + ") key kept=" + keyAfter[0] + " advancement kept="
					+ p.has(adv(HarbingerAdvancementGranter.ADV_ETERNAL_COVENANT_SEALED)) + " replant started=" + started
					+ " said " + again.saidKeys());
			clearRiteNow(p);
			helper.assertTrue(keyAfter[0] && afterMax[0] >= sealedMax && !started,
					"D6-3 expected: the sealed covenant and its bonus survive death and the rite stays sealed");
		}).run();
	}

	// =================================================================================================================
	// The D6 whisper memo
	// =================================================================================================================

	/**
	 * The fungal whisper at D6: the player's tick count arranged onto the Sanctified whisper interval (ARRANGE, 18000
	 * ticks unplayed), one player tick queues the whisper in the inbox (whispers wait there; the chat link opens them).
	 * Opened as {@code /hemowhisper} does, it carries its memo option; the memo is noted through that option, and once
	 * written into the Liber (ARRANGE) the next interval queues nothing. Whether an unnoted, or noted but unwritten,
	 * whisper is queued again is logged.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void d6WhisperMemo(GameTestHelper helper) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 10));
		PlaythroughStates.sanctifiedD6(p); // ARRANGE
		String memo = MemoHelper.memoEvent(MemoDefinitions.FUNGAL_WHISPER_SANCTIFIED.id());
		helper.assertFalse(LiberKnowledgeHelper.knowsMemo(p.player(), MemoDefinitions.FUNGAL_WHISPER_SANCTIFIED.id()),
				"the Sanctified memo is not known yet");
		int stagger = p.player().getId() * 137;
		int first = Math.floorMod(-stagger, SANCTIFIED_WHISPER_INTERVAL) + SANCTIFIED_WHISPER_INTERVAL;
		int before = p.pendingWhispers().size();
		p.player().tickCount = first; // ARRANGE: a whisper interval has passed
		p.tickOnce();
		helper.assertTrue(p.pendingWhispers().size() == before + 1, "one whisper is queued at the D6 interval; inbox "
				+ before + " -> " + p.pendingWhispers().size());
		Outcome opened = p.openWhisper();
		Optional<DialogueView> whisper = p.whisper();
		helper.assertTrue(whisper.isPresent(), "the queued whisper opens; " + opened);
		whisper.get().assertLangKeys(helper, "the D6 whisper").assertNoDeadEnds(helper, "the D6 whisper");
		PlaythroughLog.note(helper, "d6WhisperMemo", "first whisper: " + whisper.get().describe() + " lines " + whisper.get().lineKeys());
		helper.assertTrue(whisper.get().lineKeys().stream().anyMatch(key -> key.startsWith("hemomancy.whisper.sanctified.")),
				"the D6 whisper is the Sanctified one: " + whisper.get().lineKeys());
		helper.assertTrue(whisper.get().offers(memo), "the whisper offers its memo: " + whisper.get().describe());

		int queued = p.pendingWhispers().size();
		p.player().tickCount = first + SANCTIFIED_WHISPER_INTERVAL; // ARRANGE: the next interval, the memo not noted
		p.tickOnce();
		int afterUnnoted = p.pendingWhispers().size();
		PlaythroughLog.note(helper, "d6WhisperMemo", "an unnoted whisper is queued again at the next interval: "
				+ (afterUnnoted > queued) + " (inbox " + queued + " -> " + afterUnnoted + ")");

		Outcome noted = p.chooseWhisper(memo);
		PlaythroughLog.note(helper, "d6WhisperMemo", "memo noted: " + noted.describe());
		noted.said("message.hemomancy.memo.noted");
		ResourceLocation memoId = MemoDefinitions.FUNGAL_WHISPER_SANCTIFIED.id();
		helper.assertTrue(HemoCapabilityAccess.getLiberKnowledge(p.player()).getPendingMemos().contains(memoId),
				"choosing the memo option notes the memo for the Liber; " + noted);

		int afterNote = p.pendingWhispers().size();
		p.player().tickCount = first + 2 * SANCTIFIED_WHISPER_INTERVAL; // ARRANGE: another interval, noted but not written
		p.tickOnce();
		PlaythroughLog.note(helper, "d6WhisperMemo", "a noted but unwritten memo's whisper is queued again: "
				+ (p.pendingWhispers().size() > afterNote) + " (inbox " + afterNote + " -> " + p.pendingWhispers().size() + ")");

		// ARRANGE: the noted memo written into the Liber (the book's dictation, not under test here).
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE: dictation is paid in blood
		var dictated = MemoHelper.dictatePendingToLiber(p.player(), new ItemStack(ItemInit.liber_sanguinum.get()));
		helper.assertTrue(LiberKnowledgeHelper.knowsMemo(p.player(), memoId), "the dictated memo is known; " + dictated);
		int afterWrite = p.pendingWhispers().size();
		p.player().tickCount = first + 3 * SANCTIFIED_WHISPER_INTERVAL; // ARRANGE: another interval
		p.tickOnce();
		helper.assertTrue(p.pendingWhispers().size() == afterWrite, "a written memo's whisper is not queued again; inbox "
				+ afterWrite + " -> " + p.pendingWhispers().size());
		helper.succeed();
	}

	// =================================================================================================================
	// The D6 NPC sweep
	// =================================================================================================================

	/**
	 * Every outpost speaker a D6 player meets, plus the Hermit of an unspent temple: each opens a dialogue whose keys
	 * all resolve and that has no dead end; root options the hub draws no card for are logged. A forged Weight of the
	 * Frame brief (a Degree 7 assignment) at D6 records nothing and gives nothing.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void d6NpcSweep(GameTestHelper helper) {
		Entity alchemist = PlaythroughNpcs.alchemist(helper, PlaythroughSites.stand(helper, 6, 6));
		Entity mnemonist = PlaythroughNpcs.mnemonist(helper, PlaythroughSites.stand(helper, 6, 12));
		Entity artificer = PlaythroughNpcs.artificer(helper, PlaythroughSites.stand(helper, 6, 18));
		PlaythroughSites.BloodTemple temple = PlaythroughSites.bloodTemple(helper, PlaythroughSites.stand(helper, 20, 8));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 12, 12));
		PlaythroughStates.sanctifiedD6(p, PlaythroughSites.stand(helper, 26, 20)); // ARRANGE
		List<String> problems = new ArrayList<>();
		for (Entity npc : List.of(alchemist, mnemonist, artificer, temple.hermit())) {
			p.approach(npc);
			Optional<DialogueView> opened = p.tryTalkTo(npc);
			String name = PlaythroughPlayer.describe(npc) + " at D6";
			if (opened.isEmpty()) {
				problems.add(name + " opens nothing");
				continue;
			}
			DialogueView view = opened.get();
			PlaythroughLog.note(helper, "d6NpcSweep", name + ": " + view.describe() + " lines " + view.lineKeys()
					+ " hidden root options " + view.rootOptionsWithoutCard() + " unreachable " + view.unreachableNodes()
					+ " emptied " + view.emptiedNodes());
			if (!view.missingLangKeys().isEmpty()) problems.add(name + " missing keys " + view.missingLangKeys());
			if (!view.deadEnds().isEmpty()) problems.add(name + " dead ends " + view.deadEnds());
			for (String literal : view.literalLines()) {
				PlaythroughLog.note(helper, "d6NpcSweep", name + ": literal line (not a key) " + literal);
			}
		}
		p.approach(artificer);
		p.talkTo(artificer);
		Outcome brief = p.forge(HarbingerArtificerDialogueTrees.EVENT_BRIEF_WEIGHT_OF_FRAME, artificer.getId());
		PlaythroughLog.note(helper, "d6NpcSweep", "forged Weight of the Frame brief at D6: " + brief.describe());
		brief.gaveNothing();
		helper.assertFalse(p.has(adv(ArtificerAssignments.WEIGHT_OF_FRAME_BRIEFED)), "a D7 brief forged at D6 records nothing");
		helper.assertTrue(problems.isEmpty(), "D6 speakers: " + problems);
		helper.succeed();
	}

	// =================================================================================================================
	// Shared helpers
	// =================================================================================================================

	/**
	 * Lays the Greater Threshold floor now and adds the Rite of the Bloodline Covenant to {@code clock}: pillars,
	 * the three offerings lit, the Staff planted, the wave fought with a sword. Returns the floor.
	 */
	private static PlaythroughSites.RiteFloor playSanctifiedRite(GameTestHelper helper, PlaythroughPlayer p, PlaythroughClock clock) {
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), "sanctified_rite");
		tools(p); // ARRANGE
		clock.then(() -> PlaythroughStates.blood(p, 5000.0D)); // ARRANGE: a rested pool to light the braziers
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
					PlaythroughStates.blood(p, 5000.0D); // ARRANGE: a rested pool for eight anchors
					p.wield(ItemInit.living_staff.get());
				})
				.step(() -> PlaythroughClock.rite(p, floor.focus(), fighting(p)));
		return floor;
	}

	/** The standard plan with the wave fought, a sword in hand when the player carries one. */
	private static RitePlan fighting(PlaythroughPlayer p) {
		return RitePlan.standard().fightThreats().weapon(Items.NETHERITE_SWORD);
	}

	/**
	 * The {@code extraAim} for a Working craft: during {@code SUBJECT_FEEDING}, the subject cell nearest the player's
	 * eye. With {@code refill} the pool is topped up between feeds (ARRANGE: a flask drunk; the subject wants more than
	 * one pool).
	 */
	private static BiFunction<ActiveCardinalRite, CardinalRiteRecipe, Vec3> feedSubject(PlaythroughPlayer p,
			List<BlockPos> cells, boolean refill) {
		return (rite, recipe) -> {
			if (rite.getPhase() != CardinalRitePhase.SUBJECT_FEEDING || cells.isEmpty()) return null;
			if (refill && p.blood() < 1500.0D) {
				PlaythroughStates.blood(p, 5000.0D); // ARRANGE
				PlaythroughLog.note(p.helper(), "ARRANGE: the pool refilled between subject feeds");
			}
			Vec3 eye = p.player().getEyePosition();
			return cells.stream().map(Vec3::atCenterOf)
					.min(Comparator.comparingDouble(point -> point.distanceToSqr(eye))).orElse(null);
		};
	}

	/** ARRANGE: Blood Projection, Blood Absorption, a Living Staff and a sword, each given only when missing. */
	private static void tools(PlaythroughPlayer p) {
		for (Item tool : List.of(ItemInit.blood_projection.get(), ItemInit.blood_absorption.get(), ItemInit.living_staff.get(),
				Items.NETHERITE_SWORD)) {
			if (p.count(tool) == 0) p.give(new ItemStack(tool));
		}
	}

	/**
	 * The Staff back in the pack after a rite. A rite that ends with the caster online restores it at once; when it
	 * did not, the rite's recovery is delivered as the 20-tick sweep would (the sweep reaches only level-listed
	 * players, which a captured player is not), and that is logged.
	 */
	private static void returnStaff(PlaythroughPlayer p) {
		if (p.count(ItemInit.living_staff.get()) > 0) return;
		PlaythroughLog.note(p.helper(), "the Staff was not in the pack at the rite's end; delivering the rite recovery");
		CardinalRiteSavedData.get(p.player().server.overworld()).deliverRecovery(p.player());
	}

	/** Clears a rite this test started without {@link PlaythroughClock#rite}, its threats, daemon and Staff escrow. */
	private static void clearRiteAtEnd(PlaythroughPlayer p) {
		PlaythroughCleanup.onEnd(p.helper(), () -> clearRiteNow(p));
	}

	private static void clearRiteNow(PlaythroughPlayer p) {
		ServerLevel level = p.player().serverLevel();
		CardinalRiteSavedData saved = CardinalRiteSavedData.get(level);
		ActiveCardinalRite rite = saved.getRite(p.uuid());
		if (rite == null) return;
		CardinalRiteOrdealEngine.clearThreats(level, rite);
		CardinalRiteStaffEscrow.restore(p.player(), rite);
		saved.removeRite(p.uuid());
		HumanitySpriteEntity daemon = HumanitySpriteEntity.findBoundToRite(level, p.uuid(), rite.getCenterPos());
		if (daemon != null) daemon.discard();
	}

	private static int dropsNear(ServerLevel level, BlockPos near, Item item, double radius) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(near).inflate(radius), drop -> drop.getItem().is(item))
				.stream().mapToInt(drop -> drop.getItem().getCount()).sum();
	}

	private static boolean said(Outcome outcome, String key) {
		return outcome.chat().stream().anyMatch(line -> line.has(key));
	}

	private static String adv(ResourceLocation id) {
		return id.getPath();
	}
}
