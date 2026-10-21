package com.vincenthuto.hemomancy.client.render.item.harbinger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

/** Only the player's own world render may record their held pose; figurines and dolls of them must not overwrite it. */
class HeldWeaponPoseWorldRenderTest {
    private static final Vec3 CAMERA = new Vec3(100, 65.62, -40);
    private static final Vec3 FEET = new Vec3(100, 64, -40);

    @Test void aHandAtThePlayerIsTheirWorldRender() {
        // Camera-relative: the hand sits a little ahead of and below the eyes.
        Matrix4f pose = new Matrix4f().translation(0.35f, -0.6f, 0.4f);
        assertTrue(HeldWeaponPose.worldRender(pose, CAMERA, FEET));
    }

    @Test void aPodiumFigurineOfThePlayerIsNot() {
        // The Scrying Podium draws the local player a tenth size on a podium several blocks away.
        Matrix4f pose = new Matrix4f().translation(6.5f, -0.4f, 1.5f).scale(0.1f);
        assertFalse(HeldWeaponPose.worldRender(pose, CAMERA, FEET));
    }

    @Test void anInventoryDollIsNot() {
        // Screens pose the doll in GUI pixels.
        Matrix4f pose = new Matrix4f().translation(240, 180, 1050).scale(30);
        assertFalse(HeldWeaponPose.worldRender(pose, CAMERA, FEET));
    }
}
