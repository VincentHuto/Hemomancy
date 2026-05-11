package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.ChalybeateSnailEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class ChalybeateSnailModel extends HierarchicalModel<ChalybeateSnailEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("chalybeate_snail"),
			"main");

	private final ModelPart root;
	private final ModelPart foot;
	private final ModelPart body;
	private final ModelPart shell;
	private final ModelPart sclerites;
	private final ModelPart leftTentacle;
	private final ModelPart rightTentacle;

	public ChalybeateSnailModel(ModelPart root) {
		this.root = root.getChild("root");
		this.foot = this.root.getChild("foot");
		this.body = this.root.getChild("body");
		this.shell = this.root.getChild("shell");
		this.sclerites = this.root.getChild("sclerites");
		this.leftTentacle = this.root.getChild("left_tentacle");
		this.rightTentacle = this.root.getChild("right_tentacle");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("foot", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-5.0F, -2.0F, -8.0F, 10.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
						.texOffs(0, 18).addBox(-4.0F, -2.75F, -7.0F, 8.0F, 1.0F, 14.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 1.0F));

		root.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(0, 34).addBox(-4.0F, -5.0F, -7.0F, 8.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
						.texOffs(40, 34).addBox(-3.0F, -6.0F, -5.5F, 6.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -0.75F, -0.5F));

		root.addOrReplaceChild("shell", CubeListBuilder.create()
						.texOffs(40, 0).addBox(-5.0F, -8.0F, -4.0F, 10.0F, 7.0F, 10.0F, new CubeDeformation(0.2F))
						.texOffs(80, 0).addBox(-3.5F, -10.0F, -2.5F, 7.0F, 3.0F, 7.0F, new CubeDeformation(0.0F))
						.texOffs(80, 12).addBox(-2.0F, -11.0F, -1.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(40, 18).addBox(-5.5F, -4.0F, -5.0F, 11.0F, 2.0F, 11.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -2.0F, 1.5F));

		PartDefinition sclerites = root.addOrReplaceChild("sclerites", CubeListBuilder.create(),
				PartPose.offset(0.0F, -2.0F, -0.5F));
		for (int i = -4; i <= 4; i += 2) {
			sclerites.addOrReplaceChild("front_plate_" + i, CubeListBuilder.create()
							.texOffs(0, 70).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(i, 0.0F, -7.0F, -0.25F, 0.0F, i * 0.03F));
			sclerites.addOrReplaceChild("left_plate_" + i, CubeListBuilder.create()
							.texOffs(12, 70).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-5.0F, 0.0F, i, 0.0F, 0.28F, 0.3F));
			sclerites.addOrReplaceChild("right_plate_" + i, CubeListBuilder.create()
							.texOffs(24, 70).addBox(-0.5F, -1.0F, -1.0F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(5.0F, 0.0F, i, 0.0F, -0.28F, -0.3F));
		}

		root.addOrReplaceChild("left_tentacle", CubeListBuilder.create()
						.texOffs(98, 0).addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.25F, -4.0F, -7.0F, 0.25F, -0.2F, 0.0F));
		root.addOrReplaceChild("right_tentacle", CubeListBuilder.create()
						.texOffs(98, 12).addBox(-0.5F, -0.5F, -5.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.25F, -4.0F, -7.0F, 0.25F, 0.2F, 0.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(ChalybeateSnailEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
						  float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		boolean retracted = entity.isRetracted();
		float crawl = Mth.sin(ageInTicks * 0.18F) * 0.08F;

		this.leftTentacle.visible = !retracted;
		this.rightTentacle.visible = !retracted;
		this.foot.visible = !retracted;
		this.body.visible = !retracted;
		this.shell.xScale = 1.0F;
		this.shell.yScale = 1.0F;
		this.shell.zScale = 1.0F;

		if (retracted) {
			this.body.y += 1.3F;
			this.body.z += 1.0F;
			this.sclerites.y += 1.0F;
			this.shell.y += 0.35F;
			this.shell.xScale = 1.05F;
			this.shell.yScale = 1.04F;
			this.shell.zScale = 1.08F;
		} else {
			this.root.y += crawl;
			this.leftTentacle.yRot = -0.25F + Mth.sin(ageInTicks * 0.2F) * 0.12F;
			this.rightTentacle.yRot = 0.25F - Mth.sin(ageInTicks * 0.2F) * 0.12F;
			this.sclerites.zRot = Mth.sin(ageInTicks * 0.12F) * 0.025F;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
							   int packedColor) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
