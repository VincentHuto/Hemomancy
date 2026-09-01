package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.unstained.UnstainedGuardianEntity;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class UnstainedGuardianModel<T extends UnstainedGuardianEntity> extends EntityModel<T> {

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("unstained_guardian"), "main");

    private final ModelPart whole;
    private final ModelPart head;
    private final ModelPart Hood1;
    private final ModelPart mask;
    private final ModelPart canister;
    private final ModelPart body;
    private final ModelPart SideclothL;
    private final ModelPart SideclothR1;
    private final ModelPart SideclothR2;
    private final ModelPart SideclothR;
    private final ModelPart SideclothR4;
    private final ModelPart SideclothR5;
    private final ModelPart ClothBack;
    private final ModelPart ClothBack1;
    private final ModelPart ClothBackR1;
    private final ModelPart ClothBackL1;
    private final ModelPart ClothBack2;
    private final ModelPart leftArm;
    private final ModelPart body2;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart rightArm;
    private final ModelPart body3;

    public UnstainedGuardianModel(ModelPart root) {
        this.whole = root.getChild("whole");
        this.head = this.whole.getChild("head");
        this.Hood1 = this.head.getChild("Hood1");
        this.mask = this.Hood1.getChild("mask");
        this.canister = this.mask.getChild("canister");
        this.body = this.whole.getChild("body");
        this.SideclothL = this.body.getChild("SideclothL");
        this.SideclothR1 = this.SideclothL.getChild("SideclothR1");
        this.SideclothR2 = this.SideclothR1.getChild("SideclothR2");
        this.SideclothR = this.body.getChild("SideclothR");
        this.SideclothR4 = this.SideclothR.getChild("SideclothR4");
        this.SideclothR5 = this.SideclothR4.getChild("SideclothR5");
        this.ClothBack = this.body.getChild("ClothBack");
        this.ClothBack1 = this.ClothBack.getChild("ClothBack1");
        this.ClothBackR1 = this.ClothBack1.getChild("ClothBackR1");
        this.ClothBackL1 = this.ClothBack1.getChild("ClothBackL1");
        this.ClothBack2 = this.ClothBack1.getChild("ClothBack2");
        this.leftArm = this.whole.getChild("leftArm");
        this.body2 = this.leftArm.getChild("body2");
        this.leftLeg = this.whole.getChild("leftLeg");
        this.rightLeg = this.whole.getChild("rightLeg");
        this.rightArm = this.whole.getChild("rightArm");
        this.body3 = this.rightArm.getChild("body3");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = whole.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -0.5F, -4.4F, 10.0F, 5.0F, 9.0F, new CubeDeformation(-0.1F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Hood1 = head.addOrReplaceChild("Hood1", CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -8.0F, -3.6F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 51).addBox(4.0F, -5.0F, -4.5F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(42, 57).addBox(3.5F, -8.0F, -4.5F, 1.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(21, 57).addBox(-5.0F, -5.0F, -4.6F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(58, 10).addBox(-4.5F, -8.0F, -4.5F, 1.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(40, 1).addBox(-3.5F, -9.0F, -3.6F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(46, 7).addBox(-3.5F, -8.5F, -4.6F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition mask = Hood1.addOrReplaceChild("mask", CubeListBuilder.create(), PartPose.offset(2.5F, 0.0F, -7.0F));

        PartDefinition canister = mask.addOrReplaceChild("canister", CubeListBuilder.create().texOffs(79, 32).addBox(-1.5F, -1.5F, -1.25F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -2.0F, 3.15F, 0.3491F, 0.0F, 0.0F));

        PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(33, 15).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-4.0F, 0.0F, -2.75F, 8.0F, 12.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(29, 48).addBox(-4.5F, 10.4F, -2.75F, 9.0F, 2.0F, 6.0F, new CubeDeformation(0.1F))
                .texOffs(42, 71).addBox(3.3498F, -0.6865F, -2.3F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 66).addBox(-4.2998F, -0.6865F, -2.3F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(60, 24).addBox(-4.1F, -0.5F, 1.4F, 8.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(), PartPose.offset(3.8F, 12.25F, -0.25F));

        PartDefinition SideclothR1 = SideclothL.addOrReplaceChild("SideclothR1", CubeListBuilder.create().texOffs(79, 54).addBox(0.1635F, -0.9235F, -2.5F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

        PartDefinition SideclothR2 = SideclothR1.addOrReplaceChild("SideclothR2", CubeListBuilder.create().texOffs(78, 9).addBox(0.0F, 0.0F, -3.0F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.1635F, 4.0765F, 0.5F));

        PartDefinition SideclothR = body.addOrReplaceChild("SideclothR", CubeListBuilder.create(), PartPose.offset(-3.8F, 12.25F, -0.25F));

        PartDefinition SideclothR4 = SideclothR.addOrReplaceChild("SideclothR4", CubeListBuilder.create().texOffs(12, 81).addBox(-1.1635F, -0.9235F, -2.5F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition SideclothR5 = SideclothR4.addOrReplaceChild("SideclothR5", CubeListBuilder.create().texOffs(78, 20).addBox(-1.0F, 0.0F, -3.0F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.1635F, 4.0765F, 0.5F));

        PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(), PartPose.offset(0.0F, 11.3F, 4.9F));

        PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(), PartPose.offset(4.0F, 0.0F, -0.75F));

        PartDefinition ClothBackR1 = ClothBack1.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(26, 82).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBackL1 = ClothBack1.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(0, 85).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 10.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(), PartPose.offset(-2.0F, 8.0F, 0.0F));

        PartDefinition leftArm = whole.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(55, 73).addBox(-0.75F, 2.5F, -2.5F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderR_16_45_0eedefc6_r1 = leftArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r1", CubeListBuilder.create().texOffs(77, 39).addBox(-1.5F, 0.5F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -0.5F, 0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderR_16_45_0eedefc6_r2 = leftArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r2", CubeListBuilder.create().texOffs(70, 0).addBox(-1.5F, -1.5F, -3.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.125F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.5F, 0.0F, 0.0F, 0.0F));

        PartDefinition body2 = leftArm.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(-5.0F, -2.0F, 1.0F));

        PartDefinition leftLeg = whole.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(60, 39).addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(48, 10).addBox(-1.4F, 5.0F, -2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(59, 10).addBox(-0.9F, 4.0F, -2.75F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition rightLeg = whole.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(63, 56).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(39, 10).addBox(-1.6F, 5.0F, -2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(39, 10).addBox(-1.1F, 4.0F, -2.75F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition rightArm = whole.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(72, 73).addBox(-3.25F, 2.5F, -2.5F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderR_16_45_0eedefc6_r3 = rightArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r3", CubeListBuilder.create().texOffs(77, 47).addBox(-2.5F, 0.5F, -2.5F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, -0.5F, 0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderR_16_45_0eedefc6_r4 = rightArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r4", CubeListBuilder.create().texOffs(13, 72).addBox(-3.5F, -1.5F, -3.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.125F)), PartPose.offsetAndRotation(0.0F, 1.0F, 0.5F, 0.0F, 0.0F, 0.0F));

        PartDefinition body3 = rightArm.addOrReplaceChild("body3", CubeListBuilder.create(), PartPose.offset(5.0F, -2.0F, 1.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }


    @Override
    public void setupAnim(UnstainedGuardianEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
        this.head.xRot = headPitch * ((float) Math.PI / 180F);
        this.body.yRot = 0.0F;
        this.rightArm.z = 0.0F;
        this.rightArm.x = -5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        float f = 1.0F;
        this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
        this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
        this.rightArm.zRot = 0.0F;
        this.leftArm.zRot = 0.0F;
        this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
        this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / f;
        this.rightLeg.yRot = 0.005F;
        this.leftLeg.yRot = -0.005F;
        this.rightLeg.zRot = 0.005F;
        this.leftLeg.zRot = -0.005F;
        float frame = entity.tickCount + HLClientUtils.getPartialTicks();
        float idleWave = Mth.sin(frame * 0.067F);
        float walkWave = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount;

        // Back cloth panels - x axis flutter
        this.ClothBackR1.xRot = 0.1047F + idleWave * 0.03F + walkWave * 0.07F;
        this.ClothBackL1.xRot = 0.1047F + Mth.sin(frame * 0.067F + 0.3F) * 0.03F + walkWave * 0.07F;

        // Side cloth strips - z axis sway
        float sideSway = Mth.sin(frame * 0.09F) * 0.05F + walkWave * 0.05F;
        this.SideclothL.zRot = -0.08F + sideSway;
        this.SideclothR.zRot = 0.08F - sideSway;

        this.SideclothR1.zRot = -0.1222F + Mth.sin(frame * 0.067F + 0.2F) * 0.03F + walkWave * 0.04F;
        this.SideclothR2.zRot = -0.2967F + Mth.sin(frame * 0.067F + 0.8F) * 0.035F + walkWave * 0.05F;
        this.SideclothR4.zRot = 0.1222F + Mth.sin(frame * 0.067F + 0.5F) * 0.03F + walkWave * 0.04F;
        this.SideclothR5.zRot = 0.2967F + Mth.sin(frame * 0.067F + 1.0F) * 0.035F + walkWave * 0.05F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        whole.render(poseStack, buffer, packedLight, packedOverlay);

    }

}
