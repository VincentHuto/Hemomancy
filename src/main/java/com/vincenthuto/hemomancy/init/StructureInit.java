//package com.vincenthuto.hemomancy.init;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import com.google.common.collect.ImmutableList;
//import com.google.common.collect.ImmutableMap;
//import com.vincenthuto.hemomancy.Hemomancy;
//import com.vincenthuto.hemomancy.world.structure.BloodTempleStructure;
//
//import net.minecraft.core.Registry;
//import net.minecraft.data.BuiltinRegistries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.levelgen.StructureSettings;
//import net.minecraft.world.level.levelgen.feature.StructureFeature;
//import net.minecraft.world.level.levelgen.feature.StructurePieceType;
//import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
//import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
//import net.minecraftforge.registries.DeferredRegister;
//import net.minecraftforge.registries.ForgeRegistries;
//import net.minecraftforge.registries.RegistryObject;
//
//public class StructureInit {
//
//	public static final DeferredRegister<StructureFeature<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister
//			.create(ForgeRegistries.STRUCTURE_FEATURES, Hemomancy.MOD_ID);
//	public static final RegistryObject<StructureFeature<NoneFeatureConfiguration>> blood_temple = DEFERRED_REGISTRY_STRUCTURE
//			.register("blood_temple", () -> (new BloodTempleStructure(NoneFeatureConfiguration.CODEC)));
//
//	public static StructurePieceType BLOODTEMPLEPIECE = BloodTempleStructure.BloodTemplePiece.Piece::new;
//
//	public static void setupStructures() {
//
//		setupMapSpacingAndLand(blood_temple.get(), new StructureFeatureConfiguration(10, 5, 1234567890), true);
//
//	}
//
//	public static <F extends StructureFeature<?>> void setupMapSpacingAndLand(F structure,
//			StructureFeatureConfiguration StructureFeatureConfiguration, boolean transformSurroundingLand) {
//		StructureFeature.STRUCTURES_REGISTRY.put(structure.getRegistryName().toString(), structure);
//
//		if (transformSurroundingLand) {
//			StructureFeature.NOISE_AFFECTING_FEATURES = ImmutableList.<StructureFeature<?>>builder()
//					.addAll(StructureFeature.NOISE_AFFECTING_FEATURES).add(structure).build();
//		}
//
//		StructureSettings.DEFAULTS = ImmutableMap.<StructureFeature<?>, StructureFeatureConfiguration>builder()
//				.putAll(StructureSettings.DEFAULTS).put(structure, StructureFeatureConfiguration).build();
//
//		BuiltinRegistries.NOISE_GENERATOR_SETTINGS.entrySet().forEach(settings -> {
//			Map<StructureFeature<?>, StructureFeatureConfiguration> structureMap = settings.getValue()
//					.structureSettings().structureConfig();
//
//			if (structureMap instanceof ImmutableMap) {
//				Map<StructureFeature<?>, StructureFeatureConfiguration> tempMap = new HashMap<>(structureMap);
//				tempMap.put(structure, StructureFeatureConfiguration);
//				settings.getValue().structureSettings().structureConfig = tempMap;
//			} else {
//				structureMap.put(structure, StructureFeatureConfiguration);
//			}
//		});
//		StructureInit.registerAllPieces();
//	}
//
//	public static void registerAllPieces() {
//		registerStructurePiece(BLOODTEMPLEPIECE, new ResourceLocation(Hemomancy.MOD_ID, "btp"));
//
//	}
//
//	static void registerStructurePiece(StructurePieceType structurePiece, ResourceLocation rl) {
//		Registry.register(Registry.STRUCTURE_PIECE, rl, structurePiece);
//	}
//}