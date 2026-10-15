package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Everything that reaches for a brazier's bowl asks BrazierMount, so sconces and hanging braziers line up too. */
final class BrazierMountCallSitesSourceTest {
	private static final Path MAIN = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void bowlPositionsFollowTheMount() throws IOException {
		String[] files = {
				"common/rite/harbinger/CardinalRiteOrdealEngine.java",
				"common/rite/harbinger/PuppeteerTrialRiteController.java",
				"common/rite/BrazierItemAbsorptionRite.java",
				"common/block/harbinger/rite/BrazierSpecialOfferingEffects.java",
				"client/render/tile/harbinger/rite/IronBrazierRenderer.java"};
		for (String file : files) {
			String source = Files.readString(MAIN.resolve(file));
			assertFalse(source.contains("Vec3.atCenterOf(offering.pos()).add(0.0D")
					|| source.contains("Vec3.atCenterOf(pos).add(0.0D, 0."), file + " still assumes a standing bowl");
			assertTrue(source.contains("BrazierMount."), file + " must place the bowl by its mount");
		}
	}

	@Test
	void theFireAndTheDaemonFollowTheMount() throws IOException {
		String flames = Files.readString(MAIN.resolve("client/render/world/BrazierFlames.java"));
		assertTrue(flames.contains("double drop"), "the fire and its licks sit in the dropped bowl");
		String engine = Files.readString(MAIN.resolve("common/rite/harbinger/CardinalRiteOrdealEngine.java"));
		assertTrue(engine.contains("BrazierMount.daemonStop("), "the daemon drinks from where it can reach the bowl");
	}

	@Test
	void successionLabelsFollowTheMatcherOrder() throws IOException {
		String labels = Files.readString(MAIN.resolve("client/render/entity/npc/SuccessionBrazierLabels.java"));
		assertFalse(labels.contains("SOCKETS={{"), "roles no longer sit at fixed sockets");
		assertTrue(labels.contains("SuccessionBrazierOrder.roles("),
				"roles come from the shared order the matcher is checked against (BrazierMountGameTests)");
		assertFalse(labels.contains("getFormedForwards"), "the effigy, not the floor's first match, sets the order");
	}
}
