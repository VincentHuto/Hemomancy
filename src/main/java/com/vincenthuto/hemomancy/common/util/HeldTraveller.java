package com.vincenthuto.hemomancy.common.util;

import com.vincenthuto.hemomancy.common.block.shared.LidlessPassageService;
import com.vincenthuto.hemomancy.common.manipulation.ductilis.AxonalTransductionManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * A player a travel system holds in place: an Axonal Transduction traveller or a player crossing a Lidless Door.
 * The mixins and listeners that stop a held player from moving, being pushed, collecting, dropping or clicking ask
 * {@link #isHeld} and nothing else, so every hold reaches all of them.
 */
public final class HeldTraveller {
	private HeldTraveller() {
	}

	public static boolean isHeld(Player player) {
		return AxonalTransductionManager.isTraveling(player) || LidlessPassageService.isCrossing(player);
	}

	/** Pins the body this tick: no physics, no gravity, no velocity, no fall. */
	public static void hold(Player player) {
		player.noPhysics = true;
		player.setNoGravity(true);
		player.setDeltaMovement(Vec3.ZERO);
		player.resetFallDistance();
	}
}
