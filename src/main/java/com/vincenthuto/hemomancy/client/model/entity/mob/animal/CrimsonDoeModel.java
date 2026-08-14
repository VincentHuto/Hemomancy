package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.CrimsonDoeEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class CrimsonDoeModel extends HierarchicalModel<CrimsonDoeEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("crimson_doe"), "main");

	private final ModelPart body;


	public CrimsonDoeModel(ModelPart root) {
		this.body = root.getChild("body");
	}


	@Override
	public ModelPart root() {
		return this.body;
	}


	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.3333F, -7.9321F, -3.6002F, 5.0F, 16.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1667F, 9.8535F, 0.1314F, 1.5708F, 0.0F, 0.0F));

		PartDefinition brLeg = body.addOrReplaceChild("brLeg", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.3333F, 5.1465F, -1.6314F, -1.5708F, 0.0F, 0.0F));

		PartDefinition leg0_r1 = brLeg.addOrReplaceChild("leg0_r1", CubeListBuilder.create().texOffs(12, 22).addBox(-1.0F, -3.0F, -2.5F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.5F, 0.5F, -0.3491F, 0.0F, 0.0F));

		PartDefinition bone = brLeg.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(14, 32).addBox(-1.0F, -0.3328F, -1.3349F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, 2.5F, -1.5F, 0.2182F, 0.0F, 0.0F));

		PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(38, 0).addBox(-0.5F, 6.0F, -3.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.536F, 2.1247F));

		PartDefinition bone5 = bone2.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(44, 0).addBox(0.0F, -0.2706F, -0.7796F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 11.0F, -2.25F, -0.2182F, 0.0F, 0.0F));

		PartDefinition frLeg = body.addOrReplaceChild("frLeg", CubeListBuilder.create().texOffs(22, 7).addBox(-0.5F, -1.0F, -2.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.3333F, -5.3535F, 0.3686F, -1.5708F, 0.0F, 0.0F));

		PartDefinition bone6 = frLeg.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(36, 28).addBox(0.0F, -0.5F, -1.0F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -0.75F));

		PartDefinition bone7 = bone6.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(24, 37).addBox(0.0F, 0.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.5F, 0.0F));

		PartDefinition blLeg = body.addOrReplaceChild("blLeg", CubeListBuilder.create(), PartPose.offsetAndRotation(2.6667F, 5.1465F, -1.6314F, -1.5708F, 0.0F, 0.0F));

		PartDefinition leg0_r2 = blLeg.addOrReplaceChild("leg0_r2", CubeListBuilder.create().texOffs(24, 17).addBox(-1.0F, -3.0F, -2.5F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 0.5F, 0.5F, -0.3491F, 0.0F, 0.0F));

		PartDefinition bone3 = blLeg.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.5F, 2.5F, -1.5F));

		PartDefinition bone4 = bone3.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offset(0.0F, -2.536F, 2.1247F));

		PartDefinition bone8 = blLeg.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(36, 20).addBox(-1.0F, -0.3328F, -1.3349F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.25F, 2.5F, -1.5F, 0.2182F, 0.0F, 0.0F));

		PartDefinition bone9 = bone8.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(14, 40).addBox(-0.5F, 6.0F, -3.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.536F, 2.1247F));

		PartDefinition bone10 = bone9.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(20, 44).addBox(-1.0F, -0.2706F, -0.7796F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, 11.0F, -2.25F, -0.2182F, 0.0F, 0.0F));

		PartDefinition flLeg = body.addOrReplaceChild("flLeg", CubeListBuilder.create().texOffs(24, 27).addBox(-1.5F, -1.0F, -2.0F, 2.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.6667F, -5.3535F, 0.3686F, -1.5708F, 0.0F, 0.0F));

		PartDefinition bone11 = flLeg.addOrReplaceChild("bone11", CubeListBuilder.create().texOffs(36, 36).addBox(-1.0F, -0.5F, -1.0F, 1.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -0.75F));

		PartDefinition bone12 = bone11.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(30, 37).addBox(-1.0F, 0.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.5F, 0.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(34, 7).addBox(-0.5F, 0.0F, -5.25F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.3333F, 8.0679F, 2.1498F));

		PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 22).addBox(-1.5F, -8.0348F, -0.726F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1667F, -8.1358F, -1.5495F, -1.2654F, 0.0F, 0.0F));

		PartDefinition Head = neck.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(22, 0).addBox(-2.0F, -3.2472F, -1.7965F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 35).addBox(-1.5F, -1.8791F, -5.5553F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.482F, 0.7074F));

		PartDefinition ear2 = Head.addOrReplaceChild("ear2", CubeListBuilder.create().texOffs(6, 41).addBox(0.0F, -3.0463F, -0.3007F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, -3.2472F, 1.2035F));

		PartDefinition ear = Head.addOrReplaceChild("ear", CubeListBuilder.create().texOffs(0, 41).addBox(-2.0F, -3.0463F, -0.3007F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, -3.2472F, 1.2035F));

		PartDefinition bone13 = Head.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(36, 14).addBox(-1.5F, -1.4527F, -1.0675F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -0.8791F, -4.0553F, 0.3491F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(CrimsonDoeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
