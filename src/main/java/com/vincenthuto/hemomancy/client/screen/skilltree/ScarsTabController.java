package com.vincenthuto.hemomancy.client.screen.skilltree;

import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class ScarsTabController implements IProgressTab {
    private final ScarsTabState state = new ScarsTabState();

    @Override
    public void onInit(ProgressScreenContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        state.scarRecipes.clear();
        if (mc.player != null && mc.level != null)
            state.scarRecipes.addAll(ScarRecipe.getAllRecipes(mc.level));
        state.rebuildTierMap();
        state.autoSelectFirstTier(ctx.playerDegree());
    }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
        ScarsTabView.draw(gfx, ctx, state, mx, my, partial);
    }

    @Override public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}
    @Override public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn != 0) return false;
        String clickedTier = ScarsTabView.tierUnder(ctx, state, mx, my);
        if (clickedTier != null) {
            int tierIdx = java.util.Arrays.asList(ScarsTabState.TIER_NAMES).indexOf(clickedTier);
            if (tierIdx >= 0 && ctx.playerDegree() >= ScarsTabState.TIER_DEGREE_REQ[tierIdx]) {
                if (clickedTier.equals(state.selectedScarTier)) {
                    state.selectedScarTier = null;
                    state.scarSidebarScroll = 0;
                } else {
                    state.selectedScarTier = clickedTier;
                    state.selectedScarIndexInTier = 0;
                    state.scarSidebarScroll = 0;
                }
            }
            return true;
        }
        int clickedIdx = ScarsTabView.recipeUnder(ctx, state, mx, my);
        if (clickedIdx >= 0) {
            state.selectedScarIndexInTier = clickedIdx;
            return true;
        }
        return true;
    }

    @Override public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) { return false; }
    @Override public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) { return false; }

    @Override
    public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
        int scrollAmt = (int)(-delta * 14);
        if (ctx.isOverTierSidebar(mx, my)) {
            state.scarSidebarScroll = Math.max(0, state.scarSidebarScroll + scrollAmt);
            state.clampSidebarScroll(ctx.tierSidebarVisibleH());
        }
        return true;
    }

    @Override public PanZoomState getPanZoomState() { return null; }
    public ScarsTabState getState() { return state; }
}
