package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.item.ItemClientExtensions;
import com.vincenthuto.hemomancy.client.model.item.book.HemoBookBinding;
import com.vincenthuto.hemomancy.client.render.item.HemoBookItemRenderer;
import com.vincenthuto.hutoslib.client.render.item.RenderItemArmBanner;
import com.vincenthuto.hutoslib.client.render.item.RenderItemGuideBook;
import com.vincenthuto.hutoslib.common.item.ItemArmBanner;
import com.vincenthuto.hutoslib.common.item.ItemGuideBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientExtensionsRegistrar {

    private ClientExtensionsRegistrar() {
    }

    @SubscribeEvent
    public static void registerClientItemExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions armBannerRenderer = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new RenderItemArmBanner(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                        Minecraft.getInstance().getEntityModels());
            }
        };

        IClientItemExtensions bookRenderer = new IClientItemExtensions() {
            // Asked for on every draw; built once, on first use, after the entity models have loaded.
            private HemoBookItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new HemoBookItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                            Minecraft.getInstance().getEntityModels());
                }
                return renderer;
            }
        };
        IClientItemExtensions guideBookRenderer = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new RenderItemGuideBook(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                        Minecraft.getInstance().getEntityModels());
            }
        };

        FluidClientExtensions.register(event);

        for (Item item : BuiltInRegistries.ITEM) {
            if (!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(Hemomancy.MOD_ID)) {
                continue;
            }
            if (item instanceof ItemArmBanner) {
                event.registerItem(armBannerRenderer, item);
                continue;
            }
            if (item instanceof ItemGuideBook) {
                // Hemomancy's books have their own models; any other guide book keeps HutosLib's vanilla-book renderer.
                event.registerItem(HemoBookBinding.of(item) != null ? bookRenderer : guideBookRenderer, item);
                continue;
            }
            IClientItemExtensions extensions = ItemClientExtensions.create(item);
            if (extensions != null) {
                event.registerItem(extensions, item);
            }
        }
    }
}
