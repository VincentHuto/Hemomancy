package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** The Phlegethontic ichor renders translucent; its renderers and layers are registered with the others. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class PhlegethonticClientEvents {
    private PhlegethonticClientEvents() {}

    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(FluidInit.PHLEGETHONTIC_ICHOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.PHLEGETHONTIC_ICHOR_FLOWING.get(), RenderType.translucent());
        });
    }
}
