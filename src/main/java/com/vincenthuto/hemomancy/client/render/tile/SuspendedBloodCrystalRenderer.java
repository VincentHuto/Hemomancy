package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.SuspendedBloodCrystalModel;
import com.vincenthuto.hemomancy.common.tile.SuspendedBloodCrystalBlockEntity;
import com.vincenthuto.hutoslib.client.HlClientTickHandler;
import com.vincenthuto.hutoslib.math.Quaternion;
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

public class SuspendedBloodCrystalRenderer implements BlockEntityRenderer<SuspendedBloodCrystalBlockEntity> {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static ResourceLocation texture = Hemomancy.rloc("textures/entity/model_suspended_blood_crystal.png");
    private final SuspendedBloodCrystalModel heart;

    public SuspendedBloodCrystalRenderer(BlockEntityRendererProvider.Context p_173636_) {
        heart = new SuspendedBloodCrystalModel(p_173636_.bakeLayer(SuspendedBloodCrystalModel.LAYER_LOCATION));
    }

    @Override
    public void render(SuspendedBloodCrystalBlockEntity te, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        double ticks = HlClientTickHandler.ticksInGame + HlClientTickHandler.partialTicks - 1.3 * 0.14;
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5D, 1.75D, 0.5D);
        matrixStackIn.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
        float currentTime = te.getLevel().getGameTime() + partialTicks;
        matrixStackIn.translate(0D, (Math.sin(Math.PI * currentTime / 2 / 32) / 5) + 0.1D*te.timeOffset, 0D);
        matrixStackIn.mulPose(Vector3.YP.rotationDegrees((float) ticks / 2).toMoj());
        float scale = (float) Math.abs(Math.cos(currentTime * 0.045f) * 0.25f) + 0.4f;
        matrixStackIn.translate(0, (-scale * 0.7f - 0.2f +.5)*te.timeOffset, 0);
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
        heart.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();

    }

}

