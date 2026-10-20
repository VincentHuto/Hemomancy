package com.vincenthuto.hemomancy.client.player;

/**
 * Rotations in radians on the six rigid player parts plus a waist pitch; never changes entity position or aim.
 * Authoring convention: right-arm z negative / left-arm z positive = outward. Vanilla applies X before Z and uses the
 * opposite sign, so renderers call {@link #toModel()} once before touching model parts. At x near -90 z is pure roll;
 * sideways spread needs a small x with a large z, and cross-body sweeps need arm yaw.
 */
public record CastingPose(Rotation head, Rotation body, Rotation rightArm, Rotation leftArm,
                          Rotation rightLeg, Rotation leftLeg, float pitch) implements Blendable<CastingPose> {
    public record Rotation(float x, float y, float z) {
        public static final Rotation ZERO = new Rotation(0, 0, 0);
        public Rotation mirror() { return new Rotation(x, clean(-y), clean(-z)); }
        public Rotation blend(Rotation b, float t) {
            return new Rotation(x + (b.x-x)*t, y + (b.y-y)*t, z + (b.z-z)*t);
        }
        public Rotation offset(Rotation b, float s) { return new Rotation(x + b.x*s, y + b.y*s, z + b.z*s); }
        static float clean(float x) { return x == 0 ? 0 : x; }
    }
    public static final CastingPose ZERO = new CastingPose(Rotation.ZERO, Rotation.ZERO, Rotation.ZERO,
            Rotation.ZERO, Rotation.ZERO, Rotation.ZERO, 0);
    public CastingPose(Rotation head, Rotation body, Rotation rightArm, Rotation leftArm, Rotation rightLeg, Rotation leftLeg) {
        this(head, body, rightArm, leftArm, rightLeg, leftLeg, 0);
    }
    /** Authoring sign to vanilla ModelPart sign: only the arm roll flips. */
    public CastingPose toModel() {
        return new CastingPose(head, body, new Rotation(rightArm.x, rightArm.y, Rotation.clean(-rightArm.z)),
                new Rotation(leftArm.x, leftArm.y, Rotation.clean(-leftArm.z)), rightLeg, leftLeg, pitch);
    }
    public CastingPose mirror() {
        return new CastingPose(head.mirror(), body.mirror(), leftArm.mirror(), rightArm.mirror(),
                leftLeg.mirror(), rightLeg.mirror(), pitch);
    }
    @Override public CastingPose blend(CastingPose b, float t) {
        if (t <= 0) return this;
        if (t >= 1) return b;
        return new CastingPose(head.blend(b.head,t), body.blend(b.body,t), rightArm.blend(b.rightArm,t),
                leftArm.blend(b.leftArm,t), rightLeg.blend(b.rightLeg,t), leftLeg.blend(b.leftLeg,t),
                pitch + (b.pitch-pitch)*t);
    }
    @Override public CastingPose offset(CastingPose b, float s) {
        if (s == 0) return this;
        return new CastingPose(head.offset(b.head,s), body.offset(b.body,s), rightArm.offset(b.rightArm,s),
                leftArm.offset(b.leftArm,s), rightLeg.offset(b.rightLeg,s), leftLeg.offset(b.leftLeg,s),
                pitch + b.pitch*s);
    }
    public static Rotation degrees(double x, double y, double z) {
        return new Rotation(rad(x),rad(y),rad(z));
    }
    private static float rad(double degrees) {
        // Negative zero would break record equality between mirrored and authored poses.
        return degrees == 0 ? 0 : (float)Math.toRadians(degrees);
    }
    /** lean pitches the body about the waist (degrees, positive forward); twist yaws it. */
    public static CastingPose pose(double head, double lean, double twist, double rightX, double rightZ,
                                   double leftX, double leftZ, double stance) {
        return new CastingPose(degrees(head,-twist*.4,0), degrees(0,twist,0), degrees(rightX,0,rightZ),
                degrees(leftX,0,leftZ), degrees(0,0,stance), degrees(0,0,-stance), (float)Math.toRadians(lean));
    }
    public static CastingPose hands(double rightX, double rightZ, double leftX, double leftZ) {
        return pose(0,0,0,rightX,rightZ,leftX,leftZ,0);
    }
}
