package com.vincenthuto.hemomancy.world.structure;

import java.util.Random;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.init.PieceInit;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public class BloodTempleStructure extends StructureFeature<NoneFeatureConfiguration> {

	public BloodTempleStructure(Codec<NoneFeatureConfiguration> p_72474_) {
		super(p_72474_, PieceGeneratorSupplier.simple(BloodTempleStructure::checkLocation,
				BloodTempleStructure::generatePieces));
	}

	private static boolean checkLocation(PieceGeneratorSupplier.Context<NoneFeatureConfiguration> p_197155_) {
		return p_197155_.validBiomeOnTop(Heightmap.Types.WORLD_SURFACE_WG);
	}

	private static void addPiece(StructureManager structureManager, BlockPos blockPos, Rotation rotation,
			StructurePiecesBuilder structurePieceAccessor, Random random,
			NoneFeatureConfiguration noneFeatureConfiguration) {
		ResourceLocation piece = BloodTemplePiece.blood_temple;
		structurePieceAccessor.addPiece(new BloodTemplePiece(0, structureManager, piece, piece.toString(),
				BloodTemplePiece.makeSettings(rotation), blockPos));
	}

	private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder,
			PieceGenerator.Context<NoneFeatureConfiguration> configurationContext) {
		int height = configurationContext.chunkGenerator().getFirstFreeHeight(
				configurationContext.chunkPos().getMinBlockX(), configurationContext.chunkPos().getMinBlockZ(),
				Heightmap.Types.WORLD_SURFACE_WG, configurationContext.heightAccessor());
		BlockPos blockpos = new BlockPos(configurationContext.chunkPos().getMinBlockX(), height,
				configurationContext.chunkPos().getMinBlockZ());
		Rotation rotation = Rotation.getRandom(configurationContext.random());
		addPiece(configurationContext.structureManager(), blockpos, rotation, structurePiecesBuilder,
				configurationContext.random(), configurationContext.config());
	}

	@Override
	public GenerationStep.Decoration step() {
		return GenerationStep.Decoration.SURFACE_STRUCTURES;
	}

	public static class BloodTemplePiece extends TemplateStructurePiece {

		private static final ResourceLocation blood_temple = new ResourceLocation(Hemomancy.MOD_ID, "blood_temple");

		public BloodTemplePiece(int p_163661_, StructureManager p_163662_, ResourceLocation p_163663_, String p_163664_,
				StructurePlaceSettings p_163665_, BlockPos p_163666_) {
			super(PieceInit.blood_temple_piece, p_163661_, p_163662_, p_163663_, p_163664_, p_163665_, p_163666_);
		}

		public BloodTemplePiece(CompoundTag p_192678_, StructureManager p_192679_,
				Function<ResourceLocation, StructurePlaceSettings> p_192680_) {
			super(PieceInit.blood_temple_piece, p_192678_, p_192679_, p_192680_);
		}

		public BloodTemplePiece(StructurePieceSerializationContext structurePieceSerializationContext,
				CompoundTag tag) {
			super(PieceInit.blood_temple_piece, tag, structurePieceSerializationContext.structureManager(),
					(p_162451_) -> makeSettings(Rotation.getRandom(new Random())));
		}

		private static StructurePlaceSettings makeSettings(Rotation rot) {
			return (new StructurePlaceSettings()).setRotation(rot).setMirror(Mirror.NONE)
					.setRotationPivot(BlockPos.ZERO).addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
		}

		@Override
		protected void handleDataMarker(String pMarker, BlockPos pPos, ServerLevelAccessor pLevel, Random pRandom,
				BoundingBox pBox) {

		}

	}

}