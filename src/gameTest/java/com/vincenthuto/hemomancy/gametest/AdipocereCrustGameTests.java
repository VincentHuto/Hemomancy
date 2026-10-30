package com.vincenthuto.hemomancy.gametest;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.vincenthuto.hemomancy.common.block.harbinger.plant.AdipocereCrustBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.world.AdipocereCrustDecoration;
import com.vincenthuto.hemomancy.common.world.AdipocereCrustDecoration.Site;
import com.vincenthuto.hemomancy.common.world.AdipocereCrustRules.Host;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Adipocere crusts: the decoration pass on a real stronghold piece and a monster room, and the scrape. */
@GameTestHolder("adipocere_crust_validation")
@PrefixGameTestTemplate(false)
public final class AdipocereCrustGameTests {
	private AdipocereCrustGameTests() {
	}

	/** A vanilla stronghold corridor gets crusts on its walls only, never on floor or ceiling, never outside it. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void strongholdCorridorCrustsOnWallsOnly(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos origin = helper.absolutePos(new BlockPos(12, 1, 8));
		BoundingBox box = BoundingBox.orientBox(origin.getX(), origin.getY(), origin.getZ(), -1, -1, 0, 5, 5, 7,
				Direction.SOUTH);
		// Stronghold pieces only replace solid ground, as underground; bury the corridor in stone first.
		BlockPos.betweenClosed(box.minX() - 1, box.minY() - 1, box.minZ() - 1, box.maxX() + 1, box.maxY() + 1,
				box.maxZ() + 1).forEach(pos -> level.setBlock(pos, Blocks.STONE.defaultBlockState(), 2));
		var piece = new StrongholdPieces.Straight(0, RandomSource.create(7L), box, Direction.SOUTH);
		piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(),
				RandomSource.create(7L), box, new ChunkPos(origin), origin);

		Set<BlockPos> placed = new LinkedHashSet<>();
		for (ChunkPos chunk : chunksOf(box)) {
			// Each key is a fresh site seed; a stronghold chunk site takes up to two at density 10.
			for (long key = 0; key < 24; key++) {
				placed.addAll(AdipocereCrustDecoration.place(level, chunk, List.of(new Site(Host.STRONGHOLD, key,
						List.of(box))), 10.0));
			}
		}
		int walls = 0, air = 0;
		for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
			if (level.getBlockState(pos).is(AdipocereCrustDecoration.WALLS)) walls++;
			if (level.getBlockState(pos).isAir()) air++;
		}
		helper.assertTrue(walls > 40 && air > 20, "the corridor did not generate: " + walls + " masonry, " + air + " air");
		helper.assertTrue(!placed.isEmpty(), "the corridor should take at least one crust");
		for (BlockPos pos : placed) {
			helper.assertTrue(box.isInside(pos), "crust outside the piece at " + pos);
			assertOnWall(helper, level, pos);
		}
		helper.succeed();
	}

	/** A hand-built monster room takes its two crusts, and a recorded chunk is never done twice. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void monsterRoomTakesTwoCrustsAndOnlyOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos spawner = helper.absolutePos(new BlockPos(6, 2, 6));
		for (BlockPos pos : BlockPos.betweenClosed(spawner.offset(-3, -1, -3), spawner.offset(3, 4, 3))) {
			boolean shell = Math.abs(pos.getX() - spawner.getX()) == 3 || Math.abs(pos.getZ() - spawner.getZ()) == 3
					|| pos.getY() == spawner.getY() - 1 || pos.getY() == spawner.getY() + 4;
			level.setBlockAndUpdate(pos, shell ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
		}
		level.setBlockAndUpdate(spawner, Blocks.SPAWNER.defaultBlockState());

		Site room = AdipocereCrustDecoration.monsterRoom(spawner);
		ChunkPos chunk = new ChunkPos(spawner);
		List<BlockPos> placed = AdipocereCrustDecoration.place(level, chunk, List.of(room), 1.0);
		helper.assertTrue(placed.size() == 2, "a room takes two crusts, got " + placed.size());
		placed.forEach(pos -> assertOnWall(helper, level, pos));

		// The recorded pass runs once per chunk, then never again.
		ChunkPos fresh = new ChunkPos(chunk.x + 4096, chunk.z + 4096);
		AdipocereCrustDecoration.decorateOnce(level, fresh, List.of(), 1.0);
		helper.assertTrue(AdipocereCrustDecoration.decorateOnce(level, fresh, List.of(room), 2.0) == 0,
				"a recorded chunk must not be decorated again");
		helper.succeed();
	}

	/** Scraping by hand drops one adipocere; an axe drops two; the crust does not come back. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void scrapingDropsWaxOnce(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockPos hand = new BlockPos(4, 2, 4);
		BlockPos axe = new BlockPos(8, 2, 4);
		for (BlockPos pos : List.of(hand, axe)) {
			helper.setBlock(pos.south(), Blocks.STONE_BRICKS);
			helper.setBlock(pos, BlockInit.adipocere_crust.get().defaultBlockState()
					.setValue(AdipocereCrustBlock.FACING, Direction.NORTH));
		}

		player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		use(helper, player, hand);
		helper.assertBlockNotPresent(BlockInit.adipocere_crust.get(), hand);
		helper.assertTrue(waxNear(helper, hand) == 1, "a hand scrape gives one adipocere");

		player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
		use(helper, player, axe);
		helper.assertBlockNotPresent(BlockInit.adipocere_crust.get(), axe);
		helper.assertTrue(waxNear(helper, axe) == 2, "an axe scrape gives two adipocere");
		helper.succeed();
	}

	/** The crafted crust hangs on the wall face it is placed against and falls when the wall goes. */
	@GameTest(template = "empty", timeoutTicks = 40)
	public static void craftedCrustHangsOnTheClickedWall(GameTestHelper helper) {
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		BlockPos wall = new BlockPos(6, 2, 6);
		helper.setBlock(wall, Blocks.STONE_BRICKS);
		ItemStack crust = new ItemStack(BlockInit.adipocere_crust.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, crust);
		helper.placeAt(player, crust, wall, Direction.NORTH);
		helper.assertBlockProperty(wall.north(), AdipocereCrustBlock.FACING, Direction.NORTH);
		// Pulling the wall out takes the crust with it.
		helper.setBlock(wall, Blocks.AIR);
		helper.assertBlockNotPresent(BlockInit.adipocere_crust.get(), wall.north());
		helper.succeed();
	}

