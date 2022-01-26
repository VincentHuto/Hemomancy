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

// Made with Blockbench 4.1.1
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports


public class ModelMorphlingPolyp<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Hemomancy.MOD_ID, "modelmorphlingpolyp"), "main");
	private final ModelPart bone;

	public ModelMorphlingPolyp(ModelPart root) {
		this.bone = root.getChild("bone");
	}
	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition bone3 = bone.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(56, 70).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, -6.0F, -2.15F));

		PartDefinition bone4 = bone.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(34, 46).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(2.9F, -8.0F, -3.4F));

		PartDefinition bone5 = bone.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(32, 78).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.15F, -8.0F, 4.4F));

		PartDefinition bone6 = bone.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(0, 38).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(1.4F, -5.0F, 5.35F));

		PartDefinition bone7 = bone.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(32, 62).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.1F, -6.0F, 4.6F));

		PartDefinition bone8 = bone.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(26, 29).addBox(-3.5F, -4.25F, -2.5F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.6F, -7.55F, -1.25F));

		PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(26, 29).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(2.4F, -3.3F, -4.75F));

		PartDefinition bone9 = bone.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(58, 54).addBox(-4.0F, -4.0F, -2.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.1F, -5.25F, -4.15F));

		PartDefinition bone17 = bone.addOrReplaceChild("bone17", CubeListBuilder.create().texOffs(58, 54).addBox(-2.5F, -3.5F, -3.0F, 5.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.6F, -6.75F, 7.85F));

		PartDefinition bone10 = bone.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(58, 38).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.1F, -4.55F, 2.55F));

		PartDefinition bone11 = bone.addOrReplaceChild("bone11", CubeListBuilder.create().texOffs(0, 55).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(3.65F, -5.0F, -2.35F));

		PartDefinition bone12 = bone.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(0, 20).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(3.4F, -4.0F, 3.35F));

		PartDefinition bone13 = bone.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(54, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.1F, -4.35F, 5.85F));

		PartDefinition bone16 = bone.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(0, 71).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.85F, -3.3F, -2.4F));

		PartDefinition bone15 = bone.addOrReplaceChild("bone15", CubeListBuilder.create().texOffs(52, 20).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.9F, -5.5F, 6.6F));

		PartDefinition eye = bone.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(82, 54).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, -12.0F, -1.5F));

		PartDefinition eye2 = bone.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(82, 39).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.5F, -10.0F, 4.5F));

		PartDefinition eye3 = bone.addOrReplaceChild("eye3", CubeListBuilder.create().texOffs(82, 33).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.75F, -11.0F, 4.5F));

		PartDefinition eye7 = bone.addOrReplaceChild("eye7", CubeListBuilder.create().texOffs(0, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.75F, -5.0F, 10.5F));

		PartDefinition eye12 = bone.addOrReplaceChild("eye12", CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(5.25F, -8.25F, 9.5F));

		PartDefinition eye8 = bone.addOrReplaceChild("eye8", CubeListBuilder.create().texOffs(76, 16).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.75F, -3.0F, -6.5F));

		PartDefinition eye9 = bone.addOrReplaceChild("eye9", CubeListBuilder.create().texOffs(44, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -3.0F, -6.5F));

		PartDefinition eye13 = bone.addOrReplaceChild("eye13", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -4.0F, 5.5F));

		PartDefinition eye10 = bone.addOrReplaceChild("eye10", CubeListBuilder.create().texOffs(35, 23).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.75F, -5.0F, -5.5F));

		PartDefinition eye11 = bone.addOrReplaceChild("eye11", CubeListBuilder.create().texOffs(26, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.75F, -3.0F, 8.5F));

		PartDefinition eye4 = bone.addOrReplaceChild("eye4", CubeListBuilder.create().texOffs(80, 70).addBox(-3.5F, -0.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.25F, -10.0F, 4.5F));

		PartDefinition eye5 = bone.addOrReplaceChild("eye5", CubeListBuilder.create().texOffs(78, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.25F, -10.0F, -4.5F));

		PartDefinition eye6 = bone.addOrReplaceChild("eye6", CubeListBuilder.create().texOffs(76, 22).addBox(-1.5F, -2.3F, 1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.75F, -10.0F, -7.5F));

		PartDefinition bone14 = bone.addOrReplaceChild("bone14", CubeListBuilder.create().texOffs(76, 22).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, -11.8F, 1.5F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, buffer, packedLight, packedOverlay);
	}
}