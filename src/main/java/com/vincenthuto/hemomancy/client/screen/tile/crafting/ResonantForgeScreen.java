package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.enchanting.ResonantForgeRules;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.item.harbinger.CylinderMedia;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.ResonantForgeMenu;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ResonantForgeBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The Resonant Forge drawn as the worker stands at it. The hammer bay is seen side-on at the left: the pattern drum
 * end-on at the back with its crank, pitman and treadle, the helve on its fulcrum, the head over the anvil and the
 * finished-work tray between them. The quench trough in the middle is the tank. The grinder bay is seen face-on at
 * the right: the stone on its trestles with the tool rest at its inner rim, and the recording drum above it, belted
 * to the stone, with the stylus on the wax. Frames take the stage's fittings: blood-wood with the spring pole,
 * hematic iron with the gallows, cistern and lead-screw carriage, then bone crowns, the caged heart and the horn.
 */
public class ResonantForgeScreen extends AbstractContainerScreen<ResonantForgeMenu> {
    private static final int OUTER = 0xFF330808;
    private static final int INNER = 0xFF220606;
    private static final int RECESS = 0xFF1A0808;
    private static final int PALE = 0xFFE7B8B8;
    private static final int MUTED = 0xFF9B6969;
    private static final int BONE = 0xFFCFC2A2;
    private static final int WAX = 0xFFC79A4A;
    private static final int WAX_BAND = 0xFF8A2A20;
    private static final int STONE = 0xFFA29C96;
    private static final int STONE_DARK = 0xFF6A6460;
    private static final int ARTERY = 0xFF9A1820;
    private static final int TOP = ResonantForgeMenu.INVENTORY_TOP;
    private static final ResourceLocation EQUIPMENT_ICON = iconTexture("equipment");
    private static final ResourceLocation ENCHANTED_ICON = iconTexture("enchanted_equipment");
    private static final ResourceLocation BLANK_CYLINDER_ICON = iconTexture("blank_cylinder");
    private static final ResourceLocation RECORDED_CYLINDER_ICON = iconTexture("recorded_cylinder");
    /** Indexed by forge slot: application item, cylinder, output, then grinding item, cylinder, outputs. */
    private static final ResourceLocation[] SLOT_ICONS = {
            EQUIPMENT_ICON, RECORDED_CYLINDER_ICON, ENCHANTED_ICON,
            ENCHANTED_ICON, BLANK_CYLINDER_ICON, EQUIPMENT_ICON, RECORDED_CYLINDER_ICON
    };
    private static final String[] SLOT_HINTS = {
            "application_item", "application_cylinder", "application_output",
            "grinding_item", "grinding_cylinder", "grinding_equipment_output", "grinding_cylinder_output"
    };

    // Hammer bay, side-on: back at the left, the worker at the right.
    private static final int SILL_Y = 118;
    private static final int DRUM_X = 24, DRUM_Y = 54, DRUM_R = 13;
    private static final int FULCRUM_X = 49, HELVE_Y = 42, HELVE_TAIL = 22, HELVE_HEAD = 84;
    private static final int TREADLE_X = 42, TREADLE_Y = 112, TREADLE_TAIL = 14;
    // Quench trough in the middle column.
    private static final int TROUGH_X = 108, TROUGH_Y = 84, TROUGH_W = 16, TROUGH_H = 32;
    // Grinder bay, face-on.
    private static final int STONE_X = 198, STONE_Y = 96, STONE_R = 17;
    private static final int RECORD_LEFT = 150, RECORD_RIGHT = 190, RECORD_TOP = 42, RECORD_BOTTOM = 62;
    // Controls.
    private static final int APPLY_X = 8, APPLY_W = 90;
    private static final int RECORD_X = 134, RECORD_W = 52;
    private static final int STABILIZE_X = 188, STABILIZE_W = 38;
    private static final int BUTTON_Y = 124, BUTTON_H = 11;
    private static final int SELECT_X = 134, SELECT_Y = 66, SELECT_W = 64, SELECT_H = 10;
    private static final int MASTER_X = 200, MASTER_W = 26;

    private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer();
    private BloodVolumeBarWidget.Bounds bloodBounds = BloodVolumeBarWidget.Bounds.EMPTY;

    public ResonantForgeScreen(ResonantForgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = ResonantForgeMenu.WIDTH;
        imageHeight = TOP + InventoryPanelTextures.BLOODY.height();
        inventoryLabelX = (imageWidth - InventoryPanelTextures.BLOODY.width()) / 2 + 5;
        inventoryLabelY = TOP;
    }

