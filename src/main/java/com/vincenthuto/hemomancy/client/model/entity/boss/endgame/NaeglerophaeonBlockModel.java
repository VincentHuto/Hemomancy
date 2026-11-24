package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.NaeglerophaeonModel.Input;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.NaeglerophaeonRig.Face;
import com.vincenthuto.hemomancy.client.model.entity.boss.endgame.NaeglerophaeonRig.Part;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonCombatRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.NaeglerophaeonEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Poses the authored rig once for all render passes and for the attached lightning. */
public final class NaeglerophaeonBlockModel {
    private static final String MODEL = "/assets/hemomancy/entity_geometry/naeglerophaeon_cube_geometry.json";
    private static final Part ROOT = NaeglerophaeonRig.load(MODEL);
    private static final Part CORE = ROOT.child("ganglion_core");
    private static final Part MANTLE = CORE.child("folded_maroon_mantle");
    private static final Part NEURAL_BED = CORE.child("exposed_ganglia");
    private static final Part AXON = ROOT.child("crimson_capture_axon");
    private static final List<Part> AXON_JOINTS = AXON.children().getFirst().chain("axon_joint_");
    private static final Part GRIP = AXON_JOINTS.getLast().child("prehensile_tip");
    private static final Vector3f AXON_END = new Vector3f(AXON_JOINTS.getLast().bundleCenter())
            .mul(2).sub(AXON_JOINTS.getLast().pivot());
    private static final List<Part> ARMS = ROOT.child("short_ivory_tendrils").children();
    private static final List<List<Part>> ARM_JOINTS = ARMS.stream()
            .map(arm -> arm.children().getFirst().chain(arm.name() + "_joint_")).toList();
    private static final int WHITE = 0xFFFFFFFF;
    private static final int ENRAGED_TINT = 0xFFE6A8FF;
    private static final float LUNGE_WINDUP = NaeglerophaeonCombatRules.LUNGE_WINDUP;
    private static final float LASH_STRIKE = NaeglerophaeonCombatRules.LASH_WINDUP;
    private static final float NOVA_WINDUP = NaeglerophaeonCombatRules.NOVA_WINDUP;
    private static final float[] FLARE = {40, 26, 16, 8}, TUCK = {-14, -9, -6, -3},
            RADIAL = {70, 30, 10, 0}, UMBRELLA = {58, 42, 28, 14},
            REACH = {25, 18, 10, 5}, LIMP = {-3, -2, -1, 0}, EASED = {20, 12, 6, 2};
    private static final List<Web> WEBS = loadWebs();

    private NaeglerophaeonBlockModel() {}

    private record Draw(List<Face> faces, Matrix4f transform, int glow, boolean ganglia,
                        boolean activeNerve, Stretch stretch) {}
    private record Stretch(Matrix4f matrix, Matrix4f rigid, float lengthFactor) {
        Matrix4f anchored(Vector3f point) {
            var offset = matrix.transformPosition(point, new Vector3f())
                    .sub(rigid.transformPosition(point, new Vector3f()));
            return new Matrix4f(rigid).translateLocal(offset);
        }
    }
    private record ArmSkin(int left, int right, int leftJoint, int rightJoint, float mix) {}
    private record Web(Face face, ArmSkin[] weights) {}

    public static final class Frame {
        private final List<Draw> draws = new ArrayList<>();
        private final Vec3[] tips = new Vec3[ARMS.size()];
        private final Vec3[] tail = new Vec3[AXON_JOINTS.size() + 1];

        public Vec3[] tips() { return tips; }
        public Vec3[] tail() { return tail; }
    }

    public static Frame pose(Input input, Vec3 look, float lookWeight) {
        var frame = new Frame();
        var body = body(input);
        var core = new Matrix4f(body).rotateZ((float) Math.sin(input.time() * .06) * .035F);
        if (look != null && look.lengthSqr() > 1e-8 && lookWeight > 0) {
            var local = new Matrix4f(body).invert().transformDirection(vector(look));
            float yaw = (float) Math.atan2(-local.x, -local.z);
            float pitch = (float) Math.atan2(local.y, Math.hypot(local.x, local.z));
            core.rotate(new Quaternionf().slerp(new Quaternionf().rotationY(yaw).rotateX(pitch), lookWeight));
        }
        drawTree(frame, MANTLE, core, WHITE, false);
        draw(frame, NEURAL_BED, core, WHITE, false);
        for (Part nodule : NEURAL_BED.children()) {
            var matrix = new Matrix4f(core).translate(nodule.pivot())
                    .scale(1 + coreCharge(input) * .30F).translate(new Vector3f(nodule.pivot()).negate());
            drawTree(frame, nodule, matrix, input.enraged() ? ENRAGED_TINT : WHITE, true);
        }
        poseArms(frame, input, body);
        poseAxon(frame, input, body);
        return frame;
    }

