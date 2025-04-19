package com.vincenthuto.hemomancy.client.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;

public class SuspendedBloodCrystalModel extends Model {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("suspended_blood_crystal"),
			"main");
	private final ModelPart crystal;

	public SuspendedBloodCrystalModel(ModelPart root) {
		super(RenderType::entityTranslucent);
        this.crystal = root.getChild("crystal");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition crystal = partdefinition.addOrReplaceChild("crystal", CubeListBuilder.create().texOffs(0, 7).addBox(-0.4F, -0.3F, -5.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 15.3F, -0.5F, 1.5708F, 0.0F, 0.0F));

		PartDefinition cube_r1 = crystal.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(8, 11).mirror().addBox(-1.5F, -0.25F, -2.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -1.35F, -1.0F, 0.22F, -0.1278F, -0.0285F));

		PartDefinition cube_r2 = crystal.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 7).addBox(-2.5F, -0.25F, -4.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.2F, -0.05F, 0.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition cube_r3 = crystal.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 16).addBox(-0.5F, -0.25F, -4.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.7F, 0.2F, 0.0F, 0.0F, 0.1745F, 0.0F));

		PartDefinition cube_r4 = crystal.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 7).addBox(1.5F, -0.25F, -2.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 1.05F, 1.0F, -0.2618F, 0.0F, 0.0F));

		PartDefinition cube_r5 = crystal.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 7).addBox(1.5F, -0.25F, -2.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.8F, 1.15F, 0.0F, -0.1515F, -0.0869F, -0.517F));

		PartDefinition cube_r6 = crystal.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(3, 10).addBox(0.5F, -1.25F, -3.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.65F, -0.15F, 0.0F, 0.132F, 0.1298F, 0.0172F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}


	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		crystal.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

}