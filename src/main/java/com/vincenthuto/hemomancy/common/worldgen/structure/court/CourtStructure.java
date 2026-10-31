package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.CourtFacingFixtureBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.CourtFixtureBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.worldgen.feature.court.CourtSignFeature;
import com.vincenthuto.hemomancy.common.worldgen.structure.HemoJigsawStructure;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicContext;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLandmarks;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicTerrainSampler;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicWorldgen;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;

import java.util.List;
import java.util.Optional;

/**
 * One Brined Court site. Placement belongs to {@link CourtPlacementRules}; the template pool is the site's look, so
 * a site's builder replaces its pool's templates without touching where it stands. Where the start piece stands
 * relative to the marker is {@link CourtSeatLayout#start}: outposts one block above it (a pool element's ground layer
 * sits one block under its start), the Lighthouse Garden pinned to the sea surface, the Vent Sanctum's start jigsaw
 * high over its centre column. The Sanctum's heads (crowns, basins, threshold walkway) follow the terrain's chimneys,
 * so they are added here after the pieces, from {@link VentSanctumHeads}, into water only; the Garden's ring of floating
 * Sunburst blooms is added here too, onto open water only. Every site's residents take their marks here
 * ({@link CourtResidentSpawner}), chunk by chunk.
 */
public final class CourtStructure extends HemoJigsawStructure {
	public static final MapCodec<CourtStructure> CODEC = codec(CourtSite.CODEC.fieldOf("site"), CourtStructure::site,
			CourtStructure::new);

	private final CourtSite site;

	public CourtStructure(Structure.StructureSettings settings, Jigsaw jigsaw, CourtSite site) {
		super(settings, jigsaw);
		this.site = site;
	}

	public CourtSite site() {
		return site;
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return probe(context) != null;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		var stub = CourtPlacementRules.stub(site, probe(context), context.seed(),
				context.chunkPos().getMiddleBlockX(), context.chunkPos().getMiddleBlockZ());
		return stub.flatMap(marker -> addPieces(context, CourtSeatLayout.start(site, marker), Optional.empty()));
	}

	private static CourtWorldProbe probe(GenerationContext context) {
		return CourtWorldProbe.of(context.registryAccess(), context.biomeSource(), context.randomState(), context.seed());
	}

	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
			RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, PiecesContainer pieces) {
		if (pieces.isEmpty()) return;
		CourtResidentSpawner.spawn(level, chunkBox, pieces);
		if (site == CourtSite.LIGHTHOUSE_GARDEN) {
			gardenBlooms(level, chunkBox, pieces.pieces().get(0).getBoundingBox());
			CourtPresencePlacement.place(level, chunkBox, pieces);
			return;
		}
		if (site != CourtSite.VENT_SANCTUM) return;
		var geology = PelagicWorldgen.context(level.getLevel().getChunkSource().randomState());
		if (geology == null) return;
		var start = pieces.pieces().get(0).getBoundingBox();
		var heads = heads(geology, start);
		for (var spot : heads) {
			if (!chunkBox.isInside(spot.pos()) || !level.getBlockState(spot.pos()).is(Blocks.WATER)) continue;
			level.setBlock(spot.pos(), state(spot), 2);
		}
		CourtResidentSpawner.spawnHeadTender(level, chunkBox, heads, CourtSeatLayout.sanctumMarker(start));
	}

	/**
	 * Floating Sunburst blooms on the open water round the Lighthouse Garden (Track 8): the sign feature's blooms, denser
	 * here. A ring from the tide rock's edge to the start piece's edge, one open-water cell in four, by position hash.
	 */
	private static void gardenBlooms(WorldGenLevel level, BoundingBox chunkBox, BoundingBox garden) {
		int cx = (garden.minX() + garden.maxX()) / 2, cz = (garden.minZ() + garden.maxZ()) / 2;
		int surface = CourtSeatLayout.GARDEN_WATERLINE_Y;
		for (int x = Math.max(chunkBox.minX(), garden.minX()); x <= Math.min(chunkBox.maxX(), garden.maxX()); x++) {
			for (int z = Math.max(chunkBox.minZ(), garden.minZ()); z <= Math.min(chunkBox.maxZ(), garden.maxZ()); z++) {
				double r = Math.hypot(x - cx, z - cz);
				if (r < 17 || r > 23.5) continue;
				long hash = PelagicTerrainSampler.mix(level.getSeed() ^ x * 0x632BE59BD9B4E019L ^ z * 0xD1B54A32D192ED03L);
				if (Math.floorMod(hash, 4) != 0) continue;
				var water = new net.minecraft.core.BlockPos(x, surface, z);
				if (!level.getBlockState(water).is(Blocks.WATER) || !level.getBlockState(water.above()).isAir()) continue;
				level.setBlock(water.above(), CourtSignFeature.bloom(hash), 2);
			}
		}
	}

	/** The Sanctum's head spots for its start piece: the vent site under the marker, its chimneys, the ring level. */
	public static List<VentSanctumHeads.Spot> heads(PelagicContext geology, BoundingBox startPiece) {
		var marker = CourtSeatLayout.sanctumMarker(startPiece);
		var vent = geology.terrain().ventSite(Math.floorDiv(marker.getX(), PelagicTerrainSampler.VENT_CELL),
				Math.floorDiv(marker.getZ(), PelagicTerrainSampler.VENT_CELL));
		if (vent.x() != marker.getX() || vent.z() != marker.getZ()) return List.of();
		var chimneys = PelagicLandmarks.chimneys(vent, geology::column);
		return VentSanctumHeads.plan(marker, chimneys, (x, z) -> geology.column(x, z).floorY(), marker.getY());
	}

	private static BlockState state(VentSanctumHeads.Spot spot) {
		return switch (spot.kind()) {
			case CROWN -> BlockInit.hydra_crown.get().defaultBlockState().setValue(CourtFixtureBlock.WATERLOGGED, true);
			case BASIN -> BlockInit.vent_offering_basin.get().defaultBlockState()
					.setValue(CourtFixtureBlock.WATERLOGGED, true).setValue(CourtFacingFixtureBlock.FACING, spot.facing());
			case WALK -> Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
			case WALK_TOP -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
			case POST -> Blocks.POLISHED_BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
			case LIGHT -> Blocks.SEA_PICKLE.defaultBlockState().setValue(SeaPickleBlock.PICKLES, 3)
					.setValue(SeaPickleBlock.WATERLOGGED, true);
		};
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.brined_court.get();
	}
}
