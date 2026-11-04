package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.block.harbinger.plant.BloodwoodGrowthHandler;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.BloodwoodGrowthRules;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.HeartwoodCoreBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.HeartwoodCoreRules;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.HeartwoodTreeRules;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.WoodSet;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.HeartwoodCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/**
 * Growing trees by Blood Projection, the Heartwood tree's hunger and core, and the two wood sets as real blocks. Each
 * tree test has a batch of its own, because a Heartwood tree stands well above its test structure.
 */
@GameTestHolder("bloodwood_validation")
@PrefixGameTestTemplate(false)
public final class BloodwoodGameTests {
	private static final int PLANT_HEIGHT = 12;

	private BloodwoodGameTests() {
	}

	private static ServerPlayer feeder(CapturedPlayer captured) {
		ServerPlayer player = captured.player;
		var volume = HemoCapabilityAccess.getBloodVolume(player);
		volume.setActive(true);
		volume.setMaxBloodVolume(6000);
		volume.fill(6000);
		return player;
	}

	/** A plant on a floor of its own, in air cleared of any neighbouring test's tree and clear of this test's markers. */
	private static BlockPos plant(GameTestHelper helper, Block plant) {
		BlockPos pos = helper.absolutePos(new BlockPos(1, PLANT_HEIGHT, 1));
		ServerLevel level = helper.getLevel();
		clearAround(level, pos);
		level.setBlock(pos.below(), plant == Blocks.DEAD_BUSH ? Blocks.SAND.defaultBlockState() : Blocks.DIRT.defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(pos, plant.defaultBlockState(), Block.UPDATE_ALL);
		return pos;
	}

	@GameTest(template = "empty", batch = "bloodwood_saplings")
	public static void anySaplingGrowsBloodWood(GameTestHelper helper) {
		for (Block sapling : List.of(Blocks.OAK_SAPLING, Blocks.CHERRY_SAPLING, Blocks.DARK_OAK_SAPLING)) {
			try (var captured = CapturedPlayer.create(helper)) {
				ServerPlayer player = feeder(captured);
				BlockPos pos = plant(helper, sapling);
				double taken = BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, BloodwoodGrowthRules.PROJECTION_BLOOD_COST);
				helper.assertTrue(taken == BloodwoodGrowthRules.PROJECTION_BLOOD_COST, sapling + " took " + taken);
				helper.assertTrue(helper.getLevel().getBlockState(pos).is(BlockInit.blood_wood_log.get()), sapling + " did not become a blood wood log");
				clearAround(helper.getLevel(), pos);
			}
		}
		whenDone(helper, helper.absolutePos(new BlockPos(1, PLANT_HEIGHT, 1)));
	}