    private static Matrix4f body(Input input) {
        double swim = input.phase() == NaeglerophaeonEntity.IDLE || input.phase() == NaeglerophaeonEntity.VOLLEY
                ? input.hunting() : 0;
        var body = new Matrix4f().translation(0, .8F, 0)
                .rotateY((float) -input.yaw()).rotateX((float) input.pitch()).rotateY(Mth.PI)
                .translate((float) (Math.sin(input.time() * .26) * .12 * swim),
                        (float) (Math.sin(input.time() * .52) * .035 * swim), 0)
                .rotateY((float) (Math.sin(input.time() * .26 + .6) * .10 * swim));
        double shake = switch (input.phase()) {
            case NaeglerophaeonEntity.TRANSITION -> .055;
            case NaeglerophaeonEntity.OVERLOAD -> .035;
            case NaeglerophaeonEntity.NOVA -> input.phaseTicks() < NOVA_WINDUP ? .04 * input.phaseTicks() / NOVA_WINDUP : 0;
            default -> 0;
        };
        body.translate((float) (Math.sin(input.time() * 2.9) * shake),
                (float) (Math.sin(input.time() * 3.7 + 1) * shake), (float) (Math.sin(input.time() * 3.1 + 2) * shake));
        if (input.phase() == NaeglerophaeonEntity.LASH && input.phaseTicks() >= LASH_STRIKE) {
            body.rotateZ(ease((input.phaseTicks() - LASH_STRIKE) / 6) * Mth.TWO_PI);
        }
        return body.scale(bodyScale(input));
    }

    public static float bodyScale(Input input) {
        return switch (input.phase()) {
            case NaeglerophaeonEntity.NERVE_DIVE -> 1 - .85F * ease(input.phaseTicks() / 12);
            case NaeglerophaeonEntity.NERVE_EMERGE -> .15F + .85F * ease(input.phaseTicks() / 6);
            default -> 1;
        };
    }

    static float coreCharge(Input input) {
        double t = input.phaseTicks();
        return switch (input.phase()) {
            case NaeglerophaeonEntity.GRAB -> (float) Math.clamp(t / 100, 0, 1);
            case NaeglerophaeonEntity.CHARGE, NaeglerophaeonEntity.CAPTURE -> ease(t / 20);
            case NaeglerophaeonEntity.LUNGE_WINDUP -> ease(t / LUNGE_WINDUP) * .6F;
            case NaeglerophaeonEntity.CONDUCT -> ease(t / 15);
            case NaeglerophaeonEntity.NOVA -> ease(t / NOVA_WINDUP);
            case NaeglerophaeonEntity.TRANSITION -> .7F + .3F * (float) Math.abs(Math.sin(t * .9));
            case NaeglerophaeonEntity.OVERLOAD -> .6F + .4F * (float) Math.max(0, 1 - Math.floorMod((long) t, 30) / 8.0);
            case NaeglerophaeonEntity.DRAINED -> -.15F;
            default -> 0;
        };
    }

