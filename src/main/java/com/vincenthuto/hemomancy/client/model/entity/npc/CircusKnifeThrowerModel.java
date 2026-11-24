package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.google.common.collect.Iterables;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusKnifeThrowerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.List;

public final class CircusKnifeThrowerModel extends CircusJointedPerformerModel<CircusKnifeThrowerEntity> {
    private final CircusWindupPose windup = new CircusWindupPose();

    private final ModelPart knifeFan;
    private final ModelPart sideCowl;
    private final ModelPart[] jugglingKnives;

    public CircusKnifeThrowerModel() {
        this(CircusKnifeThrowerMesh.createRoot());
    }

    private CircusKnifeThrowerModel(ModelPart root) {
        super(root, 2F, 0.3F);
        knifeFan = leftElbow.getChild("left_hand").getChild("knife_fan");
        sideCowl = head.getChild("side_cowl");
        jugglingKnives = new ModelPart[] { root.getChild("juggle_knife_0"),
                root.getChild("juggle_knife_1"), root.getChild("juggle_knife_2") };
        for (ModelPart knife : jugglingKnives) knife.visible = false;
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        // These knives leave the hands. Draw them in the model's ground frame.
        return Iterables.concat(super.bodyParts(), List.of(jugglingKnives));
    }

    @Override
    public void setupAnim(CircusKnifeThrowerEntity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        knifeFan.yRot = Mth.sin(ageInTicks * 0.07F) * 0.08F;
        sideCowl.zRot = Mth.sin(ageInTicks * 0.1F) * 0.07F;
        knifeFan.visible = !entity.isJuggling();
        for (int i = 0; i < jugglingKnives.length; i++) {
            ModelPart knife = jugglingKnives[i];
            knife.visible = entity.isJuggling();
            if (!knife.visible) continue;
            float cycle = Mth.frac(ageInTicks * 0.08F + i / 3.0F);
            knife.x = Mth.lerp(cycle, -6.5F, 6.5F);
            knife.y = 5.0F - 57.0F * cycle * (1.0F - cycle);
            knife.z = -7.0F;
            knife.zRot = cycle * Mth.TWO_PI;
        }
        if (entity.getActState() == ActState.PERFORM || entity.getActState() == ActState.ALERT) {
            rightArm.xRot = -1.55F + Mth.sin(ageInTicks * 0.32F) * 0.35F;
            leftArm.xRot = -0.45F;
        }
        // Wind-up: the throwing arm drawn back over the shoulder, the head turned along the throw; the strike whips
        // it forward and down as the knives leave.
        windup.sample(entity, ageInTicks);
        float charge = windup.charge();
        float strike = windup.strike();
        rightArm.xRot = Mth.lerp(charge, rightArm.xRot, -2.85F);
        rightArm.zRot = Mth.lerp(charge, rightArm.zRot, 0.3F);
        rightArm.yRot = Mth.lerp(charge, rightArm.yRot, 0.35F);
        leftArm.xRot = Mth.lerp(charge, leftArm.xRot, -1.2F);
        leftArm.zRot = Mth.lerp(charge, leftArm.zRot, -0.5F);
        rightArm.xRot = Mth.lerp(strike, rightArm.xRot, -0.85F);
        rightArm.yRot = Mth.lerp(strike, rightArm.yRot, -0.25F);
        rightElbow.xRot = -0.15F - 0.65F * charge;
        leftElbow.xRot = -0.2F - 0.28F * charge;
        if (entity.isJuggling()) {
            rightElbow.xRot -= 0.36F;
            leftElbow.xRot -= 0.72F;
        }
        finishPose(entity, ageInTicks);
    }
}
