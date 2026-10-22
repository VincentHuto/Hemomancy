package com.vincenthuto.hemomancy.client.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.data.*;
import com.vincenthuto.hemomancy.client.render.CrimsonFireRenderer;
import com.vincenthuto.hemomancy.client.render.world.*;
import com.vincenthuto.hemomancy.client.screen.overlay.*;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;

/**
 * Full-screen effects drawn before the HUD, and the Crimson Fire screen overlay.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, value = Dist.CLIENT)
public final class ScreenOverlayEvents {

    private ScreenOverlayEvents() {
    }

    @SubscribeEvent
    public static void onRenderBlockScreenEffect(RenderBlockScreenEffectEvent event) {
        if (event.getOverlayType() != RenderBlockScreenEffectEvent.OverlayType.FIRE
                || !CrimsonFireClientState.isActive(event.getPlayer())) {
            return;
        }
        CrimsonFireRenderer.renderScreenOverlay(Minecraft.getInstance(), event.getPoseStack());
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void renderMorphicNectarScreenOverlay(RenderGuiEvent.Pre event) {
        MorphicNectarScreenOverlay.render(event.getGuiGraphics());
        CircusPerceptionOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void renderWhiteHumorScreenOverlay(RenderGuiEvent.Pre event) {
        WhiteHumorScreenOverlay.render(event.getGuiGraphics());
    }

    @SubscribeEvent
    public static void renderSanguineOmenWorldGrade(RenderGuiEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(true);
        if(com.vincenthuto.hemomancy.client.screen.overlay.AxonalTravelView.renderGrade(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(),event.getGuiGraphics().guiHeight(),partialTicks)) return;
        if(NaeglerophaeonCaptureOverlay.renderWorldGrade(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(),event.getGuiGraphics().guiHeight(),partialTicks)) return;
        FaneBoundaryRenderer.renderPost(event.getGuiGraphics(),
                event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(), partialTicks);
        if (SanguineOmenOverlay.instance != null) {
            SanguineOmenOverlay.instance.renderWorldGrade(event.getGuiGraphics(),
                    event.getGuiGraphics().guiWidth(), event.getGuiGraphics().guiHeight(), partialTicks);
        }
    }
}
