package com.vincenthuto.hemomancy.client.render.tile;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.block.SuspendedVivianiteModel;
import com.vincenthuto.hemomancy.common.tile.SuspendedVivianiteBlockEntity;
import com.vincenthuto.hutoslib.client.HlClientTickHandler;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;

public class SuspendedVivaniteRenderer implements BlockEntityRenderer<SuspendedVivianiteBlockEntity> {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static ResourceLocation texture = new ResourceLocation(Hemomancy.MOD_ID,
            "textures/entity/model_suspended_vivianite.png");
    private final SuspendedVivianiteModel heart;

    public SuspendedVivaniteRenderer(BlockEntityRendererProvider.Context p_173636_) {
        heart = new SuspendedVivianiteModel(p_173636_.bakeLayer(SuspendedVivianiteModel.LAYER_LOCATION));
    }

    @Override
    public void render(SuspendedVivianiteBlockEntity te, float partialTicks, PoseStack matrixStackIn,
                       MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        double ticks = HlClientTickHandler.ticksInGame + HlClientTickHandler.partialTicks - 1.3 * 0.14;
        matrixStackIn.pushPose();
        matrixStackIn.translate(0.5D, 1.6D, 0.5D);
        matrixStackIn.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
        float currentTime = te.getLevel().getGameTime() + partialTicks;
        matrixStackIn.translate(0D, (Math.sin(Math.PI * currentTime / 2 / 32) / 5) + 0.1D, 0D);
        matrixStackIn.mulPose(Vector3.YP.rotationDegrees((float) ticks / 2).toMoj());
        float scale = (float) Math.abs(Math.cos(currentTime * 0.045f) * 0.25f) + 0.4f;
        matrixStackIn.translate(0, -scale * 0.7f - 0.2f + .5, 0);
        MultiBufferSource.BufferSource irendertypebuffer$impl = MultiBufferSource
                .immediate(Tesselator.getInstance().getBuilder());
        RenderType renderType=heart.renderType(texture);
        RenderType wrappedType = new RenderType(renderType.toString() + "_translucent", renderType.format(), renderType.mode(),
                renderType.bufferSize(), renderType.affectsCrumbling(), true,
                () -> {
                    renderType.setupRenderState();
                    RenderSystem.setShader(GameRenderer::getRendertypeEntityTranslucentShader);
                }
                , () -> renderType.clearRenderState()) {

        };
        VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(wrappedType);
        heart.renderToBuffer(matrixStackIn, ivertexbuilder, combinedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F,
                1.0F, 1.0F);
        irendertypebuffer$impl.endBatch();
        matrixStackIn.popPose();

    }

}
