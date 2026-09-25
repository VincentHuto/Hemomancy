package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeRules;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ResonantForgeMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;

public class ResonantForgeScreen extends AbstractContainerScreen<ResonantForgeMenu> {
    private static final int OUTER = 0xFF330808;
    private static final int INNER = 0xFF220606;
    private static final int PALE = 0xFFE7B8B8;
    private static final int MUTED = 0xFF9B6969;
    private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer();
    private BloodVolumeBarWidget.Bounds bloodBounds = BloodVolumeBarWidget.Bounds.EMPTY;

    public ResonantForgeScreen(ResonantForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 220;
        imageHeight = 224;
        inventoryLabelY = 136;
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        veins.render(g, x, y, imageWidth, 136);
        g.renderOutline(x, y, imageWidth, 136, OUTER);
        g.renderOutline(x + 1, y + 1, imageWidth - 2, 134, INNER);
        InventoryPanelTextures.blit(g, InventoryPanelTextures.BLOODY,
                x + (imageWidth - InventoryPanelTextures.BLOODY.width()) / 2, y + 136);

        g.fillGradient(x + 8, y + 24, x + 100, y + 108, 0x60100406, 0x10100406);
        g.fillGradient(x + 120, y + 24, x + 212, y + 108, 0x60100406, 0x10100406);
        g.fillGradient(x + 102, y + 24, x + 118, y + 122, 0x80240708, 0x30100406);

        for (int i = 0; i < ResonantForgeBlockEntity.SLOT_COUNT; i++) {
            var slot = menu.slots.get(i);
            slot(g, x + slot.x, y + slot.y, i == ResonantForgeBlockEntity.APPLICATION_OUTPUT
                    || i >= ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT ? 0xFFB64A4A : 0xFF704040);
        }
        if (menu.forge().getItem(ResonantForgeBlockEntity.APPLICATION_OUTPUT).isEmpty()) {
            var slot = menu.slots.get(ResonantForgeBlockEntity.APPLICATION_OUTPUT);
            ItemStackPreview.render(g, menu.applicationPreview(), x + slot.x, y + slot.y);
        }
        if (menu.forge().getItem(ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT).isEmpty()) {
            var preview = menu.grindingPreview();
            if (preview.success()) {
                var equipmentSlot = menu.slots.get(ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT);
                var cylinderSlot = menu.slots.get(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT);
                ItemStackPreview.render(g, preview.equipment(), x + equipmentSlot.x, y + equipmentSlot.y);
                var cylinderPreview = menu.forge().getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER).copy();
                cylinderPreview.set(com.vincenthuto.hemomancy.common.init.DataComponentInit.RESONANT_PATTERN.get(),
                        preview.pattern());
                ItemStackPreview.render(g, cylinderPreview, x + cylinderSlot.x, y + cylinderSlot.y);
            }
        }

        control(g, x + 14, y + 96, 82, 12, menu.operation() == 1, true,
                inside(mouseX, mouseY, x + 14, y + 96, 82, 12));
        control(g, x + 124, y + 96, 82, 12, menu.operation() == 2, true,
                inside(mouseX, mouseY, x + 124, y + 96, 82, 12));
        control(g, x + 124, y + 110, 40, 10, menu.operation() == 3, menu.tier() >= 2,
                inside(mouseX, mouseY, x + 124, y + 110, 40, 10));
        control(g, x + 166, y + 110, 40, 10, menu.masterMode(), menu.tier() >= 2,
                inside(mouseX, mouseY, x + 166, y + 110, 40, 10));
        control(g, x + 124, y + 84, 82, 10, false, menu.tier() >= 1,
                inside(mouseX, mouseY, x + 124, y + 84, 82, 10));

        if (menu.totalTicks() > 0) {
            int width = Math.clamp(menu.progress() * 82 / menu.totalTicks(), 0, 82);
            control(g, x + 14, y + 111, 82, 11, true, true,
                    inside(mouseX, mouseY, x + 14, y + 111, 82, 11));
            g.fill(x + 14, y + 123, x + 96, y + 126, 0xFF2A0A0A);
            g.fill(x + 14, y + 123, x + 14 + width, y + 126, 0xFFB22C35);
        }

