package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.HemojellyEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class HemojellyModel extends EntityModel<HemojellyEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("hemojelly"), "main");

	private final ModelPart bell;
	private final ModelPart innerBell;
	private final ModelPart tentacle1;
	private final ModelPart tentacle2;
	private final ModelPart tentacle3;
	private final ModelPart tentacle4;
	private final ModelPart tentacle5;

	public HemojellyModel(ModelPart root) {
		this.bell = root.getChild("bell");
		this.innerBell = root.getChild("innerBell");
		this.tentacle1 = root.getChild("tentacle1");
		this.tentacle2 = root.getChild("tentacle2");
		this.tentacle3 = root.getChild("tentacle3");
		this.tentacle4 = root.getChild("tentacle4");
		this.tentacle5 = root.getChild("tentacle5");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Outer bell — dome shape
		partdefinition.addOrReplaceChild("bell", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 14).addBox(-3.0F, -8.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));

		// Inner bell — slightly smaller, translucent core
		partdefinition.addOrReplaceChild("innerBell", CubeListBuilder.create()
				.texOffs(24, 14).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 14.0F, 0.0F));

        // Four short joints preserve the existing UV strips while reaching two blocks below the bell.
        float[][] offsets = {{0,0}, {-2.5F,-2.5F}, {2.5F,-2.5F}, {-2.5F,2.5F}, {2.5F,2.5F}};
        for (int i = 0; i < offsets.length; i++) {
            var parent = partdefinition.addOrReplaceChild("tentacle" + (i+1), CubeListBuilder.create()
                    .texOffs(32,0).addBox(-.5F,0,-.5F,1,8,1), PartPose.offset(offsets[i][0],14,offsets[i][1]));
            for (String joint : new String[]{"middle", "lower", "tip"})
                parent = parent.addOrReplaceChild(joint, CubeListBuilder.create()
                        .texOffs(32,0).addBox(-.5F,0,-.5F,1,8,1), PartPose.offset(0,8,0));
        }

		return LayerDefinition.create(meshdefinition, 64, 32);
	}

	@Override
	public void setupAnim(HemojellyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// Gentle pulsing of the bell
		float pulse = (float) Math.sin(ageInTicks * 0.15F) * 0.05F;
		this.bell.y = 14.0F + pulse * 8.0F;
		this.innerBell.y = 14.0F + pulse * 8.0F;

        float gather = entity.captureTicks() > 0 ? (float)Math.sin((30-entity.captureTicks()) * Math.PI / 60) : 0;
        ModelPart[] tentacles = {tentacle1, tentacle2, tentacle3, tentacle4, tentacle5};
        for (int i = 0; i < tentacles.length; i++) {
            var tentacle = tentacles[i];
            tentacle.xRot = (float)Math.sin(ageInTicks*.08F+i) * .06F - Math.signum(tentacle.z)*gather*.08F;
            tentacle.zRot = (float)Math.cos(ageInTicks*.07F+i) * .06F + Math.signum(tentacle.x)*gather*.08F;
            var joint = tentacle;
            for (String name : new String[]{"middle", "lower", "tip"}) {
                joint = joint.getChild(name);
                joint.xRot = (float)Math.sin(ageInTicks*.09F+i) * .035F + gather*.05F;
                joint.zRot = (float)Math.cos(ageInTicks*.08F+i) * .035F;
            }
        }
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		bell.render(poseStack, buffer, packedLight, packedOverlay);
		innerBell.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle1.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle2.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle3.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle4.render(poseStack, buffer, packedLight, packedOverlay);
		tentacle5.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
