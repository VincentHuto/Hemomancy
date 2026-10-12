package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.client.player.UnstainedWeaponPlayerPose.DAGGER_CONTACT;
import static com.vincenthuto.hemomancy.client.player.UnstainedWeaponPlayerPose.HAMMER_APEX;
import static com.vincenthuto.hemomancy.client.player.UnstainedWeaponPlayerPose.HAMMER_CONTACT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UnstainedWeaponPlayerPoseTest {
    private static final float COIL = .18F;
    private static final float GLAIVE_ENTRY = .22F;
    private static final float GLAIVE_EXIT = .60F;

    @Test
    void daggerDrivesUpAndForwardFromItsLowCoil() {
        var low = hand(Weapon.DAGGER, COIL, true);
        var strike = hand(Weapon.DAGGER, DAGGER_CONTACT, true);
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
        Vector3f lowTip = thirdPersonBlade(UnstainedWeaponPlayerPose.dagger(COIL, true));
        Vector3f highTip = thirdPersonBlade(UnstainedWeaponPlayerPose.dagger(DAGGER_CONTACT, true));
        assertTrue(lowTip.y < 0, "The dagger starts below the guard");
        assertTrue(highTip.y > .5F && highTip.z > .2F, "The blade must point up and forward, not back over the shoulder");
    }

    @Test
    void daggerTwistsInTheGapAfterTheThrustLands() {
        var contact = hand(Weapon.DAGGER, DAGGER_CONTACT, true);
        var twisted = hand(Weapon.DAGGER, .48F, true);
        Vector3f contactBlade = new Vector3f(0, 1, 0).mulDirection(contact).normalize();
        Vector3f twistedBlade = new Vector3f(0, 1, 0).mulDirection(twisted).normalize();
        assertTrue(contactBlade.angle(twistedBlade) < Math.toRadians(15), "The point must stay in the gap");
        Vector3f contactFlat = new Vector3f(1, 0, 0).mulDirection(contact).normalize();
        Vector3f twistedFlat = new Vector3f(1, 0, 0).mulDirection(twisted).normalize();
        assertTrue(contactFlat.angle(twistedFlat) > Math.toRadians(25), "The blade must turn about its own axis");
    }

    @Test
    void hammerLiftsHangsThenSlamsAtSpeed() {
        var rest = hand(Weapon.HAMMER, 0, true);
        var raised = hand(Weapon.HAMMER, HAMMER_APEX, true);
        var dropped = hand(Weapon.HAMMER, HAMMER_CONTACT, true);
        // Bell centre after the authored renderer and ItemRenderer's centering translation.
        Vector3f head = new Vector3f(-.08F, .603125F, -.1F);
        Vector3f restHead = new Vector3f(head).mulPosition(rest);
        Vector3f raisedHead = new Vector3f(head).mulPosition(raised);
        Vector3f droppedHead = new Vector3f(head).mulPosition(dropped);
        assertTrue(raisedHead.y > restHead.y + .12F, "The hammer head must lift visibly before the strike");
        assertTrue(raisedHead.y > droppedHead.y + .3F, "The raised head must then come down");
        assertTrue(rest.equals(hand(Weapon.HAMMER, 1, true), .00001F));

        float early = headY(HAMMER_APEX + .01F) - headY(HAMMER_APEX + .02F);
        float late = headY(HAMMER_CONTACT - .02F) - headY(HAMMER_CONTACT - .01F);
        assertTrue(late > early * 3, "The slam must accelerate into the blow rather than ease into it");
        assertTrue(headY(.48F) > headY(HAMMER_CONTACT), "The bell must rebound off the impact");

        var overhead = UnstainedWeaponPlayerPose.hammer(HAMMER_APEX, true);
        var impact = UnstainedWeaponPlayerPose.hammer(HAMMER_CONTACT, true);
        assertTrue(overhead.armXRot() < -2.5F, "Third person lifts the hammer overhead");
        assertTrue(impact.armXRot() > 0, "Third person drives it down in front");
        assertTrue(overhead.offXRot() < -2.4F && overhead.offYRot() > .3F,
                "The free hand joins the haft for the two-handed lift");
    }

    @Test
    void glaiveCutsAcrossAHorizontalFanInsteadOfChoppingDown() {
        Vector3f entry = new Vector3f(0, 1, 0).mulDirection(hand(Weapon.GLAIVE, GLAIVE_ENTRY, true));
        Vector3f exit = new Vector3f(0, 1, 0).mulDirection(hand(Weapon.GLAIVE, GLAIVE_EXIT, true));
        assertTrue(entry.x > .6F && exit.x < -.6F, "The blade must cross from the outer side to the opposite side");
        assertEquals(0, entry.y, .01F);
        assertEquals(0, exit.y, .01F);
        assertTrue(entry.angle(exit) > Math.toRadians(140), "The horizontal fan must be broad");
        var startArm = UnstainedWeaponPlayerPose.glaive(GLAIVE_ENTRY, true);
        var endArm = UnstainedWeaponPlayerPose.glaive(GLAIVE_EXIT, true);
        Vector3f thirdEntry = thirdPersonBlade(startArm);
        Vector3f thirdExit = thirdPersonBlade(endArm);
        assertTrue(thirdEntry.x < -.6F && thirdExit.x > .6F,
                "The player model must sweep from the same outer side as the first-person view");
        assertEquals(0, thirdEntry.y, .01F, "The held model must also stay horizontal");
        assertEquals(0, thirdExit.y, .01F);
        assertTrue(thirdEntry.angle(thirdExit) > Math.toRadians(140));
        assertTrue(Math.signum(startArm.bodyYRot()) != Math.signum(endArm.bodyYRot()),
                "The torso coils away and then turns through the cut");
    }

    @Test
    void glaiveCrossesTheFrontAtFullSpeed() {
        float middle = yawStep(UnstainedWeaponPlayerPose.GLAIVE_CONTACT);
        assertTrue(middle > yawStep(.27F) * 2 && middle > yawStep(.56F) * 2,
                "The cut must be fastest as it passes in front of the player");
    }

    @Test
    void swingsEaseBackIntoTheSameRestingGrip() {
        for (Weapon weapon : Weapon.values()) {
            var rest = hand(weapon, 0, true);
            assertTrue(rest.equals(hand(weapon, 1, true), .00001F));
            assertTrue(rest.equals(hand(weapon, .999F, true), .001F), weapon + " recovery must not snap at the final frame");
            assertEquals(0, arm(weapon, 1).weight(), weapon + " must hand the arm back to vanilla");
        }
    }

    @Test
    void framesNeverJump() {
        for (Weapon weapon : Weapon.values()) {
            Matrix4f previous = hand(weapon, 0, true);
            var previousArm = arm(weapon, 0);
            // Fine sampling: strikes are fast, but no channel may step discontinuously.
            for (int i = 1; i <= 4000; i++) {
                float progress = i / 4000.0F;
                Matrix4f current = hand(weapon, progress, true);
                Vector3f a = new Vector3f(0, 1, 0).mulPosition(previous);
                Vector3f b = new Vector3f(0, 1, 0).mulPosition(current);
                assertTrue(a.distance(b) < .05F, weapon + " first-person jump at " + progress);
                var currentArm = arm(weapon, progress);
                assertTrue(Math.abs(currentArm.armXRot() - previousArm.armXRot()) < .05F,
                        weapon + " third-person jump at " + progress);
                assertTrue(Math.abs(currentArm.weight() - previousArm.weight()) < .01F,
                        weapon + " blend jump at " + progress);
                previous = current;
                previousArm = currentArm;
            }
        }
    }

    @Test
    void leftHandMirrorsTheStrokeWithoutReversingItsHeightOrReach() {
        for (Weapon weapon : Weapon.values()) {
            for (float progress : new float[]{.15F, .35F, .45F, .55F, .8F}) {
                var right = hand(weapon, progress, true);
                var left = hand(weapon, progress, false);
                assertEquals(-right.m30(), left.m30(), .00001F);
                assertEquals(right.m31(), left.m31(), .00001F);
                assertEquals(right.m32(), left.m32(), .00001F);
                var rightArm = armFor(weapon, progress, true);
                var leftArm = armFor(weapon, progress, false);
                assertEquals(rightArm.armXRot(), leftArm.armXRot(), .00001F);
                assertEquals(-rightArm.armYRot(), leftArm.armYRot(), .00001F);
                assertEquals(-rightArm.armZRot(), leftArm.armZRot(), .00001F);
                assertEquals(-rightArm.bodyYRot(), leftArm.bodyYRot(), .00001F);
                assertEquals(rightArm.offXRot(), leftArm.offXRot(), .00001F);
                assertEquals(-rightArm.offYRot(), leftArm.offYRot(), .00001F);
            }
        }
    }

    private enum Weapon { DAGGER, GLAIVE, HAMMER }

    private static Matrix4f hand(Weapon weapon, float progress, boolean right) {
        PoseStack poses = new PoseStack();
        switch (weapon) {
            case DAGGER -> UnstainedWeaponPlayerPose.firstPersonDagger(poses, progress, 0, right);
            case GLAIVE -> UnstainedWeaponPlayerPose.firstPersonGlaive(poses, progress, 0, right);
            case HAMMER -> UnstainedWeaponPlayerPose.firstPersonHammer(poses, progress, 0, right);
        }
        return poses.last().pose();
    }

    private static UnstainedWeaponPlayerPose.ArmPose arm(Weapon weapon, float progress) {
        return armFor(weapon, progress, true);
    }

    private static UnstainedWeaponPlayerPose.ArmPose armFor(Weapon weapon, float progress, boolean right) {
        return switch (weapon) {
            case DAGGER -> UnstainedWeaponPlayerPose.dagger(progress, right);
            case GLAIVE -> UnstainedWeaponPlayerPose.glaive(progress, right);
            case HAMMER -> UnstainedWeaponPlayerPose.hammer(progress, right);
        };
    }

    private static float headY(float progress) {
        return new Vector3f(-.08F, .603125F, -.1F).mulPosition(hand(Weapon.HAMMER, progress, true)).y;
    }

    private static float yawStep(float progress) {
        Vector3f a = new Vector3f(0, 1, 0).mulDirection(hand(Weapon.GLAIVE, progress - .005F, true));
        Vector3f b = new Vector3f(0, 1, 0).mulDirection(hand(Weapon.GLAIVE, progress + .005F, true));
        return a.angle(b);
    }

    private static Vector3f thirdPersonBlade(UnstainedWeaponPlayerPose.ArmPose arm) {
        // Player renderer, arm, vanilla held-item grip, then the Unstained model's upright axis.
        Matrix4f transform = new Matrix4f().rotateY((float)Math.PI).scale(-1, -1, 1)
                .rotateZYX(arm.armZRot(), arm.armYRot(), arm.armXRot())
                .rotateX((float)-Math.PI / 2).rotateY((float)Math.PI).rotateX((float)Math.PI);
        return new Vector3f(0, -1, 0).mulDirection(transform);
    }
}
