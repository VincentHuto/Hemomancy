package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class SummonsDetailSourceTest {

	private SummonsDetailSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String view = read("src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/SummonsTabView.java");

		assertContains("summons details should use stacked wrapped detail rows",
				view, "drawStackedDetailLine(gfx, ctx");
		assertContains("summons details should indent values without a fixed right column",
				view, "DETAIL_VALUE_INDENT");
		assertContains("summons details should give values the remaining full panel width",
				view, "int valueW = Math.max(24, totalW - DETAIL_VALUE_INDENT)");
		assertContains("summons details should wrap long values below their labels",
				view, "drawWrappedValue(gfx, ctx, valueText, x + DETAIL_VALUE_INDENT");
		assertNotContains("summons details should not draw values at the old fixed offset",
				view, "x + 78, y");
		assertNotContains("summons details should not reserve the old label column",
				view, "DETAIL_LABEL_W");
		assertNotContains("summons details should not shift values into the panel edge",
				view, "x + DETAIL_LABEL_W + DETAIL_COLUMN_GAP");
	}
}
