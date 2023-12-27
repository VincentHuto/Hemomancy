package com.vincenthuto.hemomancy;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ServerProxy implements ISidedProxy {

	@Override
	public void setFlySpeed(Player player, float speed) {
	}

	@Override
	public void setFlightEnabled(Player player, boolean enabled) {
		if (player.getAbilities().mayfly != enabled) {
			if (enabled) {
				player.getAbilities().mayfly = true;
			} else {
				boolean creative = player.isCreative() || player.isSpectator();
				player.getAbilities().mayfly = creative;
				if (!creative) {
					player.getAbilities().flying = false;
				}
			}

			if (player instanceof ServerPlayer) {
				((ServerPlayer) player).onUpdateAbilities();
			}

		}
	}

}