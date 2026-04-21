package com.vincenthuto.hemomancy.compat.mna.item;

import java.util.function.Consumer;

import com.mna.items.renderers.books.SpellBookRenderer;
import com.mna.items.sorcery.Grimoire;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.common.util.NonNullLazy;

public class HarbingerGrimoireItem extends Grimoire {

	public HarbingerGrimoireItem(Properties properties, ResourceLocation factionHarbingersId,
			ResourceLocation open_model, ResourceLocation closed_model, boolean b) {
		super(properties, factionHarbingersId, open_model, closed_model, b);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new IClientItemExtensions() {
			private final NonNullLazy<BlockEntityWithoutLevelRenderer> ister = NonNullLazy.of(() -> {

				return new SpellBookRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
						Minecraft.getInstance().getEntityModels(),
						Hemomancy.rloc("item/special/grimoire_harbinger_open"),
						Hemomancy.rloc("item/special/grimoire_harbinger_closed"), true);

			});

			@Override
			public BlockEntityWithoutLevelRenderer getCustomRenderer() {
				return ister.get();
			}
		});
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
		return 1;
	}

}
