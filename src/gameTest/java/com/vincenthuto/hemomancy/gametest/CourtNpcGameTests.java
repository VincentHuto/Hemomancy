package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtLife;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.ActKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.Standing;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtHospitality;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtRole;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtSchoolRules;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtSchools;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtVacancies;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.CourtMemberDialogue;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.DialogueTree;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtResidentSpawner;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.VentSanctumHeads;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Brined Court Track 9: every built role spawns on its resident mark through the structure spawner, persistent and
 * leashed (mobile) or held on the mark (anchored); members breathe water and never drown; they are protected and never
 * target a player; they feed, rest, give the welcome gift once and take the house's custom; the leads teach and take
 * members in at the fifth Degree and not before; an Unstained visitor is shunned by every role (one refusal, nothing
 * given, the members close up) and is never attacked.
 */
@GameTestHolder("court_npc_validation")
@PrefixGameTestTemplate(false)
public final class CourtNpcGameTests {
	private static final String EMPTY = "empty";
	private static final BlockPos MARK = new BlockPos(2, 2, 2);
	private static final BlockPos STAND = new BlockPos(4, 2, 2);
	private static final List<CourtRole> BUILT = List.copyOf(EntityInit.court_members.keySet());

	private CourtNpcGameTests() {
	}

	private static CourtMemberEntity spawn(GameTestHelper h, CourtRole role, String mark) {
		BlockPos at = h.absolutePos(MARK);
		h.assertTrue(CourtResidentSpawner.place(h.getLevel(), role, at, Direction.SOUTH.toYRot(), mark), role + " has a member type");
		// The newest member on the mark: a test may set two of one role down on the same spot.
		return h.getLevel().getEntitiesOfClass(CourtMemberEntity.class, new AABB(at).inflate(1.5D),
				member -> member.role() == role).stream().max(java.util.Comparator.comparingInt(CourtMemberEntity::getId)).orElseThrow();
	}

