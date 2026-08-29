package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class ToggleableSkillIntegrationSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();
	private static final String[] SKILLS = {
			"persistent_arsenal", "distributed_siphon", "selective_hunger", "sanguine_reserve", "sanguine_spinning",
			"automatic_coagulation", "guarded_feeding", "shared_siphon", "autonomous_retaliation",
			"merciful_command", "deep_scar_resonance", "crimson_wake", "vascular_mercy",
			"bloodhound_sense", "reflexive_coagulation", "dormant_symbiote", "symbiotic_metabolism"
	};

	@Test
	void registersAndSynchronizesEveryAgreedTechnique() throws IOException {
		String init = read("src/main/java/com/vincenthuto/hemomancy/common/init/SkillPointInit.java");
		String branches = String.join("\n",
				read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/CoreSkillBranch.java"),
				read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/LivingStaffSkillBranch.java"),
				read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/SummonSkillBranch.java"),
				read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/ScarSkillBranch.java"),
				read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/MycelialSkillBranch.java"),
				read("src/main/java/com/vincenthuto/hemomancy/common/init/skills/CovenantSkillBranch.java"));
		String packet = read("src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/PacketToggleSkill.java");
		String handler = read("src/main/java/com/vincenthuto/hemomancy/common/network/PacketHandler.java");
		String screen = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillsTabController.java");
		String shapes = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/util/EnumNodeShape.java");
		String lang = read("src/main/resources/assets/hemomancy/lang/en_us.json");
		for (String name : SKILLS) {
			assertTrue(init.contains("skill_" + name), name + " field");
			assertTrue(branches.contains("\"skill_" + name + "\""), name + " registration");
			assertTrue(lang.contains("skill.hemomancy.skill_" + name + ".desc"), name + " description");
		}
		assertTrue(branches.contains("setToggleable(true)"));
		assertTrue(branches.contains(".setBranchColor(0x"));
		assertTrue(packet.contains("toggleEnabled"));
		assertTrue(handler.contains("PacketToggleSkill.TYPE"));
		assertTrue(screen.contains("new PacketToggleSkill"));
		assertTrue(screen.contains("isEnabled(sp)"));
		assertTrue(screen.contains("EnumNodeShape.HEXAGON"));
		assertTrue(shapes.contains("DECAGON"));
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path));
	}
}
