package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O-1: a boss holds its bar through {@code BossBarBinding}, and the trial Saints share {@code SaintTrialEntity}.
 * Vesper, Annetta and the Mycophant keep their own classes on purpose (audit: do not force them under one base).
 */
class BossFrameworkGuardrailTest {
	private static final List<String> TRIAL_SAINTS = List.of(
			"common/entity/boss/saint/hemorath/HemorathEntity.java",
			"common/entity/boss/saint/putriciel/PutricielEntity.java",
			"common/entity/boss/saint/velorum/VelorumEntity.java");

	@Test
	void onlyBossBarBindingBuildsABossEvent() throws Exception {
		List<String> builders = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				if (SourceAssertions.read(file).contains("new ServerBossEvent(")) builders.add(RepoPaths.display(file));
			}
		}
		assertEquals(List.of("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/BossBarBinding.java"), builders);
	}

	@Test
	void theTrialSaintsShareOneBase() {
		for (String saint : TRIAL_SAINTS) {
			String source = SourceAssertions.readMainJava(saint);
			assertTrue(source.contains("extends SaintTrialEntity"), saint);
			for (String copied : List.of("DATA_VISUAL_STATE", "void sendFloorRing(", "void broadcastMessage(",
					"void stopSeenByPlayer(")) {
				assertTrue(!source.contains(copied), saint + " keeps its own " + copied);
			}
		}
	}
}
