package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * W-1: template-pool structures extend {@code HemoJigsawStructure}, the one copy of the jigsaw codec and of the
 * {@code JigsawPlacement.addPieces} call. The Dynasty castle (assembled from NBT in code) and the Vagrant Mind
 * (block-by-block piece) are not jigsaw structures and keep their own codecs.
 */
class JigsawStructureGuardrailTest {
	@Test
	void onlyHemoJigsawStructurePlacesJigsawPieces() throws Exception {
		List<String> placers = new ArrayList<>();
		List<String> codecCopies = new ArrayList<>();
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			for (Path file : files.filter(path -> path.toString().endsWith(".java")).sorted().toList()) {
				String source = SourceAssertions.read(file);
				String shown = RepoPaths.display(file);
				if (shown.endsWith("/HemoJigsawStructure.java")) continue;
				if (source.contains("JigsawPlacement.addPieces(")) placers.add(shown);
				if (source.contains("fieldOf(\"start_pool\")")) codecCopies.add(shown);
			}
		}
		assertEquals(List.of(), placers, "Extend HemoJigsawStructure and place through addPieces");
		assertEquals(List.of(), codecCopies, "Use HemoJigsawStructure.codec");
	}
}
