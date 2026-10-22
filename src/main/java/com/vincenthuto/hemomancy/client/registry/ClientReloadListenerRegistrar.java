package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.entity.misc.*;
import com.vincenthuto.hemomancy.client.render.world.*;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientReloadListenerRegistrar {

    private ClientReloadListenerRegistrar() {
    }

    @SubscribeEvent
    public static void registerTreeCacheReloadListener(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) resourceManager -> {
            com.vincenthuto.hemomancy.client.player.HematicMicroscopeClientState.clear();
            ArborOfWillRenderer.clearCaches();
            QliphothBloomRenderer.clearCaches();
            FaneBoundaryRenderer.clearCaches();
            if (BloodVolumeOverlay.instance != null) BloodVolumeOverlay.instance.clearCaches();
        });
    }
}
