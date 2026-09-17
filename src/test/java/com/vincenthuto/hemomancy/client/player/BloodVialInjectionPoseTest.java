package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BloodVialInjectionPoseTest {
    @Test void resolvesPhysicalArmForBothDominantHands() {
        assertTrue(BloodVialInjectionPose.rightArm(InteractionHand.MAIN_HAND, HumanoidArm.RIGHT));
        assertFalse(BloodVialInjectionPose.rightArm(InteractionHand.OFF_HAND, HumanoidArm.RIGHT));
        assertFalse(BloodVialInjectionPose.rightArm(InteractionHand.MAIN_HAND, HumanoidArm.LEFT));
        assertTrue(BloodVialInjectionPose.rightArm(InteractionHand.OFF_HAND, HumanoidArm.LEFT));
    }

    @Test void needleMeetsTheMatchingThighForBothSkinWidthsAndHands() {
        for (boolean slim : new boolean[]{false, true}) {
            for (boolean right : new boolean[]{false, true}) {
                var model = new PlayerModel<>(LayerDefinition.create(
                        PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64).bakeRoot(), slim);
                BloodVialInjectionPose.apply(model, 16, right);
                var transform = new PoseStack();
                BloodVialInjectionPose.vialTransform(transform, model, 16, right);
                var tip = transform.last().pose().transformPosition(new Vector3f(-.4375F, -.4375F, 0));
                var contact = BloodVialInjectionPose.thighContact(model, right);
                assertTrue(tip.distance(contact) < .015F, "Needle must touch the posed thigh");
                assertEquals(right, tip.x < 0, "Injection crossed to the opposite leg");
                var grip = transform.last().pose().transformPosition(new Vector3f(.125F, .125F, 0));
                assertTrue(tip.y > grip.y, "Point must lead downward into the thigh");
                assertTrue(tip.z > grip.z, "Point must lead inward, not away from the leg");
            }
        }
    }

    @Test void motionRaisesThenSlamsAndBlendsOut() {
        var raised = BloodVialInjectionPose.motion(12);
        var impact = BloodVialInjectionPose.motion(16);
        assertTrue(raised.armPitch() < impact.armPitch() - .7F);
        assertEquals(0, raised.strike(), .0001F);
        assertEquals(1, impact.strike(), .0001F);
        assertEquals(0, BloodVialInjectionPose.motion(0).weight(), .0001F);
        assertEquals(0, BloodVialInjectionPose.motion(24).weight(), .0001F);
        assertTrue(BloodVialInjectionPose.motion(23.99F).weight() < .001F);
    }

    @Test void firstPersonGripMirrorsWithoutChangingHeightOrReach() {
        var left = BloodVialInjectionPose.firstPersonGrip(16, false);
        var right = BloodVialInjectionPose.firstPersonGrip(16, true);
        assertEquals(-left.x, right.x, .0001F);
        assertEquals(left.y, right.y, .0001F);
        assertEquals(left.z, right.z, .0001F);
        assertTrue(right.y < BloodVialInjectionPose.firstPersonGrip(12, true).y - .3F);
    }

    @Test void crouchingKeepsNeedleAndClothingOnTheReceivingThigh() {
        for (boolean right : new boolean[]{true, false}) {
            var model = new PlayerModel<>(LayerDefinition.create(
                    PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64).bakeRoot(), false);
            model.rightArm.y = model.leftArm.y = 5.2F;
            model.rightLeg.y = model.leftLeg.y = 12.2F;
            model.rightLeg.z = model.leftLeg.z = 4;
            BloodVialInjectionPose.apply(model, 16, right);
            var poses = new PoseStack();
            BloodVialInjectionPose.vialTransform(poses, model, 16, right);
            var tip = poses.last().pose().transformPosition(new Vector3f(-.4375F, -.4375F, 0));
            assertTrue(tip.distance(BloodVialInjectionPose.thighContact(model, right)) < .015F);
            assertEquals(model.rightLeg.xRot, model.rightPants.xRot);
            assertEquals(model.leftLeg.zRot, model.leftPants.zRot);
            assertEquals(model.body.yRot, model.jacket.yRot);
        }
    }

    @Test void heldVialReturnsToTheOrdinaryTransformWithoutASnap() {
        var ordinary = new PoseStack();
        ordinary.translate(.56F, -.52F, -.72F);
        ordinary.mulPose(new org.joml.Quaternionf().rotationXYZ(.2F, -.8F, .4F));
        ordinary.scale(.7F, .7F, .7F);
        var injection = new PoseStack();
        BloodVialInjectionPose.orientVial(injection, new Vector3f(.4F, -.7F, -.6F),
                new Vector3f(-.12F, -.92F, .35F).normalize());
        var end = new PoseStack();
        BloodVialInjectionPose.blendTransform(end, ordinary, injection, BloodVialInjectionPose.motion(24).weight());
        assertTrue(end.last().pose().equals(ordinary.last().pose(), .00001F));
        var impact = new PoseStack();
        BloodVialInjectionPose.blendTransform(impact, ordinary, injection, BloodVialInjectionPose.motion(16).weight());
        assertTrue(impact.last().pose().equals(injection.last().pose(), .00001F));
    }
}
