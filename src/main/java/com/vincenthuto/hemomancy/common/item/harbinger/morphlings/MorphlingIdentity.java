package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import java.util.UUID;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Stable identity shared by a jar Morphling and its mutable equipped copy. */
public final class MorphlingIdentity {
	public static final String ID_KEY = "MorphlingIdentity";

	private MorphlingIdentity() {
	}

	public static void ensureIdentity(ItemStack stack) {
		if (stack.isEmpty() || !identity(stack).isEmpty()) {
			return;
		}
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putString(ID_KEY, UUID.randomUUID().toString());
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static boolean matches(ItemStack first, ItemStack second) {
		if (first.isEmpty() || second.isEmpty()) {
			return false;
		}
		return matchesIdentity(ItemStack.isSameItem(first, second), identity(first), identity(second));
	}

	static boolean matchesIdentity(boolean sameStrain, String firstId, String secondId) {
		if (!sameStrain) {
			return false;
		}
		if (!firstId.isEmpty() && !secondId.isEmpty()) {
			return firstId.equals(secondId);
		}

		// Existing saves predate per-Morphling identities. Their strain is the only
		// stable link once runtime bonding/hunger data diverges on the equipped copy.
		return true;
	}

	private static String identity(ItemStack stack) {
		if (!stack.has(DataComponents.CUSTOM_DATA)) {
			return "";
		}
		return stack.get(DataComponents.CUSTOM_DATA).copyTag().getString(ID_KEY);
	}
}
