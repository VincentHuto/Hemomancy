package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.SuspendedVivianiteModel;
import com.vincenthuto.hemomancy.common.tile.SuspendedVivianiteBlockEntity;
import com.vincenthuto.hutoslib.client.HlClientTickHandler;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class SuspendedVivaniteRenderer implements BlockEntityRenderer<SuspendedVivianiteBlockEntity> {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static ResourceLocation texture = Hemomancy.rloc("textures/entity/model_suspended_vivianite.png");
    private final SuspendedVivianiteModel heart;

    public SuspendedVivaniteRenderer(BlockEntityRendererProvider.Context p_173636_) {
        heart = new SuspendedVivianiteModel(p_173636_.bakeLayer(SuspendedVivianiteModel.LAYER_LOCATION));
    }

    @Override
    public void render(SuspendedVivianiteBlockEntity te, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        int items = 1;



        float[] angles = new float[1];
        float anglePer = 360.0F / items;
        float totalAngle = 0.0F;

        for (int i = 0; i < angles.length; i++) {
            angles[i] = totalAngle += anglePer;
        }
        double ticks = HlClientTickHandler.ticksInGame + HlClientTickHandler.partialTicks - 1.3 * 0.14;
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5F, 0, 0.5F);
        matrixStackIn.mulPose(Vector3.YP.rotationDegrees(angles[0] + (float)te.getLevel().getGameTime()).toMoj());
        matrixStackIn.translate(0.025F, -0.5F, 0.025F);
        matrixStackIn.mulPose(Vector3.YP.rotationDegrees(90.0F).toMoj());
        matrixStackIn.translate(0.0, 0.175 + 0 * 0.25, 0.0);
        float currentTime = te.getLevel().getGameTime() ;

        RenderType renderType=heart.renderType(texture);
        RenderType wrappedType = new RenderType(renderType.toString() + "_translucent", renderType.format(), renderType.mode(),
                renderType.bufferSize(), renderType.affectsCrumbling(), true,
                () -> {
                    renderType.setupRenderState();
                    RenderSystem.setShader(GameRenderer::getRendertypeEntityTranslucentShader);
                }
                , () -> renderType.clearRenderState()) {

        };
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(wrappedType);
        heart.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F,
                1.0F, 1.0F);
        matrixStackIn.popPose();

    }

}
