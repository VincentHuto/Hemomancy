package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.succession.SuccessionBrazierOrder;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Iron Braziers stand on a block, mount on the side of one, or hang from the underside of one. */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BrazierMountGameTests {
	private BrazierMountGameTests() {
	}

	@GameTest(template = "empty", timeoutTicks = 60, batch = "brazier_mounts")
	public static void theClickedFaceChoosesTheMount(GameTestHelper helper) {
		BlockPos wall = helper.absolutePos(new BlockPos(4, 3, 4));
		BlockPos ceiling = helper.absolutePos(new BlockPos(10, 6, 4));
		BlockPos ground = helper.absolutePos(new BlockPos(4, 2, 10));
		for (BlockPos support : new BlockPos[] {wall, ceiling, ground}) {
			helper.getLevel().setBlockAndUpdate(support, Blocks.STONE.defaultBlockState());
		}
		BlockState sconce = placed(helper, wall, Direction.EAST);
		helper.assertTrue(sconce != null && sconce.getValue(BrazierBlock.FACE) == AttachFace.WALL
				&& sconce.getValue(BrazierBlock.FACING) == Direction.EAST,
				"Clicking a wall's east side must mount an east-facing sconce, got " + sconce);
		BlockState hanging = placed(helper, ceiling, Direction.DOWN);
		helper.assertTrue(hanging != null && hanging.getValue(BrazierBlock.FACE) == AttachFace.CEILING,
				"Clicking a block's underside must hang the brazier, got " + hanging);
		BlockState standing = placed(helper, ground, Direction.UP);
		helper.assertTrue(standing != null && standing.getValue(BrazierBlock.FACE) == AttachFace.FLOOR,
				"Clicking a block's top must stand the brazier, got " + standing);
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 60, batch = "brazier_mounts")
	public static void aSconceFallsWithItsWallAndSpillsItsOffering(GameTestHelper helper) {
		BlockPos wall = helper.absolutePos(new BlockPos(4, 3, 4));
		helper.getLevel().setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
		BlockPos brazier = wall.east();
		mount(helper, brazier, AttachFace.WALL, Direction.EAST);
		helper.getLevel().setBlockAndUpdate(wall, Blocks.AIR.defaultBlockState());
		assertFell(helper, brazier, "a sconce whose wall is gone");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 60, batch = "brazier_mounts")
	public static void aHangingBrazierFallsWithItsCeiling(GameTestHelper helper) {
		BlockPos ceiling = helper.absolutePos(new BlockPos(4, 6, 4));
		helper.getLevel().setBlockAndUpdate(ceiling, Blocks.STONE.defaultBlockState());
		BlockPos brazier = ceiling.below();
		mount(helper, brazier, AttachFace.CEILING, Direction.NORTH);
		helper.getLevel().setBlockAndUpdate(ceiling, Blocks.AIR.defaultBlockState());
		assertFell(helper, brazier, "a hanging brazier whose ceiling is gone");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 60, batch = "brazier_mounts")
	public static void aMountedBrazierNeedsItsSupportToBePlaced(GameTestHelper helper) {
		BlockPos air = helper.absolutePos(new BlockPos(4, 4, 4));
		for (BlockPos cell : BlockPos.betweenClosed(air.offset(-1, -1, -1), air.offset(1, 1, 1))) {
			helper.getLevel().setBlockAndUpdate(cell, Blocks.AIR.defaultBlockState());
		}
		BlockState sconce = BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.FACE, AttachFace.WALL)
				.setValue(BrazierBlock.FACING, Direction.EAST);
		BlockState hanging = BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.FACE, AttachFace.CEILING);
		helper.assertFalse(sconce.canSurvive(helper.getLevel(), air), "A sconce must not hang on air");
		helper.assertFalse(hanging.canSurvive(helper.getLevel(), air), "A brazier must not hang from air");
		helper.assertTrue(BlockInit.iron_brazier.get().defaultBlockState().getValue(BrazierBlock.FACE) == AttachFace.FLOOR,
				"A brazier with no mount recorded (older worlds, spawned floors) stands");
		helper.succeed();
	}

	@GameTest(template = "empty", timeoutTicks = 100, batch = "brazier_mounts")
	public static void successionRolesFollowTheMatchersOrderWhereverTheEffigyStands(GameTestHelper helper) {
		var level = helper.getLevel();
		BlockPos center = helper.absolutePos(new BlockPos(8, 2, 8));
		for (BlockPos cell : BlockPos.betweenClosed(center.offset(-4, -1, -4), center.offset(4, 7, 4))) {
			level.setBlock(cell, Blocks.AIR.defaultBlockState(), 3);
		}
		var floor = CardinalRiteFloorRegistry.get(Hemomancy.rloc("dominion_greater")).orElseThrow();
		var pattern = floor.pattern().getPatternArray();
		for (int z = 0; z < 7; z++) {
			for (int x = 0; x < 7; x++) {
				level.setBlock(center.offset(x - 3, 0, 3 - z), floor.pattern().getSymbolList()
						.get(String.valueOf(pattern[z][0].charAt(x))).defaultBlockState(), 3);
			}
		}
		// The effigy on the Focus's west side, so the rite reads its braziers facing south, not the floor's north.
		level.setBlock(center.offset(-2, 1, 0), BlockInit.venous_stone.get().defaultBlockState(), 3);
		level.setBlock(center.offset(-2, 2, 0), BlockInit.vacant_effigy.get().defaultBlockState(), 3);
		((CardinalFocusBlockEntity) level.getBlockEntity(center)).insertMedium(null,
				new ItemStack(ItemInit.sanguine_quintessence.get()));
		BlockPos[] braziers = {center.offset(2, 1, 2), center.offset(-2, 1, 2), center.offset(1, 3, -2)};
		ItemStack[] offerings = {new ItemStack(ItemInit.bloody_vial.get()), new ItemStack(ItemInit.bloody_vial.get()),
				new ItemStack(ItemInit.mnemonic_ambergris.get())};
		for (int i = 0; i < braziers.length; i++) {
			level.setBlock(braziers[i], BlockInit.iron_brazier.get().defaultBlockState().setValue(BrazierBlock.RITUAL_PHASE, 1), 3);
			((IronBrazierBlockEntity) level.getBlockEntity(braziers[i])).insertOffering(null, offerings[i]);
		}
		var recipe = CardinalRiteRecipe.getRiteByLocation(level, Hemomancy.rloc("cardinal_rite/hematic_succession"));
		var match = CardinalRiteStationMatcher.find(level, center, recipe).orElse(null);
		helper.assertTrue(match != null, "The succession station must match with the effigy on the west");
		helper.assertTrue(SuccessionBrazierOrder.forwards(level, center) == match.floorMatch().getForwards(),
				"The roles must be read facing the way the matcher reads them, got "
						+ SuccessionBrazierOrder.forwards(level, center) + " for " + match.floorMatch().getForwards());
		var expected = match.braziers().stream().map(CardinalRiteStationMatcher.BrazierMatch::pos).toList();
		var roles = SuccessionBrazierOrder.roles(level, center, match.floor());
		helper.assertTrue(roles.equals(expected), "Roles " + roles + " must follow the matcher's order " + expected);
		helper.succeed();
	}

	private static BlockState placed(GameTestHelper helper, BlockPos clicked, Direction face) {
		var player = helper.makeMockPlayer(GameType.SURVIVAL);
		var stack = new ItemStack(BlockInit.iron_brazier.get());
		var context = new BlockPlaceContext(player, InteractionHand.MAIN_HAND, stack,
				new BlockHitResult(Vec3.atCenterOf(clicked).relative(face, 0.5D), face, clicked, false));
		return BlockInit.iron_brazier.get().getStateForPlacement(context);
	}

	private static void mount(GameTestHelper helper, BlockPos pos, AttachFace face, Direction facing) {
		helper.getLevel().setBlockAndUpdate(pos, BlockInit.iron_brazier.get().defaultBlockState()
				.setValue(BrazierBlock.FACE, face).setValue(BrazierBlock.FACING, facing));
		helper.assertTrue(((IronBrazierBlockEntity) helper.getLevel().getBlockEntity(pos))
				.insertOffering(null, new ItemStack(Items.BONE)), "The brazier must take an offering");
	}

	private static void assertFell(GameTestHelper helper, BlockPos pos, String what) {
		helper.assertTrue(helper.getLevel().getBlockState(pos).isAir(), what + " must fall");
		var drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(2.0D));
		helper.assertTrue(drops.stream().anyMatch(item -> item.getItem().is(BlockInit.iron_brazier.get().asItem())),
				what + " must drop itself");
		helper.assertTrue(drops.stream().anyMatch(item -> item.getItem().is(Items.BONE)), what + " must spill its offering");
	}
}
