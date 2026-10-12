package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting.echo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every registered manipulation has a real echo cast or is listed as pending; nothing is in both or stale. */
class EchoCastCoverageTest {
	private static Set<String> names(String source, String regex) {
		Set<String> out = new TreeSet<>();
		Matcher m = Pattern.compile(regex).matcher(source);
		while (m.find()) out.add(m.group(1));
		return out;
	}

	private static final Path ECHO = Path.of("src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/echo");
	private static final Path RETIREMENT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/capability/player/harbinger/manip/ManipulationRetirementRules.java");

	/** Every echo source, comments stripped so prose never counts as a registration or a server send. */
	private static String echoSources() throws IOException {
		StringBuilder out = new StringBuilder();
		try (var files = Files.list(ECHO)) {
			for (Path file : files.filter(f -> f.toString().endsWith(".java")).sorted().toList())
				out.append(Files.readString(file).replaceAll("(?s)/\\*.*?\\*/", "").replaceAll("//[^\\n]*", "")).append('\n');
		}
		return out.toString();
	}

	private static Set<String> pending() throws IOException {
		return new TreeSet<>(Files.readAllLines(Path.of("src/test/resources/echo_cast_pending.txt")).stream()
				.map(String::strip).filter(s -> !s.isEmpty()).toList());
	}

	@Test
	void onlyRetiredManipulationsKeepTheGestureFallback() throws IOException {
		Set<String> retired = names(Files.readString(RETIREMENT), "\"([a-z0-9_]+)\"");
		Set<String> unexpected = pending();
		unexpected.removeAll(retired);
		assertTrue(unexpected.isEmpty(), "Castable memories still without a real echo cast: " + unexpected);
	}

	@Test
	void echoCastsNeverReachTheServerOrPlayerState() throws IOException {
		String sources = echoSources();
		for (String forbidden : new String[]{"PacketDistributor", "sendToServer", "AxonalTransductionClient",
				"SyncSchoolCombatS2CPacket", "CastingAnimationClientState.accept", "ParticleTypes."})
			assertTrue(!sources.contains(forbidden), "echo casts must stay client-only and custom: " + forbidden);
	}

	@Test
	void castsAvoidFormsThatOnlyDrawBetweenLiveEntities() throws IOException {
		// MARIONETTE_TETHER draws only when its body id and caster id (in `count`) both resolve to entities.
		assertTrue(!echoSources().contains("Form.MARIONETTE_TETHER"), "echo casts have no entities for a marionette tether");
	}

	@Test
	void theLoomReleasesCastsOffTheRenderPassAndSafely() throws IOException {
		String echo = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/crafting/SomaticLoomEcho.java"));
		assertTrue(echo.contains("EchoCasts.releaseSafely(") && !echo.contains(".release(new EchoCast.EchoContext"),
				"the block entity renderer defers the cast instead of running it mid-frame");
		String casts = Files.readString(ECHO.resolve("EchoCasts.java"));
		assertTrue(casts.contains("EchoScheduler.later(1,") && casts.contains("catch (RuntimeException"),
				"a deferred cast that throws never reaches the render pass");
	}

	@Test
	void keyedCuesAreRetiredBeforeTheyReplay() throws IOException {
		String fx = Files.readString(ECHO.resolve("EchoFx.java"));
		assertTrue(fx.contains("static void keyed("), "EchoFx offers a keyed replay");
		int retire = fx.indexOf("ticks = 0 first"), resend = fx.indexOf("cue(form, from, to, radius, ticks, count)", retire);
		assertTrue(retire > 0 && resend > retire, "keyed cues are sent with ticks 0 before replaying");
	}

	@Test
	void everyManipulationHasAnEchoCastOrIsPending() throws IOException {
		Set<String> manipulations = names(Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/init/ManipulationInit.java")), "MANIPS\\.register\\(\"([a-z0-9_]+)\"");
		Set<String> cast = names(echoSources(), "register\\(\"([a-z0-9_]+)\"");
		Set<String> pending = new TreeSet<>(Files.readAllLines(Path.of("src/test/resources/echo_cast_pending.txt")).stream()
				.map(String::strip).filter(s -> !s.isEmpty()).toList());
		Set<String> missing = new TreeSet<>(manipulations);
		missing.removeAll(cast);
		missing.removeAll(pending);
		assertTrue(missing.isEmpty(), "Manipulations with neither an echo cast nor a pending entry: " + missing);
		Set<String> both = new HashSet<>(cast);
		both.retainAll(pending);
		assertTrue(both.isEmpty(), "Remove from echo_cast_pending.txt now that they have casts: " + both);
		Set<String> stale = new TreeSet<>(pending);
		stale.removeAll(manipulations);
		assertTrue(stale.isEmpty(), "Pending entries that are not manipulations: " + stale);
	}
}
