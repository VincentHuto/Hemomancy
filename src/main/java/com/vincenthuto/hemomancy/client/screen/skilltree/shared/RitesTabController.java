package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.List;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class RitesTabController implements IProgressTab {
    private final RitesTabState state = new RitesTabState();
    private final boolean unstained;

    /** Harbinger-flavoured controller (no-arg, existing behaviour preserved). */
    public RitesTabController() { this(false); }

    /**
     * Creates a rites-tab controller with a path-specific theme.
     *
     * @param unstained {@code true} for the Unstained path (teal/blue palette,
     *                  level lock active, hidden empty tiers);
     *                  {@code false} for the Harbinger path (purple palette,
     *                  degree lock active, all tiers shown).
     */
    public RitesTabController(boolean unstained) {
        this.unstained = unstained;
        if (unstained) {
            state.tabColor         = 0xFF8090BB;
            state.separatorColor   = 0xFF203050;
            state.nameColor        = 0xFFB0C0E0;
            state.rowBgSelected    = 0xDD101828;
            state.rowBgHovered     = 0xBB0C1420;
            state.rowBgNormal      = 0x990A0E18;
            state.enableDegreeLock = true;
            state.hideEmptyTiers   = true;
        }
    }

    @Override
    public void onInit(ProgressScreenContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        state.riteRecipes.clear();
        if (mc.player != null && mc.level != null) {
            for (CardinalRiteRecipe r : CardinalRiteRecipe.getAllRecipes(mc.level)) {
                if (r.isUnstained() == unstained
                        && RiteUnlockRegistry.get(r.getId()).isUnlocked(mc.player)) {
                    state.riteRecipes.add(r);
                }
            }
        }
        state.rebuildTierMap();
        state.autoSelectFirstTier(ctx.playerDegree());
    }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
        if (!state.riteDragging) state.riteRotationAngle += partial * 0.4f;
        RitesTabView.draw(gfx, ctx, state, mx, my, partial);
    }

    @Override public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}
    @Override public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn != 0) return false;
        Integer clickedTier = RitesTabView.tierUnder(ctx, state, mx, my);
        if (clickedTier != null) {
            boolean degreeOk = !state.enableDegreeLock
                    || ctx.playerDegree() >= clickedTier;
            if (degreeOk && !state.ritesByTier.getOrDefault(clickedTier, List.of()).isEmpty()) {
                if (clickedTier == state.selectedRiteTier) {
                    state.selectedRiteTier = null;
                    state.riteSidebarScroll = 0;
                    state.riteInfoScroll = 0;
                } else {
                    state.selectedRiteTier = clickedTier;
                    state.selectedRiteIndexInTier = 0;
                    state.riteVisibleLayer = -1;
                    state.riteSidebarScroll = 0;
                    state.riteInfoScroll = 0;
                }
            }
            return true;
        }
        int clickedRiteIdx = RitesTabView.recipeUnder(ctx, state, mx, my);
        if (clickedRiteIdx >= 0) {
            state.selectedRiteIndexInTier = clickedRiteIdx;
            state.riteVisibleLayer = -1;
            state.riteInfoScroll = 0;
            return true;
        }
        if (RitesTabView.isOverLayerUpButton(ctx, state, mx, my)) {
            int ml = state.riteMaxLayer;
            if (state.riteVisibleLayer == -1) state.riteVisibleLayer = ml;
            else if (state.riteVisibleLayer < ml) state.riteVisibleLayer++;
            else state.riteVisibleLayer = -1;
            return true;
        }
        if (RitesTabView.isOverLayerDownButton(ctx, state, mx, my)) {
            if (state.riteVisibleLayer == -1) state.riteVisibleLayer = 0;
            else if (state.riteVisibleLayer > 0) state.riteVisibleLayer--;
            else state.riteVisibleLayer = -1;
            return true;
        }
        if (mx >= ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 4) {
            state.riteDragging = true;
            state.riteDragLastX = mx;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn == 0) state.riteDragging = false;
        return false;
    }

    @Override
    public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) {
        if (state.riteDragging && btn == 0) {
            state.riteRotationAngle += (float)(mx - state.riteDragLastX) * 0.8f;
            state.riteDragLastX = mx;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
        int scrollAmt = (int)(-delta * 14);
        if (ctx.isOverTierSidebar(mx, my)) {
            state.riteSidebarScroll = Math.max(0, state.riteSidebarScroll + scrollAmt);
            state.clampSidebarScroll(ctx.tierSidebarVisibleH());
        } else {
            state.riteInfoScroll = Math.max(0, state.riteInfoScroll + scrollAmt);
        }
        return true;
    }

    @Override public PanZoomState getPanZoomState() { return null; }
    public RitesTabState getState() { return state; }
}