    private static void poseArms(Frame frame, Input input, Matrix4f body) {
        var transforms = new Matrix4f[ARMS.size()][];
        var pulses = NaeglerophaeonModel.pulses(input.time(), input.seed());
        for (int i = 0; i < ARMS.size(); i++) {
            var arm = ARMS.get(i);
            var joints = ARM_JOINTS.get(i);
            transforms[i] = new Matrix4f[joints.size()];
            float angle = (float) Math.atan2(arm.pivot().y, arm.pivot().x);
            var axis = new Vector3f(-(float) Math.sin(angle), (float) Math.cos(angle), 0);
            var matrix = new Matrix4f();
            draw(frame, arm, body, WHITE, false);
            float glow = input.phase() == NaeglerophaeonEntity.VOLLEY ? .85F : .54F;
            double pulseProgress = -1;
            for (var pulse : pulses) {
                if (pulse.limb() == i) {
                    pulseProgress = pulse.progress();
                    glow = Math.max(glow, .55F + .45F * ease((pulseProgress - .7) / .3));
                }
            }
            for (int j = 0; j < joints.size(); j++) {
                Part joint = joints.get(j);
                int poseSegment = Math.min(j, 3);
                var bend = new Quaternionf().rotationAxis(ringAngle(input, poseSegment, i) * Mth.DEG_TO_RAD, axis);
                if (j < 4 && !input.drained()) bend.rotateY(input.turns().ringYaw()[j]).rotateX(input.turns().ringPitch()[j]);
                matrix = around(matrix, joint.pivot(), bend);
                transforms[i][j] = matrix;
                var world = new Matrix4f(body).mul(matrix);
                boolean activeNerve = Math.abs(pulseProgress - (j + .4) / joints.size()) < .18;
                frame.draws.add(new Draw(joint.faces(), world, brightness(glow), false, activeNerve, null));
                if (joint.terminal() != null) frame.tips[i] = position(world, joint.terminal());
            }
        }
        var a = new Vector3f();
        var b = new Vector3f();
        for (Web web : WEBS) {
            var positions = new Vector3f[4];
            for (int i = 0; i < positions.length; i++) {
                var vertex = web.face().vertices()[i];
                var skin = web.weights()[i];
                transforms[skin.left()][skin.leftJoint()].transformPosition(vertex, a);
                transforms[skin.right()][skin.rightJoint()].transformPosition(vertex, b);
                positions[i] = new Vector3f(a).lerp(b, skin.mix());
            }
            var face = new Face(positions, web.face().u(), web.face().v(), 2, web.face().webPair(), 0, null);
            frame.draws.add(new Draw(List.of(face), body, WHITE, false, false, null));
        }
    }

    private static float ringAngle(Input input, int segment, int limb) {
        double t = input.phaseTicks();
        double clock = input.time() - segment * 3;
        double cycle = clock - Math.floor(clock / 100) * 100;
        float stroke = cycle < 72 ? ease(cycle / 72) : cycle < 84 ? 1 - ease((cycle - 72) / 12) : 0;
        float idle = (-5 + segment) + (13 - segment * 3) * stroke;
        float swim = (-9 + segment * 2) + (float) Math.sin(input.time() * .26 - segment * .35) * (3 - segment * .5F);
        float base = Mth.lerp((float) input.hunting(), idle, swim) / 2
                + (float) Math.sin(input.time() * .09 - limb * .5 - segment * .7) * 1.5F;
        float windup = ease(t / 4);
        float angle = switch (input.phase()) {
            case NaeglerophaeonEntity.LUNGE_WINDUP -> Mth.lerp(ease(t / LUNGE_WINDUP), base, FLARE[segment]);
            case NaeglerophaeonEntity.LUNGE -> Mth.lerp(ease(t / 2), FLARE[segment], TUCK[segment]);
            case NaeglerophaeonEntity.LASH -> t < LASH_STRIKE
                    ? Mth.lerp(windup, base, TUCK[segment]) + (float) Math.sin(t * 1.7 + limb) * 1.5F
                    : Mth.lerp(ease((t - LASH_STRIKE) / 2), TUCK[segment], RADIAL[segment]);
            case NaeglerophaeonEntity.NOVA -> t < NOVA_WINDUP
                    ? Mth.lerp(windup, base, TUCK[segment] - 2) + (float) Math.sin(t * 2.3 + limb * 1.3) * (float) (t / NOVA_WINDUP * 2)
                    : Mth.lerp(ease((t - NOVA_WINDUP) / 2), TUCK[segment], RADIAL[segment]);
            case NaeglerophaeonEntity.CONDUCT, NaeglerophaeonEntity.CHARGE -> Mth.lerp(windup * .6F, base, REACH[segment]);
            case NaeglerophaeonEntity.VOLLEY -> base + 9 * (1 - ease(Math.max(0, t) % NaeglerophaeonCombatRules.VOLLEY_INTERVAL / 4));
            case NaeglerophaeonEntity.TRANSITION -> Mth.lerp(windup, base, FLARE[segment])
                    + (float) Math.sin(input.time() * 1.9 + limb * 2.1 + segment) * 6;
            case NaeglerophaeonEntity.NERVE_DIVE -> Mth.lerp(windup, base, TUCK[segment]);
            case NaeglerophaeonEntity.NERVE_EMERGE -> Mth.lerp(ease(t / 6), FLARE[segment], base);
            case NaeglerophaeonEntity.OVERLOAD -> Mth.lerp(ease(t / 20), base, UMBRELLA[segment])
                    + (float) Math.sin(input.time() * .9 + limb * 1.7 + segment * .8) * (3 + segment * 1.5F);
            case NaeglerophaeonEntity.DRAINED -> LIMP[segment] + (float) Math.sin(input.time() * .05 + limb) * 1.2F
                    + (Math.floorMod((long) input.time() + limb * 7, 53) < 2 ? (float) Math.sin(input.time() * 4) * 4 : 0);
            case NaeglerophaeonEntity.RECOVERY -> Mth.lerp(ease(t / 10), EASED[segment], base);
            default -> base;
        };
        // The first two joints carry the attached web and do most of the flaring.
        return angle * (segment < 2 ? .65F : .45F) * (segment == 3 ? .65F : 1);
    }