    @Override protected void renderBg(GuiGraphics g, float partialTick, int mouseX, int mouseY) {
        int x = leftPos, y = topPos;
        veins.render(g, x, y, imageWidth, TOP);
        g.renderOutline(x, y, imageWidth, TOP, OUTER);
        g.renderOutline(x + 1, y + 1, imageWidth - 2, TOP - 2, INNER);
        InventoryPanelTextures.blit(g, InventoryPanelTextures.BLOODY,
                x + (imageWidth - InventoryPanelTextures.BLOODY.width()) / 2, y + TOP);

        float ticks = minecraft == null || minecraft.player == null ? 0 : AnimationTime.age(minecraft.player, partialTick);
        int operation = menu.operation();
        boolean hammering = operation == 1;
        boolean grinding = operation == 2 || operation == 3;

        g.fill(x + 6, y + SILL_Y, x + imageWidth - 6, y + SILL_Y + 3, frameDark());
        g.fill(x + 6, y + SILL_Y, x + imageWidth - 6, y + SILL_Y + 1, frame());
        if (menu.tier() >= 2) g.fill(x + 6, y + SILL_Y + 1, x + imageWidth - 6, y + SILL_Y + 2, ARTERY);

        drawHammerBay(g, x, y, hammering, ticks);
        drawTrough(g, x, y, operation != 0, ticks);
        drawGrinderBay(g, x, y, grinding, ticks);
        drawSlots(g, x, y);

        boolean idle = operation == 0;
        control(g, x + APPLY_X, y + BUTTON_Y, APPLY_W, BUTTON_H, hammering, idle || hammering,
                inside(mouseX, mouseY, x + APPLY_X, y + BUTTON_Y, APPLY_W, BUTTON_H));
        control(g, x + RECORD_X, y + BUTTON_Y, RECORD_W, BUTTON_H, operation == 2, idle || operation == 2,
                inside(mouseX, mouseY, x + RECORD_X, y + BUTTON_Y, RECORD_W, BUTTON_H));
        control(g, x + STABILIZE_X, y + BUTTON_Y, STABILIZE_W, BUTTON_H, operation == 3,
                menu.tier() >= 2 && (idle || operation == 3),
                inside(mouseX, mouseY, x + STABILIZE_X, y + BUTTON_Y, STABILIZE_W, BUTTON_H));
        control(g, x + SELECT_X, y + SELECT_Y, SELECT_W, SELECT_H, false, menu.tier() >= 1,
                inside(mouseX, mouseY, x + SELECT_X, y + SELECT_Y, SELECT_W, SELECT_H));
        control(g, x + MASTER_X, y + SELECT_Y, MASTER_W, SELECT_H, menu.masterMode(), menu.tier() >= 2,
                inside(mouseX, mouseY, x + MASTER_X, y + SELECT_Y, MASTER_W, SELECT_H));

        if (menu.totalTicks() > 0 && operation != 0) {
            int left = hammering ? APPLY_X : operation == 2 ? RECORD_X : STABILIZE_X;
            int width = hammering ? APPLY_W : operation == 2 ? RECORD_W : STABILIZE_W;
            int filled = Math.clamp(menu.progress() * (width - 4) / menu.totalTicks(), 0, width - 4);
            g.fill(x + left + 2, y + BUTTON_Y + 13, x + left + width - 2, y + BUTTON_Y + 15, 0xFF2A0A0A);
            g.fill(x + left + 2, y + BUTTON_Y + 13, x + left + 2 + filled, y + BUTTON_Y + 15, 0xFFB22C35);
        }

        bloodBounds = BloodVolumeBarWidget.render(g, x + TROUGH_X, y + TROUGH_Y, TROUGH_W, TROUGH_H,
                menu.reading(), ticks / 20F, frameDark(), frame());
    }

