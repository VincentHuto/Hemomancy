package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import java.util.List;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusSiteStateService;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.Vec3;

/**
 * The Ringmaster's stage in his Chamber (2026-11-18): a copy of the real pavilion, laid with the real site's rotation
 * so its carousel cell stands on the stage origin. Because both share a rotation, any point maps from the real tent to
 * the copy by the same offset from the carousel, which is what keeps the crossing unseen.
 */
public final class FinaleStage {
	private static final String TEMPLATE = "crimson_troupe_pavilion";
	/** The carousel's cell in the pavilion template: where the stage origin stands. */
	public static final BlockPos CAROUSEL_CELL = CircusSiteStateService.CAROUSEL;
	/** The pavilion template's size; what a clear removes, turned with the stage. */
	private static final BlockPos SIZE = new BlockPos(81, 23, 65);
	/**
	 * Where he blinks to in the rafters, in template cells (mirrors {@code RAFTER_PERCHES} in
	 * {@code tools/worldgen/circus/pavilion.py}): the two rigging-tower platforms, then the four quarter poles' collars.
	 */
	public static final List<BlockPos> HIGH_PERCHES = List.of(new BlockPos(24, 10, 32), new BlockPos(56, 10, 32),
			new BlockPos(32, 11, 24), new BlockPos(48, 11, 24), new BlockPos(32, 11, 40), new BlockPos(48, 11, 40));
	/** Where he drops to after a volley, on the ring curb, within a melee reach of the ring's floor. */
	public static final List<BlockPos> LOW_PERCHES = List.of(new BlockPos(33, 2, 32), new BlockPos(47, 2, 32),
			new BlockPos(40, 2, 25), new BlockPos(40, 2, 39));

	private FinaleStage() {
	}

	/** How far the copy reaches from its carousel cell across the ground, whatever its rotation. */
	public static int reach() {
		return Math.max(Math.max(CAROUSEL_CELL.getX(), SIZE.getX() - CAROUSEL_CELL.getX()),
				Math.max(CAROUSEL_CELL.getZ(), SIZE.getZ() - CAROUSEL_CELL.getZ()));
	}

	/** How far the copy reaches below and above its carousel cell. */
	public static int depth() {
		return CAROUSEL_CELL.getY() + 1;
	}

	public static int height() {
		return SIZE.getY() - CAROUSEL_CELL.getY();
	}

	/** The player's own stage origin, on the ordeals' grid in the Chamber. */
	public static BlockPos origin(int chamberId) {
		return OrdealRules.stageOrigin(chamberId);
	}

	/** Lays the pavilion copy so its carousel cell stands on {@code origin}, turned as the real site is. */
	public static void build(ServerLevel chamber, BlockPos origin, Rotation rotation) {
		clear(chamber, origin, rotation);
		StructureTemplate template = chamber.getStructureManager().get(Hemomancy.rloc(TEMPLATE)).orElse(null);
		if (template == null) return;
		BlockPos corner = origin.subtract(CAROUSEL_CELL.rotate(rotation));
		// Blocks only: the template's own carousel and captives would stand a second Procession inside the copy's.
		template.placeInWorld(chamber, corner, corner, new StructurePlaceSettings().setRotation(rotation).setIgnoreEntities(true),
				chamber.getRandom(), Block.UPDATE_CLIENTS);
	}

	/** Clears the copy's whole turned volume back to air. */
	public static void clear(ServerLevel chamber, BlockPos origin, Rotation rotation) {
		BlockPos a = origin.offset(BlockPos.ZERO.subtract(CAROUSEL_CELL).rotate(rotation));
		BlockPos b = origin.offset(SIZE.subtract(CAROUSEL_CELL).rotate(rotation));
		for (BlockPos pos : BlockPos.betweenClosed(Math.min(a.getX(), b.getX()), a.getY() - 1, Math.min(a.getZ(), b.getZ()),
				Math.max(a.getX(), b.getX()), b.getY(), Math.max(a.getZ(), b.getZ())))
			if (!chamber.getBlockState(pos).isAir()) chamber.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
	}

	/** Where a point in the real tent lies in the copy: the same offset from the carousel, as both share a rotation. */
	public static Vec3 toCopy(Vec3 real, BlockPos realOrigin, BlockPos copyOrigin, Rotation rotation) {
		return real.subtract(Vec3.atLowerCornerOf(realOrigin)).add(Vec3.atLowerCornerOf(copyOrigin));
	}

	/** The facing carries over unchanged: the copy is turned as the real tent is. */
	public static float yawToCopy(float yaw) {
		return yaw;
	}

	/** A perch's standing point in the copy. */
	public static Vec3 perch(BlockPos copyOrigin, Rotation rotation, BlockPos cell) {
		return Vec3.atBottomCenterOf(copyOrigin.offset(cell.subtract(CAROUSEL_CELL).rotate(rotation)));
	}
}
