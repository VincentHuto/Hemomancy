package com.vincenthuto.hemomancy.common.item.tile;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;


import com.vincenthuto.hemomancy.client.render.item.tile.crafting.ScarStationItemRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ScarStationBlockItem extends BlockItem implements HemoClientItemExtensionsProvider {

	public ScarStationBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new ScarStationItemRenderer(
					Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		};
	}
}
