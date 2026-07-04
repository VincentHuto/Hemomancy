package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class WillEquipmentRules {
	private WillEquipmentRules() {
	}

	public static String weaponId(WillOrigin origin, EnumBloodTendency school, int tier) {
		if (origin == WillOrigin.BROKEN && tier <= 1) {
			return "living_staff";
		}
		return switch (school) {
		case ANIMUS -> "living_blade";
		case FLAMMEUS -> "living_torch";
		case DUCTILIS -> "living_crossbow";
		case LUX -> "living_spear";
		case MORTEM -> "living_axe";
		case CONGEATIO -> "living_flail";
		case FERRIC -> "living_staff";
		case TENEBRIS -> "living_baghnakh";
		};
	}

	public static ItemStack createWeaponStack(WillOrigin origin, EnumBloodTendency school, int tier) {
		return new ItemStack(weaponItem(origin, school, tier));
	}

	public static Item weaponItem(WillOrigin origin, EnumBloodTendency school, int tier) {
		return switch (weaponId(origin, school, tier)) {
		case "living_blade" -> ItemInit.living_blade.get();
		case "living_torch" -> ItemInit.living_torch.get();
		case "living_crossbow" -> ItemInit.living_crossbow.get();
		case "living_spear" -> ItemInit.living_spear.get();
		case "living_axe" -> ItemInit.living_axe.get();
		case "living_flail" -> ItemInit.living_flail.get();
		case "living_baghnakh" -> ItemInit.living_baghnakh.get();
		default -> ItemInit.living_staff.get();
		};
	}
}
