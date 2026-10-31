package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtChampionRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtDiscoveryProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.ActKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.Standing;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenEntity;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionEntity;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionRules;
import com.vincenthuto.hemomancy.common.entity.boss.court.KindledChampionTrial;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtHospitality;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtTransgressions;
import com.vincenthuto.hemomancy.common.entity.npc.court.HydraRisingHead;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingStrains;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentSpawner;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtSite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Brined Court Track 10, Tier 2: the two followers who fight. Each listed wrong against the Giving Light wakes the
 * Bloom Warden once per act and nothing else does; a held offering stops its attacks and makes amends once, while a new
 * wrong made holding one still wakes it; worn down it re-anchors and its blooms come up. The Kindled Champion's
 * usurpation respects its gate; a lost challenge starts the wait and the Champion keeps the title; a won one moves the
 * title once, the head crowns the victor, and the old Champion yields and returns to the elders alive.
 */
@GameTestHolder("court_presence_validation")
@PrefixGameTestTemplate(false)
public final class CourtFollowerGameTests {
	private static final String EMPTY = "empty";
	private static final BlockPos STAND = new BlockPos(8, 2, 8);
	private static final BlockPos SITE = new BlockPos(10, 2, 10);
	private static final BlockPos TENDER = new BlockPos(16, 2, 12);
	private static final BlockPos ELDER = new BlockPos(20, 2, 12);
	private static final BlockPos CROWN = new BlockPos(24, 6, 12);

	private CourtFollowerGameTests() {
	}

