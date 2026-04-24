package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.util.ArrayList;
import java.util.List;

import com.vincenthuto.hemomancy.client.screen.skilltree.shared.*;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HarbingerProgressScreen extends Screen {

    // ── Tabs ──
    private enum Tab {
        SKILLS("Skills", 0xFFCC3333),
        MANIPULATIONS("Manipulations", 0xFFCC8833),
        CRAFTING("Crafting", 0xFFAA2222),
        SCARS("Scars", 0xFF44AACC),
        RITES("Rites", 0xFF8844CC),
        MATERIALS("Materials", 0xFFCC6644);

        final String label;
        final int color;
        Tab(String label, int color) { this.label = label; this.color = color; }
    }

    private Tab activeTab = Tab.SKILLS;
    private static final int TAB_HEIGHT = 16;
    private static final int TAB_PAD = 4;
    private static final int HOME_BTN_SIZE = 16;
    private static final int HOME_BTN_PAD  = 4;

    private int guiLeft, guiTop, guiWidth, guiHeight;
    private boolean isDragging;
    private PanZoomState view;

    private final SkillsTabController skills    = new SkillsTabController();
    private final ManipulationsTabController  manips    = new ManipulationsTabController();
    private final RitesTabController rites     = new RitesTabController();
    private final CraftingTabController crafting  = new CraftingTabController();
    private final ScarsTabController          scars     = new ScarsTabController();
    private final MaterialsTabController materials = new MaterialsTabController();

    private final VeinBackgroundRenderer  veinBg        = new VeinBackgroundRenderer();
    private final MilestoneDrawerState milestoneState = new MilestoneDrawerState();

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
            case MATERIALS     -> materials;
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

        ProgressScreenContext ctx = makeContext();
        for (IProgressTab tab : allTabs()) tab.onInit(ctx);
        view = viewForTab(activeTab);
    }

    private IProgressTab[] allTabs() {
        return new IProgressTab[]{skills, manips, rites, crafting, scars, materials};
    }

    private PanZoomState viewForTab(Tab tab) {
        PanZoomState ps = activeController(tab).getPanZoomState();
        return ps != null ? ps : (view != null ? view : skills.getPanZoomState());
    }

    private void switchTab(Tab tab) {
        if (tab == activeTab) return;
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
            if (activeController().getPanZoomState() != null && isOverHomeButton(mx, my)) {
                view.centreOn(contentWForTab(activeTab), contentHForTab(activeTab), guiWidth, guiHeight);
                return true;
            }
            Tab clicked = tabUnder(mx, my);
            if (clicked != null) { switchTab(clicked); return true; }

            if (activeTab == Tab.SKILLS && MilestoneDrawerView.isOverToggle(makeContext(), milestoneState, mx, my)) {
                milestoneState.open = !milestoneState.open;
                milestoneState.scrollOffset = 0;
                return true;
            }

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

            if (activeTab == Tab.SKILLS && milestoneState.open && MilestoneDrawerView.isOverDrawer(ctx, milestoneState, mx, my)) {
              milestoneState.scrollOffset = Math.max(0, milestoneState.scrollOffset - (int)(scrollY * 12));
            return true;
        }

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
        veinBg.render(gfx, guiLeft, guiTop, guiWidth, guiHeight);
        ScreenDrawUtils.drawBorder(gfx, guiLeft, guiTop, guiWidth, guiHeight, 0xFF330808, 0xFF220606);

        gfx.enableScissor(guiLeft + 2, guiTop + 2, guiLeft + guiWidth - 2, guiTop + guiHeight - 2);
        ProgressScreenContext ctx = makeContext();
        activeController().render(gfx, ctx, mouseX, mouseY, partial);
        gfx.disableScissor();

        activeController().renderOverlay(gfx, ctx, mouseX, mouseY);

        drawTabs(gfx, mouseX, mouseY);
        if (activeController().getPanZoomState() != null) {
            drawHomeButton(gfx, mouseX, mouseY);
        }

        if (activeTab == Tab.SKILLS) {
            MilestoneDrawerView.draw(gfx, ctx, milestoneState, mouseX, mouseY);
        }

        if (activeTab == Tab.SKILLS) {
            String spText = "Skill Points: " + SkillPointInit.skillPoints;
            gfx.drawString(font, Component.literal(spText).withStyle(s -> s.withColor(0xFFBB8833).withBold(true)),
                    guiLeft + HOME_BTN_PAD + HOME_BTN_SIZE + 4,
                    guiTop + HOME_BTN_PAD + (HOME_BTN_SIZE - 8) / 2, 0);
            EnumInitiatoryDegree deg = EnumInitiatoryDegree.byNumber(playerDegree);
            String rankTitle = playerDegree <= 0 ? "Uninitiated" : (deg != null ? deg.getTitle() : "Degree " + playerDegree);
            gfx.drawString(font, Component.literal("Rank: " + rankTitle).withStyle(s -> s.withColor(0xFFAA6666)),
                    guiLeft + HOME_BTN_PAD + HOME_BTN_SIZE + 4,
                    guiTop + HOME_BTN_PAD + HOME_BTN_SIZE + 2, 0);
        }

        if (activeController().getPanZoomState() != null) {
            if (!(activeTab == Tab.SKILLS && milestoneState.open)) {
                gfx.drawString(font, String.format("%.0f%%", view.zoom * 100),
                        guiLeft + 5, guiTop + guiHeight - 12, 0x55888888, false);
            }
        }

        activeController().renderTooltip(gfx, ctx, mouseX, mouseY);

        if (activeTab == Tab.SKILLS) {
            if (MilestoneDrawerView.isOverToggle(ctx, milestoneState, mouseX, mouseY)) {
                String tipText = milestoneState.open ? "Hide Milestones" : "Show Milestones";
                gfx.renderTooltip(font, Component.literal(tipText), mouseX, mouseY);
            }
        }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
        super.render(gfx, mouseX, mouseY, partial);
    }

    private List<ScreenDrawUtils.TabDesc> buildTabDescs() {
        List<ScreenDrawUtils.TabDesc> descs = new ArrayList<>();
        for (Tab tab : Tab.values()) descs.add(new ScreenDrawUtils.TabDesc(tab.label, tab.color, tab == activeTab));
        return descs;
    }

    private void drawTabs(GuiGraphics gfx, int mouseX, int mouseY) {
        ScreenDrawUtils.drawTabs(gfx, font, buildTabDescs(), guiLeft, guiTop, guiWidth, TAB_HEIGHT, TAB_PAD, mouseX, mouseY);
    }

    private Tab tabUnder(double mx, double my) {
        int idx = ScreenDrawUtils.tabIndexUnder(font, buildTabDescs(), guiLeft, guiTop, guiWidth, TAB_HEIGHT, TAB_PAD, mx, my);
        return idx >= 0 ? Tab.values()[idx] : null;
    }

    private boolean isOverHomeButton(double mx, double my) {
        int bx = guiLeft + HOME_BTN_PAD, by = guiTop + HOME_BTN_PAD;
        return mx >= bx && mx <= bx + HOME_BTN_SIZE && my >= by && my <= by + HOME_BTN_SIZE;
    }

    private void drawHomeButton(GuiGraphics gfx, int mouseX, int mouseY) {
        boolean hovered = isOverHomeButton(mouseX, mouseY);
        ScreenDrawUtils.drawHomeButton(gfx, font, guiLeft + HOME_BTN_PAD, guiTop + HOME_BTN_PAD, HOME_BTN_SIZE, hovered,
                0xDD1A0505, 0x99120303, 0xFFCC3333, 0xFF444444, 0xFFFFAAAA, 0xFF888888);
        if (hovered) gfx.renderTooltip(font, Component.literal("Return to Center"), mouseX, mouseY);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
