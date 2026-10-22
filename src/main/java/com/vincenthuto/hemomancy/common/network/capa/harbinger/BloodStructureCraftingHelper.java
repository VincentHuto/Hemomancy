package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.HematicStakeBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.util.PatternOrigins;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

import java.util.*;

public final class BloodStructureCraftingHelper {
	private BloodStructureCraftingHelper() {
	}

	public static Optional<ProjectionCraftMatch> findProjectionCraftMatch(
			Player player, ServerLevel level, BlockPos hitPos, ItemStack offhandCatalyst) {
		List<BloodStructureRecipe> projectionRecipes = new ArrayList<>(BloodStructureRecipe.getAllRecipes(level));
		for (BloodStructureRecipe recipe : BloodCraftingPatternSearchRules.sortedByPatternSearchCost(
				projectionRecipes,
				targetPattern -> targetPattern.getPattern().getPatternArray())) {
			if (recipe.isUnstained()) {
				continue;
			}
			BlockPattern.BlockPatternMatch match = findStructurePatternAtHit(recipe, level, hitPos);
			if (match == null) {
				continue;
			}

			if (!offhandCatalyst.is(recipe.getHeldItem().getItem())) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, missingOffhandCatalystMessage(recipe)));
			}

			int requiredDegree = RecipeDegreeGates.getRequiredDegree(recipe);
			if (!RecipeDegreeGates.playerMeets(player, recipe)
					&& !UnstainedAccessRules.bypassesUnstainedLevelGate(
									recipe.getId().getPath(), HemoCapabilityAccess.getUnstainedProgress(player))) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, missingDegreeMessage(recipe, requiredDegree)));
			}

			Component alignError = checkPathAlignment(player, recipe);
			if (alignError != null) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, alignError));
			}

			HematicStakeBlock.Formation stakeFormation = null;
			if (recipe.getResult().is(BlockInit.hematic_stake.get().asItem())) {
				stakeFormation = findStakeFormation(level, player, match, recipe.getPattern().getBlockPattern());
				if (stakeFormation == null) {
					return Optional.of(ProjectionCraftMatch.invalid(recipe, match,
							Component.translatable("block.hemomancy.hematic_stake.invalid")
									.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC)));
				}
			}

			return Optional.of(ProjectionCraftMatch.valid(recipe, match, stakeFormation));
		}
		return Optional.empty();
	}

	private static HematicStakeBlock.Formation findStakeFormation(ServerLevel level, Player player,
			BlockPattern.BlockPatternMatch match, BlockPattern pattern) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return null;
		}
		BlockPos barPos = null;
		BlockPos crystalPos = null;
		for (int i = 0; i < pattern.getWidth(); ++i) {
			for (int j = 0; j < pattern.getHeight(); ++j) {
				for (int k = 0; k < pattern.getDepth(); ++k) {
					BlockInWorld block = match.getBlock(i, j, k);
					if (block.getState().is(BlockInit.hematic_iron_bars.get())) barPos = block.getPos();
					else if (block.getState().is(BlockInit.blood_crystal.get())) crystalPos = block.getPos();
				}
			}
		}
		return barPos != null && crystalPos != null
				? HematicStakeBlock.findFormation(level, serverPlayer, barPos, crystalPos)
				: null;
	}

	public static BlockPattern.BlockPatternMatch findStructurePatternAtHit(
			BloodStructureRecipe recipe, ServerLevel level, BlockPos hitPos) {
		if (!level.getBlockState(hitPos).is(recipe.getHitBlock())) {
			return null;
		}
		return findPatternAtHit(recipe.getPattern().getBlockPattern(), level, hitPos);
	}

	/** A placement covering {@code hitPos}, scanning origins from the low corner (see {@link PatternOrigins}). */
	public static BlockPattern.BlockPatternMatch findPatternAtHit(
			BlockPattern blockPattern, ServerLevel level, BlockPos hitPos) {
		return PatternOrigins.scanAround(blockPattern, level, hitPos,
				match -> PatternOrigins.contains(match, blockPattern, hitPos));
	}

	public static List<BlockPos> getMatchPositions(
			BlockPattern.BlockPatternMatch match, BlockPattern blockPattern) {
		List<BlockPos> positions = new ArrayList<>();
		for (int i = 0; i < blockPattern.getWidth(); ++i) {
			for (int j = 0; j < blockPattern.getHeight(); ++j) {
				for (int k = 0; k < blockPattern.getDepth(); ++k) {
					positions.add(match.getBlock(i, j, k).getPos());
				}
			}
		}
		positions.sort(Comparator.comparingLong(BlockPos::asLong));
		return List.copyOf(positions);
	}

	public static List<BlockPos> getVisibleMatchPositions(
			BlockPattern.BlockPatternMatch match, BlockPattern blockPattern, String[][] patternArray) {
		List<BlockPos> positions = new ArrayList<>();
		for (int i = 0; i < blockPattern.getWidth(); ++i) {
			for (int j = 0; j < blockPattern.getHeight(); ++j) {
				for (int k = 0; k < blockPattern.getDepth(); ++k) {
					if (BloodStructureVisiblePositionRules.isVisiblePatternCell(patternArray, i, j, k)) {
						positions.add(match.getBlock(i, j, k).getPos());
					}
				}
			}
		}
		positions.sort(Comparator.comparingLong(BlockPos::asLong));
		return List.copyOf(positions);
	}

	public static Component checkPathAlignment(Player player, BloodStructureRecipe recipe) {
		boolean harbingerProgressBlocked = UnstainedAccessRules.blocksHarbingerProgress(HemoCapabilityAccess.getUnstainedProgress(player));
		if (!recipe.isUnstained() && harbingerProgressBlocked) {
			return Component.literal("Purification bars access to the Hematic Order formations.")
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
		}
		return null;
	}

	public static Component projectionHintMessage() {
		return Component.literal("Use Blood Projection on this formation with its catalyst in your offhand.")
				.withStyle(ChatFormatting.DARK_RED);
	}

	private static Component missingOffhandCatalystMessage(BloodStructureRecipe recipe) {
		return Component.literal("This formation needs Blood Projection and an offhand catalyst: ")
				.withStyle(ChatFormatting.RED)
				.append(recipe.getHeldItem().getHoverName().copy()
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
	}

	private static Component missingDegreeMessage(BloodStructureRecipe recipe, int requiredDegree) {
		return Component.literal("This formation requires " + recipe.getId().getPath() + " and demands ")
				.withStyle(ChatFormatting.RED)
				.append(Component.literal(RecipeDegreeGates.requirementLabel(recipe))
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(Component.literal(".").withStyle(ChatFormatting.RED));
	}

	public record ProjectionCraftMatch(BloodStructureRecipe recipe, BlockPattern.BlockPatternMatch match,
			HematicStakeBlock.Formation stakeFormation, Component error) {
		static ProjectionCraftMatch valid(BloodStructureRecipe recipe, BlockPattern.BlockPatternMatch match,
				HematicStakeBlock.Formation stakeFormation) {
			return new ProjectionCraftMatch(recipe, match, stakeFormation, null);
		}

		static ProjectionCraftMatch invalid(BloodStructureRecipe recipe, BlockPattern.BlockPatternMatch match,
				Component error) {
			return new ProjectionCraftMatch(recipe, match, null, error);
		}

		public boolean valid() {
			return error == null;
		}
	}
}
