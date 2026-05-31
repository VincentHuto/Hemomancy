package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.*;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.EnumSkillStates;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillPoint;
import com.vincenthuto.hemomancy.common.capability.player.shared.skill.SkillProgressClientCache;
import com.vincenthuto.hemomancy.common.init.SkillPointInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.PacketUnlockSkill;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class SkillsTabController implements IProgressTab {

    private static final int NODE_SIZE = 26;
    private static final int NODE_GAP_X = 80;
    private static final int NODE_GAP_Y = 60;
    private static final int COMPASS_CENTER_X = 480;
    private static final int COMPASS_CENTER_Y = 480;
    private static final int[] DEGREE_RING_RADII = {72, 120, 170, 220, 270, 320, 370, 420, 470};
    private static final int COMPASS_CONTENT_PADDING = 80;
    private static final int DEGREE_LABEL_X = COMPASS_CENTER_X - DEGREE_RING_RADII[DEGREE_RING_RADII.length - 1] + 10;
    private static final int DEGREE_LABEL_TOP_Y = COMPASS_CENTER_Y - DEGREE_RING_RADII[DEGREE_RING_RADII.length - 1] + 28;
    private static final int DEGREE_LABEL_GAP_Y = 34;
    private static final int TRACE_DEGREE_LABEL = 0x88D66458;
    private static final int COL_NODE_BG           = 0xCC1A0505;

    private final SkillTraceLayerCache surfaceTraceCache = new SkillTraceLayerCache();
    private final SkillTraceLayerCache deepTraceCache = new SkillTraceLayerCache();
    private final PanZoomState surfacePanZoom = new PanZoomState();
    private final PanZoomState deepPanZoom = new PanZoomState();
    private final SkillTreeDiveState diveState = new SkillTreeDiveState();
    private final Map<SkillPoint, int[]> surfaceNodePositions = new HashMap<>();
    private final Map<SkillPoint, int[]> deepNodePositions = new HashMap<>();
    private int contentW, contentH;
    private int playerDegree;
    private float animTime = 0f;
    private float deepFade = 0f;
    private float lockedPortalFade = 0f;

    @Override
    public void onInit(ProgressScreenContext ctx) {
        this.playerDegree = ctx.playerDegree();
        buildLayout();
        surfacePanZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
        deepPanZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
    }

    private void buildLayout() {
        surfaceNodePositions.clear();
        deepNodePositions.clear();
        contentW = 0;
        contentH = 0;
        buildAutomaticLayout();
        for (List<SkillPoint> branch : SkillPointInit.SKILL_TREE) {
            for (SkillPoint sp : branch) {
                if (sp.hasTreePosition()) {
                    positionsForLayer(sp).put(sp, new int[]{sp.getTreeX(), sp.getTreeY()});
                }
            }
        }
        recalculateContentBounds();
    }

    private void buildAutomaticLayout() {
        for (List<SkillPoint> branch : SkillPointInit.SKILL_TREE) {
            if (branch.isEmpty()) continue;
            Map<Integer, List<SkillPoint>> byDepth = new HashMap<>();
            for (SkillPoint sp : branch) {
                byDepth.computeIfAbsent(depth(sp), k -> new ArrayList<>()).add(sp);
            }
            int maxDepth = byDepth.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
            int widestRow = 0;
            for (int d = 0; d <= maxDepth; d++) {
                int n = byDepth.getOrDefault(d, List.of()).size();
                widestRow = Math.max(widestRow, (n - 1) * NODE_GAP_X + NODE_SIZE);
            }
            for (int d = 0; d <= maxDepth; d++) {
                List<SkillPoint> row = byDepth.getOrDefault(d, List.of());
                int n = row.size();
                int rowWidth = (n - 1) * NODE_GAP_X;
                int x0 = (widestRow - rowWidth) / 2;
                int y = 40 + (maxDepth - d) * NODE_GAP_Y;
                for (int i = 0; i < n; i++) {
                    int x = x0 + i * NODE_GAP_X;
                    SkillPoint sp = row.get(i);
                    positionsForLayer(sp).put(sp, new int[]{x, y});
                }
            }
        }
    }

    private void recalculateContentBounds() {
        contentW = 0;
        contentH = 0;
        for (Map<SkillPoint, int[]> layerPositions : List.of(surfaceNodePositions, deepNodePositions)) {
            for (int[] pos : layerPositions.values()) {
                contentW = Math.max(contentW, pos[0] + NODE_SIZE + 48);
                contentH = Math.max(contentH, pos[1] + NODE_SIZE + 48);
            }
        }
        for (int[] pos : List.of(new int[] {COMPASS_CENTER_X, COMPASS_CENTER_Y})) {
            contentW = Math.max(contentW, pos[0] + NODE_SIZE + 48);
            contentH = Math.max(contentH, pos[1] + NODE_SIZE + 48);
        }
        for (int degree = 0; degree < DEGREE_RING_RADII.length; degree++) {
            int[] labelPosition = SkillPointInit.getDegreeLabelPosition(degree);
            contentW = Math.max(contentW, labelPosition[0] + 128);
            contentH = Math.max(contentH, labelPosition[1] + 48);
        }
        contentW = Math.max(contentW, COMPASS_CENTER_X + DEGREE_RING_RADII[DEGREE_RING_RADII.length - 1] + COMPASS_CONTENT_PADDING);
        contentH = Math.max(contentH, COMPASS_CENTER_Y + DEGREE_RING_RADII[DEGREE_RING_RADII.length - 1] + COMPASS_CONTENT_PADDING);
    }

    private Map<SkillPoint, int[]> positionsForLayer(SkillPoint sp) {
        return SkillTreeLayerRules.layerForDegree(sp.getRequiredDegree()) == SkillTreeLayer.DEEP ? deepNodePositions : surfaceNodePositions;
    }

    private static int depth(SkillPoint sp) {
        return depth(sp, new HashSet<>());
    }

    private static int depth(SkillPoint sp, Set<SkillPoint> seen) {
        if (sp == null || !seen.add(sp) || sp.getParents().isEmpty()) return 0;
        int max = 0;
        for (SkillPoint parent : sp.getParents()) {
            max = Math.max(max, 1 + depth(parent, new HashSet<>(seen)));
        }
        return max;
    }

    private int sx(ProgressScreenContext ctx, PanZoomState view, int cx) { return view.sx(ctx.guiLeft(), cx); }
    private int sy(ProgressScreenContext ctx, PanZoomState view, int cy) { return view.sy(ctx.guiTop(), cy); }
    private int halfNode(PanZoomState view) { return view.halfNode(NODE_SIZE); }

    private PanZoomState activePanZoom() {
        return diveState.isDeepActive() ? deepPanZoom : surfacePanZoom;
    }

    private PanZoomState deepRenderPanZoom() {
        return diveState.isDeepActive() ? deepPanZoom : surfacePanZoom;
    }

    private PanZoomState transitionPulseView() {
        return diveState.transitionPulseEnteringDeep() ? surfacePanZoom : deepPanZoom;
    }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
        animTime += 0.016f;
        updateDiveProgress(ctx);
        float surfaceAlpha = 1.0f - deepFade;
        float deepAlpha = deepFade;
        PanZoomState deepView = deepRenderPanZoom();
        surfaceTraceCache.rebuildIfNeeded(surfaceNodePositions, contentW, contentH, false, SkillTreeLayer.SURFACE);
        deepTraceCache.rebuildIfNeeded(deepNodePositions, contentW, contentH, true, SkillTreeLayer.DEEP);

        if (surfaceAlpha > 0.01f) surfaceTraceCache.render(gfx, ctx, surfacePanZoom, surfaceAlpha);
        if (deepAlpha > 0.01f) deepTraceCache.render(gfx, ctx, deepView, deepAlpha);
        surfaceTraceCache.renderHeartbeat(gfx, ctx, surfacePanZoom, animTime, surfaceAlpha);
        if (deepAlpha > 0.01f) deepTraceCache.renderHeartbeat(gfx, ctx, deepView, animTime, deepAlpha);
        if (lockedPortalFade > 0.01f) surfaceTraceCache.renderPortalPulse(gfx, ctx, surfacePanZoom, lockedPortalFade, true);
        if (!diveState.isDeepActive() && deepFade > 0.01f) deepTraceCache.renderPortalPulse(gfx, ctx, deepView, deepFade, false);
        float transitionPulse = diveState.transitionPulseProgress();
        if (transitionPulse > 0.01f) {
            surfaceTraceCache.renderScreenTransitionPulse(gfx, ctx, transitionPulseView(), transitionPulse, diveState.transitionPulseEnteringDeep());
            diveState.tickTransitionPulse();
        }
        drawDegreeLabels(gfx, ctx, surfacePanZoom, SkillTreeLayer.SURFACE, surfaceAlpha);
        drawDegreeLabels(gfx, ctx, deepView, SkillTreeLayer.DEEP, deepAlpha);
        surfaceTraceCache.renderFlow(gfx, ctx, surfacePanZoom, animTime, surfaceAlpha);
        if (deepAlpha > 0.01f) deepTraceCache.renderFlow(gfx, ctx, deepView, animTime, deepAlpha);
        drawNodes(gfx, ctx, surfacePanZoom, surfaceNodePositions, surfaceAlpha);
        drawNodes(gfx, ctx, deepView, deepNodePositions, deepAlpha);
    }

    @Override
    public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {}

    @Override
    public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        drawTooltip(gfx, ctx, mouseX, mouseY);
    }

    public float getDeepFade() {
        return deepFade;
    }

    public float updateAndGetDeepFade(ProgressScreenContext ctx) {
        updateDiveProgress(ctx);
        return deepFade;
    }

    private void updateDiveProgress(ProgressScreenContext ctx) {
        if (diveState.isDeepActive()) {
            deepFade = 1.0f;
            lockedPortalFade = 0.0f;
            return;
        }
        double centerX = surfacePanZoom.panX + COMPASS_CENTER_X * surfacePanZoom.zoom;
        double centerY = surfacePanZoom.panY + COMPASS_CENTER_Y * surfacePanZoom.zoom;
        float centerFocus = SkillTreeLayerRules.centerFocus(centerX, centerY, ctx.guiWidth(), ctx.guiHeight());
        float surfaceDive = SkillTreeLayerRules.diveProgress(playerDegree, surfacePanZoom.zoom, centerFocus);
        if (diveState.updateSurfaceDive(playerDegree, surfaceDive)) {
            deepPanZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
            deepFade = 1.0f;
            lockedPortalFade = 0.0f;
            return;
        }
        deepFade = diveState.deepFade(surfaceDive);
        lockedPortalFade = SkillTreeLayerRules.lockedPortalProgress(playerDegree, surfacePanZoom.zoom, centerFocus);
    }

    private void drawDegreeLabels(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState view, SkillTreeLayer layer, float alpha) {
        if (alpha <= 0.01f) return;
        if (view.zoom < 0.5f) return;
        for (int degree = 0; degree < DEGREE_RING_RADII.length; degree++) {
            if (SkillTreeLayerRules.layerForDegree(degree) != layer) continue;
            int[] labelPosition = SkillPointInit.getDegreeLabelPosition(degree);
            gfx.drawString(ctx.font(), "Degree " + degree, sx(ctx, view, labelPosition[0]), sy(ctx, view, labelPosition[1]),
                    withAlpha(TRACE_DEGREE_LABEL, Math.round(((TRACE_DEGREE_LABEL >>> 24) & 0xFF) * alpha)), false);
        }
    }

    private void drawNodes(GuiGraphics gfx, ProgressScreenContext ctx, PanZoomState view, Map<SkillPoint, int[]> positions, float alpha) {
        if (alpha <= 0.01f) return;
        float time = animTime;
        int hn = halfNode(view);
        for (var e : positions.entrySet()) {
            SkillPoint sp = e.getKey();
            int[] pos = e.getValue();
            int nx = sx(ctx, view, pos[0]);
            int ny = sy(ctx, view, pos[1]);
            boolean degreeLocked = sp.isDegreeLocked(playerDegree);
            EnumNodeShape shape = sp.getNodeShape();
            int branchColor = sp.getBranchColor();
            int border;
            if (degreeLocked) {
                border = withAlpha(branchColor, Math.round(0xCC * alpha));
            } else {
                switch (SkillProgressClientCache.current().getState(sp)) {
                    case UNLOCKED -> {
                        border = fadeColor(branchColor, alpha);
                        float p = 0.7f + 0.3f * Mth.sin(time * 2f + sp.getId());
                        int ga = (int)(40 * p * alpha);
                        NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn + 3, withAlpha(branchColor, ga));
                    }
                    case LOCKED -> {
                        border = withAlpha(branchColor, Math.round(0xAA * alpha));
                        if (!sp.getParents().isEmpty() && areParentsUnlocked(sp))
                            border = withAlpha(branchColor, Math.round(0xEE * alpha));
                    }
                    default -> border = withAlpha(branchColor, Math.round(0xAA * alpha));
                }
            }
            NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn, fadeColor(COL_NODE_BG, alpha));
            NodeShapeRenderer.drawOutline(gfx, shape, nx, ny, hn, border);
            if (degreeLocked) {
                NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn - 1, fadeColor(0xBB000000, alpha));
                if (view.zoom >= 0.5f) gfx.drawCenteredString(ctx.font(), "?", nx, ny - 4, fadeColor(0xFF111111, alpha));
                continue;
            }
            if (view.zoom >= 0.5f) {
                ResourceLocation iconTex = sp.getIconTexture();
                if (iconTex != null) {
                    ScreenDrawUtils.renderScaledTexture(gfx, iconTex, nx, ny, hn);
                } else {
                    ItemStack iconStack = sp.getIconItem();
                    if (iconStack != null && !iconStack.isEmpty()) {
                        ScreenDrawUtils.renderScaledItem(gfx, iconStack, nx, ny, hn);
                    } else {
                        String ini = getSkillInitial(sp);
                        int textCol = SkillProgressClientCache.current().getState(sp) == EnumSkillStates.UNLOCKED ? 0xFFFFAAAA : 0xFF888888;
                        gfx.drawCenteredString(ctx.font(), ini, nx, ny - 4, fadeColor(textCol, alpha));
                    }
                }
                if (sp.getMaxLevels() > 0) {
                    int level = SkillProgressClientCache.current().getLevel(sp);
                    String lvlStr = level + "/" + sp.getMaxLevels();
                    int lvlCol = SkillProgressClientCache.current().isMaxed(sp) ? 0xFF44AA44 : 0xFF888888;
                    gfx.drawCenteredString(ctx.font(), lvlStr, nx, ny + hn + 3, fadeColor(lvlCol, alpha));
                }
            }
        }
    }

    private void drawTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        boolean insideGui = mouseX >= ctx.guiLeft() && mouseX < ctx.guiLeft() + ctx.guiWidth()
                && mouseY >= ctx.guiTop() && mouseY < ctx.guiTop() + ctx.guiHeight();
        if (!insideGui) return;
        PanZoomState view = activePanZoom();
        int hn = halfNode(view);
        for (var e : interactiveNodePositions().entrySet()) {
            SkillPoint sp = e.getKey();
            int[] pos = e.getValue();
            int nx = sx(ctx, view, pos[0]), ny = sy(ctx, view, pos[1]);
            if (!NodeShapeRenderer.isInside(sp.getNodeShape(), mouseX, mouseY, nx, ny, hn)) continue;
            List<Component> tip = new ArrayList<>();
            boolean degreeLocked = sp.isDegreeLocked(playerDegree);
            if (degreeLocked) {
                tip.add(Component.literal("???").withStyle(s -> s.withColor(0x555555).withBold(true)));
                EnumInitiatoryDegree needed = EnumInitiatoryDegree.byNumber(sp.getRequiredDegree());
                String degreeName = needed != null ? needed.getTitle() : ("Degree " + sp.getRequiredDegree());
                tip.add(Component.literal("Requires: " + degreeName).withStyle(s -> s.withColor(0xAA4444)));
            } else {
                String pretty = HLTextUtils.toProperCase(sp.getName().replace("skill_", "").replace("_", " "));
                tip.add(Component.literal(pretty).withStyle(s -> s.withColor(0xCC3333).withBold(true)));
                if (sp.getMaxLevels() > 0) {
                    tip.add(Component.literal("Level: " + SkillProgressClientCache.current().getLevel(sp) + " / " + sp.getMaxLevels())
                            .withStyle(s -> s.withColor(SkillProgressClientCache.current().isMaxed(sp) ? 0x44AA44 : 0x888888)));
                }
                tip.add(Component.translatable("skill.hemomancy." + sp.getName() + ".desc")
                        .withStyle(s -> s.withColor(0x999999).withItalic(true)));
                EnumSkillStates state = SkillProgressClientCache.current().getState(sp);
                if (state == EnumSkillStates.LOCKED) {
                    List<String> lockedParents = lockedParentNames(sp);
                    if (!lockedParents.isEmpty()) {
                        tip.add(Component.literal("Requires: " + String.join(", ", lockedParents)).withStyle(s -> s.withColor(0xAA4444)));
                    } else {
                        tip.add(Component.literal("Click to unlock! Cost: " + (int)SkillProgressClientCache.current().getLevelUpCost(sp) + " mL + "
                                + sp.getSkillPointCost() + " SP").withStyle(s -> s.withColor(0xBB8833)));
                    }
                } else if (state == EnumSkillStates.UNLOCKED) {
                    if (SkillProgressClientCache.current().isMaxed(sp)) {
                        tip.add(Component.literal("MAX LEVEL").withStyle(s -> s.withColor(0x44AA44).withBold(true)));
                    } else {
                        tip.add(Component.literal("Click to level up! Cost: " + (int)SkillProgressClientCache.current().getLevelUpCost(sp) + " mL + "
                                + sp.getSkillPointCost() + " SP").withStyle(s -> s.withColor(0xBB8833)));
                    }
                }
            }
            gfx.renderTooltip(ctx.font(), tip, Optional.empty(), mouseX, mouseY);
            break;
        }
    }

    private static boolean areParentsUnlocked(SkillPoint sp) {
        for (SkillPoint parent : sp.getParents()) {
            if (SkillProgressClientCache.current().getState(parent) != EnumSkillStates.UNLOCKED) return false;
        }
        return true;
    }

    private static List<String> lockedParentNames(SkillPoint sp) {
        List<String> locked = new ArrayList<>();
        for (SkillPoint parent : sp.getParents()) {
            if (SkillProgressClientCache.current().getState(parent) == EnumSkillStates.UNLOCKED) continue;
            locked.add(HLTextUtils.toProperCase(parent.getName().replace("skill_", "").replace("_", " ")));
        }
        return locked;
    }

    private static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (Mth.clamp(alpha, 0, 255) << 24);
    }

    private static int fadeColor(int color, float alpha) {
        return withAlpha(color, Math.round(((color >>> 24) & 0xFF) * Mth.clamp(alpha, 0.0f, 1.0f)));
    }

    private SkillPoint nodeUnder(ProgressScreenContext ctx, double mx, double my) {
        PanZoomState view = activePanZoom();
        int h = halfNode(view);
        for (var e : interactiveNodePositions().entrySet()) {
            int[] p = e.getValue();
            int nx = sx(ctx, view, p[0]), ny = sy(ctx, view, p[1]);
            if (NodeShapeRenderer.isInside(e.getKey().getNodeShape(), mx, my, nx, ny, h)) return e.getKey();
        }
        return null;
    }

    private Map<SkillPoint, int[]> interactiveNodePositions() {
        return diveState.isDeepActive() || SkillTreeLayerRules.isDeepUnlocked(playerDegree) && deepFade >= 0.5f ? deepNodePositions : surfaceNodePositions;
    }

    private void tryUnlock(SkillPoint sp) {
        if (sp.isDegreeLocked(playerDegree)) return;
        PacketHandler.sendToServer(new PacketUnlockSkill(sp.getId()));
    }

    private static String getSkillInitial(SkillPoint sp) {
        return switch (sp.getName()) {
            case "base"                  -> "\u2726";
            case "deep_base"             -> "\u2726";
            case "skill_capacity"        -> "C";
            case "skill_efficiency"      -> "E";
            case "skill_last_wind"       -> "W";
            case "skill_dynamic_use"     -> "D";
            case "skill_feeding_frenzy"  -> "F";
            case "skill_hemostasis"      -> "H";
            case "skill_sanguine_surge"  -> "S";
            case "skill_crimson_mastery" -> "M";
            case "skill_vital_link"      -> "V";
            case "skill_iron_will"       -> "I";
            case "skill_blood_flow"      -> "B";
            case "skill_coagulation"     -> "G";
            case "skill_sanguine_reach"  -> "R";
            case "skill_scar_affinity"   -> "\u2721";
            case "skill_scar_resonance"  -> "\u2721";
            case "skill_scar_mastery"    -> "\u2721";
            case "skill_living_conduit"  -> "L";
            case "skill_vascular_draw"   -> "V";
            case "skill_crimson_projection" -> "P";
            case "skill_hematic_focus" -> "F";
            case "skill_vespers_refusal" -> "R";
            case "skill_thread_economy" -> "T";
            case "skill_bound_command" -> "B";
            case "skill_deep_inscription" -> "D";
            case "skill_fungal_symbiosis" -> "F";
            case "skill_sanctum_suture" -> "S";
            case "skill_bloodline_concord" -> "C";
            case "skill_servitor_tender" -> "T";
            case "skill_ancestral_sovereignty" -> "A";
            case "skill_sporitic_attunement" -> "A";
            case "skill_hyphal_cultivation" -> "H";
            case "skill_qliphoth_gestation" -> "Q";
            case "skill_primal_morphogenesis" -> "P";
            default                      -> "?";
        };
    }

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn != 0) return false;
        SkillPoint hit = nodeUnder(ctx, mx, my);
        if (hit != null) {
            tryUnlock(hit);
            return true;
        }
        return false;
    }

    @Override public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) { return false; }

    @Override
    public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) {
        if (btn != 0) return false;
        PanZoomState view = activePanZoom();
        view.applyDrag(dx, dy);
        view.clamp(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
        return true;
    }

    @Override
    public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
        PanZoomState view = activePanZoom();
        view.applyScroll(ctx.guiLeft(), ctx.guiTop(), mx, my, delta);
        view.clamp(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
        if (diveState.isDeepActive()) {
            if (delta < 0 && diveState.updateDeepZoom(deepPanZoom.zoom)) {
                surfacePanZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
                deepFade = 0.0f;
            }
            return true;
        }
        updateDiveProgress(ctx);
        return true;
    }

    @Override public PanZoomState getPanZoomState() { return activePanZoom(); }
    public int getContentW() { return contentW; }
    public int getContentH() { return contentH; }
}
