//package com.vincenthuto.hemomancy.init;
//
//import com.vincenthuto.hemomancy.Hemomancy;
//
//import net.minecraft.core.Registry;
//import net.minecraft.data.BuiltinRegistries;
//import net.minecraft.resources.ResourceLocation;
//import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
//import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
//
//public class ConfiguredStructInit {
//
//	public static ConfiguredStructureFeature<?, ?> configured_blood_temple = StructureInit.blood_temple.get()
//			.configured(NoneFeatureConfiguration.INSTANCE);
//
//	public static void registerConfiguredStructures() {
//		Registry<ConfiguredStructureFeature<?, ?>> registry = BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE;
//		Registry.register(registry, new ResourceLocation(Hemomancy.MOD_ID, "configured_blood_temple"),
//				configured_blood_temple);
//	}
//}