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
 * N-3: payload classes load on the dedicated server, so {@code common/network} must not touch vanilla client classes.
 * Client-bound handlers forward to a static in {@code client/**}, or work through {@code IPayloadContext.player()}.
 */
class NetworkClientIsolationTest {
	@Test
	void commonNetworkHasNoVanillaClientReferences() throws Exception {
		List<String> offending = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.mainJava("common/network"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String source = SourceAssertions.read(file);
				if (source.contains("net.minecraft.client.") || source.contains("Minecraft.getInstance()")) {
					offending.add(RepoPaths.display(file));
				}
			}
		}
		assertEquals(List.of(), offending);
	}
}
