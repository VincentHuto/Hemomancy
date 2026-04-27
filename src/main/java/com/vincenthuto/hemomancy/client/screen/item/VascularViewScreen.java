package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.menu.VascularViewMenu;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class VascularViewScreen extends EffectRenderingInventoryScreen<VascularViewMenu> {

    public static final ResourceLocation background = Hemomancy.rloc("textures/gui/vascular_view.png");
    public static final ResourceLocation border = Hemomancy.rloc("textures/gui/vascular_border.png");
    public double dragLeftRight = 0.0;
    public double dragUpDown = 0.0;
    public int guiHeight = 254;
    public int guiWidth = 190;
    protected int left;
    protected int top;
    protected Minecraft mc = Minecraft.getInstance();
    double xDragPos = 0.0;
    double yDragPos = 0.0;
    private float oldMouseX;
    private float oldMouseY;

    /** Number of animated vein tendrils swimming across the background. */
    private static final int VEIN_COUNT = 28;
    private static final float VEIN_ANIMATION_SPEED = 0.35f;
    /** Per-vein parameters: startX/Y ratio, angle, speed, amplitude, frequency, length, thickness, brightness */
    private float[][] veinParams;

    public VascularViewScreen(VascularViewMenu container, Inventory inventory, Component name) {
        super(container, inventory, name);
    }


    @Override
    protected void init() {
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
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
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

        // Draw content on top of vein background
        drawContent(graphics, centerX, centerY);
    }
    // ───── Programmatic Dark-Red Border ─────

    private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
        int outer = 0xFF330808;
        gfx.fill(x, y, x + w, y + 1, outer);
        gfx.fill(x, y + h - 1, x + w, y + h, outer);
        gfx.fill(x, y, x + 1, y + h, outer);
        gfx.fill(x + w - 1, y, x + w, y + h, outer);

        int inner = 0xFF220606;
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, inner);
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, inner);
        gfx.fill(x + 1, y + 1, x + 2, y + h - 1, inner);
        gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, inner);
    }

    // ───── Procedural Animated Vein Background ─────
    private float animTime = 0f;
    private long lastAnimMillis = -1L;

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
        long now = Util.getMillis();
        if (lastAnimMillis >= 0L) {
            float deltaSeconds = Math.min((now - lastAnimMillis) / 1000.0f, 0.05f);
            animTime += deltaSeconds * VEIN_ANIMATION_SPEED;
        }
        lastAnimMillis = now;

        float time = animTime;
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
            float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
            float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + index);
            float displacement = squiggle + microSquiggle;

            float px = startX + step * cosA * 1.5f - displacement * sinA;
            float py = startY + step * sinA * 1.5f + displacement * cosA;

            int ix = (int) px;
            int iy = (int) py;

            if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) {
                continue;
            }

            float tipFade = 1f;
            if (step < 10) tipFade = step / 10f;
            else if (step > length - 10) tipFade = (length - step) / 10f;

            float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

            int a = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
            int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
            int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
            int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

            graphics.fill(ix, iy, ix + thickness, iy + thickness,
                    (a << 24) | (r << 16) | (g << 8) | b);
        }
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
            int k = 33;
            if (collection.size() > 5) {
                k = 132 / (collection.size() - 1);
            }

            Iterable<MobEffectInstance> iterable = collection.stream()
                .filter(effect -> net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions.of(effect).isVisibleInInventory(effect))
                .sorted().collect(Collectors.toList());
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
                    java.util.List<Component> list = List.of(this.getEffectName(mobeffectinstance), MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, 1.0F));
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
                Component component1 = MobEffectUtil.formatDuration(mobeffectinstance, 1.0F, 1.0F);
                pGuiGraphics.drawString(this.font, component1, pRenderX + 10 + 18, i + 6 + 10, 8355711);
                i += pYOffset;
            }
        }
    }
    private Component getEffectName(MobEffectInstance pEffect) {
        MutableComponent mutablecomponent = pEffect.getEffect().value().getDisplayName().copy();
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
                    TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffectinstance.getEffect());
                pGuiGraphics.blit(pRenderX + (pIsSmall ? 6 : 7), i + 7, 0, 18, 18, textureatlassprite);
                i += pYOffset;
            }
        }
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        // Drawn manually from render(); calling the container background here re-enters renderBg().
    }


    @Override
      public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
    }

    /**
     * Draws the vascular system content (entity model, vein section labels, etc.)
     * on top of the vein background. Called from render() after the background and border.
     */
    private void drawContent(GuiGraphics graphics, int centerX, int centerY) {
        this.left = this.width / 2 - this.guiWidth / 2;
        this.top = this.height / 2 - this.guiHeight / 2;
        int k = this.leftPos;
        int l = this.topPos;

        LocalPlayer player = this.minecraft.player;
        HemoCapabilityAccess.getVascularSystem(player).ifPresent(vascularSystem -> {
            ItemStack stack = player.getMainHandItem();
            Item item = stack.getItem();
            Item renderItem = ItemInit.dried_leech.get();
            double angleBetweenEach = 360.0 / EnumVeinSections.values().length;
            Point point = new Point(centerX - 45, centerY - 36), center = new Point(centerX, centerY);
            for (int i = 0; i < vascularSystem.getVascularSystem().keySet().size(); i++) {
                EnumVeinSections selectedSection = (EnumVeinSections) vascularSystem.getVascularSystem().keySet()
                        .toArray()[i];
                graphics.drawCenteredString(font,
                        HLTextUtils.toProperCase(selectedSection.toString()), point.x + guiWidth / 2, point.y -20 + guiHeight / 2,
                        new Color(255, 0, 0, 255).getRGB());
                graphics.drawCenteredString(font,
                        String.valueOf(vascularSystem.getBloodFlowBySection(selectedSection)), point.x + guiWidth / 2, point.y -30  + guiHeight / 2,
                        new Color(255, 0, 0, 255).getRGB());
                graphics.renderItem(new ItemStack(renderItem),  point.x-8 + guiWidth / 2, point.y -10 + guiHeight / 2);

                point = rotatePointAbout(point, center, angleBetweenEach);
            }

        });

            int entityCenterX = k - 7 + guiWidth / 2;
            int entityCenterY = l - 30 + guiHeight / 2;
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, entityCenterX - 25, entityCenterY - 35,
                entityCenterX + 25, entityCenterY + 35, 30, 0.0625F,
                this.oldMouseX, this.oldMouseY, player);
    }
    private static Point rotatePointAbout(Point in, Point about, double degrees) {
        double rad = degrees * Math.PI / 180.0;
        double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
        double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
        return new Point((int) newX, (int) newY);
    }

    //	public static void openScreenViaItem(int pNum, BookCodeModel pBook, ChapterTemplate pChapterTemplate) {
//		Minecraft mc = Minecraft.getInstance();
//		mc.setScreen(new VascularViewScreen());
//	}
    @Override
    public boolean mouseDragged(double xPos, double yPos, int button, double dragLeftRight, double dragUpDown) {
        this.xDragPos = xPos;
        this.yDragPos = yPos;
        this.dragLeftRight += dragLeftRight / 2.0;
        this.dragUpDown -= dragUpDown / 2.0;
        return super.mouseDragged(xPos, yPos, button, dragLeftRight, dragUpDown);
    }


}