    /** The trip-hammer: drum and crank, pitman to the treadle, helve on its fulcrum, the head over the anvil. */
    private void drawHammerBay(GuiGraphics g, int x, int y, boolean hammering, float ticks) {
        int frame = frame(), dark = frameDark();
        float crank = hammering ? ticks * 9F : 0;
        float lobe = (crank % 180F) / 180F;
        float lift = hammering ? (lobe < .8F ? lobe / .8F : (1 - lobe) / .2F) : 0;
        float tilt = (float) Math.tan(Math.toRadians(11F * lift));

        // Mast at the back and the fulcrum cheeks.
        g.fill(x + 7, y + 34, x + 10, y + SILL_Y, frame);
        g.fill(x + FULCRUM_X - 2, y + HELVE_Y, x + FULCRUM_X + 2, y + SILL_Y, frame);
        g.fill(x + FULCRUM_X - 1, y + HELVE_Y + 1, x + FULCRUM_X + 1, y + SILL_Y, dark);
        int headEnd = Math.round(HELVE_Y - (HELVE_HEAD - FULCRUM_X) * tilt);
        int tailEnd = Math.round(HELVE_Y + (FULCRUM_X - HELVE_TAIL) * tilt);
        if (menu.tier() == 0) {
            // The spring pole lashed to the mast bends to keep its cord on the head.
            ScreenDrawUtils.drawCubicTrace(g, x + 8, y + 36, x + 40, y + 30, x + 70, y + 30 + Math.round(lift * -4),
                    x + HELVE_HEAD, y + 32 + Math.round(lift * -6), 2, 0xFF5A3624);
            g.fill(x + HELVE_HEAD, y + 32 + Math.round(lift * -6), x + HELVE_HEAD + 1, y + headEnd, 0xFF8A7A60);
        } else {
            // The iron gallows over the helve, its leaf spring over the tail.
            g.fill(x + 7, y + 30, x + HELVE_HEAD + 6, y + 34, frame);
            g.fill(x + 8, y + 31, x + HELVE_HEAD + 5, y + 33, dark);
            g.fill(x + HELVE_HEAD + 2, y + 30, x + HELVE_HEAD + 6, y + 40, frame);
            for (int leaf = 0; leaf < 3; leaf++)
                g.fill(x + HELVE_TAIL + 2 + leaf * 2, y + 35 + leaf, x + FULCRUM_X - 6 - leaf * 2, y + 36 + leaf, 0xFF6E6268);
            if (menu.tier() >= 2) {
                crown(g, x + 8, y + 30);
                crown(g, x + HELVE_HEAD + 4, y + 30);
                drawMasterRack(g, x + 54, y + 20);
            }
        }

        // Treadle and pitman: the pin rides the crank on the drum's face and the tail follows it.
        float rad = (float) Math.toRadians(crank);
        int pinX = Math.round(DRUM_X + 8 * Mth.sin(rad)), pinY = Math.round(DRUM_Y + 8 * Mth.cos(rad));
        int tailY = TREADLE_Y + Math.round((pinY - DRUM_Y) * .45F);
        drawDrumEnd(g, x + DRUM_X, y + DRUM_Y, crank);
        line(g, x + pinX, y + pinY, x + TREADLE_TAIL + 4, y + tailY, 2, 0xFF5A4038);
        line(g, x + TREADLE_TAIL, y + tailY, x + TREADLE_X, y + TREADLE_Y, 3, frame);
        g.fill(x + TREADLE_X - 2, y + TREADLE_Y - 2, x + TREADLE_X + 2, y + SILL_Y, dark);
        g.fill(x + pinX - 1, y + pinY - 1, x + pinX + 2, y + pinY + 2, 0xFF2A1A16);

        // The helve: follower finger on the drum, axle at the fulcrum, haft and head over the anvil.
        line(g, x + HELVE_TAIL, y + tailEnd, x + HELVE_HEAD + 3, y + headEnd, 3, frame);
        g.fill(x + HELVE_TAIL, y + tailEnd, x + HELVE_TAIL + 2, y + DRUM_Y - DRUM_R + 1, frame);
        g.fill(x + FULCRUM_X - 2, y + HELVE_Y - 2, x + FULCRUM_X + 2, y + HELVE_Y + 2, 0xFF2A1A16);
        float wear = Math.min(1F, menu.hammerUses() / (float) ResonantForgeRules.HAMMER_SERVICE_INTERVAL);
        int anvil = ResonantForgeMenu.ANVIL_SLOT[1];
        int headTop = headEnd + 8, headBottom = anvil - Math.round(lift * 10);
        int headHeight = Math.max(5, Math.round(12 * (1 - wear * .35F)));
        g.fill(x + HELVE_HEAD - 1, y + headEnd, x + HELVE_HEAD + 1, y + headTop, frame);
        int headX = ResonantForgeMenu.ANVIL_SLOT[0];
        g.fill(x + headX - 1, y + Math.max(headTop, headBottom - headHeight), x + headX + 17, y + headBottom,
                menu.tier() > 0 ? 0xFF3C3438 : 0xFF4A4448);
        g.fill(x + headX - 1, y + headBottom - 1, x + headX + 17, y + headBottom,
                wear >= .85F ? 0xFFB04040 : 0xFF7A7276);

        // Anvil on its stump.
        int ax = headX - 6, ay = anvil + 16;
        g.fill(x + ax - 6, y + ay, x + ax, y + ay + 3, 0xFF3A3436);
        g.fill(x + ax, y + ay, x + ax + 28, y + ay + 5, 0xFF4A4448);
        g.fill(x + ax, y + ay, x + ax + 28, y + ay + 1, 0xFF7A7276);
        g.fill(x + ax + 8, y + ay + 5, x + ax + 20, y + ay + 15, 0xFF3A3436);
        g.fill(x + ax + 2, y + ay + 15, x + ax + 26, y + ay + 19, 0xFF4A4448);
        g.fill(x + ax + 4, y + ay + 19, x + ax + 24, y + SILL_Y, 0xFF3A2216);

        // The tray the finished work is set in to cool, with tongs across it.
        int[] tray = ResonantForgeMenu.HAMMERED_SLOT;
        g.fill(x + tray[0] - 3, y + tray[1] + 16, x + tray[0] + 19, y + SILL_Y, dark);
        g.fill(x + tray[0] - 3, y + tray[1] + 4, x + tray[0] - 1, y + SILL_Y, dark);
        g.fill(x + tray[0] + 17, y + tray[1] + 4, x + tray[0] + 19, y + SILL_Y, dark);
        line(g, x + tray[0] + 12, y + tray[1] + 2, x + ax + 2, y + ay - 1, 1, 0xFF5E5458);
    }

