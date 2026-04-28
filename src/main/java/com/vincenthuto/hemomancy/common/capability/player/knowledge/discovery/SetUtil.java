package com.vincenthuto.hemomancy.common.capability.player.knowledge.discovery;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

final class SetUtil {
	private SetUtil() {
	}

	@SafeVarargs
	static <T> Set<T> of(T... values) {
		LinkedHashSet<T> set = new LinkedHashSet<>();
		Collections.addAll(set, values);
		return Collections.unmodifiableSet(set);
	}
}
