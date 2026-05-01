package com.vincenthuto.hemomancy.client.render.tile.crafting;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.tile.crafting.MorphlingIncubatorModel;
import com.vincenthuto.hemomancy.common.tile.crafting.MycelialCrucibleBlockEntity;
import com.vincenthuto.hutoslib.math.Vector3;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Block entity renderer for the Mycelial Crucible.
 *
 * <p>Reuses the {@link MorphlingIncubatorModel} mesh and texture as a placeholder
 * until a dedicated Mycelial Crucible model and texture are created.
 */
public class MycelialCrucibleRenderer implements BlockEntityRenderer<MycelialCrucibleBlockEntity> {

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    /** Placeholder — reuse the Morphling Incubator texture until custom art exists. */
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("hemomancy", "textures/entity/morphling_incubator_model.png");

    private final MorphlingIncubatorModel model;

    public MycelialCrucibleRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new MorphlingIncubatorModel(
                context.bakeLayer(MorphlingIncubatorModel.LAYER_LOCATION));
    }

    @Override
    public boolean shouldRenderOffScreen(MycelialCrucibleBlockEntity te) {
        return true;
    }

    @Override
    public void render(MycelialCrucibleBlockEntity te, float partialTick,
            PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        poseStack.pushPose();

        // Flip Y (Blockbench → Minecraft) and centre on the block
        poseStack.translate(0.5D, 1.5D, 0.5D);
        poseStack.mulPose(Vector3.XP.rotationDegrees(180f).toMoj());

        // Rotate to match FACING
        Direction facing = te.getBlockState().getValue(FACING);
        float yRot = switch (facing) {
            case NORTH -> 180f;
            case EAST  -> 270f;
            case WEST  ->  90f;
            default    ->   0f;
        };
        poseStack.mulPose(Vector3.YP.rotationDegrees(yRot).toMoj());

        float ageInTicks = te.getLevel().getGameTime() + partialTick;
        model.setupAnim(ageInTicks);

        VertexConsumer vc = buffers.getBuffer(RenderType.entityTranslucentCull(TEXTURE));
        model.renderToBuffer(poseStack, vc, light, OverlayTexture.NO_OVERLAY, -1);

        poseStack.popPose();
    }
}
