package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.shared.cooldown.PlayerCooldowns;
import com.vincenthuto.hemomancy.common.capability.player.shared.migration.PlayerDataMigrations;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusPlayerProgress;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.recipe.ArmatureUpgradeRecipe;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.upgrade.UpgradeStation;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.PendingWhispers;
import com.vincenthuto.hemomancy.common.event.HarbingerAdvancementGranter;
import com.vincenthuto.hemomancy.common.encounter.ChamberVisitService;
import com.vincenthuto.hemomancy.common.worldgen.CircusDiscoveryProgress;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Old-format player saves through {@link PlayerDataMigrations} (step 12). Each fixture under
 * {@code /migration/} is the part of a player's saved NBT a migration reads, merged over a fresh player's save and
 * loaded. A login migrates it; a second login, a reload and a death clone change nothing more.
 */
@GameTestHolder("player_migration_validation")
@PrefixGameTestTemplate(false)
public final class PlayerMigrationGameTests {
	private static final String ATTACHMENTS = "neoforge:attachments";

	private PlayerMigrationGameTests() {
	}

	/** S-9: the login migrations that existed before the data version, from a save that predates all of them. */
	@GameTest(template = "empty", batch = "player_migrations")
	public static void preDataVersionSaveMigratesOnceAtLogin(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = load(helper, captured, "pre_data_version");
			helper.assertTrue(HemoCapabilityAccess.dataVersion(player).version() == 0,
					"A save without a data version must read 0");
			login(player);

			var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
			helper.assertTrue(degree.getArchonPath() == EnumArchonPath.APOTHEOS_PENDING
					&& degree.hasWitnessedFungalRevelation()
					&& !player.getPersistentData().contains(FungalGardenTravelHelper.ARCHON_CHOICE_KEY),
					"The root-key Apotheos choice was not migrated and cleared");
			var stations = HemoCapabilityAccess.stationUpgrades(player);
			helper.assertTrue(stations.legacyMigrated()
					&& stations.hasClaimed(UpgradeStation.ARMATURE, 1) && stations.hasClaimed(UpgradeStation.ARMATURE, 2)
					&& stations.hasUsed(UpgradeStation.ALEMBIC, StationUpgradeCatalog.DISTILL)
					&& stations.hasClaimed(UpgradeStation.ALEMBIC, 1),
					"The per-station claim records were not folded into station upgrades");
			CompoundTag saved = player.saveWithoutId(new CompoundTag());
			helper.assertTrue(!saved.getCompound(ATTACHMENTS).contains("hemomancy:advanced_brewing")
					&& !player.getPersistentData().contains("hemomancy.vicar_consecration_kit_claimed")
					&& !player.getPersistentData().contains("hemomancy.monolithic_cornerstone_claimed"),
					"The station migration left its old records behind");
			CompoundTag muscle = saved.getCompound(ATTACHMENTS).getCompound("hemomancy:muscle_memory");
			helper.assertTrue(!muscle.contains("Primed") && muscle.getCompound("Reserves").getInt("sanguine_fists") > 0,
					"Legacy Muscle Memory priming was not turned into a reserve: " + muscle);
			helper.assertTrue(player.getInventory().getItem(0).is(ItemInit.noctifly_agaric.get()),
					"A surviving fungal scar must be left alone");
			helper.assertTrue(HemoCapabilityAccess.dataVersion(player).version() == PlayerDataMigrations.LATEST,
					"The data version did not reach the latest migration");

			CompoundTag migrated = player.saveWithoutId(new CompoundTag());
			assertSettled(helper, player);
			helper.assertTrue(migrated.equals(player.saveWithoutId(new CompoundTag())),
					"Rerunning every migration over a fully migrated save changed it");
			helper.succeed();
		}
	}

	/** F-21: only the old root key runs (the save is at version 4); it becomes the path and is removed. */
	@GameTest(template = "empty", batch = "player_migrations")
	public static void archonRootKeyBecomesThePathOnce(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = load(helper, captured, "archon_choice_silence");
			var silentGate = new ArmatureUpgradeRecipe.PersistentDataGate(FungalGardenTravelHelper.ARCHON_CHOICE_KEY,
					FungalGardenTravelHelper.ARCHON_CHOICE_SILENCE);
			login(player);
			helper.assertTrue(silentGate.matches(player), "The Silent Archon armour gate, which names the old key, must pass through the path");
			var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
			helper.assertTrue(degree.getArchonPath() == EnumArchonPath.SILENT_ARCHON
					&& degree.hasWitnessedFungalRevelation()
					&& !player.getPersistentData().contains(FungalGardenTravelHelper.ARCHON_CHOICE_KEY),
					"The Silent root key was not migrated and cleared");
			assertSettled(helper, player);
			helper.succeed();
		}
	}

	/** F-21: a key left by the old dual-write never overrides the path the player now holds. */
	@GameTest(template = "empty", batch = "player_migrations")
	public static void archonPathOutranksALeftoverRootKey(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = load(helper, captured, "archon_choice_dual_write");
			login(player);
			helper.assertTrue(HemoCapabilityAccess.getInitiatoryDegree(player).getArchonPath()
					== EnumArchonPath.SILENT_PENDING
					&& !player.getPersistentData().contains(FungalGardenTravelHelper.ARCHON_CHOICE_KEY),
					"The leftover root key changed the path or was not cleared");
			assertSettled(helper, player);
			helper.succeed();
		}
	}

	/**
	 * S-6c: the circus root keys fold into the CircusProgress attachment and are removed; the live act timer, which is
	 * session state, stays a root key.
	 */
	@GameTest(template = "empty", batch = "player_migrations")
	public static void circusRootKeysMoveIntoTheAttachment(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = load(helper, captured, "circus_root_keys");
			login(player);
			helper.assertTrue(CircusDiscoveryProgress.hasDiscovered(player)
					&& CircusPlayerProgress.acclimation(player) == 240
					&& CircusPlayerProgress.route(player) == CircusRouteRules.Route.SUCCESSION
					&& !CircusPlayerProgress.canRepairRoute(player)
					&& CircusPlayerProgress.challenges(player) == 5,
					"Circus discovery, acclimation, route, repair or challenges were not migrated");
			helper.assertTrue(!CircusPlayerProgress.awardMilestone(player, "first_visit", 10)
					&& !CircusPlayerProgress.awardMilestone(player, "practical.scarlet_mummer", 10)
					&& CircusPlayerProgress.acclimation(player) == 240,
					"Migrated milestones were awarded again");
			var apprenticeship = CircusApprenticeshipProgress.state(player);
			helper.assertTrue(apprenticeship.getBoolean("taught.scarlet_mummer")
					&& apprenticeship.getInt("guest.pending") == 4 && apprenticeship.getLong("guest.day") == 12L,
					"The apprenticeship record was not migrated: " + apprenticeship);
			var data = player.getPersistentData();
			for (String key : data.getAllKeys()) {
				helper.assertTrue(!key.startsWith("hemomancy.circus_") || key.equals("hemomancy.circus_active_challenge"),
						"A circus root key was left behind: " + key);
			}
			helper.assertTrue(data.getInt("hemomancy.circus_active_challenge") == 2,
					"The live act timer is session state and must stay where it was");
			assertSettled(helper, player);
			helper.succeed();
		}
	}

	/**
	 * S-1c: the five earned Chamber access keys move into the ChamberAccess attachment and are removed; visit session
	 * keys stay root keys.
	 */
	@GameTest(template = "empty", batch = "player_migrations")
	public static void chamberAccessRootKeysMoveIntoTheAttachment(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = load(helper, captured, "chamber_root_keys");
			login(player);
			var access = ChamberVisitService.access(player);
			helper.assertTrue(access.chairBound() && access.attuned() && !access.dreamSeen()
					&& access.dreamAttempts() == 3 && access.guidedComplete(),
					"Chamber access was not migrated: " + access.serializeNBT(player.registryAccess()));
			var data = player.getPersistentData();
			for (String key : new String[] { "chair_bound", "attuned", "dream_seen", "dream_attempts", "guided_complete" }) {
				helper.assertTrue(!data.contains("hemomancy:chamber_visit_" + key), "Root key left behind: " + key);
			}
			helper.assertTrue(data.getInt("hemomancy:chamber_visit_exit_sleep_ticks") == 40,
					"Visit session keys must stay where they were");
			assertSettled(helper, player);
			helper.succeed();
		}
	}

	/** S-1c: a save that lost its chair binding but holds the advancement gets it back once, through the migration. */
	@GameTest(template = "empty", batch = "player_migrations")
	public static void chamberAdvancementRestoresLostBindingOnce(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = load(helper, captured, "pre_data_version");
			HarbingerAdvancementGranter.grantIfNotDone(player, HarbingerAdvancementGranter.ADV_WARP_CHAIR_BOUND);
			ChamberVisitService.access(player).setChairBound(false);
			login(player);
			helper.assertTrue(ChamberVisitService.isChairBound(player) && !ChamberVisitService.isAttuned(player),
					"The chair advancement did not restore a lost binding");
			ChamberVisitService.access(player).setChairBound(false);
			login(player);
			helper.assertTrue(!ChamberVisitService.isChairBound(player),
					"The advancement backfill ran again on a later login");
			helper.succeed();
		}
	}

	/** S-1c: the root-key whisper inbox moves into the PendingWhisperInbox attachment, entries unchanged and in order. */
	@GameTest(template = "empty", batch = "player_migrations")
	public static void pendingWhisperRootListMovesIntoTheAttachment(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = load(helper, captured, "pending_whispers_root");
			ListTag legacy = player.getPersistentData().getList("hemomancy:pending_whispers", Tag.TAG_COMPOUND).copy();
			login(player);
			helper.assertTrue(legacy.size() == 2 && PendingWhispers.entries(player).equals(legacy),
					"The whisper inbox was not moved intact: " + PendingWhispers.entries(player));

			// A tree the attachment already holds is not added twice; a new one is appended after it.
			try (CapturedPlayer both = CapturedPlayer.create(helper)) {
				ServerPlayer holder = load(helper, both, "pending_whispers_root");
				CompoundTag held = new CompoundTag();
				ListTag heldEntries = new ListTag();
				CompoundTag duplicate = legacy.getCompound(0).copy();
				duplicate.putUUID("id", java.util.UUID.randomUUID());
				heldEntries.add(duplicate);
				held.put("Entries", heldEntries);
				HemoCapabilityAccess.pendingWhispers(holder).deserializeNBT(holder.registryAccess(), held);
				login(holder);
				ListTag merged = PendingWhispers.entries(holder);
				helper.assertTrue(merged.size() == 2 && merged.getCompound(0).equals(duplicate)
						&& merged.getCompound(1).equals(legacy.getCompound(1)),
						"The inbox merge duplicated or reordered a whisper: " + merged);
			}
			helper.assertTrue(!player.getPersistentData().contains("hemomancy:pending_whispers"),
					"The root-key inbox was left behind");
			assertSettled(helper, player);
			helper.succeed();
		}
	}

	/**
	 * S-7: the cooldown root keys move into the cooldowns attachment and are removed; an effect window (Silent
	 * Slipping's duration) is not a cooldown and stays. The tier is kept: the cooldowns survive a reload and reset on a
	 * death clone and on an End-return clone, as the root keys did.
	 */
	@GameTest(template = "empty", batch = "player_migrations")
	public static void cooldownRootKeysKeepTheirTierInTheAttachment(GameTestHelper helper) {
		try (CapturedPlayer captured = CapturedPlayer.create(helper)) {
			ServerPlayer player = load(helper, captured, "cooldown_root_keys");
			login(player);
			var cooldowns = PlayerCooldowns.of(player);
			String[] ids = { PlayerCooldowns.ARMOR_SET_ABILITY_PREFIX + "hemomancy:edacious_bloodburst",
					PlayerCooldowns.SILENT_SLIPPING, PlayerCooldowns.SILENT_SLIPPING_SHARD_PULSE,
					PlayerCooldowns.PHANTASMAL_REACTIVE, PlayerCooldowns.SILENT_ARCHON_REFUSAL,
					PlayerCooldowns.PRISMATIC_FLASH, PlayerCooldowns.SCAR_PULSE, PlayerCooldowns.SCRIPTORIUM_BACKLASH };
			for (int i = 0; i < ids.length; i++) {
				helper.assertTrue(cooldowns.until(ids[i]) == 90000L + i, "Cooldown not migrated: " + ids[i]);
			}
			var data = player.getPersistentData();
			helper.assertTrue(data.size() == 1 && data.getLong("hemomancy:silent_slipping_until") == 90008L,
					"Cooldown root keys were left behind, or the effect window moved: " + data);
			assertSettled(helper, player);

			try (CapturedPlayer reloaded = CapturedPlayer.create(helper);
				 CapturedPlayer respawned = CapturedPlayer.create(helper);
				 CapturedPlayer returned = CapturedPlayer.create(helper)) {
				reloaded.player.load(player.saveWithoutId(new CompoundTag()));
				helper.assertTrue(PlayerCooldowns.of(reloaded.player).until(PlayerCooldowns.PRISMATIC_FLASH) == 90005L,
						"A persisted cooldown must survive a reload");
				respawned.player.restoreFrom(reloaded.player, false);
				returned.player.restoreFrom(reloaded.player, true);
				helper.assertTrue(PlayerCooldowns.of(respawned.player).until(PlayerCooldowns.PRISMATIC_FLASH) == 0L
						&& PlayerCooldowns.of(returned.player).until(PlayerCooldowns.PRISMATIC_FLASH) == 0L,
						"Persisted cooldowns must reset on death and End return, as the root keys did");
			}
			helper.succeed();
		}
	}

	/**
	 * Runs the login again, then reloads the save and clones it through a death: nothing may run or change. Then the
	 * version is reset to 0 twice: the second full rerun runs every migration over its own output and must change
	 * nothing.
	 */
	private static void assertSettled(GameTestHelper helper, ServerPlayer player) {
		CompoundTag migrated = player.saveWithoutId(new CompoundTag());
		helper.assertTrue(PlayerDataMigrations.migrate(player) == 0, "A second login ran a migration again");
		helper.assertTrue(migrated.equals(player.saveWithoutId(new CompoundTag())), "A second login changed the save");

		try (CapturedPlayer restored = CapturedPlayer.create(helper);
			 CapturedPlayer respawned = CapturedPlayer.create(helper)) {
			restored.player.load(migrated.copy());
			helper.assertTrue(PlayerDataMigrations.migrate(restored.player) == 0, "A reload ran a migration again");
			respawned.player.restoreFrom(restored.player, false);
			helper.assertTrue(HemoCapabilityAccess.dataVersion(respawned.player).version() == PlayerDataMigrations.LATEST
					&& PlayerDataMigrations.migrate(respawned.player) == 0,
					"A death clone lost the data version");
		}

		CompoundTag first = rerunFromZero(helper, player);
		CompoundTag second = rerunFromZero(helper, player);
		helper.assertTrue(first.equals(second), "A migration is not idempotent over its own output:\n"
				+ first + "\n" + second);
	}

	private static CompoundTag rerunFromZero(GameTestHelper helper, ServerPlayer player) {
		CompoundTag reset = new CompoundTag();
		reset.putInt("Version", 0);
		HemoCapabilityAccess.dataVersion(player).deserializeNBT(player.registryAccess(), reset);
		helper.assertTrue(PlayerDataMigrations.migrate(player) == PlayerDataMigrations.migrations().size(),
				"Resetting the version did not rerun every migration");
		return player.saveWithoutId(new CompoundTag());
	}

	static ServerPlayer load(GameTestHelper helper, CapturedPlayer captured, String fixture) {
		ServerPlayer player = captured.player;
		CompoundTag saved = player.saveWithoutId(new CompoundTag());
		saved.merge(readFixture(fixture));
		player.load(saved);
		return player;
	}

	static void login(ServerPlayer player) {
		PlayerDataMigrations.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
	}

	private static CompoundTag readFixture(String fixture) {
		String path = "/migration/" + fixture + ".snbt";
		try (InputStream in = PlayerMigrationGameTests.class.getResourceAsStream(path)) {
			if (in == null) throw new IllegalStateException("missing fixture " + path);
			return TagParser.parseTag(new String(in.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException | com.mojang.brigadier.exceptions.CommandSyntaxException e) {
			throw new IllegalStateException("unreadable fixture " + path, e);
		}
	}
}
