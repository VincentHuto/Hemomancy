package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.LayerViewNavigation;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * The Unstained Rites tab: a stage sidebar of Unstained rites with a rotating preview. Harbinger rites are
 * browsed on the Rites &amp; Crafts map ({@link RitesAndCraftsTabController}).
 */
public class RitesTabController implements IProgressTab {
    private final RitesTabState state = new RitesTabState();

    public RitesTabController() {
        state.showIchorianSigils = false;
        state.tabColor         = 0xFF8090BB;
        state.separatorColor   = 0xFF203050;
        state.nameColor        = 0xFFB0C0E0;
        state.rowBgSelected    = 0xDD101828;
        state.rowBgHovered     = 0xBB0C1420;
        state.rowBgNormal      = 0x990A0E18;
        state.enableDegreeLock = true;
        state.hideEmptyTiers   = true;
        state.unstainedStages  = true;
    }

    @Override
    public void onInit(ProgressScreenContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        state.riteRecipes.clear();
        if (mc.player != null && mc.level != null) {
            for (CardinalRiteRecipe r : CardinalRiteRecipe.getAllRecipes(mc.level)) {
                if (!r.isUnstained()) continue;
                if (RiteUnlockRegistry.get(r.getId()).isUnlocked(mc.player)) state.riteRecipes.add(r);
            }
        }
        state.rebuildTierMap();
        RitesTabView.cacheSidebarLabels(ctx.font(), state);
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
        if (RitesTabView.isOverIchorianDropdown(ctx, state, mx, my)) {
            state.ichorianSigilsExpanded = !state.ichorianSigilsExpanded;
            state.riteSidebarScroll = 0;
            return true;
        }
        ResourceLocation clickedSigil = RitesTabView.knownSigilUnder(ctx, state, mx, my);
        if (clickedSigil != null) {
			state.selectedIchorianSigilId = clickedSigil;
			Minecraft minecraft = Minecraft.getInstance();
			state.ichorianSigilPreviewStartTick = minecraft.level == null
					? 0L : minecraft.level.getGameTime();
			return true;
		}
        Integer clickedTier = RitesTabView.tierUnder(ctx, state, mx, my);
        if (clickedTier != null) {
            boolean degreeOk = !state.enableDegreeLock
                    || ctx.playerDegree() >= clickedTier;
            if (degreeOk && !state.ritesByTier.getOrDefault(clickedTier, List.of()).isEmpty()) {
				state.selectedIchorianSigilId = null;
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
			state.selectedIchorianSigilId = null;
            state.selectedRiteIndexInTier = clickedRiteIdx;
            state.riteVisibleLayer = -1;
            state.riteInfoScroll = 0;
            return true;
        }
        if (RitesTabView.isOverLayerUpButton(ctx, state, mx, my)) {
            state.riteVisibleLayer = LayerViewNavigation.cycle(
                    state.riteVisibleLayer, state.riteMaxLayer, 1);
            return true;
        }
        if (RitesTabView.isOverLayerDownButton(ctx, state, mx, my)) {
            state.riteVisibleLayer = LayerViewNavigation.cycle(
                    state.riteVisibleLayer, state.riteMaxLayer, -1);
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

	@Override public boolean closeDetails() {
		if (state.selectedRiteTier == null && state.selectedIchorianSigilId == null) return false;
		state.selectedRiteTier = null;
		state.selectedIchorianSigilId = null;
		state.selectedRiteIndexInTier = 0;
		state.riteInfoScroll = 0;
		state.riteVisibleLayer = -1;
		state.riteDragging = false;
		return true;
	}

    @Override public void resetView(ProgressScreenContext ctx) {}
    @Override public int getNavigationViewportWidth(ProgressScreenContext ctx) { return ctx.guiWidth(); }
    @Override public PanZoomState getPanZoomState() { return null; }
    @Override public int getContentW() { return 0; }
    @Override public int getContentH() { return 0; }
    public RitesTabState getState() { return state; }
}
