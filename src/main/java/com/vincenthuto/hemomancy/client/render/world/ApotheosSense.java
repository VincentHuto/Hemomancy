package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.entity.decoration.ArmorStand;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.RootedStateHelper;
import com.vincenthuto.hemomancy.common.event.ApotheosBodyEvents;
import com.vincenthuto.hemomancy.common.world.FungalGardensPlaces;
import com.vincenthuto.hemomancy.common.world.GardensNetworkRules;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** What an Apotheos feels through the Gardens' ground: every body standing on the network nearby, outlined. */
public final class ApotheosSense {
	private ApotheosSense() {}

	public static boolean sees(Entity entity) {
		Player player = Minecraft.getInstance().player;
		// Cheap gates first: this runs for every rendered entity on every client, every frame.
		if (player == null || entity == player || !(entity instanceof LivingEntity) || entity instanceof ArmorStand
				|| !FungalGardensPlaces.inDimension(player.level()) || !ApotheosBodyEvents.isApotheos(player)) return false;
		boolean onNetwork = entity.onGround()
				&& entity.level().getBlockState(entity.blockPosition().below()).is(RootedStateHelper.FUNGAL_NETWORK_GROUND);
		return GardensNetworkRules.senses(ApotheosBodyEvents.isApotheos(player),
				FungalGardensPlaces.inDimension(player.level()), onNetwork, entity.distanceToSqr(player));
	}
}
