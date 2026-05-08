package com.vincenthuto.hemomancy.common.routing;

import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

public final class EquippedGourdSource implements IBloodSourceContract {
    private final IBloodVolume gourdVolume;
    private final double maxRatePerTick;

    public EquippedGourdSource(IBloodVolume gourdVolume, double maxRatePerTick) {
        this.gourdVolume = gourdVolume;
        this.maxRatePerTick = maxRatePerTick;
    }

    @Override
    public boolean isValid() {
        return gourdVolume != null && gourdVolume.getBloodVolume() > 0 && maxRatePerTick > 0;
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
        return BloodRoutingRules.transfer(gourdVolume, target, requested, maxRatePerTick);
    }
}
