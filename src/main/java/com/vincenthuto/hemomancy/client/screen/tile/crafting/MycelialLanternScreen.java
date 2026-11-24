package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialLanternMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.Random;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;

/**
 * The Lantern drawn as a cutaway of the block (tools/model_export/mycelial_lantern.py): the carry bail and vent on
 * top, the stepped glass cloche in its iron cage with the culture standing on the wick cup inside, the fruiting bed
 * in the crock's rim, and the blood-wood crock below carrying the flask feed, the blood sight glass and the harvest
 * dish. Nothing is drawn outside the silhouette; spores and hyphae move only behind the glass.
 */
public class MycelialLanternScreen extends AbstractContainerScreen<MycelialLanternMenu> {

    private static final int SLOT_BG = 0xFF2A1508;
    private static final int SLOT_BORDER_DARK = 0xFF140A02;
    private static final int SLOT_BORDER_LIGHT = 0xFF4A2A10;
    private static final int AMBER = 0xFFDD8822;

    private static final int IRON = 0xFF3A2628;
    private static final int IRON_LIGHT = 0xFF6E4C4E;
    private static final int IRON_DARK = 0xFF180E10;
    private static final int WOOD = 0xFF4A1A12;
    private static final int WOOD_LIGHT = 0xFF64281A;
    private static final int WOOD_DARK = 0xFF2C0D08;
    private static final int BONE = 0xFFD6C8A8;
    private static final int BONE_DARK = 0xFF8E7E62;
    private static final int GLASS_DEPTH = 0xFF140804;
    private static final int GLASS_TINT = 0x26E8C0B8;
    private static final int GLASS_GLINT = 0x40FFF0E8;
    private static final int BED = 0xFF3A1610;
    private static final int BED_LIGHT = 0xFF5A2418;
    private static final int CAP = 0xFF8A2A1E;
    private static final int HYPHA = 0x60D8C8B0;
    private static final int FLESH = 0xFF8C4A44;
    private static final int FLESH_DARK = 0xFF5A2A28;

    /** Height of the cutaway above the inventory panel. */
    private static final int CRAFT_AREA_HEIGHT = 110;

    // Silhouette, top to bottom (screen pixels from the GUI origin).
    private static final int CENTRE_X = 88;
    private static final int NECK_X0 = 68, NECK_X1 = 108, NECK_Y0 = 17, NECK_Y1 = 22;
    private static final int SHOULDER_Y0 = 22, SHOULDER_Y1 = 25;
    private static final int BELL_X0 = 58, BELL_X1 = 118, BELL_Y0 = 25, BELL_Y1 = 62;
    private static final int BED_X0 = 22, BED_X1 = 154, BED_Y0 = 61, BED_Y1 = 66;
    private static final int RIM_X0 = 10, RIM_X1 = 166, RIM_Y0 = 66, RIM_Y1 = 71;
    private static final int CROCK_X0 = 14, CROCK_X1 = 162, CROCK_Y0 = 71, CROCK_Y1 = 112;
    private static final int TITLE_Y = 77;

    // The sight glass in the crock's front, where the stored blood stands.
    private static final int GAUGE_X = 84, GAUGE_Y = 88, GAUGE_W = 8, GAUGE_H = 18;

    // The harvest path: a hypha from the bell's foot across the bed, down the spout to the dish.
    private static final int SPOUT_X = 138;

    private static final int TENDRIL_COUNT = 14;
    private static final int SPORE_COUNT = 28;

    private float[][] tendrilParams;
    private float[][] sporeParams;
    private int[][] speckleParams;
    private float animTime = 0f;

    private BloodVolumeBarWidget.Bounds bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;

