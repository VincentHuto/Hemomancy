package com.vincenthuto.hemomancy.common.item.armor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ArmorItemStackSizeSourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy/common/item");

	private ArmorItemStackSizeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		List<Path> armorItems = armorItemSources();
		if (armorItems.isEmpty()) {
			throw new AssertionError("expected at least one ArmorItem subclass under " + SOURCE_ROOT);
		}

		List<String> missingStackLimit = new ArrayList<>();
		for (Path sourcePath : armorItems) {
			String source = Files.readString(sourcePath);
			if (!source.contains("stacksTo(1)")) {
				missingStackLimit.add(SOURCE_ROOT.relativize(sourcePath).toString());
			}
		}

		if (!missingStackLimit.isEmpty()) {
			throw new AssertionError("ArmorItem subclasses must set stacksTo(1): " + missingStackLimit);
		}
	}

	private static List<Path> armorItemSources() throws IOException {
		try (Stream<Path> files = Files.walk(SOURCE_ROOT)) {
			return files
					.filter(path -> path.toString().endsWith(".java"))
					.filter(ArmorItemStackSizeSourceTest::extendsArmorItem)
					.toList();
		}
	}

	private static boolean extendsArmorItem(Path path) {
		try {
			return Files.readString(path).contains("extends ArmorItem");
		} catch (IOException e) {
			throw new IllegalStateException("Could not read " + path, e);
		}
	}
}
