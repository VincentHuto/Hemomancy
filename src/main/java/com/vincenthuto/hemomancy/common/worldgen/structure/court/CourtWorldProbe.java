package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicBiomes;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicContext;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicTerrainSampler;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** The live world behind {@link CourtPlacementRules.Probe}: the seeded biome source and Pelagic sampler only. */
public final class CourtWorldProbe implements CourtPlacementRules.Probe {
	/** Upper edge of vanilla's cold temperature band; below it the surface is cold or frozen ocean. */
	static final float COLD_SURFACE = -.15F;
	private static final int SEA_LEVEL = 63;

	private final RegistryAccess registries;
	private final BiomeSource biomes;
	private final RandomState state;
	private final PelagicContext pelagic;
	private final long seed;
	private List<CourtLandmarkCells.Spread> factionSpreads;
	private List<StructureSet> factionSets;
	private CourtLandmarkCells.Spread seatSpread;

	private CourtWorldProbe(RegistryAccess registries, BiomeSource biomes, RandomState state, PelagicContext pelagic, long seed) {
		this.registries = registries;
		this.biomes = biomes;
		this.state = state;
		this.pelagic = pelagic;
		this.seed = seed;
	}

	/** Null when the Pelagic hooks are not installed: disabled config, unsupported preset or another dimension. */
	@Nullable
	public static CourtWorldProbe of(RegistryAccess registries, BiomeSource biomes, RandomState state, long seed) {
		var pelagic = PelagicWorldgen.context(state);
		return pelagic == null ? null : new CourtWorldProbe(registries, biomes, state, pelagic, seed);
	}

	public long seed() {
		return seed;
	}

	@Override
	@Nullable
	public PelagicLayer layerAt(int x, int y, int z) {
		return layerOf(biome(x, y, z));
	}

	@Nullable
	public static PelagicLayer layerOf(Holder<Biome> biome) {
		for (var layer : PelagicLayer.values()) if (biome.is(PelagicBiomes.key(layer))) return layer;
		return null;
	}

	private Holder<Biome> biome(int x, int y, int z) {
		return biomes.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), state.sampler());
	}

	@Override
	public PelagicTerrainSampler.Column column(int x, int z) {
		return pelagic.column(x, z);
	}

	@Override
	public boolean coldSurface(int x, int z) {
		var point = state.sampler().sample(QuartPos.fromBlock(x), QuartPos.fromBlock(SEA_LEVEL), QuartPos.fromBlock(z));
		return Climate.unquantizeCoord(point.temperature()) < COLD_SURFACE;
	}

	@Override
	public PelagicTerrainSampler terrain() {
		return pelagic.terrain();
	}

	@Override
	public boolean nearLandmark(CourtSite site, int x, int z, int radius) {
		if (factionSpreads == null) loadLandmarkSets();
		if (CourtLandmarkCells.within(factionSpreads, this::factionLandmark, x, z, radius)) return true;
		return site.tier() == CourtSite.Tier.OUTPOST && seatSpread != null
				&& CourtLandmarkCells.within(List.of(seatSpread), (set, chunk) -> seatAt(chunk), x, z, radius);
	}

	/** Whether a Seat could stand within {@code radius} of (x, z), from the Seat set's cells (Shared Wake schools keep clear). */
	public boolean nearSeat(int x, int z, int radius) {
		if (factionSpreads == null) loadLandmarkSets();
		return seatSpread != null && CourtLandmarkCells.within(List.of(seatSpread), (set, chunk) -> seatAt(chunk), x, z, radius);
	}

	/** A faction cell counts when its surface or its floor is a biome some structure in that set accepts. */
	@Nullable
	private BlockPos factionLandmark(int set, ChunkPos chunk) {
		int x = chunk.getMiddleBlockX(), z = chunk.getMiddleBlockZ();
		var surface = biome(x, SEA_LEVEL, z);
		var floor = biome(x, pelagic.column(x, z).floorY() + 1, z);
		for (var entry : factionSets.get(set).structures()) {
			var accepted = entry.structure().value().biomes();
			if (accepted.contains(surface) || accepted.contains(floor)) return new BlockPos(x, 0, z);
		}
		return null;
	}

	/** Where a Seat would stand for this Seat cell: the Vent Sanctum moves to its vent field. */
	@Nullable
	private BlockPos seatAt(ChunkPos chunk) {
		for (var seat : CourtSite.values()) {
			if (seat.tier() != CourtSite.Tier.SEAT) continue;
			var position = CourtPlacementRules.screen(seat, this, seed, chunk.getMiddleBlockX(), chunk.getMiddleBlockZ());
			if (position.isPresent()) return position.get();
		}
		return null;
	}

	private void loadLandmarkSets() {
		var registry = registries.registryOrThrow(Registries.STRUCTURE_SET);
		var spreads = new ArrayList<CourtLandmarkCells.Spread>();
		var sets = new ArrayList<StructureSet>();
		for (var id : CourtLandmarkCells.EXCLUDED_SETS) {
			var set = registry.get(id);
			if (set != null && set.placement() instanceof RandomSpreadStructurePlacement spread) {
				spreads.add(CourtLandmarkCells.Spread.of(spread, seed, 0, CourtLandmarkCells.FACTION_SLACK));
				sets.add(set);
			}
		}
		var seats = registry.get(CourtSeatCandidates.SEATS);
		if (seats != null && seats.placement() instanceof RandomSpreadStructurePlacement spread)
			seatSpread = CourtLandmarkCells.Spread.of(spread, seed, CourtPlacementRules.SEAT_REACH, 0);
		factionSets = List.copyOf(sets);
		factionSpreads = List.copyOf(spreads);
	}
}
