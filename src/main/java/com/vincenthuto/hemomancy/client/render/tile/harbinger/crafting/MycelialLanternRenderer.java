package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.MycelialLanternBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MycelialLanternRenderer implements BlockEntityRenderer<MycelialLanternBlockEntity> {
    private static final int DISPLAY_ITEM_MAX_LIGHT = 11;

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
            int itemLight = displayItemLight(te, light);
            renderDisplayItem(te, display, partialTick, poseStack, buffers, itemLight, overlay);
        }
    }

    private void renderDisplayItem(MycelialLanternBlockEntity te, ItemStack stack, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers, int itemLight, int overlay) {
        float age = te.getLevel() != null ? te.getLevel().getGameTime() + partialTick : partialTick;
        poseStack.pushPose();
        poseStack.translate(0.5D, 1.05D + Math.sin(age * 0.08F) * 0.03D, 0.5D);
        poseStack.mulPose(Vector3.YP.rotationDegrees(age * 2.0F).toMoj());
        poseStack.scale(0.8F, 0.8F, 0.8F);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, itemLight, overlay, poseStack, buffers, te.getLevel(), 0);
        poseStack.popPose();
    }

    private static int displayItemLight(MycelialLanternBlockEntity te, int fallbackLight) {
        if (te.getLevel() == null) {
            return capPackedLight(fallbackLight, DISPLAY_ITEM_MAX_LIGHT);
        }
        int chamberLight = LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above());
        return capPackedLight(chamberLight, DISPLAY_ITEM_MAX_LIGHT);
    }

    private static int capPackedLight(int packedLight, int maxLight) {
        int blockLight = (packedLight >> 4) & 0xF;
        int skyLight = (packedLight >> 20) & 0xF;
        return (Math.min(blockLight, maxLight) << 4) | (Math.min(skyLight, maxLight) << 20);
    }
}
