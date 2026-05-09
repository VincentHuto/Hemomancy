package com.vincenthuto.hemomancy.client.model.entity.boss;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath.HollowVesselEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class HollowVesselModel<T extends HollowVesselEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("hollow_vessel"), "main");

    public HollowVesselModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F)
                        .texOffs(36, 0).addBox(-5.5F, -11.5F, -1.0F, 11.0F, 2.0F, 2.0F)
                        .texOffs(52, 0).addBox(-1.0F, -14.5F, -1.0F, 2.0F, 4.0F, 2.0F)
                        .texOffs(0, 54).addBox(-3.5F, -1.0F, 3.5F, 7.0F, 8.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 15.0F, 4.0F)
                        .texOffs(0, 36).addBox(-4.5F, 9.0F, -2.5F, 9.0F, 9.0F, 5.0F)
                        .texOffs(38, 16).addBox(-6.0F, 2.0F, -2.5F, 12.0F, 2.0F, 5.0F)
                        .texOffs(38, 23).addBox(-5.5F, 6.0F, -2.7F, 11.0F, 1.0F, 1.0F)
                        .texOffs(38, 26).addBox(-5.0F, 9.0F, -2.7F, 10.0F, 1.0F, 1.0F)
                        .texOffs(28, 46).addBox(-1.0F, 0.0F, -3.0F, 2.0F, 16.0F, 1.0F)
                        .texOffs(34, 46).addBox(-1.0F, 0.0F, 2.0F, 2.0F, 16.0F, 1.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 30).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 17.0F, 4.0F)
                        .texOffs(56, 30).addBox(-3.5F, 12.0F, -2.5F, 2.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.35F, 0.0F, 0.18F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 30).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 17.0F, 4.0F)
                        .texOffs(56, 30).addBox(1.5F, 12.0F, -2.5F, 2.0F, 7.0F, 1.0F),
                PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.35F, 0.0F, -0.18F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offset(-2.2F, 14.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offset(2.2F, 14.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int visualState = entity.getVisualState();
        if (visualState == HollowVesselEntity.VISUAL_OVERLOAD) {
            float rupture = (float) Math.sin(ageInTicks * 0.7F) * 0.18F;
            this.body.zRot = rupture;
            this.head.zRot = -rupture;
            this.rightArm.xRot = -1.35F;
            this.leftArm.xRot = -1.35F;
            this.rightArm.zRot = 0.72F;
            this.leftArm.zRot = -0.72F;
        } else if (visualState == HollowVesselEntity.VISUAL_COLLAPSE_IMPACT) {
            this.rightArm.xRot = -1.45F;
            this.leftArm.xRot = -1.45F;
            this.body.xRot = -0.12F;
        } else if (entity.isCollapseCharging()) {
            this.rightArm.xRot = -1.1F;
            this.leftArm.xRot = -1.1F;
            this.rightArm.zRot = 0.42F;
            this.leftArm.zRot = -0.42F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) - 0.18F;
        } else if (visualState == HollowVesselEntity.VISUAL_EMPTY_PULSE) {
            this.body.xRot = 0.18F;
            this.rightArm.xRot = -0.35F;
            this.leftArm.xRot = -0.35F;
            this.rightArm.zRot = 0.9F;
            this.leftArm.zRot = -0.9F;
        } else if (visualState == HollowVesselEntity.VISUAL_EMPTY_PULSE_IMPACT) {
            this.body.xRot = -0.25F;
            this.rightArm.zRot = 1.2F;
            this.leftArm.zRot = -1.2F;
        } else if (entity.isInPhase2()) {
            this.rightArm.xRot -= 0.25F;
            this.leftArm.xRot -= 0.25F;
        }
    }
}
