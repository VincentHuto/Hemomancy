package com.vincenthuto.hemomancy.client.screen.item;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.menu.TendencyViewMenu;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ForgeHooksClient;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class TendencyViewScreen extends EffectRenderingInventoryScreen<TendencyViewMenu> {

    private static final int PREFERRED_GUI_WIDTH = 190;
    private static final int PREFERRED_GUI_HEIGHT = 254;

    public double dragLeftRight = 0.0;
    public double dragUpDown = 0.0;
    public int guiHeight = PREFERRED_GUI_HEIGHT;
    public int guiWidth = PREFERRED_GUI_WIDTH;
    /** Scale factor (0-1) of actual size vs preferred size, used to shrink content proportionally. */
    private float guiScale = 1.0f;
    protected int left;
    protected int top;
    protected Minecraft mc = Minecraft.getInstance();
    double xDragPos = 0.0;
    double yDragPos = 0.0;
    private float oldMouseX;
    private float oldMouseY;

    /** Number of animated vein tendrils swimming across the background. */
    private static final int VEIN_COUNT = 28;
    /** Per-vein parameters: startX/Y ratio, angle, speed, amplitude, frequency, length, thickness, brightness */
    private float[][] veinParams;

    public TendencyViewScreen(TendencyViewMenu container, Inventory inventory, Component name) {
        super(container, inventory, name);
    }

    private static Point rotatePointAbout(Point in, Point about, double degrees) {
        double rad = degrees * Math.PI / 180.0;
        double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
        double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
        return new Point((int) newX, (int) newY);
    }

    @Override
    protected void init() {
        // Compute GUI dimensions that fit within the screen with some padding
        int padding = 10;
        this.guiWidth = Math.min(PREFERRED_GUI_WIDTH, this.width - padding * 2);
        this.guiHeight = Math.min(PREFERRED_GUI_HEIGHT, this.height - padding * 2);
        this.guiScale = Math.min((float) guiWidth / PREFERRED_GUI_WIDTH, (float) guiHeight / PREFERRED_GUI_HEIGHT);

        this.left = this.width / 2 - this.guiWidth / 2;
        this.top = this.height / 2 - this.guiHeight / 2;
        this.clearWidgets();
        super.init();

        // Seed vein parameters for the animated background
        Random rand = new Random(42L);
        veinParams = new float[VEIN_COUNT][9];
        for (int i = 0; i < VEIN_COUNT; i++) {
            veinParams[i][0] = rand.nextFloat();                          // startX ratio
            veinParams[i][1] = rand.nextFloat();                          // startY ratio
            veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2); // base angle
            veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;           // speed mult
            veinParams[i][4] = 8f + rand.nextFloat() * 18f;              // amplitude
            veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;         // frequency
            veinParams[i][6] = 60 + rand.nextInt(120);                    // length (steps)
            veinParams[i][7] = 1 + rand.nextInt(3);                       // thickness
            veinParams[i][8] = rand.nextFloat();                           // red tint brightness
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderEffects(graphics, mouseX, mouseY);
        this.renderBackground(graphics);
        this.renderBg(graphics, partialTicks, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY);
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
        int centerX = this.width / 2 - this.guiWidth / 2;
        int centerY = this.height / 2 - this.guiHeight / 2;

        // Animated vein background clipped to the GUI bounds
        renderVeinBackground(graphics, centerX, centerY, this.guiWidth, this.guiHeight);

        // Programmatic dark-red border frame on top of the vein background
        drawBorder(graphics, centerX, centerY, this.guiWidth, this.guiHeight);

        // Title
        graphics.drawCenteredString(font, "Blood Tendency", this.width / 2, centerY + 6, 0xFFCC3344);
        graphics.drawCenteredString(font, "Alignment by kinship", this.width / 2, centerY + 18, 0xFF553333);

        // Pass the true center of the GUI panel to drawCenter
        int starCenterX = this.width / 2;
        int starCenterY = this.height / 2;
        drawCenter(graphics, starCenterX, starCenterY);

        // Footer
        graphics.drawCenteredString(font, "§4§l— §c§oSanguine Kinship §4§l—",
                this.width / 2, centerY + this.guiHeight - 14, 0xFF882222);
    }

    private void renderBackgrounds(GuiGraphics pGuiGraphics, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects, boolean pIsSmall) {
        int i = this.topPos;

        for (MobEffectInstance mobeffectinstance : pEffects) {
            if (pIsSmall) {
                pGuiGraphics.blit(INVENTORY_LOCATION, pRenderX, i, 0, 166, 120, 32);
            } else {
                pGuiGraphics.blit(INVENTORY_LOCATION, pRenderX, i, 0, 198, 32, 32);
            }

            i += pYOffset;
        }
    }

    private void renderEffects(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        int i = this.leftPos + this.imageWidth + 2;
        int j = this.width - i;
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty() && j >= 32) {
            boolean flag = j >= 120;
            ScreenEvent.RenderInventoryMobEffects event = ForgeHooksClient.onScreenPotionSize(this, j, !flag, i);
            if (event.isCanceled()) {
                return;
            }

            flag = !event.isCompact();
            i = event.getHorizontalOffset();
            int k = 33;
            if (collection.size() > 5) {
                k = 132 / (collection.size() - 1);
            }

            Iterable<MobEffectInstance> iterable = collection.stream().filter(ForgeHooksClient::shouldRenderEffect).sorted().collect(Collectors.toList());
            this.renderBackgrounds(pGuiGraphics, i, k, iterable, flag);
            this.renderIcons(pGuiGraphics, i, k, iterable, flag);
            if (flag) {
                this.renderLabels(pGuiGraphics, i, k, iterable);
            } else if (pMouseX >= i && pMouseX <= i + 33) {
                int l = this.topPos;
                MobEffectInstance mobeffectinstance = null;

                for (MobEffectInstance mobeffectinstance1 : iterable) {
                    if (pMouseY >= l && pMouseY <= l + k) {
                        mobeffectinstance = mobeffectinstance1;
                    }

                    l += k;
                }

                if (mobeffectinstance != null) {
                    List<Component> list = List.of(this.getEffectName(mobeffectinstance), MobEffectUtil.formatDuration(mobeffectinstance, 1.0F));
                    pGuiGraphics.renderTooltip(this.font, list, Optional.empty(), pMouseX, pMouseY);
                }
            }
        }
    }

    private void renderLabels(GuiGraphics pGuiGraphics, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects) {
        int i = this.topPos;

        for (MobEffectInstance mobeffectinstance : pEffects) {
            IClientMobEffectExtensions renderer = IClientMobEffectExtensions.of(mobeffectinstance);
            if (renderer.renderInventoryText(mobeffectinstance, this, pGuiGraphics, pRenderX, i, 0)) {
                i += pYOffset;
            } else {
                Component component = this.getEffectName(mobeffectinstance);
                pGuiGraphics.drawString(this.font, component, pRenderX + 10 + 18, i + 6, 16777215);
                Component component1 = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F);
                pGuiGraphics.drawString(this.font, component1, pRenderX + 10 + 18, i + 6 + 10, 8355711);
                i += pYOffset;
            }
        }
    }

    private Component getEffectName(MobEffectInstance pEffect) {
        MutableComponent mutablecomponent = pEffect.getEffect().getDisplayName().copy();
        if (pEffect.getAmplifier() >= 1 && pEffect.getAmplifier() <= 9) {
            mutablecomponent.append(CommonComponents.SPACE).append(Component.translatable("enchantment.level." + (pEffect.getAmplifier() + 1)));
        }

        return mutablecomponent;
    }

    private void renderIcons(GuiGraphics pGuiGraphics, int pRenderX, int pYOffset, Iterable<MobEffectInstance> pEffects, boolean pIsSmall) {
        MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
        int i = this.topPos;

        for (MobEffectInstance mobeffectinstance : pEffects) {
            IClientMobEffectExtensions renderer = IClientMobEffectExtensions.of(mobeffectinstance);
            if (renderer.renderInventoryIcon(mobeffectinstance, this, pGuiGraphics, pRenderX + (pIsSmall ? 6 : 7), i, 0)) {
                i += pYOffset;
            } else {
                MobEffect mobeffect = mobeffectinstance.getEffect();
                TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffect);
                pGuiGraphics.blit(pRenderX + (pIsSmall ? 6 : 7), i + 7, 0, 18, 18, textureatlassprite);
                i += pYOffset;
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        super.renderBackground(guiGraphics);
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        super.renderBackground(graphics);
        this.left = this.width / 2 - guiWidth / 2;
        this.top = this.height / 2 - guiHeight / 2;
    }

    private void drawCenter(GuiGraphics graphics, int centerX, int centerY) {
        LocalPlayer player = this.minecraft.player;
        player.getCapability(BloodTendencyProvider.TENDENCY_CAPA).ifPresent(tendency -> {
            Map<EnumBloodTendency, Float> affs = tendency.getTendency();
            float rotAngle = -90f;
            int iconDiameter = (int) (95 * guiScale);
            int diameter = (int) (15 * guiScale);
            float spikeBaseWidth = 23.5f;
            int itemSize = 16;
            int halfItem = itemSize / 2;
            for (EnumBloodTendency tend : EnumBloodTendency.values()) {
                float affVal = (float) Mth.clamp(affs.get(tend), 0, 1);

                // Spike base points (left and right edges of the base)
                int cx1 = centerX + (int) (Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter);
                int cy1 = centerY + (int) (Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter);
                int cx2 = centerX + (int) (Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter);
                int cy2 = centerY + (int) (Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter);

                // Spike tip (extends outward based on tendency value)
                double depthDist = ((iconDiameter - diameter) * affVal * 0.5 + diameter);
                int lx = centerX + (int) (Math.cos(Math.toRadians(rotAngle)) * depthDist);
                int ly = centerY + (int) (Math.sin(Math.toRadians(rotAngle)) * depthDist);

                int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2)) / 2f);
                int zLevel = 10;

                // Draw the four fractal lines forming the spike
                HLGuiUtils.fracLine(graphics.pose(), lx, ly, cx1, cy1, zLevel, tend.getColor(), displace, 1.1);
                HLGuiUtils.fracLine(graphics.pose(), lx, ly, cx2, cy2, zLevel, tend.getColor(), displace, 1.1);
                HLGuiUtils.fracLine(graphics.pose(), cx1, cy1, lx, ly, zLevel, tend.getColor(), displace, 0.8);
                HLGuiUtils.fracLine(graphics.pose(), cx2, cy2, lx, ly, zLevel, tend.getColor(), displace, 0.8);

                // Radial icon position (outside the star) - spikes point here
                double iconDist = iconDiameter / 1.75;
                int iconCenterX = centerX + (int) (Math.cos(Math.toRadians(rotAngle)) * iconDist);
                int iconCenterY = centerY + (int) (Math.sin(Math.toRadians(rotAngle)) * iconDist);

                // Enzyme item icon (centered on the radial point)
                HLGuiUtils.renderItemStackInGui(graphics, new ItemStack(EnumBloodTendency.getRepEnzyme(tend)),
                        iconCenterX - halfItem, iconCenterY - halfItem);

                // Tendency value (centered above the icon) — tinted to the tendency's color
                int tendColor = 0xFF000000 | tend.getColor().getColor();
                graphics.drawCenteredString(font,
                        String.valueOf(tendency.getAlignmentByTendency(tend)), iconCenterX, iconCenterY - halfItem - 10,
                        tendColor);

                // Tendency name (centered above the value) — dimmer version of tendency color
                int dimColor = ((tendColor >> 1) & 0x7F7F7F) | 0xFF000000;
                graphics.drawCenteredString(font,
                        HLTextUtils.toProperCase(tend.toString()), iconCenterX, iconCenterY - halfItem - 20,
                        dimColor);

                rotAngle += 45;
            }
        });


    }

    // ───── Gradient Dark-Red Border ─────

    private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
        int[] colors = {
            0xFF6B1010,  // outermost — bright crimson
            0xFF4A0C0C,  // mid-outer
            0xFF300808,  // mid-inner
            0xFF1A0404,  // innermost — near-black maroon
        };
        for (int i = 0; i < colors.length; i++) {
            int c = colors[i];
            int xi = x + i;
            int yi = y + i;
            int wi = w - i * 2;
            int hi = h - i * 2;
            gfx.fill(xi, yi, xi + wi, yi + 1, c);
            gfx.fill(xi, yi + hi - 1, xi + wi, yi + hi, c);
            gfx.fill(xi, yi, xi + 1, yi + hi, c);
            gfx.fill(xi + wi - 1, yi, xi + wi, yi + hi, c);
        }
        int hl = 0xFF8A1818;
        gfx.fill(x, y, x + 2, y + 2, hl);
        gfx.fill(x + w - 2, y, x + w, y + 2, hl);
        gfx.fill(x, y + h - 2, x + 2, y + h, hl);
        gfx.fill(x + w - 2, y + h - 2, x + w, y + h, hl);
    }

    // ───── Procedural Animated Vein Background ─────

    /**
     * Renders a dark red/black background covered in swimming, squiggling vein tendrils.
     * All rendering is clipped to the given GUI bounds.
     */
    private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
        graphics.enableScissor(gx, gy, gx + gw, gy + gh);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Layer 1: solid near-black base filling GUI area
        graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

        // Layer 2: subtle dark-red radial glow in the center of the GUI
        int cx = gx + gw / 2;
        int cy = gy + gh / 2;
        int glowRadius = Math.max(gw, gh) / 2;
        for (int ring = glowRadius; ring > 0; ring -= 4) {
            float t = (float) ring / glowRadius;
            int alpha = (int) (35 * (1f - t));
            int red = (int) (40 * (1f - t));
            int color = (alpha << 24) | (red << 16);
            graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
        }

        // Layer 3: animated vein tendrils
        float time = (System.nanoTime() / 1_000_000_000f);

        if (veinParams != null) {
            for (int i = 0; i < VEIN_COUNT; i++) {
                drawVeinTendril(graphics, i, time, gx, gy, gw, gh);
            }
        }

        // Layer 4: subtle noise-like speckles for organic texture
        Random speckRand = new Random(12345L);
        for (int s = 0; s < 120; s++) {
            int sx = gx + speckRand.nextInt(gw);
            int sy = gy + speckRand.nextInt(gh);
            int sr = 10 + speckRand.nextInt(20);
            int sg = speckRand.nextInt(6);
            int sa = 15 + speckRand.nextInt(25);
            graphics.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
        }

        RenderSystem.disableBlend();
        graphics.disableScissor();
    }

    /**
     * Draws a single animated vein tendril as a squiggling curve within the GUI bounds.
     */
    private void drawVeinTendril(GuiGraphics graphics, int index, float time,
                                 int gx, int gy, int gw, int gh) {
        float[] p = veinParams[index];
        float startX = gx + p[0] * gw;
        float startY = gy + p[1] * gh;
        float baseAngle = p[2];
        float speed = p[3];
        float amplitude = p[4];
        float frequency = p[5];
        int length = (int) p[6];
        int thickness = (int) p[7];
        float brightness = p[8];

        float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);
        float cosA = Mth.cos(angleDrift);
        float sinA = Mth.sin(angleDrift);

        float timeOffset = time * speed * 2.0f;

        int baseRed = (int) (40 + 50 * brightness);
        int baseGreen = (int) (2 + 8 * brightness);
        int baseBlue = (int) (5 + 5 * brightness);

        for (int step = 0; step < length; step++) {
            float t = step;

            float squiggle = amplitude * Mth.sin(frequency * t + timeOffset);
            float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * t + timeOffset * 1.4f + index);
            float displacement = squiggle + microSquiggle;

            float px = startX + t * cosA * 1.5f - displacement * sinA;
            float py = startY + t * sinA * 1.5f + displacement * cosA;

            int ix = (int) px;
            int iy = (int) py;

            if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
                continue;
            }

            float tipFade = 1f;
            if (step < 10) tipFade = step / 10f;
            else if (step > length - 10) tipFade = (length - step) / 10f;

            float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

            int alpha = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
            int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
            int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
            int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            graphics.fill(ix, iy, ix + thickness, iy + thickness, color);
        }
    }

    @Override
    public boolean mouseDragged(double xPos, double yPos, int button, double dragLeftRight, double dragUpDown) {
        this.xDragPos = xPos;
        this.yDragPos = yPos;
        this.dragLeftRight += dragLeftRight / 2.0;
        this.dragUpDown -= dragUpDown / 2.0;
        return super.mouseDragged(xPos, yPos, button, dragLeftRight, dragUpDown);
    }


}