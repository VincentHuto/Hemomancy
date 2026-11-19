package com.vincenthuto.hemomancy.common.encounter.ordeal;

import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * A professor's stage in the Chamber of Will: their template (written by {@code tools/worldgen/circus/ordeal_stages.py})
 * laid centred on the stage origin, the ring's floor on the origin's row. Built fresh for each ordeal and cleared after,
 * in the stage's own column of the Chamber dimension, never near a room.
 */
public final class OrdealStage {
	/** The ring a caster is held to. */
	public static final double RING_RADIUS = 9.5D;
	/** The stage's footprint and height round its origin when no template is found (and what a clear removes). */
	private static final int HALF = 20, BELOW = 2, ABOVE = 24;

	private OrdealStage() {
	}

	public static void build(ServerLevel chamber, OrdealProfessor professor, BlockPos origin) {
		clear(chamber, origin);
		StructureTemplate template = chamber.getStructureManager().get(Hemomancy.rloc(professor.stage())).orElse(null);
		if (template == null) {
			fallback(chamber, origin);
			return;
		}
		Vec3i size = template.getSize();
		BlockPos corner = origin.offset(-size.getX() / 2, -1, -size.getZ() / 2);
		template.placeInWorld(chamber, corner, corner, new StructurePlaceSettings(), chamber.getRandom(), Block.UPDATE_CLIENTS);
	}

	/** Clears the stage's volume back to air. */
	public static void clear(ServerLevel chamber, BlockPos origin) {
		for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-HALF, -BELOW, -HALF), origin.offset(HALF, ABOVE, HALF)))
			if (!chamber.getBlockState(pos).isAir()) chamber.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
	}

	/** A plain ring floor when the professor's template is missing: the ordeal still runs. */
	private static void fallback(ServerLevel chamber, BlockPos origin) {
		int r = (int) Math.ceil(RING_RADIUS) + 1;
		for (int dx = -r; dx <= r; dx++)
			for (int dz = -r; dz <= r; dz++)
				if (dx * dx + dz * dz <= r * r)
					chamber.setBlock(origin.offset(dx, 0, dz), Blocks.POLISHED_BLACKSTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
	}
}
