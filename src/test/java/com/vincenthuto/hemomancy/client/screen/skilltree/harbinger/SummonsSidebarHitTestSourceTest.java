package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class SummonsSidebarHitTestSourceTest {

	private SummonsSidebarHitTestSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String view = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabView.java");
		String state = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabState.java");

		assertContains("degree hit testing should account for the full expanded selected group height",
				view, "sy += degreeEntryHeight(entry, state);");
		assertContains("selected degree height should include the trailing gap rendered after summon rows",
				view, "height += entry.getValue().size() * 18 + rowH + 2;");
		assertContains("sidebar content height should match the rendered selected degree height",
				state, "total += rowH + 2;");
	}
}
