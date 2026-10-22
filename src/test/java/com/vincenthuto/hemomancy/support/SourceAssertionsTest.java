package com.vincenthuto.hemomancy.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SourceAssertionsTest {
	private static final String SOURCE = """
			class Example {
				void first() {
					if (ready) {
						go();
					}
				}

				void second() {
					stop();
				}
			}
			""";

	@Test
	void methodBodyMatchesNestedBraces() {
		assertEquals("{\n\t\tif (ready) {\n\t\t\tgo();\n\t\t}\n\t}", SourceAssertions.methodBody(SOURCE, "void first()"));
		assertEquals("{\n\t\tstop();\n\t}", SourceAssertions.methodBody(SOURCE, "void second()"));
	}

	@Test
	void sectionExcludesItsMarkers() {
		assertEquals(" Example ", SourceAssertions.section(SOURCE, "class", "{"));
	}

	@Test
	void failuresAreAssertionErrors() {
		assertThrows(AssertionError.class, () -> SourceAssertions.assertContains("label", SOURCE, "absent()"));
		assertThrows(AssertionError.class, () -> SourceAssertions.assertNotContains("label", SOURCE, "stop();"));
		assertThrows(AssertionError.class, () -> SourceAssertions.methodBody(SOURCE, "void third()"));
		assertThrows(AssertionError.class, () -> SourceAssertions.read("does/not/exist.txt"));
	}

	@Test
	void readNormalisesLineEndings() {
		String packetHandler = SourceAssertions.readMainJava("common/network/PacketHandler.java");
		assertTrue(packetHandler.contains("class PacketHandler"));
		assertTrue(!packetHandler.contains("\r\n"));
	}
}
