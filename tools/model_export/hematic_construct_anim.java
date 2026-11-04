
	/**
	 * An iron body that walks like a thing wound up: a heavy, flat-footed stomp that drops the whole frame onto
	 * each step, the arms swinging stiffly from the shoulder with the fists hanging, and the head fixed under the
	 * helm so only the body turns toward what it follows. The strike is both fists raised over the helm and
	 * brought down together, the body folding into it.
	 */
	@Override
	public void setupAnim(HematicConstructEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float stride = Math.min(1.0F, limbSwingAmount * 1.6F);
		float phase = limbSwing * 0.5F;
		float seethe = Mth.sin(ageInTicks * 0.06F);
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.35F;
		head.xRot = headPitch * Mth.DEG_TO_RAD * 0.5F;
		stomp(phase, stride, seethe);
		slam(this.attackTime);
	}

	private void stomp(float phase, float stride, float seethe) {
		float swing = Mth.cos(phase) * stride;
		// The frame drops onto each planted foot and rolls over it; at rest it only seethes.
		hips.y += 0.9F * stride * Math.abs(Mth.sin(phase));
		hips.zRot = 0.06F * stride * Mth.sin(phase);
		body.xRot = 0.08F + 0.03F * seethe + 0.05F * stride;
		body.yRot = 0.1F * stride * Mth.cos(phase);
		body.zRot = -0.05F * stride * Mth.sin(phase);
		rightLeg.xRot = swing * 0.8F;
		leftLeg.xRot = -swing * 0.8F;
		rightShin.xRot = stride * (0.1F + 0.75F * Math.max(0.0F, Mth.sin(phase)));
		leftShin.xRot = stride * (0.1F + 0.75F * Math.max(0.0F, -Mth.sin(phase)));
		rightArm.xRot = -0.15F - swing * 0.55F + 0.03F * seethe;
		leftArm.xRot = -0.15F + swing * 0.55F - 0.03F * seethe;
		rightArm.zRot = 0.2F;
		leftArm.zRot = -0.2F;
		rightForearm.xRot = -0.25F - 0.15F * stride;
		leftForearm.xRot = -0.25F - 0.15F * stride;
	}

	/** Both fists up over the helm, a hold at the top, then down together with the body folding into the blow. */
	private void slam(float attack) {
		if (attack <= 0.0F) return;
		float lift = HemoEasing.smoothstep(0.0F, 0.3F, attack) * (1.0F - HemoEasing.smoothstep(0.4F, 0.6F, attack));
		float drop = HemoEasing.smoothstep(0.4F, 0.6F, attack) * (1.0F - HemoEasing.smoothstep(0.8F, 1.0F, attack));
		toward(rightArm, lift, -2.8F, 0.0F, 0.35F);
		toward(leftArm, lift, -2.8F, 0.0F, -0.35F);
		toward(rightForearm, lift, -0.9F, 0.0F, 0.0F);
		toward(leftForearm, lift, -0.9F, 0.0F, 0.0F);
		toward(body, lift, -0.15F, 0.0F, 0.0F);
		toward(rightArm, drop, -0.7F, 0.0F, 0.15F);
		toward(leftArm, drop, -0.7F, 0.0F, -0.15F);
		toward(rightForearm, drop, -0.2F, 0.0F, 0.0F);
		toward(leftForearm, drop, -0.2F, 0.0F, 0.0F);
		toward(body, drop, 0.5F, 0.0F, 0.0F);
		toward(head, drop, 0.3F, 0.0F, 0.0F);
	}

	private static void toward(ModelPart part, float weight, float x, float y, float z) {
		if (weight <= 0.0F) return;
		part.xRot = Mth.lerp(weight, part.xRot, x);
		part.yRot = Mth.lerp(weight, part.yRot, y);
		part.zRot = Mth.lerp(weight, part.zRot, z);
	}
