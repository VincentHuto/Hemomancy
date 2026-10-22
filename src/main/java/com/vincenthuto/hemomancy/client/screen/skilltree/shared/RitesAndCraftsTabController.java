package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.LayerViewNavigation;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintTarget;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.ImprintMnemonicBlueprintPacket;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.recipe.RecipeDegreeGates;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteCeremonyDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorDefinition;
import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorRegistry;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilDefinition;
import com.vincenthuto.hemomancy.common.rite.sigil.IchorianSigilRegistry;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Harbinger Rites &amp; Crafts tab: every rite, Working craft, floorless Blood Structure, ritual floor and Ichorian
 * sigil on one radial map, its families gathered by purpose. Rites, floors and sigils open the rite inspector;
 * crafts open the crafting inspector with their structure on its bed.
 */
public class RitesAndCraftsTabController implements IProgressTab {
	private final RitesTabState riteState = new RitesTabState();
	private final CraftingTabState craftState = new CraftingTabState();
	private final RecipeMapCanvas mapCanvas = new RecipeMapCanvas("Rites", HarbingerRecipeMapDefinitions::group);
	private final Map<ResourceLocation, CardinalRiteRecipe> mapRites = new HashMap<>();
	private final Map<ResourceLocation, CraftingMapRecipe> mapCrafts = new HashMap<>();

	public RitesAndCraftsTabController() {
		riteState.showIchorianSigils = true;
	}

	@Override
	public void onClose() {
		mapCanvas.close();
	}

