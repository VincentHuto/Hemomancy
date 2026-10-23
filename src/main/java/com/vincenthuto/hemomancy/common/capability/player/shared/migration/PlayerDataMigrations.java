package com.vincenthuto.hemomancy.common.capability.player.shared.migration;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.scar.fungal.FungalScarMigrationRules;
import com.vincenthuto.hemomancy.common.capability.player.shared.cooldown.PlayerCooldowns;
import com.vincenthuto.hemomancy.common.capability.player.shared.knowledge.LiberKnowledgeEvents;
import com.vincenthuto.hemomancy.common.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers;
import com.vincenthuto.hemomancy.common.station.StationUpgradeMigration;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;
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

	private static final List<Migration> MIGRATIONS = List.of(
			// Five retired fungal scar ids, carried in the inventory or the scar slot.
			new Migration(1, "fungal_scar_items", "2027-04-23", FungalScarMigrationRules::migrateCarried),
			// Per-station claim records (Advanced Brewing and Resonant Forge attachments, Armature root flags).
			new Migration(2, "station_upgrade_claims", "2027-04-23", StationUpgradeMigration::migrate),
			// Muscle Memory's old "Primed" map, read at load and turned into reserves here.
			new Migration(3, "muscle_memory_priming", "2027-04-23",
					player -> HemoCapabilityAccess.muscleMemory(player).migrateLegacyPriming(player.level().getGameTime())),
			// Field-note items and the Scar Practice entry earned before its advancement unlock existed.
			new Migration(4, "liber_legacy_notes", "2027-04-23", LiberKnowledgeEvents::migrateLegacyNotes),
			// The Archon choice once kept only as a root key.
			new Migration(5, "archon_choice", "2027-04-23", InitiatoryDegreeEvents::migrateLegacyArchonChoice),
			// Circus discovery, acclimation, route, challenges, milestones and apprenticeship root keys (S-6c).
			new Migration(6, "circus_progress", "2027-04-23", CircusPlayerProgress::migrateLegacyRootKeys),
			// Chamber of Will earned-access root keys, then the advancement backfill once (S-1c).
			new Migration(7, "chamber_access", "2027-04-23", ChamberVisitService::migrateLegacyAccess),
			// The pending whisper inbox root list (S-1c).
			new Migration(8, "pending_whispers", "2027-04-23", PendingWhispers::migrateLegacyInbox),
			// Armour, Antecedent scar and Scriptorium cooldown root keys (S-7).
			new Migration(9, "persisted_cooldowns", "2027-04-23", PlayerCooldowns::migrateLegacyRootKeys));

	public static final int LATEST = MIGRATIONS.getLast().version();

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
