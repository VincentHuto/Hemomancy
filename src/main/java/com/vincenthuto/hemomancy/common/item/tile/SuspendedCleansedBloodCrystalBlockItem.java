package com.vincenthuto.hemomancy.common.item.tile;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;


import com.vincenthuto.hemomancy.client.render.item.tile.SuspendedCleansedBloodCrystalItemRenderer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class SuspendedCleansedBloodCrystalBlockItem extends BlockItem implements HemoClientItemExtensionsProvider {

	public SuspendedCleansedBloodCrystalBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new SuspendedCleansedBloodCrystalItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}
}
