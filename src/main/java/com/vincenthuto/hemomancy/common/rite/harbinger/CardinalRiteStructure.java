package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.ActiveCardinalRite;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteStationMatcher;
import com.vincenthuto.hemomancy.common.util.PatternOrigins;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

/**
 * Whether an active rite's structure still stands: its layered floor, upper structure and braziers, or the single
 * pattern of an older floorless rite. Used by the tick each second, before completion and by completion itself.
 */
public final class CardinalRiteStructure {
	private CardinalRiteStructure() {
	}

	/**
	 * Re-validates that the multiblock structure for a cardinal rite is still intact.
	 * Returns true if the pattern still matches at the rite's center position.
	 */
	public static boolean verify(ServerLevel sLevel, ActiveCardinalRite rite) {
		CardinalRiteRecipe recipe = CardinalRiteRecipe.getRiteByLocation(sLevel, rite.getRecipeId());
		if (recipe == null) {
			Hemomancy.LOGGER.warn("Rite verification failed: recipe {} not found", rite.getRecipeId());
			return false;
		}

		BlockPos center = rite.getCenterPos();
		if (recipe.hasLayeredStation()) {
			// Once a rite has taken its offerings (or holds a craft, station or puppet), only its structure must stand.
			boolean valid = (RiteKind.of(rite, recipe).holdsOnlyItsStructure(rite) || rite.getOfferingVisitIndex() > 0
					? layeredStructureMatch(sLevel, rite, recipe)
					: layeredStationMatch(sLevel, rite, recipe)) != null;
			if (!valid) {
				Hemomancy.LOGGER.warn("Layered Cardinal Rite station no longer matches {} at {}",
						rite.getRecipeId(), center);
			}
			return valid;
		}
		BlockPattern blockPattern = recipe.getPattern().getBlockPattern();
		BlockPattern.BlockPatternMatch match = findPatternNearCenter(blockPattern, sLevel, center);
		if (match == null) {
			Hemomancy.LOGGER.warn("Rite verification failed for {} at center {}. Dumping expected vs actual:",
					rite.getRecipeId(), center);
			var blockPairs = recipe.getPattern().getBlockPosBlockList();
			int width = blockPattern.getWidth();
			int height = blockPattern.getHeight();
			int depth = blockPattern.getDepth();
			Hemomancy.LOGGER.warn("  Pattern size: {}w x {}h x {}d, blockPairs: {}",
					width, height, depth, blockPairs.size());

			// Compute the origin of the structure relative to the stored center.
			// getBlockPosBlockList uses (charIndex, invertedRow, aisleIndex) as (X,Y,Z).
			// The center was derived from pattern index (width/2, height/2, depth/2).
			int halfW = width / 2;
			int halfH = height / 2;
			int halfD = depth / 2;

			int mismatches = 0;
			for (var pair : blockPairs) {
				Block expected = pair.getBlock();
				// Null or air expected means this was a space (wildcard) skip it
				if (expected == null || expected == Blocks.AIR) continue;
				BlockPos relPos = pair.getPos();
				BlockPos worldPos = center.offset(
						relPos.getX() - halfW,
						relPos.getY() - halfH,
						relPos.getZ() - halfD
				);
				Block actualBlock = sLevel.getBlockState(worldPos).getBlock();
				boolean mismatch = actualBlock != expected;
				if (mismatch) mismatches++;
				Hemomancy.LOGGER.warn("  {} Expected [{}] at rel {} -> world {} | Found [{}]{}",
						mismatch ? "XX" : "OK",
						expected,
						relPos, worldPos,
						actualBlock,
						mismatch ? " << MISMATCH" : "");
			}
			Hemomancy.LOGGER.warn("  Total mismatches: {} / {} non-wildcard positions", mismatches, blockPairs.size());
		}
		return match != null;
	}

	/**
	 * Any placement of the pattern whose origin lies within reach of the stored center. The stored center sits
	 * inside the structure, so the origin can be up to {@code maxDim - 1} blocks away in any direction; the match
	 * is not required to cover the center itself.
	 */
	static BlockPattern.BlockPatternMatch findPatternNearCenter(
			BlockPattern blockPattern, ServerLevel sLevel, BlockPos center) {
		return PatternOrigins.scanAround(blockPattern, sLevel, center, match -> true);
	}

	/** The whole station: floor, structure and lit braziers, on the floor the rite captured when it started. */
	static CardinalRiteStationMatcher.StationMatch layeredStationMatch(
			ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (rite.getMatchedFloorId() != null) {
			return CardinalRiteStationMatcher.findCaptured(level, rite.getCenterPos(), recipe,
					rite.getMatchedFloorId(), rite.getFloorForwards(), rite.getFloorUp()).orElse(null);
		}
		return CardinalRiteStationMatcher.find(level, rite.getCenterPos(), recipe).orElse(null);
	}

	/** The floor and upper structure only, once the braziers' offerings are no longer the rite's to check. */
	static CardinalRiteStationMatcher.StationMatch layeredStructureMatch(
			ServerLevel level, ActiveCardinalRite rite, CardinalRiteRecipe recipe) {
		if (rite.getMatchedFloorId() == null) return layeredStationMatch(level, rite, recipe);
		return CardinalRiteStationMatcher.findCapturedStructure(level, rite.getCenterPos(), recipe,
				rite.getMatchedFloorId(), rite.getFloorForwards(), rite.getFloorUp()).orElse(null);
	}
}
