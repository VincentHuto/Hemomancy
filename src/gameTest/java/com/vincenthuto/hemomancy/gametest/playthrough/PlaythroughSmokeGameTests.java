package com.vincenthuto.hemomancy.gametest.playthrough;

import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueScreenMode;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVicarEntity;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.mission.vicar.EarlyInitiation;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The harness on its own: a fresh player talks to a Vicar and sees a hub; a charm-bearer's initiation ticks through
 * the clock; a death and a relog keep the player's identity. The band classes assume all three work.
 */
@GameTestHolder("playthrough_validation")
@PrefixGameTestTemplate(false)
public final class PlaythroughSmokeGameTests {
	private static final String EMPTY = "empty";
	private static final BlockPos NPC = new BlockPos(8, PlaythroughSites.STAND_Y, 8);
	private static final BlockPos STAND = new BlockPos(10, PlaythroughSites.STAND_Y, 8);

	private PlaythroughSmokeGameTests() {
	}

	/** A fresh player right-clicks a Vicar: a hub opens, every key in it resolves, and nothing in it is a dead end. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = "playthrough_smoke")
	public static void freshPlayerTalksToAVicar(GameTestHelper helper) {
		List<String> column = new ArrayList<>();
		for (int y = 0; y <= 5; y++) column.add(y + "=" + helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(5, y, 5))).getBlock());
		PlaythroughLog.note(helper, "smoke", "template column at (5, y, 5): " + column);
		helper.assertTrue(!helper.getLevel().getBlockState(PlaythroughSites.stand(helper, 5, 5).below()).isAir()
				&& helper.getLevel().getBlockState(PlaythroughSites.stand(helper, 5, 5)).isAir(),
				"the empty template's floor is under STAND_Y: " + column);
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, helper.absolutePos(NPC));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, helper.absolutePos(STAND));
		PlaythroughStates.freshD0(player);
		DialogueView view = player.talkTo(vicar);
		helper.assertTrue(view.tree().presentation().mode() == DialogueScreenMode.TOPIC_HUB, "the Vicar opens a hub");
		helper.assertTrue(view.entityId() == vicar.getId(), "the hub names the Vicar");
		view.assertLangKeys(helper, "the Vicar at D0").assertNoDeadEnds(helper, "the Vicar at D0");
		List<String> offered = player.offeredEvents(vicar);
		PlaythroughLog.note(helper, "smoke", "fresh D0 Vicar: " + view.describe());
		helper.assertFalse(offered.contains("vicar_begin_initiation"), "no initiation without the charm: " + offered);
		helper.succeed();
	}

	/**
	 * A charm-bearer chooses the Vicar's initiation and stands through it: the clock ticks the player each tick and
	 * the ceremony ends at degree 1.
	 */
	@GameTest(template = EMPTY, timeoutTicks = EarlyInitiation.DURATION + 80, batch = "playthrough_smoke")
	public static void clockCarriesACeremonyToItsEnd(GameTestHelper helper) {
		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, helper.absolutePos(NPC));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, helper.absolutePos(STAND));
		PlaythroughStates.charmBearerD0(player, (net.minecraft.world.entity.Entity) null);
		PlaythroughClock.start(helper)
				.then(() -> {
					DialogueView view = player.talkTo(vicar);
					helper.assertTrue(view.offers("vicar_begin_initiation"), "a charm-bearer is offered initiation: " + view);
					Outcome begun = player.choose(vicar, "vicar_begin_initiation");
					helper.assertTrue(begun.rewardDelivered(), "the ceremony begins; " + begun);
				})
				.step(player.tickPlayer(EarlyInitiation.DURATION + 2))
				.then(() -> {
					Outcome ceremony = player.last();
					ceremony.degreeIs(1).earned(degreeOne());
					helper.assertTrue(player.degree() == 1, "the ceremony ends at degree 1; " + ceremony);
				})
				.run();
	}

	private static String degreeOne() {
		return HarbingerAdvancementGranter.ADV_DEGREE_1_NEOPHYTE.getPath();
	}

	/**
	 * The temple site and the block and channel actions: the Hermit's blessing, an empty-hand touch on the Display
	 * ({@code useOn}), then a starter flask drunk by holding right-click ({@code hold}) until it finishes.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "playthrough_smoke")
	public static void templeTouchAndHeldFlaskGoThroughRealActions(GameTestHelper helper) {
		PlaythroughSites.BloodTemple temple = PlaythroughSites.bloodTemple(helper, PlaythroughSites.stand(helper, 12, 12));
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, temple.stand());
		PlaythroughClock.start(helper)
				.then(() -> {
					DialogueView hermit = player.talkTo(temple.hermit());
					helper.assertTrue(hermit.offers("hermit_heart_offered"), "the Hermit offers his heart: " + hermit);
					player.choose(temple.hermit(), "hermit_heart_offered");
					Outcome touch = player.useOn(temple.display());
					helper.assertTrue(player.hasCharm() && player.bloodActive(), "the touch fits the charm; " + touch);
					PlaythroughStates.blood(player, 0.0D);
					player.give(new net.minecraft.world.item.ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.bloody_flask.get()));
				})
				.step(() -> player.hold(com.vincenthuto.hemomancy.common.init.ItemInit.bloody_flask.get(), 120,
						player.player().getEyePosition().add(player.player().getLookAngle())))
				.then(() -> {
					Outcome drink = player.last();
					PlaythroughLog.note(helper, "smoke", "flask hold: " + drink);
					helper.assertTrue(drink.bloodAfter() > drink.bloodBefore(), "holding the flask drinks it; " + drink);
				})
				.run();
	}

	/** A death and respawn, then a relog, keep the player's UUID, degree and advancements. */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = "playthrough_smoke")
	public static void deathAndRelogKeepTheSamePlayer(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, helper.absolutePos(STAND));
		PlaythroughStates.neophyteD1(player);
		UUID id = player.uuid();
		Outcome death = player.die();
		helper.assertTrue(player.player().isDeadOrDying(), "real damage kills the player; " + death);
		player.respawn();
		helper.assertTrue(player.player().getUUID().equals(id) && player.player().isAlive(), "the respawned player is the same one");
		helper.assertTrue(player.degree() == 1 && player.has(degreeOne()), "death keeps the degree and its advancement");
		player.relog();
		helper.assertTrue(player.player().getUUID().equals(id) && player.degree() == 1 && player.hasCharm(),
				"a relog keeps the degree and the charm");
		helper.succeed();
	}

	// =============================================================================================================
	// The late-game harness (D5 onward), from the fresh-D5 checkpoint
	// =============================================================================================================

	/**
	 * Every late checkpoint, one on the next, holds what it says ({@code assertAt}) on both paths: line founded, the
	 * covenant, D6, the Living Covenant, D7, Communion (the Spine by a real delivery tick), the choice pending, then
	 * Apotheos for one player and Silentium for another. The ledger's Blood Beneath bar reads 13/13 at D8.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = "playthrough_smoke")
	public static void lateCheckpointsHoldOnBothPaths(GameTestHelper helper) {
		PlaythroughPlayer apotheos = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 4, 4));
		PlaythroughStates.illuminatusD5(apotheos);
		PlaythroughStates.foundedLineD5(apotheos);
		PlaythroughStates.assertAt(helper, apotheos, PlaythroughStates.Checkpoint.FOUNDED_LINE_D5);
		helper.assertTrue(apotheos.count(com.vincenthuto.hemomancy.common.init.ItemInit.unsigned_ancestral_ledger.get()) == 1,
				"the founder carries the signed ledger");
		PlaythroughStates.covenantD5(apotheos, PlaythroughStates.defaultHeart(apotheos));
		PlaythroughStates.assertAt(helper, apotheos, PlaythroughStates.Checkpoint.COVENANT_D5);
		PlaythroughStates.sanctifiedD6(apotheos);
		PlaythroughStates.assertAt(helper, apotheos, PlaythroughStates.Checkpoint.SANCTIFIED_D6);
		PlaythroughStates.livingCovenantD6(apotheos);
		PlaythroughStates.assertAt(helper, apotheos, PlaythroughStates.Checkpoint.LIVING_COVENANT_D6);
		PlaythroughStates.archonD7(apotheos);
		PlaythroughStates.assertAt(helper, apotheos, PlaythroughStates.Checkpoint.ARCHON_D7);
		PlaythroughStates.communedD7(apotheos);
		PlaythroughStates.assertAt(helper, apotheos, PlaythroughStates.Checkpoint.COMMUNED_D7);
		PlaythroughStates.choicePendingD7(apotheos);
		PlaythroughStates.assertAt(helper, apotheos, PlaythroughStates.Checkpoint.CHOICE_PENDING_D7);
		LedgerView pending = apotheos.ledger();
		PlaythroughLog.note(helper, "smoke", "ledger at the choice: " + pending);
		helper.assertTrue(pending.bloodBeneathProgress() == 10, "nine pomes and Communion read 10/13: " + pending);
		PlaythroughStates.apotheosPendingD7(apotheos);
		PlaythroughStates.assertAt(helper, apotheos, PlaythroughStates.Checkpoint.APOTHEOS_PENDING_D7);
		PlaythroughStates.apotheosD8(apotheos);
		PlaythroughStates.assertAt(helper, apotheos, PlaythroughStates.Checkpoint.APOTHEOS_D8);
		LedgerView done = apotheos.ledger();
		helper.assertTrue(done.bloodBeneathProgress() == done.bloodBeneathTotal() && done.bloodBeneathComplete(),
				"Apotheos reads 13/13: " + done);

		PlaythroughPlayer silence = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 4));
		PlaythroughStates.illuminatusD5(silence);
		PlaythroughStates.vesperBeatenD7(silence);
		PlaythroughStates.assertAt(helper, silence, PlaythroughStates.Checkpoint.VESPER_BEATEN_D7);
		helper.assertTrue(!silence.owed(com.vincenthuto.hemomancy.common.encounter.VesperOrdealManager.MEMORY).isEmpty(),
				"Vesper's Memory is owed after the victory");
		PlaythroughStates.silentiumD8(silence);
		PlaythroughStates.assertAt(helper, silence, PlaythroughStates.Checkpoint.SILENTIUM_D8);
		helper.succeed();
	}

	/**
	 * The new player entry points: a Monolith spoken to as a block ({@code talkToBlock}, entity id -1), a Vicar
	 * right-clicked through {@code interactOn} (the EntityInteract event path), milk drunk with {@code consume}, a
	 * second player found by level lookups only after {@code joinLevel}, then kicked from the line ({@code kick}), and
	 * a relog that drops the join.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100, batch = "playthrough_smoke")
	public static void lateEntryPointsGoThroughRealHandlers(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		PlaythroughPlayer leader = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 6, 6));
		PlaythroughStates.archonD7(leader);
		BlockPos monolith = PlaythroughSites.monolith(leader, PlaythroughSites.stand(helper, 14, 2)); // clear of the Fane heart at home + 4 east
		DialogueView stone = leader.talkToBlock(monolith);
		PlaythroughLog.note(helper, "smoke", "D7 Monolith: " + stone.describe() + " missing=" + stone.missingLangKeys());
		helper.assertTrue(stone.entityId() == com.vincenthuto.hemomancy.common.entity.npc.dialogue.SanguineMonolithDialogueTrees.BLOCK_ENTITY_ID,
				"the Monolith's tree is a block speaker's: " + stone.entityId());

		HarbingerVicarEntity vicar = PlaythroughNpcs.vicar(helper, PlaythroughSites.stand(helper, 6, 10));
		leader.approach(vicar);
		Outcome interact = leader.interactOn(vicar);
		PlaythroughLog.note(helper, "smoke", "interactOn the Vicar: " + interact);
		helper.assertTrue(leader.lastView(vicar).isPresent(), "interactOn reaches the Vicar's dialogue; " + interact);

		leader.give(new ItemStack(Items.MILK_BUCKET));
		Outcome milk = leader.consume(Items.MILK_BUCKET);
		helper.assertTrue(leader.count(Items.BUCKET) == 1 && leader.count(Items.MILK_BUCKET) == 0,
				"consume drinks the milk to the end; " + milk);

		PlaythroughPlayer member = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 8, 8));
		PlaythroughStates.illuminatusD5(member);
		PlaythroughStates.memberOf(member, leader);
		helper.assertTrue(level.getPlayerByUUID(member.uuid()) == null, "a player not joined is not in the level's list");
		member.joinLevel();
		helper.assertTrue(level.getPlayerByUUID(member.uuid()) == member.player(), "joinLevel lists the player in the level");
		Outcome kicked = leader.kick(member.uuid());
		PlaythroughLog.note(helper, "smoke", "kick: " + kicked + " member heard " + member.chat());
		helper.assertTrue(PlaythroughStates.lineOf(member) == null, "the kicked member is in no line; " + kicked);
		helper.assertTrue(member.chat().stream().anyMatch(line -> line.has("hemomancy.bloodline.kick.you_were_removed")),
				"the joined member is told: " + member.chat());
		member.relog();
		helper.assertTrue(!member.joinedLevel() && level.getPlayerByUUID(member.uuid()) == null, "a relog drops the join");
		helper.succeed();
	}

	/**
	 * A synthetic Court site: inside it is the Holding Stone's ground ({@code CourtLife.houseAt}), and its restore
	 * takes the site away. Court standing set by {@code courtStanding} reads back.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = "playthrough_smoke")
	public static void syntheticCourtSiteAndStanding(GameTestHelper helper) {
		BlockPos centre = PlaythroughSites.stand(helper, 16, 12);
		Runnable restore = PlaythroughSites.courtSite(helper, CourtSite.TIDE_REFUGE, centre);
		helper.assertTrue(CourtLife.houseAt(helper.getLevel(), centre) == CourtHouse.HOLDING_STONE,
				"the synthetic Tide Refuge is the Holding Stone's ground");
		restore.run();
		helper.assertTrue(CourtLife.houseAt(helper.getLevel(), centre) == null, "the restored chunk has no Court site");
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, centre);
		PlaythroughStates.illuminatusD5(player);
		PlaythroughStates.courtStanding(player, CourtHouse.GIVING_LIGHT, CourtStandingRules.Standing.FRIEND);
		helper.succeed();
	}

	/**
	 * The shared rite helpers on a Founding Fane floor at D5: a founded line, the floor laid, then
	 * {@code PlaythroughRites.prepare} (pillars by hand, the offering brazier filled and lit, Sanguine Quintessence
	 * seated by a real {@code useItemOn}), then {@code rite(.., RitePlan)} plants the Staff and pauses at once.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 1200, batch = "playthrough_smoke_rites")
	public static void riteHelpersReadyAFaneFloor(GameTestHelper helper) {
		PlaythroughPlayer player = PlaythroughPlayer.create(helper, PlaythroughSites.stand(helper, 20, 12));
		PlaythroughStates.illuminatusD5(player);
		PlaythroughStates.foundedLineD5(player);
		PlaythroughStates.blood(player, 5000.0D); // ARRANGE
		player.give(new ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.living_staff.get())); // ARRANGE
		player.give(new ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.blood_projection.get())); // ARRANGE
		PlaythroughSites.RiteFloor floor = PlaythroughSites.faneFloor(player, helper.absolutePos(new BlockPos(24, 3, 12)));
		PlaythroughClock clock = PlaythroughClock.start(helper);
		PlaythroughRites.prepare(clock, player, floor);
		clock.then(() -> player.wield(com.vincenthuto.hemomancy.common.init.ItemInit.living_staff.get()))
				.step(() -> PlaythroughClock.rite(player, floor.focus(), RitePlan.standard().fightThreats().pauseWhen(() -> true)))
				.then(() -> {
					var rite = com.vincenthuto.hemomancy.common.rite.CardinalRiteSavedData.get(helper.getLevel()).getRite(player.uuid());
					PlaythroughLog.note(helper, "smoke", "founding_fane after planting: " + (rite == null ? "none" : rite.getPhase()));
					helper.assertTrue(rite != null, "the readied Fane floor takes the planted Staff");
				})
				.run();
	}

	/** {@code PlaythroughNotRun.require} for the Chamber of Will: here it is not loaded, so the test says NOT RUN. */
	@GameTest(template = EMPTY, timeoutTicks = 20, batch = "playthrough_smoke", required = false)
	public static void notRunNamesTheMissingChamber(GameTestHelper helper) {
		PlaythroughNotRun.require(helper, com.vincenthuto.hemomancy.common.encounter.ChamberOfWillManager.CHAMBER_OF_WILL,
				"the Chamber of Will", PlaythroughNotRun.CHAMBER);
		PlaythroughLog.note(helper, "smoke", "the Chamber of Will is loaded on this server");
		helper.succeed();
	}

	/**
	 * The real Crimson Troupe pavilion laid beside an {@code empty} test (its own batch; the launcher enables only this
	 * suite's template namespace, so the mod's templates cannot be the test structure): {@code PlaythroughSites.pavilion}
	 * places it with its troupe and registers the site. Optional: it reports what the template brings.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200, batch = "playthrough_smoke_pavilion", required = false)
	public static void pavilionTemplateLoads(GameTestHelper helper) {
		PlaythroughSites.TemplateSite site = PlaythroughSites.pavilion(helper, PlaythroughSites.farOrigin(helper, 64));
		// The template's actors are visible only once every chunk under it is entity-ticking.
		PlaythroughClock.start(helper).until(200, () -> site.ready(helper.getLevel()), "the pavilion's chunks show their entities")
				.then(() -> {
					var actors = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Entity.class,
							net.minecraft.world.phys.AABB.of(site.box()), entity -> !(entity instanceof net.minecraft.world.entity.player.Player));
					PlaythroughLog.note(helper, "smoke", "pavilion box " + site.box() + " actors "
							+ actors.stream().map(entity -> entity.getType().getDescriptionId()).toList());
					helper.assertTrue(helper.getLevel().structureManager().getStructureWithPieceAt(site.centre(),
							helper.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
									.get(com.vincenthuto.hemomancy.Hemomancy.rloc("circus_pavilion"))).isValid(),
							"the pavilion site is registered over the template");
					helper.assertTrue(!actors.isEmpty(), "the pavilion template brings its actors");
				}).run();
	}
}
