package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.item.HemoClientItemExtensionsProvider;
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
                event.registerItem(guideBookRenderer, item);
                continue;
            }
            if (item instanceof HemoClientItemExtensionsProvider provider) {
                event.registerItem(provider.hemomancy$getClientItemExtensions(), item);
            }
        }
    }
}
