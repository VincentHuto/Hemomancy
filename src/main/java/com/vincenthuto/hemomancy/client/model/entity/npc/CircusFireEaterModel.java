package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFireEaterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class CircusFireEaterModel extends CircusJointedPerformerModel<CircusFireEaterEntity> {
    private final CircusWindupPose windup = new CircusWindupPose();

    private final ModelPart gorget;
    private final ModelPart cowlTail;

    public CircusFireEaterModel() {
        super(CircusFireEaterMesh.createRoot(), 2F, 0.3F);
        gorget = body.getChild("chest").getChild("gorget");
        cowlTail = head.getChild("cowl_tail");
    }

    @Override
    public void setupAnim(CircusFireEaterEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        cowlTail.zRot = Mth.sin(ageInTicks * 0.09F) * 0.08F;
        gorget.xScale = gorget.zScale = 1.0F;
        if (entity.getActState() == ActState.SETUP || entity.getActState() == ActState.PERFORM) {
            head.xRot -= 0.35F;
            rightArm.xRot = -1.55F;
            leftArm.xRot = -0.8F + Mth.sin(ageInTicks * 0.18F) * 0.08F;
            gorget.xScale = gorget.zScale = 1.08F;
        }
        // Wind-up: head thrown back and the torch raised to the mouth while the breath is drawn in, the throat
        // swelling; the strike lunges the head and chest forward into the cone.
        windup.sample(entity, ageInTicks);
        float charge = windup.charge();
        float strike = windup.strike();
        head.xRot = Mth.lerp(charge, head.xRot, -0.85F);
        body.xRot = Mth.lerp(charge, body.xRot, -0.12F);
        rightArm.xRot = Mth.lerp(charge, rightArm.xRot, -2.35F);
        rightArm.zRot = Mth.lerp(charge, rightArm.zRot, 0.3F);
        leftArm.xRot = Mth.lerp(charge, leftArm.xRot, -0.4F);
        float swell = 1.0F + 0.22F * charge;
        gorget.xScale *= swell;
        gorget.zScale *= swell;
        head.xRot = Mth.lerp(strike, head.xRot, 0.25F);
        body.xRot = Mth.lerp(strike, body.xRot, 0.2F);
        rightArm.xRot = Mth.lerp(strike, rightArm.xRot, -1.45F);
        rightElbow.xRot = -0.12F - 0.45F * charge - 0.35F * strike;
        leftElbow.xRot = -0.16F - 0.25F * strike;
        cowlTail.xRot = 0.25F * strike;
        finishPose(entity, ageInTicks);
    }
}
