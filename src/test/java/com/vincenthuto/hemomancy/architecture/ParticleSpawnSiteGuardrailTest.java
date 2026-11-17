package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Where two sweeps put particles (chunk 09 T5 and T4): the Morphling Incubator spawns from its block entity's client
 * tick, never from its renderer (FX-8), and each Unstained rite completion draws its look from
 * {@code UnstainedRiteParticles}, one method per rite, so the review scene and the rite share it.
 */
class ParticleSpawnSiteGuardrailTest {
	private static final String RITES = "common/rite/unstained/UnstainedCardinalRiteEvents.java";
	private static final String RITE_PARTICLES = "common/rite/unstained/UnstainedRiteParticles.java";

	@Test
	void theIncubatorRendererSpawnsNoParticle() {
		String renderer = SourceAssertions.readMainJava(
				"client/render/tile/harbinger/crafting/MorphlingIncubatorRenderer.java");
		for (String spawn : new String[] { "addParticle(", "sendParticles(", "ParticleTypes.", "HemoParticleData." }) {
			SourceAssertions.assertNotContains("MorphlingIncubatorRenderer (FX-8)", renderer, spawn);
		}
		SourceAssertions.assertContains("MorphlingIncubatorBlockEntity client tick",
				SourceAssertions.methodBody(SourceAssertions.readMainJava(
						"common/tile/harbinger/crafting/MorphlingIncubatorBlockEntity.java"), "clientTick("),
				"addParticle(");
	}

	@Test
	void eachUnstainedRiteDrawsItsOwnLook() {
		Map<String, String> riteToLook = new LinkedHashMap<>();
		riteToLook.put("completeLetheanBaptism(", "letheanBaptism(");
		riteToLook.put("completeSilverVeil(", "silverVeil(");
		riteToLook.put("completeClarityAscension(", "clarityAscension(");
		riteToLook.put("completeClosedVein(", "closedVein(");
		riteToLook.put("completeAntisepticGround(", "antisepticGround(");
		riteToLook.put("completeGlassLungs(", "glassLungs(");
		riteToLook.put("completeMoonWashedCopper(", "moonWashedCopper(");
		riteToLook.put("completeLetheanJudgment(", "letheanJudgment(");
		riteToLook.put("completeSilverDawn(", "silverDawn(");
		riteToLook.put("completeStillWaters(", "stillWaters(");
		riteToLook.put("completePaleConsecration(", "paleConsecration(");
		riteToLook.put("completeSilthmereRemembrance(", "silthmereRemembrance(");
		riteToLook.put("completeLetheCovenantRite(", "letheCovenant(");
		riteToLook.put("completeLetheanTide(", "letheanTide(");
		riteToLook.put("completePaleVigil(", "paleVigil(");
		riteToLook.put("completeLetheanFont(", "letheanFont(");

		String rites = SourceAssertions.readMainJava(RITES);
		riteToLook.forEach((rite, look) -> SourceAssertions.assertContains(rite,
				SourceAssertions.methodBody(rites, (rites.contains("static void " + rite) ? "static void " : "static boolean ") + rite),
				"UnstainedRiteParticles." + look));
		SourceAssertions.assertNotContains(RITES, rites, "sendParticles(");

		Set<String> looks = new TreeSet<>();
		Matcher method = Pattern.compile("public static void (\\w+)\\(").matcher(SourceAssertions.readMainJava(RITE_PARTICLES));
		while (method.find()) looks.add(method.group(1) + "(");
		assertEquals(new TreeSet<>(riteToLook.values()), looks, "every rite look has its rite, and every rite its look");
	}
}