    /** The pattern drum end-on: rim, wax core, the two cam lands and the crank web turning together. */
    private void drawDrumEnd(GuiGraphics g, int cx, int cy, float crank) {
        disc(g, cx, cy, DRUM_R + 2, frameDark());
        disc(g, cx, cy, DRUM_R, frame());
        disc(g, cx, cy, DRUM_R - 2, WAX);
        disc(g, cx, cy, DRUM_R - 5, WAX_BAND);
        disc(g, cx, cy, DRUM_R - 6, RECESS);
        for (int land = 0; land < 2; land++) {
            float a = (float) Math.toRadians(crank + land * 180);
            int lx = cx + Math.round((DRUM_R + 1) * Mth.sin(a)), ly = cy - Math.round((DRUM_R + 1) * Mth.cos(a));
            g.fill(lx - 2, ly - 2, lx + 2, ly + 2, 0xFF6E6268);
        }
    }

    /** Masterwork's bone rack of three ambergris master patterns above the hammer. */
    private void drawMasterRack(GuiGraphics g, int rx, int ry) {
        g.fill(rx, ry + 8, rx + 30, ry + 10, BONE);
        for (int i = 0; i < 3; i++) {
            disc(g, rx + 5 + i * 10, ry + 4, 4, 0xFF8A6A3A);
            disc(g, rx + 5 + i * 10, ry + 4, 2, 0xFFD8B070);
        }
        g.fill(rx, ry, rx + 1, ry + 10, BONE);
        g.fill(rx + 29, ry, rx + 30, ry + 10, BONE);
    }

    /** The trough between the bays; Precision feeds it from a cistern, Masterwork from the caged heart. */
    private void drawTrough(GuiGraphics g, int x, int y, boolean working, float ticks) {
        int cx = x + TROUGH_X + TROUGH_W / 2;
        g.fill(x + TROUGH_X - 4, y + TROUGH_Y + TROUGH_H + 2, x + TROUGH_X - 1, y + SILL_Y, frameDark());
        g.fill(x + TROUGH_X + TROUGH_W + 1, y + TROUGH_Y + TROUGH_H + 2, x + TROUGH_X + TROUGH_W + 4, y + SILL_Y,
                frameDark());
        if (menu.tier() == 0) return;
        int top = y + 46, bottom = y + 72;
        g.fill(cx - 11, top, cx + 11, bottom, frameDark());
        g.fill(cx - 10, top + 1, cx + 10, bottom - 1, RECESS);
        g.fill(cx - 1, bottom, cx + 1, y + TROUGH_Y - 3, frame());
        if (menu.tier() >= 2) {
            // The heart keeps a slow resting beat and quickens while the forge works.
            float rate = working ? .42F : .16F;
            float beat = (float) Math.pow(Math.max(0, Mth.sin(ticks * rate)), 6);
            int r = 6 + Math.round(beat * 1.5F);
            disc(g, cx - 3, top + 11, r - 2, 0xFF7A1018);
            disc(g, cx + 3, top + 11, r - 2, 0xFF7A1018);
            disc(g, cx, top + 15, r - 1, 0xFF8E1820);
            g.fill(cx - 2, top + 8, cx, top + 10, 0x80FF8080);
            for (int bar = cx - 9; bar <= cx + 9; bar += 4) g.fill(bar, top + 1, bar + 1, bottom - 1, frame());
            g.fill(cx - 10, top + 1, cx + 10, top + 2, BONE);
            g.fill(cx - 1, bottom, cx + 1, y + TROUGH_Y - 3, ARTERY);
            crown(g, cx - 11, top);
        } else {
            int level = Math.round(22 * (float) Math.min(1, menu.reading().fresh() / Math.max(1, menu.reading().capacity())));
            if (level > 0) g.fill(cx - 10, bottom - 1 - level, cx + 10, bottom - 1, 0xC0601014);
            g.fill(cx + 7, top + 3, cx + 8, bottom - 3, BONE);
        }
        if (working) {
            float fall = (ticks / 12F) % 1F;
            int drop = y + 72 + Math.round(fall * (TROUGH_Y - 75));
            g.fill(cx - 1, drop, cx + 1, drop + 2, 0xE0B22C35);
        }
    }

