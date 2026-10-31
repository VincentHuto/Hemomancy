package com.vincenthuto.hemomancy.common.worldgen.feature.court;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.CourtFacingFixtureBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.CourtFixtureBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HouseMarkerBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.SunburstBloomBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.worldgen.structure.court.CourtWorldProbe;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicContext;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicTerrainSampler;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicWorldgen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.Structure;

import javax.annotation.Nullable;

/**
 * The Brined Court's signs (Track 8), once per chunk in every Pelagic biome: Sunburst blooms on the surface, lone house
 * markers, relay posts at the depth boundaries, messenger buoys and vent offerings. Each family's candidates come from
 * {@link CourtSignRules}, whose fixed slots per region are the cap; this feature only places a slot that falls in the
 * chunk it decorates and suits the water there. It reads only its own chunk's heightmaps, biomes and blocks and writes
 * only inside that chunk, and it leaves alone any chunk a Brined Court structure reaches into (no sign inside a site).
 * Set dressing only (decision L4): nothing here carries or signals anything.
 */
public final class CourtSignFeature extends Feature<NoneFeatureConfiguration> {
	public static final TagKey<Structure> COURT = TagKey.create(Registries.STRUCTURE, Hemomancy.rloc("brined_court"));
	private static final int SURFACE = 62;

	public CourtSignFeature() {
		super(NoneFeatureConfiguration.CODEC);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		var level = context.level();
		var state = level.getLevel().getChunkSource().randomState();
		PelagicContext pelagic = PelagicWorldgen.context(state);
		if (pelagic == null) return false;
		var chunk = new ChunkPos(context.origin());
		if (courtNearby(level, chunk)) return false;
		long seed = level.getSeed();
		int rx = CourtSignRules.region(chunk.getMinBlockX()), rz = CourtSignRules.region(chunk.getMinBlockZ());
		boolean placed = false;
		for (var family : CourtSignRules.Family.values()) {
			if (family == CourtSignRules.Family.VENT_OFFERING) continue;
			for (var slot : CourtSignRules.slots(seed, family, rx, rz)) {
				if ((slot.x() >> 4) != chunk.x || (slot.z() >> 4) != chunk.z) continue;
				placed |= switch (family) {
					case SUNBURST_BLOOM -> blooms(level, chunk, slot);
					case HOUSE_MARKER -> houseMarker(context, level, slot);
					case RELAY_MARKER -> relay(level, slot);
					case MESSENGER_BUOY -> buoy(level, slot);
					case VENT_OFFERING -> false;
				};
			}
		}
		if (layer(level, chunk.getMiddleBlockX(), -42, chunk.getMiddleBlockZ()) == PelagicLayer.HYDROTHERMAL) {
			for (var offering : CourtSignRules.ventOfferings(seed, pelagic.terrain(), pelagic::column, rx, rz)) {
				if ((offering.x() >> 4) != chunk.x || (offering.z() >> 4) != chunk.z) continue;
				placed |= offering(level, offering);
			}
		}
		return placed;
	}

	/** Whether a Brined Court structure has a piece reaching into this chunk (its references, fixed before features). */
	private static boolean courtNearby(WorldGenLevel level, ChunkPos chunk) {
		var structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
		for (var structure : level.getChunk(chunk.x, chunk.z).getAllReferences().keySet()) {
			var key = structures.getResourceKey(structure);
			if (key.isPresent() && structures.getHolderOrThrow(key.get()).is(COURT)) return true;
		}
		return false;
	}

	@Nullable
	private static PelagicLayer layer(WorldGenLevel level, int x, int y, int z) {
		return CourtWorldProbe.layerOf(level.getBiome(new BlockPos(x, y, z)));
	}

	/** The first water block above the floor, or null where the column is dry or not a Pelagic floor. */
	@Nullable
	private static BlockPos floor(WorldGenLevel level, int x, int z) {
		int y = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
		var pos = new BlockPos(x, y, z);
		return level.getBlockState(pos).is(Blocks.WATER) && level.getFluidState(pos).isSource() ? pos : null;
	}

	/**
	 * Whose sea a column is: the biome at the surface where the Pelagic layers reach it (shore and reef), otherwise the
	 * floor's layer (deep columns keep vanilla ocean biomes in the water above their band).
	 */
	@Nullable
	private static PelagicLayer waterOf(WorldGenLevel level, int x, int z) {
		var surface = layer(level, x, SURFACE, z);
		if (surface != null) return surface;
		var floor = floor(level, x, z);
		return floor == null ? null : layer(level, x, floor.getY(), z);
	}

	private static boolean openSurface(WorldGenLevel level, int x, int z) {
		return level.getBlockState(new BlockPos(x, SURFACE, z)).is(Blocks.WATER)
				&& level.getBlockState(new BlockPos(x, SURFACE + 1, z)).isAir();
	}

