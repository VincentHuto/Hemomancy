package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodSampleData;
import com.vincenthuto.hemomancy.common.menu.tile.functional.PhlebotomistsCabinetMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class PhlebotomistsCabinetScreen extends AbstractContainerScreen<PhlebotomistsCabinetMenu> {
    private final VeinBackgroundRenderer veinBackground = new VeinBackgroundRenderer();
    private boolean cellPress;
    public PhlebotomistsCabinetScreen(PhlebotomistsCabinetMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 226;
        inventoryLabelY = 132;
    }
    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        var firstInventorySlot = menu.slots.get(9);
        int specimenHeight = firstInventorySlot.y - 6;
        g.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF0A0204);
        veinBackground.render(g, leftPos, topPos, imageWidth, specimenHeight);
        g.renderOutline(leftPos, topPos, imageWidth, specimenHeight, 0xFF330808);
        g.renderOutline(leftPos + 1, topPos + 1, imageWidth - 2, specimenHeight - 2, 0xFF220606);
        InventoryPanelTextures.blit(g, InventoryPanelTextures.BLOODY,
                leftPos + firstInventorySlot.x - 5, topPos + firstInventorySlot.y - 6);
        for (int i = 0; i < 9; i++) {
            int x = leftPos + 8 + i % 3 * 54, y = topPos + 20 + i / 3 * 36;
            g.fill(x, y, x + 52, y + 34, 0xB01A0808);
            g.renderOutline(x, y, 52, 34, 0xFF440E0E);
            var slot = menu.slots.get(i);
            int sx = leftPos + slot.x, sy = topPos + slot.y;
            g.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0xFF0D0303);
            g.fill(sx, sy, sx + 16, sy + 16, 0xFF1A0808);
            g.fill(sx + 16, sy, sx + 17, sy + 17, 0xFF3A1212);
            g.fill(sx, sy + 16, sx + 17, sy + 17, 0xFF3A1212);
        }
    }
    private Component source(ItemStack stack) {
        var type = BloodSampleData.entityType(stack);
        return type == null ? Component.literal(BloodSampleData.rawSource(stack)) : type.getDescription();
    }
    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        String titleText = font.plainSubstrByWidth(title.getString(), imageWidth - 16);
        g.drawString(font, titleText, 8, 7, 0xFFAA2222, false);
        for (int i = 0; i < 9; i++) {
            int x = 8 + i % 3 * 54, y = 20 + i / 3 * 36;
            ItemStack stack = menu.slots.get(i).getItem();
            if (stack.isEmpty()) continue;
            String count = Integer.toString(menu.count(i));
            g.drawString(font, count, x + 49 - font.width(count), y + 6, 0xFFFFCCCC, false);
            String name = source(stack).getString();
            if (font.width(name) > 48) name = font.plainSubstrByWidth(name, 42) + "…";
            g.drawString(font, name, x + 2, y + 23, 0xFFCC8888, false);
            var profile = BloodSampleData.profile(stack, List.of());
            if (profile.identified()) {
                for (int t = 0; t < profile.tendencies().size(); t++) {
                    var color = profile.tendencies().get(t).getColor();
                    int rgb = 0xFF000000 | color.getColor();
                    g.fill(x + 2 + t * 6, y + 19, x + 7 + t * 6, y + 21, rgb);
                }
            } else g.drawString(font, "?", x + 23, y + 6, 0xFFAA6666, false);
        }
    }
    private int cellAt(double x, double y) {
        int localX = (int)x - leftPos - 8, localY = (int)y - topPos - 20;
        if (localX < 0 || localY < 0 || localX >= 162 || localY >= 108 || localX % 54 >= 52 || localY % 36 >= 34) return -1;
        return localX / 54 + localY / 36 * 3;
    }
    @Override public boolean mouseClicked(double x, double y, int button) {
        int cell = cellAt(x, y);
        if (cell >= 0) {
            cellPress = true;
            if (button == 0 || button == 1) minecraft.gameMode.handleInventoryMouseClick(menu.containerId, cell, button,
                    hasShiftDown() ? ClickType.QUICK_MOVE : ClickType.PICKUP, minecraft.player);
            return true;
        }
        return super.mouseClicked(x, y, button);
    }
    @Override public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
        return cellPress || super.mouseDragged(x, y, button, dragX, dragY);
    }
    @Override public boolean mouseReleased(double x, double y, int button) {
        if (cellPress) {
            // The cell transaction already ran on press. Vanilla release may otherwise
            // send a second pickup once the server's carried-stack update arrives.
            cellPress = false;
            return true;
        }
        return super.mouseReleased(x, y, button);
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.render(g, mouseX, mouseY, partialTick);
        int cell = cellAt(mouseX, mouseY);
        if (cell >= 0) {
            var stack = menu.slots.get(cell).getItem();
            var lines = new ArrayList<Component>();
            if (!stack.isEmpty()) lines.addAll(getTooltipFromItem(minecraft, stack));
            else lines.add(Component.translatable("tooltip.hemomancy.cabinet.empty"));
            lines.add(Component.translatable("tooltip.hemomancy.cabinet.cell_capacity", menu.count(cell), menu.capacity()));
            g.renderComponentTooltip(font, lines, mouseX, mouseY);
        } else renderTooltip(g, mouseX, mouseY);
    }
}
