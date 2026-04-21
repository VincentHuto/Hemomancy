package com.vincenthuto.hemomancy.client.screen.skilltree.util;

import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class CraftingTabController implements IProgressTab {
    private final CraftingTabState state = new CraftingTabState();

    @Override
    public void onInit(ProgressScreenContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        state.craftingRecipes.clear();
        if (mc.player != null && mc.level != null)
            for (BloodStructureRecipe r : BloodStructureRecipe.getAllRecipes(mc.level))
                if (!r.isUnstained()) state.craftingRecipes.add(r);
        state.rebuildTierMap();
        state.autoSelectFirstTier(ctx.playerDegree());
    }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
        if (!state.craftingDragging) state.craftingRotationAngle += partial * 0.4f;
        CraftingTabView.draw(gfx, ctx, state, mx, my, partial);
    }

    @Override public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}
    @Override public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn != 0) return false;
        String clickedTier = CraftingTabView.tierUnder(ctx, state, mx, my);
        if (clickedTier != null) {
            int tierIdx = java.util.Arrays.asList(CraftingTabState.TIER_NAMES).indexOf(clickedTier);
            if (tierIdx >= 0 && ctx.playerDegree() >= CraftingTabState.TIER_DEGREE_REQ[tierIdx]) {
                if (clickedTier.equals(state.selectedCraftingTier)) {
                    state.selectedCraftingTier = null;
                    state.craftingSidebarScroll = 0;
                    state.craftingInfoScroll = 0;
                } else {
                    state.selectedCraftingTier = clickedTier;
                    state.selectedCraftingIndexInTier = 0;
                    state.craftingVisibleLayer = -1;
                    state.craftingSidebarScroll = 0;
                    state.craftingInfoScroll = 0;
                }
            }
            return true;
        }
        int clickedIdx = CraftingTabView.recipeUnder(ctx, state, mx, my);
        if (clickedIdx >= 0) {
            state.selectedCraftingIndexInTier = clickedIdx;
            state.craftingVisibleLayer = -1;
            state.craftingInfoScroll = 0;
            return true;
        }
        if (CraftingTabView.isOverLayerUpButton(ctx, state, mx, my)) {
            int ml = state.craftingMaxLayer;
            if (state.craftingVisibleLayer == -1) state.craftingVisibleLayer = ml;
            else if (state.craftingVisibleLayer < ml) state.craftingVisibleLayer++;
            else state.craftingVisibleLayer = -1;
            return true;
        }
        if (CraftingTabView.isOverLayerDownButton(ctx, state, mx, my)) {
            if (state.craftingVisibleLayer == -1) state.craftingVisibleLayer = 0;
            else if (state.craftingVisibleLayer > 0) state.craftingVisibleLayer--;
            else state.craftingVisibleLayer = -1;
            return true;
        }
        if (mx >= ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 4) {
            state.craftingDragging = true;
            state.craftingDragLastX = mx;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn == 0) state.craftingDragging = false;
        return false;
    }

    @Override
    public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) {
        if (state.craftingDragging && btn == 0) {
            state.craftingRotationAngle += (float)(mx - state.craftingDragLastX) * 0.8f;
            state.craftingDragLastX = mx;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
        int scrollAmt = (int)(-delta * 14);
        if (ctx.isOverTierSidebar(mx, my)) {
            state.craftingSidebarScroll = Math.max(0, state.craftingSidebarScroll + scrollAmt);
            state.clampSidebarScroll(ctx.tierSidebarVisibleH());
        } else {
            state.craftingInfoScroll = Math.max(0, state.craftingInfoScroll + scrollAmt);
        }
        return true;
    }

    @Override public PanZoomState getPanZoomState() { return null; }
    public CraftingTabState getState() { return state; }
}
