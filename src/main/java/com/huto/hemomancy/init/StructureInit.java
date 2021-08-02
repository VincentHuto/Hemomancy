package com.huto.hemomancy.init;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.world.structure.BloodTempleStructure;
import com.huto.hemomancy.world.structure.RunDownHousePieces;
import com.huto.hemomancy.world.structure.RunDownHouseStructure;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureInit {
	public static final DeferredRegister<StructureFeature<?>> STRUCTURES = DeferredRegister
			.create(ForgeRegistries.STRUCTURE_FEATURES, Hemomancy.MOD_ID);

	public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> RUN_DOWN_HOUSE = STRUCTURES
			.register("run_down_house", () -> (new RunDownHouseStructure(NoneFeatureConfiguration.CODEC)));
	public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> blood_temple = STRUCTURES
			.register("blood_temple", () -> (new BloodTempleStructure(NoneFeatureConfiguration.CODEC)));
	public static StructurePieceType RDHP = RunDownHousePieces.Piece::new;
	public static StructurePieceType BLOODTEMPLEPIECE = BloodTempleStructure.BloodTemplePiece.Piece::new;

	public static void registerStructures(Register<StructureFeature<?>> event) {
		StructureFeature.STRUCTURES_REGISTRY.put(RUN_DOWN_HOUSE.get().getRegistryName().toString(),
				RUN_DOWN_HOUSE.get());
		StructureFeature.STRUCTURES_REGISTRY.put(blood_temple.get().getRegistryName().toString(), blood_temple.get());

		registerStructure(RUN_DOWN_HOUSE.get(), new StructureFeatureConfiguration(
				10 /*
					 * maximum distance apart in chunks between spawn attempts
					 */, 5 /*
							 * minimum distance apart in chunks between spawn attempts
							 */, 1234567890 /*
											 * this modifies the seed of the structure so no two structures always spawn
											 * over each-other. Make this large and unique.
											 */), true);

		registerStructure(blood_temple.get(), new StructureFeatureConfiguration(
				10 /*
					 * maximum distance apart in chunks between spawn attempts
					 */, 5 /*
							 * minimum distance apart in chunks between spawn attempts
							 */, 1234537890 /*
											 * this modifies the seed of the structure so no two structures always spawn
											 * over each-other. Make this large and unique.
											 */), true);

		StructureInit.registerAllPieces();
	}

	public static <F extends StructureFeature<?>> void registerStructure(F structure,
			StructureFeatureConfiguration structureSeparationSettings, boolean transformSurroundingLand) {

		/*
		 * Will add land at the base of the structure like it does for Villages and
		 * Outposts. Doesn't work well on structure that have pieces stacked vertically.
		 */
		if (transformSurroundingLand) {
			StructureFeature.NOISE_AFFECTING_FEATURES = ImmutableList.<StructureFeature<?>>builder()
					.addAll(StructureFeature.NOISE_AFFECTING_FEATURES).add(structure).build();
		}

		/*
		 * doesn't always work for code made dimensions as they read from this list
		 * beforehand. Use the LevelEvent.Load event in StructureTutorialMain to add the
		 * structure spacing from this list into that dimension.
		 */
		StructureSettings.DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder()
				.putAll(StructureSettings.DEFAULTS).put(structure, structureSeparationSettings).build();
	}

	public static void registerAllPieces() {
		registerStructurePiece(RDHP, new ResourceLocation(Hemomancy.MOD_ID, "rdhp"));
		registerStructurePiece(BLOODTEMPLEPIECE, new ResourceLocation(Hemomancy.MOD_ID, "btp"));

	}

	static void registerStructurePiece(StructurePieceType structurePiece, ResourceLocation rl) {
		Registry.register(Registry.STRUCTURE_PIECE, rl, structurePiece);
	}

}