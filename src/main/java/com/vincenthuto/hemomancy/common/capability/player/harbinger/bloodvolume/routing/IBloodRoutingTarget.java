package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;

import javax.annotation.Nullable;

public interface IBloodRoutingTarget {

    @Nullable
    IBloodVolume getRoutingBloodVolume();

    default double getRoutingWorkingReserve() {
        return BloodRoutingRules.DEFAULT_WORKING_RESERVE;
    }

    default double getBloodRoutingRequest(double maxRequest) {
        return BloodRoutingRules.calculateReserveRequest(getRoutingBloodVolume(), getRoutingWorkingReserve(), maxRequest);
    }

    default void onBloodRouted(double amount) {
    }
}
