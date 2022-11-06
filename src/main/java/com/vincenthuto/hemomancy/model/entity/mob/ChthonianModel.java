package com.vincenthuto.hemomancy.model.entity.mob;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;


public class ChthonianModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "modelchthonian"), "main");
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition abdomen = partdefinition.addOrReplaceChild("abdomen",
				CubeListBuilder.create().texOffs(0, 17)
						.addBox(-1.5F, -1.45F, 5.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 9)
						.addBox(-2.0F, -2.35F, 2.0F, 4.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 0)
						.addBox(-2.5F, -3.25F, 0.0F, 5.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 19.5F, 3.5F));

		PartDefinition thorax = partdefinition.addOrReplaceChild("thorax",
				CubeListBuilder.create().texOffs(11, 14)
						.addBox(-2.5F, -2.25F, -1.5F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(14, 25)
						.addBox(-1.5F, -2.75F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(22, 6)
						.addBox(-1.5F, 1.25F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(13, 6)
						.addBox(-2.0F, -1.5F, 2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 24)
						.addBox(-1.5F, -2.0F, 2.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(22, 12)
						.addBox(-1.5F, 1.0F, 2.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(17, 29)
						.addBox(-1.0F, -0.25F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 29)
						.addBox(-1.0F, -0.25F, 1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(28, 3)
						.addBox(-1.0F, -0.25F, 4.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.5F, 19.5F, -2.0F));

		PartDefinition skull = partdefinition.addOrReplaceChild("skull",
				CubeListBuilder.create().texOffs(16, 0)
						.addBox(-1.5F, -1.25F, -3.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(6, 21)
						.addBox(-0.5F, -2.0F, -4.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(13, 0)
						.addBox(-0.5F, -1.0F, -4.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 19.5F, -4.0F));

		PartDefinition lEye = skull.addOrReplaceChild("lEye",
				CubeListBuilder.create().texOffs(13, 28).addBox(-0.5F, 0.2153F, -1.2255F, 1.0F, 1.0F, 2.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, -1.0F, -1.5F, 0.3054F, 0.0F, 0.0F));

		PartDefinition rEye = skull.addOrReplaceChild("rEye",
				CubeListBuilder.create().texOffs(27, 27).addBox(-0.5F, 0.2153F, -1.2255F, 1.0F, 1.0F, 2.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.5F, -1.0F, -1.5F, 0.3054F, 0.0F, 0.0F));

		PartDefinition rFrontLeg = partdefinition.addOrReplaceChild("rFrontLeg",
				CubeListBuilder.create().texOffs(28, 12).addBox(-1.5559F, 0.2244F, -0.5F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.75F, 19.0F, -2.5F, 0.0F, 0.0F, 0.2618F));

		PartDefinition rFrontLeg2 = rFrontLeg.addOrReplaceChild("rFrontLeg2",
				CubeListBuilder.create().texOffs(25, 17).addBox(-3.375F, 0.1495F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.75F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition rFrontLeg3 = rFrontLeg2.addOrReplaceChild(
				"rFrontLeg3", CubeListBuilder.create().texOffs(25, 9).addBox(-3.3326F, 0.1495F, -0.3268F, 3.0F, 1.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, -0.48F, 0.0F));

		PartDefinition rFrontLeg4 = rFrontLeg3.addOrReplaceChild("rFrontLeg4", CubeListBuilder.create().texOffs(0, 23)
				.addBox(-3.3471F, 1.1634F, -0.4564F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition rMidLeg = partdefinition.addOrReplaceChild("rMidLeg",
				CubeListBuilder.create().texOffs(28, 6).addBox(-1.75F, 0.25F, -0.5F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.75F, 19.0F, -0.5F, 0.0F, 0.0F, 0.2618F));

		PartDefinition rMidLeg2 = rMidLeg.addOrReplaceChild("rMidLeg2",
				CubeListBuilder.create().texOffs(25, 0).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.75F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition rMidLeg3 = rMidLeg2.addOrReplaceChild("rMidLeg3",
				CubeListBuilder.create().texOffs(20, 25).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, -0.0873F, 0.0F));

		PartDefinition rMidLeg4 = rMidLeg3.addOrReplaceChild("rMidLeg4", CubeListBuilder.create().texOffs(16, 22)
				.addBox(-3.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition rBackLeg = partdefinition.addOrReplaceChild("rBackLeg",
				CubeListBuilder.create().texOffs(7, 26).addBox(-1.75F, 0.25F, -0.5F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.75F, 19.0F, 2.25F, 0.0F, 0.0F, 0.2618F));

		PartDefinition rBackLeg2 = rBackLeg.addOrReplaceChild("rBackLeg2",
				CubeListBuilder.create().texOffs(22, 15).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.75F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition rBackLeg3 = rBackLeg2
				.addOrReplaceChild("rBackLeg3",
						CubeListBuilder.create().texOffs(14, 12).addBox(-3.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(-3.0F, 0.0F, 0.0F, 0.0F, 0.2618F, 0.0F));

		PartDefinition rBackLeg4 = rBackLeg3.addOrReplaceChild("rBackLeg4", CubeListBuilder.create().texOffs(16, 21)
				.addBox(-3.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, 0.0F, 0.0F));

		PartDefinition lBackLeg = partdefinition.addOrReplaceChild("lBackLeg",
				CubeListBuilder.create().texOffs(6, 30).addBox(0.0F, 0.25F, -0.55F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, 19.0F, 2.3F, 0.0F, 0.0F, -0.2618F));

		PartDefinition lBackLeg2 = lBackLeg.addOrReplaceChild("lBackLeg2",
				CubeListBuilder.create().texOffs(7, 28).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 0.0F, -0.05F, 0.0F, 0.0F, 0.7854F));

		PartDefinition lBackLeg3 = lBackLeg2
				.addOrReplaceChild("lBackLeg3",
						CubeListBuilder.create().texOffs(27, 24).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, -0.2618F, 0.0F));

		PartDefinition lBackLeg4 = lBackLeg3.addOrReplaceChild("lBackLeg4", CubeListBuilder.create().texOffs(24, 11)
				.addBox(0.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition lMidLeg = partdefinition.addOrReplaceChild("lMidLeg",
				CubeListBuilder.create().texOffs(29, 14).addBox(0.0F, 0.25F, -0.55F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, 19.0F, -0.45F, 0.0F, 0.0F, -0.2618F));

		PartDefinition lMidLeg2 = lMidLeg.addOrReplaceChild("lMidLeg2",
				CubeListBuilder.create().texOffs(21, 27).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 0.0F, -0.05F, 0.0F, 0.0F, 0.7854F));

		PartDefinition lMidLeg3 = lMidLeg2
				.addOrReplaceChild("lMidLeg3",
						CubeListBuilder.create().texOffs(0, 27).addBox(0.0F, 0.25F, -0.5F, 3.0F, 1.0F, 1.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.0873F, 0.0F));

		PartDefinition lMidLeg4 = lMidLeg3.addOrReplaceChild("lMidLeg4", CubeListBuilder.create().texOffs(24, 2).addBox(
				0.0F, 1.25F, -0.5F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition lFrontLeg = partdefinition.addOrReplaceChild("lFrontLeg",
				CubeListBuilder.create().texOffs(21, 29).addBox(-0.1941F, 0.2244F, -0.55F, 2.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.5F, 19.0F, -2.45F, 0.0F, 0.0F, -0.2618F));

		PartDefinition lFrontLeg2 = lFrontLeg.addOrReplaceChild("lFrontLeg2",
				CubeListBuilder.create().texOffs(25, 21).addBox(0.375F, 0.1495F, -0.5F, 3.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, 0.0F, -0.05F, 0.0F, 0.0F, 0.7854F));

		PartDefinition lFrontLeg3 = lFrontLeg2.addOrReplaceChild(
				"lFrontLeg3", CubeListBuilder.create().texOffs(25, 19).addBox(0.3326F, 0.1495F, -0.3268F, 3.0F, 1.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.0F, 0.0F, 0.0F, 0.0F, 0.48F, 0.0F));

		PartDefinition lFrontLeg4 = lFrontLeg3.addOrReplaceChild("lFrontLeg4", CubeListBuilder.create().texOffs(16, 23)
				.addBox(0.3471F, 1.1634F, -0.4564F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, 0.0F, 0.0F));

		PartDefinition lMandible = partdefinition.addOrReplaceChild("lMandible",
				CubeListBuilder.create().texOffs(12, 21)
						.addBox(0.0F, -0.45F, -2.75F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(6, 23)
						.addBox(-0.5F, -0.7F, -3.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.5F, 20.7F, -6.25F));

		PartDefinition rMandible = partdefinition.addOrReplaceChild("rMandible",
				CubeListBuilder.create().texOffs(20, 21)
						.addBox(-1.0F, -0.375F, -3.25F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(26, 30)
						.addBox(-0.5F, -0.625F, -3.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.5F, 20.625F, -5.75F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	private final ModelPart abdomen;
	private final ModelPart thorax;
	private final ModelPart skull;
	private final ModelPart rFrontLeg;
	private final ModelPart rMidLeg;
	private final ModelPart rBackLeg;
	private final ModelPart lBackLeg;
	private final ModelPart lMidLeg;
	private final ModelPart lFrontLeg;
	private final ModelPart lMandible;

	private final ModelPart rMandible;

	public ChthonianModel(ModelPart root) {
		this.abdomen = root.getChild("abdomen");
		this.thorax = root.getChild("thorax");
		this.skull = root.getChild("skull");
		this.rFrontLeg = root.getChild("rFrontLeg");
		this.rMidLeg = root.getChild("rMidLeg");
		this.rBackLeg = root.getChild("rBackLeg");
		this.lBackLeg = root.getChild("lBackLeg");
		this.lMidLeg = root.getChild("lMidLeg");
		this.lFrontLeg = root.getChild("lFrontLeg");
		this.lMandible = root.getChild("lMandible");
		this.rMandible = root.getChild("rMandible");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		abdomen.render(poseStack, buffer, packedLight, packedOverlay);
		thorax.render(poseStack, buffer, packedLight, packedOverlay);
		skull.render(poseStack, buffer, packedLight, packedOverlay);
		rFrontLeg.render(poseStack, buffer, packedLight, packedOverlay);
		rMidLeg.render(poseStack, buffer, packedLight, packedOverlay);
		rBackLeg.render(poseStack, buffer, packedLight, packedOverlay);
		lBackLeg.render(poseStack, buffer, packedLight, packedOverlay);
		lMidLeg.render(poseStack, buffer, packedLight, packedOverlay);
		lFrontLeg.render(poseStack, buffer, packedLight, packedOverlay);
		lMandible.render(poseStack, buffer, packedLight, packedOverlay);
		rMandible.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		this.skull.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.skull.xRot = headPitch * ((float) Math.PI / 180F);

		this.lMandible.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.lMandible.xRot = headPitch * ((float) Math.PI / 180F);
		this.rMandible.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.rMandible.xRot = headPitch * ((float) Math.PI / 180F);

	}
}