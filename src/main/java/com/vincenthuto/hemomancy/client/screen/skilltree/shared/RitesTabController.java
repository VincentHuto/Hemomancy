package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.LayerViewNavigation;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ImprintMnemonicBlueprintPacket;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RitesTabController implements IProgressTab {
    private static final String FLOOR_FAMILY = "Ritual Floors";
    private final RitesTabState state = new RitesTabState();
    private final boolean unstained;
    private final RecipeMapCanvas mapCanvas = new RecipeMapCanvas(RecipeMapEntry.Kind.RITE);
    private final Map<ResourceLocation, CardinalRiteRecipe> mapRites = new HashMap<>();

	@Override
	public void onClose() {
		mapCanvas.close();
	}

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
        state.showIchorianSigils = !unstained;
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
        mapRites.clear();
        List<RecipeMapEntry> mapEntries = new ArrayList<>();
        if (mc.player != null && mc.level != null) {
            for (CardinalRiteRecipe r : CardinalRiteRecipe.getAllRecipes(mc.level)) {
                if (r.isUnstained() != unstained) continue;
                boolean discovered = RiteUnlockRegistry.get(r.getId()).isUnlocked(mc.player);
                if (discovered) state.riteRecipes.add(r);
                if (!unstained) {
                    mapRites.put(r.getId(), r);
                    String path = r.getId().getPath();
                    mapEntries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.RITE, r.getId()),
                            r.getRiteName(), r.getRiteDescription(), RecipeDegreeGates.getRequiredDegree(r),
                            HarbingerRecipeMapDefinitions.riteFamily(path),
                            HarbingerRecipeMapDefinitions.riteOrder(path), discovered,
                            discovered && ctx.playerDegree() >= RecipeDegreeGates.getRequiredDegree(r)));
                }
            }
        }
        state.rebuildTierMap();
        if (unstained) {
            state.autoSelectFirstTier(ctx.playerDegree());
        } else {
            appendFloorEntries(mapEntries);
            appendSigilEntries(mc, mapEntries);
            List<String> families = new ArrayList<>(HarbingerRecipeMapDefinitions.RITE_FAMILIES);
            families.add(families.size() - 1, FLOOR_FAMILY);
            families.add(families.size() - 1, "Ichorian Sigils");
			mapCanvas.initialise(ctx, mapEntries, families, HarbingerRecipeMapDefinitions.riteLinks(),
					HarbingerRecipeMapDefinitions.ritePositions(), entry -> {
				if (entry.key().kind() == RecipeMapEntry.Kind.FLOOR) {
					return CardinalRiteFloorRegistry.get(entry.id())
							.map(RitualFloorIcon::resolve).orElse(ItemStack.EMPTY);
				}
                CardinalRiteRecipe rite = mapRites.get(entry.id());
				ItemStack fallback = rite == null ? ItemStack.EMPTY : rite.getResult();
				return HarbingerRecipeMapDefinitions.riteIcon(entry.id().getPath(), fallback);
            });
            state.selectedRiteTier = null;
            state.selectedIchorianSigilId = null;
        }
    }

    private void appendFloorEntries(List<RecipeMapEntry> entries) {
        int order = 0;
        for (CardinalRiteFloorDefinition floor : CardinalRiteFloorRegistry.all().values().stream()
                .sorted(java.util.Comparator.comparing(CardinalRiteFloorDefinition::style)
                        .thenComparingInt(definition -> definition.tier().ordinal())).toList()) {
            String tier = HLTextUtils.toProperCase(floor.tier().getSerializedName());
            String style = HLTextUtils.toProperCase(floor.style().replace('_', ' '));
            entries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.FLOOR, floor.id()),
                    tier + " " + style + " Floor",
                    "Focused floor-only construction view for the " + style + " ritual style.",
                    floor.tier().ordinal(), FLOOR_FAMILY, order++, true, true));
        }
    }

    private void appendSigilEntries(Minecraft mc, List<RecipeMapEntry> entries) {
        var knowledge = mc.player == null ? null : HemoCapabilityAccess.getIchorianKnowledge(mc.player).orElse(null);
        int order = 0;
        for (IchorianSigilDefinition sigil : IchorianSigilRegistry.all().stream()
                .sorted(java.util.Comparator.comparingInt(IchorianSigilDefinition::tier)
                        .thenComparing(definition -> definition.id().toString())).toList()) {
            boolean known = knowledge != null && knowledge.isKnown(sigil.id());
            boolean partial = knowledge != null && knowledge.discoveredNodeCount(sigil.id()) > 0;
            entries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.SIGIL, sigil.id()),
                    known ? sigil.name() : "Unknown Sigil", known ? sigil.purpose() : "",
                    sigil.tier(), "Ichorian Sigils", order++,
                    known || partial, known));
        }
    }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
        if (!unstained) {
            mapCanvas.render(gfx, ctx, mx, my, state.tabColor);
            RecipeMapKey selected = mapCanvas.selected();
            CardinalRiteRecipe rite = selected != null && selected.kind() == RecipeMapEntry.Kind.RITE
                    ? mapRites.get(selected.id()) : null;
            IchorianSigilDefinition sigil = selected != null && selected.kind() == RecipeMapEntry.Kind.SIGIL
                    ? IchorianSigilRegistry.get(selected.id()) : null;
            CardinalRiteFloorDefinition floor = selected != null && selected.kind() == RecipeMapEntry.Kind.FLOOR
                    ? CardinalRiteFloorRegistry.get(selected.id()).orElse(null) : null;
            if (selected != null) {
                if (!state.riteDragging) state.riteRotationAngle += partial * 0.4f;
                RitesTabView.drawMapInspector(gfx, ctx, state, rite, sigil, floor,
                        mapCanvas.inspectorLayout(ctx), mx, my, partial);
            }
            return;
        }
        if (!state.riteDragging) state.riteRotationAngle += partial * 0.4f;
        RitesTabView.draw(gfx, ctx, state, mx, my, partial);
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
				MnemonicBlueprintTarget target = RecipeMapBlueprintTarget.from(entry);
				if (target != null && target.type() == MnemonicBlueprintTarget.Type.CARDINAL_RITE) {
					PacketHandler.sendToServer(new ImprintMnemonicBlueprintPacket(target));
					return true;
				}
			}
            RecipeMapInspectorLayout inspector = mapCanvas.inspectorLayout(ctx);
            if (mapCanvas.selected() != null && inspector.isOverToggle(mx, my)) {
                mapCanvas.toggleInspector();
                return true;
            }
            if (inspector.expanded() && inspector.panel().contains(mx, my)) {
                int layerButton = state.selectedIchorianSigilId == null
                        ? inspector.layerButtonAt(mx, my, state.riteMaxLayer) : 0;
                if (layerButton != 0) {
                    cycleMapLayer(layerButton);
                    return true;
                }
                if (inspector.preview().contains(mx, my) && btn == 0) {
                    state.riteDragging = true;
                    state.riteDragLastX = mx;
                }
                return true;
            }
            RecipeMapCanvas.ClickResult result = mapCanvas.mouseClicked(ctx, mx, my, btn);
            if (result.selection() != null) applyMapSelection(result.selection());
            return result.consumed();
        }
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
		if (!unstained) return mapCanvas.mouseDragged(ctx, btn, dx, dy);
		return false;
    }

    @Override
    public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
        if (!unstained) {
            RecipeMapInspectorLayout inspector = mapCanvas.inspectorLayout(ctx);
            if (inspector.expanded() && inspector.panel().contains(mx, my)) {
                if (inspector.info().contains(mx, my)) {
                    state.riteInfoScroll = Math.max(0, state.riteInfoScroll + (int) (-delta * 14));
                }
                return true;
            }
			return mapCanvas.mouseScrolled(ctx, mx, my, delta);
        }
        int scrollAmt = (int)(-delta * 14);
        if (ctx.isOverTierSidebar(mx, my)) {
            state.riteSidebarScroll = Math.max(0, state.riteSidebarScroll + scrollAmt);
            state.clampSidebarScroll(ctx.tierSidebarVisibleH());
        } else {
            state.riteInfoScroll = Math.max(0, state.riteInfoScroll + scrollAmt);
        }
        return true;
    }

    private void applyMapSelection(RecipeMapKey key) {
        state.riteInfoScroll = 0;
        state.riteVisibleLayer = -1;
        if (key.kind() == RecipeMapEntry.Kind.SIGIL) {
            state.selectedIchorianSigilId = key.id();
            Minecraft minecraft = Minecraft.getInstance();
            state.ichorianSigilPreviewStartTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
        } else {
            state.selectedIchorianSigilId = null;
        }
    }

    private void cycleMapLayer(int direction) {
        state.riteVisibleLayer = LayerViewNavigation.cycle(
                state.riteVisibleLayer, state.riteMaxLayer, direction);
    }

	@Override public boolean closeDetails() {
		boolean closed;
		if (unstained) {
			closed = state.selectedRiteTier != null || state.selectedIchorianSigilId != null;
			state.selectedRiteTier = null;
		} else {
			closed = mapCanvas.closeDetails();
		}
		if (!closed) return false;
		state.selectedIchorianSigilId = null;
		state.selectedRiteIndexInTier = 0;
		state.riteInfoScroll = 0;
		state.riteVisibleLayer = -1;
		state.riteDragging = false;
		return true;
	}

    @Override public void resetView(ProgressScreenContext ctx) { if (!unstained) mapCanvas.resetView(ctx); }
    @Override public int getNavigationViewportWidth(ProgressScreenContext ctx) {
        return unstained ? ctx.guiWidth() : mapCanvas.viewportWidth(ctx);
    }
    @Override public PanZoomState getPanZoomState() { return unstained ? null : mapCanvas.panZoom(); }
    @Override public int getContentW() { return unstained ? 0 : mapCanvas.contentWidth(); }
    @Override public int getContentH() { return unstained ? 0 : mapCanvas.contentHeight(); }
    public RitesTabState getState() { return state; }
}
