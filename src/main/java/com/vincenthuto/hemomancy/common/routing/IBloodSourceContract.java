package com.vincenthuto.hemomancy.common.routing;

import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;

public interface IBloodSourceContract {

    boolean isValid();

    double getMaxRatePerTick();

    double transferTo(IBloodVolume target, double requested);
}
