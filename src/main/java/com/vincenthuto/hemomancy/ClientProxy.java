package com.vincenthuto.hemomancy;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ClientProxy implements ISidedProxy, SoundEventListener {

	
	@Override
    public void setFlySpeed(Player player, float speed) {
        player.getAbilities().setFlyingSpeed(speed);
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
                } else {
                    player.getAbilities().setFlyingSpeed(0.05F);
                }
            }

            if (player instanceof ServerPlayer) {
                ((ServerPlayer)player).onUpdateAbilities();
            }

        }
    }
	
	@Override
	public void onPlaySound(SoundInstance inst, WeighedSoundEvents weight) {

	}


}
