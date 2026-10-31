package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import net.minecraft.world.entity.player.Player;

/**
 * Where a strain is at home: its house's habitat, in which marked powers grow stronger or only work. Each table row
 * names one from {@link MorphlingHomeConditions}; {@link #UNSET} is never at home.
 */
@FunctionalInterface
public interface MorphlingHomeCondition {
	MorphlingHomeCondition UNSET = player -> false;

	boolean isHome(Player player);
}
