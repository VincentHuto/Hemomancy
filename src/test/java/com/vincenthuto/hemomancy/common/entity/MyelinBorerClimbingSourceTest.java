package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MyelinBorerClimbingSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void tagListsTheNetworkBlocks() throws IOException {
		String tag = read("src/main/resources/data/hemomancy/tags/block/cortical_crawlable.json");
		for (String id : new String[] { "hemomancy:nerve_fiber", "hemomancy:nerve_bundle",
				"hemomancy:synaptic_node" }) {
			assertTrue(tag.contains(id), "the Borer rides the whole network, including " + id);
		}
	}

	@Test
	void entityClimbsTheTagAndTracksAgitation() throws IOException {
		String entity = read(
				"src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/MyelinBorerEntity.java");
		assertTrue(entity.contains("cortical_crawlable"),
				"climbing must be driven by the tag, not a hardcoded block list");
		assertTrue(entity.contains("public boolean onClimbable()"),
				"the Borer grips cables instead of walking");
		assertTrue(entity.contains("crawlOnCable()"),
				"the Borer must choose cable perches instead of using free flight");
		assertTrue(entity.contains("MyelinBorerMovementRules.decayAgitation"),
				"agitation must decay each tick so provoked Borers calm down again");
		assertTrue(entity.contains("defineSynchedData"),
				"agitation must be synced so the client can react to the mood");
	}
}
