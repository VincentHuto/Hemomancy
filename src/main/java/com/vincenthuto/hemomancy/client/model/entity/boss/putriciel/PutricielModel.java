package com.vincenthuto.hemomancy.client.model.entity.boss.putriciel;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel.PutricielEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class PutricielModel<T extends PutricielEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("putriciel"), "main");

    public PutricielModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 8.0F, 9.0F)
                        .texOffs(36, 0).addBox(-6.0F, -10.0F, -1.0F, 12.0F, 2.0F, 2.0F)
                        .texOffs(0, 54).addBox(-5.0F, -2.0F, 3.5F, 10.0F, 5.0F, 2.0F),
                PartPose.offset(0.0F, -3.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-6.0F, -1.0F, -3.0F, 12.0F, 15.0F, 6.0F, new CubeDeformation(0.35F))
                        .texOffs(0, 38).addBox(-7.0F, 8.0F, -3.5F, 14.0F, 10.0F, 7.0F)
                        .texOffs(44, 38).addBox(-7.5F, 2.0F, 2.5F, 4.0F, 10.0F, 2.0F)
                        .texOffs(56, 38).addBox(3.5F, 4.0F, 2.5F, 4.0F, 8.0F, 2.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-4.0F, -2.0F, -2.5F, 5.0F, 15.0F, 5.0F),
                PartPose.offsetAndRotation(-6.5F, 0.0F, 0.0F, 0.25F, 0.0F, 0.22F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.5F, 5.0F, 15.0F, 5.0F),
                PartPose.offsetAndRotation(6.5F, 0.0F, 0.0F, 0.25F, 0.0F, -0.22F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.3F, 0.0F, -2.2F, 5.0F, 14.0F, 5.0F),
                PartPose.offset(-2.5F, 12.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.7F, 0.0F, -2.2F, 5.0F, 14.0F, 5.0F),
                PartPose.offset(2.5F, 12.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity.isAbsolved()) {
            float intensity = Math.min(1.0F, entity.getAbsolutionCount() / 5.0F);
            float pulse = (float) Math.sin(ageInTicks * 0.35F) * 0.08F;
            this.body.xRot = 0.22F + intensity * 0.12F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) + 0.16F;
            this.rightArm.xRot = -0.85F + pulse;
            this.leftArm.xRot = -0.85F - pulse;
            this.rightArm.zRot = 0.75F + intensity * 0.25F;
            this.leftArm.zRot = -0.75F - intensity * 0.25F;
        } else if (entity.getVisualState() == PutricielEntity.VISUAL_ROT_NOVA) {
            float heave = (float) Math.sin(ageInTicks * 0.5F) * 0.1F;
            this.body.xRot = -0.16F;
            this.rightArm.xRot = -1.05F + heave;
            this.leftArm.xRot = -1.05F - heave;
            this.rightArm.zRot = 1.05F;
            this.leftArm.zRot = -1.05F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) - 0.12F;
        } else if (entity.getVisualState() == PutricielEntity.VISUAL_ABSOLUTION_HIT) {
            this.body.xRot = 0.35F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) + 0.24F;
            this.rightArm.xRot = -0.35F;
            this.leftArm.xRot = -0.35F;
        }
    }
}
