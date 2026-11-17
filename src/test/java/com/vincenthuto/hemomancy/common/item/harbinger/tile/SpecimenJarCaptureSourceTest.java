package com.vincenthuto.hemomancy.common.item.harbinger.tile;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;

public final class SpecimenJarCaptureSourceTest {
	private static final Path SOURCE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tile/functional/SpecimenJarBlockItem.java");

	private SpecimenJarCaptureSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String source = Files.readString(SOURCE).replace("\r\n", "\n");
		assertContains("single jar capture replaces the hand before the source stack is emptied",
				source, "if (!player.getAbilities().instabuild && stack.getCount() <= 1) {\n\t\t\tplayer.setItemInHand(hand, filled);\n\t\t}");
		String tag = Files.readString(Path.of(
				"src/main/resources/data/hemomancy/tags/entity_type/specimen_jar_capturable.json"));
		assertContains("venom rib centipedes are capturable as specimens", tag, "\"hemomancy:venom_rib_centipede\"");
		assertContains("the jar reads the capturable tag", source, "is(EntityInit.SPECIMEN_JAR_CAPTURABLE)");
		assertNotContains("single jar capture must not shrink to empty before setting the filled hand stack",
				source, "stack.shrink(1);\n\t\t}\n\t\tif (stack.isEmpty())");
	}
}
