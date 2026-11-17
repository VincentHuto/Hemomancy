package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingRules;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingService;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.cling.ClingSurface;
import com.vincenthuto.hemomancy.common.event.MausoleumEvents;
import com.vincenthuto.hemomancy.common.manipulation.verb.SculptingPulse;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbSculpting;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTarget;
import com.vincenthuto.hemomancy.common.manipulation.verb.VerbTargetKind;
import com.vincenthuto.hemomancy.common.network.mausoleum.WardStrikePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

/**
 * Venous Cling as a Projection Sculpting (core-loop Track 9): Projection into a wall or ceiling the player is touching
 * roots them to it, veins from hands and feet into the face. One pulse roots; {@link ClingService} keeps it, for blood
 * each second, until it lets go. It is a Sculpting, not plain Projection, so plain Projection at a wall still stains it.
 *
 * <p>While the player clings, the Sculpting keeps the claim on any block and pays nothing, so holding Projection does not
 * pour blood into the wall they hang from. A Saint's ward refuses the root, with the ward's mark.
 */
public final class VenousClingSculpting implements VerbSculpting {
	@Override
	public Set<VerbTargetKind> wants() {
		return Set.of(VerbTargetKind.BLOCK);
	}

	@Override
	public boolean claims(Player player, VerbTarget target) {
		if (!(target instanceof VerbTarget.Block block)) return false;
		if (HemoCapabilityAccess.getCling(player).isClinging()) return true;
		return ClingRules.Surface.forFace(block.hit().getDirection()) != ClingRules.Surface.NONE
				&& ClingSurface.touching(player, block.pos(), block.hit().getDirection());
	}

	@Override
	public boolean admits(ServerPlayer player, VerbTarget target) {
		if (HemoCapabilityAccess.getCling(player).isClinging() || !(target instanceof VerbTarget.Block block)) return false;
		if (MausoleumEvents.hallowed(player.serverLevel(), block.pos())) {
			player.displayClientMessage(Component.translatable("message.hemomancy.venous_cling.refused.ward")
					.withStyle(ChatFormatting.GRAY), true);
			MausoleumEvents.strike(player.serverLevel(), player, block.pos(), block.hit().getDirection(),
					WardStrikePacket.Kind.BREAK);
			return false;
		}
		return true;
	}

	@Override
	public int cadence() {
		return ClingRules.ROOT_CADENCE;
	}

	@Override
	public double bloodPerPulse() {
		return ClingRules.ROOT_BLOOD;
	}

	@Override
	public boolean presentsCast() {
		// The veins into the face are the presentation.
		return false;
	}

	@Override
	public void pulse(ServerPlayer player, VerbTarget target, SculptingPulse pulse) {
		if (target instanceof VerbTarget.Block block) ClingService.root(player, block.pos(), block.hit().getDirection());
	}
}
