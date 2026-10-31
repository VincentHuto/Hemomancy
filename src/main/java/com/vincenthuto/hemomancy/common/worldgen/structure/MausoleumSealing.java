package com.vincenthuto.hemomancy.common.worldgen.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;

import java.util.List;

/**
 * Keeps ground water out of a mausoleum. The complex lies under the water table in many places, and
 * {@code beard_thin} carves the ground around every piece, which the aquifer then fills. So, per chunk, after the
 * pieces are placed: water left inside a piece's box (in cells its template leaves to the world) becomes stone, a
 * waterlogged block there is dried, and the ground just outside a piece's face is sealed wherever it is wet, or open
 * against an opening in that face (a doorway whose niche never fitted, a gap a cave runs past). Faces that meet
 * another piece are its doorways and are left alone. The entrance pavilion stands in the open air, so outside its faces
 * only water is sealed. The pieces themselves place without taking up standing water
 * ({@link MausoleumStructure#liquidSettings()}).
 */
public final class MausoleumSealing {
	private static final Direction[] FACES = Direction.values();

	private MausoleumSealing() {
	}

	/** Seals every piece of the complex within one chunk's box. */
	public static void seal(WorldGenLevel level, BoundingBox chunkBox, List<StructurePiece> pieces) {
		for (StructurePiece piece : pieces) {
			BoundingBox box = piece.getBoundingBox();
			if (!box.inflatedBy(1).intersects(chunkBox)) continue;
			drain(level, chunkBox, box);
			boolean open = piece instanceof PoolElementStructurePiece pool
					&& MausoleumStructure.ABOVE_GROUND_PIECES.contains(MausoleumStructure.templateName(pool));
			for (Direction face : FACES) sealFace(level, chunkBox, piece, face, open, pieces);
		}
	}

	/** Inside the box: standing water becomes stone and waterlogged blocks are dried. */
	private static void drain(WorldGenLevel level, BoundingBox chunkBox, BoundingBox box) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = Math.max(box.minX(), chunkBox.minX()); x <= Math.min(box.maxX(), chunkBox.maxX()); x++) {
			for (int z = Math.max(box.minZ(), chunkBox.minZ()); z <= Math.min(box.maxZ(), chunkBox.maxZ()); z++) {
				for (int y = Math.max(box.minY(), chunkBox.minY()); y <= Math.min(box.maxY(), chunkBox.maxY()); y++) {
					pos.set(x, y, z);
					BlockState state = level.getBlockState(pos);
					if (state.getFluidState().isEmpty()) continue;
					if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
						level.setBlock(pos, state.setValue(BlockStateProperties.WATERLOGGED, false), Block.UPDATE_CLIENTS);
					} else {
						level.setBlock(pos, sealFor(y), Block.UPDATE_CLIENTS);
					}
				}
			}
		}
	}

	/** The layer of ground just outside one face of the box, within this chunk. */
	private static void sealFace(WorldGenLevel level, BoundingBox chunkBox, StructurePiece piece, Direction face,
			boolean openAir, List<StructurePiece> pieces) {
		BoundingBox box = piece.getBoundingBox();
		int minX = box.minX(), maxX = box.maxX(), minY = box.minY(), maxY = box.maxY(), minZ = box.minZ(), maxZ = box.maxZ();
		switch (face) {
			case WEST -> minX = maxX = box.minX() - 1;
			case EAST -> minX = maxX = box.maxX() + 1;
			case DOWN -> minY = maxY = box.minY() - 1;
			case UP -> minY = maxY = box.maxY() + 1;
			case NORTH -> minZ = maxZ = box.minZ() - 1;
			case SOUTH -> minZ = maxZ = box.maxZ() + 1;
		}
		minX = Math.max(minX, chunkBox.minX());
		maxX = Math.min(maxX, chunkBox.maxX());
		minY = Math.max(minY, chunkBox.minY());
		maxY = Math.min(maxY, chunkBox.maxY());
		minZ = Math.max(minZ, chunkBox.minZ());
		maxZ = Math.min(maxZ, chunkBox.maxZ());
		BlockPos.MutableBlockPos outside = new BlockPos.MutableBlockPos();
		BlockPos.MutableBlockPos inside = new BlockPos.MutableBlockPos();
		for (int x = minX; x <= maxX; x++) {
			for (int z = minZ; z <= maxZ; z++) {
				for (int y = minY; y <= maxY; y++) {
					outside.set(x, y, z);
					if (inAnotherPiece(outside, piece, pieces)) continue;
					BlockState state = level.getBlockState(outside);
					boolean wet = !state.getFluidState().isEmpty();
					if (!wet && (openAir || !state.canBeReplaced())) continue;
					inside.setWithOffset(outside, face.getOpposite());
					if (wet || opening(level, chunkBox, inside)) {
						level.setBlock(outside, sealFor(y), Block.UPDATE_CLIENTS);
					}
				}
			}
		}
	}

	/**
	 * Whether water could pass the face cell at {@code pos}. A cell in a neighbouring chunk may not be placed yet, so it
	 * counts as open: sealing solid ground outside a solid wall changes nothing.
	 */
	private static boolean opening(WorldGenLevel level, BoundingBox chunkBox, BlockPos pos) {
		return !chunkBox.isInside(pos) || !level.getBlockState(pos).isCollisionShapeFullBlock(level, pos);
	}

	private static boolean inAnotherPiece(BlockPos pos, StructurePiece self, List<StructurePiece> pieces) {
		for (StructurePiece other : pieces) {
			if (other != self && other.getBoundingBox().isInside(pos)) return true;
		}
		return false;
	}

	/**
	 * The plug: cobblestone, or cobbled deepslate below y 0. Neither is a block a spring may open in, so a plug beside a
	 * doorway never becomes a spring that pours through it.
	 */
	static BlockState sealFor(int y) {
		return (y < 0 ? Blocks.COBBLED_DEEPSLATE : Blocks.COBBLESTONE).defaultBlockState();
	}
}
