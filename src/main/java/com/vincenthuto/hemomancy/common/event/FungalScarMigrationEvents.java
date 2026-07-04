package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.FungalScarMigrationRules;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketSyncScarsState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class FungalScarMigrationEvents {

	private FungalScarMigrationEvents() {
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		migratePlayer(event.getEntity());
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.tickCount % 100 == 0) {
			migratePlayer(player);
		}
	}

	private static void migratePlayer(Player player) {
		if (player.level().isClientSide) {
			return;
		}
		boolean[] changed = { false };
		HemoCapabilityAccess.getScarState(player).ifPresent(scars -> {
			ItemStack current = scars.getFungalScar();
			if (!ItemStack.isSameItemSameComponents(current, FungalScarMigrationRules.migrate(current))) {
				scars.setFungalScar(FungalScarMigrationRules.migrate(current));
				changed[0] = true;
			}
		});

		Inventory inventory = player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack current = inventory.getItem(i);
			ItemStack migrated = FungalScarMigrationRules.migrate(current);
			if (!ItemStack.isSameItemSameComponents(current, migrated)) {
				inventory.setItem(i, migrated);
				changed[0] = true;
			}
		}

		if (changed[0] && player instanceof ServerPlayer serverPlayer) {
			HemoCapabilityAccess.getScarState(serverPlayer)
					.ifPresent(scars -> PacketHandler.sendToPlayer(serverPlayer,
							new PacketSyncScarsState(serverPlayer, scars)));
		}
	}
}
