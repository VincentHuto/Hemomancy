package com.vincenthuto.hemomancy.architecture;

import com.google.gson.JsonElement;
import com.vincenthuto.hemomancy.support.ResourceReader;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CR-26: keeping a creature reads entity type tags, never a class name. {@code hemomancy:menagerie_people} holds every
 * type whose class sits under {@code common/entity/npc}, {@code hemomancy:menagerie_wills} every type under
 * {@code common/entity/mob/monster/will}, and {@code hemomancy:menagerie_unkeepable} includes both and the bosses.
 */
class MenagerieKeepingGuardrailTest {
	private static final String NPC_PACKAGE = "com.vincenthuto.hemomancy.common.entity.npc.";
	private static final String WILL_PACKAGE = "com.vincenthuto.hemomancy.common.entity.mob.monster.will.";
	private static final Pattern HOLDER = Pattern.compile(
			"DeferredHolder<\\s*EntityType<\\?>\\s*,\\s*EntityType<\\s*([\\w.]+)\\s*>>\\s*\\w+\\s*=\\s*ENTITY_TYPES\\s*\\.register\\(\\s*\"([a-z0-9_]+)\"");
	private static final Pattern IMPORT = Pattern.compile("import\\s+([\\w.]+)\\.(\\w+|\\*);");
	private static final Pattern COURT_MEMBERS = Pattern.compile("court_members\\s*=\\s*courtMembers\\(([^;]*)\\);");
	private static final Pattern COURT_ROLE = Pattern.compile("(?m)^\\t(\\w+)\\(\"([a-z_]+)\"");

	@Test
	void keepingAsksNoClassName() {
		String leash = SourceAssertions.readMainJava("common/entity/menagerie/MenagerieLeash.java");
		SourceAssertions.assertNotContains("MenagerieLeash", leash, "getClass().getName()");
		SourceAssertions.assertNotContains("MenagerieLeash", leash, "\"com.vincenthuto.hemomancy.common.entity");
		SourceAssertions.assertContains("MenagerieLeash", leash, "type.is(UNKEEPABLE)");
	}

	@Test
	void thePeopleAndWillTagsHoldEveryTypeFromTheirPackages() throws Exception {
		Map<String, String> types = registrations();
		assertTrue(types.size() >= 150, "the registration scan found too few entity types: " + types.size());
		Set<String> people = new TreeSet<>(courtMembers());
		Set<String> wills = new TreeSet<>();
		types.forEach((id, className) -> {
			if (className.startsWith(NPC_PACKAGE)) people.add(id);
			if (className.startsWith(WILL_PACKAGE)) wills.add(id);
		});
		assertTrue(people.contains("harbinger_vicar") && people.contains("court_gardener"), "people scan: " + people);
		assertEquals(people, values("menagerie_people"), "a type under common/entity/npc belongs in hemomancy:menagerie_people");
		assertEquals(Set.of("will", "will_anchor"), wills);
		assertEquals(wills, values("menagerie_wills"), "a type under common/entity/mob/monster/will belongs in hemomancy:menagerie_wills");
	}

	@Test
	void theUnkeepableTagIncludesPeopleWillsAndBosses() {
		Set<String> entries = new TreeSet<>();
		for (JsonElement value : ResourceReader.data("tags/entity_type/menagerie_unkeepable.json").getAsJsonArray("values")) {
			entries.add(value.getAsString());
		}
		assertTrue(entries.containsAll(List.of("#hemomancy:menagerie_people", "#hemomancy:menagerie_wills", "#hemomancy:bosses")),
				"hemomancy:menagerie_unkeepable lost an include: " + entries);
	}

	private static Set<String> values(String tag) {
		Set<String> ids = new TreeSet<>();
		for (JsonElement value : ResourceReader.data("tags/entity_type/" + tag + ".json").getAsJsonArray("values")) {
			ids.add(value.getAsString().substring("hemomancy:".length()));
		}
		return ids;
	}

	private static List<String> courtMembers() {
		String init = SourceAssertions.readMainJava("common/init/EntityInit.java");
		String roles = SourceAssertions.readMainJava("common/entity/npc/court/CourtRole.java");
		Map<String, String> ids = new HashMap<>();
		Matcher role = COURT_ROLE.matcher(roles);
		while (role.find()) ids.put(role.group(1), role.group(2));
		Matcher members = COURT_MEMBERS.matcher(init);
		assertTrue(members.find(), "EntityInit no longer registers the Court through courtMembers(...)");
		List<String> found = new ArrayList<>();
		Matcher named = Pattern.compile("CourtRole\\.(\\w+)").matcher(members.group(1));
		while (named.find()) found.add("court_" + ids.get(named.group(1)));
		assertTrue(found.size() >= 18, "court scan: " + found);
		return found;
	}

	private Map<String, String> registrations() {
		String text = SourceAssertions.readMainJava("common/init/EntityInit.java");
		Map<String, String> imports = new HashMap<>();
		List<String> wildcards = new ArrayList<>();
		Matcher importMatcher = IMPORT.matcher(text);
		while (importMatcher.find()) {
			if (importMatcher.group(2).equals("*")) wildcards.add(importMatcher.group(1));
			else imports.put(importMatcher.group(2), importMatcher.group(1) + "." + importMatcher.group(2));
		}
		Map<String, String> found = new TreeMap<>();
		Matcher matcher = HOLDER.matcher(text);
		while (matcher.find()) found.put(matcher.group(2), resolve(matcher.group(1), imports, wildcards));
		return found;
	}

	private String resolve(String name, Map<String, String> imports, List<String> wildcards) {
		if (name.contains(".")) return name;
		if (imports.containsKey(name)) return imports.get(name);
		for (String pkg : wildcards) {
			try {
				return Class.forName(pkg + "." + name, false, getClass().getClassLoader()).getName();
			} catch (ClassNotFoundException ignored) {
			}
		}
		return name;
	}
}
