package com.vincenthuto.hemomancy.common.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.entity.summon.GoreboundHulkModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class GoreboundHulkRigTest {
    @Test
    void handsBindingsAndBootsFollowTheirLimbJoints() {
        var root = GoreboundHulkModel.createBodyLayer().bakeRoot().getChild("root");
        assertTrue(root.hasChild("pelvis"), "The Hulk needs a connected body instead of separate fists");
        var pelvis = root.getChild("pelvis");
        var chest = pelvis.getChild("chest");
        for (String side : new String[]{"left", "right"}) {
            var forearm = chest.getChild(side + "_arm").getChild(side + "_forearm");
            assertTrue(forearm.hasChild(side + "_binding"));
            assertTrue(forearm.getChild(side + "_hand").hasChild(side + "_thumb"));
            assertTrue(pelvis.getChild(side + "_thigh").getChild(side + "_shin").hasChild(side + "_foot"));
        }
        assertTrue(chest.getChild("neck").getChild("head").hasChild("jaw"));
    }

    @Test
    void windupRaisesBothFistsWhileTheBootsStayPlanted() {
        var baked = GoreboundHulkModel.createBodyLayer().bakeRoot();
        var model = new GoreboundHulkModel(baked);
        var root = baked.getChild("root");
        var pelvis = root.getChild("pelvis");
        var chest = pelvis.getChild("chest");
        for (String side : new String[]{"left", "right"}) {
            var arm = chest.getChild(side + "_arm");
            var forearm = arm.getChild(side + "_forearm");
            var hand = forearm.getChild(side + "_hand");
            var thigh = pelvis.getChild(side + "_thigh");
            var shin = thigh.getChild(side + "_shin");
            var foot = shin.getChild(side + "_foot");
            model.setupPose(0, 0, 20, 0, 0, 0, 0);
            Vector3f fistBefore = point(new Vector3f(0, 1.5F, 0), root, pelvis, chest, arm, forearm, hand);
            Vector3f soleBefore = point(new Vector3f(0, 2, 0), root, pelvis, thigh, shin, foot);
            model.setupPose(0, 0, 20, 0, 0, 9, 0);
            Vector3f fistAfter = point(new Vector3f(0, 1.5F, 0), root, pelvis, chest, arm, forearm, hand);
            Vector3f soleAfter = point(new Vector3f(0, 2, 0), root, pelvis, thigh, shin, foot);
            assertTrue(fistAfter.y < fistBefore.y - 6, side + " fist must rise with the elbow");
            assertEquals(24, soleAfter.y, .0001F);
            assertEquals(0, soleBefore.distance(soleAfter), .0001F);
        }
    }

    @Test
    void walkingBendsTheKneesAndKeepsBootSolesLevel() {
        var baked = GoreboundHulkModel.createBodyLayer().bakeRoot();
        var model = new GoreboundHulkModel(baked);
        var pelvis = baked.getChild("root").getChild("pelvis");
        for (float swing : new float[]{0, 2, 4, 6}) {
            model.setupPose(swing, .8F, 20, 0, 0, 0, 0);
            for (String side : new String[]{"left", "right"}) {
                var thigh = pelvis.getChild(side + "_thigh");
                var shin = thigh.getChild(side + "_shin");
                var foot = shin.getChild(side + "_foot");
                assertTrue(shin.xRot >= 0);
                assertEquals(0, thigh.xRot + shin.xRot + foot.xRot, .0001F);
            }
        }
        assertTrue(pelvis.getChild("left_thigh").getChild("left_shin").xRot > .1F);
    }

    @Test
    void strikeRecoveryAndPoseResetLeaveNoRaisedOrDriftingParts() {
        var baked = GoreboundHulkModel.createBodyLayer().bakeRoot();
        var model = new GoreboundHulkModel(baked);
        model.setupPose(2, .5F, 20, 30, -10, 0, 0);
        float[] idle = pose(baked);
        model.setupPose(0, 0, 100, -40, 20, 9, 0);
        model.setupPose(2, .5F, 20, 30, -10, 0, 1);
        assertArrayEquals(idle, pose(baked), .0001F);
        model.setupPose(3, 1, 40, 0, 0, 0, .25F);
        model.setupPose(2, .5F, 20, 30, -10, 0, 0);
        assertArrayEquals(idle, pose(baked), .0001F);
    }

    private static Vector3f point(Vector3f point, ModelPart... chain) {
        PoseStack pose = new PoseStack();
        for (ModelPart part : chain) part.translateAndRotate(pose);
        return pose.last().pose().transformPosition(point.div(16)).mul(16);
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
