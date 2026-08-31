package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.shared.HematicIronBarsBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class HematicIronBarsGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";
	private static final double EPSILON = 1.0e-9;

	private HematicIronBarsGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 40)
	public static void rotateFromClickedFaceAndConnectInTheirPlane(GameTestHelper helper) {
		var player = helper.makeMockPlayer(GameType.CREATIVE);

		assertPlacementAxis(helper, player, new BlockPos(1, 2, 1), Direction.UP, Direction.Axis.Y);
		assertPlacementAxis(helper, player, new BlockPos(2, 2, 1), Direction.EAST, Direction.Axis.X);
		assertPlacementAxis(helper, player, new BlockPos(3, 2, 1), Direction.NORTH, Direction.Axis.Z);
		BlockPos waterPos = new BlockPos(1, 2, 2);
		helper.setBlock(waterPos, Blocks.WATER);
		ItemStack waterloggedBars = new ItemStack(BlockInit.hematic_iron_bars.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, waterloggedBars);
		helper.placeAt(player, waterloggedBars, waterPos.below(), Direction.UP);
		helper.assertBlockProperty(waterPos, HematicIronBarsBlock.WATERLOGGED, true);

		BlockPos connectionPos = new BlockPos(2, 2, 3);
		BlockState xAxis = BlockInit.hematic_iron_bars.get().defaultBlockState()
				.setValue(HematicIronBarsBlock.AXIS, Direction.Axis.X);
		helper.setBlock(connectionPos, xAxis);
		helper.setBlock(connectionPos.above(), Blocks.STONE);
		helper.assertBlockProperty(connectionPos, PipeBlock.UP, true);
		helper.setBlock(connectionPos.east(), Blocks.STONE);
		helper.assertBlockProperty(connectionPos, PipeBlock.EAST, false);

		helper.setBlock(connectionPos.north(), xAxis.setValue(HematicIronBarsBlock.AXIS, Direction.Axis.Z));
		helper.assertBlockProperty(connectionPos, PipeBlock.NORTH, false);
		helper.setBlock(connectionPos.north(), xAxis.setValue(HematicIronBarsBlock.AXIS, Direction.Axis.Y));
		helper.assertBlockProperty(connectionPos, PipeBlock.NORTH, true);

		AABB bounds = helper.getBlockState(connectionPos)
				.getShape(helper.getLevel(), helper.absolutePos(connectionPos)).bounds();
		assertClose(helper, bounds.minX, 0, "x-axis bars must span the block on X");
		assertClose(helper, bounds.maxX, 1, "x-axis bars must span the block on X");
		assertClose(helper, bounds.maxY, 1, "the upward connection must extend to the top face");

		helper.setBlock(connectionPos.above(), Blocks.AIR);
		helper.assertBlockProperty(connectionPos, PipeBlock.UP, false);
		helper.succeed();
	}

	private static void assertPlacementAxis(GameTestHelper helper, net.minecraft.world.entity.player.Player player,
			BlockPos target, Direction clickedFace, Direction.Axis expectedAxis) {
		ItemStack bars = new ItemStack(BlockInit.hematic_iron_bars.get());
		player.setItemInHand(InteractionHand.MAIN_HAND, bars);
		helper.setBlock(target, Blocks.AIR);
		helper.placeAt(player, bars, target.relative(clickedFace.getOpposite()), clickedFace);
		helper.assertBlockPresent(BlockInit.hematic_iron_bars.get(), target);
		helper.assertBlockProperty(target, HematicIronBarsBlock.AXIS, expectedAxis);
	}

	private static void assertClose(GameTestHelper helper, double actual, double expected, String message) {
		helper.assertTrue(Math.abs(actual - expected) <= EPSILON, message + ": expected " + expected + ", got " + actual);
	}
}
