package com.vincenthuto.hemomancy.common.manipulation.stillarts;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StillArtPresentationSourceTest {
	private static final Path MAIN = Path.of("src/main/java/com/vincenthuto/hemomancy/common");

	private static String read(String relative) throws IOException {
		return Files.readString(MAIN.resolve(relative)).replace("\r\n", "\n");
	}

	private static int count(String text, String needle) {
		Matcher matcher = Pattern.compile(Pattern.quote(needle)).matcher(text);
		int found = 0;
		while (matcher.find()) {
			found++;
		}
		return found;
	}

	@Test
	void performActionPublishesReleaseAfterSuccess() throws IOException {
		String source = read("manipulation/stillarts/StillArt.java");
		int cast = source.indexOf("action.cast(");
		int release = source.indexOf("CastingAnimationManager.releaseStillArt");
		assertTrue(cast >= 0, "StillArt.performAction no longer calls action.cast");
		assertTrue(release > cast, "Still Art release must be published only after a successful action.cast");
		assertEquals(1, count(source, "CastingAnimationManager.releaseStillArt"), "Still Art release must be published once");
	}

	@Test
	void castingRefusalMatchesPresentationGate() throws IOException {
		// Disrupted and axonal travel block actions; a cast the animation gate refuses must not succeed silently.
		String art = read("manipulation/stillarts/StillArt.java");
		String manager = read("manipulation/animation/CastingAnimationManager.java");
		assertTrue(manager.contains("Paralysis.blocksActions(player)"), "Still Art presentation gate changed");
		assertTrue(art.contains("Paralysis.blocksActions(player)"), "StillArt.performAction must refuse on the same action block as its presentation");
		assertFalse(art.contains("Paralysis.isParalyzed(player)"), "StillArt.performAction still checks only hard paralysis");
	}

	@Test
	void everyRegisteredArtDeclaresAnEnding() throws IOException {
		String source = read("init/StillArtInit.java");
		// Import lines name the enum without a member access, so only registrations are counted.
		String body = source.replaceAll("(?m)^import .*$", "");
		int arts = count(body, "new StillArt(");
		int endings = count(body, "StillArtEnding.");
		assertTrue(arts >= 9, "Expected every registered Still Art in StillArtInit");
		assertEquals(arts, endings, "Every registered Still Art must declare exactly one ending");
	}

	@Test
	void blankArtSettlesIntoStillness() throws IOException {
		String source = read("manipulation/stillarts/StillArt.java");
		assertTrue(source.contains("EnumClarityStage.AWAKENED, 0, StillArtEnding.STILLNESS, StillArtAction.NOOP"),
				"StillArt.BLANK must use the STILLNESS ending");
	}

	@Test
	void noVanillaParticlesRemain() throws IOException {
		String source = read("init/StillArtInit.java");
		assertFalse(source.contains("ParticleTypes."), "Still Arts must use HemoParticleData glow motes, not vanilla particles");
		assertFalse(source.contains("import net.minecraft.core.particles.ParticleTypes;"), "Stale vanilla particle import");
	}

	@Test
	void clientLaneResolvesStageTracksEndingsAndPaleAccents() throws IOException {
		Path client = Path.of("src/main/java/com/vincenthuto/hemomancy/client");
		String tracks = Files.readString(client.resolve("player/tracks/CastingTracks.java"));
		assertTrue(tracks.contains("StillArtTracks.get("), "Unstained packets must resolve their clarity-stage track");
		assertTrue(tracks.contains("StillArtEndings.get("), "Unstained packets must resolve their ending layer");
		assertFalse(tracks.contains("TODO Task 13"), "Still Art placeholder track remains");
		String anatomy = Files.readString(client.resolve("render/world/CastingAnatomyRenderer.java"));
		assertTrue(anatomy.contains("stillHand("), "Still Arts need the pale hand ring");
		assertTrue(anatomy.contains("stillGround("), "Still Arts need the still-water ground ring");
		assertFalse(anatomy.contains("ParticleTypes."), "Casting accents must not emit vanilla particles");
		String state = Files.readString(client.resolve("player/CastingAnimationClientState.java"));
		assertTrue(state.contains("CastingTracks.ending("), "Still Art playback must use the ending as its aim layer");
	}
}
