package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import net.minecraft.client.gui.GuiGraphics;

public interface IProgressTab {
    void onInit(ProgressScreenContext ctx);
    void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial);
    void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY);
    void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY);
    boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn);
    boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn);
    boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy);
    boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta);
    PanZoomState getPanZoomState();
}
