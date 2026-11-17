package com.vincenthuto.hemomancy.common.rite.unstained;

import org.junit.jupiter.api.Test;

public final class LetheanBaptismCanonSourceTest {
	private LetheanBaptismCanonSourceTest() {}

	@Test
	void allChecksPass() {
		var ready = UnstainedRitePreflight.check("cardinal_rite/lethean_baptism",
				new UnstainedRitePreflight.State(true, true, false, false, false, false, false, false, false, false, false, 0.0f, false));
		var repeated = UnstainedRitePreflight.check("cardinal_rite/lethean_baptism",
				new UnstainedRitePreflight.State(true, true, true, false, false, false, false, false, false, false, false, 0.0f, false));
		if (!ready.success()) throw new AssertionError("first Baptism must be accepted after suppression");
		if (repeated.success() || repeated.failure() != UnstainedRitePreflight.Failure.BAPTISM_ALREADY_COMPLETE) {
			throw new AssertionError("a completed Baptism must not commit a second time");
		}
	}
}
