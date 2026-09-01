package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class MorphlingChitiniteLegAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_chitinite_leg_attachment"), "main");

    private final ModelPart root;

    public MorphlingChitiniteLegAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("thigh_plate",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(2.2f, 1.0f, -2.4f, 3.0f, 5.0f, 4.8f, new CubeDeformation(0.08f))
                        .texOffs(18, 0)
                        .addBox(5.0f, 2.0f, -1.4f, 1.2f, 3.0f, 2.8f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("shin_plate",
                CubeListBuilder.create()
                        .texOffs(0, 14)
                        .addBox(2.4f, 6.0f, -2.0f, 2.6f, 5.4f, 4.0f, new CubeDeformation(0.08f))
                        .texOffs(18, 14)
                        .addBox(4.9f, 7.2f, -1.0f, 1.2f, 3.8f, 2.0f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("spurs",
                CubeListBuilder.create()
                        .texOffs(34, 0)
                        .addBox(4.0f, 3.0f, -0.5f, 2.6f, 0.8f, 1.0f, CubeDeformation.NONE)
                        .texOffs(34, 5)
                        .addBox(4.2f, 8.3f, -0.5f, 2.2f, 0.8f, 1.0f, CubeDeformation.NONE)
                        .texOffs(34, 10)
                        .addBox(3.0f, 10.8f, -2.8f, 1.4f, 0.8f, 1.6f, CubeDeformation.NONE),
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
