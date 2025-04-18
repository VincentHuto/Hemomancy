package com.vincenthuto.hemomancy.client.screen;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.menu.TendancyViewMenu;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.extensions.common.IClientMobEffectExtensions;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TendancyViewScreen extends EffectRenderingInventoryScreen<TendancyViewMenu> {

    public static final ResourceLocation background = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/gui/tendancy_view.png");
    public static final ResourceLocation border = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/gui/tendancy_border.png");
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

    public TendancyViewScreen(TendancyViewMenu container, Inventory inventory, Component name) {
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
        this.left = this.width / 2 - this.guiWidth / 2;
        this.top = this.height / 2 - this.guiHeight / 2;
        this.clearWidgets();
        super.init();
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
       this.renderBackground(graphics); // renderBackground
        this.renderBg(graphics, partialTicks, mouseX, mouseY);
        this.renderTooltip(graphics, mouseX, mouseY); // renderHoveredToolTip
        this.oldMouseX = mouseX;
        this.oldMouseY = mouseY;
        int centerX = this.width / 2 - this.guiWidth / 2;
        int centerY = this.height / 2 - this.guiHeight / 2;
        graphics.blit(border, centerX, centerY, 0, 0, this.guiWidth, this.guiHeight);
        drawCenter(graphics, centerX-3, centerY+60);

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
        this.left = this.width / 2 - this.guiWidth / 2;
        this.top = this.height / 2 - this.guiHeight / 2;
        int centerX = this.width / 2 - this.guiWidth / 2;
        int centerY = this.height / 2 - this.guiHeight / 2;
        graphics.blit(background, centerX + 16, centerY + 14, 0, 0, this.guiWidth, this.guiHeight);

    }

    private void drawCenter(GuiGraphics graphics, int xOff, int yOff) {
        LocalPlayer player = this.minecraft.player;
        player.getCapability(BloodTendencyProvider.TENDENCY_CAPA).ifPresent(tendency -> {
            Map<EnumBloodTendency, Float> affs = tendency.getTendency();
            int centerOffset = 8;
            int cx = 0, cy = 0;
            float rotAngle = -90f;
            int iconDiameter = 95;
            int diameter =15;
            float spikeBaseWidth = 23.5f;
            for (EnumBloodTendency tend : EnumBloodTendency.values()) {
               float affVal = (float) Mth.clamp(affs.get(tend),0,1);
                int cx1 = (int) (cx + Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + xOff + 90;
                int cx2 = (int) (cx + Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + xOff + 90;
                int cy1 = (int) (cy + Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * diameter) + yOff + 47;
                int cy2 = (int) (cy + Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * diameter) + yOff + 47;
                double depthDist = ((iconDiameter - diameter) *  affVal* 0.5 + diameter);
                int lx = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * depthDist) + xOff + 90;
                int ly = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * depthDist) + yOff + 47;
                int displace = (int) ((Math.max(cx1, cx2) - Math.min(cx1, cx2) + Math.max(cy1, cy2) - Math.min(cy1, cy2))
                        / 2f);
                int zLevel = 10;
                HLGuiUtils.fracLine(graphics.pose(), lx + centerOffset, ly + centerOffset, cx1 + centerOffset,
                        cy1 + centerOffset, zLevel, tend.getColor(), displace, 1.1);
                HLGuiUtils.fracLine(graphics.pose(), lx + centerOffset, ly + centerOffset, cx2 + centerOffset,
                        cy2 + centerOffset, zLevel, tend.getColor(), displace, 1.1);
                HLGuiUtils.fracLine(graphics.pose(), cx1 + centerOffset, cy1 + 8, lx + centerOffset, ly + centerOffset,
                        zLevel, tend.getColor(), displace, 0.8);
                HLGuiUtils.fracLine(graphics.pose(), cx2 + centerOffset, cy2 + centerOffset, lx + centerOffset,
                        ly + centerOffset, zLevel, tend.getColor(), displace, 0.8);
                int newX = (int) (cx + Math.cos(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
                int newY = (int) (cy + Math.sin(Math.toRadians(rotAngle)) * iconDiameter / 1.75);
                graphics.drawCenteredString(font,
                        HLTextUtils.toProperCase(tend.toString()),  newX + xOff + 95, newY + yOff + 27,
                        new Color(255, 0, 0, 255).getRGB());
                graphics.drawCenteredString(font,
                        String.valueOf(tendency.getAlignmentByTendency(tend)),    newX + xOff + 90,newY + yOff + 37,
                        new Color(255, 0, 0, 255).getRGB());
                HLGuiUtils.renderItemStackInGui(graphics, new ItemStack(EnumBloodTendency.getRepEnzyme(tend)),
                        newX + xOff + 90, newY + yOff + 47);
                rotAngle += 45;
            }
        });


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