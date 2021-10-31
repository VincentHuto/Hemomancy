package com.vincenthuto.hemomancy.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public class ModelBloodGourd<T extends LivingEntity> extends EntityModel<T> {
	private final ModelPart body;

	public ModelBloodGourd(ModelPart root) {
		this.body = root.getChild("body");
	}

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition horn = body.addOrReplaceChild("horn", CubeListBuilder.create(),
				PartPose.offsetAndRotation(5.75F, 12.5722F, 0.25F, 0.7418F, 0.0F, 0.0F));

		PartDefinition horn2 = horn.addOrReplaceChild("horn2",
				CubeListBuilder.create().texOffs(1, 25)
						.addBox(-2.0F, -3.625F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(-0.5F)).texOffs(12, 0)
						.addBox(-1.5F, -2.675F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(-0.2F)).texOffs(9, 4)
						.addBox(-1.5F, -1.875F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.5F)).texOffs(9, 11)
						.addBox(-1.5F, -2.875F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)),
				PartPose.offsetAndRotation(0.0F, -0.0972F, -0.25F, -0.3935F, -0.0136F, -0.0059F));

		PartDefinition bone5 = horn2.addOrReplaceChild("bone5",
				CubeListBuilder.create().texOffs(0, 7).addBox(-1.5F, -4.0396F, 1.6735F, 3.0F, 4.0F, 3.0F,
						new CubeDeformation(-0.6F)),
				PartPose.offsetAndRotation(0.0F, 4.625F, 0.0F, 0.829F, 0.0F, 0.0F));

		PartDefinition bone6 = bone5.addOrReplaceChild("bone6",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.3036F, -1.3772F, 3.0F, 4.0F, 3.0F,
						new CubeDeformation(-0.8F)),
				PartPose.offsetAndRotation(0.0F, -0.5336F, 3.1745F, 0.4059F, 0.1944F, 0.0395F));

		PartDefinition bone = bone6.addOrReplaceChild("bone",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.7628F, -0.678F, 3.0F, 4.0F, 3.0F,
						new CubeDeformation(-0.9F)),
				PartPose.offsetAndRotation(0.0F, 2.6964F, 0.1228F, 0.8727F, 0.0F, 0.0F));

		PartDefinition bone2 = bone6.addOrReplaceChild("bone2",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, 0.2609F, -1.7772F, 3.0F, 4.0F, 3.0F,
						new CubeDeformation(-1.0F)),
				PartPose.offsetAndRotation(0.0F, 2.6964F, 0.0228F, 1.7017F, 0.0F, 0.0F));

		PartDefinition bone7 = bone2.addOrReplaceChild("bone7",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.55F, -1.75F, -1.35F, 3.0F, 4.0F, 3.0F,
						new CubeDeformation(-1.1F)),
				PartPose.offsetAndRotation(0.25F, 3.4725F, -0.0649F, 0.7192F, 0.1513F, -0.3159F));

		PartDefinition bone8 = bone2.addOrReplaceChild("bone8",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1.8F, -1.8F, -1.4F, 3.0F, 4.0F, 3.0F,
						new CubeDeformation(-1.2F)),
				PartPose.offsetAndRotation(0.9861F, 4.1217F, 0.8936F, 1.1937F, 0.5559F, -0.4748F));

		PartDefinition rope2 = horn.addOrReplaceChild("rope2",
				CubeListBuilder.create().texOffs(3, 19).addBox(1.4F, -0.5333F, -1.0333F, 0.0F, 1.0F, 2.0F,
						new CubeDeformation(0.05F)),
				PartPose.offsetAndRotation(-0.1F, -0.8389F, 0.0333F, -0.2269F, 0.0F, 0.0F));

		PartDefinition bone3 = rope2.addOrReplaceChild("bone3",
				CubeListBuilder.create().texOffs(4, 20).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
						new CubeDeformation(0.05F)),
				PartPose.offsetAndRotation(1.3F, -0.0333F, 1.0667F, 0.0F, 0.3927F, 0.0F));

		PartDefinition bone4 = rope2.addOrReplaceChild("bone4",
				CubeListBuilder.create().texOffs(10, 21).addBox(-4.0F, -0.5F, 0.0F, 4.0F, 1.0F, 0.0F,
						new CubeDeformation(0.05F)),
				PartPose.offsetAndRotation(1.3F, -0.0333F, -1.0333F, -0.0319F, 0.0064F, 0.0646F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		body.render(poseStack, buffer, packedLight, packedOverlay);
	}

}