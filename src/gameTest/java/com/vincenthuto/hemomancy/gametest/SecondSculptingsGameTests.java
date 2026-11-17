package com.vincenthuto.hemomancy.gametest;

import static com.vincenthuto.hemomancy.gametest.FirstConversionsGameTests.absorbTick;
import static com.vincenthuto.hemomancy.gametest.FirstConversionsGameTests.aim;
import static com.vincenthuto.hemomancy.gametest.FirstConversionsGameTests.assertTheSigilWins;
import static com.vincenthuto.hemomancy.gametest.FirstConversionsGameTests.blood;
import static com.vincenthuto.hemomancy.gametest.FirstConversionsGameTests.feet;
import static com.vincenthuto.hemomancy.gametest.FirstConversionsGameTests.prepare;
import static com.vincenthuto.hemomancy.gametest.FirstConversionsGameTests.price;
import static com.vincenthuto.hemomancy.gametest.FirstConversionsGameTests.project;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.ManipulationCostLedger;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperCharge;
import com.vincenthuto.hemomancy.common.entity.summon.EntityIronWall;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.GlacialFreezeSculpting;
import com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceManager;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricBarrierManip;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricConstructShapes;
import com.vincenthuto.hemomancy.common.manipulation.ferric.FerricRampartSculpting;
import com.vincenthuto.hemomancy.common.manipulation.ferric.SanguineMendingSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingDispatcher;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingRules;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingSessions;
import com.vincenthuto.hemomancy.common.manipulation.verb.Verb;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandService;
import com.vincenthuto.hemomancy.common.manipulation.verb.strand.StrandSculpting;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Core-loop round two P4: the second wave of Sculptings with a real player through the real verb chains. Sanguine Mending
 * (a dropped tool, a golem, an anvil), Crimson Flame Conjuration's kindling (candles, a campfire, TNT), Furnace Veins
 * (a bank of furnaces burning only while held), Glacial Grasp (water frozen ahead of its plant strand; ice thawed by
 * Absorption), the two swept Ramparts, and Carrion Communion (Mortem Temper and health from the dead, never blood).
 * Each pays exactly its pinned price per pulse, refuses unpaid where the player may not build, falls through to the
 * plain verb where it does not claim, and loses to an awakened rite sigil.
 */
@GameTestHolder("sculpting_validation")
@PrefixGameTestTemplate(false)
public final class SecondSculptingsGameTests {
	private static final String EMPTY = "bastion/mobs/empty";
	private static final double BLOOD = 3000.0D;

	private SecondSculptingsGameTests() {
	}

	// ── Fixtures ──

