package com.vincenthuto.hemomancy.common.entity.mob.monster;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Hematic Construct is Hemorath's Downfall wave mob and the Blood Trial Altar's guard; it rendered as a vanilla
 * zombie (cleanup audit AN-2 / A-9). It now has its own generated body and texture.
 */
class HematicConstructResourceTest {
	@Test
	void constructHasItsOwnGeneratedBodyAndTexture() {
		assertTrue(ResourceReader.exists("assets/hemomancy/textures/entity/hematic_construct/hematic_construct.png"),
				"generated texture");
		assertTrue(Files.exists(RepoPaths.TOOLS.resolve("model_export/hematic_construct.py")), "tracked generator");
		assertTrue(Files.exists(RepoPaths.mainJava("client/model/entity/mob/monster/HematicConstructModel.java")),
				"generated model");
		String renderer = SourceAssertions.readMainJava("client/render/entity/mob/monster/HematicConstructRenderer.java");
		SourceAssertions.assertContains("renderer draws the construct's model", renderer, "HematicConstructModel");
		SourceAssertions.assertNotContains("renderer no longer borrows the zombie", renderer, "zombie");
	}
}
