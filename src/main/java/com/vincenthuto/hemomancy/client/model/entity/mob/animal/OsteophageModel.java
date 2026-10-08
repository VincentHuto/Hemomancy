package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.OsteophageEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class OsteophageModel extends HierarchicalModel<OsteophageEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("osteophage"), "main");
    private final ModelPart root, body, neck, head, leftWing, rightWing, leftLeg, rightLeg;

    public OsteophageModel(ModelPart root) {
        this.root = root;
        body = root.getChild("body"); neck = body.getChild("neck"); head = neck.getChild("head");
        leftWing = body.getChild("left_wing"); rightWing = body.getChild("right_wing");
        leftLeg = body.getChild("left_leg"); rightLeg = body.getChild("right_leg");
    }

    @Override public ModelPart root() { return root; }

    @Override public void setupAnim(OsteophageEntity bird, float limbSwing, float limbAmount,
                                    float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float flight = bird.getFlightPoseAmount(Mth.clamp(age - bird.tickCount, 0, 1));
        float dive = Mth.clamp((float) -bird.getDeltaMovement().y, -.2F, .4F);
        body.xRot += flight * (.56F + dive);
        neck.xRot += flight * .21F;
        head.xRot -= flight * .35F;
        head.yRot = Mth.clamp(headYaw, -40, 40) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(headPitch, -25, 25) * Mth.DEG_TO_RAD * (1 - flight);
        leftLeg.xRot += flight * .96F; rightLeg.xRot += flight * .96F;
        float spread = flight * (1.22F + Mth.sin(age * .55F) * .22F);
        leftWing.zRot += spread; rightWing.zRot -= spread;
        leftWing.getChild("left_forearm").zRot -= flight * .14F;
        rightWing.getChild("right_forearm").zRot += flight * .14F;
        body.getChild("tail").xRot += flight * .18F;
    }
}
