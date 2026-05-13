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

public class MorphlingUrchinBodyAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_urchin_body_attachment"), "main");

    private final ModelPart root;
    private final ModelPart spineFan;

    public MorphlingUrchinBodyAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.spineFan = this.root.getChild("spine_fan");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("back_plate",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.6f, 1.0f, 2.1f, 9.2f, 8.8f, 1.8f, new CubeDeformation(0.12f))
                        .texOffs(0, 14)
                        .addBox(-3.2f, 2.2f, 3.4f, 6.4f, 5.6f, 1.4f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("spine_fan",
                CubeListBuilder.create()
                        .texOffs(28, 0)
                        .addBox(-0.4f, -2.0f, 3.7f, 0.8f, 7.2f, 0.8f, CubeDeformation.NONE)
                        .texOffs(32, 0)
                        .addBox(-4.4f, 0.0f, 3.8f, 0.8f, 6.2f, 0.8f, CubeDeformation.NONE)
                        .texOffs(36, 0)
                        .addBox(3.6f, 0.0f, 3.8f, 0.8f, 6.2f, 0.8f, CubeDeformation.NONE)
                        .texOffs(40, 0)
                        .addBox(-5.8f, 4.2f, 3.6f, 0.8f, 5.4f, 0.8f, CubeDeformation.NONE)
                        .texOffs(44, 0)
                        .addBox(5.0f, 4.2f, 3.6f, 0.8f, 5.4f, 0.8f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("side_barbs",
                CubeListBuilder.create()
                        .texOffs(0, 24)
                        .addBox(-6.8f, 2.2f, 2.6f, 2.8f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 28)
                        .addBox(4.0f, 2.2f, 2.6f, 2.8f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 32)
                        .addBox(-6.6f, 7.2f, 2.8f, 2.6f, 0.8f, 0.8f, CubeDeformation.NONE)
                        .texOffs(0, 36)
                        .addBox(4.0f, 7.2f, 2.8f, 2.6f, 0.8f, 0.8f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        float pulse = 1.0f + Mth.sin(ageInTicks * 0.08f) * 0.03f;
        spineFan.zScale = pulse;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
