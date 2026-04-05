package com.vincenthuto.hemomancy.client.model.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.Entity;

public class HemolymphopodaModel<T extends Entity> extends EntityModel<T> {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("modelhemolymphopoda"), "main");

	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart leftLegs;
	private final ModelPart rightLegs;

	public HemolymphopodaModel(ModelPart root) {
		this.body = root.getChild("body");
		this.head = root.getChild("head");
		this.tail = root.getChild("tail");
		this.leftLegs = root.getChild("leftLegs");
		this.rightLegs = root.getChild("rightLegs");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Main body - segmented oval shape
		partdefinition.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(0, 10).addBox(-2.5F, -3.0F, -3.0F, 5.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// Head with antennae
		partdefinition.addOrReplaceChild("head",
				CubeListBuilder.create()
						.texOffs(20, 0).addBox(-2.0F, -1.5F, -2.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(20, 4).addBox(-1.0F, -2.5F, -3.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(24, 4).addBox(1.0F, -2.5F, -3.0F, 0.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, -4.0F));

		// Tail segments
		partdefinition.addOrReplaceChild("tail",
				CubeListBuilder.create()
						.texOffs(0, 17).addBox(-2.0F, -1.5F, 0.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(14, 17).addBox(-1.0F, -1.0F, 3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 4.0F));

		// Left legs (3 pairs)
		partdefinition.addOrReplaceChild("leftLegs",
				CubeListBuilder.create()
						.texOffs(0, 22).addBox(3.0F, -1.0F, -3.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(0, 23).addBox(3.0F, -1.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(0, 24).addBox(3.0F, -1.0F, 3.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		// Right legs (3 pairs)
		partdefinition.addOrReplaceChild("rightLegs",
				CubeListBuilder.create()
						.texOffs(4, 22).addBox(-5.0F, -1.0F, -3.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(4, 23).addBox(-5.0F, -1.0F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(4, 24).addBox(-5.0F, -1.0F, 3.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		// Simple leg animation
		float legSwing = (float) Math.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount;
		this.leftLegs.zRot = legSwing;
		this.rightLegs.zRot = -legSwing;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
		head.render(poseStack, buffer, packedLight, packedOverlay);
		tail.render(poseStack, buffer, packedLight, packedOverlay);
		leftLegs.render(poseStack, buffer, packedLight, packedOverlay);
		rightLegs.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