    /** The grindstone on its trestles, the tool rest, and the recording drum belted to the stone. */
    private void drawGrinderBay(GuiGraphics g, int x, int y, boolean grinding, float ticks) {
        int frame = frame(), dark = frameDark();
        int sx = x + STONE_X, sy = y + STONE_Y;
        float wheelWear = Math.min(1F, menu.wheelUses() / (float) ResonantForgeRules.WHEEL_SERVICE_INTERVAL);

        // Drum bearings, belt from the stone's layshaft pulley up to the drum.
        g.fill(x + RECORD_LEFT - 4, y + RECORD_TOP - 6, x + RECORD_LEFT, y + RECORD_BOTTOM + 4, frame);
        g.fill(x + RECORD_RIGHT, y + RECORD_TOP - 6, x + RECORD_RIGHT + 4, y + RECORD_BOTTOM + 4, frame);
        line(g, x + RECORD_RIGHT + 2, y + RECORD_TOP + 4, sx - 3, sy, 1, 0xFF3A2A24);
        line(g, x + RECORD_RIGHT + 2, y + RECORD_BOTTOM - 4, sx + 3, sy, 1, 0xFF3A2A24);

        // Trestles splayed under the axle.
        line(g, sx, sy, sx - 14, y + SILL_Y, 3, frame);
        line(g, sx, sy, sx + 14, y + SILL_Y, 3, frame);

        // The stone: a dressed rim when fresh, chipped once the wheel wears.
        float spin = grinding ? ticks * .22F : 0;
        disc(g, sx, sy, STONE_R, STONE_DARK);
        disc(g, sx, sy, STONE_R - 1, STONE);
        disc(g, sx, sy, STONE_R - 6, 0xFF8E8882);
        for (int notch = 0; notch < 10; notch++) {
            float a = spin + notch * Mth.TWO_PI / 10;
            int nx = sx + Math.round((STONE_R - 3) * Mth.cos(a)), ny = sy + Math.round((STONE_R - 3) * Mth.sin(a));
            g.fill(nx - 1, ny - 1, nx + 1, ny + 1, notch % 3 == 0 && wheelWear > .5F ? 0xFF4A4440 : STONE_DARK);
        }
        disc(g, sx, sy, 3, dark);
        float handle = spin * .6F;
        int hx = sx + Math.round(9 * Mth.cos(handle)), hy = sy + Math.round(9 * Mth.sin(handle));
        line(g, sx, sy, hx, hy, 2, frame);
        g.fill(hx - 1, hy - 1, hx + 2, hy + 2, 0xFF5A3624);

        // Slurry trough the lower rim runs through.
        g.fill(sx - 16, sy + 10, sx + 17, sy + 20, dark);
        g.fill(sx - 15, sy + 11, sx + 16, sy + 13, 0xFF4A3A36);

        // Tool rest at the inner rim, where the work is held to the stone.
        int[] rest = ResonantForgeMenu.REST_SLOT;
        g.fill(x + rest[0] - 2, y + rest[1] + 16, sx - STONE_R + 1, y + rest[1] + 18, frame);
        g.fill(x + rest[0] - 2, y + rest[1] + 18, x + rest[0], y + SILL_Y, dark);
        if (grinding) {
            int gx = sx - STONE_R, gy = y + rest[1] + 8;
            for (int k = 0; k < 4; k++) {
                float t = (ticks * .35F + k * .27F) % 1F;
                int px = gx - 2 - Math.round(t * 9), py = gy - 3 + Math.round(t * t * 10) - k;
                g.fill(px, py, px + 1, py + 1, ((int) (0xF0 * (1 - t)) << 24) | 0xFFC060);
            }
        }

        // The recording drum face-on: wax with spiral grooves turning toward the stylus.
        g.fill(x + RECORD_LEFT, y + RECORD_TOP, x + RECORD_RIGHT, y + RECORD_BOTTOM, WAX);
        int scroll = grinding ? Math.round(ticks * .5F) : 0;
        for (int band = -RECORD_BOTTOM; band < RECORD_RIGHT - RECORD_LEFT; band += 5) {
            int offset = Math.floorMod(band + scroll, RECORD_RIGHT - RECORD_LEFT + RECORD_BOTTOM - RECORD_TOP);
            for (int row = 0; row < RECORD_BOTTOM - RECORD_TOP; row++) {
                int col = offset - row / 2;
                if (col >= 0 && col < RECORD_RIGHT - RECORD_LEFT)
                    g.fill(x + RECORD_LEFT + col, y + RECORD_TOP + row, x + RECORD_LEFT + col + 1,
                            y + RECORD_TOP + row + 1, WAX_BAND);
            }
        }
        g.fill(x + RECORD_LEFT, y + RECORD_TOP, x + RECORD_RIGHT, y + RECORD_TOP + 1, 0x60FFFFFF);
        g.fill(x + RECORD_LEFT, y + RECORD_BOTTOM - 2, x + RECORD_RIGHT, y + RECORD_BOTTOM, 0x60000000);

        float chatter = grinding ? Mth.sin(ticks * 2.3F) * .6F + Mth.sin(ticks * 5.1F) * .3F : 0;
        int cupX, cupY = y + RECORD_TOP - 6 + Math.round(chatter);
        if (menu.tier() == 0) {
            // The sound box on its hinged arm, its stylus on the wax.
            cupX = x + 174;
            line(g, x + RECORD_RIGHT + 2, y + RECORD_TOP - 10, cupX + 3, cupY + 1, 2, frame);
        } else {
            // Lead screw and rail: the carriage walks across the wax with the grind.
            float phase = menu.totalTicks() == 0 ? .5F : Math.min(1, menu.progress() / (float) menu.totalTicks());
            cupX = x + RECORD_LEFT + 4 + Math.round((grinding ? phase : .5F) * (RECORD_RIGHT - RECORD_LEFT - 14));
            g.fill(x + RECORD_LEFT - 4, y + RECORD_TOP - 10, x + RECORD_RIGHT + 4, y + RECORD_TOP - 8, frame);
            for (int thread = RECORD_LEFT - 3; thread < RECORD_RIGHT + 3; thread += 2)
                g.fill(x + thread, y + RECORD_TOP - 10, x + thread + 1, y + RECORD_TOP - 8, dark);
            g.fill(cupX - 1, y + RECORD_TOP - 11, cupX + 7, y + RECORD_TOP - 7, 0xFF6E6268);
            // Flyball governor on the layshaft, beside the belt.
            float fly = grinding ? ticks * .4F : 0;
            int spread = grinding ? 5 : 3;
            int gx = x + RECORD_RIGHT + 14, gy = y + RECORD_BOTTOM + 1;
            g.fill(gx, gy - 6, gx + 1, gy + 2, frame);
            g.fill(gx - 1 + Math.round(spread * Mth.cos(fly)), gy - 3, gx + 2 + Math.round(spread * Mth.cos(fly)), gy, BONE);
            g.fill(gx - 1 - Math.round(spread * Mth.cos(fly)), gy - 3, gx + 2 - Math.round(spread * Mth.cos(fly)), gy, BONE);
        }
        g.fill(cupX, cupY, cupX + 6, cupY + 4, 0xFF4A3A36);
        g.fill(cupX + 2, cupY + 4, cupX + 4, y + RECORD_TOP + 1, 0xFF2A1A16);
        if (menu.tier() >= 2) {
            // The bone horn: throat at the sound box, bell opening toward the worker.
            line(g, cupX + 5, cupY, x + 208, y + 30, 2, BONE);
            ScreenDrawUtils.drawCircleTrace(g, x + 214, y + 30, 7, 2, BONE);
            disc(g, x + 214, y + 30, 4, RECESS);
            crown(g, x + RECORD_RIGHT, y + RECORD_TOP - 6);
            crown(g, x + RECORD_LEFT - 4, y + RECORD_TOP - 6);
        }
    }

