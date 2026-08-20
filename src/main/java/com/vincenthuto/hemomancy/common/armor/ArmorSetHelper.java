package com.vincenthuto.hemomancy.common.armor;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustLineageRules;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ArmorSetHelper {
	private ArmorSetHelper() {
	}

	public static boolean hasFullHematicIronSet(Player player) {
		return player != null
				&& wears(player, EquipmentSlot.HEAD, ItemInit.hematic_iron_helm.get())
				&& wears(player, EquipmentSlot.CHEST, ItemInit.hematic_iron_chestplate.get())
				&& wears(player, EquipmentSlot.LEGS, ItemInit.hematic_iron_leggings.get())
				&& wears(player, EquipmentSlot.FEET, ItemInit.hematic_iron_boots.get());
	}

	public static boolean hasFullForkSet(Player player) {
		return hasFullBarbedSet(player) || hasFullChitiniteSet(player) || hasFullPrismaticSet(player);
	}

	public static boolean hasFullBarbedSet(Player player) {
		return player != null
				&& wears(player, EquipmentSlot.HEAD, ItemInit.barbed_helm.get())
				&& wears(player, EquipmentSlot.CHEST, ItemInit.barbed_chestplate.get())
				&& wears(player, EquipmentSlot.LEGS, ItemInit.barbed_leggings.get())
				&& wears(player, EquipmentSlot.FEET, ItemInit.barbed_boots.get());
	}

	public static boolean hasFullChitiniteSet(Player player) {
		return player != null
				&& wears(player, EquipmentSlot.HEAD, ItemInit.chitinite_helm.get())
				&& wears(player, EquipmentSlot.CHEST, ItemInit.chitinite_chestplate.get())
				&& wears(player, EquipmentSlot.LEGS, ItemInit.chitinite_leggings.get())
				&& wears(player, EquipmentSlot.FEET, ItemInit.chitinite_boots.get());
	}

	public static boolean hasFullPrismaticSet(Player player) {
		return player != null
				&& wears(player, EquipmentSlot.HEAD, ItemInit.prismatic_helm.get())
				&& wears(player, EquipmentSlot.CHEST, ItemInit.prismatic_chestplate.get())
				&& wears(player, EquipmentSlot.LEGS, ItemInit.prismatic_leggings.get())
				&& wears(player, EquipmentSlot.FEET, ItemInit.prismatic_boots.get());
	}

	public static boolean hasFullBloodLustSet(Player player) {
		return player != null
				&& wearsAny(player, EquipmentSlot.HEAD,
						ItemInit.blood_lust_helm.get(),
						ItemInit.blood_lust_helm_tengu.get(),
						ItemInit.blood_lust_helm_grinning.get(),
						ItemInit.blood_lust_helm_lodestone.get(),
						ItemInit.blood_lust_helm_velorum.get())
				&& wears(player, EquipmentSlot.CHEST, ItemInit.blood_lust_chest.get())
				&& wears(player, EquipmentSlot.LEGS, ItemInit.blood_lust_legs.get())
				&& wears(player, EquipmentSlot.FEET, ItemInit.blood_lust_boots.get());
	}

	public static String bloodLustLineage(Player player) {
		if (!hasFullBloodLustSet(player)) return "";
		return BloodLustLineageRules.uniformLineage(List.of(
				BloodLustArmorItem.getLineage(player.getItemBySlot(EquipmentSlot.HEAD)),
				BloodLustArmorItem.getLineage(player.getItemBySlot(EquipmentSlot.CHEST)),
				BloodLustArmorItem.getLineage(player.getItemBySlot(EquipmentSlot.LEGS)),
				BloodLustArmorItem.getLineage(player.getItemBySlot(EquipmentSlot.FEET))));
	}

	public static boolean hasFullD7Set(Player player) {
		return hasFullSilentArchonSet(player)
				|| hasFullEdaciousBloodlust(player)
				|| hasFullSheolicBloodlust(player)
				|| hasFullPhantasmalBloodlust(player);
	}

	public static boolean hasFullSilentArchonSet(Player player) {
		return player != null
				&& wears(player, EquipmentSlot.HEAD, ItemInit.silent_archon_helm.get())
				&& wears(player, EquipmentSlot.CHEST, ItemInit.silent_archon_chestplate.get())
				&& wears(player, EquipmentSlot.LEGS, ItemInit.silent_archon_leggings.get())
				&& wears(player, EquipmentSlot.FEET, ItemInit.silent_archon_boots.get());
	}

	public static boolean hasFullEdaciousBloodlust(Player player) {
		return player != null
				&& wears(player, EquipmentSlot.HEAD, ItemInit.edacious_blood_lust_helm.get())
				&& wears(player, EquipmentSlot.CHEST, ItemInit.edacious_blood_lust_chest.get())
				&& wears(player, EquipmentSlot.LEGS, ItemInit.edacious_blood_lust_legs.get())
				&& wears(player, EquipmentSlot.FEET, ItemInit.edacious_blood_lust_boots.get());
	}

	public static boolean hasFullSheolicBloodlust(Player player) {
		return player != null
				&& wears(player, EquipmentSlot.HEAD, ItemInit.sheolic_blood_lust_helm.get())
				&& wears(player, EquipmentSlot.CHEST, ItemInit.sheolic_blood_lust_chest.get())
				&& wears(player, EquipmentSlot.LEGS, ItemInit.sheolic_blood_lust_legs.get())
				&& wears(player, EquipmentSlot.FEET, ItemInit.sheolic_blood_lust_boots.get());
	}

	public static boolean hasFullPhantasmalBloodlust(Player player) {
		return player != null
				&& player.getItemBySlot(EquipmentSlot.HEAD).is(ItemInit.phantasmal_blood_lust_helm.get())
				&& player.getItemBySlot(EquipmentSlot.CHEST).is(ItemInit.phantasmal_blood_lust_chest.get())
				&& player.getItemBySlot(EquipmentSlot.LEGS).is(ItemInit.phantasmal_blood_lust_legs.get())
				&& player.getItemBySlot(EquipmentSlot.FEET).is(ItemInit.phantasmal_blood_lust_boots.get());
	}

	public static boolean isHematicIronArmorPiece(ItemStack stack) {
		return stack.is(ItemInit.hematic_iron_helm.get())
				|| stack.is(ItemInit.hematic_iron_chestplate.get())
				|| stack.is(ItemInit.hematic_iron_leggings.get())
				|| stack.is(ItemInit.hematic_iron_boots.get());
	}

	public static boolean isForkArmorPiece(ItemStack stack) {
		return stack.is(ItemInit.barbed_helm.get())
				|| stack.is(ItemInit.barbed_chestplate.get())
				|| stack.is(ItemInit.barbed_leggings.get())
				|| stack.is(ItemInit.barbed_boots.get())
				|| stack.is(ItemInit.chitinite_helm.get())
				|| stack.is(ItemInit.chitinite_chestplate.get())
				|| stack.is(ItemInit.chitinite_leggings.get())
				|| stack.is(ItemInit.chitinite_boots.get())
				|| stack.is(ItemInit.prismatic_helm.get())
				|| stack.is(ItemInit.prismatic_chestplate.get())
				|| stack.is(ItemInit.prismatic_leggings.get())
				|| stack.is(ItemInit.prismatic_boots.get());
	}

	public static boolean isBloodLustArmorPiece(ItemStack stack) {
		return stack.is(ItemInit.blood_lust_helm.get())
				|| stack.is(ItemInit.blood_lust_helm_tengu.get())
				|| stack.is(ItemInit.blood_lust_helm_grinning.get())
				|| stack.is(ItemInit.blood_lust_helm_lodestone.get())
				|| stack.is(ItemInit.blood_lust_helm_velorum.get())
				|| stack.is(ItemInit.blood_lust_chest.get())
				|| stack.is(ItemInit.blood_lust_legs.get())
				|| stack.is(ItemInit.blood_lust_boots.get());
	}

	public static boolean isD7ArmorPiece(ItemStack stack) {
		return stack.is(ItemInit.silent_archon_helm.get())
				|| stack.is(ItemInit.silent_archon_chestplate.get())
				|| stack.is(ItemInit.silent_archon_leggings.get())
				|| stack.is(ItemInit.silent_archon_boots.get())
				|| stack.is(ItemInit.edacious_blood_lust_helm.get())
				|| stack.is(ItemInit.edacious_blood_lust_chest.get())
				|| stack.is(ItemInit.edacious_blood_lust_legs.get())
				|| stack.is(ItemInit.edacious_blood_lust_boots.get())
				|| stack.is(ItemInit.sheolic_blood_lust_helm.get())
				|| stack.is(ItemInit.sheolic_blood_lust_chest.get())
				|| stack.is(ItemInit.sheolic_blood_lust_legs.get())
				|| stack.is(ItemInit.sheolic_blood_lust_boots.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_helm.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_chest.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_legs.get())
				|| stack.is(ItemInit.phantasmal_blood_lust_boots.get());
	}

	private static boolean wears(Player player, EquipmentSlot slot, Item item) {
		return player.getItemBySlot(slot).is(item);
	}

	private static boolean wearsAny(Player player, EquipmentSlot slot, Item... items) {
		ItemStack worn = player.getItemBySlot(slot);
		for (Item item : items) {
			if (worn.is(item)) {
				return true;
			}
		}
		return false;
	}
}
