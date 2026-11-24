package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.brewing.AlembicTier;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.GhastlyAlembicMenu;
import com.vincenthuto.hemomancy.common.tile.ReservoirReading;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.GhastlyAlembicBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

/**
 * Ghastly Alembic screen, drawn as the still itself: the octagonal glass cucurbit (the blood tank, with the charge
 * inside it) sits in its sand-bath over the fire, the bone still head caps it, and the head's beak runs down to the
 * receiving vial that holds the result. Progress is distillate creeping along the beak. Catalysts sit on the retort
 * stand's dosing shelf, and the vessel slots hang off a tap at the cucurbit's foot. The Condenser threads the beak
 * through its worm tub; the Athanor sets the still on its brick hearth.
 */
public class GhastlyAlembicScreen extends AbstractContainerScreen<GhastlyAlembicMenu> {

    // ── Colours ──
    private static final int SLOT_BG = 0xFF1A0808;
    private static final int SLOT_BORDER_DARK = 0xFF0D0303;
    private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
    private static final int BORDER_OUTER = 0xFF330808;
    private static final int BORDER_INNER = 0xFF220606;

    private static final int IRON_DARK = 0xFF16101A;
    private static final int IRON = 0xFF2C242E;
    private static final int IRON_LIGHT = 0xFF4A3F4C;
    private static final int BONE_LIGHT = 0xFFEDE6D2;
    private static final int BONE = 0xFFD6CDB6;
    private static final int BONE_SHADE = 0xFFADA28A;
    private static final int BONE_DARK = 0xFF7E745F;
    private static final int STOPPER = 0xFF8E1012;
    private static final int GLASS_EDGE = 0x80C9B9C2;
    private static final int GLASS_TINT = 0x18FFE0E0;
    private static final int SAND = 0xFF5E3A2A;
    private static final int WOOD = 0xFF4A2A1C;
    private static final int WOOD_DARK = 0xFF2E1910;
    private static final int BRICK = 0xFF4A1418;
    private static final int BRICK_DARK = 0xFF260A0D;

    /** Height of the band the still is drawn in; the player inventory panel sits below it. */
    private static final int CRAFT_AREA_HEIGHT = 114;

    // ── The still's anchors (relative to the screen's corner) ──
    private static final int STILL_X = 74;
    private static final int TANK_X = 58, TANK_Y = 40, TANK_W = 32, TANK_H = 40;
    private static final int BEAK_X0 = 84, BEAK_Y0 = 22, BEAK_X1 = 137, BEAK_Y1 = 34, BEAK_DROP_END = 44;
    private static final int TUB_X = 100, TUB_Y = 20, TUB_W = 22, TUB_H = 28;
    private static final int STAND_X = 46;

