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

	private final ModelPart prosoma;
	private final ModelPart opisthosoma;
	private final ModelPart telson;
	private final ModelPart leftLegs;
	private final ModelPart rightLegs;

	public HemolymphopodaModel(ModelPart root) {
		this.prosoma = root.getChild("prosoma");
		this.opisthosoma = root.getChild("opisthosoma");
		this.telson = root.getChild("telson");
		this.leftLegs = root.getChild("leftLegs");
		this.rightLegs = root.getChild("rightLegs");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Prosoma (front carapace) - large, wide, dome-shaped horseshoe crab shell
		partdefinition.addOrReplaceChild("prosoma",
				CubeListBuilder.create()
						// Main shell dome
						.texOffs(0, 0).addBox(-5.0F, -2.0F, -6.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F))
						// Raised dome top
						.texOffs(0, 12).addBox(-4.0F, -3.5F, -5.0F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
						// Front lip/rim of horseshoe shape
						.texOffs(0, 22).addBox(-5.5F, -1.5F, -7.0F, 11.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
						// Eyes on top of prosoma
						.texOffs(40, 0).addBox(-3.0F, -4.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(44, 0).addBox(2.0F, -4.0F, -3.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, -2.0F));

		// Opisthosoma (abdomen) - smaller, hexagonal section with side spines
		partdefinition.addOrReplaceChild("opisthosoma",
				CubeListBuilder.create()
						// Main abdomen plate
						.texOffs(0, 25).addBox(-4.0F, -1.5F, 0.0F, 8.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
						// Raised ridge
						.texOffs(24, 12).addBox(-3.0F, -2.5F, 0.5F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
						// Left movable spines
						.texOffs(0, 33).addBox(-5.0F, -1.0F, 1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
						// Right movable spines
						.texOffs(10, 33).addBox(4.0F, -1.0F, 1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 4.0F));

		// Telson (tail spine) - long, pointed
		partdefinition.addOrReplaceChild("telson",
				CubeListBuilder.create()
						.texOffs(30, 0).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 10.0F));

		// Left legs (5 pairs under prosoma)
		partdefinition.addOrReplaceChild("leftLegs",
				CubeListBuilder.create()
						.texOffs(20, 25).addBox(4.0F, -1.0F, -5.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(20, 26).addBox(4.0F, -1.0F, -3.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(20, 27).addBox(4.0F, -1.0F, -1.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(20, 28).addBox(3.5F, -1.0F, 1.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(20, 29).addBox(3.0F, -1.0F, 3.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, -2.0F));

		// Right legs (5 pairs under prosoma)
		partdefinition.addOrReplaceChild("rightLegs",
				CubeListBuilder.create()
						.texOffs(26, 25).addBox(-7.0F, -1.0F, -5.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(26, 26).addBox(-7.0F, -1.0F, -3.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(26, 27).addBox(-7.0F, -1.0F, -1.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(26, 28).addBox(-6.5F, -1.0F, 1.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
						.texOffs(26, 29).addBox(-6.0F, -1.0F, 3.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, -2.0F));

		return LayerDefinition.create(meshdefinition, 48, 40);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		// Leg walking animation
		float legSwing = (float) Math.cos(limbSwing * 0.6662F) * 0.4F * limbSwingAmount;
		this.leftLegs.zRot = legSwing;
		this.rightLegs.zRot = -legSwing;
		// Subtle telson sway
		this.telson.yRot = (float) Math.sin(ageInTicks * 0.05F) * 0.1F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		prosoma.render(poseStack, buffer, packedLight, packedOverlay);
		opisthosoma.render(poseStack, buffer, packedLight, packedOverlay);
		telson.render(poseStack, buffer, packedLight, packedOverlay);
		leftLegs.render(poseStack, buffer, packedLight, packedOverlay);
		rightLegs.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
