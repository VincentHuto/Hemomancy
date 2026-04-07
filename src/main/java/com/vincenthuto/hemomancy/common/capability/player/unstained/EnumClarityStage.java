package com.vincenthuto.hemomancy.common.capability.player.unstained;

public enum EnumClarityStage {
    AWAKENED(0, "Awakened", 0f),
    DISCERNING(1, "Discerning", 25f),
    VIGILANT(2, "Vigilant", 50f),
    RESOLUTE(3, "Resolute", 75f),
    ENLIGHTENED(4, "Enlightened", 100f);

    private final int level;
    private final String title;
    private final float minClarity;

    EnumClarityStage(int level, String title, float minClarity) {
        this.level = level;
        this.title = title;
        this.minClarity = minClarity;
    }

    public int getLevel() {
        return level;
    }

    public String getTitle() {
        return title;
    }

    public float getMinClarity() {
        return minClarity;
    }

    /** Returns the clarity stage corresponding to the given clarity value. */
    public static EnumClarityStage byClarity(float clarity) {
        EnumClarityStage result = AWAKENED;
        for (EnumClarityStage stage : values()) {
            if (clarity >= stage.minClarity) {
                result = stage;
            }
        }
        return result;
    }
}
