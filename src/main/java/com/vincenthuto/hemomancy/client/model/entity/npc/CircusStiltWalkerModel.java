package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusStiltWalkerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class CircusStiltWalkerModel extends CircusJointedPerformerModel<CircusStiltWalkerEntity> {
    private final CircusWindupPose windup = new CircusWindupPose();

    private final ModelPart leftTail;
    private final ModelPart rightTail;
    private final ModelPart feather;

    public CircusStiltWalkerModel() {
        super(CircusStiltWalkerMesh.createRoot(), 2F, 0F);
        leftTail = body.getChild("left_tail");
        rightTail = body.getChild("right_tail");
        feather = head.getChild("top_hat").getChild("feather");
    }

    @Override
    public void setupAnim(CircusStiltWalkerEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        rightLeg.xRot *= 0.15F;
        leftLeg.xRot *= 0.15F;
        float sway = Mth.sin(ageInTicks * 0.08F) * 0.08F;
        leftTail.zRot = sway * 0.7F;
        rightTail.zRot = -sway * 0.7F;
        feather.zRot = sway * 1.4F;
        if (entity.getActState() == ActState.PERFORM) {
            leftArm.zRot = -0.7F;
            rightArm.zRot = 0.7F;
            if (entity.isSpinning()) {
                rightLeg.xRot = leftLeg.xRot = 0.0F;
                rightLeg.zRot = 0.0F;
                leftLeg.zRot = -Mth.PI / 4.0F;
            }
        }
        if (entity.getActState() == ActState.ALERT) leftArm.xRot = rightArm.xRot = -0.55F;
        // Wind-up: both arms raised high and the body rearing back on the stilts; the strike brings them down for
        // the slam.
        windup.sample(entity, ageInTicks);
        float charge = windup.charge();
        float strike = windup.strike();
        rightArm.xRot = Mth.lerp(charge, rightArm.xRot, -3.0F);
        leftArm.xRot = Mth.lerp(charge, leftArm.xRot, -3.0F);
        rightArm.zRot = Mth.lerp(charge, rightArm.zRot, -0.2F);
        leftArm.zRot = Mth.lerp(charge, leftArm.zRot, 0.2F);
        body.xRot = Mth.lerp(charge, body.xRot, -0.12F);
        rightArm.xRot = Mth.lerp(strike, rightArm.xRot, -0.4F);
        leftArm.xRot = Mth.lerp(strike, leftArm.xRot, -0.4F);
        body.xRot = Mth.lerp(strike, body.xRot, 0.2F);
        rightElbow.xRot = -0.13F - 0.35F * strike;
        leftElbow.xRot = rightElbow.xRot;
        finishPose(entity, ageInTicks);
    }
}
