package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.worldgen.structure.HemoJigsawStructure;
import com.vincenthuto.hemomancy.common.worldgen.structure.StructurePlacementChecks;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Every Crimson Troupe site: the pavilion, the five teaching camps and the abandoned stage (chunk 15 T10, D-43). A site
 * keeps {@link #VILLAGE_CHUNKS} chunks from any village's start, stands in its biome, and passes the sampled troupe
 * gate over its laid-out footprint. Camps and stages also keep the exclusion zone from pavilions their structure sets
 * name.
 */
public final class CircusPavilionStructure extends HemoJigsawStructure {
	/** D-43: how many chunks a troupe site keeps from a village's start chunk. */
	public static final int VILLAGE_CHUNKS = 6;
	public static final MapCodec<CircusPavilionStructure> CODEC = codec(
			com.mojang.serialization.Codec.INT.optionalFieldOf("gate_core", 0), s -> s.gateCore, CircusPavilionStructure::new);
	/**
	 * The side of the centred square the terrain gate samples, 0 for the whole site: the pavilion's big top is 43 blocks
	 * across inside an 81 x 65 template whose side rings and yards sit on whatever ground is there (2026-11-16; a square,
	 * so it is the same core whichever way the template is turned).
	 */
	private final int gateCore;

	public CircusPavilionStructure(Structure.StructureSettings settings, Jigsaw jigsaw, int gateCore) {
		super(settings, jigsaw);
		this.gateCore = gateCore;
	}

	/** The part of a laid-out site the terrain gate samples. */
	public net.minecraft.world.level.levelgen.structure.BoundingBox gateBox(
			net.minecraft.world.level.levelgen.structure.BoundingBox box) {
		return TroupeFootprintScan.gateCore(box, gateCore);
	}


	@Override
	protected boolean canGenerateAt(GenerationContext context) {
		return StructurePlacementChecks.canPlaceOverworldHemomancyStructure(context)
				&& !StructurePlacementChecks.nearVillage(context, context.chunkPos(), VILLAGE_CHUNKS);
	}

	/** The usual start, then the biome and troupe-footprint checks on the assembled pieces. */
	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		var generated = super.findGenerationPoint(context);
        if (generated.isEmpty()) return Optional.empty();
        // Vanilla rejects this same stub after findGenerationPoint returns. Check before expensive terrain scans.
        var position = generated.get().position();
        if (!context.validBiome().test(context.chunkGenerator().getBiomeSource().getNoiseBiome(
                QuartPos.fromBlock(position.getX()), QuartPos.fromBlock(position.getY()),
                QuartPos.fromBlock(position.getZ()), context.randomState().sampler()))) return Optional.empty();
        var pieces = generated.get().getPiecesBuilder();
        if (!StructurePlacementChecks.isSuitableTroupeFootprint(context, gateBox(pieces.getBoundingBox()))) return Optional.empty();
        return Optional.of(new GenerationStub(generated.get().position(), com.mojang.datafixers.util.Either.right(pieces)));
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.circus_pavilion.get();
	}
}
