package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

public enum BloodRoutingMode {
    NEARBY,
    SANCTUM;

    public BloodRoutingMode next() {
        return this == NEARBY ? SANCTUM : NEARBY;
    }
}
