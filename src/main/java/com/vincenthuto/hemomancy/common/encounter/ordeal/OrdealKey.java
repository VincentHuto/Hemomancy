package com.vincenthuto.hemomancy.common.encounter.ordeal;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusApprenticeshipProgress;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.harbinger.BloodVialItem;
import com.vincenthuto.hemomancy.common.mission.shared.OwedRewards;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * The key to a professor's mind (2026-11-17): a Blood Vial of their own blood, given with the lesson and taken by the
 * ordeal's brazier. Only their gift fills it, so its custom data is exactly the professor's entity type and
 * {@code troupe_key}; the rite's ingredient matches that strictly, and a vial drawn any other way does not fit.
 */
public final class OrdealKey {
	public static final String TROUPE_KEY = "troupe_key";
	private static final long DAY = 24000L;

	private OrdealKey() {
	}

	public static CompoundTag keyData(String role) {
		CompoundTag tag = new CompoundTag();
		tag.putString(BloodVialItem.TAG_ENTITY_TYPE, "hemomancy:" + role);
		tag.putBoolean(TROUPE_KEY, true);
		return tag;
	}

	public static boolean isKeyData(CompoundTag tag, String role) {
		return tag.getBoolean(TROUPE_KEY) && ("hemomancy:" + role).equals(tag.getString(BloodVialItem.TAG_ENTITY_TYPE));
	}

	public static ItemStack vial(String role) {
		ItemStack stack = new ItemStack(ItemInit.bloody_vial.get());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(keyData(role)));
		return stack;
	}

	public static boolean is(ItemStack stack, String role) {
		return stack.is(ItemInit.bloody_vial.get())
				&& isKeyData(stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag(), role);
	}

	/** Gives the key through the owed-rewards claim and records the day, so another comes only on a later day. */
	public static void give(ServerPlayer player, String role) {
		CircusApprenticeshipProgress.setOrdealKeyDay(player, role, today(player));
		OwedRewards.give(player, java.util.List.of(vial(role)));
	}

	/** After a failed ordeal: whether the professor will give another today. */
	public static boolean mayRegive(ServerPlayer player, String role) {
		return OrdealRules.keyRegivenToday(CircusApprenticeshipProgress.ordealKeyDay(player, role), today(player));
	}

	private static long today(ServerPlayer player) {
		return player.level().getDayTime() / DAY;
	}
}
