package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MaterialsTabController implements IProgressTab {
	private static final int NODE_SIZE = 26;

	private final PanZoomState panZoom = new PanZoomState();
	private final CyclingFamilyFilter<String> familyFilter = new CyclingFamilyFilter<>(List.of());
	private final LinkedHashMap<MaterialAtlasNode, int[]> positions = new LinkedHashMap<>();
	private final LinkedHashMap<MaterialAtlasNode, int[]> viewportPositions = new LinkedHashMap<>();
	private final MaterialIconCache iconCache = new MaterialIconCache();
	private final MaterialAtlasTraceLayerCache traceCache = new MaterialAtlasTraceLayerCache();
	private final TraceLayerInvalidation traceInvalidation = new TraceLayerInvalidation();

	/** All entries provided at construction time, before gate filtering. */
	private final List<MaterialEntry> rawEntries;
	/** Entries visible to the current player, including next-tier veiled previews. */
	private List<MaterialAtlasNode> entries = List.of();
	private MaterialRenderSnapshot renderSnapshot = MaterialRenderSnapshot.empty();

	private final MaterialAtlasPath path;
	private final EnumNodeShape nodeShape;
	private final int tabColor;
	private final int nodeTransparentColor;
	private final int nodeAccentColor;
	private final int panelSeparatorColor;
	private final int panelBgColor;
	private final MiniRecipeRenderer.Theme renderer;

	private int contentW;
	private int contentH;
	private MaterialAtlasNode selectedEntry;

	/** Harbinger-flavoured controller using the orange-copper blood-magic palette. */
	public MaterialsTabController() {
		this(MaterialAtlasPath.HARBINGER, MaterialsData.getBloodEntries(), EnumNodeShape.SQUARE,
				0xFFCC6644, 0x00CC6644, 0xFFBB7733,
				0xFF442222, 0xDD1A0505, MiniRecipeRenderer.BLOOD);
	}

	public MaterialsTabController(List<MaterialEntry> entries, EnumNodeShape nodeShape,
			int tabColor, int nodeTransparentColor, int nodeAccentColor,
			int panelSeparatorColor, int panelBgColor,
			MiniRecipeRenderer.Theme renderer) {
		this(MaterialAtlasPath.HARBINGER, entries, nodeShape,
				tabColor, nodeTransparentColor, nodeAccentColor,
				panelSeparatorColor, panelBgColor, renderer);
	}

	public MaterialsTabController(MaterialAtlasPath path, List<MaterialEntry> entries, EnumNodeShape nodeShape,
			int tabColor, int nodeTransparentColor, int nodeAccentColor,
			int panelSeparatorColor, int panelBgColor,
			MiniRecipeRenderer.Theme renderer) {
		this.path = path;
		this.rawEntries = entries;
		this.nodeShape = nodeShape;
		this.tabColor = tabColor;
		this.nodeTransparentColor = nodeTransparentColor;
		this.nodeAccentColor = nodeAccentColor;
		this.panelSeparatorColor = panelSeparatorColor;
		this.panelBgColor = panelBgColor;
		this.renderer = renderer;
	}

	@Override
	public void onInit(ProgressScreenContext ctx) {
		Player player = Minecraft.getInstance().player;
		PlayerProgress progress = readProgress(player);

		List<MaterialAtlasNode> visibleEntries = new ArrayList<>();
		for (MaterialEntry material : rawEntries) {
			MaterialAtlasEntry atlasEntry = MaterialAtlasSpec.entryFor(path, material);
			MaterialVisibility visibility = atlasEntry.gate().visibilityFor(path,
					progress.degree(), progress.purity(), progress.clarity());
			if (visibility == MaterialVisibility.HIDDEN) {
				continue;
			}
			if (visibility == MaterialVisibility.UNLOCKED
					&& player != null
					&& !material.unlockPredicate().isUnlocked(player)) {
				continue;
			}
			visibleEntries.add(new MaterialAtlasNode(material, atlasEntry, visibility));
		}
		entries = visibleEntries;
		iconCache.initialize(entries.stream().map(MaterialAtlasNode::entry).toList());
		familyFilter.setOptions(MaterialAtlasSpec.buckets(path).stream()
				.filter(bucket -> entries.stream().anyMatch(node -> node.atlasEntry().bucket().id().equals(bucket.id())))
				.map(MaterialAtlasBucket::id).toList());

		if (selectedEntry != null
				&& (!entries.contains(selectedEntry)
				|| selectedEntry.visibility() != MaterialVisibility.UNLOCKED
				|| !familyFilter.includes(selectedEntry.atlasEntry().bucket().id()))) {
			selectedEntry = null;
		}

		int[] bounds = new int[2];
		MaterialsTabView.buildLayout(entries, positions, bounds, NODE_SIZE, path);
		contentW = bounds[0];
		contentH = bounds[1];
		rebuildRenderSnapshot();
		panZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
	}

	@Override
	public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
		if (traceInvalidation.consume(0L)) traceCache.rebuild(renderSnapshot.nodes(), renderSnapshot.positions(),
				contentW, contentH, MaterialAtlasSpec.hubX(path), MaterialAtlasSpec.hubY(path));
		rebuildViewportSnapshot(ctx);
		MaterialsTabView.drawAtlasTrace(traceCache, gfx, ctx, panZoom);
		MaterialsTabView.drawNodes(gfx, ctx.font(), path,
				renderSnapshot.nodes(), viewportPositions, iconCache,
				panZoom, ctx.guiLeft(), ctx.guiTop(), NODE_SIZE, nodeShape,
				tabColor, selectedEntry, nodeTransparentColor, nodeAccentColor);
		FamilyFilterControlView.draw(gfx, ctx, familyLabel(), tabColor, mx, my);
	}

	@Override
	public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
		if (selectedEntry != null && selectedEntry.visibility() == MaterialVisibility.UNLOCKED) {
			MaterialsTabView.drawInfoPanel(gfx, ctx.font(), path, selectedEntry.entry(), iconCache.get(selectedEntry.entry()),
					ctx.guiLeft(), ctx.guiTop(), ctx.guiWidth(),
					tabColor, panelSeparatorColor, panelBgColor, renderer);
		}
	}

	@Override
	public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
		MaterialsTabView.drawTooltip(gfx, ctx.font(), viewportPositions,
				panZoom, ctx.guiLeft(), ctx.guiTop(),
				ctx.guiWidth(), ctx.guiHeight(), NODE_SIZE,
				nodeShape, tabColor, nodeAccentColor, mx, my);
	}

	@Override
	public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
		if (FamilyFilterControlView.bounds(ctx).contains(mx, my)) {
			familyFilter.cycle(btn == 1 ? -1 : 1);
			rebuildRenderSnapshot();
			if (selectedEntry != null
					&& !familyFilter.includes(selectedEntry.atlasEntry().bucket().id())) selectedEntry = null;
			return true;
		}
		if (btn != 0) {
			return false;
		}
		MaterialAtlasNode hit = MaterialsTabView.nodeUnder(viewportPositions, panZoom,
				ctx.guiLeft(), ctx.guiTop(), NODE_SIZE, nodeShape, mx, my);
		if (hit == null) {
			return false;
		}
		if (hit.visibility() == MaterialVisibility.UNLOCKED) {
			selectedEntry = selectedEntry == hit ? null : hit;
		} else if (hit.visibility() == MaterialVisibility.NEXT_PREVIEW) {
			selectedEntry = null;
		}
		return true;
	}

	@Override
	public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) {
		return false;
	}

	@Override
	public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) {
		return false;
	}

	@Override
	public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
		return false;
	}

	@Override
	public PanZoomState getPanZoomState() {
		return panZoom;
	}

	@Override
	public boolean closeDetails() {
		if (selectedEntry == null) return false;
		selectedEntry = null;
		return true;
	}

	public int getContentW() {
		return contentW;
	}

	public int getContentH() {
		return contentH;
	}

	private void rebuildRenderSnapshot() {
		renderSnapshot = MaterialRenderSnapshot.filter(entries, positions, familyFilter::includes);
		traceInvalidation.markDirty();
	}

	private void rebuildViewportSnapshot(ProgressScreenContext ctx) {
		viewportPositions.clear();
		int halfExtent = panZoom.halfNode(NODE_SIZE) + 5;
		int right = ctx.guiLeft() + ctx.guiWidth();
		int bottom = ctx.guiTop() + ctx.guiHeight();
		for (var entry : renderSnapshot.positions().entrySet()) {
			int[] position = entry.getValue();
			int x = panZoom.sx(ctx.guiLeft(), position[0]);
			int y = panZoom.sy(ctx.guiTop(), position[1]);
			if (ProgressViewportCulling.intersects(x, y, halfExtent,
					ctx.guiLeft(), ctx.guiTop(), right, bottom)) {
				viewportPositions.put(entry.getKey(), position);
			}
		}
	}

	@Override
	public void onClose() {
		traceCache.close();
	}

	private String familyLabel() {
		String selected = familyFilter.selected();
		String nickname = MaterialAtlasSpec.buckets(path).stream()
				.filter(bucket -> bucket.id().equals(selected))
				.map(MaterialAtlasBucket::nickname)
				.findFirst().orElse(selected);
		return FamilyFilterLabels.display(nickname);
	}

	private static PlayerProgress readProgress(Player player) {
		if (player == null) {
			return new PlayerProgress(Integer.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		}
		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		float[] unstained = {0.0F, 0.0F};
		HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(progress -> {
			unstained[0] = progress.getPurity();
			unstained[1] = progress.getClarity();
		});
		return new PlayerProgress(degree, unstained[0], unstained[1]);
	}

	private record PlayerProgress(int degree, float purity, float clarity) {
	}
}
