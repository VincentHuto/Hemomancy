package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.mission.circus.CircusCurriculum;
import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every Crimson Troupe professor says the school's shared beats in their own words (CD-7): each step the greeting can
 * reach, the preparation answer and each ending's aftermath have a key per role, and no two professors share a line.
 */
class CircusSchoolDialogueKeysTest {
	private static final String THREADKEEPER = "circus_threadkeeper";

	@Test
	void eachProfessorSpeaksEveryReachableLineInTheirOwnWords() {
		List<String> roles = new ArrayList<>();
		CircusCurriculum.lessons().forEach(lesson -> roles.add(lesson.teacher()));
		roles.add(THREADKEEPER);
		assertEquals(8, roles.size());
		Map<String, String> seen = new HashMap<>();
		List<String> problems = new ArrayList<>();
		for (String role : roles) {
			List<String> keys = new ArrayList<>();
			List<String> steps = CircusCurriculum.forTeacher(role).isPresent()
					? List.of("degree", "lesson", "ordeal", "practical", "mastered") : List.of("preparation");
			for (String step : steps) keys.add(CircusSchoolDialogue.statusKey(role, step));
			for (CircusRouteRules.Route route : CircusRouteRules.Route.values()) {
				keys.add(CircusSchoolDialogue.aftermathKey(role, route));
			}
			keys.add("hemomancy.circus.school." + role + ".preparation");
			for (String key : keys) {
				String line = EnglishText.of(key);
				String other = seen.put(line, key);
				if (other != null) problems.add(key + " repeats " + other);
			}
		}
		for (CircusRouteRules.Route route : CircusRouteRules.Route.values()) {
			String key = CircusSchoolDialogue.aftermathKey("ringmaster", route);
			String other = seen.put(EnglishText.of(key), key);
			if (other != null) problems.add(key + " repeats " + other);
		}
		assertEquals(List.of(), problems);
	}
}
