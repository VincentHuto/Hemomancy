package com.vincenthuto.hemomancy.common.armor;

import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustArmorItem;
import com.vincenthuto.hemomancy.common.item.harbinger.armor.BloodLustLineageRules;
import com.vincenthuto.hemomancy.common.util.ArmorSets;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ArmorSetHelper {
	private ArmorSetHelper() {
	}

	public static boolean hasFullHematicIronSet(Player player) {
		return ArmorSets.isFullSet(player, ItemInit.hematic_iron_helm.get(), ItemInit.hematic_iron_chestplate.get(),
				ItemInit.hematic_iron_leggings.get(), ItemInit.hematic_iron_boots.get());
	}

	public static boolean hasFullForkSet(Player player) {
		return hasFullBarbedSet(player) || hasFullChitiniteSet(player) || hasFullPrismaticSet(player);
	}

	public static boolean hasFullBarbedSet(Player player) {
		return ArmorSets.isFullSet(player, ItemInit.barbed_helm.get(), ItemInit.barbed_chestplate.get(),
				ItemInit.barbed_leggings.get(), ItemInit.barbed_boots.get());
	}

	public static boolean hasFullChitiniteSet(Player player) {
		return ArmorSets.isFullSet(player, ItemInit.chitinite_helm.get(), ItemInit.chitinite_chestplate.get(),
				ItemInit.chitinite_leggings.get(), ItemInit.chitinite_boots.get());
	}

	public static boolean hasFullPrismaticSet(Player player) {
		return ArmorSets.isFullSet(player, ItemInit.prismatic_helm.get(), ItemInit.prismatic_chestplate.get(),
				ItemInit.prismatic_leggings.get(), ItemInit.prismatic_boots.get());
	}

	public static boolean hasFullBloodLustSet(Player player) {
		return ArmorSets.isFullSet(player, List.of(
						ItemInit.blood_lust_helm.get(),
						ItemInit.blood_lust_helm_tengu.get(),
						ItemInit.blood_lust_helm_grinning.get(),
						ItemInit.blood_lust_helm_lodestone.get(),
						ItemInit.blood_lust_helm_velorum.get()),
				ItemInit.blood_lust_chest.get(), ItemInit.blood_lust_legs.get(), ItemInit.blood_lust_boots.get());
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
		return ArmorSets.isFullSet(player, ItemInit.silent_archon_helm.get(), ItemInit.silent_archon_chestplate.get(),
				ItemInit.silent_archon_leggings.get(), ItemInit.silent_archon_boots.get());
	}

	public static boolean hasFullEdaciousBloodlust(Player player) {
		return ArmorSets.isFullSet(player, ItemInit.edacious_blood_lust_helm.get(), ItemInit.edacious_blood_lust_chest.get(),
				ItemInit.edacious_blood_lust_legs.get(), ItemInit.edacious_blood_lust_boots.get());
	}

	public static boolean hasFullSheolicBloodlust(Player player) {
		return ArmorSets.isFullSet(player, ItemInit.sheolic_blood_lust_helm.get(), ItemInit.sheolic_blood_lust_chest.get(),
				ItemInit.sheolic_blood_lust_legs.get(), ItemInit.sheolic_blood_lust_boots.get());
	}

	public static boolean hasFullPhantasmalBloodlust(Player player) {
		return ArmorSets.isFullSet(player, ItemInit.phantasmal_blood_lust_helm.get(), ItemInit.phantasmal_blood_lust_chest.get(),
				ItemInit.phantasmal_blood_lust_legs.get(), ItemInit.phantasmal_blood_lust_boots.get());
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
}
