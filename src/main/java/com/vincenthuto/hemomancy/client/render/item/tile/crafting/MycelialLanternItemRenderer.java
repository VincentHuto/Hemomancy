package com.vincenthuto.hemomancy.client.render.item.tile.crafting;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.tile.crafting.MycelialLanternModel;
import com.vincenthuto.hemomancy.client.render.tile.crafting.MycelialLanternRenderer;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MycelialLanternItemRenderer extends BlockEntityWithoutLevelRenderer {

    private static final int GLASS_TINT = 0x66FFFFFF;
    private MycelialLanternModel model;

    public MycelialLanternItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        if (modelSet != null) {
            this.model = new MycelialLanternModel(modelSet.bakeLayer(MycelialLanternModel.LAYER_LOCATION));
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
            MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (this.model == null) {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            this.model = new MycelialLanternModel(modelSet.bakeLayer(MycelialLanternModel.LAYER_LOCATION));
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        boolean isGui = displayContext == ItemDisplayContext.GUI;
        if (isGui) {
            Lighting.setupForEntityInInventory();
            poseStack.mulPose(new Quaternion(Vector3.YP, 90, true).toMoj());
            poseStack.mulPose(new Quaternion(Vector3.ZP, 30, true).toMoj());
            poseStack.translate(-0.5, -0.5, 0);
        }

        poseStack.pushPose();
        if (displayContext == ItemDisplayContext.GUI) {
            poseStack.translate(0.5, 0.85, 0.5);
            poseStack.scale(0.3f, 0.3f, 0.3f);
            poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
            poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());
        } else if (displayContext == ItemDisplayContext.FIXED) {
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.scale(0.25f, 0.25f, 0.25f);
            poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
            poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
        } else {
            poseStack.translate(0.5, 0.4, 0.5);
            poseStack.scale(0.25f, 0.25f, 0.25f);
            poseStack.mulPose(new Quaternion(Vector3.XP, 180, true).toMoj());
            poseStack.mulPose(new Quaternion(Vector3.YP, 180, true).toMoj());
        }

        VertexConsumer opaqueConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(MycelialLanternRenderer.TEXTURE));
        model.renderOpaqueParts(poseStack, opaqueConsumer, combinedLight, OverlayTexture.NO_OVERLAY, -1);

        VertexConsumer glassConsumer = buffer.getBuffer(RenderTypeInit.MYCELIAL_LANTERN_GLASS);
        model.renderGlass(poseStack, glassConsumer, combinedLight, OverlayTexture.NO_OVERLAY, GLASS_TINT);
        poseStack.popPose();

        if (isGui) {
            Lighting.setupFor3DItems();
        }
    }
}
