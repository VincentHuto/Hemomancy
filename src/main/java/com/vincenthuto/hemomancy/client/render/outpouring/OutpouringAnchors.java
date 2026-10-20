package com.vincenthuto.hemomancy.client.render.outpouring;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.client.player.FirstPersonHandRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Where the blood leaves the body. Third person captures the exits from the posed model each frame, so blood leaves the
 * real hand, chest or eyes mid-animation; first person and unrendered players fall back to an estimate from body yaw and
 * look direction. Body-local points are in blocks: x right, y up from the feet, z forward.
 */
public final class OutpouringAnchors {
    private record Captured(long tick, EnumVeinSections section, double[] exits, double[] outward) {}
    private static final Map<UUID, Captured> CAPTURED = new HashMap<>();

    private OutpouringAnchors() {}

    /** Exit points for a section in the body-local frame (xyz triples). Rank opens more of the head from Summa. */
    static double[] local(EnumVeinSections section, int rank) {
        return switch (section) {
            case HEAD -> rank >= 2
                    ? new double[]{.11, 1.56, .25, -.11, 1.56, .25, 0, 1.48, .27, .25, 1.5, 0, -.25, 1.5, 0}
                    : new double[]{.11, 1.56, .25, -.11, 1.56, .25};
            case HEART -> new double[]{-.08, 1.15, .14};
            case BODY -> new double[]{.25, 1.0, 0, -.25, 1.0, 0, 0, .85, .14, 0, 1.1, -.14};
            case LEGS -> new double[]{.12, .55, .1, -.12, .55, .1, .12, .25, .08, -.12, .25, .08};
            case ARMS -> new double[]{.35, 1.1, .45, -.35, 1.1, .45};
        };
    }

    /** The direction blood leaves each exit of {@link #local}, in the same frame. */
    static double[] outward(EnumVeinSections section, int rank) {
        return switch (section) {
            case HEAD -> rank >= 2
                    ? new double[]{0, -.2, 1, 0, -.2, 1, 0, -.4, 1, 1, -.2, 0, -1, -.2, 0}
                    : new double[]{0, -.2, 1, 0, -.2, 1};
            case HEART -> new double[]{0, 0, 1};
            case BODY -> new double[]{1, 0, 0, -1, 0, 0, 0, 0, 1, 0, 0, -1};
            case LEGS -> new double[]{0, -1, .2, 0, -1, .2, 0, -1, .2, 0, -1, .2};
            case ARMS -> new double[]{0, 0, 1, 0, 0, 1};
        };
    }

    /**
     * Places body-local exits in the world for a body at (x, y, z) facing {@code yaw} degrees (vanilla: 0 faces +z).
     * Writes the rotated outward directions into {@code outwardOut} and returns the world points.
     */
    static double[] toWorld(double[] local, double[] outward, int count, double x, double y, double z, double yaw,
                            double scale, boolean crouch, double[] outwardOut) {
        double r = Math.toRadians(yaw), fx = -Math.sin(r), fz = Math.cos(r), rx = -Math.cos(r), rz = -Math.sin(r);
        double[] world = new double[count * 3];
        for (int i = 0; i < count; i++) {
            double lx = local[i * 3], ly = local[i * 3 + 1] - (crouch ? .3 : 0), lz = local[i * 3 + 2];
            world[i * 3] = x + (rx * lx + fx * lz) * scale;
            world[i * 3 + 1] = y + ly * scale;
            world[i * 3 + 2] = z + (rz * lx + fz * lz) * scale;
            double ox = outward[i * 3], oy = outward[i * 3 + 1], oz = outward[i * 3 + 2];
            outwardOut[i * 3] = rx * ox + fx * oz;
            outwardOut[i * 3 + 1] = oy;
            outwardOut[i * 3 + 2] = rz * ox + fz * oz;
        }
        return world;
    }

    /** World exits for a caster, from this or last frame's capture when there is one. Fills {@code outwardOut}. */
    static double[] exits(Player player, EnumVeinSections section, EnumManipulationRank rank, float partial, double[] outwardOut) {
        var captured = CAPTURED.get(player.getUUID());
        long now = player.level().getGameTime();
        if (captured != null && captured.section == section && now - captured.tick <= 2 && outwardOut.length >= captured.outward.length) {
            System.arraycopy(captured.outward, 0, outwardOut, 0, captured.outward.length);
            return captured.exits;
        }
        Vec3 at = player.getPosition(partial);
        if (section == EnumVeinSections.ARMS) return hands(player, rank, partial, outwardOut);
        double[] local = local(section, rank.ordinal());
        return toWorld(local, outward(section, rank.ordinal()), local.length / 3, at.x, at.y, at.z,
                player.getPreciseBodyRotation(partial), player.getScale(), player.isCrouching(), outwardOut);
    }

