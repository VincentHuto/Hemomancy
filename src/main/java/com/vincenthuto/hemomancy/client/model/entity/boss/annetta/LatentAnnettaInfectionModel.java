package com.vincenthuto.hemomancy.client.model.entity.boss.annetta;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.annetta.LatentAnnettaInfectionEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class LatentAnnettaInfectionModel<T extends LatentAnnettaInfectionEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(Hemomancy.rloc("latent_annetta_infection"), "main");

    public LatentAnnettaInfectionModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -7.0F, -4.5F, 10.0F, 7.0F, 9.0F)
                        .texOffs(38, 0).addBox(-2.0F, -10.0F, -1.0F, 4.0F, 4.0F, 2.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

        root.addOrReplaceChild("body",
                CubeListBuilder.create()
                        .texOffs(16, 16).addBox(-6.5F, -1.0F, -3.5F, 13.0F, 15.0F, 7.0F, new CubeDeformation(0.2F))
                        .texOffs(0, 40).addBox(-4.0F, 10.0F, -4.5F, 8.0F, 8.0F, 9.0F)
                        .texOffs(42, 40).addBox(-8.0F, 3.0F, 2.5F, 5.0F, 10.0F, 2.0F)
                        .texOffs(54, 40).addBox(3.0F, 1.0F, 2.5F, 5.0F, 12.0F, 2.0F),
                PartPose.offset(0.0F, 1.0F, 0.0F));

        root.addOrReplaceChild("right_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).addBox(-2.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                        .texOffs(24, 38).addBox(-3.0F, 11.0F, -2.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(-7.0F, 3.0F, 0.0F, 0.3F, 0.0F, 0.35F));
        root.addOrReplaceChild("left_arm",
                CubeListBuilder.create()
                        .texOffs(40, 16).mirror().addBox(-1.0F, -1.0F, -1.5F, 3.0F, 16.0F, 3.0F)
                        .texOffs(32, 38).addBox(1.0F, 11.0F, -2.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(7.0F, 2.0F, 0.0F, -0.15F, 0.0F, -0.45F));

        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offsetAndRotation(-2.8F, 13.0F, 0.0F, 0.0F, 0.0F, 0.14F));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F),
                PartPose.offsetAndRotation(2.8F, 13.0F, 0.0F, 0.0F, 0.0F, -0.14F));

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        int visualState = entity.getVisualState();
        if (visualState == LatentAnnettaInfectionEntity.VISUAL_INFECTION_BLOOM) {
            float sway = (float) Math.sin(ageInTicks * 0.35F) * 0.15F;
            this.body.xRot = -0.18F;
            this.body.zRot = sway;
            this.rightArm.xRot = -0.9F;
            this.leftArm.xRot = -0.9F;
            this.rightArm.zRot = 0.85F + sway;
            this.leftArm.zRot = -0.85F + sway;
        } else if (visualState == LatentAnnettaInfectionEntity.VISUAL_PRESSURE_SPIKE) {
            float tremor = (float) Math.sin(ageInTicks * 0.9F) * 0.18F;
            this.body.xRot = 0.28F;
            this.head.xRot = headPitch * ((float) Math.PI / 180F) + 0.18F;
            this.rightArm.xRot = -1.25F + tremor;
            this.leftArm.xRot = -1.25F - tremor;
            this.rightArm.zRot = 0.35F;
            this.leftArm.zRot = -0.35F;
        }
    }
}
