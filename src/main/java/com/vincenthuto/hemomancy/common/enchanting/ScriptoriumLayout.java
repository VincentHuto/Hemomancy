package com.vincenthuto.hemomancy.common.enchanting;

/** Physical reservoir centers in the north-facing model, in model pixels. */
public final class ScriptoriumLayout {
    private static final float[] X = {-2.5F, -1.5F, 1.5F, 5.75F, 10.25F, 14.5F, 17.5F, 18.5F};
    private static final float[] Z = {8F, 12.5F, 16.25F, 18.25F, 18.25F, 16.25F, 12.5F, 8F};
    public static final float FLUID_BOTTOM = 18.5F / 16F;
    public static final float FLUID_UNIT_HEIGHT = 1.25F / 16F;

    private ScriptoriumLayout() {}

    public static float tubeX(int index) { return X[index] / 16F; }
    public static float tubeZ(int index) { return Z[index] / 16F; }
    public static float tubeY() { return 20.5F / 16F; }
}
