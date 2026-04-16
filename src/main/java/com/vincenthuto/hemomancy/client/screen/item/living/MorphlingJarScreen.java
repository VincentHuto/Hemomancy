package com.vincenthuto.hemomancy.client.screen.item.living;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.MorphlingJarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * The insertion screen opened with Shift+Right-click on the Morphling Jar.
 * Players drag Morphling items into the 2×2 slot grid to store them.
 */
public class MorphlingJarScreen extends AbstractContainerScreen<MorphlingJarMenu> {

    // Single GUI texture – matches the refactored 2×2 slot layout (176×166)
    private static final ResourceLocation GUI =
            Hemomancy.rloc("textures/gui/morphling_jar_gui.png");

    // ─── Blood ring constants ────────────────────────────────────────────────────
    /** Outer radius of the ring (pixels). */
    private static final float RING_RADIUS_OUT = 30f;
    /** Inner radius of the ring (pixels). */
    private static final float RING_RADIUS_IN  = 25f;
    /** How finely the ring is tessellated (radians per section). */
    private static final float ARC_PRECISION = 2.5f / 360.0f;
    /** Filled segment colour – deep crimson with moderate alpha. */
    private static final int FILLED_COLOR  = 0xC0901818;
    /** Empty segment colour – very dim dark. */
    private static final int EMPTY_COLOR   = 0x60200808;
    /** Glow behind filled segments – wider, more transparent. */
    private static final int GLOW_COLOR    = 0x30FF2020;

    public MorphlingJarScreen(MorphlingJarMenu container, Inventory playerInventory, Component name) {
        super(container, playerInventory, name);
        imageWidth = 176;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        // Push inventory/title labels into the correct relative positions
        titleLabelX = 8;
        titleLabelY = 6;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 94;
    }

    // ─── Rendering ───────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.blit(GUI, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        // Draw the blood volume ring over the slot area
        drawBloodRing(graphics, partialTicks);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title in a deep crimson to match the jar's biological theme
        graphics.drawString(font, this.title.getString(), titleLabelX, titleLabelY, 0x5C1010, false);
        // "Inventory" label in subdued grey
        graphics.drawString(font, this.playerInventoryTitle.getString(),
                inventoryLabelX, inventoryLabelY, 0x404040, false);

        // Draw fill text in the centre of the ring
        MorphlingJarMenu m = this.menu;
        String fillText = m.getFilledSlotCount() + "/" + m.slotcount;
        int textW = font.width(fillText);
        // Ring center relative to gui origin: (88, 45)
        graphics.drawString(font, fillText, 88 - textW / 2, 16, 0x802020, false);
    }

    // ─── Blood ring ──────────────────────────────────────────────────────────────

    /**
     * Draws a circular ring centred on the slot grid.  Filled segments are bright
     * crimson; empty segments are dim.  A subtle glow halo sits behind the filled
     * portion.  The ring starts from the top (−π/2) and sweeps clockwise.
     */
    private void drawBloodRing(GuiGraphics graphics, float partialTicks) {
        float fillRatio = menu.getFillRatio();

        // Centre of the slot grid in screen-space
        float cx = leftPos + 88;
        float cy = topPos + 45;

        // Start at 12-o'clock (−π/2), sweep full circle clockwise
        float startAngle = (float) (-Math.PI / 2.0);
        float fullSweep  = (float) (Math.PI * 2.0);
        float filledEnd   = startAngle + fullSweep * fillRatio;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // 1) Glow layer behind filled portion (wider, softer)
        if (fillRatio > 0f) {
            drawArc(buffer, cx, cy, 0f,
                    RING_RADIUS_IN - 3f, RING_RADIUS_OUT + 3f,
                    startAngle, filledEnd, GLOW_COLOR);
        }

        // 2) Empty portion of the ring
        if (fillRatio < 1f) {
            drawArc(buffer, cx, cy, 0f,
                    RING_RADIUS_IN, RING_RADIUS_OUT,
                    filledEnd, startAngle + fullSweep, EMPTY_COLOR);
        }

        // 3) Filled portion of the ring
        if (fillRatio > 0f) {
            drawArc(buffer, cx, cy, 0f,
                    RING_RADIUS_IN, RING_RADIUS_OUT,
                    startAngle, filledEnd, FILLED_COLOR);
        }

        tessellator.end();
        RenderSystem.disableBlend();
    }

    /**
     * Tessellates a pie-arc section (annulus sector) into the given buffer.
     * Mirrors the approach used by {@code GenericRadialMenu#drawPieArc}.
     */
    private static void drawArc(BufferBuilder buffer, float x, float y, float z,
                                float radiusIn, float radiusOut,
                                float startAngle, float endAngle, int color) {
        float sweep = endAngle - startAngle;
        int sections = Math.max(1, Mth.ceil(Math.abs(sweep) / ARC_PRECISION));
        float slice = sweep / sections;

        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >>  8) & 0xFF;
        int b =  color        & 0xFF;

        for (int i = 0; i < sections; i++) {
            float a1 = startAngle + i * slice;
            float a2 = startAngle + (i + 1) * slice;

            float cos1 = (float) Math.cos(a1);
            float sin1 = (float) Math.sin(a1);
            float cos2 = (float) Math.cos(a2);
            float sin2 = (float) Math.sin(a2);

            buffer.vertex(x + radiusOut * cos1, y + radiusOut * sin1, z).color(r, g, b, a).endVertex();
            buffer.vertex(x + radiusIn  * cos1, y + radiusIn  * sin1, z).color(r, g, b, a).endVertex();
            buffer.vertex(x + radiusIn  * cos2, y + radiusIn  * sin2, z).color(r, g, b, a).endVertex();
            buffer.vertex(x + radiusOut * cos2, y + radiusOut * sin2, z).color(r, g, b, a).endVertex();
        }
    }

    // ─── Input ───────────────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Allow the inventory key to close the screen
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}

