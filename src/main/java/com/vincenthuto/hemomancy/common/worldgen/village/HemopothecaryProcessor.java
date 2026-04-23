package com.vincenthuto.hemomancy.common.worldgen.village;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.VillagerInit;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class HemopothecaryProcessor extends StructureProcessor {
	public static final MapCodec<HemopothecaryProcessor> CODEC = MapCodec.unit(HemopothecaryProcessor::new);

	public static MapCodec<HemopothecaryProcessor> codec() {
		return CODEC;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return VillagerInit.HEMOPOTHECARY_PROCESSOR.get();
	}

}