    private static List<Web> loadWebs() {
        var webs = new ArrayList<Web>();
        for (Face face : ROOT.child("crimson_interarm_webbing").faces()) {
            var weights = new ArmSkin[4];
            int left = face.webPair();
            int right = (left + 1) % ARMS.size();
            for (int i = 0; i < weights.length; i++) {
                var v = face.vertices()[i];
                float a = armDistance(v, left), b = armDistance(v, right);
                weights[i] = new ArmSkin(left, right, webJoint(v, left), webJoint(v, right), a / Math.max(a + b, 1e-5F));
            }
            webs.add(new Web(face, weights));
        }
        return List.copyOf(webs);
    }

    private static float armDistance(Vector3f p, int arm) {
        var joints = ARM_JOINTS.get(arm);
        var a = joints.getFirst().pivot();
        var b = joints.get(2).pivot();
        var delta = new Vector3f(b).sub(a);
        var offset = new Vector3f(p).sub(a);
        return offset.fma(-Mth.clamp(offset.dot(delta) / delta.lengthSquared(), 0, 1), delta).length();
    }

    private static int webJoint(Vector3f vertex, int arm) {
        var joints = ARM_JOINTS.get(arm);
        var base = joints.get(0).pivot();
        var bend = joints.get(1).pivot();
        return new Vector3f(vertex).sub(bend).dot(new Vector3f(bend).sub(base)) > 0 ? 1 : 0;
    }

