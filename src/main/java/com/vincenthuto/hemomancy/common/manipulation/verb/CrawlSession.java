package com.vincenthuto.hemomancy.common.manipulation.verb;

import com.vincenthuto.hemomancy.common.util.PlayerBlockEdits;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A crawl through connected blocks from one struck block: the selector says which blocks it may claim, a budget says
 * how many it claims now, and {@link #dismantle} breaks what it reached as the player who sent it. It walks all 26
 * neighbours, never leaves its radius and stops at {@code maxBlocks}. Lignum Mortis crawls wood with it; Lode Draw and
 * the Talaromyces scar's vein crawl ore with it ({@code OreVeins}, Track 8).
 */
public final class CrawlSession {
	/** Which blocks a crawl claims, and which it may pass through without claiming (a canopy's leaves). */
	public interface Selector {
		boolean selectable(Level level, BlockPos pos);

		default boolean passesThrough(Level level, BlockPos pos) {
			return false;
		}
	}

	private final ResourceKey<Level> dimension;
	private final BlockPos origin;
	private final int radius;
	private final int maxBlocks;
	private final Selector selector;
	private final List<BlockPos> selected = new ArrayList<>();
	private final Set<BlockPos> selectedSet = new HashSet<>();
	private final Set<BlockPos> queued = new HashSet<>();
	private final ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
	private double farthestDistance;
	private double budget;
	private BlockPos latest;
	private boolean exhausted;

	public CrawlSession(ResourceKey<Level> dimension, BlockPos origin, int radius, int maxBlocks, Selector selector) {
		this.dimension = dimension;
		this.origin = origin.immutable();
		this.radius = Math.max(0, radius);
		this.maxBlocks = Math.max(1, maxBlocks);
		this.selector = selector;
		claim(this.origin);
	}

	public ResourceKey<Level> dimension() {
		return dimension;
	}

	public BlockPos origin() {
		return origin;
	}

	public int radius() {
		return radius;
	}

	public BlockPos latest() {
		return latest;
	}

	public double farthestDistance() {
		return farthestDistance;
	}

	public List<BlockPos> selected() {
		return List.copyOf(selected);
	}

	public int size() {
		return selected.size();
	}

	public boolean contains(BlockPos pos) {
		return selectedSet.contains(pos);
	}

	public boolean isFull() {
		return selected.size() >= maxBlocks;
	}

	/** Nothing more to reach: the frontier ran dry or the crawl is full. */
	public boolean isDone() {
		return exhausted || isFull();
	}

	/**
	 * Adds {@code blocks} of budget and claims as many blocks as the budget covers, nearest-first along the frontier.
	 * Returns the blocks claimed now; {@link #isDone} turns true once nothing more can be reached.
	 */
	public List<BlockPos> advance(Level level, double blocks) {
		if (isDone()) return List.of();
		budget += Math.max(0.0D, blocks);
		List<BlockPos> claimed = new ArrayList<>();
		while (budget >= 1.0D && !isFull()) {
			BlockPos next = nextEligible(level);
			if (next == null) {
				exhausted = true;
				break;
			}
			budget -= 1.0D;
			claimed.add(next);
		}
		return claimed;
	}

	/** Whether the claimed block at {@code pos} may still be broken: it is still selectable and inside the radius. */
	public boolean stillStanding(Level level, BlockPos pos) {
		return pos.distSqr(origin) <= (double) radius * radius && selector.selectable(level, pos);
	}

	/**
	 * Breaks every claimed block that still stands, as {@code player} with {@code tool} ({@link PlayerBlockEdits#breakBlock}:
	 * the break event, the block's own callbacks, drops for that tool). {@code onBroken} runs after each break with
	 * the state that was there. Returns how many blocks broke.
	 */
	public int dismantle(ServerLevel level, ServerPlayer player, ItemStack tool, Broken onBroken) {
		return dismantleExcept(level, player, tool, null, onBroken);
	}

	/**
	 * {@link #dismantle}, leaving {@code except} alone: a crawl that follows a player's own break (the Talaromyces
	 * vein) starts from a block the ordinary break is already taking.
	 */
	public int dismantleExcept(ServerLevel level, ServerPlayer player, ItemStack tool, BlockPos except, Broken onBroken) {
		return dismantleExcept(level, player, tool, except, onBroken, null);
	}

	/** {@link #dismantleExcept}, asking {@code gate} before each break; a refusal stops the dismantling there. */
	public int dismantleExcept(ServerLevel level, ServerPlayer player, ItemStack tool, BlockPos except, Broken onBroken,
			Gate gate) {
		int broken = 0;
		for (BlockPos pos : selected) {
			if (pos.equals(except)) continue;
			if (!level.isLoaded(pos) || !stillStanding(level, pos)) continue;
			if (gate != null && !gate.admit(pos)) break;
			BlockState state = level.getBlockState(pos);
			if (!PlayerBlockEdits.breakBlock(player, pos, tool)) continue;
			broken++;
			if (onBroken != null) onBroken.accept(pos, state);
		}
		return broken;
	}

	@FunctionalInterface
	public interface Broken {
		void accept(BlockPos pos, BlockState state);
	}

	@FunctionalInterface
	public interface Gate {
		boolean admit(BlockPos pos);
	}

	/** Which of {@code bands} distance bands {@code pos} falls in, nearest band 0. */
	public int band(BlockPos pos, int bands) {
		if (radius <= 0) return bands - 1;
		double distance = Math.sqrt(pos.distSqr(origin));
		return Math.min(bands - 1, (int) Math.floor(Math.max(0.0D, distance) / radius * bands));
	}

	public List<BlockPos> inBand(int band, int bands) {
		return selected.stream().filter(pos -> band(pos, bands) == band).toList();
	}

	private void claim(BlockPos pos) {
		BlockPos immutable = pos.immutable();
		selected.add(immutable);
		selectedSet.add(immutable);
		latest = immutable;
		farthestDistance = Math.max(farthestDistance, Math.sqrt(immutable.distSqr(origin)));
		enqueueNeighbors(immutable);
	}

	private BlockPos nextEligible(Level level) {
		while (!frontier.isEmpty()) {
			BlockPos candidate = frontier.removeFirst();
			if (candidate.distSqr(origin) > (double) radius * radius || !level.isLoaded(candidate)) continue;
			if (selector.passesThrough(level, candidate)) {
				enqueueNeighbors(candidate);
				continue;
			}
			if (!selector.selectable(level, candidate)) continue;
			claim(candidate);
			return candidate;
		}
		return null;
	}

	private void enqueueNeighbors(BlockPos center) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				for (int z = -1; z <= 1; z++) {
					if (x == 0 && y == 0 && z == 0) continue;
					BlockPos neighbor = center.offset(x, y, z).immutable();
					if (!selectedSet.contains(neighbor) && queued.add(neighbor)) frontier.addLast(neighbor);
				}
			}
		}
	}
}
