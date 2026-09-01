package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockPattern;

import java.util.*;

/** Matches the reusable station in its required order: floor, upper structure, then brazier signature. */
public final class CardinalRiteStationMatcher {
	private static final Direction[] DIRECTIONS = Direction.values();

	private CardinalRiteStationMatcher() {
	}

	public static Optional<StationMatch> find(ServerLevel level, BlockPos focusPos, CardinalRiteRecipe recipe) {
		if (!recipe.hasLayeredStation() || !level.getBlockState(focusPos).is(BlockInit.cardinal_focus.get())) {
			return Optional.empty();
		}
		if (!mediumMatches(level, focusPos, recipe)) return Optional.empty();
		CardinalRiteFloorDefinition required = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElse(null);
		if (required == null) return Optional.empty();

		for (CardinalRiteFloorDefinition actual : CardinalRiteFloorRegistry.highestTierFirst()) {
			if (!required.requirement().accepts(actual.style(), actual.tier())) continue;
			BlockPattern.BlockPatternMatch floorMatch = matchFloorAtFocus(level, focusPos, actual);
			if (floorMatch == null) continue;
			BlockPattern.BlockPatternMatch structureMatch =
					matchRequiredStructure(level, focusPos, floorMatch, recipe.getRequiredStructure());
			if (recipe.getRequiredStructure() != null && structureMatch == null) continue;
			List<BrazierMatch> braziers = matchedBraziers(level, focusPos, floorMatch, actual, recipe);
			if (braziers == null) continue;
			return Optional.of(new StationMatch(actual, floorMatch, structureMatch, braziers));
		}
		return Optional.empty();
	}

	public static Resolution resolve(ServerLevel level, BlockPos focusPos, List<CardinalRiteRecipe> recipes) {
		if (!level.getBlockState(focusPos).is(BlockInit.cardinal_focus.get())) {
			return new Resolution(Status.NO_FLOOR, List.of());
		}
		List<FloorMatch> floorMatches = new ArrayList<>();
		for (CardinalRiteFloorDefinition floor : CardinalRiteFloorRegistry.highestTierFirst()) {
			BlockPattern.BlockPatternMatch match = matchFloorAtFocus(level, focusPos, floor);
			if (match != null) floorMatches.add(new FloorMatch(floor, match));
		}
		if (floorMatches.isEmpty()) return new Resolution(Status.NO_FLOOR, List.of());
		int highestTier = floorMatches.stream().mapToInt(match -> match.floor().tier().ordinal()).max().orElse(-1);
		List<FloorMatch> highest = floorMatches.stream()
				.filter(match -> match.floor().tier().ordinal() == highestTier).toList();
		if (highest.size() != 1) return new Resolution(Status.AMBIGUOUS_FLOOR, List.of());
		FloorMatch actual = highest.get(0);
		List<ResolvedRecipe> resolved = new ArrayList<>();
		for (CardinalRiteRecipe recipe : recipes) {
			if (!recipe.hasLayeredStation()) continue;
			if (!mediumMatches(level, focusPos, recipe)) continue;
			CardinalRiteFloorDefinition required = CardinalRiteFloorRegistry.get(recipe.getFloorId()).orElse(null);
			if (required == null || !required.requirement().accepts(
					actual.floor().style(), actual.floor().tier())) continue;
			BlockPattern.BlockPatternMatch structure = matchRequiredStructure(
					level, focusPos, actual.match(), recipe.getRequiredStructure());
			if (recipe.getRequiredStructure() != null && structure == null) continue;
			List<BrazierMatch> braziers = matchedBraziers(
					level, focusPos, actual.match(), actual.floor(), recipe);
			if (braziers == null) continue;
			resolved.add(new ResolvedRecipe(recipe,
					new StationMatch(actual.floor(), actual.match(), structure, braziers)));
		}
		return new Resolution(resolved.size() > 1 ? Status.AMBIGUOUS_RITE
				: resolved.isEmpty() ? Status.NO_RITE : Status.MATCHED, resolved);
	}

