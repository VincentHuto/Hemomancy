package com.vincenthuto.hemomancy.client.model.tile.functional;

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

public class MnemonicReliquaryModel extends Model {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("mnemonic_reliquary_model"), "main");
	private final ModelPart base;
	private final ModelPart lid;
	private final ModelPart brain;
	private final ModelPart pillow;

	public MnemonicReliquaryModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.base = root.getChild("base");
		this.lid = root.getChild("Lid");
		this.brain = root.getChild("brain");
		this.pillow = root.getChild("pillow");
	}

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition base = partdefinition.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -2.0F, -7.0F, 12.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(52, 25).addBox(4.0F, -3.0F, 5.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(52, 29).addBox(4.0F, -3.0F, -8.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(12, 64).addBox(-7.0F, -3.0F, 5.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 64).addBox(-7.0F, -3.0F, -8.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 34).addBox(-6.0F, 2.0F, -7.0F, 2.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(32, 34).addBox(4.0F, 2.0F, -7.0F, 2.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(52, 7).addBox(-4.0F, 2.0F, -7.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(52, 10).addBox(-4.0F, 2.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition Lid = partdefinition.addOrReplaceChild("Lid", CubeListBuilder.create().texOffs(52, 13).addBox(2.0F, 0.0F, -7.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(52, 16).addBox(2.0F, 0.0F, 5.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 49).addBox(10.0F, 0.0F, -7.0F, 2.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(32, 49).addBox(0.0F, 0.0F, -7.0F, 2.0F, 1.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 18).addBox(0.0F, 1.0F, -7.0F, 12.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 6.0F, 0.0F, 0.0F, 0.0F, -1.1781F));

		PartDefinition brain = partdefinition.addOrReplaceChild("brain", CubeListBuilder.create().texOffs(52, 19).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition pillow = partdefinition.addOrReplaceChild("pillow", CubeListBuilder.create().texOffs(52, 0).addBox(-2.0F, -0.5F, -3.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.5F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
		base.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		lid.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		brain.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		pillow.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}

	public ModelPart getLid() {
		return lid;
	}

	public ModelPart getBrain() {
		return brain;
	}

	public ModelPart getBase() {
		return base;
	}
}