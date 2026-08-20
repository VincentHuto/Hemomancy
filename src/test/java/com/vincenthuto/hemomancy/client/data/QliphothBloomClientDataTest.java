package com.vincenthuto.hemomancy.client.data;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;

class QliphothBloomClientDataTest {
	@Test
	void clearDropsBloomPositionsFromThePreviousWorld() {
		BlockPos center = new BlockPos(12, 64, -8);
		QliphothBloomClientData.set(java.util.List.of(new QliphothBloomClientData.BloomEntry(center, 3)));

		QliphothBloomClientData.clear();

		assertTrue(QliphothBloomClientData.getActiveBlooms().isEmpty());
		assertTrue(!QliphothBloomClientData.containsCenter(center));
	}

	@Test
	void loginAndLogoutClearWorldSpecificBloomData() throws IOException {
		String source = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/event/ClientEvents.java"));

		assertTrue(methodBody(source, "onClientPlayerLogin").contains("QliphothBloomClientData.clear()"));
		assertTrue(methodBody(source, "onClientPlayerLogout").contains("QliphothBloomClientData.clear()"));
	}

	private static String methodBody(String source, String methodName) {
		int start = source.indexOf("void " + methodName);
		int end = source.indexOf("\n    }", start);
		return source.substring(start, end);
	}
}
