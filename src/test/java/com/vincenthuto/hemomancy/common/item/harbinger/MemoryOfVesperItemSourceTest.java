package com.vincenthuto.hemomancy.common.item.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.assertNotContains;
import static com.vincenthuto.hemomancy.support.SourceAssertions.read;

public final class MemoryOfVesperItemSourceTest {

	private MemoryOfVesperItemSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String source = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/MemoryOfVesperItem.java");
		String recipeInit = read("src/main/java/com/vincenthuto/hemomancy/common/init/RecipeInit.java");

		assertContains("memory use override", source, "InteractionResultHolder<ItemStack> use");
		assertContains("memory directs the player to its graft rite", source,
				"hemomancy.memory_of_vesper.rite_guidance");
		assertNotContains("memory no longer awakens directly", source, "progress.awakenVesperMemory()");
		assertNotContains("memory is consumed by the rite instead of item use", source, "stack.shrink(1)");
		assertNotContains("old vesper staff serializer removed", recipeInit, "vesper_staff_upgrade");
	}
}
