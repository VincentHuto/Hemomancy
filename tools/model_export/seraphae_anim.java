
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		SeraphaeAnimState motion = SeraphaeAnimState.of(entity);
		motion.update(entity, AnimationTime.partialTick());
		pose(entity.getSeraphaeState(), motion.remaining(), ageInTicks, netHeadYaw, headPitch);
		var secondary = com.vincenthuto.hemomancy.common.entity.boss.saint.SaintAttackPattern.byId(entity.getSecondaryMove());
		if (secondary != null) {
			var cast = secondary.motion(entity.secondaryTime(AnimationTime.partialTick()));
			rightArm.xRot -= cast.gather() * 1.1F;
			leftArm.xRot -= cast.gather() * .6F;
			rightForearm.yRot += cast.strike() * .6F;
			leftWing.yRot += cast.gather() * .3F;
			rightWing.yRot -= cast.gather() * .3F;
			head.xRot += cast.strike() * .15F;
			hangChain(leftChain, leftArm, leftForearm, 0);
			hangChain(rightChain, rightArm, rightForearm, 0);
		}
	}

	/** Absolute poses reset every joint, including secondary feather waves and restraints. */
	void pose(SeraphaeState state, float remaining, float age, float yaw, float pitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		root.visible = state != SeraphaeState.DISPERSED;
		var motion = SeraphaeVisualRules.pose(state, remaining, age);
		float spread = motion.spread(), fold = motion.fold(), recoil = motion.recoil(), strain = motion.strain();
		float breath = Mth.sin(age * .075F);
		root.y -= motion.lift() * 16 / SeraphaeVisualRules.MODEL_SCALE;
		body.xRot += breath * .025F + fold * .3F + recoil * .16F - spread * .12F;
		body.zRot += Mth.sin(age * .19F) * strain * .035F;
		head.yRot += Mth.clamp(yaw, -28, 28) * Mth.DEG_TO_RAD;
		head.xRot += Mth.clamp(pitch, -20, 25) * Mth.DEG_TO_RAD + fold * .28F - spread * .2F + recoil * .18F;
		head.zRot -= body.zRot * .7F;
		leftArm.zRot += fold * .68F - spread * .4F + recoil * .12F;
		rightArm.zRot -= fold * .68F - spread * .4F + recoil * .12F;
		leftArm.xRot -= fold * .85F + spread * .18F;
		rightArm.xRot -= fold * .85F + spread * .18F;
		leftForearm.xRot -= fold * .65F;
		rightForearm.xRot -= fold * .65F;
		leftForearm.yRot += fold * .23F;
		rightForearm.yRot -= fold * .23F;
		leftHand.zRot += Mth.sin(age * .15F) * strain * .08F;
		rightHand.zRot -= Mth.sin(age * .15F + 1) * strain * .08F;
		if (state == SeraphaeState.STABLE) {
			leftArm.xRot -= strain * .7F;
			rightArm.xRot -= strain * .7F;
			leftForearm.xRot -= strain * .3F;
			rightForearm.xRot -= strain * .3F;
		}
		for (int side = -1; side <= 1; side += 2) {
			ModelPart wing = side == 1 ? leftWing : rightWing;
			ModelPart outer = side == 1 ? leftWingOuter : rightWingOuter;
			String prefix = side == 1 ? "left" : "right";
			float beat = Mth.sin(age * .13F + side * .5F) * (.035F + strain * .1F);
			wing.xRot += beat - recoil * .25F;
			wing.yRot += side * (fold * .95F + spread * .22F - recoil * .18F);
			wing.zRot += side * (fold * .19F - spread * .34F);
			outer.xRot += Mth.sin(age * .13F - .8F + side * .5F) * (.04F + strain * .14F);
			outer.yRot += side * (fold * .55F + spread * .28F);
			outer.zRot += side * (fold * .12F - spread * .18F);
			for (int i = 0; i < 7; i++) {
				ModelPart feather = outer.getChild(prefix + "_primary_" + i);
				feather.xRot += SeraphaeVisualRules.featherWave(age, i, side, strain);
				feather.yRot += side * spread * i * .015F;
			}
			for (int i = 0; i < 6; i++)
				wing.getChild(prefix + "_inner_feather_" + i).xRot += SeraphaeVisualRules.featherWave(age - 2, i, side, strain) * .65F;
		}
		leftVeilTail.xRot += Mth.sin(age * .065F - 1) * (.045F + strain * .07F) - spread * .12F;
		rightVeilTail.xRot += Mth.sin(age * .065F) * (.045F + strain * .07F) - spread * .12F;
		leftHairLock.xRot += breath * .04F + recoil * .12F;
		rightHairLock.xRot -= breath * .04F - recoil * .12F;
		halo.zRot += Mth.sin(age * .045F) * .025F + body.zRot * .5F;
		halo.yRot += Mth.sin(age * .11F) * strain * .055F;
		hangChain(leftChain, leftArm, leftForearm, Mth.sin(age * .08F) * .025F);
		hangChain(rightChain, rightArm, rightForearm, -Mth.sin(age * .08F + .6F) * .025F);
		waistChain.xRot -= body.xRot;
		waistChain.zRot = -body.zRot + breath * .025F;
		for (int i = 0; i < 4; i++)
			body.getChild("robe_panel_" + i).xRot += Mth.sin(age * .075F - i * .8F) * (.035F + strain * .06F);
	}

	/** Posed hand in model space; the renderer converts it to a world-axis offset for the anchor tethers. */
	public org.joml.Vector3f wrist(boolean left) {
		ModelPart arm = left ? leftArm : rightArm;
		ModelPart forearm = left ? leftForearm : rightForearm;
		ModelPart hand = left ? leftHand : rightHand;
		PoseStack poses = new PoseStack();
		for (ModelPart part : new ModelPart[]{root, body, arm, forearm, hand}) part.translateAndRotate(poses);
		return poses.last().pose().transformPosition(new org.joml.Vector3f(0, 0, 0));
	}

	public org.joml.Vector3f featherTip(boolean left, int index) {
		ModelPart wing = left ? leftWing : rightWing;
		ModelPart outer = left ? leftWingOuter : rightWingOuter;
		ModelPart feather = outer.getChild((left ? "left" : "right") + "_primary_" + index);
		PoseStack poses = new PoseStack();
		for (ModelPart part : new ModelPart[]{root, body, wing, outer, feather}) part.translateAndRotate(poses);
		float tip = feather.cubes.stream().map(c -> c.maxY).max(Float::compare).orElse(1F);
		return poses.last().pose().transformPosition(new org.joml.Vector3f(0, tip / 16, 0));
	}

	private void hangChain(ModelPart chain, ModelPart arm, ModelPart forearm, float swing) {
		Matrix3f rotation = new Quaternionf().rotationZYX(body.zRot, body.yRot, body.xRot)
				.rotateZYX(arm.zRot, arm.yRot, arm.xRot)
				.rotateZYX(forearm.zRot, forearm.yRot, forearm.xRot)
				.conjugate().get(new Matrix3f());
		// Extract ZYX from the matrix: Minecraft's JOML 1.10.5 quaternion extraction miscomputes X.
		chain.xRot = (float) Math.atan2(rotation.m12(), rotation.m22());
		chain.yRot = -(float) Math.asin(Mth.clamp(rotation.m02(), -1.0F, 1.0F));
		chain.zRot = (float) Math.atan2(rotation.m01(), rotation.m00()) + swing;
	}
