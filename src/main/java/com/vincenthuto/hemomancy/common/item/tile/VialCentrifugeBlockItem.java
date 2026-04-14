package com.vincenthuto.hemomancy.common.item.tile;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.tile.crafting.VialCentrifugeItemRenderer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class VialCentrifugeBlockItem extends BlockItem {

	public VialCentrifugeBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new VialCentrifugeItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		});
	}
}
