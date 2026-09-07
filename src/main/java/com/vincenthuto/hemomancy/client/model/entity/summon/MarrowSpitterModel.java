package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

public class MarrowSpitterModel extends EntityModel<MarrowSpitterEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "marrow_spitter"), "main");

	private final ModelPart root;
	private final ModelPart nozzle;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart rearLeg;
	private final ModelPart tubing;

	public MarrowSpitterModel(ModelPart root) {
		this.root = root.getChild("root");
		this.nozzle = this.root.getChild("nozzle");
		this.leftLeg = this.root.getChild("left_leg");
		this.rightLeg = this.root.getChild("right_leg");
		this.rearLeg = this.root.getChild("rear_leg");
		this.tubing = this.root.getChild("tubing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3F, -10F, -2F, 6F, 10F, 5F)
				.texOffs(92, 0).addBox(-2.5F, -11F, -1.5F, 5F, 1F, 4F)
				.texOffs(46, 0).addBox(-4F, -1F, -2.5F, 8F, 2F, 6F),
				PartPose.offsetAndRotation(0F, 16F, 0F, 0F, 0F, 0F));

		PartDefinition rightCarriage = root.addOrReplaceChild("right_carriage", CubeListBuilder.create()
				.texOffs(22, 0).addBox(-0.5F, 0F, -1F, 1F, 9F, 2F),
				PartPose.offsetAndRotation(-3F, -9F, 0F, 0F, 0F, -0.08F));

		PartDefinition braceR_0 = root.addOrReplaceChild("brace_r_0", CubeListBuilder.create()
				.texOffs(52, 8).addBox(-1.5F, 0F, -1F, 3F, 1F, 1F),
				PartPose.offsetAndRotation(-2F, -8F, -2F, 0F, 0.15F, -0.12F));

		PartDefinition braceR_1 = root.addOrReplaceChild("brace_r_1", CubeListBuilder.create()
				.texOffs(52, 8).addBox(-1.5F, 0F, -1F, 3F, 1F, 1F),
				PartPose.offsetAndRotation(-2F, -5F, -2F, 0F, 0.15F, -0.12F));

		PartDefinition braceR_2 = root.addOrReplaceChild("brace_r_2", CubeListBuilder.create()
				.texOffs(52, 8).addBox(-1.5F, 0F, -1F, 3F, 1F, 1F),
				PartPose.offsetAndRotation(-2F, -2F, -2F, 0F, 0.15F, -0.12F));

		PartDefinition leftCarriage = root.addOrReplaceChild("left_carriage", CubeListBuilder.create()
				.texOffs(22, 0).addBox(-0.5F, 0F, -1F, 1F, 9F, 2F),
				PartPose.offsetAndRotation(3F, -9F, 0F, 0F, 0F, 0.08F));

		PartDefinition braceL_0 = root.addOrReplaceChild("brace_l_0", CubeListBuilder.create()
				.texOffs(52, 8).addBox(-1.5F, 0F, -1F, 3F, 1F, 1F),
				PartPose.offsetAndRotation(2F, -8F, -2F, 0F, -0.15F, 0.12F));

		PartDefinition braceL_1 = root.addOrReplaceChild("brace_l_1", CubeListBuilder.create()
				.texOffs(52, 8).addBox(-1.5F, 0F, -1F, 3F, 1F, 1F),
				PartPose.offsetAndRotation(2F, -5F, -2F, 0F, -0.15F, 0.12F));

		PartDefinition braceL_2 = root.addOrReplaceChild("brace_l_2", CubeListBuilder.create()
				.texOffs(52, 8).addBox(-1.5F, 0F, -1F, 3F, 1F, 1F),
				PartPose.offsetAndRotation(2F, -2F, -2F, 0F, -0.15F, 0.12F));

		PartDefinition nozzle = root.addOrReplaceChild("nozzle", CubeListBuilder.create()
				.texOffs(74, 0).addBox(-2F, -2F, -2F, 4F, 4F, 3F)
				.texOffs(28, 0).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 6F)
				.texOffs(110, 0).addBox(-2F, -2F, -5F, 4F, 4F, 1F)
				.texOffs(122, 5).addBox(-1F, -1F, -8.1F, 2F, 2F, 1F),
				PartPose.offsetAndRotation(0F, -5F, -2.5F, 0F, 0F, 0F));

		PartDefinition upperMuzzle = nozzle.addOrReplaceChild("upper_muzzle", CubeListBuilder.create()
				.texOffs(102, 5).addBox(-1.5F, -0.5F, -3F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(0F, -1.5F, -7.5F, 0.1F, 0F, 0F));

		PartDefinition lowerMuzzle = nozzle.addOrReplaceChild("lower_muzzle", CubeListBuilder.create()
				.texOffs(102, 5).addBox(-1.5F, -0.5F, -3F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 1.5F, -7.5F, -0.1F, 0F, 0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(114, 5).addBox(-1F, -1F, -1F, 2F, 2F, 2F)
				.texOffs(120, 0).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
				PartPose.offsetAndRotation(3F, 0F, -1F, 0.06F, 0F, -0.27F));

		PartDefinition leftLegStilt = leftLeg.addOrReplaceChild("left_leg_stilt", CubeListBuilder.create()
				.texOffs(84, 7).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F)
				.texOffs(120, 0).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F)
				.texOffs(74, 7).addBox(-1F, 3.5F, -2F, 2F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 3.5F, 0F, -0.06F, 0F, 0.135F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(114, 5).addBox(-1F, -1F, -1F, 2F, 2F, 2F)
				.texOffs(120, 0).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
				PartPose.offsetAndRotation(-3F, 0F, -1F, 0.06F, 0F, 0.27F));

		PartDefinition rightLegStilt = rightLeg.addOrReplaceChild("right_leg_stilt", CubeListBuilder.create()
				.texOffs(84, 7).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F)
				.texOffs(120, 0).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F)
				.texOffs(74, 7).addBox(-1F, 3.5F, -2F, 2F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 3.5F, 0F, -0.06F, 0F, -0.135F));

		PartDefinition rearLeg = root.addOrReplaceChild("rear_leg", CubeListBuilder.create()
				.texOffs(114, 5).addBox(-1F, -1F, -1F, 2F, 2F, 2F)
				.texOffs(120, 0).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
				PartPose.offsetAndRotation(0F, 0F, 2F, 0.35F, 0F, 0F));

		PartDefinition rearLegStilt = rearLeg.addOrReplaceChild("rear_leg_stilt", CubeListBuilder.create()
				.texOffs(84, 7).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F)
				.texOffs(120, 0).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F)
				.texOffs(74, 7).addBox(-1F, 3.5F, -2F, 2F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 3.5F, 0F, -0.35F, 0F, 0F));

		PartDefinition tubing = root.addOrReplaceChild("tubing", CubeListBuilder.create()
				.texOffs(28, 9).addBox(-4F, 0F, 0F, 8F, 1F, 1F)
				.texOffs(88, 0).addBox(-4F, 0F, 0F, 1F, 6F, 1F)
				.texOffs(88, 0).addBox(3F, 0F, 0F, 1F, 6F, 1F)
				.texOffs(28, 9).addBox(-4F, 5F, 0F, 8F, 1F, 1F),
				PartPose.offsetAndRotation(0F, -8F, 2.5F, 0F, 0F, 0F));

		PartDefinition feedHose = root.addOrReplaceChild("feed_hose", CubeListBuilder.create()
				.texOffs(92, 5).addBox(0F, 0F, -4F, 1F, 1F, 4F)
				.texOffs(46, 8).addBox(-0.5F, -0.5F, -3F, 2F, 2F, 1F),
				PartPose.offsetAndRotation(3F, -4F, -2F, 0F, 0F, 0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(MarrowSpitterEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.25F;
		this.nozzle.xRot = headPitch * Mth.DEG_TO_RAD * 0.45F + Mth.sin(ageInTicks * 0.2F) * 0.025F;
		this.tubing.xScale = 1.0F + Mth.sin(ageInTicks * 0.18F) * 0.04F;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 0.65F * limbSwingAmount;
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 0.65F * limbSwingAmount;
		this.rearLeg.xRot = Mth.sin(limbSwing * 0.6662F) * 0.35F * limbSwingAmount;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
