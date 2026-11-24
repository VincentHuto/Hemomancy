package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class CircusAcrobatModel extends CircusJointedPerformerModel<CircusAcrobatEntity> {
    private final CircusWindupPose windup = new CircusWindupPose();

    private final ModelPart sash;
    private final ModelPart leftRibbon;
    private final ModelPart rightRibbon;

    public CircusAcrobatModel() {
        super(CircusAcrobatMesh.createRoot(), 2F, 0.3F);
        sash = body.getChild("sash");
        leftRibbon = head.getChild("left_ribbon");
        rightRibbon = head.getChild("right_ribbon");
    }

    @Override
    public void setupAnim(CircusAcrobatEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        float flutter = Mth.sin(ageInTicks * 0.22F) * 0.12F;
        sash.xRot = flutter - 0.16F * limbSwingAmount;
        leftRibbon.zRot = flutter;
        rightRibbon.zRot = -flutter;
        if (entity.getActState() == ActState.SETUP || entity.getActState() == ActState.PERFORM) {
            leftArm.xRot = rightArm.xRot = -2.65F;
            leftArm.zRot = -0.15F;
            rightArm.zRot = 0.15F;
        }
        // Wind-up: a low crouch, arms swept back to spring; the strike throws both arms forward.
        windup.sample(entity, ageInTicks);
        float charge = windup.charge();
        float strike = windup.strike();
        body.xRot = Mth.lerp(charge, body.xRot, 0.3F);
        head.xRot = Mth.lerp(charge, head.xRot, -0.35F);
        rightArm.xRot = Mth.lerp(charge, rightArm.xRot, 0.95F);
        leftArm.xRot = Mth.lerp(charge, leftArm.xRot, 0.95F);
        rightArm.zRot = Mth.lerp(charge, rightArm.zRot, 0.35F);
        leftArm.zRot = Mth.lerp(charge, leftArm.zRot, -0.35F);
        rightArm.xRot = Mth.lerp(strike, rightArm.xRot, -1.75F);
        leftArm.xRot = Mth.lerp(strike, leftArm.xRot, -1.55F);
        body.xRot = Mth.lerp(strike, body.xRot, 0.15F);
        rightElbow.xRot = -0.12F - 0.3F * strike;
        leftElbow.xRot = -0.12F - 0.22F * strike;
        finishPose(entity, ageInTicks);
        if (entity.getActState() != ActState.DOWNED && !entity.isPassenger() && !entity.onGround()
                && entity.level().noCollision(entity, entity.getBoundingBox().move(0.0D, -0.08D, 0.0D))) {
            tuckInTheAir();
        }
    }

    /**
     * Audit E: off the ground the walk cycle threw the legs into a split; an airborne acrobat tucks both knees instead.
     * The authored knee pivots stay under the tucked thighs.
     */
    private void tuckInTheAir() {
        rightCalf.xRot = 1.75F;
        leftCalf.xRot = 1.75F;
        rightLeg.xRot = -1.15F;
        leftLeg.xRot = -1.05F;
        rightLeg.yRot = leftLeg.yRot = 0.0F;
        rightLeg.zRot = 0.08F;
        leftLeg.zRot = -0.08F;
    }
}