    private static void poseAxon(Frame frame, Input input, Matrix4f body) {
        var transforms = new Matrix4f[AXON_JOINTS.size()];
        var rest = new Vec3[frame.tail.length];
        var matrix = new Matrix4f(body);
        for (int i = 0; i < AXON_JOINTS.size(); i++) {
            var joint = AXON_JOINTS.get(i);
            int lag = i * input.turns().tailYaw().length / AXON_JOINTS.size();
            var bend = tailBend(input, i, lag);
            matrix = around(matrix, joint.pivot(), bend);
            transforms[i] = matrix;
            rest[i] = position(matrix, joint.pivot());
        }
        rest[rest.length - 1] = position(matrix, AXON_END);
        float reach = input.victim() == null ? 0 : switch (input.phase()) {
            case NaeglerophaeonEntity.CAPTURE -> ease(input.phaseTicks() / 20);
            case NaeglerophaeonEntity.GRAB -> 1;
            default -> 0;
        };
        Vec3[] target = reach > 0 ? NaeglerophaeonModel.captureCurve(input, rest[0], 96) : input.releasedTail();
        double blend = reach > 0 ? reach : input.releaseBlend();
        double length = 0;
        double[] distances = new double[rest.length];
        for (int i = 1; i < rest.length; i++) {
            length += rest[i].distanceTo(rest[i - 1]);
            distances[i] = length;
        }
        for (int i = 0; i < rest.length; i++) {
            frame.tail[i] = rest[i];
            if (i == 0 || target == null || blend <= 0) continue;
            // Released poses already have one sample per authored joint.
            var point = reach > 0 ? NaeglerophaeonModel.sample(target, distances[i] / length) : target[i];
            frame.tail[i] = rest[i].lerp(point, blend);
        }
        for (int i = 0; i < AXON_JOINTS.size(); i++) {
            matrix = transforms[i];
            Stretch stretch = target != null && blend > 0
                    ? fit(matrix, rest[i], rest[i + 1], frame.tail[i], frame.tail[i + 1]) : null;
            float glow = .88F;
            if (input.phase() == NaeglerophaeonEntity.OVERLOAD) {
                double climb = 1 - Math.min(1, Math.floorMod((long) input.phaseTicks(), 30) / 22.0);
                glow = .55F + .45F * (float) Math.max(0, 1 - Math.abs(climb - distances[i] / length) * 5);
            }
            if (stretch != null) matrix = stretch.matrix();
            frame.draws.add(new Draw(AXON_JOINTS.get(i).faces(), matrix, brightness(glow), false, false, stretch));
            if (stretch != null && i == AXON_JOINTS.size() - 1) matrix = stretch.anchored(AXON_END);
        }
        float closure = Math.max(reach, (float) input.releaseBlend());
        for (int finger = 0; finger < GRIP.children().size(); finger++) {
            float angle = finger * Mth.TWO_PI / GRIP.children().size();
            var close = new Quaternionf().rotationAxis(closure * .26F,
                    (float) Math.cos(angle), (float) Math.sin(angle), 0);
            drawTree(frame, GRIP.children().get(finger), around(matrix, GRIP.pivot(), close), WHITE, false);
        }
    }

    private static Quaternionf tailBend(Input input, int joint, int lag) {
        double rate = input.drained() ? .025 : .09;
        float wave = (float) (Math.sin(input.time() * rate - joint * .58) - Math.sin(-joint * .58));
        float yaw = wave * (.018F + joint * .0015F);
        float pitch = wave * .023F;
        if (input.phase() == NaeglerophaeonEntity.OVERLOAD) {
            float invert = ease(input.phaseTicks() / 20);
            pitch += invert * ((joint > 2 ? -.085F : -.045F) + (float) Math.sin(input.time() * .55 - joint * .7) * .07F);
            yaw += invert * ((joint > 3 ? -.11F : 0) + (float) Math.cos(input.time() * .71 - joint * .9) * .09F);
        }
        if (!input.drained()) {
            int start = joint * input.turns().tailYaw().length / AXON_JOINTS.size();
            int end = (joint + 1) * input.turns().tailYaw().length / AXON_JOINTS.size();
            if (start != end) {
                yaw += input.turns().tailYaw()[lag];
                pitch += input.turns().tailPitch()[lag];
            }
        }
        return new Quaternionf().rotationY(yaw).rotateX(pitch);
    }

    /** Stretch only along the bone. The patterned fascicles and ganglia keep their cross-sections during reach. */
    private static Stretch fit(Matrix4f matrix, Vec3 oldStart, Vec3 oldEnd, Vec3 start, Vec3 end) {
        Vec3 oldDelta = oldEnd.subtract(oldStart), delta = end.subtract(start);
        var oldAxis = vector(oldDelta.normalize());
        var turn = new Quaternionf().rotationTo(new Vector3f(0, 0, 1), oldAxis);
        var adjust = delta.lengthSqr() > 1e-8
                ? new Quaternionf().rotationTo(oldAxis, vector(delta.normalize())) : new Quaternionf();
        float scale = (float) (delta.length() / oldDelta.length());
        var rigid = new Matrix4f().translation(vector(start)).rotate(adjust)
                .translate(vector(oldStart).negate()).mul(matrix);
        var stretched = new Matrix4f().translation(vector(start)).rotate(adjust).rotate(turn)
                .scale(1, 1, Math.max(.02F, scale)).rotate(turn.conjugate()).translate(vector(oldStart).negate()).mul(matrix);
        return new Stretch(stretched, rigid, scale);
    }

    private static Matrix4f around(Matrix4f matrix, Vector3f pivot, Quaternionf turn) {
        return new Matrix4f(matrix).translate(pivot).rotate(turn).translate(new Vector3f(pivot).negate());
    }

    private static Vector3f vector(Vec3 p) {
        return new Vector3f((float) p.x, (float) p.y, (float) p.z);
    }

