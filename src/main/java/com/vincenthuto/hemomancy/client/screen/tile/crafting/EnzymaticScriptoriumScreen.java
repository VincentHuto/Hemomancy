package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.EnzymaticScriptoriumMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class EnzymaticScriptoriumScreen extends AbstractContainerScreen<EnzymaticScriptoriumMenu> {
    private static final int OUTER = 0xFF330808;
    private static final int INNER = 0xFF220606;
    private static final int RECESS = 0xFF1A0808;
    private static final int PALE = 0xFFE7B8B8;
    private static final int MUTED = 0xFF9B6969;
    private static final int[] BLOOD_COSTS = {100, 200, 300};
    private static final ResourceLocation[] TENDENCY_ICONS = {
            iconTexture("animus"), iconTexture("flammeus"), iconTexture("ductilis"), iconTexture("lux"),
            iconTexture("mortem"), iconTexture("congeatio"), iconTexture("ferric"), iconTexture("tenebris")
    };
    private static final ResourceLocation[] SLOT_ICONS = {
            iconTexture("equipment"), iconTexture("lapis"), iconTexture("shard")
    };
    private static final ResourceLocation INSCRIBE_ICON = iconTexture("inscribe");
    private static final ResourceLocation DENATURE_ICON = iconTexture("denature");
    private BloodVolumeBarWidget.Bounds bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;
    private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer();

    public EnzymaticScriptoriumScreen(EnzymaticScriptoriumMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 216;
        imageHeight = 214;
        inventoryLabelY = 126;
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        veins.render(g, x, y, imageWidth, 126);
        g.renderOutline(x, y, imageWidth, 126, OUTER);
        g.renderOutline(x + 1, y + 1, imageWidth - 2, 124, INNER);
        InventoryPanelTextures.blit(g, InventoryPanelTextures.BLOODY,
                x + (imageWidth - InventoryPanelTextures.BLOODY.width()) / 2, y + 126);
        bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;
        if (minecraft != null && minecraft.player != null) {
            float time = (minecraft.player.tickCount + partialTick) / 20.0F;
            bloodBarBounds = BloodVolumeBarWidget.render(g, x + 200, y + 27, 10, 91,
                    menu.bloodVolume(), menu.bloodCapacity(), time, OUTER, INNER);
        }

        g.fillGradient(x + 8, y + 24, x + 103, y + 83, 0x60100406, 0x10100406);
        g.fillGradient(x + 106, y + 24, x + 188, y + 85, 0x60100406, 0x10100406);

        for (int i = 0; i < 8; i++) {
            var slot = menu.slots.get(i);
            int sx = x + slot.x, sy = y + slot.y;
            int color = 0xFF000000 | EnumBloodTendency.values()[i].getColor().getColor();
            drawSlot(g, sx, sy, color);
            if (slot.getItem().isEmpty())
                icon(g, TENDENCY_ICONS[i], sx, sy, 16);
            int selected = menu.selected(i);
            int stored = slot.getItem().getCount();
            int controlY = sy + 18;

            for (int pip = 0; pip < 3; pip++) {
                int px = sx + 2 + pip * 5;
                g.fill(px, controlY + 2, px + 3, controlY + 4,
                        pip < selected ? color : pip < stored ? 0xFF704C4C : 0xFF2A1515);
            }
            if (selected > 0) {
                int pulse = minecraft != null && minecraft.level != null
                        && (minecraft.level.getGameTime() / 8 + i) % 2 == 0 ? 0xFFAA3030 : 0xFF5C1818;
                g.fill(sx + 15, sy + 8, x + 105, sy + 9, pulse);
            }
        }

        for (int i = 8; i < 11; i++) {
            var slot = menu.slots.get(i);
            drawSlot(g, x + slot.x, y + slot.y, i == 8 ? 0xFFBD4545 : 0xFF745353);
            if (slot.getItem().isEmpty()) {
                icon(g, SLOT_ICONS[i - 8], x + slot.x, y + slot.y, 16);
            }
        }
        for (int i = 0; i < 3; i++) {
            int bx = x + 112 + i * 25;
            boolean available = i == 0 || menu.tier() >= (i == 1 ? 5 : 7)
                    && menu.playerDegree() >= (i == 1 ? 5 : 7);
            drawControl(g, bx, y + 61, 23, 11, menu.mode() == i, available,
                    inside(mouseX, mouseY, bx, y + 61, 23, 11));
        }
        var provenance = menu.slots.get(8).getItem().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        boolean cursed = provenance != null && !provenance.curse().isBlank();
        drawControl(g, x + 112, y + 73, 73, 11, false, cursed,
                inside(mouseX, mouseY, x + 112, y + 73, 73, 11));

        for (int i = 0; i < 3; i++) {
            int rowY = y + 89 + i * 12;
            boolean available = menu.offerCost(i) > 0 && menu.hasOfferEnzymes(i);
            drawControl(g, x + 8, rowY, 180, 11, false, available,
                    inside(mouseX, mouseY, x + 8, rowY, 180, 11));
            icon(g, INSCRIBE_ICON, x + 11, rowY + 1, 9);
        }
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        smallText(g, title, 8, 7, 0xFFE8A6A6, 0.75F);
        smallText(g, Component.literal("D" + menu.tier()), 176, 8, 0xFFCC7777, 0.65F);
        label(g, "screen.hemomancy.scriptorium.tendencies", 10, 18);
        label(g, "screen.hemomancy.scriptorium.inscription", 108, 18);

        for (int i = 0; i < 3; i++) {
            String text = i == 0 ? "Normal" : "+" + i;
            smallText(g, Component.literal(text), 123 + i * 25 - font.width(text) * 0.3F,
                    64, i == menu.mode() ? 0xFFFFCCCC : MUTED, 0.6F);
        }
        icon(g, DENATURE_ICON, 119, 74, 9);
        smallText(g, Component.translatable("screen.hemomancy.scriptorium.denature"), 131, 76, PALE, 0.65F);
        for (int i = 0; i < 3; i++) {
            int rowY = 92 + i * 12;
            int cost = menu.offerCost(i);
            smallText(g, Component.translatable("screen.hemomancy.scriptorium.offer", i + 1),
                    25, rowY, cost > 0 ? PALE : MUTED, 0.65F);
            String price = cost <= 0 ? "--" : cost + " XP  /  " + BLOOD_COSTS[i] + " mL";
            smallText(g, Component.literal(price), 183 - font.width(price) * 0.65F,
                    rowY, cost > 0 ? 0xFFE9A3A3 : MUTED, 0.65F);
        }
    }

    private void label(GuiGraphics g, String key, int x, int y) {
        smallText(g, Component.translatable(key), x, y + 1, MUTED, 0.65F);
    }

    private void smallText(GuiGraphics g, Component text, float x, float y, int color, float scale) {
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1);
        g.drawString(font, text, 0, 0, color, false);
        g.pose().popPose();
    }

    private static ResourceLocation iconTexture(String name) {
        return Hemomancy.rloc("textures/gui/scriptorium/" + name + ".png");
    }

    private static void icon(GuiGraphics g, ResourceLocation texture, int x, int y, int size) {
        int sourceSize = texture.equals(TENDENCY_ICONS[3]) ? 18 : 16;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.blit(texture, x, y, size, size, 0.0F, 0.0F, sourceSize, sourceSize, sourceSize, sourceSize);
        RenderSystem.disableBlend();
    }

    private static void drawSlot(GuiGraphics g, int x, int y, int accent) {
        g.fillGradient(x + 1, y, x + 15, y + 16, RECESS, 0x701A0808);
        g.fill(x, y + 2, x + 1, y + 14, 0xFF351313);
        g.fill(x + 15, y + 2, x + 16, y + 14, 0xFF351313);
        g.fill(x + 4, y + 15, x + 12, y + 16, accent);
    }

    private static void drawControl(GuiGraphics g, int x, int y, int width, int height,
                                    boolean selected, boolean enabled, boolean hovered) {
        int border = !enabled ? 0xFF3D2222 : selected ? 0xFFEF6666 : hovered ? 0xFFB64040 : 0xFF6B2424;
        g.fillGradient(x + 2, y, x + width - 2, y + height,
                !enabled ? 0x40190F10 : hovered || selected ? 0xC0511515 : 0x6027090B, 0x1027090B);
        g.fill(x + 3, y + height - 1, x + width - 3, y + height, border);
    }

    private static boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < 8; i++) {
                var slot = menu.slots.get(i);
                if (inside(mouseX, mouseY, leftPos + slot.x, topPos + slot.y + 18, 16, 6)) {
                    send(i);
                    return true;
                }
            }
            for (int i = 0; i < 3; i++) {
                if (inside(mouseX, mouseY, leftPos + 112 + i * 25, topPos + 61, 23, 11)) {
                    send(20 + i);
                    return true;
                }
                if (inside(mouseX, mouseY, leftPos + 8, topPos + 89 + i * 12, 180, 11)) {
                    send(8 + i);
                    return true;
                }
            }
            if (inside(mouseX, mouseY, leftPos + 112, topPos + 73, 73, 11)) {
                send(11);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void send(int button) {
        if (minecraft != null && minecraft.gameMode != null)
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, button);
    }

    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        if (minecraft != null && minecraft.player != null && bloodBarBounds.contains(mouseX, mouseY)) {
            BloodVolumeBarWidget.renderTooltip(g, font, bloodBarBounds,
                    menu.bloodVolume(), menu.bloodCapacity(), mouseX, mouseY);
            return;
        }
        for (int i = 0; i < 11; i++) {
            var slot = menu.slots.get(i);
            if (!slot.getItem().isEmpty() || !inside(mouseX, mouseY, leftPos + slot.x, topPos + slot.y, 16, 16)) continue;
            Component hint = i < 8 ? EnumBloodTendency.getRepEnzyme(EnumBloodTendency.values()[i]).getDescription()
                    : Component.translatable("screen.hemomancy.scriptorium." + (i == 8 ? "item" : i == 9 ? "lapis" : "shard"));
            g.renderTooltip(font, hint, mouseX, mouseY);
            return;
        }
        for (int i = 0; i < 8; i++) {
            var slot = menu.slots.get(i);
            if (inside(mouseX, mouseY, leftPos + slot.x, topPos + slot.y + 18, 16, 6)) {
                g.renderTooltip(font, List.of(
                        EnumBloodTendency.getRepEnzyme(EnumBloodTendency.values()[i]).getDescription(),
                        Component.translatable("screen.hemomancy.scriptorium.selection",
                                menu.selected(i), slot.getItem().getCount())), Optional.empty(), mouseX, mouseY);
                return;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (!inside(mouseX, mouseY, leftPos + 8, topPos + 89 + i * 12, 180, 11)) continue;
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("screen.hemomancy.scriptorium.offer", i + 1));
            if (menu.offerCost(i) > 0)
                tooltip.add(Component.translatable("screen.hemomancy.scriptorium.cost",
                        menu.offerCost(i), i + 1, BLOOD_COSTS[i]));
            if (menu.offerCost(i) > 0) {
                tooltip.add(Component.translatable("screen.hemomancy.scriptorium.enzymes"));
                for (int tendency = 0; tendency < 8; tendency++) {
                    int required = menu.enzymeCost(i, tendency);
                    if (required == 0) continue;
                    int stored = menu.slots.get(tendency).getItem().getCount();
                    tooltip.add(Component.translatable("screen.hemomancy.scriptorium.enzyme_cost",
                            required, EnumBloodTendency.getRepEnzyme(EnumBloodTendency.values()[tendency]).getDescription(), stored)
                            .withStyle(stored >= required ? net.minecraft.ChatFormatting.GRAY : net.minecraft.ChatFormatting.RED));
                }
            }
            int clue = menu.mode() > 0 ? menu.targetId(i) : menu.clueId(i);
            if (clue >= 0 && minecraft != null && minecraft.level != null) {
                var registry = minecraft.level.registryAccess().registryOrThrow(Registries.ENCHANTMENT);
                var enchantment = registry.asHolderIdMap().byId(clue);
                if (enchantment != null) {
                    int level = menu.mode() > 0 ? enchantment.value().getMaxLevel() + menu.mode() : menu.clueLevel(i);
                    tooltip.add(Enchantment.getFullname(enchantment, level));
                }
            }
            g.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
            return;
        }
    }
}
