package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantDamageTypeResourceTest {
	private static final Path DATA = Path.of("src/main/resources/data");

	@Test
	void claimDamageIsNeverScaledByDifficulty() throws Exception {
		String claim = Files.readString(DATA.resolve("hemomancy/damage_type/mycophant_claim.json"));
		assertTrue(claim.contains("\"scaling\": \"never\""));
	}

	@Test
	void theRefusalBonusBypassesTheHurtCooldown() throws Exception {
		String refusal = Files.readString(DATA.resolve("hemomancy/damage_type/mycophant_refusal.json"));
		assertTrue(refusal.contains("\"scaling\": \"never\""));
		assertTrue(Files.readString(DATA.resolve("minecraft/tags/damage_type/bypasses_cooldown.json")).contains("hemomancy:mycophant_refusal"));
	}

	@Test
	void theRefusalBonusIsAFullEightPercentPastHerArmor() throws Exception {
		// her armor 20 would otherwise shave the 8%-of-max bonus to about 6.7%
		assertTrue(Files.readString(DATA.resolve("minecraft/tags/damage_type/bypasses_armor.json")).contains("hemomancy:mycophant_refusal"));
	}
}
