package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Every text file the mod ships or documents must be strict UTF-8. Single cp1252 bytes in Markdown broke
 * {@code Files.readString} for every test that reads docs, and double-encoded text reached players through
 * {@code en_us.json}.
 */
class RepositoryTextEncodingTest {
	private static final List<String> ROOTS = List.of("src/main", "src/test", "src/gameTest", "docs", "wiki",
			"tools/model_export");
	private static final Set<String> TEXT_EXTENSIONS = Set.of("java", "json", "md", "mcmeta", "properties", "toml",
			"txt", "fsh", "vsh", "glsl", "py", "yml", "csv", "mjs");

	@Test
	void textFilesAreStrictUtf8() throws Exception {
		List<String> invalid = new ArrayList<>();
		for (String root : ROOTS) {
			Path base = RepoPaths.resolve(root);
			if (!Files.isDirectory(base)) {
				continue;
			}
			try (Stream<Path> files = Files.walk(base)) {
				for (Path file : files.filter(Files::isRegularFile).filter(RepositoryTextEncodingTest::isText).toList()) {
					if (!isUtf8(Files.readAllBytes(file))) {
						invalid.add(RepoPaths.display(file));
					}
				}
			}
		}
		assertEquals(List.of(), invalid);
	}

	private static boolean isText(Path file) {
		String name = file.getFileName().toString();
		int dot = name.lastIndexOf('.');
		return dot >= 0 && TEXT_EXTENSIONS.contains(name.substring(dot + 1).toLowerCase(Locale.ROOT));
	}

	private static boolean isUtf8(byte[] bytes) {
		try {
			StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.decode(ByteBuffer.wrap(bytes));
			return true;
		} catch (CharacterCodingException e) {
			return false;
		}
	}
}
