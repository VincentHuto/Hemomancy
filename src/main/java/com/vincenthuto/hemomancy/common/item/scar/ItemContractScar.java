package com.vincenthuto.hemomancy.common.item.scar;

import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;

/**
 * @deprecated Renamed to {@link ItemFungalScar}. Kept as an alias for backwards compatibility.
 */
@Deprecated(forRemoval = false)
public class ItemContractScar extends ItemFungalScar {

	public ItemContractScar(Properties properties, EnumBloodTendency tendencyIn, float deepenAmountIn) {
		super(properties, tendencyIn, deepenAmountIn);
	}
}
