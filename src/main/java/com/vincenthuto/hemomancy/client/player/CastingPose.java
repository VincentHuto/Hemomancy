package com.vincenthuto.hemomancy.client.player;

/** Rotations in radians on the six rigid player parts; never changes entity position or aim. */
public record CastingPose(Rotation head, Rotation body, Rotation rightArm, Rotation leftArm,
                          Rotation rightLeg, Rotation leftLeg) {
    public record Rotation(float x, float y, float z) {
        public static final Rotation ZERO = new Rotation(0, 0, 0);
        public Rotation mirror() { return new Rotation(x, clean(-y), clean(-z)); }
        public Rotation blend(Rotation b, float t) {
            return new Rotation(x + (b.x-x)*t, y + (b.y-y)*t, z + (b.z-z)*t);
        }
        private static float clean(float x) { return x == 0 ? 0 : x; }
    }
    public static final CastingPose ZERO = new CastingPose(Rotation.ZERO, Rotation.ZERO, Rotation.ZERO,
            Rotation.ZERO, Rotation.ZERO, Rotation.ZERO);
    public CastingPose mirror() {
        return new CastingPose(head.mirror(), body.mirror(), leftArm.mirror(), rightArm.mirror(),
                leftLeg.mirror(), rightLeg.mirror());
    }
    public CastingPose blend(CastingPose b, float t) {
        if (t <= 0) return this;
        if (t >= 1) return b;
        return new CastingPose(head.blend(b.head,t), body.blend(b.body,t), rightArm.blend(b.rightArm,t),
                leftArm.blend(b.leftArm,t), rightLeg.blend(b.rightLeg,t), leftLeg.blend(b.leftLeg,t));
    }
    public static Rotation degrees(double x, double y, double z) {
        return new Rotation((float)Math.toRadians(x),(float)Math.toRadians(y),(float)Math.toRadians(z));
    }
    public static CastingPose pose(double head, double lean, double twist, double rightX, double rightZ,
                                   double leftX, double leftZ, double stance) {
        return new CastingPose(degrees(head,-twist*.4,0), degrees(lean,twist,0), degrees(rightX,0,rightZ),
                degrees(leftX,0,leftZ), degrees(0,0,stance), degrees(0,0,-stance));
    }
    public static CastingPose hands(double rightX, double rightZ, double leftX, double leftZ) {
        return pose(0,0,0,rightX,rightZ,leftX,leftZ,0);
    }
}
