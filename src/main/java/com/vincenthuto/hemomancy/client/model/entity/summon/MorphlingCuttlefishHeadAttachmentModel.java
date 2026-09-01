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

public class MorphlingCuttlefishHeadAttachmentModel extends EntityModel<LivingEntity> {

    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("morphling_cuttlefish_head_attachment"), "main");

    private final ModelPart root;
    private final ModelPart fins;
    private final ModelPart tendrils;

    public MorphlingCuttlefishHeadAttachmentModel(ModelPart root) {
        this.root = root.getChild("root");
        this.fins = this.root.getChild("fins");
        this.tendrils = this.root.getChild("tendrils");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partRoot = mesh.getRoot();
        PartDefinition root = partRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("mantle",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-4.7f, -9.6f, -4.8f, 9.4f, 3.6f, 2.0f, new CubeDeformation(0.08f))
                        .texOffs(0, 10)
                        .addBox(-3.6f, -6.8f, -5.4f, 7.2f, 3.0f, 1.4f, CubeDeformation.NONE)
                        .texOffs(24, 0)
                        .addBox(-2.2f, -10.8f, -3.9f, 4.4f, 1.4f, 1.0f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("fins",
                CubeListBuilder.create()
                        .texOffs(0, 20)
                        .addBox(-7.2f, -8.2f, -3.8f, 2.8f, 4.8f, 0.8f, new CubeDeformation(0.03f))
                        .texOffs(10, 20)
                        .addBox(4.4f, -8.2f, -3.8f, 2.8f, 4.8f, 0.8f, new CubeDeformation(0.03f))
                        .texOffs(20, 20)
                        .addBox(-6.2f, -4.4f, -3.4f, 1.8f, 2.8f, 0.7f, CubeDeformation.NONE)
                        .texOffs(28, 20)
                        .addBox(4.4f, -4.4f, -3.4f, 1.8f, 2.8f, 0.7f, CubeDeformation.NONE),
                PartPose.ZERO);

        root.addOrReplaceChild("tendrils",
                CubeListBuilder.create()
                        .texOffs(40, 0)
                        .addBox(-3.5f, -4.2f, -6.0f, 0.8f, 4.4f, 0.8f, CubeDeformation.NONE)
                        .texOffs(44, 0)
                        .addBox(-1.5f, -4.0f, -6.2f, 0.8f, 4.9f, 0.8f, CubeDeformation.NONE)
                        .texOffs(48, 0)
                        .addBox(0.7f, -4.0f, -6.2f, 0.8f, 4.9f, 0.8f, CubeDeformation.NONE)
                        .texOffs(52, 0)
                        .addBox(2.7f, -4.2f, -6.0f, 0.8f, 4.4f, 0.8f, CubeDeformation.NONE)
                        .texOffs(40, 10)
                        .addBox(-2.4f, -3.8f, -6.5f, 0.7f, 3.4f, 0.7f, CubeDeformation.NONE)
                        .texOffs(44, 10)
                        .addBox(1.7f, -3.8f, -6.5f, 0.7f, 3.4f, 0.7f, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        fins.xScale = 1.0f + Mth.sin(ageInTicks * 0.08f) * 0.025f;
        tendrils.xRot = Mth.sin(ageInTicks * 0.13f) * 0.035f;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
            int packedOverlay, int packedColor) {
        root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
