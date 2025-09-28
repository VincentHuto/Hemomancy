package com.vincenthuto.hemomancy.compat.mna.block.render;

import com.mna.ManaAndArtifice;
import com.mna.tools.render.ModelUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.compat.mna.block.BrokenManaTrapazahedronBlock;
import com.vincenthuto.hemomancy.compat.mna.block.model.BrokenManaTrapazahedronModel;
import com.vincenthuto.hemomancy.compat.mna.tile.BrokenManaTrapazahedronBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.util.RenderUtils;

public class BrokenManaTrapazahedronRenderer extends GeoBlockRenderer<BrokenManaTrapazahedronBlockEntity> {
    public static final ResourceLocation crystal = Hemomancy.rloc("block/broken_mana_trapazahedron_gem");
    public static final ResourceLocation runes = Hemomancy.rloc("block/broken_mana_trapazahedron_runes");
    protected Minecraft mc = Minecraft.getInstance();

    public BrokenManaTrapazahedronRenderer(Context context) {
        super(new BrokenManaTrapazahedronModel());
    }

    public RenderType getRenderType(BrokenManaTrapazahedronBlockEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entitySmoothCutout(texture);
    }

    public void preRender(
            PoseStack poseStack,
            BrokenManaTrapazahedronBlockEntity animatable,
            BakedGeoModel model,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {
        if (animatable.getBlockState().getValue(BrokenManaTrapazahedronBlock.HANGING)) {
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.translate(0.0F, -1.0F, -1.0F);
        }

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public void renderRecursively(
            PoseStack stack,
            BrokenManaTrapazahedronBlockEntity animatable,
            GeoBone bone,
            RenderType renderType,
            MultiBufferSource bufferSource,
            VertexConsumer buffer,
            boolean isReRender,
            float partialTick,
            int packedLight,
            int packedOverlay,
            float red,
            float green,
            float blue,
            float alpha
    ) {

        if (this.mc != null) {
            stack.pushPose();
            RenderUtils.translateMatrixToBone(stack, bone);
            RenderUtils.translateToPivotPoint(stack, bone);
            RenderUtils.rotateMatrixAroundBone(stack, bone);
            RenderUtils.scaleMatrixForBone(stack, bone);
            RenderUtils.translateAwayFromPivotPoint(stack, bone);
            BlockPos pos = animatable.getBlockPos();
            BlockState state = animatable.getBlockState();
            if (!bone.isHidden()) {
                stack.pushPose();
                String var17 = bone.getName();
                switch (var17) {
                    case "BINDING_RING":
                        stack.mulPose(Axis.YP.rotationDegrees((partialTick + (float) ManaAndArtifice.instance.proxy.getGameTicks()) / 8.0F));
                        ModelUtils.renderModel(bufferSource, this.mc.level, pos, state, runes, stack, packedLight, packedOverlay);
                        break;
                    case "CRYSTAL":
                        ModelUtils.renderModel(bufferSource, this.mc.level, pos, state, crystal, stack, packedLight, packedOverlay);
                }

                stack.popPose();

                for (GeoBone childBone : bone.getChildBones()) {
                    this.renderRecursively(
                            stack,
                            animatable,
                            childBone,
                            renderType,
                            bufferSource,
                            buffer,
                            isReRender,
                            partialTick,
                            packedLight,
                            packedOverlay,
                            red,
                            green,
                            blue,
                            alpha
                    );
                }
            }

            stack.popPose();
        }
    }
}
