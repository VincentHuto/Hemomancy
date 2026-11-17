package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

public final class LivingStaffWeaponFormRules {
	public static final double BASE_HOT_SWAP_COST_ML = 250.0D;
	public static final double HOT_SWAP_DISCOUNT_PER_LEVEL_ML = 50.0D;
	public static final double MIN_HOT_SWAP_COST_ML = 50.0D;
	public static final int WEAPONS_MASTER_MAX_LEVEL = 4;
	/** Temper a swap into a weapon form burns in that form's primary school (CB-25). */
	public static final float HOT_SWAP_TEMPER = 1.0F;

	public static final String CONJURE_BLADE = "conjure_blade";
	public static final String CONJURE_AXE = "conjure_axe";
	public static final String CONJURE_SPEAR = "conjure_spear";
	public static final String CONJURE_CLAWS = "conjure_claws";
	public static final String CONJURE_CROSSBOW = "conjure_crossbow";
	public static final String CONJURE_TORCH = "conjure_torch";
	public static final String CONJURE_FLAIL = "conjure_flail";
	public static final String CONJURE_SICKLE = "conjure_sickle";
	/** The ninth form, a pickaxe (core-loop Track 8): learned with the bond, not grafted. */
	public static final String CONJURE_PICKAXE = "conjure_pickaxe";

	private LivingStaffWeaponFormRules() {
	}

	public static boolean isStaffWeaponFormManip(String manipName) {
		return switch (manipName) {
			case CONJURE_BLADE, CONJURE_AXE, CONJURE_SPEAR, CONJURE_CLAWS, CONJURE_CROSSBOW,
					CONJURE_TORCH, CONJURE_FLAIL, CONJURE_SICKLE, CONJURE_PICKAXE -> true;
			default -> false;
		};
	}

	/** The forms that dig: they follow the Staff's ring tier and pay blood for each block they break. */
	public static boolean isDiggingForm(String manipName) {
		return CONJURE_AXE.equals(manipName) || CONJURE_PICKAXE.equals(manipName);
	}

	public static boolean shouldToggleOffSelectedForm(String selectedManipName, String currentFormName) {
		return isStaffWeaponFormManip(selectedManipName) && selectedManipName.equals(currentFormName);
	}

	public static boolean shouldEquipPairedOffhandClaw(String formName, boolean offhandEmpty) {
		return CONJURE_CLAWS.equals(formName) && offhandEmpty;
	}

	public static boolean isPairedClawForm(String mainHandFormName, String offhandFormName) {
		return CONJURE_CLAWS.equals(mainHandFormName) && CONJURE_CLAWS.equals(offhandFormName);
	}

	public static double hotSwapCostForWeaponsMasterLevel(int weaponsMasterLevel) {
		return hotSwapCost(weaponsMasterLevel, 0);
	}

	/** The hot swap's price for a Weapons Master level and a Staff that has laid {@code ring} growth rings. */
	public static double hotSwapCost(int weaponsMasterLevel, int ring) {
		int level = Math.max(0, Math.min(WEAPONS_MASTER_MAX_LEVEL, weaponsMasterLevel));
		return Math.max(LivingStaffGrowthRules.hotSwapFloor(ring),
				BASE_HOT_SWAP_COST_ML - HOT_SWAP_DISCOUNT_PER_LEVEL_ML * level
						- LivingStaffGrowthRules.hotSwapRingDiscount(ring));
	}
}
