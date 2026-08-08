package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class VesperTheCrownedRefusalModel  extends EntityModel<VesperTheCrownedRefusalEntity> {
    private static final float DEFAULT_RIDER_Y = -19.0F;
    private static final float VULNERABLE_RIDER_Y = -12.0F;
    private static final float VULNERABLE_MOUNT_PITCH = 0.32F;
    private static final float DEFAULT_MOUNT_Y = -11.0F;
    private static final float DEFAULT_MOUNT_Z = 2.2F;

    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("vesper_crowned_refusal"), "main");
    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, -15.0F, -15.0F));

        PartDefinition upperBody = partdefinition.addOrReplaceChild("upperBody", CubeListBuilder.create().texOffs(196, 200).addBox(-9.5F, -16.0F, 3.2F, 22.0F, 23.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(176, 64).addBox(-4.5F, -7.0F, 2.2F, 12.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(48, 195).addBox(-2.5F, -13.0F, 14.2F, 8.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(217, 225).addBox(-3.5F, -11.0F, 16.2F, 1.0F, 6.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(52, 221).addBox(5.5F, -11.0F, 16.2F, 1.0F, 6.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(29, 141).addBox(-2.5F, -21.0F, 9.2F, 8.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(176, 103).addBox(-3.5F, -5.0F, 20.2F, 10.0F, 8.0F, 18.0F, new CubeDeformation(0.0F))
                .texOffs(60, 246).addBox(-5.5F, -5.0F, 16.2F, 2.0F, 7.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(32, 244).addBox(6.5F, -5.0F, 16.2F, 2.0F, 7.0F, 12.0F, new CubeDeformation(0.0F))
                .texOffs(0, 101).addBox(-3.5F, -16.0F, -2.8F, 9.0F, 17.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 141).addBox(-2.5F, 1.0F, -2.1F, 7.0F, 17.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(207, 169).addBox(-8.5F, -16.0F, 0.2F, 19.0F, 23.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(-3.5F, -27.0F, 2.2F, 10.0F, 26.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(136, 297).addBox(8.25F, -14.0F, -6.8F, 5.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(138, 313).addBox(8.25F, -16.0F, -3.8F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(138, 313).addBox(-10.0F, -16.0F, -3.8F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(136, 297).addBox(-10.0F, -14.0F, -6.8F, 5.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(166, 301).addBox(-9.1F, -10.0F, -5.8F, 4.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(166, 301).addBox(8.35F, -10.0F, -5.8F, 4.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.1F, -19.0F, -19.0F));

        PartDefinition hasturForm = upperBody.addOrReplaceChild("hasturForm", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head = hasturForm.addOrReplaceChild("head", CubeListBuilder.create().texOffs(191, 242).addBox(-2.5F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(144, 57).addBox(-2.5F, -10.0F, 4.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(144, 55).addBox(-2.5F, -10.0F, -5.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 178).addBox(-3.5F, -14.0F, -5.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(177, 167).addBox(5.5F, -14.0F, -5.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(176, 103).addBox(5.5F, -14.0F, 4.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(176, 64).addBox(-3.5F, -14.0F, 4.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(142, 20).addBox(-3.5F, -13.0F, -4.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(131, 20).addBox(5.5F, -13.0F, -4.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(134, 113).addBox(-3.5F, -13.0F, 3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(66, 103).addBox(5.5F, -13.0F, 3.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(143, 2).addBox(-2.5F, -13.0F, -5.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-2.5F, -13.0F, 4.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(54, 103).addBox(4.5F, -13.0F, -5.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 0).addBox(4.5F, -13.0F, 4.0F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(72, 230).addBox(-3.5F, -10.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(185, 103).addBox(-4.5F, -10.0F, -5.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(72, 141).addBox(6.5F, -10.0F, -5.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(185, 64).addBox(-4.5F, -10.0F, 2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(173, 54).addBox(6.5F, -10.0F, 2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(187, 186).addBox(-3.5F, -10.0F, -6.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(146, 154).addBox(-3.5F, -10.0F, 5.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(179, 0).addBox(3.5F, -10.0F, -6.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(16, 166).addBox(3.5F, -10.0F, 5.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(200, 90).addBox(5.5F, -10.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(0, 28).addBox(-3.5F, -9.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(202, 169).addBox(-3.5F, -8.0F, -5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(173, 58).addBox(-3.5F, -8.0F, 3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(185, 107).addBox(4.5F, -8.0F, -5.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(185, 68).addBox(4.5F, -8.0F, 3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -28.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

        PartDefinition body = hasturForm.addOrReplaceChild("body", CubeListBuilder.create().texOffs(134, 113).addBox(-2.5F, -12.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(111, 270).addBox(-2.5F, -12.0F, -3.1F, 8.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -16.0F, 0.0F));

        PartDefinition leftArm = body.addOrReplaceChild("leftArm", CubeListBuilder.create().texOffs(281, 22).addBox(1.3858F, -2.426F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
                .texOffs(92, 230).addBox(1.3858F, -2.426F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -9.0F, 0.0F, 0.0F, 0.0F, -0.3927F));

        PartDefinition leftElbow = leftArm.addOrReplaceChild("leftElbow", CubeListBuilder.create().texOffs(249, 266).addBox(-0.6142F, 2.487F, -1.0029F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 52).addBox(0.3858F, -0.513F, -0.0029F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 7.8F, -0.5F, -1.0472F, 0.0F, 0.0F));

        PartDefinition rightArm = body.addOrReplaceChild("rightArm", CubeListBuilder.create().texOffs(82, 281).addBox(-2.5694F, -3.4511F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(54, 103).addBox(-2.5694F, -3.4511F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F)), PartPose.offsetAndRotation(-4.0F, -9.0F, 0.0F, 0.0F, 0.0F, 0.3054F));

        PartDefinition rElbow = rightArm.addOrReplaceChild("rElbow", CubeListBuilder.create().texOffs(223, 266).addBox(-0.5694F, 1.8696F, -1.7424F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(15, 52).addBox(0.4306F, -1.1304F, -0.7424F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 7.75F, -0.5F, -1.0908F, 0.0F, 0.0F));

        PartDefinition staff = rElbow.addOrReplaceChild("staff", CubeListBuilder.create().texOffs(66, 66).addBox(-2.9657F, -0.4897F, -18.8088F, 9.0F, 0.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(241, 256).addBox(-4.1402F, -1.2774F, -17.5961F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(48, 263).addBox(4.6528F, -1.3887F, -16.6894F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(124, 86).addBox(-3.3472F, -1.3887F, -10.6894F, 9.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(78, 207).addBox(0.6528F, -2.3887F, -15.6894F, 2.0F, 1.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(240, 240).addBox(0.6528F, -1.3887F, -8.6894F, 2.0F, 1.0F, 15.0F, new CubeDeformation(0.0F))
                .texOffs(198, 28).addBox(0.6528F, -0.3887F, -8.6894F, 2.0F, 1.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(208, 28).addBox(0.6528F, -1.3887F, 11.3106F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(143, 2).addBox(0.6528F, 0.6113F, 11.3106F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3222F, 10.1944F, 1.1111F, 0.3927F, 0.0F, 0.0F));

        PartDefinition bone = upperBody.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.5F, 17.5F, -3.6F, 0.6109F, 0.0F, 0.0F));

        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(180, 130).addBox(-6.0F, -5.8834F, -1.0068F, 15.0F, 12.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.1015F, 0.3548F, 0.0873F, 0.0F, 0.0F));

        PartDefinition emblem = upperBody.addOrReplaceChild("emblem", CubeListBuilder.create().texOffs(193, 173).addBox(2.5F, -19.0F, 2.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(29, 145).addBox(-0.5F, -15.0F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(142, 228).addBox(7.5F, -15.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(191, 202).addBox(7.5F, -22.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(146, 199).addBox(7.5F, -18.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(195, 186).addBox(6.3F, -13.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(208, 28).addBox(-2.5F, -15.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(146, 202).addBox(-2.5F, -22.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 199).addBox(-2.5F, -18.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(190, 103).addBox(-1.3F, -13.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(217, 28).addBox(-0.5F, -17.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(117, 218).addBox(5.5F, -21.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(117, 218).addBox(-0.5F, -21.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(104, 218).addBox(5.5F, -17.0F, 2.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(63, 145).addBox(4.5F, -15.0F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(228, 225).addBox(7.5F, -16.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(177, 185).addBox(9.5F, -11.0F, 5.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 125).addBox(-6.7F, -11.0F, 5.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(50, 185).addBox(8.5F, -10.0F, 4.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(142, 20).addBox(-3.7F, -8.0F, 3.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(131, 7).addBox(5.5F, -9.0F, 4.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 35).addBox(-4.7F, -9.0F, 4.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(152, 22).addBox(-5.7F, -10.0F, 4.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(182, 5).addBox(7.5F, -9.0F, 3.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(156, 59).addBox(-4.7F, -9.0F, 3.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(142, 173).addBox(1.3F, -6.0F, 2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(72, 145).addBox(1.3F, -8.0F, 2.0F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 145).addBox(1.3F, -10.0F, 2.8F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(228, 230).addBox(6.5F, -17.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(228, 228).addBox(3.5F, -13.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(64, 234).addBox(6.5F, -19.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(112, 230).addBox(7.5F, -21.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(116, 262).addBox(6.5F, -25.0F, 2.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(177, 182).addBox(4.5F, -23.0F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(146, 178).addBox(3.5F, -21.0F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(95, 117).addBox(3.5F, -18.0F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(235, 195).addBox(3.5F, -19.0F, 1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(23, 62).addBox(0.5F, -18.0F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(236, 19).addBox(0.5F, -19.0F, 1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(146, 181).addBox(0.5F, -21.0F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(182, 2).addBox(-0.5F, -23.0F, 2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(165, 264).addBox(-3.5F, -25.0F, 2.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(90, 230).addBox(-3.5F, -21.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(232, 50).addBox(-2.5F, -19.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(118, 230).addBox(-2.5F, -17.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(226, 50).addBox(0.5F, -13.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(72, 225).addBox(0.5F, -16.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(223, 136).addBox(-0.5F, -24.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 210).addBox(0.5F, -25.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(97, 38).addBox(-1.5F, -26.0F, 2.3F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(223, 140).addBox(3.5F, -16.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(223, 138).addBox(4.5F, -24.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(200, 167).addBox(3.5F, -25.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(126, 66).addBox(4.5F, -26.0F, 2.3F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 141).addBox(6.5F, -26.0F, 3.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(96, 55).addBox(-1.5F, -26.0F, 3.3F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(191, 200).addBox(-2.5F, -27.0F, 4.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(188, 197).addBox(6.5F, -27.0F, 4.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(223, 63).addBox(3.5F, -12.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(72, 221).addBox(5.5F, -10.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 221).addBox(5.5F, -8.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(218, 64).addBox(-1.5F, -8.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(72, 223).addBox(0.5F, -12.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 223).addBox(-1.5F, -10.0F, 2.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(140, 215).addBox(-3.5F, -10.0F, 5.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(214, 106).addBox(6.5F, -10.0F, 5.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(134, 215).addBox(-3.5F, -13.0F, 5.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(213, 50).addBox(6.5F, -13.0F, 5.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(214, 110).addBox(-2.5F, -6.9F, 5.3F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(228, 79).addBox(-3.5F, -16.0F, 2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.4F, 9.0F, -5.8F));

        PartDefinition backing = upperBody.addOrReplaceChild("backing", CubeListBuilder.create().texOffs(206, 155).addBox(-10.5F, -14.2569F, -3.2784F, 24.0F, 10.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 279).addBox(9.5F, -21.5F, 5.5F, 2.0F, 24.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(257, 85).addBox(-6.5F, -25.5F, 5.5F, 16.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.5F, -21.5F, 5.5F, 16.0F, 24.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(10, 279).addBox(-8.5F, -21.5F, 5.5F, 2.0F, 24.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(146, 200).addBox(-8.5F, -26.5F, 0.5F, 20.0F, 27.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(154, 113).addBox(8.5F, -24.2569F, -4.2784F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(21, 160).addBox(-7.5F, -24.2569F, -4.2784F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(64, 35).addBox(-6.5F, -21.2569F, -4.2784F, 16.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(92, 66).addBox(-6.5F, -27.2569F, -4.2784F, 16.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 185).addBox(10.5F, -27.2569F, -4.2784F, 5.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(116, 282).addBox(-12.5F, -27.2569F, -4.2784F, 5.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(125, 37).addBox(-10.5F, -27.2569F, -3.2784F, 24.0F, 13.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.5F, 2.5F, -0.1745F, 0.0F, 0.0F));

        PartDefinition crest = upperBody.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(0, 256).addBox(8.1429F, -19.5F, 0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(67, 245).addBox(-5.8571F, -19.5F, 0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(207, 197).addBox(-1.8571F, -9.5F, 0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(156, 64).addBox(-2.8571F, -10.5F, 0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(127, 153).addBox(-2.8571F, -22.5F, 0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(108, 230).addBox(7.1429F, -20.5F, 0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(142, 218).addBox(-4.8571F, -20.5F, 0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(279, 183).addBox(-3.8571F, -21.5F, -0.5F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(207, 195).addBox(-1.8571F, -23.5F, 0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-21.6429F, -27.5F, 6.5F));

        PartDefinition crest2 = upperBody.addOrReplaceChild("crest2", CubeListBuilder.create().texOffs(63, 245).addBox(8.1429F, -19.5F, 0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(54, 112).addBox(-5.8571F, -19.5F, 0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(198, 49).addBox(-1.8571F, -9.5F, 0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(127, 151).addBox(-2.8571F, -10.5F, 0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(143, 0).addBox(-2.8571F, -22.5F, 0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(122, 218).addBox(7.1429F, -20.5F, 0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(153, 8).addBox(-4.8571F, -20.5F, 0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(262, 279).addBox(-3.8571F, -21.5F, -0.5F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(172, 197).addBox(-1.8571F, -23.5F, 0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(21.3571F, -27.5F, 6.5F));

        PartDefinition crest3 = upperBody.addOrReplaceChild("crest3", CubeListBuilder.create().texOffs(52, 245).addBox(8.1429F, -19.5F, 0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(48, 241).addBox(-5.8571F, -19.5F, 0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(82, 197).addBox(-1.8571F, -9.5F, 0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(134, 132).addBox(-2.8571F, -10.5F, 0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(80, 101).addBox(-2.8571F, -22.5F, 0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 52).addBox(7.1429F, -20.5F, 0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(23, 150).addBox(-4.8571F, -20.5F, 0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(238, 279).addBox(-3.8571F, -21.5F, -0.5F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(82, 195).addBox(-1.8571F, -23.5F, 0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.6429F, -35.5F, 2.5F));

        PartDefinition crest4 = upperBody.addOrReplaceChild("crest4", CubeListBuilder.create().texOffs(183, 232).addBox(18.1429F, 0.5F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(62, 195).addBox(4.1429F, 0.5F, -0.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(34, 185).addBox(8.1429F, 10.5F, -0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(134, 130).addBox(7.1429F, 9.5F, -0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(125, 56).addBox(7.1429F, -2.5F, -0.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(20, 141).addBox(17.1429F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(125, 28).addBox(5.1429F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(198, 37).addBox(6.1429F, -1.5F, -1.5F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(163, 0).addBox(8.1429F, -3.5F, -0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(21.3571F, -27.5F, 7.5F));

        PartDefinition crest5 = upperBody.addOrReplaceChild("crest5", CubeListBuilder.create().texOffs(124, 189).addBox(-1.8571F, -19.5F, 1.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(79, 151).addBox(-15.8571F, -19.5F, 1.5F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(182, 17).addBox(-11.8571F, -9.5F, 1.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(125, 54).addBox(-12.8571F, -10.5F, 1.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 125).addBox(-12.8571F, -22.5F, 1.5F, 9.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(23, 52).addBox(-2.8571F, -20.5F, 1.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(98, 28).addBox(-14.8571F, -20.5F, 1.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(214, 279).addBox(-13.8571F, -21.5F, 0.5F, 11.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(182, 15).addBox(-11.8571F, -23.5F, 1.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-21.6429F, -7.5F, 6.5F));

        PartDefinition lowerBody = partdefinition.addOrReplaceChild("lowerBody", CubeListBuilder.create().texOffs(64, 0).addBox(-9.0F, 10.0F, -13.0F, 20.0F, 1.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(0, 52).addBox(-10.0F, -9.0F, -15.0F, 22.0F, 19.0F, 30.0F, new CubeDeformation(0.0F))
                .texOffs(226, 28).addBox(-8.0F, -9.0F, -17.0F, 18.0F, 20.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(64, 0).addBox(-5.0F, -9.0F, -20.0F, 12.0F, 20.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(189, 225).addBox(-7.0F, -3.0F, -28.0F, 16.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(56, 151).addBox(12.0F, -8.0F, -13.0F, 1.0F, 17.0F, 27.0F, new CubeDeformation(0.0F))
                .texOffs(0, 141).addBox(-11.0F, -8.0F, -13.0F, 1.0F, 17.0F, 27.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -11.0F, 2.2F));

        PartDefinition jaw2 = lowerBody.addOrReplaceChild("jaw2", CubeListBuilder.create().texOffs(95, 113).addBox(6.0F, 20.0F, 13.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(24, 78).addBox(-7.0F, 20.0F, 13.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -19.0F, -43.0F));

        PartDefinition topJaw = jaw2.addOrReplaceChild("topJaw", CubeListBuilder.create().texOffs(0, 78).addBox(-4.75F, -2.0F, -2.7F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(63, 141).addBox(5.25F, 0.0F, -2.7F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(29, 141).addBox(-5.75F, 0.0F, -2.7F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 19.0F, 15.7F));

        PartDefinition bone3 = topJaw.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(43, 141).addBox(-1.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 62).addBox(0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(125, 38).addBox(-5.0F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(30, 29).addBox(-4.5F, 1.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(30, 30).addBox(-2.25F, 1.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(101, 55).addBox(-3.0F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(100, 101).addBox(2.0F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(21, 101).addBox(4.0F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 28).addBox(4.25F, 1.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 34).addBox(-1.0F, 1.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 27).addBox(1.0F, 1.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 28).addBox(2.25F, 1.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 1.0F, -3.2F, -0.6109F, 0.0F, 0.0F));

        PartDefinition bottomJaw = jaw2.addOrReplaceChild("bottomJaw", CubeListBuilder.create().texOffs(152, 2).addBox(5.25F, -3.5F, -2.7F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(152, 18).addBox(-5.75F, -3.5F, -2.7F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(64, 21).addBox(-4.75F, -1.5F, -2.7F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 24.5F, 15.7F));

        PartDefinition bone2 = bottomJaw.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(177, 37).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 27).addBox(0.0F, -2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(190, 64).addBox(-4.75F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(30, 36).addBox(-4.25F, -2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 35).addBox(-2.5F, -2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(178, 54).addBox(-2.75F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 161).addBox(1.75F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(11, 161).addBox(3.75F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 35).addBox(4.5F, -2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(8, 34).addBox(2.5F, -2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, -1.5F, -3.2F, 0.6109F, 0.0F, 0.0F));

        PartDefinition fRArm = lowerBody.addOrReplaceChild("fRArm", CubeListBuilder.create(), PartPose.offset(-7.75F, 1.25F, -24.1F));

        PartDefinition fRShoulder = fRArm.addOrReplaceChild("fRShoulder", CubeListBuilder.create().texOffs(138, 269).addBox(-4.1296F, -3.3185F, -2.8914F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(131, 0).addBox(0.3322F, -5.4315F, -3.8914F, 1.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(85, 164).addBox(-20.1296F, -2.8185F, 3.0086F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6059F, 0.1815F, -0.225F, 0.0F, -0.2182F, 0.0F));

        PartDefinition fRBicep = fRShoulder.addOrReplaceChild("fRBicep", CubeListBuilder.create().texOffs(214, 106).addBox(-15.3472F, -3.8529F, -3.7811F, 17.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(236, 16).addBox(-15.3472F, 5.1471F, -3.7811F, 17.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(72, 221).addBox(-3.3472F, 4.1471F, -3.7811F, 5.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.7823F, -1.4656F, 1.8897F));

        PartDefinition fRFore = fRBicep.addOrReplaceChild("fRFore", CubeListBuilder.create().texOffs(262, 113).addBox(-12.3143F, -2.1723F, 2.1305F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(82, 207).addBox(-10.3143F, -2.1723F, -7.8695F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(147, 237).addBox(-12.3143F, -3.1723F, -5.8695F, 14.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(260, 0).addBox(-11.9143F, -2.8723F, -6.4695F, 14.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(105, 251).addBox(-12.3143F, -4.1723F, -4.8695F, 14.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.1364F, 0.4142F, -0.5847F, 0.6109F, -1.2217F, 0.0F));

        PartDefinition fRFist = fRFore.addOrReplaceChild("fRFist", CubeListBuilder.create().texOffs(63, 141).addBox(-1.8695F, -5.5687F, -4.8121F, 1.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(182, 0).addBox(-2.8695F, -4.5687F, -4.8121F, 1.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(177, 167).addBox(-0.8695F, -4.5687F, -4.8121F, 1.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(104, 218).addBox(-0.8695F, -3.5687F, -3.8121F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-12.3199F, 0.3964F, -0.2657F));

        PartDefinition rFingers = fRFist.addOrReplaceChild("rFingers", CubeListBuilder.create(), PartPose.offset(-1.4F, 0.4F, 3.0F));

        PartDefinition rFinger1 = rFingers.addOrReplaceChild("rFinger1", CubeListBuilder.create().texOffs(219, 53).addBox(-1.9167F, -1.4804F, -1.855F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5527F, -4.4882F, 1.0429F));

        PartDefinition rFinger12 = rFinger1.addOrReplaceChild("rFinger12", CubeListBuilder.create().texOffs(134, 211).addBox(-1.901F, -1.5598F, -1.7574F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(58, 229).addBox(-1.901F, -1.0598F, -0.7574F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0158F, 0.0794F, -0.0976F));

        PartDefinition rFinger13 = rFinger12.addOrReplaceChild("rFinger13", CubeListBuilder.create().texOffs(104, 213).addBox(-2.8524F, -1.7184F, -2.026F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(79, 176).addBox(-3.8524F, -1.2184F, -1.526F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0486F, 0.1586F, 0.2686F));

        PartDefinition rFinger2 = rFingers.addOrReplaceChild("rFinger2", CubeListBuilder.create().texOffs(58, 221).addBox(-1.9167F, -1.4804F, -1.855F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5527F, -1.4882F, 1.0429F));

        PartDefinition rFinger22 = rFinger2.addOrReplaceChild("rFinger22", CubeListBuilder.create().texOffs(219, 57).addBox(-1.901F, -1.5598F, -1.7574F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(229, 16).addBox(-1.901F, -1.0598F, -0.7574F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0158F, 0.0794F, -0.0976F));

        PartDefinition rFinger23 = rFinger22.addOrReplaceChild("rFinger23", CubeListBuilder.create().texOffs(235, 266).addBox(-2.8524F, -1.7184F, -2.026F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(179, 69).addBox(-3.8524F, -1.2184F, -1.526F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0486F, 0.1586F, 0.2686F));

        PartDefinition rFinger3 = rFingers.addOrReplaceChild("rFinger3", CubeListBuilder.create().texOffs(211, 258).addBox(-1.9167F, -1.4804F, -1.855F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5527F, 1.5118F, 1.0429F));

        PartDefinition rFinger32 = rFinger3.addOrReplaceChild("rFinger32", CubeListBuilder.create().texOffs(58, 225).addBox(-1.901F, -1.5598F, -1.7574F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(236, 25).addBox(-1.901F, -1.0598F, -0.7574F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0158F, 0.0794F, -0.0976F));

        PartDefinition rFinger33 = rFinger32.addOrReplaceChild("rFinger33", CubeListBuilder.create().texOffs(261, 266).addBox(-2.8524F, -1.7184F, -2.026F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(179, 108).addBox(-3.8524F, -1.2184F, -1.526F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0486F, 0.1586F, 0.2686F));

        PartDefinition rFinger4 = rFingers.addOrReplaceChild("rFinger4", CubeListBuilder.create().texOffs(264, 25).addBox(-1.9167F, -1.4804F, -1.855F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5527F, 4.5118F, 1.0429F));

        PartDefinition rFinger42 = rFinger4.addOrReplaceChild("rFinger42", CubeListBuilder.create().texOffs(258, 155).addBox(-1.901F, -1.5598F, -1.7574F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(248, 25).addBox(-1.901F, -1.0598F, -0.7574F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.0158F, 0.0794F, -0.0976F));

        PartDefinition rFinger43 = rFinger42.addOrReplaceChild("rFinger43", CubeListBuilder.create().texOffs(0, 272).addBox(-2.8524F, -1.7184F, -2.026F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(180, 172).addBox(-3.8524F, -1.2184F, -1.526F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0486F, 0.1586F, 0.2686F));

        PartDefinition rThumb = fRFist.addOrReplaceChild("rThumb", CubeListBuilder.create().texOffs(132, 270).addBox(-3.901F, -1.3432F, -1.8407F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(48, 210).addBox(-3.901F, -0.7432F, -2.8407F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.9685F, -4.2255F, -4.9714F));

        PartDefinition rThumb2 = rThumb.addOrReplaceChild("rThumb2", CubeListBuilder.create().texOffs(15, 270).addBox(-3.6695F, -1.6187F, -1.8121F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(190, 79).addBox(-4.6695F, -1.1187F, -1.3121F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.2315F, 0.2755F, -0.0286F));

        PartDefinition fLArm = lowerBody.addOrReplaceChild("fLArm", CubeListBuilder.create(), PartPose.offset(7.75F, 1.25F, -24.1F));

        PartDefinition fLShoulder = fLArm.addOrReplaceChild("fLShoulder", CubeListBuilder.create().texOffs(266, 92).addBox(-0.9178F, -3.3185F, -2.4586F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(124, 66).addBox(0.6204F, -5.4315F, -3.4586F, 1.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(100, 151).addBox(19.0822F, -2.8185F, 3.4414F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6059F, 0.1815F, -0.225F, 0.0F, 0.2182F, 0.0F));

        PartDefinition fLBicep = fLShoulder.addOrReplaceChild("fLBicep", CubeListBuilder.create().texOffs(54, 113).addBox(0.2998F, -3.8529F, -3.3483F, 17.0F, 7.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(124, 190).addBox(0.2998F, 5.1471F, -3.3483F, 17.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(156, 54).addBox(0.2998F, 4.1471F, -3.3483F, 5.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(2.7823F, -1.4656F, 1.8897F));

        PartDefinition fLFore = fLBicep.addOrReplaceChild("fLFore", CubeListBuilder.create().texOffs(223, 249).addBox(-1.4246F, -1.0349F, 3.7548F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(186, 167).addBox(4.5754F, -1.0349F, -6.2452F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(236, 136).addBox(-1.4246F, -2.0349F, -4.2452F, 14.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(74, 74).addBox(-1.8246F, -1.7349F, -4.8452F, 14.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(247, 59).addBox(-1.4246F, -3.0349F, -3.2452F, 14.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.1364F, 0.4142F, -0.5847F, 0.6109F, 1.2217F, 0.0F));

        PartDefinition fLFist = fLFore.addOrReplaceChild("fLFist", CubeListBuilder.create().texOffs(29, 141).addBox(1.1305F, -4.4313F, -3.1879F, 1.0F, 10.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(176, 103).addBox(2.1305F, -3.4313F, -3.1879F, 1.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(176, 64).addBox(0.1305F, -3.4313F, -3.1879F, 1.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(48, 199).addBox(-2.8695F, -2.4313F, -2.1879F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(12.3199F, 0.3964F, -0.2657F));

        PartDefinition lFingers = fLFist.addOrReplaceChild("lFingers", CubeListBuilder.create(), PartPose.offset(1.4F, 0.4F, 3.0F));

        PartDefinition lFinger1 = lFingers.addOrReplaceChild("lFinger1", CubeListBuilder.create().texOffs(210, 94).addBox(-1.8222F, -0.3431F, -0.2307F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5527F, -4.4882F, 1.0429F));

        PartDefinition lFinger12 = lFinger1.addOrReplaceChild("lFinger12", CubeListBuilder.create().texOffs(210, 90).addBox(-1.838F, -0.4225F, -0.1331F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(218, 79).addBox(-1.838F, 0.0775F, 0.8669F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0158F, 0.0794F, -0.0976F));

        PartDefinition lFinger13 = lFinger12.addOrReplaceChild("lFinger13", CubeListBuilder.create().texOffs(100, 170).addBox(0.1134F, -0.5811F, -0.4017F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 164).addBox(3.1134F, -0.0811F, 0.0983F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0486F, 0.1586F, 0.2686F));

        PartDefinition lFinger2 = lFingers.addOrReplaceChild("lFinger2", CubeListBuilder.create().texOffs(134, 207).addBox(-1.8222F, -0.3431F, -0.2307F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5527F, -1.4882F, 1.0429F));

        PartDefinition rFinger5 = lFinger2.addOrReplaceChild("rFinger5", CubeListBuilder.create().texOffs(187, 182).addBox(-1.838F, -0.4225F, -0.1331F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(217, 16).addBox(-1.838F, 0.0775F, 0.8669F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0158F, 0.0794F, -0.0976F));

        PartDefinition lFinger23 = rFinger5.addOrReplaceChild("lFinger23", CubeListBuilder.create().texOffs(85, 170).addBox(0.1134F, -0.5811F, -0.4017F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(144, 86).addBox(3.1134F, -0.0811F, 0.0983F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0486F, 0.1586F, 0.2686F));

        PartDefinition lFinger3 = lFingers.addOrReplaceChild("lFinger3", CubeListBuilder.create().texOffs(180, 138).addBox(-1.8222F, -0.3431F, -0.2307F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5527F, 1.5118F, 1.0429F));

        PartDefinition lFinger32 = lFinger3.addOrReplaceChild("lFinger32", CubeListBuilder.create().texOffs(180, 134).addBox(-1.838F, -0.4225F, -0.1331F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(136, 73).addBox(-1.838F, 0.0775F, 0.8669F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0158F, 0.0794F, -0.0976F));

        PartDefinition lFinger33 = lFinger32.addOrReplaceChild("lFinger33", CubeListBuilder.create().texOffs(85, 158).addBox(0.1134F, -0.5811F, -0.4017F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(144, 66).addBox(3.1134F, -0.0811F, 0.0983F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0486F, 0.1586F, 0.2686F));

        PartDefinition lFinger4 = lFingers.addOrReplaceChild("lFinger4", CubeListBuilder.create().texOffs(180, 130).addBox(-1.8222F, -0.3431F, -0.2307F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5527F, 4.5118F, 1.0429F));

        PartDefinition lFinger5 = lFinger4.addOrReplaceChild("lFinger5", CubeListBuilder.create().texOffs(71, 171).addBox(-1.838F, -0.4225F, -0.1331F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(93, 52).addBox(-1.838F, 0.0775F, 0.8669F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0158F, 0.0794F, -0.0976F));

        PartDefinition lFinger43 = lFinger5.addOrReplaceChild("lFinger43", CubeListBuilder.create().texOffs(100, 157).addBox(0.1134F, -0.5811F, -0.4017F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(86, 21).addBox(3.1134F, -0.0811F, 0.0983F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0486F, 0.1586F, 0.2686F));

        PartDefinition lThumb = fLFist.addOrReplaceChild("lThumb", CubeListBuilder.create().texOffs(71, 167).addBox(0.162F, -0.2058F, -0.2164F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(208, 34).addBox(-0.838F, 0.3942F, -1.2164F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.9685F, -4.2255F, -4.9714F));

        PartDefinition lThumb2 = lThumb.addOrReplaceChild("lThumb2", CubeListBuilder.create().texOffs(92, 70).addBox(-0.0695F, -0.4813F, -0.1879F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(22, 78).addBox(3.9305F, 0.0187F, 0.3121F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.2315F, 0.2755F, -0.0286F));

        PartDefinition fLLeg = lowerBody.addOrReplaceChild("fLLeg", CubeListBuilder.create().texOffs(77, 232).addBox(1.75F, -6.75F, -7.65F, 1.0F, 13.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(11.25F, 0.75F, -5.35F));

        PartDefinition fLHip = fLLeg.addOrReplaceChild("fLHip", CubeListBuilder.create().texOffs(158, 276).addBox(-0.9081F, -3.1821F, -4.1F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0833F, 0.0833F, 0.45F, 0.0F, 0.0F, 0.48F));

        PartDefinition fLFemur = fLHip.addOrReplaceChild("fLFemur", CubeListBuilder.create().texOffs(0, 225).addBox(0.3836F, -4.56F, -4.5647F, 17.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(244, 202).addBox(0.3836F, -4.56F, -5.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(191, 242).addBox(4.3836F, -4.56F, -5.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(36, 241).addBox(9.3836F, -4.56F, -5.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 241).addBox(13.3836F, -4.56F, -5.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(85, 151).addBox(16.3836F, -2.56F, -6.5647F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(260, 70).addBox(19.3836F, -1.56F, -5.5647F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(158, 190).addBox(0.3836F, 1.94F, -2.5647F, 17.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(257, 41).addBox(0.3836F, -5.56F, -3.5647F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(56, 265).addBox(0.3836F, -6.56F, -3.5647F, 6.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(141, 281).addBox(9.3836F, -5.56F, -3.5647F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(247, 53).addBox(-1.6164F, -2.06F, 5.3353F, 19.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 123).addBox(2.3836F, -5.56F, -4.5647F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(284, 44).addBox(3.3836F, -2.06F, 3.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(208, 270).addBox(9.3836F, -2.06F, 3.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(266, 29).addBox(-1.6164F, -2.06F, 3.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5284F, -0.8674F, -1.5353F, 0.0F, 0.0F, 0.3054F));

        PartDefinition fLTibia = fLFemur.addOrReplaceChild("fLTibia", CubeListBuilder.create().texOffs(277, 256).addBox(-1.4039F, -2.6027F, 2.9277F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(284, 38).addBox(10.5961F, -2.6027F, 4.9277F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(244, 202).addBox(-1.4039F, -3.6027F, -5.0723F, 14.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(277, 14).addBox(-2.0039F, -3.3027F, -5.6723F, 14.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(255, 106).addBox(-1.4039F, -4.6027F, -4.0723F, 14.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(16.8636F, 0.4142F, 1.4153F, 0.0F, -0.3491F, 0.7418F));

        PartDefinition fLFoot = fLTibia.addOrReplaceChild("fLFoot", CubeListBuilder.create().texOffs(280, 195).addBox(5.9186F, -2.9528F, -22.0921F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(280, 57).addBox(5.9186F, 0.0472F, -22.0921F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(177, 281).addBox(6.9186F, -1.9528F, -20.0921F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(260, 266).addBox(6.9186F, 1.0472F, -16.0921F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(28, 263).addBox(2.9186F, -2.9528F, -13.0921F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(266, 25).addBox(6.9186F, -4.9528F, -16.0921F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(102, 271).addBox(4.9186F, 1.0472F, -3.0921F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(275, 272).addBox(4.9186F, 1.0472F, 2.9079F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(281, 31).addBox(4.9186F, -1.9528F, 2.9079F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(280, 205).addBox(4.9186F, -4.9528F, 2.9079F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(166, 264).addBox(-0.0814F, -2.9528F, -5.0921F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(15, 266).addBox(4.9186F, -4.9528F, -3.0921F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.6801F, 0.3964F, 1.7343F, -0.3054F, 0.3491F, 0.0F));

        PartDefinition bLLeg = lowerBody.addOrReplaceChild("bLLeg", CubeListBuilder.create().texOffs(113, 219).addBox(-0.45F, -6.75F, -7.65F, 8.0F, 13.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(6.45F, 0.75F, 20.65F));

        PartDefinition bLHip = bLLeg.addOrReplaceChild("bLHip", CubeListBuilder.create().texOffs(188, 273).addBox(-1.8316F, -4.9561F, -3.1F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.8833F, 2.0833F, -0.55F, 0.0F, 0.0F, 0.48F));

        PartDefinition bLFemur = bLHip.addOrReplaceChild("bLFemur", CubeListBuilder.create().texOffs(217, 0).addBox(-0.3235F, -6.6814F, -5.5647F, 17.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(217, 0).addBox(-0.3235F, -6.6814F, -6.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(200, 90).addBox(3.6765F, -6.6814F, -6.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(82, 199).addBox(8.6765F, -6.6814F, -6.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(198, 53).addBox(12.6765F, -6.6814F, -6.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(124, 68).addBox(15.6765F, -4.6814F, -7.5647F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(259, 242).addBox(18.6765F, -3.6814F, -6.5647F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(57, 28).addBox(-0.3235F, -0.1814F, -3.5647F, 17.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(214, 256).addBox(-0.3235F, -7.6814F, -4.5647F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(144, 259).addBox(-0.3235F, -8.6814F, -4.5647F, 6.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(223, 136).addBox(8.6765F, -7.6814F, -4.5647F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(237, 232).addBox(-2.3235F, -4.1814F, 4.3353F, 19.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(54, 101).addBox(1.6765F, -7.6814F, -5.5647F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(43, 225).addBox(2.6765F, -4.1814F, 2.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(89, 221).addBox(8.6765F, -4.1814F, 2.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(104, 207).addBox(-2.3235F, -4.1814F, 2.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6414F, -0.4057F, 0.4647F, 0.0F, 0.0F, 0.3054F));

        PartDefinition bLTibia = bLFemur.addOrReplaceChild("bLTibia", CubeListBuilder.create().texOffs(274, 250).addBox(-8.827F, -2.9952F, 8.822F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(36, 283).addBox(3.173F, -2.9952F, 10.822F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 241).addBox(-8.827F, -3.9952F, 0.822F, 14.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(273, 217).addBox(-9.427F, -3.6952F, 0.222F, 14.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(251, 176).addBox(-8.827F, -4.9952F, 1.822F, 14.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(22.5204F, 4.6568F, -2.5847F, 0.0F, -0.3491F, 0.7418F));

        PartDefinition bLFoot = bLTibia.addOrReplaceChild("bLFoot", CubeListBuilder.create().texOffs(279, 69).addBox(5.9186F, -2.8973F, -22.1145F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(48, 245).addBox(5.9186F, 0.1027F, -22.1145F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(98, 281).addBox(6.9186F, -1.8973F, -20.1145F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(0, 266).addBox(6.9186F, 1.1027F, -16.1145F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(191, 258).addBox(2.9186F, -2.8973F, -13.1145F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(43, 221).addBox(6.9186F, -4.8973F, -16.1145F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(250, 151).addBox(4.9186F, 1.1027F, -3.1145F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(237, 238).addBox(4.9186F, 1.1027F, 2.8855F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(236, 151).addBox(4.9186F, -1.8973F, 2.8855F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(236, 85).addBox(4.9186F, -4.8973F, 2.8855F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(116, 258).addBox(-0.0814F, -2.8973F, -5.1145F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(52, 234).addBox(4.9186F, -4.8973F, -3.1145F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.257F, 0.0038F, 7.6286F, -0.6109F, 0.3491F, 0.0F));

        PartDefinition fRLeg = lowerBody.addOrReplaceChild("fRLeg", CubeListBuilder.create().texOffs(0, 52).addBox(-0.25F, -7.75F, -5.65F, 1.0F, 13.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(-11.75F, 1.75F, -7.35F));

        PartDefinition fRHip = fRLeg.addOrReplaceChild("fRHip", CubeListBuilder.create().texOffs(56, 275).addBox(-4.4098F, -3.6074F, -4.1F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0833F, 0.0833F, 2.45F, 0.0F, 0.0F, -0.48F));

        PartDefinition fRFemur = fRHip.addOrReplaceChild("fRFemur", CubeListBuilder.create().texOffs(223, 120).addBox(-14.2022F, -4.56F, -3.5647F, 17.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(136, 66).addBox(-16.2022F, -2.56F, -5.5647F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(82, 230).addBox(-12.2022F, -4.56F, -4.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(72, 230).addBox(-7.2022F, -4.56F, -4.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 225).addBox(-3.2022F, -4.56F, -4.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(260, 66).addBox(-18.2022F, -1.56F, -4.5647F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(250, 256).addBox(-6.2022F, -5.56F, -2.5647F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(262, 151).addBox(-3.2022F, -6.56F, -2.5647F, 6.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(136, 259).addBox(-8.2022F, -5.56F, -2.5647F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(105, 245).addBox(-14.2022F, -2.06F, 6.3353F, 19.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 121).addBox(-15.2022F, -5.56F, -3.5647F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 266).addBox(-9.2022F, -2.06F, 4.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(77, 265).addBox(-3.2022F, -2.06F, 4.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(233, 136).addBox(1.7978F, -2.06F, 4.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8968F, -0.4421F, -2.5353F, 0.0F, 0.0F, -0.3054F));

        PartDefinition fRTibia = fRFemur.addOrReplaceChild("fRTibia", CubeListBuilder.create().texOffs(275, 266).addBox(-14.097F, -0.561F, 4.4952F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(283, 151).addBox(-20.097F, -0.561F, 6.4952F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(243, 187).addBox(-14.097F, -1.561F, -3.5048F, 14.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(26, 275).addBox(-13.697F, -1.261F, -4.1048F, 14.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(139, 252).addBox(-14.097F, -2.561F, -2.5048F, 14.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.4293F, -1.7072F, 0.4153F, 0.0F, 0.3491F, -0.7418F));

        PartDefinition fRFoot = fRTibia.addOrReplaceChild("fRFoot", CubeListBuilder.create().texOffs(279, 235).addBox(-9.0814F, -1.0472F, -21.4912F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(279, 225).addBox(-9.0814F, 1.9528F, -21.4912F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(130, 281).addBox(-9.0814F, -0.0472F, -19.4912F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(234, 266).addBox(-9.0814F, 2.9528F, -15.4912F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(260, 66).addBox(-9.0814F, -1.0472F, -12.4912F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(208, 266).addBox(-9.0814F, -3.0472F, -15.4912F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(265, 183).addBox(-10.0814F, 2.9528F, -2.4912F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(259, 250).addBox(-10.0814F, 2.9528F, 3.5088F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(110, 258).addBox(-10.0814F, -0.0472F, 3.5088F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(257, 92).addBox(-10.0814F, -3.0472F, 3.5088F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(259, 238).addBox(-6.0814F, -1.0472F, -4.4912F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(251, 183).addBox(-10.0814F, -3.0472F, -2.4912F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.013F, 0.44F, 3.272F, -0.3054F, -0.3491F, 0.0F));

        PartDefinition bRLeg = lowerBody.addOrReplaceChild("bRLeg", CubeListBuilder.create().texOffs(218, 53).addBox(-5.5F, -6.75F, -6.65F, 8.0F, 13.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.5F, 0.75F, 19.65F));

        PartDefinition bRHip = bRLeg.addOrReplaceChild("bRHip", CubeListBuilder.create().texOffs(272, 130).addBox(-4.4463F, -1.3716F, -3.1F, 7.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.1667F, -0.9167F, 0.45F, 0.0F, 0.0F, -0.48F));

        PartDefinition bRFemur = bRHip.addOrReplaceChild("bRFemur", CubeListBuilder.create().texOffs(214, 90).addBox(-14.9093F, -3.8529F, -4.5647F, 17.0F, 7.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(30, 30).addBox(-16.9093F, -1.8529F, -6.5647F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 187).addBox(-12.9093F, -3.8529F, -5.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(127, 167).addBox(-7.9093F, -3.8529F, -5.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(56, 167).addBox(-3.9093F, -3.8529F, -5.5647F, 3.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(259, 238).addBox(-18.9093F, -0.8529F, -5.5647F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 256).addBox(-6.9093F, -4.8529F, -3.5647F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(198, 53).addBox(-3.9093F, -5.8529F, -3.5647F, 6.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(177, 182).addBox(-8.9093F, -4.8529F, -3.5647F, 2.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(236, 79).addBox(-14.9093F, -1.3529F, 5.3353F, 19.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(64, 25).addBox(-15.9093F, -4.8529F, -4.5647F, 12.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(191, 0).addBox(-9.9093F, -1.3529F, 3.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(142, 167).addBox(-3.9093F, -1.3529F, 3.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(100, 164).addBox(1.0907F, -1.3529F, 3.3353F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.4716F, 0.9066F, -0.5353F, 0.0F, 0.0F, -0.3054F));

        PartDefinition bRTibia = bRFemur.addOrReplaceChild("bRTibia", CubeListBuilder.create().texOffs(269, 8).addBox(-13.755F, -0.561F, 3.5555F, 14.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(20, 283).addBox(-19.755F, -0.561F, 5.5555F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(237, 217).addBox(-13.755F, -1.561F, -4.4445F, 14.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(262, 161).addBox(-13.355F, -1.261F, -5.0445F, 14.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(251, 169).addBox(-13.755F, -2.561F, -3.4445F, 14.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-15.1364F, -1.0F, 0.4153F, 0.0F, 0.3491F, -0.7418F));

        PartDefinition bRFoot = bRTibia.addOrReplaceChild("bRFoot", CubeListBuilder.create().texOffs(127, 167).addBox(-9.0814F, -1.1816F, -20.2186F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(56, 167).addBox(-9.0814F, 1.8184F, -20.2186F, 3.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(280, 142).addBox(-9.0814F, -0.1816F, -18.2186F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
                .texOffs(85, 164).addBox(-9.0814F, 2.8184F, -14.2186F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(171, 252).addBox(-9.0814F, -1.1816F, -11.2186F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(85, 151).addBox(-9.0814F, -3.1816F, -14.2186F, 2.0F, 2.0F, 11.0F, new CubeDeformation(0.0F))
                .texOffs(169, 232).addBox(-10.0814F, 2.8184F, -1.2186F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(155, 232).addBox(-10.0814F, 2.8184F, 4.7814F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(223, 195).addBox(-10.0814F, -0.1816F, 4.7814F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(200, 99).addBox(-10.0814F, -3.1816F, 4.7814F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(34, 187).addBox(-6.0814F, -1.1816F, -3.2186F, 6.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(98, 195).addBox(-10.0814F, -3.1816F, -1.2186F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-13.3289F, 0.44F, 1.3926F, -0.6109F, -0.3491F, 0.0F));

        PartDefinition tail = lowerBody.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(80, 113).addBox(-7.0237F, -11.1F, 2.7836F, 16.0F, 16.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(124, 66).addBox(-6.0237F, -10.1F, 0.7836F, 14.0F, 13.0F, 24.0F, new CubeDeformation(0.0F))
                .texOffs(131, 167).addBox(-5.0237F, -12.0F, 2.7836F, 12.0F, 1.0F, 22.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 12.0F, 0.0F, -0.2182F, 0.0F));

        PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(156, 37).addBox(-3.0152F, -10.0F, 1.1736F, 8.0F, 1.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(0, 101).addBox(-6.0152F, -9.0F, 1.1736F, 14.0F, 14.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-5.0152F, -8.0F, -12.8264F, 12.0F, 12.0F, 40.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 24.0F, 0.0F, 0.3927F, 0.0F));

        PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(85, 151).addBox(-3.0937F, -7.8978F, 0.6462F, 8.0F, 1.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(130, 130).addBox(-5.0937F, -6.8978F, 0.6462F, 12.0F, 11.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(70, 70).addBox(-4.0937F, -5.8978F, -7.3538F, 10.0F, 9.0F, 34.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 26.0F, 0.0F, 0.2618F, 0.0F));

        PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(134, 103).addBox(-3.3408F, -5.68F, 0.6541F, 8.0F, 1.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(132, 2).addBox(-5.3408F, -4.68F, 0.6541F, 12.0F, 9.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(74, 28).addBox(-4.3408F, -3.68F, -4.3459F, 10.0F, 7.0F, 31.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 26.0F, 0.0F, 0.829F, 0.0F));

        PartDefinition tail5 = tail4.addOrReplaceChild("tail5", CubeListBuilder.create().texOffs(173, 173).addBox(-0.5893F, -5.0357F, 0.9381F, 4.0F, 1.0F, 26.0F, new CubeDeformation(0.0F))
                .texOffs(182, 0).addBox(-2.5893F, -4.0357F, 0.9381F, 8.0F, 9.0F, 19.0F, new CubeDeformation(0.0F))
                .texOffs(90, 178).addBox(-1.5893F, -3.0357F, -2.0619F, 6.0F, 7.0F, 22.0F, new CubeDeformation(0.0F))
                .texOffs(88, 258).addBox(-2.5893F, -3.0357F, 18.9381F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(124, 178).addBox(-2.5893F, -2.0357F, 24.9381F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(80, 271).addBox(5.4107F, -1.0357F, 25.9381F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(80, 127).addBox(-18.5893F, -1.0357F, 28.9381F, 8.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(74, 52).addBox(13.4107F, -1.0357F, 28.9381F, 8.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(266, 119).addBox(-10.5893F, -1.0357F, 25.9381F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 25.5F, 0.7418F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 512, 512);
    }
    private final ModelPart whole;
    private final ModelPart upperBody;
    private final ModelPart hasturForm;

    private final ModelPart lowerBody;
    private final ModelPart head;
    private final ModelPart fLLeg;
    private final ModelPart fLTibia;
    private final ModelPart bLLeg;
    private final ModelPart bLTibia;
    private final ModelPart fRLeg;
    private final ModelPart fRTibia;
    private final ModelPart bRLeg;
    private final ModelPart bRTibia;
    private final ModelPart bLFemur;
    private final ModelPart bRFemur;
    private final ModelPart fLFemur;
    private final ModelPart fRFemur;
    private final ModelPart topJaw;
    private final ModelPart bottomJaw;
    private final ModelPart tail;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart tail4;
    private final ModelPart tail5;
    private boolean transitioning;
    private float mountOpacity = 1.0F;
    private float mountScale = 1.0F;

    public VesperTheCrownedRefusalModel(ModelPart root) {
        this.whole = root.getChild("whole");
        this.upperBody = root.getChild("upperBody");
        this.lowerBody = root.getChild("lowerBody");
        this.hasturForm = this.upperBody.getChild("hasturForm");
        this.head = this.hasturForm.getChild("head");

        this.fLLeg = this.lowerBody.getChild("fLLeg");
        ModelPart fLHip = this.fLLeg.getChild("fLHip");
        this.fLFemur = fLHip.getChild("fLFemur");
        this.fLTibia = this.fLFemur.getChild("fLTibia");

        this.bLLeg = this.lowerBody.getChild("bLLeg");
        ModelPart bLHip = this.bLLeg.getChild("bLHip");
        this.bLFemur = bLHip.getChild("bLFemur");
        this.bLTibia = this.bLFemur.getChild("bLTibia");

        this.fRLeg = this.lowerBody.getChild("fRLeg");
        ModelPart fRHip = this.fRLeg.getChild("fRHip");
        this.fRFemur = fRHip.getChild("fRFemur");
        this.fRTibia = this.fRFemur.getChild("fRTibia");

        this.bRLeg = this.lowerBody.getChild("bRLeg");
        ModelPart bRHip = this.bRLeg.getChild("bRHip");
        this.bRFemur = bRHip.getChild("bRFemur");
        this.bRTibia = this.bRFemur.getChild("bRTibia");

        ModelPart jaw2 = this.lowerBody.getChild("jaw2");
        this.topJaw = jaw2.getChild("topJaw");
        this.bottomJaw = jaw2.getChild("bottomJaw");

        this.tail = this.lowerBody.getChild("tail");
        this.tail2 = this.tail.getChild("tail2");
        this.tail3 = this.tail2.getChild("tail3");
        this.tail4 = this.tail3.getChild("tail4");
        this.tail5 = this.tail4.getChild("tail5");
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
        whole.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        if (!transitioning) {
            upperBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
            lowerBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
            return;
        }
        renderVesperOnly(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        if (mountOpacity > 0.01F) {
            int baseAlpha = packedColor >>> 24;
            int mountColor = (Mth.clamp(Math.round(baseAlpha * mountOpacity), 0, 255) << 24)
                    | (packedColor & 0x00FFFFFF);
            renderMountAssembly(poseStack, vertexConsumer, packedLight, packedOverlay, mountColor);
        }
    }

    public void renderVesperOnly(PoseStack poseStack, VertexConsumer vertexConsumer,
            int packedLight, int packedOverlay, int packedColor) {
        poseStack.pushPose();
        upperBody.translateAndRotate(poseStack);
        hasturForm.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        poseStack.popPose();
    }

    public void renderMountAssembly(PoseStack poseStack, VertexConsumer vertexConsumer,
            int packedLight, int packedOverlay, int packedColor) {
        boolean vesperVisible = hasturForm.visible;
        float oldXScale = upperBody.xScale;
        float oldYScale = upperBody.yScale;
        float oldZScale = upperBody.zScale;
        hasturForm.visible = false;
        upperBody.xScale = mountScale;
        upperBody.yScale = Math.max(0.02F, mountScale * 0.72F);
        upperBody.zScale = mountScale;
        upperBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        lowerBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
        upperBody.xScale = oldXScale;
        upperBody.yScale = oldYScale;
        upperBody.zScale = oldZScale;
        hasturForm.visible = vesperVisible;
    }

    @Override
    public void setupAnim(VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        netHeadYaw = Mth.wrapDegrees(netHeadYaw);
        float frame = entity.tickCount + HLClientUtils.getPartialTicks();
		this.upperBody.y = DEFAULT_RIDER_Y;
		this.upperBody.xRot = 0.0F;
        this.upperBody.xScale = 1.0F;
        this.upperBody.yScale = 1.0F;
        this.upperBody.zScale = 1.0F;
        this.hasturForm.x = 0.0F;
        this.hasturForm.y = 0.0F;
        this.hasturForm.z = 0.0F;
        this.hasturForm.xRot = 0.0F;
        this.hasturForm.zRot = 0.0F;
        this.lowerBody.y = DEFAULT_MOUNT_Y;
        this.lowerBody.z = DEFAULT_MOUNT_Z;
        this.lowerBody.xScale = 1.0F;
        this.lowerBody.yScale = 1.0F;
        this.lowerBody.zScale = 1.0F;
        this.transitioning = entity.getTransitionTick() > 0;
        this.mountOpacity = 1.0F;
        this.mountScale = 1.0F;
        this.head.xRot = headPitch * ((float) Math.PI / 180F) * 0.5f;
        this.head.yRot = netHeadYaw * ((float) Math.PI / 180F) * 0.5f;

        this.fLLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / 2;
        this.fLTibia.xRot = Math
                .abs(Mth.sin(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
        this.bLLeg.xRot = Mth.sin(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / 2;
        this.bLTibia.xRot = Math
                .abs(Mth.cos(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
        this.fRLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / 2;
        this.fRTibia.xRot = Math
                .abs(Mth.sin(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
        this.bRLeg.xRot = Mth.sin(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / 2;
        this.bRTibia.xRot = Math
                .abs(Mth.cos(limbSwing * 0.1662F + (float) Math.PI) * 1.4F * limbSwingAmount);
/*
		this.fLArm.rotateAngleX = -(float) (Math.cos((frame) * 0.13f) * 0.0325);
		this.fRArm.rotateAngleX = -(float) (Math.sin((frame) * 0.13f) * 0.0325);*/

        this.bLFemur.xRot = -(float) (Math.sin((frame) * 0.13f) * 0.0325);
        this.bRFemur.xRot = -(float) (Math.sin((frame) * 0.13f) * 0.0325);
        this.fLFemur.xRot = -(float) (Math.sin((frame) * 0.13f) * 0.0325);
        this.fRFemur.xRot = -(float) (Math.sin((frame) * 0.13f) * 0.0325);

        this.topJaw.xRot = -(float) (Math.sin((frame) * 0.13f) * 0.0725);
        this.bottomJaw.xRot = (float) (Math.sin((frame) * 0.13f) * 0.0725);

        this.tail.yRot = (float) (Math.sin((frame) * 0.13f) * 0.0325);
        this.tail2.yRot = -(float) (Math.sin((frame) * 0.13f) * 0.12325);
        this.tail3.yRot = (float) (Math.sin((frame) * 0.13f) * 0.1225);
        this.tail4.yRot = -(float) (Math.sin((frame) * 0.13f) * 0.1325) + 45;
        this.tail5.yRot = -(float) (Math.sin((frame) * 0.13f) * 0.12325) + 45;

		float attackFrame = entity.getAttackTick() + HLClientUtils.getPartialTicks();
		if (entity.getAttack() == com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseOneAttack.STINGER_SCRIPT) {
			float strike = VesperWeaponAnimationRules.stingerMotion(attackFrame);
			this.tail.xRot = -0.35F * strike;
			this.tail2.xRot = -0.45F * strike;
			this.tail3.xRot = -0.55F * strike;
		}
		if (entity.getAttack() == com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseOneAttack.BROOD_TRAMPLE) {
			this.lowerBody.xRot = VesperWeaponAnimationRules.broodTramplePitch(attackFrame);
		} else {
			this.lowerBody.xRot = 0.0F;
		}
		if (entity.getActiveAnchor() >= 0) {
			this.upperBody.y = VULNERABLE_RIDER_Y;
			this.upperBody.xRot = 0.18F;
			this.lowerBody.xRot = VULNERABLE_MOUNT_PITCH;
			this.upperBody.zRot = Mth.sin(frame * 0.2F) * 0.035F;
		} else {
			this.upperBody.zRot = 0.0F;
		}
        if (transitioning) {
            float transitionFrame = entity.getTransitionTick() + HLClientUtils.getPartialTicks();
            float dismount = smooth(VesperPhaseTransitionRules.dismountProgress(transitionFrame));
            float absorption = smooth(VesperPhaseTransitionRules.absorptionProgress(transitionFrame));
            float groundedOffset = dismount * (1.0F - absorption);
            this.upperBody.xRot = 0.0F;
            this.upperBody.zRot = 0.0F;
            this.hasturForm.y = 18.0F * groundedOffset;
            this.hasturForm.z = -14.0F * groundedOffset;
            this.hasturForm.xRot = -0.16F * Mth.sin(dismount * Mth.PI)
                    - 0.18F * Mth.sin(absorption * Mth.PI);
            this.hasturForm.zRot = 0.08F * Mth.sin(dismount * Mth.PI);
            this.mountOpacity = 1.0F - absorption;
            this.mountScale = Math.max(0.03F, 1.0F - absorption * 0.97F);
            this.lowerBody.y = Mth.lerp(absorption, DEFAULT_MOUNT_Y, DEFAULT_RIDER_Y);
            this.lowerBody.z = Mth.lerp(absorption, DEFAULT_MOUNT_Z, -19.0F);
            this.lowerBody.xScale = mountScale;
            this.lowerBody.yScale = Math.max(0.015F, mountScale * 0.58F);
            this.lowerBody.zScale = mountScale;
        }
    }

    private static float smooth(float progress) {
        return progress * progress * (3.0F - 2.0F * progress);
    }
}
