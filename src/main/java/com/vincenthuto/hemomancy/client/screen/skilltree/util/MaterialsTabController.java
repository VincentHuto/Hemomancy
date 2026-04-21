package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import java.util.LinkedHashMap;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;

public class MaterialsTabController implements IProgressTab {
    private final PanZoomState panZoom = new PanZoomState();
    private final LinkedHashMap<MaterialEntry, int[]> positions = new LinkedHashMap<>();
    private int contentW, contentH;
    private MaterialEntry selectedEntry = null;

    private static final int NODE_SIZE = 26;

    // ── Theme (Harbinger defaults) ────────────────────────────────
    private final List<MaterialEntry> entries;
    private final EnumNodeShape nodeShape;
    private final int tabColor;
    private final int nodeTransparentColor;
    private final int nodeAccentColor;
    private final int panelSeparatorColor;
    private final int panelBgColor;
    private final MiniRecipeRenderer.Theme renderer;

    /** Harbinger-flavoured controller using the orange-copper blood-magic palette. */
    public MaterialsTabController() {
        this(MaterialsData.getBloodEntries(), EnumNodeShape.SQUARE,
                0xFFCC6644, 0x00CC6644, 0xFFBB7733,
                0xFF442222, 0xDD1A0505, MiniRecipeRenderer.BLOOD);
    }

    /**
     * Creates a fully parametrised materials-tab controller.
     *
     * @param entries              the list of material entries to display
     * @param nodeShape            shape of the node icons
     * @param tabColor             primary accent colour
     * @param nodeTransparentColor transparent version of the accent (alpha 0)
     * @param nodeAccentColor      secondary accent colour for node highlights
     * @param panelSeparatorColor  separator line colour in the info panel
     * @param panelBgColor         background colour in the info panel
     * @param renderer             recipe renderer style
     */
    public MaterialsTabController(List<MaterialEntry> entries, EnumNodeShape nodeShape,
                                   int tabColor, int nodeTransparentColor, int nodeAccentColor,
                                   int panelSeparatorColor, int panelBgColor,
                                   MiniRecipeRenderer.Theme renderer) {
        this.entries              = entries;
        this.nodeShape            = nodeShape;
        this.tabColor             = tabColor;
        this.nodeTransparentColor = nodeTransparentColor;
        this.nodeAccentColor      = nodeAccentColor;
        this.panelSeparatorColor  = panelSeparatorColor;
        this.panelBgColor         = panelBgColor;
        this.renderer             = renderer;
    }

    @Override
    public void onInit(ProgressScreenContext ctx) {
        int[] bounds = new int[2];
        MaterialsTabView.buildLayout(entries, positions, bounds, NODE_SIZE);
        contentW = bounds[0];
        contentH = bounds[1];
        panZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
    }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
        MaterialsTabView.drawNodes(gfx, ctx.font(),
                entries, positions,
                panZoom, ctx.guiLeft(), ctx.guiTop(), NODE_SIZE, nodeShape,
                tabColor, selectedEntry, nodeTransparentColor, nodeAccentColor);
    }

    @Override
    public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
        if (selectedEntry != null) {
            MaterialsTabView.drawInfoPanel(gfx, ctx.font(), selectedEntry,
                    ctx.guiLeft(), ctx.guiTop(), ctx.guiWidth(),
                    tabColor, panelSeparatorColor, panelBgColor, renderer);
        }
    }

    @Override
    public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
        MaterialsTabView.drawTooltip(gfx, ctx.font(), positions,
                panZoom, ctx.guiLeft(), ctx.guiTop(),
                ctx.guiWidth(), ctx.guiHeight(), NODE_SIZE,
                nodeShape, tabColor, nodeAccentColor, mx, my);
    }

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn != 0) return false;
        MaterialEntry hit = MaterialsTabView.nodeUnder(positions, panZoom,
                ctx.guiLeft(), ctx.guiTop(), NODE_SIZE, nodeShape, mx, my);
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
