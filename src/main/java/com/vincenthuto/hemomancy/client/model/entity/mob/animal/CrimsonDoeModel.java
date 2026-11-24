package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.common.entity.mob.animal.CrimsonDoeEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Walks on diagonal pairs, folding each leg as it comes forward. Fleeing, it stretches its neck out and raises its pale
 * tail. Its exposed flank breathes at rest and the raised vein pulses with the glow layer.
 */
public final class CrimsonDoeModel extends HierarchicalModel<CrimsonDoeEntity> {
	private static final float STRIDE_RATE = 0.6F;
	private static final float HEARTBEAT_TICKS = 36.0F;
	private static final int FLICK_PERIOD = 90;
	private static final int FLICK_TICKS = 8;

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart neck;
	private final ModelPart head;
	private final ModelPart tail;
	private final ModelPart exposedFlank;
	private final ModelPart jugular;
	private final ModelPart leftEar;
	private final ModelPart rightEar;
	private final Foreleg leftFront;
	private final Foreleg rightFront;
	private final Hindleg leftHind;
	private final Hindleg rightHind;

	private record Foreleg(ModelPart leg, ModelPart cannon, ModelPart hoof) {
		static Foreleg of(ModelPart body, String prefix) {
			ModelPart leg = body.getChild(prefix + "_front_leg");
			ModelPart cannon = leg.getChild(prefix + "_front_cannon");
			return new Foreleg(leg, cannon, cannon.getChild(prefix + "_front_hoof"));
		}

		void stride(float phase, float reach, float amount) {
			float swing = Mth.cos(phase) * reach * amount;
			float lift = Math.max(0.0F, Mth.sin(phase)) * amount;
			leg.xRot += swing;
			cannon.xRot += lift * 0.85F;
			hoof.xRot -= swing * 0.45F + lift * 0.24F;
		}
	}

	private record Hindleg(ModelPart thigh, ModelPart gaskin, ModelPart cannon, ModelPart hoof) {
		static Hindleg of(ModelPart body, String prefix) {
			ModelPart thigh = body.getChild(prefix + "_hind_leg");
			ModelPart gaskin = thigh.getChild(prefix + "_hind_gaskin");
			ModelPart cannon = gaskin.getChild(prefix + "_hind_cannon");
			return new Hindleg(thigh, gaskin, cannon, cannon.getChild(prefix + "_hind_hoof"));
		}

		void stride(float phase, float reach, float amount) {
			float fold = Math.max(0.0F, Mth.sin(phase)) * amount;
			float swing = Mth.cos(phase) * reach * amount;
			thigh.xRot += swing;
			gaskin.xRot += fold * 0.45F;
			cannon.xRot -= fold * 0.8F;
			hoof.xRot -= swing * 0.45F - fold * 0.18F;
		}
	}

	public CrimsonDoeModel() {
		root = CrimsonDoeMesh.createRoot();
		body = root.getChild("body");
		neck = body.getChild("neck");
		head = neck.getChild("head");
		tail = body.getChild("tail");
		exposedFlank = body.getChild("exposed_flank");
		jugular = neck.getChild("raised_jugular");
		leftEar = head.getChild("left_ear");
		rightEar = head.getChild("right_ear");
		leftFront = Foreleg.of(body, "left");
		rightFront = Foreleg.of(body, "right");
		leftHind = Hindleg.of(body, "left");
		rightHind = Hindleg.of(body, "right");
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(CrimsonDoeEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		pose(limbSwing, limbSwingAmount, ageInTicks, entity.getId(), netHeadYaw, headPitch);
	}

	void pose(float limbSwing, float limbSwingAmount, float ageInTicks, int entityId, float netHeadYaw, float headPitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		jugular.xScale = 1.0F + heartbeat(ageInTicks, entityId) * 0.14F;
		float amount = Math.min(limbSwingAmount, 1.0F);
		float run = Mth.clamp((limbSwingAmount - 0.75F) / 0.25F, 0.0F, 1.0F);
		walk(limbSwing * STRIDE_RATE, amount, run);
		stand(ageInTicks + entityId * 17, amount);

		neck.xRot += run * 0.55F;
		head.xRot -= run * 0.45F;
		tail.xRot += run * 1.5F;
		neck.yRot += netHeadYaw * Mth.DEG_TO_RAD * 0.4F;
		head.yRot += netHeadYaw * Mth.DEG_TO_RAD * 0.6F;
		head.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F;
	}

	private void walk(float phase, float amount, float run) {
		float reach = 0.5F + run * 0.3F;
		leftFront.stride(phase, reach, amount);
		rightHind.stride(phase - run * 0.45F, reach, amount);
		rightFront.stride(phase + Mth.PI, reach, amount);
		leftHind.stride(phase + Mth.PI - run * 0.45F, reach, amount);
		body.y -= Math.abs(Mth.sin(phase)) * (0.25F + run * 0.75F) * amount;
		neck.xRot += Mth.sin(phase * 2.0F) * 0.03F * amount;
	}

	private void stand(float age, float amount) {
		float still = 1.0F - amount;
		exposedFlank.xScale = 1.0F + (0.04F + 0.04F * Mth.sin(age * 0.15F)) * still;
		neck.xRot += Mth.sin(age * 0.05F) * 0.015F * still;
		int cycle = (int) (age / FLICK_PERIOD);
		float flick = Mth.sin(Math.min(age % FLICK_PERIOD, FLICK_TICKS) / FLICK_TICKS * Mth.PI) * still;
		switch (cycle % 3) {
			case 0 -> leftEar.zRot += flick * 0.4F;
			case 1 -> tail.xRot += flick * 0.7F;
			default -> rightEar.zRot -= flick * 0.4F;
		}
	}

	public static float heartbeat(float ageInTicks, int entityId) {
		float cycle = Mth.frac((ageInTicks + entityId * 7.0F) / HEARTBEAT_TICKS);
		return Math.max(pulse(cycle, 0.0F, 0.14F), 0.7F * pulse(cycle, 0.2F, 0.14F));
	}

	private static float pulse(float cycle, float start, float width) {
		float at = (cycle - start) / width;
		return at < 0.0F || at > 1.0F ? 0.0F : Mth.sin(at * Mth.PI);
	}
}
