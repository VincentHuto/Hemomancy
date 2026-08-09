package com.vincenthuto.hemomancy.common.entity.boss.endgame;

/** Pure selection rules for the Evening Star's final, dual-sickle rage. */
public final class VesperRageCombatRules {
	private VesperRageCombatRules() { }

	public static boolean isRageThreshold(float health, float maxHealth) {
		return maxHealth > 0.0F && health / maxHealth < 0.30F;
	}

	public static VesperWeaponAction selectAction(VesperWeaponAction previous, double distance, int variant) {
		VesperWeaponAction primary;
		VesperWeaponAction alternate;
		if (distance <= 5.0D) {
			primary = VesperWeaponAction.SICKLE_CYCLONE;
			alternate = VesperWeaponAction.SICKLE_CROSS_REND;
		} else if (distance > 6.0D) {
			primary = VesperWeaponAction.SICKLE_HOOK;
			alternate = VesperWeaponAction.SICKLE_POUNCE;
		} else if ((variant & 1) == 0) {
			primary = VesperWeaponAction.SICKLE_CROSS_REND;
			alternate = VesperWeaponAction.SANGUINE_CRESCENTS;
		} else {
			primary = VesperWeaponAction.SANGUINE_CRESCENTS;
			alternate = VesperWeaponAction.SICKLE_HOOK;
		}
		return primary == previous ? alternate : primary;
	}

	public static int recoveryTicks() {
		return 3;
	}
}