	@Override
	public void onInit(ProgressScreenContext ctx) {
		Minecraft mc = Minecraft.getInstance();
		riteState.riteRecipes.clear();
		craftState.craftingRecipes.clear();
		mapRites.clear();
		mapCrafts.clear();
		List<RecipeMapEntry> entries = new ArrayList<>();
		if (mc.player != null && mc.level != null) {
			for (CardinalRiteRecipe rite : CardinalRiteRecipe.getAllRecipes(mc.level)) {
				if (rite.isUnstained()) continue;
				if (rite.isWorkingCraft()) {
					addCraft(ctx, entries, CraftingMapRecipe.of(rite));
					continue;
				}
				boolean discovered = RiteUnlockRegistry.get(rite.getId()).isUnlocked(mc.player);
				if (discovered) riteState.riteRecipes.add(rite);
				mapRites.put(rite.getId(), rite);
				String path = rite.getId().getPath();
				int degree = RecipeDegreeGates.getRequiredDegree(rite);
				entries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.RITE, rite.getId()),
						rite.displayName().getString(), rite.displayDescription().getString(), degree,
						HarbingerRecipeMapDefinitions.riteFamily(path), HarbingerRecipeMapDefinitions.riteOrder(path),
						discovered, discovered && ctx.playerDegree() >= degree));
			}
			for (BloodStructureRecipe structure : BloodStructureRecipe.getAllRecipes(mc.level)) {
				if (!structure.isUnstained()) addCraft(ctx, entries, CraftingMapRecipe.of(structure));
			}
		}
		riteState.rebuildTierMap();
		craftState.rebuildTierMap();
		appendFloorEntries(entries, mapRites.values());
		appendSigilEntries(mc, entries, mapRites.values());
		Map<RecipeMapKey, RecipeMapLayout.AuthoredPosition> positions = new HashMap<>(HarbingerRecipeMapDefinitions.ritePositions());
		positions.putAll(HarbingerRecipeMapDefinitions.craftingPositions());
		mapCanvas.initialise(ctx, entries, HarbingerRecipeMapDefinitions.FAMILIES, HarbingerRecipeMapDefinitions.links(),
				positions, this::icon);
		riteState.selectedRiteTier = null;
		riteState.selectedIchorianSigilId = null;
		craftState.selectedCraftingTier = null;
	}

	private void addCraft(ProgressScreenContext ctx, List<RecipeMapEntry> entries, CraftingMapRecipe recipe) {
		craftState.craftingRecipes.add(recipe);
		mapCrafts.put(recipe.getId(), recipe);
		String path = recipe.getId().getPath();
		int degree = recipe.requiredDegree();
		entries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.CRAFTING, recipe.getId()),
				CraftingTabState.displayName(recipe), craftTooltip(recipe), degree,
				HarbingerRecipeMapDefinitions.craftingFamily(path), HarbingerRecipeMapDefinitions.craftingOrder(path),
				true, ctx.playerDegree() >= degree));
	}

	private ItemStack icon(RecipeMapEntry entry) {
		return switch (entry.key().kind()) {
			case FLOOR -> CardinalRiteFloorRegistry.get(entry.id()).map(RitualFloorIcon::resolve).orElse(ItemStack.EMPTY);
			case CRAFTING -> {
				CraftingMapRecipe recipe = mapCrafts.get(entry.id());
				yield HarbingerRecipeMapDefinitions.craftingIcon(entry.id().getPath(),
						recipe == null ? ItemStack.EMPTY : recipe.getResult());
			}
			default -> {
				CardinalRiteRecipe rite = mapRites.get(entry.id());
				yield HarbingerRecipeMapDefinitions.riteIcon(entry.id().getPath(),
						rite == null ? ItemStack.EMPTY : rite.getResult());
			}
		};
	}

	private void appendFloorEntries(List<RecipeMapEntry> entries, Collection<CardinalRiteRecipe> rites) {
		List<CardinalRiteFloorDefinition> floors = CardinalRiteFloorRegistry.all().values().stream()
				.sorted(Comparator.comparingInt((CardinalRiteFloorDefinition floor) ->
						CardinalRiteFloorDefinition.listOrder(floor.style(), floor.tier()))
						.thenComparing(floor -> floor.id().toString())).toList();
		Map<ResourceLocation, Integer> used = new HashMap<>();
		for (CardinalRiteRecipe rite : rites) {
			if (rite.getFloorId() != null) used.merge(rite.getFloorId(), RecipeDegreeGates.getRequiredDegree(rite), Math::min);
		}
		for (CraftingMapRecipe craft : mapCrafts.values()) {
			if (craft.floorId() != null) used.merge(craft.floorId(), craft.requiredDegree(), Math::min);
		}
		Map<ResourceLocation, CardinalRiteType> tiers = new HashMap<>();
		Map<ResourceLocation, Integer> fallback = new HashMap<>();
		for (CardinalRiteFloorDefinition floor : floors) {
			tiers.put(floor.id(), floor.tier());
			fallback.put(floor.id(), floor.tier().ordinal());
		}
		Map<ResourceLocation, Integer> firstUse = firstUseDegrees(tiers, used, fallback);
		int order = 0;
		for (CardinalRiteFloorDefinition floor : floors) {
			String tier = HLTextUtils.toProperCase(floor.tier().getSerializedName());
			String style = HLTextUtils.toProperCase(floor.style().replace('_', ' '));
			String detail = floor.special()
					? "The special " + style + " floor, laid for the " + floor.purpose().id() + " rite performed on it."
					: "Focused floor-only construction view for the " + style + " ritual style.";
			entries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.FLOOR, floor.id()),
					tier + " " + style + " Floor", detail, firstUse.get(floor.id()),
					HarbingerRecipeMapDefinitions.floorFamily(floor.purpose()), order++, true, true));
		}
	}

	private void appendSigilEntries(Minecraft mc, List<RecipeMapEntry> entries, Collection<CardinalRiteRecipe> rites) {
		var knowledge = mc.player == null ? null : HemoCapabilityAccess.getIchorianKnowledge(mc.player);
		List<IchorianSigilDefinition> sigils = IchorianSigilRegistry.all().stream()
				.sorted(Comparator.comparingInt(IchorianSigilDefinition::tier)
						.thenComparing(definition -> definition.id().toString())).toList();
		Map<ResourceLocation, Integer> used = new HashMap<>();
		for (CardinalRiteRecipe rite : rites) {
			CardinalRiteCeremonyDefinition ceremony = rite.getCeremony();
			if (ceremony == null) continue;
			for (CardinalRiteCeremonyDefinition.SupportSocket socket : ceremony.supportSockets()) {
				ResourceLocation sigil = sigilId(socket.suggestedSigil());
				if (sigil != null) used.merge(sigil, RecipeDegreeGates.getRequiredDegree(rite), Math::min);
			}
		}
		Map<ResourceLocation, Integer> tiers = new HashMap<>();
		for (IchorianSigilDefinition sigil : sigils) tiers.put(sigil.id(), sigil.tier());
		Map<ResourceLocation, Integer> firstUse = firstUseDegrees(tiers, used, tiers);
		int order = 0;
		for (IchorianSigilDefinition sigil : sigils) {
			boolean known = knowledge != null && knowledge.isKnown(sigil.id());
			boolean partial = knowledge != null && knowledge.discoveredNodeCount(sigil.id()) > 0;
			entries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.SIGIL, sigil.id()),
					known ? sigil.name() : "Unknown Sigil", known ? sigil.purpose() : "",
					firstUse.get(sigil.id()), HarbingerRecipeMapDefinitions.SIGIL_FAMILY, order++, known || partial, known));
		}
	}

	/**
	 * The degree at which each floor or sigil first matters: the earliest required degree of any
	 * Harbinger rite or craft that uses it. Unused ones borrow the earliest use among same-tier siblings, so a
	 * whole tier rings together, and fall back to their own tier number when no sibling is used either.
	 */
	static <T> Map<ResourceLocation, Integer> firstUseDegrees(Map<ResourceLocation, T> tiers,
			Map<ResourceLocation, Integer> earliestUse, Map<ResourceLocation, Integer> fallback) {
		Map<T, Integer> tierUse = new HashMap<>();
		tiers.forEach((id, tier) -> {
			Integer degree = earliestUse.get(id);
			if (degree != null) tierUse.merge(tier, degree, Math::min);
		});
		Map<ResourceLocation, Integer> degrees = new HashMap<>();
		tiers.forEach((id, tier) -> degrees.put(id, earliestUse.containsKey(id) ? earliestUse.get(id)
				: tierUse.getOrDefault(tier, fallback.getOrDefault(id, 0))));
		return degrees;
	}

	private static ResourceLocation sigilId(String suggested) {
		if (suggested == null || suggested.isBlank()) return null;
		return ResourceLocation.tryParse(suggested.contains(":") ? suggested : "hemomancy:" + suggested);
	}

	static String craftTooltip(CraftingMapRecipe recipe) {
		if (recipe.isWorking()) {
			ItemStack medium = recipe.getHeldItem();
			return "Build it beside a Cardinal Focus on a " + recipe.floorLabel() + ", seat "
					+ (medium.isEmpty() ? "its medium" : medium.getHoverName().getString())
					+ " in the Focus, light its braziers and feed it " + (int) recipe.getBloodCost() + " mL.";
		}
		ItemStack held = recipe.getHeldItem();
		ItemStack hit = recipe.getHitBlock() == null ? ItemStack.EMPTY : new ItemStack(recipe.getHitBlock());
		return tooltipDescription((int) recipe.getBloodCost(),
				held == null || held.isEmpty() ? "the required item" : held.getHoverName().getString(),
				hit.isEmpty() ? "the required structure" : hit.getHoverName().getString());
	}

	static String tooltipDescription(int bloodCost, String heldItem, String hitBlock) {
		return "Hold " + heldItem + " and activate " + hitBlock + ". Blood cost: " + bloodCost + " mL.";
	}

	private boolean craftSelected() {
		RecipeMapKey selected = mapCanvas.selected();
		return selected != null && selected.kind() == RecipeMapEntry.Kind.CRAFTING;
	}

	@Override
	public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
		mapCanvas.render(gfx, ctx, mx, my, riteState.tabColor);
		RecipeMapKey selected = mapCanvas.selected();
		if (selected == null) return;
		if (selected.kind() == RecipeMapEntry.Kind.CRAFTING) {
			if (!craftState.craftingDragging) craftState.craftingRotationAngle += partial * 0.4f;
			CraftingTabView.drawMapInspector(gfx, ctx, craftState, mapCrafts.get(selected.id()),
					mapCanvas.inspectorLayout(ctx), mx, my);
			return;
		}
		CardinalRiteRecipe rite = selected.kind() == RecipeMapEntry.Kind.RITE ? mapRites.get(selected.id()) : null;
		IchorianSigilDefinition sigil = selected.kind() == RecipeMapEntry.Kind.SIGIL
				? IchorianSigilRegistry.get(selected.id()) : null;
		CardinalRiteFloorDefinition floor = selected.kind() == RecipeMapEntry.Kind.FLOOR
				? CardinalRiteFloorRegistry.get(selected.id()).orElse(null) : null;
		if (!riteState.riteDragging) riteState.riteRotationAngle += partial * 0.4f;
		RitesTabView.drawMapInspector(gfx, ctx, riteState, rite, sigil, floor, mapCanvas.inspectorLayout(ctx), mx, my, partial);
	}

	@Override public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {}

	@Override
	public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
		mapCanvas.renderTooltip(gfx, ctx, mx, my, riteState.tabColor);
	}

	@Override
	public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
		if (btn == 1 && Screen.hasShiftDown()) {
			RecipeMapEntry entry = mapCanvas.entryAt(ctx, mx, my);
			MnemonicBlueprintTarget target = blueprintTarget(entry);
			if (target != null) {
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
			boolean craft = craftSelected();
			int maxLayer = craft ? craftState.craftingMaxLayer
					: riteState.selectedIchorianSigilId == null ? riteState.riteMaxLayer : -1;
			int layerButton = maxLayer < 0 ? 0 : inspector.layerButtonAt(mx, my, maxLayer);
			if (layerButton != 0) {
				cycleMapLayer(craft, layerButton);
				return true;
			}
			if (inspector.preview().contains(mx, my) && btn == 0) {
				if (craft) {
					craftState.craftingDragging = true;
					craftState.craftingDragLastX = mx;
				} else {
					riteState.riteDragging = true;
					riteState.riteDragLastX = mx;
				}
			}
			return true;
		}
		RecipeMapCanvas.ClickResult result = mapCanvas.mouseClicked(ctx, mx, my, btn);
		if (result.selection() != null) applyMapSelection(result.selection());
		return result.consumed();
	}

	private MnemonicBlueprintTarget blueprintTarget(RecipeMapEntry entry) {
		if (entry == null) return null;
		if (entry.key().kind() == RecipeMapEntry.Kind.CRAFTING) {
			CraftingMapRecipe recipe = mapCrafts.get(entry.id());
			return recipe != null ? recipe.blueprintTarget()
					: new MnemonicBlueprintTarget(MnemonicBlueprintTarget.Type.BLOOD_STRUCTURE, entry.id());
		}
		MnemonicBlueprintTarget target = RecipeMapBlueprintTarget.from(entry);
		return target != null && target.type() == MnemonicBlueprintTarget.Type.CARDINAL_RITE ? target : null;
	}

	@Override
	public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) {
		if (btn == 0) {
			riteState.riteDragging = false;
			craftState.craftingDragging = false;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) {
		if (btn == 0 && craftState.craftingDragging) {
			craftState.craftingRotationAngle += (float) (mx - craftState.craftingDragLastX) * 0.8f;
			craftState.craftingDragLastX = mx;
			return true;
		}
		if (btn == 0 && riteState.riteDragging) {
			riteState.riteRotationAngle += (float) (mx - riteState.riteDragLastX) * 0.8f;
			riteState.riteDragLastX = mx;
			return true;
		}
		return mapCanvas.mouseDragged(ctx, btn, dx, dy);
	}

	@Override
	public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
		RecipeMapInspectorLayout inspector = mapCanvas.inspectorLayout(ctx);
		if (inspector.expanded() && inspector.panel().contains(mx, my)) {
			if (inspector.info().contains(mx, my)) {
				int step = (int) (-delta * 14);
				if (craftSelected()) craftState.craftingInfoScroll = Math.max(0, craftState.craftingInfoScroll + step);
				else riteState.riteInfoScroll = Math.max(0, riteState.riteInfoScroll + step);
			}
			return true;
		}
		return mapCanvas.mouseScrolled(ctx, mx, my, delta);
	}

	private void applyMapSelection(RecipeMapKey key) {
		riteState.riteInfoScroll = 0;
		riteState.riteVisibleLayer = -1;
		craftState.craftingInfoScroll = 0;
		craftState.craftingVisibleLayer = -1;
		if (key.kind() == RecipeMapEntry.Kind.SIGIL) {
			riteState.selectedIchorianSigilId = key.id();
			Minecraft minecraft = Minecraft.getInstance();
			riteState.ichorianSigilPreviewStartTick = minecraft.level == null ? 0L : minecraft.level.getGameTime();
		} else {
			riteState.selectedIchorianSigilId = null;
		}
	}

	private void cycleMapLayer(boolean craft, int direction) {
		if (craft) {
			craftState.craftingVisibleLayer = LayerViewNavigation.cycle(
					craftState.craftingVisibleLayer, craftState.craftingMaxLayer, direction);
		} else {
			riteState.riteVisibleLayer = LayerViewNavigation.cycle(
					riteState.riteVisibleLayer, riteState.riteMaxLayer, direction);
		}
	}

	@Override
	public boolean closeDetails() {
		if (!mapCanvas.closeDetails()) return false;
		riteState.selectedIchorianSigilId = null;
		riteState.selectedRiteIndexInTier = 0;
		riteState.riteInfoScroll = 0;
		riteState.riteVisibleLayer = -1;
		riteState.riteDragging = false;
		craftState.selectedCraftingIndexInTier = 0;
		craftState.craftingInfoScroll = 0;
		craftState.craftingVisibleLayer = -1;
		craftState.craftingDragging = false;
		return true;
	}

	@Override public void resetView(ProgressScreenContext ctx) { mapCanvas.resetView(ctx); }
	@Override public int getNavigationViewportWidth(ProgressScreenContext ctx) { return mapCanvas.viewportWidth(ctx); }
	@Override public PanZoomState getPanZoomState() { return mapCanvas.panZoom(); }
	@Override public int getContentW() { return mapCanvas.contentWidth(); }
	@Override public int getContentH() { return mapCanvas.contentHeight(); }
}
