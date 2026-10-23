package com.vincenthuto.hemomancy.gametest;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.shared.migration.PlayerDataMigrations;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.station.StationUpgradeCatalog;
import com.vincenthuto.hemomancy.common.station.UpgradeStation;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
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
					&& degree.hasWitnessedFungalRevelation(), "The root-key Apotheos choice was not migrated");
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

			assertSettled(helper, player);
			helper.succeed();
		}
	}

	/**
	 * Runs the login again, then reloads the save and clones it through a death: nothing may run or change. With the
	 * version reset to 0, every migration runs again over its own output and must still change nothing.
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

		CompoundTag reset = new CompoundTag();
		reset.putInt("Version", 0);
		HemoCapabilityAccess.dataVersion(player).deserializeNBT(player.registryAccess(), reset);
		helper.assertTrue(PlayerDataMigrations.migrate(player) == PlayerDataMigrations.migrations().size(),
				"Resetting the version did not rerun every migration");
		CompoundTag rerun = player.saveWithoutId(new CompoundTag());
		helper.assertTrue(migrated.equals(rerun), "A migration is not idempotent over its own output:\n"
				+ migrated + "\n" + rerun);
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
