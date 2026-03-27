package com.vincenthuto.hemomancy.common.item.block;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.FungalImplantationPylonItemRenderer;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class FungalImplantationPylonBlockItem extends BlockItem {

	public FungalImplantationPylonBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private final BlockEntityWithoutLevelRenderer renderer = new FungalImplantationPylonItemRenderer(null, null);

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return renderer;
			}
		});
	}
}

