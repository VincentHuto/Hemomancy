package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class MorphlingBatHeadAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_bat_head_attachment"), "main");

    private final ModelPart root;
    private final ModelPart leftEar;
    private final ModelPart rightEar;

    public MorphlingBatHeadAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.leftEar = this.root.getChild("left_ear");
        this.rightEar = this.root.getChild("right_ear");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("brow_crest",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.5f, -9.0f, -5.4f, 9.0f, 2.0f, 2.0f, new CubeDeformation(0.08f))
                        .texOffs(24, 0)
                        .addBox(-2.0f, -8.4f, -6.6f, 4.0f, 1.6f, 1.4f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("left_ear",
                CubeListBuilder.create()
                        .texOffs(0, 8)
                        .addBox(-1.0f, -5.2f, -0.5f, 2.0f, 5.2f, 1.0f, new CubeDeformation(0.05f))
                        .texOffs(8, 8)
                        .addBox(-0.5f, -6.8f, -0.35f, 1.0f, 2.4f, 0.7f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-3.6f, -7.2f, -3.9f, -0.12f, 0.0f, -0.28f));

        root.addOrReplaceChild("right_ear",
                CubeListBuilder.create()
                        .texOffs(16, 8)
                        .addBox(-1.0f, -5.2f, -0.5f, 2.0f, 5.2f, 1.0f, new CubeDeformation(0.05f))
                        .texOffs(24, 8)
                        .addBox(-0.5f, -6.8f, -0.35f, 1.0f, 2.4f, 0.7f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(3.6f, -7.2f, -3.9f, -0.12f, 0.0f, 0.28f));

        root.addOrReplaceChild("back_hook",
                CubeListBuilder.create()
                        .texOffs(36, 0)
                        .addBox(-2.5f, -8.8f, 3.6f, 5.0f, 2.4f, 1.6f, new CubeDeformation(0.04f)),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        float twitch = Mth.sin(ageInTicks * 0.18f) * 0.04f;
        leftEar.zRot = -0.28f - twitch;
        rightEar.zRot = 0.28f + twitch;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