        float time = minecraft == null || minecraft.player == null ? 0 : (minecraft.player.tickCount + partialTick) / 20F;
        bloodBounds = BloodVolumeBarWidget.render(g, x + 105, y + 31, 10, 90,
                menu.blood(), menu.capacity(), time, OUTER, INNER);
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        text(g, title, 8, 7, 0xFFE8A6A6, .75F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.applying"), 16, 18, MUTED, .65F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.grinding"), 126, 18, MUTED, .65F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.apply"), 42, 99, PALE, .65F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.record"), 150, 99, PALE, .65F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.stabilize"), 128, 112, PALE, .55F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.master"), 172, 112,
                menu.masterMode() ? 0xFFFFAAAA : MUTED, .55F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.selection",
                selectionName()), 128, 86, PALE, .55F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.tier", menu.tier() == 0 ? 3 : menu.tier() == 1 ? 5 : 7),
                184, 8, 0xFFCC7777, .6F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.status." + statusName()),
                8, 128, MUTED, .55F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.cost", menu.applicationCost()),
                16, 82, MUTED, .52F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.cost", menu.grindingCost()),
                128, 59, MUTED, .52F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.hammer_wear", menu.hammerUses(),
                ResonantForgeRules.HAMMER_SERVICE_INTERVAL), 12, 27,
                menu.hammerUses() >= 56 ? 0xFFFF7777 : MUTED, .48F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.wheel_wear", menu.wheelUses(),
                ResonantForgeRules.WHEEL_SERVICE_INTERVAL), 126, 27,
                menu.wheelUses() >= 28 ? 0xFFFF7777 : MUTED, .48F);
        var cylinder = menu.forge().getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER)
                .get(com.vincenthuto.hemomancy.common.init.DataComponentInit.RESONANT_PATTERN.get());
        if (cylinder != null) text(g, Component.translatable(cylinder.master()
                        ? "screen.hemomancy.resonant_forge.cylinder.reusable"
                        : "screen.hemomancy.resonant_forge.cylinder.consumed"),
                16, 72, cylinder.master() ? 0xFFE7B878 : MUTED, .48F);
        if (menu.operation() != 0)
            text(g, Component.translatable("screen.hemomancy.resonant_forge.cancel"), 42, 113, PALE, .6F);
    }

    private Component selectionName() {
        String selected = menu.forge().selection();
        if (selected.isBlank()) return Component.translatable("screen.hemomancy.resonant_forge.all");
        if (selected.equals(ResonantForgeRules.CURSE_SELECTION))
            return Component.translatable("screen.hemomancy.resonant_forge.curse");
        return Component.literal(selected.substring(selected.indexOf(':') + 1).replace('_', ' '));
    }

    private String statusName() {
        var values = ResonantForgeBlockEntity.Status.values();
        return values[Math.clamp(menu.status(), 0, values.length - 1)].name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (inside(mouseX, mouseY, leftPos + 14, topPos + 96, 82, 12)) return send(0);
            if (inside(mouseX, mouseY, leftPos + 124, topPos + 96, 82, 12)) return send(1);
            if (inside(mouseX, mouseY, leftPos + 124, topPos + 110, 40, 10)) return send(2);
            if (inside(mouseX, mouseY, leftPos + 166, topPos + 110, 40, 10)) return send(4);
            if (inside(mouseX, mouseY, leftPos + 124, topPos + 84, 82, 10)) {
                List<String> options = menu.selections();
                int current = options.indexOf(menu.forge().selection());
                return send(100 + ((current + 1) % Math.max(1, options.size())));
            }
            if (menu.operation() != 0 && inside(mouseX, mouseY, leftPos + 14, topPos + 111, 82, 15)) return send(3);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean send(int button) {
        if (minecraft != null && minecraft.gameMode != null)
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
        return true;
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        BloodVolumeBarWidget.renderTooltip(g, font, bloodBounds, menu.blood(), menu.capacity(), mouseX, mouseY);
        if (inside(mouseX, mouseY, leftPos + 124, topPos + 84, 82, 10))
            g.renderTooltip(font, Component.translatable("screen.hemomancy.resonant_forge.selection.tooltip"), mouseX, mouseY);
    }

    private static void slot(GuiGraphics g, int x, int y, int accent) {
        g.fillGradient(x + 1, y, x + 15, y + 16, 0xFF1A0808, 0x701A0808);
        g.fill(x, y + 2, x + 1, y + 14, 0xFF351313);
        g.fill(x + 15, y + 2, x + 16, y + 14, 0xFF351313);
        g.fill(x + 4, y + 15, x + 12, y + 16, accent);
    }

    private static void control(GuiGraphics g, int x, int y, int width, int height,
            boolean selected, boolean enabled, boolean hovered) {
        int border = !enabled ? 0xFF3D2222 : selected ? 0xFFEF6666 : hovered ? 0xFFB64040 : 0xFF6B2424;
        g.fillGradient(x + 2, y, x + width - 2, y + height,
                !enabled ? 0x40190F10 : hovered || selected ? 0xC0511515 : 0x6027090B, 0x1027090B);
        g.fill(x + 3, y + height - 1, x + width - 3, y + height, border);
    }

    private static final class ItemStackPreview {
        private static void render(GuiGraphics graphics, net.minecraft.world.item.ItemStack stack, int x, int y) {
            if (stack.isEmpty()) return;
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 5);
            graphics.renderFakeItem(stack, x, y);
            graphics.fill(x + 1, y + 1, x + 15, y + 15, 0x55300030);
            graphics.pose().popPose();
        }
    }

    private void text(GuiGraphics g, Component value, float x, float y, int color, float scale) {
        g.pose().pushPose(); g.pose().translate(x, y, 0); g.pose().scale(scale, scale, 1);
        g.drawString(font, value, 0, 0, color, false); g.pose().popPose();
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }
}