	public static Optional<StationMatch> findCaptured(ServerLevel level, BlockPos focusPos,
			CardinalRiteRecipe recipe, net.minecraft.resources.ResourceLocation floorId,
			Direction forwards, Direction up) {
		StationMatch structure = findCapturedStructure(
				level, focusPos, recipe, floorId, forwards, up).orElse(null);
		if (structure == null) return Optional.empty();
		List<BrazierMatch> braziers = matchedBraziers(
				level, focusPos, structure.floorMatch(), structure.floor(), recipe);
		return braziers == null ? Optional.empty()
				: Optional.of(new StationMatch(structure.floor(), structure.floorMatch(),
						structure.structureMatch(), braziers));
	}

	public static Optional<StationMatch> findCapturedStructure(ServerLevel level, BlockPos focusPos,
			CardinalRiteRecipe recipe, net.minecraft.resources.ResourceLocation floorId,
			Direction forwards, Direction up) {
		if (!mediumMatches(level, focusPos, recipe)) return Optional.empty();
		CardinalRiteFloorDefinition floor = CardinalRiteFloorRegistry.get(floorId).orElse(null);
		if (floor == null || forwards == null || up == null) return Optional.empty();
		BlockPos origin = originForCell(focusPos, forwards, up,
				floor.focus().getX(), floor.focus().getY(), floor.focus().getZ());
		BlockPattern.BlockPatternMatch floorMatch =
				floor.pattern().getBlockPattern().matches(level, origin, forwards, up);
		if (floorMatch == null) return Optional.empty();
		BlockPattern.BlockPatternMatch structure =
				matchRequiredStructure(level, focusPos, floorMatch, recipe.getRequiredStructure());
		if (recipe.getRequiredStructure() != null && structure == null) return Optional.empty();
		return Optional.of(new StationMatch(floor, floorMatch, structure, List.of()));
	}

	public static boolean mediumMatches(ServerLevel level, BlockPos focusPos, CardinalRiteRecipe recipe) {
		ItemStack seated = level.getBlockEntity(focusPos) instanceof CardinalFocusBlockEntity focus
				? focus.getMediumForMatching() : ItemStack.EMPTY;
		return CardinalRiteMediumRules.matches(recipe.getMedium(), seated);
	}

	private static BlockPattern.BlockPatternMatch matchFloorAtFocus(
			ServerLevel level, BlockPos focusPos, CardinalRiteFloorDefinition floor) {
		BlockPattern pattern = floor.pattern().getBlockPattern();
		for (Direction forwards : DIRECTIONS) {
			if (forwards.getAxis().isVertical()) continue;
			Direction up = Direction.UP;
				for (int x = 0; x < pattern.getWidth(); x++) {
					for (int y = 0; y < pattern.getHeight(); y++) {
						for (int z = 0; z < pattern.getDepth(); z++) {
							BlockPos origin = originForCell(focusPos, forwards, up, x, y, z);
							BlockPattern.BlockPatternMatch match = pattern.matches(level, origin, forwards, up);
							if (match != null && match.getBlock(
									floor.focus().getX(), floor.focus().getY(), floor.focus().getZ())
									.getPos().equals(focusPos)) {
								return match;
							}
						}
					}
				}
		}
		return null;
	}

	private static BlockPattern.BlockPatternMatch matchRequiredStructure(ServerLevel level, BlockPos focusPos,
			BlockPattern.BlockPatternMatch floorMatch, com.vincenthuto.hutoslib.math.MultiblockPattern structure) {
		if (structure == null) return null;
		BlockPattern pattern = structure.getBlockPattern();
		int centerX = pattern.getWidth() / 2;
		int centerZ = pattern.getDepth() / 2;
		int bottomY = pattern.getHeight() - 1;
		BlockPos bottomCenter = offset(focusPos, floorMatch.getForwards(), floorMatch.getUp(),
				new BlockPos(0, 1, 0));
		BlockPos origin = originForCell(bottomCenter, floorMatch.getForwards(), floorMatch.getUp(),
				centerX, bottomY, centerZ);
		return pattern.matches(level, origin, floorMatch.getForwards(), floorMatch.getUp());
	}