	/** Clears the air a tree needs; {@link #whenDone} runs it again so no tree reaches into a later test's plot. */
	private static void clearAround(ServerLevel level, BlockPos pos) {
		for (BlockPos at : BlockPos.betweenClosed(pos.offset(-16, -1, -16), pos.offset(16, 44, 16))) {
			if (!level.getBlockState(at).isAir()) level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	private static void whenDone(GameTestHelper helper, BlockPos pos) {
		clearAround(helper.getLevel(), pos);
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "bloodwood_partial")
	public static void aSaplingKeepsWhatItDrankAcrossCasts(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = feeder(captured);
			BlockPos pos = plant(helper, Blocks.OAK_SAPLING);
			BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, 400);
			helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.OAK_SAPLING), "400 mL must not grow it yet");
			BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, 500);
			helper.assertTrue(helper.getLevel().getBlockState(pos).is(BlockInit.blood_wood_log.get()), "900 mL across two casts grows it");
		}
		whenDone(helper, helper.absolutePos(new BlockPos(1, PLANT_HEIGHT, 1)));
	}

	@GameTest(template = "empty", batch = "bloodwood_shape")
	public static void aSaplingGrowsTheLimbsAndHangingLeavesOfItsShape(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = feeder(captured);
			BlockPos pos = plant(helper, Blocks.OAK_SAPLING);
			BloodwoodGrowthRules.Shape shape = BloodwoodGrowthRules.grow(pos.asLong());
			BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, BloodwoodGrowthRules.PROJECTION_BLOOD_COST);
			for (HeartwoodTreeRules.Cell cell : shape.logs()) {
				helper.assertTrue(helper.getLevel().getBlockState(pos.offset(cell.x(), cell.y(), cell.z())).is(BlockInit.blood_wood_log.get()), "no log at " + cell);
			}
			for (BloodwoodGrowthRules.Offset leaf : shape.leaves()) {
				helper.assertTrue(helper.getLevel().getBlockState(pos.offset(leaf.x(), leaf.y(), leaf.z())).is(BlockInit.blood_wood_leaves.get()), "no leaf at " + leaf);
			}
		}
		whenDone(helper, helper.absolutePos(new BlockPos(1, PLANT_HEIGHT, 1)));
	}

	@GameTest(template = "empty", batch = "bloodwood_blocked")
	public static void leavesGrowAroundABlockButWoodDoesNot(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = feeder(captured);
			BlockPos pos = plant(helper, Blocks.OAK_SAPLING);
			BloodwoodGrowthRules.Shape shape = BloodwoodGrowthRules.grow(pos.asLong());
			BloodwoodGrowthRules.Offset leaf = shape.leaves().iterator().next();
			BlockPos inLeaves = pos.offset(leaf.x(), leaf.y(), leaf.z());
			HeartwoodTreeRules.Cell limb = shape.logs().getLast();
			BlockPos inWood = pos.offset(limb.x(), limb.y(), limb.z());
			helper.getLevel().setBlock(inWood, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			double before = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
			helper.assertTrue(BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, BloodwoodGrowthRules.PROJECTION_BLOOD_COST) == 0.0D
					&& HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() == before, "stone in a limb's path must refuse the growth and take no blood");
			helper.getLevel().setBlock(inWood, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
			helper.getLevel().setBlock(inLeaves, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
			BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, BloodwoodGrowthRules.PROJECTION_BLOOD_COST);
			helper.assertTrue(helper.getLevel().getBlockState(pos).is(BlockInit.blood_wood_log.get()), "stone among the leaves must not stop the tree");
			helper.assertTrue(helper.getLevel().getBlockState(inLeaves).is(Blocks.STONE), "a leaf replaced the stone");
		}
		whenDone(helper, helper.absolutePos(new BlockPos(1, PLANT_HEIGHT, 1)));
	}

	@GameTest(template = "empty", batch = "bloodwood_adventure")
	public static void anAdventurePlayerCannotGrowATree(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = feeder(captured);
			player.gameMode.changeGameModeForPlayer(GameType.ADVENTURE);
			for (Block plant : List.of(Blocks.OAK_SAPLING, Blocks.DEAD_BUSH)) {
				BlockPos pos = plant(helper, plant);
				double before = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
				double taken = BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, HeartwoodTreeRules.PROJECTION_BLOOD_COST);
				helper.assertTrue(taken == 0.0D && HemoCapabilityAccess.getBloodVolume(player).getBloodVolume() == before, plant + " took blood from an adventure player");
				helper.assertTrue(helper.getLevel().getBlockState(pos).is(plant), plant + " grew for an adventure player");
			}
		}
		whenDone(helper, helper.absolutePos(new BlockPos(1, PLANT_HEIGHT, 1)));
	}

	@GameTest(template = "empty", batch = "bloodwood_other")
	public static void otherPlantsTakeNoBlood(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = feeder(captured);
			BlockPos pos = plant(helper, Blocks.POPPY);
			helper.assertTrue(BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, 900) == 0.0D, "a poppy drank blood");
		}
		whenDone(helper, helper.absolutePos(new BlockPos(1, PLANT_HEIGHT, 1)));
	}

	@GameTest(template = "empty", batch = "bloodwood_heartwood_grow")
	public static void aDeadBushGrowsALeaflessHeartwoodTreeWithOneCore(GameTestHelper helper) {
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = feeder(captured);
			BlockPos pos = plant(helper, Blocks.DEAD_BUSH);
			double half = HeartwoodTreeRules.PROJECTION_BLOOD_COST / 2;
			BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, half);
			helper.assertTrue(helper.getLevel().getBlockState(pos).is(Blocks.DEAD_BUSH), "half the blood must not grow it");
			BloodwoodGrowthHandler.tryGrowFromProjection(helper.getLevel(), pos, player, half);
			HeartwoodTreeRules.Shape shape = HeartwoodTreeRules.grow(pos.asLong());
			for (HeartwoodTreeRules.Cell cell : shape.logs()) {
				helper.assertTrue(helper.getLevel().getBlockState(pos.offset(cell.x(), cell.y(), cell.z())).is(BlockInit.HEARTWOOD.log().get()),
						"no log at " + cell);
			}
			helper.assertTrue(helper.getLevel().getBlockState(pos.above(HeartwoodTreeRules.CORE_HEIGHT)).is(BlockInit.heartwood_core.get()), "no core");
			int cores = 0;
			int leaves = 0;
			for (BlockPos at : BlockPos.betweenClosed(pos.offset(-16, 0, -16), pos.offset(16, 44, 16))) {
				BlockState state = helper.getLevel().getBlockState(at);
				if (state.is(BlockInit.heartwood_core.get())) cores++;
				if (state.is(net.minecraft.tags.BlockTags.LEAVES)) leaves++;
			}
			helper.assertTrue(cores == 1, "expected one core, found " + cores);
			helper.assertTrue(leaves == 0, "found leaves: " + leaves);
		}
		whenDone(helper, helper.absolutePos(new BlockPos(1, PLANT_HEIGHT, 1)));
	}

	/** A trunk log beside a core, bound together, standing on the test's floor. */
	private static HeartwoodCoreBlockEntity smallTree(GameTestHelper helper, BlockPos corePos, BlockPos logPos) {
		ServerLevel level = helper.getLevel();
		level.setBlock(logPos, BlockInit.HEARTWOOD.log().get().defaultBlockState(), Block.UPDATE_ALL);
		level.setBlock(corePos, BlockInit.heartwood_core.get().defaultBlockState(), Block.UPDATE_ALL);
		HeartwoodCoreBlockEntity core = (HeartwoodCoreBlockEntity) level.getBlockEntity(corePos);
		core.bindTree(List.of(logPos));
		return core;
	}

	@GameTest(template = "empty", batch = "bloodwood_core_item")
	public static void theCoreItemPlacesAHungryEmptyCore(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos floor = helper.absolutePos(new BlockPos(1, 1, 1));
		level.setBlock(floor, Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
		BlockPos placed = floor.above();
		helper.assertTrue(BlockInit.heartwood_core.get().asItem() != Items.AIR, "the core has no item");
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BlockInit.heartwood_core.get()));
			player.getMainHandItem().useOn(new UseOnContext(level, player, InteractionHand.MAIN_HAND, player.getMainHandItem(),
					new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false)));
		}
		helper.assertTrue(level.getBlockState(placed).is(BlockInit.heartwood_core.get()), "the item placed no core");
		helper.assertTrue(level.getBlockState(placed).getValue(HeartwoodCoreBlock.FED) == 0, "a placed core starts unfed");
		HeartwoodCoreBlockEntity core = (HeartwoodCoreBlockEntity) level.getBlockEntity(placed);
		Cow cow = EntityType.COW.create(level);
		cow.setNoAi(true);
		cow.moveTo(placed.getX() + 1.5D, placed.getY(), placed.getZ() + 0.5D);
		level.addFreshEntity(cow);
		core.pulse(level);
		helper.assertTrue(cow.hasEffect(EffectInit.blood_loss), "a placed core does not bite what touches it");
		helper.assertTrue(core.fed() == HeartwoodCoreRules.MOB_FEED, "a placed core fed " + core.fed());
		cow.discard();
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "bloodwood_touch")
	public static void aMobTouchingTheTreeBleedsAndFeedsTheCore(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
		BlockPos logPos = corePos.above();
		HeartwoodCoreBlockEntity core = smallTree(helper, corePos, logPos);
		Cow touching = EntityType.COW.create(level);
		touching.setNoAi(true);
		touching.moveTo(logPos.getX() + 1.5D, logPos.getY(), logPos.getZ() + 0.5D);
		level.addFreshEntity(touching);
		Cow away = EntityType.COW.create(level);
		away.setNoAi(true);
		away.moveTo(corePos.getX() + 6.5D, corePos.getY(), corePos.getZ() + 0.5D);
		level.addFreshEntity(away);
		core.pulse(level);
		helper.assertTrue(touching.hasEffect(EffectInit.blood_loss), "the touching cow is not bleeding");
		helper.assertTrue(!away.hasEffect(EffectInit.blood_loss), "a cow six blocks off is bleeding");
		helper.assertTrue(core.fed() == HeartwoodCoreRules.MOB_FEED, "the core fed " + core.fed());
		touching.discard();
		away.discard();
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "bloodwood_bloodless")
	public static void theBloodlessFeedNothing(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
		HeartwoodCoreBlockEntity core = smallTree(helper, corePos, corePos.above());
		Skeleton skeleton = EntityType.SKELETON.create(level);
		skeleton.setNoAi(true);
		skeleton.moveTo(corePos.getX() + 1.5D, corePos.getY(), corePos.getZ() + 0.5D);
		level.addFreshEntity(skeleton);
		core.pulse(level);
		helper.assertTrue(!skeleton.hasEffect(EffectInit.blood_loss) && core.fed() == 0.0D, "a skeleton bled");
		skeleton.discard();
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "bloodwood_player")
	public static void aPlayerNearTheTreeBleedsAndAFarOneDoesNot(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
		HeartwoodCoreBlockEntity core = smallTree(helper, corePos, corePos.above());
		try (var near = CapturedPlayer.create(helper); var far = CapturedPlayer.create(helper); var creative = CapturedPlayer.create(helper)) {
			near.joinLevel();
			far.joinLevel();
			creative.joinLevel();
			near.player.moveTo(corePos.getX() + 3.5D, corePos.getY(), corePos.getZ() + 0.5D);
			far.player.moveTo(corePos.getX() + 12.5D, corePos.getY(), corePos.getZ() + 0.5D);
			creative.player.moveTo(corePos.getX() + 1.5D, corePos.getY(), corePos.getZ() + 0.5D);
			creative.player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
			core.pulse(level);
			helper.assertTrue(near.player.hasEffect(EffectInit.blood_loss), "the near player is not bleeding");
			helper.assertTrue(!far.player.hasEffect(EffectInit.blood_loss), "a far player is bleeding");
			helper.assertTrue(!creative.player.hasEffect(EffectInit.blood_loss), "a creative player is bleeding");
			helper.assertTrue(core.fed() == HeartwoodCoreRules.PLAYER_FEED, "the core fed " + core.fed());
		}
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "bloodwood_cut")
	public static void aTreeWhoseLogIsGoneNoLongerBitesThere(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
		BlockPos logPos = corePos.above(3);
		HeartwoodCoreBlockEntity core = smallTree(helper, corePos, logPos);
		level.setBlock(logPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
		// A log someone placed where no tree grew is inert too.
		BlockPos placed = corePos.offset(5, 0, 0);
		level.setBlock(placed, BlockInit.HEARTWOOD.log().get().defaultBlockState(), Block.UPDATE_ALL);
		Cow cow = EntityType.COW.create(level);
		cow.setNoAi(true);
		cow.moveTo(logPos.getX() + 1.5D, logPos.getY(), logPos.getZ() + 0.5D);
		level.addFreshEntity(cow);
		Cow other = EntityType.COW.create(level);
		other.setNoAi(true);
		other.moveTo(placed.getX() + 1.5D, placed.getY(), placed.getZ() + 0.5D);
		level.addFreshEntity(other);
		core.pulse(level);
		helper.assertTrue(!cow.hasEffect(EffectInit.blood_loss), "a cut log still bites");
		helper.assertTrue(!other.hasEffect(EffectInit.blood_loss), "a placed log bites");
		cow.discard();
		other.discard();
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "bloodwood_project")
	public static void projectionFillsTheCoreThroughEveryStage(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
		HeartwoodCoreBlockEntity core = smallTree(helper, corePos, corePos.above());
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = feeder(captured);
			var endpoint = (HeartwoodCoreBlock) level.getBlockState(corePos).getBlock();
			int lastStage = 0;
			double before = HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
			for (int stage = 1; stage <= HeartwoodCoreRules.RAVENOUS_STAGE; stage++) {
				endpoint.projectBloodIntoBlock(level, corePos, level.getBlockState(corePos), player, HeartwoodCoreRules.FULL / HeartwoodCoreRules.RAVENOUS_STAGE);
				int shown = level.getBlockState(corePos).getValue(HeartwoodCoreBlock.FED);
				helper.assertTrue(shown == lastStage + 1, "stage " + stage + " shows " + shown);
				helper.assertTrue(level.getBlockState(corePos).getLightEmission(level, corePos) == HeartwoodCoreRules.light(stage), "light at stage " + stage);
				lastStage = shown;
			}
			double paid = before - HemoCapabilityAccess.getBloodVolume(player).getBloodVolume();
			helper.assertTrue(Math.abs(paid - HeartwoodCoreRules.FULL) < 0.01D, "paid " + paid);
			helper.assertTrue(endpoint.projectBloodIntoBlock(level, corePos, level.getBlockState(corePos), player, 100) == 0.0D, "a full core took more");
			helper.assertTrue(core.fed() == HeartwoodCoreRules.FULL, "fed " + core.fed());
		}
		helper.succeed();
	}

	private static List<ItemStack> drops(ServerLevel level, BlockPos pos) {
		return Block.getDrops(level.getBlockState(pos), level, pos, level.getBlockEntity(pos), null, new ItemStack(Items.IRON_AXE));
	}

	@GameTest(template = "empty", batch = "bloodwood_drops")
	public static void onlyAFullCoreDropsRavenousBloodwood(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
		HeartwoodCoreBlockEntity core = smallTree(helper, corePos, corePos.above());
		core.feed(level, HeartwoodCoreRules.FULL - 1);
		List<ItemStack> early = drops(level, corePos);
		helper.assertTrue(early.size() == 1 && early.getFirst().is(BlockInit.HEARTWOOD.log().get().asItem()), "an unfilled core dropped " + early);
		core.feed(level, 1);
		List<ItemStack> full = drops(level, corePos);
		helper.assertTrue(full.size() == 1 && full.getFirst().is(BlockInit.ravenous_heartwood.get().asItem()), "a full core dropped " + full);
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "bloodwood_save")
	public static void theCoreKeepsItsFeedAndTreeAcrossASave(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos corePos = helper.absolutePos(new BlockPos(1, 2, 1));
		BlockPos logPos = corePos.above();
		HeartwoodCoreBlockEntity core = smallTree(helper, corePos, logPos);
		core.feed(level, 500);
		var tag = core.saveWithoutMetadata(level.registryAccess());
		core.feed(level, 900);
		core.bindTree(List.of());
		core.loadWithComponents(tag, level.registryAccess());
		helper.assertTrue(core.fed() == 500, "fed " + core.fed());
		Cow cow = EntityType.COW.create(level);
		cow.setNoAi(true);
		cow.moveTo(logPos.getX() + 1.5D, logPos.getY(), logPos.getZ() + 0.5D);
		level.addFreshEntity(cow);
		core.pulse(level);
		helper.assertTrue(cow.hasEffect(EffectInit.blood_loss), "the loaded core forgot its log");
		cow.discard();
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "bloodwood_strip")
	public static void anAxeStripsEverySetsLogAndBark(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
		try (var captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = captured.player;
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
			for (WoodSet set : WoodSet.all()) {
				for (var pair : List.of(List.of(set.log(), set.strippedLog()), List.of(set.bark(), set.strippedBark()))) {
					level.setBlock(pos, pair.get(0).get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z), Block.UPDATE_ALL);
					UseOnContext context = new UseOnContext(level, player, InteractionHand.MAIN_HAND, player.getMainHandItem(),
							new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
					player.getMainHandItem().useOn(context);
					BlockState stripped = level.getBlockState(pos);
					helper.assertTrue(stripped.is(pair.get(1).get()), pair.get(0).getId() + " stripped to " + stripped);
					helper.assertTrue(stripped.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Z, "stripping lost the axis");
				}
			}
		}
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "bloodwood_pieces")
	public static void everySetIsMadeOfWorkingVanillaPieces(GameTestHelper helper) {
		for (WoodSet set : WoodSet.all()) {
			helper.assertTrue(BlockEntityType.SIGN.isValid(set.sign().get().defaultBlockState()), set.name() + " sign has no block entity");
			helper.assertTrue(BlockEntityType.SIGN.isValid(set.wallSign().get().defaultBlockState()), set.name() + " wall sign has no block entity");
			helper.assertTrue(BlockEntityType.HANGING_SIGN.isValid(set.hangingSign().get().defaultBlockState()), set.name() + " hanging sign has no block entity");
			helper.assertTrue(BlockEntityType.HANGING_SIGN.isValid(set.wallHangingSign().get().defaultBlockState()), set.name() + " wall hanging sign has no block entity");
			helper.assertTrue(new ItemStack(set.planks().get()).is(ItemTags.PLANKS), set.name() + " planks are not planks, so no sticks or crafting table");
			helper.assertTrue(new ItemStack(set.log().get()).is(ItemTags.LOGS), set.name() + " logs are not logs");
			helper.assertTrue(set.sign().get().asItem() != Items.AIR && set.wallSign().get().asItem() == set.sign().get().asItem(), set.name() + " wall sign has its own item");
			helper.assertTrue(set.door().get().asItem() instanceof net.minecraft.world.item.DoubleHighBlockItem, set.name() + " door places one half");
			helper.assertTrue(new ItemStack(set.sign().get()).getMaxStackSize() == 16, set.name() + " sign stacks wrong");
		}
		ServerLevel level = helper.getLevel();
		var crafting = level.getRecipeManager().byKey(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hemomancy", "heartwood_planks"));
		helper.assertTrue(crafting.isPresent(), "Heartwood planks have no recipe");
		helper.succeed();
	}
}