	private static CapturedPlayer player(GameTestHelper h, int degree) {
		CapturedPlayer captured = CapturedPlayer.create(h).online();
		BlockPos stand = h.absolutePos(STAND);
		captured.player.moveTo(stand.getX() + 0.5D, stand.getY(), stand.getZ() + 0.5D);
		HemoCapabilityAccess.getInitiatoryDegree(captured.player).setDegreeNumber(degree);
		var blood = HemoCapabilityAccess.getBloodVolume(captured.player);
		blood.setMaxBloodVolume(5000.0D);
		blood.setBloodVolume(5000.0D);
		blood.setActive(degree > 0);
		return captured;
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void everyBuiltRoleSpawnsOnItsMarkLeashedAndPersistent(GameTestHelper h) {
		h.assertTrue(BUILT.containsAll(List.of(CourtRole.GARDENER, CourtRole.ARBOR, CourtRole.LIGHTKEEPER,
				CourtRole.OFFERING_BEARER, CourtRole.KINDLED_ELDER, CourtRole.HEAD_TENDER)), "the two Seats' roles are built");
		h.assertTrue(BUILT.containsAll(List.of(CourtRole.TIDEWARDEN, CourtRole.BRACED_ELDER)), "the Holding Stone's roles are built");
		h.assertTrue(BUILT.containsAll(List.of(CourtRole.EDGE_SWIMMER, CourtRole.NURSERY_TENDER)), "the Shared Wake's roles are built");
		h.assertTrue(BUILT.containsAll(List.of(CourtRole.LINESMAN, CourtRole.LAMP_COLONY)), "the Woven Current's roles are built");
		h.assertTrue(BUILT.containsAll(List.of(CourtRole.HOST, CourtRole.VEILED_RESIDENT)), "the Veiled Fold's roles are built");
		h.assertTrue(BUILT.containsAll(List.of(CourtRole.BELLWARDEN, CourtRole.SLEEPER)), "the Still Bell's roles are built");
		h.assertTrue(BUILT.containsAll(List.of(CourtRole.MARROW_TENDER, CourtRole.ROOTED_ELDER)), "Last Marrow's roles are built");
		h.assertTrue(BUILT.size() == CourtRole.values().length, "every role has a member type");
		BlockPos at = h.absolutePos(MARK);
		for (CourtRole role : BUILT) {
			CourtMemberEntity member = spawn(h, role, role.marks().isEmpty() ? "threshold" : role.marks().getFirst());
			h.assertTrue(member.isPersistenceRequired() && !member.removeWhenFarAway(1000), role + " is persistent");
			h.assertTrue(at.equals(member.home()), role + " is homed on its mark");
			h.assertTrue(Math.abs(member.getYRot() - Direction.SOUTH.toYRot()) < 0.01F, role + " faces the way the mark faces");
			if (role.anchored()) {
				h.assertFalse(member.hasRestriction(), role + " is anchored, not leashed");
			} else {
				h.assertTrue(member.hasRestriction() && at.equals(member.getRestrictCenter())
						&& member.getRestrictRadius() == CourtMemberEntity.LEASH, role + " walks a leash round its mark");
			}
			member.discard();
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void anAnchoredMemberHoldsItsMark(GameTestHelper h) {
		CourtMemberEntity arbor = spawn(h, CourtRole.ARBOR, "arbor_anchored");
		arbor.moveTo(arbor.getX() + 2.0D, arbor.getY(), arbor.getZ());
		h.runAfterDelay(5, () -> {
			h.assertTrue(arbor.blockPosition().equals(h.absolutePos(MARK)), "the arbor is back on its anchor");
			h.assertFalse(arbor.isPushable(), "an anchored member can't be pushed off its mark");
			h.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void membersBreatheWater(GameTestHelper h) {
		for (int x = 1; x <= 3; x++) for (int y = 2; y <= 4; y++) for (int z = 1; z <= 3; z++) {
			h.setBlock(new BlockPos(x, y, z), Blocks.WATER);
		}
		CourtMemberEntity elder = spawn(h, CourtRole.KINDLED_ELDER, "kindled_elder");
		CourtMemberEntity gardener = spawn(h, CourtRole.GARDENER, "gardener");
		h.runAfterDelay(160, () -> {
			for (CourtMemberEntity member : List.of(elder, gardener)) {
				h.assertTrue(member.isInWater(), member.role() + " is under water");
				h.assertTrue(member.getAirSupply() == member.getMaxAirSupply(), member.role() + " never runs out of air");
				h.assertTrue(member.getHealth() == member.getMaxHealth(), member.role() + " takes no drowning damage");
				h.assertFalse(member.isPushedByFluid(), member.role() + " is not pushed by currents");
			}
			h.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void membersAreProtectedAndNeverTargetThePlayer(GameTestHelper h) {
		CapturedPlayer captured = player(h, 5);
		ServerPlayer player = captured.player;
		CourtMemberEntity gardener = spawn(h, CourtRole.GARDENER, "gardener");
		CourtMemberEntity tender = spawn(h, CourtRole.HEAD_TENDER, "threshold");
		float before = player.getHealth();
		h.assertFalse(gardener.hurt(player.damageSources().playerAttack(player), 6.0F), "a player can't hurt a member");
		h.assertTrue(gardener.getHealth() == gardener.getMaxHealth(), "the member is untouched");
		h.runAfterDelay(80, () -> {
			for (Mob member : List.of(gardener, tender)) {
				h.assertTrue(member.getTarget() == null, member.getType() + " never targets the player");
			}
			h.assertTrue(player.getHealth() >= before, "nobody struck the player");
			captured.close();
			h.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aDefenderTurnsOnMonstersOnly(GameTestHelper h) {
		CourtMemberEntity member = spawn(h, CourtRole.GARDENER, "gardener");
		Mob husk = EntityType.HUSK.create(h.getLevel());
		husk.moveTo(h.absoluteVec(STAND.getCenter()));
		husk.setNoAi(true);
		h.getLevel().addFreshEntity(husk);
		Mob wolf = EntityType.WOLF.create(h.getLevel());
		wolf.moveTo(h.absoluteVec(STAND.getCenter()));
		h.getLevel().addFreshEntity(wolf);
		try (CapturedPlayer captured = player(h, 5)) {
			h.assertTrue(member.defendsAgainst(husk), "a wild monster near home");
			h.assertFalse(member.defendsAgainst(wolf), "an animal is not a monster");
			h.assertFalse(member.defendsAgainst(captured.player), "never a player");
			h.assertFalse(CourtRole.GARDENER.defends(), "the Gardener itself does not defend; the Tidewarden and Edge-swimmer do");
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theGardenerFeedsRestsAndGivesTheWelcomeOnce(GameTestHelper h) {
		CourtMemberEntity gardener = spawn(h, CourtRole.GARDENER, "gardener");
		try (CapturedPlayer captured = player(h, 2)) {
			ServerPlayer player = captured.player;
			player.getFoodData().setFoodLevel(6);
			h.assertTrue(CourtHospitality.handle(player, gardener, CourtMemberDialogue.FOOD), "a hungry visitor is fed");
			h.assertTrue(player.getFoodData().getFoodLevel() == 6 + CourtHospitality.FOOD_HUNGER, "fed, below the fifth Degree too");
			player.setHealth(6.0F);
			h.assertTrue(CourtHospitality.handle(player, gardener, CourtMemberDialogue.REST), "rest");
			h.assertTrue(player.getHealth() == 6.0F + CourtHospitality.REST_HEAL, "rest heals");
			h.assertFalse(CourtHospitality.handle(player, gardener, CourtMemberDialogue.REST), "not again so soon");
			h.assertTrue(CourtHospitality.handle(player, gardener, CourtMemberDialogue.GIFT), "the welcome gift");
			h.assertTrue(player.getInventory().countItem(Items.GLOW_BERRIES) == 6, "six glow berries from the beds");
			h.assertFalse(CourtHospitality.handle(player, gardener, CourtMemberDialogue.GIFT), "a gift is never taken twice");
			h.assertTrue(player.getInventory().countItem(Items.GLOW_BERRIES) == 6, "still six");
			h.assertTrue(CourtLife.standing(player, CourtHouse.GIVING_LIGHT).atLeast(Standing.GUEST), "the welcome makes a guest");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 2));
			h.assertTrue(CourtHospitality.handle(player, gardener, CourtMemberDialogue.OFFER), "food for the table");
			h.assertTrue(player.getMainHandItem().getCount() == 1, "one loaf taken");
			h.assertTrue(CourtLife.standing(player, CourtHouse.GIVING_LIGHT) == Standing.FRIEND, "the custom raises standing");
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theLeadsTeachAndTakeMembersInAtTheFifthDegree(GameTestHelper h) {
		CourtMemberEntity keeper = spawn(h, CourtRole.LIGHTKEEPER, "lamp_keeper");
		CourtMemberEntity tender = spawn(h, CourtRole.HEAD_TENDER, "threshold");
		try (CapturedPlayer captured = player(h, 4)) {
			ServerPlayer player = captured.player;
			CourtLife.recordAct(player, CourtHouse.GIVING_LIGHT, CourtLife.VISIT_ACT, ActKind.VISIT);
			h.assertFalse(CourtHospitality.handle(player, keeper, CourtMemberDialogue.LESSON), "no lesson before the fifth Degree");
			h.assertFalse(CourtHospitality.handle(player, keeper, CourtMemberDialogue.JOIN), "no joining before the fifth Degree");
			h.assertFalse(CourtLife.hasLesson(player) || CourtLife.house(player) != null, "nothing changed");
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(5);
			h.assertTrue(CourtHospitality.handle(player, keeper, CourtMemberDialogue.LESSON), "the Lightkeeper teaches");
			h.assertTrue(CourtLife.hasLesson(player), "the lesson is recorded");
			h.assertTrue(CourtHospitality.handle(player, keeper, CourtMemberDialogue.JOIN), "joined the Giving Light");
			h.assertTrue(CourtLife.house(player) == CourtHouse.GIVING_LIGHT, "a member of the Giving Light");
			h.assertFalse(CourtHospitality.handle(player, tender, CourtMemberDialogue.LESSON), "only the Lightkeeper teaches");
			h.assertFalse(CourtHospitality.handle(player, tender, CourtMemberDialogue.JOIN), "the Sanctum doesn't know the player yet");
			CourtLife.recordAct(player, CourtHouse.KINDLING, CourtLife.VISIT_ACT, ActKind.VISIT);
			h.assertTrue(CourtHospitality.handle(player, tender, CourtMemberDialogue.JOIN), "the one switch, through the Head-tender");
			h.assertTrue(CourtLife.house(player) == CourtHouse.KINDLING && CourtLife.progress(player).switchUsed(), "switched");
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void anUnstainedVisitorIsShunnedByEveryRole(GameTestHelper h) {
		try (CapturedPlayer captured = player(h, 5)) {
			ServerPlayer player = captured.player;
			HemoCapabilityAccess.getUnstainedProgress(player).setBegunPurification(true);
			player.getFoodData().setFoodLevel(4);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 2));
			for (CourtRole role : BUILT) {
				CourtMemberEntity member = spawn(h, role, role.marks().isEmpty() ? "threshold" : role.marks().getFirst());
				DialogueTree tree = CourtMemberDialogue.forMember(member, player);
				h.assertTrue(tree.nodes().size() == 1 && tree.getStartNode().lines().getFirst()
						.equals("hemomancy.court." + role.id() + ".shunned.line1"), role + " turns the Unstained away");
				for (String event : CourtMemberDialogue.EVENTS) {
					h.assertFalse(CourtHospitality.handle(player, member, event), role + " gives the Unstained nothing: " + event);
				}
				member.discard();
			}
			h.assertTrue(player.getFoodData().getFoodLevel() == 4, "not fed");
			h.assertTrue(player.getMainHandItem().getCount() == 2, "no offering taken");
			h.assertTrue(player.getInventory().countItem(Items.GLOW_BERRIES) == 0, "no gift");
			h.assertFalse(CourtLife.hasLesson(player) || CourtLife.house(player) != null, "no lesson, no house");
		}
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void membersCloseUpNearTheUnstainedAndNeverStrike(GameTestHelper h) {
		// A player in the level, so the members can see who stands near them.
		ServerPlayer visitor = DuctilisGameTests.player(h);
		visitor.moveTo(h.absoluteVec(STAND.getCenter()));
		HemoCapabilityAccess.getUnstainedProgress(visitor).setBegunPurification(true);
		CourtMemberEntity arbor = spawn(h, CourtRole.ARBOR, "arbor_anchored");
		CourtMemberEntity keeper = spawn(h, CourtRole.LIGHTKEEPER, "lamp_keeper");
		keeper.setNoAi(true);
		h.runAfterDelay(25, () -> {
			h.assertTrue(keeper.isWary(), "the Lightkeeper closes up near the Unstained");
			h.assertTrue(arbor.isWary() && arbor.isWithdrawn(), "the arbor withdraws");
			h.assertTrue(keeper.getTarget() == null && arbor.getTarget() == null, "never an attack");
			visitor.discard();
			h.succeed();
		});
	}

	/** A mobile member steps away from an Unstained visitor and stays out of reach. */
	@GameTest(template = EMPTY, timeoutTicks = 140)
	public static void aMobileMemberKeepsItsDistance(GameTestHelper h) {
		ServerPlayer visitor = DuctilisGameTests.player(h);
		visitor.moveTo(h.absoluteVec(STAND.getCenter()));
		HemoCapabilityAccess.getUnstainedProgress(visitor).setBegunPurification(true);
		CourtMemberEntity gardener = spawn(h, CourtRole.GARDENER, "gardener");
		double start = gardener.distanceTo(visitor);
		h.runAfterDelay(100, () -> {
			h.assertTrue(gardener.distanceTo(visitor) > start, "the Gardener moved away: " + start + " -> " + gardener.distanceTo(visitor));
			h.assertTrue(gardener.getTarget() == null, "and never struck");
			visitor.discard();
			h.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theWithdrawingArborCycles(GameTestHelper h) {
		CourtMemberEntity arbor = spawn(h, CourtRole.ARBOR, "arbor_withdrawing");
		CourtMemberEntity still = spawn(h, CourtRole.ARBOR, "arbor_anchored");
		arbor.tickCount = CourtMemberEntity.WITHDRAW_CYCLE + 1;
		still.tickCount = CourtMemberEntity.WITHDRAW_CYCLE + 1;
		h.runAfterDelay(15, () -> {
			h.assertTrue(arbor.isWithdrawn(), "the withdrawing arbor withdraws in its turn");
			h.assertFalse(still.isWithdrawn(), "an anchored arbor stays open");
			h.succeed();
		});
	}

	/** The Tidewarden, the first role that defends: it turns on a wild monster near home and strikes it. */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void theTidewardenDefendsHome(GameTestHelper h) {
		CourtMemberEntity warden = spawn(h, CourtRole.TIDEWARDEN, "refuge_keeper");
		h.assertTrue(CourtRole.TIDEWARDEN.defends(), "the Tidewarden defends");
		Mob husk = EntityType.HUSK.create(h.getLevel());
		husk.moveTo(h.absoluteVec(STAND.getCenter()));
		h.getLevel().addFreshEntity(husk);
		float full = husk.getMaxHealth();
		h.succeedWhen(() -> {
			h.assertTrue(warden.getTarget() == husk, "the Tidewarden targets the monster");
			h.assertTrue(husk.getHealth() < full || !husk.isAlive(), "and strikes it");
			h.assertTrue(warden.getHealth() == warden.getMaxHealth(), "the Tidewarden is untouched");
		});
	}

	/** The Holding Stone's custom is stone for the passages, not food; the Braced elder gives kelp once and takes members in. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theHoldingStoneTakesStoneAndTheBracedElderTakesMembersIn(GameTestHelper h) {
		CourtMemberEntity warden = spawn(h, CourtRole.TIDEWARDEN, "refuge_keeper");
		CourtMemberEntity elder = spawn(h, CourtRole.BRACED_ELDER, "holdfast_braced");
		try (CapturedPlayer captured = player(h, 4)) {
			ServerPlayer player = captured.player;
			h.assertFalse(warden.isPushable() == elder.isPushable(), "the Tidewarden walks; the elder is held on its mark");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 2));
			h.assertFalse(CourtHospitality.handle(player, warden, CourtMemberDialogue.OFFER), "food is the Garden's custom, not the refuge's");
			h.assertTrue(player.getMainHandItem().getCount() == 2, "the bread is kept");
			h.assertTrue(CourtHospitality.handle(player, elder, CourtMemberDialogue.GIFT), "the elder's welcome");
			h.assertTrue(player.getInventory().countItem(Items.KELP) == 8, "eight kelp from the pool");
			h.assertFalse(CourtHospitality.handle(player, warden, CourtMemberDialogue.GIFT), "the Tidewarden gives no gift");
			h.assertTrue(CourtLife.standing(player, CourtHouse.HOLDING_STONE) == Standing.GUEST, "the welcome makes a guest");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COBBLESTONE, 3));
			DialogueTree tree = CourtMemberDialogue.forMember(warden, player);
			h.assertTrue(tree.getStartNode().options().stream().anyMatch(o -> CourtMemberDialogue.OFFER.equals(o.eventId())
					&& o.text().endsWith("offer_stone")), "the Tidewarden asks for the stone in hand");
			h.assertTrue(CourtHospitality.handle(player, warden, CourtMemberDialogue.OFFER), "stone for the passages");
			h.assertTrue(player.getMainHandItem().getCount() == 2, "one block taken");
			h.assertTrue(CourtLife.standing(player, CourtHouse.HOLDING_STONE) == Standing.FRIEND, "the custom raises standing");
			h.assertFalse(CourtHospitality.handle(player, elder, CourtMemberDialogue.JOIN), "no joining before the fifth Degree");
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(5);
			h.assertTrue(CourtHospitality.handle(player, elder, CourtMemberDialogue.JOIN), "the Braced elder takes the player in");
			h.assertTrue(CourtLife.house(player) == CourtHouse.HOLDING_STONE, "a member of the Holding Stone");
		}
		h.succeed();
	}

	/**
	 * A member removed with /kill leaves its mark vacant; a day later, with no player near, a new member of the role
	 * stands there, once. A Creative player's blow leaves no vacancy.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = "court_npc_vacancy")
	public static void aMemberLostToKillIsReplacedOnItsMark(GameTestHelper h) {
		var level = h.getLevel();
		BlockPos home = h.absolutePos(MARK);
		CourtMemberEntity warden = spawn(h, CourtRole.TIDEWARDEN, "refuge_keeper");
		CourtVacancies vacancies = CourtVacancies.get(level);
		warden.kill();
		h.assertFalse(warden.isAlive(), "/kill removes a member");
		var open = vacancies.vacancies().stream().filter(v -> v.home().equals(home)).toList();
		h.assertTrue(open.size() == 1 && open.getFirst().role() == CourtRole.TIDEWARDEN
				&& open.getFirst().mark().equals("refuge_keeper"), "the mark is vacant: " + open);
		long due = open.getFirst().due();
		h.assertTrue(due - level.getGameTime() == CourtVacancies.REFILL_TICKS, "filled after a day");
		vacancies.fill(level, due - 1);
		h.assertTrue(level.getEntitiesOfClass(CourtMemberEntity.class, new AABB(home).inflate(2.0D)).stream()
				.noneMatch(CourtMemberEntity::isAlive), "not before the day is out");
		vacancies.fill(level, due);
		var back = level.getEntitiesOfClass(CourtMemberEntity.class, new AABB(home).inflate(2.0D), CourtMemberEntity::isAlive);
		h.assertTrue(back.size() == 1 && back.getFirst().role() == CourtRole.TIDEWARDEN && home.equals(back.getFirst().home()),
				"a new Tidewarden stands on the mark");
		h.assertTrue(vacancies.vacancies().stream().noneMatch(v -> v.home().equals(home)), "the vacancy is closed");
		vacancies.fill(level, due + CourtVacancies.REFILL_TICKS);
		h.assertTrue(level.getEntitiesOfClass(CourtMemberEntity.class, new AABB(home).inflate(2.0D), CourtMemberEntity::isAlive).size() == 1,
				"never more than the mark held");
		h.assertFalse(CourtVacancies.replaces(level.damageSources().playerAttack(h.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE))),
				"a player's blow leaves no vacancy");
		h.assertTrue(CourtVacancies.replaces(level.damageSources().fellOutOfWorld()), "the void does");
		back.getFirst().discard();
		h.succeed();
	}

	/** The Shared Wake's custom is the catch, a raw fish for the young; the Nursery tender gives once and takes members in. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theNurseryTakesTheCatchAndTheTenderTakesMembersIn(GameTestHelper h) {
		CourtMemberEntity tender = spawn(h, CourtRole.NURSERY_TENDER, "wake_tender_test");
		try (CapturedPlayer captured = player(h, 4)) {
			ServerPlayer player = captured.player;
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 2));
			h.assertFalse(CourtHospitality.handle(player, tender, CourtMemberDialogue.OFFER), "bread is the Garden's custom, not the nursery's");
			h.assertTrue(CourtHospitality.handle(player, tender, CourtMemberDialogue.GIFT), "the tender's welcome");
			h.assertTrue(player.getInventory().countItem(Items.COOKED_SALMON) == 4, "four cooked salmon for the swim");
			h.assertFalse(CourtHospitality.handle(player, tender, CourtMemberDialogue.GIFT), "the welcome is given once");
			h.assertTrue(CourtLife.standing(player, CourtHouse.SHARED_WAKE) == Standing.GUEST, "the welcome makes a guest");
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.COD, 3));
			DialogueTree tree = CourtMemberDialogue.forMember(tender, player);
			h.assertTrue(tree.getStartNode().options().stream().anyMatch(o -> CourtMemberDialogue.OFFER.equals(o.eventId())
					&& o.text().endsWith("offer_catch")), "the tender asks for the fish in hand");
			h.assertTrue(CourtHospitality.handle(player, tender, CourtMemberDialogue.OFFER), "a fish for the young");
			h.assertTrue(player.getMainHandItem().getCount() == 2, "one fish taken");
			h.assertTrue(CourtLife.standing(player, CourtHouse.SHARED_WAKE) == Standing.FRIEND, "the custom raises standing");
			h.assertFalse(CourtHospitality.handle(player, tender, CourtMemberDialogue.JOIN), "no joining before the fifth Degree");
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(5);
			h.assertTrue(CourtHospitality.handle(player, tender, CourtMemberDialogue.JOIN), "the Nursery tender takes the player in");
			h.assertTrue(CourtLife.house(player) == CourtHouse.SHARED_WAKE, "a member of the Shared Wake");
		}
		h.succeed();
	}

	/**
	 * One house's custom, welcome and joining, the way the Holding Stone and the Shared Wake test theirs: the Garden's
	 * food is refused as the custom, the gift is given once and makes a guest, the house's own custom in hand shows its
	 * option and raises standing, and the role that takes members in does so at the fifth Degree and not before.
	 */
	private static void customGiftAndJoin(GameTestHelper h, CourtHouse house, CourtMemberEntity custom, CourtMemberEntity elder,
			net.minecraft.world.item.Item giftItem, int giftCount, ItemStack offering, String option) {
		try (CapturedPlayer captured = player(h, 4)) {
			ServerPlayer player = captured.player;
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BREAD, 2));
			h.assertFalse(CourtHospitality.handle(player, custom, CourtMemberDialogue.OFFER), house + ": food is the Garden's custom");
			h.assertTrue(player.getMainHandItem().getCount() == 2, house + ": the bread is kept");
			h.assertTrue(CourtHospitality.handle(player, elder, CourtMemberDialogue.GIFT), house + ": the welcome");
			h.assertTrue(player.getInventory().countItem(giftItem) == giftCount, house + ": the welcome gift");
			h.assertFalse(new ItemStack(giftItem).is(CourtHospitality.customTag(house)),
					house + ": the welcome gift can't be handed straight back as the custom");
			h.assertFalse(CourtHospitality.handle(player, elder, CourtMemberDialogue.GIFT), house + ": the welcome is given once");
			h.assertTrue(CourtLife.standing(player, house) == Standing.GUEST, house + ": the welcome makes a guest");
			player.setItemInHand(InteractionHand.MAIN_HAND, offering.copy());
			DialogueTree tree = CourtMemberDialogue.forMember(custom, player);
			h.assertTrue(tree.getStartNode().options().stream().anyMatch(o -> CourtMemberDialogue.OFFER.equals(o.eventId())
					&& o.text().endsWith(option)), house + ": the custom is asked for by " + option);
			h.assertTrue(CourtHospitality.handle(player, custom, CourtMemberDialogue.OFFER), house + ": the custom is taken");
			h.assertTrue(player.getMainHandItem().getCount() == offering.getCount() - 1, house + ": one taken");
			h.assertTrue(CourtLife.standing(player, house) == Standing.FRIEND, house + ": the custom raises standing");
			h.assertFalse(CourtHospitality.handle(player, elder, CourtMemberDialogue.JOIN), house + ": no joining before the fifth Degree");
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(5);
			h.assertTrue(CourtHospitality.handle(player, elder, CourtMemberDialogue.JOIN), house + ": taken in");
			h.assertTrue(CourtLife.house(player) == house, house + ": a member");
		}
	}

	/** The Woven Current's custom is line for the camp; the Lamp-colony gives two leads and takes members in. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theLineCampTakesLineAndTheLampColonyTakesMembersIn(GameTestHelper h) {
		CourtMemberEntity linesman = spawn(h, CourtRole.LINESMAN, "line_tender");
		CourtMemberEntity colony = spawn(h, CourtRole.LAMP_COLONY, "pod_keeper");
		h.assertTrue(colony.isNoGravity() && !linesman.isNoGravity(), "the Lamp-colony hangs in the lines; the Linesman walks");
		customGiftAndJoin(h, CourtHouse.WOVEN_CURRENT, linesman, colony, Items.LEAD, 2, new ItemStack(Items.STRING, 3), "offer_line");
		h.succeed();
	}

	/** The Veiled Fold's custom is ink for the screen; the Host gives rest, the Veiled resident dark glass and a place. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theFoldTakesInkAndTheVeiledResidentTakesMembersIn(GameTestHelper h) {
		CourtMemberEntity host = spawn(h, CourtRole.HOST, "cleft_host");
		CourtMemberEntity resident = spawn(h, CourtRole.VEILED_RESIDENT, "cleft_veiled");
		try (CapturedPlayer captured = player(h, 4)) {
			captured.player.setHealth(6.0F);
			h.assertTrue(CourtHospitality.handle(captured.player, host, CourtMemberDialogue.REST), "the Host gives a room");
			h.assertTrue(captured.player.getHealth() > 6.0F, "rest heals");
			h.assertFalse(CourtHospitality.handle(captured.player, host, CourtMemberDialogue.GIFT), "the Host gives no gift");
		}
		customGiftAndJoin(h, CourtHouse.VEILED_FOLD, host, resident, Items.TINTED_GLASS, 4, new ItemStack(Items.INK_SAC, 2), "offer_ink");
		h.succeed();
	}

	/** The Still Bell's custom is wool for its warmth; the Bellwarden gives stores, rest and a place. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theBellTakesWoolAndTheBellwardenTakesMembersIn(GameTestHelper h) {
		CourtMemberEntity warden = spawn(h, CourtRole.BELLWARDEN, "bell_keeper");
		customGiftAndJoin(h, CourtHouse.STILL_BELL, warden, warden, Items.DRIED_KELP_BLOCK, 2, new ItemStack(Items.WHITE_WOOL, 2), "offer_wool");
		h.succeed();
	}

	/** Last Marrow's custom is remains for the fall; the Marrow-tender feeds, the Rooted elder gives bone meal and a place. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theFallTakesRemainsAndTheRootedElderTakesMembersIn(GameTestHelper h) {
		CourtMemberEntity tender = spawn(h, CourtRole.MARROW_TENDER, "marrow_carer");
		CourtMemberEntity elder = spawn(h, CourtRole.ROOTED_ELDER, "marrow_rooted");
		h.assertFalse(elder.isPushable(), "the Rooted elder is held in its bone");
		try (CapturedPlayer captured = player(h, 4)) {
			captured.player.getFoodData().setFoodLevel(8);
			h.assertTrue(CourtHospitality.handle(captured.player, tender, CourtMemberDialogue.FOOD), "the tender feeds from the fall");
			h.assertTrue(captured.player.getFoodData().getFoodLevel() == 8 + CourtHospitality.FOOD_HUNGER, "fed");
		}
		customGiftAndJoin(h, CourtHouse.LAST_MARROW, tender, elder, Items.BONE_MEAL, 12, new ItemStack(Items.BONE, 3), "offer_remains");
		h.succeed();
	}

	/** A Veiled resident left still folds away out of sight, and unfolds when it is spoken to. */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void theVeiledResidentFoldsAwayWhenStill(GameTestHelper h) {
		CourtMemberEntity resident = spawn(h, CourtRole.VEILED_RESIDENT, "cleft_veiled");
		resident.setNoAi(true);
		h.runAfterDelay(CourtMemberEntity.FOLD_TICKS + 25, () -> {
			h.assertTrue(resident.isWithdrawn(), "still, it has folded away");
			try (CapturedPlayer captured = player(h, 4)) {
				resident.speakTo(captured.player);
			}
			h.runAfterDelay(15, () -> {
				h.assertFalse(resident.isWithdrawn(), "spoken to, it unfolds");
				h.succeed();
			});
		});
	}

	/** A Sleeper hangs sealed where it is set, offers nothing and can't be pushed; its dialogue is only what is seen. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void theSleeperHangsSealedAndGivesNothing(GameTestHelper h) {
		CourtMemberEntity sleeper = spawn(h, CourtRole.SLEEPER, "bell_sleeper");
		h.assertTrue(sleeper.isNoGravity() && !sleeper.isPushable(), "the sleeper hangs on its hook");
		try (CapturedPlayer captured = player(h, 5)) {
			ServerPlayer player = captured.player;
			for (String event : CourtMemberDialogue.EVENTS) {
				h.assertFalse(CourtHospitality.handle(player, sleeper, event), "a sleeper gives nothing: " + event);
			}
			DialogueTree tree = CourtMemberDialogue.forMember(sleeper, player);
			h.assertTrue(tree.getStartNode().options().stream().allMatch(o -> o.eventId() == null), "no option asks the sleeper for anything");
		}
		h.succeed();
	}

	/** The Edge-swimmer defends the school as the Tidewarden defends the refuge: monsters only, never the player. */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void theEdgeSwimmerDefendsTheSchool(GameTestHelper h) {
		CourtMemberEntity swimmer = spawn(h, CourtRole.EDGE_SWIMMER, "school_test");
		Mob husk = EntityType.HUSK.create(h.getLevel());
		husk.moveTo(h.absoluteVec(STAND.getCenter()));
		h.getLevel().addFreshEntity(husk);
		float full = husk.getMaxHealth();
		h.succeedWhen(() -> {
			h.assertTrue(swimmer.getTarget() == husk, "the Edge-swimmer targets the monster");
			h.assertTrue(husk.getHealth() < full || !husk.isAlive(), "and strikes it");
		});
	}

	/**
	 * A school: four to six Edge-swimmers, one leader, the rest on their stations. In open water the leader swims its
	 * loop waypoint by waypoint and the others keep up with it; nobody drowns or sinks to the floor.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400, batch = "court_npc_school")
	public static void aSchoolSwimsItsLoopTogether(GameTestHelper h) {
		for (int x = 1; x <= 30; x++) for (int y = 1; y <= 10; y++) for (int z = 1; z <= 22; z++) {
			h.setBlock(new BlockPos(x, y, z), Blocks.WATER);
		}
		BlockPos centre = h.absolutePos(new BlockPos(15, 6, 11));
		List<CourtMemberEntity> school = CourtSchools.launch(h.getLevel(), centre, 8, h.getLevel().getRandom(),
				net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED);
		h.assertTrue(school.size() >= CourtSchoolRules.MIN_SIZE && school.size() <= CourtSchoolRules.MAX_SIZE,
				"four to six members: " + school.size());
		h.assertTrue(school.stream().filter(CourtMemberEntity::isSchoolLeader).count() == 1, "one leader");
		h.assertTrue(school.stream().allMatch(m -> m.school() != null && m.school().equals(school.getFirst().school())),
				"one school");
		CourtMemberEntity leader = school.stream().filter(CourtMemberEntity::isSchoolLeader).findFirst().orElseThrow();
		var start = leader.position();
		h.runAfterDelay(300, () -> {
			h.assertTrue(leader.schoolWaypoint() >= 2, "the leader has swum on round its loop: waypoint " + leader.schoolWaypoint());
			h.assertTrue(leader.position().distanceTo(start) > 3.0D || leader.schoolWaypoint() >= 3, "the leader moved");
			for (CourtMemberEntity member : school) {
				h.assertTrue(member.isAlive() && member.isInWater(), member.getId() + " is in the water");
				h.assertTrue(member.getAirSupply() == member.getMaxAirSupply(), "nobody runs out of air");
				h.assertTrue(member.distanceTo(leader) < 12.0F, "member " + member.schoolSlot() + " keeps up: "
						+ member.distanceTo(leader));
				h.assertTrue(member.getY() > h.absolutePos(BlockPos.ZERO).getY() + 2.0D, "no one sinks to the floor");
			}
			school.forEach(CourtMemberEntity::discard);
			h.succeed();
		});
	}

	/**
	 * A region has one school, ever: the nursery launches it the first time, and every later launch in the region is
	 * refused. Natural starts keep to Pelagic Ocean, to the regions that have one, and clear of the Seats.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 60, batch = "court_npc_school")
	public static void eachRegionHasOneSchool(GameTestHelper h) {
		for (int x = 1; x <= 4; x++) for (int y = 1; y <= 10; y++) for (int z = 1; z <= 4; z++) {
			h.setBlock(new BlockPos(x, y, z), Blocks.WATER);
		}
		var level = h.getLevel();
		CourtMemberEntity tender = spawn(h, CourtRole.NURSERY_TENDER, "wake_tender");
		CourtSchools schools = CourtSchools.get(level);
		long region = CourtSchoolRules.region(tender.home().getX(), tender.home().getZ());
		boolean fresh = !schools.claimed(region);
		List<CourtMemberEntity> first = schools.launchFromNursery(level, tender);
		if (fresh) {
			h.assertTrue(first.size() >= CourtSchoolRules.MIN_SIZE, "the nursery launches the region's school");
		}
		h.assertTrue(schools.claimed(region), "the region is taken");
		h.assertTrue(schools.launchFromNursery(level, tender).isEmpty(), "a second school in the region is refused");
		h.assertFalse(schools.claim(region), "and so is any other start there");
		try (CapturedPlayer captured = player(h, 5)) {
			h.assertTrue(schools.tryNatural(level, captured.player, null).isEmpty(), "no natural school without Pelagic worldgen");
		}
		first.forEach(CourtMemberEntity::discard);
		h.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theHeadTenderStandsWhereTheWalkwayLeavesTheRing(GameTestHelper h) {
		BlockPos centre = new BlockPos(0, 10, 0);
		var heads = List.of(new VentSanctumHeads.Spot(new BlockPos(5, 14, 0), VentSanctumHeads.Kind.WALK_TOP, Direction.NORTH),
				new VentSanctumHeads.Spot(new BlockPos(13, 10, 0), VentSanctumHeads.Kind.WALK_TOP, Direction.NORTH),
				new VentSanctumHeads.Spot(new BlockPos(13, 9, 0), VentSanctumHeads.Kind.WALK, Direction.NORTH),
				new VentSanctumHeads.Spot(new BlockPos(20, 30, 0), VentSanctumHeads.Kind.CROWN, Direction.NORTH));
		h.assertTrue(new BlockPos(13, 11, 0).equals(CourtResidentSpawner.threshold(heads, centre)),
				"on top of the walkway's ring end");
		h.succeed();
	}
}