    private void drawSlots(GuiGraphics g, int x, int y) {
        ItemStack applicationPreview = menu.forge().getItem(ResonantForgeBlockEntity.APPLICATION_OUTPUT).isEmpty()
                ? menu.applicationPreview() : ItemStack.EMPTY;
        var grindingPreview = menu.forge().getItem(ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT).isEmpty()
                ? menu.grindingPreview() : null;
        boolean grindingPreviewed = grindingPreview != null && grindingPreview.success();
        for (int i = 0; i < ResonantForgeBlockEntity.SLOT_COUNT; i++) {
            var slot = menu.slots.get(i);
            boolean output = i == ResonantForgeBlockEntity.APPLICATION_OUTPUT
                    || i >= ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT;
            boolean onWax = i == ResonantForgeBlockEntity.APPLICATION_CYLINDER || i == ResonantForgeBlockEntity.GRINDING_CYLINDER;
            if (onWax) g.fill(x + slot.x - 1, y + slot.y - 1, x + slot.x + 17, y + slot.y + 17, 0xC0100406);
            slot(g, x + slot.x, y + slot.y, output ? 0xFFB64A4A : 0xFF704040);
            boolean previewed = i == ResonantForgeBlockEntity.APPLICATION_OUTPUT ? !applicationPreview.isEmpty()
                    : i >= ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT && grindingPreviewed;
            if (slot.getItem().isEmpty() && !previewed) icon(g, SLOT_ICONS[i], x + slot.x, y + slot.y);
        }
        var applicationSlot = menu.slots.get(ResonantForgeBlockEntity.APPLICATION_OUTPUT);
        ItemStackPreview.render(g, applicationPreview, x + applicationSlot.x, y + applicationSlot.y);
        if (grindingPreviewed) {
            var equipmentSlot = menu.slots.get(ResonantForgeBlockEntity.GRINDING_EQUIPMENT_OUTPUT);
            var cylinderSlot = menu.slots.get(ResonantForgeBlockEntity.GRINDING_CYLINDER_OUTPUT);
            ItemStackPreview.render(g, grindingPreview.equipment(), x + equipmentSlot.x, y + equipmentSlot.y);
            var cylinderPreview = menu.forge().getItem(ResonantForgeBlockEntity.GRINDING_CYLINDER).copy();
            cylinderPreview.set(DataComponentInit.RESONANT_PATTERN.get(), grindingPreview.pattern());
            ItemStackPreview.render(g, cylinderPreview, x + cylinderSlot.x, y + cylinderSlot.y);
        }
    }

