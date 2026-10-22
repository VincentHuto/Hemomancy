package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * N-4: every payload leaves through the {@code PacketHandler} facade, which skips detached GameTest players and routes
 * dialogue through {@code DialogueSender}. Only those two classes may call {@code PacketDistributor}.
 */
class SendFacadeTest {
	private static final Set<String> ALLOWED = Set.of(
			"src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java",
			"src/main/java/com/vincenthuto/hemomancy/common/network/dialogue/DialogueSender.java");

	@Test
	void onlyTheFacadeCallsPacketDistributor() throws Exception {
		List<String> offending = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String shown = RepoPaths.display(file);
				if (!ALLOWED.contains(shown) && SourceAssertions.read(file).contains("PacketDistributor")) {
					offending.add(shown);
				}
			}
		}
		assertEquals(List.of(), offending);
	}
}
