package com.vincenthuto.hemomancy.common.item.harbinger.tile.functional;

import java.util.List;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.HouseMarkerBlock;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.block.Block;

/**
 * The Court's house marker as an item: one item for all eight houses, the house carried in the stack's block-state
 * component, which {@link BlockItem} applies when it places. The creative tab lists one stack per house.
 */
public class HouseMarkerBlockItem extends BlockItem {
	public HouseMarkerBlockItem(Block block, Properties properties) {
		super(block, properties);
	}

	/** A marker stack for one house. */
	public static ItemStack forHouse(BlockItem item, CourtHouse house) {
		ItemStack stack = new ItemStack(item);
		stack.set(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY.with(HouseMarkerBlock.HOUSE, house));
		return stack;
	}

	/** The stack's house, or the block's default when it carries none. */
	public static CourtHouse house(ItemStack stack) {
		var properties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
		CourtHouse house = properties.get(HouseMarkerBlock.HOUSE);
		return house == null ? CourtHouse.GIVING_LIGHT : house;
	}

	public static List<ItemStack> creativeStacks(BlockItem item) {
		return java.util.Arrays.stream(CourtHouse.values()).map(house -> forHouse(item, house)).toList();
	}

	@Override
	public Component getName(ItemStack stack) {
		return Component.translatable("block.hemomancy.house_marker.of", house(stack).displayName());
	}
}
