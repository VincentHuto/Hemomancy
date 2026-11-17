package com.vincenthuto.hemomancy.common.init;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class GourdStewBowlReturnSourceTest {
	private static final Path ITEM_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/ItemInit.java");

	private GourdStewBowlReturnSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String itemInit = read(ITEM_INIT);
		String gourdStewRegistration = itemInit.substring(itemInit.indexOf("gourd_stew = BASEITEMS.register"),
				itemInit.indexOf("sanguine_formation = BASEITEMS.register"));

		assertContains("gourd stew should keep bowl as crafting remainder", gourdStewRegistration,
				"craftRemainder(Items.BOWL)");
		assertContains("gourd stew should return a bowl after being eaten", gourdStewRegistration,
				"usingConvertsTo(Items.BOWL)");
	}
}
