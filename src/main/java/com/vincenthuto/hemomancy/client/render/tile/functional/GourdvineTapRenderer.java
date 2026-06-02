package com.vincenthuto.hemomancy.client.render.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.tile.functional.GourdvineTapBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GourdvineTapRenderer implements BlockEntityRenderer<GourdvineTapBlockEntity> {
    private final ItemRenderer itemRenderer;

    public GourdvineTapRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(GourdvineTapBlockEntity te, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
            int combinedLight, int combinedOverlay) {
        ItemStack displayed = te.getItem(GourdvineTapBlockEntity.SLOT_GOURD);
        if (displayed.isEmpty()) {
            return;
        }

        long gameTime = te.getLevel() != null ? te.getLevel().getGameTime() : 0L;
        float time = gameTime + partialTicks;
        float bob = Mth.sin(time * 0.08F) * 0.03F;
        float spin = (time * 2.5F) % 360.0F;

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.68F + bob, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        poseStack.mulPose(Axis.XP.rotationDegrees(12.5F));
        poseStack.scale(0.75F, 0.75F, 0.75F);
        this.itemRenderer.renderStatic(null, displayed, ItemDisplayContext.FIXED, false,
                poseStack, buffer, te.getLevel(), combinedLight, combinedOverlay, 0);
        poseStack.popPose();
    }
}

