package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.BloodGutterBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.VenousBasinBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodGougeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.BloodStainBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.LivingVeinBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.ground.LuxTraceBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.IKnownManipulations;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.ManipulationInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.ManipLevel;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.tile.CoagulationRules;
import com.vincenthuto.hemomancy.common.tile.ReservoirBlood;
import com.vincenthuto.hemomancy.common.tile.harbinger.decoration.VenousBasinBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.ground.CatalystFeederBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.ground.VeinNodeBlockEntity;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.TemperRules;
import com.vincenthuto.hemomancy.common.world.ground.DarkFieldRules;
import com.vincenthuto.hemomancy.common.world.ground.DarkFields;
import com.vincenthuto.hemomancy.common.world.ground.GougeCatalysts;
import com.vincenthuto.hemomancy.mixin.core.FurnaceFuelAccessor;
import com.vincenthuto.hemomancy.common.world.ground.GroundFunction;
import com.vincenthuto.hemomancy.common.world.ground.GroundNetwork;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunRules;
import com.vincenthuto.hemomancy.common.world.ground.GroundRunSavedData;
import com.vincenthuto.hemomancy.common.world.ground.VeinLedger;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Core-loop Track 6, the ground network: plain Projection stains what nothing else claims; stains age and wash; a pulse
 * runs a gouge run, bounded and staggered; each catalyst gives the lit run its function; a feeder keeps a lit run lit
 * but never lights a dark one; Absorption takes every tier back; a vein grows from its node within its owner's
 * allowance, moves blood only while its owner is near and withers untended; a worked basin feeds its gutters for the
 * fresh window and clots; arena gutters stay out of it; and the four Sculptings cut, light, snuff and draw. Real players
 * through the real verb chains.
 */
@GameTestHolder("ground_network_validation")
@PrefixGameTestTemplate(false)
public final class GroundNetworkGameTests {
	/** The suite's own 32 by 24 empty room: the runs here are wider than the vanilla bastion template. */
	private static final String EMPTY = "empty";
	/**
	 * The feeder, draw and water tests of B09 run in their own batch. In the default batches they shifted which tests
	 * share a batch, and two Sculpting session tests (anvil mending, cast presentation) failed beside them.
	 */
	private static final String DRAW_AND_WATER = "ground_network_draw_and_water";

	private GroundNetworkGameTests() {
	}

	// ── Fixtures ──

	/** A clear floor of polished andesite (no infusion takes it) from x0..x1, z0..z1, three blocks of air above. */
	private static void floor(GameTestHelper helper, int x0, int x1, int z0, int z1) {
		for (int x = x0; x <= x1; x++) {
			for (int z = z0; z <= z1; z++) {
				helper.setBlock(new BlockPos(x, 0, z), Blocks.POLISHED_ANDESITE);
				for (int y = 1; y <= 3; y++) helper.setBlock(new BlockPos(x, y, z), Blocks.AIR);
			}
		}
	}

	/** Active blood in a large body, both verbs known, {@code manipulation} selected (or nothing), Temper carried. */
	private static void prepare(ServerPlayer player, String manipulation, double blood) {
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(5_000.0D);
		volume.setBloodVolume(blood);
		IKnownManipulations known = HemoCapabilityAccess.getKnownManipulations(player);
		known.getKnownManips().put(ManipulationInit.blood_projection.get(), new ManipLevel(1, 0));
		known.getKnownManips().put(ManipulationInit.blood_absorption.get(), new ManipLevel(1, 0));
		if (manipulation != null) {
			BloodManipulation registered = ManipulationInit.getByName(manipulation);
			known.getKnownManips().put(registered, new ManipLevel(1, 0));
			known.setEquippedManipNames(new ArrayList<>(List.of(manipulation)));
			known.setSelectedManip(registered);
			var temperament = HemoCapabilityAccess.getBloodTemperament(player);
			if (temperament.getAlignmentByTemper(registered.getTend()) < registered.getAlignLevel()) {
				temperament.setTemperAlignment(registered.getTend(), (float) registered.getAlignLevel() + 1.0F);
			}
		} else {
			known.setSelectedManip(BloodManipulation.BLANK);
		}
		TemperFixtures.carry(player);
	}

	private static void aim(ServerPlayer player, Vec3 feet, Vec3 at) {
		player.setPos(feet.x, feet.y, feet.z);
		player.lookAt(EntityAnchorArgument.Anchor.EYES, at);
		player.setOldPosAndRot();
		player.yHeadRotO = player.getYHeadRot();
	}

