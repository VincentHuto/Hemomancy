package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.Bloodline;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;

public final class BloodlineSource implements IBloodSourceContract {
    private final Bloodline bloodline;
    private final double maxRatePerTick;

    public BloodlineSource(Bloodline bloodline, double maxRatePerTick) {
        this.bloodline = bloodline;
        this.maxRatePerTick = maxRatePerTick;
    }

    @Override
    public boolean isValid() {
        return bloodline != null && bloodline.isValid() && bloodline.getBloodVolume() > 0 && maxRatePerTick > 0;
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
        amount = Math.min(amount, bloodline.getBloodVolume());
        amount = Math.min(amount, BloodRoutingRules.freeSpace(target));
        if (amount <= 0) {
            return 0;
        }
        float drawn = bloodline.drawBlood((float) amount);
        if (drawn > 0) {
            target.fill(drawn);
        }
        return drawn;
    }
}
