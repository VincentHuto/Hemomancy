package com.vincenthuto.hemomancy.client.render;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * The per-seed render-type holder: the one caching pattern for render types whose shader uniforms change.
 *
 * <p>Each seed's types are built once by the factory, from the seed and a {@link Supplier} it reads the uniforms
 * through, so asking for a seed never builds a type per call or per frame. A seed holds two types. Asking with the
 * uniforms the current one carries returns it again, so its draws batch together; asking with new uniforms moves to the
 * other one and sets them there, so geometry already queued on the first keeps the uniforms it was written with. A
 * buffer source ends a type's batch as soon as a different type is requested, so by the time a type is handed back with
 * new uniforms its earlier geometry has been drawn.
 *
 * <p>Seeds are kept in least-recently-used order up to {@code limit}; an evicted seed is rebuilt when asked for again.
 *
 * @param <K> the seed: whatever keeps one draw's type apart from another's (a shader seed, a texture)
 * @param <U> the uniform values, compared with {@code equals}
 * @param <T> the render type
 */
public final class SeededRenderTypes<K, U, T> {
	private final BiFunction<K, Supplier<U>, T> factory;
	private final Map<K, Seed> seeds;

	public SeededRenderTypes(int limit, BiFunction<K, Supplier<U>, T> factory) {
		this.factory = factory;
		this.seeds = new LinkedHashMap<>(16, 0.75F, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, Seed> eldest) {
				return size() > limit;
			}
		};
	}

	/** The seed's type, carrying {@code uniforms} from now until it is drawn. */
	public T get(K seed, U uniforms) {
		Seed held = seeds.get(seed);
		if (held == null) {
			held = new Seed(seed);
			seeds.put(seed, held);
		}
		return held.carrying(uniforms);
	}

	/** How many seeds are held, for tests. */
	int size() {
		return seeds.size();
	}

	private final class Seed {
		private final K seed;
		private Slot current;
		private Slot spare;

		Seed(K seed) {
			this.seed = seed;
		}

		T carrying(U uniforms) {
			if (current == null) {
				current = new Slot(seed, uniforms);
			} else if (!current.uniforms.equals(uniforms)) {
				Slot next = spare == null ? new Slot(seed, uniforms) : spare;
				next.uniforms = uniforms;
				spare = current;
				current = next;
			}
			return current.type;
		}
	}

	private final class Slot implements Supplier<U> {
		private U uniforms;
		private final T type;

		Slot(K seed, U uniforms) {
			this.uniforms = uniforms;
			this.type = factory.apply(seed, this);
		}

		@Override
		public U get() {
			return uniforms;
		}
	}
}
