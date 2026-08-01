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
    default boolean keyPressed(ProgressScreenContext ctx, int keyCode, int scanCode, int modifiers) { return false; }
    default boolean charTyped(ProgressScreenContext ctx, char codePoint, int modifiers) { return false; }
    /** Closes the tab's persistent node-detail window, if one is open. */
    default boolean closeDetails() { return false; }
    default int getContentW() { return 0; }
    default int getContentH() { return 0; }
    default int getNavigationViewportWidth(ProgressScreenContext ctx) { return ctx.guiWidth(); }
    default void resetView(ProgressScreenContext ctx) {
        PanZoomState view = getPanZoomState();
        if (view != null) view.centreOn(getContentW(), getContentH(), getNavigationViewportWidth(ctx), ctx.guiHeight());
    }
}
