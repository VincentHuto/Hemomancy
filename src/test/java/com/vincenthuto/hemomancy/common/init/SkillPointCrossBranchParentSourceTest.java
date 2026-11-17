package com.vincenthuto.hemomancy.common.init;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class SkillPointCrossBranchParentSourceTest {

	private SkillPointCrossBranchParentSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String init = read("src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java");
		String scars = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/ScarSkillBranch.java");
		String mycelial = read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/MycelialSkillBranch.java");

		assertBefore("hyphal cultivation must exist before fungal symbiosis registers its parents", init,
				"MycelialSkillBranch.register(BASE);", "ScarSkillBranch.register(BASE);");
		assertContains("mycelial branch registers hyphal cultivation", mycelial,
				"SkillPointInit.skill_hyphal_cultivation = SkillPointInit.registerSkill");
		assertContains("fungal symbiosis includes hyphal cultivation as an additional parent", scars,
				"addParents(SkillPointInit.skill_hyphal_cultivation, SkillPointInit.deep_base_skill)");
	}

	private static void assertBefore(String label, String text, String first, String second) {
		int firstIndex = text.indexOf(first);
		int secondIndex = text.indexOf(second);
		if (firstIndex < 0) {
			throw new AssertionError(label + " (missing first marker '" + first + "')");
		}
		if (secondIndex < 0) {
			throw new AssertionError(label + " (missing second marker '" + second + "')");
		}
		if (firstIndex > secondIndex) {
			throw new AssertionError(label + " expected '" + first + "' before '" + second + "'");
		}
	}
}
