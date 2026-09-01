package com.vincenthuto.hemomancy.common.init;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HematicIronSwordAttributesSourceTest {
	@Test
	void hematicIronSwordRegistersSwordAttackAttributes() throws IOException {
		String itemInit = Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java"))
				.replaceAll("\\s+", "");

		assertTrue(itemInit.contains("newSwordItem(EnumModToolTiers.HEMATIC_IRON,"
				+ "newItem.Properties().attributes(SwordItem.createAttributes(EnumModToolTiers.HEMATIC_IRON,3.0F,-2.4F)))"));
	}
}
