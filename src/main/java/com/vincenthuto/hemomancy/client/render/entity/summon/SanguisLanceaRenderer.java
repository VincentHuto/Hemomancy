package com.vincenthuto.hemomancy.client.render.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.item.SanguisLanceaModel;
import com.vincenthuto.hemomancy.common.entity.projectile.SanguisLanceaEntity;
import com.vincenthuto.hemomancy.common.init.RenderTypeInit;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class SanguisLanceaRenderer extends EntityRenderer<SanguisLanceaEntity> {
    public static ResourceLocation texture = Hemomancy.rloc("textures/entity/sanguis_lancea/model_sanguis_lancea.png");
    private final SanguisLanceaModel model;


    public SanguisLanceaRenderer(Context pContext) {
        super(pContext);
        this.model = new SanguisLanceaModel(pContext.bakeLayer(SanguisLanceaModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(SanguisLanceaEntity entity) {
        return texture;
    }

    @Override
    public void render(SanguisLanceaEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
		pPoseStack.pushPose();
		pPoseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.yRotO, pEntity.getYRot()) - 90.0F));
		pPoseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(pPartialTicks, pEntity.xRotO, pEntity.getXRot()) + 90.0F));
		pPoseStack.translate(0f,2f,0f);
		pPoseStack.scale(0.5f,0.5f,0.5f);

		VertexConsumer base = pBuffer.getBuffer(this.model.renderType(texture));
		this.model.renderToBuffer(pPoseStack, base, pPackedLight, OverlayTexture.NO_OVERLAY, -1);
		VertexConsumer glint = pBuffer.getBuffer(RenderTypeInit.getCrimsonGlint());
		this.model.renderToBuffer(pPoseStack, glint, pPackedLight, OverlayTexture.NO_OVERLAY, -1);

		pPoseStack.popPose();
		super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }
}

