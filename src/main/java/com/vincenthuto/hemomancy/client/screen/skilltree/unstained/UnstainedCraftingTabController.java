package com.vincenthuto.hemomancy.client.screen.skilltree.unstained;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.IProgressTab;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.PanZoomState;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ProgressScreenContext;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.recipe.BloodStructureRecipe;
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

public class UnstainedCraftingTabController implements IProgressTab {
    private static final int TAB_COLOR = 0xFF80D0C0;
    private static final int LAYER_BTN_SIZE = 16;
    private static final String[] CRAFTING_TIER_NAMES = {"Basic", "Advanced", "Expert"};
    private static final int[] CRAFTING_TIER_THRESHOLDS = {100, 200, Integer.MAX_VALUE};

    private final List<BloodStructureRecipe> craftingRecipes = new ArrayList<>();
    private final LinkedHashMap<String, List<BloodStructureRecipe>> craftingByTier = new LinkedHashMap<>();
    private String selectedCraftingTier = null;
    private int selectedCraftingIndexInTier = 0;
    private float craftingRotationAngle = 0f;
    private boolean craftingDragging = false;
    private double craftingDragLastX = 0;
    private int craftingVisibleLayer = -1;
    private int craftingMaxLayer = 0;
    private int craftingSidebarScroll = 0;
    private int craftingInfoScroll = 0;

    @Override
    public void onInit(ProgressScreenContext ctx) {
        cacheCraftingRecipes();
    }

    @Override
    public void render(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY, float partial) {
        if (!craftingDragging) craftingRotationAngle += partial * 0.4f;
        drawUnstainedCraftingContent(gfx, ctx, mouseX, mouseY);
    }

