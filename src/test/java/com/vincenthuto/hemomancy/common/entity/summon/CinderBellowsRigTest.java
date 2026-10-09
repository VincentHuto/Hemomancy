package com.vincenthuto.hemomancy.common.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.entity.summon.CinderBellowsModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CinderBellowsRigTest {
    @Test
    void serpentAndHumanoidUseConnectedJointChains() {
        var root = CinderBellowsModel.createBodyLayer().bakeRoot().getChild("root");
        assertTrue(root.hasChild("pelvis"), "Humanoid needs a pelvis joint above the coil");
        var chest = root.getChild("pelvis").getChild("abdomen").getChild("chest");
        assertTrue(chest.getChild("neck").getChild("head").hasChild("jaw"));
        for (String side : new String[]{"left", "right"}) {
            assertTrue(chest.getChild(side + "_arm").getChild(side + "_forearm")
                    .getChild(side + "_hand").hasChild(side + "_fingers"));
        }
        var segment = root.getChild("coil");
        for (int i = 0; i < 10; i++) segment = segment.getChild("tail_" + i);
        assertFalse(segment.cubes.isEmpty(), "The terminal tail joint must render a tapered tip");
    }

    @Test
    void breathingInflatesLobesAndRecoversWithoutStretchingTheRibs() {
        var baked = CinderBellowsModel.createBodyLayer().bakeRoot();
        var model = new CinderBellowsModel(baked);
        var chest = baked.getChild("root").getChild("pelvis").getChild("abdomen").getChild("chest");
        var jaw = chest.getChild("neck").getChild("head").getChild("jaw");
        float closed = jaw.xRot;
        model.setupPose(0, 0, 10, 0, 0, 40);
        for (String side : new String[]{"left", "right"}) {
            var lung = chest.getChild(side + "_lung");
            assertTrue(lung.xScale > 1 && lung.zScale > 1, "Each lobe must visibly inflate");
            assertEquals(1, chest.getChild(side + "_rib_1").xScale, .00001);
        }
        assertTrue(jaw.xRot > closed + .6F, "The breath pose must open the jaw");
        model.setupPose(0, 0, 10, 0, 0, 100);
        assertEquals(closed, jaw.xRot, .00001);
        assertEquals(1, chest.getChild("left_lung").zScale, .00001);
        assertEquals(1, chest.getChild("right_lung").zScale, .00001);
    }

    @Test
    void inhaleRearsBackThenTheBreathLungesTheNeckAndStokesTheEmbers() {
        var baked = CinderBellowsModel.createBodyLayer().bakeRoot();
        var model = new CinderBellowsModel(baked);
        var neck = baked.getChild("root").getChild("pelvis").getChild("abdomen").getChild("chest").getChild("neck");
        model.setupPose(0, 0, 10, 0, 0, 0);
        float rest = neck.xRot;
        model.setupPose(0, 0, 10, 0, 0, 18);
        assertTrue(neck.xRot < rest - .1F, "The draw must rear the neck back");
        model.setupPose(0, 0, 10, 0, 0, 40);
        assertTrue(neck.xRot > rest + .2F, "The breath must lunge the neck forward");
        assertEquals(0, CinderBellowsModel.emberHeat(0), .00001);
        assertEquals(1, CinderBellowsModel.emberHeat(40), .00001);
        assertTrue(CinderBellowsModel.emberHeat(18) < CinderBellowsModel.emberHeat(40));
    }

    @Test
    void everyFrameResetsJointRotationsTranslationsAndLungScale() {
        var baked = CinderBellowsModel.createBodyLayer().bakeRoot();
        var model = new CinderBellowsModel(baked);
        model.setupPose(4, .8F, 30, 20, -10, 40);
        float[] first = pose(baked);
        model.setupPose(28, 1, 100, -35, 25, 70);
        model.setupPose(4, .8F, 30, 20, -10, 40);
        assertArrayEquals(first, pose(baked), .00001F);
        model.setupPose(0, 0, 0, 0, 0, 0);
        assertEquals(24, baked.getChild("root").y, .00001, "The coil stays grounded");
    }

    @Test
    void movementStrengthensAPropagatingWaveRatherThanRotatingOneTailBox() {
        var baked = CinderBellowsModel.createBodyLayer().bakeRoot();
        var model = new CinderBellowsModel(baked);
        var first = baked.getChild("root").getChild("coil").getChild("tail_0");
        var second = first.getChild("tail_1");
        float restFirst = first.yRot, restSecond = second.yRot;
        model.setupPose(0, 0, 20, 0, 0, 0);
        float idle = Math.abs(first.yRot - restFirst);
        model.setupPose(0, 1, 20, 0, 0, 0);
        assertTrue(Math.abs(first.yRot - restFirst) > idle * 3);
        assertNotEquals(first.yRot - restFirst, second.yRot - restSecond, .00001);
    }

    @Test
    void articulatedTailStaysOnTheGroundThroughoutTravelAndBreathing() {
        var baked = CinderBellowsModel.createBodyLayer().bakeRoot();
        var model = new CinderBellowsModel(baked);
        var root = baked.getChild("root");
        for (int frame = 0; frame <= 100; frame += 5) {
            model.setupPose(frame * .3F, 1, frame, 0, 0, frame);
            PoseStack pose = new PoseStack();
            root.translateAndRotate(pose);
            var parent = root.getChild("coil");
            parent.translateAndRotate(pose);
            for (int i = 0; i < 10; i++) {
                var segment = parent.getChild("tail_" + i);
                segment.translateAndRotate(pose);
                float bottom = Float.NEGATIVE_INFINITY;
                for (var cube : segment.cubes) {
                    for (float x : new float[]{cube.minX, cube.maxX}) {
                        for (float z : new float[]{cube.minZ, cube.maxZ}) {
                            var point = new Vector3f(x, cube.maxY, z).div(16);
                            bottom = Math.max(bottom, pose.last().pose().transformPosition(point).y * 16);
                        }
                    }
                }
                assertEquals(24, bottom, .0001F, "Tail segment " + i + " floats or sinks at frame " + frame);
                parent = segment;
            }
        }
    }

    private static float[] pose(ModelPart root) {
        var values = new ArrayList<Float>();
        root.getAllParts().forEach(part -> {
            for (float value : new float[]{part.x, part.y, part.z, part.xRot, part.yRot, part.zRot,
                    part.xScale, part.yScale, part.zScale}) values.add(value);
        });
        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) result[i] = values.get(i);
        return result;
    }
}
