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

public class MorphlingTickBodyAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_tick_body_attachment"), "main");

    private final ModelPart root;
    private final ModelPart engorgedSac;
    private final ModelPart claspers;

    public MorphlingTickBodyAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.engorgedSac = this.root.getChild("engorged_sac");
        this.claspers = this.root.getChild("claspers");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("engorged_sac",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.2f, 1.0f, 2.0f, 8.4f, 8.6f, 3.8f, new CubeDeformation(0.20f))
                        .texOffs(0, 17)
                        .addBox(-2.8f, 2.4f, 5.2f, 5.6f, 5.4f, 1.4f, new CubeDeformation(0.08f)),
                PartPose.ZERO);

        root.addOrReplaceChild("head_latch",
                CubeListBuilder.create()
                        .texOffs(30, 0)
                        .addBox(-2.6f, -0.4f, 2.4f, 5.2f, 2.4f, 2.0f, new CubeDeformation(0.06f))
                        .texOffs(30, 8)
                        .addBox(-1.0f, -1.6f, 3.2f, 2.0f, 1.8f, 1.4f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("claspers",
                CubeListBuilder.create()
                        .texOffs(0, 28)
                        .addBox(-6.4f, 1.6f, 3.2f, 2.4f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 32)
                        .addBox(4.0f, 1.6f, 3.2f, 2.4f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 36)
                        .addBox(-6.8f, 4.6f, 3.7f, 2.8f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 40)
                        .addBox(4.0f, 4.6f, 3.7f, 2.8f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 44)
                        .addBox(-5.8f, 8.0f, 3.4f, 2.0f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 48)
                        .addBox(3.8f, 8.0f, 3.4f, 2.0f, 0.8f, 0.8f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        float pulse = Mth.sin(ageInTicks * 0.08f) * 0.06f;
        engorgedSac.xScale = 1.0f + pulse * 0.5f;
        engorgedSac.zScale = 1.0f + pulse;
        claspers.zRot = Mth.sin(ageInTicks * 0.15f + limbSwing) * (0.025f + limbSwingAmount * 0.04f);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
