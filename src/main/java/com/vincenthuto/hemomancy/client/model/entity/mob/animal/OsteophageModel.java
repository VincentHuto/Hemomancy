package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.vincenthuto.hemomancy.client.render.tile.harbinger.functional.SpecimenJarRenderer;
import com.vincenthuto.hemomancy.common.entity.mob.animal.OsteophageEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class OsteophageModel extends HierarchicalModel<OsteophageEntity> {
    private final ModelPart root = OsteophageMesh.createRoot();
    private final ModelPart body = root.getChild("body");
    private final ModelPart neck = body.getChild("neck");
    private final ModelPart head = neck.getChild("head");
    private final ModelPart jaw = head.getChild("jaw");
    private final ModelPart tail = body.getChild("tail");
    private final Wing left = new Wing(body.getChild("wing_L"), "L");
    private final Wing right = new Wing(body.getChild("wing_R"), "R");
    private final ModelPart leftLeg = body.getChild("leg_L");
    private final ModelPart rightLeg = body.getChild("leg_R");
    private final ModelPart leftFoot = leftLeg.getChild("foot_L");
    private final ModelPart rightFoot = rightLeg.getChild("foot_R");

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(OsteophageEntity bird, float limbSwing, float limbAmount,
                          float age, float headYaw, float headPitch) {
        float partialTick = Mth.clamp(age - bird.tickCount, 0, 1);
        float turn = Mth.wrapDegrees(bird.yBodyRot - bird.yBodyRotO);
        float flight = bird.getFlightPoseAmount(partialTick);
        pose(flight, bird.getDeltaMovement(), turn, age, headYaw, headPitch, bird.getFeedingPose(partialTick));
        if (bird.deathTime == 0 && flight > 0 && SpecimenJarRenderer.isDisplayEntity(bird)) {
            // The wide wings set the jar scale. Lift the small flying body into the middle of the glass.
            root.y -= 22 * flight;
        }
    }

    void pose(float flight, Vec3 velocity, float turn, float age, float headYaw, float headPitch, float feeding) {
        root.getAllParts().forEach(ModelPart::resetPose);

        float air = flight * flight * (3 - 2 * flight);
        float perch = 1 - air;
        float vertical = (float) velocity.y;
        float dive = Mth.clamp((-vertical - .10F) / .32F, 0, 1);
        float climb = Mth.clamp(vertical / .24F, 0, 1);
        float power = Mth.lerp(dive, .20F + .32F * climb + .20F * (1 - air), .035F);
        float beat = age * .51F;
        float stroke = Mth.sin(beat);
        float bank = Mth.clamp(turn * .023F, -.32F, .32F);

        body.xRot += air * (.05F + .52F * dive - .32F * climb);
        body.zRot += air * bank;
        body.y += air * stroke * power * .65F;
        neck.xRot += perch * (.07F + .035F * Mth.sin(age * .13F)) - air * (.04F + .17F * dive);
        head.xRot += Mth.clamp(headPitch, -27, 27) * Mth.DEG_TO_RAD * (1 - .65F * air)
                - body.xRot * .55F + feeding * .18F;
        head.yRot += Mth.clamp(headYaw, -44, 44) * Mth.DEG_TO_RAD * (1 - .55F * air);
        neck.xRot += .17F * feeding;
        jaw.xRot += .34F * feeding;

        left.pose(1, air, dive, power, beat);
        right.pose(-1, air, dive, power, beat);
        tail.xRot += air * (.16F * climb - .24F * dive + .06F * stroke);
        tail.yRot -= air * bank * .25F;

        float tuck = air * (.82F - .66F * dive);
        leftLeg.xRot += tuck;
        rightLeg.xRot += tuck;
        leftFoot.xRot -= tuck * .58F;
        rightFoot.xRot -= tuck * .58F;
    }

    private record Wing(ModelPart shoulder, ModelPart elbow, ModelPart wrist) {
        Wing(ModelPart shoulder, String side) {
            this(shoulder, shoulder.getChild("forewing_" + side),
                    shoulder.getChild("forewing_" + side).getChild("handwing_" + side));
        }

        void pose(int side, float air, float dive, float power, float beat) {
            // Roll the feather planes down against the flank, then reverse the elbow and wrist into a Z-fold.
            float lift = .64F * Mth.sin(air * Mth.PI);
            shoulder.xRot += Mth.lerp(air, -1.06F, -.06F - .22F * dive);
            shoulder.yRot += side * Mth.lerp(air, -1.46F, -.42F * dive);
            shoulder.zRot += side * Mth.lerp(air, .06F, power * Mth.sin(beat) - .10F * dive) - side * lift;
            elbow.xRot += Mth.lerp(air, -.12F, .10F * dive);
            elbow.yRot += -side * air * .37F * dive;
            elbow.zRot += side * Mth.lerp(air, 2.85F, power * .72F * Mth.sin(beat - .78F));
            wrist.xRot += Mth.lerp(air, .17F, -.16F * dive);
            wrist.yRot += -side * air * .22F * dive;
            wrist.zRot += side * Mth.lerp(air, -2.95F, power * .61F * Mth.sin(beat - 1.43F));
        }
    }
}
