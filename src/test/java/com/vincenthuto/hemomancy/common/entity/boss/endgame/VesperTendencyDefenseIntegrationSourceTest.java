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
				"src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolCombatEvents.java"));

		assertTrue(source.contains("TendencyAffinityRules.damageMultiplier(player, target, hit.primary(), hit.secondary())"));
		assertTrue(source.contains("event.setAmount(damage)"));
	}

	@Test
	void animusProjectilesAndBloodCloudCannotBypassTheDefense() throws IOException {
		String eventSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolDamage.java"));
		String shotSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodShotManip.java"));
		String needleSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/animus/BloodNeedleManip.java"));
		String cloudSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/projectile/CloudEntityBlood.java"));

		assertTrue(eventSource.contains("projectile instanceof TendencyDamageCarrier carrier"));
		assertTrue(shotSource.contains("shot.setDamageTendency(getTend())"));
		assertTrue(needleSource.contains("needle.setDamageTendency(getTend())"));
		assertTrue(cloudSource.contains("SchoolHitContext.Kind.PERIODIC"));
	}

	@Test
	void mixedProjectileManipulationsKeepTheirSecondaryTendency() throws IOException {
		String carrierSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/manipulation/TendencyDamageCarrier.java"));
		String eventSource = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/damage/SchoolDamage.java"));
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

        String helper = Files.readString(Path.of(
                "src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationCombatHelper.java"));

        assertTrue(source.contains("ManipulationCombatHelper.hurt("));
        assertTrue(helper.contains("SchoolDamage.context(manipulation, player).withApplication(duration, levels)"));
	}
}
