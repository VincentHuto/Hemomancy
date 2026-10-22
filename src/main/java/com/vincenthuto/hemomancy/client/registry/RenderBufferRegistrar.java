package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class RenderBufferRegistrar {

    private RenderBufferRegistrar() {
    }

    @SubscribeEvent
    public static void registerScriptoriumGlintBuffers(net.neoforged.neoforge.client.event.RegisterRenderBuffersEvent event) {
        event.registerRenderBuffer(com.vincenthuto.hemomancy.client.render.RenderTypeInit.SCRIPTORIUM_GLINT);
        event.registerRenderBuffer(com.vincenthuto.hemomancy.client.render.RenderTypeInit.SCRIPTORIUM_GLINT_TRANSLUCENT);
        event.registerRenderBuffer(com.vincenthuto.hemomancy.client.render.RenderTypeInit.SCRIPTORIUM_ENTITY_GLINT);
        event.registerRenderBuffer(com.vincenthuto.hemomancy.client.render.RenderTypeInit.SCRIPTORIUM_ENTITY_GLINT_DIRECT);
        event.registerRenderBuffer(com.vincenthuto.hemomancy.client.render.RenderTypeInit.SCRIPTORIUM_ARMOR_ENTITY_GLINT);
    }
}
