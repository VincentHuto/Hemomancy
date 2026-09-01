package com.vincenthuto.hemomancy.client.model.entity.boss.annetta;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.AnnettaKnowlesEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class AnnettaKnowlesModel<T extends AnnettaKnowlesEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("annetta_knowles"), "main");

    public AnnettaKnowlesModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-4.5F, -8.5F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.25F))
                        .texOffs(0, 50).addBox(-4.0F, -9.0F, 3.0F, 8.0F, 12.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.15F))
                        .texOffs(0, 32).addBox(-5.0F, 8.5F, -2.5F, 10.0F, 9.0F, 5.0F)
                        .texOffs(36, 34).addBox(-1.0F, 0.0F, -2.7F, 2.0F, 17.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                        .texOffs(24, 46).addBox(-4.0F, 8.0F, -2.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, 0.18F, 0.0F, 0.12F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
                        .texOffs(30, 46).addBox(2.0F, 8.0F, -2.5F, 2.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, 0.18F, 0.0F, -0.12F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(-1.9F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int visualState = entity.getVisualState();
        if (entity.getEncounterState() == AnnettaKnowlesEntity.EncounterState.COWERING) {
            this.body.xRot = 0.35F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) + 0.22F;
            this.rightArm.xRot = -0.65F;
            this.leftArm.xRot = -0.55F;
            this.rightArm.zRot = 0.18F;
            this.leftArm.zRot = -0.18F;
            this.rightLeg.xRot *= 0.35F;
            this.leftLeg.xRot *= 0.35F;
        } else if (entity.getEncounterState() == AnnettaKnowlesEntity.EncounterState.CURED_SUPPORT
                || visualState == AnnettaKnowlesEntity.VISUAL_CURED_SUPPORT) {
            float pulse = (float) Math.sin(ageInTicks * 0.25F) * 0.08F;
            this.body.xRot = 0.08F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) - 0.08F;
            this.rightArm.xRot = -0.75F + pulse;
            this.leftArm.xRot = -0.75F - pulse;
            this.rightArm.zRot = 0.42F;
            this.leftArm.zRot = -0.42F;
        } else if (visualState == AnnettaKnowlesEntity.VISUAL_SILVER_AURA) {
            this.body.xRot = -0.08F;
            this.rightArm.xRot = -1.35F;
            this.leftArm.xRot = -1.35F;
            this.rightArm.zRot = 0.28F;
            this.leftArm.zRot = -0.28F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) - 0.12F;
        } else if (visualState == AnnettaKnowlesEntity.VISUAL_VIAL_THROW) {
            this.rightArm.xRot = -1.7F;
            this.rightArm.yRot = -0.35F;
            this.leftArm.xRot = -0.28F;
            this.body.yRot = -0.12F;
        } else if (visualState == AnnettaKnowlesEntity.VISUAL_KERATIN_SLASH) {
            float snap = (float) Math.sin(ageInTicks * 0.7F) * 0.12F;
            this.body.xRot = 0.18F;
            this.rightArm.xRot = -0.35F + snap;
            this.leftArm.xRot = -0.35F - snap;
            this.rightArm.zRot = 1.15F;
            this.leftArm.zRot = -1.15F;
        }
    }
}
