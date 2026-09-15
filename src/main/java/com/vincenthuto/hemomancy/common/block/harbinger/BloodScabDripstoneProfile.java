package com.vincenthuto.hemomancy.common.block.harbinger;

import net.minecraft.world.level.block.state.properties.DripstoneThickness;

final class BloodScabDripstoneProfile {
    private BloodScabDripstoneProfile() {}

    static DripstoneThickness connectedThickness(boolean sameAhead,boolean sameBehind,
                                                  DripstoneThickness aheadThickness) {
        if(!sameAhead)return DripstoneThickness.TIP;
        if(aheadThickness==DripstoneThickness.TIP || aheadThickness==DripstoneThickness.TIP_MERGE)
            return DripstoneThickness.FRUSTUM;
        return sameBehind?DripstoneThickness.MIDDLE:DripstoneThickness.BASE;
    }
}
