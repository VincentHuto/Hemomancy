package com.vincenthuto.hemomancy.common.capability.player.shared.migration;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * Every one-shot rewrite of a player's old saved data, in the order it was added. At login each entry newer than the
 * player's {@link PlayerDataVersion} runs once and is recorded, so a migration never runs twice for the same save.
 *
 * <p>Add a migration as a new last entry with the next version; never reorder or renumber. Each entry names the date
 * after which its read path may be deleted (keep it for at least one release). Deleting an entry leaves its version
 * number unused.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public final class PlayerDataMigrations {
	/**
	 * @param version the player's data version once this has run; strictly increasing down the table
	 * @param sunset  ISO date after which the old read path may be removed
	 */
	public record Migration(int version, String id, String sunset, Consumer<ServerPlayer> apply) {
	}

	private static final List<Migration> MIGRATIONS = List.of();

	/** Versions 1 to 11 protected only dev worlds and were deleted before release; the next migration is 12. */
	public static final int LATEST = 11;

	private PlayerDataMigrations() {
	}

	public static List<Migration> migrations() {
		return MIGRATIONS;
	}

	// HIGHEST: every other login handler, and the LOWEST PlayerStateSync send, sees migrated state.
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			migrate(player);
		}
	}

	/** Runs each migration newer than the player's data version, in order, recording each one. Returns how many ran. */
	public static int migrate(ServerPlayer player) {
		PlayerDataVersion data = HemoCapabilityAccess.dataVersion(player);
		int from = data.version();
		int ran = 0;
		for (Migration migration : MIGRATIONS) {
			if (migration.version() <= data.version()) continue;
			migration.apply().accept(player);
			data.setVersion(migration.version());
			ran++;
		}
		if (ran > 0) Hemomancy.LOGGER.info("Migrated {}'s saved data from version {} to {} ({} migrations)",
				player.getGameProfile().getName(), from, data.version(), ran);
		return ran;
	}
}
