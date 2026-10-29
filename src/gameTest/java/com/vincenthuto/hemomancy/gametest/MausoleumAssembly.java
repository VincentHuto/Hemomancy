package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.worldgen.structure.MausoleumStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Dev-only: lays out a Saint's mausoleum as natural generation would on ground whose first free block is
 * {@code surface}, then (when asked) builds it. A built complex is registered with its chunks like a generated one, so
 * {@code MausoleumEvents} treats it as consecrated ground. Used by {@code /hemo test mausoleum}, the review client and
 * the assembly GameTests.
 */
public final class MausoleumAssembly {
	private static final Pattern TEMPLATE = Pattern.compile("hemomancy:mausoleum/[a-z]+/([a-z0-9_]+)");

	private MausoleumAssembly() {
	}

	/** One complex: its pieces in placement order, keyed by template name ({@code top}, {@code nave}, ...). */
	public record Layout(MausoleumStructure structure, ChunkPos origin, PiecesContainer pieces,
			Map<String, List<PoolElementStructurePiece>> byTemplate) {
		public Optional<PoolElementStructurePiece> first(String template) {
			return byTemplate.getOrDefault(template, List.of()).stream().findFirst();
		}
	}

	public static MausoleumStructure structure(ServerLevel level, EnumSaintType saint) {
		return (MausoleumStructure) level.registryAccess().registryOrThrow(Registries.STRUCTURE)
				.get(Hemomancy.rloc("mausoleum_" + saint.name().toLowerCase(Locale.ROOT)));
	}

	/** The pieces the jigsaw keeps for this seed; nothing is placed. Empty when the start piece itself fails. */
	public static Optional<Layout> layout(ServerLevel level, EnumSaintType saint, BlockPos surface, long seed) {
		MausoleumStructure structure = structure(level, saint);
		var generator = level.getChunkSource().getGenerator();
		ChunkPos origin = new ChunkPos(surface);
		var context = new Structure.GenerationContext(level.registryAccess(), generator, generator.getBiomeSource(),
				level.getChunkSource().randomState(), level.getStructureManager(), seed, origin, level, biome -> true);
		return structure.assembleOnSurface(context, surface).map(stub -> {
			PiecesContainer pieces = stub.getPiecesBuilder().build();
			Map<String, List<PoolElementStructurePiece>> byTemplate = new LinkedHashMap<>();
			for (StructurePiece piece : pieces.pieces()) {
				if (piece instanceof PoolElementStructurePiece pool) {
					byTemplate.computeIfAbsent(templateName(pool), name -> new ArrayList<>()).add(pool);
				}
			}
			return new Layout(structure, origin, pieces, byTemplate);
		});
	}

	/** Builds a laid-out complex into the level, loading its chunks, and registers it as a structure start. */
	public static void build(ServerLevel level, Layout layout) {
		StructureStart start = new StructureStart(layout.structure(), layout.origin(), 0, layout.pieces());
		BoundingBox box = start.getBoundingBox();
		ChunkPos min = new ChunkPos(SectionPos.blockToSectionCoord(box.minX()), SectionPos.blockToSectionCoord(box.minZ()));
		ChunkPos max = new ChunkPos(SectionPos.blockToSectionCoord(box.maxX()), SectionPos.blockToSectionCoord(box.maxZ()));
		level.getChunk(layout.origin().x, layout.origin().z).setStartForStructure(layout.structure(), start);
		long reference = layout.origin().toLong();
		var generator = level.getChunkSource().getGenerator();
		ChunkPos.rangeClosed(min, max).forEach(chunk -> {
			var access = level.getChunk(chunk.x, chunk.z);
			access.addReferenceForStructure(layout.structure(), reference);
			access.setUnsaved(true);
			start.placeInChunk(level, level.structureManager(), generator, level.getRandom(),
					new BoundingBox(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ(),
							chunk.getMaxBlockX(), level.getMaxBuildHeight(), chunk.getMaxBlockZ()), chunk);
		});
	}

	/** {@code nave} for a piece built from {@code hemomancy:mausoleum/<saint>/nave}. */
	public static String templateName(PoolElementStructurePiece piece) {
		var matcher = TEMPLATE.matcher(piece.getElement().toString());
		return matcher.find() ? matcher.group(1) : piece.getElement().toString();
	}

	/** A point given in a piece's template coordinates, in the world. */
	public static Vec3 toWorld(PoolElementStructurePiece piece, Vec3 local) {
		return StructureTemplate.transform(local, Mirror.NONE, piece.getRotation(), BlockPos.ZERO)
				.add(Vec3.atLowerCornerOf(piece.getPosition()));
	}
}
