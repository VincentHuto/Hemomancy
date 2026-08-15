package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import com.vincenthuto.hemomancy.client.screen.util.AbocipherText;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ManipulationTreeInit;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.BloodMemoryItem;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hemomancy.common.manipulation.ManipulationRankGates;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class ManipulationsTabController implements IProgressTab {

    private static final int NODE_SIZE = 26;
    private static final int NODE_GAP_X = 80;
    private static final int INNER_MANIPULATION_RADIUS = 175;
    private static final int COL_NODE_BG     = 0xCC1A0505;
    private static final int COL_NODE_BORDER_LOCK = 0xFF333333;
    private static final float INFO_PANEL_Z = 400.0F;
    private static final float MANIP_TOOLTIP_Z = INFO_PANEL_Z + 500.0F;
    private static final int INFO_PANEL_SHADOW = 0xAA000000;
    private static final int INFO_PANEL_MARGIN = 8;
    private static final int MANIP_PANEL_TOP_CLEARANCE = 24;
    private static final int MANIP_PANEL_BOTTOM_PAD = 3;
    private static final int MANIP_DESCRIPTION_LINE_H = 10;
    private static final int MANIP_DESCRIPTION_TOP_PAD = 3;
    private static final int MANIP_DESCRIPTION_BOTTOM_PAD = 6;
    private static final int MANIP_STAT_COLUMNS = 2;
    private static final int MANIP_STAT_ROWS = 3;
    private static final int MANIP_STAT_CELL_H = 28;
    private static final int MANIP_STAT_COL_GAP = 6;
    private static final int MANIP_STAT_ROW_GAP = 2;

    private static final int KNOWN_MANIP_REFRESH_TICKS = 60; // ~1s at ~60 FPS
    private static final int SCAR_KNOWLEDGE_REFRESH_TICKS = 60;

    private final TendencyTraceLayerCache traceCache = new TendencyTraceLayerCache();
    private final TendencyTraceLayerCache scarTraceCache = new TendencyTraceLayerCache();
    private final ScarsTabState scarState = new ScarsTabState();
    private final PanZoomState panZoom = new PanZoomState();
    private final CyclingFamilyFilter<EnumBloodTendency> familyFilter =
            new CyclingFamilyFilter<>(List.of(EnumBloodTendency.values()));
    private int manipRingCenterX, manipRingCenterY;
    private final Map<ManipulationTreeEntry, int[]> manipPositions = new HashMap<>();
    private final Set<String> knownManipNames = new HashSet<>();
    private final Map<String, ItemStack> manipMemoryItems = new HashMap<>();
    private int contentW, contentH;
    private ManipulationTreeEntry selectedEntry = null;
    private int playerDegree;
    private int knownManipRefreshCooldown = 0;
    private int scarKnowledgeRefreshCooldown = 0;

    @Override
    public void onInit(ProgressScreenContext ctx) {
        this.playerDegree = ctx.playerDegree();
        loadScarEntries();
        buildManipLayout();
        cacheKnownManipulations();
        refreshScarKnowledge();
        panZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
    }

    private void loadScarEntries() {
        scarState.rebuild(ScarsTabController.loadEntries(Minecraft.getInstance()));
    }

    private void buildManipLayout() {
        manipPositions.clear();
        contentW = 0;
        contentH = 0;
        if (ManipulationTreeInit.ENTRIES.isEmpty()) ManipulationTreeInit.init();
        if (ManipulationTreeInit.ENTRIES.isEmpty()) return;

        final int padding = 40;
        Map<EnumBloodTendency, List<ManipulationTreeEntry>> byTendency = new java.util.EnumMap<>(EnumBloodTendency.class);
        List<ManipulationTreeEntry> fallbackEntries = new ArrayList<>();

        for (ManipulationTreeEntry entry : ManipulationTreeInit.ENTRIES) {
            BloodManipulation manip = entry.resolve();
            EnumBloodTendency tendency = manip != null ? manip.getTend() : null;
            if (tendency == null) {
                fallbackEntries.add(entry);
                continue;
            }
            byTendency.computeIfAbsent(tendency, k -> new ArrayList<>()).add(entry);
        }

        Map<ManipulationTreeEntry, int[]> rawPositions = new HashMap<>();
        int maxClusterW = 0;
        int maxClusterH = 0;
        int tendencyCount = 0;
        Map<EnumBloodTendency, int[]> boundsByTendency = new java.util.EnumMap<>(EnumBloodTendency.class);

        for (EnumBloodTendency tend : EnumBloodTendency.values()) {
            List<ManipulationTreeEntry> group = byTendency.getOrDefault(tend, List.of());
            if (group.isEmpty()) continue;
            tendencyCount++;
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
            for (ManipulationTreeEntry entry : group) {
                minX = Math.min(minX, entry.getX());
                minY = Math.min(minY, entry.getY());
                maxX = Math.max(maxX, entry.getX());
                maxY = Math.max(maxY, entry.getY());
            }
            boundsByTendency.put(tend, new int[]{minX, maxX, minY, maxY});
            maxClusterW = Math.max(maxClusterW, maxX - minX + NODE_SIZE);
            maxClusterH = Math.max(maxClusterH, maxY - minY + NODE_SIZE);
        }

        if (tendencyCount == 0) {
            for (ManipulationTreeEntry entry : ManipulationTreeInit.ENTRIES) {
                int x = entry.getX();
                int y = entry.getY();
                manipPositions.put(entry, new int[]{x, y});
                contentW = Math.max(contentW, x + NODE_SIZE + 20);
                contentH = Math.max(contentH, y + NODE_SIZE + 24);
            }
            return;
        }

        List<Double> localRadialOffsets = new ArrayList<>();
        for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
            List<ManipulationTreeEntry> group = byTendency.getOrDefault(tendency, List.of());
            if (group.isEmpty()) continue;
            int[] bounds = boundsByTendency.get(tendency);
            float clusterCenterX = (bounds[0] + bounds[1]) * 0.5f;
            float clusterCenterY = (bounds[2] + bounds[3]) * 0.5f;
            double angle = Math.toRadians(-90f + tendency.ordinal() * 45f);
            double directionX = Math.cos(angle);
            double directionY = Math.sin(angle);
            for (ManipulationTreeEntry entry : group) {
                localRadialOffsets.add((entry.getX() - clusterCenterX) * directionX
                        + (entry.getY() - clusterCenterY) * directionY);
            }
        }
        int radius = ManipulationRingLayout.anchorRadius(INNER_MANIPULATION_RADIUS, localRadialOffsets);
        int clusterHalf = Math.max(maxClusterW, maxClusterH) / 2;
        float centerX = padding + clusterHalf + radius;
        float centerY = padding + clusterHalf + radius;

        for (EnumBloodTendency tend : EnumBloodTendency.values()) {
            List<ManipulationTreeEntry> group = byTendency.getOrDefault(tend, List.of());
            if (group.isEmpty()) continue;
            int[] b = boundsByTendency.get(tend);
            float clusterCenterX = (b[0] + b[1]) * 0.5f;
            float clusterCenterY = (b[2] + b[3]) * 0.5f;
            double angle = Math.toRadians(-90f + tend.ordinal() * 45f);
            float anchorX = centerX + (float)Math.cos(angle) * radius;
            float anchorY = centerY + (float)Math.sin(angle) * radius;
            for (ManipulationTreeEntry entry : group) {
                int x = Math.round(anchorX + (entry.getX() - clusterCenterX));
                int y = Math.round(anchorY + (entry.getY() - clusterCenterY));
                rawPositions.put(entry, new int[]{x, y});
            }
        }

        if (!fallbackEntries.isEmpty()) {
            int y = Math.round(centerY + radius + clusterHalf + NODE_GAP_X);
            int startX = Math.round(centerX - ((fallbackEntries.size() - 1) * NODE_GAP_X) * 0.5f);
            for (int i = 0; i < fallbackEntries.size(); i++) {
                rawPositions.put(fallbackEntries.get(i), new int[]{startX + i * NODE_GAP_X, y});
            }
        }

        Map<EnumBloodTendency, Integer> outerRadiusByTendency = new EnumMap<>(EnumBloodTendency.class);
        for (EnumBloodTendency tendency : EnumBloodTendency.values()) {
            double angle = Math.toRadians(-90f + tendency.ordinal() * 45f);
            double directionX = Math.cos(angle);
            double directionY = Math.sin(angle);
            int outerRadius = radius;
            for (ManipulationTreeEntry entry : byTendency.getOrDefault(tendency, List.of())) {
                int[] pos = rawPositions.get(entry);
                if (pos == null) continue;
                int projection = (int) Math.ceil((pos[0] - centerX) * directionX
                        + (pos[1] - centerY) * directionY) + NODE_SIZE / 2;
                outerRadius = Math.max(outerRadius, projection);
            }
            outerRadiusByTendency.put(tendency, outerRadius);
        }

        List<TendencyScarLayout.Node> scarNodes = scarState.entries.stream()
                .map(entry -> {
                    ScarTreeLayout.Point authored = scarState.positions.get(entry.id().toString());
                    return new TendencyScarLayout.Node(entry.id().toString(), entry.tendency(),
                            entry.tier(), entry.sideBranch(),
                            authored != null ? authored.x() : null,
                            authored != null ? authored.y() : null);
                })
                .toList();
        Map<String, TendencyScarLayout.Point> rawScarPositions = TendencyScarLayout.arrange(
                Math.round(centerX), Math.round(centerY), outerRadiusByTendency, scarNodes);

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (int[] pos : rawPositions.values()) {
            minX = Math.min(minX, pos[0]);
            minY = Math.min(minY, pos[1]);
            maxX = Math.max(maxX, pos[0]);
            maxY = Math.max(maxY, pos[1]);
        }
        for (TendencyScarLayout.Point point : rawScarPositions.values()) {
            minX = Math.min(minX, point.x());
            minY = Math.min(minY, point.y());
            maxX = Math.max(maxX, point.x());
            maxY = Math.max(maxY, point.y());
        }
        int offsetX = padding - minX;
        int offsetY = padding - minY;
        for (var e : rawPositions.entrySet()) {
            int[] p = e.getValue();
            manipPositions.put(e.getKey(), new int[]{p[0] + offsetX, p[1] + offsetY});
        }
        scarState.positions.clear();
        for (var entry : rawScarPositions.entrySet()) {
            TendencyScarLayout.Point point = entry.getValue();
            scarState.positions.put(entry.getKey(),
                    new ScarTreeLayout.Point(point.x() + offsetX, point.y() + offsetY));
        }
        contentW = maxX + offsetX + NODE_SIZE + padding;
        contentH = maxY + offsetY + NODE_SIZE + 24 + padding;
        manipRingCenterX = Math.round(centerX) + offsetX;
        manipRingCenterY = Math.round(centerY) + offsetY;
    }

    private void cacheKnownManipulations() {
        knownManipNames.clear();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            HemoCapabilityAccess.getKnownManipulations(mc.player).ifPresent(cap -> {
                for (BloodManipulation m : cap.getManipList()) {
                    if (m != null && m.getName() != null) knownManipNames.add(m.getName());
                }
            });
        }
        buildManipMemoryItemLookup();
    }

    private void refreshScarKnowledge() {
        ScarsTabController.refreshKnowledge(scarState, Minecraft.getInstance());
    }

    private void buildManipMemoryItemLookup() {
        if (!manipMemoryItems.isEmpty()) return;
        for (Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            if (item instanceof BloodMemoryItem memItem) {
                BloodManipulation manip = memItem.getManip();
                if (manip != null && manip.getName() != null) {
                    manipMemoryItems.put(manip.getName(), new ItemStack(item));
                }
            }
        }
    }

	private static int manipMinDegree(EnumManipulationRank rank) {
		return ManipulationRankGates.minDegreeForRank(rank);
	}

	private boolean isManipRankLocked(BloodManipulation manip) {
		if (manip == null) return false;
		return !ManipulationRankGates.playerMeetsRank(playerDegree, manip.getRank());
	}

    private int sx(ProgressScreenContext ctx, int cx) { return panZoom.sx(ctx.guiLeft(), cx); }
    private int sy(ProgressScreenContext ctx, int cy) { return panZoom.sy(ctx.guiTop(), cy); }
    private int halfNode() { return panZoom.halfNode(NODE_SIZE); }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
        tickKnownManipCache();
        tickScarKnowledgeCache();
        traceCache.rebuildIfNeeded(visibleManipulationEntries(), manipPositions, knownManipNames,
                playerDegree, contentW, contentH, manipRingCenterX, manipRingCenterY);
        traceCache.render(gfx, ctx, panZoom);
        scarTraceCache.rebuildIfNeeded(visibleScarTraceNodes(), contentW, contentH,
                manipRingCenterX, manipRingCenterY);
        scarTraceCache.render(gfx, ctx, panZoom);
        drawManipTendencyStar(gfx, ctx);
        drawManipNodes(gfx, ctx);
        ScarsTabView.drawNodes(gfx, ctx, scarState, panZoom, playerDegree, familyFilter.selected());
        FamilyFilterControlView.draw(gfx, ctx, familyLabel(), 0xFFCC8833, mouseX, mouseY);
    }

    @Override
    public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        ScarTreeEntry selectedScar = scarState.selectedEntry();
        if (selectedScar != null) ScarsTabView.drawDetails(gfx, ctx, selectedScar, scarState, playerDegree);
        else if (selectedEntry != null) drawManipInfoPanel(gfx, ctx, selectedEntry);
    }

    @Override
    public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        drawManipTooltip(gfx, ctx, mouseX, mouseY);
        ScarsTabView.drawTooltip(gfx, ctx, scarState, panZoom, playerDegree,
                familyFilter.selected(), mouseX, mouseY);
    }

    private void tickKnownManipCache() {
        if (knownManipRefreshCooldown > 0) {
            knownManipRefreshCooldown--;
            return;
        }
        knownManipRefreshCooldown = KNOWN_MANIP_REFRESH_TICKS;
        cacheKnownManipulations();
    }

    private void tickScarKnowledgeCache() {
        if (scarKnowledgeRefreshCooldown > 0) {
            scarKnowledgeRefreshCooldown--;
            return;
        }
        scarKnowledgeRefreshCooldown = SCAR_KNOWLEDGE_REFRESH_TICKS;
        refreshScarKnowledge();
    }

    private List<TendencyTraceNode> visibleScarTraceNodes() {
        Map<String, List<String>> parents = new HashMap<>();
        for (ScarTreeLayout.Edge edge : scarState.edges) {
            parents.computeIfAbsent(edge.toId(), ignored -> new ArrayList<>()).add(edge.fromId());
        }
        List<TendencyTraceNode> nodes = new ArrayList<>();
        for (ScarTreeEntry entry : scarState.entries) {
            if (!familyFilter.includes(entry.tendency())) continue;
            ScarTreeLayout.Point point = scarState.positions.get(entry.id().toString());
            if (point == null) continue;
            nodes.add(new TendencyTraceNode(entry.id().toString(), entry.tendency(), point.x(), point.y(),
                    parents.getOrDefault(entry.id().toString(), List.of()),
                    scarState.knownScarIds.contains(entry.id()),
                    ScarsTabController.isTierLocked(entry.tier(), playerDegree)));
        }
        return nodes;
    }

    private void drawManipTendencyStar(GuiGraphics gfx, ProgressScreenContext ctx) {
        TendencyStarRenderer.draw(gfx, ctx, panZoom, manipRingCenterX, manipRingCenterY);
    }
	private float animTime = 0f;

    private void drawManipNodes(GuiGraphics gfx, ProgressScreenContext ctx) {
        animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
        int hn = halfNode();

        for (var e : manipPositions.entrySet()) {
            ManipulationTreeEntry entry = e.getKey();
            if (!isVisibleFamily(entry)) continue;
            int[] pos = e.getValue();
            int nx = sx(ctx, pos[0]);
            int ny = sy(ctx, pos[1]);

            BloodManipulation manip = entry.resolve();
            boolean known = knownManipNames.contains(entry.getManipName());
            boolean rankLocked = isManipRankLocked(manip);
            boolean veilUnknown = shouldVeilUnknown(known, rankLocked);
            EnumNodeShape shape = entry.getNodeShape();

            int tendR = 128, tendG = 128, tendB = 128;
            if (manip != null) {
                ParticleColor pc = manip.getTend().getColor();
                tendR = (int)pc.getRed();
                tendG = (int)pc.getGreen();
                tendB = (int)pc.getBlue();
            }

            int borderColor;
            if (rankLocked) {
                borderColor = COL_NODE_BORDER_LOCK;
            } else if (known) {
                borderColor = 0xFF000000 | (tendR << 16) | (tendG << 8) | tendB;
                float pulse = 0.5f + 0.5f * Mth.sin(time * 2f + entry.getManipName().hashCode() * 0.1f);
                int ga = (int)(35 * pulse);
                int gr = (int)(tendR * 0.6f);
                int gg = (int)(tendG * 0.6f);
                int gb = (int)(tendB * 0.6f);
                NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn + 3, (ga << 24) | (gr << 16) | (gg << 8) | gb);
            } else {
                int dr = (int)(tendR * 0.3f);
                int dg = (int)(tendG * 0.3f);
                int db = (int)(tendB * 0.3f);
                borderColor = 0xFF000000 | (dr << 16) | (dg << 8) | db;
            }

            boolean isSelected = entry == selectedEntry;
            if (isSelected) {
                float selPulse = 0.5f + 0.5f * Mth.sin(time * 3.0f);
                int selAlpha = (int)(55 * selPulse);
                int selColor = (selAlpha << 24) | (tendR << 16) | (tendG << 8) | tendB;
                NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn + 5, selColor);
            }

            int fill = known && !rankLocked ? COL_NODE_BG : 0xCC0D0303;
            NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn, fill);
            NodeShapeRenderer.drawOutline(gfx, shape, nx, ny, hn, borderColor);
			if (shape == EnumNodeShape.SQUARE) {
				HarbingerChromeRenderer.drawFrame(gfx, nx - hn, ny - hn, hn * 2, hn * 2, borderColor,
						rankLocked ? HarbingerChromeRenderer.State.DISABLED
								: selectedEntry == entry ? HarbingerChromeRenderer.State.ACTIVE
								: HarbingerChromeRenderer.State.IDLE);
			}

            if (rankLocked) {
                NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn - 1, 0xBB000000);
                if (panZoom.zoom >= 0.5f) gfx.drawCenteredString(ctx.font(), "?", nx, ny - 4, 0xFF111111);
                continue;
            }

            if (panZoom.zoom >= 0.5f) {
                ItemStack memoryStack = manipMemoryItems.get(entry.getManipName());
                if (memoryStack != null && !memoryStack.isEmpty()) {
                    ScreenDrawUtils.renderScaledItem(gfx, memoryStack, nx, ny, hn);
                } else {
                    String sym = "?";
                    if (manip != null && manip.getType() != null) {
                        if (manip.getType() == com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType.QUICK) {
                            sym = "\u26A1";
                        } else if (manip.getType() == com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType.CONTINUOUS) {
                            sym = "\u221E";
                        } else if (manip.getType() == com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType.PASSIVE) {
                            sym = "\u25C6";
                        } else if (manip.getType() == com.vincenthuto.hemomancy.common.manipulation.EnumManipulationType.CHARGED) {
                            sym = "\u25B2";
                        }
                    }
                    int textCol = known ? 0xFFFFFFFF : 0xFF555555;
                    gfx.drawCenteredString(ctx.font(), sym, nx, ny - 4, textCol);
                }

                if (manip != null && panZoom.zoom >= 0.7f) {
                    String label = HLTextUtils.toProperCase(manip.getName().replace("_", " "));
                    int labelCol = known ? (0xFF000000 | (tendR << 16) | (tendG << 8) | tendB) : 0xFF444444;
                    int maxLabelW = Math.max(20, (int)(NODE_GAP_X * panZoom.zoom) - 4);
                    List<String> lines = ScreenDrawUtils.wrapText(ctx.font(), label, maxLabelW);
                    int ly = ny + hn + 3;
                    for (String line : lines) {
                        Component labelText = veilUnknown ? AbocipherText.veil(line) : Component.literal(line);
                        gfx.drawCenteredString(ctx.font(), labelText, nx, ly, labelCol);
                        ly += ctx.font().lineHeight;
                    }
                }
            }
        }
    }

    private void drawManipTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        boolean insideGui = mouseX >= ctx.guiLeft() && mouseX < ctx.guiLeft() + ctx.guiWidth()
                && mouseY >= ctx.guiTop() && mouseY < ctx.guiTop() + ctx.guiHeight();
        if (!insideGui) return;
        int hn = halfNode();

        for (var e : manipPositions.entrySet()) {
            ManipulationTreeEntry entry = e.getKey();
            if (!isVisibleFamily(entry)) continue;
            int[] pos = e.getValue();
            int nx = sx(ctx, pos[0]), ny = sy(ctx, pos[1]);
            if (!NodeShapeRenderer.isInside(entry.getNodeShape(), mouseX, mouseY, nx, ny, hn)) continue;

            BloodManipulation manip = entry.resolve();
            boolean known = knownManipNames.contains(entry.getManipName());
            boolean rankLocked = isManipRankLocked(manip);
            boolean veilUnknown = shouldVeilUnknown(known, rankLocked);

            List<Component> tip = new ArrayList<>();
            if (rankLocked) {
                tip.add(Component.literal("???").withStyle(s -> s.withColor(0x555555).withBold(true)));
                if (manip != null) {
                    int reqDeg = manipMinDegree(manip.getRank());
                    EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(reqDeg);
                    String degreeName = needed != null ? needed.getTitle() : ("Degree " + reqDeg);
                    tip.add(Component.literal("Requires: " + degreeName).withStyle(s -> s.withColor(0xAA4444)));
                }
            } else {
                String pretty = manip != null
                        ? HLTextUtils.toProperCase(manip.getName().replace("_", " "))
                        : HLTextUtils.toProperCase(entry.getManipName().replace("_", " "));
                int nameCol = known ? 0xFFAA44 : 0x888888;
                Component nameText = Component.literal(pretty).withStyle(s -> s.withColor(nameCol).withBold(true));
                tip.add(veilUnknown ? AbocipherText.veil(nameText) : nameText);
                tip.add(Component.literal(known ? "Known" : "Unknown")
                        .withStyle(s -> s.withColor(known ? 0x44AA44 : 0xAA4444).withItalic(!known)));

                if (manip != null) {
                    tip.add(Component.literal("Rank: " + HLTextUtils.toProperCase(manip.getRank().name()))
                            .withStyle(s -> s.withColor(0x888888)));
                    ParticleColor pc = manip.getTend().getColor();
                    int tendCol = (int)pc.getRed() << 16 | (int)pc.getGreen() << 8 | (int)pc.getBlue();
                    String tendTipName = HLTextUtils.toProperCase(manip.getTend().name());
                    double alignReq = manip.getAlignLevel();
                    String tendTipText = alignReq > 0
                            ? "Tendency: " + tendTipName + " (" + (int)alignReq + ")"
                            : "Tendency: " + tendTipName;
                    tip.add(Component.literal(tendTipText).withStyle(s -> s.withColor(tendCol)));
                    EnumBloodTendency secondaryTend = manip.getSecondaryTend();
                    if (secondaryTend != null) {
                        ParticleColor secPc = secondaryTend.getColor();
                        int secCol = (int)secPc.getRed() << 16 | (int)secPc.getGreen() << 8 | (int)secPc.getBlue();
                        tip.add(Component.literal("Secondary: " + HLTextUtils.toProperCase(secondaryTend.name()))
                                .withStyle(s -> s.withColor(secCol)));
                    }
                }

                if (!entry.getParentNames().isEmpty()) {
                    StringBuilder sb = new StringBuilder("Relates to: ");
                    for (int i = 0; i < entry.getParentNames().size(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(HLTextUtils.toProperCase(entry.getParentNames().get(i).replace("_", " ")));
                    }
                    Component relationText = Component.literal(sb.toString())
                            .withStyle(s -> s.withColor(0x666666).withItalic(true));
                    tip.add(veilUnknown ? AbocipherText.veil(relationText) : relationText);
                }
            }
            var pose = gfx.pose();
            pose.pushPose();
            pose.translate(0, 0, MANIP_TOOLTIP_Z);
            gfx.renderTooltip(ctx.font(), tip, Optional.empty(), mouseX, mouseY);
            pose.popPose();
            break;
        }
    }

    private void drawManipInfoPanel(GuiGraphics gfx, ProgressScreenContext ctx, ManipulationTreeEntry entry) {
        BloodManipulation manip = entry.resolve();
        if (manip == null) return;

        boolean known = knownManipNames.contains(entry.getManipName());
        boolean rankLocked = isManipRankLocked(manip);
        boolean veilUnknown = shouldVeilUnknown(known, rankLocked);

        ParticleColor pc = manip.getTend().getColor();
        int tendR = (int)pc.getRed();
        int tendG = (int)pc.getGreen();
        int tendB = (int)pc.getBlue();
        int tendCol = 0xFF000000 | (tendR << 16) | (tendG << 8) | tendB;

        int panelW = 188;
        int panelYTarget = ctx.guiTop() + 30;
        int panelBottomLimit = ctx.guiTop() + ctx.guiHeight() - INFO_PANEL_MARGIN;
        int minPanelY = ctx.guiTop() + MANIP_PANEL_TOP_CLEARANCE;
        int availablePanelH = panelBottomLimit - minPanelY;
        int fitPanelH = Math.max(1, availablePanelH);
        int maxW = panelW - 16;
        int lineH = 11;

        String name = rankLocked ? "???" : HLTextUtils.toProperCase(manip.getName().replace("_", " "));
        List<String> nameLines = ScreenDrawUtils.wrapText(ctx.font(), name, maxW - 20);
        int nameRowH = Math.max(22, nameLines.size() * 10 + 4);
        List<String> descriptionLines = rankLocked ? List.of()
                : manipDescriptionLines(ctx, manip, maxW - 8);
        int descriptionH = descriptionHeight(descriptionLines);

        ItemStack memoryStack = manipMemoryItems.get(entry.getManipName());
        RecipeLookup.FoundRecipe foundRecipe = (!rankLocked && memoryStack != null && !memoryStack.isEmpty())
                ? RecipeLookup.find(memoryStack) : null;
        int recipeH = MiniRecipeRenderer.estimateHeight(foundRecipe);
        int recipeSection = recipeH > 0 ? recipeH + 4 : 0;

        int statsH = 0;
        if (!rankLocked) {
            statsH += lineH; // known/unknown
            statsH += 3;
            statsH += manipStatGridHeight();
        } else {
            statsH += lineH * 2;
        }
        int panelH = 6 + nameRowH + descriptionH + 1 + 5 + statsH + recipeSection + MANIP_PANEL_BOTTOM_PAD;
        float panelScale = panelH > fitPanelH ? Math.min(1.0F, fitPanelH / (float) panelH) : 1.0F;
        int scaledPanelW = Mth.ceil(panelW * panelScale);
        int scaledPanelH = Mth.ceil(panelH * panelScale);
        int panelX = ctx.guiLeft() + ctx.guiWidth() - scaledPanelW - 8;
        int maxPanelY = Math.max(minPanelY, panelBottomLimit - scaledPanelH);
        int panelY = Mth.clamp(panelYTarget, minPanelY, maxPanelY);
        int solidBg = 0xFF000000 | (0x1A0505);

        var pose = gfx.pose();
        pose.pushPose();
        pose.translate(panelX, panelY, INFO_PANEL_Z);
        pose.scale(panelScale, panelScale, 1.0F);
        gfx.fill(-2, -2, panelW + 2, panelH + 2, INFO_PANEL_SHADOW);
        gfx.fill(0, 0, panelW, panelH, solidBg);
        ScreenDrawUtils.drawSimpleBorder(gfx, 0, 0, panelW, panelH, tendCol);
		HarbingerChromeRenderer.drawFrame(gfx, 0, 0, panelW, panelH, tendCol,
				rankLocked ? HarbingerChromeRenderer.State.DISABLED : HarbingerChromeRenderer.State.ACTIVE);

        int tx = 6;
        int ty = 6;

        if (!rankLocked && memoryStack != null && !memoryStack.isEmpty()) gfx.renderItem(memoryStack, tx, ty);
        int nameCol = rankLocked ? 0xFF555555 : tendCol;
        for (int li = 0; li < nameLines.size(); li++) {
            int nx = li == 0 ? tx + 20 : tx + 4;
            Component nameText = Component.literal(nameLines.get(li))
                    .withStyle(s -> s.withColor(nameCol).withBold(true));
            gfx.drawString(ctx.font(),
                    veilUnknown ? AbocipherText.veil(nameText) : nameText,
                    nx, ty + 4 + li * 10, 0, false);
        }
        ty += nameRowH;
        if (!rankLocked) {
            ty += drawManipDescription(gfx, ctx, descriptionLines, tx + 4, ty, veilUnknown);
        }

        gfx.fill(tx, ty, panelW - 6, ty + 1, 0xFF442222);
        ty += 5;

        if (rankLocked) {
            EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(manipMinDegree(manip.getRank()));
            String degreeName = needed != null ? needed.getTitle() : ("Degree " + manipMinDegree(manip.getRank()));
            gfx.drawString(ctx.font(), "Locked", tx, ty, 0xFF555555, false);
            ty += lineH;
            gfx.drawString(ctx.font(), "Requires: " + degreeName, tx, ty, 0xFFAA4444, false);
            ty += lineH;
        } else {
            String statusStr = known ? "Known" : "Unknown";
            int statusCol = known ? 0xFF44AA44 : 0xFFAA4444;
            gfx.drawString(ctx.font(), statusStr, tx, ty, statusCol, false);
            ty += lineH + 3;
            drawManipStatGrid(gfx, ctx, manip, tx, ty, maxW, tendCol);
            ty += manipStatGridHeight();
        }

        if (foundRecipe != null) {
            ty += 1;
            gfx.fill(tx, ty, panelW - 6, ty + 1, 0xFF442222);
            ty += 2;
            MiniRecipeRenderer.draw(gfx, ctx.font(), foundRecipe, tx, ty, maxW, tendCol, MiniRecipeRenderer.BLOOD);
        }
        pose.popPose();
    }

    private List<String> manipDescriptionLines(ProgressScreenContext ctx, BloodManipulation manip, int maxW) {
        Component description = Component.translatable(manipDescriptionKey(manip));
        return ScreenDrawUtils.wrapText(ctx.font(), description.getString(), maxW);
    }

    private static int descriptionHeight(List<String> lines) {
        return lines.isEmpty() ? 0
                : MANIP_DESCRIPTION_TOP_PAD + lines.size() * MANIP_DESCRIPTION_LINE_H + MANIP_DESCRIPTION_BOTTOM_PAD;
    }

    private int drawManipDescription(GuiGraphics gfx, ProgressScreenContext ctx, List<String> lines,
                                     int x, int y, boolean veilUnknown) {
        for (int i = 0; i < lines.size(); i++) {
            Component line = Component.literal(lines.get(i)).withStyle(s -> s.withColor(0xFFB88A8A).withItalic(true));
            gfx.drawString(ctx.font(), veilUnknown ? AbocipherText.veil(line) : line,
                    x, y + MANIP_DESCRIPTION_TOP_PAD + i * MANIP_DESCRIPTION_LINE_H, 0, false);
        }
        return descriptionHeight(lines);
    }

    private static String manipDescriptionKey(BloodManipulation manip) {
        return "hemomancy.manipulation." + manip.getName() + ".desc";
    }

    private static int manipStatGridHeight() {
        return MANIP_STAT_ROWS * MANIP_STAT_CELL_H + (MANIP_STAT_ROWS - 1) * MANIP_STAT_ROW_GAP;
    }

    private void drawManipStatGrid(GuiGraphics gfx, ProgressScreenContext ctx, BloodManipulation manip,
                                   int x, int y, int width, int tendCol) {
        List<ManipStatCell> cells = manipStatCells(manip, tendCol);
        int cellW = (width - MANIP_STAT_COL_GAP * (MANIP_STAT_COLUMNS - 1)) / MANIP_STAT_COLUMNS;
        for (int i = 0; i < cells.size(); i++) {
            int col = i % MANIP_STAT_COLUMNS;
            int row = i / MANIP_STAT_COLUMNS;
            int cellX = x + col * (cellW + MANIP_STAT_COL_GAP);
            int cellY = y + row * (MANIP_STAT_CELL_H + MANIP_STAT_ROW_GAP);
            drawManipStatCell(gfx, ctx, cells.get(i), cellX, cellY, cellW);
        }
    }

    private List<ManipStatCell> manipStatCells(BloodManipulation manip, int tendCol) {
        String tendName = HLTextUtils.toProperCase(manip.getTend().name());
        double alignReq = manip.getAlignLevel();
        String tendText = alignReq > 0 ? tendName + " (" + (int)alignReq + ")" : tendName;
        tendText += secondaryTendencyText(manip);
        EnumBloodTendency secondaryTend = manip.getSecondaryTend();
        int secondaryColor = secondaryTend != null ? tendencyColor(secondaryTend) : tendCol;
        return List.of(
                new ManipStatCell("Type", HLTextUtils.toProperCase(manip.getType().name()), 0xFFCCCCCC, 0xFFCCCCCC, false),
                new ManipStatCell("Rank", HLTextUtils.toProperCase(manip.getRank().name()), 0xFFCCCCCC, 0xFFCCCCCC, false),
                new ManipStatCell("Tendency", tendText, tendCol, secondaryColor, secondaryTend != null),
                new ManipStatCell("Cost", (int)manip.getCost() + " mL", 0xFFAA4444, 0xFFAA4444, false),
                new ManipStatCell("Section", HLTextUtils.toProperCase(manip.getSection().name()), 0xFFAAAAAA, 0xFFAAAAAA, false),
                new ManipStatCell("Cooldown", cooldownText(manip.getCooldownTicks()), 0xFFAAAA88, 0xFFAAAA88, false)
        );
    }

    private static String secondaryTendencyText(BloodManipulation manip) {
        EnumBloodTendency secondaryTend = manip.getSecondaryTend();
        if (secondaryTend == null) {
            return "";
        }
        return " / " + HLTextUtils.toProperCase(secondaryTend.name());
    }

    private void drawManipStatCell(GuiGraphics gfx, ProgressScreenContext ctx, ManipStatCell cell,
                                   int x, int y, int width) {
        gfx.fill(x, y, x + width, y + MANIP_STAT_CELL_H, 0x66180505);
        ScreenDrawUtils.drawSimpleBorder(gfx, x, y, width, MANIP_STAT_CELL_H, 0xFF442222);
		HarbingerChromeRenderer.drawFrame(gfx, x, y, width, MANIP_STAT_CELL_H, 0xFF884444,
				HarbingerChromeRenderer.State.IDLE);
        gfx.drawString(ctx.font(), cell.label(), x + 4, y + 2, 0xFF888888, false);
        if (cell.mixedTendency()) {
            drawMixedTendencyValue(gfx, ctx, cell.value(), x + 4, y + 10, width - 8, cell.valueColor(), cell.secondaryValueColor());
            return;
        }
        String value = ScreenDrawUtils.truncateText(ctx.font(), cell.value(), width - 8);
        gfx.drawString(ctx.font(), value, x + 4, y + 10, cell.valueColor(), false);
    }

    private void drawMixedTendencyValue(GuiGraphics gfx, ProgressScreenContext ctx, String value,
                                        int x, int y, int width, int color, int secondaryColor) {
        String[] parts = value.split(" / ", 2);
        String primary = ScreenDrawUtils.truncateText(ctx.font(), parts[0], width);
        gfx.drawString(ctx.font(), primary, x, y, color, false);
        if (parts.length > 1) {
            String secondary = ScreenDrawUtils.truncateText(ctx.font(), parts[1], width);
            gfx.drawString(ctx.font(), secondary, x, y + 8, secondaryColor, false);
        }
    }

    private static int tendencyColor(EnumBloodTendency tendency) {
        ParticleColor pc = tendency.getColor();
        return 0xFF000000 | ((int) pc.getRed() << 16) | ((int) pc.getGreen() << 8) | (int) pc.getBlue();
    }

    private static String cooldownText(int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return "None";
        }
        return String.format(Locale.ROOT, "%.1fs", cooldownTicks / 20.0F);
    }

    private record ManipStatCell(String label, String value, int valueColor, int secondaryValueColor, boolean mixedTendency) {
    }

    private ManipulationTreeEntry manipNodeUnder(ProgressScreenContext ctx, double mx, double my) {
        int h = halfNode();
        for (var e : manipPositions.entrySet()) {
            if (!isVisibleFamily(e.getKey())) continue;
            int[] p = e.getValue();
            int nx = sx(ctx, p[0]), ny = sy(ctx, p[1]);
            if (NodeShapeRenderer.isInside(e.getKey().getNodeShape(), mx, my, nx, ny, h)) return e.getKey();
        }
        return null;
    }

    private boolean shouldVeilUnknown(boolean known, boolean rankLocked) {
        return !known && !rankLocked && !AbocipherText.canRead(Minecraft.getInstance().player);
    }

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (FamilyFilterControlView.bounds(ctx).contains(mx, my)) {
            familyFilter.cycle(btn == 1 ? -1 : 1);
            if (selectedEntry != null && !isVisibleFamily(selectedEntry)) selectedEntry = null;
            ScarTreeEntry selectedScar = scarState.selectedEntry();
            if (selectedScar != null && !familyFilter.includes(selectedScar.tendency())) scarState.closeDetails();
            return true;
        }
        if (btn != 0) return false;
        ScarTreeEntry scarHit = ScarsTabView.nodeUnder(ctx, scarState, panZoom,
                familyFilter.selected(), mx, my);
        if (scarHit != null) {
            selectedEntry = null;
            scarState.toggleSelection(scarHit.id().toString());
            return true;
        }
        ManipulationTreeEntry hit = manipNodeUnder(ctx, mx, my);
        if (hit != null) {
            scarState.closeDetails();
            selectedEntry = (selectedEntry == hit) ? null : hit;
            return true;
        }
        return false;
    }

    @Override public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) { return false; }
    @Override public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) { return false; }
    @Override public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) { return false; }

    @Override public PanZoomState getPanZoomState() { return panZoom; }
	@Override public void onClose() {
		traceCache.close();
		scarTraceCache.close();
	}
    @Override public boolean closeDetails() {
        boolean closed = scarState.closeDetails();
        if (selectedEntry != null) {
            selectedEntry = null;
            closed = true;
        }
        return closed;
    }
    public int getContentW() { return contentW; }
    public int getContentH() { return contentH; }

    private List<ManipulationTreeEntry> visibleManipulationEntries() {
        return ManipulationTreeInit.ENTRIES.stream().filter(this::isVisibleFamily).toList();
    }

    private boolean isVisibleFamily(ManipulationTreeEntry entry) {
        BloodManipulation manipulation = entry.resolve();
        return familyFilter.includes(manipulation != null ? manipulation.getTend() : null);
    }

    private String familyLabel() {
        EnumBloodTendency selected = familyFilter.selected();
        return FamilyFilterLabels.display(selected == null ? null
                : HLTextUtils.toProperCase(selected.name().replace('_', ' ')));
    }
}
