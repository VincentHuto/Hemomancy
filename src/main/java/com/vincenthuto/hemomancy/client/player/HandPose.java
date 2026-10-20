package com.vincenthuto.hemomancy.client.player;

/** Camera-space first-person hands: translation in blocks, rotation in radians. Lead is the main arm. */
public record HandPose(Hand lead, Hand support) implements Blendable<HandPose> {
    public record Hand(float tx, float ty, float tz, float rx, float ry, float rz) {
        public static final Hand ZERO = new Hand(0,0,0,0,0,0);
        public Hand blend(Hand b, float t) {
            return new Hand(tx+(b.tx-tx)*t, ty+(b.ty-ty)*t, tz+(b.tz-tz)*t, rx+(b.rx-rx)*t, ry+(b.ry-ry)*t, rz+(b.rz-rz)*t);
        }
        public Hand offset(Hand b, float s) { return new Hand(tx+b.tx*s, ty+b.ty*s, tz+b.tz*s, rx+b.rx*s, ry+b.ry*s, rz+b.rz*s); }
        public Hand mirror() { return new Hand(clean(-tx), ty, tz, rx, clean(-ry), clean(-rz)); }
        private static float clean(float v) { return v == 0 ? 0 : v; }
    }
    public static final HandPose ZERO = new HandPose(Hand.ZERO, Hand.ZERO);
    /** Lead hand only; rotations in degrees. */
    public static HandPose hand(double tx, double ty, double tz, double rx, double ry, double rz) {
        return new HandPose(new Hand((float)tx,(float)ty,(float)tz,(float)Math.toRadians(rx),(float)Math.toRadians(ry),(float)Math.toRadians(rz)), Hand.ZERO);
    }
    /** Both hands; rotations in degrees. */
    public static HandPose hands(double ltx, double lty, double ltz, double lrx, double lry, double lrz,
                                 double stx, double sty, double stz, double srx, double sry, double srz) {
        return new HandPose(hand(ltx,lty,ltz,lrx,lry,lrz).lead(), hand(stx,sty,stz,srx,sry,srz).lead());
    }
    public HandPose mirror() { return new HandPose(support.mirror(), lead.mirror()); }
    @Override public HandPose blend(HandPose b, float t) {
        if (t <= 0) return this;
        if (t >= 1) return b;
        return new HandPose(lead.blend(b.lead,t), support.blend(b.support,t));
    }
    @Override public HandPose offset(HandPose b, float s) {
        return s == 0 ? this : new HandPose(lead.offset(b.lead,s), support.offset(b.support,s));
    }
}
