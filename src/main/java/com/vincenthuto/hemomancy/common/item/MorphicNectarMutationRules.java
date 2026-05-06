package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.MorphlingItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public final class MorphicNectarMutationRules {
	public static final String MUTATED_KEY = "MorphicNectarMutated";
	public static final int MUTATED_BORDER_COLOR = 0xAA516414;
	public static final int PRIMAL_BORDER_COLOR = 0xDDD13218;
	private static final int PRIMAL_TENDRIL_COUNT = 2;
	private static final int PRIMAL_TENDRIL_FRAME_MILLIS = 140;
	private static final int PRIMAL_TENDRIL_SEGMENTS = 11;
	private static final int[] TENDRIL_COLORS = new int[] {
			0xDDD13218,
			0xCC60791C,
			0xFFE0B536
	};

	private MorphicNectarMutationRules() {
	}

	public static boolean shouldShowMutation(boolean mutated, boolean primal) {
		return mutated || primal;
	}

	public static int borderColor(boolean primal) {
		return primal ? PRIMAL_BORDER_COLOR : MUTATED_BORDER_COLOR;
	}

	public static int tendrilColor(int phase) {
		int index = Math.floorMod(phase, TENDRIL_COLORS.length);
		return TENDRIL_COLORS[index];
	}

	public static int primalTendrilCount() {
		return PRIMAL_TENDRIL_COUNT;
	}

	public static int primalTendrilFrameMillis() {
		return PRIMAL_TENDRIL_FRAME_MILLIS;
	}

	public static int primalTendrilSegments() {
		return PRIMAL_TENDRIL_SEGMENTS;
	}

	public static void markMutated(ItemStack stack) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putBoolean(MUTATED_KEY, true);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean hasMutationMarker(ItemStack stack) {
		return stack.has(DataComponents.CUSTOM_DATA)
				&& stack.get(DataComponents.CUSTOM_DATA).copyTag().getBoolean(MUTATED_KEY);
	}

	public static boolean shouldShowMutation(ItemStack stack) {
		return shouldShowMutation(hasMutationMarker(stack), MorphlingItem.isPrimal(stack));
	}
}
