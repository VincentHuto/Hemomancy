package com.vincenthuto.hemomancy.client.screen.living;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.MorphlingJarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * The insertion screen opened with Shift+Right-click on the Morphling Jar.
 * Players drag Morphling items into the 2×2 slot grid to store them.
 */
public class MorphlingJarScreen extends AbstractContainerScreen<MorphlingJarMenu> {

    // Single GUI texture – matches the refactored 2×2 slot layout (176×166)
    private static final ResourceLocation GUI =
            Hemomancy.rloc("textures/gui/morphling_jar_gui.png");

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
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Title in a deep crimson to match the jar's biological theme
        graphics.drawString(font, this.title.getString(), titleLabelX, titleLabelY, 0x5C1010, false);
        // "Inventory" label in subdued grey
        graphics.drawString(font, this.playerInventoryTitle.getString(),
                inventoryLabelX, inventoryLabelY, 0x404040, false);
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

