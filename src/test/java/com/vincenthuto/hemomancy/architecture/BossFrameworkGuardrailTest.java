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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * O-1: a boss holds its bar through {@code BossBarBinding}, and the four trial Saints share {@code SaintTrialEntity}.
 * Vesper, Annetta and the Mycophant keep their own classes on purpose (audit: do not force them under one base).
 * A Saint cannot be killed: every ending goes through the base's one {@code endTrial}, which rolls the Saint's loot
 * table once and restores its sarcophagus; a sarcophagus never picks a Saint at random.
 */
class BossFrameworkGuardrailTest {
	private static final List<String> TRIAL_SAINTS = List.of(
			"common/entity/boss/saint/hemorath/HemorathEntity.java",
			"common/entity/boss/saint/putriciel/PutricielEntity.java",
			"common/entity/boss/saint/velorum/VelorumEntity.java",
			"common/entity/boss/saint/seraphae/SeraphaeEntity.java");

	@Test
	void onlyBossBarBindingBuildsABossEvent() throws Exception {
		List<String> builders = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				if (SourceAssertions.read(file).contains("new ServerBossEvent(")) builders.add(RepoPaths.display(file));
			}
		}
		assertEquals(List.of("src/main/java/com/vincenthuto/hemomancy/common/entity/boss/BossBarBinding.java"), builders);
	}

	@Test
	void theTrialSaintsShareOneBase() {
		for (String saint : TRIAL_SAINTS) {
			String source = SourceAssertions.readMainJava(saint);
			assertTrue(source.contains("extends SaintTrialEntity"), saint);
			for (String copied : List.of("DATA_VISUAL_STATE", "void sendFloorRing(", "void broadcastMessage(",
					"void stopSeenByPlayer(", "DATA_RISE_TICK", "BossIntroWindow", "void beginRise(")) {
				assertTrue(!source.contains(copied), saint + " keeps its own " + copied);
			}
		}
	}

	@Test
	void everySaintEndsThroughTheOneTrialEnding() {
		String base = SourceAssertions.readMainJava("common/entity/boss/saint/SaintTrialEntity.java");
		SourceAssertions.assertContains("one ending, run once", base, "protected final void endTrial(");
		SourceAssertions.assertContains("the ending rolls entities/<id>", base, "getType().getDefaultLootTable()");
		SourceAssertions.assertContains("the ending restores the tomb", base, "SaintSarcophagusBlock.restoreDormant(");
		SourceAssertions.assertContains("the ending says so", base, "message.hemomancy.saint.returns_to_tomb");
		assertTrue(SourceAssertions.methodBody(base, "protected void dropFromLootTable(").chars()
				.filter(c -> !Character.isWhitespace(c)).count() == 2,
				"vanilla death loot must not roll the Saint's table a second time");
		assertTrue(SourceAssertions.methodBody(base, "public void die(DamageSource source)").contains("endTrial("),
				"an HP death is an ending too");
		for (String saint : TRIAL_SAINTS) {
			String source = SourceAssertions.readMainJava(saint);
			for (String drop : List.of("hallowed_residuum_", "dropCustomDeathLoot(", "void dropFromLootTable(")) {
				assertTrue(!source.contains(drop), saint + " drops its reward outside endTrial: " + drop);
			}
		}
		String hooks = SourceAssertions.readMainJava("common/entity/boss/saint/HarbingerSaintEncounterHooks.java");
		assertTrue(hooks.indexOf("boss.bindTomb(pos, tomb)") >= 0
				&& hooks.indexOf("boss.bindTomb(pos, tomb)") < hooks.indexOf("level.addFreshEntity(boss)"),
				"the Saint remembers its tomb before it enters the world");
	}

	@Test
	void aSarcophagusNeverPicksASaintAtRandom() {
		String block = SourceAssertions.readMainJava("common/block/harbinger/puzzle/SaintSarcophagusBlock.java");
		String tile = SourceAssertions.readMainJava("common/tile/harbinger/puzzle/SaintSarcophagusBlockEntity.java");
		for (String source : List.of(block, tile)) {
			SourceAssertions.assertNotContains("no random Saint", source, "EnumSaintType.values()");
		}
		int relic = block.indexOf("heldStack.getItem() instanceof SaintRelicItem relic");
		int unassigned = block.indexOf("if (!sarcophagus.isSaintTypeAssigned())");
		assertTrue(relic >= 0 && relic < unassigned && unassigned < block.indexOf("heldStack.is(ItemInit.foul_paste.get())")
				&& unassigned < block.indexOf("if (!sarcophagus.isConsecrated())"),
				"only a relic names an unassigned sarcophagus; everything else is refused first");
		SourceAssertions.assertContains("the refusal is a key", block, "message.hemomancy.saint_sarcophagus.unassigned");
	}
}
