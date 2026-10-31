package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Ground water stays out of a mausoleum (MausoleumSealing). A real antechamber, alone, so both its doorways open onto
 * the ground, is built into standing water high over the test floor, as natural generation places it under an aquifer:
 * no block in it may hold water, and the water against its doorways must be sealed off.
 */
@GameTestHolder("registration_validation")
@PrefixGameTestTemplate(false)
public final class MausoleumSealingGameTests {
	private MausoleumSealingGameTests() {
	}

	@GameTest(templateNamespace = "registration_validation", template = "empty")
	public static void aPieceBuiltIntoStandingWaterStaysDry(GameTestHelper helper) {
		ServerLevel level = helper.getLevel();
		var layout = MausoleumAssembly.layout(level, EnumSaintType.HEMORATH, helper.absolutePos(BlockPos.ZERO).atY(200), 11L);
		helper.assertTrue(layout.isPresent(), "the Hemorath complex lays out");
		PoolElementStructurePiece piece = layout.get().first("antechamber").orElseThrow();
		BlockPos target = helper.absolutePos(new BlockPos(2, 60, 2));
		BoundingBox laid = piece.getBoundingBox();
		piece.move(target.getX() - laid.minX(), target.getY() - laid.minY(), target.getZ() - laid.minZ());
		BoundingBox box = piece.getBoundingBox();
		BoundingBox pond = box.inflatedBy(2);
		fill(level, pond, Blocks.WATER.defaultBlockState());
		build(level, layout.get(), piece);

		List<String> problems = new ArrayList<>();
		int openings = 0;
		for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
			BlockState state = level.getBlockState(pos);
			if (!state.getFluidState().isEmpty()) problems.add("water inside at " + pos.immutable() + ": " + state);
		}
		for (Direction face : Direction.values()) {
			for (BlockPos inside : faceCells(box, face)) {
				if (level.getBlockState(inside).isCollisionShapeFullBlock(level, inside)) continue;
				openings++;
				BlockPos outside = inside.relative(face);
				BlockState state = level.getBlockState(outside);
				if (!state.getFluidState().isEmpty() || state.canBeReplaced()) {
					problems.add("the opening at " + inside + " meets " + state + " outside");
				}
			}
		}
		fill(level, pond, Blocks.AIR.defaultBlockState());
		helper.assertTrue(openings > 0, "the antechamber's doorways open onto the ground");
		helper.assertTrue(problems.isEmpty(), "Ground water reaches the piece: " + problems.subList(0,
				Math.min(problems.size(), 4)) + (problems.size() > 4 ? " and " + (problems.size() - 4) + " more" : ""));
		helper.succeed();
	}

	/** Places the one piece chunk by chunk, as natural generation does, with the structure's own afterPlace. */
	private static void build(ServerLevel level, MausoleumAssembly.Layout layout, PoolElementStructurePiece piece) {
		BoundingBox box = piece.getBoundingBox().inflatedBy(1);
		StructureStart start = new StructureStart(layout.structure(), new ChunkPos(piece.getPosition()), 0,
				new PiecesContainer(List.of(piece)));
		ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()), SectionPos.blockToSectionCoord(box.minZ()));
		ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()), SectionPos.blockToSectionCoord(box.maxZ()));
		var generator = level.getChunkSource().getGenerator();
		ChunkPos.rangeClosed(min, max).forEach(chunk -> start.placeInChunk(level, level.structureManager(), generator,
				level.getRandom(), new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
						chunk.getMaxBlockX(), level.getMaxBuildHeight(), chunk.getMaxBlockZ()), chunk));
	}

	private static void fill(ServerLevel level, BoundingBox area, BlockState state) {
		for (BlockPos pos : BlockPos.betweenClosed(area.minX(), area.minY(), area.minZ(), area.maxX(), area.maxY(), area.maxZ())) {
			level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		}
	}

	private static List<BlockPos> faceCells(BoundingBox box, Direction face) {
		List<BlockPos> cells = new ArrayList<>();
		for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
			boolean on = switch (face) {
				case WEST -> pos.getX() == box.minX();
				case EAST -> pos.getX() == box.maxX();
				case DOWN -> pos.getY() == box.minY();
				case UP -> pos.getY() == box.maxY();
				case NORTH -> pos.getZ() == box.minZ();
				case SOUTH -> pos.getZ() == box.maxZ();
			};
			if (on) cells.add(pos.immutable());
		}
		return cells;
	}
}
