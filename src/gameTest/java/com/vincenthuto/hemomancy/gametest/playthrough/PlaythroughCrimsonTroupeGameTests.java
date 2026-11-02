package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusDiscoveryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import com.vincenthuto.hemomancy.common.entity.mob.animal.PeacockSpiderEntity;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusCarouselRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueAttention;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueNode;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTopicState;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.HarbingerMnemonistDialogueTrees;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerMnemonistEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeActs;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeCard;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeService;
import com.vincenthuto.hemomancy.common.entity.npc.trade.NpcTradeWant;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.MarionetteCrossbarItem;
import com.vincenthuto.hemomancy.common.menu.PuppeteersSpindleMenu;
import com.vincenthuto.hemomancy.common.mission.circus.CircusPracticalController;
import com.vincenthuto.hemomancy.common.network.circus.PacketSyncCircusPerception;
import com.vincenthuto.hemomancy.common.network.summon.PacketCrossbarRadialAction;
import com.vincenthuto.hemomancy.common.network.trade.NpcTradeResultPacket;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Band CT: the Crimson Troupe (the Circus) from a fresh Degree 5 Harbinger onward. Optional Side content: the Vicar's
 * and the Mnemonist's pointers, the Scarlet Waybill, pavilion discovery and acclimation, the first-talk milestones,
 * the Marionette Crossbar and the Puppeteer's Spindle, the Threadkeeper's inspection and reports, the Retired
 * Understudy's D5 Mnemonist Puppet (lesson, Cardinal Ordeal, practical), guest acts, the two investigations, the
 * Ringmaster's attention act, route choice and repair, both finales, trades, and the late-degree ordeal refusal.
 * <p>
 * Tests that need a whole pavilion lay the real {@code crimson_troupe_pavilion} template beside an {@code empty} test
 * ({@code PlaythroughSites.pavilion}, its own batch each). The template does not reliably bring every actor, so
 * {@link #pavilion} places any missing one on its template mark. A smaller "ring" (a carousel and the Ringmaster on
 * the empty template) serves tests that are about the Ringmaster's rules, not the tent. Every test whose code finds the
 * player through the level ({@code getPlayerByUUID}, {@code getNearestPlayer}: practicals, puppets, acts, the
 * carousel) calls {@code joinLevel()}. The harness player is never ticked by the level, so {@link #live} stands in for
 * the server's player tick: the tick counter (discovery reads it), item cooldowns (the Crossbar's call cooldown) and
 * the player tick events.
 * <p>
 * Fix regressions carry {@code // FIX: LF-n} and are required; they fail until the fix lands. Decision probes carry
 * {@code // DECISION CT-n:} and are optional.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughCrimsonTroupeGameTests {
	private static final String EMPTY = "empty";
	private static final String BATCH = "playthrough_crimson_troupe";
	private static final String RITES = "playthrough_crimson_troupe_rites";
	private static final String RING = "playthrough_crimson_troupe_ring";
	private static final String CLOCK = "playthrough_crimson_troupe_clock";
	private static final String PAVILION = "playthrough_crimson_troupe_pavilion";
	private static final int Y = PlaythroughSites.STAND_Y;
	private static final BlockPos RITE_ORIGIN = new BlockPos(24, 3, 12);

	private static final String MNEMONIST = PuppeteerSummonDefinitions.MNEMONIST_PUPPET;
	private static final String VULTURE = PuppeteerSummonDefinitions.VEINWING_VULTURE;
	private static final String MUMMER = PuppeteerSummonDefinitions.SCARLET_MUMMER;
	private static final String JUGGLER = PuppeteerSummonDefinitions.MARROW_SPITTER;
	private static final String TRIAL_RECIPE = "puppeteer_trial_mnemonist_puppet";

	private static final String WAYBILL = HarbingerMnemonistDialogueTrees.EVENT_CIRCUS_WAYBILL;
	private static final String TEACH = "circus_school_teach";
	private static final String PRACTICAL = "circus_school_practical";
	private static final String GUEST = "circus_school_guest";
	private static final String TESTIMONY = "circus_school_testimony";
	private static final String INSPECT = "circus_school_inspect";
	private static final String REPORT = "circus_school_report";
	private static final String CLAIM = "circus_school_claim";

	private static final String SCHOOL = "hemomancy.circus.school.";
	private static final String REFERRAL_CARD = "hemomancy.dialogue.vicar.option.circus_referral";
	private static final String NOT_FOUND = "item.hemomancy.circus_waybill.not_found";
	private static final String ATTUNED = "item.hemomancy.circus_waybill.attuned";
	private static final String DISCOVERY = "hemomancy.circus.discovery";
	private static final String INSPECTED = SCHOOL + "inspected";
	private static final String INSPECT_FAILED = SCHOOL + "inspect_failed";
	private static final String REPORT_COMPLETE = SCHOOL + "report_complete";
	private static final String REPORT_INCOMPLETE = SCHOOL + "report_incomplete";
	private static final String REWARD = SCHOOL + "reward";
	/** LF-7: the line for a guest payment delivered in full (nothing set aside). */
	private static final String REWARD_DELIVERED = SCHOOL + "reward.delivered";
	private static final String UNLOCK_FIRST = SCHOOL + "unlock_first";
	private static final String PASSED = SCHOOL + "passed";
	private static final String RETRY = SCHOOL + "retry";
	private static final String SILENT = "hemomancy.circus.ringmaster.silent";
	private static final String CLAIMED = "hemomancy.circus.finale.claimed";
	private static final String NOT_READY = "hemomancy.circus.finale.not_ready";
	private static final String SEVERED = "hemomancy.circus.captive.severed";
	private static final String PATH_GATE = "message.hemomancy.cardinal_rite_activation.this_rite_cannot_answer_your_current";
	private static final String VOID = "message.hemomancy.puppeteer_trial_rite_controller.the_void_has_claimed_your_will";
	/** LF-11: the new Circus system lines live under this prefix. */
	private static final String SYSTEM_LINE = "message.hemomancy.circus.";

	private static final ResourceLocation SPECTACLE = Hemomancy.rloc("circus_ringmaster/spectacle");
	private static final ResourceLocation BELL = Hemomancy.rloc("circus_ringmaster/bell");
	private static final ResourceLocation LEDGER_PAPER = Hemomancy.rloc("circus_threadkeeper/ledger_paper");
	private static final ResourceLocation RIGGING_LINE = Hemomancy.rloc("circus_acrobat/rigging_line");

	/** The actors' marks in {@code crimson_troupe_pavilion} (template-relative). */
	private static final Map<PlaythroughNpcs.CircusRole, BlockPos> PAVILION_MARKS = Map.of(
			PlaythroughNpcs.CircusRole.RINGMASTER, new BlockPos(32, 8, 37),
			PlaythroughNpcs.CircusRole.CAROUSEL, new BlockPos(32, 1, 32),
			PlaythroughNpcs.CircusRole.FIRE_EATER, new BlockPos(28, 1, 28),
			PlaythroughNpcs.CircusRole.ACROBAT, new BlockPos(36, 1, 28),
			PlaythroughNpcs.CircusRole.STILT_WALKER, new BlockPos(28, 1, 36),
			PlaythroughNpcs.CircusRole.KNIFE_THROWER, new BlockPos(36, 1, 36),
			PlaythroughNpcs.CircusRole.THREADKEEPER, new BlockPos(8, 1, 7),
			PlaythroughNpcs.CircusRole.UNDERSTUDY, new BlockPos(56, 1, 55),
			PlaythroughNpcs.CircusRole.STRONGMAN, new BlockPos(8, 1, 26),
			PlaythroughNpcs.CircusRole.BEAST_TAMER, new BlockPos(57, 1, 26));

	/** Passive acclimation one act's ticks can add inside the pavilion (one point per 80 living ticks). */
	private static final int PASSIVE_SLACK = 6;

	private PlaythroughCrimsonTroupeGameTests() {
	}

	// =============================================================================================================
	// Pointers: the Vicar, the Mnemonist and the Scarlet Waybill
	// =============================================================================================================

	/**
	 * CT-1/CT-2 at D5: the Vicar's Circus card is there and reads in English, its line follows discovery; the Mnemonist
	 * gives one Scarlet Waybill, hides the option while one is carried (a forged ask gives nothing), and gives another
	 * once it is lost.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void d5ReferralAndWaybill(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, helper.absolutePos(new BlockPos(6, Y, 6)));
		HarbingerMnemonistEntity mnemonist = PlaythroughNpcs.mnemonist(helper, helper.absolutePos(new BlockPos(6, Y, 18)));
		PlaythroughPlayer p = d5(helper, 8, 6);

		DialogueView view = p.talkTo(vicar);
		view.assertLangKeys(helper, "the D5 Vicar").assertNoDeadEnds(helper, "the D5 Vicar");
		helper.assertTrue(view.optionByText(REFERRAL_CARD).isPresent() && view.hasNode("circus_referral"),
				"the D5 Vicar has the Circus referral; " + view.describe());
		helper.assertTrue(view.rootOptionsWithoutCard().stream().noneMatch(hidden -> hidden.startsWith(REFERRAL_CARD)),
				"the hub draws a card for the Circus referral; hidden " + view.rootOptionsWithoutCard());
		List<String> lines = nodeLines(view, "circus_referral");
		helper.assertTrue(lines.contains("hemomancy.vicar.circus_referral.lead")
						&& lines.contains("hemomancy.vicar.circus_referral.mnemonist"),
				"before discovery the referral leads to the Circus and names the Mnemonist: " + lines);
		CircusDiscoveryProgress.markDiscovered(p.player()); // ARRANGE: the pavilion found (pavilionDiscoveryAndAcclimation plays it)
		lines = nodeLines(p.talkTo(vicar), "circus_referral");
		helper.assertTrue(lines.contains("hemomancy.vicar.circus_referral.discovered"),
				"after discovery the referral says the Circus was found: " + lines);

		p.approach(mnemonist);
		DialogueView first = p.talkTo(mnemonist);
		first.assertLangKeys(helper, "the D5 Mnemonist").assertNoDeadEnds(helper, "the D5 Mnemonist");
		helper.assertTrue(first.offers(WAYBILL), "the D5 Mnemonist offers the Scarlet Waybill; " + first.describe());
		Outcome taken = p.choose(mnemonist, WAYBILL);
		taken.said("hemomancy.dialogue.event.mnemonist_circus_waybill");
		p.pickUpNear(mnemonist.blockPosition(), ItemInit.circus_waybill.get());
		helper.assertTrue(p.count(ItemInit.circus_waybill.get()) == 1, "one Scarlet Waybill; " + taken);

		DialogueView carrying = p.talkTo(mnemonist);
		helper.assertTrue(!carrying.offers(WAYBILL) && carrying.hasNode("circus"),
				"while one is carried the waybill option is hidden, the Circus card stays; " + carrying.describe());
		Outcome again = p.forge(WAYBILL, mnemonist.getId());
		p.pickUpNear(mnemonist.blockPosition(), ItemInit.circus_waybill.get());
		helper.assertTrue(p.count(ItemInit.circus_waybill.get()) == 1, "a forged second ask gives nothing; " + again);

		removeAll(p, stack -> stack.is(ItemInit.circus_waybill.get())); // ARRANGE: the waybill is lost
		DialogueView lost = p.talkTo(mnemonist);
		helper.assertTrue(lost.offers(WAYBILL), "a lost waybill can be asked for again; " + lost.describe());
		Outcome reissued = p.choose(mnemonist, WAYBILL);
		p.pickUpNear(mnemonist.blockPosition(), ItemInit.circus_waybill.get());
		helper.assertTrue(p.count(ItemInit.circus_waybill.get()) == 1, "the lost waybill is reissued once; " + reissued);
		PlaythroughLog.note(helper, "d5ReferralAndWaybill", "first " + taken.describe() + "; reissued " + reissued.describe());
		helper.succeed();
	}

	/** CT-3: on the GameTest flat world both waybill modes (normal and sneaking) say nothing was found. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void waybillFindsNothing(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 8, 12);
		p.give(new ItemStack(ItemInit.circus_waybill.get())); // ARRANGE: the Mnemonist's waybill (d5ReferralAndWaybill)
		p.wield(ItemInit.circus_waybill.get());
		Outcome normal = p.use(InteractionHand.MAIN_HAND);
		p.player().setShiftKeyDown(true);
		Outcome sneaking;
		try {
			sneaking = p.use(InteractionHand.MAIN_HAND);
		} finally {
			p.player().setShiftKeyDown(false);
		}
		PlaythroughLog.note(helper, "waybillFindsNothing", "normal " + normal.saidKeys() + "; sneaking " + sneaking.saidKeys());
		normal.said(NOT_FOUND).didNotSay(ATTUNED);
		sneaking.said(NOT_FOUND).didNotSay(ATTUNED);
		helper.succeed();
	}

	/** CT-3: the waybill finding a real camp or pavilion needs a seeded world. */
	@GameTest(template = EMPTY, timeoutTicks = 20, batch = BATCH, required = false)
	public static void waybillLocatesACamp(GameTestHelper helper) {
		PlaythroughNotRun.notRun(helper, "the Scarlet Waybill locates troupe camps (troupe_teacher_<camp>) and pavilions "
						+ "(circus_waybill_targets) placed by seeded worldgen; the GameTest flat world generates none",
				"a seeded-world client run of the waybill toward the next untaught professor and a sneak-use toward an "
						+ "unfinished pavilion");
	}

	// =============================================================================================================
	// Discovery, acclimation and first talks
	// =============================================================================================================

	/**
	 * CT-4 on the real pavilion template (its site registered): living ticks inside discover the Circus once and add
	 * one point per 80 ticks; dead and spectator ticks keep the discovery, add nothing and sync inactive; a D0
	 * inactive-blood visitor discovers too; a troupe camp and the abandoned stage are not discovery.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = PAVILION + "_discovery")
	public static void pavilionDiscoveryAndAcclimation(GameTestHelper helper) {
		PlaythroughSites.TemplateSite pavilion = PlaythroughSites.pavilion(helper, PlaythroughSites.farOrigin(helper, 64));
		PlaythroughSites.TemplateSite camp = PlaythroughSites.camp(helper, "air", PlaythroughSites.farOrigin(helper, 140));
		PlaythroughSites.TemplateSite stage = PlaythroughSites.abandonedStage(helper, PlaythroughSites.farOrigin(helper, 176));
		BlockPos inside = pavilion.at(20, 1, 20);
		PlaythroughPlayer p = d5(helper, 8, 12);
		PlaythroughPlayer visitor = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 12));
		PlaythroughStates.freshD0(visitor); // ARRANGE: a D0 visitor with inactive blood
		PlaythroughPlayer camper = d5(helper, 12, 12);
		int[] score = new int[1];
		PlaythroughClock clock = PlaythroughClock.start(helper)
				.then(() -> {
					p.moveTo(inside); // ARRANGE: walked in
					p.player().tickCount = 0;
					score[0] = CircusPlayerProgress.acclimation(p.player());
				})
				.everyTick(160, tick -> live(p))
				.then(() -> {
					helper.assertTrue(CircusDiscoveryProgress.hasDiscovered(p.player()), "160 living ticks inside discover the Circus");
					helper.assertTrue(lines(p, DISCOVERY) == 1, "the discovery line is said once: " + p.chat());
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score[0] + 2,
							"one acclimation point per 80 ticks: " + score[0] + " -> " + CircusPlayerProgress.acclimation(p.player()));
					helper.assertTrue(lastPerception(p).map(PacketSyncCircusPerception::active).orElse(false),
							"the perception sync says the player is inside");
					score[0] = CircusPlayerProgress.acclimation(p.player());
					p.die();
				})
				.everyTick(160, tick -> live(p))
				.then(() -> {
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score[0], "dead ticks add nothing");
					helper.assertTrue(!lastPerception(p).map(PacketSyncCircusPerception::active).orElse(true),
							"dead ticks sync the player as outside");
					p.respawn();
					p.moveTo(inside); // ARRANGE: walked back in
					p.player().tickCount = 0;
					p.player().setGameMode(GameType.SPECTATOR);
				})
				.everyTick(160, tick -> live(p))
				.then(() -> {
					helper.assertTrue(CircusDiscoveryProgress.hasDiscovered(p.player()), "the discovery survives death and spectating");
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score[0], "spectator ticks add nothing");
					helper.assertTrue(!lastPerception(p).map(PacketSyncCircusPerception::active).orElse(true),
							"spectator ticks sync the player as outside");
					helper.assertTrue(lines(p, DISCOVERY) == 1, "discovery is never announced again: " + p.chat());
					p.survival();
					p.player().tickCount = 0;
				})
				.everyTick(80, tick -> live(p))
				.then(() -> helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score[0] + 1,
						"living again, the next 80 ticks add a point"))
				.then(() -> {
					visitor.moveTo(inside);
					visitor.player().tickCount = 0;
				})
				.everyTick(80, tick -> live(visitor))
				.then(() -> helper.assertTrue(CircusDiscoveryProgress.hasDiscovered(visitor.player())
								&& CircusPlayerProgress.acclimation(visitor.player()) == 1 && visitor.degree() == 0,
						"a D0 visitor with inactive blood discovers the Circus and acclimates, with no rank credit"))
				.then(() -> {
					camper.moveTo(camp.at(12, 1, 12));
					camper.player().tickCount = 0;
				})
				.everyTick(80, tick -> live(camper))
				.then(() -> {
					camper.moveTo(stage.at(12, 1, 12));
					camper.player().tickCount = 0;
				})
				.everyTick(80, tick -> live(camper))
				.then(() -> helper.assertTrue(!CircusDiscoveryProgress.hasDiscovered(camper.player())
								&& CircusPlayerProgress.acclimation(camper.player()) == 0,
						"a troupe camp and the abandoned stage are not the pavilion: no discovery, no acclimation"));
		clock.run();
	}

	/**
	 * CT-5: the first right-click on each of the eight performer roles adds 50 acclimation, once per role; a second talk
	 * adds nothing; a D0 visitor earns it too and hears no Harbinger pointer. The +50 is undocumented in CIRCUS.md
	 * (LF-17, doc drift, code authoritative).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void firstTalkMilestones(GameTestHelper helper) {
		Map<PlaythroughNpcs.CircusRole, Entity> troupe = lineUp(helper, 6);
		PlaythroughPlayer p = d5(helper, 14, 12);
		List<String> rows = new ArrayList<>();
		for (var entry : troupe.entrySet()) {
			p.approach(entry.getValue());
			int before = CircusPlayerProgress.acclimation(p.player());
			DialogueView first = p.talkTo(entry.getValue());
			int afterFirst = CircusPlayerProgress.acclimation(p.player());
			first.assertLangKeys(helper, entry.getKey() + " at D5").assertNoDeadEnds(helper, entry.getKey() + " at D5");
			helper.assertTrue(afterFirst == before + 50, entry.getKey() + ": the first talk adds 50 acclimation, "
					+ before + " -> " + afterFirst);
			helper.assertTrue(allLineKeys(first).contains("hemomancy.dialogue.circus_performer.welcome"),
					entry.getKey() + ": the first talk is a welcome");
			DialogueView second = p.talkTo(entry.getValue());
			helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == afterFirst,
					entry.getKey() + ": a second talk adds nothing");
			helper.assertTrue(!allLineKeys(second).contains("hemomancy.dialogue.circus_performer.welcome"),
					entry.getKey() + ": a second talk is not a welcome");
			rows.add(entry.getKey() + " +" + (afterFirst - before));
		}
		PlaythroughPlayer stranger = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 14, 18));
		PlaythroughStates.freshD0(stranger); // ARRANGE
		Entity keeper = troupe.get(PlaythroughNpcs.CircusRole.THREADKEEPER);
		stranger.approach(keeper);
		DialogueView d0 = stranger.talkTo(keeper);
		d0.assertLangKeys(helper, "the Threadkeeper at D0").assertNoDeadEnds(helper, "the Threadkeeper at D0");
		helper.assertTrue(CircusPlayerProgress.acclimation(stranger.player()) == 50, "a D0 visitor's first talk adds 50 too");
		helper.assertTrue(allLineKeys(d0).stream().noneMatch(key -> key.contains("vicar")),
				"a D0 visitor is not sent to the Vicar: " + allLineKeys(d0));
		PlaythroughLog.note(helper, "firstTalkMilestones", "LF-17 doc drift: +50 acclimation on the first talk to each of "
				+ "the eight roles (camps included, no pavilion needed), up to 400; CIRCUS.md lists only 100/100/150. " + rows);
		helper.succeed();
	}

	// =============================================================================================================
	// The Crossbar, the Spindle and the Threadkeeper
	// =============================================================================================================

	/**
	 * CT-6: the first use attunes an unbound Crossbar; another player's Crossbar is refused; at a placed Spindle the
	 * Crossbar and Filaments go in by shift-click, the Crossbar charges, a known shape is prepared through the
	 * Spindle's payload, and the Crossbar comes back out charged and prepared.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH)
	public static void crossbarAttuneChargePrepare(GameTestHelper helper) {
		PlaythroughPlayer p = d5(helper, 8, 12);
		PlaythroughPlayer other = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 18));
		PlaythroughStates.freshD0(other); // ARRANGE
		p.give(new ItemStack(ItemInit.marionette_crossbar.get())); // ARRANGE: a crafted Crossbar
		PlaythroughRites.wieldStack(p, PlaythroughCrimsonTroupeGameTests::unbound, "an unbound Crossbar");
		Outcome bound = p.use(InteractionHand.MAIN_HAND);
		bound.said("hemomancy.summon.bind.success");
		UUID own = MarionetteCrossbarItem.ensureCrossbarId(p.player().getMainHandItem());
		helper.assertTrue(MarionetteCrossbarItem.isBoundTo(p.player().getMainHandItem(), p.player()), "the Crossbar is attuned; " + bound);

		other.give(new ItemStack(ItemInit.marionette_crossbar.get())); // ARRANGE
		PlaythroughRites.wieldStack(other, PlaythroughCrimsonTroupeGameTests::unbound, "an unbound Crossbar");
		other.use(InteractionHand.MAIN_HAND);
		ItemStack foreign = other.player().getMainHandItem().copy();
		other.player().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); // ARRANGE: handed over
		p.give(foreign);
		PlaythroughRites.wieldStack(p, stack -> MarionetteCrossbarItem.isBoundTo(stack, other.player()), "the other player's Crossbar");
		Outcome refused = p.use(InteractionHand.MAIN_HAND);
		refused.said("hemomancy.summon.crossbar.foreign");
		helper.assertTrue(MarionetteCrossbarItem.isBoundTo(p.player().getMainHandItem(), other.player()),
				"a foreign Crossbar stays its owner's; " + refused);

		BlockPos spindle = helper.absolutePos(new BlockPos(11, Y, 12));
		p.give(new ItemStack(BlockInit.puppeteers_spindle.get())); // ARRANGE: a crafted Spindle
		Outcome placed = p.place(spindle, BlockInit.puppeteers_spindle.get());
		helper.assertTrue(helper.getLevel().getBlockState(spindle).is(BlockInit.puppeteers_spindle.get()), "the Spindle is placed; " + placed);
		PlaythroughRites.emptyHand(p);
		Outcome opened = p.useOn(spindle);
		helper.assertTrue(p.player().containerMenu instanceof PuppeteersSpindleMenu, "the Spindle opens its menu; " + opened);
		PuppeteersSpindleMenu menu = (PuppeteersSpindleMenu) p.player().containerMenu;
		int crossbarSlot = menuSlotOf(menu, p, stack -> own.equals(MarionetteCrossbarItem.getCrossbarId(stack)));
		helper.assertTrue(crossbarSlot >= 0, "the Spindle menu shows the player's Crossbar");
		p.quickMove(menu, crossbarSlot);
		helper.assertTrue(own.equals(MarionetteCrossbarItem.getCrossbarId(menu.getCrossbarStack())),
				"shift-click seats the Crossbar on the Spindle");
		p.give(new ItemStack(ItemInit.puppeteering_thread.get(), 16)); // ARRANGE: Filaments in the pack
		p.quickMove(menu, menuSlotOf(menu, p, stack -> stack.is(ItemInit.puppeteering_thread.get())));
		PlaythroughClock.start(helper)
				.idle(10)
				.then(() -> {
					helper.assertTrue(menu.getCrossbarThread() > 0, "the Spindle charges the seated Crossbar: " + menu.getCrossbarThread());
					knowShape(p, VULTURE);
					Outcome prepared = p.spindlePrepare(VULTURE);
					prepared.said("hemomancy.summon.spindle.prepared");
					helper.assertTrue(VULTURE.equals(menu.getSelectedSummonName()), "the Spindle prepares the shape; " + prepared);
					p.quickMove(menu, PuppeteersSpindleMenu.CROSSBAR_SLOT);
					p.closeMenu();
					ItemStack back = MarionetteCrossbarItem.findCrossbar(p.player(), own).orElse(ItemStack.EMPTY);
					helper.assertTrue(MarionetteCrossbarItem.getThread(back) > 0
									&& VULTURE.equals(MarionetteCrossbarItem.getSelectedSummonName(back)),
							"the Crossbar comes back charged and prepared");
				})
				.run();
	}

	/**
	 * CT-7: a charged, attuned Crossbar in hand is inspected and recorded; an empty hand, an uncharged Crossbar and
	 * another player's Crossbar fail with the inspect_failed line; below D3 the refusal says why.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void threadkeeperInspect(GameTestHelper helper) {
		Entity keeper = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.THREADKEEPER, helper.absolutePos(new BlockPos(6, Y, 12)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		UUID charged = attunedCrossbar(p, 100);
		UUID dry = attunedCrossbar(p, 0);

		PlaythroughRites.emptyHand(p);
		p.talkTo(keeper);
		p.choose(keeper, INSPECT).said(INSPECT_FAILED).didNotSay(INSPECTED);
		holdCrossbar(p, dry);
		p.talkTo(keeper);
		p.choose(keeper, INSPECT).said(INSPECT_FAILED).didNotSay(INSPECTED);
		PlaythroughPlayer other = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 18));
		PlaythroughStates.freshD0(other); // ARRANGE
		attunedCrossbar(other, 100);
		ItemStack lent = other.player().getMainHandItem().copy();
		other.player().setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY); // ARRANGE: lent to the player
		p.give(lent);
		PlaythroughRites.wieldStack(p, stack -> MarionetteCrossbarItem.isBoundTo(stack, other.player()), "the lent Crossbar");
		p.talkTo(keeper);
		p.choose(keeper, INSPECT).said(INSPECT_FAILED).didNotSay(INSPECTED);

		holdCrossbar(p, charged);
		p.talkTo(keeper);
		Outcome inspected = p.choose(keeper, INSPECT);
		inspected.said(INSPECTED);
		var state = CircusApprenticeshipProgress.state(p.player());
		helper.assertTrue(state.hasUUID("primer.crossbar") && charged.equals(state.getUUID("primer.crossbar")),
				"the inspected Crossbar is the one on record; " + inspected);

		PlaythroughPlayer votary = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 8));
		PlaythroughStates.votaryD2(votary); // ARRANGE
		attunedCrossbar(votary, 100);
		votary.approach(keeper);
		votary.talkTo(keeper);
		Outcome low = votary.choose(keeper, INSPECT);
		PlaythroughLog.note(helper, "threadkeeperInspect", "D2 inspect: " + low.describe());
		low.didNotSay(INSPECTED);
		// FIX: LF-11 - an inspect below Degree 3 is refused with a system line saying why.
		helper.assertTrue(saysSystemLine(low), "FIX LF-11: an inspect below D3 says why it is refused; " + low.describe());
		helper.succeed();
	}

	// =============================================================================================================
	// The Retired Understudy's Mnemonist Puppet: lesson, ordeal, practical
	// =============================================================================================================

	/**
	 * CT-8, CT-9, CT-11: the untaught ordeal is refused (instruction_required); the Understudy teaches the D5 shape
	 * (recipes, status moves to the ordeal); too little blood is refused (no_blood); the Dominion Lesser ordeal with
	 * the Crossbar as the Focus medium is planted, the trial puppet beaten, 1400 mL paid and the shape learned, the
	 * Crossbar kept; the practical is passed by a real replay (+100); a second ordeal is refused while a body is out
	 * on the Crossbar (crossbar_active), then as already_known once it is recalled.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 12000, batch = RITES)
	public static void understudyMnemonistPuppet(GameTestHelper helper) {
		CircusPerformerEntity understudy = (CircusPerformerEntity) PlaythroughNpcs.circus(helper,
				PlaythroughNpcs.CircusRole.UNDERSTUDY, helper.absolutePos(new BlockPos(5, Y, 12)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		p.joinLevel();
		armUp(p, true); // ARRANGE: armed for the trial; the fight's balance is not under test
		p.give(new ItemStack(ItemInit.living_staff.get())); // ARRANGE
		p.give(new ItemStack(ItemInit.blood_projection.get())); // ARRANGE
		UUID crossbar = attunedCrossbar(p, 200);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), TRIAL_RECIPE);
		PlaythroughCleanup.onEnd(helper, () -> CircusPracticalController.cancel(p.player()));
		double[] before = new double[1];
		int[] mark = new int[2];
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
					Outcome untaught = plant(p, floor);
					untaught.said(SCHOOL + "instruction_required");
					helper.assertTrue(!riteRunning(p), "an untaught ordeal does not start; " + untaught);
				})
				.then(() -> {
					p.moveTo(helper.absolutePos(new BlockPos(7, Y, 12)));
					DialogueView ready = p.talkTo(understudy);
					ready.assertLangKeys(helper, "the Understudy at D5").assertNoDeadEnds(helper, "the Understudy at D5");
					helper.assertTrue(allLineKeys(ready).contains(SCHOOL + "status.lesson") && ready.offers(TEACH),
							"a D5 player is offered the Understudy's lesson; " + ready.describe());
					Outcome taught = p.choose(understudy, TEACH);
					PlaythroughLog.note(helper, "understudyMnemonistPuppet", "lesson: " + taught.describe());
					helper.assertTrue(p.player().getRecipeBook().contains(Hemomancy.rloc("cardinal_rite/" + TRIAL_RECIPE)),
							"the lesson unlocks the ordeal's recipe; " + taught);
					DialogueView after = p.talkTo(understudy);
					helper.assertTrue(allLineKeys(after).contains(SCHOOL + "status.ordeal")
							&& !CircusApprenticeshipProgress.knows(p.player(), MNEMONIST),
							"after the lesson the Understudy points to the ordeal and the shape is not yet known");
				})
				.then(() -> {
					PlaythroughStates.blood(p, 500.0D); // ARRANGE: too little blood for the ordeal
					Outcome dry = plant(p, floor);
					dry.said("hemomancy.summon.trial.no_blood");
					helper.assertTrue(!riteRunning(p), "an ordeal without its blood does not start; " + dry);
					PlaythroughStates.blood(p, 5000.0D); // ARRANGE
					p.moveTo(PlaythroughRites.riteStand(floor));
					p.wield(ItemInit.living_staff.get());
					before[0] = p.blood();
					mark[0] = p.mark();
				})
				.step(() -> PlaythroughClock.rite(p, floor.focus(), RitePlan.standard().trialPuppet().noExtraAim()
						.weapon(Items.NETHERITE_SWORD).maxTicks(4000)))
				.then(() -> {
					helper.assertTrue(p.chatSince(mark[0]).stream().anyMatch(line -> line.has("hemomancy.summon.trial.started")),
							"the ordeal manifests its trial puppet: " + p.chatSince(mark[0]));
					helper.assertTrue(CircusApprenticeshipProgress.knows(p.player(), MNEMONIST),
							"beating the trial puppet binds the Mnemonist Puppet shape");
					double spent = before[0] - p.blood();
					PlaythroughLog.note(helper, "understudyMnemonistPuppet", "ordeal spent " + spent + " mL net of regeneration");
					// The ordeal pays 1400 mL once (BloodAccounting.pay); the volume regenerates while the rite runs.
					helper.assertTrue(spent >= 1300.0D && spent <= 1400.5D, "the ordeal pays its 1400 mL once; net spent " + spent);
					for (BlockPos socket : PlaythroughRites.usedSockets(floor)) {
						helper.assertTrue(!PlaythroughRites.brazier(p, socket).hasOffering(),
								"the ordeal draws the offering at " + socket.toShortString());
					}
					helper.assertTrue(helper.getLevel().getBlockEntity(floor.focus()) instanceof CardinalFocusBlockEntity focus
									&& crossbar.equals(MarionetteCrossbarItem.getCrossbarId(focus.getMediumForMatching())),
							"the Crossbar is kept in the Focus");
				})
				.then(() -> {
					p.player().removeEffect(MobEffects.DAMAGE_BOOST); // ARRANGE: a practical blow is a light one
					takeCrossbarBack(p, floor, crossbar);
					p.moveTo(helper.absolutePos(new BlockPos(7, Y, 12)));
					holdCrossbar(p, crossbar);
					Outcome called = p.crossbarCallOrRecall();
					called.said("hemomancy.summon.called");
					p.talkTo(understudy);
					Outcome begun = p.choose(understudy, PRACTICAL);
					begun.said(SCHOOL + "exercise." + MNEMONIST);
					helper.assertTrue(rehearsalTargets(p).size() == 2, "the practical brings two rehearsal threats; " + begun);
					// A left-click with the Crossbar is a focus command, not a blow: the player swaps it to the off hand
					// (the summon stays bound to an equipped Crossbar) and strikes with the sword.
					crossbarToOffhand(p, crossbar);
					p.wield(Items.NETHERITE_SWORD);
					mark[1] = CircusPlayerProgress.acclimation(p.player());
					mark[0] = p.mark();
				})
				.step(() -> replayPractical(p, mark[0], 1600))
				.then(() -> {
					helper.assertTrue(CircusApprenticeshipProgress.practicalComplete(p.player(), MNEMONIST),
							"the replayed strike passes the practical");
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == mark[1] + 100,
							"a passed practical adds 100 acclimation once");
					helper.assertTrue(!MarionetteCrossbarItem.activeSummonsForCrossbar(p.player(), crossbar, MNEMONIST).isEmpty(),
							"the puppet is still out on the Crossbar");
					crossbarFromOffhand(p);
				});
		PlaythroughRites.braziers(clock, p, floor);
		clock.then(() -> {
					PlaythroughRites.seatMedium(p, floor, ItemInit.marionette_crossbar.get());
					Outcome active = plant(p, floor);
					active.said("hemomancy.summon.trial.crossbar_active");
					helper.assertTrue(!riteRunning(p), "an ordeal on a Crossbar with a body out does not start; " + active);
					takeCrossbarBack(p, floor, crossbar);
					holdCrossbar(p, crossbar);
				})
				.everyTick(25, tick -> live(p))
				.then(() -> {
					Outcome recalled = p.crossbarCallOrRecall();
					recalled.said("hemomancy.summon.recalled");
					PlaythroughRites.seatMedium(p, floor, ItemInit.marionette_crossbar.get());
					Outcome known = plant(p, floor);
					known.said("hemomancy.summon.trial.already_known");
					helper.assertTrue(!riteRunning(p), "a known shape's ordeal does not start again; " + known);
				})
				.run();
	}

	/**
	 * CT-10: An Empty Ring. Call, command and recall with an uninspected Crossbar count for nothing; with the
	 * inspected one they let the Threadkeeper's report complete it once (+100); a second report finds nothing new.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = BATCH)
	public static void emptyRingReport(GameTestHelper helper) {
		Entity keeper = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.THREADKEEPER, helper.absolutePos(new BlockPos(6, Y, 12)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		p.joinLevel();
		armUp(p, false); // ARRANGE
		knowShape(p, VULTURE);
		UUID inspected = attunedCrossbar(p, 200);
		UUID spare = attunedCrossbar(p, 200);
		int[] score = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					holdCrossbar(p, inspected);
					p.talkTo(keeper);
					p.choose(keeper, INSPECT).said(INSPECTED);
					holdCrossbar(p, spare);
					p.crossbarCallOrRecall().said("hemomancy.summon.called");
				})
				.everyTick(25, tick -> live(p))
				.then(() -> {
					p.radial(PacketCrossbarRadialAction.Action.SET_MODE, "guard").said("hemomancy.summon.command.guard");
					p.crossbarCallOrRecall().said("hemomancy.summon.recalled");
					p.talkTo(keeper);
					score[0] = CircusPlayerProgress.acclimation(p.player());
					Outcome early = p.choose(keeper, REPORT);
					early.said(REPORT_INCOMPLETE).didNotSay(REPORT_COMPLETE);
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score[0],
							"another Crossbar's call, command and recall count for nothing; " + early);
					holdCrossbar(p, inspected);
				})
				.everyTick(25, tick -> live(p))
				.then(() -> p.crossbarCallOrRecall().said("hemomancy.summon.called"))
				.everyTick(25, tick -> live(p))
				.then(() -> {
					p.radial(PacketCrossbarRadialAction.Action.SET_MODE, "guard").said("hemomancy.summon.command.guard");
					p.crossbarCallOrRecall().said("hemomancy.summon.recalled");
					p.talkTo(keeper);
					score[0] = CircusPlayerProgress.acclimation(p.player());
					Outcome report = p.choose(keeper, REPORT);
					report.said(REPORT_COMPLETE);
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score[0] + 100
									&& CircusApprenticeshipProgress.state(p.player()).getBoolean("quest.empty_ring"),
							"An Empty Ring is reported once for 100 acclimation; " + report);
					p.talkTo(keeper);
					Outcome twice = p.choose(keeper, REPORT);
					twice.said(REPORT_INCOMPLETE);
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score[0] + 100,
							"a second report adds nothing; " + twice);
				})
				.run();
	}

	// =============================================================================================================
	// Guest acts and the Threadkeeper's cards
	// =============================================================================================================

	/**
	 * CT-12: a guest payment collected in full says it was delivered (LF-7), a full pack keeps the exact four set aside
	 * and says so, made room collects them; a second guest act the same day is refused; the next day it begins.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void guestActPaymentLines(GameTestHelper helper) {
		Entity keeper = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.THREADKEEPER, helper.absolutePos(new BlockPos(6, Y, 6)));
		Entity understudy = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.UNDERSTUDY, helper.absolutePos(new BlockPos(6, Y, 18)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		PlaythroughCleanup.onEnd(helper, () -> CircusPracticalController.cancel(p.player()));
		knowShape(p, MNEMONIST);
		CircusApprenticeshipProgress.completePractical(p.player(), MNEMONIST); // ARRANGE: understudyMnemonistPuppet passes it by play
		var state = CircusApprenticeshipProgress.state(p.player());
		long today = helper.getLevel().getDayTime() / 24000L;

		state.putLong("guest.day", today); // ARRANGE: today's guest act was performed...
		state.putInt("guest.pending", 4); // ARRANGE: ...and its four Filaments are owed
		p.approach(keeper);
		p.talkTo(keeper);
		Outcome paid = p.choose(keeper, CLAIM);
		paid.gave(ItemInit.puppeteering_thread.get(), 4);
		helper.assertTrue(state.getInt("guest.pending") == 0, "nothing stays set aside; " + paid);
		PlaythroughLog.note(helper, "guestActPaymentLines", "full payment said " + paid.saidKeys());
		// FIX: LF-7 - a payment delivered in full says so, not "0 Filaments are set aside".
		paid.said(REWARD_DELIVERED).didNotSay(REWARD);

		state.putInt("guest.pending", 4); // ARRANGE: another payment owed
		p.fillInventory(); // ARRANGE: a full pack
		p.talkTo(keeper);
		Outcome full = p.choose(keeper, CLAIM);
		full.said(REWARD);
		helper.assertTrue(full.delta(ItemInit.puppeteering_thread.get()) == 0 && state.getInt("guest.pending") == 4,
				"a full pack keeps the exact four set aside; " + full);
		p.player().getInventory().items.set(20, ItemStack.EMPTY); // ARRANGE: the player makes room
		p.talkTo(keeper);
		Outcome collected = p.choose(keeper, CLAIM);
		collected.gave(ItemInit.puppeteering_thread.get(), 4);
		helper.assertTrue(state.getInt("guest.pending") == 0, "made room collects the four; " + collected);
		collected.said(REWARD_DELIVERED); // FIX: LF-7

		p.player().getInventory().clearContent(); // ARRANGE: the saddles are put away
		UUID crossbar = attunedCrossbar(p, 200);
		holdCrossbar(p, crossbar);
		p.approach(understudy);
		p.talkTo(understudy);
		Outcome sameDay = p.choose(understudy, GUEST);
		sameDay.said(UNLOCK_FIRST);
		helper.assertTrue(rehearsalTargets(p).isEmpty(), "a second guest act the same day does not begin; " + sameDay);
		state.putLong("guest.day", today - 1L); // ARRANGE: the last guest act was yesterday's
		p.talkTo(understudy);
		Outcome nextDay = p.choose(understudy, GUEST);
		nextDay.said(SCHOOL + "exercise." + MNEMONIST);
		helper.succeed();
	}

	/**
	 * LF-8: with nothing to hand in the Threadkeeper's claim card is not a Turn In and none of claim, inspect and report
	 * is Urgent; with a payment owed the claim card is a Turn In and Urgent.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH)
	public static void threadkeeperCardsOnlyWhenOwed(GameTestHelper helper) {
		Entity keeper = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.THREADKEEPER, helper.absolutePos(new BlockPos(6, Y, 12)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		DialogueView idle = p.talkTo(keeper);
		List<String> wrong = new ArrayList<>();
		if (idle.questState(CLAIM).map(state -> state == DialogueTopicState.TURN_IN).orElse(false)) {
			wrong.add("claim card is TURN_IN with nothing pending");
		}
		for (String event : List.of(CLAIM, INSPECT, REPORT)) {
			if (idle.attention(event).map(attention -> attention == DialogueAttention.URGENT).orElse(false)) {
				wrong.add(event + " is URGENT with nothing to hand in");
			}
		}
		CircusApprenticeshipProgress.state(p.player()).putInt("guest.pending", 4); // ARRANGE: a guest payment owed
		DialogueView owed = p.talkTo(keeper);
		PlaythroughLog.note(helper, "threadkeeperCardsOnlyWhenOwed", "idle " + idle.describe() + "; owed " + owed.describe());
		helper.assertTrue(owed.questState(CLAIM).orElse(null) == DialogueTopicState.TURN_IN
						&& owed.attention(CLAIM).orElse(null) == DialogueAttention.URGENT,
				"with a payment owed the claim card is a Turn In and Urgent; " + owed.describe());
		// FIX: LF-8 - a card is Turn In (and Urgent) only when something can be handed in.
		helper.assertTrue(wrong.isEmpty(), "FIX LF-8: " + wrong + "; " + idle.describe());
		helper.succeed();
	}

	/**
	 * LF-11: refusals and confirmations that print nothing today each say something: a lesson taught, a practical
	 * asked for with no Crossbar in hand, below the shape's degree, or while the professor is busy, a claim with
	 * nothing pending, a route chosen and a route repaired. (A practical asked for out of range never reaches the
	 * school: the dialogue dispatch drops events from more than 8 blocks, as it does for every speaker.)
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH + "_refusals")
	public static void schoolRefusalsSpeak(GameTestHelper helper) {
		CircusPerformerEntity understudy = (CircusPerformerEntity) PlaythroughNpcs.circus(helper,
				PlaythroughNpcs.CircusRole.UNDERSTUDY, helper.absolutePos(new BlockPos(5, Y, 4)));
		Entity keeper = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.THREADKEEPER, helper.absolutePos(new BlockPos(5, Y, 20)));
		Ring ring = miniRing(helper);
		PlaythroughPlayer p = d5(helper, 8, 4);
		PlaythroughPlayer low = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughStates.initiateD3(low); // ARRANGE
		Map<String, Outcome> heard = new LinkedHashMap<>();
		PlaythroughClock.start(helper)
				.idle(2)
				.then(() -> {
					p.approach(understudy);
					p.talkTo(understudy);
					heard.put("lesson taught", p.choose(understudy, TEACH));
					knowShape(p, MNEMONIST);
					PlaythroughRites.emptyHand(p);
					p.talkTo(understudy);
					Outcome bare = p.choose(understudy, PRACTICAL);
					helper.assertTrue(rehearsalTargets(p).isEmpty(), "no practical begins without a Crossbar; " + bare);
					heard.put("practical with no Crossbar in hand", bare);

					knowShape(low, MNEMONIST);
					attunedCrossbar(low, 100);
					low.approach(understudy);
					low.talkTo(understudy);
					Outcome under = low.choose(understudy, PRACTICAL);
					helper.assertTrue(rehearsalTargets(low).isEmpty(), "no practical begins below the shape's degree; " + under);
					heard.put("practical below the shape's degree", under);

					p.approach(keeper);
					p.talkTo(keeper);
					heard.put("claim with nothing pending", p.choose(keeper, CLAIM));

					PlaythroughStates.acclimated(p, 500); // ARRANGE
					PlaythroughStates.pavilionRoute(p, CircusRouteRules.Route.NEUTRAL, 0b1); // ARRANGE: the attention act (attentionActAndRoute plays it)
					p.moveTo(ring.stand());
					p.talkTo(ring.ringmaster());
					heard.put("route chosen", p.choose(ring.ringmaster(), CircusRingmasterEntity.EVENT_ACCEPT));
					helper.assertTrue(CircusPlayerProgress.route(p.player()) == CircusRouteRules.Route.SUCCESSION, "the route is chosen");
					p.talkTo(ring.ringmaster());
					heard.put("route repaired", p.choose(ring.ringmaster(), CircusRingmasterEntity.EVENT_REPAIR));
					helper.assertTrue(CircusPlayerProgress.route(p.player()) == CircusRouteRules.Route.LIBERATION, "the route is repaired");

					armUp(p, false); // ARRANGE
					UUID crossbar = attunedCrossbar(p, 100);
					p.approach(understudy);
					p.talkTo(understudy); // the option is on screen when the professor is struck
					holdCrossbar(p, crossbar);
					p.attack(understudy);
					helper.assertTrue(!understudy.canTeachNow(), "a struck professor is busy with the threat");
					Outcome busy = p.choose(understudy, PRACTICAL);
					helper.assertTrue(rehearsalTargets(p).isEmpty(), "no practical begins while the professor is busy; " + busy);
					heard.put("practical while the professor is busy", busy);

					List<String> silent = new ArrayList<>();
					heard.forEach((what, outcome) -> {
						PlaythroughLog.note(helper, "schoolRefusalsSpeak", what + ": " + outcome.saidKeys());
						if (!saysSystemLine(outcome)) silent.add(what + " " + outcome.saidKeys());
					});
					// FIX: LF-11 - each says something (new message.hemomancy.circus.* keys with English).
					helper.assertTrue(silent.isEmpty(), "FIX LF-11: these print no system line: " + silent);
				})
				.run();
	}

	// =============================================================================================================
	// The investigations
	// =============================================================================================================

	/**
	 * CT-13 and CT-14 on the real pavilion and abandoned stage: both testimonies and both practicals, the four records
	 * (stage, loft, quarters, carousel) and the Understudy's testimony let one report at D4+ complete The Wrong
	 * Audience and The Missing Understudy (+150 each), once; a D3 player with the same testimonies is refused The Wrong
	 * Audience.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = PAVILION + "_evidence")
	public static void wrongAudienceAndMissingUnderstudy(GameTestHelper helper) {
		Pavilion pv = pavilion(helper);
		PlaythroughSites.TemplateSite stage = PlaythroughSites.abandonedStage(helper, PlaythroughSites.farOrigin(helper, 140));
		PlaythroughPlayer p = d5(helper, 8, 12);
		PlaythroughPlayer low = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 10, 12));
		PlaythroughStates.initiateD3(low); // ARRANGE
		PlaythroughClock.start(helper).until(200, pv::loaded, "the pavilion's chunks show their entities").then(pv::cast)
				.idle(2)
				.then(() -> {
					Entity stilt = pv.actor(PlaythroughNpcs.CircusRole.STILT_WALKER);
					Entity knife = pv.actor(PlaythroughNpcs.CircusRole.KNIFE_THROWER);
					Entity understudy = pv.actor(PlaythroughNpcs.CircusRole.UNDERSTUDY);
					Entity keeper = pv.actor(PlaythroughNpcs.CircusRole.THREADKEEPER);
					for (PlaythroughPlayer who : List.of(p, low)) {
						testimony(who, stilt);
						testimony(who, knife);
						knowShape(who, MUMMER);
						knowShape(who, JUGGLER);
						CircusApprenticeshipProgress.completePractical(who.player(), MUMMER); // ARRANGE: the practicals are their own tests
						CircusApprenticeshipProgress.completePractical(who.player(), JUGGLER); // ARRANGE
					}
					testimony(p, understudy);
					clue(p, stage.at(12, 1, 15), "stage");
					clue(p, pv.at(56, 4, 7), "loft");
					clue(p, pv.at(56, 1, 54), "quarters");
					clue(p, pv.at(15, 1, 32), "carousel");

					low.approach(keeper);
					low.talkTo(keeper);
					Outcome tooLow = low.choose(keeper, REPORT);
					tooLow.said(REPORT_INCOMPLETE);
					helper.assertTrue(!CircusApprenticeshipProgress.state(low.player()).getBoolean("quest.wrong_audience"),
							"The Wrong Audience is not reported below D4; " + tooLow);

					p.approach(keeper);
					p.talkTo(keeper);
					int score = CircusPlayerProgress.acclimation(p.player());
					Outcome report = p.choose(keeper, REPORT);
					report.said(REPORT_COMPLETE);
					var state = CircusApprenticeshipProgress.state(p.player());
					helper.assertTrue(state.getBoolean("quest.wrong_audience") && state.getBoolean("quest.missing_understudy"),
							"one report completes both investigations; " + report + "; school state " + state);
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score + 300,
							"each investigation adds 150 acclimation: " + score + " -> " + CircusPlayerProgress.acclimation(p.player()));
					p.talkTo(keeper);
					Outcome twice = p.choose(keeper, REPORT);
					twice.said(REPORT_INCOMPLETE);
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score + 300, "a second report adds nothing; " + twice);
				})
				.run();
	}

	/** CT-2: the abandoned stage holds the only stage record, and nothing points a player to it. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = BATCH, required = false)
	public static void abandonedStageHasNoPointer(GameTestHelper helper) {
		ResourceLocation stage = Hemomancy.rloc("troupe_abandoned_stage");
		Map<String, Boolean> tags = new LinkedHashMap<>();
		for (String tag : List.of("circus_waybill_targets", "troupe_teacher_air", "troupe_teacher_blades",
				"troupe_teacher_embers", "troupe_teacher_memory", "troupe_teacher_pageantry")) {
			tags.put(tag, structureTagHas(helper, tag, stage));
		}
		Entity keeper = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.THREADKEEPER, helper.absolutePos(new BlockPos(6, Y, 6)));
		Entity understudy = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.UNDERSTUDY, helper.absolutePos(new BlockPos(6, Y, 18)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		List<String> mentions = new ArrayList<>();
		for (Entity speaker : List.of(keeper, understudy)) {
			p.approach(speaker);
			for (String key : allLineKeys(p.talkTo(speaker))) if (key.contains("stage") || key.contains("abandoned")) mentions.add(key);
		}
		PlaythroughLog.note(helper, "abandonedStageHasNoPointer", "waybill tags holding the stage " + tags
				+ "; lines naming it " + mentions);
		// DECISION CT-2: The Missing Understudy's stage record exists only in troupe_abandoned_stage, but no waybill
		// mode or NPC says where one is (the ledger and testimony only name it). Options: a waybill mode or target for
		// the stage, an NPC direction, or exploration only.
		helper.assertTrue(tags.containsValue(true), "DECISION CT-2: no waybill mode can point to an abandoned stage: " + tags);
		helper.succeed();
	}

	// =============================================================================================================
	// The Ringmaster
	// =============================================================================================================

	/**
	 * CT-1: from every standable floor cell of the real pavilion, can a survival player's reach (as the server's
	 * interact check measures it) touch the idle Ringmaster on his perch?
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = PAVILION + "_reach", required = false)
	public static void ringmasterReachFromTheFloor(GameTestHelper helper) {
		Pavilion pv = pavilion(helper);
		PlaythroughPlayer p = d5(helper, 8, 12);
		PlaythroughClock.start(helper).until(200, pv::loaded, "the pavilion's chunks show their entities").then(pv::cast)
				.idle(3)
				.then(() -> {
					ServerLevel level = helper.getLevel();
					AABB ringmaster = pv.ringmaster().getBoundingBox();
					int cells = 0;
					List<String> reachable = new ArrayList<>();
					for (int x = 0; x < 65; x++) {
						for (int z = 0; z < 65; z++) {
							BlockPos floor = pv.at(x, 0, z);
							if (level.getBlockState(floor).getCollisionShape(level, floor).isEmpty()
									|| !level.getBlockState(floor.above()).getCollisionShape(level, floor.above()).isEmpty()
									|| !level.getBlockState(floor.above(2)).getCollisionShape(level, floor.above(2)).isEmpty()) continue;
							cells++;
							p.moveTo(floor.above());
							if (p.player().canInteractWithEntity(ringmaster, 1.0D)) reachable.add(x + "," + z);
						}
					}
					PlaythroughLog.note(helper, "ringmasterReachFromTheFloor", "Ringmaster at " + pv.ringmaster().position()
							+ "; " + cells + " standable floor cells, reachable from " + reachable);
					// DECISION CT-1: the idle Ringmaster perches on a y7 beam with no stairs; his pact, acts, finale and
					// trade box need a click a floor-standing player cannot make (CIRCUS.md calls the private
					// galleries implemented; no server code exists). Options: build the private geometry, add real
					// stairs or a reachable perch, lower the perch when idle, or accept building up.
					helper.assertTrue(!reachable.isEmpty(), "DECISION CT-1: no floor cell of " + cells
							+ " reaches the idle Ringmaster");
				})
				.run();
	}

	/**
	 * CT-15 and CT-16 on the real pavilion: below 500 acclimation the Ringmaster is silent; at 500 the attention act
	 * runs by keeping the trained spider in view for 200 ticks (bit 0, +150, one spectacle banked, the site released);
	 * then the warning nodes, a confirmed Succession (bit 1, +50, his line changes) and the one repair to Liberation,
	 * after which the repair is gone.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 900, batch = PAVILION + "_attention")
	public static void attentionActAndRoute(GameTestHelper helper) {
		Pavilion pv = pavilion(helper);
		PlaythroughPlayer p = d5(helper, 8, 12);
		p.joinLevel();
		PlaythroughStates.acclimated(p, 499); // ARRANGE
		int[] score = new int[1];
		PlaythroughClock.start(helper).until(200, pv::loaded, "the pavilion's chunks show their entities").then(pv::cast)
				.idle(2)
				.then(() -> {
					p.moveTo(pv.underPerch());
					int mark = p.mark();
					helper.assertTrue(p.tryTalkTo(pv.ringmaster()).isEmpty()
									&& p.chatSince(mark).stream().anyMatch(line -> line.has(SILENT)),
							"below 500 the Ringmaster is silent: " + p.chatSince(mark));
					PlaythroughStates.acclimated(p, 500); // ARRANGE: the last point of exposure
					DialogueView view = p.talkTo(pv.ringmaster());
					view.assertLangKeys(helper, "the Ringmaster at 500").assertNoDeadEnds(helper, "the Ringmaster at 500");
					helper.assertTrue(view.offers(CircusRingmasterEntity.EVENT_TRIAL) && !view.offers(CircusRingmasterEntity.EVENT_ACCEPT),
							"at 500 the attention act is offered first; " + view.describe());
					score[0] = CircusPlayerProgress.acclimation(p.player());
					Outcome begun = p.choose(pv.ringmaster(), CircusRingmasterEntity.EVENT_TRIAL);
					begun.said("hemomancy.circus.challenge.begin.0");
					helper.assertTrue(spider(p).isPresent(), "the trained peacock spider appears; " + begun);
				})
				.step(() -> act(p, "hemomancy.circus.challenge.complete.0", 320,
						() -> spider(p).map(spider -> spider.getBoundingBox().getCenter()).orElse(null)))
				.then(() -> {
					helper.assertTrue(p.chat().stream().anyMatch(line -> line.has("hemomancy.circus.challenge.hold.0")),
							"halfway the act says to keep watching");
						helper.assertTrue((CircusPlayerProgress.challenges(p.player()) & 1) != 0, "the attention act sets bit 0");
						int gained = CircusPlayerProgress.acclimation(p.player()) - score[0];
						// Inside the pavilion passive exposure adds a point per 80 living ticks on top of the act's 150.
						helper.assertTrue(gained >= 150 && gained <= 150 + PASSIVE_SLACK,
								"the attention act adds 150 (plus passive exposure): gained " + gained);
					helper.assertTrue(HemoCapabilityAccess.npcTrades(p.player()).acts(NpcTradeWant.Act.SPECTACLE) == 1,
							"one spectacle is banked");
					helper.assertTrue(site(pv.carousel()).activeOwner() == null && spider(p).isEmpty(),
							"the site is released and the spider gone");
					DialogueView choice = p.talkTo(pv.ringmaster());
					choice.assertLangKeys(helper, "the Ringmaster's pact").assertNoDeadEnds(helper, "the Ringmaster's pact");
					helper.assertTrue(choice.offers(CircusRingmasterEntity.EVENT_ACCEPT) && choice.offers(CircusRingmasterEntity.EVENT_REJECT)
									&& nodeLines(choice, "succession_warning").contains("hemomancy.circus.ringmaster.warning.succession"),
							"both routes are offered through their warnings; " + choice.describe());
					score[0] = CircusPlayerProgress.acclimation(p.player());
					Outcome chose = p.choose(pv.ringmaster(), CircusRingmasterEntity.EVENT_ACCEPT);
					helper.assertTrue(CircusPlayerProgress.route(p.player()) == CircusRouteRules.Route.SUCCESSION
									&& (CircusPlayerProgress.challenges(p.player()) & 2) != 0
									&& CircusPlayerProgress.acclimation(p.player()) == score[0] + 50,
							"confirming Succession sets the route, bit 1 and +50; " + chose);
					DialogueView bound = p.talkTo(pv.ringmaster());
					helper.assertTrue(allLineKeys(bound).contains("hemomancy.circus.ringmaster.line.succession")
									&& bound.offers(CircusRingmasterEntity.EVENT_REPAIR) && bound.offers(CircusRingmasterEntity.EVENT_TRIAL),
							"his line names Succession and the repair and next act are offered; " + bound.describe());
					Outcome repaired = p.choose(pv.ringmaster(), CircusRingmasterEntity.EVENT_REPAIR);
					helper.assertTrue(CircusPlayerProgress.route(p.player()) == CircusRouteRules.Route.LIBERATION,
							"the one repair switches to Liberation; " + repaired);
					DialogueView after = p.talkTo(pv.ringmaster());
					helper.assertTrue(!after.offers(CircusRingmasterEntity.EVENT_REPAIR)
									&& allLineKeys(after).contains("hemomancy.circus.ringmaster.line.liberation"),
							"the repair is used up; " + after.describe());
				})
				.run();
	}

	/** LF-9: an act owner who turns spectator during the attention act earns no bit, no acclimation and no spectacle. */
	@GameTest(template = EMPTY, timeoutTicks = 500, batch = RING)
	public static void spectatorOwnerEarnsNothing(GameTestHelper helper) {
		Ring ring = miniRing(helper);
		PlaythroughPlayer p = d5(helper, 16, 14);
		p.joinLevel();
		PlaythroughStates.acclimated(p, 500); // ARRANGE
		int[] score = new int[1];
		PlaythroughClock.start(helper)
				.idle(2)
				.then(() -> {
					p.moveTo(ring.stand());
					p.talkTo(ring.ringmaster());
					p.choose(ring.ringmaster(), CircusRingmasterEntity.EVENT_TRIAL).said("hemomancy.circus.challenge.begin.0");
					score[0] = CircusPlayerProgress.acclimation(p.player());
					p.player().setGameMode(GameType.SPECTATOR);
				})
				.everyTick(260, tick -> {
					spider(p).ifPresent(spider -> p.aim(spider.getBoundingBox().getCenter()));
					live(p);
				})
				.then(() -> {
					PlaythroughLog.note(helper, "spectatorOwnerEarnsNothing", "challenges " + CircusPlayerProgress.challenges(p.player())
							+ ", acclimation " + score[0] + " -> " + CircusPlayerProgress.acclimation(p.player()) + ", chat " + p.chat());
					// FIX: LF-9 - a spectator act owner fails the act (HR 17.1, CIRCUS.md: spectators earn nothing).
					helper.assertTrue((CircusPlayerProgress.challenges(p.player()) & 1) == 0, "FIX LF-9: a spectator earns no challenge bit");
					helper.assertTrue(CircusPlayerProgress.acclimation(p.player()) == score[0], "FIX LF-9: a spectator earns no acclimation");
					helper.assertTrue(HemoCapabilityAccess.npcTrades(p.player()).acts(NpcTradeWant.Act.SPECTACLE) == 0,
							"a spectator banks no spectacle");
					helper.assertTrue(site(ring.carousel()).activeOwner() == null, "FIX LF-9: the act ends and the site is released");
				})
				.run();
	}

	/** LF-10: act 2 (the troupe and the dolls) turns the troupe on the player, but not a faculty member by the ring. */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = RING)
	public static void act2FacultyStayOut(GameTestHelper helper) {
		Ring ring = miniRing(helper);
		CircusPerformerEntity strongman = (CircusPerformerEntity) PlaythroughNpcs.circus(helper,
				PlaythroughNpcs.CircusRole.STRONGMAN, helper.absolutePos(new BlockPos(21, Y, 7)));
		CircusPerformerEntity acrobat = (CircusPerformerEntity) PlaythroughNpcs.circus(helper,
				PlaythroughNpcs.CircusRole.ACROBAT, helper.absolutePos(new BlockPos(11, Y, 7)));
		PlaythroughPlayer p = d5(helper, 16, 14);
		p.joinLevel();
		armUp(p, false); // ARRANGE
		PlaythroughStates.acclimated(p, 500); // ARRANGE
		PlaythroughStates.pavilionRoute(p, CircusRouteRules.Route.SUCCESSION, 0b11); // ARRANGE: attention act and pact done
		PlaythroughClock.start(helper)
				.idle(2)
				.then(() -> {
					p.moveTo(ring.stand());
					p.talkTo(ring.ringmaster());
					p.choose(ring.ringmaster(), CircusRingmasterEntity.EVENT_TRIAL).said("hemomancy.circus.challenge.begin.2");
				})
				.everyTick(10, tick -> live(p))
				.then(() -> {
					PlaythroughLog.note(helper, "act2FacultyStayOut", "acrobat teaching " + acrobat.canTeachNow()
							+ ", strongman teaching " + strongman.canTeachNow());
					helper.assertTrue(!acrobat.canTeachNow(), "the original cast performs act 2 against the player");
					// FIX: LF-10 - armAct filters by participatesInFinale; new faculty do not join the original cast.
					helper.assertTrue(strongman.canTeachNow() && p.tryTalkTo(strongman).isPresent(),
							"FIX LF-10: a faculty member by the ring stays out of act 2 and can still be talked to");
				})
				.run();
	}

	/**
	 * CT-17 on the real pavilion: acts 2, 3 and 4 by standing the ring (+100 each), the finale refused before 1000,
	 * then the finale: the rafters, the troupe downed, the Ringmaster beaten in the descent; the Ringmaster Pattern,
	 * the Topper, the route complete and the site settled as SUCCESSION.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 4000, batch = PAVILION + "_succession")
	public static void successionFinale(GameTestHelper helper) {
		Pavilion pv = pavilion(helper);
		PlaythroughPlayer p = d5(helper, 8, 12);
		p.joinLevel();
		armUp(p, true); // ARRANGE: armed for the finale; its balance is not under test
		PlaythroughStates.acclimated(p, 500); // ARRANGE
		PlaythroughStates.pavilionRoute(p, CircusRouteRules.Route.SUCCESSION, 0b11); // ARRANGE: attentionActAndRoute plays these
		int[] topper = new int[1];
		PlaythroughClock clock = PlaythroughClock.start(helper).until(200, pv::loaded, "the pavilion's chunks show their entities").then(pv::cast).idle(2);
		int[][] acts = {{2, 200}, {3, 240}, {4, 300}};
		for (int[] act : acts) {
			int challenge = act[0];
			clock.then(() -> {
						p.moveTo(pv.underPerch());
						p.talkTo(pv.ringmaster());
						p.choose(pv.ringmaster(), CircusRingmasterEntity.EVENT_TRIAL).said("hemomancy.circus.challenge.begin." + challenge);
					})
					.step(() -> act(p, "hemomancy.circus.challenge.complete." + challenge, act[1] + 100, null))
					.then(() -> helper.assertTrue((CircusPlayerProgress.challenges(p.player()) & (1 << challenge)) != 0,
							"act " + challenge + " sets its bit"));
		}
		clock.then(() -> {
						int afterActs = CircusPlayerProgress.acclimation(p.player());
						// The acts add 300; passive exposure inside the pavilion adds a point per 80 living ticks.
						helper.assertTrue(afterActs >= 800 && afterActs <= 800 + 3 * PASSIVE_SLACK,
								"three acts add 300 (plus passive exposure): " + afterActs);
					p.talkTo(pv.ringmaster());
					Outcome early = p.choose(pv.ringmaster(), CircusRingmasterEntity.EVENT_FINALE);
					early.said(NOT_READY);
					helper.assertTrue(site(pv.carousel()).activeOwner() == null, "the finale does not begin before 1000; " + early);
					PlaythroughStates.acclimated(p, 1000); // ARRANGE: the rest is passive exposure (about ten hours inside)
					topper[0] = p.count(ItemInit.ringmaster_topper.get());
					p.talkTo(pv.ringmaster());
					p.choose(pv.ringmaster(), CircusRingmasterEntity.EVENT_FINALE);
					var site = site(pv.carousel());
					helper.assertTrue(p.uuid().equals(site.activeOwner()) && site.phase() == CircusPavilionStateRules.Phase.RAFTERS,
							"the finale begins in the rafters; phase " + site.phase());
					p.wield(Items.NETHERITE_SWORD);
				})
				.step(() -> finale(p, pv, true, 2400))
				.then(() -> {
					var site = site(pv.carousel());
					helper.assertTrue(p.chat().stream().anyMatch(line -> line.has("hemomancy.circus.finale.complete.succession")),
							"the Ringmaster yields: " + p.chat());
					helper.assertTrue(HemoCapabilityAccess.getKnownSummons(p.player()).getKnownSummonNames()
							.contains(PuppeteerSummonDefinitions.RINGMASTER_PATTERN), "the Ringmaster Pattern is known");
					helper.assertTrue(p.count(ItemInit.ringmaster_topper.get()) == topper[0] + 1, "the Topper is given once");
					helper.assertTrue(CircusPlayerProgress.route(p.player()) == CircusRouteRules.Route.SUCCESSION_COMPLETE,
							"the route is complete");
					helper.assertTrue(site.outcome() == CircusPavilionStateRules.Outcome.SUCCESSION && pv.ringmaster().isAlive(),
							"the site settles as SUCCESSION and the Ringmaster lives");
				});
		clock.run();
	}

	/**
	 * CT-18 on the real pavilion: the Liberation finale; the three captives severed by right-click, the three anchors
	 * struck, the descent, the Ringmaster killed; Thread Ripper, the Topper, the route complete, the site a ruin.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 2400, batch = PAVILION + "_liberation")
	public static void liberationFinale(GameTestHelper helper) {
		Pavilion pv = pavilion(helper);
		PlaythroughPlayer p = d5(helper, 8, 12);
		p.joinLevel();
		armUp(p, true); // ARRANGE
		PlaythroughStates.acclimated(p, 500); // ARRANGE
		PlaythroughStates.pavilionRoute(p, CircusRouteRules.Route.LIBERATION, 0b11); // ARRANGE
		int[] topper = new int[1];
		PlaythroughClock.start(helper).until(200, pv::loaded, "the pavilion's chunks show their entities").then(pv::cast)
				.idle(5)
				.then(() -> {
					p.moveTo(pv.underPerch());
					DialogueView view = p.talkTo(pv.ringmaster());
					helper.assertTrue(view.offers(CircusRingmasterEntity.EVENT_FINALE), "Liberation can begin its finale; " + view.describe());
					topper[0] = p.count(ItemInit.ringmaster_topper.get());
					p.choose(pv.ringmaster(), CircusRingmasterEntity.EVENT_FINALE);
					var site = site(pv.carousel());
					helper.assertTrue(p.uuid().equals(site.activeOwner()) && site.route() == CircusRouteRules.Route.LIBERATION,
							"the Liberation finale begins; " + site.phase());
				})
				.everyTick(5, tick -> live(p))
				.then(() -> {
					List<BloodDrunkPuppeteerEntity> captives = pv.carousel().getPassengers().stream()
							.filter(BloodDrunkPuppeteerEntity.class::isInstance).map(BloodDrunkPuppeteerEntity.class::cast).toList();
					helper.assertTrue(captives.size() == 3, "three captives ride the carousel: " + captives.size());
					for (BloodDrunkPuppeteerEntity captive : captives) p.interactOn(captive).said(SEVERED);
					helper.assertTrue(site(pv.carousel()).severedMask() == 0b111, "all three threads are cut");
					p.wield(Items.NETHERITE_SWORD);
					for (int horse = 0; horse < 3; horse++) {
						CircusCarouselRules.HorsePose pose = CircusCarouselRules.horsePose(pv.carousel().getYRot(), horse);
						Vec3 anchor = new Vec3(pv.carousel().getX() + pose.x(), pv.carousel().getY() + 1.1D + pose.bob(), pv.carousel().getZ() + pose.z());
						strikeAt(p, pv.carousel(), anchor);
					}
					var site = site(pv.carousel());
					helper.assertTrue(site.brokenMask() == 0b111 && site.phase() == CircusPavilionStateRules.Phase.DESCENT,
							"three anchors break and the Ringmaster descends; broken " + site.brokenMask() + ", phase " + site.phase());
				})
				.step(() -> finale(p, pv, false, 1200))
				.then(() -> {
					var site = site(pv.carousel());
					helper.assertTrue(p.chat().stream().anyMatch(line -> line.has("hemomancy.circus.finale.complete.liberation")),
							"the Ringmaster falls: " + p.chat());
					helper.assertTrue(p.knows("thread_ripper") || p.count(ItemInit.memory_thread_ripper.get()) > 0,
							"Liberation teaches Thread Ripper, or leaves its memory");
					helper.assertTrue(p.count(ItemInit.ringmaster_topper.get()) == topper[0] + 1, "the Topper is given once");
					helper.assertTrue(CircusPlayerProgress.route(p.player()) == CircusRouteRules.Route.LIBERATION_COMPLETE,
							"the route is complete");
					helper.assertTrue(site.outcome() == CircusPavilionStateRules.Outcome.RUIN, "the site is a ruin");
					BlockPos origin = pv.carousel().encounterOrigin();
					boolean webs = false;
					for (BlockPos offset : List.of(new BlockPos(3, 1, 0), new BlockPos(-3, 1, 0), new BlockPos(0, 1, 3),
							new BlockPos(0, 1, -3), new BlockPos(2, 2, 2), new BlockPos(-2, 2, -2))) {
						webs |= helper.getLevel().getBlockState(origin.offset(offset)).is(Blocks.COBWEB);
					}
					helper.assertTrue(webs, "the ruin leaves its cobwebs at the carousel");
				})
				.run();
	}

	// =============================================================================================================
	// Death and relog in the school and the ring
	// =============================================================================================================

	/**
	 * The Mummer practical under way: the pupil dies (the dead player's tick ends it: the threats are discarded and the
	 * retry line is said), respawns and asks again (the practical begins afresh); then logs out mid-practical (the
	 * logout clears the session and its threats) and on the next login can begin it again.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = BATCH + "_practical_death")
	public static void practicalDeathAndRelog(GameTestHelper helper) {
		Entity stilt = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.STILT_WALKER, helper.absolutePos(new BlockPos(5, Y, 12)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		p.joinLevel();
		PlaythroughCleanup.onEnd(helper, () -> CircusPracticalController.cancel(p.player()));
		knowShape(p, MUMMER);
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					beginMummer(p, stilt);
					mark[0] = p.mark();
					Outcome died = p.die();
					live(p); // the dead player's own tick
					PlaythroughLog.note(helper, "practicalDeathAndRelog", "the pupil dies mid-practical: " + died.describe()
							+ "; then heard " + p.chatSince(mark[0]));
					helper.assertTrue(rehearsalTargets(p).isEmpty(), "a death ends the practical and its threats go");
					helper.assertTrue(p.chatSince(mark[0]).stream().anyMatch(line -> line.has(RETRY)), "the retry line is said");
					p.respawn();
				})
				.idle(2)
				.then(() -> {
					beginMummer(p, stilt); // ARRANGE inside: a new attuned Crossbar, the old one fell with the body
					p.relog();
					p.joinLevel();
				})
				.idle(2)
				.then(() -> {
					helper.assertTrue(rehearsalTargets(p).isEmpty(), "a logout ends the practical and its threats go");
					beginMummer(p, stilt);
					PlaythroughLog.note(helper, "practicalDeathAndRelog", "the practical begins again after the relog");
				})
				.run();
	}

	/**
	 * Two pupils at one professor: A's practical is under way when B asks for theirs. B either gets a practical of
	 * their own (their own two threats, A's untouched) or is told why not. (Run: B gets their own; the suspected
	 * ring_blocked line, "blocked or wet", is not reached this way.)
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH + "_two_pupils")
	public static void twoPupilsOneProfessor(GameTestHelper helper) {
		Entity stilt = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.STILT_WALKER, helper.absolutePos(new BlockPos(5, Y, 12)));
		PlaythroughPlayer a = d5(helper, 8, 12);
		PlaythroughPlayer b = d5(helper, 8, 14);
		a.joinLevel();
		b.joinLevel();
		PlaythroughCleanup.onEnd(helper, () -> {
			CircusPracticalController.cancel(a.player());
			CircusPracticalController.cancel(b.player());
		});
		knowShape(a, MUMMER);
		knowShape(b, MUMMER);
		PlaythroughClock.start(helper)
				.then(() -> {
					beginMummer(a, stilt);
					UUID crossbar = attunedCrossbar(b, 200);
					holdCrossbar(b, crossbar);
					b.approach(stilt);
					b.talkTo(stilt);
					Outcome asked = b.choose(stilt, PRACTICAL);
					int theirs = rehearsalTargets(b).size();
					PlaythroughLog.note(helper, "twoPupilsOneProfessor", "B asks while A's threats stand: " + asked.describe()
							+ "; B's threats " + theirs + ", A's " + rehearsalTargets(a).size()
							+ (asked.saidKeys().contains(SCHOOL + "ring_blocked") ? " (CT-6: ring_blocked says 'blocked or wet')" : ""));
					helper.assertTrue(rehearsalTargets(a).size() == 2, "A's practical is untouched by B's ask");
					helper.assertTrue(theirs == 2 || !asked.chat().isEmpty(), "B gets a practical of their own or is told why not; "
							+ asked);
				})
				.run();
	}

	/**
	 * The attention act on the ring: the owner dies (the act fails with its line, the site is released on the respawn)
	 * and begins it again; relogs at once (inside the carousel's 200-tick grace the act and the site hold); then logs
	 * out past the grace (the carousel releases the site) and on return the stale act key is cleared on the first tick
	 * with no line.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 700, batch = RING + "_death")
	public static void actDeathAndRelog(GameTestHelper helper) {
		Ring ring = miniRing(helper);
		PlaythroughPlayer p = d5(helper, 16, 14);
		p.joinLevel();
		PlaythroughStates.acclimated(p, 500); // ARRANGE
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.idle(2)
				.then(() -> {
					beginAct(p, ring);
					mark[0] = p.mark();
					p.die();
					live(p); // the dead owner's own tick fails the act
					helper.assertTrue(p.chatSince(mark[0]).stream().anyMatch(line -> line.has("hemomancy.circus.challenge.failed")),
							"a death fails the act with its line: " + p.chatSince(mark[0]));
					p.respawn();
					helper.assertTrue(site(ring.carousel()).activeOwner() == null, "the site is released after the death");
					beginAct(p, ring); // the act can begin again
					p.relog();
					p.joinLevel(); // back at once, inside the grace
				})
				.idle(20)
				.then(() -> {
					helper.assertTrue(p.uuid().equals(site(ring.carousel()).activeOwner()), "inside the grace the site still holds the owner");
					helper.assertTrue(p.player().getPersistentData().getInt(ACTIVE_CHALLENGE) > 0, "and the act is still running");
					p.offline(); // away past the grace
				})
				.idle(230)
				.then(() -> {
					helper.assertTrue(site(ring.carousel()).activeOwner() == null, "past the grace the carousel releases the site");
					p.relog();
					p.joinLevel();
					mark[0] = p.mark();
					live(p);
					PlaythroughLog.note(helper, "actDeathAndRelog", "back after the grace: act key "
							+ p.player().getPersistentData().getInt(ACTIVE_CHALLENGE) + ", heard " + p.chatSince(mark[0]));
					helper.assertTrue(p.player().getPersistentData().getInt(ACTIVE_CHALLENGE) == 0, "the stale act key is cleared");
					beginAct(p, ring); // and the act can begin again
				})
				.run();
	}

	/**
	 * The Liberation finale on the ring: the owner dies in it. After the respawn the site is released and the finale
	 * can be begun again, with the carousel's three captives riding again.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = RING + "_finale_death")
	public static void finaleDeathResets(GameTestHelper helper) {
		Ring ring = miniRing(helper);
		PlaythroughPlayer p = d5(helper, 16, 14);
		p.joinLevel();
		armUp(p, false); // ARRANGE
		PlaythroughStates.acclimated(p, 500); // ARRANGE
		PlaythroughStates.pavilionRoute(p, CircusRouteRules.Route.LIBERATION, 0b11); // ARRANGE
		CircusRingmasterEntity rm = ring.ringmaster();
		PlaythroughClock.start(helper)
				.idle(2)
				.then(() -> {
					p.moveTo(ring.stand());
					p.talkTo(rm);
					p.choose(rm, CircusRingmasterEntity.EVENT_FINALE);
					helper.assertTrue(p.uuid().equals(site(ring.carousel()).activeOwner()), "the finale begins");
					Outcome died = p.die();
					PlaythroughLog.note(helper, "finaleDeathResets", "the owner dies in the finale (phase "
							+ site(ring.carousel()).phase() + "): " + died.describe());
					p.respawn();
				})
				.idle(40)
				.then(() -> {
					var site = site(ring.carousel());
					long captives = ring.carousel().getPassengers().stream().filter(BloodDrunkPuppeteerEntity.class::isInstance).count();
					PlaythroughLog.note(helper, "finaleDeathResets", "after the respawn: owner " + site.activeOwner() + ", phase "
							+ site.phase() + ", outcome " + site.outcome() + ", captives " + captives);
					helper.assertTrue(site.activeOwner() == null, "the death releases the site");
					helper.assertTrue(captives == 3, "the three captives ride the carousel again: " + captives);
					p.moveTo(ring.stand());
					DialogueView again = p.talkTo(rm);
					helper.assertTrue(again.offers(CircusRingmasterEntity.EVENT_FINALE), "the finale can be begun again: " + again.describe());
				})
				.run();
	}

	/** The pupil asks the Mummer professor for the practical with a newly attuned Crossbar in hand: two threats appear. */
	private static void beginMummer(PlaythroughPlayer p, Entity professor) {
		UUID crossbar = attunedCrossbar(p, 200); // ARRANGE
		holdCrossbar(p, crossbar);
		p.approach(professor);
		p.talkTo(professor);
		p.choose(professor, PRACTICAL).said(SCHOOL + "exercise." + MUMMER);
		p.helper().assertTrue(rehearsalTargets(p).size() == 2, "the Mummer practical brings two threats");
	}

	/** The owner asks the Ringmaster for the attention act on the ring. */
	private static void beginAct(PlaythroughPlayer p, Ring ring) {
		p.moveTo(ring.stand());
		p.talkTo(ring.ringmaster());
		p.choose(ring.ringmaster(), CircusRingmasterEntity.EVENT_TRIAL).said("hemomancy.circus.challenge.begin.0");
		p.helper().assertTrue(p.uuid().equals(site(ring.carousel()).activeOwner()), "the act holds the site for its owner");
	}

	/** The root key an act in progress keeps on its owner ({@code CircusPerformanceController}). */
	private static final String ACTIVE_CHALLENGE = "hemomancy.circus_active_challenge";

	// =============================================================================================================
	// Decision probes
	// =============================================================================================================

	/** CT-8: one rehearsal threat killed during the Mummer practical, which needs two. */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH, required = false)
	public static void practicalTargetKilledStalls(GameTestHelper helper) {
		Entity stilt = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.STILT_WALKER, helper.absolutePos(new BlockPos(5, Y, 12)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		p.joinLevel();
		armUp(p, true); // ARRANGE
		PlaythroughCleanup.onEnd(helper, () -> CircusPracticalController.cancel(p.player()));
		knowShape(p, MUMMER);
		UUID crossbar = attunedCrossbar(p, 200);
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.then(() -> {
					holdCrossbar(p, crossbar);
					p.approach(stilt);
					p.talkTo(stilt);
					p.choose(stilt, PRACTICAL).said(SCHOOL + "exercise." + MUMMER);
					List<Mob> targets = rehearsalTargets(p);
					helper.assertTrue(targets.size() == 2, "the Mummer practical brings two threats");
					mark[0] = p.mark();
					p.wield(Items.NETHERITE_SWORD);
					p.attack(targets.getFirst());
				})
				.everyTick(40, tick -> live(p))
				.then(() -> {
					List<Mob> left = rehearsalTargets(p);
					boolean told = p.chatSince(mark[0]).stream().anyMatch(line -> line.has(RETRY));
					PlaythroughLog.note(helper, "practicalTargetKilledStalls", left.size() + " threats left, retry said " + told
							+ "; " + p.chatSince(mark[0]));
					// DECISION CT-8: a killed rehearsal threat is discarded, and the Mummer (and Bellows) practical needs
					// two, so it cannot pass until its five-minute expiry or a walk-away. Options: replace the lost
					// threat, end the practical with the retry line, or accept.
					helper.assertTrue(left.size() >= 2 || told, "DECISION CT-8: one threat killed leaves the practical "
							+ "unpassable with no word: " + left.size() + " left");
				})
				.run();
	}

	/** CT-3: in Peaceful the rehearsal threats (Husks) are removed on their first tick. */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = CLOCK, required = false)
	public static void practicalsInPeaceful(GameTestHelper helper) {
		Entity acrobat = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.ACROBAT, helper.absolutePos(new BlockPos(5, Y, 12)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		p.joinLevel();
		PlaythroughCleanup.onEnd(helper, () -> CircusPracticalController.cancel(p.player()));
		knowShape(p, VULTURE);
		UUID crossbar = attunedCrossbar(p, 200);
		PlaythroughTime.difficulty(helper, Difficulty.PEACEFUL);
		holdCrossbar(p, crossbar);
		p.approach(acrobat);
		p.talkTo(acrobat);
		p.choose(acrobat, PRACTICAL).said(SCHOOL + "exercise." + VULTURE);
		PlaythroughClock.start(helper)
				.everyTick(5, tick -> live(p))
				.then(() -> {
					List<Mob> left = rehearsalTargets(p);
					PlaythroughLog.note(helper, "practicalsInPeaceful", left.size() + " threats left after 5 Peaceful ticks");
					// DECISION CT-3: rehearsal threats are Husks, which despawn in Peaceful, so no practical or guest act
					// can finish there. Options: non-monster targets, refuse with a line in Peaceful, or accept.
					helper.assertTrue(left.size() == 2, "DECISION CT-3: Peaceful removes the rehearsal threats: " + left.size() + " left");
				})
				.run();
	}

	/** CT-5: the Threadkeeper's lesson node (the full An Empty Ring briefing) has no option leading to it. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = BATCH, required = false)
	public static void threadkeeperLessonUnreachable(GameTestHelper helper) {
		Entity keeper = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.THREADKEEPER, helper.absolutePos(new BlockPos(6, Y, 12)));
		PlaythroughPlayer p = d5(helper, 8, 12);
		DialogueView view = p.talkTo(keeper);
		List<String> unreachable = view.unreachableNodes();
		PlaythroughLog.note(helper, "threadkeeperLessonUnreachable", "unreachable " + unreachable + ", lesson lines "
				+ (view.tree().getNode("lesson") == null ? "none" : view.tree().getNode("lesson").lines()));
		// DECISION CT-5: CircusSchoolDialogue builds a lesson node for every role, but only professors with a lesson get
		// the option to it, so the Threadkeeper's An Empty Ring briefing can never be read. Options: a Threadkeeper
		// option to it (Conversation), move the briefing into the inspect or report lines, or drop the node.
		helper.assertTrue(!unreachable.contains("lesson"), "DECISION CT-5: the Threadkeeper's lesson node is unreachable: " + unreachable);
		helper.succeed();
	}

	/**
	 * CT-7 (D-10 family): a Ringmaster lost mid-finale leaves the site owned and unsettled, so it never completes and
	 * a sneak-used waybill keeps treating it as unfinished. Also logs the CT-6 lines: "claimed" for a temporary owner
	 * and "not ready for Succession" for a neutral visitor.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 300, batch = RING, required = false)
	public static void ringmasterLostLeavesSiteOpen(GameTestHelper helper) {
		Ring ring = miniRing(helper);
		PlaythroughPlayer a = d5(helper, 16, 14);
		PlaythroughPlayer b = d5(helper, 17, 14);
		a.joinLevel();
		b.joinLevel();
		armUp(a, false); // ARRANGE
		PlaythroughStates.acclimated(a, 500); // ARRANGE
		PlaythroughStates.pavilionRoute(a, CircusRouteRules.Route.LIBERATION, 0b11); // ARRANGE
		PlaythroughStates.acclimated(b, 500); // ARRANGE
		CircusRingmasterEntity rm = ring.ringmaster();
		int[] mark = new int[1];
		PlaythroughClock.start(helper)
				.idle(2)
				.then(() -> {
					a.moveTo(ring.stand());
					b.moveTo(ring.stand().east());
					a.talkTo(rm);
					a.choose(rm, CircusRingmasterEntity.EVENT_FINALE);
					helper.assertTrue(a.uuid().equals(site(ring.carousel()).activeOwner()), "A holds the site in the finale");
					b.talkTo(rm);
					Outcome shut = b.choose(rm, CircusRingmasterEntity.EVENT_TRIAL);
					Outcome forged = b.forge(CircusRingmasterEntity.EVENT_FINALE, rm.getId());
					PlaythroughLog.note(helper, "ringmasterLostLeavesSiteOpen", "CT-6: B's act while A holds the site said "
							+ shut.saidKeys() + " (" + (shut.saidKeys().contains(CLAIMED) ? "claimed reads as permanent" : "no claimed line")
							+ "); B's forged neutral finale said " + forged.saidKeys() + " (" + (forged.saidKeys().contains(NOT_READY)
							? "names Succession to a neutral visitor" : "no not_ready line") + ")");
					mark[0] = a.mark();
					PlaythroughNpcs.kill(rm); // ARRANGE: the Ringmaster is lost (an admin kill)
				})
				.everyTick(100, tick -> {
					live(a);
					live(b);
				})
				.then(() -> {
					var site = site(ring.carousel());
					ServerLevel level = helper.getLevel();
					boolean settled = site.outcome() != CircusPavilionStateRules.Outcome.NEUTRAL;
					boolean returned = !level.getEntitiesOfClass(CircusRingmasterEntity.class,
							new AABB(ring.carousel().blockPosition()).inflate(20.0D), Entity::isAlive).isEmpty();
					boolean completedNear = CircusPavilionSavedData.get(level).isCompletedNear(level, ring.carousel().blockPosition());
					PlaythroughLog.note(helper, "ringmasterLostLeavesSiteOpen", "after the loss: owner " + site.activeOwner()
							+ ", phase " + site.phase() + ", outcome " + site.outcome() + ", a Ringmaster back " + returned
							+ ", waybill treats it as completed " + completedNear + ", A was told " + a.chatSince(mark[0]));
					// DECISION CT-7 (D-10 family): with the Ringmaster gone the site stays owned and never settles, the
					// finale can never end and the sneak waybill keeps pointing at it. Options: a respawn rule for the
					// Ringmaster, settle or release the site with a line when he is lost, skip such sites in the
					// waybill, or accept.
					helper.assertTrue(settled || returned, "DECISION CT-7: a lost Ringmaster leaves the site open for good");
				})
				.run();
	}

	// =============================================================================================================
	// Sweeps and late degrees
	// =============================================================================================================

	/**
	 * CT-19 and CT-5: the eight performers and the Ringmaster read in English with no dead ends and a Trade box; a
	 * Threadkeeper trade pays; the Ringmaster is silent and does not deal below 500; at 500 his spectacle card is
	 * locked until an act is banked, then spends it once; his bell is locked below D4; a performer under threat
	 * neither talks nor deals.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = BATCH + "_sweep")
	public static void circusSweepAndTrades(GameTestHelper helper) {
		Map<PlaythroughNpcs.CircusRole, Entity> troupe = lineUp(helper, 4);
		CircusRingmasterEntity rm = (CircusRingmasterEntity) PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.RINGMASTER,
				helper.absolutePos(new BlockPos(16, Y, 20)));
		PlaythroughPlayer p = d5(helper, 14, 10);
		List<String> findings = new ArrayList<>();
		for (var entry : troupe.entrySet()) {
			p.approach(entry.getValue());
			DialogueView view = p.talkTo(entry.getValue());
			String who = entry.getKey().toString();
			if (!view.missingLangKeys().isEmpty()) findings.add(who + " missing " + view.missingLangKeys());
			if (!view.deadEnds().isEmpty()) findings.add(who + " dead ends " + view.deadEnds());
			if (!view.emptiedNodes().isEmpty()) findings.add(who + " emptied " + view.emptiedNodes());
			if (!view.tradeBoard().present() || view.tradeCards().isEmpty()) findings.add(who + " has no Trade box");
			PlaythroughLog.note(helper, "circusSweepAndTrades", who + ": " + view.describe() + "; unreachable " + view.unreachableNodes());
		}
		Entity keeper = troupe.get(PlaythroughNpcs.CircusRole.THREADKEEPER);
		p.approach(keeper);
		int paper = putInFreeSlot(p, new ItemStack(Items.PAPER, 6)); // ARRANGE: the goods the card asks for
		NpcTradeResultPacket bought = p.trade(keeper, LEDGER_PAPER, paper);
		helper.assertTrue(bought != null && bought.outcome() != NpcTradeService.Outcome.REFUSED,
				"the Threadkeeper trades paper for Filaments; " + p.last());
		p.last().gave(ItemInit.puppeteering_thread.get(), 2);

		p.approach(rm);
		int mark = p.mark();
		helper.assertTrue(p.tryTalkTo(rm).isEmpty() && p.chatSince(mark).stream().anyMatch(line -> line.has(SILENT)),
				"below 500 the Ringmaster is silent");
		NpcTradeResultPacket early = p.trade(rm, SPECTACLE);
		helper.assertTrue(early == null || early.outcome() == NpcTradeService.Outcome.REFUSED, "below 500 he does not deal");
		PlaythroughStates.acclimated(p, 500); // ARRANGE
		DialogueView view = p.talkTo(rm);
		if (!view.missingLangKeys().isEmpty()) findings.add("Ringmaster missing " + view.missingLangKeys());
		if (!view.deadEnds().isEmpty()) findings.add("Ringmaster dead ends " + view.deadEnds());
		helper.assertTrue(view.tradeCard(SPECTACLE).map(card -> card.state() == NpcTradeCard.State.LOCKED).orElse(false),
				"the spectacle card is locked with no act banked; " + view.tradeCards());
		helper.assertTrue(view.tradeCard(BELL).map(card -> card.state() != NpcTradeCard.State.LOCKED).orElse(false),
				"at D5 the bell card is open");
		NpcTradeActs.witnessed(p.player(), NpcTradeWant.Act.SPECTACLE); // ARRANGE: one act banked (attentionActAndRoute banks a real one)
		NpcTradeResultPacket spent = p.trade(rm, SPECTACLE);
		helper.assertTrue(spent != null && spent.outcome() != NpcTradeService.Outcome.REFUSED
						&& HemoCapabilityAccess.npcTrades(p.player()).acts(NpcTradeWant.Act.SPECTACLE) == 0,
				"a banked act is spent on the spectacle; " + p.last());
		NpcTradeResultPacket again = p.trade(rm, SPECTACLE);
		helper.assertTrue(again == null || again.outcome() == NpcTradeService.Outcome.REFUSED, "the act is spent once; " + p.last());

		PlaythroughPlayer adept = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 18, 18));
		PlaythroughStates.initiateD3(adept); // ARRANGE
		PlaythroughStates.acclimated(adept, 500); // ARRANGE
		adept.approach(rm);
		DialogueView d3 = adept.talkTo(rm);
		helper.assertTrue(d3.tradeCard(BELL).map(card -> card.state() == NpcTradeCard.State.LOCKED && card.lockKey().isPresent())
				.orElse(false), "below D4 the bell card shows locked with a reason; " + d3.tradeCards());

		Entity acrobat = troupe.get(PlaythroughNpcs.CircusRole.ACROBAT);
		armUp(p, false); // ARRANGE
		p.approach(acrobat);
		p.attack(acrobat);
		NpcTradeResultPacket threatened = p.trade(acrobat, RIGGING_LINE);
		helper.assertTrue(threatened == null || threatened.outcome() == NpcTradeService.Outcome.REFUSED,
				"a performer under threat does not deal; " + p.last());
		helper.assertTrue(p.tryTalkTo(acrobat).isEmpty(), "a performer under threat does not talk");
		helper.assertTrue(findings.isEmpty(), "Circus speakers: " + findings);
		helper.succeed();
	}

	/**
	 * CT-20: at D7 with the Pome choice unresolved the Understudy still teaches and talks, but the ordeal is refused
	 * with the void line.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 3000, batch = RITES)
	public static void ninePomesBlockOrdeal(GameTestHelper helper) {
		Entity understudy = PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.UNDERSTUDY, helper.absolutePos(new BlockPos(5, Y, 12)));
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 4));
		PlaythroughStates.choicePendingD7(p); // ARRANGE: nine pomes, revelation witnessed, no path chosen
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE
		p.give(new ItemStack(ItemInit.living_staff.get())); // ARRANGE
		p.give(new ItemStack(ItemInit.blood_projection.get())); // ARRANGE
		p.approach(understudy);
		DialogueView view = p.talkTo(understudy);
		view.assertLangKeys(helper, "the Understudy at D7").assertNoDeadEnds(helper, "the Understudy at D7");
		helper.assertTrue(view.offers(TEACH), "the Understudy still teaches at D7; " + view.describe());
		p.choose(understudy, TEACH);
		helper.assertTrue(CircusApprenticeshipProgress.instructionSatisfied(p.player(), MNEMONIST), "the D7 lesson is recorded");
		attunedCrossbar(p, 200);
		PlaythroughSites.RiteFloor floor = PlaythroughSites.riteFloor(p, helper.absolutePos(RITE_ORIGIN), TRIAL_RECIPE);
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, p, floor);
		clock.then(() -> {
			Outcome refused = plant(p, floor);
			PlaythroughLog.note(helper, "ninePomesBlockOrdeal", "refusal said " + refused.saidKeys());
			// The activation's path gate answers first ("Resolve the ninth husk's choice first"); the trial controller's
			// void line is the same refusal one step later. Either tells the player why.
			helper.assertTrue(refused.saidKeys().contains(VOID) || refused.saidKeys().contains(PATH_GATE),
					"the ordeal is refused with a reason; " + refused);
			helper.assertTrue(!riteRunning(p), "the ordeal does not start while the Pome choice is unresolved; " + refused);
		}).run();
	}

	// =============================================================================================================
	// Helpers
	// =============================================================================================================

	/** A fresh Degree 5 Harbinger (the first suite's end state) with 5000 mL. */
	private static PlaythroughPlayer d5(GameTestHelper helper, int x, int z) {
		PlaythroughPlayer p = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, x, z));
		PlaythroughStates.illuminatusD5(p); // ARRANGE
		PlaythroughStates.blood(p, 5000.0D); // ARRANGE
		return p;
	}

	/**
	 * One server tick of the player as the server runs it for a player in the level: the tick counter (discovery and
	 * acclimation read it), item cooldowns (the Crossbar's call cooldown), then the player tick events. The harness
	 * player is never ticked by the level, so a test that waits on the player's own tick calls this each tick.
	 */
	private static void live(PlaythroughPlayer p) {
		ServerPlayer player = p.player();
		player.tickCount++;
		player.getCooldowns().tick();
		p.tickOnce();
	}

	/** ARRANGE: Resistance V (the troupe, dolls and husks cannot hurt the player), Strength when asked, a sword. */
	private static void armUp(PlaythroughPlayer p, boolean strength) {
		p.player().addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200000, 4, false, false));
		if (strength) p.player().addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200000, 29, false, false));
		p.give(new ItemStack(Items.NETHERITE_SWORD));
	}

	/** ARRANGE: {@code summon} known, as its Cardinal Ordeal binds it (understudyMnemonistPuppet plays one). */
	private static void knowShape(PlaythroughPlayer p, String summon) {
		var definition = PuppeteerSummonDefinitions.byName(summon).orElseThrow();
		KnownSummonEvents.grantSummon(p.player(), definition);
		p.helper().assertTrue(CircusApprenticeshipProgress.knows(p.player(), summon), summon + " is known");
	}

	private static boolean unbound(ItemStack stack) {
		return stack.is(ItemInit.marionette_crossbar.get()) && MarionetteCrossbarItem.getBoundOwner(stack) == null;
	}

	/**
	 * A Marionette Crossbar (ARRANGE: crafted) attuned by the player's own first use, with {@code thread} charge put on
	 * it (ARRANGE: charging is crossbarAttuneChargePrepare's). It stays in the main hand; returns its id.
	 */
	private static UUID attunedCrossbar(PlaythroughPlayer p, int thread) {
		p.give(new ItemStack(ItemInit.marionette_crossbar.get()));
		PlaythroughRites.wieldStack(p, PlaythroughCrimsonTroupeGameTests::unbound, "an unbound Crossbar");
		Outcome bound = p.use(InteractionHand.MAIN_HAND);
		ItemStack stack = p.player().getMainHandItem();
		p.helper().assertTrue(MarionetteCrossbarItem.isBoundTo(stack, p.player()), "the first use attunes the Crossbar; " + bound);
		if (thread > 0) MarionetteCrossbarItem.addThread(stack, thread);
		return MarionetteCrossbarItem.ensureCrossbarId(stack);
	}

	private static ItemStack holdCrossbar(PlaythroughPlayer p, UUID id) {
		return PlaythroughRites.wieldStack(p, stack -> stack.getItem() instanceof MarionetteCrossbarItem
				&& id.equals(MarionetteCrossbarItem.getCrossbarId(stack)), "the Crossbar " + id);
	}

	/** The player moves the Crossbar to the off hand (the swap-hands key). */
	private static void crossbarToOffhand(PlaythroughPlayer p, UUID id) {
		ItemStack held = holdCrossbar(p, id);
		ItemStack offhand = p.player().getOffhandItem();
		p.player().setItemInHand(InteractionHand.OFF_HAND, held);
		p.player().setItemInHand(InteractionHand.MAIN_HAND, offhand);
	}

	/** The player puts the off-hand Crossbar back in the pack. */
	private static void crossbarFromOffhand(PlaythroughPlayer p) {
		ItemStack offhand = p.player().getOffhandItem();
		p.player().setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
		if (!offhand.isEmpty()) putInFreeSlot(p, offhand);
	}

	/** Takes the Crossbar back out of the Focus with an empty, sneaking right-click. */
	private static void takeCrossbarBack(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor, UUID crossbar) {
		p.moveTo(PlaythroughRites.riteStand(floor));
		PlaythroughRites.emptyHand(p);
		Outcome taken = p.sneakUseOn(floor.focus());
		p.helper().assertTrue(MarionetteCrossbarItem.findCrossbar(p.player(), crossbar).isPresent(),
				"the Crossbar comes back out of the Focus; " + taken);
	}

	/** Plants the Living Staff on the floor's Focus from the rite stand. */
	private static Outcome plant(PlaythroughPlayer p, PlaythroughSites.RiteFloor floor) {
		p.moveTo(PlaythroughRites.riteStand(floor));
		if (p.count(ItemInit.living_staff.get()) == 0) {
			p.give(new ItemStack(ItemInit.living_staff.get())); // ARRANGE: a Staff in hand again
			PlaythroughLog.note(p.helper(), "the rite kept the Living Staff; a new one supplied");
		}
		p.wield(ItemInit.living_staff.get());
		return p.useOn(floor.focus());
	}

	private static boolean riteRunning(PlaythroughPlayer p) {
		return CardinalRiteSavedData.get(p.player().serverLevel()).getRite(p.uuid()) != null;
	}

	/** The player's live rehearsal threats. */
	private static List<Mob> rehearsalTargets(PlaythroughPlayer p) {
		return p.player().serverLevel().getEntitiesOfClass(Mob.class, p.player().getBoundingBox().inflate(32.0D),
				mob -> mob.isAlive() && mob.getPersistentData().hasUUID(CircusPracticalController.EXAM)
						&& p.uuid().equals(mob.getPersistentData().getUUID(CircusPracticalController.EXAM)));
	}

	/**
	 * The Mnemonist Puppet practical as a player plays it: each server tick the player lives;every 30 ticks they strike
	 * the threat their puppet is fighting (or the nearest one) with the sword, and the puppet replays that remembered
	 * wound, until the professor's passed line.
	 */
	private static PlaythroughClock.Step replayPractical(PlaythroughPlayer p, int mark, int maxTicks) {
		return ticks -> {
			live(p);
			if (p.chatSince(mark).stream().anyMatch(line -> line.has(PASSED))) return true;
			Mob puppet = MarionetteCrossbarItem.activeSummonsForOwner(p.player()).stream()
					.filter(MnemonistPuppetEntity.class::isInstance).findFirst().orElse(null);
			if (ticks >= maxTicks) {
				p.helper().fail("the practical is not passed after " + maxTicks + " ticks; puppet " + (puppet == null ? "gone"
						: "targeting " + puppet.getTarget() + ", memories " + memoriesOf(puppet)) + "; " + p.chatSince(mark));
			}
			if (ticks % 30 == 0) {
				List<Mob> targets = rehearsalTargets(p);
				Mob target = puppet != null && puppet.getTarget() instanceof Mob fought && targets.contains(fought) ? fought
						: targets.stream().min(Comparator.comparingDouble(mob -> mob.distanceToSqr(p.player()))).orElse(null);
				if (target != null) p.attack(target);
			}
			return false;
		};
	}

	/**
	 * A Ringmaster act held to its end: each tick the player lives (looking at {@code look} when given) until
	 * {@code doneKey} is said. Fails after {@code maxTicks}.
	 */
	private static PlaythroughClock.Step act(PlaythroughPlayer p, String doneKey, int maxTicks, @Nullable Supplier<Vec3> look) {
		int mark = p.mark();
		return ticks -> {
			if (look != null) {
				Vec3 at = look.get();
				if (at != null) p.aim(at);
			}
			live(p);
			if (p.chatSince(mark).stream().anyMatch(line -> line.has(doneKey))) return true;
			if (ticks >= maxTicks) p.helper().fail("the act did not end with " + doneKey + " in " + maxTicks + " ticks: " + p.chatSince(mark));
			return false;
		};
	}

	/**
	 * A finale fought to its settlement: each tick the player lives; on Succession every 12 ticks they strike each
	 * standing finale performer by the ring (non-lethal: they go down); in the descent they strike the Ringmaster.
	 */
	private static PlaythroughClock.Step finale(PlaythroughPlayer p, Pavilion pv, boolean succession, int maxTicks) {
		return ticks -> {
			live(p);
			var site = site(pv.carousel());
			if (site.outcome() != CircusPavilionStateRules.Outcome.NEUTRAL) return true;
			if (ticks >= maxTicks) p.helper().fail("the finale did not settle in " + maxTicks + " ticks; phase " + site.phase());
			if (ticks % 12 == 0 && succession) {
				for (CircusPerformerEntity performer : p.player().serverLevel().getEntitiesOfClass(CircusPerformerEntity.class,
						new AABB(pv.carousel().encounterOrigin()).inflate(20.0D),
						performer -> performer.isAlive() && performer.participatesInFinale() && !performer.isDowned())) {
					p.attack(performer);
				}
			}
			if (ticks % 12 == 6 && site.phase() == CircusPavilionStateRules.Phase.DESCENT && pv.ringmaster().isAlive()
					&& pv.ringmaster().isAttackable()) {
				p.attack(pv.ringmaster());
			}
			return false;
		};
	}

	/** Diagnostics: the puppet's remembered wounds (attacker, damage, age). */
	private static String memoriesOf(@Nullable Mob puppet) {
		if (puppet == null) return "-";
		try {
			Field field = MnemonistPuppetEntity.class.getDeclaredField("memories");
			field.setAccessible(true);
			List<?> memories = (List<?>) field.get(puppet);
			return memories.toString();
		} catch (ReflectiveOperationException exception) {
			return "unreadable";
		}
	}

	/** The trained peacock spider of the player's attention act. */
	private static Optional<PeacockSpiderEntity> spider(PlaythroughPlayer p) {
		return p.player().serverLevel().getEntitiesOfClass(PeacockSpiderEntity.class, p.player().getBoundingBox().inflate(24.0D),
				spider -> spider.isAlive() && spider.getPersistentData().hasUUID("CircusTrialOwner")
						&& p.uuid().equals(spider.getPersistentData().getUUID("CircusTrialOwner"))).stream().findFirst();
	}

	/** Hears a performer's testimony through their dialogue. */
	private static Outcome testimony(PlaythroughPlayer p, Entity speaker) {
		p.approach(speaker);
		p.talkTo(speaker);
		return p.choose(speaker, TESTIMONY);
	}

	/** Reads an evidence record with an empty hand and checks its clue line. */
	private static void clue(PlaythroughPlayer p, BlockPos record, String clue) {
		p.moveTo(record.north(2)); // ARRANGE: walked up to it
		PlaythroughRites.emptyHand(p);
		Outcome read = p.useOn(record);
		read.said(SCHOOL + "clue." + clue);
		p.helper().assertTrue(CircusApprenticeshipProgress.state(p.player()).getBoolean("clue." + clue),
				"the " + clue + " record is noted; " + read);
	}

	/** The pavilion site saved for {@code carousel}'s ring. */
	private static CircusPavilionSavedData.Site site(CircusCarouselEntity carousel) {
		ServerLevel level = (ServerLevel) carousel.level();
		return CircusPavilionSavedData.get(level).site(level, carousel.encounterOrigin());
	}

	/** The last perception sync the player was sent. */
	private static Optional<PacketSyncCircusPerception> lastPerception(PlaythroughPlayer p) {
		List<PacketSyncCircusPerception> sent = p.sent(PacketSyncCircusPerception.class);
		return sent.isEmpty() ? Optional.empty() : Optional.of(sent.getLast());
	}

	/** How many lines said to the player carried {@code key}. */
	private static long lines(PlaythroughPlayer p, String key) {
		return p.chat().stream().filter(line -> line.has(key)).count();
	}

	/** Whether the action said a Circus system line ({@code message.hemomancy.circus.*}) that has English. */
	private static boolean saysSystemLine(Outcome outcome) {
		return outcome.saidKeys().stream().anyMatch(key -> key.startsWith(SYSTEM_LINE) && LangKeys.exists(key));
	}

	/** Every line key of every node the tree carries (reachable or not). */
	private static List<String> allLineKeys(DialogueView view) {
		List<String> keys = new ArrayList<>();
		for (DialogueNode node : view.tree().nodes().values()) {
			for (String line : node.lines()) Dialogue.lineKey(line).ifPresent(keys::add);
		}
		return keys;
	}

	private static List<String> nodeLines(DialogueView view, String nodeId) {
		DialogueNode node = view.tree().getNode(nodeId);
		List<String> keys = new ArrayList<>();
		if (node != null) for (String line : node.lines()) Dialogue.lineKey(line).ifPresent(keys::add);
		return keys;
	}

	/** ARRANGE: takes every stack matching {@code what} out of the player's pack (it was lost). */
	private static void removeAll(PlaythroughPlayer p, Predicate<ItemStack> what) {
		var inventory = p.player().getInventory();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			if (what.test(inventory.getItem(slot))) inventory.setItem(slot, ItemStack.EMPTY);
		}
	}

	private static int putInFreeSlot(PlaythroughPlayer p, ItemStack stack) {
		var inventory = p.player().getInventory();
		int free = inventory.getFreeSlot();
		p.helper().assertTrue(free >= 0, "the player has a free slot");
		inventory.items.set(free, stack);
		return free;
	}

	private static int menuSlotOf(AbstractContainerMenu menu, PlaythroughPlayer p, Predicate<ItemStack> what) {
		for (int index = 0; index < menu.slots.size(); index++) {
			Slot slot = menu.slots.get(index);
			if (slot.container == p.player().getInventory() && !slot.getItem().isEmpty() && what.test(slot.getItem())) return index;
		}
		return -1;
	}

	/** A full-strength swing at {@code target} aimed at {@code point} (the harness's attack aims at the centre). */
	private static Outcome strikeAt(PlaythroughPlayer p, Entity target, Vec3 point) {
		return p.attack(target, point);
	}

	private static boolean structureTagHas(GameTestHelper helper, String tag, ResourceLocation structure) {
		TagKey<Structure> key = TagKey.create(Registries.STRUCTURE, Hemomancy.rloc(tag));
		return helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE).getTag(key)
				.map(set -> set.stream().anyMatch(holder -> holder.is(structure))).orElse(false);
	}

	/** The eight performer roles in a row at relative z {@code z}, three blocks apart, as their camps place them. */
	private static Map<PlaythroughNpcs.CircusRole, Entity> lineUp(GameTestHelper helper, int z) {
		Map<PlaythroughNpcs.CircusRole, Entity> troupe = new LinkedHashMap<>();
		int x = 3;
		for (PlaythroughNpcs.CircusRole role : PlaythroughNpcs.CircusRole.performers()) {
			troupe.put(role, PlaythroughNpcs.circus(helper, role, helper.absolutePos(new BlockPos(x, Y, z))));
			x += 3;
		}
		return troupe;
	}

	// --- Sites -------------------------------------------------------------------------------------------------------

	/**
	 * The real pavilion. Its actors are looked up on first use: the template's own entities only show to entity lookups
	 * once their chunks are entity-ticking, so a test waits for {@link #loaded} before it asks for one (an actor looked
	 * up earlier would be placed again on its mark and later meet the template's copy).
	 */
	private static final class Pavilion {
		private final GameTestHelper helper;
		private final PlaythroughSites.TemplateSite site;
		private final Map<PlaythroughNpcs.CircusRole, Entity> actors = new java.util.EnumMap<>(PlaythroughNpcs.CircusRole.class);

		Pavilion(GameTestHelper helper, PlaythroughSites.TemplateSite site) {
			this.helper = helper;
			this.site = site;
		}

		PlaythroughSites.TemplateSite site() {
			return site;
		}

		BlockPos at(int x, int y, int z) {
			return site.at(x, y, z);
		}

		/** Whether every chunk under the pavilion is entity-ticking, so the template's actors can be found. */
		boolean loaded() {
			return site.ready(helper.getLevel());
		}

		/** Every actor on its mark: the template's, or a new one where the template brought none. */
		void cast() {
			for (PlaythroughNpcs.CircusRole role : PlaythroughNpcs.CircusRole.values()) actor(role);
		}

		Entity actor(PlaythroughNpcs.CircusRole role) {
			return actors.computeIfAbsent(role, wanted -> troupe(helper, site, wanted));
		}

		CircusRingmasterEntity ringmaster() {
			return (CircusRingmasterEntity) actor(PlaythroughNpcs.CircusRole.RINGMASTER);
		}

		CircusCarouselEntity carousel() {
			return (CircusCarouselEntity) actor(PlaythroughNpcs.CircusRole.CAROUSEL);
		}

		/** The floor cell under the Ringmaster's perch: within 8 blocks of him and 5 of the carousel. */
		BlockPos underPerch() {
			return site.at(32, 1, 37);
		}
	}

	/**
	 * ARRANGE: the real {@code crimson_troupe_pavilion} beside the test ({@code PlaythroughSites.pavilion}); its actors
	 * are the ones the template brought, or new ones on their template marks (see {@link Pavilion}).
	 */
	private static Pavilion pavilion(GameTestHelper helper) {
		return new Pavilion(helper, PlaythroughSites.pavilion(helper, PlaythroughSites.farOrigin(helper, 64)));
	}

	/** The pavilion's {@code role}: the one the template placed, or a new one on its mark. */
	private static Entity troupe(GameTestHelper helper, PlaythroughSites.TemplateSite site, PlaythroughNpcs.CircusRole role) {
		List<Entity> found = helper.getLevel().getEntitiesOfClass(Entity.class, AABB.of(site.box()).inflate(2.0D),
				entity -> entity.isAlive() && entity.getType() == role.type());
		if (!found.isEmpty()) return found.getFirst();
		BlockPos mark = PAVILION_MARKS.get(role);
		PlaythroughLog.note(helper, "the pavilion template did not bring its " + role + "; placed on its mark " + mark);
		return PlaythroughNpcs.circus(helper, role, site.at(mark.getX(), mark.getY(), mark.getZ()));
	}

	/** A carousel and the Ringmaster on the empty template, and where a player stands to deal with both. */
	private record Ring(CircusRingmasterEntity ringmaster, CircusCarouselEntity carousel, BlockPos stand) {
	}

	/**
	 * ARRANGE: a ring on the empty template for tests about the Ringmaster's rules: the carousel (its three captives
	 * mount on its first tick) and the Ringmaster six blocks south on the floor. Its saved site is removed at the end.
	 */
	private static Ring miniRing(GameTestHelper helper) {
		CircusCarouselEntity carousel = (CircusCarouselEntity) PlaythroughNpcs.circus(helper, PlaythroughNpcs.CircusRole.CAROUSEL,
				helper.absolutePos(new BlockPos(16, Y, 10)));
		CircusRingmasterEntity ringmaster = (CircusRingmasterEntity) PlaythroughNpcs.circus(helper,
				PlaythroughNpcs.CircusRole.RINGMASTER, helper.absolutePos(new BlockPos(16, Y, 16)));
		ServerLevel level = helper.getLevel();
		BlockPos origin = carousel.blockPosition();
		PlaythroughCleanup.onEnd(helper, () -> CircusPavilionSavedData.get(level).removeSite(level, origin));
		return new Ring(ringmaster, carousel, helper.absolutePos(new BlockPos(16, Y, 14)));
	}
}
