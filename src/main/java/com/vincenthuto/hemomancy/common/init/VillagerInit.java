package com.vincenthuto.hemomancy.common.init;

import java.util.Arrays;
import java.util.Objects;

import com.google.common.collect.ImmutableSet;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.worldgen.village.HemopothecaryProcessor;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VillagerInit {

	public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister
			.create(ForgeRegistries.POI_TYPES, Hemomancy.MOD_ID);

	public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister
			.create(ForgeRegistries.VILLAGER_PROFESSIONS, Hemomancy.MOD_ID);

	public static final DeferredRegister<StructureProcessorType<?>> STRUCTURE_PROCESSORS = DeferredRegister
			.create(Registries.STRUCTURE_PROCESSOR, Hemomancy.MOD_ID);
	
    private static final ResourceKey<StructureProcessorList> HEMOPOTHECARY_PROCESSOR_LIST_KEY =
            ResourceKey.create(Registries.PROCESSOR_LIST, Hemomancy.rloc("hemopothecary_processors"));


	public static final RegistryObject<StructureProcessorType<HemopothecaryProcessor>> HEMOPOTHECARY_PROCESSOR = STRUCTURE_PROCESSORS
			.register("hemopothecary_processor", () -> HemopothecaryProcessor::codec);

	public static final RegistryObject<PoiType> TABLE_POI = POINTS_OF_INTEREST.register("hemopothecary",
			() -> new PoiType(
					ImmutableSet.copyOf(BlockInit.scrying_podium.get().getStateDefinition().getPossibleStates()), 1,
					1));

	public static final RegistryObject<VillagerProfession> HEMOPOTHECARY = PROFESSIONS.register("hemopothecary", () -> {
		var key = Objects.requireNonNull(TABLE_POI.getKey());
		return new VillagerProfession("hemopothecary", holder -> holder.is(key), holder -> holder.is(key),
				Arrays.stream(new Item[] { ItemInit.befouling_ash.get() }).collect(ImmutableSet.toImmutableSet()),
				ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LEATHERWORKER);
	});

}
