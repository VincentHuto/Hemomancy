package com.vincenthuto.hemomancy.common.succession;

import com.vincenthuto.hemomancy.common.block.harbinger.rite.BrazierBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

import java.util.ArrayList;
import java.util.List;

/**
 * The order a succession rite reads its offerings in, shared by the station matcher's result and the in-world role
 * labels: lit braziers holding an offering in the space above the floor, clockwise round the Focus from its left,
 * nearest, then lowest. "Left" is set by the effigy: the rite's structure puts it two blocks to the Focus's right.
 */
public final class SuccessionBrazierOrder {
	private SuccessionBrazierOrder() {
	}

	/** The floor orientation the rite reads in: the one with the effigy two blocks to the Focus's right, or null. */
	public static Direction forwards(BlockGetter level, BlockPos focus) {
		for (Direction right : Direction.Plane.HORIZONTAL) {
			if (level.getBlockState(focus.relative(right, 2).above(2)).is(BlockInit.vacant_effigy.get())) {
				return right.getCounterClockWise();
			}
		}
		return null;
	}

	/** Lit, offered braziers above the floor in the order the station matcher reports them. */
	public static List<BlockPos> roles(BlockGetter level, BlockPos focus, CardinalRiteFloorDefinition floor) {
		Direction forwards = forwards(level, focus);
		List<BlockPos> roles = new ArrayList<>();
		if (forwards == null) return roles;
		Direction right = forwards.getClockWise();
		for (BlockPos cell : floor.brazierSpace()) {
			BlockPos pos = focus.relative(right, cell.getX()).above(cell.getY()).relative(forwards, cell.getZ());
			if (BrazierBlock.isLit(level.getBlockState(pos))
					&& level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier
					&& !brazier.getOfferingForMatching().isEmpty()) {
				roles.add(pos.immutable());
			}
		}
		return roles;
	}
}