    /** Casting hands sit in front of the eyes in every cast pose, so the look direction places them. */
    private static double[] hands(Player player, EnumManipulationRank rank, float partial, double[] outwardOut) {
        Vec3 eye = player.getEyePosition(partial), look = player.getViewVector(partial);
        Vec3 side = look.cross(new Vec3(0, 1, 0));
        side = side.lengthSqr() < 1e-6 ? new Vec3(1, 0, 0) : side.normalize();
        Vec3 down = side.cross(look).normalize();
        boolean both = FirstPersonHandRules.affectsOffHand(rank, null);
        int main = player.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
        double scale = player.getScale();
        double[] world = new double[both ? 6 : 3];
        for (int i = 0; i < (both ? 2 : 1); i++) {
            Vec3 hand = eye.add(look.scale(.6 * scale)).add(down.scale(-.35 * scale)).add(side.scale(.3 * main * (i == 0 ? 1 : -1) * scale));
            world[i * 3] = hand.x; world[i * 3 + 1] = hand.y; world[i * 3 + 2] = hand.z;
            outwardOut[i * 3] = look.x; outwardOut[i * 3 + 1] = look.y; outwardOut[i * 3 + 2] = look.z;
        }
        return world;
    }

    /**
     * Captures the exits from the posed third-person model. Points are part-local pixels (model y down, front is -z),
     * carried through the layer's camera-relative pose and then offset by the camera.
     */
    public static void capture(AbstractClientPlayer player, PlayerModel<AbstractClientPlayer> model, PoseStack poses, float partial) {
        var cast = OutpouringClientState.tracker().current(player.getUUID());
        if (cast == null) return;
        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double[] exits = new double[15], outward = new double[15];
        int n = 0;
        switch (cast.section) {
            case HEAD -> {
                n = point(poses, model.head, -2, -4, -4.1f, 0, .3f, -1, camera, exits, outward, n);
                n = point(poses, model.head, 2, -4, -4.1f, 0, .3f, -1, camera, exits, outward, n);
                if (cast.rank.ordinal() >= 2) {
                    n = point(poses, model.head, 0, -3, -4.1f, 0, .5f, -1, camera, exits, outward, n);
                    n = point(poses, model.head, -4.1f, -3.5f, 0, -1, .2f, 0, camera, exits, outward, n);
                    n = point(poses, model.head, 4.1f, -3.5f, 0, 1, .2f, 0, camera, exits, outward, n);
                }
            }
            case HEART -> n = point(poses, model.body, 1.5f, 5, -2.1f, 0, 0, -1, camera, exits, outward, n);
            case BODY -> {
                n = point(poses, model.body, -4.1f, 7, 0, -1, 0, 0, camera, exits, outward, n);
                n = point(poses, model.body, 4.1f, 7, 0, 1, 0, 0, camera, exits, outward, n);
                n = point(poses, model.body, 0, 9, -2.1f, 0, 0, -1, camera, exits, outward, n);
                n = point(poses, model.body, 0, 5, 2.1f, 0, 0, 1, camera, exits, outward, n);
            }
            case LEGS -> {
                for (ModelPart leg : new ModelPart[]{model.rightLeg, model.leftLeg}) {
                    n = point(poses, leg, 0, 3, -2.1f, 0, 1, -.2f, camera, exits, outward, n);
                    n = point(poses, leg, 0, 9, -2.1f, 0, 1, -.2f, camera, exits, outward, n);
                }
            }
            case ARMS -> {
                var main = player.getMainArm();
                n = hand(poses, model, main, camera, exits, outward, n);
                if (FirstPersonHandRules.affectsOffHand(cast.rank, null)) n = hand(poses, model, main.getOpposite(), camera, exits, outward, n);
            }
        }
        if (n == 0) return;
        // The inventory doll and mirrors pose the model far from the player; only a world render places real exits.
        Vec3 at = player.position();
        double dx = exits[0] - at.x, dy = exits[1] - at.y, dz = exits[2] - at.z;
        if (dx * dx + dy * dy + dz * dz > 9) return;
        double[] e = new double[n * 3], o = new double[n * 3];
        System.arraycopy(exits, 0, e, 0, n * 3);
        System.arraycopy(outward, 0, o, 0, n * 3);
        CAPTURED.put(player.getUUID(), new Captured(player.level().getGameTime(), cast.section, e, o));
    }

    public static void clear() { CAPTURED.clear(); }

    private static int hand(PoseStack poses, PlayerModel<?> model, HumanoidArm arm, Vec3 camera, double[] exits, double[] outward, int n) {
        ModelPart part = arm == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
        // Arm-local: the palm sits 10 px down the arm from the shoulder pivot, blood runs on toward the fingertips.
        return point(poses, part, arm == HumanoidArm.RIGHT ? -1 : 1, 10.5f, 0, 0, 1, 0, camera, exits, outward, n);
    }

    private static int point(PoseStack poses, ModelPart part, float px, float py, float pz, float dx, float dy, float dz,
                             Vec3 camera, double[] exits, double[] outward, int n) {
        poses.pushPose();
        part.translateAndRotate(poses);
        var matrix = poses.last().pose();
        Vector3f at = matrix.transformPosition(px / 16f, py / 16f, pz / 16f, new Vector3f());
        Vector3f tip = matrix.transformPosition((px + dx) / 16f, (py + dy) / 16f, (pz + dz) / 16f, new Vector3f());
        poses.popPose();
        Vector3f dir = tip.sub(at, new Vector3f());
        if (dir.lengthSquared() > 1e-10) dir.normalize();
        exits[n * 3] = at.x + camera.x; exits[n * 3 + 1] = at.y + camera.y; exits[n * 3 + 2] = at.z + camera.z;
        outward[n * 3] = dir.x; outward[n * 3 + 1] = dir.y; outward[n * 3 + 2] = dir.z;
        return n + 1;
    }
}
