package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.common.entity.mob.animal.BloodlickerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public final class BloodlickerModel extends HierarchicalModel<BloodlickerEntity> {
	private final ModelPart root = BloodlickerMesh.createRoot();
	private final ModelPart thorax = root.getChild("thorax");
	private final ModelPart crop = thorax.getChild("crop");
	private final ModelPart head = thorax.getChild("head");
	private final ModelPart siphon = head.getChild("siphon");
	private final Side left = new Side(thorax, "left");
	private final Side right = new Side(thorax, "right");

	public BloodlickerModel() {
		super(RenderType::entityCutoutNoCull);
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(BloodlickerEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		root.getAllParts().forEach(ModelPart::resetPose);

		float movement = Mth.clamp(limbSwingAmount * 2.2F, 0.0F, 1.0F);
		float stride = limbSwing * 0.9F;
		float breath = Mth.sin(ageInTicks * 0.12F);
		float fullness = entity.getBloodFullness();
		float siphonAge = entity.getSiphonAnimationAge(ageInTicks);
		float feeding = Mth.clamp(siphonAge / 3.0F, 0.0F, 1.0F)
				* Mth.clamp((BloodlickerEntity.SIPHON_ANIMATION_TICKS - siphonAge) / 5.0F, 0.0F, 1.0F);
		float pump = Mth.sin(siphonAge * 1.15F) * feeding;
		// A stationary client spawn may have floor contact before its grounded flag arrives.
		boolean airborne = !entity.onGround() && !entity.isInWater()
				&& !entity.level().getBlockCollisions(entity, entity.getBoundingBox().move(0, -0.08D, 0))
						.iterator().hasNext();

		thorax.y += breath * 0.1F - Mth.abs(Mth.sin(stride)) * movement * 0.4F + feeding * 0.8F;
		crop.xScale = 0.88F + fullness * 0.32F + pump * 0.025F;
		crop.yScale = 0.9F + fullness * 0.38F + breath * 0.015F + pump * 0.04F;
		crop.zScale = crop.xScale;
		crop.xRot += breath * 0.025F - feeding * 0.06F;

		head.yRot += Mth.clamp(netHeadYaw, -40.0F, 40.0F) * Mth.DEG_TO_RAD * 0.48F;
		head.xRot += Mth.clamp(headPitch, -30.0F, 30.0F) * Mth.DEG_TO_RAD * 0.32F + feeding * 0.18F;
		siphon.xRot += breath * 0.035F + feeding * 0.14F + pump * 0.06F;
		siphon.yRot += breath * 0.07F * (1.0F - feeding);

		float flutter = Mth.sin(ageInTicks * (airborne ? 2.4F : 1.35F));
		left.animate(1.0F, stride, movement, feeding, breath, flutter, airborne);
		right.animate(-1.0F, stride + Mth.PI, movement, feeding, breath, flutter, airborne);
	}

	private record Side(ModelPart arm, ModelPart forearm, ModelPart middle, ModelPart hind, ModelPart shin,
						ModelPart wing) {
		private Side(ModelPart thorax, String side) {
			this(thorax.getChild(side + "_arm"), thorax.getChild(side + "_arm").getChild(side + "_forearm"),
					thorax.getChild(side + "_middle_leg"), thorax.getChild(side + "_hind_leg"),
					thorax.getChild(side + "_hind_leg").getChild(side + "_hind_shin"),
					thorax.getChild(side + "_wing"));
		}

		private void animate(float side, float phase, float movement, float feeding, float breath,
				float flutter, boolean airborne) {
			float step = Mth.sin(phase) * movement * (1.0F - feeding);
			float lift = Mth.cos(phase) * movement * (1.0F - feeding);
			arm.xRot += step * 0.3F - feeding * 0.16F;
			forearm.xRot += lift * 0.2F + feeding * 0.22F;
			middle.xRot -= step * 0.36F;
			middle.zRot += side * (lift * 0.12F - feeding * 0.12F);
			hind.xRot += step * 0.24F + feeding * 0.1F;
			shin.xRot -= lift * 0.19F;

			float stroke = airborne ? flutter * 0.52F : flutter * (0.035F + movement * 0.09F);
			wing.zRot += side * (stroke + breath * 0.025F + feeding * 0.12F);
			wing.yRot += side * (stroke * 0.34F + feeding * 0.16F);
		}
	}
}
