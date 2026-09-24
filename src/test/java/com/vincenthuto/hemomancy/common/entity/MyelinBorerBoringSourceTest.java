package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MyelinBorerBoringSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String ARTHROPOD = "src/main/java/com/vincenthuto/hemomancy/common/entity/mob/arthropod/";

	private static String read(String relativePath) throws IOException {
		return Files.readString(ROOT.resolve(relativePath));
	}

	@Test
	void boringNeverEatsSynapticNodes() throws IOException {
		String goal = read(ARTHROPOD + "BoreFiberGoal.java");
		assertTrue(goal.contains("synaptic_node"),
				"the goal must explicitly exclude synaptic_node so hubs and anchors survive");
		assertTrue(goal.contains("nerve_fiber") && goal.contains("nerve_bundle"),
				"only cable segments are edible");
		assertTrue(goal.contains("isAgitated"), "only provoked Borers bore");
	}

	@Test
	void boringOnlyTargetsRepairableCable() throws IOException {
		assertTrue(read(ARTHROPOD + "BoreFiberGoal.java").contains("FiberGapRules.isGap"),
				"a cut must leave opposed neighbours so calm Borers can mend it");
	}

	@Test
	void mendingOnlyFillsGenuineGaps() throws IOException {
		String goal = read(ARTHROPOD + "MendFiberGoal.java");
		assertTrue(goal.contains("FiberGapRules.isGap"),
				"mending must be gated on gap detection, or Borers would extrude fiber into open void");
		assertTrue(goal.contains("!this.borer.isAgitated()"), "only calm Borers mend");
	}

	@Test
	void breakingTheNetworkProvokesNearbyBorers() throws IOException {
		String events = read(ARTHROPOD + "MyelinBorerAgitationEvents.java");
		assertTrue(events.contains("BlockEvent.BreakEvent"), "breaking a cable is the player's provocation");
		assertTrue(events.contains("CRAWLABLE"), "only network blocks provoke; ordinary blocks must not");
		assertTrue(events.contains("provoke()"), "nearby Borers must actually be provoked");
	}

	@Test
	void beingHurtProvokes() throws IOException {
		String entity = read(ARTHROPOD + "MyelinBorerEntity.java");
		int hurt = entity.indexOf("public boolean hurt(");
		assertTrue(hurt > 0, "the Borer must override hurt so striking one angers it");
		assertTrue(entity.substring(hurt, Math.min(hurt + 500, entity.length())).contains("provokeNearby"),
				"a struck Borer must alert its neighbours rather than keep mending");
	}

	@Test
	void calmBorersDoNotHunt() throws IOException {
		String entity = read(ARTHROPOD + "MyelinBorerEntity.java");
		int target = entity.indexOf("NearestAttackableTargetGoal<>");
		assertTrue(target > 0, "the target goal must exist");
		assertTrue(entity.substring(target, Math.min(target + 300, entity.length())).contains("isAgitated()"),
				"the target goal must be gated on agitation so a mending Borer ignores players");
	}
}
