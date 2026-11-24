package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.EnzymaticScriptoriumMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.EnzymaticScriptoriumBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Scriptorium drawn as its block seen from above, reader at the bottom: eight jars on the half-circle of arms,
 * each capillary running to the hub under the open book, the back arm feeding the riser whose nib drips on the
 * spine, and the shard cup on the reader's side. Lids, rules and the riser take the stage's fittings.
 */
public final class EnzymaticScriptoriumScreen extends AbstractContainerScreen<EnzymaticScriptoriumMenu> {
    private static final int OUTER = 0xFF330808;
    private static final int INNER = 0xFF220606;
    private static final int RECESS = 0xFF1A0808;
    private static final int PALE = 0xFFE7B8B8;
    private static final int MUTED = 0xFF9B6969;
    private static final int IRON = 0xFF3A2222;
    private static final int GLASS = 0x80704848;
    private static final ResourceLocation[] TEMPER_ICONS = {
            iconTexture("animus"), iconTexture("flammeus"), iconTexture("ductilis"), iconTexture("lux"),
            iconTexture("mortem"), iconTexture("congeatio"), iconTexture("ferric"), iconTexture("tenebris")
    };
    private static final ResourceLocation[] SLOT_ICONS = {
            iconTexture("equipment"), iconTexture("lapis"), iconTexture("shard")
    };
    private static final ResourceLocation INSCRIBE_ICON = iconTexture("inscribe");
    private static final ResourceLocation DENATURE_ICON = iconTexture("denature");
    private static final int TOP = EnzymaticScriptoriumMenu.INVENTORY_TOP;
    private static final int HUB_X = EnzymaticScriptoriumMenu.HUB_X, HUB_Y = EnzymaticScriptoriumMenu.HUB_Y;
    private static final int RISER_TOP = 24, RISER_BOTTOM = 46;
    private static final int MODE_X = 8, MODE_Y = 106, MODE_W = 16, CONTROL_H = 11;
    private static final int DENATURE_X = 116, DENATURE_Y = 107, DENATURE_W = 52;
    private static final int OFFER_X = 8, OFFER_Y = 124, OFFER_W = 188;
    private BloodVolumeBarWidget.Bounds bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;
    private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer();

    public EnzymaticScriptoriumScreen(EnzymaticScriptoriumMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 216;
        imageHeight = TOP + InventoryPanelTextures.BLOODY.height();
        inventoryLabelY = TOP;
    }

    @Override
    protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        veins.render(g, x, y, imageWidth, TOP);
        g.renderOutline(x, y, imageWidth, TOP, OUTER);
        g.renderOutline(x + 1, y + 1, imageWidth - 2, TOP - 2, INNER);
        InventoryPanelTextures.blit(g, InventoryPanelTextures.BLOODY,
                x + (imageWidth - InventoryPanelTextures.BLOODY.width()) / 2, y + TOP);
        bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;
        float time = 0;
        if (minecraft != null && minecraft.player != null) {
            time = AnimationTime.age(minecraft.player, partialTick) / 20.0F;
            bloodBarBounds = BloodVolumeBarWidget.render(g, x + 200, y + 22, 10, TOP - 29,
                    menu.reading(), time, OUTER, INNER);
        }

