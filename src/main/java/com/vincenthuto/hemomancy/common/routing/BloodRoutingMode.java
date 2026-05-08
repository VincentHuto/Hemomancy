package com.vincenthuto.hemomancy.common.routing;

public enum BloodRoutingMode {
    NEARBY,
    SANCTUM;

    public BloodRoutingMode next() {
        return this == NEARBY ? SANCTUM : NEARBY;
    }
}
