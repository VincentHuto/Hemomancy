package com.vincenthuto.hemomancy.mixin.util;

import com.vincenthuto.hemomancy.common.init.AttributeInit;
import com.vincenthuto.hemomancy.common.init.AttributeInit.TriState;
import com.vincenthuto.hemomancy.common.network.PacketHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

public class ClientMixinHooks {
	public static boolean checkFlight() {
		LocalPlayer playerEntity = Minecraft.getInstance().player;

		if (playerEntity != null) {
			TriState flag = AttributeInit.canFallFly(playerEntity);

			if (flag == TriState.DENY) {
				return false;
			}

			if (!playerEntity.onGround() && !playerEntity.isFallFlying() && !playerEntity.isInWater()
					&& !playerEntity.hasEffect(MobEffects.LEVITATION) && flag == TriState.ALLOW) {
				playerEntity.startFallFlying();
				PacketHandler.sendClientElytraPacket();

			}
		}
		return true;
	}

}
