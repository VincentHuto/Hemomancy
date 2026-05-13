package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.morphling.MorphlingModelAttachment.AttachmentPoint;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

/**
 * Minimal example geometry used to demonstrate each Morphling attachment point.
 */
public class MorphlingAttachmentExampleModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation HEAD_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_head"), "main");
    public static final ModelLayerLocation BODY_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_body"), "main");
    public static final ModelLayerLocation ARMS_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_arms"), "main");
    public static final ModelLayerLocation LEGS_LAYER =
            new ModelLayerLocation(Hemomancy.rloc("morphling_attachment_example_legs"), "main");

    private final ModelPart root;

    public MorphlingAttachmentExampleModel(ModelPart root) {
        this.root = root;
    }

    public static ModelLayerLocation layerFor(AttachmentPoint point) {
        return switch (point) {
            case HEAD -> HEAD_LAYER;
            case BODY -> BODY_LAYER;
            case ARMS -> ARMS_LAYER;
            case LEGS -> LEGS_LAYER;
        };
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        partRoot.addOrReplaceChild("root",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-3f, -3f, -3f, 6f, 6f, 6f, CubeDeformation.NONE),
                PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
