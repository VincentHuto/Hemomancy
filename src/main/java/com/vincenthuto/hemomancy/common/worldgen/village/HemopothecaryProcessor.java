package com.vincenthuto.hemomancy.common.worldgen.village;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.VillagerInit;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public class HemopothecaryProcessor extends StructureProcessor {
	public static final Codec<HemopothecaryProcessor> CODEC = Codec.EMPTY
			.xmap(u -> new HemopothecaryProcessor(), p -> Unit.INSTANCE).codec();

	public static Codec<HemopothecaryProcessor> codec() {
		return CODEC;
	}

	@Override
	protected StructureProcessorType<?> getType() {
		return VillagerInit.HEMOPOTHECARY_PROCESSOR.get();
	}

}
