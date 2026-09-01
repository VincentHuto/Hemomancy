package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypEntity;
import com.vincenthuto.hemomancy.common.entity.summon.MorphlingPolypLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.EnumMap;
import java.util.Map;

public class MorphlingPolypLayerModel extends EntityModel<MorphlingPolypEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("morphling_polyp_layers"), "main");

	private final Map<MorphlingPolypLayer, ModelPart> parts = new EnumMap<>(MorphlingPolypLayer.class);

	public MorphlingPolypLayerModel(ModelPart root) {
		this.parts.put(MorphlingPolypLayer.FUNGAL, root.getChild("fungal"));
		this.parts.put(MorphlingPolypLayer.LEECHES, root.getChild("leeches"));
		this.parts.put(MorphlingPolypLayer.CHITINITE, root.getChild("chitinite"));
		this.parts.put(MorphlingPolypLayer.SERPENT, root.getChild("serpent"));
		this.parts.put(MorphlingPolypLayer.PESTS, root.getChild("pests"));
		this.parts.put(MorphlingPolypLayer.SPIDER, root.getChild("spider"));
		this.parts.put(MorphlingPolypLayer.BAT, root.getChild("bat"));
		this.parts.put(MorphlingPolypLayer.CUTTLEFISH, root.getChild("cuttlefish"));
		this.parts.put(MorphlingPolypLayer.TICK, root.getChild("tick"));
		this.parts.put(MorphlingPolypLayer.URCHIN, root.getChild("urchin"));
		this.parts.put(MorphlingPolypLayer.CENTIPEDE, root.getChild("centipede"));
		this.parts.put(MorphlingPolypLayer.MOLE, root.getChild("mole"));
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition root = meshDefinition.getRoot();

		root.addOrReplaceChild("fungal",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-2.0F, -11.0F, -1.0F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(12, 0).addBox(-4.0F, -13.0F, -2.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(24, 0).addBox(1.0F, -14.0F, 0.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("leeches",
				CubeListBuilder.create()
						.texOffs(0, 8).addBox(-8.0F, -6.0F, -6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(12, 8).addBox(4.0F, -7.0F, 3.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(24, 8).addBox(-2.0F, -5.0F, 6.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("chitinite",
				CubeListBuilder.create()
						.texOffs(0, 16).addBox(-6.0F, -8.0F, -6.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(16, 16).addBox(2.0F, -9.0F, -5.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(32, 16).addBox(-1.0F, -10.0F, 4.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("serpent",
				CubeListBuilder.create()
						.texOffs(0, 24).addBox(-1.0F, -7.0F, 5.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(20, 24).addBox(1.0F, -7.0F, 11.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("pests",
				CubeListBuilder.create()
						.texOffs(0, 36).addBox(-7.0F, -3.0F, -6.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(12, 36).addBox(3.0F, -4.0F, -7.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(24, 36).addBox(-2.0F, -3.0F, 7.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("spider",
				CubeListBuilder.create()
						.texOffs(0, 44).addBox(-10.0F, -6.0F, -5.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(14, 44).addBox(4.0F, -6.0F, -5.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(0, 47).addBox(-10.0F, -5.0F, 4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(14, 47).addBox(4.0F, -5.0F, 4.0F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("bat",
				CubeListBuilder.create()
						.texOffs(0, 52).addBox(-10.0F, -9.0F, -1.0F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
						.texOffs(24, 52).addBox(3.0F, -9.0F, -1.0F, 7.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("cuttlefish",
				CubeListBuilder.create()
						.texOffs(0, 60).addBox(-5.0F, -7.0F, -11.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
						.texOffs(16, 60).addBox(-1.0F, -8.0F, -12.0F, 2.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
						.texOffs(34, 60).addBox(3.0F, -7.0F, -11.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("tick",
				CubeListBuilder.create()
						.texOffs(0, 70).addBox(-3.0F, -12.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
						.texOffs(24, 70).addBox(-1.0F, -14.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("urchin",
				CubeListBuilder.create()
						.texOffs(0, 80).addBox(-1.0F, -16.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(8, 80).addBox(-7.0F, -8.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(24, 80).addBox(1.0F, -8.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(40, 80).addBox(-1.0F, -8.0F, -7.0F, 2.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("centipede",
				CubeListBuilder.create()
						.texOffs(0, 92).addBox(-9.0F, -5.0F, -6.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(12, 92).addBox(4.0F, -5.0F, -6.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(24, 92).addBox(-9.0F, -5.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(36, 92).addBox(4.0F, -5.0F, 0.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(48, 92).addBox(-9.0F, -5.0F, 6.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(60, 92).addBox(4.0F, -5.0F, 6.0F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("mole",
				CubeListBuilder.create()
						.texOffs(0, 100).addBox(-7.0F, -6.0F, -4.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(14, 100).addBox(3.0F, -6.0F, -4.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(28, 100).addBox(-2.0F, -8.0F, -9.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshDefinition, 128, 128);
	}

	public void renderLayer(MorphlingPolypLayer layer, PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int packedColor) {
		ModelPart part = this.parts.get(layer);
		if (part != null) {
			part.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
	}

	@Override
	public void setupAnim(MorphlingPolypEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
	}
}
