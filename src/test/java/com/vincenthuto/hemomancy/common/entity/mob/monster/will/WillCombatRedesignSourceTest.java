package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class WillCombatRedesignSourceTest {
	private WillCombatRedesignSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String will = read("src/main/java/com/vincenthuto/hemomancy/common/entity/mob/monster/will/WillEntity.java");
		String caster = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/WillManipulationCaster.java");
		String context = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/ManipulationCastContext.java");
		String entityCastable = read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/EntityCastableManipulation.java");
		assertContains("entity cast marker exists", entityCastable, "boolean castFromEntity(ManipulationCastContext context)");
		assertContains("will context factory exists", context, "forWill");
		assertContains("player context factory exists", context, "forPlayer");
		assertContains("will caster requires entity-castable manipulations", caster, "instanceof EntityCastableManipulation");
		assertContains("will caster builds will context", caster, "ManipulationCastContext.forWill");
		assertContains("broken wills use cast controller", will, "selectBrokenCast");
		assertContains("sent wills use cast controller", will, "selectSentCast");
		assertNotContains("will does not cast through drudge bridge", will, "MobManipCaster");
		// Which kit manipulations have an entity cast is behaviour now (entity casts are attached at registration):
		// entity_cast_validation pins the exact Will-castable set and each cast's outcome.
		assertContains("drudge caster still exists", read("src/main/java/com/vincenthuto/hemomancy/common/manipulation/MobManipCaster.java"),
				"class MobManipCaster");
	}

	private static String read(String path) throws IOException {
		return Files.readString(Path.of(path));
	}

	private static void assertContains(String label, String source, String expected) {
		if (!source.contains(expected)) {
			throw new AssertionError(label + ": missing `" + expected + "`");
		}
	}

	private static void assertNotContains(String label, String source, String unexpected) {
		if (source.contains(unexpected)) {
			throw new AssertionError(label + ": found `" + unexpected + "`");
		}
	}
}
