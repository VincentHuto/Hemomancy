package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.common.entity.mob.animal.SanguineRamEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class SanguineRamModel extends HierarchicalModel<SanguineRamEntity> {
    private static final float HIP_HEIGHT = 10.0F;
    private static final float HOOF_FORWARD_EXTENT = 1.75F;

    private final ModelPart root = SanguineRamMesh.createRoot();
    private final ModelPart body = root.getChild("body");
    private final ModelPart head = body.getChild("head");
    private final ModelPart tail = body.getChild("tail");
    private final ModelPart leftEar = head.getChild("ear_left");
    private final ModelPart rightEar = head.getChild("ear_right");
    private final ModelPart frontLeft = root.getChild("front_leg_left");
    private final ModelPart frontRight = root.getChild("front_leg_right");
    private final ModelPart hindLeft = root.getChild("hind_leg_left");
    private final ModelPart hindRight = root.getChild("hind_leg_right");

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(SanguineRamEntity ram, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
        pose(limbSwing, limbSwingAmount, ageInTicks, ram.getId(), netHeadYaw, headPitch);
    }

    void pose(float limbSwing, float limbSwingAmount, float age, int entityId, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float gait = Mth.clamp(limbSwingAmount, 0, 1);
        float phase = limbSwing * 0.6662F;
        float stride = Mth.cos(phase) * gait * 0.72F;
        frontLeft.xRot = hindRight.xRot = stride;
        frontRight.xRot = hindLeft.xRot = -stride;
        // Move the rig with the lowest rotating hoof corner so the stance pair reaches the ground.
        root.y += HIP_HEIGHT * (1 - Mth.cos(stride)) - HOOF_FORWARD_EXTENT * Mth.abs(Mth.sin(stride));

        float offsetAge = age + (entityId & 31) * 7;
        float breath = 1 + Mth.sin(offsetAge * 0.13F) * (1 - gait) * 0.018F;
        body.xScale = breath;
        body.yScale = breath;
        head.yRot = Mth.clamp(yaw, -55, 55) * Mth.DEG_TO_RAD;
        head.xRot = Mth.clamp(pitch, -40, 45) * Mth.DEG_TO_RAD + Mth.sin(phase * 2) * gait * 0.06F;
        tail.xRot -= gait * 0.3F;
        tail.yRot = Mth.sin(phase) * gait * 0.22F + Mth.sin(offsetAge * 0.09F) * (1 - gait) * 0.045F;
        leftEar.zRot = earFlick(offsetAge) * 0.27F;
        rightEar.zRot = -earFlick(offsetAge + 49) * 0.27F;
    }

    private static float earFlick(float age) {
        float progress = (age % 101) / 9;
        return Mth.sin(Mth.clamp(progress, 0, 1) * Mth.PI);
    }
}
