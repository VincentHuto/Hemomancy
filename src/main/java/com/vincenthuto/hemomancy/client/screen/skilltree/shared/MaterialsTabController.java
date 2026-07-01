package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.EnumNodeShape;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.MiniRecipeRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
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
	private final LinkedHashMap<MaterialAtlasNode, int[]> positions = new LinkedHashMap<>();
	private final MaterialAtlasTraceLayerCache traceCache = new MaterialAtlasTraceLayerCache();

	/** All entries provided at construction time, before gate filtering. */
	private final List<MaterialEntry> rawEntries;
	/** Entries visible to the current player, including next-tier veiled previews. */
	private List<MaterialAtlasNode> entries = List.of();

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

		if (selectedEntry != null
				&& (!entries.contains(selectedEntry)
				|| selectedEntry.visibility() != MaterialVisibility.UNLOCKED)) {
			selectedEntry = null;
		}

		int[] bounds = new int[2];
		MaterialsTabView.buildLayout(entries, positions, bounds, NODE_SIZE, path);
		contentW = bounds[0];
		contentH = bounds[1];
		panZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
	}

	@Override
	public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my, float partial) {
		traceCache.rebuildIfNeeded(entries, positions, MaterialAtlasSpec.buckets(path),
				contentW, contentH, MaterialAtlasSpec.hubX(path), MaterialAtlasSpec.hubY(path));
		MaterialsTabView.drawAtlasTrace(traceCache, gfx, ctx, panZoom);
		MaterialsTabView.drawNodes(gfx, ctx.font(), path,
				entries, positions,
				panZoom, ctx.guiLeft(), ctx.guiTop(), NODE_SIZE, nodeShape,
				tabColor, selectedEntry, nodeTransparentColor, nodeAccentColor);
	}

	@Override
	public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
		if (selectedEntry != null && selectedEntry.visibility() == MaterialVisibility.UNLOCKED) {
			MaterialsTabView.drawInfoPanel(gfx, ctx.font(), selectedEntry.entry(),
					ctx.guiLeft(), ctx.guiTop(), ctx.guiWidth(),
					tabColor, panelSeparatorColor, panelBgColor, renderer);
		}
	}

	@Override
	public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mx, int my) {
		MaterialsTabView.drawTooltip(gfx, ctx.font(), positions,
				panZoom, ctx.guiLeft(), ctx.guiTop(),
				ctx.guiWidth(), ctx.guiHeight(), NODE_SIZE,
				nodeShape, tabColor, nodeAccentColor, mx, my);
	}

	@Override
	public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
		if (btn != 0) {
			return false;
		}
		MaterialAtlasNode hit = MaterialsTabView.nodeUnder(positions, panZoom,
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

	public int getContentW() {
		return contentW;
	}

	public int getContentH() {
		return contentH;
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
