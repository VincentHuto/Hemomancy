package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.morphling.MorphlingModelAttachment.AttachmentPoint;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

/**
 * Minimal example geometry used to demonstrate each Morphling attachment point.
 */
public class MorphlingAttachmentExampleModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation HEAD_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_head"), "main");
    public static final ModelLayerLocation BODY_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_body"), "main");
    public static final ModelLayerLocation RIGHT_ARM_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_right_arm"), "main");
    public static final ModelLayerLocation LEFT_ARM_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_left_arm"), "main");
    public static final ModelLayerLocation RIGHT_LEG_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_right_leg"), "main");
    public static final ModelLayerLocation LEFT_LEG_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_left_leg"), "main");

    private final ModelPart root;

    public MorphlingAttachmentExampleModel(ModelPart root) {
        this.root = root.getChild("root");
    }

    public static ModelLayerLocation layerFor(AttachmentPoint point) {
        return switch (point) {
            case HEAD -> HEAD_LAYER;
            case BODY -> BODY_LAYER;
            case RIGHT_ARM -> RIGHT_ARM_LAYER;
            case LEFT_ARM -> LEFT_ARM_LAYER;
            case RIGHT_LEG -> RIGHT_LEG_LAYER;
            case LEFT_LEG -> LEFT_LEG_LAYER;
        };
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("root",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2f, -2f, -2f, 4f, 4f, 4f, CubeDeformation.NONE),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, float red, float green, float blue, float alpha) {
        root.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
