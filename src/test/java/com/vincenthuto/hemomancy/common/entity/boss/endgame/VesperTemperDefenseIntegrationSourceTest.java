package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperTemperDefenseIntegrationSourceTest {
	@Test
	void sharedAffinityPathAppliesTheActiveVesperTemperToWeaponsAndManipulations() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/TemperAffinityRules.java"));

		assertTrue(source.contains("target instanceof VesperTheEveningStarEntity vesper"));
		assertTrue(source.contains("VesperTemperDefenseRules.damageMultiplier("));
	}

	@Test
	void weaponDamageEventAppliesResistanceAsWellAsWeakness() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java"));

		assertTrue(source.contains("TemperAffinityRules.damageMultiplier(player, target, hit.primary(), hit.secondary())"));
		assertTrue(source.contains("event.setAmount(damage)"));
	}

	@Test
	void animusProjectilesAndBloodCloudCannotBypassTheDefense() throws IOException {
		String eventSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolDamage.java"));
		String shotSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodShotManip.java"));
		String needleSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/BloodNeedleManip.java"));
		String cloudSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/CloudEntityBlood.java"));

		assertTrue(eventSource.contains("projectile instanceof TemperDamageCarrier carrier"));
		assertTrue(shotSource.contains("shot.setDamageTemper(getTend())"));
		assertTrue(needleSource.contains("needle.setDamageTemper(getTend())"));
		assertTrue(cloudSource.contains("SchoolHitContext.Kind.PERIODIC"));
	}

	@Test
	void mixedProjectileManipulationsKeepTheirSecondaryTemper() throws IOException {
		String carrierSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/TemperDamageCarrier.java"));
		String eventSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolDamage.java"));
		String needleSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/ferric/BloodNeedleManip.java"));

		assertTrue(carrierSource.contains("getSecondaryDamageTemper()"));
		assertTrue(eventSource.contains("carrier.getSecondaryDamageTemper()"));
		assertTrue(needleSource.contains("needle.setSecondaryDamageTemper(getSecondaryTend())"));
	}

	@Test
	void cloudAffinityAndOwnerSurviveReload() throws IOException {
		String carrierSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/BloodCloudCarrierEntity.java"));
		String cloudSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/CloudEntityBlood.java"));

		assertTrue(carrierSource.contains("cloud.setDamageTempers(damageTemper, secondaryDamageTemper)"));
		assertTrue(cloudSource.contains("tag.putUUID(\"Creator\", creatorId)"));
		assertTrue(cloudSource.contains("tag.putString(\"SecondaryDamageTemper\""));
		assertTrue(cloudSource.contains("server.getEntity(creatorId)"));
	}

	@Test
	void activationPotentialDoesNotReenterTheWeaponDamageEvent() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/ductilis/ActivationPotentialManip.java"));

        String helper = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationCombatHelper.java"));

        assertTrue(source.contains("ManipulationCombatHelper.hurt("));
        assertTrue(helper.contains("SchoolDamage.context(manipulation, player).withApplication(duration, levels)"));
	}
}
