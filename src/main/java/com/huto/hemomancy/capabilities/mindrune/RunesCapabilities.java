package com.huto.hemomancy.capabilities.mindrune;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class RunesCapabilities {

    @CapabilityInject(IRunesItemHandler.class)
    public static final Capability<IRunesItemHandler> RUNES = null;

    @CapabilityInject(IRune.class)
    public static final Capability<IRune> ITEM_RUNE = null;
}