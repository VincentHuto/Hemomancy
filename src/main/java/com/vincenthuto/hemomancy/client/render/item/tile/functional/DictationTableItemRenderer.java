package com.vincenthuto.hemomancy.client.render.item.tile.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.DictationTableModel;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DictationTableItemRenderer extends BlockEntityWithoutLevelRenderer {

    public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_dictation_table.png");

    private DictationTableModel tableModel;

    public DictationTableItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
        if (modelSet != null) {
            this.tableModel = new DictationTableModel(
                    modelSet.bakeLayer(DictationTableModel.LAYER_LOCATION));
        }
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffer, int combinedLight, int combinedOverlay) {

        if (this.tableModel == null) {
            EntityModelSet modelSet = Minecraft.getInstance().getEntityModels();
            this.tableModel = new DictationTableModel(
                    modelSet.bakeLayer(DictationTableModel.LAYER_LOCATION));
        }

        poseStack.pushPose();
        poseStack.translate(0.5, .9, 0.5);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(new Quaternion(Vector3.XN, 150, true).toMoj());
        poseStack.mulPose(new Quaternion(Vector3.YN, 45, true).toMoj());
        poseStack.mulPose(new Quaternion(Vector3.ZP, 0, true).toMoj());
        tableModel.renderToBuffer(poseStack, buffer.getBuffer(tableModel.renderType(TEXTURE)), combinedLight,
                OverlayTexture.NO_OVERLAY, -1);
        poseStack.popPose();
    }
}


