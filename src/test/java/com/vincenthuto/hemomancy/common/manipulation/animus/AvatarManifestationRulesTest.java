package com.vincenthuto.hemomancy.common.manipulation.animus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AvatarManifestationRulesTest {
	@Test
	void formsProgressFromRibcageToCompleteWithoutInvulnerability() {
		assertStats("summon_avatar", 0, .25, 0, 0, 0, 0, 0, 0, 0, 0);
		assertStats("summon_avatar_arms", 1, .25, 4, .5, 0, 0, 0, 1.5, 1, 0);
		assertStats("summon_avatar_armor", 2, .45, 4, .5, 0, 0, 0, 1.5, 1, 0);
		assertStats("summon_avatar_legs", 3, .45, 4, .5, .20, .5, .20, 1.5, 1, 0);
		assertStats("summon_avatar_complete", 4, .65, 8, 1, .35, .75, .35, 4.5, 3, 1);
		assertFalse(AvatarManifestationRules.isAvatarForm("blood_shot"));
		assertTrue(AvatarManifestationRules.stats("blood_shot").isEmpty());
	}

	@Test
	void completeAvatarKeepsTheInnerPlayerAtTheirOriginalVisualScale() {
		var complete = AvatarManifestationRules.stats("summon_avatar_complete").orElseThrow();
		assertEquals(1.0F, complete.playerVisualScale(2.0F));
		assertEquals(1.25F, complete.playerVisualScale(2.5F));
		assertEquals(2.0F, complete.avatarVisualScale());
		assertEquals(0.9F, complete.playerChestLift());

		var arms = AvatarManifestationRules.stats("summon_avatar_arms").orElseThrow();
		assertEquals(1.0F, arms.playerVisualScale(1.0F));
		assertEquals(1.0F, arms.avatarVisualScale());
		assertEquals(0.0F, arms.playerChestLift());
	}

	private static void assertStats(String id, int stage, double reduction, double damage,
			double knockback, double speed, double step, double jump, double blockReach,
			double entityReach, double sizeBonus) {
		var stats = AvatarManifestationRules.stats(id).orElseThrow();
		assertTrue(AvatarManifestationRules.isAvatarForm(id));
		assertEquals(stage, stats.stage());
		assertEquals(reduction, stats.damageReduction());
		assertEquals(damage, stats.attackDamage());
		assertEquals(knockback, stats.attackKnockback());
		assertEquals(speed, stats.movementSpeed());
		assertEquals(step, stats.stepHeight());
		assertEquals(jump, stats.jumpStrength());
		assertEquals(blockReach, stats.blockReach());
		assertEquals(entityReach, stats.entityReach());
		assertEquals(sizeBonus, stats.sizeBonus());
		assertTrue(stats.damageReduction() < 1.0D);
	}
}
