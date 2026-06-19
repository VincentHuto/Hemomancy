package com.vincenthuto.hemomancy.common.init;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.structure.ActiveHarbingerVoyagerVesselStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.BloodTempleStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.BogBodyOssuaryNicheStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.BrokenChurchStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.CrimsonLodgeAnnexStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.HarbingerOutpostStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.HarbingerVoyagerWreckStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.HermitageRemnantStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.MasonsRespiteStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.MausoleumStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.SanguineSurveyorBivouacStructure;
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

	public static final DeferredHolder<StructureType<?>, StructureType<BrokenChurchStructure>> broken_church = STRUCTURES
			.register("broken_church", () -> explicitStructureTypeTyping(BrokenChurchStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<HarbingerOutpostStructure>> harbinger_outpost = STRUCTURES
			.register("harbinger_outpost", () -> explicitStructureTypeTyping(HarbingerOutpostStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<HarbingerVoyagerWreckStructure>> harbinger_voyager_wreck = STRUCTURES
			.register("harbinger_voyager_wreck", () -> explicitStructureTypeTyping(HarbingerVoyagerWreckStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<ActiveHarbingerVoyagerVesselStructure>> harbinger_voyager_vessel = STRUCTURES
			.register("harbinger_voyager_vessel",
					() -> explicitStructureTypeTyping(ActiveHarbingerVoyagerVesselStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<MausoleumStructure>> mausoleum = STRUCTURES
			.register("mausoleum", () -> explicitStructureTypeTyping(MausoleumStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<SanguineSurveyorBivouacStructure>> sanguine_surveyor_bivouac = STRUCTURES
			.register("sanguine_surveyor_bivouac", () -> explicitStructureTypeTyping(SanguineSurveyorBivouacStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<HermitageRemnantStructure>> hermitage_remnant = STRUCTURES
			.register("hermitage_remnant", () -> explicitStructureTypeTyping(HermitageRemnantStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<MasonsRespiteStructure>> masons_respite = STRUCTURES
			.register("masons_respite", () -> explicitStructureTypeTyping(MasonsRespiteStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<BogBodyOssuaryNicheStructure>> bog_body_ossuary_niche = STRUCTURES
			.register("bog_body_ossuary_niche", () -> explicitStructureTypeTyping(BogBodyOssuaryNicheStructure.CODEC));

	public static final DeferredHolder<StructureType<?>, StructureType<CrimsonLodgeAnnexStructure>> crimson_lodge_annex = STRUCTURES
			.register("crimson_lodge_annex", () -> explicitStructureTypeTyping(CrimsonLodgeAnnexStructure.CODEC));

	private static <T extends Structure> StructureType<T> explicitStructureTypeTyping(MapCodec<T> structureCodec) {
		return () -> structureCodec;
	}

}
