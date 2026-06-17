package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.HemoMilestone;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgressClientCache;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class MilestoneDrawerView {
    private MilestoneDrawerView() {}

    static final int DRAWER_W      = 140;
    static final int TAB_W         = 14;
    static final int TAB_H         = 50;
    private static final int HOME_BTN_SIZE = 16;
    private static final int HOME_BTN_PAD  = 4;
    private static final int DRAWER_SHADOW = 0xAA000000;
    private static final int PANEL_BG = 0xD0140505;
    private static final int BORDER_OUTER = 0xFF552020;
    private static final int BORDER_INNER = 0xFF3B2420;
    private static final int MILESTONE_SCROLLBAR_WIDTH = 6;
    private static final float DRAWER_Z = 700.0F;

    public static void draw(GuiGraphics gfx, ProgressScreenContext ctx, MilestoneDrawerState state, int mouseX, int mouseY) {
        int drawerX = ctx.guiLeft() + HOME_BTN_PAD;
        int drawerY = ctx.guiTop() + HOME_BTN_PAD + HOME_BTN_SIZE + 4;
        boolean tabHovered = isOverToggle(ctx, state, mouseX, mouseY);

        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(0.0F, 0.0F, DRAWER_Z);
        if (!state.open) {
            drawToggleTab(gfx, ctx, drawerX, drawerY, false, tabHovered);
            pose.popPose();
            return;
        }

        int drawerW = DRAWER_W;
        int drawerH = ctx.guiHeight() - (drawerY - ctx.guiTop()) - 4;

        gfx.fill(drawerX - 2, drawerY - 2, drawerX + drawerW + 2, drawerY + drawerH + 2, DRAWER_SHADOW);
        gfx.fill(drawerX, drawerY, drawerX + drawerW, drawerY + drawerH, PANEL_BG);
        ScreenDrawUtils.drawBorder(gfx, drawerX, drawerY, drawerW, drawerH, BORDER_OUTER, BORDER_INNER);

        drawToggleTab(gfx, ctx, drawerX + drawerW, drawerY, true, tabHovered);

        gfx.enableScissor(drawerX + 1, drawerY + 1,
                drawerX + drawerW - MILESTONE_SCROLLBAR_WIDTH - 2, drawerY + drawerH - 1);

        int x = drawerX + 6;
        int y = drawerY + 6 - state.scrollOffset;
        int centerX = drawerX + drawerW / 2;

        gfx.drawCenteredString(ctx.font(), Component.literal("Milestones"), centerX, y, 0xFFCC3333);
        y += 12;

        int completed = SkillProgressClientCache.current().getCompletedMilestoneCount();
        int visible = 0;
        for (HemoMilestone m : HemoMilestone.values()) {
            if (m.getRequiredDegree() <= ctx.playerDegree()) visible++;
        }
        gfx.drawCenteredString(ctx.font(),
                Component.literal(completed + "/" + visible + " completed").withStyle(s -> s.withColor(0xFF888888)),
                centerX, y, 0);
        y += 12;

        gfx.fill(drawerX + 8, y, drawerX + drawerW - 8, y + 1, 0x33804040);
        y += 5;

        HemoMilestone.Category lastCategory = null;
        for (HemoMilestone m : HemoMilestone.values()) {
            if (m.getRequiredDegree() > ctx.playerDegree()) continue;

            if (m.getCategory() != lastCategory) {
                lastCategory = m.getCategory();
                final HemoMilestone.Category cat = lastCategory;
                y += 3;
                gfx.drawString(ctx.font(),
                        Component.literal("\u25B8 " + cat.getLabel()).withStyle(s -> s.withColor(cat.getColor()).withBold(true)),
                        x, y, 0, false);
                y += 11;
            }

            boolean complete = SkillProgressClientCache.current().isMilestoneCompleted(m);
            String icon = complete ? "\u2713" : "\u2717";
            int iconCol = complete ? 0xFF60CC60 : 0xFF605050;
            int labelCol = complete ? 0xFFBBAAAA : 0xFF776666;
            String label = ScreenDrawUtils.truncateText(ctx.font(), Component.translatable(m.getLangKey()).getString(),
                    drawerW - MILESTONE_SCROLLBAR_WIDTH - 30);

            gfx.drawString(ctx.font(), icon, x + 4, y, iconCol, false);
            gfx.drawString(ctx.font(), Component.literal(label), x + 14, y, labelCol, false);
            y += 10;

            String rewardStr = "  +" + m.getSkillPointReward() + " SP";
            int rewardCol = complete ? 0xFF44BB44 : 0xFF555555;
            gfx.drawString(ctx.font(), rewardStr, x + 4, y, rewardCol, false);
            y += 11;
        }

        int totalContentH = y + state.scrollOffset - (drawerY + 6);
        int maxScroll = Math.max(0, totalContentH - (drawerH - 12));
        if (state.scrollOffset > maxScroll) state.scrollOffset = maxScroll;

        gfx.disableScissor();
        drawMilestoneScrollbar(gfx, drawerX + drawerW - MILESTONE_SCROLLBAR_WIDTH - 2, drawerY + 2,
                MILESTONE_SCROLLBAR_WIDTH, drawerH - 4, state.scrollOffset, maxScroll, totalContentH);
        pose.popPose();
    }

    private static void drawMilestoneScrollbar(GuiGraphics gfx, int x, int y, int w, int h,
            int scrollOffset, int maxScroll, int totalContentH) {
        gfx.fill(x, y, x + w, y + h, 0xAA100504);
        ScreenDrawUtils.drawSimpleBorder(gfx, x, y, w, h, BORDER_INNER);
        if (maxScroll <= 0 || totalContentH <= 0) {
            gfx.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0x663B2420);
            return;
        }
        int thumbH = Math.max(14, h * h / totalContentH);
        int travel = Math.max(1, h - thumbH - 2);
        int thumbY = y + 1 + (int) (travel * (scrollOffset / (float) maxScroll));
        gfx.fill(x + 1, thumbY, x + w - 1, thumbY + thumbH, 0xFF8D2323);
    }

    private static void drawToggleTab(GuiGraphics gfx, ProgressScreenContext ctx, int tabX, int tabY, boolean expanded, boolean hovered) {
        ScreenDrawUtils.drawSidebarToggleTab(gfx, ctx.font(),
                tabX, tabY, TAB_W, TAB_H,
                expanded, hovered,
                0xDD1A0505, 0xCC120303,
                0xFFCC4444, 0xFF332222,
                0xFFFFAAAA, 0xFF886666);
    }

    public static boolean isOverToggle(ProgressScreenContext ctx, MilestoneDrawerState state, double mx, double my) {
        int tabY = ctx.guiTop() + HOME_BTN_PAD + HOME_BTN_SIZE + 4;
        int tabX;
        if (state.open) {
            tabX = ctx.guiLeft() + HOME_BTN_PAD + DRAWER_W;
        } else {
            tabX = ctx.guiLeft() + HOME_BTN_PAD;
        }
        return mx >= tabX && mx <= tabX + TAB_W && my >= tabY && my <= tabY + TAB_H;
    }

    public static boolean isOverDrawer(ProgressScreenContext ctx, MilestoneDrawerState state, double mx, double my) {
        if (!state.open) return false;
        int drawerX = ctx.guiLeft() + HOME_BTN_PAD;
        int drawerY = ctx.guiTop() + HOME_BTN_PAD + HOME_BTN_SIZE + 4;
        int drawerW = DRAWER_W;
        int drawerH = ctx.guiHeight() - (drawerY - ctx.guiTop()) - 4;
        return mx >= drawerX && mx <= drawerX + drawerW && my >= drawerY && my <= drawerY + drawerH;
    }
}
