package com.vincenthuto.hemomancy.common.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Where a structure tag's random-spread sets would generate, predicted the way chunk generation decides it and without
 * loading a chunk or reading a structure start: for each set's potential chunk in a spacing cell, the set's placement
 * check (frequency and exclusion zones), vanilla's weighted pick among the set's structures, and that structure's own
 * generation point and biome check. This is the work worldgen workers already do, so it is safe off the server thread;
 * the {@code /locate} path instead reads and loads chunks and must stay on it. Concentric-ring sets are not searched.
 * Each cell's answer is cached for the session, cleared through {@code TransientServerStateEvents}.
 */
public final class StructureSitePrediction {
	private record Cell(ResourceKey<Level> dimension, Holder<StructureSet> set, long chunk) {
	}

	private record Spread(Holder<StructureSet> set, RandomSpreadStructurePlacement placement) {
	}

	private static final Map<Cell, Optional<Holder<Structure>>> GENERATES = new ConcurrentHashMap<>();

	private StructureSitePrediction() {
	}

	/**
	 * The nearest predicted site within {@code maxRings} spacing cells of {@code from}, by vanilla's locate rule: rings
	 * of cells outward, stopping after the first ring that holds a site. The position is the set's locate position.
	 */
	public static Optional<BlockPos> nearest(ServerLevel level, TagKey<Structure> tag, BlockPos from, int maxRings) {
		List<Spread> spreads = spreads(level, tag);
		if (spreads.isEmpty()) return Optional.empty();
		int chunkX = Math.floorDiv(from.getX(), 16), chunkZ = Math.floorDiv(from.getZ(), 16);
		BlockPos best = null;
		for (int ring = 0; ring <= maxRings && best == null; ring++) {
			for (Spread spread : spreads) {
				for (BlockPos site : ring(level, tag, spread, chunkX, chunkZ, ring)) {
					if (best == null || from.distSqr(site) < from.distSqr(best)) best = site;
				}
			}
		}
		return Optional.ofNullable(best);
	}

	/** Every predicted site whose locate position lies within {@code radiusBlocks} of {@code from}, nearest first. */
	public static List<BlockPos> within(ServerLevel level, TagKey<Structure> tag, BlockPos from, int radiusBlocks) {
		List<Spread> spreads = spreads(level, tag);
		List<BlockPos> found = new ArrayList<>();
		int chunkX = Math.floorDiv(from.getX(), 16), chunkZ = Math.floorDiv(from.getZ(), 16);
		double limit = (double) radiusBlocks * radiusBlocks;
		for (Spread spread : spreads) {
			int cellBlocks = spread.placement().spacing() * 16;
			// Every chunk of ring r lies at least (r - 1) cell widths from the asker.
			for (int ring = 0; (long) (ring - 1) * cellBlocks <= radiusBlocks; ring++) {
				for (BlockPos site : ring(level, tag, spread, chunkX, chunkZ, ring)) {
					if (from.distSqr(site) <= limit && !found.contains(site)) found.add(site);
				}
			}
		}
		found.sort(Comparator.comparingDouble(from::distSqr));
		return found;
	}

	public static void clearSessionState() {
		GENERATES.clear();
	}

	/** The tag's structure sets with a random-spread placement, in the level's own set order. */
	private static List<Spread> spreads(ServerLevel level, TagKey<Structure> tag) {
		List<Spread> spreads = new ArrayList<>();
		for (Holder<StructureSet> set : level.getChunkSource().getGeneratorState().possibleStructureSets()) {
			if (!(set.value().placement() instanceof RandomSpreadStructurePlacement placement)) continue;
			if (set.value().structures().stream().anyMatch(entry -> entry.structure().is(tag)))
				spreads.add(new Spread(set, placement));
		}
		return spreads;
	}

	/** The sites of {@code tag} in ring {@code ring} of {@code spread}'s cells around the asker's chunk, as locate does. */
	private static List<BlockPos> ring(ServerLevel level, TagKey<Structure> tag, Spread spread, int chunkX, int chunkZ,
			int ring) {
		List<BlockPos> sites = new ArrayList<>();
		var state = level.getChunkSource().getGeneratorState();
		int spacing = spread.placement().spacing();
		for (int dx = -ring; dx <= ring; dx++) for (int dz = -ring; dz <= ring; dz++) {
			if (Math.max(Math.abs(dx), Math.abs(dz)) != ring) continue;
			ChunkPos chunk = spread.placement().getPotentialStructureChunk(state.getLevelSeed(),
					chunkX + spacing * dx, chunkZ + spacing * dz);
			// Computed outside the map so a slow start check never holds its lock; a duplicate answer is identical.
			Cell cell = new Cell(level.dimension(), spread.set(), chunk.toLong());
			Optional<Holder<Structure>> generated = GENERATES.get(cell);
			if (generated == null) {
				generated = generatedAt(level, state, spread, chunk);
				GENERATES.putIfAbsent(cell, generated);
			}
			if (generated.isPresent() && generated.get().is(tag)) sites.add(spread.placement().getLocatePos(chunk));
		}
		return sites;
	}

	/** The structure {@code spread}'s set would start in {@code chunk}, chosen as {@code ChunkGenerator.createStructures} does. */
	private static Optional<Holder<Structure>> generatedAt(ServerLevel level, ChunkGeneratorStructureState state,
			Spread spread, ChunkPos chunk) {
		if (!spread.placement().isStructureChunk(state, chunk.x, chunk.z)) return Optional.empty();
		List<StructureSet.StructureSelectionEntry> entries = new ArrayList<>(spread.set().value().structures());
		if (entries.size() == 1) {
			Holder<Structure> only = entries.get(0).structure();
			return starts(level, state, only, chunk) ? Optional.of(only) : Optional.empty();
		}
		WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
		random.setLargeFeatureSeed(state.getLevelSeed(), chunk.x, chunk.z);
		int total = 0;
		for (var entry : entries) total += entry.weight();
		while (!entries.isEmpty()) {
			int pick = random.nextInt(total);
			int index = 0;
			for (var entry : entries) {
				pick -= entry.weight();
				if (pick < 0) break;
				index++;
			}
			var chosen = entries.get(index);
			if (starts(level, state, chosen.structure(), chunk)) return Optional.of(chosen.structure());
			entries.remove(index);
			total -= chosen.weight();
		}
		return Optional.empty();
	}

	/** Whether {@code structure} would make a valid start in {@code chunk}: {@code Structure.generate} without the chunk. */
	private static boolean starts(ServerLevel level, ChunkGeneratorStructureState state, Holder<Structure> structure,
			ChunkPos chunk) {
		var source = level.getChunkSource();
		var generator = source.getGenerator();
		var context = new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(),
				state.randomState(), level.getStructureManager(), state.getLevelSeed(), chunk, level,
				structure.value().biomes()::contains);
		return structure.value().findValidGenerationPoint(context)
				.map(stub -> !stub.getPiecesBuilder().isEmpty())
				.orElse(false);
	}
}
