/*
 * package com.vincenthuto.hemomancy.world.structure;
 * 
 * import java.util.Optional;
 * 
 * import net.minecraft.core.BlockPos; import
 * net.minecraft.world.level.NoiseColumn; import
 * net.minecraft.world.level.block.state.BlockState; import
 * net.minecraft.world.level.levelgen.GenerationStep; import
 * net.minecraft.world.level.levelgen.Heightmap; import
 * net.minecraft.world.level.levelgen.feature.StructureFeature; import
 * net.minecraft.world.level.levelgen.feature.configurations.
 * JigsawConfiguration; import
 * net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
 * import net.minecraft.world.level.levelgen.structure.PostPlacementProcessor;
 * import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
 * import
 * net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
 * import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
 * 
 * public class BloodTempleStructure extends
 * StructureFeature<JigsawConfiguration> {
 * 
 * public BloodTempleStructure() { super(JigsawConfiguration.CODEC, context -> {
 * if (!isFeatureChunk(context)) { return Optional.empty(); } else { return
 * createPiecesGenerator(context); } }, PostPlacementProcessor.NONE); }
 * 
 * @Override public GenerationStep.Decoration step() { return
 * GenerationStep.Decoration.SURFACE_STRUCTURES; }
 * 
 * private static boolean
 * isFeatureChunk(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
 * BlockPos pos = context.chunkPos().getWorldPosition();
 * 
 * int landHeight = context.chunkGenerator().getFirstOccupiedHeight(pos.getX(),
 * pos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor());
 * 
 * NoiseColumn columnOfBlocks =
 * context.chunkGenerator().getBaseColumn(pos.getX(), pos.getZ(),
 * context.heightAccessor());
 * 
 * BlockState topBlock = columnOfBlocks.getBlock(landHeight);
 * 
 * return topBlock.getFluidState().isEmpty(); // landHeight > 100; }
 * 
 * private static Optional<PieceGenerator<JigsawConfiguration>>
 * createPiecesGenerator( PieceGeneratorSupplier.Context<JigsawConfiguration>
 * context) { BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);
 * 
 * return JigsawPlacement.addPieces(context, PoolElementStructurePiece::new,
 * blockpos, false, true); } }
 */