
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		var motion = HemorathAnimState.of(entity);
		motion.update(entity, AnimationTime.partialTick());
		pose(entity.getVisualState(), entity.isCollapseCharging(), entity.isInPhase2(), motion.remaining(), ageInTicks,
				limbSwing, limbSwingAmount, netHeadYaw, headPitch);
		float attack = entity.getAttackAnim(AnimationTime.partialTick());
		float swipe = Mth.sin(Mth.sqrt(attack) * Mth.PI);
		rightArm.xRot -= swipe * .8F;
		rightForearm.xRot -= swipe * .4F;
		rightHand.zRot -= swipe * .3F;
	}

	void pose(int visual, boolean charging, boolean phase2, float remaining, float age,
			float stride, float movement, float yaw, float pitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		var action = HemorathVisualRules.pose(visual, charging, phase2, remaining);
		float breath = Mth.sin(age * .065F);
		float strain = action.pressure();
		hips.y += action.slump() * 5;
		chest.xRot += breath * .024F + action.bow() + (phase2 ? .055F : 0);
		chest.zRot += Mth.sin(age * .23F) * strain * .035F;
		head.yRot += Mth.clamp(yaw, -30, 30) * Mth.DEG_TO_RAD;
		head.xRot += Mth.clamp(pitch, -20, 25) * Mth.DEG_TO_RAD - action.lift() * .28F + action.slump() * .4F;
		head.zRot -= chest.zRot * .7F;
		rightArm.xRot -= action.lift() * 1.25F + action.reach() * .5F;
		rightArm.zRot += action.lift() * .5F + action.reach() * .65F;
		rightForearm.xRot -= action.lift() * .62F;
		rightForearm.yRot -= action.reach() * .3F;
		rightHand.xRot += action.recoil() * .4F - action.lift() * .22F;
		leftArm.xRot -= action.pressure() * .12F;
		leftForearm.xRot += action.recoil() * .15F;
		leftLeg.xRot += Mth.cos(stride * .45F) * movement * .2F;
		rightLeg.xRot -= Mth.cos(stride * .45F) * movement * .2F;
		for (int side = -1; side <= 1; side += 2) {
			String prefix = side == 1 ? "left" : "right";
			for (int i = 0; i < 7; i++) {
				ModelPart rib = ribcage.getChild(prefix + "_rib_" + i);
				float lag = Mth.sin(age * .17F - i * .65F + side * .3F);
				rib.zRot += side * (action.ribs() * .24F + lag * (.006F + action.ribs() * .025F));
				rib.yRot += side * action.ribs() * .055F;
			}
			for (int i = 0; i < 4; i++) {
				ModelPart finger = (side == 1 ? leftHand : rightHand).getChild(prefix + "_finger_" + i);
				finger.xRot -= side == -1 ? action.lift() * (.14F + i * .035F) : 0;
			}
		}
		Matrix3f handInverse = new Quaternionf().rotationZYX(chest.zRot, chest.yRot, chest.xRot)
				.rotateZYX(leftArm.zRot, leftArm.yRot, leftArm.xRot)
				.rotateZYX(leftForearm.zRot, leftForearm.yRot, leftForearm.xRot)
				.rotateZYX(leftHand.zRot, leftHand.yRot, leftHand.xRot).conjugate().get(new Matrix3f());
		setRotationFromMatrix(staff, handInverse);
		Matrix3f chestInverse = new Quaternionf().rotationZYX(chest.zRot, chest.yRot, chest.xRot)
				.conjugate().get(new Matrix3f());
		int chainIndex = 0;
		for (ModelPart chain : new ModelPart[]{leftShoulderChain, leftShoulderChainFront, rightShoulderChain, rightShoulderChainFront}) {
			setRotationFromMatrix(chain, chestInverse);
			chain.zRot += Mth.sin(age * .09F - chainIndex++ * .8F) * (.025F + strain * .055F);
		}
		staffChainLeft.zRot += Mth.sin(age * .09F - 1) * (.03F + strain * .07F);
		staffChainRight.zRot -= Mth.sin(age * .09F) * (.03F + strain * .07F);
		faceCords.xRot += Mth.sin(age * .065F - .8F) * (.035F + strain * .07F) - action.recoil() * .12F;
		waistChain.zRot += breath * .03F;
		for (int i = 0; i < 14; i++)
			hips.getChild("robe_strip_" + i).xRot += Mth.sin(age * .07F - i * .6F) * (.025F + action.recoil() * .12F);
	}

	public org.joml.Vector3f core() {
		return point(new org.joml.Vector3f(0, -.18F, -.1F), root, hips, chest);
	}

	public org.joml.Vector3f claw() {
		return point(new org.joml.Vector3f(0, .12F, 0), root, hips, chest, rightArm, rightForearm, rightHand);
	}

	public org.joml.Vector3f staffHead() {
		return point(new org.joml.Vector3f(0, -.12F, 0), root, hips, chest, leftArm, leftForearm, leftHand, staff, staffCrown);
	}

	private static org.joml.Vector3f point(org.joml.Vector3f at, ModelPart... chain) {
		PoseStack poses = new PoseStack();
		for (ModelPart part : chain) part.translateAndRotate(poses);
		return poses.last().pose().transformPosition(at);
	}

	private static void setRotationFromMatrix(ModelPart part, Matrix3f rotation) {
		// Avoid the incorrect X denominator in JOML 1.10.5's quaternion ZYX extraction.
		part.xRot = (float) Math.atan2(rotation.m12(), rotation.m22());
		part.yRot = -(float) Math.asin(Mth.clamp(rotation.m02(), -1.0F, 1.0F));
		part.zRot = (float) Math.atan2(rotation.m01(), rotation.m00());
	}
