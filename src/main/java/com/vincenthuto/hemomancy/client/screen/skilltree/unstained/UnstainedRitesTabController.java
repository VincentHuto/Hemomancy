package com.vincenthuto.hemomancy.client.screen.skilltree.unstained;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteRecipe;
import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hutoslib.client.HLTextUtils;
import com.vincenthuto.hutoslib.math.BlockPosBlockPair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UnstainedRitesTabController implements IProgressTab {
    private static final int TAB_COLOR = 0xFF8090BB;
    private static final int LAYER_BTN_SIZE = 16;

    private final List<CardinalRiteRecipe> riteRecipes = new ArrayList<>();
    private final LinkedHashMap<CardinalRiteType, List<CardinalRiteRecipe>> ritesByTier = new LinkedHashMap<>();
    private CardinalRiteType selectedRiteTier = null;
    private int selectedRiteIndexInTier = 0;
    private float riteRotationAngle = 0f;
    private boolean riteDragging = false;
    private double riteDragLastX = 0;
    private int riteVisibleLayer = -1;
    private int riteMaxLayer = 0;
    private int riteSidebarScroll = 0;
    private int riteInfoScroll = 0;

    @Override
    public void onInit(ProgressScreenContext ctx) {
        cacheRiteRecipes();
    }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
        if (!riteDragging) riteRotationAngle += partial * 0.4f;
        drawUnstainedRiteContent(gfx, ctx, mouseX, mouseY, partial);
    }

    @Override public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {}
    @Override public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {}

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn != 0) return false;

        CardinalRiteType clickedRiteTier = riteTierUnder(ctx, mx, my);
        if (clickedRiteTier != null) {
            if (!ritesByTier.getOrDefault(clickedRiteTier, List.of()).isEmpty()) {
                if (clickedRiteTier == selectedRiteTier) {
                    selectedRiteTier = null;
                    riteSidebarScroll = 0;
                    riteInfoScroll = 0;
                } else {
                    selectedRiteTier = clickedRiteTier;
                    selectedRiteIndexInTier = 0;
                    riteVisibleLayer = -1;
                    riteSidebarScroll = 0;
                    riteInfoScroll = 0;
                }
            }
            return true;
        }

        int clickedRiteIdx = riteRecipeUnder(ctx, mx, my);
        if (clickedRiteIdx >= 0) {
            selectedRiteIndexInTier = clickedRiteIdx;
            riteVisibleLayer = -1;
            riteInfoScroll = 0;
            return true;
        }

        if (isOverLayerUpButton(ctx, mx, my)) {
            if (riteVisibleLayer == -1) riteVisibleLayer = riteMaxLayer;
            else if (riteVisibleLayer < riteMaxLayer) riteVisibleLayer++;
            else riteVisibleLayer = -1;
            return true;
        }

        if (isOverLayerDownButton(ctx, mx, my)) {
            if (riteVisibleLayer == -1) riteVisibleLayer = 0;
            else if (riteVisibleLayer > 0) riteVisibleLayer--;
            else riteVisibleLayer = -1;
            return true;
        }

        if (mx >= ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 4) {
            riteDragging = true;
            riteDragLastX = mx;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn == 0) riteDragging = false;
        return false;
    }

    @Override
    public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) {
        if (riteDragging && btn == 0) {
            riteRotationAngle += (float) (mx - riteDragLastX) * 0.8f;
            riteDragLastX = mx;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
        int scrollAmt = (int) (-delta * 14);
        if (ctx.isOverTierSidebar(mx, my)) {
            riteSidebarScroll = Math.max(0, riteSidebarScroll + scrollAmt);
            clampRiteSidebarScroll(ctx);
        } else {
            riteInfoScroll = Math.max(0, riteInfoScroll + scrollAmt);
        }
        return true;
    }

    @Override public PanZoomState getPanZoomState() { return null; }

    private void cacheRiteRecipes() {
        riteRecipes.clear();
        ritesByTier.clear();
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            for (CardinalRiteRecipe r : CardinalRiteRecipe.getAllRecipes(Minecraft.getInstance().level)) {
                if (r.isUnstained()) riteRecipes.add(r);
            }
        }
        for (CardinalRiteType type : CardinalRiteType.values()) ritesByTier.put(type, new ArrayList<>());
        for (CardinalRiteRecipe recipe : riteRecipes) ritesByTier.get(recipe.getRiteType()).add(recipe);
        for (List<CardinalRiteRecipe> tierList : ritesByTier.values()) {
            tierList.sort(java.util.Comparator.comparingDouble(CardinalRiteRecipe::getBloodCost));
        }
        selectedRiteTier = null;
        selectedRiteIndexInTier = 0;
        for (CardinalRiteType type : CardinalRiteType.values()) {
            if (!ritesByTier.getOrDefault(type, List.of()).isEmpty()) {
                selectedRiteTier = type;
                break;
            }
        }
    }

    private void drawUnstainedRiteContent(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
        if (riteRecipes.isEmpty()) {
            gfx.drawCenteredString(ctx.font(), "No Unstained Rites found", ctx.guiLeft() + ctx.guiWidth() / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF666666);
            return;
        }

        int contentX = ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 6;
        int cw = ctx.guiWidth() - ProgressScreenContext.TIER_SIDEBAR_W - 10;

        if (selectedRiteTier == null) {
            gfx.pose().pushPose();
            gfx.pose().translate(0, 0, 400);
            drawRiteTierSidebar(gfx, ctx, mouseX, mouseY);
            gfx.drawCenteredString(ctx.font(), "Select a tier", contentX + cw / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
            gfx.pose().popPose();
            return;
        }

        List<CardinalRiteRecipe> tierRites = ritesByTier.getOrDefault(selectedRiteTier, List.of());
        if (tierRites.isEmpty()) {
            gfx.pose().pushPose();
            gfx.pose().translate(0, 0, 400);
            drawRiteTierSidebar(gfx, ctx, mouseX, mouseY);
            gfx.drawCenteredString(ctx.font(), "No rites in this tier", contentX + cw / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
            gfx.pose().popPose();
            return;
        }
        if (selectedRiteIndexInTier >= tierRites.size()) selectedRiteIndexInTier = 0;
        CardinalRiteRecipe rite = tierRites.get(selectedRiteIndexInTier);

        int modelAreaW = cw / 2;
        int modelX = contentX;
        int infoX = contentX + modelAreaW + 10;
        int infoW = cw - modelAreaW - 20;

        drawRiteModel(gfx, rite, modelX + 10, ctx.guiTop() + 30, modelAreaW - 20, ctx.guiHeight() - 60, partial);

        gfx.pose().pushPose();
        gfx.pose().translate(0, 0, 400);

        drawRiteTierSidebar(gfx, ctx, mouseX, mouseY);
        drawLayerButtons(gfx, ctx, mouseX, mouseY);
        drawRiteInfoPanel(gfx, ctx, rite, infoX, ctx.guiTop() + 30, infoW);

        gfx.drawCenteredString(ctx.font(), "Drag to rotate", modelX + modelAreaW / 2, ctx.guiTop() + ctx.guiHeight() - 18, 0x44888888);

        gfx.pose().popPose();
    }

    private void drawRiteTierSidebar(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        int sx = ctx.guiLeft() + 4;
        int sy = ctx.guiTop() + 24;
        int sw = ProgressScreenContext.TIER_SIDEBAR_W - 8;
        int rowH = 22;

        gfx.drawString(ctx.font(), Component.literal("Rite Tiers").withStyle(s -> s.withColor(TAB_COLOR).withBold(true)), sx + 2, sy, 0);
        sy += 14;

        gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF203050);
        sy += 4;

        int clipTop = sy;
        int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
        gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

        sy -= riteSidebarScroll;

        for (CardinalRiteType type : CardinalRiteType.values()) {
            boolean selected = (type == selectedRiteTier);
            List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());
            if (recipes.isEmpty()) {
                sy += rowH + 2;
                continue;
            }

            boolean hovered = mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + rowH && mouseY >= clipTop && mouseY <= clipBottom;

            int bg = selected ? 0xDD101828 : (hovered ? 0xBB0C1420 : 0x990A0E18);
            gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

            int bc = selected ? TAB_COLOR : 0xFF555555;
            gfx.fill(sx, sy, sx + sw, sy + 1, bc);
            gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
            gfx.fill(sx, sy, sx + 1, sy + rowH, bc);
            gfx.fill(sx + sw - 1, sy, sx + sw, sy + rowH, bc);

            String sizeLabel = type.getSize() + "x" + type.getSize();
            String tierLabel = HLTextUtils.toProperCase(type.getSerializedName());
            int textCol = selected ? 0xFFB0C0E0 : 0xFF999999;
            gfx.drawString(ctx.font(), tierLabel + " " + sizeLabel + " (" + recipes.size() + ")", sx + 4, sy + (rowH - 8) / 2, textCol, false);

            if (selected) {
                sy += rowH + 2;
                for (int j = 0; j < recipes.size(); j++) {
                    CardinalRiteRecipe r = recipes.get(j);
                    boolean recSel = (j == selectedRiteIndexInTier);
                    boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4 && mouseY >= sy && mouseY <= sy + 16 && mouseY >= clipTop && mouseY <= clipBottom;

                    int recBg = recSel ? 0xCC0E1420 : (recHov ? 0xAA0C1020 : 0x00000000);
                    gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);

                    if (recSel) gfx.fill(sx + 2, sy, sx + 3, sy + 16, TAB_COLOR);

                    String recName = r.getRiteName();
                    if (recName == null || recName.isEmpty()) {
                        String ritePath = r.getId().getPath();
                        if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
                        recName = HLTextUtils.toProperCase(ritePath.replace("_", " "));
                    }
                    recName = ScreenDrawUtils.truncateText(ctx.font(), recName, sw - 16);
                    int recCol = recSel ? 0xFFB0C0E0 : 0xFF888888;
                    gfx.drawString(ctx.font(), recName, sx + 8, sy + 4, recCol, false);
                    sy += 18;
                }
            }
            sy += rowH + 2;
        }

        gfx.disableScissor();

        int contentH = riteSidebarContentH();
        int visibleH = ctx.tierSidebarVisibleH();
        if (contentH > visibleH) {
            if (riteSidebarScroll > 0) gfx.drawCenteredString(ctx.font(), "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
            if (riteSidebarScroll < contentH - visibleH) gfx.drawCenteredString(ctx.font(), "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
        }
    }

    private CardinalRiteType riteTierUnder(ProgressScreenContext ctx, double mx, double my) {
        int sx = ctx.guiLeft() + 4;
        int sy = ctx.guiTop() + 24 + 14 + 4;
        int sw = ProgressScreenContext.TIER_SIDEBAR_W - 8;
        int rowH = 22;

        int clipTop = sy;
        int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
        if (my < clipTop || my > clipBottom) return null;

        sy -= riteSidebarScroll;

        for (CardinalRiteType type : CardinalRiteType.values()) {
            boolean selected = (type == selectedRiteTier);
            List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());
            if (recipes.isEmpty()) {
                sy += rowH + 2;
                continue;
            }

            if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + rowH && my >= clipTop && my <= clipBottom) return type;

            if (selected) sy += rowH + 2 + recipes.size() * 18;
            sy += rowH + 2;
        }
        return null;
    }

    private int riteRecipeUnder(ProgressScreenContext ctx, double mx, double my) {
        if (selectedRiteTier == null) return -1;
        List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(selectedRiteTier, List.of());
        if (recipes.isEmpty()) return -1;

        int sx = ctx.guiLeft() + 4;
        int sy = ctx.guiTop() + 24 + 14 + 4;
        int sw = ProgressScreenContext.TIER_SIDEBAR_W - 8;
        int rowH = 22;

        int clipTop = sy;
        int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
        if (my < clipTop || my > clipBottom) return -1;

        sy -= riteSidebarScroll;

        for (CardinalRiteType type : CardinalRiteType.values()) {
            boolean selected = (type == selectedRiteTier);
            List<CardinalRiteRecipe> tierRecipes = ritesByTier.getOrDefault(type, List.of());
            if (tierRecipes.isEmpty()) {
                sy += rowH + 2;
                continue;
            }

            sy += rowH + 2;

            if (selected) {
                for (int j = 0; j < tierRecipes.size(); j++) {
                    if (mx >= sx + 4 && mx <= sx + sw - 4 && my >= sy && my <= sy + 16 && my >= clipTop && my <= clipBottom) return j;
                    sy += 18;
                }
                return -1;
            }
        }
        return -1;
    }

    private void drawRiteModel(GuiGraphics gfx, CardinalRiteRecipe rite, int areaX, int areaY, int areaW, int areaH, float partial) {
        if (rite.getPattern() == null) return;

        List<BlockPosBlockPair> blockPairs = rite.getPattern().getBlockPosBlockList();
        if (blockPairs.isEmpty()) return;

        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
        for (BlockPosBlockPair pair : blockPairs) {
            BlockPos pos = pair.getPos();
            Block block = pair.getBlock();
            if (block == null || block == Blocks.AIR) continue;
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        if (minX > maxX) return;

        riteMaxLayer = maxY - minY;

        float sizeX = maxX - minX + 1;
        float sizeY = maxY - minY + 1;
        float sizeZ = maxZ - minZ + 1;
        float maxDim = Math.max(sizeX, Math.max(sizeY, sizeZ));

        float scale = Math.min(areaW, areaH) / (maxDim * 1.8f);
        int centerX = areaX + areaW / 2;
        int centerY = areaY + areaH / 2;

        PoseStack pose = gfx.pose();
        pose.pushPose();

        pose.translate(centerX, centerY, 300);
        pose.scale(scale, -scale, scale);
        pose.mulPose(com.mojang.math.Axis.XP.rotationDegrees(30));
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(riteRotationAngle));

        float offX = -(minX + sizeX / 2f);
        float offY = -(minY + sizeY / 2f);
        float offZ = -(minZ + sizeZ / 2f);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        for (BlockPosBlockPair pair : blockPairs) {
            Block block = pair.getBlock();
            if (block == null || block == Blocks.AIR) continue;
            BlockPos pos = pair.getPos();

            int relativeY = pos.getY() - minY;
            if (riteVisibleLayer >= 0 && relativeY > riteVisibleLayer) continue;

            pose.pushPose();
            pose.translate(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);

            boolean dimmed = riteVisibleLayer >= 0 && relativeY < riteVisibleLayer;

            try {
                Minecraft.getInstance().getBlockRenderer().renderSingleBlock(block.defaultBlockState(), pose, bufferSource,
                        dimmed ? 0x60006 : LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            } catch (Exception ignored) {
            }

            pose.popPose();
        }

        bufferSource.endBatch();
        pose.popPose();
    }

    private void drawRiteInfoPanel(GuiGraphics gfx, ProgressScreenContext ctx, CardinalRiteRecipe rite, int panelX, int panelY, int panelW) {
        int clipTop = panelY;
        int clipBottom = ctx.guiTop() + ctx.guiHeight() - 8;
        int visibleH = clipBottom - clipTop;

        int totalH = measureRiteInfoPanelHeight(ctx, rite, panelW);
        int maxScroll = Math.max(0, totalH - visibleH);
        if (riteInfoScroll > maxScroll) riteInfoScroll = maxScroll;

        gfx.enableScissor(panelX - 2, clipTop, panelX + panelW + 2, clipBottom);

        int y = panelY - riteInfoScroll;
        int lineH = 12;

        String name = rite.getRiteName();
        if (name == null || name.isEmpty()) {
            String ritePath = rite.getId().getPath();
            if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
            name = HLTextUtils.toProperCase(ritePath.replace("_", " "));
        }
        for (String titleLine : ScreenDrawUtils.wrapText(ctx.font(), name, panelW)) {
            gfx.drawString(ctx.font(), Component.literal(titleLine).withStyle(s -> s.withColor(0xFFB0C0E0).withBold(true)), panelX, y, 0);
            y += lineH;
        }
        y += 4;

        gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF203050);
        y += 6;

        String desc = rite.getRiteDescription();
        if (desc != null && !desc.isEmpty()) {
            List<String> lines = ScreenDrawUtils.wrapText(ctx.font(), desc, panelW);
            for (String line : lines) {
                gfx.drawString(ctx.font(), Component.literal(line).withStyle(s -> s.withColor(0x999999).withItalic(true)), panelX, y, 0);
                y += lineH;
            }
            y += 4;
        }

        CardinalRiteType type = rite.getRiteType();
        String typeStr = HLTextUtils.toProperCase(type.getSerializedName()) + " (" + type.getSize() + "x" + type.getSize() + ")";
        gfx.drawString(ctx.font(), Component.literal("Type: ").withStyle(s -> s.withColor(0x888888)).append(Component.literal(typeStr).withStyle(s -> s.withColor(0xFFB0C0E0))), panelX, y, 0);
        y += lineH;

        int ticks = type.getCastingDurationTicks();
        float seconds = ticks / 20f;
        gfx.drawString(ctx.font(), Component.literal("Cast Time: ").withStyle(s -> s.withColor(0x888888)).append(Component.literal(String.format("%.1fs", seconds)).withStyle(s -> s.withColor(0xAAAA88))), panelX, y, 0);
        y += lineH + 6;

        ItemStack result = rite.getResult();
        if (result != null && !result.isEmpty()) {
            gfx.drawString(ctx.font(), Component.literal("Result:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
            y += lineH;

            gfx.renderItem(result, panelX, y);
            gfx.renderItemDecorations(ctx.font(), result, panelX, y);
            List<String> resultLines = ScreenDrawUtils.wrapText(ctx.font(), result.getHoverName().getString(), panelW - 20);
            for (int li = 0; li < resultLines.size(); li++) {
                int ix = li == 0 ? panelX + 20 : panelX + 4;
                gfx.drawString(ctx.font(), Component.literal(resultLines.get(li)).withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
            }
            y += Math.max(20, resultLines.size() * lineH + 4);
        }

        y += 6;

        if (rite.getPattern() != null) {
            Map<Block, Integer> blockCounts = rite.getPattern().getBlockCount(false);
            if (!blockCounts.isEmpty()) {
                gfx.drawString(ctx.font(), Component.literal("Materials:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
                y += lineH;

                for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
                    Block block = entry.getKey();
                    if (block == null || block == Blocks.AIR) continue;
                    int count = entry.getValue();

                    ItemStack blockStack = new ItemStack(block);
                    if (!blockStack.isEmpty()) {
                        gfx.renderItem(blockStack, panelX + 2, y);
                        String countPrefix = " x" + count + "  ";
                        List<String> matLines = ScreenDrawUtils.wrapText(ctx.font(), countPrefix + blockStack.getHoverName().getString(), panelW - 20);
                        for (int li = 0; li < matLines.size(); li++) {
                            gfx.drawString(ctx.font(), Component.literal(matLines.get(li)).withStyle(s -> s.withColor(0xAAAAAA)), panelX + 20, y + 4 + li * lineH, 0);
                        }
                        y += Math.max(18, matLines.size() * lineH + 4);
                    }
                }
            }
        }

        gfx.disableScissor();

        if (totalH > visibleH) {
            if (riteInfoScroll > 0) gfx.drawCenteredString(ctx.font(), "\u25B2", panelX + panelW / 2, clipTop, 0xAAFFFFFF);
            if (riteInfoScroll < maxScroll) gfx.drawCenteredString(ctx.font(), "\u25BC", panelX + panelW / 2, clipBottom - 10, 0xAAFFFFFF);
        }
    }

    private int measureRiteInfoPanelHeight(ProgressScreenContext ctx, CardinalRiteRecipe rite, int panelW) {
        int y = 0;
        int lineH = 12;

        String name = rite.getRiteName();
        if (name == null || name.isEmpty()) {
            String ritePath = rite.getId().getPath();
            if (ritePath.contains("/")) ritePath = ritePath.substring(ritePath.lastIndexOf('/') + 1);
            name = HLTextUtils.toProperCase(ritePath.replace("_", " "));
        }
        y += ScreenDrawUtils.wrapText(ctx.font(), name, panelW).size() * lineH;
        y += 4 + 1 + 6;

        String desc = rite.getRiteDescription();
        if (desc != null && !desc.isEmpty()) y += ScreenDrawUtils.wrapText(ctx.font(), desc, panelW).size() * lineH + 4;

        y += lineH;
        y += lineH + 6;

        ItemStack result = rite.getResult();
        if (result != null && !result.isEmpty()) {
            y += lineH;
            List<String> resultLines = ScreenDrawUtils.wrapText(ctx.font(), result.getHoverName().getString(), panelW - 20);
            y += Math.max(20, resultLines.size() * lineH + 4);
        }

        y += 6;

        if (rite.getPattern() != null) {
            Map<Block, Integer> blockCounts = rite.getPattern().getBlockCount(false);
            if (!blockCounts.isEmpty()) {
                y += lineH;
                for (Map.Entry<Block, Integer> entry : blockCounts.entrySet()) {
                    Block block = entry.getKey();
                    if (block == null || block == Blocks.AIR) continue;
                    ItemStack blockStack = new ItemStack(block);
                    if (!blockStack.isEmpty()) {
                        String countPrefix = " x" + entry.getValue() + "  ";
                        List<String> matLines = ScreenDrawUtils.wrapText(ctx.font(), countPrefix + blockStack.getHoverName().getString(), panelW - 20);
                        y += Math.max(18, matLines.size() * lineH + 4);
                    }
                }
            }
        }

        return y;
    }

    private int layerBtnX(ProgressScreenContext ctx) { return ctx.layerBtnX(); }
    private int layerBtnCenterY(ProgressScreenContext ctx) { return ctx.layerBtnCenterY(); }

    private void drawLayerButtons(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        if (riteMaxLayer <= 0) return;

        int bx = layerBtnX(ctx);
        int cy = layerBtnCenterY(ctx);

        int upY = cy - LAYER_BTN_SIZE - 14;
        boolean upHov = isOverLayerUpButton(ctx, mouseX, mouseY);
        drawNavButton(gfx, ctx, bx, upY, LAYER_BTN_SIZE, LAYER_BTN_SIZE, "\u25B2", upHov);

        int downY = cy + 14;
        boolean downHov = isOverLayerDownButton(ctx, mouseX, mouseY);
        drawNavButton(gfx, ctx, bx, downY, LAYER_BTN_SIZE, LAYER_BTN_SIZE, "\u25BC", downHov);

        String label = riteVisibleLayer < 0 ? "All" : "Y:" + (riteVisibleLayer + 1);
        gfx.drawCenteredString(ctx.font(), label, bx + LAYER_BTN_SIZE / 2, cy - 4, 0xFFAAAAAA);

        if (upHov) gfx.renderTooltip(ctx.font(), Component.literal("Layer Up"), mouseX, mouseY);
        else if (downHov) gfx.renderTooltip(ctx.font(), Component.literal("Layer Down"), mouseX, mouseY);
    }

    private void drawNavButton(GuiGraphics gfx, ProgressScreenContext ctx, int x, int y, int w, int h, String symbol, boolean hovered) {
        int bg = hovered ? 0xDD101828 : 0x990C1020;
        gfx.fill(x, y, x + w, y + h, bg);

        int bc = hovered ? TAB_COLOR : 0xFF444444;
        gfx.fill(x, y, x + w, y + 1, bc);
        gfx.fill(x, y + h - 1, x + w, y + h, bc);
        gfx.fill(x, y, x + 1, y + h, bc);
        gfx.fill(x + w - 1, y, x + w, y + h, bc);

        int textCol = hovered ? 0xFFB0C0E0 : 0xFF888888;
        gfx.drawCenteredString(ctx.font(), symbol, x + w / 2, y + (h - 8) / 2, textCol);
    }

    private boolean isOverLayerUpButton(ProgressScreenContext ctx, double mx, double my) {
        if (riteMaxLayer <= 0) return false;
        int bx = layerBtnX(ctx);
        int cy = layerBtnCenterY(ctx);
        int upY = cy - LAYER_BTN_SIZE - 14;
        return mx >= bx && mx <= bx + LAYER_BTN_SIZE && my >= upY && my <= upY + LAYER_BTN_SIZE;
    }

    private boolean isOverLayerDownButton(ProgressScreenContext ctx, double mx, double my) {
        if (riteMaxLayer <= 0) return false;
        int bx = layerBtnX(ctx);
        int cy = layerBtnCenterY(ctx);
        int downY = cy + 14;
        return mx >= bx && mx <= bx + LAYER_BTN_SIZE && my >= downY && my <= downY + LAYER_BTN_SIZE;
    }

    private int riteSidebarContentH() {
        int rowH = 22;
        int total = 0;
        for (CardinalRiteType type : CardinalRiteType.values()) {
            List<CardinalRiteRecipe> recipes = ritesByTier.getOrDefault(type, List.of());
            if (recipes.isEmpty()) continue;
            total += rowH + 2;
            if (type == selectedRiteTier) total += rowH + 2 + recipes.size() * 18;
        }
        return total;
    }

    private void clampRiteSidebarScroll(ProgressScreenContext ctx) {
        int maxScroll = Math.max(0, riteSidebarContentH() - ctx.tierSidebarVisibleH());
        riteSidebarScroll = Math.min(riteSidebarScroll, maxScroll);
    }
}
