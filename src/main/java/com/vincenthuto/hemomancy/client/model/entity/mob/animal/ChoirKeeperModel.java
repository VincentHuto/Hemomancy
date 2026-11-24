package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.common.entity.mob.animal.ChoirKeeperEntity;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class ChoirKeeperModel extends HierarchicalModel<ChoirKeeperEntity> {
    private final ModelPart root = ChoirKeeperMesh.createRoot();
    private final ModelPart body = root.getChild("body");
    private final ModelPart head = body.getChild("head");
    private final ModelPart tail = body.getChild("tail_display");
    private final List<ModelPart> feathers = Stream.of(tail.getChild("outer_train"), tail.getChild("inner_train"))
            .flatMap(train -> train.getAllParts().filter(part -> part != train))
            .toList();
    private final ModelPart leftWing = body.getChild("left_wing");
    private final ModelPart rightWing = body.getChild("right_wing");
    private final ModelPart leftPrimaries = leftWing.getChild("left_primaries");
    private final ModelPart rightPrimaries = rightWing.getChild("right_primaries");
    private final ModelPart leftLeg = body.getChild("left_leg");
    private final ModelPart rightLeg = body.getChild("right_leg");

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(ChoirKeeperEntity entity, float limbSwing, float limbSwingAmount,
                          float age, float headYaw, float headPitch) {
        float partialTick = Mth.clamp(age - entity.tickCount, 0, 1);
        pose(entity.getFlightPoseAmount(partialTick), entity.getFlareAmount(partialTick),
                (float) entity.getDeltaMovement().y, Mth.wrapDegrees(entity.yBodyRot - entity.yBodyRotO),
                age, headYaw, headPitch);
    }

    void pose(float flight, float flare, float verticalSpeed, float turn, float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float air = smooth(flight);
        float display = smooth(flare);
        float climb = Mth.clamp(verticalSpeed * 1.4F, -.38F, .36F);
        float beat = age * .62F;
        float stroke = Mth.sin(beat);
        float bank = Mth.clamp(turn * .018F, -.26F, .26F);
        body.xRot += air * (.34F - climb);
        body.zRot += air * bank;
        body.y += air * stroke * .35F;

        head.xRot += Mth.clamp(headPitch, -27, 27) * Mth.DEG_TO_RAD * (1 - .6F * air)
                - .74F * body.xRot;
        head.yRot += Mth.clamp(headYaw, -55, 55) * Mth.DEG_TO_RAD * (1 - .55F * air);
        head.zRot -= .65F * body.zRot;
        leftLeg.xRot += air * (.72F + climb * .4F);
        rightLeg.xRot += air * (.72F + climb * .4F);

        poseWing(leftWing, leftPrimaries, -1, air, beat, stroke);
        poseWing(rightWing, rightPrimaries, 1, air, beat, stroke);

        float foldedPitch = Mth.lerp(air, -1.39F, -1.60F - body.xRot * .38F + .06F * stroke);
        tail.xRot = Mth.lerp(display, foldedPitch, tail.xRot - body.xRot + .04F * stroke * air);
        tail.zRot -= air * bank * .55F;
        for (ModelPart feather : feathers) {
            feather.zRot *= Mth.lerp(display, Mth.lerp(air, .10F, .17F), 1);
        }
    }

    private static void poseWing(ModelPart wing, ModelPart primaries, int side, float air, float beat, float stroke) {
        wing.yRot += side * air * (.26F + .09F * Mth.cos(beat));
        wing.zRot += -side * air * (1.34F + .54F * stroke);
        primaries.zRot += -side * air * (.14F + .37F * Mth.sin(beat - .85F));
    }

    private static float smooth(float progress) {
        return progress * progress * (3 - 2 * progress);
    }
}
