package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.zone.RiteBoundEvents;
import com.vincenthuto.hemomancy.common.rite.zone.RiteSpaces;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

/** A completed rite's floor keeps a rite space; rite-bound blocks are set down only inside one of their purpose. */
@GameTestHolder("rite_space_validation")
@PrefixGameTestTemplate(false)
public final class RiteSpaceGameTests {
	private RiteSpaceGameTests() {}

	@GameTest(template = "empty", batch = "rite_space", timeoutTicks = 120)
	public static void completedRiteConsecratesItsFloorUntilTheFloorBreaks(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos center = helper.absolutePos(new BlockPos(4, 2, 4));
		ResourceLocation floorId = Hemomancy.rloc("working_minor");
		layFloor(level, center, CardinalRiteFloorRegistry.get(floorId).orElseThrow());
		consecrate(helper, center, Hemomancy.rloc("cardinal_rite/working/resonant_forge"), floorId);

		helper.assertTrue(RiteSpaces.purposeAt(level, center.above()).orElse(null) == CardinalRitePurpose.MAKING,
				"the Working craft's floor should keep a Working rite space");
		helper.assertTrue(RiteSpaces.purposeAt(level, center.offset(2, 1, 0)).isEmpty(), "past the floor's edge is outside");
		helper.assertTrue(RiteSpaces.purposeAt(level, center).isEmpty(), "the floor layer itself is not in the space");
		// Two Focus floor checks (every 20 ticks) see the floor still formed.
		helper.runAfterDelay(45, () -> {
			helper.assertTrue(RiteSpaces.purposeAt(level, center.above()).isPresent(), "an intact floor keeps its space");
			level.setBlockAndUpdate(center.offset(1, 0, 1), Blocks.DIRT.defaultBlockState());
			helper.runAfterDelay(45, () -> {
				helper.assertTrue(RiteSpaces.purposeAt(level, center.above()).isEmpty(), "a broken floor releases its space");
				helper.succeed();
			});
		});
	}

	@GameTest(template = "empty", batch = "rite_space", timeoutTicks = 40)
	public static void sanguineOmenIsSetDownOnlyInsideADominionSpace(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos center = helper.absolutePos(new BlockPos(4, 2, 4));
		// A bare Focus with no floor: its first floor check (within a second) would release the space, so assert at once.
		level.setBlockAndUpdate(center, BlockInit.cardinal_focus.get().defaultBlockState());
		consecrate(helper, center, Hemomancy.rloc("cardinal_rite/rooted_vein"), Hemomancy.rloc("dominion_lesser"));
		BlockState omen = BlockInit.sanguine_omen.get().defaultBlockState();
		Player player = helper.makeMockPlayer(GameType.SURVIVAL);

		helper.assertTrue(!RiteBoundEvents.refusesPlacement(level, center.above(), omen, player),
				"a Dominion rite space admits the Sanguine Omen");
		helper.assertTrue(RiteBoundEvents.refusesPlacement(level, center.offset(6, 1, 0), omen, player),
				"outside every rite space the Omen cannot be set down");
		helper.assertTrue(!RiteBoundEvents.refusesPlacement(level, center.offset(6, 1, 0), omen, null),
				"non-player placement is not stopped");
		helper.assertTrue(!RiteBoundEvents.refusesPlacement(level, center.offset(6, 1, 0),
				Blocks.STONE.defaultBlockState(), player), "untagged blocks are never refused");
		helper.succeed();
	}

	@GameTest(template = "empty", batch = "rite_space", timeoutTicks = 40)
	public static void aRiteWhoseEffectRemovesItsFocusLeavesNoSpace(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		BlockPos center = helper.absolutePos(new BlockPos(4, 2, 4));
		// Rooted Vein's completion turns the Focus into venous stone before the rite ends.
		level.setBlockAndUpdate(center, BlockInit.venous_stone.get().defaultBlockState());
		consecrate(helper, center, Hemomancy.rloc("cardinal_rite/rooted_vein"), Hemomancy.rloc("dominion_lesser"));
		helper.assertTrue(RiteSpaces.purposeAt(level, center.above()).isEmpty(),
				"no Focus is left to keep the space, so none may be recorded");
		helper.succeed();
	}

	private static void consecrate(GameTestHelper helper, BlockPos center, ResourceLocation riteId, ResourceLocation floorId) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(helper.getLevel(), riteId);
		helper.assertTrue(recipe != null, riteId + " is missing");
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), center, riteId, 2400, 3, 3, false, 0);
		rite.setMatchedFloor(floorId, Direction.NORTH, Direction.UP);
		RiteSpaces.consecrate(helper.getLevel(), rite, recipe);
	}

	private static void layFloor(ServerLevel level, BlockPos center, CardinalRiteFloorDefinition floor) {
		for (var pair : floor.pattern().getBlockPosBlockList()) {
			BlockPos p = pair.getPos();
			if (pair.getBlock() != null) level.setBlock(center.offset(p.getX() - floor.focus().getX(),
					p.getY(), floor.focus().getZ() - p.getZ()), pair.getBlock().defaultBlockState(), Block.UPDATE_ALL);
		}
	}
}
