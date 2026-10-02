package com.vincenthuto.hemomancy.common.brewing;

public enum AlembicTier {
    BASE, CONDENSER, ATHANOR;

    public static AlembicTier fromSaved(int value) {
        return value >= 0 && value < values().length ? values()[value] : BASE;
    }

    public boolean hasSecondCatalyst() {
        return this == ATHANOR;
    }

    public int bloodCapacity() {
        return this == BASE ? 5000 : 7500;
    }

    public boolean hasInternalHeat() { return this == ATHANOR; }

    public int processingSpeed() { return this == ATHANOR ? 2 : 1; }

    public int processingTicks(int recipeTicks) {
        return Math.ceilDiv(recipeTicks, processingSpeed());
    }

    public boolean hasAdvancedBrewing() { return this != BASE; }
}
