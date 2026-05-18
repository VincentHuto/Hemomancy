package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.tile.crafting.MycelialLanternBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MycelialLanternRenderer implements BlockEntityRenderer<MycelialLanternBlockEntity> {

    private final ItemRenderer itemRenderer;

    public MycelialLanternRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public boolean shouldRenderOffScreen(MycelialLanternBlockEntity te) {
        return true;
    }

    @Override
    public void render(MycelialLanternBlockEntity te, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        ItemStack display = !te.getCultureStack().isEmpty() ? te.getCultureStack() : te.getOutputStack();
        if (!display.isEmpty()) {
            renderDisplayItem(te, display, partialTick, poseStack, buffers, light, overlay);
        }
    }

    private void renderDisplayItem(MycelialLanternBlockEntity te, ItemStack stack, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        float age = te.getLevel() != null ? te.getLevel().getGameTime() + partialTick : partialTick;
        poseStack.pushPose();
        poseStack.translate(0.5D, 0.75D + Math.sin(age * 0.08F) * 0.03D, 0.5D);
        poseStack.mulPose(Vector3.YP.rotationDegrees(age * 2.0F).toMoj());
        poseStack.scale(1.2F, 1.2F, 1.2F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, poseStack, buffers, null, 0);
        poseStack.popPose();
    }
}
