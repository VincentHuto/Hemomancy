package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * The Vagrant Mind: one colossal hollow brain per biome patch. Unlike the surface structures in
 * this package it does not project onto a heightmap — End islands float, so the Mind is placed at
 * an absolute y-band and simply hangs there.
 */
public class VagrantMindStructure extends Structure {
	public static final MapCodec<VagrantMindStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
			instance.group(
					settingsCodec(instance),
					HeightProvider.CODEC.fieldOf("start_height").forGetter(s -> s.startHeight))
			.apply(instance, VagrantMindStructure::new));

	private final HeightProvider startHeight;

	public VagrantMindStructure(StructureSettings settings, HeightProvider startHeight) {
		super(settings);
		this.startHeight = startHeight;
	}

	@Override
	public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		int centreX = chunkPos.getMiddleBlockX();
		int centreZ = chunkPos.getMiddleBlockZ();
		int y = this.startHeight.sample(context.random(),
				new WorldGenerationContext(context.chunkGenerator(), context.heightAccessor()));
		BlockPos origin = new BlockPos(centreX, y, centreZ);
		long seed = context.random().nextLong();
		return Optional.of(new GenerationStub(origin,
				builder -> builder.addPiece(new VagrantMindPiece(origin, seed))));
	}

	@Override
	public StructureType<?> type() {
		return StructureInit.vagrant_mind.get();
	}
}
