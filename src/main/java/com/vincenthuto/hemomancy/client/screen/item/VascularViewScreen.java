package com.vincenthuto.hemomancy.client.screen.item;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.manip.HematicMemoryExpression;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarNoeticRoutingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import com.vincenthuto.hemomancy.common.menu.VascularViewMenu;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.Util;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import com.vincenthuto.hemomancy.client.screen.skilltree.harbinger.VeinBackgroundRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;

public class VascularViewScreen extends EffectRenderingInventoryScreen<VascularViewMenu> {

    public static final ResourceLocation background = Hemomancy.rloc("textures/gui/vascular_view.png");
    public static final ResourceLocation border = Hemomancy.rloc("textures/gui/vascular_border.png");
    public double dragLeftRight = 0.0;
    public double dragUpDown = 0.0;
    public int guiHeight = 254;
    public int guiWidth = 300;
    protected int left;
    protected int top;
    protected Minecraft mc = Minecraft.getInstance();
    double xDragPos = 0.0;
    double yDragPos = 0.0;
    private float oldMouseX;
    private float oldMouseY;

    /** Number of animated vein tendrils swimming across the background. */
    private static final float VEIN_ANIMATION_SPEED = 0.35f;
    /** Per-vein parameters: startX/Y ratio, angle, speed, amplitude, frequency, length, thickness, brightness */
    private final VeinBackgroundRenderer veins = new VeinBackgroundRenderer(VeinBackgroundRenderer.Style.CRIMSON);

    public VascularViewScreen(VascularViewMenu container, Inventory inventory, Component name) {
        super(container, inventory, name);
    }


    @Override
    protected void init() {
        this.guiWidth = Math.min(300, this.width - 16);
        this.guiHeight = Math.min(254, this.height - 16);
        this.imageWidth = this.guiWidth;
        this.imageHeight = this.guiHeight;
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
        veins.draw(graphics, centerX, centerY, this.guiWidth, this.guiHeight, advanceVeinClock());

        // Programmatic dark-red border frame on top of the vein background
        ScreenDrawUtils.drawBorder(graphics, centerX, centerY, this.guiWidth, this.guiHeight, 0xFF330808, 0xFF220606);

        // Draw content on top of vein background
        drawContent(graphics, centerX, centerY, mouseX, mouseY);
    }

    // Vein clock: wall time, capped per frame, slowed by VEIN_ANIMATION_SPEED.
    private float animTime = 0f;
    private long lastAnimMillis = -1L;

    private float advanceVeinClock() {
        long now = Util.getMillis();
        if (lastAnimMillis >= 0L) {
            float deltaSeconds = Math.min((now - lastAnimMillis) / 1000.0f, 0.05f);
            animTime += deltaSeconds * VEIN_ANIMATION_SPEED;
        }
        lastAnimMillis = now;
        return animTime;
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


    /**
     * Draws the vascular system content (entity model, vein section labels, etc.)
     * on top of the vein background. Called from render() after the background and border.
     */
    private void drawContent(GuiGraphics graphics, int centerX, int centerY, int mouseX, int mouseY) {
        this.left = this.width / 2 - this.guiWidth / 2;
        this.top = this.height / 2 - this.guiHeight / 2;
        int k = this.leftPos;
        int l = this.topPos;

        LocalPlayer player = this.minecraft.player;
        List<Component> hoveredTooltip = new ArrayList<>();
        var vascularSystem = HemoCapabilityAccess.getVascularSystem(player);
        ItemStack stack = player.getMainHandItem();
        Item item = stack.getItem();
        Item renderItem = ItemInit.dried_leech.get();
        double angleBetweenEach = 360.0 / EnumVeinSections.values().length;
        Point point = new Point(centerX - 80, centerY - 60), center = new Point(centerX, centerY);
        for (EnumVeinSections selectedSection : EnumVeinSections.values()) {
            graphics.drawCenteredString(font,
                    HLTextUtils.toProperCase(selectedSection.toString()), point.x + guiWidth / 2, point.y -20 + guiHeight / 2,
                    0xFFE2B5AD);
            graphics.drawCenteredString(font,
                    vascularSystem.getBloodFlowBySection(selectedSection) + " "
                            + String.format("%.1f/100", vascularSystem.getHealthBySection(selectedSection)),
                    point.x + guiWidth / 2, point.y -30  + guiHeight / 2,
                    0xFFE2B5AD);
            int iconX = point.x - 8 + guiWidth / 2;
            int iconY = point.y - 10 + guiHeight / 2;
            graphics.renderItem(new ItemStack(renderItem), iconX, iconY);

            if (mouseX >= iconX && mouseX < iconX + 16 && mouseY >= iconY && mouseY < iconY + 16) {
                hoveredTooltip.add(Component.literal(HLTextUtils.toProperCase(selectedSection.name())));
                List<ScarDefinition> active = new ArrayList<>();
                HemoCapabilityAccess.getScarState(player).forEachActiveCerebralScar(active::add);
                var known = HemoCapabilityAccess.getKnownManipulations(player);
                known.getEquippedMemoryRefs().forEach(ref -> {
                    if (ref.expression() == HematicMemoryExpression.THELEMIC) {
                        ref.muscleMemory().filter(memory -> memory.section() == selectedSection).ifPresent(memory ->
                                hoveredTooltip.add(Component.literal("Thelemic: " + HLTextUtils.toProperCase(memory.id()))));
                    } else {
                        known.getKnownManips().keySet().stream()
                                .filter(manip -> manip.getName().equals(ref.id()) && manip.getSection() == selectedSection)
                                .findFirst().ifPresent(manip -> {
                                    int tier = ScarNoeticRoutingRules.bestMatchingTier(manip.getTend(), active);
                                    String reduction = tier > 0 ? " (Scar T" + tier + ": -" + tier * 5 + "% strain)" : "";
                                    hoveredTooltip.add(Component.literal("Noetic: ").append(manip.getDisplayName()).append(reduction));
                                });
                    }
                });
            }

            point = rotatePointAbout(point, center, angleBetweenEach);
        }

            int entityCenterX = centerX + guiWidth / 2;
            int entityCenterY = centerY - 4 + guiHeight / 2;
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, entityCenterX - 25, entityCenterY - 35,
                entityCenterX + 25, entityCenterY + 35, 30, 0.0625F,
                this.oldMouseX, this.oldMouseY, player);
            if (!hoveredTooltip.isEmpty()) {
                graphics.renderTooltip(font, hoveredTooltip, Optional.empty(), mouseX, mouseY);
            }
    }
    private static Point rotatePointAbout(Point in, Point about, double degrees) {
        double rad = degrees * Math.PI / 180.0;
        double newX = Math.cos(rad) * (in.x - about.x) - Math.sin(rad) * (in.y - about.y) + about.x;
        double newY = Math.sin(rad) * (in.x - about.x) + Math.cos(rad) * (in.y - about.y) + about.y;
        return new Point((int) newX, (int) newY);
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
