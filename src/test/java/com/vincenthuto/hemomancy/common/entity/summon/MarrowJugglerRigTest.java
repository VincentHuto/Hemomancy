package com.vincenthuto.hemomancy.common.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.model.entity.summon.MarrowSpitterModel;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class MarrowJugglerRigTest {
    @Test
    void allSixArmsAndMasksBelongToTheHunchedBody() {
        var root = MarrowSpitterModel.createBodyLayer().bakeRoot().getChild("root");
        assertTrue(root.hasChild("body"), "The robe needs a shared articulated torso");
        var body = root.getChild("body");
        for (int i = 0; i < 6; i++) {
            assertTrue(body.getChild("arm_" + i).getChild("forearm_" + i)
                    .getChild("hand_" + i).hasChild("fingers_" + i));
        }
        for (String expression : new String[]{"laugh", "mourn", "blank"}) {
            assertFalse(body.getChild("hood").getChild("mask_" + expression).cubes.isEmpty());
        }
        for (int i = 0; i < 3; i++) assertTrue(body.getChild("knife_" + i).cubes.size() >= 3);
        assertTrue(body.getChild("skirt").hasChild("front_left"));
    }

    @Test
    void everyKnifePassesThroughBothBentHandsAtItsCatchFrames() {
        var baked = MarrowSpitterModel.createBodyLayer().bakeRoot();
        var model = new MarrowSpitterModel(baked);
        var root = baked.getChild("root");
        var body = root.getChild("body");
        for (int pair = 0; pair < 3; pair++) {
            for (int side = 0; side < 2; side++) {
                float age = (float) ((4 * Math.PI + side * Math.PI - pair * 2 * Math.PI / 3) / .13F);
                model.setupPose(.8F, age, 30, -10);
                int index = pair * 2 + side;
                var arm = body.getChild("arm_" + index);
                var forearm = arm.getChild("forearm_" + index);
                var hand = forearm.getChild("hand_" + index);
                Vector3f palm = point(new Vector3f(0, .75F, 0), root, body, arm, forearm, hand);
                Vector3f knife = point(new Vector3f(), root, body, body.getChild("knife_" + pair));
                assertTrue(palm.distance(knife) < .02F, "Knife " + pair + " missed hand " + index);
            }
        }
    }

    @Test
    void posingResetsClothHandsMasksAndKnifeTransforms() {
        var baked = MarrowSpitterModel.createBodyLayer().bakeRoot();
        var model = new MarrowSpitterModel(baked);
        model.setupPose(.5F, 10, 20, -15);
        float[] first = pose(baked);
        model.setupPose(1, 100, -40, 25);
        model.setupPose(.5F, 10, 20, -15);
        assertArrayEquals(first, pose(baked), .00001F);
    }

    @Test
    void allMasksFollowTheHoodRatherThanFloatingSeparately() {
        var baked = MarrowSpitterModel.createBodyLayer().bakeRoot();
        var model = new MarrowSpitterModel(baked);
        var root = baked.getChild("root");
        var body = root.getChild("body");
        var hood = body.getChild("hood");
        model.setupPose(0, 0, 0, 0);
        Vector3f before = point(new Vector3f(), root, body, hood, hood.getChild("mask_laugh"));
        model.setupPose(0, 0, 40, 0);
        Vector3f after = point(new Vector3f(), root, body, hood, hood.getChild("mask_laugh"));
        assertTrue(before.distance(after) > .5F);
        assertEquals(0, body.yRot, .00001F);
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
