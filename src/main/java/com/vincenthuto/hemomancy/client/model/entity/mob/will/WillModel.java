package com.vincenthuto.hemomancy.client.model.entity.mob.will;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class WillModel extends EntityModel<WillEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("will"), "main");

	private final ModelPart body;
	private final ModelPart head;

	public WillModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = body.getChild("head");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(0, 16)
						.addBox(-3.5F, -15.0F, -2.0F, 7.0F, 15.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(24, 18)
						.addBox(-5.0F, -13.0F, 0.0F, 2.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(24, 18)
						.addBox(3.0F, -13.0F, 0.0F, 2.0F, 11.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		body.addOrReplaceChild("head",
				CubeListBuilder.create()
						.texOffs(0, 0)
						.addBox(-4.0F, -23.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(32, 0)
						.addBox(-5.0F, -24.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(WillEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		head.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
		head.xRot = headPitch * ((float) Math.PI / 180.0F);
		body.yRot = 0.04F * (float) Math.sin(ageInTicks * 0.08F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
