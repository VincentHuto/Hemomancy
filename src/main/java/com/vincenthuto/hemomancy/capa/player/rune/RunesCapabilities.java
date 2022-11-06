package com.vincenthuto.hemomancy.capa.player.rune;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class RunesCapabilities {

	//@CapabilityInject(IRunesItemHandler.class)
	public static final Capability<IRunesItemHandler> RUNES = CapabilityManager.get(new CapabilityToken<IRunesItemHandler>() {});

	//@CapabilityInject(IRune.class)
	public static final Capability<IRune> ITEM_RUNE = CapabilityManager.get(new CapabilityToken<IRune>() {});
}