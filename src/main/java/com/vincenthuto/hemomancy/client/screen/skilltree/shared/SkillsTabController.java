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
    private static final int COL_LINE_LOCKED      = 0x88444444;
    private static final int COL_LINE_UNLOCKED     = 0xFFAA0000;
    private static final int COL_NODE_BG           = 0xCC1A0505;
    private static final int COL_NODE_BORDER_LOCK  = 0xFF333333;
    private static final int COL_NODE_BORDER_UNLOCK= 0xFFCC2222;
    private static final int COL_NODE_BORDER_AVAIL = 0xFFBB8833;

    private final PanZoomState panZoom = new PanZoomState();
    private final Map<SkillPoint, int[]> nodePositions = new HashMap<>();
    private int contentW, contentH;
    private int playerDegree;

    @Override
    public void onInit(ProgressScreenContext ctx) {
        this.playerDegree = ctx.playerDegree();
        buildLayout();
        panZoom.centreOn(contentW, contentH, ctx.guiWidth(), ctx.guiHeight());
    }

    private void buildLayout() {
        nodePositions.clear();
        contentW = 0;
        contentH = 0;
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
                    nodePositions.put(row.get(i), new int[]{x, y});
                    contentW = Math.max(contentW, x + NODE_SIZE);
                    contentH = Math.max(contentH, y + NODE_SIZE + 24);
                }
            }
        }
    }

    private static int depth(SkillPoint sp) {
        int d = 0;
        for (SkillPoint p = sp; p.getParent() != null; p = p.getParent()) d++;
        return d;
    }

    private int sx(ProgressScreenContext ctx, int cx) { return panZoom.sx(ctx.guiLeft(), cx); }
    private int sy(ProgressScreenContext ctx, int cy) { return panZoom.sy(ctx.guiTop(), cy); }
    private int halfNode() { return panZoom.halfNode(NODE_SIZE); }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
        drawConnections(gfx, ctx);
        drawNodes(gfx, ctx);
    }

    @Override
    public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {}

    @Override
    public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        drawTooltip(gfx, ctx, mouseX, mouseY);
    }

    private void drawConnections(GuiGraphics gfx, ProgressScreenContext ctx) {
        int hn = halfNode();
        for (var e : nodePositions.entrySet()) {
            SkillPoint sp = e.getKey();
            if (sp.getParent() == null) continue;
            int[] cPos = e.getValue();
            int[] pPos = nodePositions.get(sp.getParent());
            if (pPos == null) continue;
            int x1 = sx(ctx, pPos[0]), y1 = sy(ctx, pPos[1]);
            int x2 = sx(ctx, cPos[0]), y2 = sy(ctx, cPos[1]);
            boolean parentUnlocked = SkillProgressClientCache.current().getState(sp.getParent()) == EnumSkillStates.UNLOCKED;
            int col = parentUnlocked ? COL_LINE_UNLOCKED : COL_LINE_LOCKED;
            int lw = Math.max(1, (int)(panZoom.zoom * 1.5f));
            int midY = (y1 + y2) / 2;
            gfx.fill(x1 - lw, y1 + hn, x1 + lw, midY, col);
            gfx.fill(Math.min(x1, x2) - lw, midY - lw, Math.max(x1, x2) + lw, midY + lw, col);
            gfx.fill(x2 - lw, midY, x2 + lw, y2 - hn, col);
        }
    }
