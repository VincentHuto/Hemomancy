package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * N-1: every payload is registered in the direction it is actually sent. Bidirectional registration is reserved for
 * payloads that really travel both ways, so a client cannot invoke a client-only handler on the server.
 */
class PayloadDirectionTest {
	private static final Set<String> BIDIRECTIONAL = Set.of("AntecedentEffectPacket");
	/** A send call whose payload is constructed inline: {@code sendToPlayer(player, new SomePacket(...))}. */
	private static final Pattern INLINE_SEND = Pattern.compile(
			"\\b(sendToServer|sendToPlayer|sendToPlayersNear|sendToPlayersTrackingEntityAndSelf|sendToPlayersTrackingEntity"
					+ "|sendToAllPlayers|sendToPlayersInDimension|sendToPlayersInChunk)\\s*\\(([^;]*?)\\bnew\\s+(?:[\\w.]+\\.)?(\\w+)\\s*\\(",
			Pattern.DOTALL);

	@Test
	void onlyTheDocumentedPayloadIsBidirectional() {
		Set<String> bidirectional = new TreeSet<>();
		NetworkRegistrations.directions().forEach((payload, method) -> {
			if (method.equals("playBidirectional")) {
				bidirectional.add(payload);
			}
		});
		assertEquals(new TreeSet<>(BIDIRECTIONAL), bidirectional);
	}

	@Test
	void registrationsMatchTheirSenders() throws Exception {
		Map<String, String> directions = NetworkRegistrations.directions();
		Set<String> mismatches = new TreeSet<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				Matcher matcher = INLINE_SEND.matcher(SourceAssertions.read(file));
				while (matcher.find()) {
					String payload = matcher.group(3);
					String method = directions.get(payload);
					if (method == null || method.equals("playBidirectional")) {
						continue;
					}
					String expected = matcher.group(1).equals("sendToServer") ? "playToServer" : "playToClient";
					if (!method.equals(expected)) {
						mismatches.add(payload + " is " + method + " but " + RepoPaths.display(file) + " sends it with "
								+ matcher.group(1));
					}
				}
			}
		}
		assertEquals(List.of(), List.copyOf(mismatches));
	}
}
