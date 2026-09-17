package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HematicMicroscopePoseTest {
    @Test void broadHeadSeatsInSocketAndNarrowEndStaysOutside() {
        for (boolean right : new boolean[]{true, false}) {
            var instrument = new PoseStack();
            HematicMicroscopePose.firstPersonInstrument(instrument, 12, right);
            var vial = new PoseStack();
            HematicMicroscopePose.vial(vial, instrument, 12, right);
            var head = vial.last().pose().transformPosition(new Vector3f(.25F, .25F, 0));
            var socket = instrument.last().pose().transformPosition(HematicMicroscopePose.seatedHead(right));
            assertTrue(head.distance(socket) < .0001F);
            var needle = vial.last().pose().transformPosition(new Vector3f(-.4375F, -.4375F, 0));
            assertTrue(right ? needle.x < head.x : needle.x > head.x);
            var before = new PoseStack();
            HematicMicroscopePose.vial(before, instrument, 6, right);
            var unseated = before.last().pose().transformPosition(new Vector3f(.25F, .25F, 0));
            assertTrue(unseated.distance(socket) > .15F);
        }
    }

    @Test void eyepieceFollowsThePlayersEyeAndBothHandsGripTheirItems() {
        for (boolean slim : new boolean[]{false, true}) for (boolean right : new boolean[]{true, false}) {
            var model = new PlayerModel<>(LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64).bakeRoot(), slim);
            model.head.xRot = .3F;
            model.head.yRot = -.25F;
            model.head.y = 4.2F;
            model.rightArm.y = model.leftArm.y = 5.2F;
            HematicMicroscopePose.apply(model, 16, right);
            var instrument = new PoseStack();
            HematicMicroscopePose.thirdPersonInstrument(instrument, model, 16, right);
            var eye = new PoseStack();
            model.head.translateAndRotate(eye);
            var expectedEye = eye.last().pose().transformPosition(new Vector3f(right ? -.125F : .125F, -.25F, -.25F));
            assertTrue(instrument.last().pose().transformPosition(new Vector3f(0, .125F, .325F)).distance(expectedEye) < .0001F);
            var vial = new PoseStack();
            HematicMicroscopePose.vial(vial, instrument, 16, right);
            var mainGrip = instrument.last().pose().transformPosition(new Vector3f(0, -.21875F, -.0625F));
            var vialGrip = vial.last().pose().transformPosition(new Vector3f(-.1875F, -.1875F, 0));
            var arm = new PoseStack();
            (right ? model.rightArm : model.leftArm).translateAndRotate(arm);
            assertTrue(arm.last().pose().transformPosition(new Vector3f(0, .625F, 0)).distance(mainGrip) < .001F);
            arm = new PoseStack();
            (right ? model.leftArm : model.rightArm).translateAndRotate(arm);
            assertTrue(arm.last().pose().transformPosition(new Vector3f(0, .625F, 0)).distance(vialGrip) < .001F);
        }
    }

    @Test void overlayWaitsForInsertionAndPoseMirrors() {
        assertEquals(0, HematicMicroscopePose.overlayAlpha(12));
        assertEquals(1, HematicMicroscopePose.overlayAlpha(16));
        for (int tick : new int[]{0, 6, 12, 16, 40}) {
            var a = new PoseStack(); var b = new PoseStack();
            HematicMicroscopePose.firstPersonInstrument(a, tick, true);
            HematicMicroscopePose.firstPersonInstrument(b, tick, false);
            assertEquals(-a.last().pose().m30(), b.last().pose().m30(), .0001F);
            assertEquals(a.last().pose().m31(), b.last().pose().m31(), .0001F);
        }
    }
}
