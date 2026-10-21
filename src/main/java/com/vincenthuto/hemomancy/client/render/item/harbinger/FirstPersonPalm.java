package com.vincenthuto.hemomancy.client.render.item.harbinger;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Where the first-person cell hand's palm is, shared by the cell particles and the cast geometry. In 1.21 the hand
 * pose starts from the inverse view rotation, so a point on the posed arm is already a world-axis offset from the
 * camera. It is drawn with the fixed 70-degree hand projection, though, so the offset is spread across the view by the
 * ratio of the hand and world projections to land on the same pixel at any field of view.
 */
public final class FirstPersonPalm {
    /** cot(35 degrees): the hand projection's focal length. */
    private static final double HAND_FOCAL = 1 / Math.tan(Math.toRadians(35));
    /** Arm-local palm, in model pixels: the bottom centre of the arm cube. */
    private static final float PALM_Y = 10, RIGHT_PALM_X = -1, LEFT_PALM_X = 1;
    private static final long STALE_NANOS = 150_000_000L;

    private static Vector3f palm, fingers;
    private static HumanoidArm arm;
    private static long recorded;
    private static double worldFocal = HAND_FOCAL;

    private FirstPersonPalm() {
    }

    /**
     * Records the palm from the first-person arm's pose. {@code pose} is the hand pose before the arm part's own
     * pivot and rotation, which {@code armPart} applies.
     */
    public static void record(Matrix4f pose, net.minecraft.client.model.geom.ModelPart armPart, HumanoidArm side) {
        var stack = new com.mojang.blaze3d.vertex.PoseStack();
        stack.mulPose(pose);
        armPart.translateAndRotate(stack);
        Matrix4f m = stack.last().pose();
        float x = (side == HumanoidArm.RIGHT ? RIGHT_PALM_X : LEFT_PALM_X) / 16f;
        palm = m.transformPosition(x, PALM_Y / 16f, 0, new Vector3f());
        fingers = m.transformDirection(0, 1, 0, new Vector3f()).normalize();
        arm = side;
        recorded = System.nanoTime();
    }

    /** Diagnostics for review harnesses: the recorded view-space palm, its age and the projection ratio. */
    public static String describe() {
        return "palm=" + palm + " fingers=" + fingers + " arm=" + arm + " ageMs=" + (System.nanoTime() - recorded) / 1_000_000
                + " k=" + HAND_FOCAL / worldFocal;
    }

    /** The world projection's focal length (its m11), set once per frame by the world renderer. */
    public static void worldProjection(Matrix4f projection) {
        double m11 = projection.m11();
        if (Double.isFinite(m11) && m11 > 0.1) worldFocal = m11;
    }

    /** Whether a palm was drawn this frame or last for {@code side}. */
    public static boolean fresh(HumanoidArm side) {
        return palm != null && arm == side && System.nanoTime() - recorded < STALE_NANOS;
    }

    /** The palm in world space, or null when no cell hand was drawn in first person just now. */
    public static Vec3 world(HumanoidArm side) {
        if (!fresh(side)) return null;
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        return camera.getPosition().add(posedToWorld(palm, camera));
    }

    /**
     * Where vanilla holds an ordinary item in first person (its item-arm transform), in world space. Used for casts
     * that grow from a held weapon or tool rather than the cell hand.
     */
    public static Vec3 itemHand(HumanoidArm side) {
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f view = new Vector3f(side == HumanoidArm.RIGHT ? 0.56f : -0.56f, -0.52f, -0.72f);
        return camera.getPosition().add(viewToWorld(view, camera));
    }

    /** The direction the fingers point, in world space, or null when stale. */
    public static Vec3 fingers(HumanoidArm side) {
        if (!fresh(side)) return null;
        // Map a point a little way along the fingers too, so the direction bends with the view the same way.
        Camera camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Vector3f tip = new Vector3f(fingers).mul(0.2f).add(palm);
        Vec3 dir = posedToWorld(tip, camera).subtract(posedToWorld(palm, camera));
        return dir.lengthSqr() < 1e-8 ? null : dir.normalize();
    }

    /** A point from a first-person hand render (a posed, camera-relative point) as a camera-relative world offset. */
    public static Vec3 posedOffset(Vector3f posed) {
        return posedToWorld(posed, Minecraft.getInstance().gameRenderer.getMainCamera());
    }

    /**
     * A point from the posed first-person arm (a world-axis offset from the camera) where the world projection draws
     * it on the same pixel: it keeps its depth and moves across the view by the ratio of the projections.
     */
    static Vec3 posedToWorld(Vector3f posed, Camera camera) {
        Vec3 right = new Vec3(camera.getLeftVector()).scale(-1), up = new Vec3(camera.getUpVector()), ahead = new Vec3(camera.getLookVector());
        Vec3 offset = new Vec3(posed.x, posed.y, posed.z);
        double k = HAND_FOCAL / worldFocal;
        return right.scale(offset.dot(right) * k).add(up.scale(offset.dot(up) * k)).add(ahead.scale(offset.dot(ahead)));
    }

    /** A view-space point (+x right, +y up, -z ahead) in world axes, scaled across the view like {@link #posedToWorld}. */
    static Vec3 viewToWorld(Vector3f view, Camera camera) {
        double k = HAND_FOCAL / worldFocal;
        // View space: +x right, +y up, -z ahead. The camera's own axes avoid any sign convention in its quaternion.
        Vec3 right = new Vec3(camera.getLeftVector()).scale(-1), up = new Vec3(camera.getUpVector()), ahead = new Vec3(camera.getLookVector());
        return right.scale(view.x * k).add(up.scale(view.y * k)).add(ahead.scale(-view.z));
    }
}
