package com.vincenthuto.hemomancy.common.entity.npc.circus;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusPerformerThreadSourceTest {
	private static final Path SOURCE = Path.of("src/main/java/com/vincenthuto/hemomancy");

	@Test
	void stageBodiesSyncTheirTeacherAsOwnerSoClientsCanDrawTheThread() throws Exception {
		String demonstrations = Files.readString(SOURCE.resolve("common/circus/CircusDemonstrations.java"));
		assertTrue(demonstrations.contains("bound.hemomancy$setOwnerUUID(teacher.getUUID())"),
				"new and already-saved stage bodies must carry the teacher in the synced owner field");
	}

	@Test
	void performersControlTheirStageBodiesAndDollsFromTheirRightHand() throws Exception {
		String threads = Files.readString(SOURCE.resolve("client/render/world/PuppeteerThreadRenderer.java"));
		assertTrue(threads.contains("entity instanceof CircusPerformerEntity"), "performers must be thread controllers");
		assertTrue(threads.contains("controller instanceof CircusPerformerEntity performer"),
				"stage bodies owned by a performer must resolve their controller");
		assertTrue(threads.contains("PuppeteerThreadEndpointRules.performerHandEndpoint"));
		assertTrue(threads.contains("findDollController"), "performer-owned Enthralled Dolls must be threaded");
		assertTrue(threads.contains("PerformerHandAnchors.anchor(performer, partialTick)"),
				"threads must leave the hand the performer renderer actually drew");
	}

	@Test
	void performerRendererRecordsTheDrawnRightHandEveryFrame() throws Exception {
		String renderer = Files.readString(SOURCE.resolve("client/render/entity/npc/CircusPerformerRenderer.java"));
		assertTrue(renderer.contains("rightArm.translateAndRotate(poseStack)"), "anchor must follow the posed arm");
		assertTrue(renderer.contains("PerformerHandAnchors.record(entity"));
	}
}