	private static List<BrazierMatch> matchedBraziers(ServerLevel level, BlockPos focusPos,
			BlockPattern.BlockPatternMatch floorMatch, CardinalRiteFloorDefinition floor, CardinalRiteRecipe recipe) {
		List<BrazierMatch> offered = new ArrayList<>();
		for (BlockPos socket : floor.brazierSockets()) {
			BlockPos worldPos = offset(focusPos, floorMatch.getForwards(), floorMatch.getUp(), socket);
			var state = level.getBlockState(worldPos);
			if (!state.is(BlockInit.iron_brazier.get())
					|| !state.hasProperty(BrazierBlock.RITUAL_PHASE)
					|| state.getValue(BrazierBlock.RITUAL_PHASE) <= 0) continue;
			if (level.getBlockEntity(worldPos) instanceof IronBrazierBlockEntity brazier
					&& !brazier.getOfferingForMatching().isEmpty()) {
				offered.add(new BrazierMatch(worldPos, brazier.getOfferingForMatching().copy(), false));
			}
		}
		int requiredCount = recipe.getBrazierSignature().stream()
				.mapToInt(CardinalRiteRecipe.BrazierRequirement::count).sum();
		if (offered.size() != requiredCount) return null;
		List<CardinalRiteRecipe.BrazierRequirement> slots = recipe.getBrazierSignature().stream()
				.flatMap(requirement -> java.util.stream.IntStream.range(0, requirement.count())
						.mapToObj(index -> requirement))
				.sorted(Comparator.comparingInt(requirement -> requirement.ingredient().getItems().length))
				.toList();
		int[] assignments = new int[offered.size()];
		java.util.Arrays.fill(assignments, -1);
		if (!assign(slots, offered, 0, new HashSet<>(), assignments)) return null;
		List<BrazierMatch> matched = new ArrayList<>(offered.size());
		for (int offeredIndex = 0; offeredIndex < offered.size(); offeredIndex++) {
			BrazierMatch offering = offered.get(offeredIndex);
			CardinalRiteRecipe.BrazierRequirement requirement = slots.get(assignments[offeredIndex]);
			matched.add(new BrazierMatch(offering.pos(), offering.stack(), requirement.consumeOnSuccess()));
		}
		return List.copyOf(matched);
	}

	private static boolean assign(List<CardinalRiteRecipe.BrazierRequirement> required,
			List<BrazierMatch> offered, int index, Set<Integer> used, int[] assignments) {
		if (index == required.size()) return true;
		for (int offeredIndex = 0; offeredIndex < offered.size(); offeredIndex++) {
			if (used.contains(offeredIndex)
					|| !required.get(index).ingredient().test(offered.get(offeredIndex).stack())) continue;
			used.add(offeredIndex);
			assignments[offeredIndex] = index;
			if (assign(required, offered, index + 1, used, assignments)) return true;
			assignments[offeredIndex] = -1;
			used.remove(offeredIndex);
		}
		return false;
	}

	private static BlockPos originForCell(BlockPos cell, Direction forwards, Direction up, int x, int y, int z) {
		var right = forwards.getNormal().cross(up.getNormal());
		return cell.offset(
				up.getStepX() * y - right.getX() * x - forwards.getStepX() * z,
				up.getStepY() * y - right.getY() * x - forwards.getStepY() * z,
				up.getStepZ() * y - right.getZ() * x - forwards.getStepZ() * z);
	}

	private static BlockPos offset(BlockPos origin, Direction forwards, Direction up, BlockPos relative) {
		var right = forwards.getNormal().cross(up.getNormal());
		return origin.offset(
				right.getX() * relative.getX() + up.getStepX() * relative.getY() + forwards.getStepX() * relative.getZ(),
				right.getY() * relative.getX() + up.getStepY() * relative.getY() + forwards.getStepY() * relative.getZ(),
				right.getZ() * relative.getX() + up.getStepZ() * relative.getY() + forwards.getStepZ() * relative.getZ());
	}

	public record StationMatch(CardinalRiteFloorDefinition floor,
			BlockPattern.BlockPatternMatch floorMatch,
			BlockPattern.BlockPatternMatch structureMatch,
			List<BrazierMatch> braziers) {
	}

	public record BrazierMatch(BlockPos pos, ItemStack stack, boolean consumeOnSuccess) {
	}

	private record FloorMatch(CardinalRiteFloorDefinition floor, BlockPattern.BlockPatternMatch match) {
	}

	public record ResolvedRecipe(CardinalRiteRecipe recipe, StationMatch station) {
	}

	public record Resolution(Status status, List<ResolvedRecipe> matches) {
		public Resolution {
			matches = List.copyOf(matches);
		}
	}

	public enum Status {
		MATCHED,
		NO_FLOOR,
		NO_RITE,
		AMBIGUOUS_FLOOR,
		AMBIGUOUS_RITE
	}
}
