package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VenousStriderEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class VenousStriderModel extends EntityModel<VenousStriderEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("venous_strider_model"), "main");

    private final ModelPart body;
    private final ModelPart neck2;
    private final ModelPart neck1;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart crest;
    private final ModelPart wing1;
    private final ModelPart leg1;
    private final ModelPart leg2;
    private final ModelPart wing2;

    public VenousStriderModel(ModelPart root) {
        this.body = root.getChild("body");
        this.neck2 = this.body.getChild("neck2");
        this.neck1 = this.neck2.getChild("neck1");
        this.head = this.neck1.getChild("head");
        this.beak = this.head.getChild("beak");
        this.crest = this.head.getChild("crest");
        this.wing1 = this.body.getChild("wing1");
        this.leg1 = this.body.getChild("leg1");
        this.leg2 = this.body.getChild("leg2");
        this.wing2 = this.body.getChild("wing2");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -4.25F, 3.0F, 6.0F, 14.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.25F, 14.5F, 0.5F, 1.5708F, 0.0F, 0.0F));

        PartDefinition neck2 = body.addOrReplaceChild("neck2", CubeListBuilder.create().texOffs(54, 18).addBox(-1.5F, -3.8333F, 0.6667F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(17, 41).addBox(-1.5F, -2.3333F, -0.8333F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, -3.6667F, 6.8333F));

        PartDefinition neck1 = neck2.addOrReplaceChild("neck1", CubeListBuilder.create().texOffs(0, 53).addBox(-1.5F, 1.0F, 1.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.8333F, 4.1667F));

        PartDefinition head = neck1.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 41).addBox(-1.5F, -5.5F, -3.25F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.5F, 5.75F));

        PartDefinition beak = head.addOrReplaceChild("beak", CubeListBuilder.create().texOffs(24, 48).addBox(0.0F, -30.5F, -3.0F, 0.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, -0.25F));

        PartDefinition crest = head.addOrReplaceChild("crest", CubeListBuilder.create().texOffs(17, 48).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 12.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 1.2485F, -0.2618F, 0.0F, 0.0F));

        PartDefinition wing1 = body.addOrReplaceChild("wing1", CubeListBuilder.create().texOffs(34, 39).addBox(-0.75F, -1.5F, -6.75F, 1.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(5.25F, -1.5F, 8.25F));

        PartDefinition leg1 = body.addOrReplaceChild("leg1", CubeListBuilder.create().texOffs(0, 23).addBox(-1.0F, -1.5F, -14.0F, 2.0F, 3.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(2.75F, 0.0F, 4.5F));

        PartDefinition leg2 = body.addOrReplaceChild("leg2", CubeListBuilder.create().texOffs(29, 0).addBox(-1.0F, -1.5F, -14.0F, 2.0F, 3.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.0F, 4.5F));

        PartDefinition wing2 = body.addOrReplaceChild("wing2", CubeListBuilder.create().texOffs(33, 18).addBox(-1.0F, 1.5F, 1.5F, 1.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, -4.5F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void setupAnim(VenousStriderEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.neck2.zRot = netHeadYaw * ((float) Math.PI / 180F);

//		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
//		this.head.xRot = headPitch * ((float) Math.PI / 180F);
//		this.neck.xRot = 0.2618F + headPitch * ((float) Math.PI / 180F) * 0.5F;
//
//		// Slow, deliberate leg movement
        this.leg1.xRot = (float) Math.cos(limbSwing * 0.6662F) * 0.8F * limbSwingAmount;
        this.leg2.xRot = (float) Math.cos(limbSwing * 0.6662F + (float) Math.PI) * 0.8F * limbSwingAmount;
//
//		// Subtle wing flutter when idle
        float wingFlutter = (float) Math.sin(ageInTicks * 0.067F) * 0.05F;
        this.wing1.yRot = -wingFlutter;
        this.wing2.yRot = wingFlutter;
//
//		// Gentle tail sway
//		this.tailFeathers.yRot = (float) Math.sin(ageInTicks * 0.05F) * 0.1F;
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        body.render(poseStack, buffer, packedLight, packedOverlay);
//		neck.render(poseStack, buffer, packedLight, packedOverlay);
//		head.render(poseStack, buffer, packedLight, packedOverlay);
//		leftLeg.render(poseStack, buffer, packedLight, packedOverlay);
//		rightLeg.render(poseStack, buffer, packedLight, packedOverlay);
//		leftWing.render(poseStack, buffer, packedLight, packedOverlay);
//		rightWing.render(poseStack, buffer, packedLight, packedOverlay);
//		tailFeathers.render(poseStack, buffer, packedLight, packedOverlay);
    }
}