    final GhastlyAlembicBlockEntity te;
    private static final VeinBackgroundRenderer.Style VEINS = new VeinBackgroundRenderer.Style(
            VeinBackgroundRenderer.Field.STATION, VeinBackgroundRenderer.Palette.MACHINE,
            VeinBackgroundRenderer.Speckles.Scattered.MACHINE.seed(54321L).count(60));
    private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VEINS);

    private BloodVolumeBarWidget.Bounds bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;
    private float animTime = 0f;

    public GhastlyAlembicScreen(GhastlyAlembicMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.te = menu.getTe();
        this.imageWidth = 176;
        this.imageHeight = GhastlyAlembicMenu.CRAFT_AREA_HEIGHT + 14 + 82;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (this.hoveredSlot != null && this.hoveredSlot.index == GhastlyAlembicMenu.FLASK_SLOT) {
            graphics.renderTooltip(font, List.of(
                    Component.translatable("container.hemomancy.alembic.containers"),
                    Component.translatable("container.hemomancy.alembic.containers.transfer"),
                    Component.translatable("container.hemomancy.alembic.containers.tincture")
            ), Optional.empty(), mouseX, mouseY);
        }
        if (this.hoveredSlot != null
                && this.hoveredSlot.index == GhastlyAlembicMenu.FLASK_OUTPUT_SLOT
                && !this.hoveredSlot.hasItem()) {
            graphics.renderTooltip(font, List.of(
                    Component.translatable("screen.hemomancy.ghastly_alembic.container_output"),
                    Component.translatable("screen.hemomancy.ghastly_alembic.place_a_blood_gourd_here_to")
            ), Optional.empty(), mouseX, mouseY);
        }
        if (this.hoveredSlot != null && this.hoveredSlot.index == GhastlyAlembicMenu.RESULT_SLOT
                && !this.hoveredSlot.hasItem() && !te.advancedPreview().isEmpty())
            graphics.renderTooltip(font, te.advancedPreview(), mouseX, mouseY);
        // The beak carries the distillate, so it answers what an advanced operation will cost.
        if (this.hoveredSlot == null && overBeak(mouseX - leftPos, mouseY - topPos)
                && !te.advancedPreview().isEmpty())
            graphics.renderTooltip(font, List.of(
                    Component.translatable("container.hemomancy.alembic.advanced.cost", te.advancedBloodCost()),
                    Component.translatable("container.hemomancy.alembic.advanced.time", te.advancedTotalTicks())),
                    Optional.empty(), mouseX, mouseY);

        // The charge sits inside the cucurbit; its own tooltip wins there.
        if (this.hoveredSlot == null)
            BloodVolumeBarWidget.renderTooltip(graphics, font, bloodBarBounds, ReservoirReading.of(te), mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        int gx = this.leftPos;
        int gy = this.topPos;
        AlembicTier tier = te.tier();
        animTime += 0.016f; // ~60 FPS approximation

        veins.draw(gfx, gx, gy, this.imageWidth, CRAFT_AREA_HEIGHT, animTime);
        ScreenDrawUtils.drawBorder(gfx, gx, gy, this.imageWidth, CRAFT_AREA_HEIGHT, BORDER_OUTER, BORDER_INNER);

        Slot firstInventorySlot = this.menu.slots.get(GhastlyAlembicMenu.SLOT_COUNT);
        InventoryPanelTextures.blit(gfx, InventoryPanelTextures.BLOODY,
                gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

        // Back to front: stand, heat, cucurbit, head, beak (and tub), receiver, tap, then the slot wells.
        drawRetortStand(gfx, gx, gy, tier);
        if (tier == AlembicTier.ATHANOR) drawHearth(gfx, gx, gy);
        else drawTrivetAndFlame(gfx, gx, gy);
        drawSandBath(gfx, gx, gy);
        drawCucurbit(gfx, gx, gy);
        drawStillHead(gfx, gx, gy);
        drawBeak(gfx, gx, gy, tier);
        drawReceiver(gfx, gx, gy);
        drawTap(gfx, gx, gy);

        for (int i = 0; i < GhastlyAlembicMenu.SLOT_COUNT; i++) {
            if (i == GhastlyAlembicMenu.CATALYST_2_SLOT && !tier.hasSecondCatalyst()) continue;
            Slot slot = this.menu.slots.get(i);
            drawSlotBackground(gfx, gx + slot.x, gy + slot.y, slot.index);
        }
        if (menu.slots.get(GhastlyAlembicMenu.RESULT_SLOT).getItem().isEmpty()
                && !te.advancedPreview().isEmpty()) {
            Slot result = menu.slots.get(GhastlyAlembicMenu.RESULT_SLOT);
            gfx.renderFakeItem(te.advancedPreview(), gx + result.x, gy + result.y);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // The tier names the still, so the title reads Ghastly Alembic, Hematic Condenser or Sanguine Athanor.
        Component name = Component.translatable("container.hemomancy.alembic.tier."
                + te.tier().name().toLowerCase(Locale.ROOT));
        gfx.drawString(font, name, this.imageWidth - 6 - font.width(name), 5, 0xFFAA2222, false);

        Component status = Component.translatable("container.hemomancy.alembic.status."
                + menu.getProcessingStatus().name().toLowerCase(Locale.ROOT));
        gfx.drawString(font, status, this.imageWidth - 6 - font.width(status), 84, 0xFFCC4400, false);

        if (!te.advancedFeedback().isEmpty()) {
            Component feedback = Component.translatable("container.hemomancy.alembic.advanced."
                    + te.advancedFeedback());
            gfx.drawString(font, feedback, (this.imageWidth - font.width(feedback)) / 2, 104, 0xFFD49372, false);
        }
    }

    // ───── Slot wells ─────

    private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, int slotIndex) {
        ScreenDrawUtils.drawSlotWell(gfx, sx, sy, SLOT_BORDER_DARK, SLOT_BG, SLOT_BORDER_LIGHT);
        if (slotIndex == GhastlyAlembicMenu.RESULT_SLOT || slotIndex == GhastlyAlembicMenu.FLASK_OUTPUT_SLOT)
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
        else if (slotIndex == GhastlyAlembicMenu.FLASK_SLOT)
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x20AA0000);
        else if (slotIndex == GhastlyAlembicMenu.INGREDIENT_SLOT)
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x15FFAA88);
    }

    // ───── The still ─────

    /** Iron retort stand behind the still: a rod with a clamp on the neck and a dosing shelf for the catalysts. */
    private void drawRetortStand(GuiGraphics gfx, int gx, int gy, AlembicTier tier) {
        int x = gx + STAND_X;
        gfx.fill(x, gy + 16, x + 2, gy + 100, IRON);
        gfx.fill(x, gy + 16, x + 1, gy + 100, IRON_LIGHT);
        gfx.fill(x - 6, gy + 100, x + 8, gy + 103, IRON_DARK);
        gfx.fill(x - 6, gy + 100, x + 8, gy + 101, IRON);
        // Clamp arm to the cucurbit's neck.
        gfx.fill(x + 2, gy + 33, gx + STILL_X - 5, gy + 35, IRON);
        gfx.fill(x + 2, gy + 33, gx + STILL_X - 5, gy + 34, IRON_LIGHT);
        gfx.fill(x - 1, gy + 32, x + 3, gy + 36, IRON_DARK);
        // Dosing shelf under the catalysts.
        gfx.fill(gx + 6, gy + 40, x, gy + 42, IRON);
        gfx.fill(gx + 6, gy + 40, x, gy + 41, IRON_LIGHT);
        gfx.fill(gx + 6, gy + 42, gx + 8, gy + 45, IRON_DARK);
        if (!tier.hasSecondCatalyst()) {
            // The Athanor's second catalyst hangs here; until then an empty hook.
            int hx = gx + 34;
            gfx.fill(hx, gy + 32, hx + 1, gy + 40, IRON_LIGHT);
            gfx.fill(hx - 2, gy + 30, hx + 1, gy + 31, IRON_LIGHT);
            gfx.fill(hx - 2, gy + 30, hx - 1, gy + 33, IRON_LIGHT);
        }
    }

    /** Sand-bath pan the cucurbit is seated in. */
    private void drawSandBath(GuiGraphics gfx, int gx, int gy) {
        int x0 = gx + 52, x1 = gx + 96, y = gy + 82;
        gfx.fill(x0, y, x1, y + 5, IRON_DARK);
        gfx.fill(x0, y, x1, y + 1, IRON_LIGHT);
        gfx.fill(x0 + 1, y + 1, x1 - 1, y + 3, SAND);
        gfx.fill(x0 + 2, y + 5, x1 - 2, y + 6, IRON_DARK);
    }

    /** Clawed trivet with the fire below the pan (base and Condenser need heat from below). */
    private void drawTrivetAndFlame(GuiGraphics gfx, int gx, int gy) {
        for (int legX : new int[]{55, 90}) {
            int lx = gx + legX;
            gfx.fill(lx, gy + 88, lx + 3, gy + 97, IRON);
            gfx.fill(lx, gy + 88, lx + 1, gy + 97, IRON_LIGHT);
            gfx.fill(lx - 3, gy + 97, lx + 6, gy + 99, IRON_DARK);
            gfx.fill(lx - 3, gy + 99, lx - 2, gy + 100, IRON_DARK);
            gfx.fill(lx + 5, gy + 99, lx + 6, gy + 100, IRON_DARK);
        }
        drawFlame(gfx, gx + 64, gy + 88, 20, 12, menu.isHeated());
    }

    /** The Athanor's iron-clad brick hearth: its barred firebox always glows. */
    private void drawHearth(GuiGraphics gfx, int gx, int gy) {
        int x0 = gx + 50, x1 = gx + 98, y0 = gy + 88, y1 = gy + 103;
        gfx.fill(x0, y0, x1, y1, BRICK);
        for (int row = 0; row < (y1 - y0) / 4; row++) {
            int ry = y0 + row * 4 + 3;
            gfx.fill(x0, ry, x1, ry + 1, BRICK_DARK);
            int offset = row % 2 == 0 ? 0 : 4;
            for (int bx = x0 + offset; bx < x1; bx += 8)
                gfx.fill(bx, ry - 3, bx + 1, ry, BRICK_DARK);
        }
        ScreenDrawUtils.drawBevelBorder(gfx, x0, y0, x1 - x0, y1 - y0, IRON_LIGHT, IRON_DARK);
        int fx = gx + 64, fy = y0 + 2;
        gfx.fill(fx - 1, fy - 1, fx + 21, fy + 12, IRON_DARK);
        gfx.fill(fx, fy, fx + 20, fy + 11, 0xFF120405);
        drawFlame(gfx, fx, fy, 20, 11, true);
        for (int bar = 2; bar < 20; bar += 4)
            gfx.fill(fx + bar, fy, fx + bar + 1, fy + 11, IRON);
    }

    private void drawFlame(GuiGraphics gfx, int flameX, int flameY, int flameW, int flameH, boolean lit) {
        float time = animTime;
        if (!lit) {
            // Cold: dim ash.
            Random r = new Random(999L);
            for (int i = 0; i < 8; i++) {
                int dx = r.nextInt(flameW);
                int dy = r.nextInt(flameH / 2) + flameH / 2;
                gfx.fill(flameX + dx, flameY + dy, flameX + dx + 1, flameY + dy + 1, 0x30666666);
            }
            return;
        }
        for (int row = 0; row < flameH; row++) {
            float rowT = (float) row / flameH; // 0 at the top, 1 at the bottom
            float flicker = 0.7f + 0.3f * Mth.sin(time * 8f + row * 0.5f);
            for (int col = 0; col < flameW; col++) {
                float center = flameW / 2f;
                float dist = Math.abs(col - center) / center;
                float taper = 1f - dist * (1f - rowT * 0.6f);
                if (taper < 0.2f) continue;
                float wave = Mth.sin(time * 6f + col * 0.7f + row * 0.3f) * 0.15f;
                float alpha = Mth.clamp(rowT * taper * flicker + wave, 0, 1);
                int r = (int) (255 * alpha);
                int g = (int) Mth.clamp(180 * rowT * alpha, 0, 255);
                int b = (int) (20 * alpha);
                int a = (int) (220 * alpha);
                if (a < 10) continue;
                int px = flameX + col;
                int py = flameY + row;
                gfx.fill(px, py, px + 1, py + 1, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    /** The glass cucurbit is the tank: an octagonal column of blood, hooped in iron, with the charge inside. */
    private void drawCucurbit(GuiGraphics gfx, int gx, int gy) {
        int x = gx + TANK_X, y = gy + TANK_Y;
        bloodBarBounds = BloodVolumeBarWidget.render(gfx, x, y, TANK_W, TANK_H,
                ReservoirReading.of(te), animTime, IRON_DARK, IRON);
        // Chamfer the corners so the column reads octagonal, like the model's.
        int x0 = x - 2, x1 = x + TANK_W + 2, y0 = y - 2, y1 = y + TANK_H + 2;
        for (int i = 0; i < 3; i++) {
            int len = 3 - i;
            gfx.fill(x0, y0 + i, x0 + len, y0 + i + 1, IRON_DARK);
            gfx.fill(x1 - len, y0 + i, x1, y0 + i + 1, IRON_DARK);
            gfx.fill(x0, y1 - 1 - i, x0 + len, y1 - i, IRON_DARK);
            gfx.fill(x1 - len, y1 - 1 - i, x1, y1 - i, IRON_DARK);
        }
        // Pale glass over the column so an empty tank still reads as glass, with a glint down each wall.
        gfx.fill(x, y, x + TANK_W, y + TANK_H, GLASS_TINT);
        gfx.fill(x + 1, y + 2, x + 2, y + TANK_H - 2, 0x40FFFFFF);
        gfx.fill(x + TANK_W - 3, y + 4, x + TANK_W - 2, y + TANK_H - 6, 0x20FFFFFF);
        // Iron hoops.
        for (int hoopY : new int[]{y + 5, y + TANK_H - 6}) {
            gfx.fill(x0, hoopY, x1, hoopY + 2, IRON);
            gfx.fill(x0, hoopY, x1, hoopY + 1, IRON_LIGHT);
        }
        // Neck up to the head, with its iron collar.
        int nx = gx + STILL_X - 5;
        gfx.fill(nx, gy + 31, nx + 10, y0, GLASS_TINT);
        gfx.fill(nx, gy + 31, nx + 1, y0, GLASS_EDGE);
        gfx.fill(nx + 9, gy + 31, nx + 10, y0, GLASS_EDGE);
        gfx.fill(nx - 1, gy + 31, nx + 11, gy + 33, IRON);
    }

    /** The ghast-pale bone still head, weeping blood from its front facet, stoppered in red. */
    private void drawStillHead(GuiGraphics gfx, int gx, int gy) {
        int cx = gx + STILL_X;
        gfx.fill(cx - 1, gy + 13, cx + 1, gy + 16, BONE_LIGHT);
        gfx.fill(cx - 3, gy + 16, cx + 3, gy + 18, STOPPER);
        gfx.fill(cx - 3, gy + 17, cx + 3, gy + 18, 0xFF5A080A);
        int[][] rows = {{18, 20, 6}, {20, 23, 8}, {23, 29, 10}, {29, 31, 8}};
        for (int[] row : rows) {
            int half = row[2];
            gfx.fill(cx - half, gy + row[0], cx + half, gy + row[1], BONE);
            gfx.fill(cx - half, gy + row[0], cx - half + 2, gy + row[1], BONE_LIGHT);
            gfx.fill(cx + half - 3, gy + row[0], cx + half, gy + row[1], BONE_SHADE);
        }
        gfx.fill(cx - 8, gy + 29, cx + 8, gy + 31, BONE_DARK);
        // The face's dark sockets and the blood tears beneath them.
        float weep = 0.5f + 0.5f * Mth.sin(animTime * 1.3f);
        for (int ex : new int[]{cx - 5, cx + 3}) {
            gfx.fill(ex, gy + 23, ex + 2, gy + 25, 0xFF1A0E0C);
            gfx.fill(ex, gy + 25, ex + 1, gy + 27 + (int) (2 * weep), 0xFF9A1414);
        }
    }

    /**
     * The beak, from the head down to the receiving vial: an iron-sheathed pipe whose channel fills with distillate as
     * the work goes on, then a drop falls into the vial. The Condenser and Athanor run it through the worm tub.
     */
    private void drawBeak(GuiGraphics gfx, int gx, int gy, AlembicTier tier) {
        double progress = Mth.clamp(this.menu.getBurnProgress() / 24.0, 0, 1);
        boolean working = progress > 0;
        int run = BEAK_X1 - BEAK_X0;
        int drop = BEAK_DROP_END - BEAK_Y1;
        int length = run + drop;
        int filled = (int) Math.round(length * progress);

        for (int i = 0; i < run; i++) {
            int px = gx + BEAK_X0 + i;
            int py = gy + beakY(i);
            gfx.fill(px, py, px + 1, py + 3, IRON);
            gfx.fill(px, py, px + 1, py + 1, IRON_LIGHT);
            gfx.fill(px, py + 1, px + 1, py + 2, i < filled ? distillate(i) : 0xFF0C0405);
        }
        int dx = gx + BEAK_X1;
        for (int j = 0; j < drop; j++) {
            int py = gy + BEAK_Y1 + j;
            gfx.fill(dx - 1, py, dx + 2, py + 1, IRON);
            gfx.fill(dx - 1, py, dx, py + 1, IRON_LIGHT);
            gfx.fill(dx, py, dx + 1, py + 1, run + j < filled ? distillate(run + j) : 0xFF0C0405);
        }
        // A bright bead at the head of the distillate.
        if (working && filled < length) {
            float edge = 0.5f + 0.5f * Mth.sin(animTime * 6f);
            int bead = ((int) (180 * edge) << 24) | 0xFF4020;
            if (filled < run) {
                int px = gx + BEAK_X0 + filled;
                gfx.fill(px, gy + beakY(filled) + 1, px + 1, gy + beakY(filled) + 2, bead);
            } else {
                gfx.fill(dx, gy + BEAK_Y1 + filled - run, dx + 1, gy + BEAK_Y1 + filled - run + 1, bead);
            }
        }
        // Once the beak runs full, drops fall into the vial.
        if (working && filled >= length) {
            float fall = (animTime * 1.5f) % 1f;
            int fy = gy + BEAK_DROP_END + (int) (fall * 8);
            gfx.fill(dx, fy, dx + 1, fy + 2, 0xE0C01818);
        }
        // Vapour rising in the head while the still works.
        if (working) {
            for (int w = 0; w < 3; w++) {
                float t = (animTime * 0.8f + w / 3f) % 1f;
                int wx = gx + STILL_X - 3 + w * 3 + (int) (Mth.sin(animTime * 3f + w) * 1.5f);
                int wy = gy + 31 - (int) (t * 9);
                int a = (int) (90 * (1f - t));
                gfx.fill(wx, wy, wx + 1, wy + 1, (a << 24) | 0xF0D0D0);
            }
        }
        if (tier != AlembicTier.BASE) drawWormTub(gfx, gx, gy, working);
    }

    /** Height of the beak's top edge {@code i} pixels along its run. */
    private static int beakY(int i) {
        return BEAK_Y0 + i * (BEAK_Y1 - BEAK_Y0) / (BEAK_X1 - BEAK_X0);
    }

    private int distillate(int i) {
        float pulse = 0.75f + 0.25f * Mth.sin(animTime * 4f - i * 0.25f);
        int r = (int) (190 * pulse);
        int g = (int) (24 * pulse);
        int b = (int) (18 * pulse);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    /** The Condenser's iron-hooped blood-wood worm tub, which the beak passes through to cool. */
    private void drawWormTub(GuiGraphics gfx, int gx, int gy, boolean working) {
        int x = gx + TUB_X, y = gy + TUB_Y;
        gfx.fill(x, y, x + TUB_W, y + TUB_H, WOOD);
        for (int stave = 3; stave < TUB_W; stave += 4)
            gfx.fill(x + stave, y + 1, x + stave + 1, y + TUB_H - 1, WOOD_DARK);
        gfx.fill(x, y, x + 2, y + TUB_H, 0xFF5E3826);
        for (int hoopY : new int[]{y + 3, y + TUB_H - 5}) {
            gfx.fill(x - 1, hoopY, x + TUB_W + 1, hoopY + 2, IRON);
            gfx.fill(x - 1, hoopY, x + TUB_W + 1, hoopY + 1, IRON_LIGHT);
        }
        ScreenDrawUtils.drawBevelBorder(gfx, x, y, TUB_W, TUB_H, 0xFF6A4632, WOOD_DARK);
        // Dark water at the brim, rippling while the coil runs hot.
        gfx.fill(x + 1, y + 1, x + TUB_W - 1, y + 2, 0xFF1A1418);
        if (working) {
            int ripple = (int) ((animTime * 6f) % (TUB_W - 4));
            gfx.fill(x + 2 + ripple, y + 1, x + 4 + ripple, y + 2, 0x60D0C8D8);
        }
    }

    /** The receiving vial on its bone pedestal: the result sits inside it. */
    private void drawReceiver(GuiGraphics gfx, int gx, int gy) {
        Slot result = this.menu.slots.get(GhastlyAlembicMenu.RESULT_SLOT);
        int sx = gx + result.x, sy = gy + result.y;
        int x0 = sx - 4, x1 = sx + 20, y0 = sy - 6, y1 = sy + 20;
        int nx0 = gx + BEAK_X1 - 4, nx1 = gx + BEAK_X1 + 5;
        gfx.fill(nx0, gy + BEAK_DROP_END - 2, nx1, y0, GLASS_TINT);
        gfx.fill(nx0 - 1, gy + BEAK_DROP_END - 3, nx1 + 1, gy + BEAK_DROP_END - 2, GLASS_EDGE);
        gfx.fill(nx0, gy + BEAK_DROP_END - 2, nx0 + 1, y0, GLASS_EDGE);
        gfx.fill(nx1 - 1, gy + BEAK_DROP_END - 2, nx1, y0, GLASS_EDGE);
        gfx.fill(x0, y0, x1, y1, GLASS_TINT);
        ScreenDrawUtils.drawSimpleBorder(gfx, x0, y0, x1 - x0, y1 - y0, GLASS_EDGE);
        gfx.fill(x0 + 1, y0 + 2, x0 + 2, y1 - 3, 0x40FFFFFF);
        // Bone pedestal and foot.
        gfx.fill(x0 + 2, y1, x1 - 2, y1 + 3, BONE);
        gfx.fill(x0 + 2, y1, x1 - 2, y1 + 1, BONE_LIGHT);
        gfx.fill(x1 - 5, y1 + 1, x1 - 2, y1 + 3, BONE_SHADE);
        gfx.fill(x0 - 1, y1 + 3, x1 + 1, y1 + 5, BONE_DARK);
    }

    /** The tap at the cucurbit's foot: a valved spigot to the vessel slot, which hands its vessel on to the one below. */
    private void drawTap(GuiGraphics gfx, int gx, int gy) {
        Slot flask = this.menu.slots.get(GhastlyAlembicMenu.FLASK_SLOT);
        Slot out = this.menu.slots.get(GhastlyAlembicMenu.FLASK_OUTPUT_SLOT);
        int spigotY = gy + flask.y + 6;
        int x0 = gx + flask.x + 17, x1 = gx + TANK_X - 2;
        gfx.fill(x0, spigotY, x1, spigotY + 3, IRON);
        gfx.fill(x0, spigotY, x1, spigotY + 1, IRON_LIGHT);
        gfx.fill(x0 + 1, spigotY + 1, x1, spigotY + 2, 0xFF3A0C0C);
        // Valve wheel.
        int vx = x0 + 4;
        gfx.fill(vx, spigotY - 4, vx + 3, spigotY - 1, STOPPER);
        gfx.fill(vx + 1, spigotY - 1, vx + 2, spigotY, IRON_LIGHT);
        // Chevron from the vessel slot down to the container output.
        int cx = gx + flask.x + 8, cy = gy + flask.y + 18;
        int gap = out.y - flask.y - 18;
        for (int i = 0; i < Math.min(3, gap); i++)
            gfx.fill(cx - 3 + i, cy + i, cx + 3 - i, cy + i + 1, 0x90802020);
    }

    private static boolean overBeak(int x, int y) {
        if (x >= BEAK_X0 && x < BEAK_X1 + 2 && y >= BEAK_Y0 - 2 && y < BEAK_Y1 + 4) return true;
        return x >= BEAK_X1 - 2 && x < BEAK_X1 + 3 && y >= BEAK_Y1 && y < BEAK_DROP_END;
    }
}