        int fitting = fittingColor();
        int hubX = x + HUB_X, hubY = y + HUB_Y;
        int doses = 0;
        for (int i = 0; i < 8; i++) {
            var slot = menu.slots.get(i);
            int jx = x + slot.x + 8, jy = y + slot.y + 8;
            int color = temperColor(i);
            int selected = menu.selected(i);
            doses += selected;
            line(g, jx, jy, hubX, hubY, 3, IRON);
            line(g, jx, jy, hubX, hubY, 1, selected > 0 ? dim(color, 0xA0) : GLASS);
            if (selected > 0)
                pulses(g, jx, jy, hubX, hubY, time * 0.7F + i * 0.13F, selected, color);
        }
        int mix = mixedColor();
        int riserX = hubX, riserFoot = y + RISER_BOTTOM;
        line(g, hubX, hubY, riserX, riserFoot, 3, IRON);
        line(g, hubX, hubY, riserX, riserFoot, 1, doses > 0 ? dim(mix, 0xA0) : GLASS);
        if (doses > 0)
            pulses(g, hubX, hubY - 12, riserX, riserFoot, time * 0.7F, doses, mix);
        drawRiser(g, riserX, y, doses, mix, fitting);
        ScreenDrawUtils.drawCircleTrace(g, hubX, hubY, 13, 3, IRON);
        ScreenDrawUtils.drawCircleTrace(g, hubX, hubY, 13, 1, fitting);
        drawBook(g, x + EnzymaticScriptoriumMenu.ITEM_SLOT[0], y + EnzymaticScriptoriumMenu.ITEM_SLOT[1], doses, mix, time);

        for (int i = 0; i < 8; i++) {
            var slot = menu.slots.get(i);
            drawJar(g, x + slot.x, y + slot.y, i, fitting);
        }

        var lapis = menu.slots.get(EnzymaticScriptoriumBlockEntity.LAPIS);
        drawSlot(g, x + lapis.x, y + lapis.y, 0xFF3F57B5);
        if (!lapis.hasItem()) icon(g, SLOT_ICONS[1], x + lapis.x, y + lapis.y, 16);
        var shard = menu.slots.get(EnzymaticScriptoriumBlockEntity.SHARD);
        int cx = x + shard.x, cy = y + shard.y;
        g.fill(hubX - 1, hubY + 13, hubX + 2, cy, IRON);
        g.fill(cx - 3, cy + 5, cx - 1, cy + 18, IRON);
        g.fill(cx + 17, cy + 5, cx + 19, cy + 18, IRON);
        g.fill(cx - 3, cy + 17, cx + 19, cy + 19, IRON);
        g.fill(cx - 3, cy + 5, cx - 1, cy + 6, fitting);
        g.fill(cx + 17, cy + 5, cx + 19, cy + 6, fitting);
        drawSlot(g, cx, cy, 0xFF745353);
        if (!shard.hasItem()) icon(g, SLOT_ICONS[2], cx, cy, 16);

        for (int i = 0; i < 3; i++) {
            int bx = x + MODE_X + i * (MODE_W + 1);
            drawControl(g, bx, y + MODE_Y, MODE_W, CONTROL_H, menu.mode() == i, modeAvailable(i),
                    inside(mouseX, mouseY, bx, y + MODE_Y, MODE_W, CONTROL_H));
        }
        var provenance = menu.slots.get(EnzymaticScriptoriumBlockEntity.ITEM).getItem().get(DataComponentInit.SCRIPTORIUM_PROVENANCE.get());
        boolean cursed = provenance != null && !provenance.curse().isBlank();
        drawControl(g, x + DENATURE_X, y + DENATURE_Y, DENATURE_W, CONTROL_H, false, cursed,
                inside(mouseX, mouseY, x + DENATURE_X, y + DENATURE_Y, DENATURE_W, CONTROL_H));

