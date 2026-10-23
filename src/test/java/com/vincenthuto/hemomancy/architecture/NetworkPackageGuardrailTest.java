package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 11 (roadmap Phase 10, N-8): {@code common/network} holds payloads, {@code PacketHandler} and
 * {@code ServerboundGuards}, plus the few listed networking helpers. Gameplay a payload triggers lives in
 * {@code rite/}, {@code tile/}, {@code entity/} or {@code capability/}, and the payload calls it.
 */
class NetworkPackageGuardrailTest {
	private static final Path NETWORK = RepoPaths.mainJava("common/network");
	private static final Set<String> CORE = Set.of("PacketHandler.java", "ServerboundGuards.java");
	/** Networking helpers that are not payloads, with why each belongs here. */
	private static final Map<String, String> LISTED = Map.of(
			"ClientPayloadHandlers.java", "the client-bound dispatch indirection that keeps client classes out of payloads",
			"dialogue/DialogueSender.java", "the dialogue send facade beside PacketHandler (SendFacadeTest)",
			"capa/harbinger/PlayerAnimationKind.java", "the enum PacketSyncPlayerAnimation writes into its byte layout");

	@Test
	void networkHoldsOnlyPayloadsAndNetworkingHelpers() throws IOException {
		Set<String> unexpected = new TreeSet<>();
		Set<String> seenListed = new TreeSet<>();
		try (Stream<Path> files = Files.walk(NETWORK)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				String relative = NETWORK.relativize(file).toString().replace('\\', '/');
				if (CORE.contains(relative)) continue;
				if (LISTED.containsKey(relative)) {
					seenListed.add(relative);
					continue;
				}
				if (!SourceAssertions.read(file).contains("CustomPacketPayload")) unexpected.add(relative);
			}
		}
		assertEquals(Set.of(), unexpected,
				"move gameplay out of common/network (a payload decodes, validates and calls one service method)");
		assertEquals(new TreeSet<>(LISTED.keySet()), seenListed, "remove stale entries from LISTED");
	}
}
