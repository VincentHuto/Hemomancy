package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class MorphlingPestsBodyAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_pests_body_attachment"), "main");

    private final ModelPart root;
    private final ModelPart broodNodes;
    private final ModelPart skitterLegs;

    public MorphlingPestsBodyAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.broodNodes = this.root.getChild("brood_nodes");
        this.skitterLegs = this.root.getChild("skitter_legs");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("hive_plate",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.6f, 1.2f, 2.1f, 9.2f, 7.8f, 1.8f, new CubeDeformation(0.10f))
                        .texOffs(0, 14)
                        .addBox(-3.6f, 2.2f, 3.5f, 7.2f, 5.8f, 1.3f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("brood_nodes",
                CubeListBuilder.create()
                        .texOffs(28, 0)
                        .addBox(-5.2f, 2.0f, 3.7f, 2.4f, 2.4f, 1.4f, new CubeDeformation(0.04f))
                        .texOffs(28, 8)
                        .addBox(2.8f, 3.2f, 3.8f, 2.2f, 2.2f, 1.2f, new CubeDeformation(0.04f))
                        .texOffs(28, 15)
                        .addBox(-1.3f, 6.0f, 3.9f, 2.6f, 2.0f, 1.2f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("skitter_legs",
                CubeListBuilder.create()
                        .texOffs(0, 25)
                        .addBox(-7.0f, 2.0f, 3.0f, 2.8f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(0, 29)
                        .addBox(4.2f, 2.8f, 3.0f, 2.8f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(0, 33)
                        .addBox(-6.8f, 6.0f, 3.2f, 2.6f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(0, 37)
                        .addBox(4.2f, 6.8f, 3.2f, 2.6f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(16, 25)
                        .addBox(-2.8f, 9.0f, 3.4f, 1.0f, 2.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(22, 25)
                        .addBox(1.8f, 9.0f, 3.4f, 1.0f, 2.8f, 0.8f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        float pulse = Mth.sin(ageInTicks * 0.12f) * 0.05f;
        float skitter = Mth.sin(ageInTicks * 0.22f + limbSwing) * (0.035f + limbSwingAmount * 0.06f);
        broodNodes.zScale = 1.0f + pulse;
        skitterLegs.zRot = skitter;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
