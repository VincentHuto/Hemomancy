package com.vincenthuto.hemomancy.common.capability.player.scar;

import net.neoforged.neoforge.capabilities.Capability;
import net.neoforged.neoforge.capabilities.CapabilityManager;
import net.neoforged.neoforge.capabilities.CapabilityToken;

public class ScarsCapabilities {

	public static final Capability<IScarsItemHandler> SCARS = CapabilityManager.get(new CapabilityToken<IScarsItemHandler>() {});
	public static final Capability<IScar> ITEM_SCAR = CapabilityManager.get(new CapabilityToken<IScar>() {});
}