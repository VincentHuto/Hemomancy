package com.vincenthuto.hemomancy.common.item;

import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * Shared ItemPropertyFunction factories to eliminate repetitive anonymous class
 * definitions in ItemInit.
 */
@OnlyIn(Dist.CLIENT)
public final class HemoItemProperties {

	private HemoItemProperties() {
	}

	/**
	 * Returns an ItemPropertyFunction that reads a boolean NBT tag and returns 1.0
	 * if true, 0.0 otherwise. Used for "open"/"state"/"unsigned" style toggles.
	 *
	 * @param tagKey the NBT boolean key to check (e.g. "state")
	 * @return the reusable property function
	 */
	@SuppressWarnings("deprecation")
	public static ItemPropertyFunction booleanTag(String tagKey) {
		return (stack, world, entity, seed) -> {
			if (stack.has(DataComponents.CUSTOM_DATA)
					&& stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getBoolean(tagKey)) {
				return 1.0F;
			}
			return 0.0F;
		};
	}

	/**
	 * Returns an ItemPropertyFunction that reads an integer NBT tag and returns its
	 * value as a float.
	 *
	 * @param tagKey the NBT int key to read (e.g. "size")
	 * @return the reusable property function
	 */
	@SuppressWarnings("deprecation")
	public static ItemPropertyFunction intTag(String tagKey) {
		return (stack, world, entity, seed) -> {
			if (stack.has(DataComponents.CUSTOM_DATA)) {
				return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(tagKey);
			}
			return 0.0F;
		};
	}
}
