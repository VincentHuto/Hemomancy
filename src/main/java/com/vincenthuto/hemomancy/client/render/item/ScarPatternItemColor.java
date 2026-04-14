package com.vincenthuto.hemomancy.client.render.item;

import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * ItemColor handler for Scar Pattern items.
 * Layer 0 (background) renders normally.
 * Layer 1 (scar overlay) pulses/fades in and out over time.
 */
public class ScarPatternItemColor implements ItemColor {

	@Override
	public int getColor(@Nonnull ItemStack stack, int tintIndex) {
		// Both layers return full white - the custom BEWLR handles the
		// translucent overlay blending and pulsing effect directly.
		return 0xFFFFFFFF;
	}
}

