package com.vincenthuto.hemomancy.common.routing;

import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

public final class LinkedPlayerSource implements IBloodSourceContract {
    private final IBloodVolume playerVolume;
    private final double safetyFloor;
    private final double maxRatePerTick;

    public LinkedPlayerSource(IBloodVolume playerVolume, double safetyFloor, double maxRatePerTick) {
        this.playerVolume = playerVolume;
        this.safetyFloor = safetyFloor;
        this.maxRatePerTick = maxRatePerTick;
    }

    @Override
    public boolean isValid() {
        return playerVolume != null
                && playerVolume.isActive()
                && BloodRoutingRules.availableAboveFloor(playerVolume, safetyFloor) > 0;
    }

    @Override
    public double getMaxRatePerTick() {
        return maxRatePerTick;
    }

    @Override
    public double transferTo(IBloodVolume target, double requested) {
        if (!isValid()) {
            return 0;
        }
        double safeRequest = Math.min(requested, BloodRoutingRules.availableAboveFloor(playerVolume, safetyFloor));
        return BloodRoutingRules.transfer(playerVolume, target, safeRequest, maxRatePerTick);
    }
}