	// --- The Bloom Warden -------------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void eachWrongWakesTheWardenOncePerAct(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		BlockPos centre = h.absolutePos(SITE);
		try (CapturedPlayer captured = player(h); var site = new CourtLifeGameTests.FakeSite(level, CourtSite.LIGHTHOUSE_GARDEN, centre)) {
			ServerPlayer player = captured.player;
			List<BlockPos> inside = inside(h);
			h.assertTrue(inside.size() >= 3, "the fake Garden covers room for three blocks: " + inside.size());
			BlockPos first = inside.get(0), second = inside.get(1), third = inside.get(2);
			h.setBlock(first, Blocks.MOSS_BLOCK);
			h.setBlock(second, Blocks.MOSS_BLOCK);
			player.gameMode.destroyBlock(h.absolutePos(first));
			h.assertTrue(wardens(h).size() == 1, "breaking the Garden's moss wakes one Warden: " + wardens(h).size());
			h.assertTrue(CourtLife.progress(player).transgressionCount() == 1 && CourtLife.turnsAway(player, CourtHouse.GIVING_LIGHT),
					"the wrong is recorded and the Garden turns the player away");
			player.gameMode.destroyBlock(h.absolutePos(second));
			h.assertTrue(CourtLife.progress(player).transgressionCount() == 1, "the same act again at once is one act");
			h.assertTrue(wardens(h).size() == 1, "and wakes no second Warden");

			CourtMemberEntity gardener = spawn(h, CourtRole.GARDENER, STAND.west(4), "gardener");
			gardener.hurt(level.damageSources().playerAttack(player), 2.0F);
			h.assertTrue(gardener.getHealth() == gardener.getMaxHealth(), "the resident is protected");
			h.assertTrue(CourtLife.progress(player).transgressionCount() == 2, "but raising harm at it is a wrong");
			BloomWardenEntity warden = wardens(h).get(0);
			h.assertTrue(wardens(h).size() == 1 && BloomWardenRules.HARM.equals(warden.wrong()),
					"the Warden already hunting the player takes the new wrong as its own");

			h.setBlock(third, BlockInit.petrified_follower.get());
			player.gameMode.destroyBlock(h.absolutePos(third));
			h.assertBlockPresent(BlockInit.petrified_follower.get(), third);
			h.assertTrue(CourtLife.progress(player).transgressionCount() == 3
							&& BloomWardenRules.UNDERCROFT.equals(CourtLife.progress(player).transgressionAct()),
					"reaching for one of the petrified is taking from the undercroft, refused or not");
			gardener.discard();
			wardens(h).forEach(BloomWardenEntity::discard);
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void reachingForMarimosMossIsAWrong(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		BlockPos centre = h.absolutePos(SITE);
		try (CapturedPlayer captured = player(h); var site = new CourtLifeGameTests.FakeSite(level, CourtSite.LIGHTHOUSE_GARDEN, centre)) {
			ServerPlayer player = captured.player;
			BlockPos moss = inside(h).get(0);
			h.setBlock(moss, BlockInit.marimo_moss.get());
			// Marimo's moss can't be mined outside Creative, so no break event ever lands: the swing is what counts.
			NeoForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(player, h.absolutePos(moss), Direction.UP,
					PlayerInteractEvent.LeftClickBlock.Action.START));
			h.assertBlockPresent(BlockInit.marimo_moss.get(), moss);
			h.assertTrue(CourtLife.progress(player).transgressionCount() == 1
							&& BloomWardenRules.UNDERCROFT.equals(CourtLife.progress(player).transgressionAct()),
					"reaching for Marimo's moss is taking from the undercroft");
			h.assertTrue(wardens(h).size() == 1, "and wakes the Warden: " + wardens(h).size());
			wardens(h).forEach(BloomWardenEntity::discard);
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void ordinaryVisitingWakesNothing(GameTestHelper h) {
		ServerLevel level = h.getLevel();
		BlockPos centre = h.absolutePos(SITE);
		try (CapturedPlayer captured = player(h); var site = new CourtLifeGameTests.FakeSite(level, CourtSite.LIGHTHOUSE_GARDEN, centre)) {
			ServerPlayer player = captured.player;
			CourtDiscoveryProgress.markKnown(player, CourtProgress.Route.VOYAGER);
			player.moveTo(centre.getX() + 0.5D, centre.getY(), centre.getZ() + 0.5D);
			player.tickCount = CourtLife.VISIT_INTERVAL_TICKS;
			CourtLife.onPlayerTick(new PlayerTickEvent.Post(player));
			h.assertTrue(CourtLife.standing(player, CourtHouse.GIVING_LIGHT).atLeast(Standing.GUEST), "visiting makes a guest");
			CourtMemberEntity gardener = spawn(h, CourtRole.GARDENER, SITE.north(2), "gardener");
			player.getFoodData().setFoodLevel(4);
			h.assertTrue(CourtHospitality.handle(player, gardener, CourtMemberDialogue.FOOD), "the Garden feeds a hungry guest");
			h.setBlock(SITE.east(), Blocks.STONE_BRICKS);
			player.gameMode.destroyBlock(centre.east());
			h.setBlock(STAND.west(3), Blocks.MOSS_BLOCK);
			player.gameMode.destroyBlock(h.absolutePos(STAND.west(3)));
			player.setShiftKeyDown(true);
			player.moveTo(centre.getX() + 1.5D, centre.getY(), centre.getZ() + 0.5D);
			h.assertFalse(CourtLife.hasTransgression(player), "visiting, eating, the Garden's stone, moss outside it and sneaking past wake nothing");
			h.assertTrue(wardens(h).isEmpty(), "and no Warden wakes");

			ServerPlayer creative = player;
			creative.setGameMode(GameType.CREATIVE);
			h.setBlock(SITE.west(), Blocks.MOSS_BLOCK);
			creative.gameMode.destroyBlock(centre.west());
			h.assertTrue(wardens(h).isEmpty(), "Creative builds and tests without waking it");
			gardener.discard();
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void aHeldOfferingStopsTheWardenAndMakesAmendsOnce(GameTestHelper h) {
		// The player has to stay in the world through the delay, so it is closed when the check runs.
		CapturedPlayer captured = player(h);
		{
			ServerPlayer player = captured.player;
			BloomWardenEntity warden = CourtTransgressions.transgress(player, BloomWardenRules.GROWTH, h.absolutePos(STAND));
			h.assertTrue(warden != null, "a wrong wakes the Warden");
			warden.forcePose(BloomWardenRules.Phase.ROOTED, BloomWardenRules.Move.NONE);
			warden.moveTo(player.getX() + 1.5D, player.getY(), player.getZ());
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 3));
			h.assertTrue(CourtLife.holdsOffering(player), "bread is a Giving Light offering");
			for (BloomWardenRules.Phase phase : BloomWardenRules.Phase.values()) {
				h.assertTrue(BloomWardenRules.choose(phase, true, 1.0D, 0, 0L) == BloomWardenRules.Move.NONE,
						"no move starts against a held offering: " + phase);
			}
			float health = player.getHealth();
			h.runAfterDelay(60, () -> {
				h.assertTrue(player.getHealth() >= health, "the Warden never struck the player holding an offering");
				h.assertTrue(warden.move() == BloomWardenRules.Move.NONE, "and holds no attack");
				BloomWardenEntity again = CourtTransgressions.transgress(player, BloomWardenRules.HARM, player.blockPosition());
				h.assertTrue(CourtLife.progress(player).transgressionCount() == 2,
						"a new wrong made holding an offering still counts: " + CourtLife.progress(player).transgressionCount());
				h.assertTrue(again == warden, "and still wakes the Warden: " + again + " / " + warden + " " + warden.outcome());
				warden.interact(player, InteractionHand.MAIN_HAND);
				h.assertTrue(player.getMainHandItem().getCount() == 2, "amends take one offering");
				h.assertFalse(CourtLife.hasTransgression(player), "and clear the wrong");
				h.assertTrue(CourtLife.standing(player, CourtHouse.GIVING_LIGHT) == Standing.GUEST, "standing comes back to guest");
				h.assertTrue(warden.outcome() == BloomWardenRules.Outcome.RECONCILED, "the fight is over");
				warden.interact(player, InteractionHand.MAIN_HAND);
				h.assertTrue(player.getMainHandItem().getCount() == 2, "nothing more is taken");
				captured.close();
				h.succeed();
			});
		}
	}

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void theWardenReanchorsWhenWornDownAndItsBloomsComeUp(GameTestHelper h) {
		try (CapturedPlayer captured = player(h)) {
			ServerPlayer player = captured.player;
			BloomWardenEntity warden = CourtTransgressions.transgress(player, BloomWardenRules.GROWTH, h.absolutePos(STAND));
			h.assertTrue(warden != null, "a wrong wakes the Warden");
			h.assertFalse(warden.hurt(h.getLevel().damageSources().playerAttack(player), 5.0F), "untouchable through its opening");
			warden.forcePose(BloomWardenRules.Phase.ROOTED, BloomWardenRules.Move.NONE);
			List<BlockPos> beds = List.of(STAND.offset(3, 0, 0), STAND.offset(-3, 0, 2), STAND.offset(0, 0, 4));
			for (BlockPos bed : beds) {
				h.setBlock(bed.below(), Blocks.STONE);
				h.setBlock(bed, Blocks.AIR);
				h.assertTrue(warden.sowAt(h.absolutePos(bed)), "a bloom grows at " + bed);
			}
			h.assertTrue(warden.bloomCount() == 3, "three blooms on the floor");
			h.assertTrue(BloomWardenRules.damageTaken(8.0F, BloomWardenRules.Phase.ROOTED, 2) == 2.0F,
					"rooted behind fed blooms a hit is worth a quarter");
			h.assertTrue(BloomWardenRules.damageTaken(8.0F, BloomWardenRules.Phase.UPROOTED, 2) == 8.0F, "uprooted, all of it");
			warden.forcePose(BloomWardenRules.Phase.UPROOTED, BloomWardenRules.Move.NONE);
			h.assertTrue(warden.hurt(h.getLevel().damageSources().playerAttack(player), 1000.0F), "worn down");
			h.assertTrue(warden.isAlive() && warden.outcome() == BloomWardenRules.Outcome.REANCHORED, "it re-anchors instead of dying");
			for (BlockPos bed : beds) h.assertBlockPresent(Blocks.AIR, bed);
			h.assertTrue(CourtLife.hasTransgression(player), "beating it is no amends: the wrong stays recorded");
			h.assertTrue(captured.chat.stream().anyMatch(line -> line.contains("planted") || line.contains("named.growth")), "the chorus names the wrong: " + captured.chat);
			h.assertFalse(warden.hurt(h.getLevel().damageSources().playerAttack(player), 5.0F), "and it can't be hurt as it sinks");
			h.runAfterDelay(BloomWardenRules.REANCHOR_TICKS + 5, () -> {
				h.assertTrue(warden.isRemoved(), "it has sunk back into the tier");
				h.succeed();
			});
		}
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aPlayerWhoDiesToTheWardenHasLost(GameTestHelper h) {
		CapturedPlayer captured = player(h);
		{
			ServerPlayer player = captured.player;
			BloomWardenEntity warden = CourtTransgressions.transgress(player, BloomWardenRules.GROWTH, h.absolutePos(STAND));
			warden.forcePose(BloomWardenRules.Phase.ROOTED, BloomWardenRules.Move.NONE);
			player.setHealth(0.0F);
			player.die(h.getLevel().damageSources().mobAttack(warden));
			h.runAfterDelay(3, () -> {
				h.assertTrue(warden.outcome() == BloomWardenRules.Outcome.PLAYER_DIED, "the fight ends with the player's death");
				h.assertTrue(CourtLife.hasTransgression(player), "and the wrong is still recorded through it");
				captured.close();
				h.succeed();
			});
		}
	}

	// --- The Kindled Champion ---------------------------------------------------------------------------------------

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theUsurpationRespectsItsGate(GameTestHelper h) {
		try (CapturedPlayer captured = player(h)) {
			ServerPlayer player = captured.player;
			CourtMemberEntity tender = spawn(h, CourtRole.HEAD_TENDER, TENDER, "threshold");
			CourtMemberEntity elder = spawn(h, CourtRole.KINDLED_ELDER, ELDER, "kindled_elder");
			h.assertTrue(KindledChampionTrial.challenge(player, tender) == null, "a stranger is refused");
			h.assertTrue(champions(h).isEmpty() && elder.isAlive(), "and no Champion comes out");
			var tree = CourtMemberDialogue.forMember(tender, player);
			h.assertTrue(tree.getStartNode().options().stream().noneMatch(o -> CourtMemberDialogue.CHALLENGE.equals(o.eventId())),
					"the Head-tender offers no challenge while the gate is shut");
			openGate(player);
			tree = CourtMemberDialogue.forMember(tender, player);
			h.assertTrue(tree.getStartNode().options().stream().anyMatch(o -> CourtMemberDialogue.CHALLENGE.equals(o.eventId())),
					"and offers it once the gate opens");
			KindledChampionEntity champion = KindledChampionTrial.challenge(player, tender);
			h.assertTrue(champion != null && champion.isFacing(player), "the Champion comes out for the challenger");
			h.assertTrue(elder.isRemoved(), "from the elder's place");
			h.assertTrue(KindledChampionTrial.challenge(player, tender) == null && champions(h).size() == 1,
					"one challenge at a time");
			champions(h).forEach(KindledChampionEntity::discard);
			tender.discard();
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void aLostUsurpationStartsTheWaitAndTheChampionKeepsItsTitle(GameTestHelper h) {
		try (CapturedPlayer captured = player(h)) {
			ServerPlayer player = captured.player;
			openGate(player);
			CourtMemberEntity tender = spawn(h, CourtRole.HEAD_TENDER, TENDER, "threshold");
			spawn(h, CourtRole.KINDLED_ELDER, ELDER, "kindled_elder");
			KindledChampionEntity champion = KindledChampionTrial.challenge(player, tender);
			h.assertFalse(champion.hurt(h.getLevel().damageSources().playerAttack(player), 5.0F), "untouchable through its opening");
			long now = h.getLevel().getGameTime();
			player.setHealth(0.0F);
			player.die(h.getLevel().damageSources().mobAttack(champion));
			h.assertTrue(champion.outcome() == KindledChampionRules.Outcome.CHALLENGER_DIED, "the challenger died");
			h.assertTrue(CourtLife.progress(player).usurpRetryAt() == now + CourtChampionRules.USURP_RETRY_TICKS, "a day's wait");
			h.assertFalse(CourtLife.isChampion(player), "the Champion keeps the title");
			// Dying dropped the strain; carry one again so the gate shows the wait rather than the strain.
			player.getInventory().add(MorphlingPowerGameTests.stack(MorphlingStrains.KINBUD, 4));
			h.assertTrue(CourtLife.championGate(player) == CourtChampionRules.Gate.REFUSED_WAIT,
					"and the gate waits: " + CourtLife.championGate(player));
			h.runAfterDelay(45, () -> {
				h.assertTrue(champion.isRemoved(), "the Champion has gone back");
				h.assertTrue(elders(h).size() == 1, "to the elders, on its mark");
				champions(h).forEach(KindledChampionEntity::discard);
				elders(h).forEach(CourtMemberEntity::discard);
				tender.discard();
				h.succeed();
			});
		}
	}

	@GameTest(template = EMPTY, timeoutTicks = 160)
	public static void aYieldedChampionLivesAndTheHeadCrownsTheVictor(GameTestHelper h) {
		h.setBlock(CROWN, BlockInit.hydra_crown.get());
		try (CapturedPlayer captured = player(h)) {
			ServerPlayer player = captured.player;
			openGate(player);
			CourtMemberEntity tender = spawn(h, CourtRole.HEAD_TENDER, TENDER, "threshold");
			spawn(h, CourtRole.KINDLED_ELDER, ELDER, "kindled_elder");
			KindledChampionEntity champion = KindledChampionTrial.challenge(player, tender);
			champion.forcePose(0, KindledChampionRules.Move.NONE);
			h.assertTrue(champion.hurt(h.getLevel().damageSources().playerAttack(player), 100.0F), "a target in its fight");
			h.assertTrue(champion.budStage() == 1, "it buds a ring at the first threshold");
			h.assertTrue(champion.hurt(h.getLevel().damageSources().playerAttack(player), 1000.0F), "worn down");
			h.assertTrue(champion.isAlive() && champion.outcome() == KindledChampionRules.Outcome.YIELDED, "it yields and lives");
			h.assertTrue(CourtLife.isChampion(player), "the title moves to the victor");
			h.assertTrue(champion.pendingVents() == 0, "the vents fall quiet");
			List<HydraRisingHead> heads = h.getLevel().getEntitiesOfClass(HydraRisingHead.class,
					new AABB(h.absolutePos(CROWN)).inflate(2.0D));
			h.assertTrue(heads.size() == 1, "Hydra's largest head rises to crown the victor");
			h.assertFalse(captured.chat.isEmpty(), "the Head-tender names the Champion");
			long since = CourtLife.progress(player).championSince();
			h.assertFalse(champion.hurt(h.getLevel().damageSources().playerAttack(player), 5.0F), "the yielded Champion is no target");
			h.assertTrue(CourtLife.progress(player).championSince() == since, "the title changed hands once");
			h.assertTrue(KindledChampionTrial.challenge(player, tender) == null, "the holder can't challenge again");
			h.runAfterDelay(KindledChampionRules.YIELD_TICKS + 5, () -> {
				h.assertTrue(champion.isRemoved(), "the old Champion steps back");
				List<CourtMemberEntity> elders = elders(h);
				h.assertTrue(elders.size() == 1 && elders.get(0).isAlive(), "and stands among the elders, alive");
				heads.forEach(HydraRisingHead::discard);
				elders.forEach(CourtMemberEntity::discard);
				tender.discard();
				h.succeed();
			});
		}
	}

	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void theChampionsCrownAndRegrowthFollowItsThresholds(GameTestHelper h) {
		h.assertTrue(KindledChampionRules.budStage(0.9F) == 0 && KindledChampionRules.budStage(0.5F) == 1
				&& KindledChampionRules.budStage(0.2F) == 2, "a ring of buds at each third");
		h.assertTrue(KindledChampionRules.crownCount(0) == 8 && KindledChampionRules.crownCount(2) == 16, "eight, then twelve, then sixteen");
		h.assertTrue(KindledChampionRules.regrowthPerSecond(2) > KindledChampionRules.regrowthPerSecond(0), "regrowth climbs with the crown");
		h.assertTrue(KindledChampionRules.yieldsAt(KindledChampionRules.MAX_HEALTH * 0.05F, KindledChampionRules.MAX_HEALTH)
				&& !KindledChampionRules.yieldsAt(KindledChampionRules.MAX_HEALTH * 0.5F, KindledChampionRules.MAX_HEALTH), "it yields near the end");
		h.assertTrue(BloomWardenRules.isNewAct(null, 0L, BloomWardenRules.GROWTH, 10L)
				&& !BloomWardenRules.isNewAct(BloomWardenRules.GROWTH, 10L, BloomWardenRules.GROWTH, 20L)
				&& BloomWardenRules.isNewAct(BloomWardenRules.GROWTH, 10L, BloomWardenRules.HARM, 20L)
				&& BloomWardenRules.isNewAct(BloomWardenRules.GROWTH, 10L, BloomWardenRules.GROWTH, 10L + BloomWardenRules.ACT_DEBOUNCE + 1),
				"one act is told from the next");
		h.succeed();
	}

	// --- Helpers ----------------------------------------------------------------------------------------------------

	private static void openGate(ServerPlayer player) {
		CourtDiscoveryProgress.markKnown(player, CourtProgress.Route.VOYAGER);
		player.getInventory().add(MorphlingPowerGameTests.stack(MorphlingStrains.KINBUD, 4));
		CourtLife.recordAct(player, CourtHouse.KINDLING, "visit", ActKind.VISIT);
		CourtLife.recordAct(player, CourtHouse.KINDLING, "custom_vent_offering", ActKind.CUSTOM);
	}

	private static CourtMemberEntity spawn(GameTestHelper h, CourtRole role, BlockPos rel, String mark) {
		BlockPos at = h.absolutePos(rel);
		CourtResidentSpawner.place(h.getLevel(), role, at, Direction.SOUTH.toYRot(), mark);
		CourtMemberEntity member = h.getLevel().getEntitiesOfClass(CourtMemberEntity.class, new AABB(at).inflate(1.0D),
				m -> m.role() == role && !(m instanceof KindledChampionEntity) && !(m instanceof BloomWardenEntity)).get(0);
		member.setNoAi(true);
		return member;
	}

	/** Relative positions round {@link #SITE} the fake Garden's piece covers (its chunk may not hold them all). */
	private static List<BlockPos> inside(GameTestHelper h) {
		List<BlockPos> found = new java.util.ArrayList<>();
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				BlockPos rel = SITE.offset(dx, 0, dz);
				if (CourtLife.houseAt(h.getLevel(), h.absolutePos(rel)) == CourtHouse.GIVING_LIGHT) found.add(rel);
			}
		}
		return found;
	}

	private static List<BloomWardenEntity> wardens(GameTestHelper h) {
		return h.getLevel().getEntitiesOfClass(BloomWardenEntity.class, new AABB(h.absolutePos(STAND)).inflate(40.0D));
	}

	private static List<KindledChampionEntity> champions(GameTestHelper h) {
		return h.getLevel().getEntitiesOfClass(KindledChampionEntity.class, new AABB(h.absolutePos(ELDER)).inflate(40.0D));
	}

	private static List<CourtMemberEntity> elders(GameTestHelper h) {
		return h.getLevel().getEntitiesOfClass(CourtMemberEntity.class, new AABB(h.absolutePos(ELDER)).inflate(40.0D),
				m -> m.role() == CourtRole.KINDLED_ELDER && !(m instanceof KindledChampionEntity));
	}

	private static CapturedPlayer player(GameTestHelper h) {
		CapturedPlayer captured = CapturedPlayer.create(h).online();
		ServerPlayer player = captured.player;
		player.setGameMode(GameType.SURVIVAL);
		BlockPos stand = h.absolutePos(STAND);
		player.moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(6);
		return captured;
	}
}
