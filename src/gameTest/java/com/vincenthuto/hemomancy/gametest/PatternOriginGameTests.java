package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.rite.structure.BloodStructureCraftingHelper;
import com.vincenthuto.hemomancy.common.util.PatternOrigins;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.level.block.state.pattern.BlockPatternBuilder;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * L-4: when several placements of a multiblock pattern overlap the block a player aims at, each solver keeps the
 * winner it picked before the solvers shared {@code PatternOrigins}. The key-press solver tries orientations first,
 * then pattern cells; the projection and rite solvers scan origins from the low corner, and the rite one does not
 * require the match to contain its center.
 */
@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PatternOriginGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	private PatternOriginGameTests() {
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, batch = "pattern_origins", timeoutTicks = 20)
	public static void overlappingLineMatchesKeepTheirWinners(GameTestHelper helper) {
		// Three gold blocks in a row along x; a two-long pattern fits them several ways around the middle one.
		for (int x = 1; x <= 3; x++) helper.setBlock(new BlockPos(x, 2, 2), Blocks.GOLD_BLOCK);
		BlockPattern pair = BlockPatternBuilder.start().aisle("##")
				.where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.GOLD_BLOCK))).build();
		BlockPos middle = helper.absolutePos(new BlockPos(2, 2, 2));

		expect(helper, "line exact", "-2, 2, -2 down north", exact(pair, helper.getLevel(), middle));
		expect(helper, "line projection", "-1, 2, -2 down north", BloodStructureCraftingHelper.findPatternAtHit(pair, helper.getLevel(), middle));
		expect(helper, "line rite", "-1, 2, -2 down north", nearCenter(pair, helper.getLevel(), middle));
		// The rite solver also accepts a match that misses its center.
		BlockPos beside = helper.absolutePos(new BlockPos(4, 2, 2));
		expect(helper, "beside projection", "none", BloodStructureCraftingHelper.findPatternAtHit(pair, helper.getLevel(), beside));
		expect(helper, "beside rite", "-3, 2, -2 down south", nearCenter(pair, helper.getLevel(), beside));
		helper.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, batch = "pattern_origins", timeoutTicks = 20)
	public static void overlappingSquareMatchesKeepTheirWinners(GameTestHelper helper) {
		// A 3x3 gold plane under a 2x2 pattern: four placements cover the centre block, in several orientations.
		for (int x = 1; x <= 3; x++) for (int z = 1; z <= 3; z++) helper.setBlock(new BlockPos(x, 2, z), Blocks.GOLD_BLOCK);
		BlockPattern square = BlockPatternBuilder.start().aisle("##").aisle("##")
				.where('#', BlockInWorld.hasState(BlockStatePredicate.forBlock(Blocks.GOLD_BLOCK))).build();
		BlockPos centre = helper.absolutePos(new BlockPos(2, 2, 2));
		BlockPos corner = helper.absolutePos(new BlockPos(3, 2, 3));

		expect(helper, "square exact", "-2, 2, -2 north down", exact(square, helper.getLevel(), centre));
		expect(helper, "square projection", "-1, 2, -1 south down", BloodStructureCraftingHelper.findPatternAtHit(square, helper.getLevel(), centre));
		expect(helper, "square rite", "-1, 2, -1 south down", nearCenter(square, helper.getLevel(), centre));
		expect(helper, "corner exact", "-3, 2, -3 north down", exact(square, helper.getLevel(), corner));
		expect(helper, "corner projection", "-2, 2, -2 south down", BloodStructureCraftingHelper.findPatternAtHit(square, helper.getLevel(), corner));
		helper.succeed();
	}

	/** The winner as its origin relative to the test, then forwards and up; values recorded before the merge. */
	private static void expect(GameTestHelper helper, String label, String expected, BlockPattern.BlockPatternMatch match) {
		String shown = match == null ? "none"
				: helper.relativePos(match.getFrontTopLeft()).toShortString() + " " + match.getForwards() + " " + match.getUp();
		helper.assertTrue(expected.equals(shown), label + " picked " + shown + ", expected " + expected);
	}

	private static BlockPattern.BlockPatternMatch exact(BlockPattern pattern, ServerLevel level, BlockPos hit) {
		return PatternOrigins.findContaining(pattern, level, hit);
	}

	/** The rite re-verification search: any placement near the center, covering it or not. */
	private static BlockPattern.BlockPatternMatch nearCenter(BlockPattern pattern, ServerLevel level, BlockPos center) {
		return PatternOrigins.scanAround(pattern, level, center, match -> true);
	}
}
