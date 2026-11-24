package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.HunterFungusEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;

/** A low stalker with a four-beat gait and a gill-maw under the cap. */
public class HunterFungusModel extends HierarchicalModel<HunterFungusEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("hunter_fungus"), "main");

	private final ModelPart root;
	private final ModelPart trunk;
	private final ModelPart head;
	private final ModelPart cap;
	private final ModelPart jaw;
	private final Leg frontLeft;
	private final Leg frontRight;
	private final Leg hindLeft;
	private final Leg hindRight;

	private record Leg(ModelPart upper, ModelPart ankle) {
		static Leg of(ModelPart trunk, String position) {
			ModelPart upper = trunk.getChild(position + "_leg");
			return new Leg(upper, upper.getChild(position + "_ankle"));
		}

		void stride(float phase, float amount) {
			float swing = Mth.cos(phase);
			float lift = Math.max(0.0F, Mth.sin(phase));
			upper.xRot += swing * 0.48F * amount;
			ankle.xRot -= (swing * 0.22F + lift * 0.28F) * amount;
		}
	}

	public HunterFungusModel(ModelPart bakedRoot) {
		root = bakedRoot.getChild("root");
		trunk = root.getChild("trunk");
		head = trunk.getChild("head");
		cap = head.getChild("cap");
		jaw = head.getChild("jaw");
		frontLeft = Leg.of(trunk, "front_left");
		frontRight = Leg.of(trunk, "front_right");
		hindLeft = Leg.of(trunk, "hind_left");
		hindRight = Leg.of(trunk, "hind_right");
	}

	public static LayerDefinition createBodyLayer() {
		return HunterFungusMesh.createBodyLayer();
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(HunterFungusEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		float pace = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
		float phase = limbSwing * 1.1F;
		frontLeft.stride(phase, pace);
		frontRight.stride(phase + Mth.PI, pace);
		hindLeft.stride(phase + Mth.HALF_PI, pace);
		hindRight.stride(phase - Mth.HALF_PI, pace);
		trunk.y -= (0.25F + Mth.cos(phase * 2.0F) * 0.23F) * pace;
		trunk.zRot += Mth.sin(phase) * 0.045F * pace;

		float breath = Mth.sin(ageInTicks * 0.16F + entity.getId());
		head.yRot += Mth.clamp(netHeadYaw, -35.0F, 35.0F) * Mth.DEG_TO_RAD;
		head.xRot += Mth.clamp(headPitch, -25.0F, 25.0F) * Mth.DEG_TO_RAD * 0.55F;
		cap.xRot += Mth.sin(phase - 0.5F) * 0.05F * pace - breath * 0.025F;
		jaw.xRot += 0.06F + breath * 0.045F + (entity.isAggressive() ? 0.13F : 0.0F);

		float strike = Mth.sin(attackTime * Mth.PI);
		float snap = Mth.sin(attackTime * Mth.TWO_PI);
		head.z -= strike * 1.35F;
		head.xRot += strike * 0.23F;
		cap.xRot -= strike * 0.08F;
		jaw.xRot += strike * 0.17F + snap * 0.29F;
	}
}
