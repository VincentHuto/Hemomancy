package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerHermitEntity;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

/**
 * Humanoid model for the Harbinger Hermit — a robed, hooded figure
 * with a hunched posture and long, flowing robes suggesting age and wisdom.
 */
public class HarbingerHermitModel<T extends HarbingerHermitEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("harbinger_hermit"), "main");

    private final ModelPart whole;
    private final ModelPart head;
    private final ModelPart Hood1;
    private final ModelPart Hood2;
    private final ModelPart Hood3;
    private final ModelPart Hood4;
    private final ModelPart body;
    private final ModelPart gourd;
    private final ModelPart rope;
    private final ModelPart bone;
    private final ModelPart bone2;
    private final ModelPart ClothBack;
    private final ModelPart ClothBack1;
    private final ModelPart ClothBackR1;
    private final ModelPart ClothBackL1;
    private final ModelPart ClothBack2;
    private final ModelPart ClothBackR2;
    private final ModelPart ClothBackR3;
    private final ModelPart ClothBackL2;
    private final ModelPart ClothBackL3;
    private final ModelPart SideclothR;
    private final ModelPart SideclothR4;
    private final ModelPart SideclothR5;
    private final ModelPart SideclothR6;
    private final ModelPart SideclothL;
    private final ModelPart SideclothR1;
    private final ModelPart SideclothR2;
    private final ModelPart SideclothR3;
    private final ModelPart leftArm;
    private final ModelPart body2;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public HarbingerHermitModel(ModelPart root) {
        this.whole = root.getChild("whole");
        this.head = this.whole.getChild("head");
        this.Hood1 = this.head.getChild("Hood1");
        this.Hood2 = this.head.getChild("Hood2");
        this.Hood3 = this.head.getChild("Hood3");
        this.Hood4 = this.head.getChild("Hood4");
        this.body = this.whole.getChild("body");
        this.gourd = this.body.getChild("gourd");
        this.rope = this.gourd.getChild("rope");
        this.bone = this.rope.getChild("bone");
        this.bone2 = this.rope.getChild("bone2");
        this.ClothBack = this.body.getChild("ClothBack");
        this.ClothBack1 = this.ClothBack.getChild("ClothBack1");
        this.ClothBackR1 = this.ClothBack1.getChild("ClothBackR1");
        this.ClothBackL1 = this.ClothBack1.getChild("ClothBackL1");
        this.ClothBack2 = this.ClothBack1.getChild("ClothBack2");
        this.ClothBackR2 = this.ClothBack2.getChild("ClothBackR2");
        this.ClothBackR3 = this.ClothBack2.getChild("ClothBackR3");
        this.ClothBackL2 = this.ClothBack2.getChild("ClothBackL2");
        this.ClothBackL3 = this.ClothBack2.getChild("ClothBackL3");
        this.SideclothR = this.body.getChild("SideclothR");
        this.SideclothR4 = this.SideclothR.getChild("SideclothR4");
        this.SideclothR5 = this.SideclothR4.getChild("SideclothR5");
        this.SideclothR6 = this.SideclothR5.getChild("SideclothR6");
        this.SideclothL = this.body.getChild("SideclothL");
        this.SideclothR1 = this.SideclothL.getChild("SideclothR1");
        this.SideclothR2 = this.SideclothR1.getChild("SideclothR2");
        this.SideclothR3 = this.SideclothR2.getChild("SideclothR3");
        this.leftArm = this.whole.getChild("leftArm");
        this.body2 = this.leftArm.getChild("body2");
        this.rightArm = this.whole.getChild("rightArm");
        this.leftLeg = this.whole.getChild("leftLeg");
        this.rightLeg = this.whole.getChild("rightLeg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = whole.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Hood1 = head.addOrReplaceChild("Hood1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -9.0F, -4.6F, 1.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(2, 2).addBox(-3.5F, -8.0F, -3.6F, 7.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(8, 1).addBox(3.5F, -8.0F, -4.6F, 1.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-4.5F, -9.0F, -4.6F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition Hood2 = head.addOrReplaceChild("Hood2", CubeListBuilder.create().texOffs(25, 19).addBox(-4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition Hood3 = head.addOrReplaceChild("Hood3", CubeListBuilder.create().texOffs(42, 45).addBox(-3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

        PartDefinition Hood4 = head.addOrReplaceChild("Hood4", CubeListBuilder.create().texOffs(38, 57).addBox(-3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));

        PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 73).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(3.3498F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 18).addBox(-4.2998F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(4, 93).addBox(-3.9F, -0.5F, 1.6F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 85).addBox(-4.1F, -0.5F, 1.4F, 8.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ClothchestR_108_38_cf815bb5_r1 = body.addOrReplaceChild("ClothchestR_108_38_cf815bb5_r1", CubeListBuilder.create().texOffs(37, 68).addBox(2.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(17, 36).addBox(-4.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.25F, 0.0F, 0.0F, 0.0F));

        PartDefinition gourd = body.addOrReplaceChild("gourd", CubeListBuilder.create().texOffs(87, 98).addBox(-3.0F, -5.4444F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(62, 105).addBox(-3.0F, -1.4444F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(106, 79).addBox(2.925F, -0.7194F, -2.625F, 1.0F, 7.0F, 5.0F, new CubeDeformation(-0.375F))
                .texOffs(79, 87).addBox(-3.575F, -0.7194F, -2.625F, 1.0F, 7.0F, 5.0F, new CubeDeformation(-0.375F))
                .texOffs(67, 88).addBox(-2.375F, -0.7194F, 2.575F, 5.0F, 7.0F, 1.0F, new CubeDeformation(-0.375F))
                .texOffs(93, 88).addBox(-2.375F, -0.7194F, -3.825F, 5.0F, 7.0F, 1.0F, new CubeDeformation(-0.375F))
                .texOffs(67, 71).addBox(-2.5F, -7.4444F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F))
                .texOffs(64, 96).addBox(-2.5F, 4.5556F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F))
                .texOffs(88, 71).addBox(-2.5F, -3.9444F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.25F, 6.5722F, 4.45F, -0.0282F, -1.0455F, 0.6876F));

        PartDefinition rope = gourd.addOrReplaceChild("rope", CubeListBuilder.create().texOffs(90, 59).addBox(2.85F, -1.0167F, -2.1167F, 0.0F, 2.0F, 4.0F, new CubeDeformation(0.05F)), PartPose.offset(-0.2F, -2.4278F, 0.0667F));

        PartDefinition bone = rope.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(94, 69).addBox(-8.6963F, -0.95F, -0.12F, 9.0F, 2.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(2.6F, -0.0667F, 2.1333F, 0.0F, 0.3927F, 0.0F));

        PartDefinition bone2 = rope.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(86, 60).addBox(-7.8145F, -0.9636F, -0.1809F, 8.0F, 2.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(2.6F, -0.0667F, -2.0667F, 0.0554F, 0.0064F, 0.0646F));

        PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(), PartPose.offset(0.0F, 12.3F, 4.4F));

        PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(), PartPose.offset(4.0F, 0.0F, -1.0F));

        PartDefinition ClothBackR1 = ClothBack1.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(26, 60).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBackL1 = ClothBack1.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(63, 42).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.0F, 8.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

        PartDefinition ClothBackR2 = ClothBack2.addOrReplaceChild("ClothBackR2", CubeListBuilder.create().texOffs(58, 68).addBox(-1.0F, 7.3522F, -2.8768F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackR3 = ClothBack2.addOrReplaceChild("ClothBackR3", CubeListBuilder.create().texOffs(37, 13).addBox(-4.0F, 7.3522F, -2.8768F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackL2 = ClothBack2.addOrReplaceChild("ClothBackL2", CubeListBuilder.create().texOffs(63, 68).addBox(-4.0F, 7.3522F, -2.8768F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackL3 = ClothBack2.addOrReplaceChild("ClothBackL3", CubeListBuilder.create().texOffs(63, 52).addBox(-3.0F, 7.3522F, -2.8768F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition SideclothR = body.addOrReplaceChild("SideclothR", CubeListBuilder.create(), PartPose.offset(-3.8F, 12.25F, 0.0F));

        PartDefinition SideclothR4 = SideclothR.addOrReplaceChild("SideclothR4", CubeListBuilder.create().texOffs(57, 57).mirror().addBox(-1.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition SideclothR5 = SideclothR4.addOrReplaceChild("SideclothR5", CubeListBuilder.create().texOffs(63, 24).mirror().addBox(-0.291F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, 0.2967F));

        PartDefinition SideclothR6 = SideclothR5.addOrReplaceChild("SideclothR6", CubeListBuilder.create().texOffs(63, 33).mirror().addBox(-0.866F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(), PartPose.offset(3.8F, 12.25F, 0.0F));

        PartDefinition SideclothR1 = SideclothL.addOrReplaceChild("SideclothR1", CubeListBuilder.create().texOffs(57, 57).addBox(0.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

        PartDefinition SideclothR2 = SideclothR1.addOrReplaceChild("SideclothR2", CubeListBuilder.create().texOffs(63, 24).addBox(-0.709F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, -0.2967F));

        PartDefinition SideclothR3 = SideclothR2.addOrReplaceChild("SideclothR3", CubeListBuilder.create().texOffs(63, 33).addBox(-0.134F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition leftArm = whole.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(38, 0).addBox(-0.5F, 2.5F, -2.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(48, 24).addBox(-0.5F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(58, 9).addBox(-0.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition ShoulderR_16_45_0eedefc6_r1 = leftArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r1", CubeListBuilder.create().texOffs(48, 13).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.125F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition body2 = leftArm.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(-5.0F, -2.0F, 1.0F));

        PartDefinition rightArm = whole.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(42, 32).addBox(-3.5F, 2.5F, -2.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 64).addBox(-3.5F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(67, 9).addBox(-2.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition ShoulderL_16_45_e24b0f90_r1 = rightArm.addOrReplaceChild("ShoulderL_16_45_e24b0f90_r1", CubeListBuilder.create().texOffs(17, 49).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.125F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition leftLeg = whole.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(25, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition rightLeg = whole.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(0, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        whole.render(poseStack, buffer, packedLight, packedOverlay);
    }

    protected float rotlerpRad(float angle, float maxAngle, float mul) {
        float f = (mul - maxAngle) % ((float) Math.PI * 2F);
        if (f < -(float) Math.PI) {
            f += ((float) Math.PI * 2F);
        }

        if (f >= (float) Math.PI) {
            f -= ((float) Math.PI * 2F);
        }

        return maxAngle + angle * f;
    }

    @Override
    public void setupAnim(HarbingerHermitEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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
        this.ClothBackR2.xRot = 0.2269F + Mth.sin(frame * 0.067F + 0.6F) * 0.035F + walkWave * 0.08F;
        this.ClothBackR3.xRot = 0.2269F + Mth.sin(frame * 0.067F + 1.0F) * 0.04F + walkWave * 0.10F;
        this.ClothBackL1.xRot = 0.1047F + Mth.sin(frame * 0.067F + 0.3F) * 0.03F + walkWave * 0.07F;
        this.ClothBackL2.xRot = 0.2269F + Mth.sin(frame * 0.067F + 0.9F) * 0.035F + walkWave * 0.08F;
        this.ClothBackL3.xRot = 0.2269F + Mth.sin(frame * 0.067F + 1.3F) * 0.04F + walkWave * 0.10F;

        // Side cloth strips - z axis sway
        float sideSway = Mth.sin(frame * 0.09F) * 0.05F + walkWave * 0.05F;
        this.SideclothL.zRot = -0.08F + sideSway;
        this.SideclothR.zRot = 0.08F - sideSway;

        this.SideclothR1.zRot = -0.1222F + Mth.sin(frame * 0.067F + 0.2F) * 0.03F + walkWave * 0.04F;
        this.SideclothR2.zRot = -0.2967F + Mth.sin(frame * 0.067F + 0.8F) * 0.035F + walkWave * 0.05F;
        this.SideclothR3.zRot = -0.5236F + Mth.sin(frame * 0.067F + 1.2F) * 0.04F + walkWave * 0.06F;
        this.SideclothR4.zRot = 0.1222F + Mth.sin(frame * 0.067F + 0.5F) * 0.03F + walkWave * 0.04F;
        this.SideclothR5.zRot = 0.2967F + Mth.sin(frame * 0.067F + 1.0F) * 0.035F + walkWave * 0.05F;
        this.SideclothR6.zRot = 0.5236F + Mth.sin(frame * 0.067F + 1.4F) * 0.04F + walkWave * 0.06F;
    }

}
