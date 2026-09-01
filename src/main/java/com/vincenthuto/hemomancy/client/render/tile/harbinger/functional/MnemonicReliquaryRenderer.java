package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.model.tile.functional.MnemonicReliquaryModel;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.MnemonicReliquaryBlock;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MnemonicReliquaryBlockEntity;
import com.vincenthuto.hutoslib.math.Quaternion;
import com.vincenthuto.hutoslib.math.Vector3;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MnemonicReliquaryRenderer implements BlockEntityRenderer<MnemonicReliquaryBlockEntity> {

    public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/model_mnemonic_reliquary.png");
    /**
     * Default Y position of the brain part (from the model definition: offset 4.0F).
     */
    private static final float BRAIN_DEFAULT_Y = 4.0F;
    /**
     * Maximum lid rotation in radians (-67.5 degrees).
     */
    private static final float LID_MAX_ANGLE = -67.5F * Mth.DEG_TO_RAD;
    /**
     * Maximum upward float distance for the brain (in model units).
     */
    private static final float BRAIN_FLOAT_HEIGHT = 3.0F;
    private final MnemonicReliquaryModel model;

    public MnemonicReliquaryRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MnemonicReliquaryModel(context.bakeLayer(MnemonicReliquaryModel.LAYER_LOCATION));
    }

    @Override
    public void render(MnemonicReliquaryBlockEntity te, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {

        float lidAngle = te.getLidAngle(partialTicks);
        poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());
        poseStack.translate(0, -0.56, -1D);
        // Animate Lid: rotate along Z axis, from 0 to -67.5 degrees
        ModelPart lid = model.getLid();
        lid.zRot = -LID_MAX_ANGLE * lidAngle;

        // Animate Brain: float up and bob when lid is open
        ModelPart brain = model.getBrain();
        ModelPart base = model.getBase();

        if (lidAngle > 0.001F && te.getLevel() != null) {
            float gameTime = te.getLevel().getGameTime() + partialTicks;
            // Bob up and down with a sine wave, scaled by how open the lid is
            float bob = Mth.sin(gameTime * 0.1F) * 0.5F;
            // Float upward as lid opens, plus gentle bob
            brain.y = BRAIN_DEFAULT_Y + (BRAIN_FLOAT_HEIGHT * lidAngle) + (bob * lidAngle);
        } else {
            brain.y = BRAIN_DEFAULT_Y;
        }
        Direction facing = te.getBlockState().getValue(MnemonicReliquaryBlock.FACING);
        float yRot = 0F;
        switch (facing) {
            case SOUTH -> {
                yRot = 270F;
                poseStack.translate(0D, 0D, 1D);
            }
            case WEST -> {
                yRot = 180F;
                poseStack.translate(1D, 0D, 1D);
            }
            case NORTH -> {
                yRot = 90F;
                poseStack.translate(1D, 0D, 0D);
            }
            case EAST -> {
                yRot = 0F;
                poseStack.translate(0D, 0D, 0D);
            }
            default -> {
                yRot = 0F;
                poseStack.translate(0D, 0D, 0D);
            }
        }

        poseStack.mulPose(new Quaternion(Vector3.YN, yRot, true).toMoj());
        poseStack.pushPose();
        // Center on block X/Z; Y offset places model so lid bottom sits at block Y=0
        // Model spans model-Y 1..9 pixels; after 180° X flip worldY = translateY - modelY/16
        // We want lid bottom (model Y=9) at block Y=0 → translateY = 9/16
        poseStack.translate(0.5D, 9.0D / 16.0D, 0.5D);
        // Flip 180° around X so model-Y-down becomes world-Y-up
        poseStack.mulPose(new Quaternion(Vector3.XN, 180, true).toMoj());

        VertexConsumer vertexConsumer = bufferIn.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, combinedLightIn, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
    }
}

