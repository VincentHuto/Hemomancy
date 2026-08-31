package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperTendencyDefenseIntegrationSourceTest {
	@Test
	void sharedAffinityPathAppliesTheActiveVesperTendencyToWeaponsAndManipulations() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/TendencyAffinityRules.java"));

		assertTrue(source.contains("target instanceof VesperTheEveningStarEntity vesper"));
		assertTrue(source.contains("VesperTendencyDefenseRules.damageMultiplier("));
	}

	@Test
	void weaponDamageEventAppliesResistanceAsWellAsWeakness() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/event/TendencyWeaponCombatEvents.java"));

		assertTrue(source.contains("if (multiplier == 1.0f)"));
		assertTrue(source.contains("event.setAmount(event.getAmount() * multiplier)"));
	}

	@Test
	void animusProjectilesAndBloodCloudCannotBypassTheDefense() throws IOException {
		String eventSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/event/TendencyWeaponCombatEvents.java"));
		String shotSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodShotManip.java"));
		String needleSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodNeedleManip.java"));
		String cloudSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/CloudEntityBlood.java"));

		assertTrue(eventSource.contains("directEntity instanceof TendencyDamageCarrier carrier"));
		assertTrue(shotSource.contains("shot.setDamageTendency(getTend())"));
		assertTrue(needleSource.contains("needle.setDamageTendency(getTend())"));
		assertTrue(cloudSource.contains("TendencyAffinityRules.damageMultiplier(player, ent,"));
	}

	@Test
	void mixedProjectileManipulationsKeepTheirSecondaryTendency() throws IOException {
		String carrierSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/TendencyDamageCarrier.java"));
		String eventSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/event/TendencyWeaponCombatEvents.java"));
		String needleSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodNeedleManip.java"));

		assertTrue(carrierSource.contains("getSecondaryDamageTendency()"));
		assertTrue(eventSource.contains("carrier.getSecondaryDamageTendency()"));
		assertTrue(needleSource.contains("needle.setSecondaryDamageTendency(getSecondaryTend())"));
	}

	@Test
	void cloudAffinityAndOwnerSurviveReload() throws IOException {
		String carrierSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/BloodCloudCarrierEntity.java"));
		String cloudSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/CloudEntityBlood.java"));

		assertTrue(carrierSource.contains("cloud.setDamageTendencies(damageTendency, secondaryDamageTendency)"));
		assertTrue(cloudSource.contains("tag.putUUID(\"Creator\", creatorId)"));
		assertTrue(cloudSource.contains("tag.putString(\"SecondaryDamageTendency\""));
		assertTrue(cloudSource.contains("server.getEntity(creatorId)"));
	}

	@Test
	void activationPotentialDoesNotReenterTheWeaponDamageEvent() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/ActivationPotentialManip.java"));

		assertTrue(source.contains("target.hurt(player.damageSources().magic(), adjusted)"));
	}
}
