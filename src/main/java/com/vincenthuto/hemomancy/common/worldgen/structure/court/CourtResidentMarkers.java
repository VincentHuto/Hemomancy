package com.vincenthuto.hemomancy.common.worldgen.structure.court;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads the resident and set-piece marks a Court structure's templates carry, for Track 9's spawner and Track 10's
 * presences. In the templates they are jigsaw blocks with pool {@code minecraft:empty} named
 * {@code hemomancy:court_resident/<role>} or {@code hemomancy:court_mark/<what>}; placement turns each into air or
 * water, so this reads them from the pieces, not the world.
 */
public final class CourtResidentMarkers {
	public static final String RESIDENT = "hemomancy:court_resident/";
	public static final String MARK = "hemomancy:court_mark/";

	/** A mark: where it is, which kind, its name after the kind prefix, and the way it faces (the jigsaw's top, rotated). */
	public record Marker(BlockPos pos, String kind, String name, Direction facing) {
		public boolean resident() {
			return kind.equals(RESIDENT);
		}
	}

	private CourtResidentMarkers() {
	}

	public static List<Marker> read(StructureTemplateManager templates, List<StructurePiece> pieces) {
		var result = new ArrayList<Marker>();
		for (var piece : pieces) {
			if (!(piece instanceof PoolElementStructurePiece pooled)) continue;
			for (var info : pooled.getElement().getShuffledJigsawBlocks(templates, pooled.getPosition(),
					pooled.getRotation(), RandomSource.create(0))) {
				if (info.nbt() == null) continue;
				String name = info.nbt().getString("name");
				for (String kind : List.of(RESIDENT, MARK)) {
					if (name.startsWith(kind)) result.add(new Marker(info.pos(), kind, name.substring(kind.length()),
							JigsawBlock.getTopFacing(info.state())));
				}
			}
		}
		return result;
	}
}
