package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.AntibodyEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** A Y-shaped binder: a stem, two arms, and the crimson tips that close on what the network has marked. */
public class AntibodyModel extends HierarchicalModel<AntibodyEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("antibody"), "main");
	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart leftArm;
	private final ModelPart rightArm;

	public AntibodyModel(ModelPart root) {
		this.root = root;
		this.body = root.getChild("body");
		this.leftArm = body.getChild("left_arm");
		this.rightArm = body.getChild("right_arm");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
		PartDefinition leftArm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(8, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.6F));
		leftArm.addOrReplaceChild("left_tip", CubeListBuilder.create()
				.texOffs(0, 12).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -6.0F, 0.0F));
		PartDefinition rightArm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(16, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, -0.6F));
		rightArm.addOrReplaceChild("right_tip", CubeListBuilder.create()
				.texOffs(12, 12).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offset(0.0F, -6.0F, 0.0F));
		return LayerDefinition.create(mesh, 32, 32);
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(AntibodyEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		root().getAllParts().forEach(ModelPart::resetPose);
		body.yRot = ageInTicks * 0.08F;
		body.y += Mth.sin(ageInTicks * 0.15F) * 0.8F;
		float grip = 0.15F * Mth.sin(ageInTicks * 0.3F);
		leftArm.zRot += grip;
		rightArm.zRot -= grip;
	}
}