	private static void use(GameTestHelper helper, net.minecraft.world.entity.player.Player player, BlockPos local) {
		BlockPos pos = helper.absolutePos(local);
		BlockState state = helper.getLevel().getBlockState(pos);
		BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false);
		ItemStack held = player.getMainHandItem();
		if (held.isEmpty()) {
			state.useWithoutItem(helper.getLevel(), player, hit);
		} else {
			state.useItemOn(held, helper.getLevel(), player, InteractionHand.MAIN_HAND, hit);
		}
	}

	private static int waxNear(GameTestHelper helper, BlockPos local) {
		AABB area = new AABB(helper.absolutePos(local)).inflate(1.5);
		return helper.getLevel().getEntitiesOfClass(ItemEntity.class, area,
				item -> item.getItem().is(ItemInit.adipocere.get())).stream()
				.mapToInt(item -> item.getItem().getCount()).sum();
	}

	private static void assertOnWall(GameTestHelper helper, ServerLevel level, BlockPos pos) {
		BlockState crust = level.getBlockState(pos);
		helper.assertTrue(crust.is(BlockInit.adipocere_crust.get()), "no crust at " + pos);
		Direction facing = crust.getValue(AdipocereCrustBlock.FACING);
		helper.assertTrue(facing.getAxis().isHorizontal(), "crust must face out of a wall");
		BlockPos wall = pos.relative(facing.getOpposite());
		helper.assertTrue(level.getBlockState(wall).is(AdipocereCrustDecoration.WALLS),
				"crust at " + pos + " is not on masonry: " + level.getBlockState(wall));
	}

	private static List<ChunkPos> chunksOf(BoundingBox box) {
		List<ChunkPos> chunks = new ArrayList<>();
		for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
			for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) chunks.add(new ChunkPos(x, z));
		}
		return chunks;
	}
}
