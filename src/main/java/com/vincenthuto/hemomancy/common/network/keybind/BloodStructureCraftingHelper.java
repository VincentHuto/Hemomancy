package com.vincenthuto.hemomancy.common.network.keybind;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class BloodStructureCraftingHelper {
	private BloodStructureCraftingHelper() {
	}

	public static Optional<ProjectionCraftMatch> findProjectionCraftMatch(
			Player player, ServerLevel level, BlockPos hitPos, ItemStack offhandCatalyst) {
		for (BloodStructureRecipe recipe : BloodCraftingPatternSearchRules.sortedByPatternSearchCost(
				BloodStructureRecipe.getAllRecipes(level),
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

			int playerLevel = RecipeDegreeGates.getPlayerLevel(player, recipe.isUnstained());
			int requiredDegree = RecipeDegreeGates.getRequiredDegree(recipe);
			if (playerLevel < requiredDegree) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, missingDegreeMessage(recipe, requiredDegree)));
			}

			Component alignError = checkPathAlignment(player, recipe);
			if (alignError != null) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, alignError));
			}

			return Optional.of(ProjectionCraftMatch.valid(recipe, match));
		}
		return Optional.empty();
	}

	public static BlockPattern.BlockPatternMatch findStructurePatternAtHit(
			BloodStructureRecipe recipe, ServerLevel level, BlockPos hitPos) {
		if (!level.getBlockState(hitPos).is(recipe.getHitBlock())) {
			return null;
		}
		return findPatternAtHit(recipe.getPattern().getBlockPattern(), level, hitPos);
	}

	public static BlockPattern.BlockPatternMatch findPatternAtHit(
			BlockPattern blockPattern, ServerLevel level, BlockPos hitPos) {
		int maxDim = Math.max(Math.max(
				blockPattern.getWidth(), blockPattern.getHeight()), blockPattern.getDepth());
		int radius = maxDim - 1;
		for (BlockPos candidate : BlockPos.betweenClosed(
				hitPos.offset(-radius, -radius, -radius),
				hitPos.offset(radius, radius, radius))) {
			for (Direction finger : Direction.values()) {
				for (Direction thumb : Direction.values()) {
					if (thumb == finger || thumb == finger.getOpposite()) {
						continue;
					}
					BlockPattern.BlockPatternMatch match = blockPattern.matches(level, candidate, finger, thumb);
					if (match != null && matchContainsPos(match, blockPattern, hitPos)) {
						return match;
					}
				}
			}
		}
		return null;
	}

	public static boolean matchContainsPos(
			BlockPattern.BlockPatternMatch match, BlockPattern blockPattern, BlockPos pos) {
		for (int i = 0; i < blockPattern.getWidth(); ++i) {
			for (int j = 0; j < blockPattern.getHeight(); ++j) {
				for (int k = 0; k < blockPattern.getDepth(); ++k) {
					if (match.getBlock(i, j, k).getPos().equals(pos)) {
						return true;
					}
				}
			}
		}
		return false;
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

	public static AABB getMatchBounds(BlockPattern.BlockPatternMatch match, BlockPattern blockPattern) {
		int width = blockPattern.getWidth();
		int height = blockPattern.getHeight();
		int depth = blockPattern.getDepth();
		int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
		int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
		int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
		for (int i = 0; i < width; ++i) {
			for (int j = 0; j < height; ++j) {
				for (int k = 0; k < depth; ++k) {
					BlockPos matchPos = match.getBlock(i, j, k).getPos();
					if (matchPos.getX() < minX) minX = matchPos.getX();
					if (matchPos.getX() > maxX) maxX = matchPos.getX();
					if (matchPos.getY() < minY) minY = matchPos.getY();
					if (matchPos.getY() > maxY) maxY = matchPos.getY();
					if (matchPos.getZ() < minZ) minZ = matchPos.getZ();
					if (matchPos.getZ() > maxZ) maxZ = matchPos.getZ();
				}
			}
		}
		return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
	}

	public static void clearMatchedPattern(ServerLevel level, BlockPattern.BlockPatternMatch match,
			BlockPattern blockPattern) {
		for (int i = 0; i < blockPattern.getWidth(); ++i) {
			for (int j = 0; j < blockPattern.getHeight(); ++j) {
				for (int k = 0; k < blockPattern.getDepth(); ++k) {
					BlockInWorld cachedBlockInfo = match.getBlock(i, j, k);
					BlockPos pos = cachedBlockInfo.getPos();
					level.levelEvent(2001, pos, Block.getId(level.getBlockState(pos)));
					level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
				}
			}
		}
	}

	public static Component checkPathAlignment(Player player, BloodStructureRecipe recipe) {
		boolean playerIsInitiated = HemoCapabilityAccess.getPlayerDegreeNumber(player) >= 1;
		boolean playerIsUnstained = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(u -> u.hasBegunPurification()).orElse(false);
		if (recipe.isUnstained() && playerIsInitiated) {
			return Component.literal("Those who have sworn blood to the Hematic Order cannot walk the Unstained path.")
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
		}
		if (!recipe.isUnstained() && playerIsUnstained) {
			return Component.literal("One who has begun the purification cannot invoke the formations of the Hematic Order.")
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
				.append(Component.literal(RecipeDegreeGates.degreeLabel(requiredDegree))
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD))
				.append(Component.literal(".").withStyle(ChatFormatting.RED));
	}

	public record ProjectionCraftMatch(BloodStructureRecipe recipe, BlockPattern.BlockPatternMatch match,
			Component error) {
		static ProjectionCraftMatch valid(BloodStructureRecipe recipe, BlockPattern.BlockPatternMatch match) {
			return new ProjectionCraftMatch(recipe, match, null);
		}

		static ProjectionCraftMatch invalid(BloodStructureRecipe recipe, BlockPattern.BlockPatternMatch match,
				Component error) {
			return new ProjectionCraftMatch(recipe, match, error);
		}

		public boolean valid() {
			return error == null;
		}
	}
}
