package com.vincenthuto.hemomancy.common.capability.player.rune;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class RunesCapabilities {

	public static final Capability<IRunesItemHandler> RUNES = CapabilityManager.get(new CapabilityToken<IRunesItemHandler>() {});
	public static final Capability<IRune> ITEM_RUNE = CapabilityManager.get(new CapabilityToken<IRune>() {});
}