    public MycelialLanternScreen(MycelialLanternMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 198;
        this.inventoryLabelY = CRAFT_AREA_HEIGHT + 7;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        int bellW = BELL_X1 - BELL_X0;
        int bellH = BELL_Y1 - BELL_Y0;

        Random rand = new Random(0xF12A7E1L);
        tendrilParams = new float[TENDRIL_COUNT][9];
        for (int i = 0; i < TENDRIL_COUNT; i++) {
            tendrilParams[i][0] = rand.nextFloat();
            tendrilParams[i][1] = 0.55f + rand.nextFloat() * 0.45f;
            // Hyphae climb the glass: mostly upward from the bed.
            tendrilParams[i][2] = (float) (-Math.PI / 2 + (rand.nextFloat() - 0.5f) * 1.6f);
            tendrilParams[i][3] = 0.18f + rand.nextFloat() * 0.55f;
            tendrilParams[i][4] = 1.5f + rand.nextFloat() * 4f;
            tendrilParams[i][5] = 0.08f + rand.nextFloat() * 0.16f;
            tendrilParams[i][6] = 12 + rand.nextInt(22);
            tendrilParams[i][7] = 1;
            tendrilParams[i][8] = rand.nextFloat();
        }

        sporeParams = new float[SPORE_COUNT][5];
        for (int i = 0; i < SPORE_COUNT; i++) {
            sporeParams[i][0] = rand.nextFloat();
            sporeParams[i][1] = rand.nextFloat();
            sporeParams[i][2] = 0.25f + rand.nextFloat() * 0.7f;
            sporeParams[i][3] = rand.nextFloat() * 100f;
            sporeParams[i][4] = rand.nextFloat();
        }

        Random speckRand = new Random(0x51EADL);
        speckleParams = new int[48][5];
        for (int s = 0; s < speckleParams.length; s++) {
            speckleParams[s][0] = speckRand.nextInt(bellW);
            speckleParams[s][1] = speckRand.nextInt(bellH);
            speckleParams[s][2] = 30 + speckRand.nextInt(48);
            speckleParams[s][3] = 10 + speckRand.nextInt(26);
            speckleParams[s][4] = 14 + speckRand.nextInt(34);
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
        BloodVolumeBarWidget.renderTooltip(gfx, font, bloodBarBounds,
                menu.reading(), mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int gx = leftPos;
        int gy = topPos;
        float progress = menu.getScaledCraftProgress() / 24f;

        renderCrock(gfx, gx, gy);

        Slot firstInventorySlot = this.menu.slots.get(MycelialLanternMenu.SLOT_COUNT);
        InventoryPanelTextures.blit(gfx, InventoryPanelTextures.FUNGAL,
                gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);
        renderFeet(gfx, gx, gy);

        renderFungalBackground(gfx, gx + BELL_X0, gy + BELL_Y0, BELL_X1 - BELL_X0, BELL_Y1 - BELL_Y0);
        renderFruitingHaze(gfx, gx, gy, progress);
        renderBed(gfx, gx, gy);
        renderWick(gfx, gx, gy);
        renderCloche(gfx, gx, gy);
        renderCrown(gfx, gx, gy);

        for (int i = 0; i < MycelialLanternMenu.SLOT_COUNT; i++) {
            Slot slot = menu.slots.get(i);
            drawSlotBackground(gfx, gx + slot.x, gy + slot.y, slot.index);
        }

        renderFeedPipe(gfx, gx, gy);
        renderHarvestPath(gfx, gx, gy, progress);
        renderBloodBar(gfx, gx, gy);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(font, title, titleLabelX, TITLE_Y, AMBER, false);
    }

    // ── The crock ────────────────────────────────────────────────

    /** The iron rim the bed sits in, and the hooped blood-wood crock below it. */
    private void renderCrock(GuiGraphics gfx, int gx, int gy) {
        int x0 = gx + CROCK_X0, x1 = gx + CROCK_X1, y0 = gy + CROCK_Y0, y1 = gy + CROCK_Y1;
        gfx.fill(x0, y0, x1, y1, WOOD);
        for (int sx = x0 + 11; sx < x1 - 4; sx += 12) {
            gfx.fill(sx, y0, sx + 1, y1, WOOD_DARK);
            gfx.fill(sx + 1, y0, sx + 2, y1, WOOD_LIGHT);
        }
        ScreenDrawUtils.drawBevelBorder(gfx, x0, y0, x1 - x0, y1 - y0, WOOD_LIGHT, WOOD_DARK);
        // The upper hoop under the rim.
        gfx.fill(x0 - 1, y0 + 2, x1 + 1, y0 + 4, IRON);
        gfx.fill(x0 - 1, y0 + 2, x1 + 1, y0 + 3, IRON_LIGHT);

        int rx = gx + RIM_X0, ry = gy + RIM_Y0;
        gfx.fill(rx, ry, gx + RIM_X1, gy + RIM_Y1, IRON);
        ScreenDrawUtils.drawBevelBorder(gfx, rx, ry, RIM_X1 - RIM_X0, RIM_Y1 - RIM_Y0, IRON_LIGHT, IRON_DARK);
        for (int sx = rx + 6; sx < gx + RIM_X1 - 4; sx += 8) {
            gfx.fill(sx, ry + 2, sx + 1, ry + 3, IRON_DARK);
        }
    }

    /** Cast feet standing on the inventory panel's top edge. */
    private static void renderFeet(GuiGraphics gfx, int gx, int gy) {
        for (int fx : new int[] {CROCK_X0 + 3, CROCK_X1 - 13}) {
            int x = gx + fx, y = gy + CROCK_Y1 - 3;
            gfx.fill(x, y, x + 10, y + 4, IRON);
            gfx.fill(x, y, x + 10, y + 1, IRON_LIGHT);
            gfx.fill(x + 1, y + 4, x + 9, y + 5, IRON_DARK);
        }
    }

    /** The flask pours through an iron pipe into the sight glass. */
    private static void renderFeedPipe(GuiGraphics gfx, int gx, int gy) {
        for (int py = GAUGE_Y + 8; py < GAUGE_Y + 11; py++) {
            gfx.fill(gx + 74, gy + py, gx + GAUGE_X - 2, gy + py + 1, py == GAUGE_Y + 8 ? IRON_LIGHT : IRON);
        }
        gfx.fill(gx + 51, gy + GAUGE_Y + 9, gx + 54, gy + GAUGE_Y + 10, IRON_LIGHT);
    }

    private void renderBloodBar(GuiGraphics gfx, int gx, int gy) {
        // Iron frame and bone ticks, as the model's sight glass.
        int fx = gx + GAUGE_X - 2, fy = gy + GAUGE_Y - 2;
        gfx.fill(fx, fy, fx + GAUGE_W + 4, fy + GAUGE_H + 4, IRON);
        ScreenDrawUtils.drawBevelBorder(gfx, fx, fy, GAUGE_W + 4, GAUGE_H + 4, IRON_LIGHT, IRON_DARK);
        for (int n = 1; n <= 3; n++) {
            int ty = gy + GAUGE_Y + GAUGE_H - n * GAUGE_H / 4;
            gfx.fill(fx + GAUGE_W + 4, ty, fx + GAUGE_W + 6, ty + 1, BONE);
        }
        bloodBarBounds = BloodVolumeBarWidget.render(gfx, gx + GAUGE_X, gy + GAUGE_Y, GAUGE_W, GAUGE_H,
                menu.reading(), animTime, IRON_DARK, 0xFF220606);
    }

    /**
     * The fruiting hypha running from the bell's foot across the bed and down the spout into the harvest dish; it
     * lights from the bell outward as the culture fruits.
     */
    private void renderHarvestPath(GuiGraphics gfx, int gx, int gy, float progress) {
        Slot output = menu.slots.get(MycelialLanternMenu.OUTPUT_SLOT);
        int startX = BELL_X1 + 2;
        int runY = BED_Y0 + 2;
        int across = SPOUT_X - startX;
        int down = output.y - 2 - runY;
        int total = across + down;
        int lit = Mth.floor(total * progress);

        // The spout through the rim and crock face.
        gfx.fill(gx + SPOUT_X - 2, gy + RIM_Y0, gx + SPOUT_X + 3, gy + output.y - 1, IRON);
        gfx.fill(gx + SPOUT_X - 2, gy + RIM_Y0, gx + SPOUT_X - 1, gy + output.y - 1, IRON_LIGHT);
        gfx.fill(gx + SPOUT_X + 2, gy + RIM_Y0, gx + SPOUT_X + 3, gy + output.y - 1, IRON_DARK);

        for (int step = 0; step < total; step++) {
            int px = step < across ? startX + step : SPOUT_X;
            int py = step < across ? runY : runY + (step - across);
            int color;
            if (step < lit) {
                float pulse = 0.65f + 0.35f * Mth.sin(animTime * 3f - step * 0.22f);
                color = (0xDD << 24) | ((int) (230 * pulse) << 16) | ((int) (120 * pulse) << 8) | (int) (20 * pulse);
            } else {
                color = 0x80201006;
            }
            gfx.fill(gx + px, gy + py, gx + px + 1, gy + py + 1, color);
        }
        if (lit > 0 && lit < total) {
            int px = lit < across ? startX + lit : SPOUT_X;
            int py = lit < across ? runY : runY + (lit - across);
            gfx.fill(gx + px - 1, gy + py - 1, gx + px + 2, gy + py + 2, 0x66FFB342);
            gfx.fill(gx + px, gy + py, gx + px + 1, gy + py + 1, 0xFFFFD27A);
        }
    }

    // ── The bed and wick ─────────────────────────────────────────

    /** The mycelium bed inside the rim, with fruiting bodies whose gills glow. */
    private void renderBed(GuiGraphics gfx, int gx, int gy) {
        gfx.fill(gx + BED_X0, gy + BED_Y0, gx + BED_X1, gy + BED_Y1, BED);
        gfx.fill(gx + BED_X0, gy + BED_Y0, gx + BED_X1, gy + BED_Y0 + 1, BED_LIGHT);
        int[][] bodies = {{28, 3, 5}, {40, 5, 7}, {64, 4, 9}, {74, 2, 5}, {104, 6, 11}, {112, 3, 6},
                {146, 4, 6}};
        for (int i = 0; i < bodies.length; i++) {
            int x = gx + bodies[i][0];
            int capHalf = bodies[i][1] / 2 + 1;
            int top = gy + BED_Y0 - bodies[i][2];
            gfx.fill(x, top, x + 1, gy + BED_Y0, 0xFFB8A890);
            float pulse = 0.55f + 0.45f * Mth.sin(animTime * 1.4f + i * 1.7f);
            int glow = ((int) (150 * pulse) << 24) | 0xFF8A2A;
            gfx.fill(x - capHalf, top + 1, x + capHalf + 1, top + 2, glow);
            gfx.fill(x - capHalf, top, x + capHalf + 1, top + 1, CAP);
            gfx.fill(x - capHalf + 1, top - 1, x + capHalf, top, CAP);
        }
    }

    /** The flesh wick lifting the culture into the bell, capped by its bone cup under the culture slot. */
    private void renderWick(GuiGraphics gfx, int gx, int gy) {
        Slot culture = menu.slots.get(MycelialLanternMenu.CULTURE_SLOT);
        int cupY = gy + culture.y + 17;
        gfx.fill(gx + CENTRE_X - 2, cupY + 2, gx + CENTRE_X + 2, gy + BED_Y0, FLESH);
        gfx.fill(gx + CENTRE_X + 1, cupY + 2, gx + CENTRE_X + 2, gy + BED_Y0, FLESH_DARK);
        gfx.fill(gx + CENTRE_X + 3, cupY + 3, gx + CENTRE_X + 4, gy + BED_Y0, 0xFF6A1410);
        gfx.fill(gx + CENTRE_X - 7, cupY, gx + CENTRE_X + 7, cupY + 2, BONE);
        gfx.fill(gx + CENTRE_X - 7, cupY + 1, gx + CENTRE_X + 7, cupY + 2, BONE_DARK);
    }

    /** A warm haze rising behind the glass while the culture fruits. */
    private static void renderFruitingHaze(GuiGraphics gfx, int gx, int gy, float progress) {
        if (progress <= 0) return;
        int height = Mth.ceil((BELL_Y1 - BELL_Y0) * progress);
        int top = gy + BELL_Y1 - height;
        gfx.fillGradient(gx + BELL_X0, top, gx + BELL_X1, gy + BELL_Y1, 0x00FF8A2A, 0x30FF8A2A);
    }

    // ── The cloche and crown ─────────────────────────────────────

    /** The bell's glass over the fungal field, its iron cage ribs, collar and stepped shoulder. */
    private static void renderCloche(GuiGraphics gfx, int gx, int gy) {
        int bx0 = gx + BELL_X0, bx1 = gx + BELL_X1, by0 = gy + BELL_Y0, by1 = gy + BELL_Y1;
        gfx.fill(bx0, by0, bx1, by1, GLASS_TINT);
        gfx.fill(bx0 + 3, by0 + 2, bx0 + 4, by1 - 6, GLASS_GLINT);
        gfx.fill(bx0 + 5, by0 + 4, bx0 + 6, by0 + 14, 0x20FFF0E8);

        // Front cage ribs (the model's diagonals, seen face on) and the collar under the culture.
        for (int rib : new int[] {CENTRE_X - 21, CENTRE_X + 20}) {
            gfx.fill(gx + rib, by0, gx + rib + 1, by1, 0x90180E10);
        }
        int collarY = gy + 44;
        gfx.fill(bx0, collarY, bx1, collarY + 2, 0xB03A2628);
        gfx.fill(bx0, collarY, bx1, collarY + 1, 0xB06E4C4E);

        // Side walls of the cage.
        for (int x : new int[] {BELL_X0 - 2, BELL_X1}) {
            gfx.fill(gx + x, gy + SHOULDER_Y0, gx + x + 2, gy + BED_Y0 + 1, IRON);
            gfx.fill(gx + x, gy + SHOULDER_Y0, gx + x + 1, gy + BED_Y0 + 1, IRON_LIGHT);
        }
        // Shoulder: the bell steps in to its neck under an iron band.
        gfx.fill(gx + BELL_X0 - 2, gy + SHOULDER_Y0, gx + BELL_X1 + 2, gy + SHOULDER_Y1, IRON);
        ScreenDrawUtils.drawBevelBorder(gfx, gx + BELL_X0 - 2, gy + SHOULDER_Y0, BELL_X1 - BELL_X0 + 4,
                SHOULDER_Y1 - SHOULDER_Y0, IRON_LIGHT, IRON_DARK);
    }

    /** Neck glass, crown band, vent cap with its glowing mouth, and the bone-gripped carry bail. */
    private void renderCrown(GuiGraphics gfx, int gx, int gy) {
        int nx0 = gx + NECK_X0, nx1 = gx + NECK_X1, ny0 = gy + NECK_Y0, ny1 = gy + NECK_Y1;
        gfx.fill(nx0, ny0, nx1, ny1, GLASS_DEPTH);
        gfx.fill(nx0, ny0, nx1, ny1, GLASS_TINT);
        gfx.fill(nx0 + 3, ny0 + 1, nx0 + 4, ny1, GLASS_GLINT);
        for (int i = 0; i < 4; i++) {
            float rise = (animTime * (6f + i * 2f) + i * 7f) % (ny1 - ny0 + 2);
            int sx = nx0 + 9 + i * 8;
            int sy = ny1 - 1 - (int) rise;
            if (sy >= ny0) gfx.fill(sx, sy, sx + 1, sy + 1, 0x90E08A3A);
        }
        gfx.fill(nx0 - 2, ny0, nx0, ny1, IRON);
        gfx.fill(nx1, ny0, nx1 + 2, ny1, IRON_DARK);

        // Vent cap, bone band, vent and its mouth.
        gfx.fill(gx + 72, gy + 13, gx + 104, gy + 17, IRON);
        ScreenDrawUtils.drawBevelBorder(gfx, gx + 72, gy + 13, 32, 4, IRON_LIGHT, IRON_DARK);
        gfx.fill(gx + 79, gy + 10, gx + 97, gy + 13, BONE);
        gfx.fill(gx + 79, gy + 12, gx + 97, gy + 13, BONE_DARK);
        gfx.fill(gx + 83, gy + 7, gx + 93, gy + 10, IRON);
        gfx.fill(gx + 83, gy + 7, gx + 93, gy + 8, IRON_LIGHT);
        float pulse = 0.6f + 0.4f * Mth.sin(animTime * 2.2f);
        gfx.fill(gx + 85, gy + 6, gx + 91, gy + 7, ((int) (220 * pulse) << 24) | 0xFF7A2A);

        // Bail: two posts, a bar and a bone grip.
        gfx.fill(gx + 74, gy + 2, gx + 76, gy + 13, IRON);
        gfx.fill(gx + 100, gy + 2, gx + 102, gy + 13, IRON);
        gfx.fill(gx + 74, gy + 1, gx + 102, gy + 3, IRON);
        gfx.fill(gx + 74, gy + 1, gx + 102, gy + 2, IRON_LIGHT);
        gfx.fill(gx + 81, gy, gx + 95, gy + 3, BONE);
        gfx.fill(gx + 81, gy + 2, gx + 95, gy + 3, BONE_DARK);
    }

    // ── Slots ────────────────────────────────────────────────────

    private static void drawSlotBackground(GuiGraphics gfx, int sx, int sy, int slotIndex) {
        if (slotIndex == MycelialLanternMenu.OUTPUT_SLOT) {
            // The harvest dish: a bone-rimmed well wider than the slot.
            gfx.fill(sx - 4, sy - 4, sx + 20, sy + 20, BONE_DARK);
            ScreenDrawUtils.drawBevelBorder(gfx, sx - 4, sy - 4, 24, 24, BONE, IRON_DARK);
        } else if (slotIndex == MycelialLanternMenu.CULTURE_SLOT) {
            // The culture stands in the glass, ringed faintly rather than sunk in a dark well.
            gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, 0x60140804);
            ScreenDrawUtils.drawSimpleBorder(gfx, sx - 2, sy - 2, 20, 20, 0x50DD8822);
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x18CC7700);
            return;
        }
        ScreenDrawUtils.drawSlotWell(gfx, sx, sy, SLOT_BORDER_DARK, SLOT_BG, SLOT_BORDER_LIGHT);

        if (slotIndex == MycelialLanternMenu.BLOOD_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x24AA0000);
        } else if (slotIndex == MycelialLanternMenu.OUTPUT_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x22FFAA44);
        } else if (slotIndex == MycelialLanternMenu.EMPTY_CONTAINER_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x18DD8822);
        }
    }

    // ── Behind the glass ─────────────────────────────────────────

    /** Dark glass depth, a warm core glow, hyphae climbing from the bed and spores rising into the bell. */
    private void renderFungalBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
        animTime += 0.016f;
        float time = animTime;

        gfx.enableScissor(gx, gy, gx + gw, gy + gh);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        gfx.fill(gx, gy, gx + gw, gy + gh, GLASS_DEPTH);

        int cx = gx + gw / 2;
        int cy = gy + gh / 2;
        int glowRadius = Math.max(gw, gh) / 2;
        for (int ring = glowRadius; ring > 0; ring -= 3) {
            float t = (float) ring / glowRadius;
            int alpha = (int) (32 * (1f - t));
            int r = (int) (62 * (1f - t));
            int g = (int) (28 * (1f - t));
            int b = (int) (6 * (1f - t));
            gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (r << 16) | (g << 8) | b);
        }

        if (tendrilParams != null) {
            for (int i = 0; i < TENDRIL_COUNT; i++) {
                drawFungalTendril(gfx, i, time, gx, gy, gw, gh);
            }
        }

        if (sporeParams != null) {
            drawFloatingSpores(gfx, time, gx, gy, gw, gh);
        }

        if (speckleParams != null) {
            for (int[] sp : speckleParams) {
                int sx = gx + sp[0];
                int sy = gy + sp[1];
                gfx.fill(sx, sy, sx + 1, sy + 1, (sp[4] << 24) | (sp[2] << 16) | (sp[3] << 8));
            }
        }

        RenderSystem.disableBlend();
        gfx.disableScissor();
    }

    private void drawFungalTendril(GuiGraphics gfx, int index, float time, int gx, int gy, int gw, int gh) {
        float[] p = tendrilParams[index];
        float startX = gx + p[0] * gw;
        float startY = gy + p[1] * gh;
        float angle = p[2] + 0.12f * Mth.sin(time * p[3] * 0.4f + index);
        float cosA = Mth.cos(angle);
        float sinA = Mth.sin(angle);
        float timeOffset = time * p[3] * 1.5f;
        int length = (int) p[6];
        int thickness = (int) p[7];
        float brightness = p[8];

        int baseRed = (int) (50 + 80 * brightness);
        int baseGreen = (int) (20 + 40 * brightness);
        int baseBlue = (int) (2 + 8 * brightness);

        for (int step = 0; step < length; step++) {
            float squiggle = p[4] * Mth.sin(p[5] * step + timeOffset);
            float microSquiggle = p[4] * 0.25f * Mth.sin(p[5] * 2.5f * step + timeOffset * 1.3f + index);
            float displacement = squiggle + microSquiggle;
            int ix = (int) (startX + step * cosA - displacement * sinA);
            int iy = (int) (startY + step * sinA + displacement * cosA);

            if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) continue;

            float tipFade = 1f;
            if (step < 4) tipFade = step / 4f;
            else if (step > length - 6) tipFade = (length - step) / 6f;

            float pulse = 0.65f + 0.35f * Mth.sin(time * 1.2f + index * 0.6f + step * 0.05f);
            int a = (int) Mth.clamp(tipFade * pulse * 160, 15, 180);
            int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
            int g = (int) Mth.clamp(baseGreen * pulse, 0, 255);
            int b = (int) Mth.clamp(baseBlue * pulse * 0.4f, 0, 255);
            gfx.fill(ix, iy, ix + thickness, iy + thickness, (a << 24) | (r << 16) | (g << 8) | b);
        }
    }

    private void drawFloatingSpores(GuiGraphics gfx, float time, int gx, int gy, int gw, int gh) {
        for (int i = 0; i < SPORE_COUNT; i++) {
            float[] p = sporeParams[i];
            float yOffset = ((time * p[2] * 8f + p[3]) % (gh + 10)) - 5;
            float xWobble = 3f * Mth.sin(time * p[2] * 2f + p[3]);

            int sx = gx + Math.floorMod((int) (p[0] * gw + xWobble), gw);
            int sy = gy + gh - (int) yOffset;
            if (sy < gy || sy >= gy + gh || sx < gx || sx >= gx + gw) continue;

            float pulse = 0.5f + 0.5f * Mth.sin(time * 3f + p[3]);
            int alpha = (int) (40 + 60 * pulse * p[4]);
            int r = (int) (120 + 80 * p[4]);
            int g = (int) (60 + 40 * p[4]);
            int b = (int) (5 + 10 * p[4]);
            gfx.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | (r << 16) | (g << 8) | b);
        }
    }
}
