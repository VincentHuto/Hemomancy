package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O-2: copy-paste content families share one base, and each kind keeps only what differs. Registry ids, NBT keys
 * and model layers stay per kind.
 */
class ContentFamilyGuardrailTest {
	private static final Map<String, String> FAMILIES = Map.of(
			"common/entity/mob/arthropod/ChitiniteEntity.java", "AbstractChitiniteEntity",
			"common/entity/mob/arthropod/FerventChitiniteEntity.java", "AbstractChitiniteEntity",
			"common/entity/mob/arthropod/ChthonianEntity.java", "AbstractChthonianEntity",
			"common/entity/mob/arthropod/ChthonianQueenEntity.java", "AbstractChthonianEntity",
			"common/entity/projectile/TrackingBloodOrbEntity.java", "AbstractTrackingOrbEntity",
			"common/entity/projectile/TrackingPestsEntity.java", "AbstractTrackingOrbEntity",
			"common/entity/projectile/TrackingSerpentEntity.java", "AbstractTrackingOrbEntity");

	@Test
	void eachKindExtendsItsFamilyAndKeepsOnlyItsDifferences() {
		FAMILIES.forEach((file, base) -> {
			String source = SourceAssertions.readMainJava(file);
			assertTrue(source.contains("extends " + base), file + " should extend " + base);
			long lines = source.lines().count();
			assertTrue(lines <= 80, file + " is " + lines + " lines; move shared code into " + base);
		});
	}

	@Test
	void combatWeaponStorageGoesThroughTheCarrierHelpers() throws Exception {
		List<String> copies = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String source = SourceAssertions.read(file);
				if (source.contains("\"CombatWeapon\"") && !file.endsWith("CombatWeaponCarrierProjectile.java")) {
					copies.add(RepoPaths.display(file));
				}
			}
		}
		assertEquals(List.of(), copies, "Save and load the firing weapon with CombatWeaponCarrierProjectile");
	}
}
