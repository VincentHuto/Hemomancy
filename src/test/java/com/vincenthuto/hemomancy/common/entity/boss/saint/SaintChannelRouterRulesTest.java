package com.vincenthuto.hemomancy.common.entity.boss.saint;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The channel router's pure rules: aim by reach and cone, the nearest Saint asked first, a refusal stopping the item. */
final class SaintChannelRouterRulesTest {
	private static final Vec3 EYE = new Vec3(0, 65.6, 0);
	private static final Vec3 LOOK = new Vec3(0, 0, 1);

	@Test
	void aimsAtByReachAndCone() {
		assertTrue(SaintChannelRouter.aimsAt(EYE, LOOK, new Vec3(0, 65.6, 5), 6.0D, 20.0D));
		assertFalse(SaintChannelRouter.aimsAt(EYE, LOOK, new Vec3(0, 65.6, 6.5), 6.0D, 20.0D), "out of reach");
		assertTrue(SaintChannelRouter.aimsAt(EYE, LOOK, new Vec3(0.8, 65.6, 5), 6.0D, 20.0D), "inside the cone");
		assertFalse(SaintChannelRouter.aimsAt(EYE, LOOK, new Vec3(2.5, 65.6, 5), 6.0D, 20.0D), "outside the cone");
		assertFalse(SaintChannelRouter.aimsAt(EYE, LOOK, new Vec3(0, 65.6, -3), 6.0D, 180.0D), "behind is never aimed at");
	}

	@Test
	void theFirstHandledResultWinsInOrder() {
		List<String> asked = new ArrayList<>();
		SaintChannelRouter.Result taken = SaintChannelRouter.Result.taken(4.0D);
		SaintChannelRouter.Result result = SaintChannelRouter.first(List.of("near", "far"), saint -> {
			asked.add(saint);
			return saint.equals("near") ? taken : SaintChannelRouter.Result.PASS;
		});
		assertSame(taken, result);
		assertEquals(List.of("near"), asked, "the nearest Saint answers first and the rest are not asked");
		asked.clear();
		assertSame(SaintChannelRouter.Result.PASS, SaintChannelRouter.first(List.of("legacy", "passing"), saint -> {
			asked.add(saint);
			return SaintChannelRouter.Result.PASS;
		}));
		assertEquals(List.of("legacy", "passing"), asked);
	}

	@Test
	void aRefusalIsHandledSoTheItemsOwnPathsStop() {
		SaintChannelRouter.Result refused = SaintChannelRouter.Result.refused(null);
		assertTrue(refused.handled());
		assertEquals(0.0D, refused.blood());
		assertFalse(SaintChannelRouter.Result.PASS.handled());
		assertSame(refused, SaintChannelRouter.first(List.of(1, 2), i -> i == 1 ? refused : SaintChannelRouter.Result.taken(3)));
		assertEquals(0.0D, SaintChannelRouter.Result.taken(-2).blood(), "never negative blood");
	}
}
