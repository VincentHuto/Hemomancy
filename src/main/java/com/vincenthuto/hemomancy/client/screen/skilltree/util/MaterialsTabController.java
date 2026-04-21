package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.LinkedHashMap;

import net.minecraft.client.gui.GuiGraphics;

public class MaterialsTabController implements IProgressTab {
    private final PanZoomState panZoom = new PanZoomState();
    private final LinkedHashMap<MaterialEntry, int[]> positions = new LinkedHashMap<>();
    private int contentW, contentH;
    private MaterialEntry selectedEntry = null;

    private static final int NODE_SIZE = 26;
    private static final int TAB_COLOR = 0xFFCC6644;

    @Override
    public void onInit(ProgressScreenContext ctx) {
        int[] bounds = new int[2];
        MaterialsTabView.buildLayout(MaterialsData.getBloodEntries(), positions, bounds, NODE_SIZE);
        contentW = bounds[0];
        contentH = bounds[1];
        panZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
    }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
        MaterialsTabView.drawNodes(gfx, ctx.font(),
                MaterialsData.getBloodEntries(), positions,
                panZoom, ctx.guiLeft(), ctx.guiTop(), NODE_SIZE, EnumNodeShape.SQUARE,
                TAB_COLOR, selectedEntry, 0x00CC6644, 0xFFBB7733);
    }

    @Override
    public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
        if (selectedEntry != null) {
            MaterialsTabView.drawInfoPanel(gfx, ctx.font(), selectedEntry,
                    ctx.guiLeft(), ctx.guiTop(), ctx.guiWidth(),
                    TAB_COLOR, 0xFF442222, 0xDD1A0505, MiniRecipeRenderer.BLOOD);
        }
    }

    @Override
    public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
        MaterialsTabView.drawTooltip(gfx, ctx.font(), positions,
                panZoom, ctx.guiLeft(), ctx.guiTop(),
                ctx.guiWidth(), ctx.guiHeight(), NODE_SIZE,
                EnumNodeShape.SQUARE, TAB_COLOR, 0xFFBB8833, mx, my);
    }

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn != 0) return false;
        MaterialEntry hit = MaterialsTabView.nodeUnder(positions, panZoom,
                ctx.guiLeft(), ctx.guiTop(), NODE_SIZE, EnumNodeShape.SQUARE, mx, my);
        if (hit != null) {
            selectedEntry = (selectedEntry == hit) ? null : hit;
            return true;
        }
        return false;
    }

    @Override public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) { return false; }
    @Override public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) { return false; }
    @Override public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) { return false; }

    @Override public PanZoomState getPanZoomState() { return panZoom; }
    public int getContentW() { return contentW; }
    public int getContentH() { return contentH; }
}
