package com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.routing;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;

public interface IBloodSourceContract {

    boolean isValid();

    double getMaxRatePerTick();

    double transferTo(IBloodVolume target, double requested);
}