    @Override protected void renderLabels(GuiGraphics g, int mouseX, int mouseY) {
        text(g, title, 8, 7, 0xFFE8A6A6, .75F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.tier", menu.tier() == 0 ? 3 : menu.tier() == 1 ? 5 : 7),
                imageWidth - 18, 8, 0xFFCC7777, .6F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.applying"), 14, 18, MUTED, .65F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.grinding"), RECORD_X, 18, MUTED, .65F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.hammer_wear", menu.hammerUses(),
                ResonantForgeRules.HAMMER_SERVICE_INTERVAL), 14, 25,
                menu.hammerUses() >= 56 ? 0xFFFF7777 : MUTED, .48F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.wheel_wear", menu.wheelUses(),
                ResonantForgeRules.WHEEL_SERVICE_INTERVAL), RECORD_X, 25,
                menu.wheelUses() >= 28 ? 0xFFFF7777 : MUTED, .48F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.cost", menu.applicationCost()),
                54, 25, MUTED, .48F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.cost", menu.grindingCost()),
                RECORD_X + 32, 25, MUTED, .48F);

        var cylinder = menu.forge().getItem(ResonantForgeBlockEntity.APPLICATION_CYLINDER);
        var pattern = cylinder.get(DataComponentInit.RESONANT_PATTERN.get());
        if (pattern != null) {
            boolean returnsShell = CylinderMedia.retainsShell(cylinder);
            String outcome = pattern.master() ? "reusable" : returnsShell ? "shell" : "consumed";
            text(g, Component.translatable("screen.hemomancy.resonant_forge.cylinder." + outcome),
                    10, DRUM_Y + DRUM_R + 4, pattern.master() || returnsShell ? 0xFFE7B878 : MUTED, .48F);
        }

        int operation = menu.operation();
        centred(g, Component.translatable(operation == 1 ? "screen.hemomancy.resonant_forge.cancel"
                : "screen.hemomancy.resonant_forge.apply"), APPLY_X, APPLY_W, BUTTON_Y + 3, PALE, .65F);
        centred(g, Component.translatable(operation == 2 ? "screen.hemomancy.resonant_forge.cancel"
                : "screen.hemomancy.resonant_forge.record"), RECORD_X, RECORD_W, BUTTON_Y + 3, PALE, .6F);
        centred(g, Component.translatable(operation == 3 ? "screen.hemomancy.resonant_forge.cancel"
                : "screen.hemomancy.resonant_forge.stabilize"), STABILIZE_X, STABILIZE_W, BUTTON_Y + 3,
                menu.tier() >= 2 ? PALE : MUTED, .55F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.selection", selectionName()),
                SELECT_X + 4, SELECT_Y + 3, menu.tier() >= 1 ? PALE : MUTED, .5F);
        centred(g, Component.translatable("screen.hemomancy.resonant_forge.master"), MASTER_X, MASTER_W, SELECT_Y + 3,
                menu.masterMode() ? 0xFFFFAAAA : MUTED, .5F);
        text(g, Component.translatable("screen.hemomancy.resonant_forge.status." + statusName()),
                8, BUTTON_Y + 18, MUTED, .55F);
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
            int operation = menu.operation();
            if (inside(mouseX, mouseY, leftPos + APPLY_X, topPos + BUTTON_Y, APPLY_W, BUTTON_H))
                return send(operation == 1 ? 3 : 0);
            if (inside(mouseX, mouseY, leftPos + RECORD_X, topPos + BUTTON_Y, RECORD_W, BUTTON_H))
                return send(operation == 2 ? 3 : 1);
            if (inside(mouseX, mouseY, leftPos + STABILIZE_X, topPos + BUTTON_Y, STABILIZE_W, BUTTON_H))
                return send(operation == 3 ? 3 : 2);
            if (inside(mouseX, mouseY, leftPos + MASTER_X, topPos + SELECT_Y, MASTER_W, SELECT_H)) return send(4);
            if (inside(mouseX, mouseY, leftPos + SELECT_X, topPos + SELECT_Y, SELECT_W, SELECT_H)) {
                List<String> options = menu.selections();
                int current = options.indexOf(menu.forge().selection());
                return send(100 + ((current + 1) % Math.max(1, options.size())));
            }
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
        BloodVolumeBarWidget.renderTooltip(g, font, bloodBounds, menu.reading(), mouseX, mouseY);
        if (inside(mouseX, mouseY, leftPos + SELECT_X, topPos + SELECT_Y, SELECT_W, SELECT_H))
            g.renderTooltip(font, Component.translatable("screen.hemomancy.resonant_forge.selection.tooltip"), mouseX, mouseY);
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.index < ResonantForgeBlockEntity.SLOT_COUNT
                && !hoveredSlot.hasItem())
            g.renderTooltip(font, Component.translatable("screen.hemomancy.resonant_forge.slot."
                    + SLOT_HINTS[hoveredSlot.index]), mouseX, mouseY);
    }

    /** Blood-wood on the base stage, hematic iron once rebuilt for Precision and Masterwork. */
    private int frame() {
        return menu.tier() == 0 ? 0xFF5A3624 : 0xFF4C3E44;
    }

    private int frameDark() {
        return menu.tier() == 0 ? 0xFF2E1A10 : 0xFF241C20;
    }

    /** Masterwork's bone crown on a post top. */
    private static void crown(GuiGraphics g, int x, int y) {
        g.fill(x - 1, y - 2, x + 5, y, BONE);
        g.fill(x, y - 4, x + 1, y - 2, BONE);
        g.fill(x + 3, y - 4, x + 4, y - 2, BONE);
    }

    private static void disc(GuiGraphics g, int cx, int cy, int r, int color) {
        for (int dy = -r; dy <= r; dy++) {
            int half = (int) Math.sqrt(r * r - dy * dy + r * .8F);
            g.fill(cx - half, cy + dy, cx + half + 1, cy + dy + 1, color);
        }
    }

    private static void line(GuiGraphics g, int x0, int y0, int x1, int y1, int thickness, int color) {
        ScreenDrawUtils.drawCubicTrace(g, x0, y0, x0 + (x1 - x0) / 3, y0 + (y1 - y0) / 3,
                x0 + (x1 - x0) * 2 / 3, y0 + (y1 - y0) * 2 / 3, x1, y1, thickness, color);
    }

    private static ResourceLocation iconTexture(String name) {
        return Hemomancy.rloc("textures/gui/resonant_forge/" + name + ".png");
    }

    private static void icon(GuiGraphics g, ResourceLocation texture, int x, int y) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.blit(texture, x, y, 0, 0, 16, 16, 16, 16);
        RenderSystem.disableBlend();
    }

    private static void slot(GuiGraphics g, int x, int y, int accent) {
        g.fillGradient(x + 1, y, x + 15, y + 16, 0xFF1A0808, 0xE01A0808);
        g.fill(x, y + 2, x + 1, y + 14, 0xFF351313);
        g.fill(x + 15, y + 2, x + 16, y + 14, 0xFF351313);
        g.fill(x + 4, y + 15, x + 12, y + 16, accent);
    }

    private static void control(GuiGraphics g, int x, int y, int width, int height,
            boolean selected, boolean enabled, boolean hovered) {
        int border = !enabled ? 0xFF3D2222 : selected ? 0xFFEF6666 : hovered ? 0xFFB64040 : 0xFF6B2424;
        g.fillGradient(x + 2, y, x + width - 2, y + height,
                !enabled ? 0xA0190F10 : hovered || selected ? 0xE0511515 : 0xC027090B, 0x9027090B);
        g.fill(x + 3, y + height - 1, x + width - 3, y + height, border);
    }

    private static final class ItemStackPreview {
        private static void render(GuiGraphics graphics, ItemStack stack, int x, int y) {
            if (stack.isEmpty()) return;
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 5);
            graphics.renderFakeItem(stack, x, y);
            graphics.fill(x + 1, y + 1, x + 15, y + 15, 0x55300030);
            graphics.pose().popPose();
        }
    }

    private void centred(GuiGraphics g, Component value, int left, int width, float y, int color, float scale) {
        text(g, value, left + (width - font.width(value) * scale) / 2F, y, color, scale);
    }

    private void text(GuiGraphics g, Component value, float x, float y, int color, float scale) {
        g.pose().pushPose(); g.pose().translate(x, y, 0); g.pose().scale(scale, scale, 1);
        g.drawString(font, value, 0, 0, color, false); g.pose().popPose();
    }

    private static boolean inside(double x, double y, int left, int top, int width, int height) {
        return x >= left && x < left + width && y >= top && y < top + height;
    }
}
