package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerVoyagerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class HarbingerVoyagerModel<T extends HarbingerVoyagerEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("harbinger_voyager"), "main");

	public HarbingerVoyagerModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition root = meshDefinition.getRoot();

		root.addOrReplaceChild("head",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F,
								new CubeDeformation(0.15F))
						.texOffs(32, 0).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 2.0F, 9.0F,
								new CubeDeformation(0.0F))
						.texOffs(0, 52).addBox(-2.5F, -11.0F, -3.5F, 5.0F, 2.0F, 7.0F,
								new CubeDeformation(0.0F)),
				PartPose.ZERO);
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("body",
				CubeListBuilder.create()
						.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F,
								new CubeDeformation(0.2F))
						.texOffs(0, 32).addBox(-4.5F, 1.0F, -2.6F, 9.0F, 15.0F, 5.0F,
								new CubeDeformation(0.0F))
						.texOffs(34, 34).addBox(-5.0F, 2.0F, 1.8F, 10.0F, 11.0F, 1.0F,
								new CubeDeformation(0.0F)),
				PartPose.ZERO);
		root.addOrReplaceChild("right_arm",
				CubeListBuilder.create()
						.texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F,
								new CubeDeformation(0.25F))
						.texOffs(56, 18).addBox(-3.3F, 3.0F, -2.3F, 4.0F, 4.0F, 4.0F,
								new CubeDeformation(0.0F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm",
				CubeListBuilder.create()
						.texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F,
								new CubeDeformation(0.25F))
						.texOffs(56, 18).mirror().addBox(-0.7F, 3.0F, -2.3F, 4.0F, 4.0F, 4.0F,
								new CubeDeformation(0.0F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_leg",
				CubeListBuilder.create()
						.texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F,
								new CubeDeformation(0.0F))
						.texOffs(40, 48).addBox(-2.1F, 7.0F, -2.2F, 4.0F, 5.0F, 4.0F,
								new CubeDeformation(0.0F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg",
				CubeListBuilder.create()
						.texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F,
								new CubeDeformation(0.0F))
						.texOffs(40, 48).mirror().addBox(-1.9F, 7.0F, -2.2F, 4.0F, 5.0F, 4.0F,
								new CubeDeformation(0.0F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshDefinition, 64, 64);
	}
}
