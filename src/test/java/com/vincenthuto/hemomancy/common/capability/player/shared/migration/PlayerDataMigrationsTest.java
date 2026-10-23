package com.vincenthuto.hemomancy.common.capability.player.shared.migration;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** S-9: the migration table's ordering and sunset rules, and the data version's saved form. */
class PlayerDataMigrationsTest {
	/** Read paths stay for at least one release after step 12 (2026-10-23). */
	private static final LocalDate EARLIEST_SUNSET = LocalDate.of(2027, 4, 23);

	@Test
	void versionsRiseStrictlyAndIdsAreUnique() {
		int previous = 0;
		Set<String> ids = new HashSet<>();
		for (PlayerDataMigrations.Migration migration : PlayerDataMigrations.migrations()) {
			assertTrue(migration.version() > previous, migration.id() + " must come after version " + previous);
			assertTrue(ids.add(migration.id()), "duplicate migration id " + migration.id());
			previous = migration.version();
		}
		assertEquals(previous, PlayerDataMigrations.LATEST);
	}

	@Test
	void everyMigrationRecordsASunsetAtLeastOneReleaseOut() {
		for (PlayerDataMigrations.Migration migration : PlayerDataMigrations.migrations()) {
			LocalDate sunset = LocalDate.parse(migration.sunset());
			assertFalse(sunset.isBefore(EARLIEST_SUNSET), migration.id() + " sunsets before " + EARLIEST_SUNSET);
		}
	}

	@Test
	void dataVersionDefaultsToZeroAndRoundTrips() {
		PlayerDataVersion fresh = new PlayerDataVersion();
		assertEquals(0, fresh.version());
		fresh.deserializeNBT(null, new CompoundTag());
		assertEquals(0, fresh.version());

		fresh.setVersion(PlayerDataMigrations.LATEST);
		CompoundTag saved = fresh.serializeNBT(null);
		assertEquals(PlayerDataMigrations.LATEST, saved.getInt("Version"));
		PlayerDataVersion restored = new PlayerDataVersion();
		restored.deserializeNBT(null, saved);
		assertEquals(PlayerDataMigrations.LATEST, restored.version());
	}
}
