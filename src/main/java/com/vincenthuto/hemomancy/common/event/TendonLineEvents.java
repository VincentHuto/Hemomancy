package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendon.TendonAnchor;
import com.vincenthuto.hemomancy.common.manipulation.animus.TendonLineSculpting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Sneaking and using the block a player's Tendon Line is rooted in lets the line go. */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class TendonLineEvents {
	private TendonLineEvents() {
	}

	@SubscribeEvent
	public static void letGo(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		if (event.getHand() != InteractionHand.MAIN_HAND || !player.isShiftKeyDown()) return;
		TendonAnchor anchor = HemoCapabilityAccess.getTendonAnchor(player);
		if (!anchor.isSetIn(event.getLevel().dimension()) || !anchor.pos().equals(event.getPos())) return;
		// Both sides cancel, so the client does not predict a placement on the rooted block.
		if (player instanceof ServerPlayer serverPlayer) {
			TendonLineSculpting.loosen(serverPlayer, "message.hemomancy.tendon_line.let_go");
		}
		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}
}
