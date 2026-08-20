package com.vincenthuto.hemomancy.common.tile.crafting;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;

class SomaticLoomMissingEnzymeFeedbackTest {
	@Test
	void missingEnzymeMessageNamesTheItemAndDeficit() {
		Method formatter = assertDoesNotThrow(() -> SomaticLoomBlockEntity.class.getDeclaredMethod(
				"missingEnzymeMessage", Component.class, int.class, int.class));
		formatter.setAccessible(true);
		Component message = assertDoesNotThrow(() -> (Component) formatter.invoke(
				null, Component.literal("Frigid Enzyme"), 1, 3));

		assertEquals("Missing 2 × Frigid Enzyme (1/3 stored).", message.getString());
	}

	@Test
	void insertingCatalystReportsAnyStillMissingEnzyme() throws Exception {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/common/tile/crafting/SomaticLoomBlockEntity.java"));
		int start = source.indexOf("void placeCatalyst");
		int end = source.indexOf("\n\t}", start);

		org.junit.jupiter.api.Assertions.assertTrue(
				source.substring(start, end).contains("provideMissingEnzymeFeedback(player)"));
	}
}
