package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingStaffWeaponFormRules {
	public static final double BASE_HOT_SWAP_COST_ML = 250.0D;
	public static final double HOT_SWAP_DISCOUNT_PER_LEVEL_ML = 50.0D;
	public static final double MIN_HOT_SWAP_COST_ML = 50.0D;
	public static final int WEAPONS_MASTER_MAX_LEVEL = 4;

	public static final String CONJURE_BLADE = "conjure_blade";
	public static final String CONJURE_AXE = "conjure_axe";
	public static final String CONJURE_SPEAR = "conjure_spear";
	public static final String CONJURE_CLAWS = "conjure_claws";
	public static final String CONJURE_CROSSBOW = "conjure_crossbow";
	public static final String CONJURE_TORCH = "conjure_torch";
	public static final String CONJURE_FLAIL = "conjure_flail";

	private LivingStaffWeaponFormRules() {
	}

	public static boolean isStaffWeaponFormManip(String manipName) {
		return switch (manipName) {
			case CONJURE_BLADE, CONJURE_AXE, CONJURE_SPEAR, CONJURE_CLAWS, CONJURE_CROSSBOW,
					CONJURE_TORCH, CONJURE_FLAIL -> true;
			default -> false;
		};
	}

	public static boolean shouldToggleOffSelectedForm(String selectedManipName, String currentFormName) {
		return isStaffWeaponFormManip(selectedManipName) && selectedManipName.equals(currentFormName);
	}

	public static double hotSwapCostForWeaponsMasterLevel(int weaponsMasterLevel) {
		int level = Math.max(0, Math.min(WEAPONS_MASTER_MAX_LEVEL, weaponsMasterLevel));
		return Math.max(MIN_HOT_SWAP_COST_ML,
				BASE_HOT_SWAP_COST_ML - HOT_SWAP_DISCOUNT_PER_LEVEL_ML * level);
	}
}
