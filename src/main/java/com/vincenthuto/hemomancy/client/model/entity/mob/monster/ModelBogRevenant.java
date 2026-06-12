package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BogRevenantEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ModelBogRevenant extends HierarchicalModel<BogRevenantEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("modelbogrevenant"),
            "main");

    private final ModelPart root;
    private final ModelPart Head;
    private final ModelPart crystal2;
    private final ModelPart Body;
    private final ModelPart crystal;
    private final ModelPart RightArm;
    private final ModelPart LeftArm;
    private final ModelPart RightLeg;
    private final ModelPart LeftLeg;

    public ModelBogRevenant(ModelPart root) {
        this.root = root.getChild("root");
        this.Head = this.root.getChild("Head");
        this.crystal2 = this.Head.getChild("crystal2");
        this.Body = this.root.getChild("Body");
        this.crystal = this.Body.getChild("crystal");
        this.RightArm = this.root.getChild("RightArm");
        this.LeftArm = this.root.getChild("LeftArm");
        this.RightLeg = this.root.getChild("RightLeg");
        this.LeftLeg = this.root.getChild("LeftLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Head = root.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -6.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition crystal2 = Head.addOrReplaceChild("crystal2", CubeListBuilder.create().texOffs(51, 31).addBox(0.875F, -1.9492F, -3.1641F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(51, 43).addBox(-2.875F, -1.9492F, -3.1641F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(54, 66).addBox(2.75F, -0.3555F, -2.2266F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(63, 22).addBox(-2.5625F, -0.6055F, -3.3516F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(45, 55).addBox(1.8125F, -0.8867F, -4.1016F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(33, 0).addBox(-0.0625F, -1.8867F, -5.9766F, 1.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(17, 52).addBox(-1.9375F, -1.8867F, -2.2266F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 51).addBox(-1.0F, -2.9492F, -4.1016F, 1.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9844F, -6.6262F, -2.3468F, -1.2585F, 0.1586F, -0.4549F));

        PartDefinition Body = root.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 17).addBox(-4.1667F, -5.7667F, -2.8333F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1667F, -18.2333F, -0.1667F, 0.1309F, 0.0F, 0.0F));

        PartDefinition crystal = Body.addOrReplaceChild("crystal", CubeListBuilder.create().texOffs(62, 55).addBox(-2.2656F, -4.7812F, -2.5F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(63, 11).addBox(-2.2656F, -4.7812F, 2.5F, 5.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 64).addBox(-0.1406F, -4.0312F, -5.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(17, 63).addBox(-1.1406F, -1.5312F, 0.75F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(67, 66).addBox(-0.1406F, -1.5312F, 3.75F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(45, 65).addBox(-1.5156F, -3.5312F, -3.75F, 3.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(34, 52).addBox(-2.5156F, -6.0312F, -1.25F, 4.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(-2.5156F, -6.0312F, 1.25F, 4.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(25, 17).addBox(-1.5156F, -4.0312F, -5.75F, 2.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(56, 0).addBox(-3.2656F, -3.5312F, 0.0F, 6.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.151F, -0.3929F, -0.1802F, -1.2217F, 0.0F, 0.0F));

        PartDefinition RightArm = root.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(0, 34).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -22.0F, -1.75F));

        PartDefinition LeftArm = root.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(17, 35).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, -22.0F, -1.75F));

        PartDefinition RightLeg = root.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(34, 35).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, -12.0F, 0.0F));

        PartDefinition LeftLeg = root.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(46, 14).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, -12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }


    @Override
    public void setupAnim(BogRevenantEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks,
                          float pNetHeadYaw, float pHeadPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);

        this.Head.yRot += pNetHeadYaw * Mth.DEG_TO_RAD;
        this.Head.xRot += pHeadPitch * Mth.DEG_TO_RAD;

        this.RightArm.xRot = Mth.cos(pLimbSwing * 0.6662F + Mth.PI) * 1.15F * pLimbSwingAmount - 0.65F;
        this.LeftArm.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.15F * pLimbSwingAmount - 0.65F;
        this.RightLeg.xRot = Mth.cos(pLimbSwing * 0.6662F) * 1.2F * pLimbSwingAmount;
        this.LeftLeg.xRot = Mth.cos(pLimbSwing * 0.6662F + Mth.PI) * 1.2F * pLimbSwingAmount;

        this.RightArm.yRot = -0.08F;
        this.LeftArm.yRot = 0.08F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
        root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }

}