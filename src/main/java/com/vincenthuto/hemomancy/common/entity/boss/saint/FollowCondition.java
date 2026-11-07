package com.vincenthuto.hemomancy.common.entity.boss.saint;

import com.vincenthuto.hemomancy.common.entity.boss.BossBands;

import java.util.EnumSet;
import java.util.List;

/** When a follow-up may fire. */
@FunctionalInterface
public interface FollowCondition {
	boolean test(SaintFollowContext c);

	FollowCondition ALWAYS = c -> true;

	/** The move before it landed a hit. */
	static FollowCondition onHit() {
		return SaintFollowContext::hitLanded;
	}

	/** The quarry stands in one of these bands. */
	static FollowCondition bands(BossBands.Band... bands) {
		EnumSet<BossBands.Band> set = EnumSet.noneOf(BossBands.Band.class);
		set.addAll(List.of(bands));
		return c -> set.contains(c.band());
	}

	/** The quarry stands in one of these sectors. */
	static FollowCondition sectors(BossBands.Sector... sectors) {
		EnumSet<BossBands.Sector> set = EnumSet.noneOf(BossBands.Sector.class);
		set.addAll(List.of(sectors));
		return c -> set.contains(c.sector());
	}

	default FollowCondition and(FollowCondition other) {
		return c -> test(c) && other.test(c);
	}
}
