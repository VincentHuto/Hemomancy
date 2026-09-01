package com.vincenthuto.hemomancy.client.model.entity.boss.seraphae;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeEntity;
import com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae.SeraphaeState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SeraphaeModel<T extends SeraphaeEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("seraphae"), "main");

    public SeraphaeModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -8.0F, -4.0F, 9.0F, 8.0F, 8.0F)
                        .texOffs(34, 0).addBox(-7.0F, -12.0F, -1.0F, 14.0F, 2.0F, 2.0F)
                        .texOffs(50, 4).addBox(-1.0F, -15.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 16.0F, 4.0F)
                        .texOffs(0, 36).addBox(-5.0F, 5.0F, -2.5F, 10.0F, 15.0F, 5.0F)
                        .texOffs(38, 16).addBox(-10.0F, 0.0F, 1.5F, 7.0F, 16.0F, 1.0F)
                        .texOffs(54, 16).addBox(3.0F, 0.0F, 1.5F, 7.0F, 16.0F, 1.0F)
                        .texOffs(38, 36).addBox(-12.0F, 4.0F, 2.0F, 5.0F, 12.0F, 1.0F)
                        .texOffs(50, 36).addBox(7.0F, 4.0F, 2.0F, 5.0F, 12.0F, 1.0F)
                        .texOffs(0, 58).addBox(-8.0F, 3.0F, -3.0F, 16.0F, 1.0F, 1.0F)
                        .texOffs(0, 61).addBox(-8.0F, 10.0F, -3.0F, 16.0F, 1.0F, 1.0F),
                PartPose.offset(0.0F, -4.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 3.0F, 16.0F, 4.0F)
                        .texOffs(28, 38).addBox(-4.0F, 3.0F, -2.5F, 1.0F, 11.0F, 1.0F),
                PartPose.offsetAndRotation(-4.5F, -2.0F, 0.0F, -0.32F, 0.0F, 0.4F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror().addBox(0.0F, -2.0F, -2.0F, 3.0F, 16.0F, 4.0F)
                        .texOffs(32, 38).addBox(3.0F, 3.0F, -2.5F, 1.0F, 11.0F, 1.0F),
                PartPose.offsetAndRotation(4.5F, -2.0F, 0.0F, -0.32F, 0.0F, -0.4F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 15.0F, 4.0F),
                PartPose.offset(-2.0F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 15.0F, 4.0F),
                PartPose.offset(2.0F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        SeraphaeState state = entity.getSeraphaeState();
        if (state == SeraphaeState.FRACTURING) {
            float urgency = Math.max(0.0F, Math.min(1.0F, (100.0F - entity.getStateTimer()) / 100.0F));
            float shake = (float) Math.sin(ageInTicks * (0.55F + urgency * 0.35F)) * (0.16F + urgency * 0.12F);
            this.body.zRot = shake;
            this.head.zRot = -shake * 0.7F;
            this.rightArm.zRot = 0.8F + shake;
            this.leftArm.zRot = -0.8F - shake;
            this.rightArm.xRot = -0.45F - urgency * 0.35F;
            this.leftArm.xRot = -0.45F - urgency * 0.35F;
        } else if (state == SeraphaeState.CONDENSING) {
            float closing = Math.max(0.0F, Math.min(1.0F, (80.0F - entity.getStateTimer()) / 80.0F));
            float pulse = (float) Math.sin(ageInTicks * 0.5F) * 0.08F;
            this.body.xRot = 0.08F + closing * 0.18F;
            this.rightArm.xRot = -1.25F - closing * 0.25F + pulse;
            this.leftArm.xRot = -1.25F - closing * 0.25F - pulse;
            this.rightArm.zRot = 0.24F + closing * 0.42F;
            this.leftArm.zRot = -0.24F - closing * 0.42F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) + 0.18F + closing * 0.14F;
        }
    }
}
