package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.crafting.MycelialLanternModel;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MycelialLanternBlock;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hemomancy.common.tile.crafting.MycelialLanternBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MycelialLanternRenderer implements BlockEntityRenderer<MycelialLanternBlockEntity> {

    public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_mycelial_lantern.png");
    private static final int GLASS_TINT = 0x66FFFFFF;
    private final MycelialLanternModel model;
    private final ItemRenderer itemRenderer;

    public MycelialLanternRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MycelialLanternModel(context.bakeLayer(MycelialLanternModel.LAYER_LOCATION));
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public boolean shouldRenderOffScreen(MycelialLanternBlockEntity te) {
        return true;
    }

    @Override
    public void render(MycelialLanternBlockEntity te, float partialTick, PoseStack poseStack,
            MultiBufferSource buffers, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5D, 0D, 0.5D);
        poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());

        Direction facing = te.getBlockState().getValue(MycelialLanternBlock.FACING);
        float yRot = switch (facing) {
            case NORTH -> 180f;
            case EAST -> 270f;
            case WEST -> 90f;
            default -> 0f;
        };
        poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

        VertexConsumer opaqueConsumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderFrameParts(poseStack, opaqueConsumer, light, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();

        ItemStack display = !te.getCultureStack().isEmpty() ? te.getCultureStack() : te.getOutputStack();
        if (!display.isEmpty()) {
            renderDisplayItem(te, display, partialTick, poseStack, buffers, light, overlay);
        }

        if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch();
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0D, 0.5D);
        poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());
        poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());
        VertexConsumer glassConsumer = buffers.getBuffer(RenderTypeInit.MYCELIAL_LANTERN_GLASS);
        model.renderGlass(poseStack, glassConsumer, light, OverlayTexture.NO_OVERLAY, GLASS_TINT);
        poseStack.popPose();

        if (buffers instanceof MultiBufferSource.BufferSource bufferSource) {
            bufferSource.endBatch(RenderTypeInit.MYCELIAL_LANTERN_GLASS);
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