    @Override public void renderOverlay(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {}
    @Override public void renderTooltip(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {}

    @Override
    public boolean mouseClicked(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn != 0) return false;

        String clickedTier = craftingTierUnder(ctx, mx, my);
        if (clickedTier != null) {
            if (clickedTier.equals(selectedCraftingTier)) {
                selectedCraftingTier = null;
                craftingSidebarScroll = 0;
                craftingInfoScroll = 0;
            } else {
                selectedCraftingTier = clickedTier;
                selectedCraftingIndexInTier = 0;
                craftingVisibleLayer = -1;
                craftingSidebarScroll = 0;
                craftingInfoScroll = 0;
            }
            return true;
        }

        int clickedRecipeIdx = craftingRecipeUnder(ctx, mx, my);
        if (clickedRecipeIdx >= 0) {
            selectedCraftingIndexInTier = clickedRecipeIdx;
            craftingVisibleLayer = -1;
            craftingInfoScroll = 0;
            return true;
        }

        if (isOverLayerUpButton(ctx, mx, my)) {
            if (craftingVisibleLayer == -1) craftingVisibleLayer = craftingMaxLayer;
            else if (craftingVisibleLayer < craftingMaxLayer) craftingVisibleLayer++;
            else craftingVisibleLayer = -1;
            return true;
        }

        if (isOverLayerDownButton(ctx, mx, my)) {
            if (craftingVisibleLayer == -1) craftingVisibleLayer = 0;
            else if (craftingVisibleLayer > 0) craftingVisibleLayer--;
            else craftingVisibleLayer = -1;
            return true;
        }

        if (mx >= ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 4) {
            craftingDragging = true;
            craftingDragLastX = mx;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(ProgressScreenContext ctx, double mx, double my, int btn) {
        if (btn == 0) craftingDragging = false;
        return false;
    }

    @Override
    public boolean mouseDragged(ProgressScreenContext ctx, double mx, double my, int btn, double dx, double dy) {
        if (craftingDragging && btn == 0) {
            craftingRotationAngle += (float) (mx - craftingDragLastX) * 0.8f;
            craftingDragLastX = mx;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(ProgressScreenContext ctx, double mx, double my, double delta) {
        int scrollAmt = (int) (-delta * 14);
        if (ctx.isOverTierSidebar(mx, my)) {
            craftingSidebarScroll = Math.max(0, craftingSidebarScroll + scrollAmt);
            clampCraftingSidebarScroll(ctx);
        } else {
            craftingInfoScroll = Math.max(0, craftingInfoScroll + scrollAmt);
        }
        return true;
    }

    @Override public PanZoomState getPanZoomState() { return null; }

    private void cacheCraftingRecipes() {
        craftingRecipes.clear();
        craftingByTier.clear();
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            for (BloodStructureRecipe r : BloodStructureRecipe.getAllRecipes(Minecraft.getInstance().level)) {
                if (r.isUnstained()) craftingRecipes.add(r);
            }
        }
        for (String tierName : CRAFTING_TIER_NAMES) craftingByTier.put(tierName, new ArrayList<>());
        for (BloodStructureRecipe recipe : craftingRecipes) {
            for (int i = 0; i < CRAFTING_TIER_THRESHOLDS.length; i++) {
                if (recipe.getBloodCost() <= CRAFTING_TIER_THRESHOLDS[i]) {
                    craftingByTier.get(CRAFTING_TIER_NAMES[i]).add(recipe);
                    break;
                }
            }
        }
        selectedCraftingTier = null;
        selectedCraftingIndexInTier = 0;
        for (String tierName : CRAFTING_TIER_NAMES) {
            if (!craftingByTier.getOrDefault(tierName, List.of()).isEmpty()) {
                selectedCraftingTier = tierName;
                break;
            }
        }
    }

    private void drawUnstainedCraftingContent(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        if (craftingRecipes.isEmpty()) {
            gfx.drawCenteredString(ctx.font(), "No Unstained Crafting recipes found", ctx.guiLeft() + ctx.guiWidth() / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF666666);
            return;
        }

        int contentX = ctx.guiLeft() + ProgressScreenContext.TIER_SIDEBAR_W + 6;
        int cw = ctx.guiWidth() - ProgressScreenContext.TIER_SIDEBAR_W - 10;

        if (selectedCraftingTier == null) {
            gfx.pose().pushPose();
            gfx.pose().translate(0, 0, 400);
            drawCraftingTierSidebar(gfx, ctx, mouseX, mouseY);
            gfx.drawCenteredString(ctx.font(), "Select a tier", contentX + cw / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
            gfx.pose().popPose();
            return;
        }

        List<BloodStructureRecipe> tierRecipes = craftingByTier.getOrDefault(selectedCraftingTier, List.of());
        if (tierRecipes.isEmpty()) {
            gfx.pose().pushPose();
            gfx.pose().translate(0, 0, 400);
            drawCraftingTierSidebar(gfx, ctx, mouseX, mouseY);
            gfx.drawCenteredString(ctx.font(), "No recipes in this tier", contentX + cw / 2, ctx.guiTop() + ctx.guiHeight() / 2, 0xFF555555);
            gfx.pose().popPose();
            return;
        }
        if (selectedCraftingIndexInTier >= tierRecipes.size()) selectedCraftingIndexInTier = 0;
        BloodStructureRecipe recipe = tierRecipes.get(selectedCraftingIndexInTier);

        int modelAreaW = cw / 2;
        int modelX = contentX;
        int infoX = contentX + modelAreaW + 10;
        int infoW = cw - modelAreaW - 20;

        drawCraftingModel(gfx, recipe, modelX + 10, ctx.guiTop() + 30, modelAreaW - 20, ctx.guiHeight() - 60);

        gfx.pose().pushPose();
        gfx.pose().translate(0, 0, 400);

        drawCraftingTierSidebar(gfx, ctx, mouseX, mouseY);
        drawLayerButtons(gfx, ctx, mouseX, mouseY);
        drawCraftingInfoPanel(gfx, ctx, recipe, infoX, ctx.guiTop() + 30, infoW);

        gfx.drawCenteredString(ctx.font(), "Drag to rotate", modelX + modelAreaW / 2, ctx.guiTop() + ctx.guiHeight() - 18, 0x44888888);

        gfx.pose().popPose();
    }

    private void drawCraftingTierSidebar(GuiGraphics gfx, ProgressScreenContext ctx, int mouseX, int mouseY) {
        int sx = ctx.guiLeft() + 4;
        int sy = ctx.guiTop() + 24;
        int sw = ProgressScreenContext.TIER_SIDEBAR_W - 8;
        int rowH = 22;

        gfx.drawString(ctx.font(), Component.literal("Tiers").withStyle(s -> s.withColor(TAB_COLOR).withBold(true)), sx + 2, sy, 0);
        sy += 14;

        gfx.fill(sx, sy, sx + sw, sy + 1, 0xFF203050);
        sy += 4;

        int clipTop = sy;
        int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
        gfx.enableScissor(sx, clipTop, sx + sw, clipBottom);

        sy -= craftingSidebarScroll;

        for (int i = 0; i < CRAFTING_TIER_NAMES.length; i++) {
            String tierName = CRAFTING_TIER_NAMES[i];
            boolean selected = tierName.equals(selectedCraftingTier);
            List<BloodStructureRecipe> recipes = craftingByTier.getOrDefault(tierName, List.of());

            boolean hovered = mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + rowH && mouseY >= clipTop && mouseY <= clipBottom;

            int bg = selected ? 0xDD101828 : (hovered ? 0xBB0C1420 : 0x990A0E18);
            gfx.fill(sx, sy, sx + sw, sy + rowH, bg);

            int bc = selected ? TAB_COLOR : 0xFF555555;
            gfx.fill(sx, sy, sx + sw, sy + 1, bc);
            gfx.fill(sx, sy + rowH - 1, sx + sw, sy + rowH, bc);
            gfx.fill(sx, sy, sx + 1, sy + rowH, bc);
            gfx.fill(sx + sw - 1, sy, sx + sw, sy + rowH, bc);

            String label = tierName + " (" + recipes.size() + ")";
            int textCol = selected ? 0xFF80D0C0 : 0xFF999999;
            gfx.drawString(ctx.font(), label, sx + 4, sy + (rowH - 8) / 2, textCol, false);

            if (selected) {
                sy += rowH + 2;
                for (int j = 0; j < recipes.size(); j++) {
                    BloodStructureRecipe r = recipes.get(j);
                    boolean recSel = (j == selectedCraftingIndexInTier);
                    boolean recHov = mouseX >= sx + 4 && mouseX <= sx + sw - 4 && mouseY >= sy && mouseY <= sy + 16 && mouseY >= clipTop && mouseY <= clipBottom;

                    int recBg = recSel ? 0xCC0E1420 : (recHov ? 0xAA0C1020 : 0x00000000);
                    gfx.fill(sx + 2, sy, sx + sw - 2, sy + 16, recBg);

                    if (recSel) gfx.fill(sx + 2, sy, sx + 3, sy + 16, TAB_COLOR);

                    String recPath = r.getId().getPath();
                    if (recPath.contains("/")) recPath = recPath.substring(recPath.lastIndexOf('/') + 1);
                    String recName = HLTextUtils.toProperCase(recPath.replace("_", " "));
                    int recCol = recSel ? 0xFF80D0C0 : 0xFF888888;
                    gfx.drawString(ctx.font(), recName, sx + 8, sy + 4, recCol, false);
                    sy += 18;
                }
            }
            sy += rowH + 2;
        }

        gfx.disableScissor();

        int cH = craftingSidebarContentH();
        int vH = ctx.tierSidebarVisibleH();
        if (cH > vH) {
            if (craftingSidebarScroll > 0) gfx.drawCenteredString(ctx.font(), "\u25B2", sx + sw / 2, clipTop, 0xAAFFFFFF);
            if (craftingSidebarScroll < cH - vH) gfx.drawCenteredString(ctx.font(), "\u25BC", sx + sw / 2, clipBottom - 10, 0xAAFFFFFF);
        }
    }

    private String craftingTierUnder(ProgressScreenContext ctx, double mx, double my) {
        int sx = ctx.guiLeft() + 4;
        int sy = ctx.guiTop() + 24 + 14 + 4;
        int sw = ProgressScreenContext.TIER_SIDEBAR_W - 8;
        int rowH = 22;

        int clipTop = sy;
        int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
        if (my < clipTop || my > clipBottom) return null;

        sy -= craftingSidebarScroll;

        for (String tierName : CRAFTING_TIER_NAMES) {
            boolean selected = tierName.equals(selectedCraftingTier);
            List<BloodStructureRecipe> recipes = craftingByTier.getOrDefault(tierName, List.of());

            if (mx >= sx && mx <= sx + sw && my >= sy && my <= sy + rowH && my >= clipTop && my <= clipBottom) return tierName;

            if (selected) sy += rowH + 2 + recipes.size() * 18;
            sy += rowH + 2;
        }
        return null;
    }

    private int craftingRecipeUnder(ProgressScreenContext ctx, double mx, double my) {
        if (selectedCraftingTier == null) return -1;
        List<BloodStructureRecipe> recipes = craftingByTier.getOrDefault(selectedCraftingTier, List.of());
        if (recipes.isEmpty()) return -1;

        int sx = ctx.guiLeft() + 4;
        int sy = ctx.guiTop() + 24 + 14 + 4;
        int sw = ProgressScreenContext.TIER_SIDEBAR_W - 8;
        int rowH = 22;

        int clipTop = sy;
        int clipBottom = ctx.guiTop() + ctx.guiHeight() - 4;
        if (my < clipTop || my > clipBottom) return -1;

        sy -= craftingSidebarScroll;

        for (String tierName : CRAFTING_TIER_NAMES) {
            boolean selected = tierName.equals(selectedCraftingTier);
            List<BloodStructureRecipe> tierRecipes = craftingByTier.getOrDefault(tierName, List.of());

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

    private void drawCraftingModel(GuiGraphics gfx, BloodStructureRecipe recipe, int areaX, int areaY, int areaW, int areaH) {
        if (recipe.getPattern() == null) return;

        List<BlockPosBlockPair> blockPairs = recipe.getPattern().getBlockPosBlockList();
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

        craftingMaxLayer = maxY - minY;

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
        pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(craftingRotationAngle));

        float offX = -(minX + sizeX / 2f);
        float offY = -(minY + sizeY / 2f);
        float offZ = -(minZ + sizeZ / 2f);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        for (BlockPosBlockPair pair : blockPairs) {
            Block block = pair.getBlock();
            if (block == null || block == Blocks.AIR) continue;
            BlockPos pos = pair.getPos();

            int relativeY = pos.getY() - minY;
            if (craftingVisibleLayer >= 0 && relativeY > craftingVisibleLayer) continue;

            pose.pushPose();
            pose.translate(pos.getX() + offX, pos.getY() + offY, pos.getZ() + offZ);

            boolean dimmed = craftingVisibleLayer >= 0 && relativeY < craftingVisibleLayer;

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

    private void drawCraftingInfoPanel(GuiGraphics gfx, ProgressScreenContext ctx, BloodStructureRecipe recipe, int panelX, int panelY, int panelW) {
        int clipTop = panelY;
        int clipBottom = ctx.guiTop() + ctx.guiHeight() - 8;
        int visibleH = clipBottom - clipTop;

        int totalH = measureCraftingInfoPanelHeight(ctx, recipe, panelW);
        int maxScroll = Math.max(0, totalH - visibleH);
        if (craftingInfoScroll > maxScroll) craftingInfoScroll = maxScroll;

        gfx.enableScissor(panelX - 2, clipTop, panelX + panelW + 2, clipBottom);

        int y = panelY - craftingInfoScroll;
        int lineH = 12;

        String namePath = recipe.getId().getPath();
        if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
        String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
        for (String titleLine : ScreenDrawUtils.wrapText(ctx.font(), name, panelW)) {
            gfx.drawString(ctx.font(), Component.literal(titleLine).withStyle(s -> s.withColor(0xFF80D0C0).withBold(true)), panelX, y, 0);
            y += lineH;
        }
        y += 4;

        gfx.fill(panelX, y, panelX + panelW, y + 1, 0xFF203050);
        y += 6;

        gfx.drawString(ctx.font(), Component.literal("Blood Cost: ").withStyle(s -> s.withColor(0x888888)).append(Component.literal((int) recipe.getBloodCost() + " mL").withStyle(s -> s.withColor(0xAA4444))), panelX, y, 0);
        y += lineH + 4;

        ItemStack heldItem = recipe.getHeldItem();
        if (heldItem != null && !heldItem.isEmpty()) {
            gfx.drawString(ctx.font(), Component.literal("Held Item:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
            y += lineH;

            gfx.renderItem(heldItem, panelX, y);
            gfx.renderItemDecorations(ctx.font(), heldItem, panelX, y);
            List<String> heldLines = ScreenDrawUtils.wrapText(ctx.font(), heldItem.getHoverName().getString(), panelW - 20);
            for (int li = 0; li < heldLines.size(); li++) {
                int ix = li == 0 ? panelX + 20 : panelX + 4;
                gfx.drawString(ctx.font(), Component.literal(heldLines.get(li)).withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
            }
            y += Math.max(20, heldLines.size() * lineH + 4);
        }

        Block hitBlock = recipe.getHitBlock();
        if (hitBlock != null && hitBlock != Blocks.AIR) {
            gfx.drawString(ctx.font(), Component.literal("Activate on:").withStyle(s -> s.withColor(0x888888)), panelX, y, 0);
            y += lineH;

            ItemStack hitStack = new ItemStack(hitBlock);
            if (!hitStack.isEmpty()) {
                gfx.renderItem(hitStack, panelX, y);
                List<String> hitLines = ScreenDrawUtils.wrapText(ctx.font(), hitStack.getHoverName().getString(), panelW - 20);
                for (int li = 0; li < hitLines.size(); li++) {
                    int ix = li == 0 ? panelX + 20 : panelX + 4;
                    gfx.drawString(ctx.font(), Component.literal(hitLines.get(li)).withStyle(s -> s.withColor(0xDDDDDD)), ix, y + 4 + li * lineH, 0);
                }
                y += Math.max(20, hitLines.size() * lineH + 4);
            }
        }

        y += 4;

        ItemStack result = recipe.getResult();
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

        if (recipe.getPattern() != null) {
            Map<Block, Integer> blockCounts = recipe.getPattern().getBlockCount(false);
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
            if (craftingInfoScroll > 0) gfx.drawCenteredString(ctx.font(), "\u25B2", panelX + panelW / 2, clipTop, 0xAAFFFFFF);
            if (craftingInfoScroll < maxScroll) gfx.drawCenteredString(ctx.font(), "\u25BC", panelX + panelW / 2, clipBottom - 10, 0xAAFFFFFF);
        }
    }

    private int measureCraftingInfoPanelHeight(ProgressScreenContext ctx, BloodStructureRecipe recipe, int panelW) {
        int y = 0;
        int lineH = 12;

        String namePath = recipe.getId().getPath();
        if (namePath.contains("/")) namePath = namePath.substring(namePath.lastIndexOf('/') + 1);
        String name = HLTextUtils.toProperCase(namePath.replace("_", " "));
        y += ScreenDrawUtils.wrapText(ctx.font(), name, panelW).size() * lineH;
        y += 4 + 1 + 6;

        y += lineH + 4;

        ItemStack heldItem = recipe.getHeldItem();
        if (heldItem != null && !heldItem.isEmpty()) {
            y += lineH;
            List<String> heldLines = ScreenDrawUtils.wrapText(ctx.font(), heldItem.getHoverName().getString(), panelW - 20);
            y += Math.max(20, heldLines.size() * lineH + 4);
        }

        Block hitBlock = recipe.getHitBlock();
        if (hitBlock != null && hitBlock != Blocks.AIR) {
            y += lineH;
            ItemStack hitStack = new ItemStack(hitBlock);
            if (!hitStack.isEmpty()) {
                List<String> hitLines = ScreenDrawUtils.wrapText(ctx.font(), hitStack.getHoverName().getString(), panelW - 20);
                y += Math.max(20, hitLines.size() * lineH + 4);
            }
        }

        y += 4;

        ItemStack result = recipe.getResult();
        if (result != null && !result.isEmpty()) {
            y += lineH;
            List<String> resultLines = ScreenDrawUtils.wrapText(ctx.font(), result.getHoverName().getString(), panelW - 20);
            y += Math.max(20, resultLines.size() * lineH + 4);
        }

        y += 6;

        if (recipe.getPattern() != null) {
            Map<Block, Integer> blockCounts = recipe.getPattern().getBlockCount(false);
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
        if (craftingMaxLayer <= 0) return;

        int bx = layerBtnX(ctx);
        int cy = layerBtnCenterY(ctx);

        int upY = cy - LAYER_BTN_SIZE - 14;
        boolean upHov = isOverLayerUpButton(ctx, mouseX, mouseY);
        drawNavButton(gfx, ctx, bx, upY, LAYER_BTN_SIZE, LAYER_BTN_SIZE, "\u25B2", upHov);

        int downY = cy + 14;
        boolean downHov = isOverLayerDownButton(ctx, mouseX, mouseY);
        drawNavButton(gfx, ctx, bx, downY, LAYER_BTN_SIZE, LAYER_BTN_SIZE, "\u25BC", downHov);

        String label = craftingVisibleLayer < 0 ? "All" : "Y:" + (craftingVisibleLayer + 1);
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
        if (craftingMaxLayer <= 0) return false;
        int bx = layerBtnX(ctx);
        int cy = layerBtnCenterY(ctx);
        int upY = cy - LAYER_BTN_SIZE - 14;
        return mx >= bx && mx <= bx + LAYER_BTN_SIZE && my >= upY && my <= upY + LAYER_BTN_SIZE;
    }

    private boolean isOverLayerDownButton(ProgressScreenContext ctx, double mx, double my) {
        if (craftingMaxLayer <= 0) return false;
        int bx = layerBtnX(ctx);
        int cy = layerBtnCenterY(ctx);
        int downY = cy + 14;
        return mx >= bx && mx <= bx + LAYER_BTN_SIZE && my >= downY && my <= downY + LAYER_BTN_SIZE;
    }

    private int craftingSidebarContentH() {
        int rowH = 22;
        int total = 0;
        for (String tierName : CRAFTING_TIER_NAMES) {
            total += rowH + 2;
            if (tierName.equals(selectedCraftingTier)) {
                List<BloodStructureRecipe> recipes = craftingByTier.getOrDefault(tierName, List.of());
                total += rowH + 2 + recipes.size() * 18;
            }
        }
        return total;
    }

    private void clampCraftingSidebarScroll(ProgressScreenContext ctx) {
        int maxScroll = Math.max(0, craftingSidebarContentH() - ctx.tierSidebarVisibleH());
        craftingSidebarScroll = Math.min(craftingSidebarScroll, maxScroll);
    }
}