private float animTime = 0f;
    private void drawNodes(GuiGraphics gfx, ProgressScreenContext ctx) {
        	animTime += 0.016f; // ~60 FPS approximation

		float time = animTime;
        int hn = halfNode();
        for (var e : nodePositions.entrySet()) {
            SkillPoint sp = e.getKey();
            int[] pos = e.getValue();
            int nx = sx(ctx, pos[0]);
            int ny = sy(ctx, pos[1]);
            boolean degreeLocked = sp.isDegreeLocked(playerDegree);
            EnumNodeShape shape = sp.getNodeShape();
            int border;
            if (degreeLocked) {
                border = COL_NODE_BORDER_LOCK;
            } else {
                switch (SkillProgressClientCache.current().getState(sp)) {
                    case UNLOCKED -> {
                        border = COL_NODE_BORDER_UNLOCK;
                        float p = 0.7f + 0.3f * Mth.sin(time * 2f + sp.getId());
                        int ga = (int)(40 * p);
                        NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn + 3, (ga << 24) | 0x00AA0000);
                    }
                    case LOCKED -> {
                        border = COL_NODE_BORDER_LOCK;
                        if (sp.getParent() != null && SkillProgressClientCache.current().getState(sp.getParent()) == EnumSkillStates.UNLOCKED)
                            border = COL_NODE_BORDER_AVAIL;
                    }
                    default -> border = COL_NODE_BORDER_LOCK;
                }
            }
            NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn, COL_NODE_BG);
            NodeShapeRenderer.drawOutline(gfx, shape, nx, ny, hn, border);
            if (degreeLocked) {
                NodeShapeRenderer.drawFill(gfx, shape, nx, ny, hn - 1, 0xBB000000);
                if (panZoom.zoom >= 0.5f) gfx.drawCenteredString(ctx.font(), "?", nx, ny - 4, 0xFF111111);
                continue;
            }
            if (panZoom.zoom >= 0.5f) {
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
                        gfx.drawCenteredString(ctx.font(), ini, nx, ny - 4, textCol);
                    }
                }
                if (sp.getMaxLevels() > 0) {
                    int level = SkillProgressClientCache.current().getLevel(sp);
                    String lvlStr = level + "/" + sp.getMaxLevels();
                    int lvlCol = SkillProgressClientCache.current().isMaxed(sp) ? 0xFF44AA44 : 0xFF888888;
                    gfx.drawCenteredString(ctx.font(), lvlStr, nx, ny + hn + 3, lvlCol);
                }
            }
        }
    }

    private void drawTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        boolean insideGui = mouseX >= ctx.guiLeft() && mouseX < ctx.guiLeft() + ctx.guiWidth()
                && mouseY >= ctx.guiTop() && mouseY < ctx.guiTop() + ctx.guiHeight();
        if (!insideGui) return;
        int hn = halfNode();
        for (var e : nodePositions.entrySet()) {
            SkillPoint sp = e.getKey();
            int[] pos = e.getValue();
            int nx = sx(ctx, pos[0]), ny = sy(ctx, pos[1]);
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
                    if (sp.getParent() != null && SkillProgressClientCache.current().getState(sp.getParent()) != EnumSkillStates.UNLOCKED) {
                        String pn = HLTextUtils.toProperCase(sp.getParent().getName().replace("skill_", "").replace("_", " "));
                        tip.add(Component.literal("Requires: " + pn).withStyle(s -> s.withColor(0xAA4444)));
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

    private SkillPoint nodeUnder(ProgressScreenContext ctx, double mx, double my) {
        int h = halfNode();
        for (var e : nodePositions.entrySet()) {
            int[] p = e.getValue();
            int nx = sx(ctx, p[0]), ny = sy(ctx, p[1]);
            if (NodeShapeRenderer.isInside(e.getKey().getNodeShape(), mx, my, nx, ny, h)) return e.getKey();
        }
        return null;
    }

    private void tryUnlock(SkillPoint sp) {
        if (sp.isDegreeLocked(playerDegree)) return;
        PacketHandler.sendToServer(new PacketUnlockSkill(sp.getId()));
    }

    private static String getSkillInitial(SkillPoint sp) {
        return switch (sp.getName()) {
            case "base"                  -> "\u2726";
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
    @Override public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) { return false; }
    @Override public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) { return false; }

    @Override public PanZoomState getPanZoomState() { return panZoom; }
    public int getContentW() { return contentW; }
    public int getContentH() { return contentH; }
}
