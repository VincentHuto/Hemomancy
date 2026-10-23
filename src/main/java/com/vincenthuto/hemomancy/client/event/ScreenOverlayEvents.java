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
import net.minecraft.client.gui.GuiGraphics;

/**
 * Full-screen effects drawn under the HUD (GuiLayerRegistrar's bottom layer), and the Crimson Fire screen overlay.
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

    /**
     * Draws the full-screen effects at the base depth before any HUD layer. The world grade re-grades what is drawn
     * before it: the morphic nectar tint and the Circus edge, but not the white humor tint.
     */
    public static void renderUnderHud(GuiGraphics graphics) {
        renderMorphicNectarScreenOverlay(graphics);
        renderSanguineOmenWorldGrade(graphics);
        renderWhiteHumorScreenOverlay(graphics);
    }

    private static void renderMorphicNectarScreenOverlay(GuiGraphics graphics) {
        MorphicNectarScreenOverlay.render(graphics);
        CircusPerceptionOverlay.render(graphics);
    }

    private static void renderWhiteHumorScreenOverlay(GuiGraphics graphics) {
        WhiteHumorScreenOverlay.render(graphics);
    }

    private static void renderSanguineOmenWorldGrade(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        float partialTicks = minecraft.getTimer().getGameTimeDeltaPartialTick(true);
        if(com.vincenthuto.hemomancy.client.screen.overlay.AxonalTravelView.renderGrade(graphics,
                graphics.guiWidth(),graphics.guiHeight(),partialTicks)) return;
        if(NaeglerophaeonCaptureOverlay.renderWorldGrade(graphics,
                graphics.guiWidth(),graphics.guiHeight(),partialTicks)) return;
        FaneBoundaryRenderer.renderPost(graphics,
                graphics.guiWidth(), graphics.guiHeight(), partialTicks);
        if (SanguineOmenOverlay.instance != null) {
            SanguineOmenOverlay.instance.renderWorldGrade(graphics,
                    graphics.guiWidth(), graphics.guiHeight(), partialTicks);
        }
    }
}
