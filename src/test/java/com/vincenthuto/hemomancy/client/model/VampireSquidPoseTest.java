package com.vincenthuto.hemomancy.client.model;

import com.vincenthuto.hemomancy.client.model.entity.mob.aquatic.VampireSquidPose;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class VampireSquidPoseTest {
    @Test void cloakedArmsReachOverTheMantleRatherThanSpreadingSideways() {
        for (int arm = 0; arm < 8; arm++) for (float age : new float[]{0, 25, 70, 1000}) {
            double angle = 0, y = 0, radius = 2.75;
            for (int joint = 0; joint < 4; joint++) {
                angle += VampireSquidPose.armAngle(age, arm, joint, 1);
                y += Math.cos(angle) * 2.75;
                radius += Math.sin(angle) * 2.75;
            }
            assertTrue(y < -8, "Arm tips must rise above the mantle's eight-pixel crown: " + y);
            assertTrue(Math.abs(radius) < 5, "Cloak must cup the mantle instead of opening into a wide umbrella: " + radius);
        }
    }
    @Test void swimmingArmsHangBelowTheMantleAndKeepTravelingCurl() {
        for (int arm = 0; arm < 8; arm++) {
            double angle = 0, y = 0;
            for (int joint = 0; joint < 4; joint++) {
                angle += VampireSquidPose.armAngle(25, arm, joint, 0);
                y += Math.cos(angle) * 2.75;
            }
            assertTrue(y > 6, "Resting arms should trail below the mantle");
        }
        assertNotEquals(VampireSquidPose.wave(25, 0, 0), VampireSquidPose.wave(25, 0, 3));
    }
}
