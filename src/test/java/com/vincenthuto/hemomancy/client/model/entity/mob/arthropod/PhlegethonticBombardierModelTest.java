package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.BombardierFlameGeometry;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhlegethonticBombardierModelTest {
    @Test
    void latestBlockbenchGeometryAndAllSixArticulatedLegsAreBaked() {
        var model = model();
        ModelPart root = model.root().getChild("bombardier");
        assertTrue(root.getChild("thorax").hasChild("head"));
        assertEquals(53, root.getAllParts().mapToInt(part -> part.cubes.size()).sum());
        assertEquals(23, root.getAllParts().count());
        assertEquals(20, root.y);
        assertEquals(6, root.getChild("abdomen").getChild("spray_valve").cubes.size());
        for (String side : List.of("l", "r")) {
            assertTrue(root.getChild("thorax").getChild("head").hasChild("mandible_" + side));
            assertTrue(root.getChild("abdomen").hasChild("elytron_" + side));
            for (String row : List.of("fore", "middle", "hind")) {
                String suffix = row + "_" + side;
                assertTrue(root.getChild("leg_" + suffix).hasChild("shin_" + suffix));
            }
        }
    }

    @Test
    void retargetedClipsMoveTheIntendedJointsAndReturnToTheirLoopPose() {
        var model = model();
        for (var clip : List.of(PhlegethonticBombardierAnimations.IDLE, PhlegethonticBombardierAnimations.WALK,
                PhlegethonticBombardierAnimations.FLATTEN_REST, PhlegethonticBombardierAnimations.WAKE_WARNING,
                PhlegethonticBombardierAnimations.GRAZE, PhlegethonticBombardierAnimations.ABDOMEN_WINDUP,
                PhlegethonticBombardierAnimations.ABDOMEN_FIRE, PhlegethonticBombardierAnimations.VENT_COOLDOWN,
                PhlegethonticBombardierAnimations.HURT, PhlegethonticBombardierAnimations.DEATH)) {
            for (String bone : clip.boneAnimations().keySet()) {
                assertTrue(model.getAnyDescendantWithName(bone).isPresent(), bone);
            }
            if (clip.looping()) assertLoopSeam(model, clip);
        }

        animate(model, PhlegethonticBombardierAnimations.WALK, 250L);
        var root = model.root().getChild("bombardier");
        assertTrue(root.getChild("leg_fore_r").yRot * root.getChild("leg_middle_r").yRot < 0);
        assertTrue(root.getChild("leg_fore_r").zRot * root.getChild("leg_fore_l").zRot > 0,
                "opposite-side legs lift on alternating steps with mirrored roll");
        animate(model, PhlegethonticBombardierAnimations.GRAZE, 250L);
        ModelPart head = root.getChild("thorax").getChild("head");
        assertTrue(head.xRot > 0);
        assertTrue(head.getChild("mandible_r").yRot * head.getChild("mandible_l").yRot < 0);
        animate(model, PhlegethonticBombardierAnimations.ABDOMEN_FIRE, 400L);
        ModelPart abdomen = root.getChild("abdomen");
        assertTrue(abdomen.xRot > .7F);
        assertTrue(abdomen.getChild("elytron_r").yRot > .1F && abdomen.getChild("elytron_l").yRot < -.1F);
        var pose = new PoseStack();
        root.translateAndRotate(pose);
        abdomen.translateAndRotate(pose);
        abdomen.getChild("spray_valve").translateAndRotate(pose);
        assertTrue(pose.last().pose().transformPosition(new Vector3f()).y < 1);
    }

    @Test
    void aimedBlockbenchValveMouthMatchesTheServerSprayOrigin() {
        for (float bodyYaw : new float[]{0, 90, 175, -45}) {
            for (Vec3 direction : List.of(new Vec3(.3, .35, 1).normalize(), new Vec3(-.9, -.3, .4).normalize(),
                    new Vec3(0, 0, -1), new Vec3(0, 1, 0))) {
                var model = model();
                model.aimValve(bodyYaw, direction);
                var pose = new PoseStack();
                pose.mulPose(Axis.YP.rotationDegrees(180 - bodyYaw));
                pose.scale(-1, -1, 1);
                pose.translate(0, -1.501F, 0);
                var bombardier = model.root().getChild("bombardier");
                var abdomen = bombardier.getChild("abdomen");
                bombardier.translateAndRotate(pose);
                abdomen.translateAndRotate(pose);
                abdomen.getChild("spray_valve").translateAndRotate(pose);

                var mouth = pose.last().pose().transformPosition(new Vector3f(0, 0, 3.25F / 16));
                Vec3 serverMouth = BombardierFlameGeometry.valveMouth(
                        BombardierFlameGeometry.raisedValvePivot(Vec3.ZERO, bodyYaw), direction);
                assertEquals(0, serverMouth.distanceTo(new Vec3(mouth.x, mouth.y, mouth.z)), .0002,
                        "body yaw " + bodyYaw + ", aim " + direction);
            }
        }
    }

    @Test
    void walkingWeightFadesContinuouslyThroughTheOldCutoff() {
        float below = PhlegethonticBombardierModel.walkBlend(.019F);
        float above = PhlegethonticBombardierModel.walkBlend(.021F);
        assertTrue(below > 0 && above > below && above - below < .01F);
        assertEquals(0.0F, PhlegethonticBombardierModel.walkBlend(0), .0001F);
        assertEquals(1.0F, PhlegethonticBombardierModel.walkBlend(1), .0001F);
    }

    private static PhlegethonticBombardierModel model() {
        return new PhlegethonticBombardierModel();
    }

    private static void assertLoopSeam(PhlegethonticBombardierModel model, AnimationDefinition clip) {
        long duration = Math.round(clip.lengthInSeconds() * 1000);
        for (String bone : clip.boneAnimations().keySet()) {
            ModelPart part = model.getAnyDescendantWithName(bone).orElseThrow();
            animate(model, clip, 0);
            var start = new Vector3f(part.xRot, part.yRot, part.zRot);
            animate(model, clip, duration - 1);
            assertTrue(start.distance(part.xRot, part.yRot, part.zRot) < .01F, bone);
        }
    }

    private static void animate(PhlegethonticBombardierModel model, AnimationDefinition clip, long millis) {
        model.root().getAllParts().forEach(ModelPart::resetPose);
        KeyframeAnimations.animate(model, clip, millis, 1, new Vector3f());
    }
}