	/** A stone floor at y 0 and clear air above it, over the rectangle. */
	private static void room(GameTestHelper helper, int fromX, int toX, int fromZ, int toZ, int height) {
		for (int x = fromX; x <= toX; x++) {
			for (int z = fromZ; z <= toZ; z++) {
				for (int y = 1; y <= height; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
				helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
			}
		}
	}

	/** A three by three pond of water sources at x 3..5, z 1..3, y 1, walled in stone on every side and below. */
	private static void pond(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		for (int x = 2; x <= 6; x++) {
			for (int z = 0; z <= 4; z++) {
				boolean wall = x == 2 || x == 6 || z == 0 || z == 4;
				helper.setBlock(new BlockPos(x, 1, z), wall ? Blocks.STONE : Blocks.WATER);
			}
		}
	}

	private static List<BlockPos> pondCells(GameTestHelper helper) {
		java.util.ArrayList<BlockPos> cells = new java.util.ArrayList<>();
		for (int x = 3; x <= 5; x++) for (int z = 1; z <= 3; z++) cells.add(helper.absolutePos(new BlockPos(x, 1, z)));
		return cells;
	}

	/** A point just inside the top of the floor block at (x, 0, z), so the pick strikes its top face. */
	private static Vec3 floorTop(GameTestHelper helper, int x, int z) {
		return Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(x, 1, z))).add(0.0D, -0.01D, 0.0D);
	}

	private static long count(GameTestHelper helper, List<BlockPos> cells, BlockState... any) {
		return cells.stream().filter(pos -> Arrays.stream(any).anyMatch(state -> helper.getLevel().getBlockState(pos).is(state.getBlock()))).count();
	}

	private static void assertUnpaidRefusal(GameTestHelper helper, CapturedPlayer captured, String manipulation, String line) {
		ServerPlayer player = captured.player;
		helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isPresent(),
				manipulation + " did not claim, so the guard was never reached");
		float[] temper = HemoCapabilityAccess.getBloodTemper(player).snapshot();
		double spent = project(player);
		helper.assertTrue(spent == 0.0D && blood(player) == BLOOD, manipulation + " was paid for where it was refused: " + blood(player));
		helper.assertTrue(Arrays.equals(temper, HemoCapabilityAccess.getBloodTemper(player).snapshot()),
				manipulation + " spent Temper where it was refused");
		helper.assertTrue(captured.said().contains(line), manipulation + "'s refusal was silent: " + captured.said());
		SculptingSessions.end(player, true);
	}

	private static void assertWholePulses(GameTestHelper helper, String what, double paid, double price, int expected) {
		helper.assertTrue(Math.abs(paid - expected * price) < 1.0E-6D,
				what + " did not pay exactly " + expected + " pulses of " + price + ": paid " + paid + " (" + paid / price + ")");
	}

	// ── Task 0: a narrow Sculpting claims ahead of the main one ──

	@GameTest(template = EMPTY, timeoutTicks = 320)
	public static void glacialGraspFreezesWaterAheadOfItsPlantAndTheIceMeltsBack(GameTestHelper helper) {
		pond(helper);
		List<BlockPos> cells = pondCells(helper);
		BlockPos middle = helper.absolutePos(new BlockPos(4, 1, 2));
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "glacial_grasp", BLOOD);
		aim(player, feet(helper, 1, 2), Vec3.atBottomCenterOf(middle).add(0.0D, 0.85D, 0.0D));
		var claim = SculptingDispatcher.currentClaim(player, Verb.PROJECT).orElse(null);
		helper.assertTrue(claim != null && claim.selected().sculpting() instanceof GlacialFreezeSculpting,
				"the freeze did not claim the water ahead of the plant: " + claim);
		double pulse = GlacialFreezeSculpting.BLOOD_PER_PULSE
				* ManipulationCostLedger.collect(player, ManipulationInit.getByName("glacial_grasp"), 1.0D).totalMultiplier();
		double spent = project(player);
		helper.assertTrue(Math.abs(spent - pulse) < 1.0E-6D && Math.abs(BLOOD - blood(player) - pulse) < 1.0E-6D,
				"the freeze did not pay one pulse of " + pulse + ": " + spent);
		helper.assertTrue(helper.getLevel().getBlockState(middle).is(BlockInit.frozen_cruor.get()), "the struck water did not freeze");
		long frozen = count(helper, cells, BlockInit.frozen_cruor.get().defaultBlockState());
		helper.assertTrue(frozen == GlacialFreezeSculpting.PER_PULSE, "one pulse froze " + frozen + " cells, not "
				+ GlacialFreezeSculpting.PER_PULSE);
		helper.assertTrue(!StrandService.isOpen(player) && !StrandService.isOut(player), "water opened the plant strand");
		SculptingSessions.end(player, true);
		captured.close();
		// The ice melts back to the water it froze, not to air.
		helper.succeedWhen(() -> helper.assertTrue(count(helper, cells, Blocks.WATER.defaultBlockState()) == cells.size(),
				"the pond has not melted back to water: " + count(helper, cells, Blocks.WATER.defaultBlockState())));
	}

	/** CQ-19: Glacial Grasp's R cast freezes a pond the way its held freeze does, so the ice melts back to water. */
	@GameTest(template = EMPTY, timeoutTicks = 320)
	public static void glacialGraspCastOnAPondMeltsBackToWater(GameTestHelper helper) {
		pond(helper);
		List<BlockPos> cells = pondCells(helper);
		BlockPos middle = helper.absolutePos(new BlockPos(4, 1, 2));
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "glacial_grasp", BLOOD);
		aim(player, feet(helper, 1, 2), Vec3.atBottomCenterOf(middle).add(0.0D, 0.85D, 0.0D));
		ManipulationInit.getByName("glacial_grasp").getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition());
		long frozen = count(helper, cells, BlockInit.frozen_cruor.get().defaultBlockState());
		captured.close();
		helper.assertTrue(frozen > 0, "the cast froze none of the pond");
		helper.succeedWhen(() -> helper.assertTrue(count(helper, cells, Blocks.WATER.defaultBlockState()) == cells.size(),
				"the pond has not melted back to water: " + count(helper, cells, Blocks.WATER.defaultBlockState())));
	}

	/** NQ-05-13: Rimebound Sentence's cage over a pond melts back to water, as every Frozen Cruor does. */
	@GameTest(template = EMPTY, timeoutTicks = 320)
	public static void rimeboundSentenceOnAPondMeltsBackToWater(GameTestHelper helper) {
		pond(helper);
		List<BlockPos> cells = pondCells(helper);
		net.minecraft.world.entity.monster.Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 1, 2));
		zombie.setNoAi(true);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "rimebound_sentence", BLOOD);
		aim(player, feet(helper, 1, 2), zombie.getBoundingBox().getCenter());
		ManipulationInit.getByName("rimebound_sentence").getAction(player, helper.getLevel(), ItemStack.EMPTY,
				player.blockPosition(), 70.0F);
		long frozen = count(helper, cells, BlockInit.frozen_cruor.get().defaultBlockState());
		captured.close();
		zombie.discard();
		helper.assertTrue(frozen > 0, "the sentence froze none of the pond");
		helper.succeedWhen(() -> helper.assertTrue(count(helper, cells, Blocks.WATER.defaultBlockState()) == cells.size(),
				"the pond has not melted back to water: " + count(helper, cells, Blocks.WATER.defaultBlockState())));
	}

	/** NQ-05-13: Glacial Rampart's R wall raised in a pond melts back to water. */
	@GameTest(template = EMPTY, timeoutTicks = 400)
	public static void glacialRampartCastInAPondMeltsBackToWater(GameTestHelper helper) {
		pond(helper);
		List<BlockPos> cells = pondCells(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "glacial_rampart", BLOOD);
		// Standing in the pond, so the aim reaches its floor over no wall.
		aim(player, feet(helper, 3, 2), floorTop(helper, 5, 2));
		ManipulationInit.getByName("glacial_rampart").getAction(player, helper.getLevel(), ItemStack.EMPTY, player.blockPosition());
		long frozen = count(helper, cells, BlockInit.frozen_cruor.get().defaultBlockState());
		captured.close();
		helper.assertTrue(frozen > 0, "the rampart froze none of the pond");
		helper.succeedWhen(() -> helper.assertTrue(count(helper, cells, Blocks.WATER.defaultBlockState()) == cells.size(),
				"the pond has not melted back to water: " + count(helper, cells, Blocks.WATER.defaultBlockState())));
	}

	/** NQ-05-13: a Will's Glacial Grasp, cast by an entity, freezes a pond that melts back to water. */
	@GameTest(template = EMPTY, timeoutTicks = 320)
	public static void anEntityCastGlacialGraspMeltsBackToWater(GameTestHelper helper) {
		pond(helper);
		List<BlockPos> cells = pondCells(helper);
		net.minecraft.world.entity.monster.Zombie caster = helper.spawn(EntityType.ZOMBIE, new BlockPos(4, 1, 2));
		caster.setNoAi(true);
		com.vincenthuto.hemomancy.common.manipulation.EntityCasts.glacialGrasp(
				com.vincenthuto.hemomancy.common.manipulation.ManipulationCastContext.forWill(caster, null, ItemStack.EMPTY));
		long frozen = count(helper, cells, BlockInit.frozen_cruor.get().defaultBlockState());
		caster.discard();
		helper.assertTrue(frozen > 0, "the entity's cast froze none of the pond");
		helper.succeedWhen(() -> helper.assertTrue(count(helper, cells, Blocks.WATER.defaultBlockState()) == cells.size(),
				"the pond has not melted back to water: " + count(helper, cells, Blocks.WATER.defaultBlockState())));
	}

	/** NQ-05-13: a Drudge's Glacial Rampart raised in a pond melts back to water. */
	@GameTest(template = EMPTY, timeoutTicks = 640)
	public static void aDrudgeRampartInAPondMeltsBackToWater(GameTestHelper helper) {
		pond(helper);
		List<BlockPos> cells = pondCells(helper);
		var drudge = EntityInit.drudge.get().create(helper.getLevel());
		drudge.setNoAi(true);
		Vec3 stand = feet(helper, 1, 2);
		drudge.moveTo(stand.x, stand.y, stand.z, 0.0F, 0.0F);
		helper.getLevel().addFreshEntity(drudge);
		drudge.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,
				Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 1, 2))));
		boolean raised = com.vincenthuto.hemomancy.common.manipulation.DrudgeActions.glacialRampart(drudge, helper.getLevel(),
				drudge.blockPosition(), 6);
		long frozen = count(helper, cells, BlockInit.frozen_cruor.get().defaultBlockState());
		drudge.discard();
		helper.assertTrue(raised && frozen > 0, "the Drudge's rampart froze none of the pond (raised " + raised + ")");
		helper.succeedWhen(() -> helper.assertTrue(count(helper, cells, Blocks.WATER.defaultBlockState()) == cells.size(),
				"the pond has not melted back to water: " + count(helper, cells, Blocks.WATER.defaultBlockState())));
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void glacialGraspClaimsNoBareGround(GameTestHelper helper) {
		// Glacial Grasp closes a hand of ice on a creature (2026-11-12); it no longer sinks a plant into the ground.
		room(helper, 0, 7, 0, 5, 4);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "glacial_grasp", BLOOD);
			aim(player, feet(helper, 1, 2), floorTop(helper, 4, 2));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "Glacial Grasp claims bare ground");
			project(player);
			helper.assertTrue(!StrandService.isOpen(player), "a strand opened on the ground under Glacial Grasp");
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	/** Frozen Cruor with no open water beside it is not the freeze's, and nothing else of Glacial Grasp's takes it. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void glacialGraspLeavesDryIceAlone(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos ice = helper.absolutePos(new BlockPos(4, 0, 2));
		helper.getLevel().setBlock(ice, BlockInit.frozen_cruor.get().defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "glacial_grasp", BLOOD);
			aim(player, feet(helper, 1, 2), floorTop(helper, 4, 2));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "the freeze took dry ice");
			project(player);
			helper.assertTrue(!StrandService.isOpen(player), "a strand opened on dry ice under Glacial Grasp");
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void theNarrowFreezeCostsAndWorksAsPinned(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "glacial_grasp", 1000.0D);
			var sculptings = SculptingDispatcher.sculptings(player, Verb.PROJECT);
			helper.assertTrue(sculptings.size() == 2, "Glacial Grasp has " + sculptings.size() + " Sculptings of Projection, not 2");
			VerbSculpting freeze = sculptings.getFirst().sculpting();
			helper.assertTrue(freeze instanceof GlacialFreezeSculpting, "the freeze is not tried first");
			helper.assertTrue(sculptings.get(1).sculpting() == SculptingDispatcher.selected(player, Verb.PROJECT).orElseThrow().sculpting(),
					"the plant is no longer the main Sculpting");
			double perSecond = SculptingRules.bloodPerSecond(freeze.bloodPerPulse(), freeze.cadence());
			helper.assertTrue(Math.abs(perSecond - 10.0D) < 1.0E-6D && freeze.cadence() == 10 && freeze.reachScale() == 1.5D,
					"the freeze drifted: " + perSecond + " mL/s, cadence " + freeze.cadence() + ", reach " + freeze.reachScale());
			helper.assertTrue(SculptingDispatcher.sculpts(ManipulationInit.getByName("glacial_grasp"), Verb.ABSORB),
					"Glacial Grasp has no Sculpting of Absorption");
		}
		helper.succeed();
	}

	// ── Glacial Grasp: Absorption thaws ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void glacialGraspThawsCruorAndIceUnderAbsorption(GameTestHelper helper) {
		pond(helper);
		BlockPos middle = helper.absolutePos(new BlockPos(4, 1, 2));
		BlockPos west = helper.absolutePos(new BlockPos(3, 1, 2));
		BlockPos east = helper.absolutePos(new BlockPos(5, 1, 2));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "glacial_grasp", BLOOD);
			helper.assertTrue(TemporaryIceManager.placeFor(player, middle, BlockInit.frozen_cruor.get().defaultBlockState(), 400)
					&& TemporaryIceManager.placeFor(player, west, BlockInit.frozen_cruor.get().defaultBlockState(), 400),
					"the fixture ice was not placed");
			helper.getLevel().setBlock(east, Blocks.ICE.defaultBlockState(), 3);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(middle).add(0.0D, 0.49D, 0.0D));
			double pulse = price(player, "glacial_grasp", Verb.ABSORB);
			absorbTick(player);
			assertWholePulses(helper, "the thaw", BLOOD - blood(player), pulse, 1);
			for (BlockPos pos : List.of(middle, west, east)) {
				helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.WATER),
						"ice at " + pos + " did not thaw to water: " + helper.getLevel().getBlockState(pos));
			}
			player.stopUsingItem();
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	// ── Sanguine Mending ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void mendingRepairsADroppedTool(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		ServerLevel level = helper.getLevel();
		Vec3 ground = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
		ItemStack pick = new ItemStack(Items.IRON_PICKAXE);
		pick.setDamageValue(100);
		ItemEntity dropped = new ItemEntity(level, ground.x, ground.y, ground.z, pick);
		dropped.setDeltaMovement(Vec3.ZERO);
		dropped.setNoGravity(true);
		level.addFreshEntity(dropped);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "sanguine_mending", BLOOD);
			aim(player, feet(helper, 1, 2), dropped.getBoundingBox().getCenter());
			double pulse = price(player, "sanguine_mending", Verb.PROJECT);
			project(player);
			assertWholePulses(helper, "mending a tool", BLOOD - blood(player), pulse, 1);
			helper.assertTrue(dropped.getItem().getDamageValue() == 100 - 10,
					"one pulse by hand did not mend 10 durability: " + dropped.getItem().getDamageValue());
			SculptingSessions.end(player, true);
		} finally {
			dropped.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void mendingPicksAlliesAndPeacefulWildGolems(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos at = helper.absolutePos(new BlockPos(3, 1, 3));
		List<net.minecraft.world.entity.Entity> spawned = new java.util.ArrayList<>();
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			IronGolem wild = injured(level, EntityType.IRON_GOLEM.create(level), at, spawned);
			helper.assertTrue(SanguineMendingSculpting.mendable(player, wild), "a wild golem fighting no one was not mendable");
			IronGolem hunting = injured(level, EntityType.IRON_GOLEM.create(level), at, spawned);
			hunting.setTarget(player);
			helper.assertFalse(SanguineMendingSculpting.mendable(player, hunting), "a wild golem hunting a player was mendable");
			var own = injured(level, com.vincenthuto.hemomancy.common.init.EntityInit.drudge.get().create(level), at, spawned);
			own.setOwnerUUID(player.getUUID());
			helper.assertTrue(SanguineMendingSculpting.mendable(player, own), "the caster's own Drudge was not mendable");
			var enemy = injured(level, com.vincenthuto.hemomancy.common.init.EntityInit.drudge.get().create(level), at, spawned);
			enemy.setOwnerUUID(java.util.UUID.randomUUID());
			helper.assertFalse(SanguineMendingSculpting.mendable(player, enemy), "another player's Drudge was mendable");
			var pillar = injured(level, com.vincenthuto.hemomancy.common.init.EntityInit.iron_pillar.get().create(level), at, spawned);
			pillar.creator = player;
			helper.assertTrue(SanguineMendingSculpting.mendable(player, pillar), "the caster's own iron pillar was not mendable");
		} finally {
			spawned.forEach(net.minecraft.world.entity.Entity::discard);
		}
		helper.succeed();
	}

	private static <T extends net.minecraft.world.entity.LivingEntity> T injured(ServerLevel level, T entity, BlockPos at,
			List<net.minecraft.world.entity.Entity> spawned) {
		entity.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 0.0F, 0.0F);
		if (entity instanceof net.minecraft.world.entity.Mob mob) mob.setNoAi(true);
		level.addFreshEntity(entity);
		spawned.add(entity);
		entity.setHealth(entity.getMaxHealth() / 2.0F);
		return entity;
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void mendingHealsAnInjuredGolem(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 5);
		ServerLevel level = helper.getLevel();
		IronGolem golem = EntityType.IRON_GOLEM.create(level);
		BlockPos at = helper.absolutePos(new BlockPos(5, 1, 2));
		golem.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 90.0F, 0.0F);
		golem.setNoAi(true);
		level.addFreshEntity(golem);
		golem.setHealth(50.0F);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "sanguine_mending", BLOOD);
			aim(player, feet(helper, 1, 2), golem.getBoundingBox().getCenter());
			double pulse = price(player, "sanguine_mending", Verb.PROJECT);
			project(player);
			assertWholePulses(helper, "mending a golem", BLOOD - blood(player), pulse, 1);
			helper.assertTrue(Math.abs(golem.getHealth() - 53.0F) < 1.0E-4F, "one pulse did not heal the golem by 3: " + golem.getHealth());
			SculptingSessions.end(player, true);
		} finally {
			golem.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 160)
	public static void mendingBeatsAChippedAnvilBackAfterEightPulses(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos anvil = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(anvil, Blocks.CHIPPED_ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.WEST), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "sanguine_mending", BLOOD);
		Vec3 feet = feet(helper, 1, 2);
		double pulse = price(player, "sanguine_mending", Verb.PROJECT);
		helper.onEachTick(() -> {
			if (!helper.getLevel().getBlockState(anvil).is(Blocks.CHIPPED_ANVIL)) return;
			aim(player, feet, Vec3.atCenterOf(anvil).add(0.0D, 0.49D, 0.0D));
			project(player);
		});
		helper.succeedWhen(() -> {
			BlockState now = helper.getLevel().getBlockState(anvil);
			helper.assertTrue(now.is(Blocks.ANVIL), "the anvil is still " + now);
			helper.assertTrue(now.getValue(AnvilBlock.FACING) == Direction.WEST, "the mended anvil turned");
			assertWholePulses(helper, "mending an anvil a stage", BLOOD - blood(player), pulse, SanguineMendingSculpting.PULSES_PER_STAGE);
			SculptingSessions.end(player, true);
			captured.close();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void mendingRefusesAnAnvilBeforeAnyBloodWhereThePlayerMayNotBuild(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos anvil = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(anvil, Blocks.CHIPPED_ANVIL.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			prepare(captured.player, "sanguine_mending", BLOOD);
			captured.player.setGameMode(GameType.ADVENTURE);
			aim(captured.player, feet(helper, 1, 2), Vec3.atCenterOf(anvil).add(0.0D, 0.49D, 0.0D));
			assertUnpaidRefusal(helper, captured, "sanguine_mending", StaffMiningGameTests.REFUSED);
			helper.assertTrue(helper.getLevel().getBlockState(anvil).is(Blocks.CHIPPED_ANVIL), "a refused anvil changed");
		}
		helper.succeed();
	}

	/** CN-13: a mend already running stops, unpaid, once the player may not build. */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void mendingStopsMidStageWhenThePlayerMayNoLongerBuild(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos anvil = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(anvil, Blocks.CHIPPED_ANVIL.defaultBlockState(), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "sanguine_mending", BLOOD);
		Vec3 feet = feet(helper, 1, 2);
		int[] tick = {0};
		double[] afterFirst = {Double.NaN};
		helper.onEachTick(() -> {
			if (tick[0]++ >= 40) return;
			aim(player, feet, Vec3.atCenterOf(anvil).add(0.0D, 0.49D, 0.0D));
			double spent = project(player);
			if (Double.isNaN(afterFirst[0]) && spent > 0.0D) {
				afterFirst[0] = blood(player);
				player.setGameMode(GameType.ADVENTURE);
			}
		});
		helper.runAtTickTime(45, () -> {
			try {
				helper.assertFalse(Double.isNaN(afterFirst[0]), "the mend never paid its first pulse");
				helper.assertTrue(blood(player) == afterFirst[0], "a mend kept paying after the player lost the right to build: "
						+ (afterFirst[0] - blood(player)) + " mL more");
				helper.assertTrue(helper.getLevel().getBlockState(anvil).is(Blocks.CHIPPED_ANVIL), "a refused anvil changed");
				helper.assertTrue(captured.said().contains(StaffMiningGameTests.REFUSED), "the refusal was silent: " + captured.said());
				SculptingSessions.end(player, true);
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Crimson Flame Conjuration: kindling ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void kindlingLightsACampfireAndACandleButNotATorch(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		ServerLevel level = helper.getLevel();
		BlockPos campfire = helper.absolutePos(new BlockPos(4, 1, 2));
		BlockPos candle = helper.absolutePos(new BlockPos(4, 1, 3));
		BlockPos torch = helper.absolutePos(new BlockPos(4, 1, 1));
		level.setBlock(campfire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 3);
		level.setBlock(candle, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, false), 3);
		level.setBlock(torch, Blocks.TORCH.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "crimson_flame_conjuration", BLOOD);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(campfire).add(0.0D, -0.2D, 0.0D));
			double pulse = price(player, "crimson_flame_conjuration", Verb.PROJECT);
			project(player);
			assertWholePulses(helper, "kindling", BLOOD - blood(player), pulse, 1);
			helper.assertTrue(level.getBlockState(campfire).getValue(CampfireBlock.LIT), "the campfire was not kindled");
			helper.assertTrue(level.getBlockState(candle).getValue(CandleBlock.LIT), "the candle was not kindled");
			helper.assertTrue(level.getBlockState(torch).is(Blocks.TORCH), "the torch changed");
			SculptingSessions.end(player, true);
		} finally {
			level.setBlock(campfire, Blocks.AIR.defaultBlockState(), 3);
			level.setBlock(candle, Blocks.AIR.defaultBlockState(), 3);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void kindlingPrimesTntAsThePlayers(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		ServerLevel level = helper.getLevel();
		BlockPos tnt = helper.absolutePos(new BlockPos(4, 1, 2));
		level.setBlock(tnt, Blocks.TNT.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "crimson_flame_conjuration", BLOOD);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(tnt));
			project(player);
			List<PrimedTnt> primed = level.getEntitiesOfClass(PrimedTnt.class, new AABB(tnt).inflate(1.0D));
			try {
				helper.assertTrue(level.getBlockState(tnt).isAir(), "the TNT block was not taken up");
				helper.assertTrue(primed.size() == 1 && primed.getFirst().getOwner() == player,
						"the TNT was not primed as the player's: " + primed.size());
			} finally {
				primed.forEach(Entity::discard);
			}
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void kindlingLeavesAnIronBrazierToPlainProjection(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos brazier = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(brazier, BlockInit.iron_brazier.get().defaultBlockState(), 3);
		helper.getLevel().setBlock(brazier.north(), Blocks.CANDLE.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			prepare(captured.player, "crimson_flame_conjuration", BLOOD);
			// The kindling is the narrow Sculpting tried ahead of the plant strand (round two P5).
			VerbSculpting kindling = SculptingDispatcher.sculptings(captured.player, Verb.PROJECT).getFirst().sculpting();
			var struck = new VerbTarget.Block(new BlockHitResult(Vec3.atCenterOf(brazier), Direction.UP, brazier, false),
					helper.getLevel().getBlockState(brazier));
			helper.assertTrue(!kindling.claims(captured.player, struck),
					"the kindling claimed an Iron Brazier, whose ignition is plain Projection's");
			var beside = helper.absolutePos(new BlockPos(4, 0, 2));
			helper.assertTrue(kindling.claims(captured.player, new VerbTarget.Block(
							new BlockHitResult(Vec3.atCenterOf(beside), Direction.UP, beside, false), helper.getLevel().getBlockState(beside))),
					"the unlit candle beside the brazier is not in reach, so the test proves nothing");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void kindlingRefusesBeforeAnyBloodWhereThePlayerMayNotBuild(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos campfire = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(campfire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			prepare(captured.player, "crimson_flame_conjuration", BLOOD);
			captured.player.setGameMode(GameType.ADVENTURE);
			aim(captured.player, feet(helper, 1, 2), Vec3.atCenterOf(campfire).add(0.0D, -0.2D, 0.0D));
			assertUnpaidRefusal(helper, captured, "crimson_flame_conjuration", StaffMiningGameTests.REFUSED);
			helper.assertTrue(!helper.getLevel().getBlockState(campfire).getValue(CampfireBlock.LIT), "a refused campfire was lit");
		}
		helper.succeed();
	}

	// ── Furnace Veins ──

	private static AbstractFurnaceBlockEntity furnace(GameTestHelper helper, BlockPos pos, ItemStack input) {
		helper.getLevel().setBlock(pos, Blocks.BLAST_FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.WEST), 3);
		AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) helper.getLevel().getBlockEntity(pos);
		if (!input.isEmpty()) furnace.setItem(0, input);
		return furnace;
	}

	@GameTest(template = EMPTY, timeoutTicks = 320)
	public static void furnaceVeinsCooksABankWhileHeldAndGoesOutOnRelease(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos struck = helper.absolutePos(new BlockPos(4, 1, 2));
		BlockPos beside = helper.absolutePos(new BlockPos(4, 1, 3));
		AbstractFurnaceBlockEntity furnace = furnace(helper, struck, new ItemStack(Items.RAW_IRON, 2));
		furnace(helper, beside, new ItemStack(Items.RAW_GOLD, 2));
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "furnace_veins", BLOOD);
		Vec3 feet = feet(helper, 1, 2);
		double pulse = price(player, "furnace_veins", Verb.PROJECT);
		boolean[] cooked = {false};
		boolean[] besideLit = {false};
		double[] firstPaid = {-1.0D};
		int[] releasedAt = {-1};
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (helper.getLevel().getBlockState(beside).getValue(AbstractFurnaceBlock.LIT)) besideLit[0] = true;
			if (cooked[0]) return;
			if (furnace.getItem(2).is(Items.IRON_INGOT)) {
				cooked[0] = true;
				releasedAt[0] = t;
				SculptingSessions.end(player, true);
				return;
			}
			aim(player, feet, Vec3.atCenterOf(struck));
			project(player);
			if (firstPaid[0] < 0.0D && BLOOD - blood(player) > 0.0D) firstPaid[0] = BLOOD - blood(player);
		});
		helper.succeedWhen(() -> {
			helper.assertTrue(cooked[0], "the held stream has not cooked an ingot yet");
			// Priced per furnace fed (CQ-15): the first pulse fed both furnaces of the bank.
			helper.assertTrue(Math.abs(firstPaid[0] - 2.0D * pulse) < 1.0E-6D,
					"the first pulse on a bank of two cost " + firstPaid[0] + ", one furnace costs " + pulse);
			helper.assertTrue(besideLit[0], "the furnace beside it in the bank never lit");
			helper.assertTrue(!helper.getLevel().getBlockState(struck).getValue(AbstractFurnaceBlock.LIT),
					"the furnace still burns after the stream was let go");
			helper.assertTrue(tick[0] - releasedAt[0] <= 20, "the fire outlived the stream by more than a second");
			double paid = BLOOD - blood(player);
			long pulses = Math.round(paid / pulse);
			helper.assertTrue(pulses > 0 && Math.abs(paid - pulses * pulse) < 1.0E-6D, "the bank did not pay whole pulses: " + paid);
			helper.assertTrue(furnace.getItem(1).isEmpty(), "fuel appeared in the furnace");
			captured.close();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void furnaceVeinsRefusesAnEmptyBankUnpaid(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos struck = helper.absolutePos(new BlockPos(4, 1, 2));
		furnace(helper, struck, ItemStack.EMPTY);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			prepare(captured.player, "furnace_veins", BLOOD);
			aim(captured.player, feet(helper, 1, 2), Vec3.atCenterOf(struck));
			assertUnpaidRefusal(helper, captured, "furnace_veins", "Nothing in these furnaces can be cooked.");
			helper.assertTrue(!helper.getLevel().getBlockState(struck).getValue(AbstractFurnaceBlock.LIT), "an empty furnace was lit");
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void furnaceVeinsSelectedStillFillsAStation(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos station = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(station, BlockInit.enzymatic_scriptorium.get().defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "furnace_veins", BLOOD);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(station));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.PROJECT).isEmpty(), "Furnace Veins claimed a station");
			for (int i = 0; i < 3; i++) project(player);
			double held = ReservoirBlood.settled(helper.getLevel().getBlockEntity(station)).getBloodVolume();
			helper.assertTrue(held > 0.0D, "the station did not fill with Furnace Veins selected");
		}
		helper.succeed();
	}

	// ── The swept Ramparts ──

	/** A floor to sweep along z 4 from x 4 to 7, a body standing at x 7, and the player at (5, 0) looking down the room. */
	private static ArmorStand sweepFloor(GameTestHelper helper) {
		room(helper, 0, 9, 0, 7, 5);
		ArmorStand stand = EntityType.ARMOR_STAND.create(helper.getLevel());
		BlockPos at = helper.absolutePos(new BlockPos(7, 1, 4));
		stand.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D, 0.0F, 0.0F);
		stand.setNoGravity(true);
		helper.getLevel().addFreshEntity(stand);
		return stand;
	}

	/** Holds Projection: four ticks on the floor at x 4, then on the floor at x 7, a sweep three cells long. */
	private static void sweep(GameTestHelper helper, ServerPlayer player, int[] tick, int until) {
		Vec3 feet = feet(helper, 5, 0);
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t > until) return;
			aim(player, feet, floorTop(helper, t < 5 ? 4 : 7, 4));
			project(player);
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void glacialRampartSweepsAJoinedWallAndClosesOverNobody(GameTestHelper helper) {
		ArmorStand body = sweepFloor(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "glacial_rampart", BLOOD);
		double pulse = price(player, "glacial_rampart", Verb.PROJECT);
		int[] tick = {0};
		sweep(helper, player, tick, 7);
		helper.runAtTickTime(10, () -> {
			try {
				for (int x = 4; x <= 6; x++) {
					for (int y = 1; y <= 3; y++) {
						BlockPos pos = helper.absolutePos(new BlockPos(x, y, 4));
						helper.assertTrue(helper.getLevel().getBlockState(pos).is(BlockInit.frozen_cruor.get()),
								"the wall has a gap at x " + x + ", y " + y + ": " + helper.getLevel().getBlockState(pos));
					}
				}
				for (int y = 1; y <= 2; y++) {
					helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(7, y, 4))).is(BlockInit.frozen_cruor.get()),
							"the wall closed over the body standing at x 7");
				}
				assertWholePulses(helper, "the ice wall", BLOOD - blood(player), pulse, 2);
			} finally {
				SculptingSessions.end(player, true);
				TemporaryIceManager.clearOwned(helper.getLevel(), new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0D), player.getUUID());
				body.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	/**
	 * Held on its own wall the ice wall raises nothing toward the player and keeps the stretch it is held on standing,
	 * so the pulse the player pays for is never wasted.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void glacialRampartHeldOnItsOwnWallKeepsItStanding(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 5);
		List<BlockPos> column = List.of(helper.absolutePos(new BlockPos(4, 1, 2)), helper.absolutePos(new BlockPos(4, 2, 2)),
				helper.absolutePos(new BlockPos(4, 3, 2)));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "glacial_rampart", BLOOD);
			for (BlockPos pos : column) {
				helper.assertTrue(TemporaryIceManager.placeFor(player, pos, BlockInit.frozen_cruor.get().defaultBlockState(), 20),
						"the fixture wall was not raised at " + pos);
			}
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(column.get(1)).add(-0.49D, 0.0D, 0.0D));
			var claim = SculptingDispatcher.currentClaim(player, Verb.PROJECT).orElse(null);
			helper.assertTrue(claim != null && claim.target() instanceof VerbTarget.Block block && block.state().is(BlockInit.frozen_cruor.get()),
					"the rampart did not claim its own ice: " + claim);
			double pulse = price(player, "glacial_rampart", Verb.PROJECT);
			long now = helper.getLevel().getGameTime();
			project(player);
			assertWholePulses(helper, "holding the ice wall", BLOOD - blood(player), pulse, 1);
			for (BlockPos pos : column) {
				var entry = com.vincenthuto.hemomancy.common.manipulation.congeatio.TemporaryIceSavedData.get(helper.getLevel()).get(pos);
				helper.assertTrue(entry != null && entry.meltsAt() >= now + 200,
						"the held wall was not kept standing at " + pos + ": " + (entry == null ? "untracked" : entry.meltsAt() - now));
			}
			for (int y = 1; y <= 3; y++) {
				helper.assertTrue(!helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(3, y, 2))).is(BlockInit.frozen_cruor.get()),
						"the wall crept back toward the player at y " + y);
			}
			SculptingSessions.end(player, true);
			TemporaryIceManager.clearOwned(helper.getLevel(), new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0D), player.getUUID());
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void ferricRampartSweepsAJoinedWallAndRaisesNothingOnABody(GameTestHelper helper) {
		ArmorStand body = sweepFloor(helper);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "ferric_rampart", BLOOD);
		double pulse = price(player, "ferric_rampart", Verb.PROJECT);
		int[] tick = {0};
		AABB room = new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(16.0D);
		sweep(helper, player, tick, 12);
		helper.runAtTickTime(16, () -> {
			List<EntityIronWall> segments = helper.getLevel().getEntitiesOfClass(EntityIronWall.class, room,
					wall -> wall.constructKind() == FerricConstructShapes.Kind.SEGMENT
							&& wall.ownerId().map(player.getUUID()::equals).orElse(false));
			try {
				helper.assertTrue(segments.size() == 3, "a three-cell sweep raised " + segments.size() + " segments, not 3");
				for (int x = 4; x <= 6; x++) {
					BlockPos cell = helper.absolutePos(new BlockPos(x, 1, 4));
					helper.assertTrue(segments.stream().anyMatch(wall -> wall.blockPosition().equals(cell)), "no segment at x " + x);
				}
				helper.assertTrue(segments.stream().allMatch(wall -> wall.constructFacing().getAxis() == Direction.Axis.Z
								&& Math.abs(wall.getBoundingBox().getXsize() - 1.0D) < 1.0E-6D),
						"a segment's long side does not lie along the sweep");
				assertWholePulses(helper, "the iron wall", BLOOD - blood(player), pulse, 2);
				helper.assertTrue(FerricRampartSculpting.standing(player) == 3, "the standing count is not the player's three");
			} finally {
				SculptingSessions.end(player, true);
				segments.forEach(Entity::discard);
				body.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void ferricRampartRefusesUnpaidPastTwelveSegments(GameTestHelper helper) {
		room(helper, 0, 9, 0, 9, 4);
		ServerLevel level = helper.getLevel();
		List<EntityIronWall> standing = new java.util.ArrayList<>();
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "ferric_rampart", BLOOD);
			for (int i = 0; i < FerricRampartSculpting.MAX_SEGMENTS; i++) {
				EntityIronWall wall = new EntityIronWall(EntityInit.ferric_rampart.get(), level, player);
				Vec3 at = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(i % 6 + 2, 1, 8 + i / 6)));
				wall.moveTo(at.x, at.y, at.z, 0.0F, 0.0F);
				wall.configure(player, FerricConstructShapes.Kind.SEGMENT, Direction.SOUTH, FerricBarrierManip.RAMPART_DURATION_TICKS);
				level.addFreshEntity(wall);
				standing.add(wall);
			}
			aim(player, feet(helper, 1, 2), floorTop(helper, 4, 2));
			assertUnpaidRefusal(helper, captured, "ferric_rampart", FerricRampartSculpting.MAX_SEGMENTS + " blocks of your iron already stand.");
		} finally {
			standing.forEach(Entity::discard);
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void bothRampartsRefuseBeforeAnyBloodWhereThePlayerMayNotBuild(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		for (String rampart : List.of("glacial_rampart", "ferric_rampart")) {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				prepare(captured.player, rampart, BLOOD);
				captured.player.setGameMode(GameType.ADVENTURE);
				aim(captured.player, feet(helper, 1, 2), floorTop(helper, 4, 2));
				assertUnpaidRefusal(helper, captured, rampart, StaffMiningGameTests.REFUSED);
			}
		}
		helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(4, 1, 2))).isAir(), "a refused wall rose");
		helper.assertTrue(helper.getLevel().getEntitiesOfClass(EntityIronWall.class,
				new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(12.0D)).isEmpty(), "a refused iron wall rose");
		helper.succeed();
	}

	// ── Carrion Communion: Absorption on the dead ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void carrionDrawsMortemTemperAndHealthFromFleshButNoBlood(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		ServerLevel level = helper.getLevel();
		Vec3 ground = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(4, 1, 2)));
		ItemEntity flesh = new ItemEntity(level, ground.x, ground.y, ground.z, new ItemStack(Items.ROTTEN_FLESH, 3));
		flesh.setDeltaMovement(Vec3.ZERO);
		flesh.setNoGravity(true);
		level.addFreshEntity(flesh);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "carrion_communion", BLOOD);
			var temper = HemoCapabilityAccess.getBloodTemper(player);
			temper.set(EnumBloodTemper.MORTEM, 10.0F, TemperCharge.cap(player, EnumBloodTemper.MORTEM));
			player.setHealth(10.0F);
			aim(player, feet(helper, 1, 2), flesh.getBoundingBox().getCenter());
			double pulse = price(player, "carrion_communion", Verb.ABSORB);
			helper.assertTrue(!com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CARRION_DRAWN), "the fixture had already drawn from the dead");
			absorbTick(player);
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CARRION_DRAWN), "drawing from flesh did not grant What the Dead Give");
			assertWholePulses(helper, "drawing from flesh", BLOOD - blood(player), pulse, 1);
			helper.assertTrue(flesh.getItem().getCount() == 2, "one pulse did not draw one flesh: " + flesh.getItem().getCount());
			helper.assertTrue(Math.abs(temper.get(EnumBloodTemper.MORTEM) - 11.75F) < 1.0E-4F,
					"Mortem did not rise by the flesh's 2 less the pulse's quarter (an 8 mL/s form, LP-14): " + temper.get(EnumBloodTemper.MORTEM));
			helper.assertTrue(Math.abs(player.getHealth() - 11.0F) < 1.0E-4F, "the flesh gave no health: " + player.getHealth());
			player.stopUsingItem();
			SculptingSessions.end(player, true);
		} finally {
			flesh.discard();
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void carrionDrawsABoneBlockWholeAfterFourPulsesWithNoDrop(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos bone = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(bone, Blocks.BONE_BLOCK.defaultBlockState(), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "carrion_communion", BLOOD);
		var temper = HemoCapabilityAccess.getBloodTemper(player);
		temper.set(EnumBloodTemper.MORTEM, 10.0F, TemperCharge.cap(player, EnumBloodTemper.MORTEM));
		Vec3 feet = feet(helper, 1, 2);
		double pulse = price(player, "carrion_communion", Verb.ABSORB);
		helper.onEachTick(() -> {
			if (!helper.getLevel().getBlockState(bone).is(Blocks.BONE_BLOCK)) return;
			aim(player, feet, Vec3.atCenterOf(bone).add(0.0D, 0.49D, 0.0D));
			absorbTick(player);
		});
		helper.succeedWhen(() -> {
			helper.assertTrue(helper.getLevel().getBlockState(bone).isAir(), "the bone block still stands");
			assertWholePulses(helper, "drawing a bone block", BLOOD - blood(player), pulse, 4);
			helper.assertTrue(Math.abs(temper.get(EnumBloodTemper.MORTEM) - (10.0F + 9.0F - 4 * 0.25F)) < 1.0E-4F,
					"a bone block did not give its 9 Mortem: " + temper.get(EnumBloodTemper.MORTEM));
			helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(bone).inflate(2.0D)).isEmpty(),
					"the drawn bone block dropped something");
			player.stopUsingItem();
			SculptingSessions.end(player, true);
			captured.close();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void carrionRefusesABoneBlockBeforeAnyBloodWhereThePlayerMayNotBuild(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos bone = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(bone, Blocks.BONE_BLOCK.defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, "carrion_communion", BLOOD);
			player.setGameMode(GameType.ADVENTURE);
			aim(player, feet(helper, 1, 2), Vec3.atCenterOf(bone).add(0.0D, 0.49D, 0.0D));
			helper.assertTrue(SculptingDispatcher.currentClaim(player, Verb.ABSORB).isPresent(), "Carrion did not claim the bone block");
			float[] before = HemoCapabilityAccess.getBloodTemper(player).snapshot();
			absorbTick(player);
			helper.assertTrue(blood(player) == BLOOD, "a refused draw was paid for: " + blood(player));
			helper.assertTrue(Arrays.equals(before, HemoCapabilityAccess.getBloodTemper(player).snapshot()), "a refused draw moved Temper");
			helper.assertTrue(captured.said().contains(StaffMiningGameTests.REFUSED), "the refusal was silent: " + captured.said());
			helper.assertTrue(helper.getLevel().getBlockState(bone).is(Blocks.BONE_BLOCK), "a refused bone block was drawn");
			player.stopUsingItem();
			SculptingSessions.end(player, true);
		}
		helper.succeed();
	}

	/** CN-13: a draw already running stops, unpaid, once the player may not build. */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void carrionStopsMidDrawWhenThePlayerMayNoLongerBuild(GameTestHelper helper) {
		room(helper, 0, 7, 0, 5, 4);
		BlockPos bone = helper.absolutePos(new BlockPos(4, 1, 2));
		helper.getLevel().setBlock(bone, Blocks.BONE_BLOCK.defaultBlockState(), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "carrion_communion", BLOOD);
		Vec3 feet = feet(helper, 1, 2);
		int[] tick = {0};
		double[] afterFirst = {Double.NaN};
		helper.onEachTick(() -> {
			if (tick[0]++ >= 40) return;
			aim(player, feet, Vec3.atCenterOf(bone).add(0.0D, 0.49D, 0.0D));
			absorbTick(player);
			if (Double.isNaN(afterFirst[0]) && blood(player) < BLOOD) {
				afterFirst[0] = blood(player);
				player.setGameMode(GameType.ADVENTURE);
			}
		});
		helper.runAtTickTime(45, () -> {
			try {
				helper.assertFalse(Double.isNaN(afterFirst[0]), "the draw never paid its first pulse");
				helper.assertTrue(blood(player) == afterFirst[0], "a draw kept paying after the player lost the right to build: "
						+ (afterFirst[0] - blood(player)) + " mL more");
				helper.assertTrue(helper.getLevel().getBlockState(bone).is(Blocks.BONE_BLOCK), "a refused bone block was drawn");
				helper.assertTrue(captured.said().contains(StaffMiningGameTests.REFUSED), "the refusal was silent: " + captured.said());
				player.stopUsingItem();
				SculptingSessions.end(player, true);
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── An awakened rite sigil still takes the blood first ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesSanguineMending(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "sanguine_mending",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.CHIPPED_ANVIL.defaultBlockState(), 3);
				},
				player -> helper.getLevel().getBlockState(at[0]).is(Blocks.CHIPPED_ANVIL)
						&& helper.getLevel().getBlockState(at[0].offset(0, 1, 3)).is(Blocks.CHIPPED_ANVIL));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesTheKindling(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "crimson_flame_conjuration",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 3);
				},
				player -> !helper.getLevel().getBlockState(at[0]).getValue(CampfireBlock.LIT)
						&& !helper.getLevel().getBlockState(at[0].offset(0, 1, 3)).getValue(CampfireBlock.LIT));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesFurnaceVeins(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "furnace_veins",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.FURNACE.defaultBlockState(), 3);
					if (helper.getLevel().getBlockEntity(floor) instanceof AbstractFurnaceBlockEntity furnace) {
						furnace.setItem(0, new ItemStack(Items.RAW_IRON));
					}
				},
				player -> !helper.getLevel().getBlockState(at[0]).getValue(AbstractFurnaceBlock.LIT)
						&& !helper.getLevel().getBlockState(at[0].offset(0, 1, 3)).getValue(AbstractFurnaceBlock.LIT));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesTheFreeze(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "glacial_grasp",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.WATER.defaultBlockState(), 3);
				},
				// Water behind the sigil too: the freeze is all Glacial Grasp claims of a block now.
				floor -> helper.getLevel().setBlock(floor, Blocks.WATER.defaultBlockState(), 2),
				player -> helper.getLevel().getBlockState(at[0]).is(Blocks.WATER) && !StrandService.isOpen(player));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesTheGlacialRampart(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "glacial_rampart",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
				},
				player -> !helper.getLevel().getBlockState(at[0].above()).is(BlockInit.frozen_cruor.get()));
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void anAwakenedSigilStillTakesTheFerricRampart(GameTestHelper helper) {
		BlockPos[] at = new BlockPos[1];
		assertTheSigilWins(helper, "ferric_rampart",
				floor -> {
					if (at[0] == null) at[0] = floor;
					helper.getLevel().setBlock(floor, Blocks.STONE.defaultBlockState(), 3);
				},
				player -> FerricRampartSculpting.standing(player) == 0);
		helper.succeed();
	}
}
