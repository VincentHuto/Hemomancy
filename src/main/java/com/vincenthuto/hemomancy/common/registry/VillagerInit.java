package com.vincenthuto.hemomancy.common.registry;

import java.util.Arrays;
import java.util.Objects;

import com.google.common.collect.ImmutableSet;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class VillagerInit {

	public static final DeferredRegister<PoiType> POINTS_OF_INTEREST = DeferredRegister
			.create(ForgeRegistries.POI_TYPES, Hemomancy.MOD_ID);

	public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister
			.create(ForgeRegistries.VILLAGER_PROFESSIONS, Hemomancy.MOD_ID);

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
