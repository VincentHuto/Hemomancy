package com.vincenthuto.hemomancy.client.render;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The per-seed holder hands out the same type for the same seed, never builds one per call, and keeps queued geometry's
 * uniforms when the next draw of that seed changes them.
 */
final class SeededRenderTypesTest {
	/** A stand-in render type that remembers the uniforms supplier it was built with. */
	private record Type(int id, Supplier<Float> uniforms) {
	}

	private final List<Type> built = new ArrayList<>();
	private final SeededRenderTypes<Integer, Float, Type> types = new SeededRenderTypes<>(4, (seed, uniforms) -> {
		Type type = new Type(built.size(), uniforms);
		built.add(type);
		return type;
	});

	@Test
	void theSameSeedAndUniformsReturnTheSameType() {
		Type first = types.get(7, 1.5F);
		for (int i = 0; i < 100; i++) assertSame(first, types.get(7, 1.5F));
		assertEquals(1, built.size());
		assertEquals(1.5F, first.uniforms().get());
	}

	@Test
	void changedUniformsMoveToTheSpareAndLeaveQueuedUniformsAlone() {
		Type first = types.get(7, 1.0F);
		Type second = types.get(7, 2.0F);
		assertNotSame(first, second);
		assertEquals(1.0F, first.uniforms().get(), "geometry queued on the first type keeps its uniforms");
		assertEquals(2.0F, second.uniforms().get());
		assertSame(second, types.get(7, 2.0F));
	}

	@Test
	void aSeedNeverBuildsMoreThanTwoTypes() {
		for (int frame = 0; frame < 1000; frame++) {
			Type type = types.get(3, (float) frame);
			assertEquals((float) frame, type.uniforms().get());
		}
		assertEquals(2, built.size());
	}

	@Test
	void seedsAreKeptApart() {
		Type a = types.get(1, 0.5F);
		Type b = types.get(2, 0.5F);
		assertNotSame(a, b);
		assertSame(a, types.get(1, 0.5F));
		assertSame(b, types.get(2, 0.5F));
	}

	@Test
	void theLeastRecentlyUsedSeedIsDroppedPastTheLimit() {
		Type first = types.get(0, 0F);
		for (int seed = 1; seed <= 4; seed++) types.get(seed, 0F);
		assertEquals(4, types.size());
		assertNotSame(first, types.get(0, 0F), "seed 0 was evicted, so it is rebuilt");
	}
}
