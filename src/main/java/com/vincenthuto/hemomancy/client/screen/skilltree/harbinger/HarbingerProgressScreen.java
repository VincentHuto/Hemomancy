package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.skilltree.shared.*;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgressClientCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class HarbingerProgressScreen extends Screen {

    // ── Tabs ──
    private enum Tab {
        SKILLS("Skills", 0xFFCC3333, 0, false),
        MANIPULATIONS("Manipulations", 0xFFCC8833, 3, false),
        CRAFTING("Crafting", 0xFFAA2222, 0, false),
        SCARS("Scars", 0xFF44AACC, 4, false),
        SUMMONS("Summons", 0xFFBB3355, 2, false),
        RITES("Rites", 0xFF8844CC, 0, false),
        MATERIALS("Materials", 0xFFCC6644, 0, false),
        BESTIARY("Bestiary", 0xFF77AA66, 2, true);

        final String label;
        final int color;
        final int requiredDegree;
        final boolean bottomRight;
        Tab(String label, int color, int requiredDegree, boolean bottomRight) {
            this.label = label;
            this.color = color;
            this.requiredDegree = requiredDegree;
            this.bottomRight = bottomRight;
        }

        boolean visibleAtDegree(int degree) {
            return degree >= requiredDegree;
        }
    }

    private Tab activeTab = Tab.SKILLS;
    private static final int TAB_HEIGHT = 16;
    private static final int TAB_PAD = 4;
    private static final int HOME_BTN_SIZE = 16;
    private static final int HOME_BTN_PAD  = 4;
    private static final int SKILL_POINT_BADGE_X_PAD = 2;
    private static final int SKILL_POINT_BADGE_Y_PAD = 2;
    private static final int SKILL_POINT_BADGE_GAP = 5;
    private static final float SCREEN_CHROME_Z = 400.0F;

    private int guiLeft, guiTop, guiWidth, guiHeight;
    private boolean isDragging;
    private PanZoomState view;

    private final SkillsTabController skills    = new SkillsTabController();
    private final ManipulationsTabController  manips    = new ManipulationsTabController();
    private final RitesTabController rites     = new RitesTabController();
    private final CraftingTabController crafting  = new CraftingTabController();
    private final ScarsTabController          scars     = new ScarsTabController();
    private final SummonsTabController summons = new SummonsTabController();
    private final MaterialsTabController materials = new MaterialsTabController();
    private final BestiaryTabController bestiary = new BestiaryTabController();

    private final VeinBackgroundRenderer  veinBg        = new VeinBackgroundRenderer();

    private int playerDegree = 0;

    public HarbingerProgressScreen() {
        super(Component.translatable("screen.hemomancy.skill_tree"));
    }

    public static void openScreen() {
        Minecraft.getInstance().setScreen(new HarbingerProgressScreen());
    }

    private IProgressTab activeController() {
        return activeController(activeTab);
    }

    private IProgressTab activeController(Tab tab) {
        return switch (tab) {
            case SKILLS        -> skills;
            case MANIPULATIONS -> manips;
            case RITES         -> rites;
            case CRAFTING      -> crafting;
            case SCARS         -> scars;
            case SUMMONS       -> summons;
            case MATERIALS     -> materials;
            case BESTIARY      -> bestiary;
        };
    }

    @Override
    protected void init() {
        super.init();
        RecipeLookup.clearCache();
        int margin = 16;
        guiLeft = margin; guiTop = margin;
        guiWidth = width - margin * 2;
        guiHeight = height - margin * 2;
        clearWidgets();

        if (Minecraft.getInstance().player != null) {
            playerDegree = HemoCapabilityAccess.getInitiatoryDegree(Minecraft.getInstance().player)
                    .map(d -> d.getDegreeNumber()).orElse(0);
        }
        if (!activeTab.visibleAtDegree(playerDegree)) {
            activeTab = firstVisibleTab(playerDegree);
        }

        ProgressScreenContext ctx = makeContext();
        for (IProgressTab tab : allTabs()) tab.onInit(ctx);
        view = viewForTab(activeTab);
    }

    private IProgressTab[] allTabs() {
        return new IProgressTab[]{skills, manips, rites, crafting, scars, summons, materials, bestiary};
    }

    private PanZoomState viewForTab(Tab tab) {
        PanZoomState ps = activeController(tab).getPanZoomState();
        return ps != null ? ps : (view != null ? view : skills.getPanZoomState());
    }

    private void switchTab(Tab tab) {
        if (tab == activeTab) return;
        if (!tab.visibleAtDegree(playerDegree)) return;
        if (view != null && activeController(activeTab).getPanZoomState() != null) {
            view.clamp(contentWForTab(activeTab), contentHForTab(activeTab), guiWidth, guiHeight);
        }
        activeTab = tab;
        PanZoomState ps = activeController(tab).getPanZoomState();
        if (ps != null) view = ps;
    }

    private int contentWForTab(Tab tab) {
        return switch (tab) {
            case SKILLS        -> skills.getContentW();
            case MANIPULATIONS -> manips.getContentW();
            case MATERIALS     -> materials.getContentW();
            default            -> 0;
        };
    }

    private int contentHForTab(Tab tab) {
        return switch (tab) {
            case SKILLS        -> skills.getContentH();
            case MANIPULATIONS -> manips.getContentH();
            case MATERIALS     -> materials.getContentH();
            default            -> 0;
        };
    }

    private boolean insideGui(double mx, double my) {
        return mx >= guiLeft && mx < guiLeft + guiWidth
            && my >= guiTop  && my < guiTop  + guiHeight;
    }

    private ProgressScreenContext makeContext() {
        return new ProgressScreenContext(font, guiLeft, guiTop, guiWidth, guiHeight, playerDegree);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        if (btn == 0) {
            PanZoomState activeView = activeController().getPanZoomState();
            if (activeView != null && isOverHomeButton(mx, my)) {
                view = activeView;
                view.centreOn(contentWForTab(activeTab), contentHForTab(activeTab), guiWidth, guiHeight);
                return true;
            }
            Tab clicked = tabUnder(mx, my);
            if (clicked != null) { switchTab(clicked); return true; }

            if (insideGui(mx, my)) {
                if (activeController().mouseClicked(makeContext(), mx, my, btn)) return true;
                if (activeController().getPanZoomState() != null) isDragging = true;
                return true;
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        if (btn == 0) {
            isDragging = false;
            activeController().mouseReleased(makeContext(), mx, my, btn);
        }
        return super.mouseReleased(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        if (activeController().mouseDragged(makeContext(), mx, my, btn, dx, dy)) return true;
        if (isDragging && btn == 0 && view != null) {
            view.applyDrag(dx, dy);
            view.clamp(contentWForTab(activeTab), contentHForTab(activeTab), guiWidth, guiHeight);
            return true;
        }
        return super.mouseDragged(mx, my, btn, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        ProgressScreenContext ctx = makeContext();

            if (activeController().mouseScrolled(ctx, mx, my, scrollY)) return true;

        if (view != null) {
                  view.applyScroll(guiLeft, guiTop, mx, my, scrollY);
            view.clamp(contentWForTab(activeTab), contentHForTab(activeTab), guiWidth, guiHeight);
        }
        return true;
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
            // Do NOT call renderBackground() — it applies blur. This is not a pause screen.
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        ProgressScreenContext ctx = makeContext();
        veinBg.render(gfx, guiLeft, guiTop, guiWidth, guiHeight, activeTab == Tab.SKILLS ? skills.updateAndGetDeepFade(ctx) : 0.0f);
        ScreenDrawUtils.drawBorder(gfx, guiLeft, guiTop, guiWidth, guiHeight, 0xFF330808, 0xFF220606);

        gfx.enableScissor(guiLeft + 2, guiTop + 2, guiLeft + guiWidth - 2, guiTop + guiHeight - 2);
        activeController().render(gfx, ctx, mouseX, mouseY, partial);
        gfx.disableScissor();

        activeController().renderOverlay(gfx, ctx, mouseX, mouseY);

        drawTabsAboveCanvas(gfx, mouseX, mouseY);
        PanZoomState activeView = activeController().getPanZoomState();
        if (activeView != null) {
            view = activeView;
            drawHomeButton(gfx, mouseX, mouseY);
        }

        if (activeTab == Tab.SKILLS) {
            drawSkillPointsAboveCanvas(gfx);
        }

        if (activeController().getPanZoomState() != null) {
            drawZoomPercentage(gfx);
        }

        activeController().renderTooltip(gfx, ctx, mouseX, mouseY);

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        super.render(gfx, mouseX, mouseY, partial);
    }

    /**
     * In MC 1.21.1, Screen#renderBackground triggers the menu_blur post-process effect
     * which blurs everything previously drawn this frame. We render our own background
     * (vein art) and want the screen contents crisp, so we override this to a no-op.
     */
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // intentionally empty — no blur, no dimming
    }

    private List<ScreenDrawUtils.TabDesc> buildTabDescs() {
        List<ScreenDrawUtils.TabDesc> descs = new ArrayList<>();
        for (Tab tab : visibleTabs(playerDegree)) {
            descs.add(new ScreenDrawUtils.TabDesc(tab.label, tab.color, tab == activeTab));
        }
        return descs;
    }

    private void drawTabs(GuiGraphics gfx, int mouseX, int mouseY) {
        ScreenDrawUtils.drawTabs(gfx, font, buildTabDescs(), guiLeft, guiTop, guiWidth, TAB_HEIGHT, TAB_PAD, mouseX, mouseY);
    }

    private void drawTabsAboveCanvas(GuiGraphics gfx, int mouseX, int mouseY) {
        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, SCREEN_CHROME_Z);
        drawTabs(gfx, mouseX, mouseY);
        drawBottomRightTabs(gfx, mouseX, mouseY);
        pose.popPose();
    }

    private void drawSkillPointsAboveCanvas(GuiGraphics gfx) {
        Component spComponent = skillPointBadgeComponent();
        int textW = font.width(spComponent);
        int badgeLeft = guiLeft + HOME_BTN_PAD;
        int badgeTop = guiTop + HOME_BTN_PAD;
        int badgeW = textW + SKILL_POINT_BADGE_X_PAD * 2;
        int badgeH = font.lineHeight + SKILL_POINT_BADGE_Y_PAD * 2;
        int textX = badgeLeft + SKILL_POINT_BADGE_X_PAD;
        int textY = badgeTop + SKILL_POINT_BADGE_Y_PAD;

        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, SCREEN_CHROME_Z);
        gfx.fill(badgeLeft, badgeTop, badgeLeft + badgeW, badgeTop + badgeH, 0xFF120303);
        ScreenDrawUtils.drawSimpleBorder(gfx, badgeLeft, badgeTop, badgeW, badgeH, 0xFF442222);
        gfx.drawString(font, spComponent, textX, textY, 0, false);
        pose.popPose();
    }

    private Component skillPointBadgeComponent() {
        return Component.literal("SP:" + SkillProgressClientCache.current().getSkillPoints())
                .withStyle(s -> s.withColor(0xFFBB8833));
    }

    private void drawZoomPercentage(GuiGraphics gfx) {
        int x = homeButtonX() + HOME_BTN_SIZE + SKILL_POINT_BADGE_GAP;
        int y = homeButtonY() + (HOME_BTN_SIZE - font.lineHeight) / 2;
        gfx.drawString(font, String.format("%.0f%%", view.zoom * 100), x, y, 0x55888888, false);
    }

    private Tab tabUnder(double mx, double my) {
        Tab bottomRight = bottomRightTabUnder(mx, my);
        if (bottomRight != null) {
            return bottomRight;
        }
        List<Tab> visibleTabs = visibleTabs(playerDegree);
        int idx = ScreenDrawUtils.tabIndexUnder(font, buildTabDescs(), guiLeft, guiTop, guiWidth, TAB_HEIGHT, TAB_PAD, mx, my);
        return idx >= 0 ? visibleTabs.get(idx) : null;
    }

    private void drawBottomRightTabs(GuiGraphics gfx, int mouseX, int mouseY) {
        for (Tab tab : bottomRightTabs(playerDegree)) {
            int tw = font.width(tab.label) + 14;
            int tx = guiLeft + guiWidth - TAB_PAD - tw;
            int ty = guiTop + guiHeight - TAB_PAD - TAB_HEIGHT;
            boolean hovered = mouseX >= tx && mouseX <= tx + tw && mouseY >= ty && mouseY <= ty + TAB_HEIGHT;
            boolean active = tab == activeTab;
            gfx.fill(tx, ty, tx + tw, ty + TAB_HEIGHT, active ? 0xFF07120B : (hovered ? 0xFF0A160D : 0xFF050B07));
            ScreenDrawUtils.drawSimpleBorder(gfx, tx, ty, tw, TAB_HEIGHT, active ? tab.color : 0xFF444444);
            if (active) {
                gfx.fill(tx + 1, ty + 1, tx + tw - 1, ty + 2, tab.color);
            }
            gfx.drawCenteredString(font, tab.label, tx + tw / 2, ty + (TAB_HEIGHT - 8) / 2,
                    active ? tab.color : (hovered ? 0xFFB8C8B8 : 0xFF777777));
        }
    }

    private Tab bottomRightTabUnder(double mx, double my) {
        for (Tab tab : bottomRightTabs(playerDegree)) {
            int tw = font.width(tab.label) + 14;
            int tx = guiLeft + guiWidth - TAB_PAD - tw;
            int ty = guiTop + guiHeight - TAB_PAD - TAB_HEIGHT;
            if (mx >= tx && mx <= tx + tw && my >= ty && my <= ty + TAB_HEIGHT) {
                return tab;
            }
        }
        return null;
    }

    private static Tab firstVisibleTab(int degree) {
        return visibleTabs(degree).getFirst();
    }

    private static List<Tab> visibleTabs(int degree) {
        List<Tab> visible = new ArrayList<>();
        for (Tab tab : Tab.values()) {
            if (!tab.bottomRight && tab.visibleAtDegree(degree)) {
                visible.add(tab);
            }
        }
        return visible;
    }

    private static List<Tab> bottomRightTabs(int degree) {
        List<Tab> visible = new ArrayList<>();
        for (Tab tab : Tab.values()) {
            if (tab.bottomRight && tab.visibleAtDegree(degree)) {
                visible.add(tab);
            }
        }
        return visible;
    }

    private boolean isOverHomeButton(double mx, double my) {
        int bx = homeButtonX(), by = homeButtonY();
        return mx >= bx && mx <= bx + HOME_BTN_SIZE && my >= by && my <= by + HOME_BTN_SIZE;
    }

    private void drawHomeButton(GuiGraphics gfx, int mouseX, int mouseY) {
        boolean hovered = isOverHomeButton(mouseX, mouseY);
        ScreenDrawUtils.drawHomeButton(gfx, font, homeButtonX(), homeButtonY(), HOME_BTN_SIZE, hovered,
                0xDD1A0505, 0x99120303, 0xFFCC3333, 0xFF444444, 0xFFFFAAAA, 0xFF888888);
        if (hovered) gfx.renderTooltip(font, Component.literal("Return to Center"), mouseX, mouseY);
    }

    private int homeButtonX() {
        return guiLeft + HOME_BTN_PAD;
    }

    private int homeButtonY() {
        return guiTop + guiHeight - HOME_BTN_PAD - HOME_BTN_SIZE;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
