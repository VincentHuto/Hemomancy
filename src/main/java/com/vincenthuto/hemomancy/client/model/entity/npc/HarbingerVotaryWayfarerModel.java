package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVotaryWayfarerEntity;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class HarbingerVotaryWayfarerModel<T extends HarbingerVotaryWayfarerEntity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("harbinger_votary_wayfarer"), "main");

    private final ModelPart whole;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart gourd;
    private final ModelPart rope4;
    private final ModelPart bone5;
    private final ModelPart bone6;
    private final ModelPart ClothFrontR4;
    private final ModelPart ClothFrontR1;
    private final ModelPart ClothFrontR2;
    private final ModelPart ClothFrontR3;
    private final ModelPart ClothFrontL1;
    private final ModelPart ClothFrontL2;
    private final ModelPart ClothFrontL3;
    private final ModelPart chestPouch;
    private final ModelPart device;
    private final ModelPart backpack;
    private final ModelPart bag;
    private final ModelPart compass;
    private final ModelPart strap;
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

    public HarbingerVotaryWayfarerModel(ModelPart root) {
        this.whole = root.getChild("whole");
        this.head = this.whole.getChild("head");
        this.body = this.whole.getChild("body");
        this.gourd = this.body.getChild("gourd");
        this.rope4 = this.gourd.getChild("rope4");
        this.bone5 = this.rope4.getChild("bone5");
        this.bone6 = this.rope4.getChild("bone6");
        this.ClothFrontR4 = this.body.getChild("ClothFrontR4");
        this.ClothFrontR1 = this.ClothFrontR4.getChild("ClothFrontR1");
        this.ClothFrontR2 = this.ClothFrontR4.getChild("ClothFrontR2");
        this.ClothFrontR3 = this.ClothFrontR4.getChild("ClothFrontR3");
        this.ClothFrontL1 = this.ClothFrontR4.getChild("ClothFrontL1");
        this.ClothFrontL2 = this.ClothFrontR4.getChild("ClothFrontL2");
        this.ClothFrontL3 = this.ClothFrontR4.getChild("ClothFrontL3");
        this.chestPouch = this.body.getChild("chestPouch");
        this.device = this.chestPouch.getChild("device");
        this.backpack = this.body.getChild("backpack");
        this.bag = this.backpack.getChild("bag");
        this.compass = this.bag.getChild("compass");
        this.strap = this.backpack.getChild("strap");
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

        PartDefinition head = whole.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 15).addBox(-4.0F, -8.0F, -4.5F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.1F, 0.5F));

        PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(31, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(58, 0).addBox(3.5998F, 0.5F, -2.5F, 1.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(19, 65).addBox(-4.5423F, 0.5F, -2.5F, 1.0F, 12.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0F, -0.5F, -4.4F, 10.0F, 5.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(0, 32).addBox(-4.0F, 4.5F, -3.5F, 8.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(56, 18).addBox(-4.1F, 0.5F, 1.4F, 8.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition gourd = body.addOrReplaceChild("gourd", CubeListBuilder.create(), PartPose.offsetAndRotation(5.25F, 12.5722F, 0.65F, 0.7418F, 0.0F, 0.0F));

        PartDefinition rope4 = gourd.addOrReplaceChild("rope4", CubeListBuilder.create(), PartPose.offset(-0.1F, -0.8389F, 0.0333F));

        PartDefinition bone5 = rope4.addOrReplaceChild("bone5", CubeListBuilder.create(), PartPose.offsetAndRotation(1.3F, -0.0333F, 1.0667F, 0.0F, 0.3927F, 0.0F));

        PartDefinition bone6 = rope4.addOrReplaceChild("bone6", CubeListBuilder.create(), PartPose.offsetAndRotation(1.3F, -0.0333F, -1.0333F, -0.0319F, 0.0064F, 0.0646F));

        PartDefinition ClothFrontR4 = body.addOrReplaceChild("ClothFrontR4", CubeListBuilder.create(), PartPose.offset(-0.2F, 12.2F, -5.5F));

        PartDefinition ClothFrontR1 = ClothFrontR4.addOrReplaceChild("ClothFrontR1", CubeListBuilder.create().texOffs(74, 47).addBox(-3.8F, 0.0944F, 1.0154F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 1.0F, -0.1047F, 0.0F, 0.0F));

        PartDefinition ClothFrontR2 = ClothFrontR4.addOrReplaceChild("ClothFrontR2", CubeListBuilder.create().texOffs(13, 66).addBox(-0.8F, 7.9103F, 1.9943F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 1.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition ClothFrontR3 = ClothFrontR4.addOrReplaceChild("ClothFrontR3", CubeListBuilder.create().texOffs(9, 77).addBox(-3.8F, 7.9103F, 1.9943F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 0.0F, 1.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition ClothFrontL1 = ClothFrontR4.addOrReplaceChild("ClothFrontL1", CubeListBuilder.create().texOffs(74, 57).addBox(-3.8F, 0.0944F, 1.0154F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.1047F, 0.0F, 0.0F));

        PartDefinition ClothFrontL2 = ClothFrontR4.addOrReplaceChild("ClothFrontL2", CubeListBuilder.create().texOffs(27, 83).addBox(-3.8F, 7.9103F, 1.9943F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition ClothFrontL3 = ClothFrontR4.addOrReplaceChild("ClothFrontL3", CubeListBuilder.create().texOffs(78, 79).addBox(-2.8F, 7.9103F, 1.9943F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, -0.2269F, 0.0F, 0.0F));

        PartDefinition chestPouch = body.addOrReplaceChild("chestPouch", CubeListBuilder.create().texOffs(84, 0).addBox(-1.5F, -3.25F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.25F, 9.0F, -3.0F, 0.0F, 0.1745F, 0.0F));

        PartDefinition device = chestPouch.addOrReplaceChild("device", CubeListBuilder.create().texOffs(38, 95).addBox(-1.0F, -0.8F, -0.2F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -6.25F, -1.0F, 0.0F, 0.7418F, 0.0F));

        PartDefinition backpack = body.addOrReplaceChild("backpack", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition bag = backpack.addOrReplaceChild("bag", CubeListBuilder.create().texOffs(33, 18).addBox(-4.5F, -4.0F, -1.5F, 7.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(69, 76).addBox(-5.5F, -0.5F, -1.5F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 77).addBox(2.5F, -0.5F, -1.5F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(75, 28).addBox(-3.5F, -5.0F, -1.5F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(45, 67).addBox(-4.0F, -1.25F, 1.5F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(78, 76).addBox(-3.5F, -2.25F, 1.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 4.0F, 3.5F, 0.0F, 0.4363F, 0.0F));

        PartDefinition compass = bag.addOrReplaceChild("compass", CubeListBuilder.create().texOffs(60, 91).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -2.75F, 1.0F, 0.0F, 0.6109F, 0.0F));

        PartDefinition strap = backpack.addOrReplaceChild("strap", CubeListBuilder.create().texOffs(39, 0).addBox(-1.6F, -2.0F, -5.25F, 1.0F, 9.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 48).addBox(-10.85F, -2.0F, -5.25F, 1.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(5.75F, 2.0F, 0.5F));

        PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(), PartPose.offset(0.0F, 12.3F, 4.4F));

        PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(), PartPose.offset(4.0F, 0.0F, -1.0F));

        PartDefinition ClothBackR1 = ClothBack1.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(74, 47).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBackL1 = ClothBack1.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(45, 75).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

        PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(), PartPose.offset(-2.0F, 8.0F, 0.0F));

        PartDefinition ClothBackR2 = ClothBack2.addOrReplaceChild("ClothBackR2", CubeListBuilder.create().texOffs(13, 66).addBox(-1.0F, 7.6966F, -2.96F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackR3 = ClothBack2.addOrReplaceChild("ClothBackR3", CubeListBuilder.create().texOffs(9, 77).addBox(-4.0F, 7.6966F, -2.96F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackL2 = ClothBack2.addOrReplaceChild("ClothBackL2", CubeListBuilder.create().texOffs(13, 66).addBox(-4.0F, 7.6966F, -2.96F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition ClothBackL3 = ClothBack2.addOrReplaceChild("ClothBackL3", CubeListBuilder.create().texOffs(32, 82).addBox(-3.0F, 7.6966F, -2.96F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

        PartDefinition SideclothR = body.addOrReplaceChild("SideclothR", CubeListBuilder.create(), PartPose.offset(-3.8F, 12.25F, 0.0F));

        PartDefinition SideclothR4 = SideclothR.addOrReplaceChild("SideclothR4", CubeListBuilder.create().texOffs(0, 66).mirror().addBox(-1.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

        PartDefinition SideclothR5 = SideclothR4.addOrReplaceChild("SideclothR5", CubeListBuilder.create().texOffs(71, 0).mirror().addBox(-0.1341F, -0.4073F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-0.9076F, 5.4763F, 0.0F));

        PartDefinition SideclothR6 = SideclothR5.addOrReplaceChild("SideclothR6", CubeListBuilder.create().texOffs(60, 67).mirror().addBox(-0.6125F, -0.0086F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, 0.5236F));

        PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(), PartPose.offset(3.8F, 12.25F, 0.0F));

        PartDefinition SideclothR1 = SideclothL.addOrReplaceChild("SideclothR1", CubeListBuilder.create().texOffs(32, 65).addBox(0.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

        PartDefinition SideclothR2 = SideclothR1.addOrReplaceChild("SideclothR2", CubeListBuilder.create().texOffs(60, 67).addBox(-0.8659F, -0.4073F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.9076F, 5.4763F, 0.0F));

        PartDefinition SideclothR3 = SideclothR2.addOrReplaceChild("SideclothR3", CubeListBuilder.create().texOffs(73, 67).addBox(-0.3875F, -0.0086F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, -0.5236F));

        PartDefinition leftArm = whole.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(19, 49).addBox(-0.5F, -1.75F, -2.5F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(74, 40).addBox(0.0F, 8.25F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(32, 76).addBox(-0.5F, 5.25F, 2.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(56, 82).addBox(0.0F, 3.25F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition body2 = leftArm.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(-5.0F, -2.0F, 1.0F));

        PartDefinition rightArm = whole.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(38, 49).addBox(-3.5F, -1.75F, -2.5F, 4.0F, 10.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(74, 33).addBox(-3.0F, 8.25F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(56, 76).addBox(-3.5F, 5.25F, 2.5F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(18, 83).addBox(-3.0F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leftLeg = whole.addOrReplaceChild("leftLeg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition right_leg_0_16_c9b3f04f_r1 = leftLeg.addOrReplaceChild("right_leg_0_16_c9b3f04f_r1", CubeListBuilder.create().texOffs(57, 33).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        PartDefinition rightLeg = whole.addOrReplaceChild("rightLeg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition left_leg_0_16_90ee26c7_r1 = rightLeg.addOrReplaceChild("left_leg_0_16_90ee26c7_r1", CubeListBuilder.create().texOffs(57, 50).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        whole.render(poseStack, buffer, packedLight, packedOverlay);

    }
    protected float rotlerpRad(float angle, float maxAngle, float mul) {
        float f = (mul - maxAngle) % ((float)Math.PI * 2F);
        if (f < -(float)Math.PI) {
            f += ((float)Math.PI * 2F);
        }

        if (f >= (float)Math.PI) {
            f -= ((float)Math.PI * 2F);
        }

        return maxAngle + angle * f;
    }

    @Override
    public void setupAnim(HarbingerVotaryWayfarerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
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