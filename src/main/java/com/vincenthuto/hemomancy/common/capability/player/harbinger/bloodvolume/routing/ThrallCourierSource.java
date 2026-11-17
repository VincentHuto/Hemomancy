package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.tile.BlockBloodVolume;

/**
 * A Blood Thrall's load. Its blood keeps the age of the tank it came from and never wakes clot where it lands
 * (NQ-01-5), so two tanks and a courier cannot keep blood fresh.
 */
public final class ThrallCourierSource implements IBloodSourceContract {
    private double carriedBlood;
    private final double maxRatePerTick;
    private final long carriedAge;

    public ThrallCourierSource(double carriedBlood, double maxRatePerTick) {
        this(carriedBlood, maxRatePerTick, 0L);
    }

    public ThrallCourierSource(double carriedBlood, double maxRatePerTick, long carriedAge) {
        this.carriedBlood = Math.max(0, carriedBlood);
        this.maxRatePerTick = maxRatePerTick;
        this.carriedAge = Math.max(0L, carriedAge);
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
        if (target instanceof BlockBloodVolume block) {
            block.fillAged(amount, carriedAge);
        } else {
            target.fill(amount);
        }
        carriedBlood -= amount;
        return amount;
    }

    public double getCarriedBlood() {
        return carriedBlood;
    }
}
