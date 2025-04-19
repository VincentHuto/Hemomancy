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

public class SuspendedVivianiteModel extends Model {

	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("suspended_vivianite"),
			"main");
	private final ModelPart crystal;

	public SuspendedVivianiteModel(ModelPart root) {
		super(RenderType::entityTranslucent);
        this.crystal = root.getChild("crystal");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition crystal = partdefinition.addOrReplaceChild("crystal", CubeListBuilder.create().texOffs(3, 10).addBox(-0.4F, -1.4F, -3.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(0, 7).addBox(-0.3F, 0.9F, -2.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(0, 7).addBox(-1.5F, 0.8F, -1.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16).addBox(0.7F, -0.3F, -4.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(0, 7).addBox(-2.7F, -0.3F, -4.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(0, 7).addBox(-0.4F, -0.3F, -5.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(8, 11).mirror().addBox(-1.5F, -0.6F, -2.5F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, 15.3F, -0.5F, 1.5708F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		crystal.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

}