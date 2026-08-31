package com.vincenthuto.hemomancy.common.network.capa.harbinger;

import com.vincenthuto.hemomancy.common.block.harbinger.BrazierBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.HematicStakeBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureOffering;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.tile.IronBrazierBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

			if (isRiteExclusive(recipe)) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, riteExclusiveMessage()));
			}

			if (!offhandCatalyst.is(recipe.getHeldItem().getItem())) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, missingOffhandCatalystMessage(recipe)));
			}

			int requiredDegree = RecipeDegreeGates.getRequiredDegree(recipe);
			if (!RecipeDegreeGates.playerMeets(player, recipe)
					&& !HemoCapabilityAccess.getUnstainedProgress(player)
							.map(progress -> UnstainedAccessRules.bypassesUnstainedLevelGate(
									recipe.getId().getPath(), progress)).orElse(false)) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, missingDegreeMessage(recipe, requiredDegree)));
			}

			Component alignError = checkPathAlignment(player, recipe);
			if (alignError != null) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, alignError));
			}

			OfferingMatch offeringMatch = findOfferingMatch(recipe, level, match);
			if (!offeringMatch.valid()) {
				return Optional.of(ProjectionCraftMatch.invalid(recipe, match, offeringMatch.error()));
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

			return Optional.of(ProjectionCraftMatch.valid(recipe, match, offeringMatch.positions(), stakeFormation));
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

	public static OfferingMatch findOfferingMatch(
			BloodStructureRecipe recipe, ServerLevel level, BlockPattern.BlockPatternMatch match) {
		if (recipe.getOfferings().isEmpty()) {
			return OfferingMatch.valid(List.of());
		}

		List<BlockPos> candidates = new ArrayList<>(offeringSearchBounds(match, recipe.getPattern().getBlockPattern()));
		candidates.sort(Comparator.comparingLong(BlockPos::asLong));

		List<BlockPos> matched = new ArrayList<>();
		Set<BlockPos> used = new HashSet<>();
		for (BloodStructureOffering offering : recipe.getOfferings()) {
			for (int required = 0; required < offering.count(); required++) {
				BlockPos offeringPos = findUnusedOfferingBrazier(level, candidates, used, offering);
				if (offeringPos == null) {
					return OfferingMatch.invalid(missingOfferingMessage(offering));
				}
				used.add(offeringPos);
				matched.add(offeringPos);
			}
		}
		return OfferingMatch.valid(matched);
	}

	public static List<BlockPos> offeringSearchBounds(BlockPattern.BlockPatternMatch match, BlockPattern blockPattern) {
		List<BlockPos> positions = new ArrayList<>();
		AABB bounds = getMatchBounds(match, blockPattern);
		BlockPos min = BlockPos.containing(
				bounds.minX - blockPattern.getWidth(),
				bounds.minY - blockPattern.getHeight(),
				bounds.minZ - blockPattern.getDepth());
		BlockPos max = BlockPos.containing(
				bounds.maxX - 1 + blockPattern.getWidth(),
				bounds.maxY - 1 + blockPattern.getHeight(),
				bounds.maxZ - 1 + blockPattern.getDepth());
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			positions.add(pos.immutable());
		}
		return List.copyOf(positions);
	}

	public static List<ConsumedOffering> consumeMatchedOfferings(ServerLevel level, List<BlockPos> positions) {
		if (positions.isEmpty()) {
			return List.of();
		}
		List<ConsumedOffering> consumed = new ArrayList<>();
		for (BlockPos pos : positions) {
			if (level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier) {
				ItemStack stack = brazier.consumeOffering();
				if (!stack.isEmpty()) {
					consumed.add(new ConsumedOffering(pos.immutable(), stack.copy()));
					unlightOfferingBrazier(level, pos);
				}
			}
		}
		return List.copyOf(consumed);
	}

	private static BlockPos findUnusedOfferingBrazier(ServerLevel level, List<BlockPos> candidates, Set<BlockPos> used,
			BloodStructureOffering offering) {
		for (BlockPos pos : candidates) {
			if (used.contains(pos)) {
				continue;
			}
			if (level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier
					&& BrazierBlock.isLit(level.getBlockState(pos))
					&& offering.ingredient().test(brazier.getOfferingForMatching())) {
				return pos;
			}
		}
		return null;
	}

	private static void unlightOfferingBrazier(ServerLevel level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() instanceof BrazierBlock
				&& state.getValue(BrazierBlock.RITUAL_PHASE) > 0) {
			level.setBlock(pos, state.setValue(BrazierBlock.RITUAL_PHASE, 0), Block.UPDATE_ALL);
		}
	}

	public static Component checkPathAlignment(Player player, BloodStructureRecipe recipe) {
		boolean harbingerProgressBlocked = HemoCapabilityAccess.getUnstainedProgress(player)
				.map(UnstainedAccessRules::blocksHarbingerProgress).orElse(false);
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

	public static boolean isRiteExclusive(BloodStructureRecipe recipe) {
		return recipe.getResult().is(BlockInit.consecrated_bloodwell.get().asItem());
	}

	public static Component riteExclusiveMessage() {
		return Component.literal("Consecrated Bloodwells may only be manifested by the Rite of the Founding Fane.")
				.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC);
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

	private static Component missingOfferingMessage(BloodStructureOffering offering) {
		return Component.literal("This formation needs " + offering.count() + " matching brazier offering"
				+ (offering.count() == 1 ? "." : "s."))
				.withStyle(ChatFormatting.RED);
	}

	public record ProjectionCraftMatch(BloodStructureRecipe recipe, BlockPattern.BlockPatternMatch match,
			List<BlockPos> offerings, HematicStakeBlock.Formation stakeFormation, Component error) {
		public ProjectionCraftMatch {
			offerings = List.copyOf(offerings == null ? List.of() : offerings);
		}

		static ProjectionCraftMatch valid(BloodStructureRecipe recipe, BlockPattern.BlockPatternMatch match,
				List<BlockPos> offerings, HematicStakeBlock.Formation stakeFormation) {
			return new ProjectionCraftMatch(recipe, match, offerings, stakeFormation, null);
		}

		static ProjectionCraftMatch invalid(BloodStructureRecipe recipe, BlockPattern.BlockPatternMatch match,
				Component error) {
			return new ProjectionCraftMatch(recipe, match, List.of(), null, error);
		}

		public boolean valid() {
			return error == null;
		}
	}

	public record OfferingMatch(List<BlockPos> positions, Component error) {
		public OfferingMatch {
			positions = List.copyOf(positions == null ? List.of() : positions);
		}

		static OfferingMatch valid(List<BlockPos> positions) {
			return new OfferingMatch(positions, null);
		}

		static OfferingMatch invalid(Component error) {
			return new OfferingMatch(List.of(), error);
		}

		public boolean valid() {
			return error == null;
		}
	}

	public record ConsumedOffering(BlockPos pos, ItemStack stack) {
		public ConsumedOffering {
			stack = stack.copy();
		}
	}
}
