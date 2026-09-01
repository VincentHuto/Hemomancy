package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.FungalWhisperDialogueTrees;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.PacketSyncFungalProjection;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import com.vincenthuto.hemomancy.common.worldgen.FungalGardenTravelHelper;
import com.vincenthuto.hemomancy.common.worldgen.FungalProjectionRules;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class FungalProjectionEvents {
	private FungalProjectionEvents() {}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer player)
				|| !FungalGardenTravelHelper.isProjectionActive(player)) return;

		if (!player.level().dimension().equals(FungalGardenTravelHelper.FUNGAL_GARDENS)) {
			FungalGardenTravelHelper.performForcedProjectionReturn(player);
			return;
		}

		if (player.containerMenu != player.inventoryMenu) player.closeContainer();
		int remaining = FungalGardenTravelHelper.getProjectionRemainingTicks(player) - 1;
		player.getPersistentData().putInt(FungalGardenTravelHelper.PROJECTION_REMAINING, remaining);
		if (FungalProjectionRules.shouldForceReturn(remaining)) {
			FungalGardenTravelHelper.performForcedProjectionReturn(player);
		} else if (FungalProjectionRules.shouldSync(remaining)) {
			PacketHandler.sendToPlayer(player, new PacketSyncFungalProjection(true, remaining,
					FungalProjectionRules.FIRST_VISIT_TICKS));
		}
	}

	@SubscribeEvent
	public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && active(player)) {
			FungalGardenTravelHelper.performForcedProjectionReturn(player);
		}
	}

	@SubscribeEvent
	public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player
				&& player.getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING)) {
			PacketHandler.sendToPlayer(player, new OpenDialoguePacket(FungalWhisperDialogueTrees.coreWitnessDialogue()));
		}
	}

	@SubscribeEvent
	public static void onIncomingDamage(LivingIncomingDamageEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && active(player)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onAttack(AttackEntityEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && active(player)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
		if (event.getEntity() instanceof ServerPlayer player && active(player)) event.setCanceled(true);
	}
	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		if (event.getEntity() instanceof ServerPlayer player && active(player)) event.setCanceled(true);
	}
	@SubscribeEvent
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (event.getEntity() instanceof ServerPlayer player && active(player)) event.setCanceled(true);
	}
	@SubscribeEvent
	public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getEntity() instanceof ServerPlayer player && active(player)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onBreakBlock(BlockEvent.BreakEvent event) {
		if (event.getPlayer() instanceof ServerPlayer player && active(player)) event.setCanceled(true);
	}

	private static boolean active(ServerPlayer player) {
		return FungalGardenTravelHelper.isProjectionActive(player);
	}
}
