
    @Override
    public void setupAnim(T entity,float stride,float movement,float age,float yaw,float pitch) {
        var motion=VelorumAnimState.of(entity);
        motion.update(entity,AnimationTime.partialTick());
        pose(motion.phase(),age,yaw,pitch,entity.getAttackAnim(AnimationTime.partialTick()),
                movement,(float)com.vincenthuto.hemomancy.common.util.HemoEasing.smoothstep(entity.deathTime/18.0));
    }
    void pose(int visual,boolean martyrdom,float remaining,float age,float yaw,float pitch) {
        pose(VelorumVisualRules.legacy(visual,martyrdom,remaining),age,yaw,pitch,0,0,0);
    }
    void pose(VelorumVisualRules.Phase phase,float age,float yaw,float pitch,float attack,float movement,float death) {
        root.getAllParts().forEach(ModelPart::resetPose);
        var action=VelorumVisualRules.pose(phase);
        float life=1-death,gather=action.gather()*life,frost=action.frost()*life;
        float veil=action.veil()*life,drain=action.silence()*life,martyr=action.martyr()*life;
        float breath=Mth.sin(age*.045F),swipe=Mth.sin(Mth.sqrt(attack)*Mth.PI)*life;
        hips.y+=death*3;
        chest.xRot+=breath*.014F+gather*.16F-frost*.23F+drain*.12F+martyr*.14F+death*.55F;
        chest.zRot+=Mth.sin(age*.06F)*veil*.025F-swipe*.08F;
        head.yRot+=Mth.clamp(yaw,-20,20)*Mth.DEG_TO_RAD;
        head.xRot+=Mth.clamp(pitch,-15,15)*Mth.DEG_TO_RAD+gather*.08F-frost*.15F+veil*.17F+martyr*.2F+death*.25F;
        rightArm.xRot-=gather*.35F+frost*.8F+veil*.35F+swipe*.75F;
        rightArm.zRot+=gather*.15F+frost*.28F+veil*.4F;
        rightForearm.xRot-=drain*.6F+swipe*.3F;
        rightHand.xRot-=drain*.23F;
        leftArm.xRot-=gather*.04F;
        for(int i=0;i<4;i++) {
            var finger=rightHand.getChild("right_finger_"+i);
            finger.xRot+=drain*(.3F+i*.04F)+gather*.08F-frost*.1F;
            finger.getChild("right_finger_"+i+"_tip").xRot+=drain*.25F+swipe*.1F;
        }
        faceVeil.xRot+=breath*.025F-frost*.12F+veil*.08F;
        backVeil.xRot+=Mth.sin(age*.055F)*.02F-frost*.08F;
        for(int i=0;i<9;i++) {
            var strip=faceVeil.getChild("veil_strip_"+i);
            strip.xRot+=Mth.sin(age*.065F-i*.6F)*(.012F+veil*.025F)-frost*.05F;
            var mantle=chest.getChild("mantle_panel_"+i);
            mantle.xRot+=Mth.sin(age*.05F-i*.65F)*(.014F+movement*.025F)-frost*.055F+veil*.025F;
            mantle.getChild("mantle_panel_"+i+"_train").xRot+=Mth.sin(age*.055F-i*.65F-1)*.02F-frost*.04F;
        }
        for(int i=0;i<16;i++)hips.getChild("robe_panel_"+i).xRot+=Mth.sin(age*.05F-i*.7F)*(.012F+movement*.02F)-frost*.035F;
        setRotationFromMatrix(rightSleeve,armInverse(chest,rightArm,rightForearm,null));
        setRotationFromMatrix(clawIce,armInverse(chest,rightArm,rightForearm,rightHand));
        setRotationFromMatrix(leftSleeve,armInverse(chest,leftArm,leftForearm,null));
        setRotationFromMatrix(staff,armInverse(chest,leftArm,leftForearm,leftHand));
        pendantChain.zRot+=Mth.sin(age*.055F)*(.03F+frost*.05F+veil*.025F);
        sideChain.zRot-=Mth.sin(age*.055F+.8F)*(.025F+frost*.04F);
        staffLanternCrystal.yRot+=age*.006F;
        pendantLanternCrystal.yRot-=age*.005F;
        sideLanternCrystal.yRot+=age*.004F;
    }
    public org.joml.Vector3f core(){return point(0,0,-.18F,root,hips,chest);}
    public org.joml.Vector3f claw(){return point(0,.08F,0,root,hips,chest,rightArm,rightForearm,rightHand);}
    public org.joml.Vector3f[] lanterns(){
        return new org.joml.Vector3f[]{point(0,0,0,root,hips,chest,leftArm,leftForearm,leftHand,staff,staffLantern,staffLanternCrystal),
                point(0,0,0,root,hips,chest,leftArm,leftForearm,leftHand,staff,pendantChain,pendantLantern,pendantLanternCrystal),
                point(0,0,0,root,hips,chest,sideChain,sideLantern,sideLanternCrystal)};
    }
    private static org.joml.Vector3f point(float x,float y,float z,ModelPart... parts){
        PoseStack pose=new PoseStack();for(ModelPart part:parts)part.translateAndRotate(pose);
        return pose.last().pose().transformPosition(new org.joml.Vector3f(x,y,z));
    }
	private static Matrix3f armInverse(ModelPart chest, ModelPart arm, ModelPart forearm, ModelPart hand) {
		Quaternionf rotation = new Quaternionf().rotationZYX(chest.zRot, chest.yRot, chest.xRot)
				.rotateZYX(arm.zRot, arm.yRot, arm.xRot)
				.rotateZYX(forearm.zRot, forearm.yRot, forearm.xRot);
		if (hand != null) rotation.rotateZYX(hand.zRot, hand.yRot, hand.xRot);
		return rotation.conjugate().get(new Matrix3f());
	}

	private static void setRotationFromMatrix(ModelPart part, Matrix3f rotation) {
		// Matrix extraction avoids the quaternion ZYX X denominator error in JOML 1.10.5.
		part.xRot = (float) Math.atan2(rotation.m12(), rotation.m22());
		part.yRot = -(float) Math.asin(Mth.clamp(rotation.m02(), -1.0F, 1.0F));
		part.zRot = (float) Math.atan2(rotation.m01(), rotation.m00());
	}
