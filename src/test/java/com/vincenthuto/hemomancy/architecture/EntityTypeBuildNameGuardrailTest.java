package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** F-26: every entity type is built under its own registry name (the Chthonian Queen was built as "chthonian"). */
class EntityTypeBuildNameGuardrailTest {
	private static final Pattern REGISTRATION = Pattern.compile("ENTITY_TYPES\\.register\\(\\s*\"(\\w+)\"(.*?)\\);\\s*\\n",
			Pattern.DOTALL);
	private static final Pattern BUILD_NAME = Pattern.compile("\\.build\\((?:Hemomancy\\.rloc\\(\"(\\w+)\"\\)|\"(?:hemomancy:)?(\\w+)\")");

	@Test
	void everyEntityTypeIsBuiltUnderItsRegistryName() throws Exception {
		String source = SourceAssertions.read(RepoPaths.mainJava("common/init/EntityInit.java"));
		List<String> mismatched = new ArrayList<>();
		int registrations = 0;
		Matcher registration = REGISTRATION.matcher(source);
		while (registration.find()) {
			registrations++;
			Matcher build = BUILD_NAME.matcher(registration.group(2));
			String built = build.find() ? (build.group(1) != null ? build.group(1) : build.group(2)) : "<no build name>";
			if (!built.equals(registration.group(1))) mismatched.add(registration.group(1) + " built as " + built);
		}
		assertTrue(registrations > 100, "Expected to find the entity registrations, found " + registrations);
		assertEquals(List.of(), mismatched);
	}
}
