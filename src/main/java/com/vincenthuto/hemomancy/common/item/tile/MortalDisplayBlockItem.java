package com.vincenthuto.hemomancy.common.item.tile;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;


import com.vincenthuto.hemomancy.client.render.item.tile.functional.MortalDisplayItemRenderer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class MortalDisplayBlockItem extends BlockItem implements HemoClientItemExtensionsProvider {

	public MortalDisplayBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new MortalDisplayItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}
}
