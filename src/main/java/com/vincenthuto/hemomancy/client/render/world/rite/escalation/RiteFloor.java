package com.vincenthuto.hemomancy.client.render.world.rite.escalation;

import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorGeometry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.CardinalFocusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** The consecrated floor under a rite's Focus, as the world layers need it: its colours and its size. */
public record RiteFloor(CardinalRiteFloorVisualRules.Palette palette, boolean formed, int half) {
	/** No formed floor: the default Harbinger palette, and no trench or creep to draw. */
	public static final RiteFloor NONE = new RiteFloor(
			CardinalRiteFloorVisualRules.palette(CardinalRiteFloorVisualRules.STYLE_THRESHOLD), false, 0);

	public static RiteFloor at(Level level, BlockPos focus) {
		if (level == null || !(level.getBlockEntity(focus) instanceof CardinalFocusBlockEntity entity)
				|| entity.getFormedFloor() == null) {
			return NONE;
		}
		return CardinalRiteFloorRegistry.get(entity.getFormedFloor())
				.map(floor -> new RiteFloor(CardinalRiteFloorVisualRules.palette(
						CardinalRiteFloorVisualRules.styleIndex(floor.style())), true,
						CardinalRiteFloorGeometry.halfWidth(floor)))
				.orElse(NONE);
	}

	/** The floor's edge, in blocks from the Focus centre, where the creep begins. */
	public float edge() {
		return half + 0.5F;
	}
}
