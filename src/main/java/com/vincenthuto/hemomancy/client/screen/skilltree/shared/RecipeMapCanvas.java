package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.HarbingerChromeRenderer;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.FamilyFilterLabels;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class RecipeMapCanvas {
	private static final RecipeMapRecentHistory RITE_RECENTS = new RecipeMapRecentHistory(5);
	private static final RecipeMapRecentHistory CRAFTING_RECENTS = new RecipeMapRecentHistory(5);

	private final RecipeMapEntry.Kind primaryKind;
	private final PanZoomState surfacePanZoom = new PanZoomState();
	private final PanZoomState deepPanZoom = new PanZoomState();
	private final SkillTreeDiveState diveState = new SkillTreeDiveState();
	private final RecipeMapTraceLayerCache surfaceTraceCache = new RecipeMapTraceLayerCache();
	private final RecipeMapTraceLayerCache deepTraceCache = new RecipeMapTraceLayerCache();
	private RecipeMapLayout.Result layout = RecipeMapLayout.build(List.of(), List.of());
	private List<RecipeMapLink> links = List.of();
	private List<String> families = List.of();
	private final Map<RecipeMapKey, ItemStack> icons = new HashMap<>();
	private Integer degreeFilter;
	private String familyFilter;
	private RecipeMapKey selected;
	private boolean inspectorExpanded;
	private boolean traceDirty = true;
	private int cachedTraceAccent = Integer.MIN_VALUE;
	private float deepFade;
	private RecipeMapEntry hoveredTooltipEntry;

	public RecipeMapCanvas(RecipeMapEntry.Kind primaryKind) {
		this.primaryKind = primaryKind;
	}

	public void initialise(ProgressScreenContext ctx, List<RecipeMapEntry> entries, List<String> families,
			List<RecipeMapLink> links, Function<RecipeMapEntry, ItemStack> iconProvider) {
		initialise(ctx, entries, families, links, Map.of(), iconProvider);
	}

	public void initialise(ProgressScreenContext ctx, List<RecipeMapEntry> entries, List<String> families,
			List<RecipeMapLink> links, Map<RecipeMapKey, RecipeMapLayout.AuthoredPosition> authoredPositions,
			Function<RecipeMapEntry, ItemStack> iconProvider) {
		this.families = List.copyOf(families);
		this.links = List.copyOf(links);
		this.layout = RecipeMapLayout.build(entries, families, authoredPositions);
		this.traceDirty = true;
		this.icons.clear();
		for (RecipeMapEntry entry : entries) {
			ItemStack icon = iconProvider.apply(entry);
			if (icon != null && !icon.isEmpty()) icons.put(entry.key(), icon.copy());
		}
		diveState.resetToSurface();
		deepFade = 0.0F;
		centreView(surfacePanZoom, ctx);
		centreView(deepPanZoom, ctx);
	}

	public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, int accent) {
		hoveredTooltipEntry = null;
		RecipeMapInspectorLayout inspector = inspectorLayout(ctx);
		RecipeMapInspectorLayout.IntRect viewport = inspector.mapViewport();
		updateDiveProgress(ctx, viewport);
		float surfaceAlpha = 1.0F - deepFade;
		float deepAlpha = deepFade;
		PanZoomState deepView = diveState.isDeepActive() ? deepPanZoom : surfacePanZoom;
		if (traceDirty || cachedTraceAccent != accent) {
			surfaceTraceCache.rebuildIfNeeded(RecipeMapTracePlan.build(
					layout, links, degreeFilter, familyFilter, accent, SkillTreeLayer.SURFACE),
					layout.contentWidth(), layout.contentHeight());
			deepTraceCache.rebuildIfNeeded(RecipeMapTracePlan.build(
					layout, links, degreeFilter, familyFilter, accent, SkillTreeLayer.DEEP),
					layout.contentWidth(), layout.contentHeight());
			traceDirty = false;
			cachedTraceAccent = accent;
		}
		gfx.enableScissor(viewport.left(), viewport.top(), viewport.right(), viewport.bottom());
		if (surfaceAlpha > 0.01F) surfaceTraceCache.render(gfx, ctx, surfacePanZoom, surfaceAlpha);
		if (deepAlpha > 0.01F) deepTraceCache.render(gfx, ctx, deepView, deepAlpha);
		drawLayer(gfx, ctx, mouseX, mouseY, accent, surfacePanZoom, SkillTreeLayer.SURFACE, surfaceAlpha);
		drawLayer(gfx, ctx, mouseX, mouseY, accent, deepView, SkillTreeLayer.DEEP, deepAlpha);
		gfx.disableScissor();
		if (!viewport.contains(mouseX, mouseY)) hoveredTooltipEntry = null;
		drawControls(gfx, ctx, mouseX, mouseY, accent, viewport);
	}

	public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, int accent) {
		RecipeMapEntry entry = hoveredTooltipEntry;
		if (entry == null) return;
		RecipeMapTooltip.Content tooltip = RecipeMapTooltip.content(entry);
		int maxWidth = RecipeMapTooltip.maxWidth(ctx.guiWidth());
		List<Component> lines = new ArrayList<>();
		for (String line : ScreenDrawUtils.wrapText(ctx.font(), tooltip.title(), maxWidth)) {
			lines.add(Component.literal(line).withStyle(style -> style.withColor(accent & 0xFFFFFF).withBold(true)));
		}
		lines.add(Component.literal(tooltip.context())
				.withStyle(style -> style.withColor(0xFF888888).withItalic(true)));
		boolean shiftDown = net.minecraft.client.gui.screens.Screen.hasShiftDown();
		String visibleDescription = RecipeMapTooltip.visibleDescription(entry, shiftDown);
		if (!visibleDescription.isBlank()) {
			for (String line : ScreenDrawUtils.wrapText(ctx.font(), visibleDescription, maxWidth)) {
				lines.add(Component.literal(line).withStyle(style -> style.withColor(0xFFBBBBBB)));
			}
		} else if (entry.key().kind() == RecipeMapEntry.Kind.RITE && !tooltip.description().isBlank()) {
			lines.add(Component.literal("Hold Shift to read explanation")
					.withStyle(style -> style.withColor(0xFF777777).withItalic(true)));
		}
		if (entry.unlocked()) {
			lines.add(Component.literal("Click to view details")
					.withStyle(style -> style.withColor(0xFFAA7777)));
		} else {
			lines.add(Component.literal("Requires Degree " + entry.column())
					.withStyle(style -> style.withColor(0xFFAA5555)));
		}
		boolean blueprintCue = canImprint(entry);
		if (blueprintCue) {
			StringBuilder iconPadding = new StringBuilder(" ");
			while (ctx.font().width(iconPadding.toString()) < RecipeMapTooltip.blueprintCueReservedWidth()) {
				iconPadding.append(' ');
			}
			lines.add(Component.translatable("item.hemomancy.mnemonic_blueprint.imprint")
					.withStyle(style -> style.withColor(0xFFC69ACF).withItalic(true))
					.append(Component.literal(iconPadding.toString())));
			for (int line = 0; line < RecipeMapTooltip.blueprintCueSpacerLines(); line++) {
				lines.add(Component.literal("                    "));
			}
		}
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 900);
		RecipeMapTooltipPositioner tooltipPositioner = new RecipeMapTooltipPositioner();
		gfx.renderTooltip(ctx.font(), lines.stream().map(Component::getVisualOrderText).toList(),
				tooltipPositioner, mouseX, mouseY);
		if (blueprintCue) {
			RecipeMapTooltipPositioner.CuePosition cue = tooltipPositioner.cuePosition();
			gfx.pose().pushPose();
			gfx.pose().translate(0, 0, 500);
			gfx.renderItem(new ItemStack(com.vincenthuto.hemomancy.common.init.ItemInit.mnemonic_blueprint.get()),
					cue.x(), cue.y());
			gfx.pose().popPose();
		}
		gfx.pose().popPose();
	}

	private static boolean canImprint(RecipeMapEntry entry) {
		if (RecipeMapBlueprintTarget.from(entry) == null) return false;
		net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
		return minecraft.player != null && minecraft.player.getInventory().items.stream().anyMatch(stack ->
				stack.is(com.vincenthuto.hemomancy.common.init.ItemInit.mnemonic_blueprint.get())
						&& com.vincenthuto.hemomancy.common.item.shared.MnemonicBlueprintItem.isBlank(stack));
	}

	private void drawLayer(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, int accent,
			PanZoomState view, SkillTreeLayer layer, float alpha) {
		if (alpha <= 0.01F) return;
		gfx.pose().pushPose();
		gfx.pose().translate(ctx.guiLeft() + view.panX, ctx.guiTop() + view.panY, 0);
		gfx.pose().scale(view.zoom, view.zoom, 1.0F);
		drawDegreeLabelsAndHub(gfx, ctx, accent, layer, alpha);
		drawFamilyLabels(gfx, ctx, accent, layer, alpha);
		drawNodes(gfx, ctx, mouseX, mouseY, accent, view, layer, alpha);
		gfx.pose().popPose();
	}

	private void drawDegreeLabelsAndHub(GuiGraphics gfx, ProgressScreenContext ctx, int accent,
			SkillTreeLayer layer, float alpha) {
		int centerX = layout.centerX();
		int centerY = layout.centerY();
		for (int degree = 0; degree <= 8; degree++) {
			if (SkillTreeLayerRules.layerForDegree(degree) != layer) continue;
			int radius = SkillTreeLayerRules.ringRadiusForDegree(degree, layer);
			double labelAngle = Math.toRadians(220.0);
			int labelX = centerX + (int) Math.round(Math.cos(labelAngle) * radius) + 5;
			int labelY = centerY + (int) Math.round(Math.sin(labelAngle) * radius) - 4;
			gfx.drawString(ctx.font(), "Degree " + degree, labelX, labelY,
					fadeColor(withAlpha(accent, degreeFilter == null || degreeFilter == degree ? 0xA8 : 0x45), alpha), false);
		}
		int hubSize = 42;
		gfx.fill(centerX - hubSize / 2, centerY - hubSize / 2,
				centerX + hubSize / 2, centerY + hubSize / 2, 0xE018070B);
		ScreenDrawUtils.drawSimpleBorder(gfx, centerX - hubSize / 2, centerY - hubSize / 2,
				hubSize, hubSize, fadeColor(withAlpha(accent, 0xCC), alpha));
		HarbingerChromeRenderer.drawFrame(gfx, centerX - hubSize / 2, centerY - hubSize / 2,
				hubSize, hubSize, fadeColor(accent, alpha), HarbingerChromeRenderer.State.ACTIVE);
		gfx.drawCenteredString(ctx.font(), primaryKind == RecipeMapEntry.Kind.RITE ? "Rites" : "Craft",
				centerX, centerY - 8, fadeColor(withAlpha(accent, 0xEE), alpha));
		gfx.drawCenteredString(ctx.font(), layer == SkillTreeLayer.SURFACE ? "0-4" : "5-8",
				centerX, centerY + 3, fadeColor(0xFF999999, alpha));
	}

	private void drawFamilyLabels(GuiGraphics gfx, ProgressScreenContext ctx, int accent,
			SkillTreeLayer layer, float alpha) {
		int outerRadius = SkillTreeLayerRules.outerRingRadius(layer);
		for (Map.Entry<String, Double> family : layout.familyAngles().entrySet()) {
			double angle = family.getValue();
			int endX = layout.centerX() + (int) Math.round(Math.cos(angle) * outerRadius);
			int endY = layout.centerY() + (int) Math.round(Math.sin(angle) * outerRadius);

			String label = ctx.font().plainSubstrByWidth(family.getKey(), 112);
			int labelWidth = ctx.font().width(label);
			int labelX = Mth.clamp(endX - labelWidth / 2, 8, layout.contentWidth() - labelWidth - 8);
			int labelY = Mth.clamp(endY - 4, 6, layout.contentHeight() - 16);
			gfx.drawString(ctx.font(), Component.literal(label).withStyle(style -> style.withBold(true)),
					labelX, labelY, fadeColor(withAlpha(accent, 0xD8), alpha), false);
		}
	}

	private void drawNodes(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, int accent,
			PanZoomState view, SkillTreeLayer layer, float layerAlpha) {
		double contentMouseX = view.cx(ctx.guiLeft(), mouseX);
		double contentMouseY = view.cy(ctx.guiTop(), mouseY);
		for (RecipeMapLayout.NodeBounds node : layout.nodes().values()) {
			RecipeMapEntry entry = node.entry();
			if (!entry.visible() || !passesFilters(entry)
					|| SkillTreeLayerRules.layerForDegree(entry.column()) != layer) continue;
			boolean hovered = node.contains(contentMouseX, contentMouseY);
			boolean active = entry.key().equals(selected);
			int alpha = 0xEE;
			int background = fadeColor(entry.unlocked() ? ((alpha << 24) | 0x15060A) : 0xAA111111, layerAlpha);
			int border = active ? 0xFFFFD66B : hovered && entry.unlocked() ? withAlpha(accent, 0xFF)
					: entry.unlocked() ? withAlpha(accent, alpha) : 0xFF3A3A3A;
			border = fadeColor(border, layerAlpha);
			if (entry.key().kind() == RecipeMapEntry.Kind.FLOOR) {
				drawDiamondNode(gfx, node, background, border);
			} else {
				gfx.fill(node.x(), node.y(), node.x() + node.width(), node.y() + node.height(), background);
				ScreenDrawUtils.drawSimpleBorder(gfx, node.x(), node.y(), node.width(), node.height(), border);
				HarbingerChromeRenderer.State chromeState = !entry.unlocked()
						? HarbingerChromeRenderer.State.DISABLED
						: active ? HarbingerChromeRenderer.State.ACTIVE
						: hovered ? HarbingerChromeRenderer.State.HOVERED : HarbingerChromeRenderer.State.IDLE;
				HarbingerChromeRenderer.drawFrame(gfx, node.x(), node.y(), node.width(), node.height(), border, chromeState);
			}
			ItemStack icon = icons.get(entry.key());
			if (icon != null && !icon.isEmpty()) {
				ScreenDrawUtils.renderScaledItem(gfx, icon, node.centerX(), node.centerY(),
						iconHalfSize(entry.key().kind(), node.width()));
			}
			else drawKindGlyph(gfx, ctx, entry, node, border);
			if (hovered && layerAlpha > 0.5F) hoveredTooltipEntry = entry;
		}
	}

	static int iconHalfSize(RecipeMapEntry.Kind kind, int nodeWidth) {
		if (kind != RecipeMapEntry.Kind.FLOOR) return nodeWidth / 2;
		int nativeItemHalfSize = (16 + ScreenDrawUtils.ITEM_PADDING) / 2;
		return Math.min(nodeWidth / 2, nativeItemHalfSize);
	}

	private void drawControls(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, int accent,
			RecipeMapInspectorLayout.IntRect viewport) {
		gfx.pose().pushPose();
		gfx.pose().translate(0, 0, 650);
		RecipeMapControlsLayout.Result controls = RecipeMapControlsLayout.calculate(viewport);
		ProgressFilterControlsView.draw(gfx, ctx, controls.degree(),
				degreeFilter == null ? "Degree: All" : "Degree: " + degreeFilter, accent, mouseX, mouseY);
		ProgressFilterControlsView.draw(gfx, ctx, controls.family(),
				FamilyFilterLabels.display(FamilyFilterLabels.nickname(familyFilter)), accent, mouseX, mouseY);
		ProgressFilterControlsView.draw(gfx, ctx, controls.layer(),
				"Layer: " + (diveState.isDeepActive() ? "5-8" : "0-4"), accent, mouseX, mouseY);
		List<RecipeMapKey> recents = history().entries();
		if (!recents.isEmpty()) {
			gfx.drawString(ctx.font(), "Recent", controls.recentLabelX(),
					controls.recentBaselineY(), 0xFF888888, false);
			int recentX = controls.firstRecent().left();
			for (RecipeMapKey key : recents) {
				RecipeMapLayout.NodeBounds node = layout.node(key);
				if (node == null || !node.entry().visible() || !isInteractiveLayer(node.entry())) continue;
				gfx.fill(recentX, controls.firstRecent().top(),
						recentX + controls.firstRecent().width(), controls.firstRecent().bottom(), 0xCC12070B);
				boolean recentHovered = inside(mouseX, mouseY, recentX, controls.firstRecent().top(),
						controls.firstRecent().width(), controls.firstRecent().height());
				HarbingerChromeRenderer.drawFrame(gfx, recentX, controls.firstRecent().top(),
						controls.firstRecent().width(), controls.firstRecent().height(), accent,
						recentHovered ? HarbingerChromeRenderer.State.HOVERED : HarbingerChromeRenderer.State.IDLE);
				ItemStack icon = icons.get(key);
				if (icon != null && !icon.isEmpty()) {
					gfx.pose().pushPose();
					gfx.pose().translate(recentX + 1, controls.firstRecent().top() + 1, 0);
					gfx.pose().scale(0.75F, 0.75F, 1.0F);
					gfx.renderItem(icon, 0, 0);
					gfx.pose().popPose();
				} else {
					gfx.drawCenteredString(ctx.font(), "•", recentX + 9,
							controls.recentBaselineY() + 1, accent);
				}
				recentX += 21;
			}
		}
		gfx.pose().popPose();
	}

	public ClickResult mouseClicked(ProgressScreenContext ctx, double mouseX, double mouseY, int button) {
		RecipeMapInspectorLayout.IntRect viewport = inspectorLayout(ctx).mapViewport();
		RecipeMapControlsLayout.Result controls = RecipeMapControlsLayout.calculate(viewport);
		if (controls.degree().contains(mouseX, mouseY)) {
			cycleDegree(button == 1 ? -1 : 1);
			return new ClickResult(true, null);
		}
		if (controls.family().contains(mouseX, mouseY)) {
			cycleFamily(button == 1 ? -1 : 1);
			return new ClickResult(true, null);
		}
		if (controls.layer().contains(mouseX, mouseY)) {
			toggleLayer(ctx);
			return new ClickResult(true, null);
		}
		int recentX = controls.firstRecent().left();
		for (RecipeMapKey key : history().entries()) {
			RecipeMapLayout.NodeBounds recentNode = layout.node(key);
			if (recentNode == null || !recentNode.entry().visible() || !isInteractiveLayer(recentNode.entry())) continue;
			if (inside(mouseX, mouseY, recentX, controls.firstRecent().top(),
					controls.firstRecent().width(), controls.firstRecent().height())) {
				return recentNode.entry().unlocked() ? select(ctx, key) : new ClickResult(true, null);
			}
			recentX += 21;
		}
		if (!viewport.contains(mouseX, mouseY)) return new ClickResult(false, null);
		PanZoomState view = activePanZoom();
		RecipeMapLayout.NodeBounds node = layout.visibleNodeAt(
				view.cx(ctx.guiLeft(), mouseX), view.cy(ctx.guiTop(), mouseY),
				entry -> passesFilters(entry) && isInteractiveLayer(entry));
		if (node != null) {
			if (node.entry().unlocked() && passesFilters(node.entry()) && isInteractiveLayer(node.entry())) {
				return select(ctx, node.entry().key());
			}
			return new ClickResult(true, null);
		}
		return new ClickResult(false, null);
	}

	/** Returns an unlocked, currently visible node under the pointer without changing selection. */
	public RecipeMapEntry entryAt(ProgressScreenContext ctx, double mouseX, double mouseY) {
		RecipeMapInspectorLayout.IntRect viewport = inspectorLayout(ctx).mapViewport();
		if (!viewport.contains(mouseX, mouseY)) return null;
		PanZoomState view = activePanZoom();
		RecipeMapLayout.NodeBounds node = layout.visibleNodeAt(
				view.cx(ctx.guiLeft(), mouseX), view.cy(ctx.guiTop(), mouseY),
				entry -> passesFilters(entry) && isInteractiveLayer(entry));
		if (node == null || !node.entry().unlocked() || !passesFilters(node.entry())
				|| !isInteractiveLayer(node.entry())) return null;
		return node.entry();
	}

	public ClickResult select(ProgressScreenContext ctx, RecipeMapKey key) {
		RecipeMapLayout.NodeBounds node = layout.node(key);
		if (node == null || !node.entry().visible() || !node.entry().unlocked()
				|| !isInteractiveLayer(node.entry())) return new ClickResult(true, null);
		selected = key;
		inspectorExpanded = true;
		history().touch(key);
		return new ClickResult(true, key);
	}

	public void clamp(ProgressScreenContext ctx) {
		RecipeMapInspectorLayout.IntRect viewport = inspectorLayout(ctx).mapViewport();
		activePanZoom().clamp(layout.contentWidth(), layout.contentHeight(), viewport.width(), viewport.height());
	}

	public void resetView(ProgressScreenContext ctx) {
		centreView(activePanZoom(), ctx);
	}

	public boolean mouseDragged(ProgressScreenContext ctx, int button, double dx, double dy) {
		if (button != 0) return false;
		activePanZoom().applyDrag(dx, dy);
		clamp(ctx);
		return true;
	}

	public boolean mouseScrolled(ProgressScreenContext ctx, double mouseX, double mouseY, double delta) {
		RecipeMapInspectorLayout.IntRect viewport = inspectorLayout(ctx).mapViewport();
		if (!viewport.contains(mouseX, mouseY)) return false;
		PanZoomState view = activePanZoom();
		view.applyScroll(ctx.guiLeft(), ctx.guiTop(), mouseX, mouseY, delta);
		view.clamp(layout.contentWidth(), layout.contentHeight(), viewport.width(), viewport.height());
		if (diveState.isDeepActive()) {
			if (delta < 0 && diveState.updateDeepZoom(deepPanZoom.zoom)) {
				centreView(surfacePanZoom, ctx);
				deepFade = 0.0F;
				resetLayerFilter();
				closeDetails();
			}
			return true;
		}
		updateDiveProgress(ctx, viewport);
		return true;
	}

	public RecipeMapInspectorLayout inspectorLayout(ProgressScreenContext ctx) {
		if (selected == null) {
			RecipeMapInspectorLayout.IntRect full = new RecipeMapInspectorLayout.IntRect(
					ctx.guiLeft(), ctx.guiTop(), ctx.guiWidth(), ctx.guiHeight());
			RecipeMapInspectorLayout.IntRect empty = new RecipeMapInspectorLayout.IntRect(
					ctx.guiLeft() + ctx.guiWidth(), ctx.guiTop(), 0, 0);
			return new RecipeMapInspectorLayout(full, empty, empty, empty, false, false);
		}
		if (primaryKind == RecipeMapEntry.Kind.CRAFTING) {
			return RecipeMapInspectorLayout.calculateCrafting(
					ctx.guiLeft(), ctx.guiTop(), ctx.guiWidth(), ctx.guiHeight(), inspectorExpanded);
		}
		return RecipeMapInspectorLayout.calculate(
				ctx.guiLeft(), ctx.guiTop(), ctx.guiWidth(), ctx.guiHeight(), inspectorExpanded);
	}

	public void toggleInspector() {
		if (selected != null) inspectorExpanded = !inspectorExpanded;
	}

	public boolean closeDetails() {
		if (selected == null) return false;
		selected = null;
		inspectorExpanded = false;
		return true;
	}

	public RecipeMapKey selected() { return selected; }
	public PanZoomState panZoom() { return activePanZoom(); }
	public int contentWidth() { return layout.contentWidth(); }
	public int contentHeight() { return layout.contentHeight(); }
	public int viewportWidth(ProgressScreenContext ctx) { return inspectorLayout(ctx).mapViewport().width(); }
	public RecipeMapLayout.Result layout() { return layout; }

	private boolean passesFilters(RecipeMapEntry entry) {
		return (degreeFilter == null || entry.column() == degreeFilter)
				&& (familyFilter == null || familyFilter.equals(entry.family()));
	}

	private void cycleDegree(int direction) {
		int min = interactiveLayer() == SkillTreeLayer.SURFACE ? 0 : SkillTreeLayerRules.DEEP_MIN_DEGREE;
		int max = interactiveLayer() == SkillTreeLayer.SURFACE ? SkillTreeLayerRules.SURFACE_MAX_DEGREE : 8;
		int current = degreeFilter == null ? min - 1 : degreeFilter;
		current += direction;
		if (current > max) current = min - 1;
		if (current < min - 1) current = max;
		degreeFilter = current < min ? null : current;
		traceDirty = true;
	}

	private void cycleFamily(int direction) {
		List<String> available = new ArrayList<>();
		available.add(null);
		available.addAll(families.stream().filter(family -> !RecipeMapLayout.MISC_FAMILY.equals(family)
				|| layout.entries().stream().anyMatch(entry -> family.equals(entry.family()))).toList());
		int index = available.indexOf(familyFilter);
		index = Math.floorMod(index + direction, available.size());
		familyFilter = available.get(index);
		traceDirty = true;
	}

	private void toggleLayer(ProgressScreenContext ctx) {
		if (!diveState.toggleLayer(ctx.playerDegree())) return;
		closeDetails();
		if (diveState.isDeepActive()) {
			centreView(deepPanZoom, ctx);
			deepFade = 1.0F;
		} else {
			centreView(surfacePanZoom, ctx);
			deepFade = 0.0F;
		}
		resetLayerFilter();
	}

	private RecipeMapRecentHistory history() {
		return primaryKind == RecipeMapEntry.Kind.RITE ? RITE_RECENTS : CRAFTING_RECENTS;
	}

	private static void drawKindGlyph(GuiGraphics gfx, ProgressScreenContext ctx, RecipeMapEntry entry,
			RecipeMapLayout.NodeBounds node, int color) {
		String glyph = switch (entry.key().kind()) {
			case SIGIL -> "◇";
			case RITE -> "R";
			case FLOOR -> "F";
			case CRAFTING -> "C";
		};
		gfx.drawCenteredString(ctx.font(), glyph, node.centerX(), node.y() + 9, color);
	}

	private static void drawDiamondNode(GuiGraphics gfx, RecipeMapLayout.NodeBounds node,
			int background, int border) {
		int centerX = node.centerX();
		int centerY = node.centerY();
		int half = (Math.min(node.width(), node.height()) - 1) / 2;
		for (int offsetY = -half; offsetY <= half; offsetY++) {
			int span = half - Math.abs(offsetY);
			int y = centerY + offsetY;
			gfx.fill(centerX - span, y, centerX + span + 1, y + 1, border);
			if (span > 1) gfx.fill(centerX - span + 1, y, centerX + span, y + 1, background);
		}
	}

	private static int withAlpha(int color, int alpha) {
		return (alpha << 24) | (color & 0x00FFFFFF);
	}

	private static int fadeColor(int color, float alpha) {
		return withAlpha(color, Math.round(((color >>> 24) & 0xFF) * Mth.clamp(alpha, 0.0F, 1.0F)));
	}

	private PanZoomState activePanZoom() {
		return diveState.isDeepActive() ? deepPanZoom : surfacePanZoom;
	}

	private SkillTreeLayer interactiveLayer() {
		return diveState.isDeepActive() || deepFade >= 0.5F ? SkillTreeLayer.DEEP : SkillTreeLayer.SURFACE;
	}

	private boolean isInteractiveLayer(RecipeMapEntry entry) {
		return SkillTreeLayerRules.layerForDegree(entry.column()) == interactiveLayer();
	}

	private void updateDiveProgress(ProgressScreenContext ctx, RecipeMapInspectorLayout.IntRect viewport) {
		if (diveState.isDeepActive()) {
			deepFade = 1.0F;
			return;
		}
		double centerX = ctx.guiLeft() + surfacePanZoom.panX + layout.centerX() * surfacePanZoom.zoom;
		double centerY = ctx.guiTop() + surfacePanZoom.panY + layout.centerY() * surfacePanZoom.zoom;
		float focus = SkillTreeLayerRules.centerFocus(centerX - viewport.left(), centerY - viewport.top(),
				viewport.width(), viewport.height());
		float surfaceDive = SkillTreeLayerRules.diveProgress(ctx.playerDegree(), surfacePanZoom.zoom, focus);
		if (diveState.updateSurfaceDive(ctx.playerDegree(), surfaceDive)) {
			centreView(deepPanZoom, ctx);
			deepFade = 1.0F;
			resetLayerFilter();
			closeDetails();
			return;
		}
		deepFade = diveState.deepFade(surfaceDive);
	}

	private void centreView(PanZoomState view, ProgressScreenContext ctx) {
		RecipeMapInspectorLayout.IntRect viewport = inspectorLayout(ctx).mapViewport();
		view.centreOn(layout.contentWidth(), layout.contentHeight(), viewport.width(), viewport.height());
	}

	private void resetLayerFilter() {
		degreeFilter = null;
		traceDirty = true;
	}

	private static boolean inside(double x, double y, int left, int top, int width, int height) {
		return x >= left && x < left + width && y >= top && y < top + height;
	}

	public record ClickResult(boolean consumed, RecipeMapKey selection) {}
}
