package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.ScarType;
import com.vincenthuto.hemomancy.common.init.ScarInit;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ItemScar;
import com.vincenthuto.hemomancy.common.item.harbinger.scar.ScarDefinition;
import com.vincenthuto.hemomancy.common.recipe.ScarRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ScarsTabController implements IProgressTab {
	static final int NODE_SIZE = 26;
	private static final int KNOWLEDGE_REFRESH_TICKS = 60;

	private final ScarsTabState state = new ScarsTabState();
	private final PanZoomState panZoom = new PanZoomState();
	private final TendencyTraceLayerCache traceCache = new TendencyTraceLayerCache();
	private int playerDegree;
	private int knowledgeRefreshCooldown;

	@Override
	public void onInit(ProgressScreenContext ctx) {
		playerDegree = ctx.playerDegree();
		loadEntries();
		refreshKnowledge();
		panZoom.centreOn(getContentW(), getContentH(), ctx.guiWidth(), ctx.guiHeight());
	}

	private void loadEntries() {
		Minecraft mc = Minecraft.getInstance();
		List<ScarTreeEntry> entries = new ArrayList<>();
		if (mc.player != null && mc.level != null) {
			for (ScarRecipe recipe : ScarRecipe.getAllRecipes(mc.level)) {
				if (!ScarUnlockRegistry.get(recipe.getId()).isUnlocked(mc.player)) continue;
				ItemStack result = recipe.getResultItem();
				if (!(result.getItem() instanceof ItemScar scarItem)) continue;
				ScarDefinition definition = scarItem.getScarDefinition();
				if (definition.getScarType() != ScarType.CEREBRAL || definition.getAssignedTendency() == null) continue;
				ResourceLocation id = ScarInit.SCARS_TYPE_REGISTRY.getKey(definition);
				if (id == null) continue;
				entries.add(new ScarTreeEntry(recipe, id, definition, result,
						definition.getAssignedTendency(), definition.getTier(),
						"scar_blood_honed".equals(id.getPath())));
			}
		}
		entries.sort(Comparator.comparingInt((ScarTreeEntry entry) -> entry.tendency().ordinal())
				.thenComparingInt(ScarTreeEntry::tier)
				.thenComparing(entry -> entry.id().toString()));
		state.rebuild(entries);
	}

	private void refreshKnowledge() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;
		HemoCapabilityAccess.getScarState(mc.player).ifPresent(scars ->
				state.updateKnowledge(scars.getKnownCerebralScars(), scars.getActiveCerebralScars()));
	}

	private void tickKnowledgeCache() {
		if (knowledgeRefreshCooldown-- > 0) return;
		knowledgeRefreshCooldown = KNOWLEDGE_REFRESH_TICKS;
		refreshKnowledge();
	}

	@Override
	public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
		tickKnowledgeCache();
		if (state.entries.isEmpty()) {
			gfx.drawCenteredString(ctx.font(), "No cerebral scar recipes found",
					ctx.guiLeft() + ctx.guiWidth() / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF666666);
			return;
		}
		traceCache.rebuildIfNeeded(traceNodes(), getContentW(), getContentH(),
				ScarTreeLayout.CENTER_X, ScarTreeLayout.CENTER_Y);
		traceCache.render(gfx, ctx, panZoom);
		TendencyStarRenderer.draw(gfx, ctx, panZoom, ScarTreeLayout.CENTER_X, ScarTreeLayout.CENTER_Y);
		ScarsTabView.drawNodes(gfx, ctx, state, panZoom, playerDegree);
	}

	private List<TendencyTraceNode> traceNodes() {
		Map<String, List<String>> parents = new HashMap<>();
		for (ScarTreeLayout.Edge edge : state.edges) {
			parents.computeIfAbsent(edge.toId(), ignored -> new ArrayList<>()).add(edge.fromId());
		}
		List<TendencyTraceNode> nodes = new ArrayList<>();
		for (ScarTreeEntry entry : state.entries) {
			ScarTreeLayout.Point point = state.positions.get(entry.id().toString());
			if (point == null) continue;
			nodes.add(new TendencyTraceNode(entry.id().toString(), entry.tendency(), point.x(), point.y(),
					parents.getOrDefault(entry.id().toString(), List.of()),
					state.knownScarIds.contains(entry.id()), isTierLocked(entry.tier(), playerDegree)));
		}
		return nodes;
	}

	@Override
	public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
		ScarTreeEntry selected = state.selectedEntry();
		if (selected != null) ScarsTabView.drawDetails(gfx, ctx, selected, state, playerDegree);
	}

	@Override
	public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
		ScarsTabView.drawTooltip(gfx, ctx, state, panZoom, playerDegree, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(ProgressScreenContext ctx, double mouseX, double mouseY, int button) {
		if (button != 0) return false;
		ScarTreeEntry hit = ScarsTabView.nodeUnder(ctx, state, panZoom, mouseX, mouseY);
		if (hit == null) return false;
		state.toggleSelection(hit.id().toString());
		return true;
	}

	@Override public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) { return false; }
	@Override public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) { return false; }
	@Override public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) { return false; }
	@Override public PanZoomState getPanZoomState() { return panZoom; }
	@Override public boolean closeDetails() { return state.closeDetails(); }
	@Override public int getContentW() { return ScarTreeLayout.CONTENT_W; }
	@Override public int getContentH() { return ScarTreeLayout.CONTENT_H; }

	static boolean isTierLocked(int tier, int playerDegree) {
		return playerDegree < (tier >= 3 ? 5 : 4);
	}

	public ScarsTabState getState() { return state; }
}
