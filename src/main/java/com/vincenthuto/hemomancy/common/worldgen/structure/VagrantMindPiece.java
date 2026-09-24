package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.EntityInit;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerEntity;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.worldgen.CorticalAxis;
import com.vincenthuto.hemomancy.common.worldgen.feature.VoxelBresenhamIterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Places the Vagrant Mind. Everything is derived from {@link #seed} and {@link #origin}, so each
 * chunk independently recomputes the identical organ and the shell never tears at chunk borders.
 */
public class VagrantMindPiece extends StructurePiece {
	/**
	 * Empty guard faces around every tested shell voxel. Keeping this at eight chunks from the start
	 * chunk also stays inside vanilla's structure-reference search radius.
	 */
	private static final int HALF_WIDTH = 128;
	/** Includes the descending brainstem without clipping its inferior shell. */
	private static final int HALF_HEIGHT = 128;
	private static final double SHELL_THICKNESS = 8.0;
	private static final double FISSURE_HALF_WIDTH = 3.0;
	/** Segments each strand is split into so the midpoint droop reads as a curve, not a kink. */
	private static final int STRAND_SEGMENTS = 8;
	private static final double MAX_DROOP = 6.0;

	private final BlockPos origin;
	private final long seed;
	/**
	 * Lobes and strands are pure functions of {@link #seed}, but each costs real work -- the strand
	 * pass alone walks up to 4000 anchor attempts of 40 steps each -- and the same piece instance
	 * postProcesses every one of the ~225 chunks it covers. They are derived once and shared. The
	 * fields are volatile and the computation is idempotent, so a race between chunk workers can at
	 * worst duplicate the work, never publish a half-built list or a differing one.
	 */
	private volatile List<VagrantMindGeometry.Lobe> cachedLobes;
	private volatile List<VagrantMindFiberWeb.Strand> cachedStrands;

	public BlockPos origin() {
		return origin;
	}

	public long seed() {
		return seed;
	}

	public boolean containsInterior(BlockPos pos) {
		double x = pos.getX() - origin.getX();
		double y = pos.getY() - origin.getY();
		double z = pos.getZ() - origin.getZ();
		List<VagrantMindGeometry.Lobe> lobes = lobes();
		return VagrantMindGeometry.field(lobes, x, y, z) >= 1.0
				&& !VagrantMindGeometry.isShell(lobes, x, y, z, SHELL_THICKNESS);
	}

	public List<BlockPos> nodePositions() {
		return VagrantMindFiberWeb.nodes(strands(lobes())).stream()
				.map(point -> world(point[0], point[1], point[2]))
				.toList();
	}

	public VagrantMindPiece(BlockPos origin, long seed) {
		super(StructureInit.vagrant_mind_piece.get(), 0, envelope(origin));
		this.origin = origin;
		this.seed = seed;
	}

	public VagrantMindPiece(StructurePieceSerializationContext context, CompoundTag tag) {
		super(StructureInit.vagrant_mind_piece.get(), tag);
		this.origin = new BlockPos(tag.getInt("OriginX"), tag.getInt("OriginY"), tag.getInt("OriginZ"));
		this.seed = tag.getLong("Seed");
	}

	private static BoundingBox envelope(BlockPos origin) {
		return new BoundingBox(
				origin.getX() - HALF_WIDTH, origin.getY() - HALF_HEIGHT, origin.getZ() - HALF_WIDTH,
				origin.getX() + HALF_WIDTH, origin.getY() + HALF_HEIGHT, origin.getZ() + HALF_WIDTH);
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
		tag.putInt("OriginX", this.origin.getX());
		tag.putInt("OriginY", this.origin.getY());
		tag.putInt("OriginZ", this.origin.getZ());
		tag.putLong("Seed", this.seed);
	}

	@Override
	public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator,
			RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos pos) {
		// Derived once per piece: never inside the voxel loop, and never from unseeded randomness.
		List<VagrantMindGeometry.Lobe> lobes = lobes();
		BlockState folds = BlockInit.cortical_folds.get().defaultBlockState();
		BlockState tissue = BlockInit.neural_tissue.get().defaultBlockState();
		BlockState synapse = BlockInit.synaptic_node.get().defaultBlockState();

		BoundingBox box = this.boundingBox;
		int minX = Math.max(chunkBox.minX(), box.minX());
		int maxX = Math.min(chunkBox.maxX(), box.maxX());
		int minY = Math.max(chunkBox.minY(), box.minY());
		int maxY = Math.min(chunkBox.maxY(), box.maxY());
		int minZ = Math.max(chunkBox.minZ(), box.minZ());
		int maxZ = Math.min(chunkBox.maxZ(), box.maxZ());
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		carveNaturalTerrain(level, lobes, minX, maxX, minY, maxY, minZ, maxZ, cursor);

		for (int x = minX; x <= maxX; x++) {
			double lx = x - this.origin.getX();
			for (int z = minZ; z <= maxZ; z++) {
				double lz = z - this.origin.getZ();
				// The envelope is a box; the organ inside it is not. Columns and rows the metaball
				// field cannot reach are skipped before paying for isShell's field evaluations.
				double[] span = VagrantMindGeometry.columnSpan(lobes, lx, lz);
				if (span == null) {
					continue;
				}
				int fromY = Math.max(minY, this.origin.getY() + Mth.ceil(span[0]));
				int toY = Math.min(maxY, this.origin.getY() + Mth.floor(span[1]));
				for (int y = fromY; y <= toY; y++) {
					double ly = y - this.origin.getY();
					VagrantMindGeometry.SurfaceMaterial material = VagrantMindGeometry.surfaceMaterial(
							lobes, lx, ly, lz, SHELL_THICKNESS, FISSURE_HALF_WIDTH);
					if (material == VagrantMindGeometry.SurfaceMaterial.NONE) {
						continue;
					}
					BlockState wall = material == VagrantMindGeometry.SurfaceMaterial.FOLDS ? folds : tissue;
					if (VagrantMindGeometry.isSynapseBlob(seed, (int) lx, (int) ly, (int) lz)
							&& VagrantMindGeometry.isInnerWall(lobes, (int) lx, (int) ly, (int) lz,
									SHELL_THICKNESS)) {
						wall = synapse;
					}
					level.setBlock(cursor.set(x, y, z), wall, 2);
				}
			}
		}
		placeFiberWeb(level, chunkBox, lobes);
		placeBorers(level, chunkBox, lobes);
	}

	/** A few persistent residents start on the middle of interior strands, never on the shell. */
	private void placeBorers(WorldGenLevel level, BoundingBox chunkBox,
			List<VagrantMindGeometry.Lobe> lobes) {
		if (level.getDifficulty() == Difficulty.PEACEFUL) {
			return;
		}
		List<VagrantMindFiberWeb.Strand> web = strands(lobes);
		for (int i = 0; i < web.size(); i += 6) {
			BlockPos cable = saggedPoint(web.get(i), 4);
			if (!chunkBox.isInside(cable) || !level.getBlockState(cable).is(MyelinBorerEntity.CRAWLABLE)
					|| !level.getBlockState(cable.above()).isAir()
					|| !level.getBlockState(cable.above(2)).isAir()) {
				continue;
			}
			var borer = EntityInit.myelin_borer.get().create(level.getLevel());
			if (borer != null) {
				BlockPos perch = cable.above();
				borer.moveTo(perch.getX() + 0.5D, perch.getY(), perch.getZ() + 0.5D,
						level.getRandom().nextFloat() * 360.0F, 0.0F);
				borer.finalizeSpawn(level, level.getCurrentDifficultyAt(perch), MobSpawnType.STRUCTURE, null);
				borer.setPersistenceRequired();
				level.addFreshEntityWithPassengers(borer);
			}
		}
	}

	/**
	 * Removes only vanilla End Stone where it intersects the expanded metaball silhouette. The
	 * boundary follows the same joined lobes as the brain, leaving an irregular terrain lip instead
	 * of a spherical or cuboid deletion wall. Shell placement runs afterward and closes the organ.
	 */
	private void carveNaturalTerrain(WorldGenLevel level, List<VagrantMindGeometry.Lobe> lobes,
			int minX, int maxX, int minY, int maxY, int minZ, int maxZ, BlockPos.MutableBlockPos cursor) {
		BlockState air = Blocks.AIR.defaultBlockState();
		for (int x = minX; x <= maxX; x++) {
			double lx = x - this.origin.getX();
			for (int z = minZ; z <= maxZ; z++) {
				double lz = z - this.origin.getZ();
				double[] span = VagrantMindGeometry.columnSpan(lobes, lx, lz);
				if (span == null) {
					continue;
				}
				int fromY = Math.max(minY, this.origin.getY() + Mth.ceil(span[0]));
				int toY = Math.min(maxY, this.origin.getY() + Mth.floor(span[1]));
				for (int y = fromY; y <= toY; y++) {
					double ly = y - this.origin.getY();
					if (!VagrantMindGeometry.clearsTerrain(lobes, lx, ly, lz)) {
						continue;
					}
					cursor.set(x, y, z);
					if (level.getBlockState(cursor).is(Blocks.END_STONE)) {
						level.setBlock(cursor, air, 2);
					}
				}
			}
		}
	}

	/**
	 * Strings the interior nerve web. {@link VoxelBresenhamIterator} draws straight lines only, so
	 * each strand is subdivided and its intermediate points pulled downward — maximum sag at the
	 * midpoint, tapering to zero at both anchors — to give cables a hanging curve.
	 */
	private void placeFiberWeb(WorldGenLevel level, BoundingBox chunkBox,
			List<VagrantMindGeometry.Lobe> lobes) {
		List<VagrantMindFiberWeb.Strand> strands = strands(lobes);
		BlockState bundle = BlockInit.nerve_bundle.get().defaultBlockState();
		BlockState fiber = BlockInit.nerve_fiber.get().defaultBlockState();
		BlockState node = BlockInit.synaptic_node.get().defaultBlockState();

		for (VagrantMindFiberWeb.Strand strand : strands) {
			BlockState cable = strand.thick() ? bundle : fiber;
			BlockPos previous = saggedPoint(strand, 0);
			for (int step = 1; step <= STRAND_SEGMENTS; step++) {
				BlockPos next = saggedPoint(strand, step);
				Direction.Axis axis = CorticalAxis.between(previous, next);
				BlockState oriented = cable.setValue(RotatedPillarBlock.AXIS, axis);
				for (BlockPos cablePos : new VoxelBresenhamIterator(previous, next)) {
					if (chunkBox.isInside(cablePos)) {
						level.setBlock(cablePos.immutable(), oriented, 2);
					}
				}
				previous = next;
			}
		}

		for (double[] junction : VagrantMindFiberWeb.nodes(strands)) {
			BlockPos nodePos = world(junction[0], junction[1], junction[2]);
			if (chunkBox.isInside(nodePos)) {
				level.setBlock(nodePos, node, 2);
			}
		}
	}

	private List<VagrantMindGeometry.Lobe> lobes() {
		List<VagrantMindGeometry.Lobe> lobes = this.cachedLobes;
		if (lobes == null) {
			lobes = VagrantMindGeometry.lobes(this.seed);
			this.cachedLobes = lobes;
		}
		return lobes;
	}

	private List<VagrantMindFiberWeb.Strand> strands(List<VagrantMindGeometry.Lobe> lobes) {
		List<VagrantMindFiberWeb.Strand> strands = this.cachedStrands;
		if (strands == null) {
			strands = VagrantMindFiberWeb.strands(this.seed, lobes);
			this.cachedStrands = strands;
		}
		return strands;
	}

	/** Point {@code step}/{@code STRAND_SEGMENTS} along the strand, displaced by the catenary sag. */
	private BlockPos saggedPoint(VagrantMindFiberWeb.Strand strand, int step) {
		double t = (double) step / STRAND_SEGMENTS;
		double x = Mth.lerp(t, strand.ax(), strand.bx());
		double y = Mth.lerp(t, strand.ay(), strand.by()) - MAX_DROOP * Math.sin(Math.PI * t);
		double z = Mth.lerp(t, strand.az(), strand.bz());
		return world(x, y, z);
	}

	private BlockPos world(double x, double y, double z) {
		return new BlockPos(
				this.origin.getX() + Mth.floor(x),
				this.origin.getY() + Mth.floor(y),
				this.origin.getZ() + Mth.floor(z));
	}
}
