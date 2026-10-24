package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * S-8: session-only server state has one home. Every static UUID-keyed collection in {@code common/} belongs to a class
 * that {@code TransientServerStateEvents} clears, and only the hub listens for server start, server stop and logout,
 * apart from the logout handlers listed here, which do gameplay work rather than forget session maps.
 */
class SessionStateRegistryTest {
	private static final Path COMMON = RepoPaths.MAIN_JAVA.resolve("common");
	private static final String HUB = "common/event/TransientServerStateEvents.java";
	private static final Pattern UUID_COLLECTION = Pattern.compile(
			"\\bstatic\\s+(?:final\\s+)?(?:[\\w.]+\\.)?(?:Map|Set|HashMap|HashSet|ConcurrentMap|ConcurrentHashMap|WeakHashMap"
					+ "|LinkedHashMap|IdentityHashMap)\\s*<\\s*(?:java\\.util\\.)?UUID\\b[^;=(]*?\\s(\\w+)\\s*=",
			Pattern.DOTALL);

	/** Collections the hub leaves alone, each with the reason. Keys are {@code path#FIELD} under {@code common/}. */
	private static final Map<String, String> EXEMPT = Map.of(
			"item/shared/ScaleGripEvents.java#PENDING_RETURNS",
			"holds the protected items until respawn; clearing it would lose them (F-4 persists them instead)",
			"manipulation/mortem/BlackheartedEvents.java#RUPTURING", "re-entrancy guard for one rupture",
			"init/EffectInit.java#MNEMONIC_WHISPERS_REDRINKS", "re-entrancy guard for one drink",
			"manipulation/mortem/LignumMortisManip.java#SESSIONS",
			"each session ends with its channel, which ManipulationChannelManager clears");

	/** Classes whose logout handler does gameplay work, not session-map cleanup. */
	private static final Map<String, String> LOGOUT_HANDLERS = Map.of(
			"common/event/ChamberVisitEvents.java", "returns the player from an interrupted Chamber visit",
			"common/capability/player/harbinger/morphling/EquippedMorphlingEvents.java", "persists the equipped morphling",
			"common/event/FungalProjectionEvents.java", "forces a projection back to its body",
			"common/item/harbinger/tool/living/LivingStaffWeaponFormEvents.java", "restores the staff's held form",
			"common/item/harbinger/tool/living/LivingTorchChannelEvents.java", "stops a breathing channel",
			"common/entity/summon/puppeteer/PuppeteerCommandEvents.java", "rotates the owner's puppet session",
			"common/manipulation/ductilis/Paralysis.java", "lifts paralysis, as on login, respawn and dimension change");

	@Test
	void everyUuidKeyedCollectionIsRegisteredWithTheHub() throws Exception {
		String hub = SourceAssertions.read(RepoPaths.MAIN_JAVA.resolve(HUB));
		List<String> unregistered = new ArrayList<>();
		TreeSet<String> exemptSeen = new TreeSet<>();
		for (Path file : javaFiles(COMMON)) {
			String text = SourceAssertions.read(file);
			String relative = COMMON.relativize(file).toString().replace('\\', '/');
			String owner = file.getFileName().toString().replace(".java", "");
			Matcher matcher = UUID_COLLECTION.matcher(text);
			while (matcher.find()) {
				String key = relative + "#" + matcher.group(1);
				if (EXEMPT.containsKey(key)) {
					exemptSeen.add(key);
				} else if (!hub.contains(owner + ".")) {
					unregistered.add(key);
				}
			}
		}
		assertEquals(List.of(), unregistered, "Register these maps' clears and logout removal in TransientServerStateEvents");
		assertEquals(new TreeSet<>(EXEMPT.keySet()), exemptSeen, "Remove exemptions whose fields are gone");
	}

	/** Clearing a map that tracks placed blocks would orphan them; such maps release their blocks on logout first. */
	@Test
	void trackedHelmetLightsAreRemovedBeforeTheirMapIsCleared() {
		String hub = SourceAssertions.read(RepoPaths.MAIN_JAVA.resolve(HUB));
		SourceAssertions.assertContains("logout removes the light while its level is loaded", hub,
				"LanternTickLightManager.logout(serverPlayer);");
	}

	@Test
	void onlyTheHubListensForServerStartAndStop() throws Exception {
		List<String> offending = new ArrayList<>();
		for (Path file : javaFiles(COMMON)) {
			String shown = RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
			if (shown.equals(HUB)) continue;
			String text = SourceAssertions.read(file);
			if (text.contains("ServerStoppedEvent") || text.contains("ServerStoppingEvent")) offending.add(shown);
		}
		assertEquals(List.of(), offending);
	}

	@Test
	void logoutHandlersAreTheHubOrListedGameplay() throws Exception {
		TreeSet<String> found = new TreeSet<>();
		for (Path file : javaFiles(COMMON)) {
			String shown = RepoPaths.MAIN_JAVA.relativize(file).toString().replace('\\', '/');
			if (!shown.equals(HUB) && SourceAssertions.read(file).contains("PlayerLoggedOutEvent")) found.add(shown);
		}
		assertEquals(new TreeSet<>(LOGOUT_HANDLERS.keySet()), found,
				"Session-map cleanup on logout belongs in TransientServerStateEvents.endPlayerSession");
	}

	private static List<Path> javaFiles(Path root) throws Exception {
		try (Stream<Path> files = Files.walk(root)) {
			return files.filter(path -> path.toString().endsWith(".java")).sorted().toList();
		}
	}
}
