package com.vincenthuto.hemomancy.common.item.harbinger.tile;

import com.vincenthuto.hemomancy.client.item.HemoClientItemExtensionsProvider;
import com.vincenthuto.hemomancy.client.render.item.tile.functional.HematicStakeItemRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class HematicStakeBlockItem extends BlockItem implements HemoClientItemExtensionsProvider {
	public HematicStakeBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public IClientItemExtensions hemomancy$getClientItemExtensions() {
		return new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new HematicStakeItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		};
	}
}
