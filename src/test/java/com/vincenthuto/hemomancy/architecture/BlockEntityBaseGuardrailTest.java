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

/**
 * L-7: a syncing block entity extends {@code HemoBlockEntity} or {@code HemoContainerBlockEntity}, the only
 * {@code getUpdatePacket} overrides. Block blood is saved through its attachment ({@code BlockBloodPersistenceGameTests}
 * checks each store's save and its read of old saves).
 */
class BlockEntityBaseGuardrailTest {
	@Test
	void onlyTheBasesBuildTheUpdatePacket() throws Exception {
		List<String> overrides = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
				if (SourceAssertions.read(file).contains(" getUpdatePacket()")) overrides.add(RepoPaths.display(file));
			}
		}
		assertEquals(List.of(
				"src/main/java/com/vincenthuto/hemomancy/common/tile/HemoBlockEntity.java",
				"src/main/java/com/vincenthuto/hemomancy/common/tile/HemoContainerBlockEntity.java"), overrides);
	}
}
