package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.MasonsEffigyModel;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class MasonsEffigyItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_masons_effigy.png");

    private static final float GUI_PITCH = 180.0F;
    private static final float GUI_YAW = 0.0F;

    private MasonsEffigyModel model;

    public MasonsEffigyItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        if (modelSet != null) {
            this.model = new MasonsEffigyModel(modelSet.bakeLayer(MasonsEffigyModel.LAYER_LOCATION));
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        if (this.model == null) {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            this.model = new MasonsEffigyModel(modelSet.bakeLayer(MasonsEffigyModel.LAYER_LOCATION));
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        boolean isGui = displayContext == ItemDisplayContext.GUI;
        if (isGui) {
            Lighting.setupForEntityInInventory();
        }

        poseStack.pushPose();

        if (isGui) {
            poseStack.translate(0.5, .7, 0.5);
            poseStack.scale(0.4f, 0.4f, .4f);
            poseStack.mulPose(new Quaternion(Vector3.XN, 150, true).toMoj());
            poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());
            poseStack.mulPose(new Quaternion(Vector3.ZP, 0, true).toMoj());
        } else if (displayContext == ItemDisplayContext.FIXED) {
            poseStack.translate(0.5D, 0.6D, 0.5D);
            poseStack.scale(0.45F, 0.45F, 0.45F);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        } else {
            poseStack.translate(0.5D, 0.55D, 0.5D);
            poseStack.scale(0.45F, 0.45F, 0.45F);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        }

        this.model.setupAnim(0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, combinedLight, OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();

        if (isGui) {
            Lighting.setupFor3DItems();
        }
    }
}

