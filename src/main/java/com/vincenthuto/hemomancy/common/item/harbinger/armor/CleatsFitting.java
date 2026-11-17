package com.vincenthuto.hemomancy.common.item.harbinger.armor;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.init.DataComponentInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Hematic Cleats as an Armature fitting (CN-10). The Armature sets a Hematic Cleats item from its bowl into the boots
 * the restrained player wears, if those boots are in {@link #TAKES_CLEATS} (the nine blood-side boots). The boots keep
 * themselves; the fitting is the {@link DataComponentInit#CLEATS} component, which survives repair and is carried
 * across the Armature's own lineage upgrades, and cannot be taken out. Its value is the inner-radial toggle: off means
 * no cling discount, no faster crawl and no spurs drawn. Fitting costs the blood of a hematic iron upgrade, asks no
 * Degree beyond the base Armature, and records no armor-upgrade practice (NQ-05-11).
 */
public final class CleatsFitting {
	public static final TagKey<Item> TAKES_CLEATS = ItemTags.create(Hemomancy.rloc("takes_cleats"));
	/** The blood of {@code iron_to_hematic_iron_boots}. */
	public static final double BLOOD_COST = 250.0D;

	private CleatsFitting() {
	}

	public static boolean isCleats(ItemStack reagent) {
		return reagent.is(ItemInit.hematic_cleats.get());
	}

	public static boolean canFit(ItemStack boots) {
		return boots.is(TAKES_CLEATS) && !isFitted(boots);
	}

	public static boolean isFitted(ItemStack boots) {
		return boots.has(DataComponentInit.CLEATS.get());
	}

	public static boolean isOn(ItemStack boots) {
		return Boolean.TRUE.equals(boots.get(DataComponentInit.CLEATS.get()));
	}

	/** Sets the cleats into {@code boots}, turned on. */
	public static void fit(ItemStack boots) {
		boots.set(DataComponentInit.CLEATS.get(), Boolean.TRUE);
	}

	/** An Armature upgrade's result keeps the cleats, and their toggle, of the boots it replaced. */
	public static void carry(ItemStack from, ItemStack to) {
		Boolean state = from.get(DataComponentInit.CLEATS.get());
		if (state != null && to.is(TAKES_CLEATS)) to.set(DataComponentInit.CLEATS.get(), state);
	}

	/** Whether the worn boots carry cleats that are on; they do nothing until the player clings. */
	public static boolean wornAndOn(Player player) {
		return isOn(player.getItemBySlot(EquipmentSlot.FEET));
	}

	/** Turns the worn boots' cleats on or off. Returns false when the worn boots have none. */
	public static boolean toggle(ServerPlayer player) {
		ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
		if (!isFitted(boots)) return false;
		boolean on = !isOn(boots);
		boots.set(DataComponentInit.CLEATS.get(), on);
		player.displayClientMessage(Component.translatable(on ? "message.hemomancy.cleats.on" : "message.hemomancy.cleats.off")
				.withStyle(ChatFormatting.GRAY), true);
		return true;
	}
}
