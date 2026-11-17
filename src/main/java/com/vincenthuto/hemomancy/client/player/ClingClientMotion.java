package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingState;
import net.minecraft.client.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Moves the local player while Venous Cling holds them (core-loop Track 9): their own client moves them, so the
 * walking input is laid onto the face here ({@link ClingRules#crawl}) and taken out of the input, which leaves vanilla's
 * travel nothing to add, and the velocity is set fresh each tick, so gravity never builds. Jump pushes off and lets go
 * at once on this side; the server lets go when it sees the body leave the face.
 */
public final class ClingClientMotion {
	private ClingClientMotion() {
	}

	/** Called with the movement input each client tick, before travel. */
	public static void apply(Player player, Input input) {
		ClingState cling = HemoCapabilityAccess.getCling(player);
		if (!cling.isClinging()) return;
		if (input.jumping) {
			player.setDeltaMovement(ClingRules.jumpOff(cling.face()));
			cling.clear();
			return;
		}
		Vec3 intent = ClingRules.intent(input.forwardImpulse, input.leftImpulse, player.getYRot());
		double speed = ClingRules.speed(cling.surface(), cling.cleats());
		player.setDeltaMovement(ClingRules.crawl(cling.face(), intent, speed));
		player.resetFallDistance();
		input.forwardImpulse = 0.0F;
		input.leftImpulse = 0.0F;
		input.up = false;
		input.down = false;
		input.left = false;
		input.right = false;
	}
}
