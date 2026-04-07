package com.vincenthuto.hemomancy.common.capability.player.unstained;

public enum EnumPurityStage {
    CORRUPTED(0, "Corrupted", 0f, 0f),
    TAINTED(1, "Tainted", 25f, 0.10f),
    CLEANSING(2, "Cleansing", 50f, 0.25f),
    ABSOLVED(3, "Absolved", 75f, 0.50f),
    PURIFIED(4, "Purified", 100f, 1.0f);

    private final int level;
    private final String title;
    private final float minPurity;
    /** Multiplier reduction to blood manipulation effectiveness at this stage. */
    private final float bloodMagicPenalty;

    EnumPurityStage(int level, String title, float minPurity, float bloodMagicPenalty) {
        this.level = level;
        this.title = title;
        this.minPurity = minPurity;
        this.bloodMagicPenalty = bloodMagicPenalty;
    }

    public int getLevel() {
        return level;
    }

    public String getTitle() {
        return title;
    }

    public float getMinPurity() {
        return minPurity;
    }

    public float getBloodMagicPenalty() {
        return bloodMagicPenalty;
    }

    /** Returns the purity stage corresponding to the given purity value. */
    public static EnumPurityStage byPurity(float purity) {
        EnumPurityStage result = CORRUPTED;
        for (EnumPurityStage stage : values()) {
            if (purity >= stage.minPurity) {
                result = stage;
            }
        }
        return result;
    }
}
