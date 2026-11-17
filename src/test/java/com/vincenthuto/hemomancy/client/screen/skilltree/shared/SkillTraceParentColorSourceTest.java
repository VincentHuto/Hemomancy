package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class SkillTraceParentColorSourceTest {

	private SkillTraceParentColorSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String traceCache = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/shared/SkillTraceLayerCache.java");

		assertContains("trace cache derives connector color from the parent skill", traceCache,
				"int branchColor = parentUnlocked ? branchTraceColor(parent) : dimTraceColor(branchTraceColor(parent));");
		assertNotContains("trace cache must not color connectors from the child skill", traceCache,
				"int branchColor = parentUnlocked ? branchTraceColor(sp) : dimTraceColor(branchTraceColor(sp));");
	}
}
