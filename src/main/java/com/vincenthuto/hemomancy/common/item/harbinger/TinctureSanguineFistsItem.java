package com.vincenthuto.hemomancy.common.item.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemory;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory.MuscleMemoryPrimingRules;
import com.vincenthuto.hutoslib.common.registry.HLItemInit;

public class TinctureSanguineFistsItem extends MuscleMemoryTinctureItem {
    public TinctureSanguineFistsItem(Properties properties) {
        super(properties, MuscleMemory.SANGUINE_FISTS, MuscleMemoryPrimingRules.FLASK_DOSES,
                () -> HLItemInit.cured_clay_flask.get());
    }
}
