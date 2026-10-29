package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.WardAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Saint's mausoleum complex. Its entrance stands on the highest ground under and around it
 * ({@link MausoleumEntranceRules}); its discovery inscriptions are part of the crypt gallery's templates
 * ({@code tools/worldgen/mausoleum_complexes.py}), not placed after the fact. Once its threshold hall is placed, the
 * ward anchor in its wall is settled with the threshold slab and the boxes of the warded and hallowed pieces.
 */
public class MausoleumStructure extends HemoJigsawStructure {

	public static final MapCodec<MausoleumStructure> CODEC = codec(MausoleumStructure::new);
	/** The halls before the threshold: open to every Harbinger, consecrated but not warded. */
	static final Set<String> OUTER_PIECES = Set.of("top", "stairs", "antechamber", "descent", "gallery",
			"descent_turn_0", "descent_turn_1");
	static final String THRESHOLD_PIECE = "threshold";
	private static final Pattern TEMPLATE = Pattern.compile("hemomancy:mausoleum/[a-z]+/([a-z0-9_]+)");

	public MausoleumStructure(Structure.StructureSettings config, Jigsaw jigsaw) {
		super(config, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return StructurePlacementChecks.isSuitableBuriedMausoleumSite(context);
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		return super.findGenerationPoint(context).flatMap(stub -> settleOnEntranceGround(context, stub));
	}

	/** Moves the whole complex up so its entrance stands on the highest ground around it, or refuses the site. */
	private static Optional<GenerationStub> settleOnEntranceGround(GenerationContext context, GenerationStub stub) {
		BlockPos centre = stub.position();
		int steps = MausoleumEntranceRules.REACH * 2 / MausoleumEntranceRules.SAMPLE_STEP + 1;
		int[] footprint = new int[steps * steps];
		int i = 0;
		for (int dx = -MausoleumEntranceRules.REACH; dx <= MausoleumEntranceRules.REACH;
				dx += MausoleumEntranceRules.SAMPLE_STEP) {
			for (int dz = -MausoleumEntranceRules.REACH; dz <= MausoleumEntranceRules.REACH;
					dz += MausoleumEntranceRules.SAMPLE_STEP) {
				footprint[i++] = firstFree(context, centre.getX() + dx, centre.getZ() + dz);
			}
		}
		var rise = MausoleumEntranceRules.rise(footprint, firstFree(context, centre.getX(), centre.getZ()));
		if (rise.isEmpty()) return Optional.empty();
		if (rise.getAsInt() == 0) return Optional.of(stub);
		int by = rise.getAsInt();
		return Optional.of(new GenerationStub(centre.above(by), builder -> {
			StructurePiecesBuilder laid = new StructurePiecesBuilder();
			stub.generator().ifLeft(generator -> generator.accept(laid))
					.ifRight(built -> built.build().pieces().forEach(laid::addPiece));
			for (StructurePiece piece : laid.build().pieces()) {
				piece.move(0, by, 0);
				if (piece instanceof PoolElementStructurePiece pool) raiseJunctions(pool, by);
				builder.addPiece(piece);
			}
		}));
	}

	/**
	 * Jigsaw junctions hold absolute ground heights that {@link StructurePiece#move} leaves behind; the beardifier
	 * blends terrain toward them, so they rise with their piece.
	 */
	private static void raiseJunctions(PoolElementStructurePiece piece, int by) {
		List<JigsawJunction> junctions = piece.getJunctions();
		List<JigsawJunction> raised = junctions.stream().map(junction -> new JigsawJunction(junction.getSourceX(),
				junction.getSourceGroundY() + by, junction.getSourceZ(), junction.getDeltaY(),
				junction.getDestProjection())).toList();
		junctions.clear();
		junctions.addAll(raised);
	}

	private static int firstFree(GenerationContext context, int x, int z) {
		return context.chunkGenerator().getFirstFreeHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
				context.heightAccessor(), context.randomState());
	}

	@Override
	public void afterPlace(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
			RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, PiecesContainer pieces) {
		for (StructurePiece piece : pieces.pieces()) {
			if (piece instanceof PoolElementStructurePiece pool && THRESHOLD_PIECE.equals(templateName(pool))
					&& piece.getBoundingBox().intersects(chunkBox)) {
				settleWard(level, chunkBox, pool, pieces);
			}
		}
	}

	/** Finds the threshold hall's ward anchor in this chunk and gives it the slab and the piece boxes. */
	private static void settleWard(WorldGenLevel level, BoundingBox chunkBox, PoolElementStructurePiece threshold,
			PiecesContainer pieces) {
		BoundingBox hall = threshold.getBoundingBox();
		BlockPos from = new BlockPos(Math.max(hall.minX(), chunkBox.minX()), Math.max(hall.minY(), chunkBox.minY()),
				Math.max(hall.minZ(), chunkBox.minZ()));
		BlockPos to = new BlockPos(Math.min(hall.maxX(), chunkBox.maxX()), Math.min(hall.maxY(), chunkBox.maxY()),
				Math.min(hall.maxZ(), chunkBox.maxZ()));
		if (from.getX() > to.getX() || from.getY() > to.getY() || from.getZ() > to.getZ()) return;
		for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
			if (!level.getBlockState(pos).is(BlockInit.ward_anchor.get())
					|| !(level.getBlockEntity(pos) instanceof WardAnchorBlockEntity anchor)) continue;
			anchor.settle(threshold.getRotation(), List.of(), List.of());
			AABB slab = anchor.threshold();
			List<BoundingBox> ward = new ArrayList<>();
			List<BoundingBox> hallowed = new ArrayList<>();
			for (StructurePiece piece : pieces.pieces()) {
				hallowed.add(piece.getBoundingBox());
				String name = piece instanceof PoolElementStructurePiece pool ? templateName(pool) : "";
				if (piece == threshold) {
					ward.add(pastThreshold(piece.getBoundingBox(), slab, anchor.inward()));
				} else if (!OUTER_PIECES.contains(name)) {
					ward.add(piece.getBoundingBox());
				}
			}
			anchor.settle(threshold.getRotation(), ward, hallowed);
			return;
		}
	}

	/** The part of the threshold hall from the ward line inward. */
	static BoundingBox pastThreshold(BoundingBox hall, AABB slab, Direction inward) {
		int minX = hall.minX(), maxX = hall.maxX(), minZ = hall.minZ(), maxZ = hall.maxZ();
		switch (inward) {
			case EAST -> minX = (int) Math.floor(slab.minX);
			case WEST -> maxX = (int) Math.ceil(slab.maxX) - 1;
			case SOUTH -> minZ = (int) Math.floor(slab.minZ);
			default -> maxZ = (int) Math.ceil(slab.maxZ) - 1;
		}
		return new BoundingBox(minX, hall.minY(), minZ, maxX, hall.maxY(), maxZ);
	}

	/** {@code nave} for a piece built from {@code hemomancy:mausoleum/<saint>/nave}. */
	static String templateName(PoolElementStructurePiece piece) {
		Matcher matcher = TEMPLATE.matcher(piece.getElement().toString());
		return matcher.find() ? matcher.group(1) : "";
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.mausoleum.get();
	}
}
