package com.vincenthuto.hemomancy.model.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

// Made with Blockbench 4.4.0-beta.1
// Exported for Minecraft version 1.17 - 1.18 with Mojang mappings
// Paste this class into your mod and generate all required imports

public class CentrifugeArmsModel extends Model {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "modelcentrifugemodel"), "main");
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition crossBrace2 = partdefinition.addOrReplaceChild("crossBrace2",
				CubeListBuilder.create().texOffs(18, 5)
						.addBox(-2.5F, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(18, 5)
						.addBox(-2.5F, -0.5F, -7.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.9F, 17.6F, 2.9F, 0.0F, 0.7854F, 0.0F));

		PartDefinition crossBrace = partdefinition.addOrReplaceChild("crossBrace",
				CubeListBuilder.create().texOffs(18, 5)
						.addBox(-2.5F, -0.5F, -0.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(18, 5)
						.addBox(-2.5F, -0.5F, -7.5F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.1F, 17.6F, 1.9F, 0.0F, -0.7854F, 0.0F));

		PartDefinition center = partdefinition.addOrReplaceChild("center",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-0.5F, -9.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(4, 27)
						.addBox(-1.5F, -7.5F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition nonCardinalArms = partdefinition.addOrReplaceChild("nonCardinalArms",
				CubeListBuilder.create().texOffs(13, 23)
						.addBox(-6.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 23)
						.addBox(1.0F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 15)
						.addBox(6.25F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)).texOffs(13, 15)
						.addBox(-7.25F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)).texOffs(13, 15)
						.addBox(-0.5F, -0.5F, -7.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)).texOffs(13, 15)
						.addBox(-0.5F, -0.5F, 6.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)).texOffs(18, 21)
						.addBox(-0.5F, -0.5F, -6.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(18, 21)
						.addBox(-0.5F, -0.5F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 17.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cardinalArms = partdefinition.addOrReplaceChild("cardinalArms",
				CubeListBuilder.create().texOffs(13, 23)
						.addBox(-6.0F, -1.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 23)
						.addBox(1.0F, -1.0F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 15)
						.addBox(6.25F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)).texOffs(13, 15)
						.addBox(-7.25F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)).texOffs(13, 15)
						.addBox(-0.5F, -1.0F, -7.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)).texOffs(13, 15)
						.addBox(-0.5F, -1.0F, 6.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.2F)).texOffs(18, 21)
						.addBox(-0.5F, -1.0F, -6.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(18, 21)
						.addBox(-0.5F, -1.0F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		PartDefinition vial1 = partdefinition.addOrReplaceChild("vial1",
				CubeListBuilder.create().texOffs(27, 0)
						.addBox(-0.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offset(0.0F, 17.0F, -6.75F));

		PartDefinition vial1Empty = partdefinition.addOrReplaceChild("vial1Empty",
				CubeListBuilder.create().texOffs(22, 0)
						.addBox(-0.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offset(0.0F, 17.0F, -6.75F));

		PartDefinition vial2 = partdefinition.addOrReplaceChild("vial2",
				CubeListBuilder.create().texOffs(27, 0)
						.addBox(-2.8388F, -3.0F, -0.8536F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-2.8388F, -3.5F, -0.8536F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offsetAndRotation(6.65F, 19.0F, -6.15F, 0.0F, 0.7854F, 0.0F));

		PartDefinition vial2Empty = partdefinition.addOrReplaceChild("vial2Empty",
				CubeListBuilder.create().texOffs(22, 0)
						.addBox(-2.8388F, -3.0F, -0.8536F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-2.8388F, -3.5F, -0.8536F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offsetAndRotation(6.65F, 19.0F, -6.15F, 0.0F, 0.7854F, 0.0F));

		PartDefinition vial3 = partdefinition.addOrReplaceChild("vial3",
				CubeListBuilder.create().texOffs(27, 0)
						.addBox(-0.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offset(6.75F, 17.0F, 0.0F));

		PartDefinition vial3Empty = partdefinition.addOrReplaceChild("vial3Empty",
				CubeListBuilder.create().texOffs(22, 0)
						.addBox(-0.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offset(6.75F, 17.0F, 0.0F));

		PartDefinition vial4 = partdefinition.addOrReplaceChild("vial4",
				CubeListBuilder.create().texOffs(27, 0)
						.addBox(-0.3232F, -3.0F, -0.5407F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.3232F, -3.5F, -0.5407F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offsetAndRotation(4.65F, 19.0F, 4.9F, 0.0F, 0.7854F, 0.0F));

		PartDefinition vial4Empty = partdefinition.addOrReplaceChild("vial4Empty",
				CubeListBuilder.create().texOffs(22, 0)
						.addBox(-0.3232F, -3.0F, -0.5407F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.3232F, -3.5F, -0.5407F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offsetAndRotation(4.65F, 19.0F, 4.9F, 0.0F, 0.7854F, 0.0F));

		PartDefinition vial5 = partdefinition.addOrReplaceChild("vial5",
				CubeListBuilder.create().texOffs(27, 0)
						.addBox(-0.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offset(0.0F, 17.0F, 6.75F));

		PartDefinition vial5Empty = partdefinition.addOrReplaceChild("vial5Empty",
				CubeListBuilder.create().texOffs(22, 0)
						.addBox(-0.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offset(0.0F, 17.0F, 6.75F));

		PartDefinition vial6 = partdefinition.addOrReplaceChild("vial6",
				CubeListBuilder.create().texOffs(27, 0)
						.addBox(-0.2825F, -3.0F, -0.1464F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.2825F, -3.5F, -0.1464F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offsetAndRotation(-5.15F, 19.0F, 4.65F, 0.0F, 0.7854F, 0.0F));

		PartDefinition vial6Empty = partdefinition.addOrReplaceChild("vial6Empty",
				CubeListBuilder.create().texOffs(22, 0)
						.addBox(-0.2825F, -3.0F, -0.1464F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.2825F, -3.5F, -0.1464F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offsetAndRotation(-5.15F, 19.0F, 4.65F, 0.0F, 0.7854F, 0.0F));

		PartDefinition vial7 = partdefinition.addOrReplaceChild("vial7",
				CubeListBuilder.create().texOffs(27, 0)
						.addBox(-0.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offset(-6.75F, 17.0F, 0.0F));

		PartDefinition vial7Empty = partdefinition.addOrReplaceChild("vial7Empty",
				CubeListBuilder.create().texOffs(22, 0)
						.addBox(-0.5F, -1.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offset(-6.75F, 17.0F, 0.0F));

		PartDefinition vial8 = partdefinition.addOrReplaceChild("vial8",
				CubeListBuilder.create().texOffs(27, 0)
						.addBox(-1.5607F, -3.0F, 1.2317F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-1.5607F, -3.5F, 1.2317F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offsetAndRotation(-5.25F, 19.0F, -6.75F, 0.0F, 0.7854F, 0.0F));

		PartDefinition vial8Empty = partdefinition.addOrReplaceChild("vial8Empty",
				CubeListBuilder.create().texOffs(22, 0)
						.addBox(-1.5607F, -3.0F, 1.2317F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(28, 10)
						.addBox(-1.5607F, -3.5F, 1.2317F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F)),
				PartPose.offsetAndRotation(-5.25F, 19.0F, -6.75F, 0.0F, 0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}
	public final ModelPart crossBrace2;
	public final ModelPart crossBrace;
	public final ModelPart center;
	public final ModelPart nonCardinalArms;
	public final ModelPart cardinalArms;
	public final ModelPart vial1;
	public final ModelPart vial1Empty;
	public final ModelPart vial2;
	public final ModelPart vial2Empty;
	public final ModelPart vial3;
	public final ModelPart vial3Empty;
	public final ModelPart vial4;
	public final ModelPart vial4Empty;
	public final ModelPart vial5;
	public final ModelPart vial5Empty;
	public final ModelPart vial6;
	public final ModelPart vial6Empty;
	public final ModelPart vial7;
	public final ModelPart vial7Empty;
	public final ModelPart vial8;

	public final ModelPart vial8Empty;

	public CentrifugeArmsModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.crossBrace2 = root.getChild("crossBrace2");
		this.crossBrace = root.getChild("crossBrace");
		this.center = root.getChild("center");
		this.nonCardinalArms = root.getChild("nonCardinalArms");
		this.cardinalArms = root.getChild("cardinalArms");
		this.vial1 = root.getChild("vial1");
		this.vial1Empty = root.getChild("vial1Empty");
		this.vial2 = root.getChild("vial2");
		this.vial2Empty = root.getChild("vial2Empty");
		this.vial3 = root.getChild("vial3");
		this.vial3Empty = root.getChild("vial3Empty");
		this.vial4 = root.getChild("vial4");
		this.vial4Empty = root.getChild("vial4Empty");
		this.vial5 = root.getChild("vial5");
		this.vial5Empty = root.getChild("vial5Empty");
		this.vial6 = root.getChild("vial6");
		this.vial6Empty = root.getChild("vial6Empty");
		this.vial7 = root.getChild("vial7");
		this.vial7Empty = root.getChild("vial7Empty");
		this.vial8 = root.getChild("vial8");
		this.vial8Empty = root.getChild("vial8Empty");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		crossBrace2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		crossBrace.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		center.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		nonCardinalArms.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		cardinalArms.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial1.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial1Empty.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial2.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial2Empty.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial3.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial3Empty.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial4.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial4Empty.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial5.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial5Empty.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial6.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial6Empty.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial7.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial7Empty.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial8.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		vial8Empty.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}