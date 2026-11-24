package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.render.world.PerformerHandAnchors;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Vector3f;

/** A humanoid pose with articulated hands and knees for the four physical performers. */
public abstract class CircusJointedPerformerModel<T extends CircusPerformerEntity> extends HumanoidModel<T> {
    protected final ModelPart rightElbow;
    protected final ModelPart leftElbow;
    protected final ModelPart rightCalf;
    protected final ModelPart leftCalf;
    private final ModelPart rightHand;
    private final ModelPart leftHand;
    private final Vector3f rightGrip;
    private final Vector3f leftGrip;
    private final ModelPart root;
    private final CircusDownedPose downedPose;

    protected CircusJointedPerformerModel(ModelPart root, float seatDepth, float kneeBend) {
        super(root);
        this.root = root;
        rightElbow = rightArm.getChild("right_elbow");
        leftElbow = leftArm.getChild("left_elbow");
        rightHand = rightElbow.getChild("right_hand");
        leftHand = leftElbow.getChild("left_hand");
        rightCalf = rightLeg.getChild("right_calf");
        leftCalf = leftLeg.getChild("left_calf");
        rightGrip = PerformerHandAnchors.gripOf(rightHand);
        leftGrip = PerformerHandAnchors.gripOf(leftHand);
        downedPose = new CircusDownedPose(this, seatDepth, kneeBend);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
            float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // Vanilla writes fixed humanoid pivots for the head, torso, shoulders and hips.
        // Use its joint rotations with the authored positions.
        restorePivot(body);
        restorePivot(head);
        restorePivot(rightArm);
        restorePivot(leftArm);
        restorePivot(rightLeg);
        restorePivot(leftLeg);
    }

    protected final void finishPose(T entity, float ageInTicks) {
        if (entity.getActState() == CircusPerformerEntity.ActState.DOWNED) {
            downedPose.apply(true, ageInTicks);
            return;
        }
        carryWithBody(head);
        carryWithBody(rightArm);
        carryWithBody(leftArm);
    }

    private static void restorePivot(ModelPart part) {
        PartPose rest = part.getInitialPose();
        part.setPos(rest.x, rest.y, rest.z);
    }

    private void carryWithBody(ModelPart part) {
        float y = part.y - body.y;
        float z = part.z - body.z;
        float cos = Mth.cos(body.xRot);
        float sin = Mth.sin(body.xRot);
        part.y = body.y + y * cos - z * sin;
        part.z = body.z + y * sin + z * cos;
        part.xRot += body.xRot;
    }

    @Override
    public void translateToHand(HumanoidArm side, PoseStack stack) {
        boolean right = side == HumanoidArm.RIGHT;
        (right ? rightArm : leftArm).translateAndRotate(stack);
        (right ? rightElbow : leftElbow).translateAndRotate(stack);
        (right ? rightHand : leftHand).translateAndRotate(stack);
        Vector3f grip = right ? rightGrip : leftGrip;
        stack.translate(grip.x / 16.0F, grip.y / 16.0F, grip.z / 16.0F);
    }
}

