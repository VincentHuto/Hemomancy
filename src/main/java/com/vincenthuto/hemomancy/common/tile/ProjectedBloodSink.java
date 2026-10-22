package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodAccounting;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodFlowContribution.Category;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * A block entity that takes blood projected at it by Blood Projection or a Living Staff. The sink charges the player
 * through {@link BloodAccounting} and returns the blood it took. {@link IBloodReservoir} supplies the default.
 */
public interface ProjectedBloodSink {
	BloodAccounting.Source PROJECTION = BloodAccounting.Source.of("blood_projection", Category.STATION, false, false);

	/** {@code hitPos} is the block the player aimed at, which may be another part of a multiblock. */
	double takeProjectedBlood(Player player, BlockPos hitPos, double rate, boolean livingStaff);
}
