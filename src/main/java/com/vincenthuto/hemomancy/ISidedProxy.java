package com.vincenthuto.hemomancy;

import net.minecraft.world.entity.player.Player;

public interface ISidedProxy {

	
    void setFlySpeed(Player player, float speed);

    void setFlightEnabled(Player player, boolean enabled);

}
