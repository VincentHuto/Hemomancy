package com.huto.hemomancy.init;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.huto.hemomancy.Hemomancy;
import com.huto.hemomancy.world.structure.BloodTempleStructure;
import com.huto.hemomancy.world.structure.RunDownHousePieces;
import com.huto.hemomancy.world.structure.RunDownHouseStructure;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.structure.IStructurePieceType;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class StructureInit {
	public static final DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister
			.create(ForgeRegistries.STRUCTURE_FEATURES, Hemomancy.MOD_ID);

	public static final RegistryObject<Structure<NoFeatureConfig>> RUN_DOWN_HOUSE = STRUCTURES
			.register("run_down_house", () -> (new RunDownHouseStructure(NoFeatureConfig.CODEC)));
	public static final RegistryObject<Structure<NoFeatureConfig>> blood_temple = STRUCTURES.register("blood_temple",
			() -> (new BloodTempleStructure(NoFeatureConfig.CODEC)));
	public static IStructurePieceType RDHP = RunDownHousePieces.Piece::new;
	public static IStructurePieceType BLOODTEMPLEPIECE = BloodTempleStructure.BloodTemplePiece.Piece::new;

	public static void registerStructures(Register<Structure<?>> event) {
		Structure.NAME_STRUCTURE_BIMAP.put(RUN_DOWN_HOUSE.get().getRegistryName().toString(), RUN_DOWN_HOUSE.get());
		Structure.NAME_STRUCTURE_BIMAP.put(blood_temple.get().getRegistryName().toString(), blood_temple.get());

		registerStructure(RUN_DOWN_HOUSE.get(), new StructureSeparationSettings(
				10 /*
					 * maximum distance apart in chunks between spawn attempts
					 */, 5 /*
							 * minimum distance apart in chunks between spawn attempts
							 */, 1234567890 /*
											 * this modifies the seed of the structure so no two structures always spawn
											 * over each-other. Make this large and unique.
											 */), true);

		registerStructure(blood_temple.get(), new StructureSeparationSettings(
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

	public static <F extends Structure<?>> void registerStructure(F structure,
			StructureSeparationSettings structureSeparationSettings, boolean transformSurroundingLand) {

		/*
		 * Will add land at the base of the structure like it does for Villages and
		 * Outposts. Doesn't work well on structure that have pieces stacked vertically.
		 */
		if (transformSurroundingLand) {
			Structure.field_236384_t_ = ImmutableList.<Structure<?>>builder().addAll(Structure.field_236384_t_)
					.add(structure).build();
		}

		/*
		 * doesn't always work for code made dimensions as they read from this list
		 * beforehand. Use the WorldEvent.Load event in StructureTutorialMain to add the
		 * structure spacing from this list into that dimension.
		 */
		DimensionStructuresSettings.field_236191_b_ = ImmutableMap.<Structure<?>, StructureSeparationSettings>builder()
				.putAll(DimensionStructuresSettings.field_236191_b_).put(structure, structureSeparationSettings)
				.build();
	}

	public static void registerAllPieces() {
		registerStructurePiece(RDHP, new ResourceLocation(Hemomancy.MOD_ID, "rdhp"));
		registerStructurePiece(BLOODTEMPLEPIECE, new ResourceLocation(Hemomancy.MOD_ID, "btp"));

	}

	static void registerStructurePiece(IStructurePieceType structurePiece, ResourceLocation rl) {
		Registry.register(Registry.STRUCTURE_PIECE, rl, structurePiece);
	}

}