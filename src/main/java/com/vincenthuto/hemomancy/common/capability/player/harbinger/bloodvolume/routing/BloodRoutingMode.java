package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

public enum BloodRoutingMode {
    NEARBY,
    FANE;

    public BloodRoutingMode next() {
        return this == NEARBY ? FANE : NEARBY;
    }
}
