package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

/** Registry-independent angles for the connected arm rig. */
public final class VampireSquidPose {
    private VampireSquidPose() {}
    public static float wave(float age, int arm, int joint) {
        return (float)Math.sin(age * .13F - joint * .55F + arm * .35F);
    }
    public static float armAngle(float age, int arm, int joint, float cloak) {
        float open = (joint == 0 ? .49F : -.17F) + wave(age, arm, joint) * (.12F + joint * .035F);
        float folded = joint == 0 ? 2.8F : .22F;
        return open + cloak * (folded - open);
    }
}
