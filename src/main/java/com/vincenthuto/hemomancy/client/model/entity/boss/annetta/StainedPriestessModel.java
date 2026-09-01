package com.vincenthuto.hemomancy.client.model.entity.boss.annetta;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.StainedPriestessEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class StainedPriestessModel<T extends StainedPriestessEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("stained_priestess"), "main");

    public StainedPriestessModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-5.0F, -10.0F, -1.0F, 10.0F, 2.0F, 2.0F)
                        .texOffs(52, 0).addBox(-1.0F, -13.0F, -1.0F, 2.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.5F, 0.0F, -2.2F, 9.0F, 15.0F, 5.0F, new CubeDeformation(0.05F))
                        .texOffs(0, 36).addBox(-6.0F, 10.0F, -3.0F, 12.0F, 10.0F, 6.0F)
                        .texOffs(44, 36).addBox(-1.0F, -3.0F, 2.0F, 2.0F, 21.0F, 2.0F)
                        .texOffs(52, 38).addBox(-7.0F, 2.0F, 2.5F, 14.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -2.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F)
                        .texOffs(24, 52).addBox(-3.5F, 11.0F, -2.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(-5.5F, 0.0F, 0.0F, 0.08F, 0.0F, 0.08F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 15.0F, 4.0F)
                        .texOffs(30, 52).addBox(2.5F, 11.0F, -2.5F, 1.0F, 6.0F, 1.0F),
                PartPose.offsetAndRotation(5.5F, 0.0F, 0.0F, 0.08F, 0.0F, -0.08F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offset(-2.0F, 11.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 14.0F, 4.0F),
                PartPose.offset(2.0F, 11.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int visualState = entity.getVisualState();
        if (visualState == StainedPriestessEntity.VISUAL_LANCE) {
            this.body.yRot = -0.18F;
            this.rightArm.xRot = -1.65F;
            this.rightArm.yRot = -0.35F;
            this.leftArm.xRot = -0.55F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) - 0.08F;
        } else if (visualState == StainedPriestessEntity.VISUAL_LUNGE) {
            this.body.xRot = 0.35F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) + 0.22F;
            this.rightArm.xRot = -0.95F;
            this.leftArm.xRot = -0.95F;
            this.rightLeg.xRot = -0.55F;
            this.leftLeg.xRot = 0.42F;
        } else if (visualState == StainedPriestessEntity.VISUAL_PRESSURE_BLOOM) {
            float pulse = (float) Math.sin(ageInTicks * 0.6F) * 0.12F;
            this.body.xRot = -0.12F;
            this.rightArm.xRot = -1.05F + pulse;
            this.leftArm.xRot = -1.05F - pulse;
            this.rightArm.zRot = 0.95F;
            this.leftArm.zRot = -0.95F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) - 0.18F;
        }
    }
}
