package com.vincenthuto.hemomancy.common.enchanting;

public enum ResonantForgeTier {
    BASE(3),
    PRECISION(5),
    MASTERWORK(7);

    private final int degree;

    ResonantForgeTier(int degree) {
        this.degree = degree;
    }

    public int degree() {
        return degree;
    }
}
