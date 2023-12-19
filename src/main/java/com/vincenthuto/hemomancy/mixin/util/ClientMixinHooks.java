
package com.vincenthuto.hemomancy.mixin.util;

import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class ClientMixinHooks {

	public static void checkFlight() {
		LocalPlayer playerEntity = Minecraft.getInstance().player;

		if (playerEntity != null && MixinHooks.startFlight(playerEntity)) {
			PacketHandler.sendFlightC2S();
		}
	}

}
