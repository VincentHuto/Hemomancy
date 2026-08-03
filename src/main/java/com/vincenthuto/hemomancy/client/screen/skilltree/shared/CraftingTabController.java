package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.LayerViewNavigation;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ImprintMnemonicBlueprintPacket;
import com.vincenthuto.hutoslib.client.HLTextUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftingTabController implements IProgressTab {
    private final CraftingTabState state = new CraftingTabState();
    private final boolean unstained;
    private final RecipeMapCanvas mapCanvas = new RecipeMapCanvas(RecipeMapEntry.Kind.CRAFTING);
    private final Map<ResourceLocation, BloodStructureRecipe> mapRecipes = new HashMap<>();

    /** Harbinger-flavoured controller (no-arg, existing behaviour preserved). */
    public CraftingTabController() { this(false); }

    /**
     * Creates a crafting-tab controller with a path-specific theme.
     *
     * @param unstained {@code true} for the Unstained path (teal/blue palette,
     *                  level lock active);
     *                  {@code false} for the Harbinger path (red palette,
     *                  degree lock active).
     */
    public CraftingTabController(boolean unstained) {
        this.unstained = unstained;
        if (unstained) {
            state.tabColor         = 0xFF80D0C0;
            state.separatorColor   = 0xFF203050;
            state.nameColor        = 0xFF80D0C0;
            state.rowBgSelected    = 0xDD101828;
            state.rowBgHovered     = 0xBB0C1420;
            state.rowBgNormal      = 0x990A0E18;
            state.enableDegreeLock = true;
        }
    }

    @Override
    public void onInit(ProgressScreenContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        state.craftingRecipes.clear();
        mapRecipes.clear();
        if (mc.player != null && mc.level != null)
            for (BloodStructureRecipe r : BloodStructureRecipe.getAllRecipes(mc.level))
                if (r.isUnstained() == unstained) state.craftingRecipes.add(r);
        state.rebuildTierMap();
        if (unstained) {
            state.autoSelectFirstTier(ctx.playerDegree());
        } else {
            List<RecipeMapEntry> entries = new ArrayList<>();
            for (BloodStructureRecipe recipe : state.craftingRecipes) {
                mapRecipes.put(recipe.getId(), recipe);
                String path = recipe.getId().getPath();
				String name = displayName(recipe);
                int degree = RecipeDegreeGates.getRequiredDegree(recipe);
                entries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.CRAFTING, recipe.getId()),
                        name, tooltipDescription(recipe), degree, HarbingerRecipeMapDefinitions.craftingFamily(path),
                        HarbingerRecipeMapDefinitions.craftingOrder(path), true, ctx.playerDegree() >= degree));
            }
			mapCanvas.initialise(ctx, entries, HarbingerRecipeMapDefinitions.CRAFTING_FAMILIES,
					HarbingerRecipeMapDefinitions.craftingLinks(), HarbingerRecipeMapDefinitions.craftingPositions(), entry -> {
                BloodStructureRecipe recipe = mapRecipes.get(entry.id());
				ItemStack fallback = recipe == null ? ItemStack.EMPTY : recipe.getResult();
				return HarbingerRecipeMapDefinitions.craftingIcon(entry.id().getPath(), fallback);
            });
            state.selectedCraftingTier = null;
        }
    }

    private static String displayName(BloodStructureRecipe recipe) {
        ItemStack result = recipe.getResult();
        if (result != null && !result.isEmpty()) return result.getHoverName().getString();
        String path = recipe.getId().getPath();
        if (path.contains("/")) path = path.substring(path.lastIndexOf('/') + 1);
        return HLTextUtils.toProperCase(path.replace("_", " "));
    }

	private static String tooltipDescription(BloodStructureRecipe recipe) {
		ItemStack held = recipe.getHeldItem();
		ItemStack hit = recipe.getHitBlock() == null ? ItemStack.EMPTY : new ItemStack(recipe.getHitBlock());
		return tooltipDescription((int) recipe.getBloodCost(),
				held == null || held.isEmpty() ? "the required item" : held.getHoverName().getString(),
				hit.isEmpty() ? "the required structure" : hit.getHoverName().getString());
	}

	static String tooltipDescription(int bloodCost, String heldItem, String hitBlock) {
		return "Hold " + heldItem + " and activate " + hitBlock + ". Blood cost: " + bloodCost + " mL.";
	}

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
        if (!unstained) {
            mapCanvas.render(gfx, ctx, mx, my, state.tabColor);
            RecipeMapKey selected = mapCanvas.selected();
            BloodStructureRecipe recipe = selected == null ? null : mapRecipes.get(selected.id());
            if (selected != null) {
                if (!state.craftingDragging) state.craftingRotationAngle += partial * 0.4f;
                CraftingTabView.drawMapInspector(gfx, ctx, state, recipe, mapCanvas.inspectorLayout(ctx), mx, my);
            }
            return;
        }
        if (!state.craftingDragging) state.craftingRotationAngle += partial * 0.4f;
        CraftingTabView.draw(gfx, ctx, state, mx, my, partial);
    }

    @Override public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}
    @Override public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
        if (!unstained) mapCanvas.renderTooltip(gfx, ctx, mx, my, state.tabColor);
    }

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (!unstained) {
			if (btn == 1 && net.minecraft.client.gui.screens.Screen.hasShiftDown()) {
				RecipeMapEntry entry = mapCanvas.entryAt(ctx, mx, my);
				if (entry != null && entry.key().kind() == RecipeMapEntry.Kind.CRAFTING) {
					PacketHandler.sendToServer(new ImprintMnemonicBlueprintPacket(new MnemonicBlueprintTarget(
							MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, entry.id())));
					return true;
				}
			}
            RecipeMapInspectorLayout inspector = mapCanvas.inspectorLayout(ctx);
            if (mapCanvas.selected() != null && inspector.isOverToggle(mx, my)) {
                mapCanvas.toggleInspector();
                return true;
            }
            if (inspector.expanded() && inspector.panel().contains(mx, my)) {
                int layerButton = inspector.layerButtonAt(mx, my, state.craftingMaxLayer);
                if (layerButton != 0) {
                    cycleMapLayer(layerButton);
                    return true;
                }
                if (inspector.preview().contains(mx, my) && btn == 0) {
                    state.craftingDragging = true;
                    state.craftingDragLastX = mx;
                }
                return true;
            }
            RecipeMapCanvas.ClickResult result = mapCanvas.mouseClicked(ctx, mx, my, btn);
            if (result.selection() != null) applyMapSelection();
            return result.consumed();
        }
        if (btn != 0) return false;
        String clickedTier = CraftingTabView.tierUnder(ctx, state, mx, my);
        if (clickedTier != null) {
            int tierIdx = java.util.Arrays.asList(CraftingTabState.TIER_NAMES).indexOf(clickedTier);
            boolean degreeOk = !state.enableDegreeLock
                    || (tierIdx >= 0 && ctx.playerDegree() >= CraftingTabState.TIER_DEGREE_REQ[tierIdx]);
            if (degreeOk) {
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
            state.craftingVisibleLayer = LayerViewNavigation.cycle(
                    state.craftingVisibleLayer, state.craftingMaxLayer, 1);
            return true;
        }
        if (CraftingTabView.isOverLayerDownButton(ctx, state, mx, my)) {
            state.craftingVisibleLayer = LayerViewNavigation.cycle(
                    state.craftingVisibleLayer, state.craftingMaxLayer, -1);
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
		if (!unstained) return mapCanvas.mouseDragged(ctx, btn, dx, dy);
		return false;
    }

    @Override
    public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
        if (!unstained) {
            RecipeMapInspectorLayout inspector = mapCanvas.inspectorLayout(ctx);
            if (inspector.expanded() && inspector.panel().contains(mx, my)) {
                if (inspector.info().contains(mx, my)) {
                    state.craftingInfoScroll = Math.max(0, state.craftingInfoScroll + (int) (-delta * 14));
                }
                return true;
            }
			return mapCanvas.mouseScrolled(ctx, mx, my, delta);
        }
        int scrollAmt = (int)(-delta * 14);
        if (ctx.isOverTierSidebar(mx, my)) {
            state.craftingSidebarScroll = Math.max(0, state.craftingSidebarScroll + scrollAmt);
            state.clampSidebarScroll(ctx.tierSidebarVisibleH());
        } else {
            state.craftingInfoScroll = Math.max(0, state.craftingInfoScroll + scrollAmt);
        }
        return true;
    }

    private void applyMapSelection() {
        state.craftingInfoScroll = 0;
        state.craftingVisibleLayer = -1;
    }

    private void cycleMapLayer(int direction) {
        state.craftingVisibleLayer = LayerViewNavigation.cycle(
                state.craftingVisibleLayer, state.craftingMaxLayer, direction);
    }

	@Override public boolean closeDetails() {
		boolean closed;
		if (unstained) {
			closed = state.selectedCraftingTier != null;
			state.selectedCraftingTier = null;
		} else {
			closed = mapCanvas.closeDetails();
		}
		if (!closed) return false;
		state.selectedCraftingIndexInTier = 0;
		state.craftingInfoScroll = 0;
		state.craftingVisibleLayer = -1;
		state.craftingDragging = false;
		return true;
	}

    @Override public void resetView(ProgressScreenContext ctx) { if (!unstained) mapCanvas.resetView(ctx); }
    @Override public int getNavigationViewportWidth(ProgressScreenContext ctx) {
        return unstained ? ctx.guiWidth() : mapCanvas.viewportWidth(ctx);
    }
    @Override public PanZoomState getPanZoomState() { return unstained ? null : mapCanvas.panZoom(); }
    @Override public int getContentW() { return unstained ? 0 : mapCanvas.contentWidth(); }
    @Override public int getContentH() { return unstained ? 0 : mapCanvas.contentHeight(); }
    public CraftingTabState getState() { return state; }
}
