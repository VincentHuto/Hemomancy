package com.vincenthuto.hemomancy.common.brewing;

public enum AlembicTier {
    BASE, CONDENSER, ATHANOR;

    public static AlembicTier fromSaved(int value) {
        return value >= 0 && value < values().length ? values()[value] : BASE;
    }

    public boolean hasSecondCatalyst() {
        return this == ATHANOR;
    }

    public boolean hasAdvancedBrewing() { return this != BASE; }
}
