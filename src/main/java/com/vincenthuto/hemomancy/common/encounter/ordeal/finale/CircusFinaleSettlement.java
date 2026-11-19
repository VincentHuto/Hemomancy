package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionSavedData;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusSiteStateService;
import com.vincenthuto.hemomancy.common.entity.summon.puppeteer.PuppeteerSummonDefinitions;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.summon.KnownSummonEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

/**
 * The finale's ending laid on the real site once the player is home (2026-11-18): killing him at the kneel ruins the
 * pavilion and frees the troupe (Liberation); absorbing him takes his place (Succession), with the banner and the
 * Ringmaster Pattern. What happens to the real Ringmaster and his troupe is his own ({@link
 * CircusRingmasterEntity#settleFromSite}, read from the site whenever he ticks).
 */
public final class CircusFinaleSettlement {
	private CircusFinaleSettlement() {
	}

	public static CircusRouteRules.Route routeFor(Ending ending) {
		return ending == Ending.ABSORBED ? CircusRouteRules.Route.SUCCESSION_COMPLETE
				: CircusRouteRules.Route.LIBERATION_COMPLETE;
	}

	/** Settles the site at {@code origin} for {@code player}; loads its chunk if it is not loaded. */
	public static void apply(ServerLevel siteLevel, BlockPos origin, ServerPlayer player, Ending ending) {
		CircusPavilionStateRules.Outcome outcome = ending == Ending.ABSORBED
				? CircusPavilionStateRules.Outcome.SUCCESSION : CircusPavilionStateRules.Outcome.RUIN;
		if (!CircusPavilionSavedData.get(siteLevel).complete(siteLevel, origin, player.getUUID(), outcome)) return;
		CircusPlayerProgress.completeRoute(player, routeFor(ending));
		if (ending == Ending.ABSORBED) {
			PuppeteerSummonDefinitions.byName(PuppeteerSummonDefinitions.RINGMASTER_PATTERN)
					.ifPresent(definition -> KnownSummonEvents.grantSummon(player, definition));
			CircusSiteStateService.begin(siteLevel, origin, CircusSiteStateService.State.TENANCY,
					player.getGameProfile().getName());
		} else {
			CircusRingmasterEntity.grantLiberationMemory(player);
			CircusSiteStateService.begin(siteLevel, origin, CircusSiteStateService.State.RUIN, null);
		}
		CircusPlayerProgress.oweTopper(player);
		player.displayClientMessage(net.minecraft.network.chat.Component.translatable("hemomancy.circus.finale.complete."
				+ (ending == Ending.KILLED ? "liberation" : "succession")).withStyle(net.minecraft.ChatFormatting.DARK_RED), false);
		// The real cast reads the outcome from the site when it next ticks (it is usually unloaded now); those loaded take
		// it at once.
		for (CircusRingmasterEntity real : siteLevel.getEntitiesOfClass(CircusRingmasterEntity.class,
				new AABB(origin).inflate(48.0D), ringmaster -> !ringmaster.stageCopy()))
			real.settleFromSite(outcome);
	}
}
