package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MaterialIconCacheTest {

	@Test
	void initializationInvokesEachMaterialSupplierOnceAndReadsReuseTheStack() {
		AtomicInteger calls = new AtomicInteger();
		MaterialEntry entry = new MaterialEntry("test", "Test", "", "Test", () -> {
			calls.incrementAndGet();
			return null;
		});
		MaterialIconCache cache = new MaterialIconCache();

		cache.initialize(List.of(entry));
		ItemStack first = cache.get(entry);
		ItemStack second = cache.get(entry);

		assertEquals(1, calls.get());
		assertSame(first, second);
	}
}
