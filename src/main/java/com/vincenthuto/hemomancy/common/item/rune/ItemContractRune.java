package com.vincenthuto.hemomancy.common.item.rune;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

/**
 * @deprecated Renamed to {@link ItemFungalRune}. Kept as an alias for backwards compatibility.
 */
@Deprecated(forRemoval = false)
public class ItemContractRune extends ItemFungalRune {

	public ItemContractRune(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}
}
