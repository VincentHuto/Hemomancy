package com.vincenthuto.hemomancy.common.capability.player.scar;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ScarsCapabilities {

	public static final Capability<IScarsItemHandler> SCARS = CapabilityManager.get(new CapabilityToken<IScarsItemHandler>() {});
	public static final Capability<IScar> ITEM_SCAR = CapabilityManager.get(new CapabilityToken<IScar>() {});
}