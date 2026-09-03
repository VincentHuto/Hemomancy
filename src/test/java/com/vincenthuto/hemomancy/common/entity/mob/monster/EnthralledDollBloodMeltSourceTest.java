package com.vincenthuto.hemomancy.common.entity.mob.monster;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EnthralledDollBloodMeltSourceTest {
	@Test
	void everyDollUsesTheSharedBloodMeltDeath() throws IOException {
		String entity = source("common/entity/mob/monster/EnthralledDollEntity.java");
		String renderer = source("client/render/entity/mob/monster/EnthralledDollRenderer.java");
		assertTrue(entity.contains("public void die(DamageSource source)"));
		assertTrue(entity.contains("broadcastEntityEvent(this, BLOOD_MELT_EVENT)"));
		assertTrue(entity.contains("BloodCellParticleFactory.createData(ParticleColor.BLOOD)"));
		assertTrue(renderer.contains("entity.deathTime"));
	}

	private static String source(String relativePath) throws IOException {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/" + relativePath));
	}
}
