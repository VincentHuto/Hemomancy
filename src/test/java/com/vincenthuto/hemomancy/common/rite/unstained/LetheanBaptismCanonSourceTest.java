package com.vincenthuto.hemomancy.common.rite.unstained;

public final class LetheanBaptismCanonSourceTest {
	private LetheanBaptismCanonSourceTest() {}

	public static void main(String[] args) {
		var ready = UnstainedRitePreflight.check("cardinal_rite/lethean_baptism",
				new UnstainedRitePreflight.State(true, true, false, false, false, false, false, false, false, false));
		var repeated = UnstainedRitePreflight.check("cardinal_rite/lethean_baptism",
				new UnstainedRitePreflight.State(true, true, true, false, false, false, false, false, false, false));
		if (!ready.success()) throw new AssertionError("first Baptism must be accepted after suppression");
		if (repeated.success() || repeated.failure() != UnstainedRitePreflight.Failure.BAPTISM_ALREADY_COMPLETE) {
			throw new AssertionError("a completed Baptism must not commit a second time");
		}
	}
}
