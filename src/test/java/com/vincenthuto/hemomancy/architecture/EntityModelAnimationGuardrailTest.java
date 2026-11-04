package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every entity model moves. A {@code setupAnim} with an empty body ships a creature that never walks, turns its head
 * or strikes (eight live mobs did, cleanup audit AN-3 / A-11). A model whose stillness is the design is listed here
 * with the reason. Stale entries fail.
 */
class EntityModelAnimationGuardrailTest {
	private static final Pattern EMPTY_SETUP_ANIM = Pattern.compile(
			"void\\s+setupAnim\\s*\\([^)]*\\)\\s*\\{\\s*\\}");

	private static final Map<String, String> ALLOWED = Map.of(
			"src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc/CircusCarouselModel.java",
			"The carousel is a ride, not a creature; its renderer turns it",
			"src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/BloodBulletModel.java",
			"A projectile: its renderer spins it along its flight",
			"src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/IronPillarModel.java",
			"A raised construct that stands still by design",
			"src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/IronSpikeModel.java",
			"A raised construct that stands still by design",
			"src/main/java/com/vincenthuto/hemomancy/client/model/entity/summon/IronWallModel.java",
			"A raised construct that stands still by design");

	@Test
	void everyEntityModelAnimates() throws Exception {
		TreeSet<String> still = new TreeSet<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA.resolve("client/model/entity"))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
				Matcher matcher = EMPTY_SETUP_ANIM.matcher(SourceAssertions.read(file));
				if (matcher.find()) still.add(RepoPaths.display(file));
			}
		}
		assertEquals(new TreeSet<>(ALLOWED.keySet()), still,
				"give the model a walk cycle and an attack beat, or list it here with the reason it stays still");
	}
}
