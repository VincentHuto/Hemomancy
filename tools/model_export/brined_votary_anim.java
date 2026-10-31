
	/**
	 * On the sea floor it wades: a slow, heavy stride that sinks into each step, the body hunched forward and lurching
	 * side to side, both arms held half up in front of it with the hands hanging, and higher and reaching once it has
	 * a target. Swimming toward a target (the renderer lays the body along the stroke) the arms reach ahead and pull
	 * together while the legs trail in a slow kick. A strike is an overhead clubbing blow with the right arm. The kelp,
	 * the tether, the coat skirts and the votive vial drift behind whatever the body does.
	 */
	@Override
	public void setupAnim(BrinedVotaryEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		boolean swimming = entity.isSwimming();
		float stride = Math.min(1.0F, limbSwingAmount * 1.6F);
		float phase = limbSwing * 0.45F;
		float drift = Mth.sin(ageInTicks * 0.05F);
		float driftSlow = Mth.sin(ageInTicks * 0.031F + 1.3F);
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		head.xRot = headPitch * Mth.DEG_TO_RAD;
		if (swimming) {
			swim(ageInTicks, stride);
		} else {
			wade(phase, stride, drift, driftSlow, entity.isAggressive());
		}
		strike(this.attackTime, swimming);
		trail(ageInTicks, stride, drift, swimming);
	}

	private void wade(float phase, float stride, float drift, float driftSlow, boolean reaching) {
		float swing = Mth.cos(phase) * stride * 0.75F;
		// Each footfall sinks the whole body a little; the shoulders roll over the planted leg.
		hips.y += 0.7F * stride * Math.abs(Mth.sin(phase));
		hips.zRot = 0.05F * stride * Mth.sin(phase);
		body.xRot = 0.16F + 0.03F * drift + 0.06F * stride;
		body.zRot = -0.07F * stride * Mth.sin(phase) + 0.025F * driftSlow;
		body.yRot = 0.06F * stride * Mth.cos(phase);
		head.xRot -= 0.12F;
		head.zRot = 0.06F * driftSlow;
		rightLeg.xRot = swing;
		leftLeg.xRot = -swing;
		rightShin.xRot = stride * (0.15F + 0.6F * Math.max(0.0F, Mth.sin(phase)));
		leftShin.xRot = stride * (0.15F + 0.6F * Math.max(0.0F, -Mth.sin(phase)));
		float raise = reaching ? -1.4F : -0.85F;
		rightArm.xRot = raise + 0.08F * drift - swing * 0.18F;
		leftArm.xRot = raise - 0.08F * drift + swing * 0.18F;
		rightArm.zRot = 0.14F + 0.03F * driftSlow;
		leftArm.zRot = -0.14F - 0.03F * driftSlow;
		rightArm.yRot = -0.08F;
		leftArm.yRot = 0.08F;
		// The hands hang from the wrists while the arms are only half up; reaching, the forearms come level.
		rightForearm.xRot = reaching ? -0.15F : 0.35F + 0.05F * driftSlow;
		leftForearm.xRot = reaching ? -0.2F : 0.4F - 0.05F * driftSlow;
	}

	private void swim(float age, float stride) {
		float pull = Mth.sin(age * 0.09F);
		float reach = Math.max(0.0F, pull);
		rightArm.xRot = -2.7F + 0.6F * pull;
		leftArm.xRot = -2.7F + 0.6F * pull;
		rightArm.zRot = 0.25F + 0.55F * reach;
		leftArm.zRot = -0.25F - 0.55F * reach;
		rightForearm.xRot = -0.7F * Math.max(0.0F, -pull);
		leftForearm.xRot = -0.7F * Math.max(0.0F, -pull);
		float kick = Mth.sin(age * 0.16F) * (0.2F + 0.2F * stride);
		rightLeg.xRot = kick;
		leftLeg.xRot = -kick;
		rightShin.xRot = 0.25F + 0.25F * Math.max(0.0F, kick);
		leftShin.xRot = 0.25F + 0.25F * Math.max(0.0F, -kick);
		body.xRot = 0.04F * Mth.sin(age * 0.09F - 0.6F);
		head.xRot -= 0.85F;
	}

	/** Raise the right arm over the head, then bring it down hard, leaning the body into the blow. */
	private void strike(float attack, boolean swimming) {
		if (attack <= 0.0F) return;
		float lift = HemoEasing.smoothstep(0.0F, 0.35F, attack) * (1.0F - HemoEasing.smoothstep(0.35F, 0.6F, attack));
		float slam = HemoEasing.smoothstep(0.35F, 0.6F, attack) * (1.0F - HemoEasing.smoothstep(0.75F, 1.0F, attack));
		toward(rightArm, lift, -2.6F, 0.1F, 0.25F);
		toward(rightForearm, lift, -0.7F, 0.0F, 0.0F);
		toward(rightArm, slam, -0.55F, -0.2F, 0.05F);
		toward(rightForearm, slam, -0.1F, 0.0F, 0.0F);
		toward(leftArm, Math.max(lift, slam), -1.0F, 0.25F, -0.3F);
		if (!swimming) {
			body.xRot += 0.38F * slam - 0.12F * lift;
			body.yRot += -0.22F * slam + 0.12F * lift;
		}
	}

	/** What the water moves: kelp, the tether, the coat skirts, the vial, the hood's peak and the jaw. */
	private void trail(float age, float stride, float drift, boolean swimming) {
		float stream = swimming ? 0.5F : 0.3F * stride;
		float wave = Mth.sin(age * 0.07F);
		kelpRight.xRot += 0.12F + stream + 0.12F * wave;
		kelpRight.zRot = 0.1F + 0.06F * Mth.sin(age * 0.05F + 0.8F);
		kelpRightTip.xRot = 0.1F + 0.2F * Mth.sin(age * 0.07F - 1.1F) + stream * 0.5F;
		kelpLeft.xRot += 0.12F + stream + 0.12F * Mth.sin(age * 0.07F + 2.0F);
		kelpLeft.zRot = -0.1F - 0.06F * Mth.sin(age * 0.05F + 2.4F);
		kelpLeftTip.xRot = 0.1F + 0.2F * Mth.sin(age * 0.07F + 0.9F) + stream * 0.5F;
		kelpBack.xRot += 0.15F + stream + 0.1F * Mth.sin(age * 0.06F + 0.4F);
		kelpBack.zRot = 0.08F * Mth.sin(age * 0.045F);
		kelpBackTip.xRot = 0.15F + 0.25F * Mth.sin(age * 0.06F - 0.8F) + stream * 0.5F;
		kelpWrist.xRot += 0.1F + stream * 0.6F + 0.2F * Mth.sin(age * 0.08F + 1.7F);
		tether.xRot = 0.12F * Mth.sin(age * 0.05F + 0.3F) + stream * 0.8F - 0.5F * body.xRot;
		tether.zRot = -0.06F + 0.05F * drift;
		tetherEnd.xRot = 0.2F * Mth.sin(age * 0.05F - 0.9F) + stream * 0.4F;
		skirtFrontRight.xRot = Math.min(0.0F, rightLeg.xRot) * 0.9F;
		skirtFrontLeft.xRot = Math.min(0.0F, leftLeg.xRot) * 0.9F;
		skirtBack.xRot = Math.max(0.0F, Math.max(rightLeg.xRot, leftLeg.xRot)) * 0.85F + 0.04F + 0.03F * wave + stream * 0.4F;
		mantle.xRot = 0.05F + 0.04F * Mth.sin(age * 0.06F + 1.0F) + stream * 0.5F;
		// The slack jaw hangs and works a little; the wet peak of the hood sways behind the head.
		jaw.xRot += 0.06F + 0.05F * Mth.sin(age * 0.04F + 0.5F);
		hoodTip.xRot += 0.06F * Mth.sin(age * 0.05F + 1.4F) - stream * 0.3F;
		hoodTipEnd.xRot += 0.1F * Mth.sin(age * 0.05F + 0.6F);
		// The vial hangs plumb from the collar however far the body leans.
		votive.xRot = swimming ? 0.3F : -body.xRot * 0.9F + 0.05F * wave;
		votive.zRot = -body.zRot + 0.04F * Mth.sin(age * 0.08F);
	}

	private static void toward(ModelPart part, float weight, float x, float y, float z) {
		part.xRot = Mth.lerp(weight, part.xRot, x);
		part.yRot = Mth.lerp(weight, part.yRot, y);
		part.zRot = Mth.lerp(weight, part.zRot, z);
	}