	private static Vec3 feet(GameTestHelper helper, int x, int z) {
		return Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(x, 1, z)));
	}

	/** The middle of the top face of the floor block under the cell at (x, 1, z). */
	private static Vec3 floorTop(GameTestHelper helper, int x, int z) {
		return Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(x, 1, z))).add(0.0D, 0.001D, 0.0D);
	}

	private static double project(ServerPlayer player) {
		return BloodProjectionItem.projectFromEntity(player.level(), player, 5.0D, 100.0D);
	}

	private static void absorbTick(ServerPlayer player) {
		if (!player.isUsingItem() || !player.getUseItem().is(ItemInit.blood_absorption.get())) {
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ItemInit.blood_absorption.get()));
			player.startUsingItem(InteractionHand.MAIN_HAND);
		}
		ItemStack hand = player.getUseItem();
		hand.getItem().onUseTick(player.level(), player, hand, player.getUseItemRemainingTicks());
	}

	private static double blood(ServerPlayer player) {
		return HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
	}

	private static BlockState face(Block block, Direction face) {
		return block.defaultBlockState().setValue(MultifaceBlock.getFaceProperty(face), true);
	}

	/** A dormant gouge on the floor of the cell (x, 1, z). */
	private static BlockPos gouge(GameTestHelper helper, int x, int z) {
		BlockPos cell = helper.absolutePos(new BlockPos(x, 1, z));
		helper.getLevel().setBlock(cell, face(BlockInit.blood_gouge.get(), Direction.DOWN), 3);
		return cell;
	}

	private static boolean lit(ServerLevel level, BlockPos cell, GroundFunction function) {
		BlockState state = level.getBlockState(cell);
		return state.getBlock() instanceof BloodGougeBlock && state.getValue(BloodGougeBlock.LIT)
				&& state.getValue(BloodGougeBlock.FUNCTION) == function;
	}

	// ── Stains ──

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void plainProjectionStainsWhatNothingElseClaims(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 1_000.0D);
			aim(player, feet(helper, 1, 2), floorTop(helper, 4, 2));
			for (int i = 0; i < 6; i++) project(player);
			BlockState stain = helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(4, 1, 2)));
			helper.assertTrue(stain.is(BlockInit.blood_stain.get()) && MultifaceBlock.hasFace(stain, Direction.DOWN)
					&& stain.getValue(BloodStainBlock.AGE) == GroundRunRules.WET, "no wet stain on the struck floor: " + stain);
			helper.assertTrue(Math.abs(1_000.0D - blood(player) - GroundRunRules.STAIN_ML) < 1.0E-6D,
					"a wet stain already there took more blood: paid " + (1_000.0D - blood(player)));
		}
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aStainAgesThenWearsAwayAndWaterWashesIt(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = helper.absolutePos(new BlockPos(2, 1, 2));
		level.setBlock(cell, face(BlockInit.blood_stain.get(), Direction.DOWN), 3);
		for (int expected : new int[] {GroundRunRules.TACKY, GroundRunRules.DRY}) {
			GroundNetwork.ageStain(level, cell, level.getBlockState(cell), 0.1F);
			helper.assertTrue(level.getBlockState(cell).getValue(BloodStainBlock.AGE) == expected, "the stain did not age to " + expected);
		}
		GroundNetwork.ageStain(level, cell, level.getBlockState(cell), 0.1F);
		helper.assertTrue(level.getBlockState(cell).isAir(), "a dry stain did not wear away");
		BlockPos wet = helper.absolutePos(new BlockPos(4, 1, 2));
		level.setBlock(wet, face(BlockInit.blood_stain.get(), Direction.DOWN), 3);
		level.setBlock(wet.east(), Blocks.WATER.defaultBlockState(), 3);
		GroundNetwork.ageStain(level, wet, level.getBlockState(wet), 0.99F);
		helper.assertTrue(level.getBlockState(wet).isAir(), "water beside a stain did not wash it");
		level.setBlock(wet.east(), Blocks.AIR.defaultBlockState(), 3);
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void absorptionTakesBackAWetStainAndCleansADryOne(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos wet = helper.absolutePos(new BlockPos(4, 1, 2));
		BlockPos dry = helper.absolutePos(new BlockPos(4, 1, 3));
		level.setBlock(wet, face(BlockInit.blood_stain.get(), Direction.DOWN), 3);
		level.setBlock(dry, face(BlockInit.blood_stain.get(), Direction.DOWN).setValue(BloodStainBlock.AGE, GroundRunRules.DRY), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, null, 1_000.0D);
		aim(player, feet(helper, 1, 2), floorTop(helper, 4, 2));
		absorbTick(player);
		helper.assertTrue(level.getBlockState(wet).isAir(), "Absorption left the wet stain");
		helper.assertTrue(Math.abs(blood(player) - 1_000.0D - GroundRunRules.STAIN_REFUND_ML) < 1.0E-6D,
				"a wet stain did not give back part of its blood: " + blood(player));
		helper.runAtTickTime(10, () -> {
			try {
				aim(player, feet(helper, 1, 3), floorTop(helper, 4, 3));
				double before = blood(player);
				absorbTick(player);
				helper.assertTrue(level.getBlockState(dry).isAir(), "Absorption did not clean the dry stain");
				helper.assertTrue(blood(player) == before, "a dry stain gave blood back");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aCreatureStandingOnAStainIsDrawnBeforeTheStain(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = helper.absolutePos(new BlockPos(4, 1, 2));
		level.setBlock(cell, face(BlockInit.blood_stain.get(), Direction.DOWN), 3);
		Pig pig = EntityType.PIG.create(level);
		pig.moveTo(cell.getX() + 0.5D, cell.getY(), cell.getZ() + 0.5D);
		pig.setNoAi(true);
		level.addFreshEntity(pig);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, null, 1_000.0D);
		aim(player, feet(helper, 1, 2), pig.getBoundingBox().getCenter());
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < 30) absorbTick(player);
		});
		helper.runAtTickTime(35, () -> {
			try {
				helper.assertTrue(level.getBlockState(cell).is(BlockInit.blood_stain.get()), "Absorption took the stain under the pig");
				helper.assertTrue(pig.getHealth() < pig.getMaxHealth(), "Absorption did not draw from the pig on the stain");
			} finally {
				pig.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	// ── Gouges: the pulse ──

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void aPulseRunsTheRunStaggeredAndStopsAtSixtyFour(GameTestHelper helper) {
		floor(helper, 0, 9, 0, 8);
		List<BlockPos> cells = new ArrayList<>();
		for (int x = 1; x <= 9; x++) for (int z = 1; z <= 8; z++) cells.add(gouge(helper, x, z));
		ServerLevel level = helper.getLevel();
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, null, 1_000.0D);
		aim(player, feet(helper, 1, 0), floorTop(helper, 1, 1));
		double paid = project(player);
		helper.assertTrue(paid == GroundRunRules.PULSE_ML && Math.abs(1_000.0D - blood(player) - GroundRunRules.PULSE_ML) < 1.0E-6D,
				"a pulse did not cost its price: returned " + paid + ", paid " + (1_000.0D - blood(player)));
		helper.runAtTickTime(3, () -> {
			long early = cells.stream().filter(cell -> lit(level, cell, GroundFunction.NONE)).count();
			helper.assertTrue(early > 0 && early < GroundRunRules.MAX_RUN, "the run did not light outward a few cells a tick: " + early);
		});
		helper.runAtTickTime(50, () -> {
			try {
				long lit = cells.stream().filter(cell -> lit(level, cell, GroundFunction.NONE)).count();
				helper.assertTrue(lit == GroundRunRules.MAX_RUN, "a pulse lit " + lit + " cells, bounded at 64");
				helper.assertTrue(level.getBrightness(LightLayer.BLOCK, cells.getFirst()) >= GroundFunction.NONE.light() - 1,
						"a bare run gave no faint glow");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void glowstoneInTheOffHandLightsTheRunForReal(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		List<BlockPos> cells = new ArrayList<>();
		for (int x = 2; x <= 5; x++) cells.add(gouge(helper, x, 2));
		ServerLevel level = helper.getLevel();
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, null, 1_000.0D);
		player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLOWSTONE_DUST, 2));
		aim(player, feet(helper, 0, 2), floorTop(helper, 2, 2));
		project(player);
		helper.assertTrue(player.getOffhandItem().getCount() == 1, "the glowstone was not dissolved into the run");
		helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_CUT_AND_LIT),
				"lighting a run did not grant Cut and Lit (core-loop Track 11)");
		helper.runAtTickTime(30, () -> {
			try {
				for (BlockPos cell : cells) {
					helper.assertTrue(lit(level, cell, GroundFunction.LIGHT), "a cell did not light with the glowstone's function");
					helper.assertTrue(level.getBrightness(LightLayer.BLOCK, cell) == GroundFunction.LIGHT.light(),
							"a light run gave light " + level.getBrightness(LightLayer.BLOCK, cell));
				}
				GroundRunSavedData.Lit clock = GroundRunSavedData.get(level).get(cells.getFirst());
				helper.assertTrue(clock != null && clock.until() - level.getGameTime() > 6_000L,
						"glowstone should keep the run lit for minutes");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aBarePulseNeverStripsALitRunsFunction(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos first = gouge(helper, 3, 2);
		gouge(helper, 4, 2);
		GroundNetwork.pulse(level, first, GroundFunction.LIGHT, 2_400, null);
		helper.runAtTickTime(10, () -> {
			try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = captured.player;
				prepare(player, null, 1_000.0D);
				aim(player, feet(helper, 1, 2), floorTop(helper, 3, 2));
				project(player);
				GroundRunSavedData.Lit clock = GroundRunSavedData.get(level).get(first);
				helper.assertTrue(clock != null && clock.function() == GroundFunction.LIGHT
								&& clock.until() - level.getGameTime() > GroundRunRules.BARE_LIT_TICKS,
						"a bare pulse stripped or shortened a light run: " + clock);
			}
		});
		helper.runAtTickTime(30, () -> {
			helper.assertTrue(lit(level, first, GroundFunction.LIGHT), "the run lost its light after a bare pulse");
			helper.succeed();
		});
	}

	/** A hot run of three cells from (2, 2) with raw iron on the first and an unlit campfire past the last; returns {first, campfire}. */
	private static BlockPos[] hotRun(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos first = gouge(helper, 2, 2);
		gouge(helper, 3, 2);
		BlockPos last = gouge(helper, 4, 2);
		BlockPos campfire = last.east();
		level.setBlock(campfire, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, false), 3);
		ItemEntity ore = new ItemEntity(level, first.getX() + 0.5D, first.getY() + 0.05D, first.getZ() + 0.5D, new ItemStack(Items.RAW_IRON, 3));
		ore.setDeltaMovement(Vec3.ZERO);
		ore.setNoGravity(true);
		level.addFreshEntity(ore);
		return new BlockPos[] {first, campfire};
	}

	private static int ingotsOn(ServerLevel level, BlockPos cell) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(cell).inflate(1.0D), item -> item.getItem().is(Items.IRON_INGOT))
				.stream().mapToInt(item -> item.getItem().getCount()).sum();
	}

	/** A player in the server's UUID lookup, standing in this test's level, so a run they own can find them. */
	private static CapturedPlayer runOwner(GameTestHelper helper, GameType mode) {
		CapturedPlayer owner = CapturedPlayer.create(helper).online();
		owner.player.setGameMode(mode);
		Vec3 stand = feet(helper, 1, 1);
		owner.player.setPos(stand.x, stand.y, stand.z);
		return owner;
	}

	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void aHotRunSmeltsWhatLiesOnItAndLightsACampfire(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] run = hotRun(helper);
		BlockPos first = run[0];
		BlockPos campfire = run[1];
		CapturedPlayer owner = runOwner(helper, GameType.SURVIVAL);
		GroundNetwork.pulse(level, first, GroundFunction.HOT, 400, null, owner.player.getUUID());
		helper.startSequence().thenWaitUntil(() -> {
			helper.assertTrue(ingotsOn(level, first) >= 1, "a hot run smelted nothing lying on it");
			helper.assertTrue(level.getBlockState(campfire).getValue(CampfireBlock.LIT), "a hot run did not light the campfire it touches");
		}).thenExecute(() -> {
			owner.close();
			level.getEntitiesOfClass(ItemEntity.class, new AABB(first).inflate(3.0D)).forEach(ItemEntity::discard);
			level.setBlock(campfire, Blocks.AIR.defaultBlockState(), 3);
		}).thenSucceed();
	}

	/**
	 * Lighting is a block edit for the run's owner: a hot run owned by a player who may not build here smelts what lies
	 * on it (items are not blocks) but leaves the campfire and a candle beside it unlit.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void aHotRunLightsNothingWhereItsOwnerMayNotBuild(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] run = hotRun(helper);
		BlockPos first = run[0];
		BlockPos campfire = run[1];
		BlockPos candle = first.west();
		level.setBlock(candle, Blocks.CANDLE.defaultBlockState(), 3);
		CapturedPlayer owner = runOwner(helper, GameType.ADVENTURE);
		GroundNetwork.pulse(level, first, GroundFunction.HOT, 400, null, owner.player.getUUID());
		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(ingotsOn(level, first) >= 1, "a hot run smelted nothing lying on it"))
				.thenIdle(2 * GroundRunRules.EFFECT_INTERVAL + 1).thenExecute(() -> {
					try {
						helper.assertTrue(!level.getBlockState(campfire).getValue(CampfireBlock.LIT),
								"a hot run lit a campfire for an owner who may not build there");
						helper.assertTrue(!level.getBlockState(candle).getValue(CandleBlock.LIT),
								"a hot run lit a candle for an owner who may not build there");
					} finally {
						owner.close();
						level.getEntitiesOfClass(ItemEntity.class, new AABB(first).inflate(3.0D)).forEach(ItemEntity::discard);
						level.setBlock(campfire, Blocks.AIR.defaultBlockState(), 3);
						level.setBlock(candle, Blocks.AIR.defaultBlockState(), 3);
					}
				}).thenSucceed();
	}

	/** A run nobody owns (a test or scenario pulse) still smelts but lights nothing. */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void anOwnerlessHotRunLightsNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] run = hotRun(helper);
		BlockPos first = run[0];
		BlockPos campfire = run[1];
		BlockPos furnace = loadedFurnace(level, first.north(), Blocks.FURNACE, Items.RAW_IRON);
		GroundNetwork.pulse(level, first, GroundFunction.HOT, 400, null);
		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(ingotsOn(level, first) >= 1, "a hot run smelted nothing lying on it"))
				.thenIdle(2 * GroundRunRules.EFFECT_INTERVAL + 1).thenExecute(() -> {
					try {
						helper.assertTrue(!level.getBlockState(campfire).getValue(CampfireBlock.LIT), "a run nobody owns lit a campfire");
						helper.assertTrue(!level.getBlockState(furnace).getValue(AbstractFurnaceBlock.LIT) && litTime(level, furnace) == 0,
								"a run nobody owns burned a furnace: lit time " + litTime(level, furnace));
					} finally {
						level.getEntitiesOfClass(ItemEntity.class, new AABB(first).inflate(3.0D)).forEach(ItemEntity::discard);
						level.setBlock(campfire, Blocks.AIR.defaultBlockState(), 3);
						clearFurnace(level, furnace);
					}
				}).thenSucceed();
	}

	// -- Hot runs and furnaces (round two P8) --

	/** A furnace-like block at {@code pos} with {@code input} to cook (or nothing) and nothing in its fuel slot. */
	private static BlockPos loadedFurnace(ServerLevel level, BlockPos pos, Block block, net.minecraft.world.item.Item input) {
		level.setBlock(pos, block.defaultBlockState(), 3);
		if (input != null && level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace) furnace.setItem(0, new ItemStack(input, 4));
		return pos;
	}

	private static int litTime(ServerLevel level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace ? ((FurnaceFuelAccessor) furnace).hemomancy$getLitTime() : -1;
	}

	private static boolean burning(ServerLevel level, BlockPos pos) {
		return level.getBlockState(pos).getValue(AbstractFurnaceBlock.LIT) && litTime(level, pos) > 0;
	}

	private static boolean cooked(ServerLevel level, BlockPos pos) {
		return level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace && !furnace.getItem(2).isEmpty();
	}

	private static void clearFurnace(ServerLevel level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof AbstractFurnaceBlockEntity furnace) furnace.clearContent();
		level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
	}

	/**
	 * A hot run burns the furnace, smoker and blast furnace beside it, for an owner who may build there, through their
	 * own burn time (no fuel in the slot): each lights and cooks what it holds. An empty furnace beside the run is never
	 * lit.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 400)
	public static void aHotRunBurnsTheFurnacesBesideIt(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] run = hotRun(helper);
		BlockPos first = run[0];
		BlockPos campfire = run[1];
		BlockPos furnace = loadedFurnace(level, first.north(), Blocks.FURNACE, Items.RAW_IRON);
		BlockPos smoker = loadedFurnace(level, first.east().north(), Blocks.SMOKER, Items.BEEF);
		BlockPos blast = loadedFurnace(level, first.east().east().north(), Blocks.BLAST_FURNACE, Items.RAW_IRON);
		BlockPos empty = loadedFurnace(level, first.east().south(), Blocks.FURNACE, null);
		CapturedPlayer owner = runOwner(helper, GameType.SURVIVAL);
		GroundNetwork.pulse(level, first, GroundFunction.HOT, 600, null, owner.player.getUUID());
		helper.startSequence().thenWaitUntil(() -> {
			helper.assertTrue(burning(level, furnace), "the furnace beside a hot run is not burning");
			helper.assertTrue(burning(level, smoker), "the smoker beside a hot run is not burning");
			helper.assertTrue(burning(level, blast), "the blast furnace beside a hot run is not burning");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(owner.player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_STOKED), "burning a furnace on a hot run did not grant Stoked");
		}).thenWaitUntil(() -> {
			helper.assertTrue(cooked(level, smoker), "the smoker cooked nothing on a hot run's heat");
			helper.assertTrue(cooked(level, blast), "the blast furnace smelted nothing on a hot run's heat");
			helper.assertTrue(cooked(level, furnace), "the furnace smelted nothing on a hot run's heat");
		}).thenExecute(() -> {
			try {
				helper.assertTrue(!level.getBlockState(empty).getValue(AbstractFurnaceBlock.LIT) && litTime(level, empty) == 0,
						"a hot run lit a furnace with nothing to cook");
			} finally {
				owner.close();
				level.getEntitiesOfClass(ItemEntity.class, new AABB(first).inflate(3.0D)).forEach(ItemEntity::discard);
				level.setBlock(campfire, Blocks.AIR.defaultBlockState(), 3);
				for (BlockPos pos : new BlockPos[] {furnace, smoker, blast, empty}) clearFurnace(level, pos);
			}
		}).thenSucceed();
	}

	/** Burning a furnace is an edit for the run's owner: an owner who may not build here burns none. */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void aHotRunLightsNoFurnaceWhereItsOwnerMayNotBuild(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] run = hotRun(helper);
		BlockPos first = run[0];
		BlockPos campfire = run[1];
		BlockPos furnace = loadedFurnace(level, first.north(), Blocks.FURNACE, Items.RAW_IRON);
		CapturedPlayer owner = runOwner(helper, GameType.ADVENTURE);
		GroundNetwork.pulse(level, first, GroundFunction.HOT, 400, null, owner.player.getUUID());
		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(ingotsOn(level, first) >= 1, "a hot run smelted nothing lying on it"))
				.thenIdle(2 * GroundRunRules.EFFECT_INTERVAL + 1).thenExecute(() -> {
					try {
						helper.assertTrue(!level.getBlockState(furnace).getValue(AbstractFurnaceBlock.LIT) && litTime(level, furnace) == 0,
								"a hot run burned a furnace for an owner who may not build there: lit time " + litTime(level, furnace));
					} finally {
						owner.close();
						level.getEntitiesOfClass(ItemEntity.class, new AABB(first).inflate(3.0D)).forEach(ItemEntity::discard);
						level.setBlock(campfire, Blocks.AIR.defaultBlockState(), 3);
						clearFurnace(level, furnace);
					}
				}).thenSucceed();
	}

	/** Nothing is banked in the furnace: within two seconds of the run going dark, it goes out. */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void aFurnaceGoesOutWhenTheRunGoesDark(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] run = hotRun(helper);
		BlockPos first = run[0];
		BlockPos campfire = run[1];
		BlockPos furnace = loadedFurnace(level, first.north(), Blocks.FURNACE, Items.RAW_IRON);
		CapturedPlayer owner = runOwner(helper, GameType.SURVIVAL);
		GroundNetwork.pulse(level, first, GroundFunction.HOT, 60, null, owner.player.getUUID());
		long[] darkAt = {-1L};
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(burning(level, furnace), "the furnace never caught"))
				.thenWaitUntil(() -> helper.assertTrue(!level.getBlockState(first).getValue(BloodGougeBlock.LIT), "the run never went dark"))
				.thenExecute(() -> darkAt[0] = helper.getTick())
				.thenWaitUntil(() -> helper.assertTrue(!level.getBlockState(furnace).getValue(AbstractFurnaceBlock.LIT),
						"the furnace kept burning after the run went dark"))
				.thenExecute(() -> {
					try {
						long after = helper.getTick() - darkAt[0];
						helper.assertTrue(after <= 40, "the furnace burned on " + after + " ticks after the run went dark");
					} finally {
						owner.close();
						level.getEntitiesOfClass(ItemEntity.class, new AABB(first).inflate(3.0D)).forEach(ItemEntity::discard);
						level.setBlock(campfire, Blocks.AIR.defaultBlockState(), 3);
						clearFurnace(level, furnace);
					}
				}).thenSucceed();
	}

	/** A cold cell at (3, 2) with still water east of it and young wheat south of it; returns {cell, water, crop}. */
	private static BlockPos[] coldRun(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 4);
		ServerLevel level = helper.getLevel();
		BlockPos cell = gouge(helper, 3, 2);
		BlockPos water = cell.east();
		level.setBlock(water, Blocks.WATER.defaultBlockState(), 3);
		BlockPos crop = cell.south();
		level.setBlock(crop.below(), Blocks.FARMLAND.defaultBlockState(), 3);
		level.setBlock(crop, Blocks.WHEAT.defaultBlockState(), 3);
		helper.assertTrue(CommonHooks.canCropGrow(level, crop, level.getBlockState(crop), true), "the crop could not grow before the cold");
		return new BlockPos[] {cell, water, crop};
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aColdRunFreezesStillWaterAndHoldsCropsBack(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] run = coldRun(helper);
		BlockPos water = run[1];
		BlockPos crop = run[2];
		CapturedPlayer owner = runOwner(helper, GameType.SURVIVAL);
		GroundNetwork.pulse(level, run[0], GroundFunction.COLD, 400, null, owner.player.getUUID());
		helper.startSequence().thenWaitUntil(() -> {
			helper.assertTrue(level.getBlockState(water).is(Blocks.FROSTED_ICE), "still water beside a cold run did not freeze");
			helper.assertTrue(!CommonHooks.canCropGrow(level, crop, level.getBlockState(crop), true), "a crop beside a cold run still grows");
		}).thenExecute(() -> {
			owner.close();
			level.setBlock(water, Blocks.AIR.defaultBlockState(), 3);
		}).thenSucceed();
	}

	/** Freezing is a block edit for the run's owner: one who may not build leaves the water, though crops are still held back. */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void aColdRunFreezesNothingWhereItsOwnerMayNotBuild(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] run = coldRun(helper);
		BlockPos cell = run[0];
		BlockPos water = run[1];
		BlockPos crop = run[2];
		CapturedPlayer owner = runOwner(helper, GameType.ADVENTURE);
		GroundNetwork.pulse(level, cell, GroundFunction.COLD, 400, null, owner.player.getUUID());
		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(lit(level, cell, GroundFunction.COLD), "the cold run never lit"))
				.thenIdle(2 * GroundRunRules.EFFECT_INTERVAL + 1).thenExecute(() -> {
					try {
						helper.assertTrue(level.getBlockState(water).is(Blocks.WATER),
								"a cold run froze water for an owner who may not build there: " + level.getBlockState(water));
						helper.assertTrue(!CommonHooks.canCropGrow(level, crop, level.getBlockState(crop), true),
								"a crop beside a cold run still grows");
					} finally {
						owner.close();
						level.setBlock(water, Blocks.AIR.defaultBlockState(), 3);
					}
				}).thenSucceed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void aPullingRunCarriesItemsTowardItsOrigin(GameTestHelper helper) {
		floor(helper, 0, 9, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos origin = gouge(helper, 2, 2);
		for (int x = 3; x <= 6; x++) gouge(helper, x, 2);
		BlockPos far = gouge(helper, 7, 2);
		ItemEntity item = new ItemEntity(level, far.getX() + 0.5D, far.getY() + 0.05D, far.getZ() + 0.5D, new ItemStack(Items.COBBLESTONE));
		item.setDeltaMovement(Vec3.ZERO);
		level.addFreshEntity(item);
		double startX = item.getX();
		GroundNetwork.pulse(level, origin, GroundFunction.PULL, 400, null);
		helper.runAtTickTime(60, () -> {
			try {
				GroundRunSavedData.Lit clock = GroundRunSavedData.get(level).get(far);
				helper.assertTrue(item.getX() < startX - 1.5D, "the item did not slide toward the pulse's origin: " + startX + " to "
						+ item.getX() + " (alive " + item.isAlive() + ", far cell " + level.getBlockState(far) + ", clock " + clock + ")");
			} finally {
				item.discard();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aCompassPointsTheRunAndItsNearEndFlares(GameTestHelper helper) {
		floor(helper, 0, 9, 0, 3);
		ServerLevel level = helper.getLevel();
		List<BlockPos> cells = new ArrayList<>();
		for (int x = 1; x <= 8; x++) cells.add(gouge(helper, x, 2));
		ItemStack compass = new ItemStack(Items.COMPASS);
		BlockPos lodestone = helper.absolutePos(new BlockPos(40, 1, 2));
		compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(level.dimension(), lodestone)), false));
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 1_000.0D);
			player.setItemInHand(InteractionHand.OFF_HAND, compass);
			aim(player, feet(helper, 1, 0), floorTop(helper, 1, 2));
			project(player);
			GroundRunSavedData data = GroundRunSavedData.get(level);
			helper.assertTrue(data.get(cells.getLast()) != null && data.get(cells.getLast()).flare(),
					"the end nearest the lodestone does not flare");
			helper.assertTrue(data.get(cells.getFirst()) != null && !data.get(cells.getFirst()).flare(), "the near end flared too");
			helper.assertTrue(data.get(cells.getFirst()).function() == GroundFunction.POINT, "a compass did not make the run point");
		}
		helper.succeed();
	}

	/** CB-24: a feeding run grows a crop one stage every 30 s, so a stage lands within 600 ticks of lighting. */
	@GameTest(template = EMPTY, timeoutTicks = 700)
	public static void aFeedingRunGrowsTheCropsBesideIt(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 4);
		ServerLevel level = helper.getLevel();
		BlockPos cell = gouge(helper, 3, 2);
		BlockPos crop = cell.south();
		level.setBlock(crop.below(), Blocks.FARMLAND.defaultBlockState(), 3);
		level.setBlock(crop, Blocks.WHEAT.defaultBlockState(), 3);
		GroundNetwork.pulse(level, cell, GroundFunction.FEED, 700, null);
		helper.runAtTickTime(640, () -> {
			BlockState wheat = level.getBlockState(crop);
			helper.assertTrue(wheat.getBlock() instanceof CropBlock block && block.getAge(wheat) >= 1,
					"a feeding run did not grow the wheat beside it: " + wheat);
			helper.assertTrue(wheat.getBlock() instanceof CropBlock block && block.getAge(wheat) <= 2,
					"a feeding run grew the wheat faster than a stage every 30 s: " + wheat);
			helper.succeed();
		});
	}

	/** CB-24: a crop between two feeding cells still grows one stage every 30 s, not one per cell. */
	@GameTest(template = EMPTY, timeoutTicks = 1300)
	public static void aCropBesideTwoFeedingCellsGrowsOnce(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 4);
		ServerLevel level = helper.getLevel();
		BlockPos west = gouge(helper, 2, 2);
		BlockPos east = gouge(helper, 4, 2);
		BlockPos crop = west.east();
		level.setBlock(crop.below(), Blocks.FARMLAND.defaultBlockState(), 3);
		level.setBlock(crop, Blocks.WHEAT.defaultBlockState(), 3);
		GroundNetwork.pulse(level, west, GroundFunction.FEED, 1300, null);
		GroundNetwork.pulse(level, east, GroundFunction.FEED, 1300, null);
		// Each stage gained, with the tick it landed on. Both cells growing it would land two stages in one beat.
		List<Long> stages = new ArrayList<>();
		int[] last = {0};
		helper.onEachTick(() -> {
			BlockState wheat = level.getBlockState(crop);
			int age = wheat.getBlock() instanceof CropBlock block ? block.getAge(wheat) : last[0];
			for (; last[0] < age; last[0]++) stages.add(level.getGameTime());
		});
		helper.runAtTickTime(1250, () -> {
			helper.assertTrue(stages.size() >= 2, "1,250 ticks of feeding grew only " + stages.size() + " stages");
			for (int i = 1; i < stages.size(); i++) {
				helper.assertTrue(stages.get(i) - stages.get(i - 1) >= 20,
						"two stages landed " + (stages.get(i) - stages.get(i - 1)) + " ticks apart: " + stages);
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aDarkRunHidesItselfAndDimsCreaturesOnIt(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 4);
		ServerLevel level = helper.getLevel();
		BlockPos cell = gouge(helper, 3, 2);
		Pig pig = EntityType.PIG.create(level);
		pig.moveTo(cell.getX() + 0.5D, cell.getY(), cell.getZ() + 0.5D);
		pig.setNoAi(true);
		level.addFreshEntity(pig);
		GroundNetwork.pulse(level, cell, GroundFunction.DARK, 400, null);
		helper.runAtTickTime(25, () -> {
			try {
				helper.assertTrue(lit(level, cell, GroundFunction.DARK), "the dark run did not light");
				helper.assertTrue(BloodGougeBlock.lightOf(level.getBlockState(cell)) == 0, "a dark run gives light");
				helper.assertTrue(pig.hasEffect(EffectInit.obscured), "a creature on a dark run was not dimmed");
			} finally {
				pig.discard();
			}
			helper.succeed();
		});
	}

	// ── The catalyst feeder ──

	/** A lit run of two cells from (3, 2) with a feeder south of the first, stocked with four glowstone dust. */
	private static CatalystFeederBlockEntity fedRun(GameTestHelper helper, BlockPos[] cellOut) {
		floor(helper, 0, 6, 1, 4);
		ServerLevel level = helper.getLevel();
		BlockPos cell = gouge(helper, 3, 2);
		gouge(helper, 4, 2);
		BlockPos feederPos = cell.south();
		level.setBlock(feederPos, BlockInit.catalyst_feeder.get().defaultBlockState(), 3);
		CatalystFeederBlockEntity feeder = (CatalystFeederBlockEntity) level.getBlockEntity(feederPos);
		feeder.stock().insertItem(0, new ItemStack(Items.GLOWSTONE_DUST, 4), false);
		cellOut[0] = cell;
		return feeder;
	}

	/** A stocked feeder keeps a lit run lit, paying 15 mL of its lighter's blood for each catalyst it dissolves (CQ-7). */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void aFeederKeepsALitRunLit(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] cell = new BlockPos[1];
		CatalystFeederBlockEntity feeder = fedRun(helper, cell);
		CapturedPlayer owner = runOwner(helper, GameType.SURVIVAL);
		prepare(owner.player, null, 5_000.0D);
		GroundNetwork.pulse(level, cell[0], GroundFunction.LIGHT, 80, null, owner.player.getUUID());
		helper.runAtTickTime(170, () -> {
			try {
				helper.assertTrue(lit(level, cell[0], GroundFunction.LIGHT), "the run went dark beside a stocked feeder");
				int dissolved = 4 - feeder.stock().getStackInSlot(0).getCount();
				helper.assertTrue(dissolved > 0, "the feeder dissolved nothing");
				double paid = 5_000.0D - blood(owner.player);
				helper.assertTrue(Math.abs(paid - dissolved * GroundRunRules.FEEDER_ML) < 1.0E-6D,
						"the feeder dissolved " + dissolved + " catalysts and its lighter paid " + paid + " mL");
			} finally {
				owner.close();
			}
			helper.succeed();
		});
	}

	/** With its lighter offline the feeder dissolves nothing and the run goes dark on time (NQ-02-1). */
	@GameTest(template = EMPTY, batch = DRAW_AND_WATER, timeoutTicks = 200)
	public static void aFeederStopsWhenItsOwnerIsAway(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] cell = new BlockPos[1];
		CatalystFeederBlockEntity feeder = fedRun(helper, cell);
		CapturedPlayer owner = runOwner(helper, GameType.SURVIVAL);
		prepare(owner.player, null, 5_000.0D);
		GroundNetwork.pulse(level, cell[0], GroundFunction.LIGHT, 80, null, owner.player.getUUID());
		owner.offline();
		helper.runAtTickTime(170, () -> {
			try {
				helper.assertTrue(!level.getBlockState(cell[0]).getValue(BloodGougeBlock.LIT), "a feeder kept a run lit for a lighter who is away");
				helper.assertTrue(feeder.stock().getStackInSlot(0).getCount() == 4, "a feeder dissolved a catalyst with its lighter away");
			} finally {
				owner.close();
			}
			helper.succeed();
		});
	}

	/** A lighter who cannot pay the 15 mL in full gets no top-up, and the run goes dark on time (NQ-02-1). */
	@GameTest(template = EMPTY, batch = DRAW_AND_WATER, timeoutTicks = 200)
	public static void aFeederStopsWhenItsOwnerIsDry(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos[] cell = new BlockPos[1];
		CatalystFeederBlockEntity feeder = fedRun(helper, cell);
		CapturedPlayer owner = runOwner(helper, GameType.SURVIVAL);
		prepare(owner.player, null, 0.0D);
		GroundNetwork.pulse(level, cell[0], GroundFunction.LIGHT, 80, null, owner.player.getUUID());
		helper.runAtTickTime(170, () -> {
			try {
				helper.assertTrue(!level.getBlockState(cell[0]).getValue(BloodGougeBlock.LIT), "a feeder kept a run lit for a dry lighter");
				helper.assertTrue(feeder.stock().getStackInSlot(0).getCount() == 4, "a feeder dissolved a catalyst its lighter could not pay for");
			} finally {
				owner.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void aFeederNeverLightsADarkRun(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 4);
		ServerLevel level = helper.getLevel();
		BlockPos cell = gouge(helper, 3, 2);
		BlockPos feederPos = cell.south();
		level.setBlock(feederPos, BlockInit.catalyst_feeder.get().defaultBlockState(), 3);
		CatalystFeederBlockEntity feeder = (CatalystFeederBlockEntity) level.getBlockEntity(feederPos);
		feeder.stock().insertItem(0, new ItemStack(Items.GLOWSTONE_DUST, 4), false);
		helper.runAtTickTime(60, () -> {
			helper.assertTrue(!level.getBlockState(cell).getValue(BloodGougeBlock.LIT), "a feeder lit a dark run without blood");
			helper.assertTrue(feeder.stock().getStackInSlot(0).getCount() == 4, "a feeder spent a catalyst on a dark run");
			helper.succeed();
		});
	}

	/**
	 * A remote client mirrors the catalyst table (the feeder's slot reads it there): the server's sync snapshot, received
	 * on its own with no Temper table behind it, answers {@code of} as the server does for a named item (a compass), a
	 * school's Temper catalyst (coal, through Flammeus) and an item that is no catalyst. The swap and restore happen in
	 * one call on the server thread, so no other test sees the mirrored table.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void aClientMirrorsTheCatalystTableTheFeederReads(GameTestHelper helper) {
		ItemStack compass = new ItemStack(Items.COMPASS);
		ItemStack coal = new ItemStack(Items.COAL);
		ItemStack dirt = new ItemStack(Items.DIRT);
		GougeCatalysts.Catalyst namedOnServer = GougeCatalysts.of(compass);
		GougeCatalysts.Catalyst schoolOnServer = GougeCatalysts.of(coal);
		helper.assertTrue(namedOnServer != null && namedOnServer.function() == GroundFunction.POINT, "a compass is no pointing catalyst");
		helper.assertTrue(schoolOnServer != null && schoolOnServer.function() == GroundFunction.HOT, "coal is no hot catalyst");
		java.util.Map<net.minecraft.resources.ResourceLocation, String> snapshot = GougeCatalysts.syncSnapshot();
		helper.assertTrue(snapshot.size() <= GougeCatalysts.MAX_SYNCED_FUNCTIONS, "the snapshot holds " + snapshot.size() + " definitions");
		helper.assertTrue(snapshot.values().stream().allMatch(json -> json.length() <= GougeCatalysts.MAX_SYNCED_JSON_LENGTH),
				"a synced definition is longer than the channel allows");
		helper.assertTrue(snapshot.values().stream().noneMatch(json -> json.contains("temper")), "the snapshot names a school");
		GougeCatalysts.Tables live = GougeCatalysts.tables();
		try {
			GougeCatalysts.restore(new GougeCatalysts.Tables(java.util.Map.of(), java.util.Map.of()));
			helper.assertTrue(GougeCatalysts.of(coal) == null || GougeCatalysts.of(compass) == null, "the tables did not empty");
			GougeCatalysts.receiveSync(snapshot);
			helper.assertTrue(namedOnServer.equals(GougeCatalysts.of(compass)), "the mirror read the compass as " + GougeCatalysts.of(compass));
			helper.assertTrue(schoolOnServer.equals(GougeCatalysts.of(coal)), "the mirror read coal as " + GougeCatalysts.of(coal));
			helper.assertTrue(GougeCatalysts.of(dirt) == null, "the mirror took dirt for a catalyst");
			CatalystFeederBlockEntity feeder = new CatalystFeederBlockEntity(BlockPos.ZERO, BlockInit.catalyst_feeder.get().defaultBlockState());
			helper.assertTrue(feeder.accepts(coal) && !feeder.accepts(dirt), "the feeder's slot did not follow the mirror");
		} finally {
			GougeCatalysts.restore(live);
		}
		helper.assertTrue(schoolOnServer.equals(GougeCatalysts.of(coal)), "the live table was not restored");
		helper.succeed();
	}

	/**
	 * The catalyst table as JEI lists it (round two P8): every function a lit run can have has at least one catalyst,
	 * and the track's named pairs read as the track says (glowstone dust lights, coal heats, a compass points, an ink
	 * sac darkens).
	 */
	@GameTest(template = EMPTY, timeoutTicks = 20)
	public static void theCatalystTableListsEveryFunction(GameTestHelper helper) {
		var listed = GougeCatalysts.listed();
		for (GroundFunction function : GroundFunction.values()) {
			if (function == GroundFunction.NONE) continue;
			helper.assertTrue(listed.containsKey(function) && !listed.get(function).isEmpty(), "no catalyst gives " + function);
		}
		helper.assertTrue(!listed.containsKey(GroundFunction.NONE), "a catalyst is listed for the bare run");
		java.util.Map<net.minecraft.world.item.Item, GroundFunction> pairs = java.util.Map.of(Items.GLOWSTONE_DUST, GroundFunction.LIGHT,
				Items.COAL, GroundFunction.HOT, Items.COMPASS, GroundFunction.POINT, Items.INK_SAC, GroundFunction.DARK);
		pairs.forEach((item, function) -> {
			var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item);
			helper.assertTrue(listed.getOrDefault(function, List.of()).stream().anyMatch(entry -> entry.item().equals(id)),
					id + " is not listed under " + function);
		});
		helper.succeed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void absorptionFillsInAGouge(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = gouge(helper, 4, 2);
		GroundNetwork.pulse(level, cell, GroundFunction.NONE, 400, null);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 1_000.0D);
			aim(player, feet(helper, 1, 2), floorTop(helper, 4, 2));
			absorbTick(player);
			helper.assertTrue(level.getBlockState(cell).isAir(), "Absorption did not fill the gouge in");
			helper.assertTrue(GroundRunSavedData.get(level).get(cell) == null, "a filled gouge kept its clock");
		}
		helper.succeed();
	}

	// ── Living veins ──

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void aVeinGrowsFromItsNodeWithinItsOwnersAllowance(GameTestHelper helper) {
		floor(helper, 0, 12, 0, 3);
		ServerLevel level = helper.getLevel();
		BlockPos node = helper.absolutePos(new BlockPos(1, 1, 2));
		level.setBlock(node, BlockInit.vein_node.get().defaultBlockState(), 3);
		List<BlockPos> cells = new ArrayList<>();
		for (int x = 2; x <= 12; x++) {
			cells.add(gouge(helper, x, 2));
			cells.add(gouge(helper, x, 3));
		}
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, null, 2_000.0D);
		HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(3);
		aim(player, feet(helper, 1, 0), Vec3.atCenterOf(node).add(0.0D, -0.2D, 0.0D));
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < 80) project(player);
		});
		helper.runAtTickTime(90, () -> {
			try {
				long veins = cells.stream().filter(cell -> level.getBlockState(cell).is(BlockInit.living_vein.get())).count();
				int allowance = GroundRunRules.veinAllowance(3);
				helper.assertTrue(veins == allowance, "a Third Degree vein grew " + veins + " cells, allowed " + allowance);
				helper.assertTrue(VeinLedger.get(level).length(player.getUUID()) == allowance, "the ledger did not count the growth");
				helper.assertTrue(Math.abs(2_000.0D - blood(player) - allowance * GroundRunRules.VEIN_GROWTH_ML) < 1.0E-6D,
						"growth did not cost its price: paid " + (2_000.0D - blood(player)));
				helper.assertTrue(((VeinNodeBlockEntity) level.getBlockEntity(node)).owner().equals(player.getUUID()),
						"the node did not take its grower as owner");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void tissueWillNotTakeBeforeTheThirdDegree(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos node = helper.absolutePos(new BlockPos(1, 1, 2));
		level.setBlock(node, BlockInit.vein_node.get().defaultBlockState(), 3);
		BlockPos cell = gouge(helper, 2, 2);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 1_000.0D);
			HemoCapabilityAccess.getInitiatoryDegree(player).setDegreeNumber(2);
			double spent = GroundNetwork.growVein(player, node);
			helper.assertTrue(spent == 0.0D && level.getBlockState(cell).is(BlockInit.blood_gouge.get()),
					"tissue grew for a Second Degree Harbinger");
		}
		helper.succeed();
	}

	/** Two scriptoriums joined by a vein between two nodes, grown by {@code owner}; returns the two block positions. */
	private static BlockPos[] veinBetweenTanks(GameTestHelper helper, ServerPlayer owner) {
		return veinBetweenTanks(helper, owner, BlockInit.enzymatic_scriptorium.get().defaultBlockState());
	}

	/** Two {@code tank} blocks joined by a vein between two nodes, grown by {@code owner}; returns the two block positions. */
	private static BlockPos[] veinBetweenTanks(GameTestHelper helper, ServerPlayer owner, BlockState tank) {
		floor(helper, 0, 9, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos a = helper.absolutePos(new BlockPos(1, 1, 2));
		BlockPos b = helper.absolutePos(new BlockPos(8, 1, 2));
		level.setBlock(a, tank, 3);
		level.setBlock(b, tank, 3);
		BlockPos nodeA = a.east();
		BlockPos nodeB = b.west();
		level.setBlock(nodeA, BlockInit.vein_node.get().defaultBlockState(), 3);
		level.setBlock(nodeB, BlockInit.vein_node.get().defaultBlockState(), 3);
		((VeinNodeBlockEntity) level.getBlockEntity(nodeA)).setOwner(owner.getUUID(), level.getGameTime());
		((VeinNodeBlockEntity) level.getBlockEntity(nodeB)).setOwner(owner.getUUID(), level.getGameTime());
		for (int x = 3; x <= 6; x++) {
			BlockPos cell = helper.absolutePos(new BlockPos(x, 1, 2));
			level.setBlock(cell, face(BlockInit.living_vein.get(), Direction.DOWN), 3);
			VeinLedger.get(level).claim(level, cell, owner.getUUID());
		}
		return new BlockPos[] {a, b};
	}

	/**
	 * Both scriptoriums are machines under their 600 mL working reserve, so both draw (NQ-02-3 A) and the fuller runs
	 * into the dry one, while the owner is near and not after.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 160)
	public static void aVeinMovesBloodOnlyWhileItsOwnerIsNear(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		ServerPlayer owner = captured.player;
		prepare(owner, null, 1_000.0D);
		HemoCapabilityAccess.getInitiatoryDegree(owner).setDegreeNumber(3);
		owner.setPos(feet(helper, 4, 1));
		BlockPos[] tanks = veinBetweenTanks(helper, owner);
		BlockBloodVolume a = ReservoirBlood.settled(level.getBlockEntity(tanks[0]));
		BlockBloodVolume b = ReservoirBlood.settled(level.getBlockEntity(tanks[1]));
		double[] heldWhileAway = new double[1];
		helper.runAtTickTime(2, () -> {
			a.setMaxBloodVolume(600.0D);
			b.setMaxBloodVolume(600.0D);
			a.fill(500.0D);
		});
		helper.runAtTickTime(50, () -> {
			helper.assertTrue(ReservoirBlood.settled(level.getBlockEntity(tanks[1])).getBloodVolume() > 0.0D,
					"the vein moved no blood with its owner beside it");
			helper.assertTrue(blood(owner) < 1_000.0D, "the owner paid no upkeep");
			captured.offline();
			heldWhileAway[0] = ReservoirBlood.settled(level.getBlockEntity(tanks[1])).getBloodVolume();
		});
		helper.runAtTickTime(110, () -> {
			try {
				double now = ReservoirBlood.settled(level.getBlockEntity(tanks[1])).getBloodVolume();
				helper.assertTrue(now == heldWhileAway[0], "the vein ran a machine with its owner away: " + heldWhileAway[0] + " to " + now);
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	/**
	 * Two worked Venous Basins (stores, which never pull by their level) joined by a vein its owner tends, the owner at
	 * full blood: 1,500 mL in the first, 200 in the second.
	 */
	private static BlockPos[] restingTanks(GameTestHelper helper, CapturedPlayer captured) {
		ServerPlayer owner = captured.player;
		prepare(owner, null, 5_000.0D);
		HemoCapabilityAccess.getInitiatoryDegree(owner).setDegreeNumber(3);
		owner.setPos(feet(helper, 4, 1));
		BlockPos[] tanks = veinBetweenTanks(helper, owner, BlockInit.venous_basin.get().defaultBlockState()
				.setValue(VenousBasinBlock.WORKED, true).setValue(VenousBasinBlock.FACING, Direction.NORTH));
		ServerLevel level = helper.getLevel();
		helper.runAtTickTime(2, () -> {
			((VenousBasinBlockEntity) level.getBlockEntity(tanks[0])).configure().fill(1_500.0D);
			((VenousBasinBlockEntity) level.getBlockEntity(tanks[1])).configure().fill(200.0D);
		});
		return tanks;
	}

	private static double tank(ServerLevel level, BlockPos pos) {
		return ReservoirBlood.settled(level.getBlockEntity(pos)).getBloodVolume();
	}

	/**
	 * CL-14, NQ-02-3: two stores with nothing drawing at either end. The vein moves no blood, levels nothing and costs no
	 * upkeep, so a full basin never siphons into an emptier one.
	 */
	@GameTest(template = EMPTY, batch = DRAW_AND_WATER, timeoutTicks = 100)
	public static void aVeinWithNothingDrawingMovesNoBlood(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		BlockPos[] tanks = restingTanks(helper, captured);
		helper.runAtTickTime(80, () -> {
			try {
				helper.assertTrue(Math.abs(tank(level, tanks[0]) - 1_500.0D) < 1.0E-6D && Math.abs(tank(level, tanks[1]) - 200.0D) < 1.0E-6D,
						"a vein with nothing drawing moved blood: " + tank(level, tanks[0]) + " and " + tank(level, tanks[1]));
				helper.assertTrue(blood(captured.player) == 5_000.0D, "the owner paid upkeep while nothing moved");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	/**
	 * CL-14: blood taken out of one end draws the vein toward it, from the other end, for the draw window; once nothing
	 * has been taken for five seconds the vein stops again. The source is not stamped as drawn by the vein's own move.
	 */
	@GameTest(template = EMPTY, batch = DRAW_AND_WATER, timeoutTicks = 260)
	public static void aDrawnTankPullsThroughTheVein(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		CapturedPlayer captured = CapturedPlayer.create(helper).online();
		BlockPos[] tanks = restingTanks(helper, captured);
		double[] settled = new double[1];
		helper.runAtTickTime(30, () -> ReservoirBlood.settled(level.getBlockEntity(tanks[1])).drain(50.0D));
		helper.runAtTickTime(90, () -> {
			helper.assertTrue(tank(level, tanks[1]) > 150.0D, "the drawn tank pulled nothing down the vein: " + tank(level, tanks[1]));
			helper.assertTrue(tank(level, tanks[0]) < 1_500.0D, "the far tank gave nothing");
			helper.assertTrue(blood(captured.player) < 5_000.0D, "the owner paid no upkeep while blood moved");
			helper.assertTrue(ReservoirBlood.settled(level.getBlockEntity(tanks[0])).lastDrawn() == Long.MIN_VALUE,
					"the vein's own move stamped its source as drawn");
		});
		helper.runAtTickTime(180, () -> settled[0] = tank(level, tanks[1]));
		helper.runAtTickTime(240, () -> {
			try {
				helper.assertTrue(Math.abs(tank(level, tanks[1]) - settled[0]) < 1.0E-6D,
						"the vein kept moving after the draw window: " + settled[0] + " to " + tank(level, tanks[1]));
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void anUntendedVeinWithersBackToGouge(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer owner = captured.player;
			prepare(owner, null, 1_000.0D);
			BlockPos[] tanks = veinBetweenTanks(helper, owner);
			long longAgo = level.getGameTime() - GroundRunRules.VEIN_WITHER_TICKS - 100L;
			((VeinNodeBlockEntity) level.getBlockEntity(tanks[0].east())).setOwner(owner.getUUID(), longAgo);
			((VeinNodeBlockEntity) level.getBlockEntity(tanks[1].west())).setOwner(owner.getUUID(), longAgo);
			helper.runAtTickTime(30, () -> {
				for (int x = 3; x <= 6; x++) {
					BlockPos cell = helper.absolutePos(new BlockPos(x, 1, 2));
					helper.assertTrue(level.getBlockState(cell).is(BlockInit.blood_gouge.get()), "an untended vein did not wither to gouge");
				}
				helper.assertTrue(VeinLedger.get(level).length(owner.getUUID()) == 0, "withered cells stayed on the ledger");
				helper.succeed();
			});
		}
	}

	/**
	 * A lit run whose cells turn to vein (tissue grown) or back to gouge (a vein withered) still goes dark on time: the
	 * scheduled tick the old block set is dropped with it, so the new block takes the cell's clock up.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void aLitRunStillGoesDarkAfterTissueGrowsOrWithers(GameTestHelper helper) {
		floor(helper, 0, 8, 1, 3);
		ServerLevel level = helper.getLevel();
		List<BlockPos> cells = new ArrayList<>();
		for (int x = 2; x <= 6; x++) cells.add(gouge(helper, x, 2));
		GroundNetwork.pulse(level, cells.getFirst(), GroundFunction.LIGHT, 30, null);
		helper.runAtTickTime(10, () -> {
			for (int i = 0; i < cells.size(); i++) {
				BlockPos cell = cells.get(i);
				BlockState state = level.getBlockState(cell);
				helper.assertTrue(lit(level, cell, GroundFunction.LIGHT), "the run did not light before the swap");
				// Alternate cells grow tissue; the rest grow it and wither straight back to gouge.
				BlockState vein = BlockInit.living_vein.get().defaultBlockState()
						.setValue(MultifaceBlock.getFaceProperty(Direction.DOWN), true)
						.setValue(BloodGougeBlock.LIT, true).setValue(BloodGougeBlock.FUNCTION, state.getValue(BloodGougeBlock.FUNCTION));
				level.setBlock(cell, vein, 3);
				if (i % 2 == 1) level.setBlock(cell, state, 3);
			}
		});
		helper.runAtTickTime(70, () -> {
			for (BlockPos cell : cells) {
				BlockState state = level.getBlockState(cell);
				helper.assertTrue(state.getBlock() instanceof BloodGougeBlock && !state.getValue(BloodGougeBlock.LIT),
						"a swapped cell stayed lit past its time: " + state);
				helper.assertTrue(GroundRunSavedData.get(level).get(cell) == null, "a swapped cell kept its clock");
			}
			helper.succeed();
		});
	}

	// ── Basins and gutters ──

	/** A worked basin at (1, 1, 2) pouring east into worked gutters (2..5, 0, 2), a scriptorium above the last. */
	private static VenousBasinBlockEntity pouringBasin(GameTestHelper helper, boolean workedGutters) {
		floor(helper, 0, 8, 1, 3);
		ServerLevel level = helper.getLevel();
		for (int x = 2; x <= 5; x++) {
			level.setBlock(helper.absolutePos(new BlockPos(x, 0, 2)), BlockInit.blood_gutter.get().defaultBlockState()
					.setValue(BloodGutterBlock.WORKED, workedGutters), 3);
		}
		level.setBlock(helper.absolutePos(new BlockPos(5, 1, 2)), BlockInit.enzymatic_scriptorium.get().defaultBlockState(), 3);
		BlockPos basin = helper.absolutePos(new BlockPos(1, 1, 2));
		level.setBlock(basin, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true)
				.setValue(VenousBasinBlock.FACING, Direction.EAST), 3);
		return (VenousBasinBlockEntity) level.getBlockEntity(basin);
	}

	@GameTest(template = EMPTY, timeoutTicks = 160)
	public static void aBasinRunsDownItsGuttersFeedsAMachineThenClots(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		VenousBasinBlockEntity basin = pouringBasin(helper, true);
		BlockPos firstGutter = helper.absolutePos(new BlockPos(2, 0, 2));
		BlockPos machine = helper.absolutePos(new BlockPos(5, 1, 2));
		helper.runAtTickTime(2, () -> ReservoirBlood.settled(basin).fill(1_500.0D));
		helper.runAtTickTime(60, () -> {
			helper.assertTrue(level.getBlockState(firstGutter).getValue(BloodGutterBlock.FILL) >= 2, "the gutter under the spout stayed dry");
			helper.assertTrue(basin.getBlockState().getValue(VenousBasinBlock.POURING), "a basin of fresh blood over a gutter is not pouring");
			helper.assertTrue(ReservoirBlood.settled(level.getBlockEntity(machine)).getBloodVolume() > 0.0D,
					"the machine at the run's end was not fed");
			ReservoirBlood.settled(basin).backdate(level.getGameTime(), CoagulationRules.FULL_CLOT_TICKS);
		});
		helper.runAtTickTime(110, () -> {
			BlockBloodVolume volume = ReservoirBlood.settled(basin);
			helper.assertTrue(volume.getBloodVolume() == 0.0D && volume.clotted() > 0.0D, "the basin did not clot");
			helper.assertTrue(!basin.getBlockState().getValue(VenousBasinBlock.POURING), "a clotted basin still pours");
			helper.assertTrue(level.getBlockState(firstGutter).getValue(BloodGutterBlock.FILL) == 0, "the gutters did not dry with the basin");
			helper.assertTrue(basin.getBlockState().getValue(VenousBasinBlock.FILL) > 0, "the clot left no mark in the bowl");
			helper.succeed();
		});
	}

	/**
	 * Two worked basins pour into one gutter line, one full and one dry. The dry one's walk does not dry the gutters the
	 * full one keeps wet (each gutter shows the highest stream any basin gives it), even after both have walked again; a
	 * drip under the shared line takes from the full basin, not the dry one; and when the full one clots the gutters dry.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 240)
	public static void twoBasinsOnOneGutterLineKeepTheWetOnesStream(GameTestHelper helper) {
		floor(helper, 0, 10, 0, 3);
		ServerLevel level = helper.getLevel();
		List<BlockPos> gutters = new ArrayList<>();
		for (int x = 2; x <= 9; x++) {
			BlockPos gutter = helper.absolutePos(new BlockPos(x, 0, 2));
			level.setBlock(gutter, BlockInit.blood_gutter.get().defaultBlockState().setValue(BloodGutterBlock.WORKED, true), 3);
			gutters.add(gutter);
		}
		BlockPos wetPos = helper.absolutePos(new BlockPos(1, 1, 2));
		level.setBlock(wetPos, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true)
				.setValue(VenousBasinBlock.FACING, Direction.EAST), 3);
		BlockPos dryPos = helper.absolutePos(new BlockPos(6, 1, 1));
		level.setBlock(dryPos, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true)
				.setValue(VenousBasinBlock.FACING, Direction.SOUTH), 3);
		VenousBasinBlockEntity wet = (VenousBasinBlockEntity) level.getBlockEntity(wetPos);
		VenousBasinBlockEntity dry = (VenousBasinBlockEntity) level.getBlockEntity(dryPos);
		BlockPos underDry = helper.absolutePos(new BlockPos(6, 0, 2));
		helper.assertTrue(VenousBasinBlock.spoutTarget(dryPos, level.getBlockState(dryPos)).equals(underDry),
				"the dry basin does not pour into the shared line");
		ReservoirBlood.settled(wet).fill(1_500.0D);
		Runnable showsTheWetStream = () -> {
			for (BlockPos gutter : gutters) {
				int fill = level.getBlockState(gutter).getValue(BloodGutterBlock.FILL);
				helper.assertTrue(fill == wet.streamAt(gutter), "the gutter at " + gutter + " shows " + fill
						+ " where the full basin gives it " + wet.streamAt(gutter));
			}
			helper.assertTrue(wet.streamAt(underDry) > 0, "the full basin's stream does not reach the dry basin's spout");
		};
		helper.startSequence()
				.thenWaitUntil(() -> {
					helper.assertTrue(!dry.run().gutters().isEmpty() && !wet.run().gutters().isEmpty(), "a basin has not walked its run");
					showsTheWetStream.run();
				})
				// Both walk again, the dry one last, as a rescan would have them.
				.thenExecute(() -> {
					wet.markStale();
					dry.markStale();
				})
				.thenIdle(3)
				.thenExecute(showsTheWetStream)
				.thenExecute(() -> {
					dry.markStale();
				})
				.thenIdle(3)
				.thenExecute(() -> {
					showsTheWetStream.run();
					helper.assertTrue(dry.run().others().contains(wetPos), "the dry basin did not find the full one on its line");
					helper.assertTrue(VenousBasinBlock.feedingBasin(level, underDry) == wet,
							"a drip under the shared line would take from the dry basin");
					ReservoirBlood.settled(wet).backdate(level.getGameTime(), CoagulationRules.FULL_CLOT_TICKS);
				})
				.thenWaitUntil(() -> {
					for (BlockPos gutter : gutters) {
						helper.assertTrue(level.getBlockState(gutter).getValue(BloodGutterBlock.FILL) == 0,
								"the gutter at " + gutter + " did not dry with the full basin");
					}
				})
				.thenSucceed();
	}

	/**
	 * Two basins on raised branches that run downhill into one trunk: neither branch is on the other's stream or uphill
	 * of its spout, so each finds the other only by walking the whole network. The dry one walking last still leaves
	 * the trunk showing the full one's stream.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void basinsOnBranchesThatJoinDownhillShareTheTrunk(GameTestHelper helper) {
		floor(helper, 0, 10, 0, 4);
		ServerLevel level = helper.getLevel();
		BlockState worked = BlockInit.blood_gutter.get().defaultBlockState().setValue(BloodGutterBlock.WORKED, true);
		List<BlockPos> trunk = new ArrayList<>();
		for (int x = 5; x <= 9; x++) {
			BlockPos gutter = helper.absolutePos(new BlockPos(x, 0, 3));
			level.setBlock(gutter, worked, 3);
			trunk.add(gutter);
		}
		// Branch A runs east along z = 3 one block up; branch B runs south along x = 5 one block up.
		for (BlockPos raised : new BlockPos[] {new BlockPos(3, 1, 3), new BlockPos(4, 1, 3), new BlockPos(5, 1, 1), new BlockPos(5, 1, 2)}) {
			level.setBlock(helper.absolutePos(raised), worked, 3);
		}
		// The raised basins stand on stone.
		helper.setBlock(new BlockPos(2, 1, 3), Blocks.POLISHED_ANDESITE);
		helper.setBlock(new BlockPos(5, 1, 0), Blocks.POLISHED_ANDESITE);
		BlockPos wetPos = helper.absolutePos(new BlockPos(2, 2, 3));
		level.setBlock(wetPos, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true)
				.setValue(VenousBasinBlock.FACING, Direction.EAST), 3);
		BlockPos dryPos = helper.absolutePos(new BlockPos(5, 2, 0));
		level.setBlock(dryPos, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.WORKED, true)
				.setValue(VenousBasinBlock.FACING, Direction.SOUTH), 3);
		VenousBasinBlockEntity wet = (VenousBasinBlockEntity) level.getBlockEntity(wetPos);
		VenousBasinBlockEntity dry = (VenousBasinBlockEntity) level.getBlockEntity(dryPos);
		ReservoirBlood.settled(wet).fill(1_500.0D);
		Runnable trunkShowsTheWetStream = () -> {
			helper.assertTrue(wet.streamAt(trunk.getFirst()) > 0, "the full basin's stream does not reach the trunk");
			for (BlockPos gutter : trunk) {
				int fill = level.getBlockState(gutter).getValue(BloodGutterBlock.FILL);
				helper.assertTrue(fill == wet.streamAt(gutter), "the trunk gutter at " + gutter + " shows " + fill
						+ " where the full basin gives it " + wet.streamAt(gutter));
			}
		};
		helper.startSequence()
				.thenWaitUntil(() -> {
					helper.assertTrue(!dry.run().gutters().isEmpty() && !wet.run().gutters().isEmpty(), "a basin has not walked its run");
					trunkShowsTheWetStream.run();
				})
				.thenExecute(() -> dry.markStale())
				.thenIdle(3)
				.thenExecute(() -> {
					trunkShowsTheWetStream.run();
					helper.assertTrue(dry.run().others().contains(wetPos), "the dry basin did not find the full one across the join");
					helper.assertTrue(wet.run().others().contains(dryPos), "the full basin did not find the dry one across the join");
				})
				.thenSucceed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void arenaGuttersAndBasinsStayOutOfAPlayersStream(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		VenousBasinBlockEntity basin = pouringBasin(helper, false);
		BlockPos firstGutter = helper.absolutePos(new BlockPos(2, 0, 2));
		BlockPos machine = helper.absolutePos(new BlockPos(5, 1, 2));
		BlockPos arenaBasin = helper.absolutePos(new BlockPos(7, 1, 2));
		level.setBlock(arenaBasin, BlockInit.venous_basin.get().defaultBlockState().setValue(VenousBasinBlock.FILL, 3), 3);
		helper.runAtTickTime(2, () -> ReservoirBlood.settled(basin).fill(1_500.0D));
		helper.runAtTickTime(60, () -> {
			helper.assertTrue(level.getBlockState(firstGutter).getValue(BloodGutterBlock.FILL) == 0,
					"a player's basin wet a structure's gutter");
			helper.assertTrue(ReservoirBlood.settled(level.getBlockEntity(machine)).getBloodVolume() == 0.0D,
					"a structure's gutters carried a player's blood");
			helper.assertTrue(level.getBlockEntity(arenaBasin) == null, "a structure's basin keeps a tank a hopper could drain");
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void projectionLightsAnAshTrail(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos trail = helper.absolutePos(new BlockPos(4, 1, 2));
		level.setBlock(trail, BlockInit.smouldering_ash_trail.get().defaultBlockState(), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 1_000.0D);
			aim(player, feet(helper, 1, 2), floorTop(helper, 4, 2));
			project(player);
			helper.assertTrue(level.getBlockState(trail).is(BlockInit.active_smouldering_ash_trail.get()), "Projection did not light the ash trail");
			helper.assertTrue(Math.abs(1_000.0D - blood(player) - 25.0D) < 1.0E-6D, "lighting a trail did not cost 25 mL");
		}
		helper.succeed();
	}

	// ── The Sculptings ──

	@GameTest(template = EMPTY, timeoutTicks = 140)
	public static void aCaveRouteCutWithDowsingLightsWithGlowstone(GameTestHelper helper) {
		floor(helper, 0, 8, 1, 3);
		ServerLevel level = helper.getLevel();
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "vascular_dowsing", 2_000.0D);
		int[] tick = {0};
		helper.onEachTick(() -> {
			int t = tick[0]++;
			if (t < 30) {
				// Sweep along the floor from x 2 to x 5, a cell every seven ticks.
				int x = 2 + Math.min(3, t / 7);
				aim(player, feet(helper, 1, 2), floorTop(helper, x, 2));
				project(player);
			}
		});
		helper.runAtTickTime(32, () -> {
			for (int x = 2; x <= 5; x++) {
				helper.assertTrue(level.getBlockState(helper.absolutePos(new BlockPos(x, 1, 2))).is(BlockInit.blood_gouge.get()),
						"Vascular Dowsing did not cut a gouge at x " + x);
			}
		});
		// Past the pulse spacing of any pulse the sweep sent into the cells it had already cut.
		helper.runAtTickTime(60, () -> {
			prepare(player, null, 2_000.0D);
			player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GLOWSTONE_DUST));
			aim(player, feet(helper, 1, 2), floorTop(helper, 3, 2));
			project(player);
		});
		helper.runAtTickTime(100, () -> {
			try {
				for (int x = 2; x <= 5; x++) {
					BlockPos cell = helper.absolutePos(new BlockPos(x, 1, 2));
					helper.assertTrue(lit(level, cell, GroundFunction.LIGHT) && level.getBrightness(LightLayer.BLOCK, cell) == GroundFunction.LIGHT.light(),
							"the route is not lit at x " + x);
				}
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void hematicBeaconSpreadsFadingLinesOfLight(GameTestHelper helper) {
		floor(helper, 0, 12, 0, 8);
		ServerLevel level = helper.getLevel();
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "hematic_beacon", 2_000.0D);
		aim(player, feet(helper, 1, 4), floorTop(helper, 6, 4));
		// The pattern's forks and turns roll on the player's random; a fixed seed makes the spread the same every run.
		player.getRandom().setSeed(0x6C7578L);
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < 25) project(player);
		});
		BlockPos origin = helper.absolutePos(new BlockPos(6, 1, 4));
		helper.startSequence().thenExecuteAfter(30, () -> {
			List<BlockPos> traces = new ArrayList<>();
			for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(12, 1, 8)))) {
				if (level.getBlockState(pos).is(BlockInit.lux_trace.get())) traces.add(pos.immutable());
			}
			helper.assertTrue(traces.size() >= 4, "three pulses spread only " + traces.size() + " traces");
			helper.assertTrue(level.getBlockState(origin).is(BlockInit.lux_trace.get()), "the struck cell holds no trace");
			helper.assertTrue(level.getBlockTicks().hasScheduledTick(origin, BlockInit.lux_trace.get()), "a trace has no clock to fade on");
			helper.assertTrue(LuxTraceBlock.lightOf(level.getBlockState(origin).setValue(LuxTraceBlock.FADE, LuxTraceBlock.MAX_FADE)) < 12,
					"a faded trace is as bright as a new one");
		}).thenWaitUntil(() -> helper.assertTrue(level.getBrightness(LightLayer.BLOCK, origin) >= 12,
				"the struck cell gives no real light: block light " + level.getBrightness(LightLayer.BLOCK, origin)))
				.thenExecute(captured::close).thenSucceed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void bloodEclipseDrawsTheLightOutOfWhatItCanPutOut(GameTestHelper helper) {
		floor(helper, 0, 8, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos candle = helper.absolutePos(new BlockPos(4, 1, 2));
		BlockPos trace = helper.absolutePos(new BlockPos(5, 1, 2));
		level.setBlock(candle, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, true), 3);
		level.setBlock(trace, face(BlockInit.lux_trace.get(), Direction.DOWN), 3);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "blood_eclipse", 2_000.0D);
		aim(player, feet(helper, 1, 2), Vec3.atCenterOf(candle).add(0.0D, -0.3D, 0.0D));
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < 25) absorbTick(player);
		});
		helper.runAtTickTime(30, () -> {
			try {
				helper.assertTrue(!level.getBlockState(candle).getValue(CandleBlock.LIT), "the candle was not put out");
				helper.assertTrue(level.getBlockState(trace).isAir(), "the Lux trace beside it was not put out");
				helper.assertTrue(blood(player) < 2_000.0D, "the Sculpting paid nothing");
			} finally {
				captured.close();
			}
			helper.succeed();
		});
	}

	/**
	 * Putting out a lit run is the caster's edit: one who may not build there leaves it lit for its whole time, and no
	 * field hangs. (The snuff once forgot the cell's clock before the edit was refused, so the run went dark anyway.)
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void bloodEclipseLeavesALitRunWhereTheCasterMayNotBuild(GameTestHelper helper) {
		floor(helper, 0, 8, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = gouge(helper, 4, 2);
		helper.assertTrue(GroundNetwork.pulse(level, cell, GroundFunction.LIGHT, 400, null) == 1, "the gouge took no pulse");
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "blood_eclipse", 2_000.0D);
		player.setGameMode(GameType.ADVENTURE);
		aim(player, feet(helper, 1, 2), floorTop(helper, 4, 2));
		int[] tick = {0};
		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(lit(level, cell, GroundFunction.LIGHT), "the run never lit"))
				.thenExecute(() -> helper.onEachTick(() -> {
					if (tick[0]++ < 25) absorbTick(player);
				}))
				.thenIdle(25 + 2 * GroundRunRules.EFFECT_INTERVAL)
				.thenExecute(() -> {
					try {
						helper.assertTrue(lit(level, cell, GroundFunction.LIGHT),
								"a caster who may not build here put the run out: " + level.getBlockState(cell));
						helper.assertTrue(DarkFields.of(player) == null, "a refused snuff hung a field of dark");
					} finally {
						DarkFields.logout(player);
						captured.close();
						level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
					}
				}).thenSucceed();
	}

	@GameTest(template = EMPTY, timeoutTicks = 120)
	public static void sanguineMagnetismDrawsDroppedItemsEvenOnAFullBody(GameTestHelper helper) {
		floor(helper, 0, 9, 1, 3);
		ServerLevel level = helper.getLevel();
		ItemEntity item = new ItemEntity(level, helper.absolutePos(new BlockPos(7, 1, 2)).getX() + 0.5D,
				helper.absolutePos(new BlockPos(7, 1, 2)).getY() + 0.1D, helper.absolutePos(new BlockPos(7, 1, 2)).getZ() + 0.5D,
				new ItemStack(Items.COBBLESTONE, 5));
		item.setDeltaMovement(Vec3.ZERO);
		level.addFreshEntity(item);
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "sanguine_magnetism", 5_000.0D);
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < 100 && item.isAlive()) {
				aim(player, feet(helper, 1, 2), item.getBoundingBox().getCenter());
				absorbTick(player);
			}
		});
		helper.runAtTickTime(105, () -> {
			try {
				int carried = player.getInventory().countItem(Items.COBBLESTONE);
				helper.assertTrue(carried == 5, "the item draw did not bring the cobblestone to a full body: carried " + carried);
			} finally {
				item.discard();
				captured.close();
			}
			helper.succeed();
		});
	}

	// -- The field of dark (round two P8) --

	/** A floor from x 0..20, z 0..14 with {@code candles} lit candles at (4, 1, 6); returns the candle's cell. */
	private static BlockPos candleRoom(GameTestHelper helper, int candles) {
		floor(helper, 0, 20, 0, 14);
		BlockPos candle = helper.absolutePos(new BlockPos(4, 1, 6));
		if (candles > 0) {
			helper.getLevel().setBlock(candle, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, candles)
					.setValue(CandleBlock.LIT, true), 3);
		}
		return candle;
	}

	/** A caster at (1, 1, 6) holding Blood Eclipse's Absorption toward the candle each tick for {@code ticks} ticks. */
	private static CapturedPlayer eclipseCaster(GameTestHelper helper, BlockPos candle, boolean tempered, int ticks) {
		CapturedPlayer captured = CapturedPlayer.create(helper);
		ServerPlayer player = captured.player;
		prepare(player, "blood_eclipse", 2_000.0D);
		if (!tempered) {
			var temper = HemoCapabilityAccess.getBloodTemper(player);
			for (EnumBloodTemper school : EnumBloodTemper.values()) temper.set(school, 0.0F, TemperRules.MAX_CAP);
		}
		aim(player, feet(helper, 1, 6), Vec3.atCenterOf(candle).add(0.0D, -0.3D, 0.0D));
		int[] tick = {0};
		helper.onEachTick(() -> {
			if (tick[0]++ < ticks) absorbTick(player);
		});
		return captured;
	}

	private static Husk husk(GameTestHelper helper, int x, int z) {
		ServerLevel level = helper.getLevel();
		Husk husk = EntityType.HUSK.create(level);
		BlockPos at = helper.absolutePos(new BlockPos(x, 1, z));
		husk.moveTo(at.getX() + 0.5D, at.getY(), at.getZ() + 0.5D);
		husk.setNoAi(true);
		husk.setPersistenceRequired();
		level.addFreshEntity(husk);
		return husk;
	}

	/**
	 * The light Blood Eclipse takes hangs where it was aimed as a field of dark: four candles' light (12) hangs for 240
	 * ticks, over a radius of 3.6, and a creature inside is Obscured.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 80)
	public static void bloodEclipseHangsAFieldOfDarkWhereItTookTheLight(GameTestHelper helper) {
		BlockPos candle = candleRoom(helper, 4);
		Husk inside = husk(helper, 6, 6);
		CapturedPlayer captured = eclipseCaster(helper, candle, true, 15);
		ServerPlayer player = captured.player;
		ServerLevel level = helper.getLevel();
		long[] longest = {0L};
		// Read inside the sequence, which runs after every per-tick task: those run in hash order, so a reader there
		// could see the field only a tick after it was hung.
		helper.startSequence().thenWaitUntil(() -> {
			DarkFields.Field field = DarkFields.of(player);
			helper.assertTrue(field != null, "no field of dark hangs where the light was taken");
			longest[0] = Math.max(longest[0], field.remaining(level.getGameTime()));
			helper.assertTrue(field.centre().equals(Vec3.atCenterOf(candle)), "the field hangs at " + field.centre());
			helper.assertTrue(inside.hasEffect(EffectInit.obscured), "a creature inside the field still sees");
			helper.assertTrue(com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.hasAdvancement(player, com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter.ADV_FIELD_OF_DARK), "hanging a field of dark did not grant Snuffed");
		}).thenExecute(() -> {
			try {
				int expected = DarkFieldRules.charge(12);
				helper.assertTrue(expected == 240, "four candles' light charges " + expected);
				helper.assertTrue(longest[0] >= expected - 2 && longest[0] <= expected,
						"the field hung for " + longest[0] + " ticks, not " + expected);
				helper.assertTrue(Math.abs(DarkFieldRules.radius(expected) - 3.6D) < 1.0E-6, "the field's radius is wrong");
				helper.assertTrue(!level.getBlockState(candle).getValue(CandleBlock.LIT), "the candles were not put out");
			} finally {
				inside.discard();
				DarkFields.logout(player);
				captured.close();
			}
		}).thenSucceed();
	}

	/**
	 * The caster standing in their field of dark is hidden from a creature more than four blocks off, as a Veiled body
	 * is: a husk ten blocks away loses the caster, while another husk keeps a player who stands outside it. The field
	 * hides only its caster (CQ-41): a second player standing beside them in it is still seen.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 100)
	public static void aCreatureFarOffLosesSightOfTheCasterInTheField(GameTestHelper helper) {
		BlockPos candle = candleRoom(helper, 4);
		CapturedPlayer captured = eclipseCaster(helper, candle, true, 15);
		ServerPlayer player = captured.player;
		CapturedPlayer outsider = CapturedPlayer.create(helper);
		CapturedPlayer guest = CapturedPlayer.create(helper);
		Vec3 out = feet(helper, 12, 12);
		outsider.player.setPos(out.x, out.y, out.z);
		Husk far = husk(helper, 15, 6);
		Husk watcher = husk(helper, 17, 12);
		far.setTarget(player);
		watcher.setTarget(outsider.player);
		helper.assertTrue(far.getTarget() == player, "the far husk could not see the caster before the field");
		helper.startSequence().thenWaitUntil(() -> {
			helper.assertTrue(DarkFields.of(player) != null, "no field of dark hangs");
			helper.assertTrue(DarkFields.covers(player), "the caster does not stand in their own field");
			helper.assertTrue(far.getTarget() == null, "a husk ten blocks off still sees the caster in the dark");
		}).thenExecute(() -> {
			try {
				helper.assertTrue(!DarkFields.covers(outsider.player), "the field reaches a player outside it");
				guest.player.setPos(player.getX(), player.getY(), player.getZ());
				helper.assertTrue(!DarkFields.covers(guest.player), "the field hides a second player standing in it");
				helper.assertTrue(watcher.getTarget() == outsider.player, "a husk lost a player standing outside the field");
				far.setTarget(player);
				helper.assertTrue(far.getTarget() == null, "a husk could take as its target a player standing in the dark");
			} finally {
				far.discard();
				watcher.discard();
				DarkFields.logout(player);
				outsider.close();
				guest.close();
				captured.close();
			}
		}).thenSucceed();
	}

	/** A field lifts on time: one candle's light (3) hangs for 60 ticks and no more. */
	@GameTest(template = EMPTY, timeoutTicks = 160)
	public static void aFieldOfDarkLiftsOnTime(GameTestHelper helper) {
		BlockPos candle = candleRoom(helper, 1);
		CapturedPlayer captured = eclipseCaster(helper, candle, true, 15);
		ServerPlayer player = captured.player;
		long[] hungAt = {-1L};
		helper.startSequence().thenWaitUntil(() -> helper.assertTrue(DarkFields.of(player) != null, "no field of dark hangs"))
				.thenExecute(() -> hungAt[0] = helper.getTick())
				.thenWaitUntil(() -> helper.assertTrue(DarkFields.of(player) == null, "the field never lifted"))
				.thenExecute(() -> {
					try {
						long hung = helper.getTick() - hungAt[0];
						int expected = DarkFieldRules.charge(3);
						helper.assertTrue(hung >= expected - 2 && hung <= expected + 2,
								"one candle's field hung " + hung + " ticks, not " + expected);
					} finally {
						DarkFields.logout(player);
						captured.close();
					}
				}).thenSucceed();
	}

	/** Nothing to put out, nothing taken, no field. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void snuffingNothingHangsNoField(GameTestHelper helper) {
		BlockPos candle = candleRoom(helper, 0);
		CapturedPlayer captured = eclipseCaster(helper, candle, true, 20);
		ServerPlayer player = captured.player;
		helper.runAtTickTime(25, () -> {
			try {
				helper.assertTrue(DarkFields.of(player) == null, "a field of dark hangs where no light was taken");
			} finally {
				DarkFields.logout(player);
				captured.close();
			}
			helper.succeed();
		});
	}

	/** Temper never refuses: a dry pull still puts the candles out and hangs a field, only a shorter one. */
	@GameTest(template = EMPTY, timeoutTicks = 60)
	public static void aDryPullHangsAShorterFieldNeverNone(GameTestHelper helper) {
		BlockPos candle = candleRoom(helper, 4);
		CapturedPlayer captured = eclipseCaster(helper, candle, false, 15);
		ServerPlayer player = captured.player;
		ServerLevel level = helper.getLevel();
		long[] longest = {0L};
		// Read inside the sequence, which runs after every per-tick task (those run in hash order), so the field is
		// read in the very tick it was hung, at its full length.
		helper.startSequence().thenWaitUntil(() -> {
			DarkFields.Field field = DarkFields.of(player);
			helper.assertTrue(field != null, "a dry pull hung no field");
			longest[0] = field.remaining(level.getGameTime());
		})
				.thenExecute(() -> {
					try {
						int full = DarkFieldRules.charge(12);
						helper.assertTrue(longest[0] > 0 && longest[0] < full - 10,
								"a dry field hung " + longest[0] + " ticks against a tempered " + full);
					} finally {
						DarkFields.logout(player);
						captured.close();
					}
				}).thenSucceed();
	}

	// -- Water (CQ-44): stains wash, gouges break, living veins hold --

	/** A source at (4, 1, 2) walled in on three sides, so the cell east of it is the only way its water can go. */
	private static void walledSource(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos source = helper.absolutePos(new BlockPos(4, 1, 2));
		for (BlockPos wall : new BlockPos[] {source.west(), source.north(), source.south()}) {
			level.setBlock(wall, Blocks.STONE.defaultBlockState(), 3);
		}
		level.setBlock(source, Blocks.WATER.defaultBlockState(), 3);
	}

	private static void clearWater(GameTestHelper helper) {
		for (int x = 2; x <= 7; x++) {
			for (int z = 1; z <= 3; z++) helper.getLevel().setBlock(helper.absolutePos(new BlockPos(x, 1, z)), Blocks.AIR.defaultBlockState(), 3);
		}
	}

	/** Water that flows into a lit gouge's cell takes it, and the cell's clock goes with it (NQ-02-5). */
	@GameTest(template = EMPTY, timeoutTicks = 140)
	public static void flowingWaterWashesAGougeAway(GameTestHelper helper) {
		floor(helper, 0, 8, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = gouge(helper, 5, 2);
		GroundNetwork.pulse(level, cell, GroundFunction.LIGHT, 2_000, null);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(lit(level, cell, GroundFunction.LIGHT), "the gouge never lit"))
				.thenExecute(() -> walledSource(helper))
				.thenWaitUntil(() -> helper.assertTrue(level.getFluidState(cell).is(net.minecraft.tags.FluidTags.WATER),
						"water never reached the gouge's cell: " + level.getBlockState(cell)))
				.thenExecute(() -> {
					try {
						helper.assertTrue(!(level.getBlockState(cell).getBlock() instanceof BloodGougeBlock),
								"flowing water did not wash the gouge away: " + level.getBlockState(cell));
						helper.assertTrue(GroundRunSavedData.get(level).get(cell) == null, "a washed-out gouge kept its clock");
					} finally {
						clearWater(helper);
					}
				}).thenSucceed();
	}

	/** A bucket of water poured onto a gouge takes its cell (NQ-02-5). */
	@GameTest(template = EMPTY, batch = DRAW_AND_WATER, timeoutTicks = 40)
	public static void waterPouredOnAGougeWashesItAway(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = gouge(helper, 4, 2);
		try {
			helper.assertTrue(((net.minecraft.world.item.BucketItem) Items.WATER_BUCKET).emptyContents(null, level, cell, null),
					"the bucket would not pour onto the gouge");
			helper.assertTrue(level.getFluidState(cell).isSourceOfType(Fluids.WATER) && !(level.getBlockState(cell).getBlock() instanceof BloodGougeBlock),
					"poured water left " + level.getBlockState(cell));
		} finally {
			level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
		}
		helper.succeed();
	}

	/**
	 * Flowing water runs over a living vein and on past it, and the vein stays and still pulses (CQ-44, NQ-02-4: a
	 * custom water state, rewritten from the old dam). When the source goes, the water drains off and the vein is dry.
	 */
	@GameTest(template = EMPTY, timeoutTicks = 200)
	public static void flowingWaterRunsOverALivingVein(GameTestHelper helper) {
		floor(helper, 0, 8, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = helper.absolutePos(new BlockPos(5, 1, 2));
		BlockPos past = cell.east();
		BlockPos source = cell.west();
		level.setBlock(cell, face(BlockInit.living_vein.get(), Direction.DOWN), 3);
		// The vein's cell is walled north and south too, so the stream can only run on east, past it.
		level.setBlock(cell.north(), Blocks.STONE.defaultBlockState(), 3);
		level.setBlock(cell.south(), Blocks.STONE.defaultBlockState(), 3);
		walledSource(helper);
		helper.startSequence()
				.thenWaitUntil(() -> helper.assertTrue(level.getFluidState(past).is(net.minecraft.tags.FluidTags.WATER),
						"water did not run on past the vein: " + level.getBlockState(cell) + ", east " + level.getBlockState(past)))
				.thenExecute(() -> {
					BlockState state = level.getBlockState(cell);
					helper.assertTrue(state.getBlock() instanceof LivingVeinBlock, "flowing water broke the vein: " + state);
					helper.assertTrue(state.getValue(LivingVeinBlock.WATER) == 7 && !state.getFluidState().isSource(),
							"the vein does not hold the stream's flowing water: " + state);
					helper.assertTrue(GroundNetwork.pulse(level, cell, GroundFunction.LIGHT, 400, null) == 1, "the vein under water took no pulse");
				})
				.thenWaitUntil(() -> helper.assertTrue(lit(level, cell, GroundFunction.LIGHT), "the vein under water did not light"))
				.thenExecute(() -> level.setBlock(source, Blocks.STONE.defaultBlockState(), 3))
				.thenWaitUntil(() -> helper.assertTrue(level.getBlockState(cell).getValue(LivingVeinBlock.WATER) == LivingVeinBlock.DRY,
						"the water never drained off the vein: " + level.getBlockState(cell)))
				.thenExecute(() -> {
					try {
						helper.assertTrue(level.getBlockState(cell).getBlock() instanceof LivingVeinBlock, "the vein went with the water");
					} finally {
						clearWater(helper);
					}
				}).thenSucceed();
	}

	/** A bucket poured onto a living vein lies over it as a source, and a bucket takes it back up. */
	@GameTest(template = EMPTY, batch = DRAW_AND_WATER, timeoutTicks = 40)
	public static void waterPouredOnALivingVeinLiesOverIt(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = helper.absolutePos(new BlockPos(4, 1, 2));
		level.setBlock(cell, face(BlockInit.living_vein.get(), Direction.DOWN), 3);
		try {
			helper.assertTrue(((net.minecraft.world.item.BucketItem) Items.WATER_BUCKET).emptyContents(null, level, cell, null),
					"the bucket would not pour onto the vein");
			BlockState state = level.getBlockState(cell);
			helper.assertTrue(state.getBlock() instanceof LivingVeinBlock && state.getValue(LivingVeinBlock.WATER) == LivingVeinBlock.SOURCE,
					"poured water left " + state);
			ItemStack taken = ((LivingVeinBlock) state.getBlock()).pickupBlock(null, level, cell, state);
			helper.assertTrue(taken.is(Items.WATER_BUCKET) && level.getBlockState(cell).getValue(LivingVeinBlock.WATER) == LivingVeinBlock.DRY,
					"a bucket did not take the source back up");
		} finally {
			level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
		}
		helper.succeed();
	}

	/** Absorption filling in the last face of a vein under a source leaves the water standing in the cell. */
	@GameTest(template = EMPTY, timeoutTicks = 40)
	public static void aWaterloggedVeinFilledInLeavesItsWater(GameTestHelper helper) {
		floor(helper, 0, 6, 1, 3);
		ServerLevel level = helper.getLevel();
		BlockPos cell = helper.absolutePos(new BlockPos(4, 1, 2));
		level.setBlock(cell, face(BlockInit.living_vein.get(), Direction.DOWN).setValue(LivingVeinBlock.WATER, LivingVeinBlock.SOURCE), 3);
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			prepare(player, null, 1_000.0D);
			aim(player, feet(helper, 1, 2), floorTop(helper, 4, 2));
			absorbTick(player);
			helper.assertTrue(level.getFluidState(cell).isSourceOfType(Fluids.WATER),
					"filling in a waterlogged vein left " + level.getBlockState(cell));
			helper.assertTrue(!(level.getBlockState(cell).getBlock() instanceof BloodGougeBlock), "Absorption did not fill the vein in");
		} finally {
			level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
		}
		helper.succeed();
	}

	/** A waterlogged vein that withers back to gouge is washed out: the cell keeps its water and no gouge (section 2, 02 T6). */
	@GameTest(template = EMPTY, batch = DRAW_AND_WATER, timeoutTicks = 60)
	public static void aWitheredVeinUnderWaterIsWashedOut(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer owner = captured.player;
			prepare(owner, null, 1_000.0D);
			BlockPos[] tanks = veinBetweenTanks(helper, owner);
			BlockPos wet = helper.absolutePos(new BlockPos(4, 1, 2));
			level.setBlock(wet, level.getBlockState(wet).setValue(LivingVeinBlock.WATER, LivingVeinBlock.SOURCE), 3);
			long longAgo = level.getGameTime() - GroundRunRules.VEIN_WITHER_TICKS - 100L;
			((VeinNodeBlockEntity) level.getBlockEntity(tanks[0].east())).setOwner(owner.getUUID(), longAgo);
			((VeinNodeBlockEntity) level.getBlockEntity(tanks[1].west())).setOwner(owner.getUUID(), longAgo);
			helper.runAtTickTime(30, () -> {
				try {
					helper.assertTrue(level.getFluidState(wet).isSourceOfType(Fluids.WATER) && !(level.getBlockState(wet).getBlock() instanceof BloodGougeBlock),
							"a withered vein under water left " + level.getBlockState(wet));
				} finally {
					clearWater(helper);
				}
				helper.succeed();
			});
		}
	}
}
