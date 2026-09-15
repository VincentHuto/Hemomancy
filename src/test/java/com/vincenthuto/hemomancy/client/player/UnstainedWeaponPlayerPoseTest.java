package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnstainedWeaponPlayerPoseTest {
    @Test
    void daggerDrivesUpAndForwardFromItsLowWindup() {
        var low = hand(true, .20F, true);
        var strike = hand(true, .52F, true);
        // Grip centre after the authored renderer, before the animated hand transform.
        Vector3f lowGrip = new Vector3f(.10F, -.185F, -.16F).mulPosition(low);
        Vector3f highGrip = new Vector3f(.10F, -.185F, -.16F).mulPosition(strike);
        assertTrue(highGrip.y > lowGrip.y + .3F, "The dagger must rise from below the target");
        assertTrue(highGrip.z < lowGrip.z, "The rising strike must also reach outward");
        Vector3f lowBlade = new Vector3f(0, 1, 0).mulDirection(low);
        Vector3f highBlade = new Vector3f(0, 1, 0).mulDirection(strike);
        assertTrue(lowBlade.y < 0, "The blade must begin pointing down, not stay upright behind a punching hilt");
        assertTrue(highBlade.y > .5F && highBlade.z < -.4F, "The tip must rise and extend ahead of the hilt");
        assertTrue(highGrip.y - lowGrip.y > 2 * (lowGrip.z - highGrip.z),
                "The dagger should travel mostly upward instead of lunging straight forward");
        Vector3f lowTip = thirdPersonBlade(UnstainedWeaponPlayerPose.dagger(.20F, true));
        Vector3f highTip = thirdPersonBlade(UnstainedWeaponPlayerPose.dagger(.52F, true));
        assertTrue(lowTip.y < 0, "The dagger starts below the guard");
        assertTrue(highTip.y > .5F && highTip.z > .2F, "The blade must point up and forward, not back over the shoulder");
    }

    @Test
    void hammerLiftsItsHeadBeforeTheDownstroke() {
        var rest = hammerHand(0, true);
        var raised = hammerHand(.38F, true);
        var dropped = hammerHand(.66F, true);
        // Bell centre after the authored renderer and ItemRenderer's centering translation.
        Vector3f head = new Vector3f(-.08F, .603125F, -.1F);
        Vector3f restHead = new Vector3f(head).mulPosition(rest);
        Vector3f raisedHead = new Vector3f(head).mulPosition(raised);
        Vector3f droppedHead = new Vector3f(head).mulPosition(dropped);
        assertTrue(raisedHead.y > restHead.y + .12F, "The hammer head must lift visibly before the strike");
        assertTrue(raisedHead.y > droppedHead.y + .3F, "The raised head must then come down");
        assertTrue(rest.equals(hammerHand(1, true), .00001F));
        var left = hammerHand(.38F, false);
        assertEquals(-raised.m30(), left.m30(), .00001F);
        assertEquals(raised.m31(), left.m31(), .00001F);
        assertEquals(raised.m32(), left.m32(), .00001F);
    }

    @Test
    void glaiveCutsAcrossAHorizontalFanInsteadOfChoppingDown() {
        Vector3f entry = new Vector3f(0, 1, 0).mulDirection(hand(false, .28F, true));
        Vector3f exit = new Vector3f(0, 1, 0).mulDirection(hand(false, .68F, true));
        assertTrue(entry.x > .6F && exit.x < -.6F, "The blade must cross from the outer side to the opposite side");
        assertEquals(0, entry.y, .01F);
        assertEquals(0, exit.y, .01F);
        assertTrue(entry.angle(exit) > Math.toRadians(100), "The horizontal fan must be broad");
        var startArm = UnstainedWeaponPlayerPose.glaive(.28F, true);
        var endArm = UnstainedWeaponPlayerPose.glaive(.68F, true);
        Vector3f thirdEntry = thirdPersonBlade(startArm);
        Vector3f thirdExit = thirdPersonBlade(endArm);
        assertTrue(thirdEntry.x < -.6F && thirdExit.x > .6F,
                "The player model must sweep from the same outer side as the first-person view");
        assertEquals(0, thirdEntry.y, .01F, "The held model must also stay horizontal");
        assertEquals(0, thirdExit.y, .01F);
        assertTrue(thirdEntry.angle(thirdExit) > Math.toRadians(100));
    }

    @Test
    void bothSwingsEaseBackIntoTheSameRestingGrip() {
        for (boolean dagger : new boolean[]{true, false}) {
            var rest = hand(dagger, 0, true);
            assertTrue(rest.equals(hand(dagger, 1, true), .00001F));
            assertTrue(rest.equals(hand(dagger, .999F, true), .001F), "Recovery must not snap at the final frame");
        }
        assertEquals(0, UnstainedWeaponPlayerPose.dagger(1, true).weight());
        assertEquals(0, UnstainedWeaponPlayerPose.glaive(1, true).weight());
    }

    @Test
    void leftHandMirrorsTheStrokeWithoutReversingItsHeightOrReach() {
        for (boolean dagger : new boolean[]{true, false}) {
            for (float progress : new float[]{.15F, .35F, .55F, .8F}) {
                var right = hand(dagger, progress, true);
                var left = hand(dagger, progress, false);
                assertEquals(-right.m30(), left.m30(), .00001F);
                assertEquals(right.m31(), left.m31(), .00001F);
                assertEquals(right.m32(), left.m32(), .00001F);
                var rightArm = dagger ? UnstainedWeaponPlayerPose.dagger(progress, true)
                        : UnstainedWeaponPlayerPose.glaive(progress, true);
                var leftArm = dagger ? UnstainedWeaponPlayerPose.dagger(progress, false)
                        : UnstainedWeaponPlayerPose.glaive(progress, false);
                assertEquals(rightArm.armXRot(), leftArm.armXRot(), .00001F);
                assertEquals(-rightArm.armYRot(), leftArm.armYRot(), .00001F);
                assertEquals(-rightArm.armZRot(), leftArm.armZRot(), .00001F);
                assertEquals(-rightArm.bodyYRot(), leftArm.bodyYRot(), .00001F);
            }
        }
    }

    private static Matrix4f hand(boolean dagger, float progress, boolean right) {
        PoseStack poses = new PoseStack();
        if (dagger) UnstainedWeaponPlayerPose.firstPersonDagger(poses, progress, 0, right);
        else UnstainedWeaponPlayerPose.firstPersonGlaive(poses, progress, 0, right);
        return poses.last().pose();
    }

    private static Matrix4f hammerHand(float progress, boolean right) {
        PoseStack poses = new PoseStack();
        UnstainedWeaponPlayerPose.firstPersonHammer(poses, progress, 0, right);
        return poses.last().pose();
    }

    private static Vector3f thirdPersonBlade(UnstainedWeaponPlayerPose.ArmPose arm) {
        // Player renderer, arm, vanilla held-item grip, then the Unstained model's upright axis.
        Matrix4f transform = new Matrix4f().rotateY((float)Math.PI).scale(-1, -1, 1)
                .rotateZYX(arm.armZRot(), arm.armYRot(), arm.armXRot())
                .rotateX((float)-Math.PI / 2).rotateY((float)Math.PI).rotateX((float)Math.PI);
        return new Vector3f(0, -1, 0).mulDirection(transform);
    }
}
