package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.StructureInit;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.ConductionManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.Optional;

@GameTestHolder(Hemomancy.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CorticalDriftGameTests {
	private static final String EMPTY_TEMPLATE = "bastion/mobs/empty";

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
	public static void synapticNodeConductsDuctilis(GameTestHelper h) {
		BlockPos pos = h.absolutePos(new BlockPos(1, 2, 1));
		h.getLevel().setBlock(pos, BlockInit.synaptic_node.get().defaultBlockState(), 3);
		h.assertTrue(h.getLevel().getBlockState(pos).is(ConductionManager.CONDUCTORS),
				"Synaptic Node no longer matches the ductilis_conductors tag");
		h.succeed();
	}

	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
	public static void nerveFiberSegmentsAreAxisAligned(GameTestHelper h) {
		BlockState fiber = BlockInit.nerve_fiber.get().defaultBlockState();
		for (Direction.Axis axis : Direction.Axis.values()) {
			BlockPos pos = h.absolutePos(new BlockPos(1 + axis.ordinal(), 2, 1));
			h.getLevel().setBlock(pos, fiber.setValue(RotatedPillarBlock.AXIS, axis), 3);
			h.assertTrue(h.getLevel().getBlockState(pos).getValue(RotatedPillarBlock.AXIS) == axis,
					"Nerve Fiber lost its axis after placement: expected " + axis);
		}
		h.succeed();
	}

	/**
	 * The Vagrant Mind's envelope is roughly 256x256x256 blocks (see VagrantMindPiece.HALF_WIDTH/
	 * HALF_HEIGHT), far larger than a GameTest region can host, and its placement runs through
	 * StructurePiece.postProcess against a WorldGenLevel during chunk generation, not a plain
	 * ServerLevel a GameTestHelper exposes. Driving VagrantMindPiece directly here would not
	 * exercise anything real at this scale. The pure metaball geometry (VagrantMindGeometry.field/
	 * isShell) is already covered by VagrantMindGeometryTest (JVM unit test, multiple seeds, wider
	 * range checks), so this GameTest instead checks the things only a live server can see: that the
	 * structure type, the structure piece type, and the datapack structure itself actually made it
	 * into the running game's registries.
	 */
	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
	public static void vagrantMindIsRegistered(GameTestHelper h) {
		var registryAccess = h.getLevel().registryAccess();

		StructureType<?> structureType = StructureInit.vagrant_mind.get();
		Optional<ResourceKey<StructureType<?>>> structureTypeKey = registryAccess
				.registryOrThrow(Registries.STRUCTURE_TYPE).getResourceKey(structureType);
		h.assertTrue(structureTypeKey.isPresent() && structureTypeKey.get().location().equals(Hemomancy.rloc("vagrant_mind")),
				"vagrant_mind StructureType is not registered under hemomancy:vagrant_mind");

		StructurePieceType pieceType = StructureInit.vagrant_mind_piece.get();
		Optional<ResourceKey<StructurePieceType>> pieceTypeKey = registryAccess
				.registryOrThrow(Registries.STRUCTURE_PIECE).getResourceKey(pieceType);
		h.assertTrue(pieceTypeKey.isPresent() && pieceTypeKey.get().location().equals(Hemomancy.rloc("vagrant_mind")),
				"vagrant_mind StructurePieceType is not registered under Registries.STRUCTURE_PIECE");

		ResourceKey<Structure> structureKey = ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc("vagrant_mind"));
		Optional<Holder.Reference<Structure>> structure = registryAccess.lookupOrThrow(Registries.STRUCTURE)
				.get(structureKey);
		h.assertTrue(structure.isPresent(),
				"data/hemomancy/worldgen/structure/vagrant_mind.json did not load into Registries.STRUCTURE");

		h.succeed();
	}

	/**
	 * Samples the live End biome source over a wide grid and tallies what it returns. Guards against the
	 * biome silently never being selected (e.g. a TerraBlender category that never fires).
	 */
	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE)
	public static void corticalDriftAppearsInTheEnd(GameTestHelper h) {
		net.minecraft.server.level.ServerLevel end = h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.END);
		h.assertTrue(end != null, "The End is not loaded on the GameTest server");
		net.minecraft.world.level.biome.BiomeSource source = end.getChunkSource().getGenerator().getBiomeSource();
		net.minecraft.world.level.biome.Climate.Sampler sampler = end.getChunkSource().randomState().sampler();
		java.util.Map<String, Integer> tally = new java.util.TreeMap<>();
		double minErosion = Double.MAX_VALUE, maxErosion = -Double.MAX_VALUE;
		for (int x = -12800; x <= 12800; x += 96) {
			for (int z = -12800; z <= 12800; z += 96) {
				Holder<net.minecraft.world.level.biome.Biome> biome = source.getNoiseBiome(
						net.minecraft.core.QuartPos.fromBlock(x), net.minecraft.core.QuartPos.fromBlock(64),
						net.minecraft.core.QuartPos.fromBlock(z), sampler);
				String key = biome.unwrapKey().map(k -> k.location().toString()).orElse("?");
				tally.merge(key, 1, Integer::sum);
				double e = sampler.erosion().compute(new net.minecraft.world.level.levelgen.DensityFunction.SinglePointContext(x, 64, z));
				minErosion = Math.min(minErosion, e);
				maxErosion = Math.max(maxErosion, e);
			}
		}
		System.out.println("[CorticalDriftDiag] biome tally=" + tally + " erosion range=[" + minErosion + ", " + maxErosion + "] source=" + source.getClass().getName());
		h.assertTrue(tally.getOrDefault("hemomancy:cortical_drift", 0) > 0,
				"hemomancy:cortical_drift never selected in the End. tally=" + tally
						+ " erosion=[" + minErosion + ", " + maxErosion + "]");
		h.succeed();
	}

	/** GameTest worlds disable structure generation, so validate the candidate and its start directly. */
	@GameTest(templateNamespace = "minecraft", template = EMPTY_TEMPLATE, timeoutTicks = 80)
	public static void vagrantMindCanBuildEndGenerationStart(GameTestHelper h) {
		net.minecraft.server.level.ServerLevel end = h.getLevel().getServer().getLevel(net.minecraft.world.level.Level.END);
		h.assertTrue(end != null, "The End is not loaded on the GameTest server");
		var generator = end.getChunkSource().getGenerator();
		var registries = end.registryAccess();
		Holder<Structure> mind = registries.registryOrThrow(Registries.STRUCTURE)
				.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE, Hemomancy.rloc("vagrant_mind")));
		boolean biomePossible = generator.getBiomeSource().possibleBiomes().stream()
				.anyMatch(b -> b.is(com.vincenthuto.hemomancy.common.init.BiomeInit.CORTICAL_DRIFT));
		boolean setPossible = end.getChunkSource().getGeneratorState().possibleStructureSets().stream()
				.anyMatch(set -> set.is(ResourceKey.create(Registries.STRUCTURE_SET, Hemomancy.rloc("vagrant_mind"))));
		h.assertTrue(biomePossible && setPossible,
				"The End must include Cortical Drift and the Vagrant Mind structure set");
		var set = registries.registryOrThrow(Registries.STRUCTURE_SET)
				.getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET, Hemomancy.rloc("vagrant_mind")));
		var placement = (net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement) set.placement();
		net.minecraft.world.level.ChunkPos probe = null;
		for (int cx = -500; cx <= 500 && probe == null; cx += placement.spacing()) {
			for (int cz = -500; cz <= 500 && probe == null; cz += placement.spacing()) {
				net.minecraft.world.level.ChunkPos c = placement.getPotentialStructureChunk(end.getSeed(), cx, cz);
				var context = new Structure.GenerationContext(registries, generator, generator.getBiomeSource(),
						end.getChunkSource().randomState(), end.getStructureManager(), end.getSeed(), c, end,
						mind.value().biomes()::contains);
				if (placement.isStructureChunk(end.getChunkSource().getGeneratorState(), c.x, c.z)
						&& mind.value().findValidGenerationPoint(context).isPresent()) {
					probe = c;
				}
			}
		}
		h.assertTrue(probe != null, "No End placement candidate accepts the Vagrant Mind biome tag");
		var generated = mind.value().generate(registries, generator, generator.getBiomeSource(),
				end.getChunkSource().randomState(), end.getStructureManager(), end.getSeed(), probe, 0, end,
				mind.value().biomes()::contains);
		h.assertTrue(generated.isValid() && generated.getPieces().size() == 1,
				"The Vagrant Mind must build one valid piece at an End placement candidate");
		h.succeed();
	}
}
