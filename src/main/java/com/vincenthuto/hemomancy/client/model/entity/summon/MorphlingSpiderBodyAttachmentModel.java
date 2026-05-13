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

public class MorphlingSpiderBodyAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_spider_body_attachment"), "main");

    private final ModelPart root;
    private final ModelPart leftLegs;
    private final ModelPart rightLegs;

    public MorphlingSpiderBodyAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.leftLegs = this.root.getChild("left_legs");
        this.rightLegs = this.root.getChild("right_legs");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("carapace",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.6f, 2.0f, -6.2f, 9.2f, 7.0f, 3.6f, new CubeDeformation(0.16f))
                        .texOffs(28, 0)
                        .addBox(-3.2f, 8.4f, -5.7f, 6.4f, 4.2f, 3.2f, new CubeDeformation(0.1f)),
                PartPose.ZERO);

        root.addOrReplaceChild("left_legs",
                CubeListBuilder.create()
                        .texOffs(0, 18)
                        .addBox(-0.2f, -0.4f, -0.3f, 5.8f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 22)
                        .addBox(-0.1f, 2.0f, -0.3f, 5.2f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 26)
                        .addBox(-0.1f, 4.4f, -0.3f, 4.6f, 0.8f, 0.8f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(4.1f, 3.0f, -5.2f, 0.0f, -0.16f, 0.2f));

        root.addOrReplaceChild("right_legs",
                CubeListBuilder.create()
                        .texOffs(22, 18)
                        .addBox(-5.6f, -0.4f, -0.3f, 5.8f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(22, 22)
                        .addBox(-5.1f, 2.0f, -0.3f, 5.2f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(22, 26)
                        .addBox(-4.5f, 4.4f, -0.3f, 4.6f, 0.8f, 0.8f, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-4.1f, 3.0f, -5.2f, 0.0f, 0.16f, -0.2f));

        root.addOrReplaceChild("fangs",
                CubeListBuilder.create()
                        .texOffs(48, 0)
                        .addBox(-2.4f, 5.0f, -7.0f, 1.0f, 4.0f, 1.0f, CubeDeformation.NONE)
                        .texOffs(54, 0)
                        .addBox(1.4f, 5.0f, -7.0f, 1.0f, 4.0f, 1.0f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        float crawl = Mth.sin(ageInTicks * 0.16f + limbSwing) * (0.05f + limbSwingAmount * 0.08f);
        leftLegs.zRot = 0.2f + crawl;
        rightLegs.zRot = -0.2f - crawl;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
