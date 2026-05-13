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

public class MorphlingCentipedeBodyAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_centipede_body_attachment"), "main");

    private final ModelPart root;
    private final ModelPart feelers;

    public MorphlingCentipedeBodyAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.feelers = this.root.getChild("feelers");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("segments",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.8f, 0.8f, -2.9f, 9.6f, 1.5f, 5.8f, new CubeDeformation(0.08f))
                        .texOffs(0, 9)
                        .addBox(-4.6f, 2.8f, -3.0f, 9.2f, 1.5f, 6.0f, new CubeDeformation(0.08f))
                        .texOffs(0, 18)
                        .addBox(-4.4f, 4.8f, -3.0f, 8.8f, 1.5f, 6.0f, new CubeDeformation(0.08f))
                        .texOffs(0, 27)
                        .addBox(-4.2f, 6.8f, -2.8f, 8.4f, 1.5f, 5.6f, new CubeDeformation(0.08f))
                        .texOffs(0, 36)
                        .addBox(-3.8f, 8.8f, -2.4f, 7.6f, 1.2f, 4.8f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("side_legs",
                CubeListBuilder.create()
                        .texOffs(34, 0)
                        .addBox(-7.0f, 1.4f, -1.6f, 2.4f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(34, 4)
                        .addBox(4.6f, 1.4f, -1.6f, 2.4f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(34, 8)
                        .addBox(-7.2f, 3.4f, 0.0f, 2.8f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(34, 12)
                        .addBox(4.4f, 3.4f, 0.0f, 2.8f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(34, 16)
                        .addBox(-6.8f, 5.4f, 1.4f, 2.4f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(34, 20)
                        .addBox(4.4f, 5.4f, 1.4f, 2.4f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(34, 24)
                        .addBox(-6.4f, 8.2f, -1.0f, 2.2f, 0.7f, 0.7f, CubeDeformation.NONE)
                        .texOffs(34, 28)
                        .addBox(4.2f, 8.2f, -1.0f, 2.2f, 0.7f, 0.7f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("feelers",
                CubeListBuilder.create()
                        .texOffs(50, 0)
                        .addBox(-5.4f, -1.4f, -3.4f, 0.7f, 3.4f, 0.7f, CubeDeformation.NONE)
                        .texOffs(54, 0)
                        .addBox(4.7f, -1.4f, -3.4f, 0.7f, 3.4f, 0.7f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        feelers.zRot = Mth.sin(ageInTicks * 0.12f) * 0.035f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