        for (int i = 0; i < 3; i++) {
            int rowY = y + OFFER_Y + i * 12;
            boolean available = menu.offerCost(i) > 0 && menu.hasOfferEnzymes(i);
            drawControl(g, x + OFFER_X, rowY, OFFER_W, CONTROL_H, false, available,
                    inside(mouseX, mouseY, x + OFFER_X, rowY, OFFER_W, CONTROL_H));
            icon(g, INSCRIBE_ICON, x + OFFER_X + 3, rowY + 1, 9);
        }
    }

    /** A jar on its post: lid, glass walls, the stored level behind the item, and its three dose pips. */
    private void drawJar(GuiGraphics g, int sx, int sy, int temper, int fitting) {
        var slot = menu.slots.get(temper);
        int color = temperColor(temper);
        g.fill(sx - 2, sy - 1, sx + 18, sy + 18, IRON);
        g.fill(sx - 1, sy, sx + 17, sy + 17, GLASS);
        drawSlot(g, sx, sy, color);
        int stored = slot.getItem().getCount();
        int level = stored <= 0 ? 0 : Math.max(1, Math.round(14.0F * stored / 64.0F));
        if (level > 0) g.fill(sx + 1, sy + 15 - level, sx + 15, sy + 15, dim(color, 0x48));
        g.fill(sx - 2, sy - 4, sx + 18, sy - 1, menu.tier() >= 5 ? fitting : 0xFF5E5454);
        g.fill(sx + 2, sy - 5, sx + 14, sy - 4, menu.tier() >= 5 ? fitting : 0xFF4A4242);
        if (slot.getItem().isEmpty())
            icon(g, TEMPER_ICONS[temper], sx, sy, 16);
        int selected = menu.selected(temper);
        for (int pip = 0; pip < 3; pip++) {
            int px = sx + 2 + pip * 5;
            g.fill(px, sy + 20, px + 3, sy + 22,
                    pip < selected ? color : pip < stored ? 0xFF704C4C : 0xFF2A1515);
        }
    }

    /** The riser behind the book, filling with the merged feed; Palimpsest adds the caged bulb and bands. */
    private void drawRiser(GuiGraphics g, int rx, int y, int doses, int mix, int fitting) {
        int top = y + RISER_TOP, bottom = y + RISER_BOTTOM;
        if (menu.tier() >= 7) {
            ScreenDrawUtils.drawCircleTrace(g, rx, bottom + 4, 5, 3, IRON);
            g.fill(rx - 3, bottom + 1, rx + 4, bottom + 8, doses > 0 ? dim(mix, 0x90) : GLASS);
            ScreenDrawUtils.drawCircleTrace(g, rx, bottom + 4, 5, 1, fitting);
        }
        g.fill(rx - 4, top, rx + 5, bottom, IRON);
        g.fill(rx - 3, top + 1, rx + 4, bottom - 1, RECESS);
        int filled = Math.round((bottom - top - 2) * Math.min(3, doses) / 3.0F);
        if (filled > 0) g.fill(rx - 3, bottom - 1 - filled, rx + 4, bottom - 1, dim(mix, 0xB0));
        g.fill(rx - 2, top + 2, rx - 1, bottom - 2, 0x30FFFFFF);
        g.fill(rx - 6, top - 3, rx + 7, top, menu.tier() >= 5 ? fitting : IRON);
        if (menu.tier() >= 7)
            for (int band = top + 6; band < bottom - 2; band += 7)
                g.fill(rx - 5, band, rx + 6, band + 1, fitting);
    }

    /** The open grimoire on the hub, ruled in red once rubricated; the nib drips the merged feed onto its spine. */
    private void drawBook(GuiGraphics g, int sx, int sy, int doses, int mix, float time) {
        g.fill(sx - 10, sy - 2, sx + 26, sy + 18, 0xFF1A0A08);
        g.fill(sx - 9, sy - 1, sx + 7, sy + 17, 0xFF4A2C22);
        g.fill(sx + 9, sy - 1, sx + 25, sy + 17, 0xFF4A2C22);
        int rule = menu.tier() >= 5 ? 0xFF8A2020 : 0xFF5E3A2E;
        for (int line = sy + 2; line < sy + 16; line += 3) {
            g.fill(sx - 7, line, sx - 1, line + 1, rule);
            g.fill(sx + 17, line, sx + 23, line + 1, rule);
        }
        drawSlot(g, sx, sy, 0xFFBD4545);
        if (!menu.slots.get(EnzymaticScriptoriumBlockEntity.ITEM).hasItem())
            icon(g, SLOT_ICONS[0], sx, sy, 16);
        int nibX = sx + 8;
        g.fill(nibX - 1, sy - 6, nibX + 2, sy - 3, IRON);
        g.fill(nibX, sy - 3, nibX + 1, sy - 2, IRON);
        if (doses > 0) {
            float fall = (time * 1.4F) % 1.0F;
            int dropY = sy - 2 + Math.round(fall * 5);
            g.fill(nibX, dropY, nibX + 1, dropY + 2, dim(mix, Math.round(0xE0 * (1 - fall))));
        }
    }

    private boolean modeAvailable(int mode) {
        return mode == 0 || menu.tier() >= (mode == 1 ? 5 : 7) && menu.playerDegree() >= (mode == 1 ? 5 : 7);
    }

    /** Plain iron on the base stage, lacquer once rubricated, bone once it scrapes back. */
    private int fittingColor() {
        return menu.tier() >= 7 ? 0xFFCFC2A2 : menu.tier() >= 5 ? 0xFF8A1E22 : 0xFF6A4A44;
    }

    private static int temperColor(int temper) {
        return 0xFF000000 | EnumBloodTemper.values()[temper].getColor().getColor();
    }

    /** The selected doses' colours blended by weight, as the feed reaches the riser. */
    private int mixedColor() {
        int r = 0, gr = 0, b = 0, total = 0;
        for (int i = 0; i < 8; i++) {
            int weight = menu.selected(i);
            if (weight <= 0) continue;
            int c = temperColor(i);
            r += (c >> 16 & 0xFF) * weight;
            gr += (c >> 8 & 0xFF) * weight;
            b += (c & 0xFF) * weight;
            total += weight;
        }
        return total == 0 ? 0xFF6A1010 : 0xFF000000 | r / total << 16 | gr / total << 8 | b / total;
    }

    private static int dim(int color, int alpha) {
        return alpha << 24 | color & 0xFFFFFF;
    }

    private static void line(GuiGraphics g, int x0, int y0, int x1, int y1, int thickness, int color) {
        ScreenDrawUtils.drawCubicTrace(g, x0, y0, x0 + (x1 - x0) / 3, y0 + (y1 - y0) / 3,
                x0 + (x1 - x0) * 2 / 3, y0 + (y1 - y0) * 2 / 3, x1, y1, thickness, color);
    }

    /** One bead per selected dose, travelling the capillary toward the hub. */
    private static void pulses(GuiGraphics g, int x0, int y0, int x1, int y1, float phase, int count, int color) {
        for (int k = 0; k < count; k++) {
            float t = (phase + k / (float) count) % 1.0F;
            int px = Math.round(x0 + (x1 - x0) * t), py = Math.round(y0 + (y1 - y0) * t);
            g.fill(px - 1, py - 1, px + 1, py + 1, color);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        smallText(g, title, 8, 7, 0xFFE8A6A6, 0.75F);
        smallText(g, Component.literal("D" + menu.tier()), 186, 8, 0xFFCC7777, 0.65F);

        for (int i = 0; i < 3; i++) {
            Component text = Component.translatable("screen.hemomancy.scriptorium.mode." + i);
            float centre = MODE_X + i * (MODE_W + 1) + MODE_W / 2.0F;
            smallText(g, text, centre - font.width(text) * 0.3F, MODE_Y + 3,
                    i == menu.mode() ? 0xFFFFCCCC : modeAvailable(i) ? PALE : MUTED, 0.6F);
        }
        icon(g, DENATURE_ICON, DENATURE_X + 3, DENATURE_Y + 1, 9);
        smallText(g, Component.translatable("screen.hemomancy.scriptorium.denature"),
                DENATURE_X + 15, DENATURE_Y + 3, PALE, 0.65F);
        for (int i = 0; i < 3; i++) {
            int rowY = OFFER_Y + 3 + i * 12;
            int cost = menu.offerCost(i);
            smallText(g, Component.translatable("screen.hemomancy.scriptorium.offer", i + 1),
                    OFFER_X + 17, rowY, cost > 0 ? PALE : MUTED, 0.65F);
            String price = cost <= 0 ? "--" : cost + " XP  /  " + EnzymaticScriptoriumBlockEntity.inscriptionBlood(i) + " mL";
            smallText(g, Component.literal(price), OFFER_X + OFFER_W - 5 - font.width(price) * 0.65F,
                    rowY, cost > 0 ? 0xFFE9A3A3 : MUTED, 0.65F);
        }
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
        int sourceSize = texture.equals(TEMPER_ICONS[3]) ? 18 : 16;
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

    private boolean overPips(double mouseX, double mouseY, int temper) {
        var slot = menu.slots.get(temper);
        return inside(mouseX, mouseY, leftPos + slot.x, topPos + slot.y + 18, 16, 6);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < 8; i++) {
                if (overPips(mouseX, mouseY, i)) {
                    send(i);
                    return true;
                }
            }
            for (int i = 0; i < 3; i++) {
                if (inside(mouseX, mouseY, leftPos + MODE_X + i * (MODE_W + 1), topPos + MODE_Y, MODE_W, CONTROL_H)) {
                    send(20 + i);
                    return true;
                }
                if (inside(mouseX, mouseY, leftPos + OFFER_X, topPos + OFFER_Y + i * 12, OFFER_W, CONTROL_H)) {
                    send(8 + i);
                    return true;
                }
            }
            if (inside(mouseX, mouseY, leftPos + DENATURE_X, topPos + DENATURE_Y, DENATURE_W, CONTROL_H)) {
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

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        super.render(g, mouseX, mouseY, partialTick);
        renderTooltip(g, mouseX, mouseY);
        if (minecraft != null && minecraft.player != null && bloodBarBounds.contains(mouseX, mouseY)) {
            BloodVolumeBarWidget.renderTooltip(g, font, bloodBarBounds,
                    menu.reading(), mouseX, mouseY);
            return;
        }
        for (int i = 0; i < 11; i++) {
            var slot = menu.slots.get(i);
            if (!slot.getItem().isEmpty() || !inside(mouseX, mouseY, leftPos + slot.x, topPos + slot.y, 16, 16))
                continue;
            Component hint = i < 8 ? EnumBloodTemper.getRepEnzyme(EnumBloodTemper.values()[i]).getDescription()
                    : Component.translatable("screen.hemomancy.scriptorium." + (i == 8 ? "item" : i == 9 ? "lapis" : "shard"));
            g.renderTooltip(font, hint, mouseX, mouseY);
            return;
        }
        for (int i = 0; i < 8; i++) {
            if (overPips(mouseX, mouseY, i)) {
                g.renderTooltip(font, List.of(
                        EnumBloodTemper.getRepEnzyme(EnumBloodTemper.values()[i]).getDescription(),
                        Component.translatable("screen.hemomancy.scriptorium.selection",
                                menu.selected(i), menu.slots.get(i).getItem().getCount())), Optional.empty(), mouseX, mouseY);
                return;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (inside(mouseX, mouseY, leftPos + MODE_X + i * (MODE_W + 1), topPos + MODE_Y, MODE_W, CONTROL_H)) {
                g.renderTooltip(font, Component.translatable("screen.hemomancy.scriptorium.mode." + i + ".hint"), mouseX, mouseY);
                return;
            }
        }
        for (int i = 0; i < 3; i++) {
            if (!inside(mouseX, mouseY, leftPos + OFFER_X, topPos + OFFER_Y + i * 12, OFFER_W, CONTROL_H)) continue;
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.translatable("screen.hemomancy.scriptorium.offer", i + 1));
            if (menu.offerCost(i) > 0)
                tooltip.add(Component.translatable("screen.hemomancy.scriptorium.cost",
                        menu.offerCost(i), i + 1, EnzymaticScriptoriumBlockEntity.inscriptionBlood(i)));
            if (menu.offerCost(i) > 0) {
                tooltip.add(Component.translatable("screen.hemomancy.scriptorium.enzymes"));
                for (int temper = 0; temper < 8; temper++) {
                    int required = menu.enzymeCost(i, temper);
                    if (required == 0) continue;
                    int stored = menu.slots.get(temper).getItem().getCount();
                    tooltip.add(Component.translatable("screen.hemomancy.scriptorium.enzyme_cost",
                                    required, EnumBloodTemper.getRepEnzyme(EnumBloodTemper.values()[temper]).getDescription(), stored)
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