	/** A patch of three to seven blooms round the slot, on open water over reef and coast (rarer over the open sea). */
	private static boolean blooms(WorldGenLevel level, ChunkPos chunk, CourtSignRules.Slot slot) {
		var surface = waterOf(level, slot.x(), slot.z());
		if (surface == null || !CourtSignRules.bloomSurface(surface, slot.hash()) || !openSurface(level, slot.x(), slot.z()))
			return false;
		int count = 3 + (int) Math.floorMod(slot.hash(), 5);
		boolean placed = false;
		for (int i = 0; i < count; i++) {
			long h = PelagicTerrainSampler.mix(slot.hash() + i * 31L);
			int x = slot.x() + (int) Math.floorMod(h, 7) - 3, z = slot.z() + (int) Math.floorMod(h >>> 8, 7) - 3;
			if ((x >> 4) != chunk.x || (z >> 4) != chunk.z || !openSurface(level, x, z)) continue;
			level.setBlock(new BlockPos(x, SURFACE + 1, z), bloom(h), 2);
			placed = true;
		}
		return placed;
	}

	/** A bloom's look from a hash: mostly pads, some pairs, one in five a frond bloom; turned four ways. */
	public static BlockState bloom(long hash) {
		int pick = (int) Math.floorMod(hash >>> 16, 5);
		var variant = pick == 0 ? SunburstBloomBlock.Variant.BLOOM : pick <= 2 ? SunburstBloomBlock.Variant.PAD
				: SunburstBloomBlock.Variant.PAIR;
		return BlockInit.sunburst_bloom.get().defaultBlockState().setValue(SunburstBloomBlock.VARIANT, variant)
				.setValue(SunburstBloomBlock.TURN, (int) Math.floorMod(hash >>> 20, 4));
	}

	private static boolean houseMarker(FeaturePlaceContext<NoneFeatureConfiguration> context, WorldGenLevel level,
			CourtSignRules.Slot slot) {
		var pos = floor(level, slot.x(), slot.z());
		if (pos == null) return false;
		var layer = layer(level, pos.getX(), pos.getY(), pos.getZ());
		if (layer == null) return false;
		boolean cold = false;
		if (layer == PelagicLayer.MIDNIGHT) {
			var probe = CourtWorldProbe.of(level.registryAccess(), context.chunkGenerator().getBiomeSource(),
					level.getLevel().getChunkSource().randomState(), level.getSeed());
			cold = probe != null && probe.coldSurface(pos.getX(), pos.getZ());
		}
		var state = BlockInit.house_marker.get().defaultBlockState()
				.setValue(HouseMarkerBlock.HOUSE, CourtSignRules.house(layer, cold))
				.setValue(CourtFacingFixtureBlock.FACING, Direction.from2DDataValue((int) Math.floorMod(slot.hash(), 4)))
				.setValue(CourtFixtureBlock.WATERLOGGED, true);
		if (!state.canSurvive(level, pos)) return false;
		level.setBlock(pos, state, 2);
		return true;
	}

	private static boolean relay(WorldGenLevel level, CourtSignRules.Slot slot) {
		var pos = floor(level, slot.x(), slot.z());
		if (pos == null || !CourtSignRules.nearBoundary(pos.getY() - 1) || layer(level, pos.getX(), pos.getY(), pos.getZ()) == null)
			return false;
		if (!level.getBlockState(pos.above()).is(Blocks.WATER)) return false;
		level.setBlock(pos, BlockInit.relay_marker.get().defaultBlockState().setValue(CourtFixtureBlock.WATERLOGGED, true), 2);
		return true;
	}

	/** A buoy in the top water block over the Court's deep water (a Pelagic floor at Y40 or lower), its line trailing. */
	private static boolean buoy(WorldGenLevel level, CourtSignRules.Slot slot) {
		if (!openSurface(level, slot.x(), slot.z())) return false;
		var floor = floor(level, slot.x(), slot.z());
		if (floor == null || floor.getY() > 40 || layer(level, slot.x(), floor.getY(), slot.z()) == null) return false;
		level.setBlock(new BlockPos(slot.x(), SURFACE, slot.z()),
				BlockInit.messenger_buoy.get().defaultBlockState().setValue(CourtFixtureBlock.WATERLOGGED, true), 2);
		return true;
	}

	private static boolean offering(WorldGenLevel level, CourtSignRules.Offering offering) {
		var pos = new BlockPos(offering.x(), offering.y(), offering.z());
		if (!level.getBlockState(pos).is(Blocks.WATER) || level.getBlockState(pos.below()).isAir()
				|| level.getBlockState(pos.below()).is(Blocks.WATER)) return false;
		var facing = Direction.getNearest(offering.towardX() - offering.x(), 0, offering.towardZ() - offering.z());
		level.setBlock(pos, BlockInit.vent_offering_basin.get().defaultBlockState()
				.setValue(CourtFixtureBlock.WATERLOGGED, true).setValue(CourtFacingFixtureBlock.FACING, facing), 2);
		return true;
	}
}
