package com.vincenthuto.hemomancy.client.render;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * GLSL reserves words it may use later, and some drivers (NVIDIA's in particular) refuse to compile a shader that
 * uses one as a name even where {@code glslangValidator} lets it through, as {@code packed} once did in the floor
 * membrane. Every mod shader is scanned for them, with comments removed.
 */
final class ShaderReservedWordTest {
	private static final Path SHADERS = Path.of("src/main/resources/assets/hemomancy/shaders");
	/** Reserved for future use in GLSL 1.50, plus later keywords that drivers enforce in older versions. */
	private static final Set<String> RESERVED = Set.of(
			"common", "partition", "active", "asm", "class", "union", "enum", "typedef", "template", "this",
			"packed", "resource", "goto", "inline", "noinline", "volatile", "public", "static", "extern", "external",
			"interface", "long", "short", "double", "half", "fixed", "unsigned", "superp", "input", "output",
			"hvec2", "hvec3", "hvec4", "dvec2", "dvec3", "dvec4", "fvec2", "fvec3", "fvec4", "filter", "sizeof",
			"cast", "namespace", "using", "sample", "patch", "subroutine", "buffer", "shared", "precise",
			"coherent", "restrict", "readonly", "writeonly");
	private static final Pattern WORD = Pattern.compile("\\b[A-Za-z_][A-Za-z0-9_]*\\b");

	@Test
	void noShaderUsesAReservedWord() throws IOException {
		List<String> found = new ArrayList<>();
		try (Stream<Path> files = Files.walk(SHADERS)) {
			for (Path file : files.filter(ShaderReservedWordTest::isShader).toList()) {
				String[] lines = withoutComments(Files.readString(file)).split("\n", -1);
				for (int line = 0; line < lines.length; line++) {
					Matcher matcher = WORD.matcher(lines[line]);
					while (matcher.find()) {
						if (RESERVED.contains(matcher.group())) {
							found.add(SHADERS.relativize(file) + ":" + (line + 1) + " uses '" + matcher.group() + "'");
						}
					}
				}
			}
		}
		assertTrue(found.isEmpty(), "reserved GLSL words used as names:\n" + String.join("\n", found));
	}

	private static boolean isShader(Path file) {
		String name = file.getFileName().toString();
		return name.endsWith(".fsh") || name.endsWith(".vsh") || name.endsWith(".glsl");
	}

	/** Blanks comments but keeps their newlines, so reported line numbers match the file. */
	private static String withoutComments(String source) {
		StringBuilder result = new StringBuilder(source.length());
		int i = 0;
		while (i < source.length()) {
			if (source.startsWith("//", i)) {
				while (i < source.length() && source.charAt(i) != '\n') i++;
			} else if (source.startsWith("/*", i)) {
				int end = source.indexOf("*/", i + 2);
				int stop = end < 0 ? source.length() : end + 2;
				for (int j = i; j < stop; j++) {
					if (source.charAt(j) == '\n') result.append('\n');
				}
				i = stop;
			} else {
				result.append(source.charAt(i++));
			}
		}
		return result.toString().replace("\r", "");
	}
}
