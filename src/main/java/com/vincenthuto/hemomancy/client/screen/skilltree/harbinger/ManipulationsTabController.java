package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import com.vincenthuto.hemomancy.common.capability.player.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ManipulationTreeInit;
import com.vincenthuto.hemomancy.common.manipulation.BloodManipulation;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import com.vincenthuto.hutoslib.client.screen.HLGuiUtils;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ManipulationsTabController implements IProgressTab {

    private static final int NODE_SIZE = 26;
    private static final int NODE_GAP_X = 80;
    private static final float TENDENCY_VALUE_DISTANCE_DIVISOR = 1.75f;
    private static final int TENDENCY_VALUE_VERTICAL_OFFSET = 0;
    private static final int COL_LINE_LOCKED = 0x88444444;
    private static final int COL_NODE_BG     = 0xCC1A0505;
    private static final int COL_NODE_BORDER_LOCK = 0xFF333333;
    private static final int ALPHA_OPAQUE_MASK = 0xFF000000;

    private final PanZoomState panZoom = new PanZoomState();
    private int manipRingCenterX, manipRingCenterY;
    private final Map<ManipulationTreeEntry, int[]> manipPositions = new HashMap<>();
    private final Set<String> knownManipNames = new HashSet<>();
    private final Map<String, ItemStack> manipMemoryItems = new HashMap<>();
    private int contentW, contentH;
    private ManipulationTreeEntry selectedEntry = null;
    private int playerDegree;

    @Override
    public void onInit(ProgressScreenContext ctx) {
        this.playerDegree = ctx.playerDegree();
        buildManipLayout();
        cacheKnownManipulations();
        panZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
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
            if (manip == null || manip.getTend() == null) {
                fallbackEntries.add(entry);
                continue;
            }
            byTendency.computeIfAbsent(manip.getTend(), k -> new ArrayList<>()).add(entry);
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

        int radius = Math.max(250, (int)Math.ceil(Math.max(maxClusterW, maxClusterH) * 1.35));
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

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (int[] pos : rawPositions.values()) {
            minX = Math.min(minX, pos[0]);
            minY = Math.min(minY, pos[1]);
            maxX = Math.max(maxX, pos[0]);
            maxY = Math.max(maxY, pos[1]);
        }
        int offsetX = padding - minX;
        int offsetY = padding - minY;
        for (var e : rawPositions.entrySet()) {
            int[] p = e.getValue();
            manipPositions.put(e.getKey(), new int[]{p[0] + offsetX, p[1] + offsetY});
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

    private void buildManipMemoryItemLookup() {
        if (!manipMemoryItems.isEmpty()) return;
        for (Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM) {
            if (item instanceof com.vincenthuto.hemomancy.common.item.memories.BloodMemoryItem memItem) {
                BloodManipulation manip = memItem.getManip();
                if (manip != null && manip.getName() != null) {
                    manipMemoryItems.put(manip.getName(), new ItemStack(item));
                }
            }
        }
    }

    private static int manipMinDegree(EnumManipulationRank rank) {
        return switch (rank) {
            case HUMILIS     -> 0;
            case MEDIOCRITAS -> 1;
            case SUMMA       -> 3;
            case MAGISTER    -> 5;
            case PERFECTUS   -> 6;
        };
    }

    private boolean isManipRankLocked(BloodManipulation manip) {
        if (manip == null) return false;
        return playerDegree < manipMinDegree(manip.getRank());
    }

    private int sx(ProgressScreenContext ctx, int cx) { return panZoom.sx(ctx.guiLeft(), cx); }
    private int sy(ProgressScreenContext ctx, int cy) { return panZoom.sy(ctx.guiTop(), cy); }
    private int halfNode() { return panZoom.halfNode(NODE_SIZE); }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
        drawManipConnections(gfx, ctx);
        drawManipTendencyStar(gfx, ctx);
        drawManipNodes(gfx, ctx);
    }

    @Override
    public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        if (selectedEntry != null) drawManipInfoPanel(gfx, ctx, selectedEntry);
    }

    @Override
    public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        drawManipTooltip(gfx, ctx, mouseX, mouseY);
    }

    private void drawManipConnections(GuiGraphics gfx, ProgressScreenContext ctx) {
        int hn = halfNode();
        int lw = Math.max(1, (int)(panZoom.zoom * 1.5f));

        for (ManipulationTreeEntry entry : ManipulationTreeInit.ENTRIES) {
            int[] childPos = manipPositions.get(entry);
            if (childPos == null) continue;
            boolean childKnown = knownManipNames.contains(entry.getManipName());
            boolean childLocked = isManipRankLocked(entry.resolve());

            for (String parentName : entry.getParentNames()) {
                ManipulationTreeEntry parentEntry = ManipulationTreeInit.getEntry(parentName);
                if (parentEntry == null) continue;
                int[] parentPos = manipPositions.get(parentEntry);
                if (parentPos == null) continue;

                boolean parentKnown = knownManipNames.contains(parentName);
                boolean parentLocked = isManipRankLocked(parentEntry.resolve());

                int col;
                if (childLocked || parentLocked) {
                    col = 0x44222222;
                } else if (parentKnown && childKnown) {
                    BloodManipulation manip = entry.resolve();
                    if (manip != null) {
                        ParticleColor pc = manip.getTend().getColor();
                        int r = (int)Math.min(pc.getRed() * 0.7f, 255);
                        int g = (int)Math.min(pc.getGreen() * 0.7f, 255);
                        int b = (int)Math.min(pc.getBlue() * 0.7f, 255);
                        col = 0xCC000000 | (r << 16) | (g << 8) | b;
                    } else {
                        col = 0xFFAA6600;
                    }
                } else {
                    col = COL_LINE_LOCKED;
                }

                int x1 = sx(ctx, parentPos[0]), y1 = sy(ctx, parentPos[1]);
                int x2 = sx(ctx, childPos[0]),  y2 = sy(ctx, childPos[1]);
                int midY = (y1 + y2) / 2;
                gfx.fill(x1 - lw, y1 + hn, x1 + lw, midY, col);
                gfx.fill(Math.min(x1, x2) - lw, midY - lw, Math.max(x1, x2) + lw, midY + lw, col);
                gfx.fill(x2 - lw, midY, x2 + lw, y2 - hn, col);
            }
        }
    }

    private void drawManipTendencyStar(GuiGraphics gfx, ProgressScreenContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int screenX = sx(ctx, manipRingCenterX);
        int screenY = sy(ctx, manipRingCenterY);

        int starRadius = (int)(95 * panZoom.zoom);
        if (screenX + starRadius < ctx.guiLeft() || screenX - starRadius > ctx.guiLeft() + ctx.guiWidth()
                || screenY + starRadius < ctx.guiTop() || screenY - starRadius > ctx.guiTop() + ctx.guiHeight()) return;

        HemoCapabilityAccess.getBloodTendency(mc.player).ifPresent(tendency -> {
            Map<EnumBloodTendency, Float> affs = tendency.getTendency();
            float rotAngle = -90f;
            int outerRadius = (int)(210 * panZoom.zoom);
            int innerRadius = (int)(54 * panZoom.zoom);
            float spikeBaseWidth = 23.5f;
            double valueDist = outerRadius / TENDENCY_VALUE_DISTANCE_DIVISOR;

            for (EnumBloodTendency tend : EnumBloodTendency.values()) {
                float affVal = Mth.clamp(affs.getOrDefault(tend, 0f), 0f, 1f);
                int cx1 = screenX + (int)(Math.cos(Math.toRadians(rotAngle + spikeBaseWidth)) * innerRadius);
                int cy1 = screenY + (int)(Math.sin(Math.toRadians(rotAngle + spikeBaseWidth)) * innerRadius);
                int cx2 = screenX + (int)(Math.cos(Math.toRadians(rotAngle - spikeBaseWidth)) * innerRadius);
                int cy2 = screenY + (int)(Math.sin(Math.toRadians(rotAngle - spikeBaseWidth)) * innerRadius);
                double tipDist = (outerRadius - innerRadius) * affVal * 0.5 + innerRadius;
                int lx = screenX + (int)(Math.cos(Math.toRadians(rotAngle)) * tipDist);
                int ly = screenY + (int)(Math.sin(Math.toRadians(rotAngle)) * tipDist);
                int displace = (int)((Math.max(cx1, cx2) - Math.min(cx1, cx2)
                        + Math.max(cy1, cy2) - Math.min(cy1, cy2)) / 2f);
                HLGuiUtils.fracLine(gfx.pose(), lx, ly, cx1, cy1, 10, tend.getColor(), displace, 1.1);
                HLGuiUtils.fracLine(gfx.pose(), lx, ly, cx2, cy2, 10, tend.getColor(), displace, 1.1);
                HLGuiUtils.fracLine(gfx.pose(), cx1, cy1, lx, ly, 10, tend.getColor(), displace, 0.8);
                HLGuiUtils.fracLine(gfx.pose(), cx2, cy2, lx, ly, 10, tend.getColor(), displace, 0.8);
                int valueX = screenX + (int)(Math.cos(Math.toRadians(rotAngle)) * valueDist);
                int valueY = screenY + (int)(Math.sin(Math.toRadians(rotAngle)) * valueDist);
                int tendColor = ALPHA_OPAQUE_MASK | tend.getColor().getColor();
                gfx.drawCenteredString(ctx.font(), String.valueOf(tendency.getAlignmentByTendency(tend)),
                        valueX, valueY - TENDENCY_VALUE_VERTICAL_OFFSET, tendColor);
                rotAngle += 45f;
            }
        });
    }
		private float animTime = 0f;

    private void drawManipNodes(GuiGraphics gfx, ProgressScreenContext ctx) {
        animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
        int hn = halfNode();

        for (var e : manipPositions.entrySet()) {
            ManipulationTreeEntry entry = e.getKey();
            int[] pos = e.getValue();
            int nx = sx(ctx, pos[0]);
            int ny = sy(ctx, pos[1]);

            BloodManipulation manip = entry.resolve();
            boolean known = knownManipNames.contains(entry.getManipName());
            boolean rankLocked = isManipRankLocked(manip);
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
                    if (manip != null) {
                        sym = switch (manip.getType()) {
                            case QUICK      -> "\u26A1";
                            case CONTINUOUS -> "\u221E";
                            case PASSIVE    -> "\u25C6";
                            case CHARGED    -> "\u25B2";
                        };
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
                        gfx.drawCenteredString(ctx.font(), line, nx, ly, labelCol);
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
            int[] pos = e.getValue();
            int nx = sx(ctx, pos[0]), ny = sy(ctx, pos[1]);
            if (!NodeShapeRenderer.isInside(entry.getNodeShape(), mouseX, mouseY, nx, ny, hn)) continue;

            BloodManipulation manip = entry.resolve();
            boolean known = knownManipNames.contains(entry.getManipName());
            boolean rankLocked = isManipRankLocked(manip);

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
                tip.add(Component.literal(pretty).withStyle(s -> s.withColor(nameCol).withBold(true)));
                tip.add(Component.literal(known ? "Known" : "Unknown")
                        .withStyle(s -> s.withColor(known ? 0x44AA44 : 0xAA4444).withItalic(!known)));

                if (manip != null) {
                    tip.add(Component.literal("Type: " + HLTextUtils.toProperCase(manip.getType().name()))
                            .withStyle(s -> s.withColor(0x888888)));
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
                    tip.add(Component.literal("Blood Cost: " + (int)manip.getCost() + " mL")
                            .withStyle(s -> s.withColor(0xAA4444)));
                    tip.add(Component.literal("Vein Section: " + HLTextUtils.toProperCase(manip.getSection().name()))
                            .withStyle(s -> s.withColor(0x666666)));
                }

                if (!entry.getParentNames().isEmpty()) {
                    StringBuilder sb = new StringBuilder("Relates to: ");
                    for (int i = 0; i < entry.getParentNames().size(); i++) {
                        if (i > 0) sb.append(", ");
                        sb.append(HLTextUtils.toProperCase(entry.getParentNames().get(i).replace("_", " ")));
                    }
                    tip.add(Component.literal(sb.toString()).withStyle(s -> s.withColor(0x666666).withItalic(true)));
                }
            }
            gfx.renderTooltip(ctx.font(), tip, Optional.empty(), mouseX, mouseY);
            break;
        }
    }

    private void drawManipInfoPanel(GuiGraphics gfx, ProgressScreenContext ctx, ManipulationTreeEntry entry) {
        BloodManipulation manip = entry.resolve();
        if (manip == null) return;

        boolean known = knownManipNames.contains(entry.getManipName());
        boolean rankLocked = isManipRankLocked(manip);

        ParticleColor pc = manip.getTend().getColor();
        int tendR = (int)pc.getRed();
        int tendG = (int)pc.getGreen();
        int tendB = (int)pc.getBlue();
        int tendCol = 0xFF000000 | (tendR << 16) | (tendG << 8) | tendB;

        int panelW = 170;
        int panelX = ctx.guiLeft() + ctx.guiWidth() - panelW - 8;
        int panelY = ctx.guiTop() + 30;
        int maxW = panelW - 16;
        int lineH = 11;

        String name = rankLocked ? "???" : HLTextUtils.toProperCase(manip.getName().replace("_", " "));
        List<String> nameLines = ScreenDrawUtils.wrapText(ctx.font(), name, maxW - 20);
        int nameRowH = Math.max(22, nameLines.size() * 10 + 4);

        ItemStack memoryStack = manipMemoryItems.get(entry.getManipName());
        RecipeLookup.FoundRecipe foundRecipe = (memoryStack != null && !memoryStack.isEmpty())
                ? RecipeLookup.find(memoryStack) : null;
        int recipeH = MiniRecipeRenderer.estimateHeight(foundRecipe);
        int recipeSection = recipeH > 0 ? recipeH + 12 : 0;

        int statsH = 0;
        if (!rankLocked) {
            statsH += lineH; // known/unknown
            statsH += lineH; // type
            statsH += lineH; // rank
            statsH += lineH; // tendency
            statsH += lineH; // blood cost
            statsH += lineH; // section
            if (manip.getCooldownTicks() > 0) statsH += lineH;
        } else {
            statsH += lineH * 2;
        }
        int panelH = 6 + nameRowH + 1 + 5 + statsH + recipeSection + 8;

        gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xDD1A0505);
        ScreenDrawUtils.drawSimpleBorder(gfx, panelX, panelY, panelW, panelH, tendCol);

        int tx = panelX + 6;
        int ty = panelY + 6;

        if (memoryStack != null && !memoryStack.isEmpty()) gfx.renderItem(memoryStack, tx, ty);
        int nameCol = rankLocked ? 0xFF555555 : tendCol;
        for (int li = 0; li < nameLines.size(); li++) {
            int nx = li == 0 ? tx + 20 : tx + 4;
            gfx.drawString(ctx.font(),
                    Component.literal(nameLines.get(li)).withStyle(s -> s.withColor(nameCol).withBold(true)),
                    nx, ty + 4 + li * 10, 0, false);
        }
        ty += nameRowH;

        gfx.fill(tx, ty, panelX + panelW - 6, ty + 1, 0xFF442222);
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
            ty += lineH;

            gfx.drawString(ctx.font(), Component.literal("Type: ").withStyle(s -> s.withColor(0xFF888888))
                    .append(Component.literal(HLTextUtils.toProperCase(manip.getType().name())).withStyle(s -> s.withColor(0xFFCCCCCC))),
                    tx, ty, 0, false);
            ty += lineH;

            gfx.drawString(ctx.font(), Component.literal("Rank: ").withStyle(s -> s.withColor(0xFF888888))
                    .append(Component.literal(HLTextUtils.toProperCase(manip.getRank().name())).withStyle(s -> s.withColor(0xFFCCCCCC))),
                    tx, ty, 0, false);
            ty += lineH;

            String tendName = HLTextUtils.toProperCase(manip.getTend().name());
            double alignReq = manip.getAlignLevel();
            String tendText = alignReq > 0 ? tendName + " (" + (int)alignReq + ")" : tendName;
            gfx.drawString(ctx.font(), Component.literal("Tendency: ").withStyle(s -> s.withColor(0xFF888888))
                    .append(Component.literal(tendText).withStyle(s -> s.withColor(tendCol))),
                    tx, ty, 0, false);
            ty += lineH;

            gfx.drawString(ctx.font(), Component.literal("Blood Cost: ").withStyle(s -> s.withColor(0xFF888888))
                    .append(Component.literal((int)manip.getCost() + " mL").withStyle(s -> s.withColor(0xFFAA4444))),
                    tx, ty, 0, false);
            ty += lineH;

            gfx.drawString(ctx.font(), Component.literal("Section: ").withStyle(s -> s.withColor(0xFF888888))
                    .append(Component.literal(HLTextUtils.toProperCase(manip.getSection().name())).withStyle(s -> s.withColor(0xFFAAAAAA))),
                    tx, ty, 0, false);
            ty += lineH;

            if (manip.getCooldownTicks() > 0) {
                float seconds = manip.getCooldownTicks() / 20f;
                gfx.drawString(ctx.font(), Component.literal("Cooldown: ").withStyle(s -> s.withColor(0xFF888888))
                        .append(Component.literal(String.format("%.1fs", seconds)).withStyle(s -> s.withColor(0xFFAAAA88))),
                        tx, ty, 0, false);
                ty += lineH;
            }
        }

        if (foundRecipe != null) {
            ty += 3;
            gfx.fill(tx, ty, panelX + panelW - 6, ty + 1, 0xFF442222);
            ty += 4;
            MiniRecipeRenderer.draw(gfx, ctx.font(), foundRecipe, tx, ty, maxW, tendCol, MiniRecipeRenderer.BLOOD);
        }
    }

    private ManipulationTreeEntry manipNodeUnder(ProgressScreenContext ctx, double mx, double my) {
        int h = halfNode();
        for (var e : manipPositions.entrySet()) {
            int[] p = e.getValue();
            int nx = sx(ctx, p[0]), ny = sy(ctx, p[1]);
            if (NodeShapeRenderer.isInside(e.getKey().getNodeShape(), mx, my, nx, ny, h)) return e.getKey();
        }
        return null;
    }

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn != 0) return false;
        ManipulationTreeEntry hit = manipNodeUnder(ctx, mx, my);
        if (hit != null) {
            selectedEntry = (selectedEntry == hit) ? null : hit;
            return true;
        }
        return false;
    }

    @Override public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) { return false; }
    @Override public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) { return false; }
    @Override public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) { return false; }

    @Override public PanZoomState getPanZoomState() { return panZoom; }
    public int getContentW() { return contentW; }
    public int getContentH() { return contentH; }
}
