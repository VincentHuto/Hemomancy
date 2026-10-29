package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.HunterFungusEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/** A low stalker: a fleshy bulb on four hyphal legs, a cap for a head, and a gill-maw that works beneath it. */
public class HunterFungusModel extends HierarchicalModel<HunterFungusEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("hunter_fungus"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart cap;
	private final ModelPart jaw;
	private final ModelPart frontLeftLeg;
	private final ModelPart frontRightLeg;
	private final ModelPart backLeftLeg;
	private final ModelPart backRightLeg;

	public HunterFungusModel(ModelPart root) {
		this.root = root;
		this.head = root.getChild("head");
		this.cap = head.getChild("cap");
		this.jaw = head.getChild("jaw");
		this.frontLeftLeg = root.getChild("front_left_leg");
		this.frontRightLeg = root.getChild("front_right_leg");
		this.backLeftLeg = root.getChild("back_left_leg");
		this.backRightLeg = root.getChild("back_right_leg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -2.5F, -5.0F, 6.0F, 5.0F, 10.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(32, 0).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 14.0F, -5.0F));
		head.addOrReplaceChild("cap", CubeListBuilder.create()
				.texOffs(0, 15).addBox(-5.0F, -3.0F, -5.0F, 10.0F, 3.0F, 10.0F), PartPose.offset(0.0F, -2.0F, -2.0F));
		head.addOrReplaceChild("jaw", CubeListBuilder.create()
				.texOffs(48, 0).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 2.0F, -1.0F));
		CubeListBuilder leg = CubeListBuilder.create().texOffs(40, 15).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F);
		root.addOrReplaceChild("front_left_leg", leg, PartPose.offset(2.5F, 17.0F, -3.5F));
		root.addOrReplaceChild("front_right_leg", leg, PartPose.offset(-2.5F, 17.0F, -3.5F));
		root.addOrReplaceChild("back_left_leg", leg, PartPose.offset(2.5F, 17.0F, 3.5F));
		root.addOrReplaceChild("back_right_leg", leg, PartPose.offset(-2.5F, 17.0F, 3.5F));
		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(HunterFungusEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		root().getAllParts().forEach(ModelPart::resetPose);
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		head.xRot = headPitch * Mth.DEG_TO_RAD;
		float stride = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		frontLeftLeg.xRot = stride;
		backRightLeg.xRot = stride;
		frontRightLeg.xRot = -stride;
		backLeftLeg.xRot = -stride;
		jaw.xRot = 0.25F + 0.2F * Mth.sin(ageInTicks * 0.2F) + (entity.isAggressive() ? 0.35F : 0.0F);
		cap.y += Mth.sin(ageInTicks * 0.1F) * 0.3F;
	}
}
