package com.vincenthuto.hemomancy.client.registry;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.ClientBridgeImpl;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import com.vincenthuto.hemomancy.common.init.*;
import com.vincenthuto.hemomancy.common.util.ClientBridge;
import net.minecraft.client.renderer.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client setup: the client bridge, morphling mutation visuals, translucent block and fluid layers, and the HUD
 * overlay instances.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ClientSetupRegistrar {

    private ClientSetupRegistrar() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        ClientBridge.install(new ClientBridgeImpl());
        com.vincenthuto.hemomancy.client.model.morphling.MorphlingMutationRegistry.init();
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(BlockInit.frozen_cruor.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.WHITE_HUMOR.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.WHITE_HUMOR_FLOWING.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.RENDERING_BILE.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(FluidInit.RENDERING_BILE_FLOWING.get(), RenderType.translucent());
        });
        BloodVolumeOverlay.instance = new BloodVolumeOverlay();
        EquippedMorphlingOverlay.instance = new EquippedMorphlingOverlay();
        HarbingerLodestoneOverlay.instance = new HarbingerLodestoneOverlay();
        StillArtCooldownOverlay.instance = new StillArtCooldownOverlay();
        UnstainedGaugeOverlay.instance = new UnstainedGaugeOverlay();
        FungalWhisperVignetteOverlay.instance = new FungalWhisperVignetteOverlay();
        SanguineOmenOverlay.instance = new SanguineOmenOverlay();
        WillPresenceOverlay.instance = new WillPresenceOverlay();
        CardinalRiteOverlay.instance = new CardinalRiteOverlay();
    }
}
