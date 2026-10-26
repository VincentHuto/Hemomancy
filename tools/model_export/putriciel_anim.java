
    @Override
    public void setupAnim(T entity, float stride, float movement, float age, float yaw, float pitch) {
        var motion = PutricielAnimState.of(entity);
        motion.update(entity, AnimationTime.partialTick());
        pose(entity.getVisualState(), entity.isAbsolved(), entity.getAbsolutionCount(), motion.visual(),
                motion.window(), motion.nova(), motion.release(), age, yaw, pitch, stride, movement, entity.getAttackAnim(AnimationTime.partialTick()));
    }

    void pose(int visual, boolean absolved, int count, float remaining, float age, float yaw, float pitch) {
        pose(visual, absolved, count, remaining, absolved ? remaining : 0,
                PutricielVisualRules.NOVA_INTERVAL, visual == 1 ? remaining : 0, age, yaw, pitch, 0, 0, 0);
    }

    void pose(int visual, boolean absolved, int count, float remaining, float window, float nova, float novaRelease,
              float age, float yaw, float pitch, float stride, float movement, float attack) {
        root.getAllParts().forEach(ModelPart::resetPose);
        var action = PutricielVisualRules.pose(visual, absolved, count, remaining, window, nova, novaRelease);
        float breath = Mth.sin(age * .065F);
        float gather = action.gather(), release = action.release(), offer = action.offering(), flinch = action.flinch();
        float swipe = Mth.sin(Mth.sqrt(attack) * Mth.PI);
        chest.xRot += breath * .018F + gather * .23F - release * .3F + offer * .12F + flinch * .25F;
        chest.zRot += Mth.sin(age * .13F) * gather * .025F - swipe * .12F;
        head.yRot += Mth.clamp(yaw, -25, 25) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(pitch, -20, 20) * Mth.DEG_TO_RAD + gather * .12F - release * .22F + offer * .15F + flinch * .22F;
        rightArm.xRot += gather * .25F - release * .85F + offer * .16F - swipe * .65F;
        rightArm.zRot += gather * .12F + release * .3F + offer * .12F;
        rightForearm.xRot += flinch * .2F - gather * .25F - swipe * .3F;
        leftArm.xRot -= gather * .08F;
        leftForearm.xRot += flinch * .06F;
        for (int i = 0; i < 4; i++) {
            var finger = rightHand.getChild("offering_finger_" + i);
            finger.xRot += gather * (.14F + i * .025F) - offer * .08F + flinch * .2F;
            finger.getChild("offering_finger_" + i + "_tip").xRot += gather * .12F - release * .2F;
        }
        int index = 0;
        for (ModelPart spore : new ModelPart[]{spore0, spore1, spore2, spore3, shoulderSpore, hemSpore}) {
            float lag = Mth.sin(age * .11F - index++ * .9F);
            float swell = 1 + lag * .025F + action.spores() * (.85F + lag * .15F);
            spore.xScale = spore.zScale = swell;
            spore.yScale = 1 / swell;
            spore.zRot += lag * (.012F + gather * .02F);
        }
        Matrix3f offering = new Quaternionf().rotationZYX(chest.zRot, chest.yRot, chest.xRot)
                .rotateZYX(rightArm.zRot, rightArm.yRot, rightArm.xRot)
                .rotateZYX(rightForearm.zRot, rightForearm.yRot, rightForearm.xRot)
                .conjugate().get(new Matrix3f());
        setRotationFromMatrix(rightHand, offering);
        Matrix3f grip = new Quaternionf().rotationZYX(chest.zRot, chest.yRot, chest.xRot)
                .rotateZYX(leftArm.zRot, leftArm.yRot, leftArm.xRot)
                .rotateZYX(leftForearm.zRot, leftForearm.yRot, leftForearm.xRot)
                .rotateZYX(leftHand.zRot, leftHand.yRot, leftHand.xRot)
                .conjugate().get(new Matrix3f());
        setRotationFromMatrix(staff, grip);
        staffChainLeft.zRot += Mth.sin(age * .075F) * (.04F + release * .1F);
        staffChainRight.zRot -= Mth.sin(age * .075F + .9F) * (.04F + release * .09F);
        leftVestmentChain.xRot -= chest.xRot * .5F;
        rightVestmentChain.xRot -= chest.xRot * .5F;
        leftVestmentChain.zRot += Mth.sin(age * .07F) * .04F;
        rightVestmentChain.zRot -= Mth.sin(age * .07F + 1) * .04F;
        faceVines.xRot += breath * .03F - release * .15F + flinch * .08F;
        offeringDrips.xRot += Mth.sin(age * .12F) * .035F + release * .14F;
        leftMantle.xRot += Mth.sin(age * .07F) * .018F - release * .08F;
        rightMantle.xRot += Mth.sin(age * .07F + 1.4F) * .018F - release * .08F;
        backColony.xRot += Mth.sin(age * .06F) * .012F - release * .045F;
        for (int i = 0; i < 14; i++) {
            var strip = hips.getChild("robe_panel_" + i);
            strip.xRot += Mth.sin(age * .07F - i * .7F) * (.018F + movement * .025F)
                    + release * (.04F + (i % 3) * .015F);
        }
    }

    public org.joml.Vector3f core() { return point(0, 0, -.2F, root, hips, chest); }
    public org.joml.Vector3f palm() { return point(0, -.08F, 0, root, hips, chest, rightArm, rightForearm, rightHand); }
    public org.joml.Vector3f[] sacs() {
        return new org.joml.Vector3f[]{point(0, 0, 0, root, hips, chest, sporeCluster, spore0),
                point(0, 0, 0, root, hips, chest, sporeCluster, spore1),
                point(0, 0, 0, root, hips, chest, sporeCluster, spore2),
                point(0, 0, 0, root, hips, chest, sporeCluster, spore3)};
    }
    public org.joml.Vector3f[] censers() {
        return new org.joml.Vector3f[]{point(0, -.08F, 0, root, hips, chest, leftArm, leftForearm, leftHand, staff, staffHead, staffChainLeft, censerLeft),
                point(0, -.08F, 0, root, hips, chest, leftArm, leftForearm, leftHand, staff, staffHead, staffChainRight, censerRight)};
    }
    private static org.joml.Vector3f point(float x, float y, float z, ModelPart... parts) {
        PoseStack pose = new PoseStack();
        for (ModelPart part : parts) part.translateAndRotate(pose);
        return pose.last().pose().transformPosition(new org.joml.Vector3f(x, y, z));
    }
    private static void setRotationFromMatrix(ModelPart part, Matrix3f rotation) {
        part.xRot = (float) Math.atan2(rotation.m12(), rotation.m22());
        part.yRot = -(float) Math.asin(Mth.clamp(rotation.m02(), -1, 1));
        part.zRot = (float) Math.atan2(rotation.m01(), rotation.m00());
    }
