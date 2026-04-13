package com.vincenthuto.hemomancy.common.item;

import java.util.function.Consumer;

import com.vincenthuto.hemomancy.client.render.item.QliphothPomeItemRenderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Edible reagent fruit dropped by Qliphoth Bloom trees. Renders as a dark
 * glowing orb matching the void-like spheres embedded in the tree's trunk.
 */
public class QliphothPomeItem extends Item {

	public QliphothPomeItem(Properties properties) {
		super(properties);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return new QliphothPomeItemRenderer(
						Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels());
			}
		});
	}
}
