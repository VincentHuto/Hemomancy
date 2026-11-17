package com.vincenthuto.hemomancy.common.tile.harbinger.menagerie;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.SpecimenVatBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.VatPart;
import com.vincenthuto.hemomancy.common.entity.menagerie.MenagerieRules;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;
import com.vincenthuto.hemomancy.common.util.BlockEntitySync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * How Specimen Vats join (core-loop Track 5). Empty vats standing together, side by side or one on another, join
 * into square vessels: from the lowest corner, the largest square of vats up to {@link MenagerieRules#VAT_MAX_WIDTH}
 * across and {@link MenagerieRules#VAT_MAX_LAYERS} high that is all there, all empty and kept by one keeper, then the
 * next. A vessel holding a creature or a husk never re-forms: build the vessel first, then lead the creature in.
 * Whenever a vat is set down or taken away, the empty vats it touched re-form, and the blood of every vessel that
 * re-forms is shared out a vat at a time into the vessels it becomes. The block states follow ({@link VatPart}), so the
 * vessel is drawn as one octagonal vat grown to its size.
 */
public final class VatAssembly {
	/** No re-forming reaches further than this many vats from where it began. */
	private static final int MAX_REACH = 256;

	private VatAssembly() {
	}

	/** One vat's share of its old vessel's blood, and that vessel's keeper. */
	private record Share(double blood, @Nullable UUID owner) {
	}

	/** Whether the lower half of a Specimen Vat stands at {@code pos}. */
	static boolean isVat(Level level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.getBlock() instanceof SpecimenVatBlock && state.getValue(SpecimenVatBlock.HALF) == DoubleBlockHalf.LOWER;
	}

	/** A vat was set down at {@code pos}: it and the empty vats it touches re-form. */
	public static void placed(ServerLevel level, BlockPos pos) {
		reform(level, List.of(pos), Map.of());
	}

	/**
	 * The vat at {@code pos} is going (its block already changed, its block entity still readable): the creature its
	 * vessel kept is let go ({@link SpecimenVessels#releaseOnRemove}), and the rest of the vessel, with its share of the
	 * blood, re-forms with the empty vats around it. The going vat's share of the blood goes with it.
	 */
	public static void removed(ServerLevel level, BlockPos pos, SpecimenVatBlockEntity vat, boolean suppressed) {
		SpecimenVatBlockEntity runner = vat.runner();
		List<BlockPos> vats = vat.vatPositions();
		SpecimenVessels.releaseOnRemove(level, runner.releasePos(), runner, suppressed);
		BlockBloodVolume buffer = runner.buffer();
		Share share = new Share(buffer.getBloodVolume() / vats.size(), buffer.owner());
		Map<BlockPos, Share> left = new LinkedHashMap<>();
		for (BlockPos other : vats) {
			if (!other.equals(pos)) left.put(other, share);
		}
		List<BlockPos> seeds = new ArrayList<>(left.keySet());
		for (Direction direction : Direction.Plane.HORIZONTAL) seeds.add(pos.relative(direction));
		seeds.add(pos.above(2));
		seeds.add(pos.below(2));
		reform(level, seeds, left);
	}

	/**
	 * Gathers the empty vats reachable from {@code seeds}, splits them into vessels, and writes the vessels: who runs
	 * each, its size, its blood and keeper, and every vat's block state. {@code shares} holds the share of vats whose
	 * old vessel is already gone; every other vat takes its share from the vessel it stands in now.
	 */
	private static void reform(ServerLevel level, List<BlockPos> seeds, Map<BlockPos, Share> shares) {
		Map<BlockPos, Share> free = new LinkedHashMap<>();
		Deque<BlockPos> open = new ArrayDeque<>(seeds);
		while (!open.isEmpty() && free.size() < MAX_REACH) {
			BlockPos at = open.poll();
			if (free.containsKey(at) || !isVat(level, at) || !(level.getBlockEntity(at) instanceof SpecimenVatBlockEntity vat)) continue;
			Share share = shares.get(at);
			if (share == null) {
				SpecimenVatBlockEntity runner = vat.runner();
				// A vessel keeping a creature or a husk stays exactly as it is.
				if (runner.kept().occupied()) continue;
				BlockBloodVolume buffer = runner.buffer();
				share = new Share(buffer.getBloodVolume() / runner.vats(), buffer.owner());
			}
			free.put(at, share);
			for (Direction direction : Direction.Plane.HORIZONTAL) open.add(at.relative(direction));
			open.add(at.above(2));
			open.add(at.below(2));
		}
		if (free.isEmpty()) return;
		List<List<BlockPos>> vessels = split(free);
		for (List<BlockPos> vessel : vessels) write(level, vessel, free);
	}

	/**
	 * The free vats as vessels: lowest corner first (by height, then x, then z), each the largest square that is all
	 * free, not yet taken, and kept by at most one keeper.
	 */
	private static List<List<BlockPos>> split(Map<BlockPos, Share> free) {
		List<BlockPos> order = new ArrayList<>(free.keySet());
		order.sort(Comparator.<BlockPos>comparingInt(BlockPos::getY).thenComparingInt(BlockPos::getX).thenComparingInt(BlockPos::getZ));
		Set<BlockPos> taken = new HashSet<>();
		List<List<BlockPos>> vessels = new ArrayList<>();
		for (BlockPos corner : order) {
			if (taken.contains(corner)) continue;
			List<BlockPos> chosen = null;
			for (int width = MenagerieRules.VAT_MAX_WIDTH; width >= 1 && chosen == null; width--) {
				for (int layers = MenagerieRules.VAT_MAX_LAYERS; layers >= 1 && chosen == null; layers--) {
					List<BlockPos> cells = cells(corner, width, layers);
					if (cells.stream().allMatch(cell -> free.containsKey(cell) && !taken.contains(cell)) && oneKeeper(cells, free)) {
						chosen = cells;
					}
				}
			}
			taken.addAll(chosen);
			vessels.add(chosen);
		}
		return vessels;
	}

	/** The vats of a vessel {@code width} across and {@code layers} high from {@code corner}, the corner first. */
	static List<BlockPos> cells(BlockPos corner, int width, int layers) {
		List<BlockPos> cells = new ArrayList<>();
		for (int layer = 0; layer < layers; layer++) {
			for (int dx = 0; dx < width; dx++) {
				for (int dz = 0; dz < width; dz++) cells.add(corner.offset(dx, layer * 2, dz));
			}
		}
		return cells;
	}

	private static boolean oneKeeper(List<BlockPos> cells, Map<BlockPos, Share> free) {
		return cells.stream().map(cell -> free.get(cell).owner()).filter(Objects::nonNull).distinct().count() <= 1;
	}

	/** Writes one vessel: its corner runs it with the vessel's blood and keeper; the rest answer to the corner. */
	private static void write(ServerLevel level, List<BlockPos> cells, Map<BlockPos, Share> free) {
		BlockPos corner = cells.get(0);
		int layers = 1 + (cells.get(cells.size() - 1).getY() - corner.getY()) / 2;
		int width = (int) Math.round(Math.sqrt(cells.size() / (double) layers));
		double blood = 0.0D;
		UUID owner = null;
		for (BlockPos cell : cells) {
			Share share = free.get(cell);
			blood += share.blood();
			if (owner == null) owner = share.owner();
		}
		for (BlockPos cell : cells) {
			if (!(level.getBlockEntity(cell) instanceof SpecimenVatBlockEntity vat)) continue;
			if (cell.equals(corner)) vat.runAs(width, layers);
			else vat.joinTo(corner);
		}
		if (level.getBlockEntity(corner) instanceof SpecimenVatBlockEntity runner) {
			BlockBloodVolume buffer = runner.buffer();
			buffer.setBloodVolume(Math.min(blood, buffer.getMaxBloodVolume()));
			buffer.setOwner(owner);
		}
		for (BlockPos cell : cells) {
			VatPart part = VatPart.of(width, layers, (cell.getY() - corner.getY()) / 2, cell.getX() - corner.getX(),
					cell.getZ() - corner.getZ());
			shape(level, cell, part);
			if (level.getBlockEntity(cell) instanceof SpecimenVatBlockEntity vat) BlockEntitySync.markAndSync(vat);
		}
	}

	/** Sets a vat's two halves to what the vat draws of its vessel. */
	private static void shape(ServerLevel level, BlockPos cell, VatPart part) {
		BlockState lower = level.getBlockState(cell);
		BlockState shaped = lower.setValue(SpecimenVatBlock.PART, part);
		int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
		if (shaped != lower) level.setBlock(cell, shaped, flags);
		BlockState upper = level.getBlockState(cell.above());
		if (upper.getBlock() instanceof SpecimenVatBlock) {
			BlockState upperShaped = shaped.setValue(SpecimenVatBlock.HALF, DoubleBlockHalf.UPPER);
			if (upperShaped != upper) level.setBlock(cell.above(), upperShaped, flags);
		}
	}
}
