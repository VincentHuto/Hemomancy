package com.vincenthuto.hemomancy.common.init;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.structure.BloodTempleStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.HarbingerOutpostStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.MausoleumStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.UnstainedChurchStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class StructureInit {
	/**
	 * We are using the Deferred Registry system to register our structure as this
	 * is the preferred way on Forge. This will handle registering the base
	 * structure for us at the correct time so we don't have to handle it ourselves.
	 */
	public static final DeferredRegister<StructureType<?>> STRUCTURES = DeferredRegister
			.create(Registries.STRUCTURE_TYPE, Hemomancy.MOD_ID);

	public static DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister
			.create(NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS, Hemomancy.MOD_ID);

	public static final DeferredHolder<StructureType<?>, StructureType<BloodTempleStructure>> blood_temple = STRUCTURES
			.register("blood_temple", () -> explicitStructureTypeTyping(BloodTempleStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<UnstainedChurchStructure>> unstained_church = STRUCTURES
			.register("unstained_church", () -> explicitStructureTypeTyping(UnstainedChurchStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<HarbingerOutpostStructure>> harbinger_outpost = STRUCTURES
			.register("harbinger_outpost", () -> explicitStructureTypeTyping(HarbingerOutpostStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<MausoleumStructure>> mausoleum = STRUCTURES
			.register("mausoleum", () -> explicitStructureTypeTyping(MausoleumStructure.CODEC));

	private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(MapCodec<T> structureCodec) {
		return () -> structureCodec;
	}

}
