package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class HermitageRemnantStructure extends HemoJigsawStructure {
	public static final MapCodec<HermitageRemnantStructure> CODEC = codec(HermitageRemnantStructure::new);

	public HermitageRemnantStructure(Structure.StructureSettings config, Jigsaw jigsaw) {
		super(config, jigsaw);
	}

	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return StructurePlacementChecks.canPlaceOverworldHemomancyStructure(context);
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.hermitage_remnant.get();
	}
}
