package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class VelorumModel<T extends VelorumEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("velorum"), "main");

    public VelorumModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
                        .texOffs(32, 0).addBox(-5.0F, -9.0F, -4.5F, 10.0F, 10.0F, 9.0F, new CubeDeformation(0.15F)),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-3.5F, 0.0F, -2.0F, 7.0F, 13.0F, 4.0F)
                        .texOffs(0, 34).addBox(-7.0F, -1.0F, -2.8F, 14.0F, 19.0F, 6.0F, new CubeDeformation(0.05F))
                        .texOffs(42, 34).addBox(-6.0F, 4.0F, -3.2F, 12.0F, 2.0F, 1.0F)
                        .texOffs(42, 38).addBox(-6.0F, 9.0F, -3.2F, 12.0F, 2.0F, 1.0F),
                PartPose.offset(0.0F, -1.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 3.0F, 13.0F, 4.0F),
                PartPose.offsetAndRotation(-4.5F, 1.0F, 0.0F, -0.45F, 0.0F, 0.45F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(40, 16).mirror().addBox(0.0F, -2.0F, -2.0F, 3.0F, 13.0F, 4.0F),
                PartPose.offsetAndRotation(4.5F, 1.0F, 0.0F, -0.45F, 0.0F, -0.45F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F),
                PartPose.offset(-1.8F, 11.0F, 0.0F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 13.0F, 4.0F),
                PartPose.offset(1.8F, 11.0F, 0.0F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int visualState = entity.getVisualState();
        if (entity.isMartyrdom()) {
            this.body.xRot = -0.06F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) - 0.1F;
            this.rightArm.xRot = -0.85F;
            this.leftArm.xRot = -0.85F;
            this.rightArm.zRot = 0.18F;
            this.leftArm.zRot = -0.18F;
        }
        if (visualState == VelorumEntity.VISUAL_FROST_NOVA) {
            this.body.xRot = 0.08F;
            this.rightArm.xRot = -1.1F;
            this.leftArm.xRot = -1.1F;
            this.rightArm.zRot = 0.55F;
            this.leftArm.zRot = -0.55F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) - 0.14F;
        } else if (visualState == VelorumEntity.VISUAL_VEIL) {
            this.body.xRot = 0.28F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) + 0.32F;
            this.rightArm.xRot = -0.35F;
            this.leftArm.xRot = -0.35F;
            this.rightArm.zRot = -0.1F;
            this.leftArm.zRot = 0.1F;
        } else if (visualState == VelorumEntity.VISUAL_SILENCE_DRAIN) {
            float pulse = (float) Math.sin(ageInTicks * 0.65F) * 0.09F;
            this.body.xRot = -0.12F;
            this.rightArm.xRot = -1.35F + pulse;
            this.leftArm.xRot = -1.35F - pulse;
            this.rightArm.zRot = 0.7F;
            this.leftArm.zRot = -0.7F;
        }
    }
}
