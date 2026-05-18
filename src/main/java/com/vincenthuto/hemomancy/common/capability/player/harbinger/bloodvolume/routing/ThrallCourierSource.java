package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;

public final class ThrallCourierSource implements IBloodSourceContract {
    private double carriedBlood;
    private final double maxRatePerTick;

    public ThrallCourierSource(double carriedBlood, double maxRatePerTick) {
        this.carriedBlood = Math.max(0, carriedBlood);
        this.maxRatePerTick = maxRatePerTick;
    }

    @Override
    public boolean isValid() {
        return carriedBlood > 0 && maxRatePerTick > 0;
    }

    @Override
    public double getMaxRatePerTick() {
        return maxRatePerTick;
    }

    @Override
    public double transferTo(IBloodVolume target, double requested) {
        if (!isValid() || target == null || requested <= 0) {
            return 0;
        }
        double amount = Math.min(requested, maxRatePerTick);
        amount = Math.min(amount, carriedBlood);
        amount = Math.min(amount, BloodRoutingRules.freeSpace(target));
        if (amount <= 0) {
            return 0;
        }
        target.fill(amount);
        carriedBlood -= amount;
        return amount;
    }

    public double getCarriedBlood() {
        return carriedBlood;
    }
}