    private static Vec3 position(Matrix4f matrix, Vector3f point) {
        var p = matrix.transformPosition(point, new Vector3f());
        return new Vec3(p.x, p.y, p.z);
    }

    private static float ease(double time) {
        return HemoEasing.smoothstep((float) time);
    }

    private static int brightness(float strength) {
        int shade = Math.round(strength * 255);
        return 0xFF000000 | shade << 16 | shade << 8 | shade;
    }

    private static void draw(Frame frame, Part part, Matrix4f transform, int glow, boolean ganglia) {
        if (!part.faces().isEmpty()) frame.draws.add(new Draw(part.faces(), transform, glow, ganglia, false, null));
    }

    private static void drawTree(Frame frame, Part part, Matrix4f transform, int glow, boolean ganglia) {
        draw(frame, part, transform, glow, ganglia);
        for (Part child : part.children()) drawTree(frame, child, transform, glow, ganglia);
    }

    /** Passes are opaque tissue, luminous nodes, translucent webbing, and the local victim's eye marker. */
    public static void render(Frame frame, PoseStack stack, VertexConsumer consumer, int pass, int light,
                              int overlay, boolean drained, boolean markEyes) {
        var points = new Vector3f[]{new Vector3f(), new Vector3f(), new Vector3f(), new Vector3f()};
        var normal = new Vector3f();
        var edge = new Vector3f();
        var pose = stack.last();
        for (Draw draw : frame.draws) {
            for (Face face : draw.faces()) {
                int material = face.pass();
                if (material == 4) material = draw.activeNerve() && !drained ? 1 : 0;
                if (material == 1) material = drained ? 0 : markEyes && draw.ganglia() ? 3 : 1;
                if (material != pass) continue;
                Matrix4f transform = draw.stretch() != null && face.axonDetail() != null
                        ? draw.stretch().anchored(face.axonDetail()) : draw.transform();
                for (int i = 0; i < 4; i++) transform.transformPosition(face.vertices()[i], points[i]);
                normal.set(points[1]).sub(points[0]).cross(edge.set(points[2]).sub(points[0]));
                if (normal.lengthSquared() < 1e-12F) continue;
                normal.normalize();
                int color = pass == 1 ? draw.glow() : WHITE;
                int faceLight = pass == 1 || pass == 3 ? LightTexture.FULL_BRIGHT : light;
                float repeats = face.axonAxis() != 0 && draw.stretch() != null ? draw.stretch().lengthFactor() : 1;
                if (repeats > 1.01F) {
                    renderStretchedFace(consumer, pose, face, points, normal, edge, repeats, color, overlay, faceLight);
                    continue;
                }
                for (int i = 0; i < 4; i++) {
                    vertex(consumer, pose, points[i], normal, face.u()[i], face.v()[i], color, overlay, faceLight);
                }
            }
        }
    }

    /** Repeat only along the extending fibers so their texels do not smear into long stripes. */
    private static void renderStretchedFace(VertexConsumer consumer, PoseStack.Pose pose, Face face,
                                            Vector3f[] points, Vector3f normal, Vector3f point, float repeats,
                                            int color, int overlay, int light) {
        for (int strip = 0; strip < repeats; strip++) {
            float span = Math.min(1, repeats - strip);
            for (int i = 0; i < 4; i++) {
                boolean end = face.axonAxis() == 1 ? i >= 2 : i == 1 || i == 2;
                int peer = i ^ (face.axonAxis() == 1 ? 3 : 1);
                int from = end ? peer : i, to = end ? i : peer;
                point.set(points[from]).lerp(points[to], (strip + (end ? span : 0)) / repeats);
                float u = Mth.lerp(end ? span : 0, face.u()[from], face.u()[to]);
                float v = Mth.lerp(end ? span : 0, face.v()[from], face.v()[to]);
                vertex(consumer, pose, point, normal, u, v, color, overlay, light);
            }
        }
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, Vector3f point, Vector3f normal,
                               float u, float v, int color, int overlay, int light) {
        consumer.addVertex(pose.pose(), point.x, point.y, point.z).setColor(color).setUv(u, v)
                .setOverlay(overlay).setLight(light).setNormal(pose, normal.x, normal.y, normal.z);
    }
}
